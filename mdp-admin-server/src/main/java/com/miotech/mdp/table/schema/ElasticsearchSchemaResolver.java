package com.miotech.mdp.table.schema;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.miotech.mdp.common.client.ElasticSearchClient;
import com.miotech.mdp.common.models.protobuf.schema.*;
import com.miotech.mdp.common.util.JSONUtil;
import com.miotech.mdp.common.util.StringUtil;
import com.miotech.mdp.table.exception.SchemaResolveException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ElasticsearchSchemaResolver extends SchemaResolver {

    private ElasticSearchClient esClient;

    public ElasticsearchSchemaResolver(JsonObject details) {
        String dbHost = details.get("host").getAsString();
        Integer dbPort = details.get("port").getAsInt();
        esClient = new ElasticSearchClient(dbHost, dbPort);
    }

    public ElasticsearchSchemaResolver(ElasticSearchClient client) {
        esClient = client;
    }

    @Override
    public TableSchema describeTable(TableIdentifier table) throws SchemaResolveException {

        String indexName = table.getName();
        String query = "DESC  " + StringUtil.escape(indexName);
        String response = esClient.sqlQuery(query);
        Iterator<JsonElement> array = JSONUtil.stringToJson(response)
                .getAsJsonArray("rows")
                .iterator();
        List<FieldSchema> fieldSchemaList = new ArrayList<>();
        while (array.hasNext()) {
            FieldSchema field = metaToFieldSchema(array.next().getAsJsonArray());
            fieldSchemaList.add(field);
        }
        return TableSchema.newBuilder()
                .setName(indexName)
                .addAllFields(fieldSchemaList).build();
    }

    @Override
    public DatabaseSchema describeDatabase(DatabaseIdentifier database) {
        return null;
    }

    private FieldSchema metaToFieldSchema(JsonArray jsonObject) {
        String fieldName = jsonObject.get(0).getAsString();
        String dataType = jsonObject.get(1).getAsString();
        return FieldSchema.newBuilder()
                .setName(fieldName)
                .setDatabaseType(dataType)
                .build();
    }
}
