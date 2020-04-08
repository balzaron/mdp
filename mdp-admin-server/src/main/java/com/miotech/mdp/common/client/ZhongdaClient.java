package com.miotech.mdp.common.client;

import com.google.gson.JsonObject;

import java.util.List;

public class ZhongdaClient extends HttpApiClient {
    private String host;
    private String token;


    public ZhongdaClient(String host, String token) {
        this.host = host;
        this.token = token;
    }

    @Override
    public String getBase() {
        return host;
    }

    public String sendMessage(String content, String group, List<String> users) {
        String api = buildUrl("/alertservice/send-wechat");

        JsonObject payload = new JsonObject();
        payload.addProperty("token", token);
        payload.addProperty("group", group);
        payload.addProperty("body", content);

        payload.addProperty("user_list", String.join(",", users));
        return getBody(post(api, payload.toString()));
    }
}
