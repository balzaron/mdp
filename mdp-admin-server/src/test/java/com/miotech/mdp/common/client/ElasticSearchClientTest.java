package com.miotech.mdp.common.client;

import com.google.gson.JsonObject;
import com.miotech.mdp.common.exception.ElasticSearchClientException;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Objects;
import java.util.Optional;

public class ElasticSearchClientTest {
    private ElasticSearchClient client;

    private final String TEST_INDEX = "mio-statistic";

    @BeforeTest
    public void init() {
        client = new ElasticSearchClient(
                "127.0.0.1",
                9200
        );
    }

    @Test
    public void documentCRUDShouldWork() throws ElasticSearchClientException {
        JsonObject respObj1 = client.createDocument("{ \"name\": \"test\", \"value\": 1 }", TEST_INDEX);

        JsonObject postJsonObject = new JsonObject();
        postJsonObject.addProperty("name", "test2");
        postJsonObject.addProperty("value", 2);
        JsonObject respObj2 = client.createDocument(postJsonObject, TEST_INDEX);

        String id1 = respObj1.get("_id").getAsString();
        String id2 = respObj2.get("_id").getAsString();

        Optional<JsonObject> maybeObj1 = client.findById(id1, TEST_INDEX);
        Optional<JsonObject> maybeObj2 = client.findById(id2, TEST_INDEX);

        assert maybeObj1.isPresent();
        assert maybeObj2.isPresent();
        assert Objects.equals(maybeObj1.get()
                .get("_source").getAsJsonObject()
                .get("name").getAsString(), "test");
        assert Objects.equals(maybeObj2.get()
                .get("_source").getAsJsonObject()
                .get("name").getAsString(), "test2");

        client.updateDocument(id1, "{ \"name\": \"test1\", \"value\": 3 }", TEST_INDEX);
        Optional<JsonObject> maybeUpdatedObj1 = client.findById(id1, TEST_INDEX);
        assert maybeUpdatedObj1.isPresent();
        assert Objects.equals(maybeUpdatedObj1.get()
                .get("_source").getAsJsonObject()
                .get("name").getAsString(), "test1");

        // should return empty Optional object if not found
        Optional<JsonObject> maybeNullObj = client.findById("THIS_ID_SHOULD_NOT_EXIST", TEST_INDEX);
        assert !maybeNullObj.isPresent();
    }

    @Test(dependsOnMethods = "documentCRUDShouldWork", expectedExceptions = ElasticSearchClientException.class)
    public void shouldThrowExceptionWhenCreatingInvalidObject() {
        client.createDocument("{ this is invalid }", TEST_INDEX, "randomType");
    }

    @AfterTest
    public void clear() {
    }
}
