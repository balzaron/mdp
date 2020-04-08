package com.miotech.mdp.flow.constant;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.miotech.mdp.common.constant.CommonConstant;

import java.io.IOException;

public class ConstantSerializer extends StdSerializer {

    protected ConstantSerializer() {
        super(CommonConstant.class);
    }

    protected ConstantSerializer(Class t) {
        super(t);
    }

    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(((CommonConstant) o).getName());

    }

}
