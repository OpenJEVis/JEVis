/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecc.map;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * @author broder
 */
public class GPSSample {

    private final StringProperty date;
    private final DoubleProperty longitude;
    private final DoubleProperty latitude;

    public GPSSample(Double latValue, Double longValue, DateTime timestamp) {
        date = new SimpleStringProperty(timestamp.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
        longitude = new SimpleDoubleProperty(longValue);
        latitude = new SimpleDoubleProperty(latValue);
    }

    public String getDate() {
        return date.get();
    }

    public Double getLongitude() {
        return longitude.get();
    }

    public Double getLatitude() {
        return latitude.get();
    }

}
