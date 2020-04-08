package com.miotech.mdp.common.constant;

import java.util.HashMap;
import java.util.Map;

public enum  GraphDirection {

    INPUT("input", "<-", "-"),

    OUTPUT("output", "-", "->"),

    BOTH("both", "<-", "->");

    private static final Map<String, GraphDirection> namingMap = new HashMap<>();
    static {
        for (GraphDirection direction : GraphDirection.values()) {
            namingMap.put(direction.getName(), direction);
        }
    }

    private String name;
    private String leftSide;
    private String rightSide;
    GraphDirection(String name,
                   String leftSide,
                   String rightSide) {
        this.name = name;
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public String getName() {
        return name;
    }

    public String getLeftSide() {
        return leftSide;
    }

    public String getRightSide() {
        return rightSide;
    }

    public static GraphDirection fromName(String name) {
        return namingMap.get(name);
    }
}
