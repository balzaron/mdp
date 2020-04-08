package com.miotech.mdp.graph.ontology.model.vo;

import com.miotech.mdp.common.model.vo.EdgeVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.json.simple.JSONObject;


@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel
public class GraphOntologyEdgeVO extends EdgeVO {
    @ApiModelProperty(value = "Graph relation type", required = true)
    private String type;

    @ApiModelProperty(value = "Graph relation properties", required = true)
    private JSONObject details;
}
