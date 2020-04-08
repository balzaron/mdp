package com.miotech.mdp.flow.service;


import com.miotech.mdp.common.exception.InvalidQueryException;
import com.miotech.mdp.common.jpa.CustomFilter;
import com.miotech.mdp.common.model.dao.TagsEntity;
import com.miotech.mdp.common.service.BaseService;
import com.miotech.mdp.common.service.TagService;
import com.miotech.mdp.common.util.StringUtil;
import com.miotech.mdp.flow.entity.bo.FlowCopyOption;
import com.miotech.mdp.flow.entity.bo.FlowInfo;
import com.miotech.mdp.flow.entity.bo.FlowSearch;
import com.miotech.mdp.flow.entity.bo.FlowTaskInfo;
import com.miotech.mdp.flow.entity.dao.Flow;
import com.miotech.mdp.flow.entity.dao.FlowTask;
import com.miotech.mdp.flow.repository.FlowRepository;
import com.miotech.mdp.flow.tasks.FlowExecutionJob;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.modelmapper.ModelMapper;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

import static org.quartz.CronScheduleBuilder.cronSchedule;


@Service
@Slf4j
public class FlowService extends BaseService<Flow> {

    @Value("${airflow.enable: false}")
    private Boolean ENABLE_AIRFLOW;

    @Autowired
    AirflowService airflowService;

    @Autowired
    SchedulerService schedulerService;

    @Autowired
    TagService tagService;

    @Autowired
    FlowRepository flowRepository;

    @Autowired
    private FlowTaskService flowTaskService;

