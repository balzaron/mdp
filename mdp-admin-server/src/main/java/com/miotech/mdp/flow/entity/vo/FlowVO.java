package com.miotech.mdp.flow.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.miotech.mdp.flow.entity.dao.FlowTask;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FlowVO {

    private String id;

    private String name;

    private String description;

    private String airflowAddress;

    private Boolean enableParallel;

    private Boolean enableScheduler;

    private String executionScheduler;

    private Integer retryPolicy;

    private List<FlowTask> flowTasks;

    private List<String> tags;

    private String[] tagIds;

    private String[] userIds;

    private FlowState state;

    private String creatorId;

    @JsonFormat(timezone = "GMT+0", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createTime;

    @JsonFormat(timezone = "GMT+0", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updateTime;
}
