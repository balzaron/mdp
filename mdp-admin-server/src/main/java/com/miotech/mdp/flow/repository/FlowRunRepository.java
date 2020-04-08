package com.miotech.mdp.flow.repository;

import com.miotech.mdp.common.jpa.BaseRepository;
import com.miotech.mdp.flow.constant.FlowState;
import com.miotech.mdp.flow.entity.dao.FlowRun;
import com.miotech.mdp.flow.entity.dao.IStateCount;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlowRunRepository extends BaseRepository<FlowRun>, JpaSpecificationExecutor<FlowRun> {
    @Query(value = "select run.* from flow_run AS run" +
            " WHERE run.flow_id = ?1 and (deleted = 0) " +
            " ORDER BY create_time DESC LIMIT 1 ",
            nativeQuery = true)
    Optional<FlowRun> findLatestRun(String flowid);


    @Query(value = "select run from FlowRun run WHERE run.state in ?1")
    List<FlowRun> findByStates(List<FlowState> flowStates);

    @Query(value = "select run.state as state, count(run.id) as count " +
            "FROM FlowRun run " +
            "WHERE run.flow.id in ?1 group by run.state")
    List<IStateCount> countFlowRunByFlow(List<String> flowIds);

}
