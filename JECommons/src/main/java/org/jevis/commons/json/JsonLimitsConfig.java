/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JECommons.
 * <p>
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEWebService is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */
@XmlRootElement(name = "GapFillingConfig")
public class JsonLimitsConfig {

    private String name;
    private String min;
    private String max;
    private String durationOverUnderRun;
    private String typeOfSubstituteValue;
    private String defaultValue;
    private String referencePeriod;
    private String bindToSpecific;
    private String referencePeriodCount;

    public JsonLimitsConfig() {
    }

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "min")
    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    @XmlElement(name = "max")
    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    @XmlElement(name = "durationoverunderrun")
    public String getDurationOverUnderRun() {
        return durationOverUnderRun;
    }

    public void setDurationOverUnderRun(String durationOverUnderRun) {
        this.durationOverUnderRun = durationOverUnderRun;
    }

    @XmlElement(name = "typeofsubstitutevalue")
    public String getTypeOfSubstituteValue() {
        return typeOfSubstituteValue;
    }

    public void setTypeOfSubstituteValue(String typeOfSubstituteValue) {
        this.typeOfSubstituteValue = typeOfSubstituteValue;
    }

    @XmlElement(name = "defaultvalue")
    public String getDefaultvalue() {
        return defaultValue;
    }

    public void setDefaultvalue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @XmlElement(name = "referenceperiod")
    public String getReferenceperiod() {
        return referencePeriod;
    }

    public void setReferenceperiod(String referencePeriod) {
        this.referencePeriod = referencePeriod;
    }

    @XmlElement(name = "bindtospecific")
    public String getBindtospecific() {
        return bindToSpecific;
    }

    public void setBindtospecific(String bindToSpecific) {
        this.bindToSpecific = bindToSpecific;
    }

    @XmlElement(name = "referenceperiodcount")
    public String getReferenceperiodcount() {
        return referencePeriodCount;
    }

    public void setReferenceperiodcount(String referencePeriodCount) {
        this.referencePeriodCount = referencePeriodCount;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(this);
        return prettyJson;
    }
}
