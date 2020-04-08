package com.miotech.mdp.graph.ontology.model.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.json.simple.JSONObject;

@Data
@ApiModel
public class GraphOntologyVertexInfo {
    @ApiModelProperty(value = "Graph node type", required = true)
    private String type;

    @ApiModelProperty(value = "Graph node properties", required = true)
    private JSONObject details;
}
