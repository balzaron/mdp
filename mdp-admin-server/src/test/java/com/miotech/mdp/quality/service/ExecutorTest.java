package com.miotech.mdp.quality.service;

import com.miotech.mdp.quality.models.entity.CaseValidator;
import com.miotech.mdp.quality.models.entity.DataTestResult;
import com.miotech.mdp.quality.models.vo.rule.request.CreateCustomizedRuleVo;
import com.miotech.mdp.quality.models.vo.rule.response.CreateCustomizedRuleResponseVo;
import com.miotech.mdp.quality.models.vo.test.RelatedTableVO;
import com.miotech.mdp.quality.util.ExecutorUtil;
import com.miotech.mdp.table.model.dao.MetaTableEntity;
import com.miotech.mdp.table.model.dao.TableMeta;
import com.miotech.mdp.table.persistence.TableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author: shanyue.gao
 * @date: 2020/4/2 1:04 PM
 */
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class})
@AutoConfigureMockMvc
@WithMockUser(username = "test")
public class ExecutorTest {

    @Autowired
    private ExecutorUtil executorUtil;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private TableRepository tableRepository;

    private String caseId;
    private String tableId;

    @BeforeTest
    public void init() {

        MetaTableEntity tableEntity = new MetaTableEntity();
        tableEntity.setName("testTable");
        tableEntity.setDatabaseType("athena");
        tableEntity.setSchema("schema");
        tableEntity.setDatabaseType("type1");
        tableEntity.setLifecycle("life");
        tableEntity.setDbId(8);
        tableEntity.setVersion(1);

        Integer[] refDbIds = {8};
        tableEntity.setRefDbIds(refDbIds);
        tableEntity.setIsArchived(false);

        TableMeta tableMeta = new TableMeta();
        tableMeta.setMetaData("meta");
        tableEntity.setTableMeta(tableMeta);

        MetaTableEntity savedTable = tableRepository.saveAndFlush(tableEntity);
        tableId = savedTable.getId();

        CaseValidator validator = new CaseValidator();
        validator.setFieldName("validate_result")
                .setExpected(0)
                .setOperator(">=");
        List<CaseValidator> validators = Collections.singletonList(validator);
        CreateCustomizedRuleVo ruleVo = new CreateCustomizedRuleVo();

        RelatedTableVO relatedTableVO = new RelatedTableVO();
        relatedTableVO.setId(tableId);

        ruleVo.setOwnerId("1")
                .setRelatedTables(Collections.singletonList(relatedTableVO))
                .setValidateObject(validators)
                .setName("test1")
                .setDbId(8)
                .setCaseSql("select round(cast(count(distinct exchangeid ) as double )/cast(count(1) as  double ), 6)*100.0 as validate_result from dm.cnassets_search_names;");
        CreateCustomizedRuleResponseVo responseVo = ruleService.createCustomizedRule(ruleVo);
        caseId = responseVo.getId();
    }

    @Test
    public void testExec() {
        List<Future<DataTestResult>> futures = executorUtil.submit(Collections.singletonList(caseId));

    }
}
