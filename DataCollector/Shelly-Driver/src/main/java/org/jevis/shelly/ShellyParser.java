package org.jevis.shelly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ShellyParser {

    public ShellyParser(String json, Configuration config) throws JsonProcessingException {

        System.out.println();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(json);

        JsonNode test = actualObj.findValue("total_act");

        System.out.println("Test: " + test);
        if (test.isDouble()) {
            System.out.println("2: " + test.doubleValue());
        }

        JsonNode test2 = actualObj.findParent("total_act");
        System.out.println("test2: " + test2);

        JsonNode result = actualObj.findPath("total_act");
        System.out.println(result.get("total_act"));

    }


}
