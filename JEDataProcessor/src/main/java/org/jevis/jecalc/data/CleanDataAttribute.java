/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.data;

import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.jecalc.gap.Gap.GapStrategy;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.List;

/**
 * @author broder
 */
public interface CleanDataAttribute {

    DateTime getFirstDate();

    DateTime getMaxEndDate();

    Period getPeriodAlignment();

    List<JEVisSample> getRawSamples();

    List<JEVisSample> getConversionDifferential();

    Boolean getValueIsQuantity();

    Boolean getLimitsEnabled();

    Boolean getGapFillingEnabled();

    Integer getPeriodOffset();

    Double getLastDiffValue();

    List<JEVisSample> getMultiplier();

    Double getOffset();

    Double getLastCleanValue();

    GapStrategy getGapFillingMode();

    Boolean getIsPeriodAligned();

    Boolean getEnabled();

    String getName();

    List<JsonGapFillingConfig> getGapFillingConfig();

    List<JsonLimitsConfig> getLimitsConfig();

    JEVisObject getObject();

    List<JEVisSample> getCounterOverflow();
}
