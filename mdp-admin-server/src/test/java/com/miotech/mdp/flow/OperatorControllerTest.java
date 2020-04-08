package com.miotech.mdp.flow;

import com.miotech.mdp.common.util.JSONUtil;
import com.miotech.mdp.flow.entity.bo.OperatorSearch;
import com.miotech.mdp.flow.entity.bo.ParameterInfo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OperatorControllerTest extends BaseControllerTest {

    @Test
    public void testCreateOperator() throws Exception {
        String operatorId = createBashOperator();

        getR("/api/operator/" + operatorId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").exists())
                .andExpect(jsonPath("$.result.parameters").isNotEmpty())
                .andReturn();
        OperatorSearch search = new OperatorSearch();
        search.setTags(new String[]{"test"});
        postR("/api/operator/list", JSONUtil.objectToString(search))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.result.operators").isNotEmpty());;

        deleteR("/api/operator/" + operatorId)
        .andExpect(status().is2xxSuccessful())
        .andReturn();
    }

    @Test
    public void testCreateOperatorWithChoieUrl() throws Exception {
        List< ParameterInfo> parameterList = new ArrayList<>();
        ParameterInfo p = new ParameterInfo();
        p.setParameterKey("a");
        p.setParameterType("choices");
        p.setChoiceUrl("http://test-url");
        parameterList.add(p);
        String operatorId = createBashOperator(null, parameterList);

        getR("/api/operator/"+ operatorId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").exists())
                .andExpect(jsonPath("$.result.parameters").isNotEmpty())
                .andReturn();

        deleteR("/api/operator/" + operatorId)
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }
}