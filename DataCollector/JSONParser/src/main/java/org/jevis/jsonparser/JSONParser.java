package org.jevis.jsonparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JSONParser {
    private static final Logger logger = LogManager.getLogger(JSONParser.class);
    List<JsonNode> results = new ArrayList<>();

    private Optional<JsonNode> rootObject;

    public JSONParser(InputStream inputStream) {
        rootObject = getJSONObject(inputStream);

    }

    public void setInputStream(InputStream inputStream) {
        rootObject = getJSONObject(inputStream);
    }

    private Optional<JsonNode> getJSONObject(InputStream inputStream) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return Optional.of(objectMapper.readTree(inputStream));
        } catch (Exception e) {
            logger.error(e);
        }
        return Optional.empty();

    }

    public List<JsonNode> parse(String path) {
        results.clear();
        try {
            if (!rootObject.isPresent()) return new ArrayList<>();
            List<String> pathList = Arrays.asList(path.split("\\."));
            parseRecursive(rootObject.get(), 0, pathList);
        } catch (Exception e) {
            logger.error("Could not parse path string ", e);
        }


        logger.debug("Parse Result: {}", results);
        return results;
    }

    private void parseRecursive(JsonNode jsonNode, int index, List<String> pathList) {
        if (index + 1 > pathList.size()) {
            results.add(jsonNode);
        } else {
            if (jsonNode.isArray()) {
                jsonNode.forEach(jsonNode1 -> parseRecursive(jsonNode1, index, pathList));
            } else {
                if (jsonNode.get(pathList.get(index)) != null) {
                    parseRecursive(jsonNode.get(pathList.get(index)), index + 1, pathList);
                }
            }
        }

    }
}
