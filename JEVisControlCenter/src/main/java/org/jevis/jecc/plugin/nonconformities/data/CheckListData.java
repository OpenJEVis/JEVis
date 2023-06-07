package org.jevis.jecc.plugin.nonconformities.data;

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

    public void setIsProcessInstructions(boolean isProcessInstructions) {
        this.isProcessInstructions.set(isProcessInstructions);
    }

    public SimpleBooleanProperty isProcessInstructionsProperty() {
        return isProcessInstructions;
    }

    public boolean isIsWorkInstructions() {
        return isWorkInstructions.get();
    }

    public void setIsWorkInstructions(boolean isWorkInstructions) {
        this.isWorkInstructions.set(isWorkInstructions);
    }

    public SimpleBooleanProperty isWorkInstructionsProperty() {
        return isWorkInstructions;
    }

    public boolean isIsTestInstructions() {
        return isTestInstructions.get();
    }

    public void setIsTestInstructions(boolean isTestInstructions) {
        this.isTestInstructions.set(isTestInstructions);
    }

    public SimpleBooleanProperty isTestInstructionsProperty() {
        return isTestInstructions;
    }

    public boolean isIsDesign() {
        return isDesign.get();
    }

    public void setIsDesign(boolean isDesign) {
        this.isDesign.set(isDesign);
    }

    public SimpleBooleanProperty isDesignProperty() {
        return isDesign;
    }

    public boolean isIsModel() {
        return isModel.get();
    }

    public void setIsModel(boolean isModel) {
        this.isModel.set(isModel);
    }

    public SimpleBooleanProperty isModelProperty() {
        return isModel;
    }

    public boolean isIsMiscellaneous() {
        return isMiscellaneous.get();
    }

    public void setIsMiscellaneous(boolean isMiscellaneous) {
        this.isMiscellaneous.set(isMiscellaneous);
    }

    public SimpleBooleanProperty isMiscellaneousProperty() {
        return isMiscellaneous;
    }

    public boolean isIsImmediateActionRequired() {
        return isImmediateActionRequired.get();
    }

    public void setIsImmediateActionRequired(boolean isImmediateActionRequired) {
        this.isImmediateActionRequired.set(isImmediateActionRequired);
    }

    public SimpleBooleanProperty isImmediateActionRequiredProperty() {
        return isImmediateActionRequired;
    }

    public boolean isIsEffectOnOngoingProcesses() {
        return isEffectOnOngoingProcesses.get();
    }

    public void setIsEffectOnOngoingProcesses(boolean isEffectOnOngoingProcesses) {
        this.isEffectOnOngoingProcesses.set(isEffectOnOngoingProcesses);
    }

    public SimpleBooleanProperty isEffectOnOngoingProcessesProperty() {
        return isEffectOnOngoingProcesses;
    }

    public boolean isIsRoutinelyAffected() {
        return isRoutinelyAffected.get();
    }

    public void setIsRoutinelyAffected(boolean isRoutinelyAffected) {
        this.isRoutinelyAffected.set(isRoutinelyAffected);
    }

    public SimpleBooleanProperty isRoutinelyAffectedProperty() {
        return isRoutinelyAffected;
    }

    public boolean isIsEmployeeTrained() {
        return isEmployeeTrained.get();
    }

    public void setIsEmployeeTrained(boolean isEmployeeTrained) {
        this.isEmployeeTrained.set(isEmployeeTrained);
    }

    public SimpleBooleanProperty isEmployeeTrainedProperty() {
        return isEmployeeTrained;
    }

    public boolean isIsDocumentsChangesNeeded() {
        return isDocumentsChangesNeeded.get();
    }

    public void setIsDocumentsChangesNeeded(boolean isDocumentsChangesNeeded) {
        this.isDocumentsChangesNeeded.set(isDocumentsChangesNeeded);
    }

    public SimpleBooleanProperty isDocumentsChangesNeededProperty() {
        return isDocumentsChangesNeeded;
    }

    public boolean isIsMetrics() {
        return isMetrics.get();
    }

    public void setIsMetrics(boolean isMetrics) {
        this.isMetrics.set(isMetrics);
    }

    public SimpleBooleanProperty isMetricsProperty() {
        return isMetrics;
    }
}
