package com.miotech.mdp.flow.constant;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miotech.mdp.common.constant.CommonConstant;

import java.util.HashMap;
import java.util.Map;

@JsonSerialize(using = ConstantSerializer.class)
public enum ParameterExecutionType implements CommonConstant {

    KEYNAME("keyName"),
    VALUES("values"),
    TASKID("taskId"),
    IGNORE("ignore");

    private static final Map<String, ParameterExecutionType> namingMap = new HashMap<>();
    static {
        for (ParameterExecutionType model : ParameterExecutionType.values()) {
            namingMap.put(model.getName(), model);
        }
    }

    private String name;
    ParameterExecutionType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ParameterExecutionType fromName(String name) {
        return namingMap.get(name);
    }
}
