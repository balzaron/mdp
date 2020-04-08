package com.miotech.mdp.flow.controller;

import com.miotech.mdp.common.exception.ResourceNotFoundException;
import com.miotech.mdp.common.log.AuditLog;
import com.miotech.mdp.common.log.EnableAuditLog;
import com.miotech.mdp.common.model.vo.Result;
import com.miotech.mdp.flow.entity.bo.TaskSearch;
import com.miotech.mdp.flow.entity.dao.TaskInstance;
import com.miotech.mdp.flow.entity.vo.TaskListVO;
import com.miotech.mdp.flow.service.TaskService;
import com.miotech.mdp.flow.util.Converter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
@RequestMapping("/api")
@Api(tags = "flow management")
@EnableAuditLog
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping("/tasks/list")
    @ApiOperation("list tasks")
    public Result<TaskListVO> listOperators(@RequestBody TaskSearch taskSearch) {
        Page<TaskInstance> taskPage = taskService.searchTasks(taskSearch);

        return Result.success(Converter.convert2TaskList(taskPage));
    }

    @GetMapping("/task/{id}")
    @ApiOperation("get task")
    public Result<TaskInstance> getTask(@PathVariable String id) {
        return Result.success(taskService.find(id));
    }

    @PostMapping("/task/{id}/execute")
    @ApiOperation("execute task")
    @AuditLog(topic = "execute", model = "task")
    public Result<TaskInstance> executeTask(@PathVariable String id) {
        TaskInstance task = taskService.find(id);
        return Result.success(taskService.enqueueTask(task));
    }

    @PostMapping("/task/{id}/cancel")
    @ApiOperation("cancel task")
    @AuditLog(topic = "cancel", model = "task")
    public Result<TaskInstance> cancelTask(@PathVariable String id) {
        TaskInstance task = taskService.find(id);
        taskService.cancelTask(task);
        return Result.success(taskService.find(id));
    }

    @GetMapping(value = "/flow/task/{taskInstanceId}/log", produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation("get task instance log")
    public ResponseEntity<Resource> getTaskInstanceLog(@PathVariable String taskInstanceId) {
        TaskInstance task = taskService.find(taskInstanceId);
        try {
            Path logPath = Paths.get(task.getLogPath());
            Resource resource = new UrlResource(logPath.toUri());
            return ResponseEntity
                    .ok()
                    .body(resource);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new ResourceNotFoundException("Malformed log file URL");
        }
    }
}
