package com.miotech.mdp.flow.repository;

import com.miotech.mdp.common.jpa.BaseRepository;
import com.miotech.mdp.flow.entity.dao.Operator;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OperatorRepository extends BaseRepository<Operator>, JpaSpecificationExecutor<Operator> {
}
