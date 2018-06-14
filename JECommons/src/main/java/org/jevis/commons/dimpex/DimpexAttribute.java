package org.jevis.commons.dimpex;

import org.jevis.commons.ws.json.JsonSample;
import org.jevis.commons.ws.json.JsonUnit;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

public class DimpexAttribute {


    private String name = "";
    private JsonUnit inputUnit = new JsonUnit();
    private JsonUnit displayUnit = new JsonUnit();
    private String inputRate = Period.ZERO.toString();
    private String displayRate = Period.ZERO.toString();
    private String sampleID = "";
    private List<JsonSample> samples = new ArrayList<>();


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public JsonUnit getInputUnit() {
        return inputUnit;
    }

    public void setInputUnit(JsonUnit inputUnit) {
        this.inputUnit = inputUnit;
    }

    public JsonUnit getDisplayUnit() {
        return displayUnit;
    }

    public void setDisplayUnit(JsonUnit displayUnit) {
        this.displayUnit = displayUnit;
    }

    public String getInputRate() {
        return inputRate;
    }

    public void setInputRate(String inputRate) {
        this.inputRate = inputRate;
    }

    public String getDisplayRate() {
        return displayRate;
    }

    public void setDisplayRate(String displayRate) {
        this.displayRate = displayRate;
    }

    public String getSampleID() {
        return sampleID;
    }

    public void setSampleID(String sampleID) {
        this.sampleID = sampleID;
    }

    public List<JsonSample> getSamples() {
        return samples;
    }

    public void setSamples(List<JsonSample> samples) {
        this.samples = samples;
    }
}
