package com.miotech.mdp.flow.service;

import com.google.common.io.Files;
import com.miotech.mdp.flow.entity.bo.AirflowOperatorParam;
import com.miotech.mdp.flow.entity.dao.Flow;
import com.miotech.mdp.flow.util.AirflowRestTemplate;
import com.miotech.mdp.flow.util.AirflowUtil;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.Version;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AirflowService {

    @Value("${airflow.docker-name}")
    private String DOCKER_NAME;

    @Value("${airflow.enable: false}")
    private Boolean ENABLE_AIRFLOW;

    @Autowired
    AirflowRestTemplate airflowRestTemplate;

    @Autowired
    FlowService flowService;

    @Autowired
    @Lazy
    ExecutorServiceProvider executorServiceProvider;

    @Autowired
    Version freemarkerVersion;

    @Autowired
    Template template;

    public void test() {
        airflowRestTemplate.test();
    }

    public void createDAG(String flowId) {
        if (!ENABLE_AIRFLOW) return;
        AirflowUtil.transferFile(buildDAGFile(flowId));
        airflowRestTemplate.refreshDAG(flowId);
    }

    private File buildDAGFile(String flowId) {
        Flow flow = flowService.find(flowId);
        Map<String, Object> dagParam = new HashMap<>();
        List<AirflowOperatorParam> airflowOperatorParams = flow.getFlowTasks().stream().map(flowTask -> AirflowOperatorParam.builder()
                .id(flowTask.getId())
                .bash_command(executorServiceProvider.getTaskCommand(flowTask))
                .retries(flowTask.getRetryPolicy())
                .trigger_rule(flowTask.getTriggerRule())
                .parent_ids(Arrays.asList(flowTask.getParentTaskIds()))
                .build()).collect(Collectors.toList());
        dagParam.put(AirflowUtil.DAG_ID_KEY, flow.getId());
        dagParam.put(AirflowUtil.DAG_SCHEDULE_INTERVAL_KEY, flow.getExecutionScheduler());
        dagParam.put(AirflowUtil.TASKS_KEY, airflowOperatorParams);
        String fileName = AirflowUtil.LOCAL_DAG_OUTPUT_PATH + File.separator + flowId + ".py";
        File newFile = new File(fileName);
        try {
            template.process(dagParam, Files.newWriter(newFile, Charset.defaultCharset()), new BeansWrapperBuilder(freemarkerVersion).build());
        } catch (Exception e) {
            log.error("Failed to generate dag file.", e);
            throw new RuntimeException("Failed to generate dag file.", e);
        }
        return newFile;
    }

    public void unpauseDAG(String dagId) {
        if (!ENABLE_AIRFLOW) return;
        AirflowUtil.executeCLI("docker exec -i " + DOCKER_NAME + " airflow unpause " + dagId);
    }

    public void pauseDAG(String dagId) {
        if (!ENABLE_AIRFLOW) return;
        AirflowUtil.executeCLI("docker exec -i " + DOCKER_NAME + " airflow pause " + dagId);
    }

    public void deleteDAG(String dagId) {
        if (!ENABLE_AIRFLOW) return;
        AirflowUtil.executeCLI("rm " + AirflowUtil.AIRFLOW_DAG_PATH + File.separator + dagId + ".py");
        AirflowUtil.executeCLI("docker exec -i " + DOCKER_NAME + " rm " + AirflowUtil.DOCKER_DAG_PYCACHE_PATH + File.separator + dagId + ".cpython-36.pyc");
        airflowRestTemplate.deleteDAG(dagId);
    }
}
