package com.miotech.mdp.table.model.dao;

import com.miotech.mdp.common.model.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "meta_field", schema = "public", catalog = "mdp")

public class MetaFieldEntity extends BaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "base_type")
    private String baseType;

    @Column(name = "special_type")
    private String specialType;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "description")
    private String description;

    @Column(name = "position")
    private int position;

    @Column(name = "visibility_type")
    private String visibilityType;

    @Column(name = "fk_target_field_id")
    private Integer fkTargetFieldId;

    @Column(name = "last_analyzed")
    private LocalDateTime lastAnalyzed;

    @Column(name = "fingerprint")
    private String fingerprint;

    @Column(name = "fingerprint_Version")
    private int fingerprintVersion;

    @Column(name = "database_type")
    private String databaseType;

    @Column(name = "has_field_values")
    private String hasFieldValues;

    @Column(name = "settings")
    private String settings;

    @Column(name = "is_key_field")
    private Boolean isKeyField = false;

    @Column(name = "is_nullable")
    private Boolean isNullable = true;

    @Column(name = "is_unique")
    private Boolean isUnique = false;

    @Column(name = "table_id")
    private String tableId;

    @ManyToOne
    @JoinColumn(name = "table_id", insertable = false, updatable = false)
    private MetaTableEntity table;

}
