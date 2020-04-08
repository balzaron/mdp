package com.miotech.mdp.quality.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Editor;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.util.concurrent.MoreExecutors;
import com.miotech.mdp.common.client.metabase.MetabaseCardRequestFactory;
import com.miotech.mdp.common.client.metabase.MetabaseClient;
import com.miotech.mdp.common.models.protobuf.metabase.*;
import com.miotech.mdp.quality.models.entity.CaseValidator;
import com.miotech.mdp.quality.models.entity.DataTestCase;
import com.miotech.mdp.quality.models.entity.DataTestResult;
import com.miotech.mdp.quality.models.entity.ExecuteResult;
import com.miotech.mdp.quality.repository.DataTestCaseRepository;
import com.miotech.mdp.quality.repository.DataTestResultRepository;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author: shanyue.gao
 * @date: 2020/1/13 3:07 PM
 */

@Component
@Slf4j
public class ExecutorUtil {

    /*
    Because of an entity could not get a connection to DB via a thread handover the task to another threadpool.
    so we choice a bio task to handle the db connection.
     */
    private ExecutorService threadPoolTaskExecutor = MoreExecutors.newDirectExecutorService();

    @Autowired
    private MetabaseClient metabaseClient;

    @Autowired
    private DataTestResultRepository resultRepository;

    @Autowired
    private DataTestCaseRepository caseRepository;

    /**
     * submit by case ids
     * @return result IDS
     */
    public List<Future<DataTestResult>> submit(List<String> testCaseIds) {
        List<String> resultIds = new ArrayList<>();

        return testCaseIds.stream().map(i -> {
            DataTestResult dataTestResult = new DataTestResult();
            dataTestResult.setCaseId(i);
            DataTestResult dataTestResultSaved = resultRepository.saveAndFlush(dataTestResult);
            resultIds.add(dataTestResultSaved.getId());
            //submit tasks
            Callable<DataTestResult> callable = buildCaseExecutorFutureTask(i, dataTestResultSaved);
            return threadPoolTaskExecutor.submit(callable);
        } ).collect(Collectors.toList());
    }

    @Data
    @Accessors(chain = true)
    static class DataCase {
        private String id;
        private String sql;
        private List<CaseValidator> validate;
        private Integer dbId;
    }

