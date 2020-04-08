package com.miotech.mdp.flow.tasks;

import com.miotech.mdp.flow.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExecutionTask {

    @Autowired
    private TaskService taskService;

    @Scheduled(cron = "0/20 * * * * ?")
    public void runAndUpdateTask() {
        taskService.findRunningTasks()
                .forEach(taskService::updateTask);
    }

    @Scheduled(cron = "0/20 * * * * ?")
    public void retryTask() {
        taskService.findRetryTasks()
                .forEach(x -> taskService.retryTask(x));
    }
}
