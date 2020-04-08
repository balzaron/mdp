package com.miotech.mdp.table.persistence;

import com.miotech.mdp.common.exception.InvalidQueryException;
import com.miotech.mdp.common.util.IdGenerator;
import com.miotech.mdp.table.model.bo.TableEdgeInfo;
import com.miotech.mdp.table.model.bo.TableEdgeUpdate;
import com.miotech.mdp.table.model.bo.TableLineageInfo;
import com.miotech.mdp.table.model.dto.LineageEdge;
import com.miotech.mdp.table.model.dto.LineagePath;
import com.miotech.mdp.table.model.dto.LineageVertex;
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
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Repository
public class TableLineageRepository {

    private final Lock lineageCreateLock = new ReentrantLock();

    @Autowired
    JdbcTemplate jdbcTemplate;

    public boolean isVertexExist(String tableId) {
        String sql = "match (t:meta_table {table_id: ?})\n" +
                "return t";
        LineageVertex lineageVertex = jdbcTemplate.query(sql, ps -> ps.setString(1, tableId), new VertexResultExtractor());
        return !lineageVertex.getVertices().isEmpty();
    }

    public LineageEdge createLineage(TableEdgeInfo tableEdgeInfo) {
        if (StringUtils.isEmpty(tableEdgeInfo.getInputId())) {
            throw new RuntimeException("Input id must not empty.");
        }
        if (StringUtils.isEmpty(tableEdgeInfo.getOutputId())) {
            throw new RuntimeException("Output id must not empty.");
        }
        if (StringUtils.equals(tableEdgeInfo.getInputId(), tableEdgeInfo.getOutputId())) {
            throw new RuntimeException("Input id and output id must not be equal.");
        }
        if (!isVertexExist(tableEdgeInfo.getInputId())) {
            lineageCreateLock.lock();
            createVertex(tableEdgeInfo.getInputId());
            lineageCreateLock.unlock();
        }
        if (!isVertexExist(tableEdgeInfo.getOutputId())) {
            lineageCreateLock.lock();
            createVertex(tableEdgeInfo.getOutputId());
            lineageCreateLock.unlock();
        }
        return createEdge(tableEdgeInfo);
    }

    public void createVertex(String tableId) {
        if (isVertexExist(tableId)) {
            return;
        }
        String sql = "create (t :meta_table ?)\n" +
                "return t";
        Jsonb vertexProps = JsonbUtil.createObjectBuilder()
                .add("table_id", tableId)
                .add("create_time", LocalDateTime.now().toString())
                .add("update_time", "")
                .add("input_count", 0)
                .add("output_count", 0)
                .build();
        jdbcTemplate.query(sql, ps -> ps.setObject(1, vertexProps), rs -> null);
    }

    public boolean isEdgeExist(TableEdgeInfo tableEdgeInfo) {
        return !getLineageEdge(tableEdgeInfo).getEdges().isEmpty();
    }

    public LineageEdge getLineageEdge(TableEdgeInfo tableEdgeInfo) {
        String sql = "match (t1:meta_table {table_id: ?})<-[e:meta_table_lineage ]->(t2:meta_table {table_id: ?})\n" +
                "return e";
        return jdbcTemplate.query(sql, ps -> {
            ps.setString(1, tableEdgeInfo.getInputId());
            ps.setString(2, tableEdgeInfo.getOutputId());
        }, new EdgeResultExtractor());
    }

    @Transactional(rollbackFor = Exception.class)
    public synchronized LineageEdge createEdge(TableEdgeInfo tableEdgeInfo) {
        LineageEdge lineageEdge;
        lineageEdge = getLineageEdge(tableEdgeInfo);
        if (!lineageEdge.getEdges().isEmpty()) {
            throw new InvalidQueryException("Lineage edge has been created between vertex: " + tableEdgeInfo.getInputId() + ", " + tableEdgeInfo.getOutputId());
        }
        String sql = "match (t1:meta_table {table_id: ?}),(t2:meta_table {table_id: ?})\n" +
                "create (t1)-[e:meta_table_lineage ?]->(t2)\n" +
                "return e";
        Jsonb edgeProps = JsonbUtil.createObjectBuilder()
                .add("edge_id", IdGenerator.generateId())
                .add("create_time", LocalDateTime.now().toString())
                .add("update_time", "")
                .add("input_id", tableEdgeInfo.getInputId())
                .add("output_id", tableEdgeInfo.getOutputId())
                .add("flow_ids", JsonbUtil.createArray(tableEdgeInfo.getFlowIds()))
                .build();
        lineageEdge = jdbcTemplate.query(sql, ps -> {
            ps.setString(1, tableEdgeInfo.getInputId());
            ps.setString(2, tableEdgeInfo.getOutputId());
            ps.setObject(3, edgeProps);
        }, new EdgeResultExtractor());


        String sql1 = "match (t1:meta_table {table_id: ?}),(t2:meta_table {table_id: ?})\n" +
                "set t1.output_count = t1.output_count + 1" +
                ", t1.update_time = ?" +
                ", t2.input_count = t2.input_count + 1" +
                ", t2.update_time = ?\n" +
                "return 0";
        String updateTime = LocalDateTime.now().toString();
        jdbcTemplate.query(sql1, ps -> {
            ps.setString(1, tableEdgeInfo.getInputId());
            ps.setString(2, tableEdgeInfo.getOutputId());
            ps.setString(3, updateTime);
            ps.setString(4, updateTime);
        }, rs -> null);

        return lineageEdge;
    }

