package com.miotech.mdp.flow.constant;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miotech.mdp.common.constant.CommonConstant;

import java.util.HashMap;
import java.util.Map;

@JsonSerialize(using = ConstantSerializer.class)
public enum TriggerRule implements CommonConstant {
    ALL_SUCCESS("all_success"),
    ONE_SUCCESS("one_success"),
    ONE_FAILED("one_failed");

    private static final Map<String, TriggerRule> namingMap = new HashMap<>();
    static {
        for (TriggerRule model : TriggerRule.values()) {
            namingMap.put(model.getName(), model);
        }
    }

    private String name;
    TriggerRule(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static TriggerRule fromName(String name) {
        return namingMap.get(name);
    }
}
