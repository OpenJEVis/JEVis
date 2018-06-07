/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.data;

import org.jevis.jecalc.gap.Gap.GapMode;
import java.util.List;
import org.jevis.api.JEVisSample;
import org.jevis.jecalc.gap.Gap.GapStrategy;
import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 *
 * @author broder
 */
public interface CleanDataAttribute {

    public DateTime getFirstDate();

    public DateTime getMaxEndDate();

    public Period getPeriodAlignment();

    public List<JEVisSample> getRawSamples();

    public Boolean getConversionDifferential();

    public Boolean getValueIsQuantity();

    public Integer getPeriodOffset();

    public Double getLastDiffValue();

    public Double getMultiplier();

    public Double getOffset();

    public Double getLastCleanValue();

    public GapStrategy getGapFillingMode();

    public Boolean getIsPeriodAligned();

    public Boolean getEnabled();

    public String getName();

}
