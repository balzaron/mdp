package com.miotech.mdp.quality.models.enums;

import cn.hutool.core.collection.CollUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: shanyue.gao
 * @date: 2020/3/11 2:03 PM
 */


public enum TemplateOperationEnum {

    //总行数
    TOTAL_NUM(QualityPropertyEnum.CONSISTENCY, TemplateTypeEnum.TABLE),

    //去重后总行数
    DISTINCT_NUM(QualityPropertyEnum.ACCURACY, TemplateTypeEnum.BOTH),

    //去重后行数占比
    DISTINCT_NUM_RATIO(QualityPropertyEnum.ACCURACY, TemplateTypeEnum.BOTH),

    //平均值
    AVERAGE(QualityPropertyEnum.ACCURACY, TemplateTypeEnum.NUMERIC),

    //最大值
    MAX(QualityPropertyEnum.ACCURACY, TemplateTypeEnum.NUMERIC),

    //最小值
    MIN(QualityPropertyEnum.ACCURACY, TemplateTypeEnum.NUMERIC),

    //空对象行数
    NULL_NUM(QualityPropertyEnum.ACCURACY, TemplateTypeEnum.BOTH),

    //空对象行数占比
    NULL_NUM_RATIO(QualityPropertyEnum.ACCURACY, TemplateTypeEnum.BOTH),

    //空值行数
    EMPTY_NUM(QualityPropertyEnum.ACCURACY, TemplateTypeEnum.BOTH),

    //空值行数占比
    EMPTY_NUM_RATIO(QualityPropertyEnum.ACCURACY, TemplateTypeEnum.BOTH),

    //总和
    SUM(QualityPropertyEnum.ACCURACY, TemplateTypeEnum.NUMERIC);

    @Getter
    private QualityPropertyEnum qualityProperty;

    @Getter
    private TemplateTypeEnum templateType;

    TemplateOperationEnum(QualityPropertyEnum qualityProperty,
                          TemplateTypeEnum templateType) {
        this.qualityProperty=qualityProperty;
        this.templateType=templateType;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public static List<TemplateOperationEnum> list(TemplateTypeEnum type) {
        List<TemplateOperationEnum> all = new ArrayList<>(
                Arrays.asList(TemplateOperationEnum.values())
        );
        if (type.equals(TemplateTypeEnum.BOTH)) {
            return all.stream().filter(
                    c -> c.templateType.equals(TemplateTypeEnum.BOTH)
            ).collect(Collectors.toList());
        } else if (type.equals(TemplateTypeEnum.TABLE)) {
            return all.stream().filter(
                    c -> c.templateType.equals(TemplateTypeEnum.TABLE)
            ).collect(Collectors.toList());
        } else {
            List<TemplateOperationEnum> hits = all.stream().filter(
                    c -> c.templateType.equals(type)
            ).collect(Collectors.toList());

            return new ArrayList<>(CollUtil.union(hits, list(TemplateTypeEnum.BOTH), list(TemplateTypeEnum.TABLE)));

        }
    }

}
