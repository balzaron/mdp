package com.miotech.mdp.quality.models.vo.rule.request;

import com.miotech.mdp.quality.models.vo.rule.FilterVo;
import com.miotech.mdp.quality.models.vo.rule.SortField;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author: shanyue.gao
 * @date: 2020/3/17 5:05 PM
 */

@Data
@NoArgsConstructor
public class FetchRulesListVo {

    @NotBlank
    private String tableId;

    private FilterVo filters;

    private Integer pageSize;

    private Integer pageNum;

    private SortField sortField;
}
