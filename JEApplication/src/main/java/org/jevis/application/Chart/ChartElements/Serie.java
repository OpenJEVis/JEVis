package org.jevis.application.Chart.ChartElements;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;

import java.text.NumberFormat;
import java.util.List;

public interface Serie {

    default void calcTableValues(TableEntry te, List<JEVisSample> samples, String unit) throws JEVisException {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        Double avg = 0.0;
        Double sum = 0.0;

        for (JEVisSample smp : samples) {
            min = Math.min(min, smp.getValueAsDouble());
            max = Math.max(max, smp.getValueAsDouble());
            sum += smp.getValueAsDouble();
        }

        if (samples.size() > 0)
            avg = sum / samples.size();

        NumberFormat nf_out = NumberFormat.getNumberInstance();
        nf_out.setMaximumFractionDigits(2);
        nf_out.setMinimumFractionDigits(2);

        if (min == Double.MAX_VALUE)
            te.setMin("- " + unit);
        else te.setMin(nf_out.format(min) + " " + unit);

        if (max == Double.MIN_VALUE)
            te.setMax("- " + unit);
        else te.setMax(nf_out.format(max) + " " + unit);

        if (avg == 0.0)
            te.setAvg("- " + unit);
        else te.setAvg(nf_out.format(avg) + " " + unit);

        te.setSum(nf_out.format(sum) + " " + unit);
    }
}
