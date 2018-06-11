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
 * @author Florian Simon <gerrit.schutz@envidatec.com>
 */
@XmlRootElement(name = "GapFillingConfig")
public class JsonGapFillingConfig {

    private String name;
    private String type;
    private String boundary;
    private String defaultvalue;
    private String referenceperiod;
    private String bindtospecific;
    private String referenceperiodcount;

    public JsonGapFillingConfig() {
    }

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlElement(name = "boundary")
    public String getBoundary() {
        return boundary;
    }

    public void setBoundary(String boundary) {
        this.boundary = boundary;
    }

    @XmlElement(name = "defaultvalue")
    public String getDefaultvalue() {
        return defaultvalue;
    }

    public void setDefaultvalue(String defaultvalue) {
        this.defaultvalue = defaultvalue;
    }

    @XmlElement(name = "referenceperiod")
    public String getReferenceperiod() {
        return referenceperiod;
    }

    public void setReferenceperiod(String referenceperiod) {
        this.referenceperiod = referenceperiod;
    }

    @XmlElement(name = "bindtospecific")
    public String getBindtospecific() {
        return bindtospecific;
    }

    public void setBindtospecific(String bindtospecific) {
        this.bindtospecific = bindtospecific;
    }

    @XmlElement(name = "referenceperiodcount")
    public String getReferenceperiodcount() {
        return referenceperiodcount;
    }

    public void setReferenceperiodcount(String referenceperiodcount) {
        this.referenceperiodcount = referenceperiodcount;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(this);
        return prettyJson;
    }
}
