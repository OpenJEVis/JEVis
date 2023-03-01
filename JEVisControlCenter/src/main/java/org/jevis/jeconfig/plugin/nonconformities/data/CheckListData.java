package org.jevis.jeconfig.plugin.nonconformities.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.jevis.commons.i18n.I18n;

public class CheckListData {

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
    private final SimpleBooleanProperty isModel = new SimpleBooleanProperty("Model", I18n.getInstance().getString("plugin.nonconforrmities.model"), false);

    @Expose
    @SerializedName("Miscellaneous")
    private final SimpleBooleanProperty isMiscellaneous = new SimpleBooleanProperty("Miscellaneous", I18n.getInstance().getString("plugin.nonconforrmities.miscellaneous"), false);

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
}
