package com.miotech.mdp.flow.entity.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class ArgumentInfo {

    @ApiModelProperty(value="parameter key name", notes = "should be unique",required = true)
    private String parameterKey;

    @ApiModelProperty(value="parameter value", required = true)
    private String parameterValue;
}
