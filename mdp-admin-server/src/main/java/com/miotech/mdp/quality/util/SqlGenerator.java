package com.miotech.mdp.quality.util;

import cn.hutool.core.util.StrUtil;
import com.miotech.mdp.quality.models.enums.TemplateOperationEnum;

import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/3/19 12:51 PM
 */

public class SqlGenerator {

    private static final String BASE_STATEMENT = "select {}({}) as validate_{} from {};";

    private static final String BASE_NUM_STATEMENT_DISTINCT = "select count(distinct {}) as validate_result from {};";

    private static final String BASE_NUM_STATEMENT = "select sum(case when {} then 1 else 0 end) as validate_result from {};";

    private static final String BASE_TOTAL_NUM_STATEMENT = "select count(1) as validate_result from {};";

    private static final String BASE_RATIO_STATEMENT = "select round(cast(sum(case when {} then 1 else 0 end) as double) / cast(count(1) as double), 4) as validate_result from {};";

    private static final String DISTINCT_NUM_RATIO_STATEMENT = "select round(cast(count(distinct {} ) as double )/cast(count(1) as  double ), 4) as validate_result from {};";


    public static String generateSqlByOperation(TemplateOperationEnum operation, Boolean isTableLevel, List<String> fields, String tableName) {

        String sql = null;

        String singleField;
        if (!isTableLevel) {
            singleField = fields.get(0);
        } else {
            singleField = "";
        }
        switch (operation) {

            // base statement.
            case MAX:
                sql = StrUtil.format(BASE_STATEMENT, "max", singleField, tableName);
                break;

            case MIN:
                sql = StrUtil.format(BASE_STATEMENT, "min", singleField, tableName);
                break;

            case SUM:
                sql = StrUtil.format(BASE_STATEMENT, "sum", singleField, tableName);
                break;

            case AVERAGE:
                sql = StrUtil.format(BASE_STATEMENT, "avg", singleField, tableName);
                break;

            case NULL_NUM:
                sql = StrUtil.format(BASE_NUM_STATEMENT, singleField + " is null ", tableName);
                break;

            case EMPTY_NUM:
                sql = StrUtil.format(BASE_NUM_STATEMENT, singleField + " =''", tableName);
                break;

            case TOTAL_NUM:
                sql = StrUtil.format(BASE_TOTAL_NUM_STATEMENT, tableName);
                break;

            case DISTINCT_NUM:
                String f, fs;
                if (fields.size() > 1) {
                    f = fields.stream().reduce((a, b)-> a+", "+b).get();
                    fs = fields.stream().reduce((a, b) -> a+"_"+b).get();
                    sql = StrUtil.format(BASE_NUM_STATEMENT_DISTINCT, f, tableName);
                } else {
                    sql = StrUtil.format(BASE_NUM_STATEMENT_DISTINCT, singleField, tableName);
                }
                break;
            // end of base

            // ratio
            case EMPTY_NUM_RATIO:
                sql = StrUtil.format(BASE_RATIO_STATEMENT, singleField +" = ''", tableName);
                break;

            case DISTINCT_NUM_RATIO:

            case NULL_NUM_RATIO:
                sql = StrUtil.format(BASE_RATIO_STATEMENT, singleField+" is null", tableName);
                break;
            // end of ratio

            default:
                break;
        }
        return sql;
    }

}
