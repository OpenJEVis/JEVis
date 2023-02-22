package org.jevis.jeconfig.plugin.action.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.jevis.jeconfig.plugin.action.ActionPlugin;
import org.jevis.jeconfig.tool.gson.GsonBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class ActionData {

    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(ActionData.class);
    private static DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
    @Expose
    @SerializedName("From User")
    public final SimpleStringProperty fromUser = new SimpleStringProperty("From User", I18n.getInstance().getString("plugin.action.fromuser"), "");
    @Expose
    @SerializedName("Nr")
    public final SimpleIntegerProperty nr = new SimpleIntegerProperty("Nr", I18n.getInstance().getString("plugin.action.nr"), 0);
    @Expose
    @SerializedName("Desciption")
    public final SimpleStringProperty desciption = new SimpleStringProperty("Desciption", I18n.getInstance().getString("plugin.action.description"), "");
    @Expose
    @SerializedName("Note")
    public final SimpleStringProperty note = new SimpleStringProperty("Note", I18n.getInstance().getString("plugin.action.note"), "");
    @Expose
    @SerializedName("Create Date")
    public final SimpleObjectProperty<DateTime> createDate = new SimpleObjectProperty<>("Create Date", "Erstellt", (new DateTime()));
    @Expose
    @SerializedName("Planned Date")
    public final SimpleObjectProperty<DateTime> plannedDate = new SimpleObjectProperty<>("Planned Date", "Umsetzung", (new DateTime()));
    @Expose
    @SerializedName("Done Date")
    public final SimpleObjectProperty<DateTime> doneDate = new SimpleObjectProperty<>("Done Date", "Abgeschlossen", null);
    @Expose
    @SerializedName("Attachment")
    public final SimpleStringProperty attachment = new SimpleStringProperty("Attachment", "Anhang", "");

    @Expose
    @SerializedName("Title")
    public final SimpleStringProperty title = new SimpleStringProperty("Title", I18n.getInstance().getString("plugin.action.title"), "");
    @Expose
    @SerializedName("Additional Meter")
    public final SimpleBooleanProperty isNeedAdditionalMeters = new SimpleBooleanProperty("Additional Meter", "Zusätzliche Messtechnik notwendig?", false);
    @Expose
    @SerializedName("Affect Others")
    public final SimpleBooleanProperty isAffectsOtherProcess = new SimpleBooleanProperty("Affect Others", "Sind vor- oder nachgelagerte Prozesse betroffen?", false);
    @Expose
    @SerializedName("Consumption Documented")
    public final SimpleBooleanProperty isConsumptionDocumented = new SimpleBooleanProperty("Consumption Documented", "Wird der Energieverbrauch gemessen und dokumentiert?", false);
    @Expose
    @SerializedName("Target Reached")
    public final SimpleBooleanProperty isTargetReached = new SimpleBooleanProperty("Target Reached", "Wurde das Ziel der Maßnahme erreicht?", false);
    @Expose
    @SerializedName("New EnPI")
    public final SimpleStringProperty isNewEnPI = new SimpleStringProperty("New EnPI", "Falls ja neue EnPI bitte angeben", "");
    @Expose
    @SerializedName("Document Correction")
    public final SimpleBooleanProperty isNeedCorrection = new SimpleBooleanProperty("Document Correction", "Müssen Unterlagen geändert werden?", false);
    @Expose
    @SerializedName("Additional Action")
    public final SimpleBooleanProperty isNeedAdditionalAction = new SimpleBooleanProperty("Additional Action", "Prüfanweisungen", false);
    @Expose
    @SerializedName("Document Correction Text")
    public final SimpleBooleanProperty isNeedDocumentCorrection = new SimpleBooleanProperty("Document Correction Text", "Falls nein, Korrekturmaßname notwendig?", false);
    @Expose
    @SerializedName("Process Document")
    public final SimpleBooleanProperty isNeedProcessDocument = new SimpleBooleanProperty("Process Document", "Prozessanweisungen", false);
    @Expose
    @SerializedName("isNeedWorkInstruction")
    public final SimpleBooleanProperty isNeedWorkInstruction = new SimpleBooleanProperty("isNeedWorkInstruction", "Arbeitsanweisungen", false);
    @Expose
    @SerializedName("Test Instruction")
    public final SimpleBooleanProperty isNeedTestInstruction = new SimpleBooleanProperty("Test Instruction", "Prüfanweisungen", false);
    @Expose
    @SerializedName("Drawing")
    public final SimpleBooleanProperty isNeedDrawing = new SimpleBooleanProperty("Drawing", "Prüfanweisungen", false);
    @Expose
    @SerializedName("Other")
    public final SimpleBooleanProperty isNeedOther = new SimpleBooleanProperty("Other", "Zeichnungen", false);
    @Expose
    @SerializedName("Status Tags")
    public final SimpleStringProperty statusTags = new SimpleStringProperty("Status Tags", "Status", "Offen;In Bearbeitung");
    @Expose
    @SerializedName("Field Tags")
    public final SimpleStringProperty fieldTags = new SimpleStringProperty("Field Tags", "Bereich", "Lager");
    @Expose
    @SerializedName("Medium Tags")
    public final SimpleStringProperty mediaTags = new SimpleStringProperty("Medium Tags", "Medium", "Strom");
    @Expose
    @SerializedName("Correction")
    public final SimpleStringProperty noteCorrection = new SimpleStringProperty("Correction", "Korrekturmaßnahmen", "");
    @Expose
    @SerializedName("Follow Action")
    public final SimpleStringProperty noteFollowUpAction = new SimpleStringProperty("Follow Action", "Folgemaßnahmen", "");
    @Expose
    @SerializedName("Alternative Measures")
    public final SimpleStringProperty noteAlternativeMeasures = new SimpleStringProperty("Alternative Measures"
            , "Alternativmaßnahmen", "");
    @Expose
    @SerializedName("Responsible")
    public final SimpleStringProperty responsible = new SimpleStringProperty("Responsible", "Verantwortlichkeit", "");
    @Expose
    @SerializedName("Affected Process Note")
    public final SimpleStringProperty noteBetroffenerProzess = new SimpleStringProperty("Affected Process Note", I18n.getInstance().getString("plugin.action.affectedprocess"), "");
    @Expose
    @SerializedName("Energy Flow Note")
    public final SimpleStringProperty noteEnergiefluss = new SimpleStringProperty("Energy Flow Note", "Maßnahmenbeschreibung", "");
    @Expose
    @SerializedName("Evaluation Note")
    public final SimpleStringProperty noteBewertet = new SimpleStringProperty("Evaluation Note", I18n.getInstance().getString("plugin.action.noteBewertet"), "");
    @Expose
    @SerializedName("EnPI After")
    public final SimpleStringProperty enpiAfter = new SimpleStringProperty("EnPI After", I18n.getInstance().getString("plugin.action.enpiafter"), "");
    @Expose
    @SerializedName("EnPI Before")
    public final SimpleStringProperty enpiBefore = new SimpleStringProperty("EnPI Before", I18n.getInstance().getString("plugin.action.enpiabefore"), "");
    @Expose
    @SerializedName("EnPI Change")
    public final SimpleDoubleProperty enpiChange = new SimpleDoubleProperty("EnPI Change",
            I18n.getInstance().getString("plugin.action.enpiabechange"), 0);
    @Expose
    @SerializedName("Distributor")
    public final SimpleStringProperty distributor = new SimpleStringProperty("Distributor", I18n.getInstance().getString("plugin.action.distributor"), "");
    @Expose
    @SerializedName("Investment")
    public final SimpleStringProperty investment = new SimpleStringProperty("Investment", I18n.getInstance().getString("plugin.action.investment"), "");

    public final SimpleStringProperty DELETEsavingyear = new SimpleStringProperty("Saving Year",
            I18n.getInstance().getString("plugin.action.savingyear"), "");
    @Expose
    @SerializedName("EnPI Link")
    public final SimpleStringProperty enpilinks = new SimpleStringProperty("EnPI Link", I18n.getInstance().getString("plugin.action.enpilink"), "");
    @Expose
    @SerializedName("Consumption Actual")
    public final SimpleDoubleProperty consumptionActual = new SimpleDoubleProperty("Consumption Actual", I18n.getInstance().getString("plugin.action.consumptionactual"), 0d);
    @Expose
    @SerializedName("Consumption Diff")
    public final SimpleDoubleProperty consumptionDiff = new SimpleDoubleProperty("Consumption Diff",
            I18n.getInstance().getString("plugin.action.consumption.diff"), 0d);
    @Expose
    @SerializedName("Consumption Unit")
    public final SimpleStringProperty consumptionUnit = new SimpleStringProperty("Consumption Unit", I18n.getInstance().getString("plugin.action.consumptionunit"), "kWh");
    @Expose
    @SerializedName("Consumption Target")
    public final SimpleDoubleProperty consumptionTarget = new SimpleDoubleProperty("Consumption Target",
            I18n.getInstance().getString("plugin.action.consumptiontarget"), 0d);

    public final SimpleObjectProperty<NPVData> npv = new SimpleObjectProperty<>(new NPVData());
    public final SimpleBooleanProperty valueChanged = new SimpleBooleanProperty(false);
    private ChangeListener changeListener;
    private JEVisObject object;
    private ObjectNode dataNode = JsonNodeFactory.instance.objectNode();

    private List<ReadOnlyProperty> propertyList = new ArrayList<>();

    private ActionPlan actionPlan = null;

    public ActionData(ActionPlan actionPlan, JEVisObject obj) {
        this.object = obj;
        this.actionPlan = actionPlan;
        reload();
    }

    public ActionData() {
        reload();
    }

    public void setObject(JEVisObject object) {
        this.object = object;
    }

    public ActionPlan getActionPlan() {
        return actionPlan;
    }

    public void setActionPlan(ActionPlan actionPlan) {
        this.actionPlan = actionPlan;
    }

    public void reload() {

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
            registerChanges(fromUser, dataNode);
            registerChanges(nr, dataNode);
            registerChanges(statusTags, dataNode);
            registerChanges(fieldTags, dataNode);
            registerChanges(mediaTags, dataNode);
            registerChanges(desciption, dataNode);
            registerChanges(note, dataNode);
            registerChanges(createDate, dataNode);
            registerChanges(plannedDate, dataNode);
            registerChanges(doneDate, dataNode);
            registerChanges(attachment, dataNode);

            registerChanges(noteCorrection, dataNode);
            registerChanges(isNeedAdditionalMeters, dataNode);
            registerChanges(isConsumptionDocumented, dataNode);
            registerChanges(isTargetReached, dataNode);
            registerChanges(isNewEnPI, dataNode);
            registerChanges(isNeedAdditionalAction, dataNode);
            registerChanges(isNeedDocumentCorrection, dataNode);
            registerChanges(isNeedProcessDocument, dataNode);
            registerChanges(isNeedTestInstruction, dataNode);
            registerChanges(isNeedDrawing, dataNode);
            registerChanges(isNeedOther, dataNode);
            registerChanges(noteFollowUpAction, dataNode);
            registerChanges(noteAlternativeMeasures, dataNode);
            registerChanges(responsible, dataNode);
            registerChanges(noteBewertet, dataNode);
            registerChanges(noteEnergiefluss, dataNode);
            registerChanges(noteBetroffenerProzess, dataNode);
            registerChanges(title, dataNode);

            // registerChanges(DELETEsavingyear, dataNode);
            registerChanges(investment, dataNode);
            registerChanges(enpilinks, dataNode);
            valueChanged.set(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public JEVisObject getObject() {
        return object;
    }

    private void registerChanges(Object propertyObj, JsonNode jsonNode) {
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
            if (!valueChanged.getValue()) return;


            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    try {
                        try {
                            // System.out.println("Json:\n" + ActionData.this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataNode));

                            if (object != null) {
                                JEVisAttribute dataModel = object.getAttribute("Data");
                                Gson gson = GsonBuilder.createDefaultBuilder().create();
                                JEVisFileImp jsonFile = new JEVisFileImp(
                                        "DataModel_v2" + "_" + DateTime.now().toString("yyyyMMddHHmm") + ".json"
                                        , gson.toJson(this).getBytes(StandardCharsets.UTF_8));
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
            JEConfig.getStatusBar().addTask(ActionPlugin.class.getName(), task, widgetTaskIcon, true);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void delete() throws Exception {
        object.delete();
    }


    public SimpleStringProperty enpilinksProperty() {
        return enpilinks;
    }

    public SimpleStringProperty fromUserProperty() {
        return fromUser;
    }


    public SimpleIntegerProperty nrProperty() {
        return nr;
    }


    public SimpleStringProperty desciptionProperty() {
        return desciption;
    }

    public SimpleStringProperty noteProperty() {
        return note;
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


    public SimpleBooleanProperty isNeedAdditionalMetersProperty() {
        return isNeedAdditionalMeters;
    }


    public SimpleBooleanProperty isAffectsOtherProcessProperty() {
        return isAffectsOtherProcess;
    }


    public SimpleBooleanProperty isConsumptionDocumentedProperty() {
        return isConsumptionDocumented;
    }


    public SimpleBooleanProperty isTargetReachedProperty() {
        return isTargetReached;
    }

    public SimpleStringProperty isNewEnPIProperty() {
        return isNewEnPI;
    }


    public SimpleBooleanProperty isNeedCorrectionProperty() {
        return isNeedCorrection;
    }

    public SimpleBooleanProperty isNeedAdditionalActionProperty() {
        return isNeedAdditionalAction;
    }


    public SimpleBooleanProperty isNeedDocumentCorrectionProperty() {
        return isNeedDocumentCorrection;
    }

    public SimpleBooleanProperty isNeedProcessDocumentProperty() {
        return isNeedProcessDocument;
    }


    public SimpleBooleanProperty isNeedWorkInstructionProperty() {
        return isNeedWorkInstruction;
    }


    public SimpleBooleanProperty isNeedTestInstructionProperty() {
        return isNeedTestInstruction;
    }


    public SimpleBooleanProperty isNeedDrawingProperty() {
        return isNeedDrawing;
    }


    public SimpleBooleanProperty isNeedOtherProperty() {
        return isNeedOther;
    }

    public SimpleStringProperty statusTagsProperty() {
        return statusTags;
    }


    public SimpleStringProperty fieldTagsProperty() {
        return fieldTags;
    }

    public SimpleStringProperty mediaTagsProperty() {
        return mediaTags;
    }

    public SimpleObjectProperty<DateTime> doneDateProperty() {
        return doneDate;
    }

    public SimpleStringProperty noteCorrectionProperty() {
        return noteCorrection;
    }


    public SimpleStringProperty noteFollowUpActionProperty() {
        return noteFollowUpAction;
    }

    public SimpleStringProperty noteAlternativeMeasuresProperty() {
        return noteAlternativeMeasures;
    }

    public SimpleStringProperty responsibleProperty() {
        return responsible;
    }

    public SimpleStringProperty noteBetroffenerProzessProperty() {
        return noteBetroffenerProzess;
    }

    public SimpleStringProperty noteEnergieflussProperty() {
        return noteEnergiefluss;
    }

    public SimpleStringProperty noteBewertetProperty() {
        return noteBewertet;
    }

    public SimpleObjectProperty<DateTime> plannedDateProperty() {
        return plannedDate;
    }


    public SimpleStringProperty enpiAfterProperty() {
        return enpiAfter;
    }


    public SimpleStringProperty enpiBeforeProperty() {
        return enpiBefore;
    }

    public SimpleDoubleProperty enpiChangeProperty() {
        return enpiChange;
    }

    public SimpleStringProperty distributorProperty() {
        return distributor;
    }

    public SimpleStringProperty investmentProperty() {
        return investment;
    }

    public SimpleStringProperty DELETEsavingyearProperty() {
        return DELETEsavingyear;
    }


}
