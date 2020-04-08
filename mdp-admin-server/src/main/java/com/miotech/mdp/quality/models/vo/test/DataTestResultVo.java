package com.miotech.mdp.quality.models.vo.test;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author: shanyue.gao
 * @date: 2020/1/6 12:02 PM
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DataTestResultVo implements Serializable {

    private String id;

    private Boolean passed;

    private String caseId;

    private String errorCatchLog;
}
