package com.miotech.mdp.graph.ontology.model.dto;

import lombok.Data;
import net.bitnine.agensgraph.graph.Vertex;

import java.util.ArrayList;
import java.util.List;

@Data
public class GraphOntologyVertex {

    private List<Vertex> vertices = new ArrayList<>();

    public void addVertex(Vertex vertex) {
        this.vertices.add(vertex);
    }
}
