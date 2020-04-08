package com.miotech.mdp.flow.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.miotech.mdp.flow.constant.FlowState;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeltaUpdate {
    private String deltaPoint;

    private String deltaSource;

    private String deltaStep;

    private String deltaRequired;

    @JsonFormat(timezone = "GMT+0", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime runStartTime;

    @JsonFormat(timezone = "GMT+0", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime runEndTime;

    private FlowState runStatus;

    private String errorMessage;

    private String deltaCheckPoint;

    private String watchTableId;

    private String watchTableFullName;

    private Integer deltaCount;
}
