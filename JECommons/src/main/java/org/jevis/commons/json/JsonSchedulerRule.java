//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.jevis.commons.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "JsonSchedulerRule")
public class JsonSchedulerRule {
    private String id;
    private String months;
    private String dayOfMonth;
    private String dayOfWeek;
    private String startTimeHours;
    private String startTimeMinutes;
    private String endTimeHours;
    private String endTimeMinutes;

    @XmlElement(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(name = "months")
    public String getMonths() {
        return this.months;
    }

    public void setMonths(String months) {
        this.months = months;
    }

    @XmlElement(name = "dayOfMonth")
    public String getDayOfMonth() {
        return this.dayOfMonth;
    }

    public void setDayOfMonth(String dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    @XmlElement(name = "dayOfWeek")
    public String getDayOfWeek() {
        return this.dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    @XmlElement(name = "startTimeHours")
    public String getStartTimeHours() {
        return this.startTimeHours;
    }

    public void setStartTimeHours(String startTimeHours) {
        this.startTimeHours = startTimeHours;
    }

    @XmlElement(name = "startTimeMinutes")
    public String getStartTimeMinutes() {
        return this.startTimeMinutes;
    }

    public void setStartTimeMinutes(String startTimeMinutes) {
        this.startTimeMinutes = startTimeMinutes;
    }

    @XmlElement(name = "endTimeHours")
    public String getEndTimeHours() {
        return this.endTimeHours;
    }

    public void setEndTimeHours(String endTimeHours) {
        this.endTimeHours = endTimeHours;
    }

    @XmlElement(name = "endTimeMinutes")
    public String getEndTimeMinutes() {
        return this.endTimeMinutes;
    }

    public void setEndTimeMinutes(String endTimeMinutes) {
        this.endTimeMinutes = endTimeMinutes;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}
