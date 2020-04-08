package com.miotech.mdp.graph.ontology.model.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.json.simple.JSONObject;

@Data
@ApiModel
public class GraphOntologyEdgeInfo {

    @ApiModelProperty(value = "Graph relation type", required = true)
    private String type;

    @ApiModelProperty(value = "Graph relation properties", required = true)
    private JSONObject details;

    @ApiModelProperty(required = true, value = "edge input id")
    private String inputId;

    @ApiModelProperty(required = true, value = "edge output id")
    private String outputId;
}
