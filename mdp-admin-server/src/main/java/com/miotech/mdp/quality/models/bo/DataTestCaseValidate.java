package com.miotech.mdp.quality.models.bo;

import com.miotech.mdp.quality.models.entity.CaseValidator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


@Data
@ApiModel
public class DataTestCaseValidate {

    @ApiModelProperty(value="Test case sql", required = true)
    private String caseSql;

    @ApiModelProperty(value = "Test validate database id", required = true)
    private Integer dbId;

    private List<CaseValidator> validateObject;

}
