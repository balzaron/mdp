package com.miotech.mdp.common.persistent;

import com.miotech.mdp.common.model.dao.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, String> {
}
