package com.miotech.mdp.common.constant;

import java.util.HashMap;
import java.util.Map;

public enum Model {

    TABLE("table");

    private static final Map<String, Model> namingMap = new HashMap<>();
    static {
        for (Model model : Model.values()) {
            namingMap.put(model.getName(), model);
        }
    }

    private String name;
    Model(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Model fromName(String name) {
        return namingMap.get(name);
    }
}
