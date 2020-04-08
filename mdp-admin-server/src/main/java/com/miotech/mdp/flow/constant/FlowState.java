package com.miotech.mdp.flow.constant;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miotech.mdp.common.constant.CommonConstant;

import java.util.HashMap;
import java.util.Map;

@JsonSerialize(using = ConstantSerializer.class)
public enum FlowState implements CommonConstant {
    CREATED("CREATED"),
    KILLED("KILLED"),
    PAUSED("PAUSED"),
    RUNNING("RUNNING"),
    FINISHED("FINISHED"),
    FAILED("FAILED");

    private static final Map<String, FlowState> namingMap = new HashMap<>();
    static {
        for (FlowState model : FlowState.values()) {
            namingMap.put(model.getName(), model);
        }
    }

    private String name;
    FlowState(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static FlowState fromName(String name) {
        return namingMap.get(name);
    }

    public static boolean isValidName(String name) {
        return namingMap.containsKey(name);
    }
}
