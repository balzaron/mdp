package com.miotech.mdp.quality.models.vo.rule;

import com.alibaba.fastjson.JSONArray;
import com.miotech.mdp.quality.models.vo.test.RelatedTableVO;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/3/17 1:53 PM
 */
@Accessors(chain = true)
@Data
public class RuleBaseVo {

    private String description;

    @NotBlank(message = "name cannot be null or empty")
    private String name;

    @NotBlank(message = "validation cannot be null or empty")
    private JSONArray validateObject;

    private List<String> tags;

    @NotBlank(message = "sql cannot be null or empty")
    private List<RelatedTableVO> relatedTables;

    private String ownerId;
}
