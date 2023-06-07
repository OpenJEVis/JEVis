package org.jevis.jecc.plugin.charts;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.ChartUnits.ChartUnits;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.jecc.application.Chart.data.ChartDataRow;

import java.text.NumberFormat;

public class SumsAbove {
    private final ChartDataRow chartDataRow;
    private String below = "";
    private String above = "";

    public SumsAbove(ChartDataRow chartDataRow, Double limit) {
        this.chartDataRow = chartDataRow;

        Double aboveValue = 0d;
        Double belowValue = 0d;

        try {
            for (JEVisSample sample : chartDataRow.getSamples()) {
                if (sample.getValueAsDouble() < limit) {
                    belowValue += sample.getValueAsDouble();
                } else {
                    aboveValue += sample.getValueAsDouble();
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        JEVisUnit unit = chartDataRow.getUnit();
        QuantityUnits qu = new QuantityUnits();
        JEVisUnit sumUnit = qu.getSumUnit(unit);
        ChartUnits cu = new ChartUnits();
        double newScaleFactor = cu.scaleValue(unit.toString(), sumUnit.toString());
        JEVisUnit inputUnit = null;
        try {
            inputUnit = chartDataRow.getAttribute().getInputUnit();
            JEVisUnit sumUnitOfInputUnit = qu.getSumUnit(inputUnit);

            if (qu.isDiffPrefix(sumUnitOfInputUnit, sumUnit)) {
                belowValue = belowValue * newScaleFactor / chartDataRow.getTimeFactor();
                aboveValue = aboveValue * newScaleFactor / chartDataRow.getTimeFactor();
            } else {
                belowValue = belowValue / chartDataRow.getScaleFactor() / chartDataRow.getTimeFactor();
                aboveValue = aboveValue / chartDataRow.getScaleFactor() / chartDataRow.getTimeFactor();
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        NumberFormat nf = NumberFormat.getInstance(I18n.getInstance().getLocale());
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);

        below = nf.format(belowValue) + " " + sumUnit;

        above = nf.format(aboveValue) + " " + sumUnit;
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
