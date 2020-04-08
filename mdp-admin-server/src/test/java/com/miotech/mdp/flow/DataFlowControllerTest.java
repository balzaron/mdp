package com.miotech.mdp.flow;

import com.google.gson.JsonObject;
import com.miotech.mdp.common.util.JSONUtil;
import com.miotech.mdp.flow.constant.TaskState;
import com.miotech.mdp.flow.entity.bo.*;
import com.miotech.mdp.quality.models.vo.rule.request.CreateCustomizedRuleVo;
import com.miotech.mdp.quality.models.vo.test.RelatedTableVO;
import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class DataFlowControllerTest extends BaseControllerTest {

    @Test
    public void testCreateFlow() throws Exception {
        String flowId = createFlow();

        FlowSearch search = new FlowSearch();
        search.setName("test");
        search.setTags(new String[]{"test", "flow"});
        postR("/api/flow/list",JSONUtil.objectToString(search))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.result.flows").isNotEmpty());;

        deleteR("/api/flow/" + flowId)
        .andExpect(status().is2xxSuccessful())
        .andReturn();

        getR("/api/flow/" + flowId)
                .andExpect(status().is5xxServerError());

        String cronFlowId = createFlow("scheduling",
                "0/3 * * * * ?", true);

        Thread.sleep(6000);
        getR("/api/flow/" + cronFlowId + "/runs")
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.result.runs").isNotEmpty());
        deleteR("/api/flow/" + cronFlowId)
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }

    @Test
    public void testCreateFlowTask() throws Exception {
        String flowId = createFlow();
        String operatorId = createBashOperator();
        ArgumentInfo arg = new ArgumentInfo();
        arg.setParameterKey("a");
        arg.setParameterValue("a");
        String flowTaskId = createFlowTask(flowId, operatorId, Collections.singletonList(arg));
        MvcResult result = postR(String.format("/api/flow/%s/tasks/%s/execute", flowId, flowTaskId), null)
                .andReturn();

        waitForTaskCompletion(result);
        deleteR("/api/flow/" + flowId)
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }

    @Test
    public void testCopyFlow() throws Exception {
        String flowId = createFlow();

        String operatorId = createBashOperator();
        ArgumentInfo arg = new ArgumentInfo();
        arg.setParameterKey("a");
        arg.setParameterValue("a");
        createFlowTask(flowId, operatorId, Collections.singletonList(arg));

        FlowCopyOption copyOption = new FlowCopyOption();
        copyOption.setFlowId(flowId);
        MvcResult result = postR("/api/flow/copy", JSONUtil.objectToString(copyOption))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.result.id").isNotEmpty())
                .andExpect(jsonPath("$.result.flowTasks").isNotEmpty())
                .andReturn();
        String copiedFlowId1 = getId(result);

        result = postR("/api/flow/copy", JSONUtil.objectToString(copyOption))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.result.id").isNotEmpty())
                .andExpect(jsonPath("$.result.flowTasks").isNotEmpty())
                .andReturn();
        String copiedFlowId2 = getId(result);

        deleteR("/api/flow/" + flowId)
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        deleteR("/api/flow/" + copiedFlowId1)
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        deleteR("/api/flow/" + copiedFlowId2)
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }

    @Test
    public void testRunFlow() throws Exception {
        String flowId = createFlow();
        String operatorId = createBashOperator();
        ArgumentInfo arg = new ArgumentInfo();
        arg.setParameterKey("a");
        arg.setParameterValue("a");
        String flowTaskId1 = createFlowTask(flowId, operatorId, Collections.singletonList(arg));
        String flowTaskId2 = createFlowTask(flowId, operatorId, Collections.singletonList(arg));
        String failedOperatorId = createBashOperator("exit 1");
        String failTaskId = createFlowTask(flowId, failedOperatorId, Collections.singletonList(arg));

        // run with only one
        FlowExecution execution = new FlowExecution();
        execution.setIncludeTasks(new String[]{flowTaskId1});
        postR(String.format("/api/flow/%s/execute", flowId), JSONUtil.objectToString(execution))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        waitForFlow(flowId);

        TaskSearch taskSearch = new TaskSearch();
        taskSearch.setFlowId(flowId);
        postR("/api/tasks/list", JSONUtil.objectToString(taskSearch))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.result.tasks").isNotEmpty())
                .andDo(print());

        // run with failed
        FlowTaskInfo updateFlowTaskInfo = new FlowTaskInfo();
        updateFlowTaskInfo.setName("test task 2");
        updateFlowTaskInfo.setFlowId(flowId);
        updateFlowTaskInfo.setOperatorId(operatorId);
        updateFlowTaskInfo.setParentTaskIds(new String[]{failTaskId});
        putR(String.format("/api/flow/%s/tasks/%s", flowId, flowTaskId2), JSONUtil.objectToString(updateFlowTaskInfo))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        postR(String.format("/api/flow/%s/execute", flowId), JSONUtil.objectToString(new FlowExecution()))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        waitForFlow(flowId, "FAILED");

        taskSearch.setFlowTaskId(failTaskId);
        postR("/api/tasks/list", JSONUtil.objectToString(taskSearch))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.result.tasks").isNotEmpty())
                .andDo(print());

        // run with one success
        updateFlowTaskInfo.setParentTaskIds(new String[]{flowTaskId1, failTaskId});
        updateFlowTaskInfo.setTriggerRule("one_success");
        putR(String.format("/api/flow/%s/tasks/%s", flowId, flowTaskId2), JSONUtil.objectToString(updateFlowTaskInfo))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        postR(String.format("/api/flow/%s/execute", flowId), JSONUtil.objectToString(new FlowExecution()))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        waitForFlow(flowId, "FAILED");

        taskSearch.setFlowTaskId(flowTaskId2);
        postR("/api/tasks/list", JSONUtil.objectToString(taskSearch))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.result.tasks").isNotEmpty())
                .andDo(print());

        deleteR("/api/flow/" + flowId)
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }

    @Test
    public void testCreateSparkFlowTask() throws Exception {
        String flowId = createFlow();
        String operatorId = createSparkOperator();

        ArgumentInfo arg1 = new ArgumentInfo();
        arg1.setParameterKey("iteration");
        arg1.setParameterValue("100");

        ArgumentInfo arg2 = new ArgumentInfo();
        arg2.setParameterKey("code");
        arg2.setParameterValue("println(112345678)");
        createFlowTask(flowId, operatorId, Arrays.asList(arg1, arg2));

        FlowExecution execution = new FlowExecution();
        postR(String.format("/api/flow/%s/execute", flowId), JSONUtil.objectToString(execution))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        waitForFlow(flowId);
        deleteR("/api/flow/" + flowId)
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }

    @Test
    public void testCreateDockerFlowTask() throws Exception {
        String flowId = createFlow();
        String operatorId = createDockerOperator();

        ArgumentInfo arg = new ArgumentInfo();
        arg.setParameterKey("inputs");
        arg.setParameterValue("s3://com.miotech.data.prd/test/athena/test_csv/");

        createFlowTask(flowId, operatorId, Collections.singletonList(arg));

        FlowExecution execution = new FlowExecution();
        postR(String.format("/api/flow/%s/execute", flowId), JSONUtil.objectToString(execution))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        waitForFlow(flowId);
        deleteR("/api/flow/" + flowId)
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }

    @Test
    public void testCreateBuiltinFlowTask() throws Exception {
        String flowId = createFlow();

        ParameterInfo p = new ParameterInfo();
        p.setParameterKey("host");
        p.setParameterType("string");
        ParameterInfo p2 = new ParameterInfo();
        p2.setParameterKey("name");
        p2.setParameterType("string");
        ParameterInfo p3 = new ParameterInfo();
        p3.setParameterKey("states");
        p3.setParameterType("multiple-string");
        List<ParameterInfo> params = Arrays.asList(p, p2, p3);
        String operatorId = createBuiltinOperator("com.miotech.mdp.flow.operators.SparkJobAvailabilityOperator", params);
        ArgumentInfo arg1 = new ArgumentInfo();
        arg1.setParameterKey("host");
        arg1.setParameterValue("3.112.50.207");
        ArgumentInfo arg2 = new ArgumentInfo();
        arg2.setParameterKey("name");
        arg2.setParameterValue("Event Processing App");
        ArgumentInfo arg3 = new ArgumentInfo();
        arg3.setParameterKey("states");
        arg3.setParameterValue("[\"running\"]");

        List<ArgumentInfo> args = Arrays.asList(arg1, arg2, arg3);
        createFlowTask(flowId, operatorId, args);

        FlowExecution execution = new FlowExecution();
        postR(String.format("/api/flow/%s/execute", flowId), JSONUtil.objectToString(execution))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        waitForFlow(flowId);
        deleteR("/api/flow/" + flowId)
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }

    @Test
    public void testCreateTestFlowTask() throws Exception {
        String flowId = createFlow();
        String flowTaskId = createTestTask(flowId, "SELECT COUNT(1) = 1 as validate_cnt from  dm.cn_company_data");

        MvcResult result = postR(String.format("/api/flow/%s/tasks/%s/execute",
                flowId, flowTaskId),
                null)
                .andReturn();

        waitForTaskCompletion(result);
        deleteR("/api/flow/" + flowId)
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }

    private void waitForFlow(String flowId) throws Exception {
        waitForFlow(flowId, "FINISHED");
    }

    private void waitForFlow(String flowId, String state) throws Exception {
        getR(String.format("/api/flow/%s/state", flowId))
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andExpect(jsonPath("$.result.latestRun").isNotEmpty())
                .andExpect(jsonPath("$.result.runStats").isNotEmpty())
                .andExpect(jsonPath("$.result.taskStats").isNotEmpty())
                .andReturn();

        String flowState = "";
        while (!flowState.equals("FINISHED")
                && !flowState.equals("FAILED")) {
            MvcResult tasksResult = getR(String.format("/api/flow/%s/runs", flowId))
                    .andExpect(status().is2xxSuccessful())
                    .andReturn();
            JsonObject tasks = JSONUtil.stringToJson(tasksResult.getResponse().getContentAsString());
            JsonObject task = tasks.get("result")
                    .getAsJsonObject()
                    .get("runs")
                    .getAsJsonArray()
                    .get(0)
                    .getAsJsonObject();
            flowState = task.get("state").getAsString();
        }
        getR(String.format("/api/flow/%s/state", flowId))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.result.latestRun.state").value(state))
                .andExpect(jsonPath("$.result.runStats").isNotEmpty())
                .andDo(print())
                .andReturn();
    }

    private void waitForTaskCompletion(MvcResult result) throws Exception {
        JsonObject taskJson = JSONUtil.stringToJson(result.getResponse().getContentAsString())
                .get("result")
                .getAsJsonObject();
        String taskId = taskJson.get("id").getAsString();

        String taskState = taskJson.get("state").getAsString();
        while (!TaskState.END_STATES
                .contains(TaskState.fromName(taskState))
        ) {
            MvcResult tasksResult = getR(String.format("/api/task/%s", taskId))
                    .andExpect(status().is2xxSuccessful())
                    .andReturn();
            JsonObject tasks = JSONUtil.stringToJson(tasksResult.getResponse().getContentAsString());
            JsonObject task = tasks.get("result")
                    .getAsJsonObject();
            taskState = task.get("state").getAsString();
            Thread.sleep(3000);
        }

        assert taskState.equals("SUCCEEDED") || taskState.equals("FINISHED");
    }

    private String createTestTask(String flowId, String testSql) throws Exception {
        RelatedTableVO relatedTableVO = new RelatedTableVO();
        relatedTableVO.setId("4835318377218048");
        CreateCustomizedRuleVo testCaseVo = new CreateCustomizedRuleVo();
        testCaseVo.setCaseSql(testSql);
        testCaseVo.setDbId(8);
        testCaseVo.setName("test case sql").setRelatedTables(Arrays.asList(relatedTableVO))
        .setValidateObject(Arrays.asList()).setOwnerId("123490");

        MvcResult result = postR(String.format("/api/rule/create-customized-rule", flowId), JSONUtil.objectToString(testCaseVo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").exists())
                .andReturn();

        String testId = getId(result);
        FlowTaskInfo flowTaskInfo = new FlowTaskInfo();
        flowTaskInfo.setFlowId(flowId);
        flowTaskInfo.setTestId(testId);
        result = postR(String.format("/api/flow/%s/tasks", flowId), JSONUtil.objectToString(flowTaskInfo))
                .andExpect(status().isOk())
                .andReturn();

        return getId(result);
    }

    private String createFlowTask(String flowId, String operatorId, List<ArgumentInfo> args) throws Exception {
        FlowTaskInfo flowTaskInfo = new FlowTaskInfo();
        flowTaskInfo.setFlowId(flowId);
        flowTaskInfo.setOperatorId(operatorId);

        flowTaskInfo.setName("test flow task");
        flowTaskInfo.setTriggerRule("all_success");
        if (args != null && !args.isEmpty()) {
            flowTaskInfo.setArguments(args);
        }
        return createFlowTask(flowId, flowTaskInfo);
    }

    private String createFlowTask(String flowId, FlowTaskInfo flowTaskInfo) throws Exception {
        MvcResult result = postR(String.format("/api/flow/%s/tasks", flowId), JSONUtil.objectToString(flowTaskInfo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").exists())
                .andReturn();

        return getId(result);
    }
    private String createFlow() throws Exception {
        return createFlow("test flow", null, null);
    }

    private String createFlow(String name, String cronTab, Boolean enableScheduling) throws Exception {
        FlowInfo flowInfo = new FlowInfo();
        flowInfo.setTags(new String[]{"test", "flow"});
        flowInfo.setExecutionScheduler(cronTab);
        flowInfo.setEnableScheduler(enableScheduling);
        flowInfo.setName(name);
        MvcResult result = postR("/api/flow", JSONUtil.objectToString(flowInfo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.tags").isNotEmpty())
                .andExpect(jsonPath("$.result.name").exists()).andDo(print())
                .andReturn();

        return getId(result);
    }
}