package com.miotech.mdp.flow.service.impl;

import ch.qos.logback.classic.Logger;
import com.miotech.mdp.flow.constant.TaskConfig;
import com.miotech.mdp.flow.constant.TaskState;
import com.miotech.mdp.flow.entity.dao.FlowTask;
import com.miotech.mdp.flow.entity.dao.Operator;
import com.miotech.mdp.flow.entity.dao.TaskInstance;
import com.miotech.mdp.flow.service.ExecutorService;
import com.miotech.mdp.flow.service.TaskService;
import com.miotech.mdp.flow.util.CommandUtil;
import com.miotech.mdp.flow.util.LoggingUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class BashExecutor implements ExecutorService {

    @Autowired
    private TaskService taskService;

    @Override
    public void executeTask(TaskInstance task) {
        Logger logger = LoggingUtil.getFileLogger(task.getLogPath());
        String commandPath = task.getLogPath().replaceAll("\\.log$", ".cmd");

        String command = String.join(" ", getCommandAndArgs(task.getFlowTask()));
        logger.info("run command: {}", command);
        int exitCode = 0;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(commandPath));
            writer.write(command);
            writer.close();

            List<String> commands = Arrays.asList("sh", commandPath);
            Process process = CommandUtil.executeCommand(commands, null);

            Long pid = CommandUtil.getPidOfProcess(process);
            if (pid > 0) {
                JSONObject config = new JSONObject();
                config.put(TaskConfig.PROCESS_ID, pid);
                task.setInfo(config);
                task.setState(TaskState.RUNNING);
                taskService.save(task);
            }
            logProcess(logger, process);
            process.waitFor();
            exitCode = process.exitValue();
        } catch (Exception e) {
            logger.error("Failed to execute task: {}, due to {}", task.getId(), e);
            exitCode = 1;
            task.setErrorReason(e.getMessage());
        } finally {
            new File(commandPath).delete();
        }
        task.setEndTime(LocalDateTime.now());
        if (exitCode != 0) {
            task.setState(TaskState.FAILED);
        } else {
            logger.info("Bash task finished");
            task.setState(TaskState.FINISHED);
        }
        logger.info("Task exit with return code: {}", exitCode);
    }

    @Override
    public void updateTask(TaskInstance task) {

    }

    @Override
    public void cancelTask(TaskInstance task) {
        Logger logger = LoggingUtil.getFileLogger(task.getLogPath());
        try {
            if (task.isRunning()) {
                Long pid = (Long) task.getInfo().get(TaskConfig.PROCESS_ID);
                Process killProcess = CommandUtil.executeCommand(
                        Arrays.asList("taskkill", "/F", "/PID ", pid.toString()),
                        task.getLogPath());

                logProcess(logger, killProcess);
            }
        } catch (Exception e) {
            logger.error("Failed to cancel task: {}, due to {}", task.getId(), e);
        }
    }

    @Override
    public String getCommand(FlowTask task) {
        return String.join(" ", getCommandAndArgs(task));
    }

    private void logProcess(Logger logger, Process process) throws IOException {
        var reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            logger.info(line);
        }
    }
    private List<String> getCommandAndArgs(FlowTask task) {
        Operator operator = task.getOperator();

        JSONObject platformConfig = operator.getPlatformConfig();
        String command = platformConfig.get("command").toString();
        List<String> commands = new ArrayList<>();
        commands.addAll(Collections.singletonList(command));
        commands.addAll(task.getParams());
        return commands;
    }

}
