package com.miotech.mdp.graph.ontology.persistence;

import com.miotech.mdp.common.constant.GraphDirection;
import com.miotech.mdp.common.exception.ResourceNotFoundException;
import com.miotech.mdp.common.util.IdGenerator;
import com.miotech.mdp.graph.ontology.model.bo.GraphOntologyEdgeInfo;
import com.miotech.mdp.graph.ontology.model.bo.GraphOntologySearchCondition;
import com.miotech.mdp.graph.ontology.model.bo.GraphOntologyVertexInfo;
import com.miotech.mdp.graph.ontology.model.bo.GraphOntologyVertexUpdate;
import com.miotech.mdp.graph.ontology.model.dto.GraphOntologyEdge;
import com.miotech.mdp.graph.ontology.model.dto.GraphOntologyPath;
import com.miotech.mdp.graph.ontology.model.dto.GraphOntologySummary;
import com.miotech.mdp.graph.ontology.model.dto.GraphOntologyVertex;
import net.bitnine.agensgraph.graph.Edge;
import net.bitnine.agensgraph.graph.Path;
import net.bitnine.agensgraph.graph.Vertex;
import net.bitnine.agensgraph.util.Jsonb;
import net.bitnine.agensgraph.util.JsonbUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class GraphOntologyRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public GraphOntologySummary summary() {
        String vertexTypesSql = "select distinct v->>'type' as type from (match (v:graph_ontology_vertex ) return v) as vertex;";
        List<String> vertexTypes = jdbcTemplate.query(vertexTypesSql, rs -> {
            List<String> vertexTypesTemp = new ArrayList<>();
            while (rs.next()) {
                String type = rs.getString(1);
                vertexTypesTemp.add(type);
            }
            return vertexTypesTemp;
        });
        String vertexCountSql = "select count(1) from (match (v:graph_ontology_vertex ) return v) as vertex;";
        Long vertexCount = jdbcTemplate.query(vertexCountSql, rs -> {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        });
        String edgeTypesSql = "select distinct e->>'type' as type from (match (:graph_ontology_vertex)-[e:graph_ontology_edge ]->(:graph_ontology_vertex) return e) as edge;";
        List<String> edgeTypes = jdbcTemplate.query(edgeTypesSql, rs -> {
            List<String> edgeTypesTemp = new ArrayList<>();
            while (rs.next()) {
                String type = rs.getString(1);
                edgeTypesTemp.add(type);
            }
            return edgeTypesTemp;
        });
        String edgeCountSql = "select count(1) from (match (:graph_ontology_vertex)-[e:graph_ontology_edge ]->(:graph_ontology_vertex) return e) as edge;";
        Long edgeCount = jdbcTemplate.query(edgeCountSql, rs -> {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        });
        GraphOntologySummary summary = new GraphOntologySummary();
        summary.setVertexTypes(vertexTypes);
        summary.setVertexCount(vertexCount);
        summary.setEdgeTypes(edgeTypes);
        summary.setEdgeCount(edgeCount);
        return summary;
    }

    public GraphOntologyVertex createVertex(GraphOntologyVertexInfo vertexInfo) {
        String sql = "create (v:graph_ontology_vertex ?)\n" +
                "return v;";
        Jsonb vertexProps = JsonbUtil.createObjectBuilder()
                .add("vertex_id", IdGenerator.generateId())
                .add("type", vertexInfo.getType())
                .add("details", vertexInfo.getDetails())
                .add("create_time", LocalDateTime.now().toString())
                .add("update_time", "")
                .build();
        return jdbcTemplate.query(sql, ps -> ps.setObject(1, vertexProps), new VertexResultExtractor());
    }

    public GraphOntologyVertex updateVertex(String vertexId,
                                            GraphOntologyVertexUpdate vertexUpdate) {
        String sql = "match (v:graph_ontology_vertex {vertex_id: ?})\n" +
                "set v.details = ?, v.update_time = ?\n" +
                "return v;";
        Jsonb details = JsonbUtil.createObject(vertexUpdate.getDetails());
        return jdbcTemplate.query(sql, ps -> {
            ps.setString(1, vertexId);
            ps.setObject(2, details);
            ps.setString(3, LocalDateTime.now().toString());
        }, new VertexResultExtractor());
    }

    public void deleteVertex(String vertexId) {
        String sql = "match (v:graph_ontology_vertex {vertex_id: ?})\n" +
                "delete v\n" +
                "return 0";
        jdbcTemplate.query(sql, ps -> ps.setString(1, vertexId), rs -> null);
    }

    public GraphOntologyEdge createEdge(GraphOntologyEdgeInfo edgeInfo) {
        if (!isVertexExist(edgeInfo.getInputId())) {
            throw new ResourceNotFoundException("Graph Ontology vertex doesn't exist. your request id: " + edgeInfo.getInputId());
        }
        if (!isVertexExist(edgeInfo.getOutputId())) {
            throw new ResourceNotFoundException("Graph Ontology vertex doesn't exist. your request id: " + edgeInfo.getOutputId());
        }
        String sql = "match (v1:graph_ontology_vertex {vertex_id: ?}), (v2:graph_ontology_vertex {vertex_id: ?})\n" +
                "create (v1)-[e:graph_ontology_edge ?]->(v2)\n" +
                "return e";
        return jdbcTemplate.query(sql, ps -> {
            ps.setString(1, edgeInfo.getInputId());
            ps.setString(2, edgeInfo.getOutputId());
            Jsonb jsonb = JsonbUtil.createObjectBuilder()
                    .add("edge_id", IdGenerator.generateId())
                    .add("input_id", edgeInfo.getInputId())
                    .add("output_id", edgeInfo.getOutputId())
                    .add("type", edgeInfo.getType())
                    .add("details", edgeInfo.getDetails())
                    .add("create_time", LocalDateTime.now().toString())
                    .add("update_time", "")
                    .build();
            ps.setObject(3, jsonb);
        }, new EdgeResultExtractor());
    }

    public GraphOntologyEdge updateEdge(String edgeId,
                                        GraphOntologyEdgeInfo edgeInfo) {
        String sql = "match (:graph_ontology_vertex )-[e:graph_ontology_edge {edge_id: ?}]->(:graph_ontology_vertex )\n" +
                "set e.type = ?, e.input_id = ?, e.output_id = ?, e.details = ?, e.update_time = ?\n" +
                "return e;";
        return jdbcTemplate.query(sql, ps -> {
            ps.setString(1, edgeId);
            ps.setString(2, edgeInfo.getType());
            ps.setString(3, edgeInfo.getInputId());
            ps.setString(4, edgeInfo.getOutputId());
            ps.setObject(5, JsonbUtil.createObject(edgeInfo.getDetails()));
            ps.setString(6, LocalDateTime.now().toString());
        }, new EdgeResultExtractor());
    }

    public void deleteEdge(String edgeId) {
        String sql = "match (:graph_ontology_vertex )-[e:graph_ontology_edge {edge_id: ?}]->(:graph_ontology_vertex )\n" +
                "delete e\n" +
                "return 0";
        jdbcTemplate.query(sql, ps -> ps.setString(1, edgeId), rs -> null);
    }

    public boolean isVertexExist(String vertexId) {
        String sql = "match (v:graph_ontology_vertex {vertex_id: ?})\n" +
                "return v";
        return !jdbcTemplate.query(sql, ps -> ps.setString(1, vertexId), new VertexResultExtractor()).getVertices().isEmpty();
    }

    public GraphOntologyPath searchGraph(GraphOntologySearchCondition graphOntologySearchCondition) {
        if (StringUtils.isNotEmpty(graphOntologySearchCondition.getSrcId()) && StringUtils.isNotEmpty(graphOntologySearchCondition.getDstId())) {
            String pathSql = "match (v1:graph_ontology_vertex {vertex_id: ?}),(v2:graph_ontology_vertex {vertex_id: ?}),\n" +
                    "path=allshortestpaths((v1)-[:graph_ontology_edge*0.." + graphOntologySearchCondition.getLayerNum() + "]->(v2))\n" +
                    "return path";
            GraphOntologyPath ontologyPath = jdbcTemplate.query(pathSql, ps -> {
                ps.setString(1, graphOntologySearchCondition.getSrcId());
                ps.setString(2, graphOntologySearchCondition.getDstId());
            }, new PathResultExtractor());

            if (ontologyPath.getVertices().isEmpty()) {
                String vertexSql = "match (v:graph_ontology_vertex {vertex_id: ?})\n" +
                        "return v";
                GraphOntologyVertex srcVertex = jdbcTemplate.query(vertexSql, ps -> ps.setString(1, graphOntologySearchCondition.getSrcId()), new VertexResultExtractor());
                GraphOntologyVertex dstVertex = jdbcTemplate.query(vertexSql, ps -> ps.setString(1, graphOntologySearchCondition.getDstId()), new VertexResultExtractor());
                ontologyPath.getVertices().addAll(srcVertex.getVertices());
                ontologyPath.getVertices().addAll(dstVertex.getVertices());
            }

            return ontologyPath;
        } else if (StringUtils.isNotEmpty(graphOntologySearchCondition.getSrcId()) || StringUtils.isNotEmpty(graphOntologySearchCondition.getDstId())) {
            String vertexId = StringUtils.isNotEmpty(graphOntologySearchCondition.getSrcId()) ? graphOntologySearchCondition.getSrcId() : graphOntologySearchCondition.getDstId();
            GraphDirection direction = GraphDirection.fromName(graphOntologySearchCondition.getDirection());
            if (direction == null) {
                throw new RuntimeException("Unknown direction type : " + graphOntologySearchCondition.getDirection());
            }
            String pathSql = "match (v1:graph_ontology_vertex {vertex_id: ?}),(v2:graph_ontology_vertex ),\n" +
                    "path=allshortestpaths((v1)" + direction.getLeftSide() + "[:graph_ontology_edge*0.." + graphOntologySearchCondition.getLayerNum() + "]" + direction.getRightSide() + "(v2))\n" +
                    "return path";
            return jdbcTemplate.query(pathSql, ps -> ps.setString(1, vertexId), new PathResultExtractor());
        } else {
            String pathSql = "match (v1:graph_ontology_vertex ),(v2:graph_ontology_vertex ),\n" +
                    "path=allshortestpaths((v1)-[:graph_ontology_edge*0.." + graphOntologySearchCondition.getLayerNum() + "]->(v2))\n" +
                    "return path";
            return jdbcTemplate.query(pathSql, new PathResultExtractor());
        }
    }

    private class PathResultExtractor implements ResultSetExtractor<GraphOntologyPath> {

        @Override
        public GraphOntologyPath extractData(ResultSet rs) throws SQLException, DataAccessException {
            GraphOntologyPath path = new GraphOntologyPath();
            Set<String> vertexSet = new HashSet<>();
            Set<String> edgeSet = new HashSet<>();
            while (rs.next()) {
                Object object = rs.getObject(1);
                ((Path) object).vertices().forEach(vertex -> {
                    if (!vertexSet.contains(vertex.getVertexId().getValue())) {
                        vertexSet.add(vertex.getVertexId().getValue());
                        path.addVertex(vertex);
                    }
                });
                ((Path) object).edges().forEach(edge -> {
                    if (!edgeSet.contains(edge.getEdgeId().getValue())) {
                        edgeSet.add(edge.getEdgeId().getValue());
                        path.addEdge(edge);
                    }
                });
            }
            return path;
        }
    }

    private class EdgeResultExtractor implements ResultSetExtractor<GraphOntologyEdge> {

        @Override
        public GraphOntologyEdge extractData(ResultSet rs) throws SQLException, DataAccessException {
            GraphOntologyEdge graphOntologyEdge = new GraphOntologyEdge();
            while (rs.next()) {
                Object object = rs.getObject(1);
                graphOntologyEdge.addEdge((Edge) object);
            }
            return graphOntologyEdge;
        }
    }

    private class VertexResultExtractor implements ResultSetExtractor<GraphOntologyVertex> {

        @Override
        public GraphOntologyVertex extractData(ResultSet rs) throws SQLException, DataAccessException {
            GraphOntologyVertex graphOntologyVertex = new GraphOntologyVertex();
            while (rs.next()) {
                Object object = rs.getObject(1);
                graphOntologyVertex.addVertex((Vertex) object);
            }
            return graphOntologyVertex;
        }
    }
}
