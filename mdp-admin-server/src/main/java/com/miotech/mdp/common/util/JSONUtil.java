package com.miotech.mdp.common.util;

import com.google.gson.*;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JSONUtil {

    public static JsonObject stringToJson(String s) {
        Gson gson = new Gson();

        return gson.fromJson(s, JsonObject.class);
    }

    public static <T> List<T> jsonToArray(JsonElement json) {
        if (!json.isJsonArray()) {
            throw new RuntimeException(json.toString() + " is not a JSON array");
        }
        return (new Gson()).fromJson(json.getAsJsonArray(), ArrayList.class);
    }

    public static String objectToString(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    public static String objectToStringWithTimeToMillisecond(Object obj) {
        GsonBuilder builder = new GsonBuilder();
        // Register an adapter to manage the date types as long values
        builder.registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public JsonElement serialize(LocalDateTime localDateTime, Type typeOfSrc, JsonSerializationContext context) {
                long milliSec = DateUtil.dateTimeToMillis(localDateTime);
                return new JsonPrimitive(milliSec);
            }
        });
        Gson gson = builder.create();
        return gson.toJson(obj);
    }

    public static JsonParser getJsonParser() {
        return new JsonParser();
    }

    public static JsonFormat.Parser getParser() {
        return JsonFormat.parser().ignoringUnknownFields();
    }

    public static Message toMessage(String s, Message.Builder builder) throws InvalidProtocolBufferException {
         getParser().merge(s, builder);
         return builder.build();
    }

    public static String messageToString(MessageOrBuilder message) {
        try {
            return JsonFormat.printer()
                    .preservingProtoFieldNames()
                    .omittingInsignificantWhitespace()
                    .print(message);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("");
        }
    }

    public static JsonElement messageToJson(MessageOrBuilder message) throws InvalidProtocolBufferException {
        return getJsonParser().parse(messageToString(message));
    }
}
