package com.task.account.transaction.service;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Transaction {
  private String service;
  private BigDecimal amount;
  TransactionType type;
}
