package com.miotech.mdp.common.util;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.testng.util.Strings;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StringUtil {

    public static String escape(String s) { return escape(s, "\""); }

    public static String escape(String s, String quote) { return quote + s + quote; }

    public static boolean isNullOrEmpty(String s) { return Strings.isNullOrEmpty(s); }

    public static boolean isDuplicate(Iterable<String> s) {
        Map<String, Boolean> map = new HashMap<>();
        for (String v : s) {
            if (map.get(v) != null) {
                return true;
            }
            map.put(v, true);
        }
        return false;
    }
}
