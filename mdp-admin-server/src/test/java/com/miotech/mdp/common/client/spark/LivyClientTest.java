package com.miotech.mdp.common.client.spark;

import com.miotech.mdp.common.models.protobuf.livy.SparkApp;
import com.miotech.mdp.common.models.protobuf.livy.Statement;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class LivyClientTest {

    private LivyClient client;
    private SparkClient sparkClient;


    @BeforeTest
    public void init() {
        client = new LivyClient("http://10.0.1.4:8998");
        sparkClient = new SparkClient("10.0.1.4", 8088);
    }

    @Test
    public void testRunSparkJob() throws InterruptedException {
        SparkJob job = new SparkJob();
        job.setQueue("data");
        job.setFile("s3://com.miotech.data.prd/tmp/spark-examples_2.11-2.3.1.jar");
        job.setName("test spark pi");
        job.setClassName("org.apache.spark.examples.SparkPi");

        SparkApp app = client.runSparkJob(job);

        Integer jobId = app.getId();
        while(!app.getState().equals("success")) {
            app = client.getSparkJob(jobId);
            Thread.sleep(3000);
        }
        String applicationState = sparkClient.getApp(app.getAppId())
                .getApp()
                .getState();
        assert applicationState.equals("FINISHED");

    }

    @Test
    public void testRunSparkStatementsb() throws InterruptedException {

        SparkApp app = client.runSparkStatements(new SparkJob());

        Integer sessionId = app.getId();
        String state = app.getState();
        while(!state.equals("idle")) {
            state = client.getSessionState(sessionId)
                    .getState();
            Thread.sleep(3000);
        }

        StatementRequest stateReq = new StatementRequest();
        stateReq.setCode("println(1)");
        stateReq.setKind("spark");
        Statement statement = client.createSessionStatement(sessionId, stateReq);

        while(statement.getProgress() < 1.0) {
            statement = client.getSessionStatement(sessionId, statement.getId());
            Thread.sleep(3000);
        }

        String output = statement
                .getOutput()
                .getData()
                .getTextPlain().replaceAll("\\n","");
        assert output.equals("1");
        client.deleteSession(sessionId);

    }
}
