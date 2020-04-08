package com.miotech.mdp.common.client.spark;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SparkJob {

    /**
     * 必须有
     * 包含需要执行应用的文件，主要是jar包
     */
    private String file;
    /**
     * User to impersonate when running the job
     */
    private String proxyUser;
    /**
     * Application Java/Spark main class
     * 主类
     */
    private String className;

    /**
     * Command line arguments for the application
     * 参数
     */
    private List<String> args;

    /**
     * jars to be used in this session
     * 这个任务里面用到的其他 jar 包
     */
    private List<String> jars;

    /**
     * Python files to be used in this session
     */
    private List<String> pyFiles;
    /**
     * files to be used in this session
     */
    private List<String> files;

    /**
     * Amount of memory to use for the driver process
     */
    private String driverMemory;

    /**
     * Number of cores to use for the driver process
     */
    private int driverCores;

    /**
     * Amount of memory to use per executor process
     */
    private String executorMemory;
    /**
     * Number of cores to use for each executor
     */
    private int executorCores;
    /**
     * Number of executors to launch for this session
     */
    private int numExecutors;
    /**
     * Archives to be used in this session
     */
    private List<String> archives;
    /**
     * The name of the YARN queue to which submitted
     */
    private String queue;
    /**
     * The name of this session
     * 任务名称
     */
    private String name;
    /**
     * Spark configuration properties
     * spark 配置文件
     */
    private Map<String, String> conf;
}