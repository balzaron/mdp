package com.miotech.mdp.flow.service.impl;

import com.miotech.mdp.flow.util.CommandUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class BashExecutorTest {

    @Test
    void executeTask() throws IOException {
//        CommandUtil.executeRuntimeCommand("sh /Users/jentle/PycharmProjects/mdp-backend/test.sh jentle hello ", null);

        CommandUtil.executeRuntimeCommand(" curl -X POST http://zhongda.miotech.com:8000/alertservice/send-wechat -H 'Content-Type: application/json' -d '{ \\\"token\\\": \"ZXGfFpQs7ubLBPJYCZVHn\", \"group\":\"\", \"user_list\": \"jentle\", \"body\": \"hell world\" }'", null);
    }
}