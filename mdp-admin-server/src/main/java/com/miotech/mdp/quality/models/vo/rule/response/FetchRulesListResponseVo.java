package com.miotech.mdp.quality.models.vo.rule.response;

import com.miotech.mdp.quality.models.entity.CaseValidator;
import com.miotech.mdp.quality.models.enums.QualityPropertyEnum;
import com.miotech.mdp.quality.models.enums.RuleTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/3/17 5:23 PM
 */

@Data
public class FetchRulesListResponseVo {

    private String id;

    private List<CaseValidator> validateObject;

    private String ownerName;

    private LocalDateTime lastRunTime;

    private QualityPropertyEnum qualityProperty;

    private String name;

    private RuleTypeEnum ruleType;
}
