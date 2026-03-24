package org.jevis.jeconfig.application.Chart.ChartElements;

import com.ibm.icu.text.NumberFormat;
import javafx.beans.property.*;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TableHSample {
    private static final Logger logger = LogManager.getLogger(TableHSample.class);
    private final NumberFormat nf;
    private final StringProperty sampleName = new SimpleStringProperty(this, "sampleName", "");
    private final DoubleProperty sampleValue = new SimpleDoubleProperty(this, "sampleValue", 0);
    private final StringProperty sampleUnit = new SimpleStringProperty(this, "sampleUnit", "");
    private final ObjectProperty<Color> sampleColor = new SimpleObjectProperty<>(this, "sampleColor", null);


    /**
     * Used for the sum row only
     */
    public TableHSample(double value, String name, String unit) {
        setSampleName(name);
        setSampleValue(value);
        setSampleUnit(unit);
        nf = com.ibm.icu.text.NumberFormat.getNumberInstance();
    }

    public TableHSample(XYChartSerie xyChartSerie) {
        setSampleName(xyChartSerie.getTableEntryName());
        try {
            setSampleValue(xyChartSerie.getSingleRow().getSamples().get(0).getValueAsDouble());
        } catch (Exception e) {
            logger.error(e);
        }
        setSampleUnit(xyChartSerie.getUnit());
        nf = xyChartSerie.getNf();
    }

    public String getSampleName() {
        return sampleName.get();
    }

    public void setSampleName(String sampleName) {
        this.sampleName.set(sampleName);
    }

    public StringProperty sampleNameProperty() {
        return sampleName;
    }

    public double getSampleValue() {
        return sampleValue.get();
    }

    public void setSampleValue(double sampleValue) {
        this.sampleValue.set(sampleValue);
    }

    public DoubleProperty sampleValueProperty() {
        return sampleValue;
    }

    public String getSampleUnit() {
        return sampleUnit.get();
    }

    public void setSampleUnit(String sampleUnit) {
        this.sampleUnit.set(sampleUnit);
    }

    public StringProperty sampleUnitProperty() {
        return sampleUnit;
    }

    public Color getSampleColor() {
        return sampleColor.get();
    }

    public void setSampleColor(Color sampleColor) {
        this.sampleColor.set(sampleColor);
    }

    public Property<Color> sampleColorProperty() {
        return sampleColor;
    }

    public NumberFormat getNf() {
        return nf;
    }
}
