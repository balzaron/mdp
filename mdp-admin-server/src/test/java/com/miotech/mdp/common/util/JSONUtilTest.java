package com.miotech.mdp.common.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.Data;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JSONUtilTest {
    @Test
    public void testJsonToArray() {
        Gson gson = new Gson();
        // In Gson, every number will be parsed to Double
        JsonElement inputJson1 = gson.fromJson("[1, 2, 3, 4, 5]", JsonElement.class);
        JsonElement inputJson2 = gson.fromJson("[ \"test\", \"case\", 78 ]", JsonElement.class);

        List<Object> arrOfNumber = JSONUtil.jsonToArray(inputJson1);
        List<Object> arrOfElements = JSONUtil.jsonToArray(inputJson2);

        assertEquals(5, arrOfNumber.size());
        assertEquals(3, arrOfElements.size());
        assertEquals(Double.valueOf(4), arrOfNumber.get(3));
        assertEquals("case", arrOfElements.get(1));
        assertEquals(Double.valueOf(78), arrOfElements.get(2));
    }

    @Data
    class InternalDemo {
        private LocalDateTime t;
        private String s;
    }

    @Test
    public void testDatetimeConvert() {
        InternalDemo demoObj = new InternalDemo();
        demoObj.setS("hello");
        demoObj.setT(LocalDateTime.of(2020, 3, 6, 17, 51, 55, 0));

        String jsonStr = JSONUtil.objectToStringWithTimeToMillisecond(demoObj);
        assertEquals("{\"t\":1583488315000,\"s\":\"hello\"}", jsonStr);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testJsonToArrayWithExceptions() {
        exceptionRule.expect(RuntimeException.class);
        exceptionRule.expectMessage("{\"a\":1,\"b\":\"hello\"} is not a JSON array");

        Gson gson = new Gson();
        JsonElement inputJson = gson.fromJson("{ \"a\": 1, \"b\": \"hello\" }", JsonElement.class);
        JSONUtil.jsonToArray(inputJson);
    }
}
