package com.khabalita.sistemacomercial.Repositories;

import com.khabalita.sistemacomercial.Entities.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogRepository extends BaseRepository<AuditLog, Long> {
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
