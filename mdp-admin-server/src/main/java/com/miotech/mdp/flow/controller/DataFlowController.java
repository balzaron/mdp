package com.miotech.mdp.flow.controller;


import com.miotech.mdp.common.log.AuditLog;
import com.miotech.mdp.common.log.EnableAuditLog;
import com.miotech.mdp.common.model.vo.Result;
import com.miotech.mdp.common.util.StringUtil;
import com.miotech.mdp.flow.entity.bo.*;
import com.miotech.mdp.flow.entity.dao.Flow;
import com.miotech.mdp.flow.entity.dao.FlowRun;
import com.miotech.mdp.flow.entity.dao.FlowTask;
import com.miotech.mdp.flow.entity.dao.TaskInstance;
import com.miotech.mdp.flow.entity.vo.*;
import com.miotech.mdp.flow.service.FlowRunService;
import com.miotech.mdp.flow.service.FlowService;
import com.miotech.mdp.flow.service.FlowTaskService;
import com.miotech.mdp.flow.service.TaskService;
import com.miotech.mdp.flow.util.Converter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@RestController
@RequestMapping("/api")
@Api(tags = "flow management")
@EnableAuditLog
public class DataFlowController {

    @Autowired
    private FlowService flowService;

    @Autowired
    private FlowTaskService flowTaskService;

    @Autowired
    private FlowRunService flowRunService;

    @Autowired
    private TaskService taskService;

    @PostMapping("/flow/list")
    @ApiOperation("list flows")
    public Result<FlowListVO> listFlow(@RequestBody FlowSearch flowSearch) {
        Page<Flow> flowPage = flowService.searchFlows(flowSearch);
        FlowListVO vo = Converter.convert2FlowList(flowPage);
        vo.getFlows()
                .forEach(x -> x.setState(getState(x.getId())));
        return Result.success(vo);
    }

    @PostMapping("/flow")
    @ApiOperation("create flow")
    @AuditLog(topic = "create", model = "flow")
    public Result<FlowVO> createFlow(@Valid @RequestBody FlowInfo flowInfo) {
        Flow flow = flowService.createFlow(flowInfo);
        FlowVO flowVO = Converter.convert2FlowVO(flow);
        return Result.success(flowVO);
    }

    @PostMapping("/flow/{id}/copy")
    @ApiOperation("copy flow from existing flow")
    @AuditLog(topic = "copy", model = "flow")
    public Result<FlowVO> copyFlow(@PathVariable String id,
                                   @Valid @RequestBody FlowCopyOption flowCopyOption) {
        if (StringUtil.isNullOrEmpty(flowCopyOption.getFlowId())) {
            flowCopyOption.setFlowId(id);
        }
        Flow flow = flowService.copyFlow(flowCopyOption);
        FlowVO flowVO = Converter.convert2FlowVO(flow);
        return Result.success(flowVO);
    }

    @GetMapping("/flow/{id}")
    @ApiOperation("get flow")
    public Result<FlowVO> getFlow(@PathVariable String id) {
        FlowVO flowVO = Converter.convert2FlowVO(flowService.find(id));
        // Add extra state
        FlowState state = getState(id);
        val taskStats = taskService.getTaskStateInFlow(id);
        state.setTaskStats(taskStats);
        flowVO.setState(state);
        return Result.success(flowVO);
    }

    @PutMapping("/flow/{id}")
    @ApiOperation("update flow")
    @AuditLog(topic = "update", model = "flow")
    public Result<FlowVO> updateFlow(@PathVariable String id, @RequestBody FlowInfo flowInfo) {
        Flow flow = flowService.updateFlow(id, flowInfo);
        FlowVO flowVO = Converter.convert2FlowVO(flow);
        return Result.success(flowVO);
    }

    @DeleteMapping("/flow/{id}")
    @ApiOperation("delete flow")
    @AuditLog(topic = "delete", model = "flow")
    public Result<Object> deleteFlow(@PathVariable String id) {
        flowService.delete(id);
        return Result.success();
    }

    @PostMapping("/flow/{id}/execute")
    @ApiOperation("execute flow")
    @AuditLog(topic = "execute", model = "flow")
    public Result<FlowRun> executeFlow(@PathVariable String id,
                                    @Valid @RequestBody FlowExecution flowExecution) {
        Flow flow = flowService.find(id);
        return Result.success(flowRunService.executeFlow(flow, flowExecution));
    }

    @PutMapping("/flow/{id}/cancel")
    @ApiOperation("cancel a running flow")
    public Result<Flow> cancelFlow(@PathVariable String id) {
        return null;
    }

