package org.jevis.jecc.plugin.dashboard.config;

import javafx.scene.paint.Color;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jecc.application.Chart.ChartType;

public class DataPointNode {

    private Long objectID;
    private Long cleanObjectID;
    private String attribute;
    private ChartType chartType;
    private ManipulationMode manipulationMode = ManipulationMode.NONE;
    private AggregationPeriod aggregationPeriod = AggregationPeriod.NONE;
    private Color color;
    private boolean absolute;
    private String unit;
    private String name;
    private Integer axis = 0;
    private String customCSS;
    private boolean enpi = false;
    private Long calculationID;

    public boolean isAbsolute() {
        return this.absolute;
    }

    public void setAbsolute(boolean absolute) {
        this.absolute = absolute;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return
     * @deprecated the CalculationID field should be enough and It's not only for EnPI
     */
    @Deprecated
    public boolean isEnpi() {
        return this.enpi;
    }

    /**
     * @return
     * @deprecated the CalculationID field should be enough and It's not only for EnPI
     */
    @Deprecated
    public void setEnpi(boolean enpi) {
        this.enpi = enpi;
    }

    public Long getCalculationID() {
        return this.calculationID;
    }

    public void setCalculationID(Long calculationID) {
        this.calculationID = calculationID;
        this.setEnpi(calculationID != null && calculationID > 0l);

    }

    public Long getObjectID() {
        return this.objectID;
    }

    public void setObjectID(Long objectID) {
        this.objectID = objectID;
    }

    public String getAttribute() {
        return this.attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public ManipulationMode getManipulationMode() {
        return this.manipulationMode;
    }

    public void setManipulationMode(ManipulationMode manipulationMode) {
        this.manipulationMode = manipulationMode;
    }

    public AggregationPeriod getAggregationPeriod() {
        return this.aggregationPeriod;
    }

    public void setAggregationPeriod(AggregationPeriod aggregationPeriod) {
        this.aggregationPeriod = aggregationPeriod;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Long getCleanObjectID() {
        return this.cleanObjectID;
    }

    public void setCleanObjectID(Long cleanObjectID) {
        this.cleanObjectID = cleanObjectID;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public ChartType getChartType() {
        return chartType;
    }

    public void setChartType(ChartType chartType) {
        this.chartType = chartType;
    }

    public Integer getAxis() {
        return axis;
    }

    public void setAxis(Integer axis) {
        this.axis = axis;
    }

    public String getCustomCSS() {
        return customCSS;
    }

    public void setCustomCSS(String customCSS) {
        this.customCSS = customCSS;
    }

    @Override
    public String toString() {
        return "DataPointNode{" +
                "objectID=" + this.objectID +
                ", cleanObjectID=" + this.cleanObjectID +
                ", attribute='" + this.attribute + '\'' +
                ", name=" + this.name +
                ", chartType=" + this.chartType +
                ", manipulationMode=" + this.manipulationMode +
                ", aggregationPeriod=" + this.aggregationPeriod +
                ", color=" + this.color +
                ", absolute=" + this.absolute +
                ", enpi=" + this.enpi +
                ", unit=" + this.unit +
                ", calculationID=" + this.calculationID +
                ", axis=" + this.axis +
                ", customCSS=" + this.customCSS +
                '}';
    }
}
