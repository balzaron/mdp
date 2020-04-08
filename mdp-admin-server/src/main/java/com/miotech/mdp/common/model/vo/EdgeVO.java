package com.miotech.mdp.common.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class EdgeVO {

    private String id;

    @ApiModelProperty(required = true, value = "edge input id")
    private String inputId;

    @ApiModelProperty(required = true, value = "edge output id")
    private String outputId;

}
