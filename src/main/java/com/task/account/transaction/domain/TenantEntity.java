package com.task.account.transaction.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tenant")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantEntity {
  @Id
  private Long id;
  private String name;
}
