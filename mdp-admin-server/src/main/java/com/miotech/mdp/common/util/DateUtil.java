package com.miotech.mdp.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static String dateTimeMillisToString(Long dateTime) {
        return Instant.ofEpochMilli(dateTime).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public static Long dateTimeToMillis(String dateTime) {
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static Long dateTimeToMillis(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
