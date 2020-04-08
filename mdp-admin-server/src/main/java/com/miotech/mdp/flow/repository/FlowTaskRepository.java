package com.miotech.mdp.flow.repository;

import com.miotech.mdp.common.jpa.BaseRepository;
import com.miotech.mdp.flow.entity.dao.FlowTask;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface FlowTaskRepository extends BaseRepository<FlowTask>, JpaSpecificationExecutor<FlowTask> {
    @Query(value = "SELECT flowTasks FROM FlowTask flowTasks " +
            "INNER JOIN flowTasks.relatedTables tbl WHERE tbl.id = ?1")
    List<FlowTask> findByReferencedTableId(String tableId);

    @Query(value = "SELECT flowTasks FROM FlowTask flowTasks " +
            "INNER JOIN flowTasks.flow flow " +
            "INNER JOIN flowTasks.relatedTables tbl " +
            "WHERE tbl.id = ?1 AND flow.id = ?2")
    List<FlowTask> findByReferencedTableIdAndFlowId(String tableId, String flowId);
}
