/**
 * Copyright (C) 2013 - 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEWebService.
 *
 * JEWebService is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEWebService is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEWebService. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEWebService is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.ws.json;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This Class is used to represents an JEVisAttribue in JSON by the WebService
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
@XmlRootElement(name = "Attribute")
public class JsonAttribute {

    private String type;
    private String begins;
    private String ends;
    private JsonUnit inputUnit;
    private JsonUnit displayUnit;
    private long sampleCount;
    private JsonSample latestValue;
    private String inputSampleRate;
    private String displaySampleRate;
    private int primitiveType;

    public JsonAttribute() {
    }

    @XmlElement(name = "primitiveType")
    public int getPrimitiveType() {
        return primitiveType;
    }

    public void setPrimitiveType(int primitiveType) {
        this.primitiveType = primitiveType;
    }
    
    @XmlElement(name = "inputSampleRate")
    public String getInputSampleRate() {
        return inputSampleRate;
    }

    public void setInputSampleRate(String sampleRate) {
        this.inputSampleRate = sampleRate;
    }

    @XmlElement(name = "displaySampleRate")
    public String getDisplaySampleRate() {
        return displaySampleRate;
    }

    public void setDisplaySampleRate(String sampleRate) {
        this.displaySampleRate = sampleRate;
    }

    @XmlElement(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlElement(name = "begins")
    public String getBegins() {
        return begins;
    }

    public void setBegins(String begins) {
        this.begins = begins;
    }

    @XmlElement(name = "ends")
    public String getEnds() {
        return ends;
    }

    public void setEnds(String ends) {
        this.ends = ends;
    }

    @XmlElement(name = "inputUnit")
    public JsonUnit getInputUnit() {
        return inputUnit;
    }

    public void setInputUnit(JsonUnit unit) {
        this.inputUnit = unit;
    }

    @XmlElement(name = "displayUnit")
    public JsonUnit getDisplayUnit() {
        return displayUnit;
    }

    public void setDisplayUnit(JsonUnit displayUnit) {
        this.displayUnit = displayUnit;
    }

    @XmlElement(name = "sampleCount")
    public long getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(long sampleCount) {
        this.sampleCount = sampleCount;
    }

    @XmlElement(name = "latestValue")
    public JsonSample getLatestValue() {
        return latestValue;
    }

    public void setLatestValue(JsonSample latestValue) {
        this.latestValue = latestValue;
    }

}
