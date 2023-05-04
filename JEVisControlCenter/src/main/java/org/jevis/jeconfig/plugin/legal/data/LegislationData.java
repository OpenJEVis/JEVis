package org.jevis.jeconfig.plugin.legal.data;


import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.nonconformities.NonconformitiesPlugin;
import org.jevis.jeconfig.tool.gson.GsonBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class LegislationData {

    //private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(LegislationData.class);
    private static DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
    @Expose
    @SerializedName("title")
    public final SimpleStringProperty title = new SimpleStringProperty("title", I18n.getInstance().getString("plugin.Legalcadastre.legislation.title"), "");
    @Expose
    @SerializedName("Designation")
    public final SimpleStringProperty designation = new SimpleStringProperty("Designation", I18n.getInstance().getString("plugin.Legalcadastre.legislation.designation"), "");


    @Expose
    @SerializedName("Nr")
    private final SimpleIntegerProperty nr = new SimpleIntegerProperty("Nr", I18n.getInstance().getString("plugin.Legalcadastre.legislation.nr"), 0);
    @Expose
    @SerializedName("Description")
    private final SimpleStringProperty description = new SimpleStringProperty("Description", I18n.getInstance().getString("plugin.Legalcadastre.legislation.description"), "");
    @Expose
    @SerializedName("issue date")
    private final SimpleObjectProperty<DateTime> issueDate = new SimpleObjectProperty<>("issue date", I18n.getInstance().getString("plugin.Legalcadastre.legislation.issuedate"), null);

    @Expose
    @SerializedName("current version date")
    private final SimpleObjectProperty<DateTime> currentVersionDate = new SimpleObjectProperty<>("current version date", I18n.getInstance().getString("plugin.Legalcadastre.legislation.currentversiondate"), null);

    @Expose
    @SerializedName("Relevance 50001")
    private final BooleanProperty relevant = new SimpleBooleanProperty("Relevance 50001", I18n.getInstance().getString("plugin.Legalcadastre.legislation.relevance"), false);

    @Expose
    @SerializedName("Date of Examination")
    private final SimpleObjectProperty<DateTime> dateOfExamination = new SimpleObjectProperty<>("Date of Examination", I18n.getInstance().getString("plugin.Legalcadastre.legislation.dateofexamination"), null);


    @Expose
    @SerializedName("importance for the company")
    private final SimpleStringProperty importanceForTheCompany = new SimpleStringProperty("importance for the company", I18n.getInstance().getString("plugin.Legalcadastre.legislation.importanceforthecompany"), "");

    @Expose
    @SerializedName("link")
    private final SimpleStringProperty linkToVersion = new SimpleStringProperty("link", I18n.getInstance().getString("plugin.Legalcadastre.legislation.link"), "");


    @Expose
    @SerializedName("Deleted")
    private final SimpleBooleanProperty deleted = new SimpleBooleanProperty("Deleted", I18n.getInstance().getString("plugin.Legalcadastre.legislation.deleted"), false);


    @Expose
    @SerializedName("Attachment")
    private final SimpleStringProperty attachment = new SimpleStringProperty("Attachment", I18n.getInstance().getString("plugin.Legalcadastre.legislation.attachment"), "");

    private LegalCadastre legalCadastre;

    public final SimpleBooleanProperty valueChanged = new SimpleBooleanProperty(false);
    private ChangeListener changeListener;
    private JEVisObject object;

    private List<ReadOnlyProperty> propertyList = new ArrayList<>();


    public static final String IMMEDIATE_ACTION = I18n.getInstance().getString("plugin.nonconformities.error.immediatemeasures");
    public static final String DONE_DATE_ACTION = I18n.getInstance().getString("plugin.nonconformities.error.donedate");
    public static final String DONE_DATE_AFTER_NOW = I18n.getInstance().getString("plugin.nonconformities.error.donedateafter");
    public static final String REQUIREMENTS_MET = I18n.getInstance().getString("plugin.nonconforrmities.error.ok");

    public LegislationData(JEVisObject obj, LegalCadastre legalCadastre) {
        this.legalCadastre = legalCadastre;
        this.object = obj;
        reload();
    }

    public LegislationData() {
        reload();
    }

    public void setObject(JEVisObject object) {
        this.object = object;
    }

    public void reload() {

        if (this.changeListener == null) {
            this.changeListener = new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    valueChanged.set(true);
                    //System.out.println("Value Changed:" + newValue.toString());
                }
            };
        }


        try {
            propertyList = new ArrayList<>();


            registerChanges(title);
            registerChanges(designation);
            registerChanges(nr);
            registerChanges(description);
            registerChanges(issueDate);
            registerChanges(currentVersionDate);
            registerChanges(relevant);
            registerChanges(dateOfExamination);
            registerChanges(importanceForTheCompany);
            registerChanges(deleted);
            registerChanges(attachment);
            registerChanges(deleted);
            registerChanges(attachment);
            valueChanged.set(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public JEVisObject getObject() {
        return object;
    }

    private void registerChanges(Object propertyObj) {
        try {

            if (propertyObj instanceof ReadOnlyProperty) {
                ((ReadOnlyProperty) propertyObj).removeListener(changeListener);
                ((ReadOnlyProperty) propertyObj).addListener(changeListener);
                propertyList.add(((ReadOnlyProperty) propertyObj));
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    public void commit() {

        System.out.println(this);


        try {
            //if (!valueChanged.getValue()) return;

            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    try {
                        try {
                            Gson gson = GsonBuilder.createDefaultBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create();
                            logger.info("Json: {}", gson.toJson(LegislationData.this));


                            if (object != null) {
                                JEVisAttribute dataModel = object.getAttribute("Data");
                                JEVisFileImp jsonFile = new JEVisFileImp(
                                        "DataModel_v2" + "_" + DateTime.now().toString("yyyyMMddHHmm") + ".json"
                                        , gson.toJson(LegislationData.this).getBytes(StandardCharsets.UTF_8));
                                JEVisSample newSample = dataModel.buildSample(new DateTime(), jsonFile);
                                newSample.commit();
                            }

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    super.done();
                    return null;
                }
            };
            Image widgetTaskIcon = JEConfig.getImage("if_dashboard_46791.png");
            JEConfig.getStatusBar().addTask(NonconformitiesPlugin.class.getName(), task, widgetTaskIcon, true);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void delete() throws Exception {
        object.delete();
    }


    public String getTitle() {
        return title.get();
    }

    public SimpleStringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public String getDesignation() {
        return designation.get();
    }

    public SimpleStringProperty designationProperty() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation.set(designation);
    }

    public int getNr() {
        return nr.get();
    }

    public SimpleIntegerProperty nrProperty() {
        return nr;
    }

    public void setNr(int nr) {
        this.nr.set(nr);
    }

    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public DateTime getIssueDate() {
        return issueDate.get();
    }

    public SimpleObjectProperty<DateTime> issueDateProperty() {
        return issueDate;
    }

    public void setIssueDate(DateTime issueDate) {
        this.issueDate.set(issueDate);
    }

    public DateTime getCurrentVersionDate() {
        return currentVersionDate.get();
    }

    public SimpleObjectProperty<DateTime> currentVersionDateProperty() {
        return currentVersionDate;
    }

    public void setCurrentVersionDate(DateTime currentVersionDate) {
        this.currentVersionDate.set(currentVersionDate);
    }

    public boolean getRelevant() {
        return relevant.get();
    }

    public BooleanProperty relevantProperty() {
        return relevant;
    }

    public void setRelevant(boolean relevant) {
        this.relevant.set(relevant);
    }

    public DateTime getDateOfExamination() {
        return dateOfExamination.get();
    }

    public SimpleObjectProperty<DateTime> dateOfExaminationProperty() {
        return dateOfExamination;
    }

    public void setDateOfExamination(DateTime dateOfExamination) {
        this.dateOfExamination.set(dateOfExamination);
    }


    public boolean isDeleted() {
        return deleted.get();
    }

    public SimpleBooleanProperty deletedProperty() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted.set(deleted);
    }

    public String getAttachment() {
        return attachment.get();
    }

    public SimpleStringProperty attachmentProperty() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment.set(attachment);
    }

    public LegalCadastre getLegalCadastre() {
        return legalCadastre;
    }

    public void setLegalCadastre(LegalCadastre legalCadastre) {
        this.legalCadastre = legalCadastre;
    }

    public String getImportanceForTheCompany() {
        return importanceForTheCompany.get();
    }

    public SimpleStringProperty importanceForTheCompanyProperty() {
        return importanceForTheCompany;
    }

    public void setImportanceForTheCompany(String importanceForTheCompany) {
        this.importanceForTheCompany.set(importanceForTheCompany);
    }

    public String getLinkToVersion() {
        return linkToVersion.get();
    }

    public SimpleStringProperty linkToVersionProperty() {
        return linkToVersion;
    }

    public void setLinkToVersion(String linkToVersion) {
        this.linkToVersion.set(linkToVersion);
    }

    @Override
    public String toString() {
        return "LegislationData{" +
                "nr=" + nr +
                ", description=" + description +
                ", issueDate=" + issueDate +
                ", currentVersionDate=" + currentVersionDate +
                ", relevance=" + relevant +
                ", dateOfExamination=" + dateOfExamination +
                '}';
    }
}
