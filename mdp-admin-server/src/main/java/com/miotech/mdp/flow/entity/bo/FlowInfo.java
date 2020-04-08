package com.miotech.mdp.flow.entity.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@ApiModel
public class FlowInfo {

    @ApiModelProperty(value="flow name", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value="Flow description", required = false)
    private String description;

    @ApiModelProperty(value="User id array for notifying when flow state change", notes = "Default is the creator")
    private String[] userIds;

    @ApiModelProperty(value="Crontab Expression for scheduling")
    private String executionScheduler;

    @ApiModelProperty(value="Whether enable scheduling", notes = "Default is false")
    private Boolean enableScheduler;

    @ApiModelProperty(value="Whether enable parallel execution", notes = "Default is false")
    private Boolean enableParallel;

    @ApiModelProperty(value="Retries when flow execution failed or error")
    private Integer retryPolicy;

    private String[] tags;
}
