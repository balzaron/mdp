package com.miotech.mdp.table.service;

import com.miotech.mdp.common.constant.Lifecycle;
import com.miotech.mdp.common.exception.ResourceNotFoundException;
import com.miotech.mdp.common.jpa.CustomFilter;
import com.miotech.mdp.common.model.dao.TagsEntity;
import com.miotech.mdp.common.model.vo.TagVO;
import com.miotech.mdp.common.service.BaseService;
import com.miotech.mdp.common.service.TagService;
import com.miotech.mdp.table.model.bo.*;
import com.miotech.mdp.table.model.dao.MetaTableEntity;
import com.miotech.mdp.table.model.dao.MetabaseDatabaseEntity;
import com.miotech.mdp.table.model.dto.LineageEdge;
import com.miotech.mdp.table.model.dto.LineagePath;
import com.miotech.mdp.table.model.vo.*;
import com.miotech.mdp.table.persistence.MetaTableMetricsRepository;
import com.miotech.mdp.table.persistence.TableLineageRepository;
import com.miotech.mdp.table.persistence.TableRepository;
import com.miotech.mdp.table.schema.QueryParser;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.bitnine.agensgraph.deps.org.json.simple.JSONArray;
import net.bitnine.agensgraph.graph.Edge;
import net.bitnine.agensgraph.util.Jsonb;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TableService extends BaseService<MetaTableEntity> {

    @Autowired
    TableRepository tableRepository;

    @Autowired
    DatabaseService databaseService;

    @Autowired
    TagService tagService;

    @Autowired
    TableLineageRepository tableLineageRepository;

    @Autowired
    MetaTableMetricsRepository metaTableMetricsRepository;

    @Autowired
    TableColumnService tableColumnService;

    @Autowired
    QueryParser queryParser;

    @Transactional
    public MetaTableEntity createTable(TableInfo tableInfo) {

        MetaTableEntity entity = new MetaTableEntity();
        entity.setName(tableInfo.getTableName());
        entity.setDbId(tableInfo.getCurrentDBId());
        entity.setDescription(tableInfo.getDescription());
        entity.setDatabaseType(tableInfo.getDbType());
        if (!CollectionUtils.isEmpty(tableInfo.getTags())) {
            setTags(entity, tableInfo.getTags());
        }

        if (Lifecycle.fromName(tableInfo.getLifecycle()) == null) {
            throw new RuntimeException("Unknown lifecycle : " + tableInfo.getLifecycle());
        }
        entity.setLifecycle(tableInfo.getLifecycle());
        entity.setSchema(tableInfo.getSchema());

        entity.setRefDbIds(tableInfo.getReferenceDBIds().stream().toArray(Integer[]::new));
        MetaTableEntity savedEntity = tableRepository.save(entity);
        tableColumnService.syncTableColumnInfo(savedEntity.getId());
        return savedEntity;
    }

    public List<MetaTableEntity> getRelatedTableFromQuery(Integer dbId, String query) {
        MetabaseDatabaseEntity database = databaseService.getDatabase(dbId);
        return queryParser.getSourceTablesNames(database.getEngine(), query)
                .stream()
                .flatMap(x -> this.getTables(database.getId(), x).stream())
                .collect(Collectors.toList());
    }

    public List<MetaTableEntity> getTables(Integer dbId, String name) {
        String[] schemaAndTable = name.trim().split("\\.");
        String schema = null;
        String tableName;
        if (schemaAndTable.length > 1) {
            schema = schemaAndTable[0];
            tableName = schemaAndTable[1];
        } else {
            tableName = schemaAndTable[0];
        }
        return getTables(dbId, schema, tableName);
    }

    public List<MetaTableEntity> getTables(Integer dbId, String schema, String name) {
        if (schema == null) {
            return tableRepository.findByDbAndName(dbId, name);
        } else {
            return tableRepository.findByDbAndSchemaName(dbId, schema, name);
        }
    }

    public Page<MetaTableEntity> searchTable(TableSearchCondition tableSearchCondition) {

        if (tableSearchCondition == null) {
            tableSearchCondition = new TableSearchCondition();
        }

        //check lifecycle
        List<String> lifecycles = tableSearchCondition.getLifecycles();
        if (lifecycles != null && !lifecycles.isEmpty()) {
            for (String lifecycle : lifecycles) {
                if (Lifecycle.fromName(lifecycle) == null) {
                    throw new RuntimeException("Unknown lifecycle : " + lifecycle);
                }
            }
        }

        TableSearchCondition finalTableSearchCondition = tableSearchCondition;
        Specification<MetaTableEntity> specification = (Specification<MetaTableEntity>) (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.isNotEmpty(finalTableSearchCondition.getTableName())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("name")), "%" + finalTableSearchCondition.getTableName().toUpperCase() + "%"));
            }
            if (finalTableSearchCondition.getLifecycles() != null
                    && !finalTableSearchCondition.getLifecycles().isEmpty()) {
                predicates.add(root.get("lifecycle").in(finalTableSearchCondition.getLifecycles()));
            }
            if (finalTableSearchCondition.getDbTypes() != null
                    && !finalTableSearchCondition.getDbTypes().isEmpty()) {
                predicates.add(root.get("databaseType").in(finalTableSearchCondition.getDbTypes()));
            }
            if (finalTableSearchCondition.getTags() != null
                    && !finalTableSearchCondition.getTags().isEmpty()) {
                val tagIds = tagService.findTags(finalTableSearchCondition.getTags())
                        .stream().map(TagsEntity::getId).collect(Collectors.toList());
                val customFilter = new CustomFilter<MetaTableEntity>();
                predicates.add(criteriaBuilder.isTrue(
                        customFilter
                                .udfArrayContains(criteriaBuilder, root.get("tagIds"), tagIds))
                );
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.isNotNull(root.get("id"));
            }
            return criteriaBuilder.and(predicates.stream().toArray(Predicate[]::new));
        };

        Pageable pageableRequest = PageRequest.of(tableSearchCondition.getPageNum(),
                tableSearchCondition.getPageSize(),
                Sort.by("createTime").descending());

        return tableRepository.findAll(specification, pageableRequest);
    }

    public TableVO getTable(String id) {
        MetaTableEntity entity = tableRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Table entity doesn't exist."));
        return convertToTableVO(entity);
    }

    public MetaTableEntity updateTable(String tableId,
                                       TableUpdate tableUpdate) {
        if (StringUtils.isEmpty(tableId)) {
            throw new RuntimeException("TableId must not empty.");
        }
        MetaTableEntity updateEntity = tableRepository.findById(tableId).orElseThrow(() -> new ResourceNotFoundException("Table entity doesn't exist."));

        if (Lifecycle.fromName(tableUpdate.getLifecycle()) == null) {
            throw new RuntimeException("Unknown lifecycle : " + tableUpdate.getLifecycle());
        }

        updateEntity.setLifecycle(tableUpdate.getLifecycle());
        updateEntity.setDescription(tableUpdate.getDescription());
        setTags(updateEntity, tableUpdate.getTags());
        updateEntity.setUpdateTime(LocalDateTime.now());
        return tableRepository.save(updateEntity);
    }

    public LineagePath getTableLineage(TableLineageInfo tableLineageInfo) {
        if (StringUtils.isEmpty(tableLineageInfo.getTableId())) {
            throw new RuntimeException("TableId must not empty.");
        }
        return tableLineageRepository.getLineage(tableLineageInfo);
    }

    public LineageEdge createTableLineage(TableEdgeInfo tableEdgeInfo) {
        return tableLineageRepository.createLineage(tableEdgeInfo);
    }

    public LineageEdge deleteTableLineage(String edgeId) {
        return tableLineageRepository.deleteLineage(edgeId);
    }

    public LineageEdge updateTableLineage(String edgeId,
                                          TableEdgeUpdate tableEdgeUpdate) {
        return tableLineageRepository.updateLineage(edgeId, tableEdgeUpdate);
    }

    public TableVO convertToTableVO(MetaTableEntity metaTableEntity) {
        return convertToTableVO(metaTableEntity, true);
    }

    public TableVO convertToTableVO(MetaTableEntity metaTableEntity, Boolean withRelated) {
        TableVO tableVO = new TableVO();
        if (metaTableEntity == null) {
            return tableVO;
        }
        tableVO.setId(metaTableEntity.getId());
        tableVO.setSchema(metaTableEntity.getSchema());
        tableVO.setName(metaTableEntity.getName());
        tableVO.setLifecycle(metaTableEntity.getLifecycle());
        tableVO.setDescription(metaTableEntity.getDescription());
        tableVO.setDbType(metaTableEntity.getDatabaseType());

        if (withRelated) {
            tableVO.setTableColumns(
                    tableColumnService.findByTableId(metaTableEntity.getId())
            );
            tableVO.setCurrentDB(
                    databaseService.convertToDatabaseVO(databaseService.getDatabase(metaTableEntity.getDbId()))
            );
            tableVO.setReferenceDBs(
                    databaseService.convertToDatabaseVOs(databaseService.getDatabases(Arrays.asList(metaTableEntity.getRefDbIds())))
            );
        }
        tableVO.setTags(metaTableEntity.getTags().stream().map(TagsEntity::getName).collect(Collectors.toList()));
        return tableVO;
    }

    public List<TagVO> convertToTagVOs(List<TagsEntity> tagsEntitys) {
        return tagsEntitys.stream().map(this::convertToTagVO).collect(Collectors.toList());
    }

    public TagVO convertToTagVO(TagsEntity tagsEntity) {
        TagVO tagVO = new TagVO();
        tagVO.setId(tagsEntity.getId());
        tagVO.setName(tagsEntity.getName());
        tagVO.setColor(tagsEntity.getColor());
        return tagVO;
    }

    public TableSearchVO convertToTableSearchVO(Page<MetaTableEntity> pageResult) {
        List<TableVO> searchResult = pageResult
                .stream()
                // do not set related columns to prevent large payload
                .map(x -> convertToTableVO(x, false))
                .collect(Collectors.toList());
        TableSearchVO tableSearchVO = new TableSearchVO();
        tableSearchVO.setTables(searchResult);
        tableSearchVO.setPageNum(pageResult.getNumber());
        tableSearchVO.setPageSize(pageResult.getSize());
        tableSearchVO.setTotalCount(pageResult.getTotalElements());
        return tableSearchVO;
    }

    public TableLineageVO convertToTableLineageVO(LineagePath path) {
        TableLineageVO tableLineageVO = new TableLineageVO();
        List<TableVertexVO> vertexVOs = new ArrayList<>();
        List<TableEdgeVO> edgeVOs = new ArrayList<>();
        path.getVertices().forEach(vertex -> {
            TableVertexVO vo = new TableVertexVO();
            String tableId = vertex.getString("table_id");
            vo.setId(tableId);
            vo.setTable(getTable(tableId));
            vo.setInputCount(vertex.getInt("input_count"));
            vo.setOutputCount(vertex.getInt("output_count"));
            vertexVOs.add(vo);
        });
        path.getEdges().forEach(edge -> edgeVOs.add(convertToTableEdgeVO(edge)));

        tableLineageVO.setVertices(vertexVOs);
        tableLineageVO.setEdges(edgeVOs);
        return tableLineageVO;
    }

    public TableEdgeVO convertToTableEdgeVO(LineageEdge lineageEdge) {
        return convertToTableEdgeVO(lineageEdge.getEdges().get(0));
    }

    public TableEdgeVO convertToTableEdgeVO(Edge edge) {
        TableEdgeVO vo = new TableEdgeVO();
        vo.setId(edge.getString("edge_id"));
        vo.setInputId(edge.getString("input_id"));
        vo.setOutputId(edge.getString("output_id"));
        Jsonb flowIdsJson = edge.getArray("flow_ids");
        List<FlowInfoVO> flowVOS = (List<FlowInfoVO>) ((JSONArray) flowIdsJson.getJsonValue())
                .stream()
                .map(flowId -> {
                    FlowInfoVO flowVO = new FlowInfoVO();
                    flowVO.setId((String) flowId);
                    return flowVO;
                }).collect(Collectors.toList());
        vo.setFlows(flowVOS);
        return vo;
    }

    private void setTags(MetaTableEntity metaTableEntity, List<String> tags) {
        if (tags != null && !tags.isEmpty()) {
            List<TagsEntity> tagsEntities = tagService.findTags(tags);
            String[] tagIds = tagsEntities.stream()
                    .map(TagsEntity::getId)
                    .toArray(String[]::new);
            metaTableEntity.setTagIds(tagIds);
            metaTableEntity.setTags(new HashSet<>(tagsEntities));
        }
    }
}
