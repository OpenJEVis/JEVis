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
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class DocumentsDirectory {

    private long ID;
    private JsonObject object;
    private SQLDataSource ds;
    private String name;

    private EnergyManager energymanager;
    private List<ActionPlan> ActionPlans;
    private List<Long> ActionPlansYears;
    private List<Announcement> Announcements;
    private List<Long> AnnouncementsYears;
    private List<EnergyTeamMember> EnergyTeamMember;
    private List<ExternalAudit> ExternalAudits;
    private List<Long> ExternalAuditsYears;
    private List<InternalAudit> InternalAudits;
    private List<Long> InternalAuditsYears;
    private List<LegalRegulation> LegalRegulations;
    private ManagementManualDirectory managementManualDirectory;
    private List<ManagementReview> ManagementReviews;
    private List<Long> ManagementReviewsYears;
    private List<ProceduralDocument> ProceduralDocuments;
    private List<TrainingCourse> TrainingCourses;
    private List<Training> Trainings;
    private List<Long> TrainingsYears;

    private String actionPlansName;
    private long actionPlansDirID;
    private String announcementsName;
    private long annoucementsDirID;
    private String energyTeamMemberName;
    private long energyTeamMemberDirID;
    private String AuditsDirName;
    private long auditsDirID;
    private String legalRegulationsName;
    private long legalRegulationsDirID;
    private String managementManualDirName;
    private long managementManualDirID;
    private String managementReviewsName;
    private long managementReviewsDirID;
    private String proceduralDocumentsName;
    private long proceduralDocumentsID;
    private String trainingCoursesName;
    private long trainingCoursesDirID;
    private String trainingsName;
    private long trainingsDirID;

    public DocumentsDirectory(SQLDataSource ds, JsonObject input) {

        this.object = input;
        this.ds = ds;

        ID = 0L;
        this.ID = input.getId();
        name = "";
        this.name = input.getName();

        AuditsDirName = "";
        energyTeamMemberName = "";
        energymanager = new EnergyManager();
        ActionPlans = new ArrayList<>();
        ActionPlansYears = new ArrayList<>();
        Announcements = new ArrayList<>();
        AnnouncementsYears = new ArrayList<>();
        EnergyTeamMember = new ArrayList<>();
        ExternalAudits = new ArrayList<>();
        ExternalAuditsYears = new ArrayList<>();
        InternalAudits = new ArrayList<>();
        InternalAuditsYears = new ArrayList<>();
        LegalRegulations = new ArrayList<>();
        managementManualDirectory = new ManagementManualDirectory();
        ManagementReviews = new ArrayList<>();
        ManagementReviewsYears = new ArrayList<>();
        ProceduralDocuments = new ArrayList<>();
        TrainingCourses = new ArrayList<>();
        Trainings = new ArrayList<>();
        TrainingsYears = new ArrayList<>();
        actionPlansName = "";
        actionPlansDirID = 0L;
        announcementsName = "";
        annoucementsDirID = 0L;
        energyTeamMemberDirID = 0L;
        auditsDirID = 0L;
        legalRegulationsName = "";
        legalRegulationsDirID = 0L;
        managementManualDirName = "";
        managementManualDirID = 0L;
        managementReviewsName = "";
        managementReviewsDirID = 0L;
        proceduralDocumentsName = "";
        proceduralDocumentsID = 0L;
        trainingCoursesName = "";
        trainingCoursesDirID = 0L;
        trainingsName = "";
        trainingsDirID = 0L;
    }

    DocumentsDirectory() {

        AuditsDirName = "";
        energyTeamMemberName = "";
        name = "";
        ID = 0L;
        energymanager = new EnergyManager();
        ActionPlans = new ArrayList<>();
        ActionPlansYears = new ArrayList<>();
        Announcements = new ArrayList<>();
        AnnouncementsYears = new ArrayList<>();
        EnergyTeamMember = new ArrayList<>();
        ExternalAudits = new ArrayList<>();
        ExternalAuditsYears = new ArrayList<>();
        InternalAudits = new ArrayList<>();
        InternalAuditsYears = new ArrayList<>();
        LegalRegulations = new ArrayList<>();
        managementManualDirectory = new ManagementManualDirectory();
        ManagementReviews = new ArrayList<>();
        ManagementReviewsYears = new ArrayList<>();
        ProceduralDocuments = new ArrayList<>();
        TrainingCourses = new ArrayList<>();
        Trainings = new ArrayList<>();
        TrainingsYears = new ArrayList<>();
        actionPlansName = "";
        actionPlansDirID = 0L;
        announcementsName = "";
        annoucementsDirID = 0L;
        energyTeamMemberDirID = 0L;
        auditsDirID = 0L;
        legalRegulationsName = "";
        legalRegulationsDirID = 0L;
        managementManualDirName = "";
        managementManualDirID = 0L;
        managementReviewsName = "";
        managementReviewsDirID = 0L;
        proceduralDocumentsName = "";
        proceduralDocumentsID = 0L;
        trainingCoursesName = "";
        trainingCoursesDirID = 0L;
        trainingsName = "";
        trainingsDirID = 0L;
    }

    public List<Long> getListYearsActionPlans() {
        List<Long> output = new ArrayList<>();
        for (ActionPlan ap : ActionPlans) {
            if (!output.contains(ap.getYear())) {
                output.add(ap.getYear());
            }
        }
        return output;
    }

    public List<Long> getListYearsAnnouncements() {
        List<Long> output = new ArrayList<>();
        for (Announcement a : Announcements) {
            if (!output.contains(a.getYear())) {
                output.add(a.getYear());
            }
        }
        return output;
    }

    public List<Long> getListYearsExternalAudits() {
        List<Long> output = new ArrayList<>();
        for (ExternalAudit ea : ExternalAudits) {
            if (!output.contains(ea.getYear())) {
                output.add(ea.getYear());
            }
        }
        return output;
    }

    public List<Long> getListYearsInternalAudits() {
        List<Long> output = new ArrayList<>();
        for (InternalAudit ia : InternalAudits) {
            if (!output.contains(ia.getYear())) {
                output.add(ia.getYear());
            }
        }
        return output;
    }

    public List<Long> getListYearsManagementReviews() {
        List<Long> output = new ArrayList<>();
        for (ManagementReview mr : ManagementReviews) {
            if (!output.contains(mr.getYear())) {
                output.add(mr.getYear());
            }
        }
        return output;
    }

    public List<Long> getListYearsTrainings() {
        List<Long> output = new ArrayList<>();
        for (Training t : Trainings) {
            if (!output.contains(t.getYear())) {
                output.add(t.getYear());
            }
        }
        return output;
    }

    public long getId() {
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

    public ActionPlan getActionPlan(Long ID) throws Exception {
        ActionPlan ap = null;
        if (getObject() != null) {
            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getActionPlanDir().getName(), false)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject m : Snippets.getAllChildren(getDs(), obj)) {
                        Snippets.getParent(getDs(), m);
                        if (m.getParent() == obj.getId()) {
                            ap = new ActionPlan(getDs(), m);
                        }
                    }
                }
            }
        }
        return ap;
    }

    public SQLDataSource getDs() {
        return ds;
    }

    public void setDs(SQLDataSource ds) {
        this.ds = ds;
    }

    public ActionPlan getActionPlanByYear(Long Year) throws Exception {
        ActionPlan ap = null;
        if (getObject() != null) {
            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getActionPlanDir().getName(), false)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject m : getDs().getObjects(ISO50001.getJc().getActionPlan().getName(), true)) {
                        Snippets.getParent(getDs(), m);
                        if (m.getParent() == obj.getId()) {
                            String s = getDs().getAttribute(m.getId(), ManagementDocument.AttDateOfDecontrol).getLatestValue().getValue();
                            DateTimeFormatter format = DateTimeFormat.forPattern("dd.MM.yyyy");
                            if (!"".equals(s)) {
                                DateTime dt = format.parseDateTime(s);
                                long now = ((long) dt.getYear());
                                if (Objects.equals(now, Year)) {
                                    ap = new ActionPlan(getDs(), m);
                                }
                            }
                        }
                    }
                }
            }
        }
        return ap;
    }

    public List<ActionPlan> getActionPlans() throws Exception {
        ActionPlans.clear();
        if (getObject() != null) {
            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getActionPlanDir().getName(), false)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject m : getDs().getObjects(ISO50001.getJc().getActionPlan().getName(), false)) {
                        Snippets.getParent(getDs(), m);
                        if (m.getParent() == obj.getId()) {
                            ActionPlan ap = new ActionPlan(ds, m);
                            ActionPlans.add(ap);
                        }
                    }
                }
            }
        }
        return ActionPlans;
    }

    public void setActionPlans(List<ActionPlan> ActionPlans) {
        this.ActionPlans = ActionPlans;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EnergyManager getEnergymanager() throws Exception {
        if (getObject() != null) {
            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getEnergyTeamDir().getName(), false)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject em : getDs().getObjects(ISO50001.getJc().getEnergyManager().getName(), false)) {
                        Snippets.getParent(getDs(), em);
                        if (em.getParent() == obj.getId()) {
                            energymanager = new EnergyManager(ds, em);
                        }
                    }
                }
            }
        }
        return energymanager;
    }

    public void setEnergymanager(EnergyManager energymanager) {
        this.energymanager = energymanager;
    }

    public List<EnergyTeamMember> getEnergyTeamMember() throws Exception {
        if (getObject() != null) {
            EnergyTeamMember.clear();
            //JsonObject JEVisEnergyTeam;
            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getEnergyTeamDir().getName(), false)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject m : getDs().getObjects(ISO50001.getJc().getEnergyTeamMember().getName(), false)) {
                        Snippets.getParent(getDs(), m);
                        if (m.getParent() == obj.getId()) {
                            EnergyTeamMember etm = new EnergyTeamMember(ds, m);
                            EnergyTeamMember.add(etm);
                        }
                    }
                }
            }
        }
        return EnergyTeamMember;
    }

    public void setEnergyTeamMember(List<EnergyTeamMember> EnergyTeamMember) {
        this.EnergyTeamMember = EnergyTeamMember;
    }

    public long getActionPlansDirID() throws JEVisException {
        this.actionPlansDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getActionPlanDir().getName(), getObject());
        return actionPlansDirID;
    }

    public void setActionPlansDirID(long actionPlansDirID) {
        this.actionPlansDirID = actionPlansDirID;
    }

    public long getAnnoucementsDirID() throws JEVisException {
        this.annoucementsDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getAnnouncementDir().getName(), getObject());
        return annoucementsDirID;
    }

    public void setAnnoucementsDirID(long annoucementsDirID) {
        this.annoucementsDirID = annoucementsDirID;
    }

    public long getEnergyTeamMemberDirID() throws JEVisException {
        this.energyTeamMemberDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getEnergyTeamDir().getName(), getObject());
        return energyTeamMemberDirID;
    }

    public void setEnergyTeamMemberDirID(long energyTeamMemberDirID) {
        this.energyTeamMemberDirID = energyTeamMemberDirID;
    }

    public long getAuditsDirID() throws JEVisException {
        this.auditsDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getAuditDir().getName(), getObject());
        return auditsDirID;
    }

    public void setAuditsDirID(long auditsDirID) {
        this.auditsDirID = auditsDirID;
    }

    public long getLegalRegulationsDirID() throws JEVisException {
        this.legalRegulationsDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getLegalRegulationDir().getName(), getObject());
        return legalRegulationsDirID;
    }

    public void setLegalRegulationsDirID(long legalRegulationsDirID) {
        this.legalRegulationsDirID = legalRegulationsDirID;
    }

    public String getManagementManualDirName() throws JEVisException {
        this.managementManualDirName = Snippets.getChildName(getDs(), ISO50001.getJc().getManagementManualDir().getName(), getObject());
        return managementManualDirName;
    }

    public void setManagementManualDirName(String managementManualDirName) {
        this.managementManualDirName = managementManualDirName;
    }

    public long getManagementManualDirID() throws JEVisException {
        this.managementManualDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getManagementManualDir().getName(), getObject());
        return managementManualDirID;
    }

    public void setManagementManualDirID(long managementManualDirID) {
        this.managementManualDirID = managementManualDirID;
    }

    public long getManagementReviewsDirID() throws JEVisException {
        this.managementReviewsDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getManagementReviewDir().getName(), getObject());
        return managementReviewsDirID;
    }

    public void setManagementReviewsDirID(long managementReviewsDirID) {
        this.managementReviewsDirID = managementReviewsDirID;
    }

    public String getProceduralDocumentsName() throws JEVisException {
        this.proceduralDocumentsName = Snippets.getChildName(getDs(), ISO50001.getJc().getProceduralDocumentsDir().getName(), getObject());
        return proceduralDocumentsName;
    }

    public void setProceduralDocumentsName(String proceduralDocumentsName) {
        this.proceduralDocumentsName = proceduralDocumentsName;
    }

    public long getProceduralDocumentsID() throws JEVisException {
        this.proceduralDocumentsID = Snippets.getChildId(getDs(), ISO50001.getJc().getProceduralDocumentsDir().getName(), getObject());
        return proceduralDocumentsID;
    }

    public void setProceduralDocumentsID(long proceduralDocumentsID) {
        this.proceduralDocumentsID = proceduralDocumentsID;
    }

    public long getTrainingCoursesDirID() throws JEVisException {
        this.trainingCoursesDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getTrainingCourseDir().getName(), getObject());
        return trainingCoursesDirID;
    }

    public void setTrainingCoursesDirID(long trainingCoursesDirID) {
        this.trainingCoursesDirID = trainingCoursesDirID;
    }

    public long getTrainingsDirID() throws JEVisException {
        this.trainingsDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getTrainingDir().getName(), getObject());
        return trainingsDirID;
    }

    public void setTrainingsDirID(long trainingsDirID) {
        this.trainingsDirID = trainingsDirID;
    }

    public String getActionPlansName() throws JEVisException {
        this.actionPlansName = Snippets.getChildName(getDs(), ISO50001.getJc().getActionPlanDir().getName(), getObject());
        return actionPlansName;
    }

    public void setActionPlansName(String actionPlansName) {
        this.actionPlansName = actionPlansName;
    }

    public String getAnnouncementsName() throws JEVisException {
        this.announcementsName = Snippets.getChildName(getDs(), ISO50001.getJc().getAnnouncementDir().getName(), getObject());
        return announcementsName;
    }

    public void setAnnouncementsName(String announcementsName) {
        this.announcementsName = announcementsName;
    }

    public String getEnergyTeamMemberName() throws JEVisException {
        this.energyTeamMemberName = Snippets.getChildName(getDs(), ISO50001.getJc().getEnergyTeamDir().getName(), getObject());
        return energyTeamMemberName;
    }

    public void setEnergyTeamMemberName(String energyTeamMemberName) {
        this.energyTeamMemberName = energyTeamMemberName;
    }

    public String getAuditsDirName() throws JEVisException {
        this.AuditsDirName = Snippets.getChildName(getDs(), ISO50001.getJc().getAuditDir().getName(), getObject());
        return AuditsDirName;
    }

    public void setAuditsDirName(String AuditsDirName) {
        this.AuditsDirName = AuditsDirName;
    }

    public String getLegalRegulationsName() throws JEVisException {
        this.legalRegulationsName = Snippets.getChildName(getDs(), ISO50001.getJc().getLegalRegulationDir().getName(), getObject());
        return legalRegulationsName;
    }

    public void setLegalRegulationsName(String legalRegulationsName) {
        this.legalRegulationsName = legalRegulationsName;
    }

    public String getManagementReviewsName() throws JEVisException {
        this.managementReviewsName = Snippets.getChildName(getDs(), ISO50001.getJc().getManagementReviewDir().getName(), getObject());
        return managementReviewsName;
    }

    public void setManagementReviewsName(String managementReviewsName) {
        this.managementReviewsName = managementReviewsName;
    }

    public String getTrainingCoursesName() throws JEVisException {
        this.trainingCoursesName = Snippets.getChildName(getDs(), ISO50001.getJc().getTrainingCourseDir().getName(), getObject());
        return trainingCoursesName;
    }

    public void setTrainingCoursesName(String trainingCoursesName) {
        this.trainingCoursesName = trainingCoursesName;
    }

    public String getTrainingsName() throws JEVisException {
        this.trainingsName = Snippets.getChildName(getDs(), ISO50001.getJc().getTrainingDir().getName(), getObject());
        return trainingsName;
    }

    public void setTrainingsName(String trainingsName) {
        this.trainingsName = trainingsName;
    }

    public List<Announcement> getAnnouncements() throws Exception {
        Announcements.clear();
        for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getAnnouncementDir().getName(), false)) {
            Snippets.getParent(getDs(), obj);
            if (obj.getParent() == getObject().getId()) {
                for (JsonObject m : getDs().getObjects(ISO50001.getJc().getAnnouncement().getName(), false)) {
                    Snippets.getParent(getDs(), m);
                    if (m.getParent() == obj.getId()) {
                        Announcement a = new Announcement(getDs(), m);
                        Announcements.add(a);
                    }
                }
            }
        }
        return Announcements;
    }

    public void setAnnouncements(List<Announcement> Announcements) {
        this.Announcements = Announcements;
    }

    public List<ExternalAudit> getExternalAudits() throws Exception {
        ExternalAudits.clear();
        for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getAuditDir().getName(), false)) {
            Snippets.getParent(getDs(), obj);
            if (obj.getParent() == getObject().getId()) {
                for (JsonObject ea : getDs().getObjects(ISO50001.getJc().getExternalAudit().getName(), false)) {
                    Snippets.getParent(getDs(), ea);
                    if (ea.getParent() == obj.getId()) {
                        ExternalAudit extaudit = new ExternalAudit(getDs(), ea);
                        ExternalAudits.add(extaudit);
                    }
                }
            }
        }
        return ExternalAudits;
    }

    public void setExternalAudits(List<ExternalAudit> ExternalAudits) {
        this.ExternalAudits = ExternalAudits;
    }

    public List<InternalAudit> getInternalAudits() throws Exception {
        InternalAudits.clear();
        for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getAuditDir().getName(), false)) {
            Snippets.getParent(getDs(), obj);
            if (obj.getParent() == getObject().getId()) {
                for (JsonObject ia : getDs().getObjects(ISO50001.getJc().getInternalAudit().getName(), false)) {
                    Snippets.getParent(getDs(), ia);
                    if (ia.getParent() == obj.getId()) {
                        InternalAudit intaudit = new InternalAudit(getDs(), ia);
                        InternalAudits.add(intaudit);
                    }
                }
            }
        }
        return InternalAudits;
    }

    public void setInternalAudits(List<InternalAudit> InternalAudits) {
        this.InternalAudits = InternalAudits;
    }

    public InternalAudit getInternalAudit(String name) throws Exception {
        for (InternalAudit ia : getInternalAudits()) {
            if (name.equals(ia.getName())) {
                return ia;
            } else {
                throw new JEVisException("Internal Audit not found!", 666);
            }
        }
        return null;
    }

    public List<LegalRegulation> getLegalRegulations() throws Exception {
        LegalRegulations.clear();

        for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getLegalRegulationDir().getName(), false)) {
            Snippets.getParent(getDs(), obj);
            if (obj.getParent() == getObject().getId()) {
                for (JsonObject m : getDs().getObjects(ISO50001.getJc().getLegalRegulation().getName(), false)) {
                    Snippets.getParent(getDs(), m);
                    if (m.getParent() == obj.getId()) {
                        LegalRegulation lr = new LegalRegulation(getDs(), m);
                        LegalRegulations.add(lr);
                    }
                }
            }
        }
        return LegalRegulations;
    }

    public void setLegalRegulations(List<LegalRegulation> LegalRegulations) {
        this.LegalRegulations = LegalRegulations;
    }

    public ManagementManualDirectory getManagementManualDirectory() throws Exception {
        for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getManagementManualDir().getName(), true)) {
            Snippets.getParent(getDs(), obj);
            if (obj.getParent() == getObject().getId()) {
                this.managementManualDirectory = new ManagementManualDirectory(getDs(), obj);
            }
        }
        return managementManualDirectory;
    }

    public void setManagementManualDirectory(ManagementManualDirectory managementManualDirectory) {
        this.managementManualDirectory = managementManualDirectory;
    }

    public ManagementReview getManagementReview(Long ID) throws Exception {
        ManagementReview mr = null;
        if (getObject() != null) {
            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getManagementReviewDir().getName(), false)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject m : getDs().getObjects(ISO50001.getJc().getManagementReview().getName(), false)) {
                        Snippets.getParent(getDs(), m);
                        if (m.getParent() == obj.getId()) {
                            if (Objects.equals(m.getId(), ID)) {
                                mr = new ManagementReview(getDs(), m);
                            }
                        }
                    }
                }
            }
        }
        return mr;
    }

    public List<ManagementReview> getManagementReviews() throws Exception {
        ManagementReviews.clear();
        for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getManagementReviewDir().getName(), false)) {
            Snippets.getParent(getDs(), obj);
            if (obj.getParent() == getObject().getId()) {
                for (JsonObject m : getDs().getObjects(ISO50001.getJc().getManagementReview().getName(), false)) {
                    Snippets.getParent(getDs(), m);
                    if (m.getParent() == obj.getId()) {
                        ManagementReview mr = new ManagementReview(getDs(), m);
                        ManagementReviews.add(mr);
                    }
                }
            }
        }
        return ManagementReviews;
    }

    public void setManagementReviews(List<ManagementReview> ManagementReviews) {
        this.ManagementReviews = ManagementReviews;
    }

    public List<ProceduralDocument> getProceduralDocuments() throws Exception {
        ProceduralDocuments.clear();

        for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getProceduralDocumentsDir().getName(), false)) {
            Snippets.getParent(getDs(), obj);
            if (obj.getParent() == getObject().getId()) {
                for (JsonObject m : getDs().getObjects(ISO50001.getJc().getProceduralDocument().getName(), false)) {
                    Snippets.getParent(getDs(), m);
                    if (m.getParent() == obj.getId()) {
                        ProceduralDocument pd = new ProceduralDocument(getDs(), m);
                        ProceduralDocuments.add(pd);
                    }
                }
            }
        }
        return ProceduralDocuments;
    }

    public void setProceduralDocuments(List<ProceduralDocument> ProceduralDocuments) {
        this.ProceduralDocuments = ProceduralDocuments;
    }

    public List<TrainingCourse> getTrainingCourses() throws Exception {
        TrainingCourses.clear();

        for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getTrainingCourseDir().getName(), false)) {
            Snippets.getParent(getDs(), obj);
            if (obj.getParent() == getObject().getId()) {
                for (JsonObject m : getDs().getObjects(ISO50001.getJc().getTrainingCourse().getName(), false)) {
                    Snippets.getParent(getDs(), m);
                    if (m.getParent() == obj.getId()) {
                        TrainingCourse tc = new TrainingCourse(getDs(), m);
                        TrainingCourses.add(tc);
                    }
                }
            }
        }
        return TrainingCourses;
    }

    public void setTrainingCourses(List<TrainingCourse> TrainingCourses) {
        this.TrainingCourses = TrainingCourses;
    }

    public List<Training> getTrainings() throws Exception {
        Trainings.clear();

        for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getTrainingDir().getName(), false)) {
            Snippets.getParent(getDs(), obj);
            if (obj.getParent() == getObject().getId()) {
                for (JsonObject m : getDs().getObjects(ISO50001.getJc().getTraining().getName(), false)) {
                    Snippets.getParent(getDs(), m);
                    if (m.getParent() == obj.getId()) {
                        Training t = new Training(getDs(), m);
                        Trainings.add(t);
                    }
                }
            }
        }
        return Trainings;
    }

    public void setTrainings(List<Training> Trainings) {
        this.Trainings = Trainings;
    }

    public List<String> getDirNames() throws JEVisException {
        List<String> output = new ArrayList<>();

        output.add(getAuditsDirName());
        output.add(getActionPlansName());
        output.add(getAnnouncementsName());
        output.add(getEnergyTeamMemberName());
        output.add(getLegalRegulationsName());
        output.add(getManagementReviewsName());
        output.add(getTrainingCoursesName());
        output.add(getTrainingsName());

        return output;
    }
}
