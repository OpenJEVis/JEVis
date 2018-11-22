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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonGapFillingConfig that = (JsonGapFillingConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (boundary != null ? !boundary.equals(that.boundary) : that.boundary != null) return false;
        if (defaultvalue != null ? !defaultvalue.equals(that.defaultvalue) : that.defaultvalue != null) return false;
        if (referenceperiod != null ? !referenceperiod.equals(that.referenceperiod) : that.referenceperiod != null)
            return false;
        if (bindtospecific != null ? !bindtospecific.equals(that.bindtospecific) : that.bindtospecific != null)
            return false;
        if (referenceperiodcount != null ? referenceperiodcount.equals(that.referenceperiodcount) : that.referenceperiodcount != null)
            return false;
        return false;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (boundary != null ? boundary.hashCode() : 0);
        result = 31 * result + (defaultvalue != null ? defaultvalue.hashCode() : 0);
        result = 31 * result + (referenceperiod != null ? referenceperiod.hashCode() : 0);
        result = 31 * result + (bindtospecific != null ? bindtospecific.hashCode() : 0);
        result = 31 * result + (referenceperiodcount != null ? referenceperiodcount.hashCode() : 0);
        return result;
    }
}
