package com.miotech.mdp.quality.service;

import com.miotech.mdp.quality.models.bo.DataTestCaseValidate;
import com.miotech.mdp.quality.models.vo.test.*;

import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/1/3 10:29 AM
 */
public interface DataTestCaseService {
    /**
     *
      * @param searchCaseRequestVo
     * @return
     */
     DataTestCasePageVo retrieve(SearchCaseRequestVo searchCaseRequestVo);

    /**
     *
     * @param id
     * @return
     */
    DataTestCaseResultVo findById(String id);

    /**
     *
     * @param
     * @return
     */
    String addCase(DataTestCaseRequestVo dataTestCaseRequestVo);

    /**
     *
     * @param id
     * @param dataTestCaseVo
     * @return
     */
    String updateCase(String id, DataTestCaseRequestVo dataTestCaseVo);

    /**
     *
     * @param id
     * @return
     */
    String deleteCase(String id);

    /**
     *
     * @param ids
     * @return
     */
    String deleteCases(List<String> ids);

    DataTestCaseValidationResult validateCase(DataTestCaseValidate dataTestCaseValidate);

    /**
     * @param tableId referenced table id
     * @return
     */
    List<DataTestCaseVo> getTableTestCases(String tableId);

    void deleteAll();
}
