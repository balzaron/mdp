package com.miotech.mdp.graph.ontology.model.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.json.simple.JSONObject;

@Data
@ApiModel
public class GraphOntologySearchCondition {
    @ApiModelProperty( position = 0,value = "graph query start point", notes = "when srcId and dstId are both empty, the whole graph may return")
    private String srcId;

    @ApiModelProperty( position = 1,value = "graph query end point", notes = "when srcId and dstId are both empty, the whole graph may return")
    private String dstId;

    @ApiModelProperty( position = 2,value = "graph query node type filter")
    private String vertexType;

    @ApiModelProperty( position = 3,value = "graph query relation type filter")
    private String relationType;

    @ApiModelProperty( position = 4,value = "graph query node filter")
    private JSONObject vertexDetails;

    @ApiModelProperty( position = 5,value = "graph query relation filter")
    private JSONObject relationDetails;

    @ApiModelProperty( position = 6,value = "graph query layers")
    private Integer layerNum = 1;

    @ApiModelProperty( position = 7,value = "graph query direction",allowableValues = "input, output, both")
    private String direction = "both";
}
