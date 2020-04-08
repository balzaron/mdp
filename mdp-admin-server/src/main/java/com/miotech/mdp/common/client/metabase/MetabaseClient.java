package com.miotech.mdp.common.client.metabase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.*;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.miotech.mdp.common.client.HttpApiClient;
import com.miotech.mdp.common.models.protobuf.metabase.*;
import com.miotech.mdp.common.util.JSONUtil;
import org.springframework.http.*;

import static com.miotech.mdp.common.util.JSONUtil.getJsonParser;

/**
 *
 */
public class MetabaseClient extends HttpApiClient {

    private final String BASE_HOST = "https://bi.miotech.com";
    private String sessionCode;


    public MetabaseClient(String userName, String pass) throws InvalidProtocolBufferException {
        sessionCode = this.auth(userName, pass);
    }

    public MetabaseClient(String token)  {
        this.sessionCode = token;
    }

    private String auth(String userName, String pass) throws InvalidProtocolBufferException {
        String url = buildUrl("/api/session");
        AuthRequest request = AuthRequest.newBuilder()
                .setUsername(userName)
                .setPassword(pass)
                .build();
        AuthResponse response = (AuthResponse) parseResponse(post(url, request),
                AuthResponse.newBuilder());
        return response.getId();
    }

    public MetabaseCard createCard(MetabaseCardRequest card) throws InvalidProtocolBufferException {
        String url = buildUrl("/api/card");
        return (MetabaseCard) parseResponse(post(url, card),
                MetabaseCard.newBuilder());
    }

    public MetabaseCard getCard(Integer cardId) throws InvalidProtocolBufferException {
        String url = buildUrl(String.format("/api/card/%s", cardId));
        return (MetabaseCard) parseResponse(get(url),
                MetabaseCard.newBuilder());
    }

    public MetabaseCard updateCard(Integer cardId, MetabaseCardRequest card) throws InvalidProtocolBufferException {
        String url = buildUrl(String.format("/api/card/%s", cardId));
        return (MetabaseCard) parseResponse(put(url, card),
                MetabaseCard.newBuilder());
    }

    public void deleteCard(Integer cardId) {
        String url = buildUrl(String.format("/api/card/%s", cardId));
        delete(url);
    }

    public QueryResult queryCard(Integer cardId) {
        return this.queryCard(cardId, (String) null);
    }

    public QueryResult queryCard(Integer cardId, QueryRequest queryRequest) throws InvalidProtocolBufferException {
        if (queryRequest == null) {
            queryRequest = QueryRequest.getDefaultInstance();
        }
        return this.queryCard(cardId, JSONUtil.messageToString(queryRequest));
    }

    public QueryResult queryCard(Integer cardId,
                                 JsonObject queryRequest) {
        return queryCard(cardId, queryRequest.toString());
    }

    public QueryResult queryCard(Integer cardId,
                                 String queryRequest) {
        String url = buildUrl(String.format("/api/card/%s/query", cardId));
        return queryFromRemote(url, queryRequest);
    }

    public QueryResult queryDataset(DatasetQuery queryRequest) {
        return queryDataset(JSONUtil.messageToString(queryRequest));
    }

    public QueryResult queryDataset(DatasetQuery queryRequest, JsonObject parameters)
            throws InvalidProtocolBufferException {
        JsonObject query = JSONUtil.messageToJson(queryRequest).getAsJsonObject();
        query.add("parameters", parameters.get("parameters"));
        return queryDataset(query.toString());
    }

    public QueryResult queryDataset(String queryRequest) {
        String url = buildUrl(String.format("/api/dataset"));
        return queryFromRemote(url, queryRequest);
    }

    private QueryResult queryFromRemote(String url, String queryRequest) {
        try {
            if (queryRequest == null) {
                queryRequest = "{}";
            }
            ResponseEntity<String> responseEntity = request(url,
                    HttpMethod.POST,
                    new HttpEntity<>(queryRequest, createHeaders()));
            String responseBody = getBody(responseEntity);
            // TODO: cannot handle list of list: data.rows
            JsonParser jsonParser = getJsonParser();
            JsonObject result = jsonParser.parse(responseBody).getAsJsonObject();
            JsonObject data = result.remove("data").getAsJsonObject();
            JsonArray rows = data.remove("rows").getAsJsonArray();

            QueryData.Builder queryDataBuilder = QueryData.newBuilder()
                    .addAllRows(buildRows(rows));
            QueryData queryData = (QueryData) JSONUtil.toMessage(data.toString(),
                    queryDataBuilder);

            QueryResult.Builder queryResultBuilder = QueryResult.newBuilder()
                    .setData(queryData);
            return (QueryResult) JSONUtil.toMessage(result.toString(),
                    queryResultBuilder);
        } catch (InvalidProtocolBufferException e) {
            throw new MetabaseApiException(e);
        }

    }

