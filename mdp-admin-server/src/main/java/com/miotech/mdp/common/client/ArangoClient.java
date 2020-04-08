package com.miotech.mdp.common.client;


import com.google.gson.JsonObject;
import com.miotech.mdp.common.util.JSONUtil;
import org.springframework.http.HttpHeaders;

public class ArangoClient extends HttpApiClient {
    private String base;
    private String token;

    public ArangoClient(String host, Integer port, String dbName) {
        this(host, port, dbName, null, null);
    }

    public ArangoClient(String host, Integer port, String dbName,
                        String username, String password){
        base = String.format("%s:%s/_db/%s", host, port, dbName);
        if (username != null && password != null)  {
            token = auth(username, password);
        }
    }

    @Override
    public String getBase() {
        return base;
    }

    private String auth(String user, String password) {
        String url = buildUrl("/_open/auth");
        JsonObject payload = new JsonObject();

        payload.addProperty("username", user);
        payload.addProperty("password", password);
        String response = this.getBody(this.post(url, payload.toString()));
        return JSONUtil.stringToJson(response).get("jwt").getAsString();
    }

    @Override
    protected HttpHeaders createHeaders() {
        HttpHeaders headers = super.createHeaders();
        headers.add("Authorization" , "bearer " + token);
        return headers;
    }

    public String getCollections() {
        String url = buildUrl("/_api/collection");
        return this.getBody(this.get(url));
    }

    public String queryCursor(String query, Integer batchSize) {
        String url = buildUrl("/_api/cursor");
        JsonObject payload = new JsonObject();
        payload.addProperty("query", query);
        payload.addProperty("batchSize", batchSize);

        return this.getBody(this.post(url, payload.toString()));
    }
}
