/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.iso.add.Snippets;
import org.jevis.ws.sql.SQLDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class MonitoringRegisterDirectory {

    private long ID;
    private String name;

    private JsonObject object;
    private SQLDataSource ds;

    private String MeasuringPointDirName;
    private long MeasuringPointDirID;
    private List<MeasuringPoint> MeasuringPointDir;
    private String MeterDirName;
    private long MeterDirID;
    private List<Meter> MeterDir;
    private String StationDirName;
    private long StationDirID;
    private List<Station> StationDir;

    public MonitoringRegisterDirectory(SQLDataSource ds, JsonObject input) {

        ID = 0L;
        name = "";

        this.ID = input.getId();
        this.name = input.getName();

        this.object = input;
        this.ds = ds;

        MeasuringPointDirName = "";
        MeasuringPointDirID = 0L;
        MeasuringPointDir = new ArrayList<>();
        MeterDirName = "";
        MeterDirID = 0L;
        MeterDir = new ArrayList<>();
        StationDirName = "";
        StationDirID = 0L;
        StationDir = new ArrayList<>();
    }

    MonitoringRegisterDirectory() {

        ID = 0L;
        name = "";
        MeasuringPointDirName = "";
        MeasuringPointDirID = 0L;
        MeasuringPointDir = new ArrayList<>();
        MeterDirName = "";
        MeterDirID = 0L;
        MeterDir = new ArrayList<>();
        StationDirName = "";
        StationDirID = 0L;
        StationDir = new ArrayList<>();
    }

    public SQLDataSource getDs() {
        return ds;
    }

    public void setDs(SQLDataSource ds) {
        this.ds = ds;
    }

    public long getId() {
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

    public JsonObject getObject() {
        return object;
    }

    public void setObject(JsonObject object) {
        this.object = object;
    }

    public List<MeasuringPoint> getMeasuringPointDir() throws Exception {

        if (getObject() != null) {
            MeasuringPointDir.clear();

            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getMeasuringPointDir().getName(), true)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject m : getDs().getObjects(ISO50001.getJc().getMeasuringPoint().getName(), true)) {
                        Snippets.getParent(getDs(), m);
                        if (m.getParent() == obj.getId()) {
                            MeasuringPoint mp = new MeasuringPoint(getDs(), m);
                            MeasuringPointDir.add(mp);
                        }
                    }
                }
            }
        }
        return MeasuringPointDir;
    }

    public void setMeasuringPointDir(List<MeasuringPoint> MeasuringPointDir) {
        this.MeasuringPointDir = MeasuringPointDir;
    }

    public List<Meter> getMeterDir() throws Exception {

        if (getObject() != null) {
            MeterDir.clear();

            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getMeterDir().getName(), true)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject m : getDs().getObjects(ISO50001.getJc().getMeter().getName(), true)) {
                        Snippets.getParent(getDs(), m);
                        if (m.getParent() == obj.getId()) {
                            Meter met = new Meter(getDs(), m);
                            MeterDir.add(met);
                        }
                    }
                }
            }
        }
        return MeterDir;
    }

    public void setMeterDir(List<Meter> MeterDir) {
        this.MeterDir = MeterDir;
    }

    public List<Station> getStationDir() throws Exception {

        if (getObject() != null) {
            StationDir.clear();
            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getStationDir().getName(), true)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject m : getDs().getObjects(ISO50001.getJc().getStation().getName(), true)) {
                        Snippets.getParent(getDs(), m);
                        if (m.getParent() == obj.getId()) {
                            Station s = new Station(getDs(), m);
                            StationDir.add(s);
                        }
                    }
                }
            }
        }
        return StationDir;
    }

    public void setStationDir(List<Station> StationDir) {
        this.StationDir = StationDir;
    }

    public String getMeasuringPointDirName() throws JEVisException {
        MeasuringPointDirName = Snippets.getChildName(getDs(), ISO50001.getJc().getMeasuringPointDir().getName(), getObject());
        return MeasuringPointDirName;
    }

    public void setMeasuringPointDirName(String MeasuringPointDirName) {
        this.MeasuringPointDirName = MeasuringPointDirName;
    }

    public String getMeterDirName() throws JEVisException {
        MeterDirName = Snippets.getChildName(getDs(), ISO50001.getJc().getMeterDir().getName(), getObject());
        return MeterDirName;
    }

    public void setMeterDirName(String MeterDirName) {
        this.MeterDirName = MeterDirName;
    }

    public String getStationDirName() throws JEVisException {
        StationDirName = Snippets.getChildName(getDs(), ISO50001.getJc().getStationDir().getName(), getObject());
        return StationDirName;
    }

    public void setStationDirName(String StationDirName) {
        this.StationDirName = StationDirName;
    }

    public long getMeasuringPointDirID() throws JEVisException {
        MeasuringPointDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getMeasuringPointDir().getName(), getObject());
        return MeasuringPointDirID;
    }

    public void setMeasuringPointDirID(long MeasuringPointDirID) {
        this.MeasuringPointDirID = MeasuringPointDirID;
    }

    public long getMeterDirID() throws JEVisException {
        MeterDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getMeterDir().getName(), getObject());
        return MeterDirID;
    }

    public void setMeterDirID(long MeterDirID) {
        this.MeterDirID = MeterDirID;
    }

    public long getStationDirID() throws JEVisException {
        StationDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getStationDir().getName(), getObject());
        return StationDirID;
    }

    public void setStationDirID(long StationDirID) {
        this.StationDirID = StationDirID;
    }

}
