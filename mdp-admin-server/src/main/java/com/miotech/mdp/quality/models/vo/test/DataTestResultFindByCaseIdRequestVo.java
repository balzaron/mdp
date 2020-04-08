package com.miotech.mdp.quality.models.vo.test;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author: shanyue.gao
 * @date: 2020/1/17 10:23 AM
 */

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DataTestResultFindByCaseIdRequestVo {

    private Integer pageNum;

    private Integer pageSize;

    private String caseId;
}
