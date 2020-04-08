package com.miotech.mdp.quality.models.vo.test;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author: shanyue.gao
 * @date: 2020/2/24 11:23 AM
 */
@Data
@Accessors(chain = true)
public class CaseCoverageResultVo {
    private LocalDateTime createTime;

    private BigDecimal coverage;

    private BigDecimal numerator;

    private BigDecimal denominator;
}
