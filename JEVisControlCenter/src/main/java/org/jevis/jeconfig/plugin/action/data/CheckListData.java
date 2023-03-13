package org.jevis.jeconfig.plugin.action.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class CheckListData {


    @Expose
    @SerializedName("Process Document")
    public final SimpleBooleanProperty isNeedProcessDocument = new SimpleBooleanProperty("Process Document",
            "Prozessanweisungen", false);

    @Expose
    @SerializedName("isNeedWorkInstruction")
    public final SimpleBooleanProperty isNeedWorkInstruction = new SimpleBooleanProperty("isNeedWorkInstruction",
            "Arbeitsanweisungen", false);

    @Expose
    @SerializedName("Test Instruction")
    public final SimpleBooleanProperty isNeedTestInstruction = new SimpleBooleanProperty("Test Instruction",
            "Prüfanweisungen", false);

    @Expose
    @SerializedName("Drawing")
    public final SimpleBooleanProperty isNeedDrawing = new SimpleBooleanProperty("Drawing",
            "Prüfanweisungen", false);
    @Expose
    @SerializedName("Other")
    public final SimpleBooleanProperty isNeedOther = new SimpleBooleanProperty("Other",
            "Zeichnungen", false);
    @Expose
    @SerializedName("Additional Action")
    public final SimpleBooleanProperty isNeedAdditionalAction = new SimpleBooleanProperty("Additional Action",
            "Prüfanweisungen", false);
    @Expose
    @SerializedName("Affect Others")
    public final SimpleBooleanProperty isAffectsOtherProcess = new SimpleBooleanProperty("Affect Others",
            "Sind vor- oder nachgelagerte Prozesse betroffen?", false);

    @Expose
    @SerializedName("Consumption Documented")
    public final SimpleBooleanProperty isConsumptionDocumented = new SimpleBooleanProperty("Consumption Documented",
            "Wird der Energieverbrauch gemessen und dokumentiert?", false);

    @Expose
    @SerializedName("Target Reached")
    public final SimpleBooleanProperty isTargetReached = new SimpleBooleanProperty("Target Reached",
            "Wurde das Ziel der Maßnahme erreicht?", false);

    @Expose
    @SerializedName("New EnPI")
    public final SimpleStringProperty isNewEnPI = new SimpleStringProperty("New EnPI",
            "Falls ja neue EnPI bitte angeben", "");

    @Expose
    @SerializedName("Document Correction Text")
    public final SimpleBooleanProperty isNeedDocumentCorrection = new SimpleBooleanProperty("Document Correction Text",
            "Falls nein, Korrekturmaßname notwendig?", false);
    @Expose
    @SerializedName("Document Correction")
    public final SimpleBooleanProperty isNeedCorrection = new SimpleBooleanProperty("Document Correction",
            "Müssen Unterlagen geändert werden?", false);
    @Expose
    @SerializedName("Additional Meter")
    public final SimpleBooleanProperty isNeedAdditionalMeters = new SimpleBooleanProperty("Additional Meter",
            "Zusätzliche Messtechnik notwendig?", false);


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


    public SimpleBooleanProperty isNeedAdditionalActionProperty() {
        return isNeedAdditionalAction;
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


    public SimpleBooleanProperty isNeedDocumentCorrectionProperty() {
        return isNeedDocumentCorrection;
    }


    public SimpleBooleanProperty isNeedCorrectionProperty() {
        return isNeedCorrection;
    }


    public SimpleBooleanProperty isNeedAdditionalMetersProperty() {
        return isNeedAdditionalMeters;
    }
}
