package com.miotech.mdp.flow.service;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SchedulerService {

    @Autowired
    private Scheduler scheduler;

    public JobDetail getTaskInfo(JobKey jobKey)
            throws SchedulerException {
        return scheduler.getJobDetail(jobKey);
    }

    public void rescheduleTask(JobDetail job, Trigger triger)
            throws SchedulerException {
        String jobName = job.getKey().getName();

        List<Trigger> oldTrigger = (List<Trigger>) scheduler
                .getTriggersOfJob(job.getKey());
        if (!oldTrigger.isEmpty()) {
            scheduler.rescheduleJob(oldTrigger.get(0).getKey(), triger);
        } else {
            log.error("No existed trigger for job : {}", jobName);
            throw new RuntimeException("No existed trigger for job");
        }
    }

    public void scheduleTask(JobDetail job, Trigger triger)
            throws SchedulerException {
        try {
            scheduler.scheduleJob(job, triger);
        } catch (org.quartz.ObjectAlreadyExistsException existed ){
            log.info("Job already exists: {}", job.getKey().getName());
            rescheduleTask(job, triger);
        }
    }

    public void deleteTask(JobKey jobKey)
            throws SchedulerException {
        scheduler.deleteJob(jobKey);
    }
}
