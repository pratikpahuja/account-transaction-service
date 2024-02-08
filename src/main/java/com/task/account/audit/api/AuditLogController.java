package com.task.account.audit.api;

import com.task.account.audit.domain.AuditLog;
import com.task.account.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

  private final AuditLogRepository auditLogRepository;

  @GetMapping
  Iterable<AuditLog> getAuditLogs(@RequestParam int count) {
    return auditLogRepository.findAllOrderByPerformedAtDesc(Limit.of(count));
  }
}
