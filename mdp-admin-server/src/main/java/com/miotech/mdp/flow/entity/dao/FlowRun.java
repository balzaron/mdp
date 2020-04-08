package com.miotech.mdp.flow.entity.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.miotech.mdp.common.model.LogicDeleteEntity;
import com.miotech.mdp.flow.constant.FlowState;
import lombok.Data;
import org.hibernate.annotations.*;
import org.json.simple.JSONObject;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Transactional
@SQLDelete(sql = "update flow_run set deleted = 1 where id = ?")
@Where(clause = "deleted = 0")
@Table(name = "flow_run", schema = "public")
public class FlowRun extends LogicDeleteEntity {

    @Type(type="jsonb")
    @Column(name ="conf" , columnDefinition = "jsonb")
    @JsonRawValue
    private JSONObject config = new JSONObject();

    @Column(name ="state" , columnDefinition = "varchar(50)")
    @JsonFormat
    @Enumerated(EnumType.STRING)
    private FlowState state;

    @Column(name ="last_running_stage")
    @JsonFormat
    private Integer lastRunningStage;

    @Column(name = "start_time")
    @JsonFormat(timezone = "GMT+0", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    @JsonFormat(timezone = "GMT+0", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime endTime;

    @Column(name ="creator_id" , columnDefinition = "varchar(50)")
    private String creatorId;

    @Column(name ="flow_id" , columnDefinition = "varchar(50)")
    @JsonIgnore
    private String flowId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flow_id", insertable = false, updatable = false)
    @JsonIgnore
    @LazyCollection(LazyCollectionOption.FALSE)
    private Flow flow;

    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "flowRun")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<TaskInstance> taskSet = new ArrayList<>();

}
