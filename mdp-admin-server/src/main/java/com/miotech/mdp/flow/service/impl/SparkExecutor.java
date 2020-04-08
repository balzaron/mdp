package com.miotech.mdp.flow.service.impl;

import ch.qos.logback.classic.Logger;
import com.google.protobuf.InvalidProtocolBufferException;
import com.miotech.mdp.common.client.spark.LivyClient;
import com.miotech.mdp.common.client.spark.SparkClient;
import com.miotech.mdp.common.client.spark.SparkJob;
import com.miotech.mdp.common.client.spark.StatementRequest;
import com.miotech.mdp.common.models.protobuf.livy.Application;
import com.miotech.mdp.common.models.protobuf.livy.LogInfo;
import com.miotech.mdp.common.models.protobuf.livy.SparkApp;
import com.miotech.mdp.common.models.protobuf.livy.Statement;
import com.miotech.mdp.common.util.JSONUtil;
import com.miotech.mdp.common.util.StringUtil;
import com.miotech.mdp.flow.constant.TaskConfig;
import com.miotech.mdp.flow.constant.TaskState;
import com.miotech.mdp.flow.entity.dao.FlowRun;
import com.miotech.mdp.flow.entity.dao.FlowTask;
import com.miotech.mdp.flow.entity.dao.Operator;
import com.miotech.mdp.flow.entity.dao.TaskInstance;
import com.miotech.mdp.flow.service.ExecutorService;
import com.miotech.mdp.flow.util.LoggingUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SparkExecutor implements ExecutorService {

    private final String CODE_MODE = "CODE";
    private final String JOB_MODE = "JOB";

    @Autowired
    private LivyClient livyClient;

    @Autowired
    private SparkClient sparkClient;

    @Override
    public void executeTask(TaskInstance task) {
        Logger logger = LoggingUtil.getFileLogger(task.getLogPath());

        if (hasCodeToRun(task)) {
            createSparkApp(task, CODE_MODE, logger);
        } else {
            createSparkApp(task, JOB_MODE, logger);
        }
    }

    @Override
    public void updateTask(TaskInstance task) {
        Logger logger = LoggingUtil.getFileLogger(task.getLogPath());

        SparkApp app = getSparkApp(task);
        if (hasCodeToRun(task)
                && app == null) {
            updateSparkStatement(task, logger);
        }
        if (task.isFailed()) {
            return;
        }

        // try to run application after code completion
        if (task.isEnded()
                && hasCodeToRun(task)
                && app == null) {
            createSparkApp(task, JOB_MODE, logger);
        }
        if (app != null) {
            updateSparkApp(task, logger);
        }
    }

    @Override
    public void cancelTask(TaskInstance task) {
        SparkApp app = getSparkApp(task);
        getLivyClient(task).deleteSparkJob(app.getId());
        task.setState(TaskState.KILLED);
    }

    private void createSparkApp(TaskInstance task, String runningMode, Logger logger) {
        FlowTask flowTask = task.getFlowTask();
        Operator operator = flowTask.getOperator();

        JSONObject platformConfig = operator.getPlatformConfig();
        String jars = platformConfig.get("jars").toString();
        String files = platformConfig.get("files").toString();
        String application = platformConfig.get("application").toString();
        String args = platformConfig.get("args").toString();

        SparkJob job = new SparkJob();
        job.setName(task.getName());
        if (!StringUtil.isNullOrEmpty(jars)) {
            job.setJars(Arrays.asList(jars.split(",")));
        }
        List<String> jobFiles = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(files)) {
            jobFiles = Arrays.stream(files.split(","))
                    .map(String::trim)
                    .filter(x -> !x.isEmpty())
                    .collect(Collectors.toList());
        }
        try {
            LivyClient client = getLivyClient(task);
            if (runningMode.equals(CODE_MODE)) {
                job.setFiles(jobFiles);
                String code = (String) flowTask
                        .getConfig().get("code");
                logger.info("Execute code :\n {}" , code);
                StatementRequest statementRequest = new StatementRequest();
                statementRequest.setCode(code);
                SparkApp session = client.runSparkStatements(job);
                Integer sessionId = session.getId();

                String state = session.getState();
                while(!state.equals("idle")) {
                    state = client.getSessionState(sessionId)
                            .getState();
                    Thread.sleep(3000);
                }

                Statement statement = client.createSessionStatement(
                        sessionId,
                        statementRequest);

                logger.info("Execute session code in session: {}", sessionId);
                task.getInfo().put("sessionId", sessionId);
                setSparkStatement(task, statement);
            } else if (runningMode.equals(JOB_MODE)){
                if (!jobFiles.isEmpty()) {
                    job.setFile(jobFiles.get(0));
                    // set extra files
                    if (jobFiles.size()>1) {
                        job.setFiles(jobFiles.subList(1, jobFiles.size()-1));
                    }
                }
                if (StringUtil.isNullOrEmpty(application)) {
                    // TODO: consider for python
                    return;
                }
                job.setClassName(application);
                List<String> jobArgs = new ArrayList<>();
                if (!StringUtil.isNullOrEmpty(args)) {
                    jobArgs = Arrays.stream(args.split("\\s+"))
                            .filter(x -> !x.isEmpty())
                            .map(String::trim)
                            .collect(Collectors.toList());
                }

                jobArgs.addAll(flowTask.getParams());
                if (!jobArgs.isEmpty()) {
                    job.setArgs(jobArgs);
                }
                SparkApp app = client.runSparkJob(job);
                logger.info("Execute spark application using livy : batch id {}", app.getId());
                logger.info("Execution job : {}", JSONUtil.objectToString(job));
                logger.info("Execution config: {}", flowTask.getConfig().toJSONString());
                setSparkApp(task, app);
            }

            task.setState(TaskState.RUNNING);
        } catch (Exception e) {
            logger.info("Faild to Execution: ", e);
            task.setState(TaskState.FAILED);
            task.setErrorReason(e.getMessage());
        }
    }

    private boolean hasCodeToRun(TaskInstance taskInstance) {
        String code = (String) taskInstance
                .getFlowTask()
                .getConfig().get("code");
        return !StringUtil.isNullOrEmpty(code);
    }

    private void updateSparkStatement(TaskInstance task, Logger logger) {
        if (!task.getInfo().containsKey("sessionId")) {
            return;
        }
        Integer sessionId = (Integer) task.getInfo().get("sessionId");
        if (sessionId < 0) {
            logger.error("Invalid sessionId: {}", sessionId);
            return;
        }
        Statement statement = getSparkStatement(task);
        statement = getLivyClient(task)
                .getSessionStatement(sessionId, statement.getId());
        setSparkStatement(task, statement);

        if (statement.getOutput() != null) {
            String status = statement.getOutput().getStatus();
            // if statement failed
            if (status.toLowerCase().equals("error")) {
                task.setState(TaskState.FAILED);
                task.setErrorReason(statement.getOutput().getEvalue());

                logger.error(statement.getOutput().getEvalue() + "\n");
                logger.error(String.join("\n", statement.getOutput().getTracebackList()));
            } else {
                task.setState(TaskState.SUCCEEDED);

                logger.info(statement.getOutput().getData().getTextPlain());
            }

            getLivyClient(task)
                    .deleteSession(sessionId);
        }
    }

    private void updateSparkApp(TaskInstance task, Logger logger) {
        SparkApp app = getSparkApp(task);
        if (app == null ) {
            return;
        }

        SparkApp remote = getLivyClient(task).getSparkJob(app.getId());
        tailingLog(task, logger);
        setSparkApp(task, remote);
        TaskState currentState = livyToTaskState(remote.getState());
        String appId = remote.getAppId();

        if (!StringUtil.isNullOrEmpty(appId)) {
            Application application = getSparkClient(task).getApp(appId);

            logger.debug("App Info: {}", JSONUtil.messageToString(application));
            String appState = application.getApp().getState();
            if (TaskState.FINISHED.getName().equals(appState)) {
                appState = application.getApp().getFinalStatus();
            }
            currentState = TaskState.fromName(appState);

            if (currentState != null
                    && TaskState.END_STATES.contains(currentState)) {
                String diagnostics = application.getApp().getDiagnostics();
                if (!StringUtil.isNullOrEmpty(diagnostics)) {
                    logger.error("Task failed: \n", diagnostics);
                    task.setErrorReason(diagnostics);
                }
                logger.info(String.join("\n", remote.getLogList()));
            }
        }
        if (null != currentState) {
            task.setState(currentState);
        }
    }

    private void tailingLog(TaskInstance task, Logger logger) {
        SparkApp app = getSparkApp(task);
        Integer from = 0;

        final String logKey = "livyLogFrom";
        if (task.getInfo().containsKey(logKey)) {
            from = (Integer) task.getInfo().get(logKey);
        }
        LogInfo logInfo = getLivyClient(task)
                .getSparkJoblog(app.getId(), from);
        task.getInfo().put(logKey, from + logInfo.getLogList().size());
        logger.info(String.join("\n", logInfo.getLogList()));
    }

    private LivyClient getLivyClient(TaskInstance task) {
        FlowRun flowRun = task.getFlowRun();
        if (flowRun != null && flowRun.getConfig()
                .containsKey(TaskConfig.LIVY_HOST)) {
            // if task is executed in flow and livy host configured
            String master = flowRun.getConfig()
            .get(TaskConfig.LIVY_HOST).toString();

            return new LivyClient(master);
        } else {
            return livyClient;
        }
    }

    private TaskState livyToTaskState(String state) {
        switch (state) {
            case "not_started":
            case "starting":
            case "busy":
            case "idle":
                return TaskState.RUNNING;
            case "shutting_down":
            case "killed":
                return TaskState.KILLED;
            case "dead":
            case "error":
                return TaskState.FAILED;
            case "success":
                return TaskState.SUCCEEDED;
        }
        return TaskState.FINISHED;
    }

    private SparkClient getSparkClient(TaskInstance task) {
        FlowRun flowRun = task.getFlowRun();
        if (flowRun != null && flowRun.getConfig()
                .containsKey(TaskConfig.SPARK_MASTER)) {
            // if task is executed in flow and livy host configured
            String master = flowRun.getConfig()
                    .get(TaskConfig.SPARK_MASTER).toString();
            String port = flowRun.getConfig()
                    .get(TaskConfig.SPARK_PORT).toString();
            return new SparkClient(master, Integer.parseInt(port));
        } else {
            return sparkClient;
        }
    }

    private SparkApp getSparkApp(TaskInstance task) {
        if (!task.getInfo().containsKey("job")) {
            return null;
        }
        try {
            return (SparkApp) JSONUtil.toMessage(
                    task.getInfo().get("job").toString(),
                    SparkApp.newBuilder());
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Cannot resolve spark app");
        }
    }

    private void setSparkApp(TaskInstance task, SparkApp app) {
        JSONObject taskInfo = task.getInfo();
        taskInfo.put("job", JSONUtil.messageToString(app));
        task.setInfo(taskInfo);
    }

    private Statement getSparkStatement(TaskInstance task) {
        if (!task.getInfo().containsKey("stat")) {
            return null;
        }
        try {
            return (Statement) JSONUtil.toMessage(
                    task.getInfo().get("stat").toString(),
                    Statement.newBuilder());
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Cannot resolve spark statement");
        }
    }

    private void setSparkStatement(TaskInstance task, Statement statement) {
        JSONObject taskInfo = task.getInfo();
        taskInfo.put("stat", JSONUtil.messageToString(statement));
        task.setInfo(taskInfo);
    }

    @Override
    public String getCommand(FlowTask task) {
        Operator operator = task.getOperator();

        JSONObject platformConfig = operator.getPlatformConfig();
        String jars = platformConfig.get("jars").toString();
        String application = platformConfig.get("application").toString();
        String args = platformConfig.get("args").toString();


        String sparkParams = args + " " + String.join(" ", task.getParams());
        return getCommand(jars, application, sparkParams, task.getName());
    }

    public String getCommand(String jars,
                             String applicationClass,
                             String applicationParam,
                             String sparkApplicationName) {
        return getCommand(jars,
                applicationClass,
                applicationParam,
                sparkApplicationName,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    public String getCommand(String jars,
                             String applicationClass,
                             String applicationParam,
                             String sparkApplicationName,
                             String sparkMaster,
                             String sparkQueue,
                             String sparkDeployMode,
                             Map<String, String> driverConfDict,
                             Map<String, String>  sparkConfDict,
                             Boolean wait_completion) {
        if (sparkMaster == null) sparkMaster = "yarn";
        if (sparkQueue == null) sparkMaster = "default";
        if (sparkDeployMode == null) sparkMaster = "client";
        if (sparkApplicationName == null) sparkMaster = "spark app";

        if (sparkDeployMode.equals("cluster") || wait_completion ) {
            sparkConfDict.put("spark.yarn.submit.waitAppCompletion" , "false");
        }
        StringBuilder sparkConfiguration = new StringBuilder();
        if (sparkConfDict != null && !sparkConfDict.isEmpty()) {
            for (String key : sparkConfDict.keySet()) {
                sparkConfiguration.append(String.format(" --conf %s=%s ", key, sparkConfDict.get(key)));
            }
        }

        StringBuilder driverConfiguration = new StringBuilder();
        if (driverConfDict != null && !driverConfDict.isEmpty()) {
            for (String key : driverConfDict.keySet()) {
                driverConfiguration.append(String.format(" --%s %s ", key, driverConfDict.get(key)));
            }
        }

        List<String> cmd = new ArrayList<>();
        cmd .add("spark-submit");
        cmd.add(String.format("--name %s", sparkApplicationName));
        cmd.add(String.format("--master %s", sparkMaster));
        cmd.add(String.format("--queue %s", sparkQueue));
        cmd.add(String.format("--deploy-mode %s", sparkDeployMode));
        cmd.add(driverConfiguration.toString());
        cmd.add(sparkConfiguration.toString());
        cmd.add(String.format("--class %s", applicationClass));
        cmd.add(String.format("--jars %s", jars));
        cmd.add(applicationParam);

        return String.join(" ", cmd);
    }
}
