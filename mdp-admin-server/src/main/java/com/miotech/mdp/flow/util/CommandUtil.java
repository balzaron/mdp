package com.miotech.mdp.flow.util;

import com.miotech.mdp.common.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public class CommandUtil {

    /**
     *
     * @param commands
     * @param workingDir
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static Process executeCommand(List<String> commands, String workingDir)
            throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();

        builder.command(commands);
        // set user home as working directory
        if (!StringUtil.isNullOrEmpty(workingDir)) {
            File f = new File(workingDir);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
            }
            builder.directory(f);
        } else {
            String homeDir = System.getProperty("user.home");
            builder.directory(new File(homeDir));
        }
        return builder.start();
    }

    public static Process executeRuntimeCommand(String command, String workingDir)
            throws IOException {

        return Runtime.getRuntime().exec(command);
    }


    public static synchronized long getPidOfProcess(Process p) {
        long pid = -1;
        try {
            if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
                Field f = p.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                pid = f.getLong(p);
                f.setAccessible(false);
            }
        } catch (Exception e) {
            pid = -1;
        }
        return pid;
    }
}
