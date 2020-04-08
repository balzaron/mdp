package com.miotech.mdp.table.model.dto;

import lombok.Data;
import net.bitnine.agensgraph.graph.Edge;

import java.util.ArrayList;
import java.util.List;

@Data
public class LineageEdge {

    private List<Edge> edges = new ArrayList<>();

    public void addEdge(Edge edge) {
        edges.add(edge);
    }
}
