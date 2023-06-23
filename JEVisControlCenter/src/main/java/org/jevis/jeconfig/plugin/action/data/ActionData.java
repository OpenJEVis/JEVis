package org.jevis.jeconfig.plugin.action.data;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.gson.GsonBuilder;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.action.ActionPlugin;
import org.joda.time.DateTime;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class ActionData {

    private static final Logger logger = LogManager.getLogger(ActionData.class);
    @Expose
    @SerializedName("NPV")
    public final SimpleObjectProperty<NPVData> npv = new SimpleObjectProperty<>(new NPVData());
    @Expose
    @SerializedName("From User")
    public final SimpleStringProperty fromUser = new SimpleStringProperty("From User",
            I18n.getInstance().getString("plugin.action.fromuser"), "");
    @Expose
    @SerializedName("Nr")
    public final SimpleIntegerProperty nr = new SimpleIntegerProperty("Nr",
            I18n.getInstance().getString("plugin.action.nr"), 0);
    @Expose
    @SerializedName("Desciption")
    public final SimpleStringProperty desciption = new SimpleStringProperty("Desciption",
            I18n.getInstance().getString("plugin.action.description"), "");
    @Expose
    @SerializedName("Note")
    public final SimpleStringProperty note = new SimpleStringProperty("Note",
            I18n.getInstance().getString("plugin.action.note"), "");
    @Expose
    @SerializedName("Create Date")
    public final SimpleObjectProperty<DateTime> createDate = new SimpleObjectProperty<>("Create Date",
            "Erstellt", (new DateTime()));
    @Expose
    @SerializedName("Planned Date")
    public final SimpleObjectProperty<DateTime> plannedDate = new SimpleObjectProperty<>("Planned Date",
            "Umsetzung", (new DateTime()));
    @Expose
    @SerializedName("Done Date")
    public final SimpleObjectProperty<DateTime> doneDate = new SimpleObjectProperty<>("Done Date",
            I18n.getInstance().getString("plugin.action.donedate"), null);
    @Expose
    @SerializedName("Attachment")
    public final SimpleStringProperty attachment = new SimpleStringProperty("Attachment",
            "Anhang", "");
    @Expose
    @SerializedName("Title")
    public final SimpleStringProperty title = new SimpleStringProperty("Title",
            I18n.getInstance().getString("plugin.action.title"), "");
    @Expose
    @SerializedName("Status Tags")
    public final SimpleStringProperty statusTags = new SimpleStringProperty("Status Tags",
            "Status", ActionPlanData.STATUS_OPEN);
    @Expose
    @SerializedName("Field Tags")
    public final SimpleStringProperty fieldTags = new SimpleStringProperty("Field Tags",
            "Bereich", "Strom");
    @Expose
    @SerializedName("Medium Tags")
    public final SimpleStringProperty mediaTags = new SimpleStringProperty("Medium Tags",
            "Medium", "");
    @Expose
    @SerializedName("Correction")
    public final SimpleStringProperty noteCorrection = new SimpleStringProperty("Correction",
            "Korrekturmaßnahmen", "");
    @Expose
    @SerializedName("Follow Action")
    public final SimpleStringProperty noteFollowUpAction = new SimpleStringProperty("Follow Action",
            "Folgemaßnahmen", "");
    @Expose
    @SerializedName("Alternative Measures")
    public final SimpleStringProperty noteAlternativeMeasures = new SimpleStringProperty("Alternative Measures"
            , "Alternativmaßnahmen", "");
    @Expose
    @SerializedName("Responsible")
    public final SimpleStringProperty responsible = new SimpleStringProperty("Responsible",
            "Verantwortlichkeit", "");
    @Expose
    @SerializedName("Affected Process Note")
    public final SimpleStringProperty noteBetroffenerProzess = new SimpleStringProperty("Affected Process Note",
            I18n.getInstance().getString("plugin.action.affectedprocess"), "");
    @Expose
    @SerializedName("Energy Flow Note")
    public final SimpleStringProperty noteEnergiefluss = new SimpleStringProperty("Energy Flow Note",
            "Maßnahmenbeschreibung", "");
    @Expose
    @SerializedName("Evaluation Note")
    public final SimpleStringProperty noteBewertet = new SimpleStringProperty("Evaluation Note",
            I18n.getInstance().getString("plugin.action.noteBewertet"), "");
    @Expose
    @SerializedName("Distributor")
    public final SimpleStringProperty distributor = new SimpleStringProperty("Distributor",
            I18n.getInstance().getString("plugin.action.distributor"), "");
    @Expose
    @SerializedName("EnpI")
    public final SimpleObjectProperty<ConsumptionData> enpi = new SimpleObjectProperty<>(new ConsumptionData());
    @Expose
    @SerializedName("Consumption")
    public final SimpleObjectProperty<ConsumptionData> consumption = new SimpleObjectProperty<>(new ConsumptionData());
    public final SimpleBooleanProperty valueChanged = new SimpleBooleanProperty(false);
    @Expose
    @SerializedName("SEU Tags")
    private final SimpleObjectProperty<String> seuTags = new SimpleObjectProperty<>("");
    private final StringProperty nrText = new SimpleStringProperty();
    @Expose
    @SerializedName("Check List")
    private final SimpleObjectProperty<CheckListData> checkListData = new SimpleObjectProperty<>(new CheckListData());
    @Expose
    @SerializedName("Deleted")
    private final SimpleBooleanProperty isDeleted = new SimpleBooleanProperty(false);
    private final Gson gson = GsonBuilder.createDefaultBuilder().create();
    private String originalSettings = "";
    private ChangeListener changeListener;
    private JEVisObject object;
    private final ObjectNode dataNode = JsonNodeFactory.instance.objectNode();

    private final List<ReadOnlyProperty> propertyList = new ArrayList<>();

    private ActionPlanData actionPlan = null;
    private boolean isNew = false;


    public ActionData(ActionPlanData actionPlan, JEVisObject obj) {
        this.object = obj;
        this.isNew = true;
        setActionPlan(actionPlan);
        update();
    }

    public ActionData() {
    }

    public void update() {
        //System.out.println("-ActionData.update: " + this);
        nr.addListener((observable, oldValue, newValue) -> updateNrText());
        originalSettings = gson.toJson(this);
        consumption.get().update();
        enpi.get().setEnPI(true);
        enpi.get().update();
        npv.get().update();
    }

    public ActionPlanData getActionPlan() {
        return actionPlan;
    }

    public void setActionPlan(ActionPlanData actionPlan) {
        this.actionPlan = actionPlan;
        actionPlan.nrPrefixProperty().addListener((observable, oldValue, newValue) -> updateNrText());

        updateNrText();
    }

    private void updateNrText() {
        nrText.set(String.format("%s %03d", actionPlan.getNrPrefix(), nrProperty().get()));
    }

    public String getNrText() {
        return nrText.get();
    }

    public StringProperty nrTextProperty() {
        return nrText;
    }

    public JEVisObject getObject() {
        return object;
    }

    public void setObject(JEVisObject object) {
        this.object = object;
    }

    public boolean hasChanged() {
        // if (valueChanged.getValue()) return true;

        if (isDeletedProperty().get() || originalSettings == null || !gson.toJson(this).equals(originalSettings)) {
            System.out.println("gson change");
            return true;
        }


        return false;
    }

    public void commit() {
        try {
            System.out.println("ActonData.commit: " + nr.get() + " changes: " + valueChanged.getValue());
            if (!hasChanged()) return;

            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    try {
                        try {
                            // System.out.println("Json:\n" + ActionData.this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataNode));

                            if (object != null) {
                                JEVisAttribute dataModel = object.getAttribute("Data");

                                JEVisFileImp jsonFile = new JEVisFileImp(
                                        "DataModel_v2" + "_" + DateTime.now().toString("yyyyMMddHHmm") + ".json"
                                        , gson.toJson(ActionData.this).getBytes(StandardCharsets.UTF_8));
                                System.out.println("Json to commit: " + gson.toJson(ActionData.this));

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
        // object.delete();
        isDeleted.set(true);
        commit();
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

    public SimpleStringProperty distributorProperty() {
        return distributor;
    }

    /*
    public SimpleStringProperty investmentProperty() {
        return investment;
    }

    public SimpleStringProperty DELETEsavingyearProperty() {
        return DELETEsavingyear;
    }

     */

    public CheckListData getCheckListData() {
        return checkListData.get();
    }

    public SimpleObjectProperty<CheckListData> checkListDataProperty() {
        return checkListData;
    }

    public ConsumptionData getEnpi() {
        return enpi.get();
    }

    public SimpleObjectProperty<ConsumptionData> enpiProperty() {
        return enpi;
    }


    public ConsumptionData getConsumption() {
        return consumption.get();
    }

    public SimpleObjectProperty<ConsumptionData> consumptionProperty() {
        return consumption;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public SimpleBooleanProperty isDeletedProperty() {
        return isDeleted;
    }

    public SimpleObjectProperty<String> seuTagsProperty() {
        return seuTags;
    }

}
