package com.miotech.mdp.quality.service;

import com.miotech.mdp.quality.models.entity.CaseValidator;
import com.miotech.mdp.quality.models.entity.DataTestResult;
import com.miotech.mdp.quality.models.vo.rule.request.CreateCustomizedRuleVo;
import com.miotech.mdp.quality.models.vo.rule.request.CreatePresetRuleTemplateVo;
import com.miotech.mdp.quality.models.vo.rule.request.FetchRulesListVo;
import com.miotech.mdp.quality.models.vo.rule.request.GetTemplateVo;
import com.miotech.mdp.quality.models.vo.rule.response.*;

import java.util.List;
import java.util.concurrent.Future;

/**
 * @author: shanyue.gao
 * @date: 2020/3/11 5:56 PM
 */
public interface RuleService {

    GetTemplateResponseVo getTemplate(GetTemplateVo getTemplate);

    CreateCustomizedRuleResponseVo createCustomizedRule(CreateCustomizedRuleVo createCustomizedRule);

    CreatePresetRuleTemplateResponseVo createPresetRuleTemplate(CreatePresetRuleTemplateVo createPresetRuleTemplate);

    FetchRulesListResponsePageVo fetchRulesList(FetchRulesListVo fetchRulesList);

    CreateCustomizedRuleResponseVo updateCustomizedRule(CreateCustomizedRuleResponseVo updateVo);

    CreatePresetRuleTemplateResponseVo updatePresetRule(CreatePresetRuleTemplateResponseVo updateVo);

    String deleteRule(String id);

    /**
     *
     * @param caseIds
     * @return task ids
     */
    List<Future<DataTestResult>> submitByCaseIds(List<String> caseIds);

    /**
     * get case running result by alias case id;
     * @param caseId
     * @return list of data_test_result;
     */
    List<DataTestResult> getResultsByCaseId(String caseId);

    /**
     * preset rules validate object generator
     */
    List<CaseValidator> sqlValidate(String sql, List<CaseValidator> validators);


    /**
     *
     * @param id case id
     * @return ruleDetail view model;
     */
    RuleDetailVo getRuleDetailById(String id);

    /**
     *
     * @param caseIds
     * @return ids
     */
    List<String> deleteRuleByIds(List<String> caseIds);
}
