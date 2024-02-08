package com.task.account.transaction.service.exception;

import com.task.account.transaction.domain.CustomerEntity;

public class TransactionNotPresentException extends RuntimeException {
  public TransactionNotPresentException(CustomerEntity customer, long transactionId) {
    super(STR."No transaction found for tenant: \{customer.getTenant().getName()}, customer id: \{customer.getCustomerId()}, transactionId: \{transactionId}");
  }
}
