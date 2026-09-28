package com.khabalita.sistemacomercial.audit;

import com.khabalita.sistemacomercial.Entities.AuditLog;
import com.khabalita.sistemacomercial.Repositories.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest request;

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void record(String action, String entityType, String entityId, String details) {
        record(action, entityType, entityId, details, "SUCCESS");
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void record(String action, String entityType, String entityId, String details, String outcome) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication == null || !authentication.isAuthenticated()
                ? "anonymous" : authentication.getName();
        auditLogRepository.save(AuditLog.builder()
                .username(username)
                .action(action)
                .outcome(outcome)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(request.getRemoteAddr())
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> findRecent(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
