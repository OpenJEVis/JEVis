package org.jevis.jecc.plugin.charts;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.jevis.jecc.application.Chart.Charts.regression.RegressionType;

public class ToolBarSettings {

    private final SimpleBooleanProperty showRawData = new SimpleBooleanProperty(this, "showRawData", false);
    private final SimpleBooleanProperty showSum = new SimpleBooleanProperty(this, "showSum", false);
    private final SimpleBooleanProperty showL1L2 = new SimpleBooleanProperty(this, "ShowL1L2", false);
    private final SimpleObjectProperty<RegressionType> regressionType = new SimpleObjectProperty<>(this, "regressionType", RegressionType.NONE);
    private final SimpleBooleanProperty showIcons = new SimpleBooleanProperty(this, "showIcons", true);
    private final SimpleBooleanProperty calculateRegression = new SimpleBooleanProperty(this, "calculateRegression", false);
    private final SimpleIntegerProperty polyRegressionDegree = new SimpleIntegerProperty(this, "polyRegressionDegree", -1);
    private final SimpleBooleanProperty autoResize = new SimpleBooleanProperty(this, "autoResize", true);
    private final SimpleBooleanProperty customWorkday = new SimpleBooleanProperty(this, "customWorkday", true);
    private final SimpleBooleanProperty runUpdate = new SimpleBooleanProperty(this, "runUpdate", false);


    public boolean isShowRawData() {
        return showRawData.get();
    }

    public void setShowRawData(boolean showRawData) {
        this.showRawData.set(showRawData);
    }

    public SimpleBooleanProperty showRawDataProperty() {
        return showRawData;
    }

    public boolean isShowSum() {
        return showSum.get();
    }

    public void setShowSum(boolean showSum) {
        this.showSum.set(showSum);
    }

    public SimpleBooleanProperty showSumProperty() {
        return showSum;
    }

    public boolean isShowL1L2() {
        return showL1L2.get();
    }

    public void setShowL1L2(boolean showL1L2) {
        this.showL1L2.set(showL1L2);
    }

    public SimpleBooleanProperty showL1L2Property() {
        return showL1L2;
    }

    public RegressionType getRegressionType() {
        return regressionType.get();
    }

    public void setRegressionType(RegressionType regressionType) {
        this.regressionType.set(regressionType);
    }

    public SimpleObjectProperty<RegressionType> regressionTypeProperty() {
        return regressionType;
    }

    public boolean isShowIcons() {
        return showIcons.get();
    }

    public void setShowIcons(boolean showIcons) {
        this.showIcons.set(showIcons);
    }

    public SimpleBooleanProperty showIconsProperty() {
        return showIcons;
    }

    public boolean isCalculateRegression() {
        return calculateRegression.get();
    }

    public void setCalculateRegression(boolean calculateRegression) {
        this.calculateRegression.set(calculateRegression);
    }

    public SimpleBooleanProperty calculateRegressionProperty() {
        return calculateRegression;
    }

    public int getPolyRegressionDegree() {
        return polyRegressionDegree.get();
    }

    public void setPolyRegressionDegree(int polyRegressionDegree) {
        this.polyRegressionDegree.set(polyRegressionDegree);
    }

    public SimpleIntegerProperty polyRegressionDegreeProperty() {
        return polyRegressionDegree;
    }

    public boolean isAutoResize() {
        return autoResize.get();
    }

    public void setAutoResize(boolean autoResize) {
        this.autoResize.set(autoResize);
    }

    public SimpleBooleanProperty autoResizeProperty() {
        return autoResize;
    }

    public boolean isCustomWorkday() {
        return customWorkday.get();
    }

    public void setCustomWorkday(boolean customWorkday) {
        this.customWorkday.set(customWorkday);
    }

    public SimpleBooleanProperty customWorkdayProperty() {
        return customWorkday;
    }

    public boolean isRunUpdate() {
        return runUpdate.get();
    }

    public void setRunUpdate(boolean runUpdate) {
        this.runUpdate.set(runUpdate);
    }

    public SimpleBooleanProperty runUpdateProperty() {
        return runUpdate;
    }
}
