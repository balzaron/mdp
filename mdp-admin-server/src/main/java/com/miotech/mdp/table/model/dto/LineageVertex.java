package com.miotech.mdp.table.model.dto;

import lombok.Data;
import net.bitnine.agensgraph.graph.Vertex;

import java.util.ArrayList;
import java.util.List;

@Data
public class LineageVertex {

    private List<Vertex> vertices = new ArrayList<>();

    public void addVertex(Vertex vertex) {
        vertices.add(vertex);
    }
}
