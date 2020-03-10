package org.jevis.jeconfig.application.jevistree.methods;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.jeconfig.plugin.unit.SamplingRateUI;
import org.jevis.jeconfig.plugin.unit.UnitSelectUI;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class DataMethods extends CommonMethods {
    private static final Logger logger = LogManager.getLogger(DataMethods.class);

    public static void setUnitAndPeriod(JEVisObject jeVisObject, boolean isUnit, UnitSelectUI unit, boolean isPeriod, SamplingRateUI rate) throws JEVisException {
        if (jeVisObject.getJEVisClassName().equals("Data") || jeVisObject.getJEVisClassName().equals("Clean Data")) {
            JEVisAttribute valueAtt = jeVisObject.getAttribute("Value");
            if (isUnit) {
                valueAtt.setDisplayUnit(unit.getUnit());
                valueAtt.setInputUnit(unit.getUnit());
            }

            if (isPeriod) {
                valueAtt.setDisplaySampleRate(rate.samplingRateProperty().getValue());
                valueAtt.setInputSampleRate(rate.samplingRateProperty().getValue());
            }
            valueAtt.commit();
        }

        for (JEVisObject jeVisObject1 : jeVisObject.getChildren()) {
            setUnitAndPeriod(jeVisObject1, isUnit, unit, isPeriod, rate);
        }
    }

    public static void setLimits(JEVisObject jeVisObject, List<JsonLimitsConfig> list) {
        try {
            if (jeVisObject.getJEVisClassName().equals("Clean Data")) {
                JEVisAttribute limitsAttribute = jeVisObject.getAttribute(CleanDataObject.AttributeName.LIMITS_CONFIGURATION.getAttributeName());
                JEVisAttribute limitsEnabledAttribute = jeVisObject.getAttribute(CleanDataObject.AttributeName.LIMITS_ENABLED.getAttributeName());
                if (limitsAttribute != null && limitsEnabledAttribute != null) {
                    limitsEnabledAttribute.addSamples(Collections.singletonList(limitsEnabledAttribute.buildSample(new DateTime(), true)));
                    limitsAttribute.addSamples(Collections.singletonList(limitsAttribute.buildSample(new DateTime(), list.toString())));
                }
            }
            for (JEVisObject child : jeVisObject.getChildren()) {
                setLimits(child, list);
            }
        } catch (JEVisException e) {
            logger.error("Could not delete value samples for {}:{}", jeVisObject.getName(), jeVisObject.getID());
        }
    }

    public static void setAllMultiplierAndDifferential(JEVisObject jeVisObject, BigDecimal multiplierValue, Boolean differentialValue, DateTime dateTime) {
        try {
            if (jeVisObject.getJEVisClassName().equals("Clean Data")) {
                JEVisAttribute multiplierAttribute = jeVisObject.getAttribute(CleanDataObject.AttributeName.MULTIPLIER.getAttributeName());
                JEVisAttribute differentialAttribute = jeVisObject.getAttribute(CleanDataObject.AttributeName.CONVERSION_DIFFERENTIAL.getAttributeName());
                if (multiplierAttribute != null && differentialAttribute != null) {
                    List<JEVisSample> previousMultiplierSamples = multiplierAttribute.getSamples(dateTime, dateTime);
                    if (previousMultiplierSamples.size() > 0) {
                        multiplierAttribute.deleteSamplesBetween(dateTime, dateTime);
                    }
                    multiplierAttribute.addSamples(Collections.singletonList(multiplierAttribute.buildSample(dateTime, multiplierValue.doubleValue())));

                    List<JEVisSample> previousDifferentialSamples = differentialAttribute.getSamples(dateTime, dateTime);
                    if (previousDifferentialSamples.size() > 0) {
                        differentialAttribute.deleteSamplesBetween(dateTime, dateTime);
                    }
                    differentialAttribute.addSamples(Collections.singletonList(differentialAttribute.buildSample(dateTime, differentialValue)));
                }

            }
            for (JEVisObject child : jeVisObject.getChildren()) {
                setAllMultiplierAndDifferential(child, multiplierValue, differentialValue, dateTime);
            }
        } catch (JEVisException e) {
            logger.error("Could not delete value samples for {}:{}", jeVisObject.getName(), jeVisObject.getID());
        }
    }
}
