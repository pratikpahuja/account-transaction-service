package com.task.account.transaction.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "customer_tenant")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEntity {

  @Id
  private Long id;
  private Long customerId;
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "tenant_id")
  private TenantEntity tenant;
  private BigDecimal accountBalance;
}
