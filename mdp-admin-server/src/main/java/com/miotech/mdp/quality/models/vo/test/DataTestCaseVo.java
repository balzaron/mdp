package com.miotech.mdp.quality.models.vo.test;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2019/12/26 11:25 AM
 */

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class DataTestCaseVo implements Serializable {

    private String id;

    private String name;

    private String caseSql;

    private String description;

    private JSONArray validateObject;

    private List<String> tags;

    private List<RelatedTableVO> relatedTables;

    private LocalDateTime updateTime;

    private Integer dbId;
}
