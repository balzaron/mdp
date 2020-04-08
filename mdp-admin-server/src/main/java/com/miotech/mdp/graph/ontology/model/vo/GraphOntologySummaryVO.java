package com.miotech.mdp.graph.ontology.model.vo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@Api
public class GraphOntologySummaryVO {
    @ApiModelProperty(position = 0, value = "node types")
    private List<String> vertexTypes;

    @ApiModelProperty(position = 2, value = "node count")
    private Long vertexCount;

    @ApiModelProperty(position = 3, value = "edge types")
    private List<String> edgeTypes;

    @ApiModelProperty(position = 4, value = "edge count")
    private Long edgeCount;
}
