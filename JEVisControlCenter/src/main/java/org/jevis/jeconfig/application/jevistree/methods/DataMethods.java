package org.jevis.jeconfig.application.jevistree.methods;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.jeconfig.dialog.ProgressForm;
import org.jevis.jeconfig.plugin.unit.SamplingRateUI;
import org.jevis.jeconfig.plugin.unit.UnitSelectUI;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DataMethods extends CommonMethods {
    private static final Logger logger = LogManager.getLogger(DataMethods.class);

    public static List<String> DATA_CHILDREN = Arrays.asList("Clean Data", "Math Data", "Forecast Data");
    public static List<String> DATA_TYPES = Arrays.asList("Data", "Clean Data", "Base Data", "Math Data", "String Data");

    public static void setUnitAndPeriod(ProgressForm pForm, JEVisObject jeVisObject, boolean isUnit, UnitSelectUI unit, boolean isPeriod, boolean isNewType, DateTime dateTime, SamplingRateUI rate) throws JEVisException {
        if (DATA_TYPES.contains(jeVisObject.getJEVisClassName())) {
            JEVisAttribute valueAtt = jeVisObject.getAttribute("Value");
            if (isUnit) {
                pForm.addMessage("Setting unit for object " + jeVisObject.getName() + ":" + jeVisObject.getID());
                valueAtt.setDisplayUnit(unit.getUnit());
                valueAtt.setInputUnit(unit.getUnit());
                valueAtt.commit();
            }

            if (isPeriod && !isNewType) {
                pForm.addMessage("Setting period for object " + jeVisObject.getName() + ":" + jeVisObject.getID());
                valueAtt.setDisplaySampleRate(rate.samplingRateProperty().getValue());
                valueAtt.setInputSampleRate(rate.samplingRateProperty().getValue());
                valueAtt.commit();

            } else if (isPeriod) {
                pForm.addMessage("Setting period for object " + jeVisObject.getName() + ":" + jeVisObject.getID());
                JEVisAttribute periodAttribute = jeVisObject.getAttribute("Period");
                if (periodAttribute != null) {
                    List<JEVisSample> oldSamples = periodAttribute.getSamples(dateTime, dateTime);
                    if (!oldSamples.isEmpty()) {
                        periodAttribute.deleteSamplesBetween(dateTime, dateTime);
                    }

                    JEVisSample newSample = periodAttribute.buildSample(dateTime, rate.getPeriod());
                    periodAttribute.addSamples(Collections.singletonList(newSample));
                }
            }
        }

        for (JEVisObject jeVisObject1 : jeVisObject.getChildren()) {
            setUnitAndPeriod(pForm, jeVisObject1, isUnit, unit, isPeriod, isNewType, dateTime, rate);
        }
    }

    public static void setLimits(ProgressForm pForm, JEVisObject jeVisObject, List<JsonLimitsConfig> list) {
        try {
            if (jeVisObject.getJEVisClassName().equals("Clean Data")) {
                JEVisAttribute limitsAttribute = jeVisObject.getAttribute(CleanDataObject.AttributeName.LIMITS_CONFIGURATION.getAttributeName());
                JEVisAttribute limitsEnabledAttribute = jeVisObject.getAttribute(CleanDataObject.AttributeName.LIMITS_ENABLED.getAttributeName());
                if (limitsAttribute != null && limitsEnabledAttribute != null) {
                    pForm.addMessage("Setting limits config for object " + jeVisObject.getName() + ":" + jeVisObject.getID());
                    limitsEnabledAttribute.addSamples(Collections.singletonList(limitsEnabledAttribute.buildSample(new DateTime(), true)));
                    limitsAttribute.addSamples(Collections.singletonList(limitsAttribute.buildSample(new DateTime(), list.toString())));
                }
            }
            for (JEVisObject child : jeVisObject.getChildren()) {
                setLimits(pForm, child, list);
            }
        } catch (JEVisException e) {
            logger.error("Could not set limits for {}:{}", jeVisObject.getName(), jeVisObject.getID());
        }
    }

    public static void setSubstitutionSettings(ProgressForm pForm, JEVisObject jeVisObject, List<JsonGapFillingConfig> list) {
        try {
            if (jeVisObject.getJEVisClassName().equals("Clean Data")) {
                JEVisAttribute gapFillingAttribute = jeVisObject.getAttribute(CleanDataObject.AttributeName.GAP_FILLING_CONFIG.getAttributeName());
                JEVisAttribute gapFillingEnabledAttribute = jeVisObject.getAttribute(CleanDataObject.AttributeName.GAPFILLING_ENABLED.getAttributeName());
                if (gapFillingAttribute != null && gapFillingEnabledAttribute != null) {
                    pForm.addMessage("Setting substitution config for object " + jeVisObject.getName() + ":" + jeVisObject.getID());
                    gapFillingEnabledAttribute.addSamples(Collections.singletonList(gapFillingEnabledAttribute.buildSample(new DateTime(), true)));
                    gapFillingAttribute.addSamples(Collections.singletonList(gapFillingAttribute.buildSample(new DateTime(), list.toString())));
                }
            }
            for (JEVisObject child : jeVisObject.getChildren()) {
                setSubstitutionSettings(pForm, child, list);
            }
        } catch (JEVisException e) {
            logger.error("Could not set gap filling config for {}:{}", jeVisObject.getName(), jeVisObject.getID());
        }
    }

    public static void setAllMultiplierAndDifferential(ProgressForm pForm, JEVisObject jeVisObject, BigDecimal multiplierValue, Boolean differentialValue, DateTime dateTime) {
        try {
            if (jeVisObject.getJEVisClassName().equals("Clean Data")) {
                JEVisAttribute multiplierAttribute = jeVisObject.getAttribute(CleanDataObject.AttributeName.MULTIPLIER.getAttributeName());
                JEVisAttribute differentialAttribute = jeVisObject.getAttribute(CleanDataObject.AttributeName.CONVERSION_DIFFERENTIAL.getAttributeName());
                if (multiplierAttribute != null && differentialAttribute != null) {
                    pForm.addMessage("Setting multiplier and differential for object " + jeVisObject.getName() + ":" + jeVisObject.getID());

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
                setAllMultiplierAndDifferential(pForm, child, multiplierValue, differentialValue, dateTime);
            }
        } catch (JEVisException e) {
            logger.error("Could not delete value samples for {}:{}", jeVisObject.getName(), jeVisObject.getID());
        }
    }
}
