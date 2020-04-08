package com.miotech.mdp.table.schema;

import com.google.gson.JsonObject;
import com.miotech.mdp.common.models.protobuf.schema.*;
import com.miotech.mdp.common.util.JSONUtil;
import com.miotech.mdp.table.exception.SchemaResolveException;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.*;


public class KinesisSchemaResolver extends SchemaResolver  {

    AmazonKinesis amazonKinesis;

    public KinesisSchemaResolver(JsonObject details){
        String accessKey = null;
        String secretKey = null;
        if (details.has("accessKey")) {
            accessKey = details.get("accessKey").getAsString();
        }
        if (details.has("secretKey")) {
            secretKey = details.get("secretKey").getAsString();
        }
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        amazonKinesis = AmazonKinesisClientBuilder
                .standard().withRegion(Regions.AP_NORTHEAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }

    public String getRecordFromStream(String stream){
        String shardIterator;

        GetShardIteratorRequest getShardIteratorRequest = new GetShardIteratorRequest();
        getShardIteratorRequest.setStreamName(stream);
        getShardIteratorRequest.setShardId("shardId-000000000000");
        getShardIteratorRequest.setShardIteratorType("TRIM_HORIZON");
        GetShardIteratorResult getShardIteratorResult = amazonKinesis.getShardIterator(getShardIteratorRequest);
        shardIterator = getShardIteratorResult.getShardIterator();
        GetRecordsRequest getRecordsRequest = new GetRecordsRequest();
        getRecordsRequest.setShardIterator(shardIterator);
        getRecordsRequest.setLimit(1);
        GetRecordsResult result = amazonKinesis.getRecords(getRecordsRequest);
        if(result.getRecords().size() == 0)
            return null;

        // Put the result into record list. The result can be empty.
        Record record = result.getRecords().get(0);
        Charset charset = Charset.forName("UTF-8");
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer charBuffer = null;
        try {
            charBuffer = decoder.decode(record.getData().asReadOnlyBuffer());
        } catch (CharacterCodingException e) {
            e.printStackTrace();
        }
        return charBuffer.toString();
    }

    @Override
    public TableSchema describeTable(TableIdentifier table) throws SchemaResolveException {
        String stream = table.getName();
        String response = getRecordFromStream(stream);
        if(response == null)
            return TableSchema.newBuilder().build();

        JsonObject jsonObj = JSONUtil.stringToJson(response);
        return TableSchema.newBuilder()
                .setName(stream)
                .addAllFields(docToFields(jsonObj, null)).build();

    }

    @Override
    public DatabaseSchema describeDatabase(DatabaseIdentifier database) {
        return null;
    }

}
