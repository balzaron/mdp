package com.miotech.mdp.flow.entity.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class FlowExecution {
    @ApiModelProperty(value = "Exclusion task ids")
    private String[] excludeTasks;

    @ApiModelProperty(value = "Task ids if only need run specific tasks in flow order")
    private String[] includeTasks;
}
