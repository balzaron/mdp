package com.miotech.mdp.flow.entity.bo;

import com.miotech.mdp.common.model.bo.PageInfo;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel
public class FlowRunSearch extends PageInfo {

    private String state;

    private String flowId;

    private String flowRunId;
}