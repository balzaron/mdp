package com.miotech.mdp.quality.service;

import com.miotech.mdp.quality.models.entity.DataTestResult;
import com.miotech.mdp.quality.models.vo.test.DataTestResultPageVo;
import com.miotech.mdp.quality.models.vo.test.DataTestResultVo;
import org.springframework.data.domain.Page;

/**
 * @author: shanyue.gao
 * @date: 2020/1/6 12:07 PM
 */
public interface DataTestResultService {
    /**
     *
     * @param pageNum
     * @param pageSize
     * @param sortField
     * @return
     */
    Page<DataTestResult> findAll(Integer pageNum, Integer pageSize, String sortField);

    /**
     *
     * @param result
     * @return
     */
    DataTestResult insert(DataTestResultVo result);

    /**
     *
     * @param id
     * @return
     */

    DataTestResult findById(String id);

    /**
     *
     * @param id
     * @return
     */
    DataTestResultPageVo findByCaseId(String id, Integer pageNum, Integer  pageSize);



}
