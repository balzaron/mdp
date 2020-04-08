package com.miotech.mdp.flow.entity.vo;

import com.miotech.mdp.flow.entity.dao.FlowRun;
import lombok.Data;

import java.util.List;

@Data
public class FlowState {
    private String flowId;

    private List<FlowRunStateCount> runStats;

    private List<TaskStateCount> taskStats ;

    private FlowRun latestRun;
}
