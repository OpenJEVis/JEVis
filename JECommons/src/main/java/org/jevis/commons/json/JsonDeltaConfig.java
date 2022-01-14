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

import com.fasterxml.jackson.core.JsonProcessingException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */
@XmlRootElement(name = "JsonLimitsConfig")
public class JsonDeltaConfig {

    private String name;
    private String min;
    private String max;
    private JsonGapFillingConfig maxConfig;

    public JsonDeltaConfig() {
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

    @XmlElement(name = "maxConfig")
    public JsonGapFillingConfig getMaxConfig() {
        return maxConfig;
    }

    public void setMaxConfig(JsonGapFillingConfig maxConfig) {
        this.maxConfig = maxConfig;
    }

    @Override
    public String toString() {
        try {
            return JsonTools.prettyObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
