package com.task.account.transaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.account.config.JMSConfig;
import com.task.account.config.JacksonConfig;
import com.task.account.config.TestClockConfig;
import com.task.account.transaction.domain.CustomerEntity;
import com.task.account.transaction.repository.CustomerRepository;
import com.task.account.transaction.repository.TransactionRepository;
import com.task.account.transaction.service.exception.CustomerNotPresentException;
import com.task.account.transaction.service.exception.TransactionNotPresentException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Limit;
import org.springframework.jms.core.JmsTemplate;

import java.time.Clock;
import java.util.List;

import static java.time.Instant.now;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.task.account.fixtures.Fixtures.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

  TransactionService service;
  CustomerRepository customerRepository;
  TransactionRepository transactionRepository;
  private Clock clock;
  private JmsTemplate jmsTemplate;
  private ObjectMapper mapper;
  private EntityManager entityManager;

  @BeforeEach
  void setup() {
    customerRepository = mock(CustomerRepository.class);
    transactionRepository = mock(TransactionRepository.class);
    clock = TestClockConfig.CLOCK;
    jmsTemplate = mock(JmsTemplate.class);
    mapper = new JacksonConfig().objectMapper();
    entityManager = mock(EntityManager.class);

    service = new TransactionService(customerRepository, transactionRepository, clock, jmsTemplate, mapper, entityManager);
  }

  @Test
  void postTransactionWhenCustomerNotPresent() {
    makeCustomerNotPresent();

    assertThrows(CustomerNotPresentException.class, () -> service.postTransaction(sampleCustomer(1, "test-tenant"), sampleTransaction()));
  }

  @Test
  void postTransaction() {
    var createdAt = now(clock);
    var customerEntity = makeCustomerPresent();
    var sampleTransactionEntity = sampleTransactionEntity(customerEntity, createdAt);
    when(transactionRepository.save(sampleTransactionEntity)).thenReturn(sampleTransactionEntity);
    when(transactionRepository.findById(sampleTransactionEntity.getId())).thenReturn(of(sampleTransactionEntity));

    var result = service.postTransaction(sampleCustomer(1, "test-tenant"), sampleTransaction());

    verify(customerRepository).findByIdAndTenant(1, "test-tenant");
    verify(transactionRepository).save(sampleTransactionEntity);
    verify(transactionRepository).findById(sampleTransactionEntity.getId());
    assertThat(result, is(sampleTransactionEntity));
  }

  @Test
  void postTransactionWhenPayOut() {
    var createdAt = now(clock);
    var customerEntity = makeCustomerPresent();

    var sampleTransactionEntity = sampleTransactionEntity(customerEntity, createdAt);
    sampleTransactionEntity.setAmount(sampleTransactionEntity.getAmount().negate());
    when(transactionRepository.save(sampleTransactionEntity)).thenReturn(sampleTransactionEntity);
    when(transactionRepository.findById(sampleTransactionEntity.getId())).thenReturn(of(sampleTransactionEntity));

    var requestedTransaction = sampleTransaction();
    requestedTransaction.setType(TransactionType.PAY_OUT);
    var result = service.postTransaction(sampleCustomer(1, "test-tenant"), requestedTransaction);

    verify(customerRepository).findByIdAndTenant(1, "test-tenant");
    verify(transactionRepository).save(sampleTransactionEntity);
    assertThat(result, is(sampleTransactionEntity));
  }

  @Test
  void testFetchLatestTransactionsWhenCustomerNotPresent() {
    makeCustomerNotPresent();

    assertThrows(CustomerNotPresentException.class, () -> service.fetchLatestTransactions(sampleCustomer(1, "test-tenant"), 10));
  }

  @Test
  void testFetchLatestTransactionsWhenNoTransactions() {
    var customerEntity = makeCustomerPresent();
    when(transactionRepository.findByCustomerOrderByCreatedAtDesc(customerEntity, Limit.of(10)))
      .thenReturn(List.of(
        sampleTransactionEntity(customerEntity, now(clock).minusSeconds(60)),
        sampleTransactionEntity(customerEntity, now(clock).minusSeconds(120))
      ));


    var result = service.fetchLatestTransactions(sampleCustomer(1, "test-tenant"), 10);

    verify(transactionRepository).findByCustomerOrderByCreatedAtDesc(customerEntity, Limit.of(10));
    assertThat(result, hasItems(
      sampleTransactionEntity(customerEntity, now(clock).minusSeconds(120)),
      sampleTransactionEntity(customerEntity, now(clock).minusSeconds(60))
    ));
  }

  @Test
  void testRollbackWhenCustomerNotPresent() {
    makeCustomerNotPresent();

    assertThrows(CustomerNotPresentException.class, () -> service.rollbackTransaction(sampleCustomer(1, "test-tenant"), 100L));
  }

  @Test
  void testRollbackWhenTransactionNotPresent() {
    var customerEntity = makeCustomerPresent();
    assertThrows(TransactionNotPresentException.class, () -> service.rollbackTransaction(sampleCustomer(1, "test-tenant"), 100L));
  }

  private CustomerEntity makeCustomerPresent() {
    var customerEntity = sampleCustomerEntity(1, "test-tenant");
    when(customerRepository.findByIdAndTenant(1, "test-tenant")).thenReturn(of(customerEntity));

    return customerEntity;
  }

  private void makeCustomerNotPresent() {
    when(customerRepository.findByIdAndTenant(1, "test-tenant")).thenReturn(empty());
  }
}