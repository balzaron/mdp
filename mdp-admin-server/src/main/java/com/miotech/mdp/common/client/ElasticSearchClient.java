package com.miotech.mdp.common.client;

import com.google.gson.JsonObject;
import com.miotech.mdp.common.exception.ElasticSearchClientException;
import com.miotech.mdp.common.util.JSONUtil;
import com.miotech.mdp.common.util.StringUtil;
import com.miotech.mdp.common.util.VersionHelper;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

/**
 * Use rest api to interact with Elasticsearch for version compatible
 */
public class ElasticSearchClient extends HttpApiClient {

    public final String DEFAULT_TYPE = "_doc";

    private String base;
    private String username;
    private String password;

    private String version_number;

    public ElasticSearchClient(String host, Integer port) {
        this(host, port, null, null);
    }

    public ElasticSearchClient(String host, Integer port, String username, String password) {
        base = String.format("%s:%s", host, port);
        this.username = username;
        this.password = password;
        this.version_number = getVersion();
    }

    public String getIndexSpecification(String indexName) {
        String url = buildUrl(String.format("/%s", indexName));
        return this.getBody(this.get(url));
    }


    /**
     * Elasticsearch sql query
     * Before 7.x.x , the API is under xpack:
     *  POST /_xpack/sql
     * After:
     *  POST /_sql
     * @param query
     * @return
     */
    public String sqlQuery(String query) {
        String url = buildUrl("/_sql");
        if (VersionHelper.compare(version_number, "7.0.0") < 0) {
            url = buildUrl("/_xpack/sql");
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("query", query);
        return this.getBody(this.post(url, payload.toString()));
    }

    public String getVersion() {
        String url = buildUrl("");
        String info = this.getBody(this.get(url));
        JsonObject version = JSONUtil.stringToJson(info)
                .get("version").getAsJsonObject();
        return version.get("number").getAsString();
    }

    public JsonObject createDocument(JsonObject jsonObject, String indexName) throws ElasticSearchClientException {
        String jsonObjectStr = jsonObject.toString();
        return this.createDocument(jsonObjectStr, indexName, null);
    }

    public JsonObject createDocument(String requestBody, String indexName) throws ElasticSearchClientException {
        return this.createDocument(requestBody, indexName, null);
    }

    public JsonObject createDocument(JsonObject jsonObject, String indexName, String type)
            throws ElasticSearchClientException {
        String jsonObjectStr = jsonObject.toString();
        return this.createDocument(jsonObjectStr, indexName, type);
    }

    public JsonObject createDocument(String requestBody, String indexName, String type)
            throws ElasticSearchClientException {
        String url;
        if (VersionHelper.compare(this.version_number, "7.0.0") >= 0) {
            // In elastic search 7+, type will be deprecated,
            // @see: https://www.elastic.co/cn/blog/moving-from-types-to-typeless-apis-in-elasticsearch-7-0
            url = buildUrl(String.format("/%s/%s", indexName, DEFAULT_TYPE));
        } else {
            String typeStr = StringUtil.isNullOrEmpty(type) ? DEFAULT_TYPE: type;
            url = buildUrl(String.format("/%s/%s", indexName, typeStr));
        }
        ResponseEntity<String> r = this.post(url, requestBody);
        return JSONUtil.stringToJson(this.getBody(r));
    }

    public JsonObject updateDocument(String id, JsonObject jsonObject, String indexName) {
        String jsonObjectStr = jsonObject.toString();
        return this.updateDocument(id, jsonObjectStr, indexName, null);
    }

    public JsonObject updateDocument(String id, String requestBody, String indexName) {
        return this.updateDocument(id, requestBody, indexName, null);
    }

    public JsonObject updateDocument(String id, String requestBody, String indexName, String type)
            throws ElasticSearchClientException {
        String url;
        if (VersionHelper.compare(this.version_number, "7.0.0") >= 0) {
            // In elastic search 7+, type will be deprecated,
            // @see: https://www.elastic.co/cn/blog/moving-from-types-to-typeless-apis-in-elasticsearch-7-0
            url = buildUrl(String.format("/%s/%s/%s", indexName, DEFAULT_TYPE, id));
        } else {
            String typeStr = StringUtil.isNullOrEmpty(type) ? DEFAULT_TYPE : type;
            url = buildUrl(String.format("/%s/%s/%s", indexName, typeStr, id));
        }
        ResponseEntity<String> resp = this.put(url, requestBody);
        String respBody = this.getBody(resp);
        JsonObject json = JSONUtil.stringToJson(respBody);
        return json;
    }

    public Optional<JsonObject> findById(String id, String indexName) {
        return this.findById(id, indexName, null);
    }

    public Optional<JsonObject> findById(String id, String indexName, String type) {
        String typeStr = StringUtil.isNullOrEmpty(type) ? DEFAULT_TYPE : type;
        String url = buildUrl(String.format("/%s/%s/%s", indexName, typeStr, id));
        ResponseEntity<String> resp = this.get(url);
        JsonObject json = JSONUtil.stringToJson(resp.getBody());
        return Optional.of(json);
    }

    @Override
    public String getBase() {
        return this.base;
    }
}
