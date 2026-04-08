package org.jevis.commons.datasource;

import org.jevis.api.JEVisFile;
import org.jevis.commons.driver.dwd.Attribute;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Station {

    private long id = -1;
    private String name = "";
    private DateTime from;
    private DateTime to;
    private long height = 0;
    private double geoWidth;
    private double geoHeight;
    private String state = "";
    private Map<Attribute, List<String>> intervalPath = new HashMap<>();
    private Map<Attribute, StationData> stationData = new HashMap<>();
    private JEVisFile descriptionFile;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateTime getFrom() {
        return from;
    }

    public void setFrom(DateTime from) {
        this.from = from;
    }

    public DateTime getTo() {
        return to;
    }

    public void setTo(DateTime to) {
        this.to = to;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public double getGeoWidth() {
        return geoWidth;
    }

    public void setGeoWidth(double geoWidth) {
        this.geoWidth = geoWidth;
    }

    public double getGeoHeight() {
        return geoHeight;
    }

    public void setGeoHeight(double geoHeight) {
        this.geoHeight = geoHeight;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Map<Attribute, List<String>> getIntervalPath() {
        return intervalPath;
    }

    public void setIntervalPath(Map<Attribute, List<String>> intervalPath) {
        this.intervalPath = intervalPath;
    }

    public Map<Attribute, StationData> getStationData() {
        return stationData;
    }

    public void setStationData(Map<Attribute, StationData> stationData) {
        this.stationData = stationData;
    }

    public JEVisFile getDescriptionFile() {
        return descriptionFile;
    }

    public void setDescriptionFile(JEVisFile descriptionFile) {
        this.descriptionFile = descriptionFile;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Station) {
            Station otherStation = (Station) obj;
            return getId() == otherStation.getId();
        }
        return false;
    }
}
