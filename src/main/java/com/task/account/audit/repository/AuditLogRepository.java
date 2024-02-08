package com.task.account.audit.repository;

import com.task.account.audit.domain.AuditLog;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends CrudRepository<AuditLog, Long> {

  @Query("FROM AuditLog al ORDER BY al.performedAt DESC")
  List<AuditLog> findAllOrderByPerformedAtDesc(Limit limit);

}
