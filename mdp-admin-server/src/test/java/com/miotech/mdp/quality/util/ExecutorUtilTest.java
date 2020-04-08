package com.miotech.mdp.quality.util;

import com.miotech.mdp.quality.BaseTest;
import com.miotech.mdp.quality.models.entity.CaseValidator;
import com.miotech.mdp.quality.models.entity.DataTestResult;
import com.miotech.mdp.quality.models.vo.rule.request.CreateCustomizedRuleVo;
import com.miotech.mdp.quality.models.vo.test.RelatedTableVO;
import com.miotech.mdp.quality.service.RuleService;
import com.miotech.mdp.table.model.bo.TableInfo;
import com.miotech.mdp.table.service.TableService;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author: shanyue.gao
 * @date: 2020/4/7 5:15 PM
 */
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class})
@AutoConfigureMockMvc
@WithMockUser(username = "test")
public class ExecutorUtilTest extends BaseTest {

    @Autowired
    private RuleService ruleService;

    @Autowired
    private ExecutorUtil executorUtil;

    private String caseId;

    @Autowired
    private TableService tableService;

    private String tableId;
    @Before
    public void init() {
        System.out.println("start init");
        TableInfo tableInfo =  TableInfo.builder()
                .currentDBId(8)
                .schema("dm")
                .dbType("ATHENA")
                .lifecycle("Process")
                .tableName("TEST")
                .referenceDBIds(Collections.singletonList(8))
                .build();

        tableId = tableService.createTable(tableInfo).getId();

        CreateCustomizedRuleVo request = new CreateCustomizedRuleVo();
        request.setCaseSql("select * from nothing");
        List<CaseValidator> validators = Collections.singletonList(new CaseValidator().setExpected(true).setFieldName("v").setOperator("=="));
        RelatedTableVO relatedTableVO = new RelatedTableVO();
        relatedTableVO.setId(tableId);
        relatedTableVO.setDbType("ATHENA");
        List<RelatedTableVO> relatedTableVOS = Collections.singletonList(relatedTableVO);
        request.setDbId(8)
                .setName("testing")
                .setValidateObject(validators)
                .setRelatedTables(relatedTableVOS);
        caseId = ruleService.createCustomizedRule(request).getId();
    }

    @Test
    public void  test1() throws ExecutionException, InterruptedException {

        List<Future<DataTestResult>> resultFutures = executorUtil.submit(Collections.singletonList(caseId));
        DataTestResult result = resultFutures.get(0).get();
        Assertions.assertThat(result.getPassed()).isFalse();
        Assertions.assertThat(result.getCaseId()).isEqualTo(caseId);
        Assertions.assertThat(result.getErrorCatchLog())
                .contains(caseId);
    }

    @After
    public void destroy() {
        ruleService.deleteRule(caseId);
        tableService.delete(tableId);
        System.out.println("destroyed");
    }
}
