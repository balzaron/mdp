package com.miotech.mdp.flow.entity.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.miotech.mdp.common.model.BaseEntity;
import com.miotech.mdp.common.model.dao.UsersEntity;
import com.miotech.mdp.flow.constant.TaskConfig;
import com.miotech.mdp.flow.constant.TaskState;
import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;
import org.json.simple.JSONObject;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Entity
@Data
@Transactional
@Table(name = "tasks", schema = "public")
@JsonIgnoreProperties(value = {"createTime", "updateTime"})
public class TaskInstance extends BaseEntity {

    @Column(name ="name" , columnDefinition = "varchar(50)")
    @JsonFormat
    private String name;

    @Type(type = "jsonb")
    @Column(name ="info" , columnDefinition = "jsonb")
    @JsonIgnore
    private JSONObject info = new JSONObject();

    @Column(name ="state" , columnDefinition = "varchar(50)")
    @JsonFormat
    @Enumerated(EnumType.STRING)
    private TaskState state;

    @Column(name ="error_reason" , columnDefinition = "text")
    @JsonFormat
    private String errorReason;

    @Column(name ="max_retry")
    @JsonIgnore
    private Integer maxRetry;

    @Column(name ="try_number")
    @JsonIgnore
    private Integer tryNumber;

    @Column(name ="creator_id" , columnDefinition = "varchar(50)")
    private String creatorId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id", updatable = false, insertable = false)
    @JsonIgnore
    @LazyCollection(LazyCollectionOption.FALSE)
    private UsersEntity creator;

    @Column(name = "start_time")
    @JsonFormat(timezone = "GMT+0", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    @JsonFormat(timezone = "GMT+0", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime endTime;

    @Column(name = "flow_id", columnDefinition = "varchar(50)")
    private String flowId;

    @Column(name = "flow_task_id", columnDefinition = "varchar(50)",
            insertable=false, updatable = false)
    private String flowTaskId;

    @Column(name = "flow_run_id", columnDefinition = "varchar(50)",
            insertable=false, updatable = false)
    private String flowRunId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flow_task_id")
    @JsonIgnore
    @LazyCollection(LazyCollectionOption.FALSE)
    private FlowTask flowTask;

    @ManyToOne()
    @JoinColumn(name = "flow_run_id")
    @JsonIgnore
    private FlowRun flowRun;

    @JsonRawValue
    public JSONObject getConfig() {
        return flowTask.getConfig();
    }

    /********* Instance Metod *********/

    public void clear() {
        this.errorReason = null;
        this.info = new JSONObject();
    }

    public void increaseTry() {
        if (tryNumber == null || tryNumber <= 0) tryNumber = 1;
        else {
            tryNumber = tryNumber + 1;
        }
    }

    public String getLogPath() {
        String loggingDir = System.getProperty("user.dir") + "/logs";
        if (info.get(TaskConfig.LOGGIN_DIR) != null){
            loggingDir = (String) info.get(TaskConfig.LOGGIN_DIR);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(loggingDir);
        if (flowRun != null) {
            builder.append("/");
            builder.append(flowRun.getId());
        }
        if (flowTask != null) {
            builder.append("/");
            builder.append(flowTask.getId());
        }
        builder.append("/");
        builder.append(getId());
        builder.append(".log");
        return builder.toString();
    }

    @JsonIgnore
    public Boolean isRunning() {
        return TaskState.RUNNING_STATES.contains(state);
    }

    @JsonIgnore
    public Boolean isEnded() {
        if (isUpForRetry()) return false;
        return TaskState.END_STATES.contains(state);
    }

    @JsonIgnore
    public Boolean isFailed() {
        return TaskState.FAILED_STATES.contains(state);
    }

    @JsonIgnore
    public Boolean isUpForRetry() {
        return TaskState.UP_FOR_RETRY_STATES
                .contains(state)
                    && maxRetry > tryNumber;
    }

}
