package com.miotech.mdp.flow.constant;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miotech.mdp.common.constant.CommonConstant;

import java.util.HashMap;
import java.util.Map;

@JsonSerialize(using = ConstantSerializer.class)
public enum ParameterType implements CommonConstant {

    CHOICES("choices"),
    STRING("string"),
    MULTIPLE_STRING("multiple-string"),
    DATE("date"),
    TEXT("text"),
    DATABASEID("databaseId"),
    TABLEID("tableId"),
    MULTIPLE_TABLEID("multiple-tableId"),
    NUMBER("number");

    private static final Map<String, ParameterType> namingMap = new HashMap<>();
    static {
        for (ParameterType model : ParameterType.values()) {
            namingMap.put(model.getName(), model);
        }
    }

    private String name;
    ParameterType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ParameterType fromName(String name) {
        return namingMap.get(name);
    }
}
