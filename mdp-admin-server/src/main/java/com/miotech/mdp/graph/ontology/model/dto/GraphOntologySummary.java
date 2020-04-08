package com.miotech.mdp.graph.ontology.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class GraphOntologySummary {

    private List<String> vertexTypes;

    private Long vertexCount;

    private List<String> edgeTypes;

    private Long edgeCount;
}
