package com.miotech.mdp.flow.entity.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class FlowList {
    @ApiModelProperty
    private String[] flowIds;
}
