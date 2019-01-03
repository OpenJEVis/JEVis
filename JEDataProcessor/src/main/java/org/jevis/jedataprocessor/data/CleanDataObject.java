/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.data;

import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.jedataprocessor.gap.Gap.GapStrategy;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.List;

/**
 * @author broder
 */
public interface CleanDataObject {

    /**
     * Check if the configuration is valid. Throws exception if configuration is not valid.
     *
     * @throws Exception
     */
    void checkConfig() throws Exception;

    /**
     * Returns the last clean data timestamp plus the period as the expected next first new date
     *
     * @return
     */
    DateTime getFirstDate();

    DateTime getMaxEndDate();

    Period getPeriodAlignment();

    List<JEVisSample> getRawSamples();

    List<JEVisSample> getConversionDifferential();

    Boolean getValueIsQuantity();

    Boolean getLimitsEnabled();

    Boolean getGapFillingEnabled();

    Integer getPeriodOffset();

    /**
     * Returns the last counter value
     *
     * @return
     * @throws Exception
     */
    Double getLastCounterValue() throws Exception;

    /**
     * Return true if this is the first run of the precalc for this Clean Data
     *
     * @return
     */
    boolean isFirstRun() throws Exception;

    List<JEVisSample> getMultiplier();

    Double getOffset();

    Double getLastCleanValue() throws Exception;

    GapStrategy getGapFillingMode();

    Boolean getIsPeriodAligned();

    Boolean getEnabled();

    String getName();

    List<JsonGapFillingConfig> getGapFillingConfig();

    List<JsonLimitsConfig> getLimitsConfig() throws Exception;

    JEVisObject getObject();

    List<JEVisSample> getCounterOverflow();
}