    @GetMapping("/flow/{id}/runs")
    @ApiOperation("get flow runs ")
    public Result<FlowRunListVO> getFlowRuns(
            @PathVariable String id,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "15") Integer pageSize,
            @RequestParam(value = "state", defaultValue = "") String state
    ) {
        FlowRunSearch flowRunSearch = new FlowRunSearch();
        flowRunSearch.setFlowId(id);
        flowRunSearch.setPageNum(pageNum);
        flowRunSearch.setPageSize(pageSize);
        if (!StringUtil.isNullOrEmpty(state)) {
            flowRunSearch.setState(state);
        }
        FlowRunListVO vo = Converter.convert2FlowRunList(
                flowRunService.searchFlowRun(flowRunSearch));
        return Result.success(vo);
    }

    @GetMapping("/flow/{id}/state")
    @ApiOperation("get flow state ")
    public Result<FlowState> getFlowState(@PathVariable String id) {
        FlowState state = getState(id);
        val taskStats = taskService.getTaskStateInFlow(id);
        state.setTaskStats(taskStats);
        return Result.success(state);
    }

    @PostMapping("/flow/states")
    @ApiOperation("get flow state ")
    public Result<List<FlowState>> getFlowStates(@RequestBody FlowList flowList) {
        val states = Arrays.stream(flowList.getFlowIds())
                .map(this::getState)
                .collect(Collectors.toList());

        return Result.success(states);
    }

    private FlowState getState(String flowId) {
        FlowState state = new FlowState();
        state.setRunStats(flowRunService.getFlowState(flowId));
        state.setFlowId(flowId);
        flowRunService.getLatestRun(flowId)
                .map(x -> {
                    state.setLatestRun(x);
                    return x;
                });
        return state;
    }

    @PostMapping("/flow/{id}/tasks")
    @ApiOperation("create flow task")
    @AuditLog(topic = "create", model = "flow-task")
    public Result<FlowTask> addFlowTask(@PathVariable String id,
                                        @RequestBody FlowTaskInfo flowTaskInfo) {
        return Result.success(flowTaskService.createFlowTask(flowTaskInfo));
    }

    @PutMapping("/flow/{id}/tasks/{taskId}")
    @ApiOperation("update flow task")
    @AuditLog(topic = "update", model = "flow-task")
    public Result<FlowTask> updateFlowTask(@PathVariable String id,
                                           @PathVariable String taskId,
                                           @Valid @RequestBody FlowTaskInfo flowTaskInfo) {
        return Result.success(flowTaskService.updateFlowTask(taskId, flowTaskInfo));
    }

    @GetMapping("/flow/{id}/tasks/{taskId}")
    @ApiOperation("get flow task")
    public Result<FlowTask> getFlowTask(@PathVariable String id,
                                         @PathVariable String taskId) {
        return Result.success(flowTaskService.find(taskId));
    }

    @GetMapping("/flow-tasks/{taskId}")
    @ApiOperation("get flow task")
    public Result<FlowTask> getFlowTaskV2(@PathVariable String taskId) {
        return Result.success(flowTaskService.find(taskId));
    }

    @DeleteMapping("/flow/{id}/tasks/{taskId}")
    @ApiOperation("delete flow task")
    @AuditLog(topic = "delete", model = "flow-task")
    public Result<Object> deleteFlowTask(@PathVariable String id,
                                      @PathVariable String taskId) {
        flowTaskService.delete(taskId);
        return Result.success();
    }

    @PostMapping("/flow/{id}/tasks/{taskId}/execute")
    @ApiOperation("execute flow task")
    @AuditLog(topic = "execute", model = "flow-task")
    public Result<TaskInstance> executeFlowTask(@PathVariable String id,
                                         @PathVariable String taskId) {
        FlowTask flowTask = flowTaskService.find(taskId);
        TaskInstance task = taskService.executeFlowTask(flowTask);
        return Result.success(task);
    }

    @PutMapping("/flow/{id}/tasks/{taskId}/cancel")
    @ApiOperation("cancel a running flow task")
    @AuditLog(topic = "cancel", model = "flow-task")
    public Result<FlowTask> cancelFlowTask(@PathVariable String id,
                                            @PathVariable String taskId) {
        return null;
    }

    @GetMapping("/flow/{id}/tasks/{taskId}/instances")
    @ApiOperation("get flow task state")
    public Result<TaskListVO> getFlowTaskState(@PathVariable String id,
                                               @PathVariable String taskId,
                                               @RequestParam(required = false) String flowTaskId,
                                               @RequestParam(required = false) String state) {
        TaskSearch taskSearch = new TaskSearch();
        taskSearch.setState(state);
        taskSearch.setFlowTaskId(flowTaskId);
        taskSearch.setFlowTaskId(taskId);
        Page<TaskInstance> taskPage = taskService.searchTasks(taskSearch);
        return Result.success(Converter.convert2TaskList(taskPage));
    }
}
