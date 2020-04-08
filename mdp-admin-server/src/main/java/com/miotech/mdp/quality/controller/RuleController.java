package com.miotech.mdp.quality.controller;

import com.miotech.mdp.common.model.vo.Result;
import com.miotech.mdp.quality.models.entity.DataTestResult;
import com.miotech.mdp.quality.models.vo.rule.request.CreateCustomizedRuleVo;
import com.miotech.mdp.quality.models.vo.rule.request.CreatePresetRuleTemplateVo;
import com.miotech.mdp.quality.models.vo.rule.request.FetchRulesListVo;
import com.miotech.mdp.quality.models.vo.rule.request.GetTemplateVo;
import com.miotech.mdp.quality.models.vo.rule.response.*;
import com.miotech.mdp.quality.service.RuleService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/3/11 7:52 PM
 */
@RestController
@RequestMapping("/api/rule")
@Slf4j
@Api(tags = "rules controller")
public class RuleController{

    @Autowired
    private RuleService ruleService;



    @PostMapping("/create-preset-rule")
    public Result<CreatePresetRuleTemplateResponseVo> createPresetRule(@RequestBody @Valid CreatePresetRuleTemplateVo createPresetRuleTemplateVo) {
        Result<CreatePresetRuleTemplateResponseVo> result = new Result<>();
        result.setCode(0);
        result.setResult(ruleService.createPresetRuleTemplate(createPresetRuleTemplateVo));
        return result;
    }

    @PostMapping("/create-customized-rule")
    public Result<CreateCustomizedRuleResponseVo> createCustomizedRule(@RequestBody @Valid CreateCustomizedRuleVo createCustomizedRuleVo) {
        Result<CreateCustomizedRuleResponseVo> result = new Result<>();
        result.setCode(0);
        result.setResult(ruleService.createCustomizedRule(createCustomizedRuleVo));
        return result;
    }

    @PostMapping("/get-template")
    public Result<GetTemplateResponseVo> getTemplate(@RequestBody @Valid GetTemplateVo getTemplateVo) {
        Result<GetTemplateResponseVo> result = new Result<>();
        result.setCode(0);
        result.setResult(ruleService.getTemplate(getTemplateVo));
        return result;
    }

    @PostMapping("/fetch-rules-list")
    public Result<FetchRulesListResponsePageVo> fetchRulesList(@RequestBody @Valid FetchRulesListVo fetchRulesListVo) {
        Result<FetchRulesListResponsePageVo> result = new Result<>();
        result.setCode(0);
        result.setResult(ruleService.fetchRulesList(fetchRulesListVo));
        return result;
    }

    @PutMapping("/update-customized-rule")
    public Result<CreateCustomizedRuleResponseVo> updateCustomizedRule(@RequestBody CreateCustomizedRuleResponseVo createCustomizedRuleVo) {
        Result<CreateCustomizedRuleResponseVo> result = new Result<>();
        result.setCode(0);
        result.setResult(ruleService.updateCustomizedRule(createCustomizedRuleVo));
        return result;
    }

    @PutMapping("/update-preset-rule")
    public Result<CreatePresetRuleTemplateResponseVo> udpatePresetRule(@RequestBody CreatePresetRuleTemplateResponseVo createPresetRuleTemplateResponseVo) {
        Result<CreatePresetRuleTemplateResponseVo> result = new Result<>();
        result.setCode(0);
        result.setResult(ruleService.updatePresetRule(createPresetRuleTemplateResponseVo));
        return result;
    }

    @DeleteMapping("/delete-rule")
    public Result<String> delRule(@RequestParam String id) {
        String ret = ruleService.deleteRule(id);
        Result<String> result = new Result<>();
        result.setResult(ret);
        result.setCode(0);
        return result;
    }

    @GetMapping("/async-get-result")
    public Result<List<DataTestResult>> asyncGetResult(@RequestParam List<String> resultIds) {
        List<DataTestResult> dataTestResultVos = new ArrayList<>(
                ruleService.getResultsByCaseId("")
        );
        Result<List<DataTestResult>> result = new Result<>();
        result.setResult(dataTestResultVos);
        result.setCode(0);
        return result;
    }

    @GetMapping("/get-rule-detail")
    public Result<RuleDetailVo> getRuleDetail(@RequestParam @Validated @NotBlank String id) {
        RuleDetailVo ruleDetailVo = ruleService.getRuleDetailById(id);
        Result<RuleDetailVo> result = new Result<>();
        result.setCode(0);
        result.setResult(ruleDetailVo);
        return result;
    }

    @DeleteMapping("/delete-all-by-ids")
    public Result<List<String>> deleteAllByIds(@RequestParam List<String> ids) {
        List<String> res = ruleService.deleteRuleByIds(ids);
        return Result.success(res);
    }
}
