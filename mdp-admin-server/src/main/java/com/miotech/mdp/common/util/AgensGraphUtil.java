package com.miotech.mdp.common.util;

import org.json.simple.JSONObject;

import java.util.Map;

public class AgensGraphUtil {

    public static JSONObject jsonValueToObject(Object jsonValue) {
        if (jsonValue instanceof Map) {
            return new JSONObject((Map) jsonValue);
        }
        return null;
    }
}
