package com.miotech.mdp.quality.models.vo.rule;

import com.miotech.mdp.quality.models.enums.QualityPropertyEnum;
import com.miotech.mdp.quality.models.enums.RuleTypeEnum;
import com.miotech.mdp.quality.models.enums.TemplateOperationEnum;
import lombok.Data;

/**
 * @author: shanyue.gao
 * @date: 2020/3/17 5:15 PM
 */
@Data
public class FilterVo {
    private RuleTypeEnum ruleType;

    private QualityPropertyEnum qualityProperty;

    private String ownerId;

    private TemplateOperationEnum templateOperation;
}
