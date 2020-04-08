package com.miotech.mdp.flow.service.impl;

import ch.qos.logback.classic.Logger;
import com.miotech.mdp.flow.constant.TaskState;
import com.miotech.mdp.flow.entity.dao.FlowTask;
import com.miotech.mdp.flow.entity.dao.Operator;
import com.miotech.mdp.flow.entity.dao.TaskInstance;
import com.miotech.mdp.flow.operators.AbstractTaskOperator;
import com.miotech.mdp.flow.service.ExecutorService;
import com.miotech.mdp.flow.service.TaskService;
import com.miotech.mdp.flow.util.LoggingUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BuiltinOperatorExecutor implements ExecutorService {
    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    TaskService taskService;

    @Override
    public void executeTask(TaskInstance task) {
        Logger logger = LoggingUtil.getFileLogger(task.getLogPath());

        Operator operator = task.getFlowTask().getOperator();

        JSONObject platformConfig = operator.getPlatformConfig();
        String className = platformConfig.get("className").toString();
        try {
            Class<?> clazz = Class.forName(className);
            logger.info("Execute class: {}", className);
            AbstractTaskOperator taskOperator = (AbstractTaskOperator) applicationContext.getBean(clazz);
            task.setState(TaskState.RUNNING);
            taskService.save(task);
            logger.info("Started to run: {}", task.getName());

            taskOperator.run(task, logger);
            task.setState(TaskState.FINISHED);
        } catch (ClassNotFoundException e) {
            task.setState(TaskState.FAILED);
            task.setErrorReason(e.getMessage());
            logger.error("Builtin Operator Class not found: ", e);
        }
    }

    @Override
    public void updateTask(TaskInstance task) {

    }

    @Override
    public void cancelTask(TaskInstance task) {

    }

    @Override
    public String getCommand(FlowTask task) {
        return null;
    }
}
