package org.jevis.jeconfig.plugin.metersv2.cells;


import org.jevis.jeconfig.plugin.metersv2.data.MeterData;


public class LastRawValuePojo {

    private MeterData meterData;

    private double value;

    private String unitLabel;

    private int precision;

    public LastRawValuePojo() {
    }

    public LastRawValuePojo(MeterData meterData, double value, String unitLabel, int precision) {
        this.meterData = meterData;
        this.value = value;
        this.unitLabel = unitLabel;
        this.precision = precision;
    }

    public MeterData getMeterData() {
        return meterData;
    }

    public void setMeterData(MeterData meterData) {
        this.meterData = meterData;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnitLabel() {
        return unitLabel;
    }

    public void setUnitLabel(String unitLabel) {
        this.unitLabel = unitLabel;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    @Override
    public String toString() {
        return "LastRawValuePojo{" +
                "meterData=" + meterData +
                ", value=" + value +
                ", unitLabel='" + unitLabel + '\'' +
                ", precision=" + precision +
                '}';
    }
}
