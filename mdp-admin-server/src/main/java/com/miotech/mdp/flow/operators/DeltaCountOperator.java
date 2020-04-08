package com.miotech.mdp.flow.operators;

import ch.qos.logback.classic.Logger;
import com.google.gson.JsonObject;
import com.miotech.mdp.common.client.ElasticSearchClient;
import com.miotech.mdp.common.models.protobuf.schema.DBType;
import com.miotech.mdp.common.util.JSONUtil;
import com.miotech.mdp.flow.constant.FlowState;
import com.miotech.mdp.flow.entity.dao.FlowRun;
import com.miotech.mdp.flow.entity.dao.FlowTask;
import com.miotech.mdp.flow.entity.dao.TaskInstance;
import com.miotech.mdp.flow.entity.vo.DeltaUpdate;
import com.miotech.mdp.table.model.dao.MetaTableEntity;
import com.miotech.mdp.table.model.dao.MetaTableMetricsEntity;
import com.miotech.mdp.table.model.dao.MetabaseDatabaseEntity;
import com.miotech.mdp.table.model.dao.Metric;
import com.miotech.mdp.table.service.DatabaseService;
import com.miotech.mdp.table.service.TableMetricsService;
import com.miotech.mdp.table.service.TableService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DeltaCountOperator extends AbstractTaskOperator {

    @Autowired
    private TableService tableService;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private TableMetricsService tableMetricsService;

    private ElasticSearchClient initElasticSearchClient(Integer dbId) {
        MetabaseDatabaseEntity db = databaseService.getDatabase(dbId);
        JsonObject dbDetails = db.getDetails();
        String host = dbDetails.get("host").getAsString();
        Integer port = dbDetails.get("port").getAsInt();
        String username = dbDetails.has("username") ?
                dbDetails.get("username").getAsString() : null;
        String password = dbDetails.has("password") ?
                dbDetails.get("password").getAsString() : null;
        return new ElasticSearchClient(host, port, username, password);
    }

    private void insertDeltaUpdate(DeltaUpdate deltaUpdate, String tableId) {
        MetaTableEntity insertTable = tableService.find(tableId);
        DBType dbType = DBType.valueOf(insertTable.getDatabaseType().toUpperCase());

        switch (dbType) {
            case ELASTICSEARCH:
                ElasticSearchClient esClient = initElasticSearchClient(insertTable.getDbId());
                esClient.createDocument(JSONUtil.objectToStringWithTimeToMillisecond(deltaUpdate), insertTable.getName());
                break;
            default:
                throw new RuntimeException(
                        String.format("Undefined insertion behavior for DB type: %s\n", dbType.toString())
                );
        }
    }

    @Override
    public void run(TaskInstance instance, Logger logger) {
        FlowTask flowTask = instance.getFlowTask();
        FlowRun flowRun = instance.getFlowRun();

        if (flowRun != null) {
            LocalDateTime startTime = flowRun.getStartTime();
            LocalDateTime endTime = flowRun.getEndTime();
            logger.info("Flow Run start from {} to {}", startTime, endTime);
            String errorReason = instance.getErrorReason();
            FlowState flowRunStatus = flowRun.getState();

            // JSONObject flowRunConfig = flowRun.getConfig();
            JSONObject flowTaskConfig = flowTask.getConfig();
            logger.info("Task parameter: {}", flowTaskConfig);
            JsonObject configJson = JSONUtil.stringToJson(flowTaskConfig.toJSONString());

            String deltaPoint = configJson.get("deltaPoint").getAsString();
            String deltaSource = configJson.get("deltaSource").getAsString();
            String deltaStep = configJson.get("deltaStep").getAsString();
            String deltaRequired = configJson.get("deltaRequired").getAsString();
            String deltaCheckPoint = configJson.get("deltaCheckPoint").getAsString();
            // we have converted the insertTable as table name, so we take the parameter from argument
            String insertTableId = flowTask.getArguments().stream()
                    .filter(x -> x.getParameterKey().equals("insertTable"))
                    .findFirst().get().getParameterValue();

            List<String> tableWatchList = JSONUtil.jsonToArray(configJson.get("tableWatchList"));

            tableWatchList.stream().forEach(watchTableId -> {
                DeltaUpdate deltaUpdate = new DeltaUpdate();

                deltaUpdate.setRunStartTime(startTime);
                deltaUpdate.setRunEndTime(endTime);
                deltaUpdate.setErrorMessage(errorReason);
                deltaUpdate.setRunStatus(flowRunStatus);
                deltaUpdate.setDeltaPoint(deltaPoint);
                deltaUpdate.setDeltaSource(deltaSource);
                deltaUpdate.setDeltaStep(deltaStep);
                deltaUpdate.setDeltaRequired(deltaRequired);
                deltaUpdate.setDeltaCheckPoint(deltaCheckPoint);

                logger.info("Inserting delta update info of table {} ...", watchTableId);
                MetaTableEntity watchTableInfo = tableService.find(watchTableId);
                deltaUpdate.setWatchTableId(watchTableId);
                deltaUpdate.setWatchTableFullName(watchTableInfo.getFullName());

                /* get previous metrics record */
                MetaTableMetricsEntity prevMetrics = tableMetricsService.getLatestMetrics(watchTableId);
                Integer prevCount;
                if (prevMetrics != null) {
                    prevCount = prevMetrics.getMetricCount();
                } else {
                    prevCount = 0;
                }

                /* get current metrics and save */
                Integer count = tableMetricsService.getRecordsCount(watchTableInfo);
                Metric metric = new Metric();
                metric.setCount(count);
                tableMetricsService.createMetric(watchTableId, metric);

                logger.info("Current count: {}, previous count: {}", count, prevCount);
                deltaUpdate.setDeltaCount(count - prevCount);
                insertDeltaUpdate(deltaUpdate, insertTableId);
            });
        }
    }
}
