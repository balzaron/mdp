package com.miotech.mdp.quality.repository;

import com.miotech.mdp.quality.models.entity.DataTestResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author: shanyue.gao
 * @date: 2020/1/6 12:06 PM
 */
@Repository
public interface DataTestResultRepository extends CrudRepository<DataTestResult, String>, JpaRepository<DataTestResult, String> {

    Page<DataTestResult> findAllByCaseId(String caseId, Pageable pageable);
}
