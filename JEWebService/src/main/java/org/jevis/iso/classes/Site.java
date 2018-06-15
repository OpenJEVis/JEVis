/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;
import org.jevis.ws.sql.SQLDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class Site {
    private long ID;
    private JsonObject object;
    private SQLDataSource ds;
    private long MeetingsDirID;
    private String name;

    private DocumentsDirectory documents;
    private EnergyPlanningDirectory energyplanning;
    private List<Meeting> MeetingsDir;
    private String MeetingsDirName;
    private MonitoringRegisterDirectory monitoringregister;

    public Site(JsonObject input, SQLDataSource ds_in) {

        this.object = input;
        this.ds = ds_in;

        ID = 0L;
        MeetingsDirID = 0L;
        name = "";
        documents = new DocumentsDirectory();
        energyplanning = new EnergyPlanningDirectory();
        MeetingsDir = new ArrayList<>();
        MeetingsDirName = "";
        monitoringregister = new MonitoringRegisterDirectory();
    }

    public List<Long> getListYearsMeetings() {
        List<Long> output = new ArrayList<>();
        for (Meeting m : MeetingsDir) {
            if (!output.contains(m.getYear())) {
                output.add(m.getYear());
            }
        }
        return output;
    }

    public long getID() {

        this.ID = getObject().getId();
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public JsonObject getObject() {
        return object;
    }

    public void setObject(JsonObject object) {
        this.object = object;
    }

    public SQLDataSource getDs() {
        return ds;
    }

    public void setDs(SQLDataSource ds) {
        this.ds = ds;
    }

    public String getName() {

        this.name = getObject().getName();
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMeetingsDirName() throws JEVisException {
        for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getMeetingsDir().getName(), true)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == obj.getId()) {
                    if (rel.getType() == 1) {
                        obj.setParent(rel.getTo());
                    }
                }
            }
            if (obj.getParent() == getObject().getId()) {
                this.MeetingsDirName = obj.getName();
            }
        }
        return MeetingsDirName;
    }

    public void setMeetingsDirName(String MeetingsDirName) {
        this.MeetingsDirName = MeetingsDirName;
    }

    public long getMeetingsDirID() throws JEVisException {
        for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getMeetingsDir().getName(), true)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == obj.getId()) {
                    if (rel.getType() == 1) {
                        obj.setParent(rel.getTo());
                    }
                }
            }
            if (obj.getParent() == getObject().getId()) {
                this.MeetingsDirID = obj.getId();
            }
        }
        return MeetingsDirID;
    }

    public void setMeetingsDirID(long MeetingsDirID) {
        this.MeetingsDirID = MeetingsDirID;
    }

    public DocumentsDirectory getDocuments() throws JEVisException {
        for (JsonObject doc : getDs().getObjects(ISO50001.getJc().getDocumentsDir().getName(), false)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == doc.getId()) {
                    if (rel.getType() == 1) {
                        doc.setParent(rel.getTo());
                    }
                }
            }
            if (doc.getParent() == getObject().getId()) {
                documents = new DocumentsDirectory(getDs(), doc);
            }
        }
        return documents;
    }

    public void setDocuments(DocumentsDirectory documents) {
        this.documents = documents;
    }

    public EnergyPlanningDirectory getenergyplanning() throws JEVisException {
        //Energy Planning
        for (JsonObject ep : getDs().getObjects(ISO50001.getJc().getEnergyPlanningDir().getName(), false)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == ep.getId()) {
                    if (rel.getType() == 1) {
                        ep.setParent(rel.getTo());
                    }
                }
            }
            if (ep.getParent() == getObject().getId()) {
                energyplanning = new EnergyPlanningDirectory(getDs(), ep);
            }
        }
        return energyplanning;
    }

    public void setEnergyplanning(EnergyPlanningDirectory energyplanning) {
        this.energyplanning = energyplanning;
    }

    public List<Meeting> getMeetingsDir() throws Exception {
        //Meetings on site
        MeetingsDir.clear();
        for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getMeetingsDir().getName(), true)) {
            this.MeetingsDirName = obj.getName();
            this.MeetingsDirID = obj.getId();

            for (JsonObject m : getDs().getObjects(ISO50001.getJc().getMeeting().getName(), true)) {
                for (JsonRelationship rel : getDs().getRelationships()) {
                    if (rel.getFrom() == m.getId()) {
                        if (rel.getType() == 1) {
                            m.setParent(rel.getTo());
                        }
                    }
                }

                if (m.getParent() == obj.getId()) {
                    Meeting mf = new Meeting(getDs(), m);
                    MeetingsDir.add(mf);
                }
            }
        }
        return MeetingsDir;
    }

    public void setMeetingsDir(List<Meeting> MeetingsDir) {
        this.MeetingsDir = MeetingsDir;
    }

    public MonitoringRegisterDirectory getMonitoringregister() throws Exception {
        //Monitoring Register
        for (JsonObject mr : getDs().getObjects(ISO50001.getJc().getMonitoringRegisterDir().getName(), false)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == mr.getId()) {
                    if (rel.getType() == 1) {
                        mr.setParent(rel.getTo());
                    }
                }
            }
            if (mr.getParent() == getObject().getId()) {
                monitoringregister = new MonitoringRegisterDirectory(getDs(), mr);
            }
        }
        return monitoringregister;
    }

    public void setMonitoringregister(MonitoringRegisterDirectory monitoringregister) {
        this.monitoringregister = monitoringregister;
    }

    @Override
    public String toString() {
        return "Site{" + "ID=" + ID + ", name=" + name + ", documents=" + documents + ", energyplanning=" + energyplanning + ", MeetingsDir=" + MeetingsDir + ", monitoringregister=" + monitoringregister + '}';
    }

}
