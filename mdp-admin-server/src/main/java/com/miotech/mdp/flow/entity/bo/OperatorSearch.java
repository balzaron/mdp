package com.miotech.mdp.flow.entity.bo;

import com.miotech.mdp.common.model.bo.PageInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class OperatorSearch extends PageInfo {

    @ApiModelProperty("Operator tags")
    private String[] tags;

    @ApiModelProperty("Operator name")
    private String name;
}