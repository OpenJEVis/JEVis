package org.jevis.jeconfig.application.Chart.ChartElements;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;

import java.text.NumberFormat;
import java.util.List;

public interface Serie {

    default void calcTableValues(TableEntry te, List<JEVisSample> samples, String unit, boolean isQuantity) throws JEVisException {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        double avg = 0.0;
        Double sum = 0.0;

        for (JEVisSample smp : samples) {
            Double currentValue = smp.getValueAsDouble();
            min = Math.min(min, currentValue);
            max = Math.max(max, currentValue);
            sum += smp.getValueAsDouble();
        }

        if (samples.size() > 0)
            avg = sum / samples.size();

        NumberFormat nf_out = NumberFormat.getNumberInstance();
        nf_out.setMaximumFractionDigits(2);
        nf_out.setMinimumFractionDigits(2);

        if (min == Double.MAX_VALUE || samples.size() == 0)
            te.setMin("- " + unit);
        else te.setMin(nf_out.format(min) + " " + unit);

        if (max == Double.MIN_VALUE || samples.size() == 0)
            te.setMax("- " + unit);
        else te.setMax(nf_out.format(max) + " " + unit);

        if (samples.size() == 0) {
            te.setAvg("- " + unit);
            te.setSum("- " + unit);
        } else {
            te.setAvg(nf_out.format(avg) + " " + unit);
            if (isQuantity) te.setSum(nf_out.format(sum) + " " + unit);
            else te.setSum("- " + unit);
        }
    }

}
