package com.miotech.mdp.table.schema;

import com.google.gson.JsonObject;
import com.miotech.mdp.common.models.protobuf.schema.DBType;
import com.miotech.mdp.common.models.protobuf.schema.TableIdentifier;
import com.miotech.mdp.common.models.protobuf.schema.TableSchema;
import com.miotech.mdp.table.model.dao.MetabaseDatabaseEntity;
import com.miotech.mdp.table.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class SchemaResolverProvider {

    @Autowired @Lazy
    MetabaseSchemaResolver metaResolver;

    @Autowired
    DatabaseService databaseService;

    public TableSchema resolve(String db, int dbId, String name, String schema) {
        DBType dbType = DBType.valueOf(db.toUpperCase());

        MetabaseDatabaseEntity databaseEntity = databaseService.getDatabase(dbId);
        JsonObject details = databaseEntity.getDetails();
        String dbName = getDbName(details);
        String tableSchema = getSchema(dbType, schema, dbName);

        TableIdentifier table = TableIdentifier.newBuilder()
                .setDatabaseType(dbType)
                .setDatabaseId(dbId)
                .setName(name)
                .setSchema(tableSchema)
                .build();

        switch (dbType) {
            case ARANGO:
                ArangoSchemaResolver arangoSchemaResolver = new ArangoSchemaResolver(details);
                return arangoSchemaResolver.describeTable(table);
            case ELASTICSEARCH:
                ElasticsearchSchemaResolver esResolver = new ElasticsearchSchemaResolver(details);
                return esResolver.describeTable(table);
            case KINESIS:
                KinesisSchemaResolver kinesisSchemaResolver = new KinesisSchemaResolver(details);
                return kinesisSchemaResolver.describeTable(table);
            default:
                return metaResolver.describeTable(table);
        }
    }

    private String getSchema(DBType databaseType, String schema, String dbName) {
        String tableSchema = schema;
        if (tableSchema == null) tableSchema = "";
        switch (databaseType) {
            case MONGO:
                tableSchema = "";
                break;
            case ATHENA:
            case PRESTO:
            case HIVE:
            case SPARKSQL:
                tableSchema = schema;
                break;
            case POSTGRES:
                // default schema is "public"
                if (tableSchema.isEmpty()) {
                    tableSchema = "public";
                }
                break;
            case SQLSERVER:
                // default schema is "dbo"
                if (tableSchema.isEmpty()) {
                    tableSchema = "dbo";
                }
                break;
           default:
               if (dbName != null && !dbName.isEmpty()) {
                   tableSchema = dbName;
               }
        }
        return tableSchema;
    }

    private String getDbName(JsonObject details) {
        String db = null;
        if (details.has("dbname")) {
            db = details.get("dbname").getAsString();
        } else if (details.has("db")) {
            db = details.get("db").getAsString();
        }
        return db;
    }
}
