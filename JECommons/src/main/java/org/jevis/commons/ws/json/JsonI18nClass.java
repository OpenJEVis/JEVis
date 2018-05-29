package org.jevis.commons.ws.json;

import java.util.List;
import java.util.Map;

public class JsonI18nClass {


    private String jevisclass;
    private Map<String, String> names;
    private Map<String, String> descriptions;
    private List<JsonI18nType> types;

    public JsonI18nClass() {
    }


    public String getJevisclass() {
        return jevisclass;
    }

    public void setJevisclass(String jevisclass) {
        this.jevisclass = jevisclass;
    }

    public Map<String, String> getNames() {
        return names;
    }

    public void setNames(Map<String, String> names) {
        this.names = names;
    }

    public Map<String, String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(Map<String, String> descriptions) {
        this.descriptions = descriptions;
    }

    public List<JsonI18nType> getTypes() {
        return types;
    }

    public void setTypes(List<JsonI18nType> types) {
        this.types = types;
    }
}
