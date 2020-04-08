package com.miotech.mdp.quality.models.vo.rule;

import com.miotech.mdp.quality.models.enums.OrderEnum;
import lombok.Data;

/**
 * @author: shanyue.gao
 * @date: 2020/3/17 5:19 PM
 */

@Data
public class SortField {
    private String field;

    private OrderEnum order;
}
