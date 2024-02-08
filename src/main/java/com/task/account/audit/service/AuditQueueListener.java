package com.task.account.audit.service;

import com.task.account.audit.domain.AuditLog;
import com.task.account.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import static com.task.account.audit.service.AuditQueueConstants.AUDIT_QUEUE_NAME;

@Component
@RequiredArgsConstructor
public class AuditQueueListener {
  private final AuditLogRepository repository;

  @JmsListener(destination = AUDIT_QUEUE_NAME)
  public void saveMessage(AuditLog message) {
    repository.save(message);
  }
}
