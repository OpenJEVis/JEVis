package org.jevis.jeconfig.plugin.legal.data;


import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.gson.GsonBuilder;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.nonconformities.NonconformitiesPlugin;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ObligationData {

    //private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(ObligationData.class);
    private static DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
    @Expose
    @SerializedName("title")
    public final SimpleStringProperty title = new SimpleStringProperty("title", I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.title"), "");
    @Expose
    @SerializedName("Designation")
    public final SimpleStringProperty designation = new SimpleStringProperty("Designation", I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.designation"), "");
    public final SimpleBooleanProperty valueChanged = new SimpleBooleanProperty(false);
    @Expose
    @SerializedName("Nr")
    private final SimpleIntegerProperty nr = new SimpleIntegerProperty("Nr", I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.nr"), 0);
    @Expose
    @SerializedName("Description")
    private final SimpleStringProperty description = new SimpleStringProperty("Description", I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.description"), "");
    @Expose
    @SerializedName("issue date")
    private final SimpleObjectProperty<DateTime> issueDate = new SimpleObjectProperty<>("issue date", I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.issuedate"), null);
    @Expose
    @SerializedName("current version date")
    private final SimpleObjectProperty<DateTime> currentVersionDate = new SimpleObjectProperty<>("current version date", I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.currentversiondate"), null);
    @Expose
    @SerializedName("Relevance 50001")
    private final BooleanProperty relevant = new SimpleBooleanProperty("Relevance 50001", I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.relevance"), false);
    @Expose
    @SerializedName("Date of Examination")
    private final SimpleObjectProperty<DateTime> dateOfExamination = new SimpleObjectProperty<>("Date of Examination", I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.dateofexamination"), null);
    @Expose
    @SerializedName("importance for the company")
    private final SimpleStringProperty importanceForTheCompany = new SimpleStringProperty("importance for the company", I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.importanceforthecompany"), "");
    @Expose
    @SerializedName("link")
    private final SimpleStringProperty linkToVersion = new SimpleStringProperty("link", I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.link"), "");
    @Expose
    @SerializedName("category")
    private final SimpleStringProperty category = new SimpleStringProperty("category", I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.category"), "");
    @Expose
    @SerializedName("scope")
    private final SimpleStringProperty scope = new SimpleStringProperty("scope", I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.scope"), "");
    @Expose
    @SerializedName("Deleted")
    private final SimpleBooleanProperty deleted = new SimpleBooleanProperty("Deleted", I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.deleted"), false);
    @Expose
    @SerializedName("Attachment")
    private final SimpleStringProperty attachment = new SimpleStringProperty("Attachment", I18n.getInstance().getString("plugin.indexoflegalprovisions.obligation.attachment"), "");
    private IndexOfLegalProvisions indexOfLegalProvisions;
    private ChangeListener changeListener;
    private JEVisObject object;

    private List<ReadOnlyProperty> propertyList = new ArrayList<>();


    public ObligationData(JEVisObject obj, IndexOfLegalProvisions indexOfLegalProvisions) {
        this.indexOfLegalProvisions = indexOfLegalProvisions;
        this.object = obj;
        reload();
    }

    public ObligationData() {
        reload();
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

    public void setObject(JEVisObject object) {
        this.object = object;
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
        try {
            this.getObject().setName(String.valueOf(nr.getValue()));
            //Map<String, String> commitLangMap = new HashMap<>();
            // commitLangMap.put(I18n.getInstance().getLocale().getLanguage(), String.valueOf(nr.getValue()));


            //this.getObject().setLocalNames(commitLangMap);
            this.getObject().commit();

        } catch (JEVisException jeVisException) {
            jeVisException.printStackTrace();
        } catch (NumberFormatException numberFormatException) {
            numberFormatException.printStackTrace();
        }


        try {
            //if (!valueChanged.getValue()) return;

            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    try {
                        try {
                            Gson gson = GsonBuilder.createDefaultBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create();
                            logger.info("Json: {}", gson.toJson(ObligationData.this));


                            if (object != null) {
                                JEVisAttribute dataModel = object.getAttribute("Data");
                                JEVisFileImp jsonFile = new JEVisFileImp(
                                        "DataModel_v2" + "_" + DateTime.now().toString("yyyyMMddHHmm") + ".json"
                                        , gson.toJson(ObligationData.this).getBytes(StandardCharsets.UTF_8));
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

    public void setTitle(String title) {
        this.title.set(title);
    }

    public SimpleStringProperty titleProperty() {
        return title;
    }

    public String getDesignation() {
        return designation.get();
    }

    public void setDesignation(String designation) {
        this.designation.set(designation);
    }

    public SimpleStringProperty designationProperty() {
        return designation;
    }

    public int getNr() {
        return nr.get();
    }

    public void setNr(int nr) {
        this.nr.set(nr);
    }

    public SimpleIntegerProperty nrProperty() {
        return nr;
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public DateTime getIssueDate() {
        return issueDate.get();
    }

    public void setIssueDate(DateTime issueDate) {
        this.issueDate.set(issueDate);
    }

    public SimpleObjectProperty<DateTime> issueDateProperty() {
        return issueDate;
    }

    public DateTime getCurrentVersionDate() {
        return currentVersionDate.get();
    }

    public void setCurrentVersionDate(DateTime currentVersionDate) {
        this.currentVersionDate.set(currentVersionDate);
    }

    public SimpleObjectProperty<DateTime> currentVersionDateProperty() {
        return currentVersionDate;
    }

    public boolean getRelevant() {
        return relevant.get();
    }

    public void setRelevant(boolean relevant) {
        this.relevant.set(relevant);
    }

    public BooleanProperty relevantProperty() {
        return relevant;
    }

    public DateTime getDateOfExamination() {
        return dateOfExamination.get();
    }

    public void setDateOfExamination(DateTime dateOfExamination) {
        this.dateOfExamination.set(dateOfExamination);
    }

    public SimpleObjectProperty<DateTime> dateOfExaminationProperty() {
        return dateOfExamination;
    }

    public boolean isDeleted() {
        return deleted.get();
    }

    public void setDeleted(boolean deleted) {
        this.deleted.set(deleted);
    }

    public SimpleBooleanProperty deletedProperty() {
        return deleted;
    }

    public String getAttachment() {
        return attachment.get();
    }

    public void setAttachment(String attachment) {
        this.attachment.set(attachment);
    }

    public SimpleStringProperty attachmentProperty() {
        return attachment;
    }

    public IndexOfLegalProvisions getLegalCadastre() {
        return indexOfLegalProvisions;
    }

    public void setLegalCadastre(IndexOfLegalProvisions indexOfLegalProvisions) {
        this.indexOfLegalProvisions = indexOfLegalProvisions;
    }

    public String getImportanceForTheCompany() {
        return importanceForTheCompany.get();
    }

    public void setImportanceForTheCompany(String importanceForTheCompany) {
        this.importanceForTheCompany.set(importanceForTheCompany);
    }

    public SimpleStringProperty importanceForTheCompanyProperty() {
        return importanceForTheCompany;
    }

    public String getLinkToVersion() {
        return linkToVersion.get();
    }

    public void setLinkToVersion(String linkToVersion) {
        this.linkToVersion.set(linkToVersion);
    }

    public SimpleStringProperty linkToVersionProperty() {
        return linkToVersion;
    }

    @Override
    public String toString() {
        return "obligationData{" +
                "nr=" + nr +
                ", description=" + description +
                ", issueDate=" + issueDate +
                ", currentVersionDate=" + currentVersionDate +
                ", relevance=" + relevant +
                ", dateOfExamination=" + dateOfExamination +
                '}';
    }

    public String getCategory() {
        return category.get();
    }

    public void setCategory(String category) {
        this.category.set(category);
    }

    public SimpleStringProperty categoryProperty() {
        return category;
    }

    public String getScope() {
        return scope.get();
    }

    public void setScope(String scope) {
        this.scope.set(scope);
    }

    public SimpleStringProperty scopeProperty() {
        return scope;
    }

    public StringProperty getNrAsStringProperty() {
        StringProperty stringProperty = new SimpleStringProperty();
        stringProperty.bind(Bindings.createStringBinding(() -> String.format("%02d", nrProperty().get()), nrProperty()));

        return stringProperty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObligationData that = (ObligationData) o;
        return Objects.equals(title, that.title) && Objects.equals(designation, that.designation) && Objects.equals(nr, that.nr) && Objects.equals(description, that.description) && Objects.equals(issueDate, that.issueDate) && Objects.equals(currentVersionDate, that.currentVersionDate) && Objects.equals(relevant, that.relevant) && Objects.equals(dateOfExamination, that.dateOfExamination) && Objects.equals(importanceForTheCompany, that.importanceForTheCompany) && Objects.equals(linkToVersion, that.linkToVersion) && Objects.equals(category, that.category) && Objects.equals(scope, that.scope) && Objects.equals(deleted, that.deleted) && Objects.equals(attachment, that.attachment) && Objects.equals(indexOfLegalProvisions, that.indexOfLegalProvisions) && Objects.equals(valueChanged, that.valueChanged) && Objects.equals(changeListener, that.changeListener) && Objects.equals(object, that.object) && Objects.equals(propertyList, that.propertyList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, designation, nr, description, issueDate, currentVersionDate, relevant, dateOfExamination, importanceForTheCompany, linkToVersion, category, scope, deleted, attachment, indexOfLegalProvisions, valueChanged, changeListener, object, propertyList);
    }
}
