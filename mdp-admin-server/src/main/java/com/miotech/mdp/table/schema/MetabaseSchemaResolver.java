package com.miotech.mdp.table.schema;

import com.google.gson.JsonObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.miotech.mdp.common.client.metabase.MetabaseCardRequestFactory;
import com.miotech.mdp.common.client.metabase.MetabaseClient;
import com.miotech.mdp.common.models.protobuf.metabase.*;
import com.miotech.mdp.common.models.protobuf.schema.*;
import com.miotech.mdp.common.util.JSONUtil;
import com.miotech.mdp.table.exception.SchemaResolveException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MetabaseSchemaResolver extends SchemaResolver {

    @Autowired @Lazy
    MetabaseClient client;

    public MetabaseSchemaResolver(MetabaseClient client) {
        this.client = client;
    }

    @Override
    public TableSchema describeTable(TableIdentifier table) throws SchemaResolveException {

        Integer dbId = table.getDatabaseId();
        String schema = table.getSchema();
        String name = table.getName();
        if (table.getDatabaseType() == DBType.MONGO) {
            return describeMongo(dbId, name);
        }
        return describeJDBC(dbId, name, schema);
    }

    private TableSchema describeMongo(Integer dbId, String collectionName) {
        try {
            client.syncDatabase(dbId);
            MetabaseDatabaseMeta db = client.getDatabaseMeta(dbId);
            Optional<MetabaseTable> optionTable = db
            .getTablesList()
            .stream().filter( x-> x.getName().equals(collectionName))
            .findAny();

            if (optionTable.isPresent()) {
                return metaToTableSchema( optionTable.get());
            } else {
                throw new SchemaResolveException(String.format("Cannot find table schema for %s in database %s", collectionName, db.getName()));
            }

        } catch (InvalidProtocolBufferException e) {
            throw new SchemaResolveException(e);
        }
    }

    private TableSchema describeJDBC(Integer dbId, String name, String schema) throws SchemaResolveException {
        try {
            if (schema == null || schema.isEmpty()) {
                schema = client.getDatabase(dbId).getDbName();
            }
            String query = String.format("select b.column_name as name, b.data_type as dataType, b.is_nullable as isNullable " +
                    "from information_schema.tables a " +
                    "join information_schema.columns b   " +
                    "on a.table_schema = b.table_schema  " +
                    " and a.table_name = b.table_name " +
                    "where a.table_schema={{schemaname}} " +
                    "and a.table_name ={{tablename}}");

            Map<String, TemplateTag> templateTagMap = new HashMap<>();
            TemplateTag schemaTag = MetabaseCardRequestFactory.createTemplateTag("schemaname", "SchemaName", "text", true);
            templateTagMap.put("schemaname", schemaTag);
            TemplateTag tableTag = MetabaseCardRequestFactory.createTemplateTag("tablename", "TableName", "text", true);
            templateTagMap.put("tablename", tableTag);

            String parameters =String.format("{\"parameters\":[{\"type\":\"category\",\n" +
                    "                                \"target\":[\"variable\",[\"template-tag\",\"schemaname\"]]\n" +
                    "                                   ,\"value\": \"%s\"},\n" +
                    "                               {\"type\":\"category\",\n" +
                    "                                \"target\":[\"variable\",[\"template-tag\",\"tablename\"]],\n" +
                    "                                \"value\": \"%s\"}\n" +
                    "]}", schema, name);
            DatasetQuery datasetQuery = MetabaseCardRequestFactory.createDatasetQuery(
                    dbId,
                    query,
                    templateTagMap
            );

            // query with dataset
            JsonObject params = JSONUtil.getJsonParser().parse(parameters).getAsJsonObject();

            QueryResult result = client.queryDataset(datasetQuery, params);
            List<FieldSchema> fields = result.getData()
                    .getRowsList()
                    .stream()
                    .map( x -> FieldSchema.newBuilder()
                            .setName(x.getValues(0).getStringValue())
                            .setDatabaseType(x.getValues(1).getStringValue())
                            .setIsNullable(x.getValues(2).getStringValue().equals("YES"))
                            .build()
                    ).collect(Collectors.toList());

            return TableSchema.newBuilder()
                    .setName(name)
                    .setSchema(schema)
                    .addAllFields(fields)
                    .build();
        } catch (InvalidProtocolBufferException e) {
            throw new SchemaResolveException(e);
        }
    }

    @Override
    public DatabaseSchema describeDatabase(DatabaseIdentifier database) {
        return null;
    }

    private TableSchema metaToTableSchema(MetabaseTable table) {
        List<FieldSchema> fields = table.getFieldsList()
                .stream().map(this::metaToFieldSchema)
                .collect(Collectors.toList());
        return TableSchema.newBuilder()
                .setName(table.getName())
                .setDescription(table.getDescription())
                .setSchema(table.getSchema())
                .addAllFields(fields)
                .build();
    }

    private FieldSchema metaToFieldSchema(MetabaseField field) {
        return FieldSchema.newBuilder()
                .setName(field.getName())
                .setDescription(field.getDescription())
                .setIsActive(field.getActive())
                .setDatabaseType(field.getBaseType())
                .build();
    }
}
