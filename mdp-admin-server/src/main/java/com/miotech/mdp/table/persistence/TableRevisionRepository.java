package com.miotech.mdp.table.persistence;

import com.miotech.mdp.table.model.dao.MetaTableEntity;
import com.miotech.mdp.table.model.dao.MetaTableRevisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TableRevisionRepository
        extends JpaRepository<MetaTableRevisionEntity, String>, JpaSpecificationExecutor<MetaTableEntity> {

}
