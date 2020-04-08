package com.miotech.mdp.flow.service;

import ch.qos.logback.classic.Logger;
import com.miotech.mdp.common.client.ZhongdaClient;
import com.miotech.mdp.common.util.StringUtil;
import com.miotech.mdp.flow.constant.TaskState;
import com.miotech.mdp.flow.entity.dao.FlowTask;
import com.miotech.mdp.flow.entity.dao.TaskInstance;
import com.miotech.mdp.flow.service.impl.*;
import com.miotech.mdp.flow.util.LoggingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;


@Component
@Slf4j
public class ExecutorServiceProvider {
    @Autowired
    private SparkExecutor sparkExecutor;

    @Autowired
    private BashExecutor bashExecutor;

    @Autowired
    private K8sExecutor k8sExecutor;

    @Autowired
    private TaskService taskService;

    @Autowired
    private BuiltinOperatorExecutor builtinOperatorExecutor;

    @Autowired
    private TestExecutor testExecutor;

    @Autowired
    private ZhongdaClient zhongdaClient;

    @Async
    public TaskInstance execute(TaskInstance instance) {
        Logger logger = LoggingUtil.getFileLogger(instance.getLogPath());
        if (instance.getStartTime() == null) {
            instance.setStartTime(LocalDateTime.now());
        }
        saveState(instance, TaskState.ACCEPTED);
        try{
            logger.info("Started to execute task: {}", instance.getName());
            getExecutorService(instance.getFlowTask())
                    .executeTask(instance);
        } catch (Exception e) {
            instance.setErrorReason(e.getMessage());
            logger.error("Failed to execute task: {}", instance.getName());
            logger.error("Error Cause\n:", e);
            saveState(instance, TaskState.FAILED);
        }
        instance.setUpdateTime(LocalDateTime.now());
        alterOnTaskState(instance);
        return taskService.save(instance);
    }

    public TaskInstance update(String taskId) {
        TaskInstance instance = taskService.find(taskId);
        if (instance.isEnded()) {
            return  instance;
        }
        try {
            getExecutorService(instance.getFlowTask())
                    .updateTask(instance);
        } catch (Exception e) {
            Logger logger = LoggingUtil.getFileLogger(instance.getLogPath());
            logger.error("Failed to update task: {}", instance.getName());
            logger.error("Error Cause:", e);

            // TODO: if lost connection out of 30 minutes
          instance.setState(TaskState.DETACHED);
          instance.setErrorReason("Lost task instance state: " + e);
        }

        if (instance.isEnded() && instance.getEndTime() == null) {
            instance.setEndTime(LocalDateTime.now());
        }
        instance.setUpdateTime(LocalDateTime.now());
        alterOnTaskState(instance);
        return taskService.save(instance);
    }

    @Async
    public TaskInstance cancel(TaskInstance instance) {
        Logger logger = LoggingUtil.getFileLogger(instance.getLogPath());
        logger.info("Trying to kill task: {}", instance.getName());

        if (instance.isEnded()) {
            return  instance;
        }
        getExecutorService(instance.getFlowTask())
                .cancelTask(instance);
        saveState(instance, TaskState.KILLED);
        if (instance.getEndTime() == null) {
            instance.setEndTime(LocalDateTime.now());
        }
        instance.setUpdateTime(LocalDateTime.now());
        alterOnTaskState(instance);
        return taskService.save(instance);
    }

    public String getTaskCommand(FlowTask flowTask) {
        return getExecutorService(flowTask)
                .getCommand(flowTask);
    }

    private void alterOnTaskState(TaskInstance task) {
        try {
            if (task.isEnded()
                    && task.getFlowRun() == null
                    && !StringUtil.isNullOrEmpty(task.getCreatorId())) {
                String msg = String.format("Task \"%s\" - %s in state: %s ",
                        task.getName(), task.getId(), task.getState().getName());
                if (task.isFailed()) {
                    msg = msg + "\n\nError Reason:\n" + task.getErrorReason();
                }

                zhongdaClient.sendMessage(
                        msg,
                        "",
                        Collections.singletonList(task.getCreator().getUsername()));
            }
        } catch (Exception e) {
            log.info("Failed to send alert message: ", e);
        }
    }

    private void saveState(TaskInstance instance, TaskState state) {
        instance.setState(state);
        taskService.save(instance);
    }

    private ExecutorService getExecutorService(FlowTask flowTask) {

        if (flowTask.getOperator() != null) {
            switch (flowTask.getOperator().getPlatform()) {
                case DOCKER:
                    return k8sExecutor;
                case SPARK:
                    return sparkExecutor;
                case BASH:
                    return bashExecutor;
                case BUILTIN:
                    return builtinOperatorExecutor;
                default:
                    throw new RuntimeException("Platform not supported");
            }
        }
        if (flowTask.getTestId() != null ){
            return testExecutor;
        }
        return null;
    }
}