    @Transactional(rollbackFor = Exception.class)
    public LineageEdge deleteLineage(String edgeId) {
        String sql = "match (t1:meta_table )-[e:meta_table_lineage {edge_id: ?}]->(t2:meta_table)\n" +
                "return e";
        LineageEdge edge = jdbcTemplate.query(sql, ps -> ps.setString(1, edgeId), new EdgeResultExtractor());
        List<String> vertexIds = new ArrayList<>();
        if (edge.getEdges().isEmpty()) {
            throw new RuntimeException("No edge match this id.");
        }
        vertexIds.add(edge.getEdges().get(0).getProperties().getString("input_id"));
        vertexIds.add(edge.getEdges().get(0).getProperties().getString("output_id"));

        String sql1 = "match (t1:meta_table )-[e:meta_table_lineage {edge_id: ?}]->(t2:meta_table)\n" +
                "delete e\n" +
                "return 0";
        jdbcTemplate.query(sql1, ps -> ps.setString(1, edgeId), rs -> null);

        String sql2 = "match (t1:meta_table {table_id: ?}),(t2:meta_table {table_id: ?})\n" +
                "set t1.output_count = t1.output_count - 1" +
                ", t1.update_time = ?" +
                ", t2.input_count = t2.input_count - 1" +
                ", t2.update_time = ?\n" +
                "return 0";
        String updateTime = LocalDateTime.now().toString();
        jdbcTemplate.query(sql2, ps -> {
            ps.setString(1, vertexIds.get(0));
            ps.setString(2, vertexIds.get(1));
            ps.setString(3, updateTime);
            ps.setString(4, updateTime);
        }, rs -> null);

        return edge;
    }

    public LineageEdge updateLineage(String edgeId,
                                     TableEdgeUpdate tableEdgeUpdate) {
        String sql = "match (t1:meta_table )-[e:meta_table_lineage {edge_id: ?}]->(t2:meta_table)\n" +
                "set e.flow_ids = ?, e.update_time= ?\n" +
                "return e";
        return jdbcTemplate.query(sql, ps -> {
            ps.setString(1, edgeId);
            ps.setObject(2, JsonbUtil.createArray(tableEdgeUpdate.getFlowIds()));
            ps.setString(3, LocalDateTime.now().toString());
        }, new EdgeResultExtractor());
    }

    public LineagePath getLineage(TableLineageInfo tableLineageInfo) {
        String sql = "match (t1:meta_table {table_id: ?}),(t2:meta_table),\n" +
                "path=allshortestpaths((t1)" + tableLineageInfo.getDirection().getLeftSide() + "[:meta_table_lineage*1.." + tableLineageInfo.getLayerNum() + "]" + tableLineageInfo.getDirection().getRightSide() + "(t2))\n" +
                "return path";
        return jdbcTemplate.query(sql, ps -> ps.setString(1, tableLineageInfo.getTableId()), new PathResultExtractor());
    }

    private static class EdgeResultExtractor implements ResultSetExtractor<LineageEdge> {

        @Override
        public LineageEdge extractData(ResultSet rs) throws SQLException, DataAccessException {
            LineageEdge edge = new LineageEdge();
            while (rs.next()) {
                Object object = rs.getObject(1);
                edge.addEdge((Edge) object);
            }
            return edge;
        }
    }

    private static class VertexResultExtractor implements ResultSetExtractor<LineageVertex> {

        @Override
        public LineageVertex extractData(ResultSet rs) throws SQLException, DataAccessException {
            LineageVertex vertex = new LineageVertex();
            while (rs.next()) {
                Object object = rs.getObject(1);
                vertex.addVertex((Vertex) object);
            }
            return vertex;
        }
    }

    private static class PathResultExtractor implements ResultSetExtractor<LineagePath> {

        @Override
        public LineagePath extractData(ResultSet rs) throws SQLException, DataAccessException {
            LineagePath path = new LineagePath();
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
}
