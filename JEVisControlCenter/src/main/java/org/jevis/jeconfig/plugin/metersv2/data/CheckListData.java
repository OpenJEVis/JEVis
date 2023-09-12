package org.jevis.jeconfig.plugin.metersv2.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.beans.property.SimpleBooleanProperty;
import org.jevis.commons.i18n.I18n;

public class CheckListData {
    @Expose
    @SerializedName("Immediate Action Required")
    private final SimpleBooleanProperty isImmediateActionRequired = new SimpleBooleanProperty("Immediate Action Required", I18n.getInstance().getString("plugin.nonconformities.immediateactionrequired"), false);

    @Expose
    @SerializedName("Effect on ongoing Processes")
    private final SimpleBooleanProperty isEffectOnOngoingProcesses = new SimpleBooleanProperty("Effect on ongoing Processes", I18n.getInstance().getString("plugin.nonconformities.effectonongoingprocesses"), false);
    @Expose
    @SerializedName("Routinely Affected")
    private final SimpleBooleanProperty isRoutinelyAffected = new SimpleBooleanProperty("Routinely Affected", I18n.getInstance().getString("plugin.nonconformities.routinelyaffected"), false);
    @Expose
    @SerializedName("Employee Trained")
    private final SimpleBooleanProperty isEmployeeTrained = new SimpleBooleanProperty("Employee Trained", I18n.getInstance().getString("plugin.nonconformities.employeetrained"), false);

    @Expose
    @SerializedName("Documents Changes Needed")
    private final SimpleBooleanProperty isDocumentsChangesNeeded = new SimpleBooleanProperty("Documents Changes Needed", I18n.getInstance().getString("plugin.nonconformities.documentschangesneeded"), false);

    @Expose
    @SerializedName("Process Instructions")
    private final SimpleBooleanProperty isProcessInstructions = new SimpleBooleanProperty("Process Instructions", I18n.getInstance().getString("plugin.nonconformities.processinstructions"), false);


    @Expose
    @SerializedName("Work Instructions")
    private final SimpleBooleanProperty isWorkInstructions = new SimpleBooleanProperty("Work Instructions", I18n.getInstance().getString("plugin.nonconformities.workinstructions"), false);

    @Expose
    @SerializedName("Test Instructions")
    private final SimpleBooleanProperty isTestInstructions = new SimpleBooleanProperty("Test Instructions", I18n.getInstance().getString("plugin.nonconformities.testinstructions"), false);

    @Expose
    @SerializedName("Design")
    private final SimpleBooleanProperty isDesign = new SimpleBooleanProperty("Design", I18n.getInstance().getString("plugin.nonconformities.design"), false);

    @Expose
    @SerializedName("Model")
    private final SimpleBooleanProperty isModel = new SimpleBooleanProperty("Model", I18n.getInstance().getString("plugin.nonconformities.model"), false);

    @Expose
    @SerializedName("Miscellaneous")
    private final SimpleBooleanProperty isMiscellaneous = new SimpleBooleanProperty("Miscellaneous", I18n.getInstance().getString("plugin.nonconformities.miscellaneous"), false);

    @Expose
    @SerializedName("Metrics")
    private final SimpleBooleanProperty isMetrics = new SimpleBooleanProperty("Immediate Action Required", I18n.getInstance().getString("plugin.nonconformities.metrics"), false);


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

    public boolean isIsModel() {
        return isModel.get();
    }

    public SimpleBooleanProperty isModelProperty() {
        return isModel;
    }

    public void setIsModel(boolean isModel) {
        this.isModel.set(isModel);
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

    public boolean isIsImmediateActionRequired() {
        return isImmediateActionRequired.get();
    }

    public SimpleBooleanProperty isImmediateActionRequiredProperty() {
        return isImmediateActionRequired;
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

    public boolean isIsDocumentsChangesNeeded() {
        return isDocumentsChangesNeeded.get();
    }

    public SimpleBooleanProperty isDocumentsChangesNeededProperty() {
        return isDocumentsChangesNeeded;
    }

    public void setIsDocumentsChangesNeeded(boolean isDocumentsChangesNeeded) {
        this.isDocumentsChangesNeeded.set(isDocumentsChangesNeeded);
    }

    public boolean isIsMetrics() {
        return isMetrics.get();
    }

    public SimpleBooleanProperty isMetricsProperty() {
        return isMetrics;
    }

    public void setIsMetrics(boolean isMetrics) {
        this.isMetrics.set(isMetrics);
    }
}
