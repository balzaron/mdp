package com.miotech.mdp.flow.entity.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class FlowCopyOption {
    @ApiModelProperty(required = true)
    private String flowId;

    @ApiModelProperty
    private String[] includedTasks;

    @ApiModelProperty
    private Boolean syncFlow;
}
