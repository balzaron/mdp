package com.miotech.mdp.flow.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import com.google.common.hash.Hashing;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class LoggingUtil {

    public static Logger getFileLogger(String logPath) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        String sha256hex = Hashing.sha256()
                .hashString(logPath, StandardCharsets.UTF_8)
                .toString();

        Logger logbackLogger = loggerContext.getLogger("Main - " + sha256hex);
        logbackLogger.setAdditive(false);
        if (logbackLogger.getAppender(logPath) != null) {
            return logbackLogger;
        }

        FileAppender fileAppender = new FileAppender();
        fileAppender.setContext(loggerContext);
        fileAppender.setName(logPath);
        // set the file name
        fileAppender.setFile(logPath);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%date [%level] in %thread - %msg%n");
        encoder.start();

        fileAppender.setEncoder(encoder);
        fileAppender.start();

        logbackLogger.addAppender(fileAppender);
        return logbackLogger;
    }
}
