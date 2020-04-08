package com.miotech.mdp.flow.service;

import com.miotech.mdp.flow.entity.dao.FlowTask;
import com.miotech.mdp.flow.entity.dao.TaskInstance;

public interface ExecutorService {

    void executeTask(TaskInstance task);

    void updateTask(TaskInstance task);

    void cancelTask(TaskInstance task);

    String getCommand(FlowTask task);

}
