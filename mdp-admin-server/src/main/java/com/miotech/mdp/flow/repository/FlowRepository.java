package com.miotech.mdp.flow.repository;

import com.miotech.mdp.common.jpa.BaseRepository;
import com.miotech.mdp.flow.entity.dao.Flow;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowRepository extends BaseRepository<Flow>, JpaSpecificationExecutor<Flow> {
    @Query(value = "SELECT DISTINCT flow FROM Flow flow " +
            "INNER JOIN flow.flowTasks flowTask " +
            "INNER JOIN flowTask.relatedTables tbl WHERE tbl.id = ?1")
    List<Flow> findFlowsByReferencedTableId(String tableId);
}
