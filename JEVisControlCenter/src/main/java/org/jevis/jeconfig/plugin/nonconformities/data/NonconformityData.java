package org.jevis.jeconfig.plugin.nonconformities.data;


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


public class NonconformityData {

    //private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(NonconformityData.class);
    private static DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
    @Expose
    @SerializedName("Title")
    public final SimpleStringProperty title = new SimpleStringProperty("Title", I18n.getInstance().getString("plugin.nonconformities.title"), "test");
    @Expose
    @SerializedName("Creator")
    public final SimpleStringProperty creator = new SimpleStringProperty("Creator", I18n.getInstance().getString("plugin.nonconformities.creator"), "");


    @Expose
    @SerializedName("Nr")
    public final SimpleIntegerProperty nr = new SimpleIntegerProperty("Nr", I18n.getInstance().getString("plugin.nonconformities.nr"), 0);
    @Expose
    @SerializedName("Description")
    public final SimpleStringProperty description = new SimpleStringProperty("Description", I18n.getInstance().getString("plugin.nonconformities.description"), "");
    @Expose
    @SerializedName("Cause")
    public final SimpleStringProperty cause = new SimpleStringProperty("Cause", I18n.getInstance().getString("plugin.nonconformities.cause"), "");

    @Expose
    @SerializedName("Immediate Measures")
    public final SimpleStringProperty immediateMeasures = new SimpleStringProperty("Immediate Measures", I18n.getInstance().getString("plugin.nonconformities.immediatemeasures"), "");

    @Expose
    @SerializedName("Corrective Actions")
    public final SimpleStringProperty correctiveActions = new SimpleStringProperty("Corrective Actions", I18n.getInstance().getString("plugin.nonconformities.correctiveactions"), "");

    @Expose
    @SerializedName("Responsible Person")
    public final SimpleStringProperty responsiblePerson = new SimpleStringProperty("Responsible Person", I18n.getInstance().getString("plugin.nonconformities.responsibleperson"), "");

    @Expose
    @SerializedName("Immediate Action Required")
    public final SimpleBooleanProperty isImmediateActionRequired = new SimpleBooleanProperty("Immediate Action Required", I18n.getInstance().getString("plugin.nonconformities.immediateactionrequired"), false);
    @Expose
    @SerializedName("Effect on ongoing Processes")
    public final SimpleBooleanProperty isEffectOnOngoingProcesses = new SimpleBooleanProperty("Effect on ongoing Processes", I18n.getInstance().getString("plugin.nonconformities.effectonongoingprocesses"), false);
    @Expose
    @SerializedName("Routinely Affected")
    public final SimpleBooleanProperty isRoutinelyAffected = new SimpleBooleanProperty("Routinely Affected", I18n.getInstance().getString("plugin.nonconformities.routinelyaffected"), false);
    @Expose
    @SerializedName("Employee Trained")
    public final SimpleBooleanProperty isEmployeeTrained = new SimpleBooleanProperty("Employee Trained", I18n.getInstance().getString("plugin.nonconformities.employeetrained"), false);
    @Expose
    @SerializedName("Supplier Change Goods Needed")
    public final SimpleBooleanProperty isSupplierChangeGoodsNeeded = new SimpleBooleanProperty("Supplier Change Goods Needed", I18n.getInstance().getString("plugin.nonconformities.supplierchangegoodsneeded"), false);
    @Expose
    @SerializedName("Management Notification Needed")
    public final SimpleBooleanProperty isManagementNotificationNeeded = new SimpleBooleanProperty("Management Notification Needed", I18n.getInstance().getString("plugin.nonconformities.managementnotificationneeded"), false);

    @Expose
    @SerializedName("Documents Changes Needed")
    public final SimpleBooleanProperty isDocumentsChangesNeeded = new SimpleBooleanProperty("Documents Changes Needed", I18n.getInstance().getString("plugin.nonconformities.documentschangesneeded"), false);

    @Expose
    @SerializedName("Process Instructions")
    public final SimpleBooleanProperty isProcessInstructions = new SimpleBooleanProperty("Process Instructions", I18n.getInstance().getString("plugin.nonconformities.processinstructions"), false);


    @Expose
    @SerializedName("Work Instructions")
    public final SimpleBooleanProperty isWorkInstructions = new SimpleBooleanProperty("Work Instructions", I18n.getInstance().getString("plugin.nonconformities.workinstructions"), false);

    @Expose
    @SerializedName("Test Instructions")
    public final SimpleBooleanProperty isTestInstructions = new SimpleBooleanProperty("Test Instructions", I18n.getInstance().getString("plugin.nonconformities.testinstructions"), false);

    @Expose
    @SerializedName("Design")
    public final SimpleBooleanProperty isDesign = new SimpleBooleanProperty("Design", I18n.getInstance().getString("plugin.nonconformities.design"), false);

    @Expose
    @SerializedName("Model")
    public final SimpleBooleanProperty isModel = new SimpleBooleanProperty("Model", I18n.getInstance().getString("plugin.nonconforrmities.model"), false);

