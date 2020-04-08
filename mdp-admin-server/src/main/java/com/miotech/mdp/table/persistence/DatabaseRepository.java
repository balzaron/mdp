package com.miotech.mdp.table.persistence;

import com.miotech.mdp.table.model.dao.MetaDatabaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseRepository extends JpaRepository<MetaDatabaseEntity, Integer> {

}
