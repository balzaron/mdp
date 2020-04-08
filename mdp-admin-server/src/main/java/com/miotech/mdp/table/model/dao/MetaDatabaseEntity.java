package com.miotech.mdp.table.model.dao;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "meta_database", schema = "public", catalog = "mdp")
@Data
public class MetaDatabaseEntity {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "details")
    private String details;

    @Column(name = "engine")
    private String engine;

    @Column(name = "is_full_sync")
    private boolean isFullSync;

    @Column(name = "metadata_sync_schedule")
    private String metadataSyncSchedule;

    @Column(name = "cache_field_values_schedule")
    private String cacheFieldValuesSchedule;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "options")
    private String options;

}
