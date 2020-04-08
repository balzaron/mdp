package com.miotech.mdp.quality.models.vo.rule.response;

import com.miotech.mdp.quality.models.entity.CaseValidator;
import com.miotech.mdp.quality.models.enums.QualityPropertyEnum;
import com.miotech.mdp.quality.models.vo.test.RelatedTableVO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/3/17 4:22 PM
 */

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class CreateCustomizedRuleResponseVo {

    private String id;

    @NotBlank(message = "sql cannot be null or empty")
    private String caseSql;

    @NotBlank(message = "db id cannot be null or empty")
    private Integer dbId;

    private String description;

    @NotBlank(message = "name cannot be null or empty")
    private String name;

    @NotNull(message = "validation cannot be null or empty")
    private List<CaseValidator> validateObject;

    private List<String> tags;

    @NotNull(message = "sql cannot be null or empty")
    private List<RelatedTableVO> relatedTables;

    @NotNull
    private QualityPropertyEnum qualityPropertyEnum;

    private String ownerId;
}
