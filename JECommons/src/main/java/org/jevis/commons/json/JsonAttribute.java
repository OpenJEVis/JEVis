/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEWebService is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.json;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@XmlRootElement(name = "Attribute")
public class JsonAttribute {

    private String name;
    private String firstSample;
    private String lastSample;
    private String period;
    private long samplecount;
    private long object;
    private String unit;
    private String lastvalue;
    private List<JsonSample> samples;

    public JsonAttribute() {
    }

    public String getLastvalue() {
        return lastvalue;
    }

    public void setLastvalue(String lastvalue) {
        this.lastvalue = lastvalue;
    }

    public String getUnit() {
        return "kw";
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstTS() {
        return firstSample;
    }

    public void setFirstTS(String firstSample) {
        this.firstSample = firstSample;
    }

    public String getLastTS() {
        return lastSample;
    }

    public void setLastTS(String lastSample) {
        this.lastSample = lastSample;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public long getSamplecount() {
        return samplecount;
    }

    public void setSamplecount(long samplecount) {
        this.samplecount = samplecount;
    }

    public long getObject() {
        return object;
    }

    public void setObject(long object) {
        this.object = object;
    }

    public List<JsonSample> getSamples() {
        return samples;
    }

    public void setSamples(List<JsonSample> samples) {
        this.samples = samples;
    }

}
