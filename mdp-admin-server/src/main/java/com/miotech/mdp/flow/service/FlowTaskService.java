package com.miotech.mdp.flow.service;

import com.google.gson.Gson;
import com.miotech.mdp.common.exception.InvalidQueryException;
import com.miotech.mdp.common.service.BaseService;
import com.miotech.mdp.common.util.StringUtil;
import com.miotech.mdp.flow.constant.ParameterType;
import com.miotech.mdp.flow.entity.bo.ArgumentInfo;
import com.miotech.mdp.flow.entity.bo.FlowTaskInfo;
import com.miotech.mdp.flow.entity.dao.Flow;
import com.miotech.mdp.flow.entity.dao.FlowTask;
import com.miotech.mdp.flow.entity.dao.Operator;
import com.miotech.mdp.flow.entity.dao.Parameter;
import com.miotech.mdp.flow.repository.FlowTaskRepository;
import com.miotech.mdp.table.model.dao.MetaTableEntity;
import com.miotech.mdp.table.model.dao.MetabaseDatabaseEntity;
import com.miotech.mdp.table.service.DatabaseService;
import com.miotech.mdp.table.service.TableService;
import com.miotech.mdp.quality.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
@Slf4j
public class FlowTaskService extends BaseService<FlowTask> {

    @Autowired
    private FlowService flowService;

    @Autowired
    private OperatorService operatorService;

    @Autowired
    private DataTestCaseService testCaseService;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private TableService tableService;

    @Autowired
    private AirflowService airflowService;

    @Autowired
    private FlowTaskRepository flowTaskRepository;

    public FlowTask createFlowTask(FlowTaskInfo flowTaskInfo) {
        validateFlowTask(flowTaskInfo);
        FlowTask flowTask = new FlowTask();
        setEntity(flowTask, flowTaskInfo);
        if (getCurrentUser() != null) {
            flowTask.setCreatorId(getCurrentUser().getId());
        }
        super.save(flowTask);
        if (!StringUtil.isNullOrEmpty(flowTask.getFlowId())) {
            flowService.reOrderFlowTasks(flowTask.getFlowId());
            airflowService.createDAG(flowTask.getFlowId());
        }
        return find(flowTask.getId());
    }

    public FlowTask updateFlowTask(String id, FlowTaskInfo flowTaskInfo) {
        validateFlowTask(flowTaskInfo);
        FlowTask flowTask = super.find(id);
        setEntity(flowTask, flowTaskInfo);
        super.save(flowTask);
        if (!StringUtil.isNullOrEmpty(flowTask.getFlowId())) {
            flowService.reOrderFlowTasks(flowTask.getFlowId());
            airflowService.createDAG(flowTask.getFlowId());
        }
        return find(flowTask.getId());
    }

    /**
     * @param tableId MetaTable id
     * @return list of flow tasks related to specified table
     */
    public List<FlowTask> getTableFlowTasks(String tableId) {
        if (StringUtil.isNullOrEmpty(tableId)) {
            return new ArrayList<>();
        }
        return flowTaskRepository.findByReferencedTableId(tableId);
    }

    /**
     * @param tableId MetaTable id
     * @param flowId Flow id
     * @return list of flow tasks related to specified table and flow
     */
    public List<FlowTask> getTableFlowTasks(String tableId, String flowId) {
        if (StringUtil.isNullOrEmpty(tableId) || StringUtil.isNullOrEmpty(flowId)) {
            return new ArrayList<>();
        }
        return flowTaskRepository.findByReferencedTableIdAndFlowId(tableId, flowId);
    }

    public void allowDelete(String id) {
        FlowTask deletedFlowTask = super.find(id);
        Flow flow = flowService.find(deletedFlowTask.getFlowId());
        flow.getFlowTasks().forEach(flowTask -> {
            for (String parentTaskId : flowTask.getParentTaskIds()) {
                if (StringUtils.equals(parentTaskId, id)) {
                    throw new InvalidQueryException("FlowTask has child task, unable to delete it.");
                }
            }
        });
    }

