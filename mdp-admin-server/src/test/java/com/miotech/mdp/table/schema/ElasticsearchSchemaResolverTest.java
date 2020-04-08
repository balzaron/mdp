package com.miotech.mdp.table.schema;

import com.miotech.mdp.common.client.ElasticSearchClient;
import com.miotech.mdp.common.models.protobuf.schema.DBType;
import com.miotech.mdp.common.models.protobuf.schema.TableIdentifier;
import com.miotech.mdp.common.models.protobuf.schema.TableSchema;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


public class ElasticsearchSchemaResolverTest {
    private ElasticsearchSchemaResolver resolver = null;

    @BeforeTest
    public void init() {
        ElasticSearchClient client = new ElasticSearchClient("18.179.147.4",11005 );
        resolver = new ElasticsearchSchemaResolver(client);
    }

    @Test
    void describeTable() {
        TableIdentifier table1 = TableIdentifier.newBuilder()
                .setDatabaseId(-1)
                .setName("mio-company")
                .setDatabaseType(DBType.ELASTICSEARCH)
                .build();

        TableSchema schema = resolver.describeTable(table1);
        assert !schema.getFieldsList().isEmpty();
    }
}