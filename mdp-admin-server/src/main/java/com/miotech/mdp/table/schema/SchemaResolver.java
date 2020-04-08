package com.miotech.mdp.table.schema;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.miotech.mdp.common.models.protobuf.schema.*;
import com.miotech.mdp.table.exception.SchemaResolveException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class SchemaResolver {

    abstract TableSchema describeTable(TableIdentifier table) throws SchemaResolveException;

    abstract DatabaseSchema describeDatabase(DatabaseIdentifier database);

    public List<FieldSchema> docToFields(JsonElement element, String parent) {
        List<FieldSchema> fieldSchemaList = new ArrayList<>();
        if (element.isJsonObject()) {
            JsonObject doc = element.getAsJsonObject();
            doc.keySet().forEach( key -> {
                String keyName = key;
                if (parent != null) keyName = String.format("%s.%s", parent, key);
                fieldSchemaList
                        .addAll(docToFields(doc.get(key), keyName));

            });
        }

        else if (element.isJsonArray()) {
            Iterator<JsonElement> array = element.getAsJsonArray()
                    .iterator();
            if (array.hasNext()) {
                fieldSchemaList.addAll(docToFields(array.next(), parent));
            }
        } else {

            String fieldType = null;
            if (element.isJsonNull()) {
                fieldType = "type/Unknown";
            } else if (element.isJsonPrimitive()) {
                JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();
                if (jsonPrimitive.isNumber()) {
                    fieldType = "type/Number";
                } else if (jsonPrimitive.isBoolean()) {
                    fieldType = "type/Boolean";
                } else {
                    fieldType = "type/Text";
                }
            }
            FieldSchema fieldSchema = FieldSchema.newBuilder()
                    .setName(parent)
                    .setDatabaseType(fieldType)
                    .build();

            fieldSchemaList.add(fieldSchema);
        }

        return fieldSchemaList;
    }

}