    @Override
    public void delete(String id) {
        allowDelete(id);
        super.delete(id);
        airflowService.createDAG(id);
    }

    @Override
    public void delete(List<FlowTask> entities) {
        Set<String> relatedFlows = new HashSet<>();
        for (FlowTask entity : entities) {
            allowDelete(entity.getId());
            relatedFlows.add(entity.getFlowId());
        }
        super.delete(entities);
        for (String relatedFlow : relatedFlows) {
            airflowService.createDAG(relatedFlow);
        }
    }

    private void setEntity(FlowTask flowTask, FlowTaskInfo flowTaskInfo) {
        if (!StringUtil.isNullOrEmpty(flowTaskInfo.getName())) {
            flowTask.setName(flowTaskInfo.getName());
        }

        if (!StringUtil.isNullOrEmpty(flowTaskInfo.getOperatorId())) {
            flowTask.setOperatorId(flowTaskInfo.getOperatorId());
        }

        if (flowTaskInfo.getRetryPolicy()>0) {
            flowTask.setRetryPolicy(flowTaskInfo.getRetryPolicy());
        }

        if (!StringUtil.isNullOrEmpty(flowTaskInfo.getFlowId())) {
            Flow flow = flowService.find(flowTaskInfo.getFlowId());
            flowTask.setFlowId(flow.getId());
            if (flowTask.getRetryPolicy() <= 0) {
                flowTask.setRetryPolicy(flow.getRetryPolicy());
            }
        }

        if (!StringUtil.isNullOrEmpty(flowTaskInfo.getTestId())) {
            flowTask.setTestId(flowTaskInfo.getTestId());
        }

        String[] parentTaskIds = flowTaskInfo.getParentTaskIds();
        if (parentTaskIds != null) {
            flowTask.setParentTaskIds(parentTaskIds);
        }

        List<ArgumentInfo> arguments = flowTaskInfo.getArguments();
        if (arguments != null) {
            setArguments(flowTask, arguments);
        }

        if (!StringUtil.isNullOrEmpty(flowTaskInfo.getTriggerRule())) {
            flowTask.setTriggerRule(flowTaskInfo.getTriggerRule());
        }
    }

