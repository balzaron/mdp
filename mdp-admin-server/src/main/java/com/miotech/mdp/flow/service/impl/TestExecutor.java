package com.miotech.mdp.flow.service.impl;

import ch.qos.logback.classic.Logger;
import cn.hutool.core.util.StrUtil;
import com.miotech.mdp.flow.config.TaskExecutionConfig;
import com.miotech.mdp.flow.constant.TaskState;
import com.miotech.mdp.flow.entity.dao.FlowTask;
import com.miotech.mdp.flow.entity.dao.TaskInstance;
import com.miotech.mdp.flow.service.ExecutorService;
import com.miotech.mdp.flow.util.LoggingUtil;
import com.miotech.mdp.quality.models.entity.CaseValidator;
import com.miotech.mdp.quality.models.entity.DataTestCase;
import com.miotech.mdp.quality.models.entity.DataTestResult;
import com.miotech.mdp.quality.repository.DataTestCaseRepository;
import com.miotech.mdp.quality.repository.DataTestResultRepository;
import com.miotech.mdp.quality.util.ExecutorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TestExecutor implements ExecutorService {

    @Value("${security.pass-token}")
    private String passToken;

    @Autowired
    TaskExecutionConfig taskExecutionConfig;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    private DataTestResultRepository dataTestResultRepository;

    @Autowired
    private DataTestCaseRepository caseRepository;

    @Autowired
    private ExecutorUtil testUtil;

    @Override
    public void executeTask(TaskInstance task) {
        Logger logger = LoggingUtil.getFileLogger(task.getLogPath());

        String testId = task.getFlowTask()
                .getTestId();

        try {
            Long starTime = System.currentTimeMillis();
            DataTestCase testCase = caseRepository.getOne(testId);
            List<CaseValidator> validators = testCase.getValidateObject();
            List<String> validateLog = validators.stream().map(v-> StrUtil.format("{} {} {}", v.getFieldName(), v.getOperator(), v.getExpected())).collect(Collectors.toList());
            logger.info("running case id is: {}, name is: {}", testId, testCase.getName());
            logger.info(" expected expression is: {} start time is: {}", validateLog, LocalDateTime.now().toString());
            logger.info("executed SQL is: {}", testCase.getCaseSql());

            List<Future<DataTestResult>> resultList = testUtil.submit(Collections.singletonList(testId));
            if (resultList.isEmpty()) {
                task.setErrorReason("No result found for this test");
                task.setState(TaskState.FAILED);
                task.setEndTime(LocalDateTime.now());
            }

            Future<DataTestResult> future = resultList.get(0);
            DataTestResult result = future.get();
            Long endTime = System.currentTimeMillis();
            String isPass = result.getPassed()? "successfully" : "failed";
            logger.warn("case executed {}! error log is: {}", isPass, result.getErrorCatchLog());
            logger.info("case end time is: {}, case consumed {} ms", LocalDateTime.now().toString(), endTime-starTime);
            if (result.getPassed()) {
                task.setState(TaskState.SUCCEEDED);
                task.setEndTime(LocalDateTime.now());
            } else {
                task.setErrorReason(result.getErrorCatchLog());
                task.setState(TaskState.FAILED);
                task.setEndTime(LocalDateTime.now());
            }
        } catch (Exception e) {
            task.setErrorReason("Test failed: " + e);
            task.setState(TaskState.FAILED);
            task.setEndTime(LocalDateTime.now());

        }
    }

    @Override
    public void updateTask(TaskInstance task) {

    }

    @Override
    public void cancelTask(TaskInstance task) {
        task.setState(TaskState.KILLED);
    }

    @Override
    public String getCommand(FlowTask task) {
        String flowId = task.getFlowId();
        String flowTskID = task.getId();
        String apiUrl = taskExecutionConfig.getMdpHost();

        String api = String.format("task_id=$(curl -s -X POST %s/api/flow/%s/tasks/%s/execute?pass-token=%s | jq -r .result.id);", apiUrl,flowId, flowTskID, passToken);
        api += "\n" + String.format("  while true;    " +
                "do sleep 5;   " +
                "task_state=$(curl -s -XGET  %s/api/task/\"$task_id\"?pass-token=%s | jq -r .result.state);" +
                "if [ \"$task_state\" == 'SUCCEEDED' ]; then   " +
                "     echo $task_state;   exit; " +
                "else" +
                "     if [ \"$task_state\" == 'FAILED' ]; then" +
                "         echo $task_state;         " +
                "         exit -1;     " +
                "      else         " +
                "         echo 'WAITING for task complete';     " +
                "      fi; " +
                "fi; " +
                "done", apiUrl, passToken);
        return api;
    }
}
