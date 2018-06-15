/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.ws.sql.SQLDataSource;

import java.io.File;
import java.util.List;

import static org.jevis.iso.add.Snippets.getValueString;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class MeasuringPoint {

    public static final String AttMeter = "Meter";
    public static final String AttMonitoringID = "Monitoring ID";
    public static final String AttStation = "Station";

    private long ID;
    private String name;

    private String Comment;
    private String DataPointAssignment;
    private String InstallationLocation;
    private Long Meter;
    private Long MonitoringID;
    private String MeasuringPointName;
    private File Photo;
    private String PhysicalProperty;
    private Long Station;
    private String Unit;

    MeasuringPoint(SQLDataSource ds, JsonObject input) throws Exception {
        ID = 0L;
        name = "";
        Comment = "";
        InstallationLocation = "";
        DataPointAssignment = "";
        Meter = 0L;
        MonitoringID = 0L;
        MeasuringPointName = "";
        PhysicalProperty = "";
        Station = 0L;
        Unit = "";
        this.ID = input.getId();
        this.name = input.getName();

        List<JsonAttribute> listMeasuringPointAttributes = ds.getAttributes(input.getId());

        for (JsonAttribute att : listMeasuringPointAttributes) {
            String name = att.getType();

            final String attUnit = "Unit";
            final String attPhysicalProperty = "Physical Property";
            final String attPhoto = "Photo";
            final String attMeasuringPointName = "Name";
            final String attInstallationLocation = "Installation Location";
            final String attDataPointAssignment = "Data Point Assignment";
            final String attComment = "Comment";
            switch (name) {
                case attComment:
                    this.setComment(getValueString(att, ""));
                    break;
                case attDataPointAssignment:
                    this.setDataPointAssignment(getValueString(att, ""));
                    break;
                case attInstallationLocation:
                    this.setInstallationLocation(getValueString(att, ""));
                    break;
                case attMeasuringPointName:
                    this.setMeasuringPointName(getValueString(att, ""));
                    break;
                case AttMeter:
                    if (getValueString(att, "") != "") {
                        this.setMeter(Long.parseLong(getValueString(att, "")));
                    } else this.setMeter(0L);
                    break;
                case AttMonitoringID:
                    if (getValueString(att, "") != "") {
                        this.setMonitoringID(Long.parseLong(getValueString(att, "")));
                    } else this.setMonitoringID(0L);
                    break;
                case attPhoto:
                    //this.setPhoto(Organisation.getValueFile(att, ""));
                    break;
                case attPhysicalProperty:
                    this.setPhysicalProperty(getValueString(att, ""));
                    break;
                case AttStation:
                    if (getValueString(att, "") != "") {
                        this.setStation(Long.parseLong(getValueString(att, "")));
                    } else this.setStation(0L);
                    break;
                case attUnit:
                    this.setUnit(getValueString(att, ""));
                    break;
                default:
                    break;
            }
        }


    }

    public MeasuringPoint() {
        ID = 0L;
        name = "";
        Comment = "";
        InstallationLocation = "";
        DataPointAssignment = "";
        Meter = 0L;
        MonitoringID = 0L;
        MeasuringPointName = "";
        PhysicalProperty = "";
        Station = 0L;
        Unit = "";
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String Comment) {
        this.Comment = Comment;
    }

    public String getDataPointAssignment() {
        return DataPointAssignment;
    }

    public void setDataPointAssignment(String DataPointAssignment) {
        this.DataPointAssignment = DataPointAssignment;
    }

    public String getInstallationLocation() {
        return InstallationLocation;
    }

    public void setInstallationLocation(String InstallationLocation) {
        this.InstallationLocation = InstallationLocation;
    }

    public String getAttMeter() {
        return AttMeter;
    }

    public String getAttMonitoringID() {
        return AttMonitoringID;
    }

    public String getAttStation() {
        return AttStation;
    }

    public Long getMeter() {
        return Meter;
    }

    public void setMeter(Long Meter) {
        this.Meter = Meter;
    }

    public Long getMonitoringID() {
        return MonitoringID;
    }

    public void setMonitoringID(Long MonitoringID) {
        this.MonitoringID = MonitoringID;
    }

    public String getMeasuringPointName() {
        return MeasuringPointName;
    }

    public void setMeasuringPointName(String MeasuringPointName) {
        this.MeasuringPointName = MeasuringPointName;
    }

    public File getPhoto() {
        return Photo;
    }

    public void setPhoto(File Photo) {
        this.Photo = Photo;
    }

    public String getPhysicalProperty() {
        return PhysicalProperty;
    }

    public void setPhysicalProperty(String PhysicalProperty) {
        this.PhysicalProperty = PhysicalProperty;
    }

    public Long getStation() {
        return Station;
    }

    public void setStation(Long Station) {
        this.Station = Station;
    }

    public String getUnit() {
        return Unit;
    }

    public void setUnit(String Unit) {
        this.Unit = Unit;
    }

}
