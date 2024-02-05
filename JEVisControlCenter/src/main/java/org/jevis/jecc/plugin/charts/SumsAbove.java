package org.jevis.jecc.plugin.charts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.ChartUnits.ChartUnits;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.jecc.application.Chart.data.ChartDataRow;
import org.joda.time.Period;

import java.text.NumberFormat;
import java.util.List;

public class SumsAbove {
    private static final Logger logger = LogManager.getLogger(SumsAbove.class);
    private final ChartDataRow chartDataRow;
    private String below = "";
    private String above = "";

    public SumsAbove(ChartDataRow chartDataRow, Double limit) {
        this.chartDataRow = chartDataRow;

        try {
            Double aboveValue = 0d;
            Double belowValue = 0d;
            List<JEVisSample> samples = chartDataRow.getSamples();

            for (JEVisSample sample : samples) {
                if (sample.getValueAsDouble() < limit) {
                    belowValue += sample.getValueAsDouble();
                } else {
                    aboveValue += sample.getValueAsDouble();
                }
            }

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

            below = nf.format(belowValue) + " " + sumUnit;

            above = nf.format(aboveValue) + " " + sumUnit;
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public String getBelow() {
        return below;
    }

    public String getAbove() {
        return above;
    }

    public ChartDataRow getChartDataRow() {
        return chartDataRow;
    }

}
