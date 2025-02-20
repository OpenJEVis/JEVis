package org.jevis.jecc.tool.template;

import org.jevis.api.*;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.jecc.application.application.I18nWS;
import org.joda.time.DateTime;
import org.joda.time.Period;

import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

public class VolumeMeterCounterLiter extends Template {


    @Override
    public String getName() {
        return I18n.getInstance().getString("datarow.template.volumemetercounterliter");
    }

    @Override
    public boolean supportsClass(JEVisClass jclass) throws JEVisException {
        return jclass.getName().equals("Data");
    }

    @Override
    public boolean create(JEVisClass jclass, JEVisObject parent, String name) throws JEVisException {
        JEVisClass dataClass = parent.getDataSource().getJEVisClass("Data");
        JEVisClass cleanDataClass = parent.getDataSource().getJEVisClass("Clean Data");

        JEVisObject newRawData = parent.buildObject(name, dataClass);
        newRawData.commit();

        JEVisObject newCleanData = newRawData.buildObject(I18nWS.getInstance().getClassName(cleanDataClass), cleanDataClass);
        newCleanData.commit();

        Period p15m = Period.minutes(15);

        JEVisAttribute valueAttributeClean = newCleanData.getAttribute(CleanDataObject.AttributeName.VALUE.getAttributeName());
        valueAttributeClean.setInputSampleRate(p15m);
        valueAttributeClean.setDisplaySampleRate(p15m);

        Unit _l = NonSI.LITER;
        JEVisUnit liter = new JEVisUnitImp(_l);
        valueAttributeClean.setInputUnit(liter);
        valueAttributeClean.setDisplayUnit(liter);
        valueAttributeClean.commit();

        JEVisAttribute valueAttributeRaw = newRawData.getAttribute(CleanDataObject.AttributeName.VALUE.getAttributeName());
        valueAttributeRaw.setInputSampleRate(p15m);
        valueAttributeRaw.setDisplaySampleRate(p15m);
        valueAttributeRaw.setInputUnit(liter);
        valueAttributeRaw.setDisplayUnit(liter);
        valueAttributeRaw.commit();

        DateTime startDate = new DateTime(1990, 1, 1, 0, 0, 0);


        setAttribute(newRawData, "Period", startDate, p15m.toString());
        setAttribute(newCleanData, "Period", startDate, p15m.toString());
        setAttribute(newCleanData, "Conversion to Differential", startDate, true);
        setAttribute(newCleanData, "Enabled", startDate, true);
        setAttribute(newCleanData, "GapFilling Enabled", startDate, true);
        setAttribute(newCleanData, "Period Alignment", startDate, true);
        setAttribute(newCleanData, "Value is a Quantity", startDate, true);
        setAttribute(newCleanData, "Enabled", startDate, true);
        setAttribute(newCleanData, "Value Multiplier", startDate, 1);
        setAttribute(newCleanData, "Value Offset", startDate, 0);
        setAttribute(newCleanData, "Gap Filling Config", startDate, getGapFillingConfig());
        return true;
    }


    public String getGapFillingConfig() {
        return "[{\n" +
                "  \"name\" : \"Stufe 1\",\n" +
                "  \"type\" : \"INTERPOLATION\",\n" +
                "  \"boundary\" : \"3600000\",\n" +
                "  \"defaultvalue\" : null,\n" +
                "  \"referenceperiod\" : null,\n" +
                "  \"bindtospecific\" : null,\n" +
                "  \"referenceperiodcount\" : null\n" +
                "}, {\n" +
                "  \"name\" : \"Stufe 2\",\n" +
                "  \"type\" : \"Interpolation\",\n" +
                "  \"boundary\" : \"2592000000\",\n" +
                "  \"defaultvalue\" : null,\n" +
                "  \"referenceperiod\" : null,\n" +
                "  \"bindtospecific\" : null,\n" +
                "  \"referenceperiodcount\" : null\n" +
                "}]";
    }


    public void createAttribute(JEVisObject obj, String attribute, DateTime timestamp, Object value) {
        try {
            JEVisSample sample = obj.getAttribute(attribute).buildSample(timestamp, value);
            sample.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
