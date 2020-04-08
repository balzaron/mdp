package com.miotech.mdp.flow.service.impl;

import com.miotech.mdp.flow.constant.TaskState;
import com.miotech.mdp.flow.entity.dao.FlowTask;
import com.miotech.mdp.flow.entity.dao.Operator;
import com.miotech.mdp.flow.entity.dao.TaskInstance;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.api.model.batch.JobBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.fabric8.kubernetes.client.utils.Utils;
import org.json.simple.JSONObject;
import org.junit.Rule;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class K8sExecutorTest {
    @Rule
    public KubernetesServer server = new KubernetesServer();

    private K8sExecutor k8sExecutor = null;
    @BeforeTest
    public void init () {
        server.before();
        KubernetesClient client = server.getClient();
        k8sExecutor = new K8sExecutor(client);
    }

    @Test
    public void testExecuteTask() {
        TaskInstance task = createTask();

        server.expect().withPath("/apis/batch/v1/namespaces/default/jobs")
                .andReturn(200, new JobBuilder()
                        .withNewMetadata()
                        .withName(task.getId())
                        .withNamespace("default")
                        .endMetadata()
                        .build())
                .once();

        k8sExecutor.executeTask(task);
        assert  task.getInfo() != null;

        server.expect().withPath("/apis/batch/v1/namespaces/default/jobs/" + task.getId())
                .andReturn(200, new JobBuilder()
                        .withNewMetadata()
                        .withName(task.getId())
                        .withNamespace("default")
                        .addToLabels("TASK_ID", "1")
                        .endMetadata()
                        .withNewStatus().withActive(1).endStatus()
                        .build())
                .once();

        k8sExecutor.updateTask(task);
        assert task.getState().equals(TaskState.RUNNING);

        server.expect().withPath("/apis/batch/v1/namespaces/default/jobs/" + task.getId())
                .andReturn(200, new JobBuilder()
                        .withNewMetadata()
                        .withName(task.getId())
                        .withNamespace("default")
                        .addToLabels("TASK_ID", "1")
                        .endMetadata()
                        .withNewStatus().withCompletionTime("2019-1-1").withFailed(1).endStatus()
                        .build())
                .once();
        server.expect().withPath("/api/v1/namespaces/default/pods?labelSelector=" + Utils.toUrlEncoded("TASK_ID=1"))
                .andReturn(200, new PodListBuilder()
                        .addNewItem()
                        .withNewStatus()
                        .addNewContainerStatus()
                        .editOrNewState()
                        .withNewTerminated()
                        .withMessage("")
                        .endTerminated()
                        .endState()
                        .endContainerStatus()
                        .endStatus().and()
                        .build()).once();

        k8sExecutor.updateTask(task);
        assert task.getState().equals(TaskState.FAILED);

        server.expect().withPath("/apis/batch/v1/namespaces/default/jobs/" + task.getId())
                .andReturn(200, new JobBuilder()
                        .withNewMetadata()
                        .withName(task.getId())
                        .withNamespace("default")
                        .addToLabels("TASK_ID", "1")
                        .endMetadata()
                        .withNewStatus().withCompletionTime("2019-1-1").withSucceeded(1).endStatus()
                        .build())
                .once();
        k8sExecutor.updateTask(task);
        assert task.getState().equals(TaskState.SUCCEEDED);
    }

    private TaskInstance createTask() {
        Operator operator = new Operator();
        JSONObject config = new JSONObject();
        config.put("image", "ubuntu");
        config.put("name", "test");
        config.put("command", "echo 1");
        operator.setPlatformConfig(config);

        FlowTask flowTask = new FlowTask();
        flowTask.setOperator(operator);

        TaskInstance task = new TaskInstance();
        task.setId("1");
        task.setMaxRetry(1);
        task.setTryNumber(1);
        task.setFlowTask(flowTask);
        return task;
    }
}