    private void setArguments(FlowTask flowTask,  List<ArgumentInfo> arguments) {
        flowTask.setArguments(arguments);
        Map<String, String> argumentKeys = new HashMap<>();
        arguments.forEach(x -> argumentKeys.put(x.getParameterKey(), x.getParameterValue()));

        if (flowTask.getOperatorId()!= null) {
            JSONObject taskConfig = new JSONObject();

            Operator op = operatorService.find(flowTask.getOperatorId());

            arguments.forEach(x -> {
                String parameterType = op.getParameters().stream()
                        .filter(t -> t.getParameterKey().equals(x.getParameterKey()))
                        .findAny()
                        .get().getParameterType();
                if (parameterType.equals(ParameterType.MULTIPLE_STRING.getName())) {
                    try {
                        taskConfig.put(x.getParameterKey(), new JSONParser().parse(x.getParameterValue()));
                    } catch (ParseException e) {
                        throw new RuntimeException("Invalid multiple-string value: ", e);
                    }
                } else if (parameterType.equals(ParameterType.MULTIPLE_TABLEID.getName())) {
                    try {
                        taskConfig.put(x.getParameterKey(), new JSONParser().parse(x.getParameterValue()));
                    } catch (ParseException e) {
                        throw new RuntimeException("Invalid multiple-tableId value: " , e);
                    }
                } else {
                    taskConfig.put(x.getParameterKey(), x.getParameterValue());
                }
            });

            Stream<Parameter> tableIdRelatedParam = op.getParameters()
                    .stream()
                    .filter(x -> (
                            x.getParameterType().equals(ParameterType.TABLEID.getName()) ||
                            x.getParameterKey().equals(ParameterType.MULTIPLE_TABLEID.getName())
                    ));

            List<MetaTableEntity> relatedTables = new ArrayList<>();
            tableIdRelatedParam.forEach(p -> {
                    String key = p.getParameterKey();
                    if (p.getParameterType().equals(ParameterType.MULTIPLE_TABLEID.getName())) {
                        List<String> tableIds = (new Gson()).fromJson(argumentKeys.get(key), ArrayList.class);
                        List<String> fullTableNames = new ArrayList<>();
                        tableIds.stream().forEach(tableId -> {
                            MetaTableEntity tableEntity = tableService.find(tableId);
                            fullTableNames.add(tableEntity.getFullName());
                            relatedTables.add(tableEntity);
                        });
                        taskConfig.put(key, fullTableNames);
                    } else {
                        // when x.getParameterType() equals ParameterType.TABLEID.getName()
                        String tableId = argumentKeys.get(key);
                        MetaTableEntity tableEntity = tableService.find(tableId);
                        relatedTables.add(tableEntity);
                        taskConfig.put(key, tableEntity.getFullName());
                    }
                });

            flowTask.getRelatedTables().addAll(relatedTables);

            Optional<Parameter> dbIdParam = op.getParameters()
                    .stream()
                    .filter(x -> x.getParameterType()
                    .equals(ParameterType.DATABASEID.getName()))
                    .findFirst();
            if (dbIdParam.isPresent() || !relatedTables.isEmpty()) {
                Integer dbId = dbIdParam.map(x -> argumentKeys.get(x.getParameterKey()))
                        .map(Integer::new)
                        .orElseGet(() -> relatedTables.get(0).getDbId());
                MetabaseDatabaseEntity db = databaseService.getDatabase(dbId);
                try {
                    taskConfig.put("dbDetails", new JSONParser().parse(db.getDetails().toString()));
                } catch (ParseException e) {
                    log.error("Error in set dbdetails: ", e);
                }
                taskConfig.put("engine", db.getEngine());
            }

            flowTask.setConfig(taskConfig);
        }

        //        List<MetaTableEntity> relatedTables = tableService.getRelatedTableFromQuery(database.getId(), sql);
//        flowTask.setRelatedTables(relatedTables);
    }

    private void validateFlowTask(FlowTaskInfo flowTaskInfo) {
        String[] parentTaskIds = flowTaskInfo.getParentTaskIds();
        if (parentTaskIds!= null) {
            Arrays.stream(parentTaskIds).forEach(super::find);
        }
        if (!StringUtil.isNullOrEmpty(flowTaskInfo.getFlowId())) {
            flowService.find(flowTaskInfo.getFlowId());
        }

        if (!StringUtil.isNullOrEmpty(flowTaskInfo.getTestId())) {
            testCaseService.findById(flowTaskInfo.getTestId());
            return;
        }

        Operator operator = operatorService.find(flowTaskInfo.getOperatorId());
        if (flowTaskInfo.getArguments() != null) {
            Map<String, String> argumentKeys = new HashMap<>();
            flowTaskInfo.getArguments()
                    .forEach(x -> {
                        argumentKeys.put(x.getParameterKey(), x.getParameterValue());
                    });
            // no duplicate arguments
            if (StringUtil.isDuplicate(argumentKeys.keySet())) {
                throw new RuntimeException("Argument parameterKey should be unique");
            }
            // no extra arguments
            List<String> parameterKeys = operator.getParameterKeys();
            argumentKeys.keySet().forEach( x -> {
                if (parameterKeys.indexOf(x) < 0) {
                    throw new RuntimeException("Argument parameterKey: \"" + x + "\" not found in operator");
                }
            });
            // if default value not specified
            for (Parameter parameter: operator.getParameters()) {
                if (parameter.getDefaultValue() == null
                        && argumentKeys.get(parameter.getParameterKey()) == null) {
                    throw new RuntimeException(
                            String.format("Argument parameterKey %s not specified and no default value", parameter.getParameterKey()));
                }
            }
        }
    }
}
