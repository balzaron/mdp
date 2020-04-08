package com.miotech.mdp.flow.entity.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel
public class ParameterInfo {
    @ApiModelProperty(value="parameter id")
    private String id;

    @ApiModelProperty(value="parameter type",
            required = true,
            allowableValues = "choices,string,number,date,text")
    private String parameterType;

    @ApiModelProperty(value="parameter key name",
            required = true,
            notes = "Should be in the camelCase format and unique for single operator")
    private String parameterKey;

    @ApiModelProperty(value="If parameterType is choices and choice options are loading from a valid remote http/https url")
    private String choiceUrl;

    @ApiModelProperty(value="If parameterType is choices, provide choice options")
    private String[] choices;

    @ApiModelProperty(value="Default value for this parameter")
    private String defaultValue;
}
