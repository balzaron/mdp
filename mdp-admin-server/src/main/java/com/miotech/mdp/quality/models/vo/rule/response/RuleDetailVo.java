package com.miotech.mdp.quality.models.vo.rule.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.miotech.mdp.quality.models.entity.CaseValidator;
import com.miotech.mdp.quality.models.enums.QualityPropertyEnum;
import com.miotech.mdp.quality.models.enums.RuleTypeEnum;
import com.miotech.mdp.quality.models.enums.TemplateOperationEnum;
import com.miotech.mdp.quality.models.enums.TemplateTypeEnum;
import com.miotech.mdp.quality.models.vo.test.RelatedTableVO;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/3/26 2:08 PM
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleDetailVo {
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

    private String caseSql;

    private Integer dbId;

    private QualityPropertyEnum qualityPropertyEnum;
}
