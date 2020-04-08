package com.miotech.mdp.flow.util;

import ch.ethz.ssh2.Connection;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Slf4j
@Component
public class AirflowUtil {

    public static final String LOCAL_DAG_OUTPUT_PATH = System.getProperty("user.home") + "/airflow/dags";
    public static final String DOCKER_DAG_PYCACHE_PATH = "/home/airflow/dags/__pycache__";
    public static final String DAG_TEMPLATE_FILE_NAME = "airflow-dag-template.py";

    public static final String DAG_ID_KEY = "dag_id";
    public static final String DAG_SCHEDULE_INTERVAL_KEY = "dag_schedule_interval";
    public static final String TASKS_KEY = "tasks";

    private static String SSH_HOST;

    private static int SSH_PORT;

    private static String SSH_USERNAME;

    public static String AIRFLOW_DAG_PATH;

    private static Interner<String> writeFileInterner = Interners.newWeakInterner();

    @Value("${airflow.ssh.host}")
    public void setSSHHost(String sshHost) {
        SSH_HOST = sshHost;
    }

    @Value("${airflow.ssh.port}")
    public void setSSHPort(int sshPort) {
        SSH_PORT = sshPort;
    }

    @Value("${airflow.ssh.username}")
    public void setSSHUsername(String username) {
        SSH_USERNAME = username;
    }

    @Value("${airflow.dag-path}")
    public void setAirflowDagPath(String airflowDagPath) {
        AIRFLOW_DAG_PATH = airflowDagPath;
    }

    public static void transferFile(File localFile) {
        Connection connection = null;
        try {
            connection = SSHUtil.loginByPublicKey(SSH_HOST, SSH_PORT, SSH_USERNAME, null);
            connection.createSCPClient().put(localFile.getAbsolutePath(), AIRFLOW_DAG_PATH, "0644");
        } catch (IOException e) {
            log.error("Transfer file failed.", e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

    }

    public static void executeCLI(String command) {
        Connection connection = null;
        try {
            connection = SSHUtil.loginByPublicKey(SSH_HOST, SSH_PORT, SSH_USERNAME, null);
            SSHUtil.execute(connection, command);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

    }
}
