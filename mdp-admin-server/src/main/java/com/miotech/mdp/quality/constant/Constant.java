package com.miotech.mdp.quality.constant;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * @author: shanyue.gao
 * @date: 2020/2/26 10:02 AM
 */
public interface Constant {
    DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd[[' 'HH][:mm][:ss]]")
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 8)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
            .toFormatter();

    String VALIDATE_RESULT = "validate_result";
}