    /**
     *
     * @param rows
     * @return
     */
    private List<Row> buildRows(JsonArray rows) {
        Iterator<JsonElement> rowsIterator = rows.iterator();
        List<Row> resultRows = new ArrayList<>();
        while (rowsIterator.hasNext()) {
            JsonArray row = rowsIterator.next().getAsJsonArray();
            Iterator<JsonElement> rowIterator = row.iterator();
            List<RowValue> rowValues = new ArrayList<>();
            while (rowIterator.hasNext()) {
                RowValue.Builder rowValue = RowValue.newBuilder();
                JsonElement rowVal = rowIterator.next();
                if (rowVal.isJsonNull()) {
                    rowValue.setNull(true);
                } else {
                    JsonPrimitive value = rowVal.getAsJsonPrimitive();
                    if (value.isBoolean()) {
                        rowValue.setBooleanValue(value.getAsBoolean());
                    } else if (value.isString()) {
                        rowValue.setStringValue(value.getAsString());
                    } else {
                        // TODO: dirty check, if is number type
                        rowValue.setNumberValue(value.getAsString());
                    }
                }
                rowValues.add(rowValue.build());
            }
            resultRows.add(Row.newBuilder().addAllValues(rowValues).build());
        }
        return resultRows;
    }

    public List<MetabaseDatabase> getDatabases() throws InvalidProtocolBufferException {
        String url = buildUrl("/api/database");
        String responseBody = getBody(get(url));
        JsonParser jsonParser = getJsonParser();
        JsonObject result = new JsonObject();
        result.add("databases", jsonParser.parse(responseBody));

        MetabaseDatabaseList metabaseDatabaseList = (MetabaseDatabaseList) JSONUtil.toMessage(result.toString(),
                MetabaseDatabaseList.newBuilder());
        return metabaseDatabaseList.getDatabasesList();
    }

    public MetabaseDatabase getDatabase(Integer dbId) throws InvalidProtocolBufferException {
        String url = buildUrl(String.format("/api/database/%s", dbId));
        String responseBody = getBody(get(url));
        JsonParser jsonParser = getJsonParser();
        JsonObject result = jsonParser.parse(responseBody).getAsJsonObject()
                .get("details").getAsJsonObject();
        String dbName = "";
        if (result.has("dbname")) {
            dbName = result.get("dbname").getAsString();
        } else if (result.has("db")) {
            dbName = result.get("db").getAsString();
        }
        MetabaseDatabase.Builder builder = MetabaseDatabase.newBuilder()
                .setDbName(dbName);
        return (MetabaseDatabase) parseResponse(responseBody, builder);
    }

    public List<String> getDatabaseSchemas(Integer dbId) throws InvalidProtocolBufferException {
        String url = buildUrl(String.format("/api/database/%s/schemas", dbId));
        String responseBody = getBody(get(url));
        JsonParser jsonParser = getJsonParser();
        JsonObject result = new JsonObject();
        result.add("schemas", jsonParser.parse(responseBody));

        SchemaList schemaList = (SchemaList) JSONUtil.toMessage(result.toString(),
                SchemaList.newBuilder());
        return schemaList.getSchemasList();
    }

    public MetabaseDatabaseMeta getDatabaseMeta(Integer dbId)
            throws InvalidProtocolBufferException {
        String url = buildUrl(String.format("/api/database/%s/metadata", dbId));
        return (MetabaseDatabaseMeta) parseResponse(get(url),
                MetabaseDatabaseMeta.newBuilder());
    }

    public void syncDatabase(Integer dbId) throws InvalidProtocolBufferException {
        String url = buildUrl(String.format("/api/database/%s/sync", dbId));
        post(url, "{}");
    }

    public MetabaseTable getTableMeta(Integer tableId) throws InvalidProtocolBufferException {
        String url = buildUrl(String.format("/api/table/%s/query_metadata", tableId));
        return (MetabaseTable) parseResponse(get(url),
                MetabaseTable.newBuilder());
    }

    @Override
    public String getBase() {
        return this.BASE_HOST;
    }


    @Override
    protected HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Metabase-Session", sessionCode);
        return headers;
    }

    private ResponseEntity<String> post(String url
            , MessageOrBuilder message) throws InvalidProtocolBufferException {
        String payload = null;
        if (message!=null) {
            payload = JSONUtil.messageToString(message);
        } else {
            payload = "{}";
        }

        return request(url,
                HttpMethod.POST,
                new HttpEntity<>(payload, createHeaders()));
    }

    private ResponseEntity<String> put(String url
            , MessageOrBuilder message) throws InvalidProtocolBufferException {
        String payload = JSONUtil.messageToString(message);
        return request(url,
                HttpMethod.PUT,
                new HttpEntity<>(payload, createHeaders()));
    }

    private Message parseResponse(ResponseEntity<String> response
            , Message.Builder builder) throws InvalidProtocolBufferException {
        return parseResponse(response.getBody(), builder);
    }

    private Message parseResponse(String response
            , Message.Builder builder) throws InvalidProtocolBufferException {
        return JSONUtil.toMessage(response, builder);
    }
}
