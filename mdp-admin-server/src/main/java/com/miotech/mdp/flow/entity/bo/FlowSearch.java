package com.miotech.mdp.flow.entity.bo;

import com.miotech.mdp.common.model.bo.PageInfo;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel
public class FlowSearch extends PageInfo {

    private String[] tags;

    private String name;

}