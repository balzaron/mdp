package com.miotech.mdp.flow.entity.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@ApiModel
@Data
public class FlowTaskInfo {

    @ApiModelProperty(value="Flow task Name", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value="Flow id", required = true)
    private String flowId;

    @ApiModelProperty(value="Operator id", required = false)
    private String operatorId;

    @ApiModelProperty(value="Test case id", required = false)
    private String testId;

    @ApiModelProperty(value="Upstream parent task ids",
            notes = "If not specified, the task would be the root level task")
    private String[] parentTaskIds;

    @ApiModelProperty(value="Arguments to the operator",
            notes = "parameterKey should be unique")
    private List<ArgumentInfo> arguments;

    @ApiModelProperty(value="Triger Rule for the task ",
            required = true,
            allowableValues = "all_success,one_success,one_failed")
    private String triggerRule;

    @ApiModelProperty(value="Max Retry if task failed, Default using flow retryPolicy",
            required = false)
    private Integer retryPolicy = 0;
}
