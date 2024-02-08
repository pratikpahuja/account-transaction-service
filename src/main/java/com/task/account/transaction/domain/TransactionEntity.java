package com.task.account.transaction.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "transaction")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tx_id_generator")
  @SequenceGenerator(name = "tx_id_generator", sequenceName = "transaction_id_seq", allocationSize = 1)
  @EqualsAndHashCode.Exclude
  long id;
  String service;
  BigDecimal amount;
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "customer_tenant_id")
  CustomerEntity customer;
  @Enumerated(EnumType.STRING)
  @Builder.Default
  TransactionState state = TransactionState.ACTIVE;
  Long rolledBackByTransactionId;
  Long rollingBackTransactionId;
  Instant createdAt;

  public Instant getCreatedAt() {
    if (this.createdAt == null)
      return null;

    return this.createdAt.truncatedTo(ChronoUnit.SECONDS);
  }

  public BigDecimal getAmount() {
    if (this.amount == null)
      return null;

    return this.amount.setScale(2, RoundingMode.DOWN);
  }
}
