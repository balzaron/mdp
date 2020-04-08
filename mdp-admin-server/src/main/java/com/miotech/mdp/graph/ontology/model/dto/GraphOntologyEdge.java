package com.miotech.mdp.graph.ontology.model.dto;

import lombok.Data;
import net.bitnine.agensgraph.graph.Edge;

import java.util.ArrayList;
import java.util.List;

@Data
public class GraphOntologyEdge {

    private List<Edge> edges = new ArrayList<>();

    public void addEdge(Edge edge) {
        this.edges.add(edge);
    }
}
