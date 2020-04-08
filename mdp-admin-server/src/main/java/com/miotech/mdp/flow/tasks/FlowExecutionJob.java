package com.miotech.mdp.flow.tasks;

import com.miotech.mdp.flow.entity.bo.FlowExecution;
import com.miotech.mdp.flow.entity.dao.Flow;
import com.miotech.mdp.flow.service.FlowRunService;
import com.miotech.mdp.flow.service.FlowService;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class FlowExecutionJob extends QuartzJobBean {
    @Autowired
    private FlowService flowService;

    @Autowired
    private FlowRunService flowRunService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext)
            throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        String flowId = jobDataMap.getString("flowId");
        Flow flow = flowService.find(flowId);
        flowRunService.executeFlow(flow, new FlowExecution());
    }
}
