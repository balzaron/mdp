package com.miotech.mdp.flow.constant;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miotech.mdp.common.constant.CommonConstant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonSerialize(using = ConstantSerializer.class)
public enum TaskState implements CommonConstant {
    CREATED("CREATED"),
    KILLED("KILLED"),
    PENDING("PENDING"),
    RUNNING("RUNNING"),
    FINISHED("FINISHED"),
    FAILED("FAILED"),
    SUBMITTED("SUBMITTED"),
    SUCCEEDED("SUCCEEDED"),
    ACCEPTED("ACCEPTED"),
    UPRETRY("UPRETRY"),
    UPSTREAM_FAILED("UPSTREAM_FAILED"),
    DETACHED("DETACHED");

    private static final Map<String, TaskState> namingMap = new HashMap<>();
    static {
        for (TaskState model : TaskState.values()) {
            namingMap.put(model.getName(), model);
        }
    }

    public static final List<TaskState> RUNNING_STATES = Arrays.asList(RUNNING, ACCEPTED, PENDING, SUBMITTED);

    public static final List<TaskState> END_STATES = Arrays.asList(KILLED, FAILED, UPSTREAM_FAILED, FINISHED, SUCCEEDED, DETACHED);

    public static final List<TaskState> FAILED_STATES = Arrays.asList(KILLED, FAILED, UPSTREAM_FAILED, DETACHED);

    public static final List<TaskState> UP_FOR_RETRY_STATES = Arrays.asList(FAILED, DETACHED);

    private String name;
    TaskState(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static TaskState fromName(String name) {
        return namingMap.get(name);
    }
}
