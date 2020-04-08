package com.miotech.mdp.table.schema;

import com.google.gson.JsonObject;
import com.miotech.mdp.common.models.protobuf.schema.DBType;
import com.miotech.mdp.common.models.protobuf.schema.TableIdentifier;
import com.miotech.mdp.common.models.protobuf.schema.TableSchema;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


public class ArangoSchemaResolverTest {
    private ArangoSchemaResolver resolver = null;

    @BeforeTest
    public void init() {
        String host= "18.179.147.4";
        String dbName = "miograph_unmerged_one";
        JsonObject details = new JsonObject();
        details.addProperty("host", host);
        details.addProperty("port", 12002);
        details.addProperty("db", dbName);


        resolver = new ArangoSchemaResolver(details);
    }

    @Test
    void describeTable() {
        TableIdentifier table1 = TableIdentifier.newBuilder()
                .setDatabaseId(-1)
                .setName("mio_company")
                .setDatabaseType(DBType.ARANGO)
                .build();

        TableSchema schema = resolver.describeTable(table1);
        assert !schema.getFieldsList().isEmpty();
    }
}