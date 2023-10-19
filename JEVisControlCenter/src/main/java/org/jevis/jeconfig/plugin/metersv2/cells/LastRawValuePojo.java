package org.jevis.jeconfig.plugin.metersv2.cells;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisType;
import org.jevis.commons.classes.JC;
import org.jevis.jeconfig.plugin.metersv2.MeterController;
import org.jevis.jeconfig.plugin.metersv2.data.JEVisTypeWrapper;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;
import org.joda.time.DateTime;


public class LastRawValuePojo {
    private static final Logger logger = LogManager.getLogger(LastRawValuePojo.class);

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
        setPrecisionInJEVisObject(precision);
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

    private void setPrecisionInJEVisObject(int decimalPlaces) {
        try {
            JEVisSample jeVisSample = meterData.getJeVisObject().getAttribute(JC.MeasurementInstrument.a_DecimalPlaces).buildSample(DateTime.now(), decimalPlaces);
            jeVisSample.commit();
            meterData.load();

        } catch (Exception e) {
            logger.error(e);
        }
    }
}
