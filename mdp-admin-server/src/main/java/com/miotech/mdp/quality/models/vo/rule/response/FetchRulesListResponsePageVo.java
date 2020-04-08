package com.miotech.mdp.quality.models.vo.rule.response;

import com.miotech.mdp.common.model.vo.PageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/3/17 4:40 PM
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class FetchRulesListResponsePageVo extends PageVO {

    private List<FetchRulesListResponseVo> fetchRulesListResponse;
}
