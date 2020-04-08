package com.miotech.mdp.table.schema;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.miotech.mdp.common.client.ArangoClient;
import com.miotech.mdp.common.models.protobuf.schema.*;
import com.miotech.mdp.common.util.JSONUtil;
import com.miotech.mdp.table.exception.SchemaResolveException;

import java.util.Iterator;

public class ArangoSchemaResolver extends SchemaResolver {
    private ArangoClient arangoClient;

    public ArangoSchemaResolver(JsonObject details) {
        String user = null;
        String password = null;
        if (details.has("user")) {
            user = details.get("user").getAsString();
        }
        if (details.has("password")) {
            password = details.get("password").getAsString();
        }
        arangoClient = new ArangoClient(
                details.get("host").getAsString(),
                details.get("port").getAsInt(),
                details.get("db").getAsString(),
                user,
                password
        );
    }

    @Override
    public TableSchema describeTable(TableIdentifier table) throws SchemaResolveException {
        Integer batchSize = 1;
        String collectionName = table.getName();
        String query = String.format("FOR c IN %s LIMIT %s RETURN c", collectionName, batchSize);
        String response = arangoClient.queryCursor(query, batchSize);
        Iterator<JsonElement> array = JSONUtil.stringToJson(response)
                .getAsJsonArray("result")
                .iterator();

        if (!array.hasNext()) {
            throw new SchemaResolveException("No document in collection " + collectionName);
        }

        JsonObject doc = array.next().getAsJsonObject();
        return TableSchema.newBuilder()
                .setName(collectionName)
                .addAllFields(
                        docToFields(doc, null)
                )
                .build();
    }

    @Override
    public DatabaseSchema describeDatabase(DatabaseIdentifier database) {
        return null;
    }
}
