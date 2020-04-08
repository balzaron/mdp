package com.miotech.mdp.quality.models.vo.test;

import com.miotech.mdp.quality.models.entity.CaseValidator;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;


@Data
@Accessors(chain = true)
@NoArgsConstructor
public class DataTestCaseValidationResult {

    private Integer dbId;

    private String caseSql;

    private List<CaseValidator> validateObject;

    private List<RelatedTableVO> relatedTables;

}
