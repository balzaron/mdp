package com.miotech.mdp.common.model.dao;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log", schema = "public", catalog = "mdp")
@Data
@GenericGenerator(name = "custom_id", strategy = "com.miotech.mdp.common.jpa.CustomIdentifierGenerator")
public class AuditLogEntity {

    @Id
    @GeneratedValue(generator = "custom_id")
    @Column(name = "id")
    private String id;

    @Column(name = "topic")
    private String topic;

    @Column(name = "timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "user_id")
    private String userId;

    @Column(name = "model")
    private String model;

    @Column(name = "model_id")
    private String modelId;

    @Column(name = "custom_id")
    private String customId;

    @Column(name = "details")
    private String details;
}
