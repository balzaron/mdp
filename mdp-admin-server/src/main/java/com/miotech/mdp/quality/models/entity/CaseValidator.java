package com.miotech.mdp.quality.models.entity;

import cn.hutool.core.convert.Convert;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Type;

/**
 * @author: shanyue.gao
 * @date: 2020/3/23 2:54 PM
 */

@Data
@Accessors(chain = true)
public class CaseValidator {

    private String fieldName;

    private String operator;

    private Object expected;

    public <T> void convertExp(Class<T> tClass) {
        Convert.convert((Type) tClass, expected);
    }
}
