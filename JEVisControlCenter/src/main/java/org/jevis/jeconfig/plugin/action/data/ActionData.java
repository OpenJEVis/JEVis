package org.jevis.jeconfig.plugin.action.data;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.action.ActionPlugin;
import org.jevis.jeconfig.tool.AttributeFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class ActionData {


    public static DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
    private final SimpleStringProperty fromUser = new SimpleStringProperty("From User", I18n.getInstance().getString("plugin.action.fromuser"), "");
    private final SimpleIntegerProperty actionNr = new SimpleIntegerProperty("Nr", I18n.getInstance().getString("plugin.action.nr"), 0);
    private final SimpleStringProperty desciption = new SimpleStringProperty("Desciption", I18n.getInstance().getString("plugin.action.description"), "");
    private final SimpleStringProperty note = new SimpleStringProperty("Note", I18n.getInstance().getString("plugin.action.note"), "");
    private final SimpleObjectProperty<DateTime> createDate = new SimpleObjectProperty<>("Create Date", "Erstellt", (new DateTime()));
    private final SimpleObjectProperty<DateTime> plannedDate = new SimpleObjectProperty<>("Planned Date", "Umsetzung", (new DateTime()));
    private final SimpleObjectProperty<DateTime> doneDate = new SimpleObjectProperty<>("Done Date", "Abgeschlossen", null);
    //anhang, als file link
    private final SimpleStringProperty attachment = new SimpleStringProperty("Attachment", "Anhang", "");
    private final SimpleStringProperty title = new SimpleStringProperty("Title", I18n.getInstance().getString("plugin.action.title"), "");
    private final SimpleBooleanProperty isNeedAdditionalMeters = new SimpleBooleanProperty("Additional Meter", "Zusätzliche Messtechnik notwendig?", false);
    private final SimpleBooleanProperty isAffectsOtherProcess = new SimpleBooleanProperty("Affect Others", "Sind vor- oder nachgelagerte Prozesse betroffen?", false);
    private final SimpleBooleanProperty isConsumptionDocumented = new SimpleBooleanProperty("Consumption Documented", "Wird der Energieverbrauch gemessen und dokumentiert?", false);
    private final SimpleBooleanProperty isTargetReached = new SimpleBooleanProperty("Target Reached", "Wurde das Ziel der Maßnahme erreicht?", false);
    private final SimpleStringProperty isNewEnPI = new SimpleStringProperty("New EnPI", "Falls ja neue EnPI bitte angeben", "");
    private final SimpleBooleanProperty isNeedCorrection = new SimpleBooleanProperty("Document Correction", "Müssen Unterlagen geändert werden?", false);
    private final SimpleBooleanProperty isNeedAdditionalAction = new SimpleBooleanProperty("Additional Action", "Prüfanweisungen", false);
    private final SimpleBooleanProperty isNeedDocumentCorrection = new SimpleBooleanProperty("Document Correction", "Falls nein, Korrekturmaßname notwendig?", false);
    private final SimpleBooleanProperty isNeedProcessDocument = new SimpleBooleanProperty("Process Document", "Prozessanweisungen", false);
    private final SimpleBooleanProperty isNeedWorkInstruction = new SimpleBooleanProperty("isNeedWorkInstruction", "Arbeitsanweisungen", false);
    private final SimpleBooleanProperty isNeedTestInstruction = new SimpleBooleanProperty("Test Instruction", "Prüfanweisungen", false);
    private final SimpleBooleanProperty isNeedDrawing = new SimpleBooleanProperty("Drawing", "Prüfanweisungen", false);
    private final SimpleBooleanProperty isNeedOther = new SimpleBooleanProperty("Other", "Zeichnungen", false);
    private final SimpleStringProperty statusTags = new SimpleStringProperty("Status Tags", "Status", "Offen;In Bearbeitung");
    private final SimpleStringProperty fieldTags = new SimpleStringProperty("Field Tags", "Bereich", "Lager");
    private final SimpleStringProperty mediaTags = new SimpleStringProperty("Medium Tags", "Medium", "Strom");
    private final SimpleStringProperty noteCorrection = new SimpleStringProperty("Correction", "Korrekturmaßnahmen", "");
    private final SimpleStringProperty noteFollowUpAction = new SimpleStringProperty("Follow Action", "Folgemaßnahmen", "");
    private final SimpleStringProperty noteAlternativeMeasures = new SimpleStringProperty(
            "Alternative Measures", "Alternativmaßnahmen (falls die Einsparmaßnahme sich als nicht umsetzbar herausstellt)"
            , "");
    private final SimpleStringProperty responsible = new SimpleStringProperty("Responsible", "Verantwortlichkeit"
            , "");
    private final SimpleStringProperty noteBetroffenerProzess = new SimpleStringProperty("Affected Process Note", I18n.getInstance().getString("plugin.action.affectedprocess")
            , "");
    private final SimpleStringProperty noteEnergiefluss = new SimpleStringProperty(
            "Energy Flow Note", "Maßnahmenbeschreibung"
            , "");
    private final SimpleStringProperty noteBewertet = new SimpleStringProperty(
            "Evaluation Note", I18n.getInstance().getString("plugin.action.noteBewertet")
            , "");
    private final SimpleStringProperty enpiAfter = new SimpleStringProperty("EnPI After", I18n.getInstance().getString("plugin.action.enpiafter"), "");
    private final SimpleStringProperty enpiBefore = new SimpleStringProperty("EnPI After", I18n.getInstance().getString("plugin.action.enpiabefore"), "");
    private final SimpleStringProperty enpiChange = new SimpleStringProperty("EnPI After", I18n.getInstance().getString("plugin.action.enpiabechange"), "");
    private final SimpleStringProperty distributor = new SimpleStringProperty("Distributor", I18n.getInstance().getString("plugin.action.distributor"), "");
    private SimpleStringProperty investment = new SimpleStringProperty("Investment", I18n.getInstance().getString("plugin.action.investment"), "");
    private SimpleStringProperty savingyear = new SimpleStringProperty("Saving Year", I18n.getInstance().getString("plugin.action.savingyear"), "");
    private SimpleBooleanProperty valueChanged = new SimpleBooleanProperty(false);
    private ChangeListener changeListener;
    private JEVisObject object;

    public ActionData(JEVisObject obj) {
        this.object = obj;
        this.changeListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                valueChanged.set(true);
                //System.out.println("Value Changed:" + newValue.toString());
            }
        };
        reload();
    }

    public ActionData() {
    }

    public void reload() {
        //if (!valueChanged.getValue()) return;

        //TODO:
        //distributor

        registerChanges(fromUser);
        registerChanges(actionNr);
        registerChanges(statusTags);
        registerChanges(fieldTags);
        registerChanges(mediaTags);
        registerChanges(desciption);
        registerChanges(note);
        registerChanges(createDate);
        registerChanges(plannedDate);
        registerChanges(doneDate);
        registerChanges(attachment);

        registerChanges(noteCorrection);
        registerChanges(isNeedAdditionalMeters);
        registerChanges(isConsumptionDocumented);
        registerChanges(isTargetReached);
        registerChanges(isNewEnPI);
        registerChanges(isNeedAdditionalAction);
        registerChanges(isNeedDocumentCorrection);
        registerChanges(isNeedProcessDocument);
        registerChanges(isNeedTestInstruction);
        registerChanges(isNeedDrawing);
        registerChanges(isNeedOther);
        registerChanges(noteFollowUpAction);
        registerChanges(noteAlternativeMeasures);
        registerChanges(responsible);
        registerChanges(noteBewertet);
        registerChanges(noteEnergiefluss);
        registerChanges(noteBetroffenerProzess);
        registerChanges(title);

        valueChanged.set(false);
    }

    public JEVisObject getObject() {
        return object;
    }

    private void registerChanges(ReadOnlyProperty propertyObj) {
        propertyObj.removeListener(changeListener);
        propertyObj.addListener(changeListener);
        AttributeFactory.fillProperty(propertyObj, object);
    }

    public void commit() {
        try {
            if (!valueChanged.getValue()) return;


            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    try {
                        DateTime now = new DateTime();
                        AttributeFactory.commitAttribute(title, object, now);//->
                        AttributeFactory.commitAttribute(fromUser, object, now);
                        AttributeFactory.commitAttribute(actionNr, object, now);
                        AttributeFactory.commitAttribute(statusTags, object, now);
                        AttributeFactory.commitAttribute(fieldTags, object, now);
                        AttributeFactory.commitAttribute(mediaTags, object, now);

                        AttributeFactory.commitAttribute(desciption, object, now);
                        AttributeFactory.commitAttribute(note, object, now);
                        AttributeFactory.commitAttribute(createDate, object, now);
                        AttributeFactory.commitAttribute(plannedDate, object, now);
                        AttributeFactory.commitAttribute(doneDate, object, now);
                        //AttributeFactory.commitAttribute(attachment, object, now);

                        AttributeFactory.commitAttribute(noteCorrection, object, now);

                        AttributeFactory.commitAttributeIE(isNeedAdditionalMeters, object, now);
                        AttributeFactory.commitAttribute(isConsumptionDocumented, object, now);
                        AttributeFactory.commitAttribute(isTargetReached, object, now);
                        AttributeFactory.commitAttribute(isNewEnPI, object, now);
                        AttributeFactory.commitAttribute(isNeedAdditionalAction, object, now);
                        AttributeFactory.commitAttribute(isNeedDocumentCorrection, object, now);
                        AttributeFactory.commitAttribute(isNeedProcessDocument, object, now);
                        AttributeFactory.commitAttribute(isNeedTestInstruction, object, now);
                        AttributeFactory.commitAttribute(isNeedDrawing, object, now);
                        AttributeFactory.commitAttribute(isNeedOther, object, now);
                        AttributeFactory.commitAttribute(noteFollowUpAction, object, now);
                        AttributeFactory.commitAttribute(noteAlternativeMeasures, object, now);
                        AttributeFactory.commitAttribute(responsible, object, now);
                        AttributeFactory.commitAttribute(noteBewertet, object, now);
                        AttributeFactory.commitAttribute(noteEnergiefluss, object, now);
                        AttributeFactory.commitAttribute(noteBetroffenerProzess, object, now);
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

    public SimpleStringProperty fromUserProperty() {
        return fromUser;
    }


    public SimpleIntegerProperty actionNrProperty() {
        return actionNr;
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

    public SimpleStringProperty enpiChangeProperty() {
        return enpiChange;
    }

    public SimpleStringProperty distributorProperty() {
        return distributor;
    }

    public SimpleStringProperty investmentProperty() {
        return investment;
    }

    public SimpleStringProperty savingyearProperty() {
        return savingyear;
    }
}
