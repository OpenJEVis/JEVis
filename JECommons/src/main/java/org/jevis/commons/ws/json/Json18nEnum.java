package org.jevis.commons.ws.json;

import java.util.Map;

public class Json18nEnum {

    private Map<String, String> descriptions;
    private Map<String, String> names;
    private Map<String, Integer> positions;

    public Json18nEnum() {
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


    public Map<String, Integer> getPositions() {
        return positions;
    }

    public void setPositions(Map<String, Integer> positions) {
        this.positions = positions;
    }
}
