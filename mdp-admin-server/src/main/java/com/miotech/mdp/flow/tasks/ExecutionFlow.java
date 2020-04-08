package com.miotech.mdp.flow.tasks;


import com.google.common.collect.Lists;
import com.miotech.mdp.flow.entity.dao.Flow;
import com.miotech.mdp.flow.entity.dao.FlowRun;
import com.miotech.mdp.flow.entity.dao.TaskInstance;
import com.miotech.mdp.flow.service.AlertService;
import com.miotech.mdp.flow.service.FlowRunService;
import com.miotech.mdp.flow.service.FlowService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Component
@Slf4j
public class ExecutionFlow {

    @Autowired
    private FlowRunService flowRunService;

    @Autowired
    private FlowService flowService;

    @Autowired
    private AlertService alertService;

    @Scheduled(cron = "0/10 * * * * ?")
    public void runAndUpdateTask() {
        flowRunService.getAllRunning()
                .forEach(x -> {
                   FlowRun flowRun = flowRunService.processFlowRun(x);
                   try {
                       alertFlowStatus(flowRun);
                   } catch (Exception e) {
                       log.error("Error in alert flow state: ", e);
                   }
                });
    }

    private void alertFlowStatus(FlowRun flowRun) {
        Flow flow = flowService.find(flowRun.getFlowId());
        String[] users = flow.getUserIds();
        List<String> userIds = Lists.newArrayList();
        if (ArrayUtils.isNotEmpty(users)) {
            userIds.addAll(Arrays.asList(users));
        }
        if (flowRun.getCreatorId() != null) {
            userIds.add(flowRun.getCreatorId());
        }

        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(" Flow \"");
        messageBuilder.append(flow.getName());
        messageBuilder.append("\" in state: ");
        messageBuilder.append(flowRun.getState().getName());
        messageBuilder.append("\n");

        switch (flowRun.getState()) {
            case FAILED:
                List<String> failedTasks = flowRun.getTaskSet()
                        .stream()
                        .filter(TaskInstance::isFailed)
                        .map (x -> String.format("\"%s\" state: %s, error: %s", x.getName(), x.getState().getName(), x.getErrorReason()))
                        .collect(Collectors.toList());
                messageBuilder.append( String.join( "\n", failedTasks));
                alertService.sendMessageToUserIds(messageBuilder.toString(), userIds);
                break;

            case FINISHED:
                alertService.sendMessageToUserIds(messageBuilder.toString(), userIds);
                break;
        }
    }
}
