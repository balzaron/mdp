package com.miotech.mdp.flow;

import com.amazonaws.util.StringUtils;
import com.miotech.mdp.ServiceTest;
import com.miotech.mdp.common.util.JSONUtil;
import com.miotech.mdp.flow.entity.bo.OperatorInfo;
import com.miotech.mdp.flow.entity.bo.ParameterInfo;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public abstract class BaseControllerTest extends ServiceTest {

    @Autowired
    public MockMvc mvc;

    protected ResultActions getR(String url) throws Exception {
        return mvc
                .perform(
                        get(url).contentType(MediaType.APPLICATION_JSON)
                );
    }

    protected ResultActions postR(String url, String payload) throws Exception {
        MockHttpServletRequestBuilder actions = post(url)
                .contentType(MediaType.APPLICATION_JSON);
        if (payload != null) {
            actions.content(payload);
        }
        return mvc.perform(actions);
    }

    protected ResultActions putR(String url, String payload) throws Exception {
        MockHttpServletRequestBuilder actions = put(url)
                .contentType(MediaType.APPLICATION_JSON);
        if (payload != null) {
            actions.content(payload);
        }
        return mvc.perform(actions);
    }

    public String getId(MvcResult result) throws UnsupportedEncodingException {
        return JSONUtil.stringToJson(result.getResponse().getContentAsString())
                .get("result").getAsJsonObject()
                .get("id").getAsString();
    }

    protected ResultActions deleteR(String url) throws Exception {
        return mvc.perform(delete(url));
    }

    public String createSparkOperator() throws Exception {
        OperatorInfo operatorInfo = new OperatorInfo();
        operatorInfo.setName("test spark operator");
        operatorInfo.setTags(new String[]{"test"});
        operatorInfo.setPlatform("spark");
        JSONObject platformConfig = new JSONObject();
        platformConfig.put("files", "s3://com.miotech.data.prd/tmp/spark-examples_2.11-2.3.1.jar");
        platformConfig.put("jars", "");
        platformConfig.put("args", "");
        platformConfig.put("application", "org.apache.spark.examples.SparkPi");

        ParameterInfo p = new ParameterInfo();
        p.setParameterKey("iteration");
        p.setParameterType("number");
        ParameterInfo p2 = new ParameterInfo();
        p2.setParameterKey("code");
        p2.setParameterType("text");
        operatorInfo.setParameters(Arrays.asList(p, p2));

        operatorInfo.setPlatformConfig(platformConfig);
        operatorInfo.setParameterExecutionType("values");
        MvcResult result = postR("/api/operator",
                JSONUtil.objectToString(operatorInfo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.platform").value("spark"))
                .andExpect(jsonPath("$.result.name").exists())
                .andExpect(jsonPath("$.result.parameters").isNotEmpty())
                .andReturn();

        return getId(result);
    }


    public String createDockerOperator() throws Exception {
        OperatorInfo operatorInfo = new OperatorInfo();
        operatorInfo.setName("test K8S operator");
        operatorInfo.setTags(new String[]{"test"});
        operatorInfo.setPlatform("docker");
        JSONObject platformConfig = new JSONObject();
        platformConfig.put("image", "ubuntu");
        platformConfig.put("command", "ls -R /opt/mdp");
        platformConfig.put("name", "test job");
        ParameterInfo p = new ParameterInfo();

        p.setParameterKey("inputs");
        p.setParameterType("string");
        operatorInfo.setParameters(Collections.singletonList(p));

        operatorInfo.setPlatformConfig(platformConfig);
        operatorInfo.setParameterExecutionType("ignore");
        MvcResult result = postR("/api/operator",
                JSONUtil.objectToString(operatorInfo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.platform").value("docker"))
                .andExpect(jsonPath("$.result.name").exists())
                .andExpect(jsonPath("$.result.parameters").isNotEmpty())
                .andReturn();

        return getId(result);
    }

    public String createBuiltinOperator(String className, List<ParameterInfo> params) throws Exception {
        OperatorInfo operatorInfo = new OperatorInfo();
        operatorInfo.setName("test builtin operator");
        operatorInfo.setTags(new String[]{"test"});
        operatorInfo.setPlatform("builtin");
        JSONObject platformConfig = new JSONObject();
        platformConfig.put("className", className);

        operatorInfo.setPlatformConfig(platformConfig);
        operatorInfo.setParameterExecutionType("ignore");
        operatorInfo.setParameters(params);
        MvcResult result = postR("/api/operator",
                JSONUtil.objectToString(operatorInfo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.platform").value("builtin"))
                .andExpect(jsonPath("$.result.name").exists())
                .andReturn();

        return getId(result);
    }

    public String createBashOperator() throws Exception {
        return createBashOperator(null);
    }

    public String createBashOperator(String cmd) throws Exception {
        return createBashOperator(cmd, null);
    }

    public String createBashOperator(String cmd, List<ParameterInfo> parameterList) throws Exception {
        OperatorInfo operatorInfo = new OperatorInfo();
        operatorInfo.setName("test operator");
        operatorInfo.setTags(new String[]{"test"});
        operatorInfo.setPlatform("bash");
        JSONObject platformConfig = new JSONObject();
        if (StringUtils.isNullOrEmpty(cmd)) {
            cmd = "sleep ";
        }
        platformConfig.put("command", cmd);
        operatorInfo.setPlatformConfig(platformConfig);
        operatorInfo.setParameterExecutionType("values");
        if (parameterList == null || parameterList.isEmpty()) {
            parameterList = new ArrayList<>();
            ParameterInfo p = new ParameterInfo();
            p.setParameterKey("a");
            p.setParameterType("choices");
            p.setChoices(new String[]{"10"});
            parameterList.add(p);
        }

        operatorInfo.setParameters(parameterList);
        MvcResult result = postR("/api/operator",
                JSONUtil.objectToString(operatorInfo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.platform").value("bash"))
                .andExpect(jsonPath("$.result.name").exists())
                .andExpect(jsonPath("$.result.parameters").isNotEmpty())
                .andReturn();

        return getId(result);
    }
}