    private Callable<DataTestResult> buildCaseExecutorFutureTask(String testCaseId, DataTestResult dataTestResult) {
        DataTestCase testCase = caseRepository.getOne(testCaseId);
        Integer dbId = testCase.getDbId();
        Long starTime = System.currentTimeMillis();
        log.info("running case id is: {}, name is: {}, start time is: {}", testCase.getId(), testCase.getName(), LocalDateTime.now().toString());

        return () -> {
                    QueryData queryData;

                    try {
                        DatasetQuery query = MetabaseCardRequestFactory.createDatasetQuery(dbId, testCase.getCaseSql(), null);
                        QueryResult result = metabaseClient.queryDataset(query);
                        queryData = result.getData();
                    } catch (Exception ex) {
                        DataTestCase case1 = caseRepository.getOne(testCaseId);
                        caseRepository.saveAndFlush(case1);
                        dataTestResult.setCaseId(testCaseId)
                                .setTemplateOperation(case1.getTemplateOperation())
                                .setPassed(false)
                                .setErrorCatchLog(ex.getMessage())
                                .setImpactLevel(case1.getQualityProperty());
                        return dataTestResult;
                    }
                    List<Row> rows;
                    List<Col> columns;

                    if (queryData.getRowsCount() > 0) {
                        rows = queryData.getRowsList();
                        columns = queryData.getColsList();
                    } else {
                        String errorLog = StrUtil.format("query nothing exception case id is: {} query data is: {}", testCaseId, queryData.toString());
                        log.error(errorLog);
                        DataTestCase case1 = caseRepository.getOne(testCaseId);
                        dataTestResult.setCaseId(testCaseId)
                                .setTemplateOperation(case1.getTemplateOperation())
                                .setPassed(false)
                                .setErrorCatchLog(errorLog)
                                .setImpactLevel(case1.getQualityProperty());
                        return resultRepository.saveAndFlush(dataTestResult);
                    }
                    val colNum = columns.size();
                    List<CaseValidator> validates = testCase.getValidateObject();
                    List<AssertResult> assertResults = new ArrayList<>();

                    List<ExecuteResult> executeResults = new ArrayList<>();
                    for (Row row : rows) {
                        int j;
                        for (j = 0; j < colNum; j++) {
                            String colType = columns.get(j).getBaseType();
                            String colName = columns.get(j).getName();
                            RowValue rowValue = row.getValues(j);

                            CaseValidator target = getFiltered(colName, validates);
                            if (ObjectUtil.isEmpty(target)) {
                                continue;
                            }

                            validates.forEach(v -> {
                                Object expected = null;
                                String operator = v.getOperator();
                                Object actual = null;

                                if ("type/Text".equals(colType)) {
                                    v.convertExp(String.class);
                                    expected = v.getExpected();
                                    actual = rowValue.getStringValue();
                                } else if ("type/Integer".equals(colType)) {
                                    v.convertExp(Integer.class);
                                    expected = v.getExpected();
                                    actual = rowValue.getNumberValue();
                                } else if ("type/Boolean".equals(colType)) {
                                    v.convertExp(Boolean.class);
                                    expected = v.getExpected();
                                    actual = rowValue.getBooleanValue();
                                } else if ("type/Float".equals(colType) || "type/Number".equals(colType)) {
                                    v.convertExp(Double.class);
                                    expected = v.getExpected();
                                    actual = Float.parseFloat(rowValue.getNumberValue());
                                }
                                val res = assertion(actual, operator, expected, colName);
                                ExecuteResult executeResult = new ExecuteResult();
                                executeResult.setFieldName(colName);
                                executeResult.setResult(actual);
                                executeResults.add(executeResult);
                                assertResults.add(res);
                                log.info("case id {} expected expression is: {} {} {}", testCaseId, actual, operator, expected);
                            });
                        }
                    }

                    List<AssertResult> results = CollUtil.filter(assertResults,
                            (Editor<AssertResult>) assertResult -> {
                                if (assertResult.getPassed().equals(false)) {
                                    return assertResult;
                                }
                                return null;
                            });
                    if (CollUtil.isEmpty(results)) {
                        Long endTime = System.currentTimeMillis();
                        log.info("case id {} executed successfully! case end time is :{}, case consumed {} ms",
                                testCaseId,
                                LocalDateTime.now().toString(), endTime-starTime);
                        DataTestCase case1 = caseRepository.getOne(testCaseId);
                        dataTestResult.setCaseId(testCaseId)
                                .setTemplateOperation(case1.getTemplateOperation())
                                .setPassed(true)
                                .setErrorCatchLog(null)
                                .setExecuteResults(executeResults)
                                .setImpactLevel(case1.getQualityProperty());

                        return resultRepository.saveAndFlush(dataTestResult);

                    } else {
                        String errLogs = "";
                        for (AssertResult r : results) {
                            errLogs += r.getErrorLog();
                        }
                        Long endTime = System.currentTimeMillis();
                        log.warn("case executed failed! error log is: {}, case end time is: {}, case consumed {} ms", errLogs, LocalDateTime.now().toString(), endTime-starTime);
                        DataTestCase case1 = caseRepository.getOne(testCaseId);
                        caseRepository.saveAndFlush(case1);
                        dataTestResult.setCaseId(testCaseId)
                                .setTemplateOperation(case1.getTemplateOperation())
                                .setPassed(false)
                                .setErrorCatchLog(errLogs)
                                .setExecuteResults(executeResults)
                                .setImpactLevel(case1.getQualityProperty());
                        return resultRepository.saveAndFlush(dataTestResult);
                    }
                };
        }

    private CaseValidator getFiltered(String fieldName, List<CaseValidator> array) {
        CaseValidator target = new CaseValidator();

        for (CaseValidator o : array) {
            if (fieldName.equals(o.getFieldName())){
                target = o;
                break;
            }
        }
        return target;
    }

    private AssertResult assertion(Object actual, String operator, Object expected, String fieldName) {
        try {
            switch (operator) {
                case "==":
                    Assertions.assertThat(actual)
                            .isEqualTo(expected);
                    break;
                case ">=":
                    Assertions.assertThat(Convert.convert(Double.class, actual) >= Convert.convert(Double.class, expected))
                            .isTrue();
                    break;
                case ">":
                    Assertions.assertThat(Convert.convert(Double.class, actual) > Convert.convert(Double.class, expected))
                            .isTrue();
                    break;
                case "<=":
                    Assertions.assertThat(Convert.convert(Double.class, actual) <= Convert.convert(Double.class, expected))
                            .isTrue();
                    break;

                case "<":
                    Assertions.assertThat(Convert.convert(Double.class, actual) < Convert.convert(Double.class, expected))
                            .isTrue();
                    break;

                case "!=":
                    Assertions.assertThat(actual != expected)
                            .isTrue();
                    break;
                case "is":
                    Assertions.assertThat(actual)
                            .isEqualTo(Convert.convert(Boolean.class, expected));

                    break;
                case "isNot":
                    Assertions.assertThat(actual)
                            .isNotEqualTo(Convert.convert(Boolean.class, expected));
                    break;
                default:
                    break;
            }
            return new AssertResult().setErrorLog(null).setPassed(true);
        } catch (AssertionError ae){
            String msg = String.format("the field %s actual return is %s but expected return is %s\n", fieldName, actual, expected);
            return new AssertResult().setPassed(false).setErrorLog(msg);
        }
    }

    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    static class AssertResult {
        private Boolean passed;
        private String errorLog;
    }
}
