package com.miotech.mdp.quality.models.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: shanyue.gao
 * @date: 2020/3/24 6:52 PM
 */
@Data
@Accessors(chain = true)
public class ExecuteResult {

    private String fieldName;

    private Object result;
}
