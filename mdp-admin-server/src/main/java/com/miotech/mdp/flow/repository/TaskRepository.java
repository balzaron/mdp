package com.miotech.mdp.flow.repository;

import com.miotech.mdp.common.jpa.BaseRepository;
import com.miotech.mdp.flow.constant.TaskState;
import com.miotech.mdp.flow.entity.dao.ITaskStateCount;
import com.miotech.mdp.flow.entity.dao.TaskInstance;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends BaseRepository<TaskInstance>, JpaSpecificationExecutor<TaskInstance> {
    @Query(value = "select task from TaskInstance task WHERE task.state in ?1 and task.flowTask <> null ")
    List<TaskInstance> findByStates(List<TaskState> taskStates);

    @Query(value = "select task from TaskInstance task WHERE task.state in ?1 and task.flowTask <> null " +
            "and task.maxRetry > task.tryNumber")
    List<TaskInstance> findRetryTasks(List<TaskState> taskStates);

    @Query(value = "select flowTask.id as flowTaskId, task.state as state, count(task.id) as count " +
            "FROM TaskInstance task join FlowTask flowTask on task.flowTask.id = flowTask.id " +
            "WHERE flowTask.flowId in ?1 group by flowTask.id , task.state")
    List<ITaskStateCount> countTasksByFlow(List<String> flowIds);
}
