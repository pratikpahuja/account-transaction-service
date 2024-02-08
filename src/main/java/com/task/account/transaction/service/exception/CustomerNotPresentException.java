package com.task.account.transaction.service.exception;

public class CustomerNotPresentException extends RuntimeException {
  public CustomerNotPresentException(long customerId, String tenant) {
    super(STR."No customer found for tenant: \{tenant}, customer id: \{customerId}");
  }
}
