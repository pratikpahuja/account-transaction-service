package com.task.account.transaction.repository;

import com.task.account.config.TestClockConfig;
import com.task.account.transaction.domain.CustomerEntity;
import com.task.account.transaction.domain.TransactionEntity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Limit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Clock;

import static com.task.account.fixtures.Fixtures.sampleTransactionEntityWithoutId;
import static java.time.Instant.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
  "spring.datasource.url=jdbc:postgresql://localhost/transaction_test_db",
  "spring.liquibase:.contexts=tests"
})
@ActiveProfiles("test")
@Import(TestClockConfig.class)
class TransactionRepositoryTest {

  @Autowired TransactionRepository repository;
  @Autowired CustomerRepository customerRepository;
  @Autowired Clock clock;
  @Autowired JdbcTemplate jdbcTemplate;

  @Test
  void testSaveWithoutCustomer() {
    var t = TransactionEntity.builder()
      .service("test-service")
      .amount(BigDecimal.valueOf(123.23))
      .createdAt(now(clock))
      .build();
    assertThrows(DataIntegrityViolationException.class, () -> repository.save(t));
  }

  @Test
  @Transactional
  void testSave() {
    var customerEntity = customerRepository.findByIdAndTenant(100, "tenant1");
    deleteTransactionsFor(customerEntity.get());

    var t = TransactionEntity.builder()
      .service("test-service")
      .amount(BigDecimal.valueOf(123.23))
      .createdAt(now())
      .customer(customerEntity.get())
      .build();
    repository.save(t);
  }

  @Test
  @Transactional
  void testFindByCustomerOrderByCreatedAtDesc() {
    var customerEntity = customerRepository.findByIdAndTenant(100, "tenant1");
    deleteTransactionsFor(customerEntity.get());

    repository.save(sampleTransactionEntityWithoutId(customerEntity.get(), now(clock).minusSeconds(120)));
    repository.save(sampleTransactionEntityWithoutId(customerEntity.get(), now(clock).minusSeconds(60)));
    repository.save(sampleTransactionEntityWithoutId(customerEntity.get(), now(clock).minusSeconds(80)));

    var result = repository.findByCustomerOrderByCreatedAtDesc(customerEntity.get(), Limit.of(10));

    assertThat(result, contains(
      sampleTransactionEntityWithoutId(customerEntity.get(), now(clock).minusSeconds(60)),
      sampleTransactionEntityWithoutId(customerEntity.get(), now(clock).minusSeconds(80)),
      sampleTransactionEntityWithoutId(customerEntity.get(), now(clock).minusSeconds(120))
    ));
  }

  @Test
  @Transactional
  void testLimitFindByCustomerOrderByCreatedAtDesc() {
    var customerEntity = customerRepository.findByIdAndTenant(100, "tenant1");
    deleteTransactionsFor(customerEntity.get());

    repository.save(sampleTransactionEntityWithoutId(customerEntity.get(), now(clock).minusSeconds(120)));
    repository.save(sampleTransactionEntityWithoutId(customerEntity.get(), now(clock).minusSeconds(60)));
    repository.save(sampleTransactionEntityWithoutId(customerEntity.get(), now(clock).minusSeconds(80)));

    var result = repository.findByCustomerOrderByCreatedAtDesc(customerEntity.get(), Limit.of(2));

    assertThat(result, contains(
      sampleTransactionEntityWithoutId(customerEntity.get(), now(clock).minusSeconds(60)),
      sampleTransactionEntityWithoutId(customerEntity.get(), now(clock).minusSeconds(80))
    ));
  }

  @Test
  void testRollbackTransaction() {
    var customerEntity = customerRepository.findByIdAndTenant(100, "tenant1");
    deleteTransactionsFor(customerEntity.get());

    var transactionToRollback = repository.save(sampleTransactionEntityWithoutId(customerEntity.get(), now(clock)));

    repository.markTransactionRollingBack(transactionToRollback.getId());
    var transactionRolledBackById = repository.rollbackTransaction(transactionToRollback.getId(), now(clock).plusSeconds(300));
    repository.markTransactionRolledBack(transactionToRollback.getId(), transactionRolledBackById.get());
  }

  void deleteTransactionsFor(CustomerEntity customerEntity) {
    jdbcTemplate.execute(STR."DELETE FROM transaction WHERE customer_tenant_id=\{customerEntity.getId()}");
  }
}