/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.sql.SQLDataSource;

import java.util.ArrayList;
import java.util.List;

import static org.jevis.iso.add.Snippets.getValueString;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class InternalAuditQuestion {
    private long ID;
    private String name;
    private String auditobservation;
    private String correctiveaction;
    private boolean evaluation000points;
    private boolean evaluation025points;
    private boolean evaluation050points;
    private boolean evaluation075points;
    private boolean evaluation100points;
    private String normchapter;
    private String question;

    public InternalAuditQuestion(SQLDataSource ds, JsonObject input) throws Exception {
        ID = 0L;
        name = "";
        auditobservation = "";
        correctiveaction = "";
        evaluation000points = false;
        evaluation025points = false;
        evaluation050points = false;
        evaluation075points = false;
        evaluation100points = false;
        normchapter = "";
        question = "";
        this.ID = input.getId();
        this.name = input.getName();

        List<JsonAttribute> listAuditQuestionAttributes = new ArrayList<>();

        listAuditQuestionAttributes = ds.getAttributes(input.getId());

        for (JsonAttribute att : listAuditQuestionAttributes) {
            String name = att.getType();

            final String attQuestion = "Question";
            final String attNormChapter = "Norm Chapter";
            final String attEvaluation100Points = "Evaluation 100 points";
            final String attEvaluation075Points = "Evaluation 075 points";
            final String attEvaluation050Points = "Evaluation 050 points";
            final String attEvaluation025Points = "Evaluation 025 points";
            final String attEvaluation000Points = "Evaluation 000 points";
            final String attCorrectiveAction = "Proposed Measures";
            final String attAuditObservation = "Audit Observations";
            switch (name) {

                case attAuditObservation:
                    this.setauditobservation(getValueString(att, ""));
                    break;
                case attCorrectiveAction:
                    this.setcorrectiveaction(getValueString(att, ""));
                    break;
                case attEvaluation000Points:
                    if (getValueString(att, "") != "") {
                        this.setevaluation000points(Boolean.parseBoolean(getValueString(att, "")));
                    } else this.setevaluation000points(false);
                    break;
                case attEvaluation025Points:
                    if (getValueString(att, "") != "") {
                        this.setevaluation025points(Boolean.parseBoolean(getValueString(att, "")));
                    } else this.setevaluation000points(false);
                    break;
                case attEvaluation050Points:
                    if (getValueString(att, "") != "") {
                        this.setevaluation050points(Boolean.parseBoolean(getValueString(att, "")));
                    } else this.setevaluation000points(false);
                    break;
                case attEvaluation075Points:
                    if (getValueString(att, "") != "") {
                        this.setevaluation075points(Boolean.parseBoolean(getValueString(att, "")));
                    } else this.setevaluation000points(false);
                    break;
                case attEvaluation100Points:
                    if (getValueString(att, "") != "") {
                        this.setevaluation100points(Boolean.parseBoolean(getValueString(att, "")));
                    } else this.setevaluation000points(false);
                    break;
                case attNormChapter:
                    this.setnormchapter(getValueString(att, ""));
                    break;
                case attQuestion:
                    this.setquestion(getValueString(att, ""));
                    break;
                default:
                    break;
            }

        }

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

    public String getauditobservation() {
        return auditobservation;
    }

    public void setauditobservation(String auditobservation) {
        this.auditobservation = auditobservation;
    }

    public String getcorrectiveaction() {
        return correctiveaction;
    }

    public void setcorrectiveaction(String correctiveaction) {
        this.correctiveaction = correctiveaction;
    }

    public boolean isevaluation000points() {
        return evaluation000points;
    }

    public void setevaluation000points(boolean evaluation000points) {
        this.evaluation000points = evaluation000points;
    }

    public boolean isevaluation025points() {
        return evaluation025points;
    }

    public void setevaluation025points(boolean evaluation025points) {
        this.evaluation025points = evaluation025points;
    }

    public boolean isevaluation050points() {
        return evaluation050points;
    }

    public void setevaluation050points(boolean evaluation050points) {
        this.evaluation050points = evaluation050points;
    }

    public boolean isevaluation075points() {
        return evaluation075points;
    }

    public void setevaluation075points(boolean evaluation075points) {
        this.evaluation075points = evaluation075points;
    }

    public boolean isevaluation100points() {
        return evaluation100points;
    }

    public void setevaluation100points(boolean evaluation100points) {
        this.evaluation100points = evaluation100points;
    }

    public String getnormchapter() {
        return normchapter;
    }

    public void setnormchapter(String normchapter) {
        this.normchapter = normchapter;
    }

    public String getquestion() {
        return question;
    }

    public void setquestion(String question) {
        this.question = question;
    }

    @Override
    public String toString() {
        return "InternalAuditQuestion{" + "ID=" + ID + ", auditobservation=" + auditobservation + ", correctiveaction=" + correctiveaction + ", evaluation000points=" + evaluation000points + ", evaluation025points=" + evaluation025points + ", evaluation050points=" + evaluation050points + ", evaluation075points=" + evaluation075points + ", evaluation100points=" + evaluation100points + ", normchapter=" + normchapter + ", question=" + question + '}';
    }

}
