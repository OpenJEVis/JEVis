/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing.processor;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;


/**
 * @author broder
 */
public class Gap {

    private final List<DateTime> missingDateTimes = new ArrayList<>();
    private Double lastValue;
    private Double firstValue;
    private String startNote;

    public void addDateTime(DateTime missingDateTime) {
        missingDateTimes.add(missingDateTime);
    }

    public List<DateTime> getMissingDateTimes() {
        return missingDateTimes;
    }

    public Double getFirstValue() {
        return firstValue;
    }

    public void setFirstValue(Double firstValue) {
        this.firstValue = firstValue;
    }

    public Double getLastValue() {
        return lastValue;
    }

    public void setLastValue(Double lastValue) {
        this.lastValue = lastValue;
    }

    public String getStartNote() {
        return startNote;
    }

    public void setStartNote(String startNote) {
        this.startNote = startNote;
    }
}
