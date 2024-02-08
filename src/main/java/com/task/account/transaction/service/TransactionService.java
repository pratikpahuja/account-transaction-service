package com.task.account.transaction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.account.audit.domain.AuditLog;
import com.task.account.transaction.domain.CustomerEntity;
import com.task.account.transaction.domain.TransactionEntity;
import com.task.account.transaction.domain.TransactionState;
import com.task.account.transaction.repository.CustomerRepository;
import com.task.account.transaction.repository.TransactionRepository;
import com.task.account.transaction.service.exception.CustomerNotPresentException;
import com.task.account.transaction.service.exception.RollbackFailedException;
import com.task.account.transaction.service.exception.TransactionNotPresentException;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static com.task.account.audit.service.AuditQueueConstants.AUDIT_QUEUE_NAME;
import static java.time.Instant.now;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

  private final CustomerRepository customerRepository;
  private final TransactionRepository transactionRepository;
  private final Clock clock;
  private final JmsTemplate jmsTemplate;
  private final ObjectMapper objectMapper;
  private final EntityManager entityManager;

  @Transactional
  public TransactionEntity postTransaction(Customer customer, Transaction transaction) {
    var customerEntity = getCustomerOrFail(customer);

    var transactionEntity = transactionRepository.save(mapToEntity(customerEntity, transaction));
    customerRepository.updateAccountBalance(customerEntity.getId(), transactionEntity.getAmount());

    jmsTemplate.convertAndSend(AUDIT_QUEUE_NAME, createAuditLog("POST_TRANSACTION", customerEntity, transactionEntity.getCreatedAt(), auditDataForTransaction(transactionEntity)));

    //To remove cached customer entity
    entityManager.refresh(transactionEntity.getCustomer());

    return transactionRepository.findById(transactionEntity.getId()).get();
  }

  public List<TransactionEntity> fetchLatestTransactions(Customer customer, int limit) {
    var customerEntity = getCustomerOrFail(customer);

    jmsTemplate.convertAndSend(AUDIT_QUEUE_NAME, createAuditLog("FETCH_TRANSACTIONs", customerEntity, now(clock), ""));
    return transactionRepository.findByCustomerOrderByCreatedAtDesc(customerEntity, Limit.of(limit));
  }

  @Transactional
  public void rollbackTransaction(Customer customer, long transactionId) {
    var customerEntity = getCustomerOrFail(customer);
    var transactionToRollback = getTransactionOrFail(transactionId, customerEntity);

    validateTransactionState(transactionToRollback, customerEntity);

    performRollback(transactionToRollback, customerEntity);
    customerRepository.updateAccountBalance(customerEntity.getId(), transactionToRollback.getAmount().negate());

    jmsTemplate.convertAndSend(AUDIT_QUEUE_NAME, createAuditLog("ROLLBACK_TRANSACTION", customerEntity, now(clock), auditDataForTransaction(transactionToRollback)));
  }

  private void performRollback(TransactionEntity transactionToRollback, CustomerEntity customerEntity) {
    transactionRepository.markTransactionRollingBack(transactionToRollback.getId());
    var transactionRolledBackById = transactionRepository.rollbackTransaction(transactionToRollback.getId(), now(clock));

    transactionRolledBackById
      .ifPresentOrElse(
        id -> transactionRepository.markTransactionRolledBack(transactionToRollback.getId(), id)
        , () -> {
          throw new RollbackFailedException(customerEntity, transactionToRollback.getId());
        });
  }

  private static void validateTransactionState(TransactionEntity transactionToRollback, CustomerEntity customerEntity) {
    if (transactionToRollback.getState() != TransactionState.ACTIVE) {
      throw new RollbackFailedException("Transaction is not in the right state to rollback", customerEntity, transactionToRollback.getId());
    }
  }

  private TransactionEntity getTransactionOrFail(long transactionId, CustomerEntity customerEntity) {
    return transactionRepository.findById(transactionId)
      .orElseThrow(() -> new TransactionNotPresentException(customerEntity, transactionId));
  }

  private TransactionEntity mapToEntity(CustomerEntity customerEntity, Transaction transaction) {
    return TransactionEntity.builder()
      .service(transaction.getService())
      .customer(customerEntity)
      .amount(getAmount(transaction))
      .createdAt(now(clock))
      .build();

  }

  private static BigDecimal getAmount(Transaction transaction) {
    return isPayIn(transaction)
      ? transaction.getAmount()
      : transaction.getAmount().negate();
  }

  private static boolean isPayIn(Transaction transaction) {
    return transaction.getType() == TransactionType.PAY_IN;
  }

  private CustomerEntity getCustomerOrFail(Customer customer) {
    return customerRepository.findByIdAndTenant(customer.getCustomerId(), customer.getTenant())
      .orElseThrow(() -> new CustomerNotPresentException(customer.getCustomerId(), customer.getTenant()));
  }

  private AuditLog createAuditLog(String operationName, CustomerEntity customer, Instant performedAt, String auditData) {
    return AuditLog.builder()
      .operationName(operationName)
      .performedAt(performedAt)
      .customer(customer)
      .data(auditData)
      .build();
  }

  private String auditDataForTransaction(TransactionEntity transaction) {
    try {
      return objectMapper.writeValueAsString(new TransactionAuditData(transaction.getId(), transaction.getService(), transaction.getAmount()));
    } catch (JsonProcessingException e) {
      log.error("Exception raised while creating audit data for transaction.");
      return "";
    }
  }

  private record TransactionAuditData(long id, String service, BigDecimal amount) {}
}
