package com.task.account.transaction.service.exception;

import com.task.account.transaction.domain.CustomerEntity;

public class RollbackFailedException extends RuntimeException {
  public RollbackFailedException(CustomerEntity customer, long transactionId) {
    super(STR."Rollback failed for transaction found with tenant: \{customer.getTenant().getName()}, customer id: \{customer.getCustomerId()}, transactionId: \{transactionId}");
  }

  public RollbackFailedException(String message, CustomerEntity customer, long transactionId) {
    super(STR."Rollback failed for transaction found with tenant: \{customer.getTenant().getName()}, customer id: \{customer.getCustomerId()}, transactionId: \{transactionId}. Message: " + message);
  }
}
