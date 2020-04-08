package com.miotech.mdp.table.schema;

import com.google.gson.JsonObject;
import com.miotech.mdp.common.models.protobuf.schema.DBType;
import com.miotech.mdp.common.models.protobuf.schema.TableIdentifier;
import com.miotech.mdp.common.models.protobuf.schema.TableSchema;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class KinesisSchemaResolverTest {
    private KinesisSchemaResolver resolver = null;
    @BeforeTest
    public void init() {
        String accessKey = "AKIAIL42HPN4LO3XUIHQ";
        String secretKey = "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";
        JsonObject details = new JsonObject();
        details.addProperty("accessKey", accessKey);
        details.addProperty("secretKey", secretKey);

        resolver = new KinesisSchemaResolver(details);
    }

    @Test
    void describeTable() {
        TableIdentifier table1 = TableIdentifier.newBuilder()
                .setDatabaseId(-1)
                .setName("mio-ontology-narrative")
                .setDatabaseType(DBType.KINESIS)
                .build();

        TableSchema schema = resolver.describeTable(table1);
        assert !schema.getFieldsList().isEmpty();
    }
}
