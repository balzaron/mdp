package com.miotech.mdp.common.client.spark;

import com.google.protobuf.InvalidProtocolBufferException;
import com.miotech.mdp.common.client.HttpApiClient;
import com.miotech.mdp.common.models.protobuf.livy.App;
import com.miotech.mdp.common.models.protobuf.livy.Application;
import com.miotech.mdp.common.models.protobuf.livy.Applications;
import com.miotech.mdp.common.util.JSONUtil;
import com.miotech.mdp.common.util.StringUtil;

import java.util.List;
import java.util.stream.Collectors;

public class SparkClient extends HttpApiClient {
    private String host;
    private Integer port;

    public SparkClient(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String getBase() {
        return String.format("%s:%s", host, port);
    }

    public Application getApp(String appId) {
        try {
            return (Application) JSONUtil.toMessage(getApplication(appId), Application.newBuilder());
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("");
        }
    }

    private Applications getApplications(String parameters) {
        try {
            String appUrl = buildUrl(String.format("/ws/v1/cluster/apps"));
            if (!StringUtil.isNullOrEmpty(parameters)) {
                appUrl = appUrl + "?" + parameters;
            }
            return (Applications) JSONUtil.toMessage(getBody(get(appUrl)), Applications.newBuilder());
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    public List<App> getApplicationsByStates(List<String> states) {

        String statesFilters = null;
        if (states.size()>0) {
            statesFilters = "states=" + states.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.joining(","));
        }
        return getApplications(statesFilters).getApps().getAppList();
    }

    public String getApplication(String applicationId) {
        String appUrl = buildUrl(String.format("/ws/v1/cluster/apps/%s", applicationId));
        return getBody(get(appUrl));
    }

    public String killApplication(String applicationId) {
        String appUrl = buildUrl(String.format("/ws/v1/submissions/kill/%s", applicationId));
        return getBody(post(appUrl, null));
    }
}
