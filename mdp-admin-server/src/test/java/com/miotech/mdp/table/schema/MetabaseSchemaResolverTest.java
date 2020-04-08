package com.miotech.mdp.table.schema;

import com.miotech.mdp.common.client.metabase.MetabaseClient;
import com.miotech.mdp.common.models.protobuf.schema.DBType;
import com.miotech.mdp.common.models.protobuf.schema.TableIdentifier;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


public class MetabaseSchemaResolverTest {
    private MetabaseSchemaResolver resolver = null;

    @BeforeTest
    public void init() {
        MetabaseClient client = new MetabaseClient("f5d38e13-59ad-469f-b84d-3586f23a7551");
        resolver = new MetabaseSchemaResolver(client);
    }

    @Test
    public void testDescribeTable() {
        TableIdentifier table1 = TableIdentifier.newBuilder()
                .setDatabaseId(8)
                .setSchema("dm")
                .setName("cn_company_data")
                .setDatabaseType(DBType.ATHENA)
                .build();

        assert !resolver.describeTable(table1).getFieldsList().isEmpty();

        TableIdentifier table2 = TableIdentifier.newBuilder()
                .setDatabaseId(19)
                .setSchema("public")
                .setName("dossier_json")
                .setDatabaseType(DBType.POSTGRES)
                .build();

        assert !resolver.describeTable(table2).getFieldsList().isEmpty();

        TableIdentifier table3 = TableIdentifier.newBuilder()
                .setDatabaseId(24)
                .setName("crawler_company_basic_info_itjuzi")
                .setDatabaseType(DBType.MONGO)
                .build();

        assert !resolver.describeTable(table3).getFieldsList().isEmpty();

        TableIdentifier table4 = TableIdentifier.newBuilder()
                .setDatabaseId(10)
                .setName("CDSY_SECUCODE")
                .setDatabaseType(DBType.MYSQL)
                .build();

        assert !resolver.describeTable(table4).getFieldsList().isEmpty();

        TableIdentifier table5 = TableIdentifier.newBuilder()
                .setDatabaseId(3)
                .setName("ciqCompany")
                .setSchema("dbo")
                .setDatabaseType(DBType.SQLSERVER)
                .build();

        assert !resolver.describeTable(table5).getFieldsList().isEmpty();
    }
}