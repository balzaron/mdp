package com.miotech.mdp.table.model.dto;

import lombok.Data;
import net.bitnine.agensgraph.graph.Edge;
import net.bitnine.agensgraph.graph.Vertex;

import java.util.ArrayList;
import java.util.List;

@Data
public class LineagePath {

    List<Vertex> vertices = new ArrayList<>();

    List<Edge> edges = new ArrayList<>();

    public void addVertex(Vertex vertex) {
        vertices.add(vertex);
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }
}
