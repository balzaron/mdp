package com.miotech.mdp.graph.ontology.service;

import com.miotech.mdp.common.util.AgensGraphUtil;
import com.miotech.mdp.graph.ontology.model.bo.GraphOntologyEdgeInfo;
import com.miotech.mdp.graph.ontology.model.bo.GraphOntologySearchCondition;
import com.miotech.mdp.graph.ontology.model.bo.GraphOntologyVertexInfo;
import com.miotech.mdp.graph.ontology.model.bo.GraphOntologyVertexUpdate;
import com.miotech.mdp.graph.ontology.model.dto.GraphOntologyEdge;
import com.miotech.mdp.graph.ontology.model.dto.GraphOntologyPath;
import com.miotech.mdp.graph.ontology.model.dto.GraphOntologySummary;
import com.miotech.mdp.graph.ontology.model.dto.GraphOntologyVertex;
import com.miotech.mdp.graph.ontology.model.vo.GraphOntologyEdgeVO;
import com.miotech.mdp.graph.ontology.model.vo.GraphOntologySummaryVO;
import com.miotech.mdp.graph.ontology.model.vo.GraphOntologyVO;
import com.miotech.mdp.graph.ontology.model.vo.GraphOntologyVertexVO;
import com.miotech.mdp.graph.ontology.persistence.GraphOntologyRepository;
import net.bitnine.agensgraph.graph.Edge;
import net.bitnine.agensgraph.graph.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GraphOntologyService {

    @Autowired
    GraphOntologyRepository graphOntologyRepository;

    public GraphOntologySummary summary() {
        return graphOntologyRepository.summary();
    }

    public GraphOntologyVertex createVertex(GraphOntologyVertexInfo vertexInfo) {
        return graphOntologyRepository.createVertex(vertexInfo);
    }

    public GraphOntologyVertex updateVertex(String vertexId,
                                            GraphOntologyVertexUpdate vertexUpdate) {
        return graphOntologyRepository.updateVertex(vertexId, vertexUpdate);
    }

    public void deleteVertex(String vertexId) {
        graphOntologyRepository.deleteVertex(vertexId);
    }

    public GraphOntologyEdge createEdge(GraphOntologyEdgeInfo edgeInfo) {
        return graphOntologyRepository.createEdge(edgeInfo);
    }

    public GraphOntologyEdge updateEdge(String edgeId,
                                        GraphOntologyEdgeInfo edgeInfo) {
        return graphOntologyRepository.updateEdge(edgeId, edgeInfo);
    }

    public void deleteEdge(String edgeId) {
        graphOntologyRepository.deleteEdge(edgeId);
    }

    public GraphOntologyPath searchGraph(GraphOntologySearchCondition graphOntologySearchCondition) {
        return graphOntologyRepository.searchGraph(graphOntologySearchCondition);
    }

    public GraphOntologyVO convertToGraphVO(GraphOntologyPath graphOntologyPath) {
        GraphOntologyVO graphOntologyVO = new GraphOntologyVO();
        List<GraphOntologyVertexVO> vertexVOs = new ArrayList<>();
        List<GraphOntologyEdgeVO> edgeVOs = new ArrayList<>();
        graphOntologyPath.getVertices().forEach(vertex -> {
            GraphOntologyVertexVO vertexVO = new GraphOntologyVertexVO();
            vertexVO.setId(vertex.getString("vertex_id"));
            vertexVO.setType(vertex.getString("type"));
            vertexVO.setDetails(AgensGraphUtil.jsonValueToObject(vertex.getObject("details").getJsonValue()));
            vertexVOs.add(vertexVO);
        });
        graphOntologyPath.getEdges().forEach(edge -> {
            GraphOntologyEdgeVO edgeVO = new GraphOntologyEdgeVO();
            edgeVO.setId(edge.getString("edge_id"));
            edgeVO.setType(edge.getString("type"));
            edgeVO.setInputId(edge.getString("input_id"));
            edgeVO.setOutputId(edge.getString("output_id"));
            edgeVO.setDetails(AgensGraphUtil.jsonValueToObject(edge.getObject("details").getJsonValue()));
            edgeVOs.add(edgeVO);
        });
        graphOntologyVO.setVertices(vertexVOs);
        graphOntologyVO.setEdges(edgeVOs);
        return graphOntologyVO;
    }

    public GraphOntologySummaryVO convertToSummaryVO(GraphOntologySummary summary) {
        GraphOntologySummaryVO vo = new GraphOntologySummaryVO();
        vo.setVertexTypes(summary.getVertexTypes());
        vo.setVertexCount(summary.getVertexCount());
        vo.setEdgeTypes(summary.getEdgeTypes());
        vo.setEdgeCount(summary.getEdgeCount());
        return vo;
    }

    public GraphOntologyEdgeVO convertToEdgeVO(GraphOntologyEdge graphOntologyEdge) {
        Edge edge = graphOntologyEdge.getEdges().get(0);
        GraphOntologyEdgeVO edgeVO = new GraphOntologyEdgeVO();
        edgeVO.setId(edge.getString("edge_id"));
        edgeVO.setInputId(edge.getString("input_id"));
        edgeVO.setOutputId(edge.getString("output_id"));
        edgeVO.setType(edge.getString("type"));
        edgeVO.setDetails(AgensGraphUtil.jsonValueToObject(edge.getObject("details").getJsonValue()));
        return edgeVO;
    }

    public GraphOntologyVertexVO convertToVertexVO(GraphOntologyVertex graphOntologyVertex) {
        Vertex vertex = graphOntologyVertex.getVertices().get(0);
        GraphOntologyVertexVO vertexVO = new GraphOntologyVertexVO();
        vertexVO.setId(vertex.getString("vertex_id"));
        vertexVO.setType(vertex.getString("type"));
        vertexVO.setDetails(AgensGraphUtil.jsonValueToObject(vertex.getObject("details").getJsonValue()));
        return vertexVO;
    }
}
