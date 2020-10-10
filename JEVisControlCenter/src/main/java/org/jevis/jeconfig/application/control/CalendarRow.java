package org.jevis.jeconfig.application.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CalendarRow {
    private String countryCode;
    private String countryName;
    private String stateCode;
    private String stateName;

    public CalendarRow(String countryCode, String countryName, String stateCode, String stateName) {
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.stateCode = stateCode;
        this.stateName = stateName;
    }

    public CalendarRow(String countryCode, String countryName) {
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.stateCode = "";
        this.stateName = "";
    }

    public CalendarRow(String value) {
        List<String> tempList = new ArrayList<>(Arrays.asList(value.split(",")));
        if (tempList.size() == 2) {
            this.countryCode = tempList.get(0);
            this.stateCode = tempList.get(1);
        }
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getStateCode() {
        return stateCode;
    }

    public String getStateName() {
        return stateName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CalendarRow) {
            CalendarRow otherObject = (CalendarRow) obj;
            if (this.getCountryCode().equals(otherObject.getCountryCode()) && this.getStateCode() == null && otherObject.getStateCode() == null) {
                return true;
            } else {
                return (this.getCountryCode().equals(otherObject.getCountryCode()) && this.getStateCode().equals(otherObject.getStateCode()));
            }
        } else return false;
    }
}