    @Expose
    @SerializedName("Miscellaneous")
    public final SimpleBooleanProperty isMiscellaneous = new SimpleBooleanProperty("Miscellaneous", I18n.getInstance().getString("plugin.nonconforrmities.miscellaneous"), false);


    @Expose
    @SerializedName("Create Date")
    public final SimpleObjectProperty<DateTime> createDate = new SimpleObjectProperty<>("Create Date", I18n.getInstance().getString("plugin.nonconforrmities.createdate"),  new DateTime());

    @Expose
    @SerializedName("Planned Date")
    public final SimpleObjectProperty<DateTime> plannedDate = new SimpleObjectProperty<>("Planned Date", I18n.getInstance().getString("plugin.nonconforrmities.planneddate"),null);


    @Expose
    @SerializedName("Done Date")
    public final SimpleObjectProperty<DateTime> doneDate = new SimpleObjectProperty<>("Done Date", I18n.getInstance().getString("plugin.nonconforrmities.donedate"), null);



    @Expose
    @SerializedName("Attachment")
    public final SimpleStringProperty attachment = new SimpleStringProperty("Attachment", I18n.getInstance().getString("plugin.nonconforrmities.attachment"), "");


    public final SimpleBooleanProperty valueChanged = new SimpleBooleanProperty(false);
    private ChangeListener changeListener;
    private JEVisObject object;

    private List<ReadOnlyProperty> propertyList = new ArrayList<>();

    public NonconformityData(JEVisObject obj) {
        this.object = obj;
        System.out.println("nr title: " + nr.getName());
        reload();
    }

    public NonconformityData() {
        System.out.println("nr title: " + nr.getName());
        reload();
    }

    public void setObject(JEVisObject object) {
        this.object = object;
    }

