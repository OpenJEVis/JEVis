package org.jevis.commons.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonTools {
    private final static ObjectMapper prettyObjectMapper = new ObjectMapper();
    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static ObjectMapper prettyObjectMapper() {
        prettyObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return prettyObjectMapper;
    }

    public static ObjectMapper objectMapper() {
        return objectMapper;
    }
}
