package org.jevis.application.Chart.ChartElements;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;

import java.text.NumberFormat;
import java.util.List;

public interface Serie {

    default void calcTableValues(TableEntry te, List<JEVisSample> samples, String unit) throws JEVisException {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        Double avg;
        Double sum = 0.0;

        for (JEVisSample smp : samples) {
            min = Math.min(min, smp.getValueAsDouble());
            max = Math.max(max, smp.getValueAsDouble());
            sum += smp.getValueAsDouble();
        }

        avg = sum / samples.size();
        NumberFormat nf_out = NumberFormat.getNumberInstance();
        nf_out.setMaximumFractionDigits(2);
        nf_out.setMinimumFractionDigits(2);

        te.setMin(nf_out.format(min) + " " + unit);
        te.setMax(nf_out.format(max) + " " + unit);
        te.setAvg(nf_out.format(avg) + " " + unit);
        te.setSum(nf_out.format(sum) + " " + unit);
    }
}
