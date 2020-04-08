package com.miotech.mdp.table.model.dao;

import com.google.gson.JsonObject;
import com.miotech.mdp.common.util.JSONUtil;
import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "metabase_database", schema = "public", catalog = "mdp")
public class MetabaseDatabaseEntity {

    @Basic
    @Id
    @Column(name = "id")
    private Integer id;

    @Basic
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Basic
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @Basic
    @Column(name = "name")
    public String name;

    @Basic
    @Column(name = "description")
    private String description;

    @Basic
    @Column(name = "engine")
    private String engine;

    @Basic
    @Column(name = "details")
    private String details;

    public JsonObject getDetails() {
        return JSONUtil.stringToJson(details);
    }

    @Basic
    @Column(name = "is_full_sync")
    private Boolean isFullSync;

    @Basic
    @Column(name = "timezone")
    private String timezone;

}
