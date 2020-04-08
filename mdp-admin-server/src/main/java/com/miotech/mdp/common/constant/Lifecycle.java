package com.miotech.mdp.common.constant;

import java.util.HashMap;
import java.util.Map;

public enum Lifecycle {

    COLLECTION("Collection"),

    STORAGE("Storage"),

    PROCESS("Process"),

    ANALYSIS("Analysis"),

    RETIRE("Retire");

    private static final Map<String, Lifecycle> namingMap = new HashMap<>();
    static {
        for (Lifecycle lifecycle : Lifecycle.values()) {
            namingMap.put(lifecycle.getName(), lifecycle);
        }
    }

    private final String name;
    Lifecycle(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Lifecycle fromName(String name) {
        return namingMap.get(name);
    }
}
