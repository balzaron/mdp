package com.miotech.mdp.flow.service;

import com.miotech.mdp.common.exception.InvalidQueryException;
import com.miotech.mdp.common.service.BaseService;
import com.miotech.mdp.common.util.StringUtil;
import com.miotech.mdp.flow.config.TaskExecutionConfig;
import com.miotech.mdp.flow.constant.TaskConfig;
import com.miotech.mdp.flow.constant.TaskState;
import com.miotech.mdp.flow.entity.bo.TaskSearch;
import com.miotech.mdp.flow.entity.dao.FlowRun;
import com.miotech.mdp.flow.entity.dao.FlowTask;
import com.miotech.mdp.flow.entity.dao.ITaskStateCount;
import com.miotech.mdp.flow.entity.dao.TaskInstance;
import com.miotech.mdp.flow.entity.vo.TaskStateCount;
import com.miotech.mdp.flow.repository.TaskRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class TaskService extends BaseService<TaskInstance> {
    @Autowired
    private TaskExecutionConfig taskExecutionConfig;

    @Autowired
    @Lazy
    private ExecutorServiceProvider executorServiceProvider;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private FlowTaskService flowTaskService;

    public List<TaskInstance> findRunningTasks() {
        return taskRepository.findByStates(TaskState.RUNNING_STATES);
    }

    public List<TaskInstance> findRetryTasks() {
        return taskRepository.findRetryTasks(TaskState.UP_FOR_RETRY_STATES);
    }

    public TaskInstance retryTask(TaskInstance instance) {
        instance.setState(TaskState.UPRETRY);
        instance.clear();
        instance = save(instance);
        return enqueueTask(instance);
    }

    public TaskInstance enqueueTask(TaskInstance instance) {
        // TODO: give to task queue, currently using async
        instance.increaseTry();
        return executorServiceProvider.execute(instance);
    }

    public TaskInstance updateTask(TaskInstance instance) {
        return executorServiceProvider.update(instance.getId());
    }

    public List<TaskStateCount> getTaskStateInFlow(String flowId) {
        List<ITaskStateCount> taskStateCounts = taskRepository.countTasksByFlow(
                Collections.singletonList(flowId));

        return taskStateCounts.stream()
                .map (x -> {
                    TaskStateCount taskStat = new TaskStateCount();
                    taskStat.setFlowId(flowId);
                    taskStat.setCount(x.getCount());
                    taskStat.setFlowTaskId(x.getFlowTaskId());
                    TaskState state = TaskState.valueOf(x.getState());
                    taskStat.setState(state);
                    return taskStat;
                }).collect(Collectors.toList());
    }

    public Page<TaskInstance> searchTasks(TaskSearch taskSearch) {

        Specification<TaskInstance> specification = (Specification<TaskInstance>) (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (!StringUtil.isNullOrEmpty(taskSearch.getFlowTaskId())) {
                predicates.add(root.get("flowTaskId").in(taskSearch.getFlowTaskId()));
            }
            if (!StringUtil.isNullOrEmpty(taskSearch.getFlowId())) {
                predicates.add(root.get("flowId").in(taskSearch.getFlowId()));
            }
            if (!StringUtil.isNullOrEmpty(taskSearch.getFlowRunId())) {
                predicates.add(root.get("flowRunId").in(taskSearch.getFlowRunId()));
            }
            if (!StringUtil.isNullOrEmpty(taskSearch.getState())) {
                TaskState state = TaskState.fromName(taskSearch.getState());
                if (state == null) {
                    throw new InvalidQueryException(String.format("Invalid state: \"%s\"", taskSearch.getState()));
                }
                predicates.add(root.get("state").in(state));
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.isNotNull(root.get("id"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageableRequest = PageRequest.of(taskSearch.getPageNum(),
                taskSearch.getPageSize(),
                Sort.by("createTime").descending());

        return taskRepository.findAll(specification, pageableRequest);
    }

    public void cancelTask(TaskInstance instance) {
        executorServiceProvider
        .cancel(instance);
    }

    public TaskInstance executeFlowTask(FlowTask flowTask) {
        TaskInstance task = createTask(flowTask.getName(), flowTask, null);
        enqueueTask(task);
        return task;
    }

    public TaskInstance createTask(String name,
                                   FlowTask flowTask,
                                   FlowRun flowRun) {
        if (StringUtil.isNullOrEmpty(name)) {
            name = flowTask.getName();
        }

        TaskInstance task = new TaskInstance();
        task.setName(name);
        task.setState(TaskState.CREATED);

        JSONObject infoJson = new JSONObject();
        infoJson.put(TaskConfig.LOGGIN_DIR, taskExecutionConfig.getLoggingDirectory());
        task.setInfo(infoJson);

        if (flowTask != null) {
            task.setFlowTask(flowTask);
            task.setFlowId(flowTask.getFlowId());
            task.setMaxRetry(flowTask.getRetryPolicy());
        }
        if (flowRun != null) {
            task.setFlowRun(flowRun);
        }

        if (getCurrentUser() != null) {
            task.setCreatorId(getCurrentUser().getId());
        }

        return super.save(task);
    }
}
