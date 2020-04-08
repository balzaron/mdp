package com.miotech.mdp.graph.ontology.model.vo;

import com.miotech.mdp.common.model.vo.VertexVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.json.simple.JSONObject;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel
public class GraphOntologyVertexVO extends VertexVO {
    @ApiModelProperty(value = "Graph node type", required = true)
    private String type;

    @ApiModelProperty(value = "Graph node properties", required = true)
    private JSONObject details;
}
