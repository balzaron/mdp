package com.miotech.mdp.common.client.spark;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.miotech.mdp.common.client.HttpApiClient;
import com.miotech.mdp.common.models.protobuf.livy.*;
import com.miotech.mdp.common.util.JSONUtil;
import com.miotech.mdp.common.util.StringUtil;

import java.util.HashSet;
import java.util.Set;

public class LivyClient extends HttpApiClient {
    private String base;
    private String queue;
    private String proxyUser;

    public LivyClient(String host) {
        this(host, null, null);
    }

    public LivyClient(String host, String queue, String proxyUser) {
        base = host;
        this.proxyUser = proxyUser;
        this.queue = queue;
    }

    @Override
    public String getBase() {
        return base;
    }

    public SparkApp runSparkJob(SparkJob job) {
        try {
            if (StringUtil.isNullOrEmpty(job.getQueue())
                    && !StringUtil.isNullOrEmpty(queue)) {
                job.setQueue(queue);
            }
            if (!StringUtil.isNullOrEmpty(proxyUser)) {
                job.setProxyUser(proxyUser);
            }
            String payload = filterZero(JSONUtil.objectToString(job));
            return (SparkApp) JSONUtil.toMessage(createBatch(payload), SparkApp.newBuilder());
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    public SparkApp runSparkStatements(SparkJob job) {
        try {
            if (StringUtil.isNullOrEmpty(job.getQueue())
                    && !StringUtil.isNullOrEmpty(queue)) {
                job.setQueue(queue);
            }
            if (!StringUtil.isNullOrEmpty(proxyUser)) {
                job.setProxyUser(proxyUser);
            }
            String payload = filterZero(JSONUtil.objectToString(job));
            return (SparkApp) JSONUtil.toMessage(createSession(payload), SparkApp.newBuilder());
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    public StateInfo getSessionState(Integer sessionId) {
        try {
            String url = buildUrl("/sessions/" + sessionId + "/state");
            return (StateInfo) JSONUtil.toMessage(this.getBody(this.get(url)), StateInfo.newBuilder());
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    public Statement createSessionStatement(Integer sessionId, StatementRequest statementRequest) {
        try {
            if (StringUtil.isNullOrEmpty(statementRequest.getKind())) {
                statementRequest.setKind("spark");
            }
            String url = buildUrl("/sessions/" + sessionId + "/statements");
            String payload = filterZero(JSONUtil.objectToString(statementRequest));
            return  parseStatement(this.getBody(this.post(url, payload)));
        }  catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    public Statement getSessionStatement(Integer sessionId, Integer statId) {
        try {
            String url = buildUrl("/sessions/" + sessionId + "/statements/" + statId);
            return  parseStatement(this.getBody(this.get(url)));
        }  catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    private Statement parseStatement(String response) throws InvalidProtocolBufferException {
        JsonObject jsonObject = JSONUtil.stringToJson(response);

        Statement.Builder stateBuilder = Statement.newBuilder();
        JsonElement output =  jsonObject.remove("output");
        if (output.isJsonObject()) {
            StatementOutput.Builder outputBuilder = StatementOutput.newBuilder();
            JsonObject jsonOutput = output.getAsJsonObject();
            if (jsonOutput.has("data")
                    && jsonOutput.get("data").isJsonObject()) {
                JsonObject data = jsonOutput.remove("data")
                        .getAsJsonObject();
                String mimeType = data.keySet().iterator().next();
                if (mimeType.equals("text/plain")) {
                    String text = data.get("text/plain").getAsString();
                    outputBuilder
                            .setData(
                                    StatementOutput.Data.newBuilder()
                                            .setTextPlain(
                                                    text
                                            )
                            );
                }
            }
            stateBuilder.setOutput(
                    (StatementOutput) JSONUtil.toMessage(jsonOutput.toString(), outputBuilder)
            );
        }
        return (Statement) JSONUtil.toMessage(jsonObject.toString(), stateBuilder);
    }

    private String filterZero(String payload) {
        JsonObject json = JSONUtil.stringToJson(payload);
        Set<String> keys = new HashSet<>(json.keySet());
        keys.forEach(x -> {
                    JsonElement v = json.get(x);
                    if (v.isJsonPrimitive() && v.getAsJsonPrimitive().isNumber()
                            && v.getAsJsonPrimitive().getAsInt() == 0)  {
                        json.remove(x);
                    }
                });
        return json.toString();
    }

    public AppList getActiveSparkJobs() {
        try {
            return (AppList) JSONUtil.toMessage(getBatches(), AppList.newBuilder());
        } catch (InvalidProtocolBufferException e) {
            throw new LivyApiException("");
        }
    }

    public SparkApp getSparkJob(int sparkJobID) {
        try {
            return (SparkApp) JSONUtil.toMessage(getBatch(sparkJobID), SparkApp.newBuilder());
        } catch (InvalidProtocolBufferException e) {
            throw new LivyApiException("");
        }
    }

    public String getSparkJobState(int sparkJobID) {
        return JSONUtil.stringToJson(getBatchState(sparkJobID))
                .get("state").getAsString();
    }

    public LogInfo getSparkJoblog(int sparkJobID, int from) {
        try {
            return (LogInfo) JSONUtil.toMessage(getBatchLog(sparkJobID, from), LogInfo.newBuilder());
        } catch (InvalidProtocolBufferException e) {
            throw new LivyApiException("");
        }
    }

    public JsonObject deleteSparkJob(int sparkJobID) {
        return JSONUtil.stringToJson(deleteBatch(sparkJobID));
    }

    public String deleteSession(Integer sessionId) {
        String url = buildUrl("/sessions/" + sessionId);
        return this.getBody(this.delete(url));
    }

    public String createBatch(String jobPayload) {
        String url = buildUrl("/batches");
        return this.getBody(this.post(url, jobPayload));
    }

    public String getBatches() {
        String url = buildUrl("/batches");
        return this.getBody(this.get(url));
    }

    public String getBatch(Integer batchId) {
        String url = buildUrl("/batches/" + batchId);
        return this.getBody(this.get(url));
    }

    public String getBatchState(Integer batchId) {
        String url = buildUrl("/batches/" + batchId + "/state");
        return this.getBody(this.get(url));
    }

    public String getBatchLog(Integer batchId, Integer from) {
        String url = buildUrl("/batches/" + batchId + "/log?from=" + from);
        return this.getBody(this.get(url));
    }

    public String deleteBatch(Integer batchId) {
        String url = buildUrl("/batches/" + batchId);
        return this.getBody(this.delete(url));
    }
    public String createSession(String jobPayload) {
        String url = buildUrl("/sessions");
        return this.getBody(this.post(url, jobPayload));
    }

    public String getSessionLog(Integer sessionId) {
        String url = buildUrl("/sessions/" + sessionId + "/log");
        return this.getBody(this.get(url));
    }

    public String getSessionStatements(Integer sessionId) {
        String url = buildUrl("/sessions/" + sessionId + "/statements");
        return this.getBody(this.get(url));
    }
}
