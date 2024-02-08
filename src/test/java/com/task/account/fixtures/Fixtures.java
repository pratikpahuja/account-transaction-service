package com.task.account.fixtures;

import com.task.account.transaction.domain.CustomerEntity;
import com.task.account.transaction.domain.TenantEntity;
import com.task.account.transaction.domain.TransactionEntity;
import com.task.account.transaction.service.Customer;
import com.task.account.transaction.service.Transaction;
import com.task.account.transaction.service.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;

public class Fixtures {

  public static Transaction sampleTransaction() {
    return Transaction.builder()
      .amount(BigDecimal.valueOf(23.23))
      .service("test-service")
      .type(TransactionType.PAY_IN)
      .build();
  }

  public static TransactionEntity sampleTransactionEntity(CustomerEntity customerEntity, Instant createdAt) {
    return TransactionEntity.builder()
      .id(randomIdentifier())
      .amount(BigDecimal.valueOf(23.23))
      .service("test-service")
      .customer(customerEntity)
      .createdAt(createdAt)
      .build();
  }

  public static TransactionEntity sampleTransactionEntityWithoutId(CustomerEntity customerEntity, Instant createdAt) {
    return TransactionEntity.builder()
      .amount(BigDecimal.valueOf(23.23))
      .service("test-service")
      .customer(customerEntity)
      .createdAt(createdAt)
      .build();
  }

  public static Customer sampleCustomer(long customerId, String tenant) {
    return Customer.builder()
      .customerId(customerId)
      .tenant(tenant)
      .build();
  }

  public static CustomerEntity sampleCustomerEntity(long customerId, String tenant) {
    return CustomerEntity.builder()
      .id(randomIdentifier())
      .customerId(customerId)
      .tenant(sampleTenantEntity(tenant))
      .build();
  }

  public static TenantEntity sampleTenantEntity(String tenant) {
    return TenantEntity.builder()
      .id(randomIdentifier())
      .name(tenant)
      .build();
  }

  public static long randomIdentifier() {
    return new Random().nextLong();
  }

}