    public Page<Flow> searchFlows(FlowSearch flowSearch) {

        Specification<Flow> specification = (Specification<Flow>) (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
            val customFilter = new CustomFilter<Flow>();

            if (!StringUtil.isNullOrEmpty(flowSearch.getName())) {
                String name = flowSearch.getName();
                val nameLike = cb.like(
                        cb.upper(root.get("name")),
                        "%" + name + "%");
                val nameTs = cb.isTrue(
                        customFilter.udfTsFilter(cb, root.get("tsQuery"), name)
                );
                predicates.add(
                        cb.or(nameLike, nameTs)
                );
            }

            if (ArrayUtils.isNotEmpty(flowSearch.getTags())) {
                val tagIds = tagService.findTags(Arrays.asList(flowSearch.getTags()))
                .stream().map(TagsEntity::getId).collect(Collectors.toList());
                predicates.add(cb.isTrue(
                        customFilter
                        .udfArrayContains(cb, root.get("tagIds"), tagIds))
                );
            }
            if (predicates.isEmpty()) {
                return cb.isNotNull(root.get("id"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageableRequest = PageRequest.of(flowSearch.getPageNum(),
                flowSearch.getPageSize(),
                Sort.by("createTime").descending());

        return this.page(specification, pageableRequest);
    }

    /**
     * @param tableId MetaTable id
     * @return list of flows related to specified MetaTable
     */
    public List<Flow> getTableFlows(String tableId) {
        return flowRepository.findFlowsByReferencedTableId(tableId);
    }

    public Flow createFlow(FlowInfo flow) {
        Flow flowEntity = new Flow();
        setEntity(flowEntity, flow);
        if (getCurrentUser() != null) {
            flowEntity.setCreatorId(getCurrentUser().getId());
        }
        Flow savedEntity = super.save(flowEntity);
        createOrUpdateScheduling(savedEntity);

        return savedEntity;
    }

    public Flow updateFlow(String flowId, FlowInfo flow) {
        Flow flowEntity = super.find(flowId);
        setEntity(flowEntity, flow);
        Flow updatedEntity = super.save(flowEntity);
        createOrUpdateScheduling(updatedEntity);

        return updatedEntity;
    }

    @Transactional
    public Flow copyFlow(FlowCopyOption flowCopyOption) {

        Flow flowEntity = super.find(flowCopyOption.getFlowId());

        // create flow it self
        FlowInfo flowInfo = new FlowInfo();
        ModelMapper mapper = new ModelMapper();
        mapper.map(flowEntity, flowInfo);
        flowInfo.setName(tryForName(flowEntity.getName() + " - Copy"));
        flowInfo.setTags(flowEntity.getTags().stream().map(TagsEntity::getName).toArray(String[]::new));

        Flow copiedFlow = createFlow(flowInfo);

        // create flow tasks
        List<FlowTask> flowTasks = flowEntity.getFlowTasks();
        Map<String, String> flowTaskMapping = new HashMap<>();
        Map<String, String[]> parentTasksMapping = new HashMap<>();

        List<FlowTask> copyiedTasks = flowTasks.stream()
                .filter(x -> {
                    if (ArrayUtils.isNotEmpty(flowCopyOption.getIncludedTasks())) {
                        return Arrays.asList(flowCopyOption.getIncludedTasks())
                                .contains(x.getId());
                    } else {
                        return true;
                    }
                })
                .map( x ->  {
                    FlowTaskInfo flowTaskInfo = new FlowTaskInfo();
                    flowTaskInfo.setName(x.getName());
                    flowTaskInfo.setTestId(x.getTestId());
                    flowTaskInfo.setOperatorId(x.getOperatorId());
                    flowTaskInfo.setArguments(x.getArguments());
                    flowTaskInfo.setTriggerRule(x.getTriggerRule());
                    flowTaskInfo.setRetryPolicy(x.getRetryPolicy());

                    FlowTask copied = flowTaskService.createFlowTask(flowTaskInfo);
                    String copiedId = copied.getId();
                    String oldId = x.getId();
                    flowTaskMapping.put(oldId, copiedId);
                    parentTasksMapping.put(copiedId, x.getParentTaskIds());
                    return copied;
                })
                .collect(Collectors.toList());

        String copiedFlowId = copiedFlow.getId();
        copyiedTasks = copyiedTasks
                .stream()
                .map(x -> {
                    String[] parentIds = Arrays.stream(parentTasksMapping.get(x.getId()))
                    .map(flowTaskMapping::get)
                    .toArray(String[]::new);
                    x.setFlowId(copiedFlowId);
                    x.setParentTaskIds(parentIds);
                    return flowTaskService.save(x);
                })
                .collect(Collectors.toList());

        copiedFlow.setFlowTasks(copyiedTasks);
        copiedFlow = super.save(copiedFlow);
        reOrderFlowTasks(copiedFlow.getId());
        return copiedFlow;
    }

    private String tryForName(String flowName) {
        Flow flow = Flow.emptyFlow();
        String findName = flowName;
        flow.setName(findName);
        int idx = 0;
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
        while (flowRepository.exists(Example.of(flow, matcher))) {
            idx += 1;
            findName = flowName + " " + idx;
            flow.setName(findName);
        }
        return findName;
    }

    @Override
    public void delete(String id) {
        if (ENABLE_AIRFLOW) {
            airflowService.deleteDAG(id);
        } else {
            removeFlowJob(find(id));
        }
        super.delete(id);
    }

    @Override
    public void delete(List<Flow> entities) {
        super.delete(entities);
        for (Flow entity : entities) {
            airflowService.deleteDAG(entity.getId());
        }
    }

    public void reOrderFlowTasks(String flowId) {
        Flow flow = this.find(flowId);
        List<FlowTask> tasks = flow.getFlowTasks();
        Map<String, Integer> cache = new HashMap<>();
        tasks.forEach(x -> getTaskOrder(tasks, x, cache));
        tasks.forEach(x -> {
            x.setTaskOrder(cache.get(x.getId()));
            flowTaskService.save(x);
        });
    }

    public int getTaskOrder(List<FlowTask> tasks,
                            FlowTask flowTask,
                            Map<String, Integer> cache) {
        if (cache.get(flowTask.getId()) != null) {
            return cache.get(flowTask.getId());
        }
        int currentMaxLevel = 1;

        if (ArrayUtils.isNotEmpty(flowTask.getParentTaskIds())) {
            currentMaxLevel = Arrays.stream(flowTask.getParentTaskIds())
                    .map(x -> {
                        val parentTask = tasks.stream()
                                .filter(p-> p.getId().equals(x))
                                .findFirst()
                                .get();
                        return getTaskOrder(tasks, parentTask, cache);
                    })
                    .reduce(1, Math::max) + 1;
        }
        cache.put(flowTask.getId(), currentMaxLevel);
        return currentMaxLevel;
    }

    private void setEntity(Flow flowEntity, FlowInfo flow) {
        if (!StringUtil.isNullOrEmpty(flow.getName())) {
            flowEntity.setName(flow.getName());
        }

        if (flow.getDescription() != null) {
            flowEntity.setDescription(flow.getDescription());
        }

        if (flow.getExecutionScheduler() != null) {
            // TODO: validate crontab expression
            if (!CronExpression.isValidExpression(flow.getExecutionScheduler())) {
                throw new InvalidQueryException("Execution scheduler only support valid crontab expression.");
            }
            flowEntity.setExecutionScheduler(flow.getExecutionScheduler());
        }

        if (flow.getEnableScheduler() != null) {
            flowEntity.setEnableScheduler(flow.getEnableScheduler());
        }

        if (flow.getEnableParallel() != null) {
            flowEntity.setEnableParallel(flow.getEnableParallel());
        }

        if (flow.getRetryPolicy() != null) {
            flowEntity.setRetryPolicy(flow.getRetryPolicy());
        }

        if (flow.getUserIds() != null) {
            // TODO: validate user ids
            flowEntity.setUserIds(flow.getUserIds());
        }

        if (flow.getTags()!= null) {
            List<TagsEntity> tagsEntities = tagService.findTags(Arrays.asList(flow.getTags()));
            String[] tagIds = tagsEntities.stream()
                    .map(TagsEntity::getId)
                    .toArray(String[]::new);
            flowEntity.setTagIds(tagIds);
            flowEntity.setTags(tagsEntities);
        }
    }

    private void createOrUpdateScheduling(Flow flow) {
        if (ENABLE_AIRFLOW) {
            airflowService.createDAG(flow.getId());
            if (flow.getEnableScheduler()) {
                airflowService.unpauseDAG(flow.getId());
            } else {
                airflowService.pauseDAG(flow.getId());
            }
        } else {
            if (flow.getEnableScheduler()) {
                buildFlowJob(flow);
            } else {
                removeFlowJob(flow);
            }
        }

    }

    private void removeFlowJob(Flow flow) {
        JobKey jobKey = new JobKey(flow.getId(),"flow-cron-jobs");
        try {
            JobDetail detail = schedulerService.getTaskInfo(jobKey);
            if (detail == null) {
                return;
            }
            schedulerService.deleteTask(jobKey);
        } catch (SchedulerException e) {
            throw new InvalidQueryException(e);
        }
    }

    private JobKey buildFlowJob(Flow flow) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("flowId", flow.getId());

        JobDetail jobDetail = JobBuilder.newJob(FlowExecutionJob.class)
                .withIdentity(flow.getId(), "flow-cron-jobs")
                .withDescription("flow.execution.cron.job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "flow-cron-triggers")
                .withDescription("flow.execution.cron.trigger")
                .withSchedule(cronSchedule(flow.getExecutionScheduler()))
                .build();

        try {
            schedulerService.scheduleTask(jobDetail, trigger);
        } catch (SchedulerException e) {
            log.error("Error in scheduling flow: {}", flow.getId(), e);
            throw new InvalidQueryException(e);
        }
        return jobDetail.getKey();
    }

}
