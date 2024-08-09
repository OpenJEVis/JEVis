package org.jevis.jeconfig.plugin.charts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.processor.workflow.PeriodRule;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.ChartUnits.ChartUnits;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import java.text.NumberFormat;
import java.util.List;

public class ValuesAbove {
    private static final Logger logger = LogManager.getLogger(ValuesAbove.class);
    private final ChartDataRow chartDataRow;
    private final List<PeriodRule> periodRules;
    private String belowValue = "";
    private String aboveValue = "";
    private String belowDuration = "";
    private String aboveDuration = "";

    public ValuesAbove(ChartDataRow chartDataRow, Double limit) {
        this.chartDataRow = chartDataRow;
        this.periodRules = CleanDataObject.getPeriodAlignmentForObject(chartDataRow.getAttribute().getObject());

        try {
            Double aboveValue = 0d;
            Double belowValue = 0d;
            Long aboveMillis = 0L;
            Long belowMillis = 0L;

            List<JEVisSample> samples = chartDataRow.getSamples();

            for (JEVisSample sample : samples) {
                if (sample.getValueAsDouble() < limit) {
                    belowValue += sample.getValueAsDouble();
                    belowMillis += getPeriodForDate(sample.getTimestamp()).toStandardDuration().getMillis();
                } else {
                    aboveValue += sample.getValueAsDouble();
                    aboveMillis += getPeriodForDate(sample.getTimestamp()).toStandardDuration().getMillis();
                }
            }

            Duration aboveDuration = new Duration(aboveMillis);
            Period abovePeriod = aboveDuration.toPeriod();
            Duration belowDuration = new Duration(belowMillis);
            Period belowPeriod = belowDuration.toPeriod();

            this.belowDuration = PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale()).print(belowPeriod.normalizedStandard())
                    + " (" + PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale()).print(belowPeriod.toStandardHours()) + ")";
            this.aboveDuration = PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale()).print(abovePeriod.normalizedStandard())
                    + " (" + PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale()).print(abovePeriod.toStandardHours()) + ")";

            JEVisUnit unit = chartDataRow.getUnit();
            QuantityUnits qu = new QuantityUnits();
            JEVisUnit sumUnit = qu.getSumUnit(unit);
            ChartUnits cu = new ChartUnits();

            Period currentPeriod = new Period(samples.get(0).getTimestamp(), samples.get(1).getTimestamp());
            Period rawPeriod = CleanDataObject.getPeriodForDate(chartDataRow.getAttribute().getObject(), samples.get(0).getTimestamp());

            double newScaleFactor = cu.scaleValue(rawPeriod, unit.toString(), currentPeriod, sumUnit.toString());
            JEVisUnit inputUnit = null;

            inputUnit = chartDataRow.getAttribute().getInputUnit();
            JEVisUnit sumUnitOfInputUnit = qu.getSumUnit(inputUnit);

            if (qu.isDiffPrefix(sumUnitOfInputUnit, sumUnit)) {
                belowValue = belowValue * newScaleFactor / chartDataRow.getTimeFactor();
                aboveValue = aboveValue * newScaleFactor / chartDataRow.getTimeFactor();
            } else {
                belowValue = belowValue / chartDataRow.getScaleFactor() / chartDataRow.getTimeFactor();
                aboveValue = aboveValue / chartDataRow.getScaleFactor() / chartDataRow.getTimeFactor();
            }


            NumberFormat nf = NumberFormat.getInstance(I18n.getInstance().getLocale());
            nf.setMaximumFractionDigits(2);
            nf.setMinimumFractionDigits(2);

            this.belowValue = nf.format(belowValue) + " " + sumUnit;

            this.aboveValue = nf.format(aboveValue) + " " + sumUnit;
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private Period getPeriodForDate(DateTime dateTime) {
        for (PeriodRule periodRule : periodRules) {
            PeriodRule nextRule = null;
            if (periodRules.size() > periodRules.indexOf(periodRule) + 1) {
                nextRule = periodRules.get(periodRules.indexOf(periodRule) + 1);
            }

            DateTime ts = periodRule.getStartOfPeriod();
            if (dateTime.equals(ts) || dateTime.isAfter(ts) && (nextRule == null || dateTime.isBefore(nextRule.getStartOfPeriod()))) {
                return periodRule.getPeriod();
            }
        }
        return Period.ZERO;
    }

    public String getBelowValue() {
        return belowValue;
    }

    public String getAboveValue() {
        return aboveValue;
    }

    public ChartDataRow getChartDataRow() {
        return chartDataRow;
    }

    public String getBelowDuration() {
        return belowDuration;
    }

    public String getAboveDuration() {
        return aboveDuration;
    }
}
