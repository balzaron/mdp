package com.miotech.mdp.flow.constant;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miotech.mdp.common.constant.CommonConstant;

import java.util.HashMap;
import java.util.Map;

@JsonSerialize(using = ConstantSerializer.class)
public enum Platform implements CommonConstant {

    BASH("bash"),
    DOCKER("docker"),
    SPARK("spark"),
    BUILTIN("builtin");

    private static final Map<String, Platform> namingMap = new HashMap<>();
    static {
        for (Platform model : Platform.values()) {
            namingMap.put(model.getName(), model);
        }
    }

    private String name;
    Platform(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Platform fromName(String name) {
        return namingMap.get(name);
    }
}
