package org.jevis.jestatus;

import org.jevis.api.JEVisObject;
import org.joda.time.DateTime;

public class DataServerLine {
    private StringBuilder lineString = new StringBuilder();
    private String name;
    private Long id;
    private String nameTarget;
    private String organizationName;
    private String buildingName;
    private JEVisObject targetObject;
    private DateTime lastTimeStamp;

    public DataServerLine() {
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

    public String getNameTarget() {
        return nameTarget;
    }

    public void setNameTarget(String nameTarget) {
        this.nameTarget = nameTarget;
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

    public JEVisObject getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(JEVisObject targetObject) {
        this.targetObject = targetObject;
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