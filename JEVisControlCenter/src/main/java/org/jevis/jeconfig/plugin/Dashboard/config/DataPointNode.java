package org.jevis.jeconfig.plugin.Dashboard.config;

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

    private boolean enpi = false;
    private Long calculationID;

    public boolean isEnpi() {
        return enpi;
    }

    public void setEnpi(boolean enpi) {
        this.enpi = enpi;
    }

    public Long getCalculationID() {
        return calculationID;
    }

    public void setCalculationID(Long calculationID) {
        this.calculationID = calculationID;
    }

    public Long getObjectID() {
        return objectID;
    }

    public void setObjectID(Long objectID) {
        this.objectID = objectID;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public ManipulationMode getManipulationMode() {
        return manipulationMode;
    }

    public void setManipulationMode(ManipulationMode manipulationMode) {
        this.manipulationMode = manipulationMode;
    }

    public AggregationPeriod getAggregationPeriod() {
        return aggregationPeriod;
    }

    public void setAggregationPeriod(AggregationPeriod aggregationPeriod) {
        this.aggregationPeriod = aggregationPeriod;
    }

    public Long getCleanObjectID() {
        return cleanObjectID;
    }

    public void setCleanObjectID(Long cleanObjectID) {
        this.cleanObjectID = cleanObjectID;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
