package com.task.account.audit.domain;

import com.task.account.transaction.domain.CustomerEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class AuditLog {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_log_id_generator")
  @SequenceGenerator(name = "audit_log_id_generator", sequenceName = "audit_log_id_seq", allocationSize = 1)
  @EqualsAndHashCode.Exclude
  Long id;
  String operationName;
  String data;
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "customer_tenant_id")
  CustomerEntity customer;
  Instant performedAt;
}
