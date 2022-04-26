package org.jevis.jestatus;

import com.fasterxml.jackson.databind.JsonNode;

public class SimCardInfos {
    private JsonNode simUsage;
    private JsonNode simDetails;

    public SimCardInfos(JsonNode simUsage, JsonNode simDetails) {
        this.simUsage = simUsage;
        this.simDetails = simDetails;
    }

    public JsonNode getSimUsage() {
        return simUsage;
    }

    public void setSimUsage(JsonNode simUsage) {
        this.simUsage = simUsage;
    }

    public JsonNode getSimDetails() {
        return simDetails;
    }

    public void setSimDetails(JsonNode simDetails) {
        this.simDetails = simDetails;
    }
}
