package com.miotech.mdp.flow.operators;

import ch.qos.logback.classic.Logger;
import com.miotech.mdp.common.client.spark.SparkClient;
import com.miotech.mdp.common.models.protobuf.livy.App;
import com.miotech.mdp.flow.constant.TaskState;
import com.miotech.mdp.flow.entity.dao.FlowTask;
import com.miotech.mdp.flow.entity.dao.TaskInstance;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SparkJobAvailabilityOperator extends AbstractTaskOperator  {

    public void run(TaskInstance instance, Logger logger) {
        FlowTask flowTask = instance.getFlowTask();
        JSONObject conf = flowTask.getConfig();
        String host = conf.get("host").toString();
        String jobName = conf.get("name").toString();
        int port = 8088;
        if (conf.containsKey("port")) {
            port = Integer.parseInt((String) conf.get("port"));
        }

        List<String> states = null;
        if (conf.containsKey("states")) {
            states = (List<String>) conf.get("states");
        } else {
            states = Arrays.asList(
                    TaskState.RUNNING.getName(),
                    TaskState.ACCEPTED.getName(),
                    TaskState.SUBMITTED.getName()
            );
        }
        SparkClient sparkClient = new SparkClient(host, port);

        List<App> apps = sparkClient.getApplicationsByStates(states);
        if (apps.size()>0) {
            for(App application : apps){
                String appName = application.getName();
                String state = application.getState();
                String id = application.getId();

                logger.info("Application {}: \"{}\" in state: \"{}\"", id, appName, state);
            }
            instance.setState(TaskState.SUCCEEDED);
        } else {
            logger.error("Did not find active application : \"{}\"", jobName);
            instance.setState(TaskState.FAILED);
        }
    }

}
