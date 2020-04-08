package com.miotech.mdp.flow.entity.dao;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.miotech.mdp.common.model.LogicDeleteEntity;
import com.miotech.mdp.common.model.dao.TagsEntity;
import lombok.Data;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Transactional
@SQLDelete(sql = "update flow set deleted = 1 where id = ?")
@Where(clause = "deleted = 0")
@Table(name = "flow", schema = "public")
public class Flow extends LogicDeleteEntity {

    @Column(name ="name", columnDefinition = "varchar(50)")
    @NotNull
    @JsonFormat
    private String name;

    @Column(name ="description", columnDefinition = "text")
    private String description;

    @Column(name="airflow_address", columnDefinition = "text")
    private String airflowAddress;

    @Type(type = "string-array")
    @Column(name="user_ids", columnDefinition = "varchar(50) []")
    private String[] userIds;

    @Column(name="execution_scheduler", columnDefinition = "varchar(50)")
    private String executionScheduler;

    @Column(name="enable_scheduler")
    private Boolean enableScheduler = false;

    @Column(name="enable_parallel")
    private Boolean enableParallel = false;

    @Column(name="retry_policy")
    private Integer retryPolicy = 0;

    @Column(name ="creator_id" , columnDefinition = "varchar(50)")
    @JsonIgnore
    private String creatorId;

    @Column(name="_ts_query", columnDefinition = "tsvector",
            insertable = false, updatable = false)
    @JsonIgnore
    private String tsQuery;

    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            mappedBy = "flow")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<FlowTask> flowTasks = new ArrayList<>();

    @Type(type = "string-array")
    @Column(name="tag_ids", columnDefinition = "varchar(50) []")
    private String[] tagIds;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "flow_tags_ref",
            joinColumns = {@JoinColumn(name = "flow_id", referencedColumnName = "id", insertable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "tag_id", referencedColumnName = "id", insertable = false, updatable = false)}
    )
    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonIgnore
    private List<TagsEntity> tags;

    /********* Instance Metod *********/
    public static Flow emptyFlow() {
        Flow flow = new Flow();
        flow.setName(null);
        flow.setDescription(null);
        flow.setUserIds(null);
        flow.setRetryPolicy(null);
        flow.setExecutionScheduler(null);
        flow.setEnableParallel(null);
        flow.setEnableScheduler(null);
        flow.setCreateTime(null);
        flow.setUpdateTime(null);
        return flow;
    }
}
