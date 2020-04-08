package com.miotech.mdp.quality.models.vo.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;


/**
 * @author: shanyue.gao
 * @date: 2020/1/8 4:50 PM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchCaseRequestVo {

    private Integer pageNum;

    private Integer pageSize;

    @Nullable
    private String searchString;

}
