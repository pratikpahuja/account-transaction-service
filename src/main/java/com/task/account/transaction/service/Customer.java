package com.task.account.transaction.service;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
  @Min(value = 0, message = "customerId: cannot be less than 0")
  private long customerId;
  @NotNull(message = "tenant: cannot be null")
  @NotEmpty(message = "tenant: cannot be empty")
  private String tenant;
}
