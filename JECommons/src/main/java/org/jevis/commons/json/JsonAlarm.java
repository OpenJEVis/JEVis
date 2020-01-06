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
import org.jevis.commons.alarm.AlarmType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */
@XmlRootElement(name = "JsonAlarm")
public class JsonAlarm {

    private String attribute;
    private String timeStamp;
    private Double isValue;
    private String operator;
    private Double shouldBeValue;
    private Long object;
    private AlarmType alarmType;
    private Integer logValue;
    private Double tolerance;

    public JsonAlarm() {
    }

    @XmlElement(name = "attribute")
    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    @XmlElement(name = "timeStamp")
    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    @XmlElement(name = "isValue")
    public Double getIsValue() {
        return isValue;
    }

    public void setIsValue(Double isValue) {
        this.isValue = isValue;
    }

    @XmlElement(name = "operator")
    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @XmlElement(name = "shouldBeValue")
    public Double getShouldBeValue() {
        return shouldBeValue;
    }

    public void setShouldBeValue(Double shouldBeValue) {
        this.shouldBeValue = shouldBeValue;
    }

    @XmlElement(name = "object")
    public Long getObject() {
        return object;
    }

    public void setObject(Long object) {
        this.object = object;
    }

    @XmlElement(name = "alarmType")
    public AlarmType getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(AlarmType alarmType) {
        this.alarmType = alarmType;
    }

    @XmlElement(name = "logValue")
    public Integer getLogValue() {
        return logValue;
    }

    public void setLogValue(Integer logValue) {
        this.logValue = logValue;
    }

    @XmlElement(name = "tolerance")
    public Double getTolerance() {
        return tolerance;
    }

    public void setTolerance(Double tolerance) {
        this.tolerance = tolerance;
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
