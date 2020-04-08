package com.miotech.mdp.quality.models.vo.test;

import com.miotech.mdp.quality.models.entity.CaseValidator;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/1/13 8:33 PM
 */

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DataTestCaseResultVo {
    private String id;

    private String name;

    private String caseSql;

    private String description;

    private List<CaseValidator> validateObject;

    private List<String> tags;

    private Integer dbId;

    private LocalDateTime updateTime;

    private List<RelatedTableVO> relatedTables;
}
