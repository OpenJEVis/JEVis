package org.jevis.commons.ws.json;

import java.util.Map;

public class JsonI18nType {

    private String type;
    private Map<String, String> descriptions;
    private Map<String, String> names;
    private Map<String, Json18nEnum> enums;

    public JsonI18nType() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(Map<String, String> descriptions) {
        this.descriptions = descriptions;
    }

    public Map<String, String> getNames() {
        return names;
    }

    public void setNames(Map<String, String> names) {
        this.names = names;
    }

    public Map<String, Json18nEnum> getEnums() {
        return enums;
    }

    public void setEnums(Map<String, Json18nEnum> enums) {
        this.enums = enums;
    }
}
