/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;
import org.jevis.ws.sql.SQLDataSource;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import static org.jevis.iso.add.Snippets.getValueString;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class InternalAudit {
    private static final Logger logger = LogManager.getLogger(InternalAudit.class);

    private long ID;
    private String name;
    private Long year;

    private JsonObject object;
    private SQLDataSource ds;
    private String auditor;
    private String auditdate;

    private List<InternalAuditQuestion> generalquestions;
    private String generalquestionsname;
    private Long generalquestionsid;
    private List<InternalAuditQuestion> planquestions;
    private String planquestionsname;
    private Long planquestionsid;
    private List<InternalAuditQuestion> doquestions;
    private String doquestionsname;
    private Long doquestionsid;
    private List<InternalAuditQuestion> checkquestions;
    private String checkquestionsname;
    private Long checkquestionsid;
    private List<InternalAuditQuestion> actquestions;
    private String actquestionsname;
    private Long actquestionsid;

    public InternalAudit(SQLDataSource ds, JsonObject input) {
        ID = 0L;
        name = "";
        year = 0L;
        auditor = "";
        auditdate = "";
        generalquestions = new ArrayList<>();
        generalquestionsname = "";
        generalquestionsid = 0L;
        planquestions = new ArrayList<>();
        planquestionsname = "";
        planquestionsid = 0L;
        doquestions = new ArrayList<>();
        doquestionsname = "";
        doquestionsid = 0L;
        checkquestions = new ArrayList<>();
        checkquestionsname = "";
        checkquestionsid = 0L;
        actquestions = new ArrayList<>();
        actquestionsname = "";
        actquestionsid = 0L;
        this.ID = input.getId();
        this.name = input.getName();

        this.object = input;
        this.ds = ds;

        List<JsonAttribute> listAnnouncementAttributes = ds.getAttributes(input.getId());

        for (JsonAttribute att : listAnnouncementAttributes) {

            String name = att.getType();

            final String attAuditDate = "Audit Date";
            final String attAuditor = "Auditor";
            switch (name) {
                case attAuditor:
                    this.setauditor(getValueString(att, ""));
                    break;
                case attAuditDate:
                    String s = getValueString(att, "");
                    DateTimeFormatter format = DateTimeFormat.forPattern("dd.MM.yyyy");
                    if (!"".equals(s)) {
                        DateTime dt = format.parseDateTime(s);
                        this.setYear((long) dt.getYear());
                    } else {
                        this.setYear(0L);
                    }
                    this.setauditdate(s);
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

    public Long getYear() {
        return year;
    }

    public void setYear(Long year) {
        this.year = year;
    }

    public SQLDataSource getDs() {
        return ds;
    }

    public void setDs(SQLDataSource ds) {
        this.ds = ds;
    }

    public String getauditor() {
        return auditor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setauditor(String auditor) {
        this.auditor = auditor;
    }

    public String getGeneralquestionsname() {
        try {
            for (JsonObject iag : getDs().getObjects(ISO50001.getJc().getInternalAuditGeneral().getName(), true)) {
                for (JsonRelationship rel : getDs().getRelationships()) {
                    if (rel.getFrom() == iag.getId()) {
                        if (rel.getType() == 1) {
                            iag.setParent(rel.getTo());
                        }
                    }
                }
                if (iag.getParent() == getObject().getId()) {
                    this.generalquestionsname = iag.getName();
                }
            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
        return generalquestionsname;
    }

    public void setGeneralquestionsname(String generalquestionsname) {
        this.generalquestionsname = generalquestionsname;
    }

    public String getPlanquestionsname() throws JEVisException {
        for (JsonObject iag : getDs().getObjects(ISO50001.getJc().getInternalAuditPlan().getName(), true)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == iag.getId()) {
                    if (rel.getType() == 1) {
                        iag.setParent(rel.getTo());
                    }
                }
            }
            if (iag.getParent() == getObject().getId()) {
                this.planquestionsname = iag.getName();
            }
        }
        return planquestionsname;
    }

    public void setPlanquestionsname(String planquestionsname) {
        this.planquestionsname = planquestionsname;
    }

    public String getDoquestionsname() throws JEVisException {
        for (JsonObject iag : getDs().getObjects(ISO50001.getJc().getInternalAuditDo().getName(), true)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == iag.getId()) {
                    if (rel.getType() == 1) {
                        iag.setParent(rel.getTo());
                    }
                }
            }
            if (iag.getParent() == getObject().getId()) {
                this.doquestionsname = iag.getName();
            }
        }
        return doquestionsname;
    }

    public void setDoquestionsname(String doquestionsname) {
        this.doquestionsname = doquestionsname;
    }

    public String getCheckquestionsname() throws JEVisException {
        for (JsonObject iag : getDs().getObjects(ISO50001.getJc().getInternalAuditCheck().getName(), true)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == iag.getId()) {
                    if (rel.getType() == 1) {
                        iag.setParent(rel.getTo());
                    }
                }
            }
            if (iag.getParent() == getObject().getId()) {
                this.checkquestionsname = iag.getName();
            }
        }
        return checkquestionsname;
    }

    public void setCheckquestionsname(String checkquestionsname) {
        this.checkquestionsname = checkquestionsname;
    }

    public String getActquestionsname() throws JEVisException {
        for (JsonObject iag : getDs().getObjects(ISO50001.getJc().getInternalAuditAct().getName(), true)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == iag.getId()) {
                    if (rel.getType() == 1) {
                        iag.setParent(rel.getTo());
                    }
                }
            }
            if (iag.getParent() == getObject().getId()) {
                this.actquestionsname = iag.getName();
            }
        }
        return actquestionsname;
    }

    public void setActquestionsname(String actquestionsname) {
        this.actquestionsname = actquestionsname;
    }

    public String getauditdate() {
        return auditdate;
    }

    public void setauditdate(String auditdate) {
        this.auditdate = auditdate;
    }

    public List<InternalAuditQuestion> getGeneralquestions() {
        return generalquestions;
    }

    public void setGeneralquestions(List<InternalAuditQuestion> generalquestions) {
        this.generalquestions = generalquestions;
    }

    public void buildGeneralQuestions() throws Exception {
        generalquestions.clear();
        for (JsonObject iag : getDs().getObjects(ISO50001.getJc().getInternalAuditGeneral().getName(), true)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == iag.getId()) {
                    if (rel.getType() == 1) {
                        iag.setParent(rel.getTo());
                    }
                }
            }
            if (iag.getParent() == getObject().getId()) {
                this.generalquestionsname = iag.getName();
                this.generalquestionsid = iag.getId();

                getAuditQuestionList(iag, generalquestions);
            }
        }
    }

    private void getAuditQuestionList(JsonObject internalAuditTopic, List<InternalAuditQuestion> questionList) throws Exception {
        for (JsonObject iaq : getDs().getObjects(ISO50001.getJc().getAuditQuestion().getName(), true)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == iaq.getId()) {
                    if (rel.getType() == 1) {
                        iaq.setParent(rel.getTo());
                    }
                }
            }
            if (iaq.getParent() == internalAuditTopic.getId()) {
                InternalAuditQuestion aq = new InternalAuditQuestion(getDs(), iaq);
                questionList.add(aq);
            }
        }
    }

    public List<InternalAuditQuestion> getPlanquestions() {
        return planquestions;
    }

    public void setPlanquestions(List<InternalAuditQuestion> planquestions) {
        this.planquestions = planquestions;
    }

    public void buildPlanQuestions() throws Exception {
        planquestions.clear();
        for (JsonObject iag : getDs().getObjects(ISO50001.getJc().getInternalAuditPlan().getName(), true)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == iag.getId()) {
                    if (rel.getType() == 1) {
                        iag.setParent(rel.getTo());
                    }
                }
            }
            if (iag.getParent() == getObject().getId()) {
                this.planquestionsname = iag.getName();
                this.planquestionsid = iag.getId();

                getAuditQuestionList(iag, planquestions);
            }
        }
    }

    public List<InternalAuditQuestion> getDoquestions() {
        return doquestions;
    }

    public void setDoquestions(List<InternalAuditQuestion> doquestions) {
        this.doquestions = doquestions;
    }

    public void buildDoQuestions() throws Exception {
        doquestions.clear();
        for (JsonObject iag : getDs().getObjects(ISO50001.getJc().getInternalAuditDo().getName(), true)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == iag.getId()) {
                    if (rel.getType() == 1) {
                        iag.setParent(rel.getTo());
                    }
                }
            }
            if (iag.getParent() == getObject().getId()) {
                this.doquestionsname = iag.getName();
                this.doquestionsid = iag.getId();

                getAuditQuestionList(iag, doquestions);
            }
        }
    }

    public List<InternalAuditQuestion> getCheckquestions() {
        return checkquestions;
    }

    public void setCheckquestions(List<InternalAuditQuestion> checkquestions) {
        this.checkquestions = checkquestions;
    }

    public void buildCheckQuestions() throws Exception {
        checkquestions.clear();
        for (JsonObject iag : getDs().getObjects(ISO50001.getJc().getInternalAuditCheck().getName(), true)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == iag.getId()) {
                    if (rel.getType() == 1) {
                        iag.setParent(rel.getTo());
                    }
                }
            }
            if (iag.getParent() == getObject().getId()) {
                this.checkquestionsname = iag.getName();
                this.checkquestionsid = iag.getId();

                getAuditQuestionList(iag, checkquestions);
            }
        }
    }

    public List<InternalAuditQuestion> getActquestions() {
        return actquestions;
    }

    public void setActquestions(List<InternalAuditQuestion> actquestions) {
        this.actquestions = actquestions;
    }

    public void buildActQuestions() throws Exception {
        actquestions.clear();
        for (JsonObject iag : getDs().getObjects(ISO50001.getJc().getInternalAuditAct().getName(), true)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == iag.getId()) {
                    if (rel.getType() == 1) {
                        iag.setParent(rel.getTo());
                    }
                }
            }
            if (iag.getParent() == getObject().getId()) {
                this.actquestionsname = iag.getName();
                this.actquestionsid = iag.getId();

                getAuditQuestionList(iag, actquestions);
            }
        }
    }

    @Override
    public String toString() {
        return "InternalAudit{" + "ID=" + ID + ", name=" + name + ", auditor=" + auditor + ", auditdate=" + auditdate + ", GeneralQuestions=" + generalquestions + ", GeneralQuestionsName=" + generalquestionsname + ", PlanQuestions=" + planquestions + ", PlanQuestionsName=" + planquestionsname + ", DoQuestions=" + doquestions + ", DoQuestionsName=" + doquestionsname + ", CheckQuestions=" + checkquestions + ", CheckQuestionsName=" + checkquestionsname + ", ActQuestions=" + actquestions + ", ActQuestionsName=" + actquestionsname + '}';
    }


    public JsonObject getObject() {
        return object;
    }

    public void setObject(JsonObject object) {
        this.object = object;
    }

    public String getAuditor() {
        return auditor;
    }

    public void setAuditor(String auditor) {
        this.auditor = auditor;
    }

    public String getAuditdate() {
        return auditdate;
    }

    public void setAuditdate(String auditdate) {
        this.auditdate = auditdate;
    }

    public Long getGeneralquestionsid() throws JEVisException {
        for (JsonObject iag : getDs().getObjects(ISO50001.getJc().getInternalAuditGeneral().getName(), true)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == iag.getId()) {
                    if (rel.getType() == 1) {
                        iag.setParent(rel.getTo());
                    }
                }
            }
            if (iag.getParent() == getObject().getId()) {
                this.generalquestionsid = iag.getId();

            }
        }
        return generalquestionsid;
    }

    public void setGeneralquestionsid(Long generalquestionsid) {
        this.generalquestionsid = generalquestionsid;
    }

    public Long getPlanquestionsid() throws JEVisException {
        for (JsonObject iag : getDs().getObjects(ISO50001.getJc().getInternalAuditPlan().getName(), true)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == iag.getId()) {
                    if (rel.getType() == 1) {
                        iag.setParent(rel.getTo());
                    }
                }
            }
            if (iag.getParent() == getObject().getId()) {
                this.planquestionsid = iag.getId();

            }
        }
        return planquestionsid;
    }

    public void setPlanquestionsid(Long planquestionsid) {
        this.planquestionsid = planquestionsid;
    }

    public Long getDoquestionsid() throws JEVisException {
        for (JsonObject iag : getDs().getObjects(ISO50001.getJc().getInternalAuditDo().getName(), true)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == iag.getId()) {
                    if (rel.getType() == 1) {
                        iag.setParent(rel.getTo());
                    }
                }
            }
            if (iag.getParent() == getObject().getId()) {
                this.doquestionsid = iag.getId();

            }
        }
        return doquestionsid;
    }

    public void setDoquestionsid(Long doquestionsid) {
        this.doquestionsid = doquestionsid;
    }

    public Long getCheckquestionsid() throws JEVisException {
        for (JsonObject iag : getDs().getObjects(ISO50001.getJc().getInternalAuditCheck().getName(), true)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == iag.getId()) {
                    if (rel.getType() == 1) {
                        iag.setParent(rel.getTo());
                    }
                }
            }
            if (iag.getParent() == getObject().getId()) {
                this.checkquestionsid = iag.getId();

            }
        }
        return checkquestionsid;
    }

    public void setCheckquestionsid(Long checkquestionsid) {
        this.checkquestionsid = checkquestionsid;
    }

    public Long getActquestionsid() throws JEVisException {
        for (JsonObject iag : getDs().getObjects(ISO50001.getJc().getInternalAuditAct().getName(), true)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == iag.getId()) {
                    if (rel.getType() == 1) {
                        iag.setParent(rel.getTo());
                    }
                }
            }
            if (iag.getParent() == getObject().getId()) {
                this.actquestionsid = iag.getId();

            }
        }
        return actquestionsid;
    }

    public void setActquestionsid(Long actquestionsid) {
        this.actquestionsid = actquestionsid;
    }

}
