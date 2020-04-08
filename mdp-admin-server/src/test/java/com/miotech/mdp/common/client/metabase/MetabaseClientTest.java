package com.miotech.mdp.common.client.metabase;

import com.google.gson.JsonObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.miotech.mdp.common.models.protobuf.metabase.*;
import com.miotech.mdp.common.util.JSONUtil;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetabaseClientTest {

    private MetabaseClient client = null;

    @BeforeTest
    public void init() {
        client = new MetabaseClient("f5d38e13-59ad-469f-b84d-3586f23a7551");
    }

    @Test
    public void parseQuery() throws InvalidProtocolBufferException {

        String json = "{\"database_id\":8,\"started_at\":\"2019-12-29T14:25:31.589Z\",\"json_query\":{\"database\":8,\"type\":\"native\",\"native\":{\"query\":\"select 1\"},\"constraints\":{\"max-results\":10000,\"max-results-bare-rows\":2000},\"parameters\":null,\"middleware\":null,\"cache-ttl\":21},\"average_execution_time\":null,\"status\":\"completed\",\"context\":\"question\",\"row_count\":1,\"running_time\":1259,\"data\":{\"columns\":[\"_col0\"],\"native_form\":{\"query\":\"select 1\",\"params\":[]},\"cols\":[{\"name\":\"_col0\",\"display_name\":\"Co L0\",\"base_type\":\"type/Integer\",\"source\":\"native\"}],\"results_metadata\":{\"checksum\":\"gu6rF/AsrmLt8lau/2upkg==\",\"columns\":[{\"name\":\"_col0\",\"display_name\":\"Co L0\",\"base_type\":\"type/Integer\",\"special_type\":null,\"fingerprint\":{\"global\":{\"distinct-count\":1,\"nil%\":0.0},\"type\":{\"type/Number\":{\"min\":1.0,\"max\":1.0,\"avg\":1.0,\"sd\":null,\"q1\":1.0,\"q3\":1.0}}}}]},\"insights\":null}}";
        JSONUtil.toMessage(json, QueryResult.newBuilder());
    }

    @Test
    public void testGetDatabase() throws InvalidProtocolBufferException {
        List<MetabaseDatabase> databaseList = client.getDatabases();
        int dbId = databaseList.get(0).getId();
        MetabaseDatabase db = client.getDatabase(dbId);
        List<String> schemas = client.getDatabaseSchemas(dbId);
        MetabaseTable table = client.getTableMeta(3163);
        System.out.println(table.getFieldsList().size());

    }

    @Test
    public void testCreateCard() throws InvalidProtocolBufferException {
        TemplateTag tag = MetabaseCardRequestFactory.createTemplateTag("test", "test", "text", false);
        Map<String, TemplateTag> templateTagMap = new HashMap<>();
        templateTagMap.put("test", tag);
        MetabaseCardRequest card = MetabaseCardRequestFactory
                .createRequest("test", "test",
                        83, 8,
                        "select 1",
                        templateTagMap, "native");

        // create card
        MetabaseCard createdCard = client.createCard(card);
        MetabaseCard existedCard = client.getCard(createdCard.getId());
        assert card.getName().equals(existedCard.getName());

        // query card
        QueryResult queryResult = client.queryCard(createdCard.getId(), QueryRequest.getDefaultInstance());
        assert  queryResult
                .getData()
                .getRows(0)
                .getValues(0)
                .getNumberValue().equals("1");

        // update card
        String updateName = "update name";
        MetabaseCardRequest updateRequest = MetabaseCardRequest.newBuilder()
                .setName(updateName)
                .build();
        MetabaseCard updatedCard = client.updateCard(existedCard.getId(), updateRequest);
        assert updateName.equals(updatedCard.getName());


        // delete card
        client.deleteCard(createdCard.getId());
    }

    @Test
    public void testQuerySchema() throws InvalidProtocolBufferException {
        Integer dbId = 8;
        Map<String, TemplateTag> templateTagMap = new HashMap<>();
        TemplateTag schemaTag = MetabaseCardRequestFactory.createTemplateTag("schemaname", "SchemaName", "text", true);
        templateTagMap.put("schemaname", schemaTag);
        TemplateTag tableTag = MetabaseCardRequestFactory.createTemplateTag("tablename", "TableName", "text", true);
        templateTagMap.put("tablename", tableTag);
        String query = String.format("select b.column_name as name, b.data_type as dataType " +
        "from information_schema.tables a " +
        "join information_schema.columns b   " +
        "on a.table_schema = b.table_schema  " +
        " and a.table_name = b.table_name " +
        "where a.table_schema={{schemaname}} " +
        "and a.table_name ={{tablename}}");

        MetabaseCardRequest card = MetabaseCardRequestFactory
                .createRequest("testSchema", "test",
                        83, dbId,
                        query, templateTagMap, "native");
        MetabaseCard createdCard = client.createCard(card);
        assert createdCard.getId()>0;

        String parameters = "   {\"parameters\":[{\"type\":\"category\",\n" +
                "                                \"target\":[\"variable\",[\"template-tag\",\"schemaname\"]]\n" +
                "                                   ,\"value\": \"dm\"},\n" +
                "                               {\"type\":\"category\",\n" +
                "                                \"target\":[\"variable\",[\"template-tag\",\"tablename\"]],\n" +
                "                                \"value\": \"cn_company_join_data\"}\n" +
                "                               ]}";

        //  query with card
        QueryResult queryResult = client.queryCard(createdCard.getId(), parameters);

        client.deleteCard(createdCard.getId());

        DatasetQuery datasetQuery = MetabaseCardRequestFactory.createDatasetQuery(
                dbId,
                query,
                templateTagMap
        );

        // query with dataset
        JsonObject params = JSONUtil.getJsonParser().parse(parameters).getAsJsonObject();
        QueryResult result = client.queryDataset(datasetQuery, params);

        assert queryResult.getData().getRowsList().size() == result.getData().getRowsList().size();

    }


    @Test
    public void testQueryDataset() throws InvalidProtocolBufferException {
        Integer dbId = 24;
        DatasetQuery query = MetabaseCardRequestFactory.createDatasetQuery(
                dbId,
                "[ { \"$group\" : { \"_id\" : \"$EntityCreatedAt\", \"count\": { \"$sum\": 1 } } } ]",
                null,
                "crawler_company_basic_info_itjuzi",
                null
        );
        QueryResult result = client.queryDataset(query);
        assert !result.getData().getRowsList().isEmpty();
    }

    @Test
    public void testSyncSchema() throws InvalidProtocolBufferException {
        Integer dbId = 26;
        client.syncDatabase(dbId);
        MetabaseDatabaseMeta dbMeta = client.getDatabaseMeta(dbId);
        dbMeta.getTablesList()
                .stream()
                .forEach(x -> System.out.println(x.getName()));
    }

    @Test
    public void testQueryDataset1() throws InvalidProtocolBufferException {
        Integer dbId = 8;
        DatasetQuery query = MetabaseCardRequestFactory.createDatasetQuery(
                dbId,
                "select 1 as c1, '2' as c2,true as c3, 1.1 as c4, current_date as c5",
                null
        );
        QueryResult result = client.queryDataset(query);
        assert !result.getData().getRowsList().isEmpty();
    }

}