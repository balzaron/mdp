package com.miotech.mdp.quality.models.vo.rule.request;

import com.miotech.mdp.quality.models.entity.CaseValidator;
import com.miotech.mdp.quality.models.enums.TemplateOperationEnum;
import com.miotech.mdp.quality.models.vo.test.RelatedTableVO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/3/13 2:11 PM
 */
@Data
@NoArgsConstructor
public class CreatePresetRuleTemplateVo {

    @NotNull
    private Boolean isBlockFlow;

    @NotNull
    private Boolean isTableLevel;

    private List<String> fieldIds;

    @NotNull
    private TemplateOperationEnum templateOperation;

    private String description;

    @NotBlank(message = "name cannot be null or empty")
    private String name;

    @NotNull
    private List<CaseValidator> validateObject;

    private List<String> tags;

    private List<RelatedTableVO> relatedTables;

    @NotNull
    private String ownerId;
}
