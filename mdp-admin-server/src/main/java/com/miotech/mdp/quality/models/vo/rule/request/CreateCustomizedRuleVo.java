package com.miotech.mdp.quality.models.vo.rule.request;

import com.miotech.mdp.quality.models.entity.CaseValidator;
import com.miotech.mdp.quality.models.vo.test.RelatedTableVO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/3/13 2:24 PM
 */

@Accessors(chain = true)
@Data
@NoArgsConstructor
public class CreateCustomizedRuleVo {

    @NotBlank(message = "sql cannot be null or empty")
    private String caseSql;

    @NotNull
    private Integer dbId;

    private String description;

    @NotBlank(message = "name cannot be null or empty")
    private String name;

    @NotNull
    private List<CaseValidator> validateObject;

    private List<String> tags;

    private List<RelatedTableVO> relatedTables;

    @NotBlank
    private String ownerId;

}
