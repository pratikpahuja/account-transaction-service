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
public class RollbackTransactionRequest {
  @Valid
  @NotNull(message = "customer: cannot be null")
  Customer customer;
}
