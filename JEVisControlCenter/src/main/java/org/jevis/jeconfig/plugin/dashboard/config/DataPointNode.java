package org.jevis.jeconfig.plugin.dashboard.config;

import javafx.scene.paint.Color;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;

public class DataPointNode {

    private Long objectID;
    private Long cleanObjectID;
    private String attribute;
    private ManipulationMode manipulationMode;
    private AggregationPeriod aggregationPeriod;
    private Color color;
    private boolean absolute;
    private String unit;

    public boolean isAbsolute() {
        return this.absolute;
    }

    public void setAbsolute(boolean absolute) {
        this.absolute = absolute;
    }

    private boolean enpi = false;
    private Long calculationID;

    public boolean isEnpi() {
        return this.enpi;
    }

    public void setEnpi(boolean enpi) {
        this.enpi = enpi;
    }

    public Long getCalculationID() {
        return this.calculationID;
    }

    public void setCalculationID(Long calculationID) {
        this.calculationID = calculationID;
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

    @Override
    public String toString() {
        return "DataPointNode{" +
                "objectID=" + this.objectID +
                ", cleanObjectID=" + this.cleanObjectID +
                ", attribute='" + this.attribute + '\'' +
                ", manipulationMode=" + this.manipulationMode +
                ", aggregationPeriod=" + this.aggregationPeriod +
                ", color=" + this.color +
                ", absolute=" + this.absolute +
                ", enpi=" + this.enpi +
                ", unit=" + this.unit +
                ", calculationID=" + this.calculationID +
                '}';
    }
}
