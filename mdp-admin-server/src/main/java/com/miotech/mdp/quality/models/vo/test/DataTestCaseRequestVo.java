package com.miotech.mdp.quality.models.vo.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/1/8 6:08 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DataTestCaseRequestVo {

    private String description;

    private String name;

    private String caseSql;

    private List<String> tags;

    private List<RelatedTableVO> relatedTables;

    private Integer dbId;
}
