package com.miotech.mdp.table.service;

import com.miotech.mdp.common.models.protobuf.schema.FieldSchema;
import com.miotech.mdp.common.models.protobuf.schema.TableSchema;
import com.miotech.mdp.table.model.bo.TableColumnInfo;
import com.miotech.mdp.table.model.dao.MetaFieldEntity;
import com.miotech.mdp.table.model.dao.MetaTableEntity;
import com.miotech.mdp.table.model.vo.TableColumnVO;
import com.miotech.mdp.table.persistence.TableColumnRepository;
import com.miotech.mdp.table.persistence.TableRepository;
import com.miotech.mdp.table.schema.SchemaResolverProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TableColumnService {

    @Autowired
    SchemaResolverProvider schemaResolver;

    @Autowired
    TableRepository tableRepository;

    @Autowired
    TableColumnRepository tableColumnRepository;


    public MetaFieldEntity createMetaField(String tableId, FieldSchema fieldSchema) {
        MetaFieldEntity entity = new MetaFieldEntity();
        entity.setDatabaseType(fieldSchema.getDatabaseType());
        entity.setName(fieldSchema.getName());
        entity.setActive(fieldSchema.getIsActive());
        entity.setCreateTime(LocalDateTime.now());
        entity.setTableId(tableId);

        tableColumnRepository.saveAndFlush(entity);
        return entity;
    }

    public List<TableColumnVO> findByTableId(String tableId) {
        return tableColumnRepository.findByTableId(tableId)
                .stream()
                .map(this::convertToTableColumnVO)
                .collect(Collectors.toList());
    }

    public List<TableColumnVO> getTableColumns(TableColumnInfo tableInfo) {
        TableSchema tableSchema = schemaResolver.resolve(tableInfo.getDbType(),
                tableInfo.getCurrentDBId(),
                tableInfo.getTableName(),
                tableInfo.getSchema());
        return getTableColumns(tableSchema);
    }

    @Transactional
    public List<TableColumnVO> syncTableColumnInfo(String tableId) {
        MetaTableEntity tableInfo = tableRepository.findById(tableId).get();
        TableSchema tableSchema = getTableSchema(tableId);

        // remove old columns
        tableColumnRepository.deleteInBatch(tableInfo.getFields());
        // save columns
        tableSchema.getFieldsList()
                .forEach(x -> createMetaField(tableInfo.getId(), x));
        return getTableColumns(tableSchema);
    }

    private TableSchema getTableSchema(String tableId) {
        MetaTableEntity tableInfo = tableRepository.findById(tableId).get();
        return schemaResolver.resolve(
                tableInfo.getDatabaseType(),
                tableInfo.getDbId(),
                tableInfo.getName(),
                tableInfo.getSchema());
    }

    private List<TableColumnVO> getTableColumns(TableSchema tableSchema) {
        return tableSchema.getFieldsList()
                .stream()
                .map(x -> {
                    TableColumnVO col = new TableColumnVO();
                    col.setName(x.getName());
                    col.setDescription(x.getDescription());
                    col.setType(x.getDatabaseType());
                    col.setNullable(x.getIsNullable());
                    return col;
                })
        .collect(Collectors.toList());
    }

    public TableColumnVO convertToTableColumnVO(MetaFieldEntity metaFieldEntity) {
        TableColumnVO columnVO = new TableColumnVO();
        columnVO.setId(metaFieldEntity.getId());
        columnVO.setType(metaFieldEntity.getDatabaseType());
        columnVO.setDescription(metaFieldEntity.getDescription());
        columnVO.setName(metaFieldEntity.getName());
        columnVO.setKey(metaFieldEntity.getIsKeyField());
        columnVO.setNullable(metaFieldEntity.getIsNullable());
        columnVO.setUnique(metaFieldEntity.getIsUnique());
        return columnVO;
    }
}
