package org.jevis.jeconfig.application.Chart.ChartElements;

import de.gsi.chart.axes.spi.AxisRange;
import de.gsi.chart.axes.spi.DefaultNumericAxis;

public class CustomNumericAxis extends DefaultNumericAxis {

    public CustomNumericAxis() {
        super();
    }


    @Override
    protected AxisRange autoRange(final double minValue, final double maxValue, final double length,
                                  final double labelSize) {
        double min = minValue > 0 && isForceZeroInRange() ? 0 : minValue;
        if (isLogAxis && minValue <= 0) {
            min = DefaultNumericAxis.DEFAULT_LOG_MIN_VALUE;
            isUpdating = true;
            // TODO: check w.r.t. inverted axis (lower <-> upper bound exchange)
            setMin(DefaultNumericAxis.DEFAULT_LOG_MIN_VALUE);
            isUpdating = false;
        }

        final double max = maxValue < 0 && isForceZeroInRange() ? 0 : maxValue;
        final double padding = DefaultNumericAxis.getEffectiveRange(min, max) * getAutoRangePadding();
        final double paddingScale = 1.0 + getAutoRangePadding();
        final double paddedMin = isLogAxis ? minValue / paddingScale
                : DefaultNumericAxis.clampBoundToZero(min - padding, min);
        final double paddedMax = isLogAxis ? maxValue * paddingScale
                : DefaultNumericAxis.clampBoundToZero(max + padding, max) * 1.10;

        return computeRange(paddedMin, paddedMax, length, labelSize);
    }
}
