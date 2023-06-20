package org.jevis.jeconfig.plugin.nonconformities.data;


import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.nonconformities.NonconformitiesPlugin;
import org.jevis.commons.gson.GsonBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class NonconformityData {

    //private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(NonconformityData.class);
    private static DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
    @Expose
    @SerializedName("Title")
    public final SimpleStringProperty title = new SimpleStringProperty("Title", I18n.getInstance().getString("plugin.nonconformities.title"), "");
    @Expose
    @SerializedName("Creator")
    public final SimpleStringProperty creator = new SimpleStringProperty("Creator", I18n.getInstance().getString("plugin.nonconformities.creator"), "");


    @Expose
    @SerializedName("Nr")
    private final SimpleIntegerProperty nr = new SimpleIntegerProperty("Nr", I18n.getInstance().getString("plugin.nonconformities.nr"), 0);
    @Expose
    @SerializedName("Description")
    private final SimpleStringProperty description = new SimpleStringProperty("Description", I18n.getInstance().getString("plugin.nonconformities.description"), "");
    @Expose
    @SerializedName("Cause")
    private final SimpleStringProperty cause = new SimpleStringProperty("Cause", I18n.getInstance().getString("plugin.nonconformities.cause"), "");

    @Expose
    @SerializedName("Immediate Measures")
    private final SimpleStringProperty immediateMeasures = new SimpleStringProperty("Immediate Measures", I18n.getInstance().getString("plugin.nonconformities.immediatemeasures"), "");

    @Expose
    @SerializedName("Corrective Actions")
    private final SimpleStringProperty correctiveActions = new SimpleStringProperty("Corrective Actions", I18n.getInstance().getString("plugin.nonconformities.correctiveactions"), "");

    @Expose
    @SerializedName("Responsible Person")
    private final SimpleStringProperty responsiblePerson = new SimpleStringProperty("Responsible Person", I18n.getInstance().getString("plugin.nonconformities.responsibleperson"), "");


    @Expose
    @SerializedName("Check List")
    private final SimpleObjectProperty<CheckListData> checkListData = new SimpleObjectProperty<>(new CheckListData());

    @Expose
    @SerializedName("Create Date")
    private final SimpleObjectProperty<DateTime> createDate = new SimpleObjectProperty<>("Create Date", I18n.getInstance().getString("plugin.nonconformities.createdate"), new DateTime());

    @Expose
    @SerializedName("Planned Date")
    private final SimpleObjectProperty<DateTime> deadLine = new SimpleObjectProperty<>("deadline", I18n.getInstance().getString("plugin.nonconformities.planneddate"), null);


    @Expose
    @SerializedName("Done Date")
    private final SimpleObjectProperty<DateTime> doneDate = new SimpleObjectProperty<>("Done Date", I18n.getInstance().getString("plugin.nonconformities.donedate"), null);

    @Expose
    @SerializedName("Deleted")
    private final SimpleBooleanProperty deleted = new SimpleBooleanProperty("Deleted", I18n.getInstance().getString("plugin.nonconformities.donedate"), false);


    @Expose
    @SerializedName("Attachment")
    private final SimpleStringProperty attachment = new SimpleStringProperty("Attachment", I18n.getInstance().getString("plugin.nonconformities.attachment"), "");


    @Expose
    @SerializedName("Medium")
    public final SimpleStringProperty medium = new SimpleStringProperty("Medium Tags",
            I18n.getInstance().getString("plugin.nonconformities.medium"), "Strom");

    @Expose
    @SerializedName("Field Tags")
    private final ListProperty<String> fieldTags = new SimpleListProperty<>("Field Tags",
            I18n.getInstance().getString("plugin.nonconformities.field"), FXCollections.observableArrayList());


    @Expose
    @SerializedName("Action")
    private final SimpleStringProperty action = new SimpleStringProperty("Field Tags",
            I18n.getInstance().getString("plugin.nonconformities.action"), "");

    @Expose
    @SerializedName("SEU")
    private final SimpleStringProperty seu = new SimpleStringProperty("Field Tags",
            I18n.getInstance().getString("plugin.nonconformities.seu"), "");


    public final SimpleBooleanProperty valueChanged = new SimpleBooleanProperty(false);
    private ChangeListener changeListener;
    private JEVisObject object;

    private List<ReadOnlyProperty> propertyList = new ArrayList<>();

    private NonconformityPlan nonconformityPlan;

    public static final String IMMEDIATE_ACTION = I18n.getInstance().getString("plugin.nonconformities.error.immediatemeasures");
    public static final String DONE_DATE_ACTION = I18n.getInstance().getString("plugin.nonconformities.error.donedate");
    public static final String DONE_DATE_AFTER_NOW = I18n.getInstance().getString("plugin.nonconformities.error.donedateafter");
    public static final String REQUIREMENTS_MET = I18n.getInstance().getString("plugin.nonconforrmities.error.ok");

    public NonconformityData(JEVisObject obj, NonconformityPlan nonconformityPlan) {
        this.nonconformityPlan = nonconformityPlan;
        this.object = obj;
        reload();
    }

    public NonconformityData() {
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
            registerChanges(creator);
            registerChanges(nr);
            registerChanges(description);
            registerChanges(cause);
            registerChanges(immediateMeasures);
            registerChanges(correctiveActions);
            registerChanges(responsiblePerson);
            registerChanges(createDate);
            registerChanges(deadLine);
            registerChanges(doneDate);
            registerChanges(deleted);
            registerChanges(attachment);
            registerChanges(medium);
            registerChanges(fieldTags);
            registerChanges(action);
            registerChanges(seu);


            registerChanges(getCheckListData().isImmediateActionRequiredProperty());
            registerChanges(getCheckListData().isEffectOnOngoingProcessesProperty());
            registerChanges(getCheckListData().isRoutinelyAffectedProperty());
            registerChanges(getCheckListData().isEmployeeTrainedProperty());
            registerChanges(getCheckListData().isDocumentsChangesNeededProperty());
            registerChanges(getCheckListData().isProcessInstructionsProperty());
            registerChanges(getCheckListData().isWorkInstructionsProperty());
            registerChanges(getCheckListData().isDesignProperty());
            registerChanges(getCheckListData().isModelProperty());
            registerChanges(getCheckListData().isMiscellaneousProperty());
            registerChanges(getCheckListData().isMetricsProperty());
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

    public String checkForRequirements() {
        if (getCheckListData().isIsImmediateActionRequired() && immediateMeasures.get().isEmpty()) {
            return IMMEDIATE_ACTION;
        } else if (getDoneDate() != null && getAction().isEmpty()) {
            return DONE_DATE_ACTION;
        } else if (getDoneDate() != null && getDoneDate().isAfter(DateTime.now())) {
            return DONE_DATE_AFTER_NOW;
        } else {
            return REQUIREMENTS_MET;
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
                            logger.info("Json: {}", gson.toJson(NonconformityData.this));


                            if (object != null) {
                                JEVisAttribute dataModel = object.getAttribute("Data");
                                JEVisFileImp jsonFile = new JEVisFileImp(
                                        "DataModel_v2" + "_" + DateTime.now().toString("yyyyMMddHHmm") + ".json"
                                        , gson.toJson(NonconformityData.this).getBytes(StandardCharsets.UTF_8));
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


    public SimpleStringProperty creatorProperty() {
        return creator;
    }


    public SimpleIntegerProperty nrProperty() {
        return nr;
    }


    public SimpleStringProperty descriptionProperty() {
        return description;
    }


    public SimpleObjectProperty<DateTime> createDateProperty() {
        return createDate;
    }


    public SimpleStringProperty attachmentProperty() {
        return attachment;
    }


    public SimpleStringProperty titleProperty() {
        return title;
    }


    public SimpleObjectProperty<DateTime> doneDateProperty() {
        return doneDate;
    }


    public SimpleObjectProperty<DateTime> deadLineProperty() {
        return deadLine;
    }


    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public String getCreator() {
        return creator.get();
    }

    public void setCreator(String creator) {
        this.creator.set(creator);
    }

    public int getNr() {
        return nr.get();
    }

    public void setNr(int nr) {
        this.nr.set(nr);
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public String getCause() {
        return cause.get();
    }

    public SimpleStringProperty causeProperty() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause.set(cause);
    }

    public String getImmediateMeasures() {
        return immediateMeasures.get();
    }

    public SimpleStringProperty immediateMeasuresProperty() {
        return immediateMeasures;
    }

    public void setImmediateMeasures(String immediateMeasures) {
        this.immediateMeasures.set(immediateMeasures);
    }

    public String getCorrectiveActions() {
        return correctiveActions.get();
    }

    public SimpleStringProperty correctiveActionsProperty() {
        return correctiveActions;
    }

    public void setCorrectiveActions(String correctiveActions) {
        this.correctiveActions.set(correctiveActions);
    }

    public String getResponsiblePerson() {
        return responsiblePerson.get();
    }

    public SimpleStringProperty responsiblePersonProperty() {
        return responsiblePerson;
    }

    public void setResponsiblePerson(String responsiblePerson) {
        this.responsiblePerson.set(responsiblePerson);
    }

    public DateTime getCreateDate() {
        return createDate.get();
    }

    public void setCreateDate(DateTime createDate) {
        this.createDate.set(createDate);
    }

    public DateTime getDeadLine() {
        return deadLine.get();
    }

    public void setDeadLine(DateTime deadLine) {
        this.deadLine.set(deadLine);
    }

    public DateTime getDoneDate() {
        return doneDate.get();
    }

    public void setDoneDate(DateTime doneDate) {
        this.doneDate.set(doneDate);
    }

    public String getAttachment() {
        return attachment.get();
    }

    public void setAttachment(String attachment) {
        this.attachment.set(attachment);
    }

    public boolean isValueChanged() {
        return valueChanged.get();
    }

    public SimpleBooleanProperty valueChangedProperty() {
        return valueChanged;
    }

    public void setValueChanged(boolean valueChanged) {
        this.valueChanged.set(valueChanged);
    }


    public String getMedium() {
        return medium.get();
    }

    public SimpleStringProperty mediumProperty() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium.set(medium);
    }

    public NonconformityPlan getNonconformityPlan() {
        return nonconformityPlan;
    }

    public void setNonconformityPlan(NonconformityPlan nonconformityPlan) {
        this.nonconformityPlan = nonconformityPlan;
    }

    public CheckListData getCheckListData() {
        return checkListData.get();
    }

    public SimpleObjectProperty<CheckListData> checkListDataProperty() {
        return checkListData;
    }

    public void setCheckListData(CheckListData checkListData) {
        this.checkListData.set(checkListData);
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

    public String getPrefix() {
        return getNonconformityPlan().getPrefix();
    }


    public List<String> getFieldTags() {
        return fieldTags.get();
    }

    public ListProperty<String> fieldTagsProperty() {
        return fieldTags;
    }


    public String getAction() {
        return action.get();
    }

    public SimpleStringProperty actionProperty() {
        return action;
    }

    public void setAction(String action) {
        this.action.set(action);
    }

    public String getSeu() {
        return seu.get();
    }

    public SimpleStringProperty seuProperty() {
        return seu;
    }

    public void setSeu(String seu) {
        this.seu.set(seu);
    }

    public SimpleStringProperty getPrefixProperty() {
        return getNonconformityPlan().prefix;
    }

    @Override
    public String toString() {
        return "NonconformityData{" +
                "title=" + title +
                ", creator=" + creator +
                ", nr=" + nr +
                ", description=" + description +
                ", cause=" + cause +
                ", immediateMeasures=" + immediateMeasures +
                ", correctiveActions=" + correctiveActions +
                ", responsiblePerson=" + responsiblePerson +
                ", checkListData=" + checkListData +
                ", createDate=" + createDate +
                ", deadLine=" + deadLine +
                ", doneDate=" + doneDate +
                ", deleted=" + deleted +
                ", attachment=" + attachment +
                ", medium=" + medium +
                ", fieldTags=" + fieldTags +
                ", action=" + action +
                ", seu=" + seu +
                ", valueChanged=" + valueChanged +
                '}';
    }

    public StringProperty getPrefixPlusNumber() {
        StringProperty stringProperty = new SimpleStringProperty();
        stringProperty.bind(Bindings.concat(getPrefixProperty(), nr));

        return stringProperty;

    }

    public StringProperty getfieldAsString() {


        StringProperty stringProperty = new SimpleStringProperty();
        stringProperty.bindBidirectional(fieldTags, new StringConverter<ObservableList<String>>() {
            @Override
            public String toString(ObservableList<String> strings) {
                return String.join(",", strings);
            }

            @Override
            public ObservableList<String> fromString(String s) {
                return FXCollections.observableArrayList(Arrays.asList(s.split(",")));
            }
        });


        return stringProperty;

    }


}
