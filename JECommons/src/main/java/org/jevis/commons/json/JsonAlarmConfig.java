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
@XmlRootElement(name = "JsonAlarmConfig")
public class JsonAlarmConfig {

    private String name;
    private String id;
    private String limitData;
    private String limit;
    private String operator;
    private JsonScheduler silentTime;
    private JsonScheduler standbyTime;
    private String tolerance;

    public JsonAlarmConfig() {
    }

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(name = "limitData")
    public String getLimitData() {
        return limitData;
    }

    public void setLimitData(String limitData) {
        this.limitData = limitData;
    }

    @XmlElement(name = "limit")
    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    @XmlElement(name = "operator")
    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @XmlElement(name = "silentTime")
    public JsonScheduler getSilentTime() {
        return silentTime;
    }

    public void setSilentTime(JsonScheduler silentTime) {
        this.silentTime = silentTime;
    }

    @XmlElement(name = "standbyTime")
    public JsonScheduler getStandbyTime() {
        return standbyTime;
    }

    public void setStandbyTime(JsonScheduler standbyTime) {
        this.standbyTime = standbyTime;
    }

    @XmlElement(name = "tolerance")
    public String getTolerance() {
        return tolerance;
    }

    public void setTolerance(String tolerance) {
        this.tolerance = tolerance;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}
