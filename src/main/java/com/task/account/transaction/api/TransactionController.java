package com.task.account.transaction.api;

import com.task.account.transaction.api.interfaces.PerformTransactionRequest;
import com.task.account.transaction.api.interfaces.RollbackTransactionRequest;
import com.task.account.transaction.domain.TransactionEntity;
import com.task.account.transaction.service.Customer;
import com.task.account.transaction.service.Transaction;
import com.task.account.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.util.List;

import static java.time.Instant.now;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

  private final TransactionService service;
  private final Clock clock;

  @PostMapping
  TransactionEntity postTransaction(@RequestBody @Validated PerformTransactionRequest request) {
    return service.postTransaction(request.getCustomer(), mapToTransaction(request));
  }

  @GetMapping
  List<TransactionEntity> getTransactions(
    @RequestParam String tenant,
    @RequestParam int customerId,
    @RequestParam int count) {
    var customer = Customer.builder()
      .tenant(tenant)
      .customerId(customerId)
      .build();
    return service.fetchLatestTransactions(customer, count);
  }

  @PostMapping("/{transactionId}/rollback")
  ResponseEntity<?> rollbackTransaction(
    @PathVariable long transactionId,
    @RequestBody RollbackTransactionRequest request) {
    service.rollbackTransaction(request.getCustomer(), transactionId);
    return ResponseEntity
      .noContent()
      .build();
  }

  private Transaction mapToTransaction(PerformTransactionRequest request) {
    return Transaction.builder()
      .service(request.getService())
      .amount(request.getAmount())
      .type(request.getType())
      .build();
  }
}