    public void reload() {
        //if (!valueChanged.getValue()) return;
        //dataNode = JsonNodeFactory.instance.objectNode();

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

            /*
            JEVisSample lastConfigSample = object.getAttribute("Data").getLatestSample();
            JEVisFile file = lastConfigSample.getValueAsFile();
            JsonNode dataNode = this.mapper.readTree(file.getBytes());


             */

            registerChanges(title);

            registerChanges(creator);
            registerChanges(nr);
            registerChanges(createDate);
            registerChanges(description);
            registerChanges(cause);
            registerChanges(isImmediateActionRequired);
            registerChanges(isEffectOnOngoingProcesses);
            registerChanges(isRoutinelyAffected);
            registerChanges(isEmployeeTrained);
            registerChanges(isSupplierChangeGoodsNeeded);
            registerChanges(isManagementNotificationNeeded);

            registerChanges(isDocumentsChangesNeeded);
            registerChanges(isProcessInstructions);
            registerChanges(isWorkInstructions);
            registerChanges(isTestInstructions);
            registerChanges(isDesign);
            registerChanges(isModel);
            registerChanges(isMiscellaneous);

            registerChanges(immediateMeasures);
            registerChanges(correctiveActions);
            registerChanges(responsiblePerson);
            registerChanges(plannedDate);
            registerChanges(doneDate);
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
        System.out.println("commit");
        System.out.println(this);
        System.out.println();
        try {
            if (!valueChanged.getValue()) return;


            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    try {
                        try {
                            System.out.println(this);
                            Gson gson = GsonBuilder.createDefaultBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create();
                            System.out.println("Json:\n" + gson.toJson(NonconformityData.this));



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


    public SimpleBooleanProperty isImmediateActionRequiredProperty() {
        return isImmediateActionRequired;
    }




    public SimpleObjectProperty<DateTime> doneDateProperty() {
        return doneDate;
    }



    public SimpleObjectProperty<DateTime> plannedDateProperty() {
        return plannedDate;
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

    public boolean isIsImmediateActionRequired() {
        return isImmediateActionRequired.get();
    }

    public void setIsImmediateActionRequired(boolean isImmediateActionRequired) {
        this.isImmediateActionRequired.set(isImmediateActionRequired);
    }

    public boolean isIsEffectOnOngoingProcesses() {
        return isEffectOnOngoingProcesses.get();
    }

    public SimpleBooleanProperty isEffectOnOngoingProcessesProperty() {
        return isEffectOnOngoingProcesses;
    }

    public void setIsEffectOnOngoingProcesses(boolean isEffectOnOngoingProcesses) {
        this.isEffectOnOngoingProcesses.set(isEffectOnOngoingProcesses);
    }

    public boolean isIsRoutinelyAffected() {
        return isRoutinelyAffected.get();
    }

    public SimpleBooleanProperty isRoutinelyAffectedProperty() {
        return isRoutinelyAffected;
    }

    public void setIsRoutinelyAffected(boolean isRoutinelyAffected) {
        this.isRoutinelyAffected.set(isRoutinelyAffected);
    }

    public boolean isIsEmployeeTrained() {
        return isEmployeeTrained.get();
    }

    public SimpleBooleanProperty isEmployeeTrainedProperty() {
        return isEmployeeTrained;
    }

    public void setIsEmployeeTrained(boolean isEmployeeTrained) {
        this.isEmployeeTrained.set(isEmployeeTrained);
    }

    public boolean isIsSupplierChangeGoodsNeeded() {
        return isSupplierChangeGoodsNeeded.get();
    }

    public SimpleBooleanProperty isSupplierChangeGoodsNeededProperty() {
        return isSupplierChangeGoodsNeeded;
    }

    public void setIsSupplierChangeGoodsNeeded(boolean isSupplierChangeGoodsNeeded) {
        this.isSupplierChangeGoodsNeeded.set(isSupplierChangeGoodsNeeded);
    }

    public boolean isIsManagementNotificationNeeded() {
        return isManagementNotificationNeeded.get();
    }

    public SimpleBooleanProperty isManagementNotificationNeededProperty() {
        return isManagementNotificationNeeded;
    }

    public void setIsManagementNotificationNeeded(boolean isManagementNotificationNeeded) {
        this.isManagementNotificationNeeded.set(isManagementNotificationNeeded);
    }

    public boolean isIsDocumentsChangesNeeded() {
        return isDocumentsChangesNeeded.get();
    }

    public SimpleBooleanProperty isDocumentsChangesNeededProperty() {
        return isDocumentsChangesNeeded;
    }

    public void setIsDocumentsChangesNeeded(boolean isDocumentsChangesNeeded) {
        this.isDocumentsChangesNeeded.set(isDocumentsChangesNeeded);
    }

    public boolean isIsProcessInstructions() {
        return isProcessInstructions.get();
    }

    public SimpleBooleanProperty isProcessInstructionsProperty() {
        return isProcessInstructions;
    }

    public void setIsProcessInstructions(boolean isProcessInstructions) {
        this.isProcessInstructions.set(isProcessInstructions);
    }

    public boolean isIsWorkInstructions() {
        return isWorkInstructions.get();
    }

    public SimpleBooleanProperty isWorkInstructionsProperty() {
        return isWorkInstructions;
    }

    public void setIsWorkInstructions(boolean isWorkInstructions) {
        this.isWorkInstructions.set(isWorkInstructions);
    }

    public boolean isIsTestInstructions() {
        return isTestInstructions.get();
    }

    public SimpleBooleanProperty isTestInstructionsProperty() {
        return isTestInstructions;
    }

    public void setIsTestInstructions(boolean isTestInstructions) {
        this.isTestInstructions.set(isTestInstructions);
    }

    public boolean isIsDesign() {
        return isDesign.get();
    }

    public SimpleBooleanProperty isDesignProperty() {
        return isDesign;
    }

    public void setIsDesign(boolean isDesign) {
        this.isDesign.set(isDesign);
    }

    public boolean isIsMiscellaneous() {
        return isMiscellaneous.get();
    }

    public SimpleBooleanProperty isMiscellaneousProperty() {
        return isMiscellaneous;
    }

    public void setIsMiscellaneous(boolean isMiscellaneous) {
        this.isMiscellaneous.set(isMiscellaneous);
    }

    public DateTime getCreateDate() {
        return createDate.get();
    }

    public void setCreateDate(DateTime createDate) {
        this.createDate.set(createDate);
    }

    public DateTime getPlannedDate() {
        return plannedDate.get();
    }

    public void setPlannedDate(DateTime plannedDate) {
        this.plannedDate.set(plannedDate);
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

    public boolean isIsModel() {
        return isModel.get();
    }

    public SimpleBooleanProperty isModelProperty() {
        return isModel;
    }

    public void setIsModel(boolean isModel) {
        this.isModel.set(isModel);
    }


    @Override
    public String toString() {
        return "NonconformityData{" +
                "title=" + title+
                ", fromUser=" + creator +
                ", nr=" + nr +
                ", description=" + description +
                ", cause=" + cause +
                ", immediateMeasures=" + immediateMeasures +
                ", correctiveActions=" + correctiveActions +
                ", responsiblePerson=" + responsiblePerson +
                ", isImmediateActionRequired=" + isImmediateActionRequired +
                ", isEffectOnOngoingProcesses=" + isEffectOnOngoingProcesses +
                ", isRoutinelyAffected=" + isRoutinelyAffected +
                ", isEmployeeTrained=" + isEmployeeTrained +
                ", isSupplierChangeGoodsNeeded=" + isSupplierChangeGoodsNeeded +
                ", isManagementNotificationNeeded=" + isManagementNotificationNeeded +
                ", isDocumentsChangesNeeded=" + isDocumentsChangesNeeded +
                ", isProcessInstructions=" + isProcessInstructions +
                ", isWorkInstructions=" + isWorkInstructions +
                ", isTestInstructions=" + isTestInstructions +
                ", isDesign=" + isDesign +
                ", isModel=" + isModel +
                ", isMiscellaneous=" + isMiscellaneous +
                ", createDate=" + createDate +
                ", plannedDate=" + plannedDate +
                ", doneDate=" + doneDate +
                ", attachment=" + attachment +
                '}';
    }
}
