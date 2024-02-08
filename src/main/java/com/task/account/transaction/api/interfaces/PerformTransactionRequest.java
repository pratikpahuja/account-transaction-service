package com.task.account.transaction.api.interfaces;

import com.task.account.transaction.service.Customer;
import com.task.account.transaction.service.TransactionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class PerformTransactionRequest {
  @NotNull(message = "service: cannot be null")
  @NotEmpty(message = "service: cannot be empty")
  @Size(max = 100, message = "service: cannot have more than 100 chars")
  String service;
  @NotNull(message = "message: cannot be null")
  @Min(value = 0, message = "message: cannot be less than 0")
  BigDecimal amount;
  @NotNull(message = "type: cannot be null")
  TransactionType type;
  @Valid
  @NotNull(message = "customer: cannot be null")
  Customer customer;
}
