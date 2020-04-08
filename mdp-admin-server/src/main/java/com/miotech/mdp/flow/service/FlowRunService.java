package com.miotech.mdp.flow.service;

import com.miotech.mdp.common.exception.InvalidQueryException;
import com.miotech.mdp.common.service.BaseService;
import com.miotech.mdp.common.util.StringUtil;
import com.miotech.mdp.flow.constant.FlowState;
import com.miotech.mdp.flow.constant.TaskState;
import com.miotech.mdp.flow.constant.TriggerRule;
import com.miotech.mdp.flow.entity.bo.FlowExecution;
import com.miotech.mdp.flow.entity.bo.FlowRunSearch;
import com.miotech.mdp.flow.entity.dao.Flow;
import com.miotech.mdp.flow.entity.dao.FlowRun;
import com.miotech.mdp.flow.entity.dao.FlowTask;
import com.miotech.mdp.flow.entity.dao.TaskInstance;
import com.miotech.mdp.flow.entity.vo.FlowRunStateCount;
import com.miotech.mdp.flow.repository.FlowRunRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FlowRunService extends BaseService<FlowRun> {

    @Autowired
    private FlowRunRepository flowRunRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private FlowService flowService;

    public Optional<FlowRun> getLatestRun(String flowId) {
        return flowRunRepository.findLatestRun(flowId);
    }

    public List<FlowRunStateCount> getFlowState(String flowId) {
        return flowRunRepository.countFlowRunByFlow(
                Collections.singletonList(flowId))
                .stream()
                .map (x -> {
                    FlowRunStateCount flowStateCount = new FlowRunStateCount();
                    flowStateCount.setCount(x.getCount());
                    FlowState state = FlowState.valueOf(x.getState());
                    flowStateCount.setState(state);
                    return flowStateCount;
                }).collect(Collectors.toList());
    }

    public Page<FlowRun> searchFlowRun(FlowRunSearch flowRunSearch) {

        Specification<FlowRun> specification = (Specification<FlowRun>) (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (!StringUtil.isNullOrEmpty(flowRunSearch.getFlowId())) {
                Flow flow = flowService.find(flowRunSearch.getFlowId());
                predicates.add(root.get("flow").in(flow));
            }

            if (!StringUtil.isNullOrEmpty(flowRunSearch.getFlowRunId())) {
                predicates.add(root.get("id").in(flowRunSearch.getFlowRunId()));
            }

            if (!StringUtil.isNullOrEmpty(flowRunSearch.getState())) {
                if (FlowState.isValidName(flowRunSearch.getState())) {
                    predicates.add(criteriaBuilder.equal(
                            root.get("state"),
                            FlowState.fromName(flowRunSearch.getState())
                    ));
                } else {
                    throw new InvalidQueryException("Invalid run state requested");
                }
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.isNotNull(root.get("id"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageableRequest = PageRequest.of(flowRunSearch.getPageNum(),
                flowRunSearch.getPageSize(),
                Sort.by("createTime").descending());

        return flowRunRepository.findAll(specification, pageableRequest);
    }

    public List<FlowRun> getAllRunning() {
        return flowRunRepository.findByStates(Arrays.asList(FlowState.RUNNING));
    }

    public FlowRun executeFlow(Flow flow, FlowExecution flowExecution) {
        Boolean isFlowRunning = this.getLatestRun(flow.getId())
                .map(x -> x.getState().equals(FlowState.RUNNING))
                .orElseGet(() -> false);
        if (!flow.getEnableParallel() && isFlowRunning) {
            // If not allow parallel run
            throw new RuntimeException("Flow is running");
        }

        if (flowExecution.getExcludeTasks() == null) {
            flowExecution.setExcludeTasks(new String[]{});
        }
        if (flowExecution.getIncludeTasks() == null) {
            flowExecution.setIncludeTasks(new String[]{});
        }
        return this.createFlowRun(flow, flowExecution) ;
    }

    public FlowRun processFlowRun(FlowRun flowRun) {
        if (flowRun.getStartTime() == null) {
            flowRun.setStartTime(LocalDateTime.now());
        }
        List<TaskInstance> taskSets = flowRun.getTaskSet();

        List<TaskInstance> runningTasks = taskSets
                .stream()
                .filter(x -> x.isRunning() || x.isUpForRetry())
                .collect(Collectors.toList());

        List<TaskInstance> unprocessedTasks = taskSets
                .stream()
                .filter(x ->x.getState().equals(TaskState.CREATED))
                .collect(Collectors.toList());

        if (unprocessedTasks.isEmpty()
                && runningTasks.isEmpty()) {
            if (taskSets.stream()
                    .anyMatch(TaskInstance::isFailed)) {
                flowRun.setState(FlowState.FAILED);
            } else {
                flowRun.setState(FlowState.FINISHED);
            }
            flowRun.setEndTime( LocalDateTime.now());
            return save(flowRun);
        }

        for (TaskInstance task: unprocessedTasks) {
            FlowTask flowTask = task.getFlowTask();
            List<String> parent = Optional.ofNullable(flowTask.getParentTaskIds())
                    .map(Arrays::asList)
                    .orElse(new ArrayList<>());
            boolean dependencyDone = true;
            boolean shouldCancel = false;
            if (!parent.isEmpty()) {
                List<TaskInstance> parentTasks = taskSets.stream()
                        .filter( x-> parent.contains(x.getFlowTask().getId()))
                        .collect(Collectors.toList());
                if (!parentTasks.isEmpty()) {
                    TriggerRule triggerRule = TriggerRule.fromName(flowTask.getTriggerRule());
                    switch (triggerRule) {
                        case ALL_SUCCESS:
                            dependencyDone = parentTasks.stream()
                                    .allMatch(t -> t.isEnded() && !t.isFailed());
                            shouldCancel = parentTasks.stream()
                                    .anyMatch(t ->t.isEnded() && t.isFailed());
                            break;
                        case ONE_SUCCESS:
                            dependencyDone = parentTasks.stream()
                                    .anyMatch(t -> t.isEnded() && !t.isFailed());
                            shouldCancel = parentTasks.stream()
                                    .allMatch(t ->t.isEnded() && t.isFailed());
                            break;
                        case ONE_FAILED:
                            dependencyDone = parentTasks.stream()
                                    .anyMatch(t ->t.isEnded() && t.isFailed());
                            shouldCancel = parentTasks.stream()
                                    .allMatch(t -> t.isEnded() && !t.isFailed());
                            break;
                    }
                }
            }

            if (shouldCancel) {
                task.setState(TaskState.UPSTREAM_FAILED);
                taskService.save(task);
            } else if (dependencyDone) {
                taskService.enqueueTask(task);
            }
        }
        return save(flowRun);
    }

    public FlowRun createFlowRun(Flow flow, FlowExecution flowExecution) {
        FlowRun flowRunEntity = new FlowRun();
        flowRunEntity.setFlowId(flow.getId());
        flowRunEntity.setState(FlowState.RUNNING);
        if (getCurrentUser() != null) {
            flowRunEntity.setCreatorId(getCurrentUser().getId());
        }
        save(flowRunEntity);

        List<String> excludeTasks = Arrays.asList(flowExecution.getExcludeTasks());
        List<String> includeTasks = Arrays.asList(flowExecution.getIncludeTasks());

        flow.getFlowTasks()
                .stream()
                .filter ( x -> {
                    boolean isValid = true;
                    if (!includeTasks.isEmpty()) {
                        isValid = includeTasks.contains(x.getId());
                    }
                    if (!excludeTasks.isEmpty()) {
                        isValid = !excludeTasks.contains(x.getId());
                    }
                    return isValid;
                })
                .forEach(x -> {
                    taskService.createTask(x.getName(), x, flowRunEntity);
                });

        return flowRunEntity;
    }
}
