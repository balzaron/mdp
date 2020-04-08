package com.miotech.mdp.quality.models.vo.rule.response;

import com.miotech.mdp.quality.models.entity.CaseValidator;
import com.miotech.mdp.quality.models.enums.RuleTypeEnum;
import com.miotech.mdp.quality.models.enums.TemplateOperationEnum;
import com.miotech.mdp.quality.models.enums.TemplateTypeEnum;
import com.miotech.mdp.quality.models.vo.test.RelatedTableVO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/3/17 4:16 PM
 */

@Data
@NoArgsConstructor
public class CreatePresetRuleTemplateResponseVo {

    @NotBlank(message = "id could not be null or empty")
    private String id;

    private Boolean isBlockFlow;

    private Boolean isTableLevel;

    private TemplateTypeEnum templateType;

    private TemplateOperationEnum templateOperation;

    private String description;

    private String name;

    private List<CaseValidator> validateObject;

    private List<String> tags;

    private List<RelatedTableVO> relatedTables;

    private String ownerId;

    private List<String> fieldIds;

    private RuleTypeEnum ruleType;

}
