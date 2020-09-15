/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.jevis.iso.add.Snippets;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.jevis.iso.add.Snippets.getValueString;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class ActionPlan extends ManagementDocument {

    private final SQLDataSource ds;
    private final JsonObject input;
    private String participants;
    private File actionplanfile;

    private List<EnergySavingAction> implementedactions;
    private String implementedactionsdirname;
    private Long implementedactionsdirid;

    private List<EnergySavingAction> plannedactions;
    private String plannedactionsdirname;
    private Long plannedactionsdirid;

    public ActionPlan(SQLDataSource ds, JsonObject input) throws Exception {
        super(ds, input);
        this.ds = ds;
        this.input = input;
        participants = "";
        implementedactions = new ArrayList<>();
        implementedactionsdirname = "";
        implementedactionsdirid = 0L;
        plannedactions = new ArrayList<>();
        plannedactionsdirname = "";
        plannedactionsdirid = 0L;
        for (JsonAttribute att : getDs().getAttributes(input.getId())) {
            String name = att.getType();

            final String attActionPlanFile = "Action Plan File";
            final String attParticipants = "Participants";
            switch (name) {
                case attParticipants:
                    this.setparticipants(getValueString(att, ""));
                    break;
                case attActionPlanFile:
//                  if (Objects.nonNull(getObject())) {
//                        byte[] arr = null;
//                        String filename = "";
//                        JEVisFile file = ds.getFile(getObject().getId(), att.getType(), null);
//                        arr = file.getBytes();
//                        filename = file.getFilename();
//                        this.setActionplanfile(new File(filename));
//                    }
                    break;
                default:
                    break;
            }
        }
        buildImplementedActions();
        buildPlannedActions();
    }

    public void setparticipants(String participants) {
        this.participants = participants;
    }

    public String getparticipants() {
        return participants;
    }

    public File getActionplanfile() {
        return actionplanfile;
    }

    public void setActionplanfile(File actionplanfile) {
        this.actionplanfile = actionplanfile;
    }

    public List<EnergySavingAction> getImplementedactions() {
        return implementedactions;
    }


    public void setImplementedactions(List<EnergySavingAction> implementedactions) {
        this.implementedactions = implementedactions;
    }

    public Long getImplementedactionsdirid() throws JEVisException {
        implementedactionsdirid = Snippets.getChildId(getDs(), ISO50001.getJc().getImplementedActionsDir().getName(), getObject());
        return implementedactionsdirid;
    }

    public void setImplementedactionsdirid(Long implementedactionsdirid) {
        this.implementedactionsdirid = implementedactionsdirid;
    }

    public String getImplementedactionsdirname() throws JEVisException {
        implementedactionsdirname = Snippets.getChildName(getDs(), ISO50001.getJc().getImplementedActionsDir().getName(), getObject());
        return implementedactionsdirname;
    }

    public void setImplementedactionsdirname(String implementedactionsdirname) {
        this.implementedactionsdirname = implementedactionsdirname;
    }

    public List<EnergySavingAction> getPlannedactions() {
        return plannedactions;
    }

    public void setPlannedactions(List<EnergySavingAction> plannedactions) {
        this.plannedactions = plannedactions;
    }

    public void buildImplementedActions() throws Exception {
        if (getObject() != null) {
            implementedactions.clear();
            for (JsonObject implementedActionsDirectory : getDs().getObjects(ISO50001.getJc().getImplementedActionsDir().getName(), false)) {
                Snippets.getParent(getDs(), implementedActionsDirectory);
                if (implementedActionsDirectory.getParent() == getObject().getId()) {
                    implementedactionsdirid = implementedActionsDirectory.getId();
                    implementedactionsdirname = implementedActionsDirectory.getName();
                    getEnergySavingActions(implementedActionsDirectory, implementedactions);
                }
            }
        }
    }

    public void buildPlannedActions() throws Exception {
        if (getObject() != null) {
            plannedactions.clear();
            for (JsonObject plannedActionsDirectory : getDs().getObjects(ISO50001.getJc().getPlannedActionsDir().getName(), false)) {
                Snippets.getParent(getDs(), plannedActionsDirectory);
                if (plannedActionsDirectory.getParent() == getObject().getId()) {
                    plannedactionsdirid = plannedActionsDirectory.getId();
                    plannedactionsdirname = plannedActionsDirectory.getName();
                    getEnergySavingActions(plannedActionsDirectory, plannedactions);
                }
            }
        }
    }

    private void getEnergySavingActions(JsonObject actionDirectory, List<EnergySavingAction> listEnergySavingActions) throws Exception {
        for (JsonObject esa : getDs().getObjects(ISO50001.getJc().getEnergySavingAction().getName(), false)) {
            Snippets.getParent(getDs(), esa);
            if (esa.getParent() == actionDirectory.getId()) {
                EnergySavingAction p = new EnergySavingAction(getDs(), esa);
                listEnergySavingActions.add(p);
            }
        }
    }

    public String getPlannedactionsdirname() throws JEVisException {
        plannedactionsdirname = Snippets.getChildName(getDs(), ISO50001.getJc().getPlannedActionsDir().getName(), getObject());
        return plannedactionsdirname;
    }

    public void setPlannedactionsdirname(String plannedactionsdirname) {
        this.plannedactionsdirname = plannedactionsdirname;
    }

    public Long getPlannedactionsdirid() throws JEVisException {
        plannedactionsdirid = Snippets.getChildId(getDs(), ISO50001.getJc().getPlannedActionsDir().getName(), getObject());
        return plannedactionsdirid;
    }

    public void setPlannedactionsdirid(Long plannedactionsdirid) {
        this.plannedactionsdirid = plannedactionsdirid;
    }

    @Override
    public String toString() {
        return "ActionPlan{" + "ID=" + ID + ", name=" + name + ", year=" + year + ", content=" + content + ", object=" + object + ", createdby=" + createdby + ", dateofcreation=" + dateofcreation + ", dateofdecontrol=" + dateofdecontrol + ", decontrolby=" + decontrolby + ", number=" + number + ", title=" + title + ", version=" + version + ", participants=" + participants + ", implementedactions=" + implementedactions + ", implementedactionsdirname=" + implementedactionsdirname + ", implementedactionsdirid=" + implementedactionsdirid + ", plannedactions=" + plannedactions + ", plannedactionsdirname=" + plannedactionsdirname + ", plannedactionsdirid=" + plannedactionsdirid + '}';
    }


}
