package com.miotech.mdp.quality.service;

import com.miotech.mdp.ServiceTest;
import com.miotech.mdp.quality.models.entity.CaseValidator;
import com.miotech.mdp.quality.models.enums.TemplateOperationEnum;
import com.miotech.mdp.quality.models.vo.rule.request.CreatePresetRuleTemplateVo;
import com.miotech.mdp.quality.models.vo.rule.response.CreatePresetRuleTemplateResponseVo;
import com.miotech.mdp.quality.models.vo.test.RelatedTableVO;
import com.miotech.mdp.quality.repository.DataTestCaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: shanyue.gao
 * @date: 2020/3/23 4:15 PM
 */

@Slf4j
public class RuleServiceTest extends ServiceTest {


    @Autowired
    private RuleService ruleService;

    @Autowired
    private DataTestCaseRepository caseRepository;

    static CreatePresetRuleTemplateVo createPresetRuleTemplateVo = new CreatePresetRuleTemplateVo();

    @BeforeAll
    public static void before() {
        RelatedTableVO relatedTableVO = new RelatedTableVO();
        relatedTableVO.setId("1582570382819328");
        createPresetRuleTemplateVo.setDescription("desc");
        createPresetRuleTemplateVo.setIsBlockFlow(true);
        createPresetRuleTemplateVo.setIsTableLevel(false);
        createPresetRuleTemplateVo.setName("preset rule for ut");
        createPresetRuleTemplateVo.setOwnerId("0");
        createPresetRuleTemplateVo.setRelatedTables(Collections.singletonList(relatedTableVO));
        createPresetRuleTemplateVo.setTemplateOperation(TemplateOperationEnum.EMPTY_NUM_RATIO);
        createPresetRuleTemplateVo.setValidateObject(Collections
                .singletonList(
                        new CaseValidator().setFieldName("field1").setOperator("==").setExpected(0)));
        createPresetRuleTemplateVo.setFieldIds(Collections.singletonList("4736881828823040"));
    }

    //=========== test cases==============

    @Test
    public void testAdd() {
        String id = addRule();
        Assertions.assertThat(id).isNotBlank();
        delRule(id);
    }

    @Test
    public void testEdit() {
    }

    @Test
    public void testDeleteByIds() {
        String id = addRule();
        List<String> ids = ruleService.deleteRuleByIds(Collections.singletonList(id));
        List<Integer> list = new ArrayList<>();
        ids.forEach(
                i -> caseRepository.findById(i).map(
                        a -> list.add(1)
                )        );
        Assertions.assertThat(list).isEmpty();
    }

    //========== after all ==============
    @After
    public void teardown() {
    }


    private String addRule() {
        CreatePresetRuleTemplateVo createPresetRuleTemplateVo = new CreatePresetRuleTemplateVo();

        RelatedTableVO relatedTableVO = new RelatedTableVO();
        relatedTableVO.setId("1582570382819328");
        createPresetRuleTemplateVo.setDescription("desc");
        createPresetRuleTemplateVo.setIsBlockFlow(true);
        createPresetRuleTemplateVo.setIsTableLevel(false);
        createPresetRuleTemplateVo.setName("preset rule for ut");
        createPresetRuleTemplateVo.setOwnerId("0");
        createPresetRuleTemplateVo.setRelatedTables(Collections.singletonList(relatedTableVO));
        createPresetRuleTemplateVo.setTemplateOperation(TemplateOperationEnum.EMPTY_NUM_RATIO);
        createPresetRuleTemplateVo.setValidateObject(Collections
                .singletonList(
                        new CaseValidator().setFieldName("field1").setOperator("==").setExpected(0)));
        createPresetRuleTemplateVo.setFieldIds(Collections.singletonList("4736881828823040"));
        CreatePresetRuleTemplateResponseVo responseVo = ruleService.createPresetRuleTemplate(createPresetRuleTemplateVo);
        return responseVo.getId();
    }

    private void delRule(String id) {
        ruleService.deleteRule(id);
    }
}
