package com.miotech.mdp.flow.service.impl;

import ch.qos.logback.classic.Logger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.util.StringUtils;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.miotech.mdp.common.util.JSONUtil;
import com.miotech.mdp.common.util.StringUtil;
import com.miotech.mdp.flow.config.K8sClientConfig;
import com.miotech.mdp.flow.config.S3ClientConfig;
import com.miotech.mdp.flow.config.TaskExecutionConfig;
import com.miotech.mdp.flow.constant.TaskConfig;
import com.miotech.mdp.flow.constant.TaskState;
import com.miotech.mdp.flow.entity.dao.FlowTask;
import com.miotech.mdp.flow.entity.dao.Operator;
import com.miotech.mdp.flow.entity.dao.TaskInstance;
import com.miotech.mdp.flow.service.ExecutorService;
import com.miotech.mdp.flow.service.S3Service;
import com.miotech.mdp.flow.util.LoggingUtil;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.batch.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.PodResource;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class K8sExecutor implements ExecutorService {

    @Autowired
    TaskExecutionConfig taskExecutionConfig;

    @Autowired
    private KubernetesClient client;

    @Autowired
    private K8sClientConfig config;

    @Autowired
    private S3ClientConfig s3ClientConfig;

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private S3Service s3Service;

    public K8sExecutor(KubernetesClient client) {
        this.client = client;
    }

    @Override
    public void executeTask(TaskInstance task) {
        FlowTask flowTask = task.getFlowTask();
        Operator operator = flowTask.getOperator();

        JSONObject platformConfig = operator.getPlatformConfig();
        String image = platformConfig.get("image").toString();
        String command = platformConfig.get("command").toString();
        String containerName = platformConfig.get("name").toString();
        List<String> commands = new ArrayList<>();
        commands.addAll(Arrays.asList(command.split("\\s+")));
        commands.addAll(flowTask.getParams());

        Map<String, String> labels = new HashMap();
        labels.put(TaskConfig.TASK_ID, task.getId());
        labels.put(TaskConfig.FLOW_TASK_ID, flowTask.getId());
        if (task.getFlowRun()!= null) {
            labels.put(TaskConfig.FLOW_RUN_ID, task.getFlowRun().getId());
        }

        ContainerBuilder containerBuilder = new ContainerBuilder()
                .withCommand(commands)
                .withImage(image)
                .withName(containerName.replaceAll("\\s+", ""));

        PodSpecBuilder podSpecBuilder = new PodSpecBuilder()
                .withRestartPolicy("Never");

        // mount config and input files if enabled
        if (taskExecutionConfig != null
                && taskExecutionConfig.getMdpS3Enable()) {
            String mountPath = prepareInput(task);
            String volumeName = config.getS3FsShareVolumeName();
            Volume volume = new VolumeBuilder()
                    .withName(volumeName)
                    .withNewEmptyDir()
                    .endEmptyDir()
                    .build();
            EnvVar volumeEnv = new EnvVarBuilder()
                    .withName("MDP_DATA_PATH")
                    .withValue(config.getS3FsMntPoint())
                    .build();
            containerBuilder
                    .withEnv(volumeEnv)
                    .addNewVolumeMount()
                    .withName(volumeName)
                    .withMountPath(config.getS3FsMntPoint())
                    .withMountPropagation("HostToContainer")
                    .endVolumeMount()
                    .build();
            Container volumeContainer = createVolumeContainer(volumeName, mountPath);
            podSpecBuilder
                    .withInitContainers(volumeContainer)
                    .withVolumes(volume);
        }

        podSpecBuilder.withContainers(containerBuilder.build());
        String jobName = task.getId();
        String nameSpace = "default";
        if (config != null
                && !StringUtil.isNullOrEmpty(config.getNameSpace())) {
            nameSpace = config.getNameSpace();
        }
        PodTemplateSpec podTemplateSpec = createPodSpec(jobName,
                nameSpace,
                podSpecBuilder.build(),
                labels);
        Job job = createJob(jobName, nameSpace, labels, podTemplateSpec);
        client.batch().jobs().inNamespace(nameSpace).create(job);
        setJob(task, job);
        task.setState(TaskState.RUNNING);
    }

    @Override
    public void updateTask(TaskInstance task) {
        Logger logger = LoggingUtil.getFileLogger(task.getLogPath());

        if (!task.getState().equals(TaskState.RUNNING)) {
            return;
        }
        Job job = getJob(task);
        ObjectMeta meta = job.getMetadata();
        Job remote = client.batch().jobs()
                .inNamespace(meta.getNamespace())
                .withName(meta.getName())
                .get();
        setJob(task, remote);
        JobStatus status = remote.getStatus();
        TaskState currentState = task.getState();
        Optional<JobCondition> condition = status.getConditions()
                .stream()
                .filter ( t ->t.getType().equals("Failed"))
        .findFirst();
        if (condition.isPresent()) {
           task.setErrorReason(condition.get().getReason());
           currentState = TaskState.FAILED;
        } else {
            boolean isCompleted = !StringUtil.isNullOrEmpty(status.getCompletionTime());
            if (!isCompleted) {
                currentState = TaskState.RUNNING;
            } else if (status.getFailed() != null
                    && status.getFailed() > 0) {
                currentState = TaskState.FAILED;
            } else if (status.getSucceeded() != null
                    && status.getSucceeded() > 0) {
                currentState = TaskState.SUCCEEDED;
            }
        }
        Optional<Pod> jobPod = listPod(meta.getNamespace(), meta.getLabels())
                .stream()
                .findFirst();
        jobPod.ifPresent(x -> tailingLog(task, x, logger));

        task.setState(currentState);
        if (task.isFailed()) {
            jobPod.ifPresent(x -> {
                x.getStatus()
                        .getContainerStatuses()
                        .stream()
                        .findFirst()
                        .ifPresent(t -> {
                            Optional.ofNullable(t.getState()
                                    .getTerminated())
                                    .ifPresent(msg -> {
                                        task.setErrorReason(msg.getMessage());
                                        logger.error(msg.getMessage());
                                    });
                        });
            });
        }
        if (task.isEnded()) {
            cancelTask(task);
        }
    }

    private void tailingLog(TaskInstance task, Pod pod, Logger logger) {
        Integer from = 0;

        final String logKey = "k8sLogFrom";
        if (task.getInfo().containsKey(logKey)) {
            from = (Integer) task.getInfo().get(logKey);
        }
        PodResource<Pod, DoneablePod> podResource = client.pods()
                .inNamespace(pod.getMetadata().getNamespace())
                .withName(pod.getMetadata().getName());
        String logs;
        if (from > 0) {
            logs = podResource
                    .usingTimestamps()
                    .sinceSeconds(from)
                    .getLog();
        } else {
            logs = podResource
                    .usingTimestamps()
                    .getLog();
        }

        logger.info(logs);
        task.getInfo().put(logKey, System.currentTimeMillis()/1000);
    }

    @Override
    public void cancelTask(TaskInstance task) {
        Logger logger = LoggingUtil.getFileLogger(task.getLogPath());
        try{
            Job job = getJob(task);
            ObjectMeta meta = job.getMetadata();
            client.batch().jobs()
                    .inNamespace(meta.getNamespace())
                    .withName(meta.getName())
                    .delete();
        } catch (Exception e) {
            logger.error("Fail to cancel task: ", e);
        }
    }

    @Override
    public String getCommand(FlowTask task) {
        return null;
    }

    private String prepareInput(TaskInstance task) {
        String bucket = taskExecutionConfig.getMdpS3Bucket();
        String destKey = String.format("mdp/%s", task.getId());

        FlowTask flowTask = task.getFlowTask();
        s3Client.putObject(bucket, destKey + "/config/config.json", flowTask.getConfig().toJSONString());

        if (flowTask.getConfig() != null
        && flowTask.getConfig().get("inputs") != null) {
            String targetKey = destKey + "/inputs/";
            Object inputs = flowTask.getConfig().get("inputs");
            List<String> inputFiles = new ArrayList<>();
            if (inputs instanceof String) {
                inputFiles.addAll(Arrays.asList(((String) inputs).split(",")));
            } else if (inputs instanceof String[]) {
                inputFiles.addAll(Arrays.asList(((String[]) inputs)));
            } else if (inputs instanceof List) {
                inputFiles.addAll((List<String>) inputs);
            }

            inputFiles
                    .stream()
                    .map (String::trim)
                    .filter( x -> x.startsWith("s3://")
                            && !StringUtils.isNullOrEmpty(x))
                    .map(AmazonS3URI::new)
                    .forEach(r -> {
                        s3Service.copyFolders(r.getBucket(), r.getKey(), bucket, targetKey);
                    });

        }
        return "s3://" + bucket + "/" + destKey;
    }

    private List<Pod> listPod(String ns, Map<String,String> labels) {
        return client.pods().inNamespace(ns).withLabels(labels)
        .list().getItems();
    }

    private Container createVolumeContainer(String volumeName, String dataPath) {
        Map<String, String> envMap = Maps.newHashMap();
        String mountPath = config.getS3FsMntPoint();
        envMap.put("S3_BUCKET", dataPath);
        envMap.put("S3_ENDPOINT", s3ClientConfig.getS3EndpointUrl());
        envMap.put("AWS_ACCESS_KEY_ID", s3ClientConfig.getS3AccessKeyId());
        envMap.put("AWS_SECRET_ACCESS_KEY", s3ClientConfig.getS3SecretAccessKey());
        envMap.put("MNT_POINT", "/data");

        envMap.put("MOUNT_POINT", mountPath);
        List<EnvVar> envs = envMap.keySet()
                .stream()
                .map (x -> new EnvVarBuilder()
                        .withName(x)
                        .withValue(envMap.get(x))
                        .build())
                .collect(Collectors.toList());

        return new ContainerBuilder()
                .withImage(config.getS3FsImage())
                .withName(volumeName)
                .withNewSecurityContext()
                .withPrivileged(true)
                .endSecurityContext()
                .withEnv(envs)
                .addNewVolumeMount()
                .withName(volumeName)
                .withMountPath(mountPath)
                .withMountPropagation("Bidirectional")
                .endVolumeMount()
                .build();
    }

    private PodTemplateSpec createPodSpec(String name,
                                          String nameSpace,
                                          PodSpec podSpec,
                                          Map<String,String> labels) {
        // pod labels is used for filtering
        ObjectMeta podMeta = new ObjectMetaBuilder()
                .withName(name)
                .withNamespace(nameSpace)
                .withLabels(labels)
                .build();
        return new PodTemplateSpecBuilder()
                .withMetadata(podMeta)
                .withSpec(podSpec)
                .build();
    }

    private Job createJob(String name, String nameSpace,
                          Map<String,String> labels,
                          PodTemplateSpec podTemplateSpec) {
        JobSpec spec = new JobSpecBuilder()
                .withBackoffLimit(0)
                .withTemplate(podTemplateSpec)
                .build();

        ObjectMeta meta = new ObjectMetaBuilder()
                .withName(name)
                .withNamespace(nameSpace)
                .withLabels(labels)
                .build();

        return new JobBuilder()
                .withMetadata(meta)
                .withSpec(spec)
                .build();
    }

    private Job getJob(TaskInstance task) {
        Gson gson = new Gson();
        return gson.fromJson(task.getInfo().get("job").toString()
                , Job.class);
    }

    private void setJob(TaskInstance task, Job job) {
        JSONObject taskInfo = task.getInfo();
        taskInfo.put("job", JSONUtil.objectToString(job));
        task.setInfo(taskInfo);
    }
}
