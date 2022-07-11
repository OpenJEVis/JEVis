package org.jevis.jestatus;

import org.joda.time.DateTime;

public class ReportStatusLine {
    private final StringBuilder lineString = new StringBuilder();
    private String name;
    private Long id;
    private String linkStatus;
    private String organizationName;
    private String buildingName;
    private DateTime lastTimeStamp = new DateTime();

    public ReportStatusLine() {
    }

    public StringBuilder getLineString() {
        return lineString;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLinkStatus() {
        return linkStatus;
    }

    public void setLinkStatus(String linkStatus) {
        this.linkStatus = linkStatus;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }


    public DateTime getLastTimeStamp() {
        return lastTimeStamp;
    }

    public void setLastTimeStamp(DateTime lastTimeStamp) {
        this.lastTimeStamp = lastTimeStamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}