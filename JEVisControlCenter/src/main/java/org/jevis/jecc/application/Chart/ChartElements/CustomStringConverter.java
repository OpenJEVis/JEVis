/*****************************************************************************
 *                                                                           *
 * BI Common - convert Number <-> String                                     *
 *                                                                           *
 * modified: 2017-04-25 Harald Braeuning                                     *
 *                                                                           *
 ****************************************************************************/

package org.jevis.jecc.application.Chart.ChartElements;

import com.ibm.icu.text.NumberFormat;
import de.gsi.chart.utils.NumberFormatter;
import javafx.util.StringConverter;
import org.jevis.commons.i18n.I18n;

/**
 * @author braeun
 */
public class CustomStringConverter extends StringConverter<Number> implements NumberFormatter {

    private final NumberFormat format = NumberFormat.getInstance(I18n.getInstance().getLocale());
    private int min = 6;
    private int max = 6;

    public CustomStringConverter() {
        this(6, 6);
    }

    public CustomStringConverter(int min, int max) {
        this.min = min;
        this.max = max;
        buildFormat();
    }

    private void buildFormat() {
        this.format.setMinimumFractionDigits(min);
        this.format.setMaximumFractionDigits(max);
    }


    @Override
    public Number fromString(String string) {
        return Double.parseDouble(string);
    }

    /*
     * (non-Javadoc)
     *
     * @see de.gsi.chart.utils.NumberFormatter#getPrecision()
     */
    @Override
    public int getPrecision() {
        return max;
    }

    @Override
    public boolean isExponentialForm() {
        return false;
    }

    @Override
    public NumberFormatter setExponentialForm(boolean state) {
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.gsi.chart.utils.NumberFormatter#setPrecision(int)
     */
    @Override
    public NumberFormatter setPrecision(int precision) {
        this.min = precision;
        this.max = precision;
        buildFormat();
        return this;
    }

    public NumberFormatter setPrecision(int min, int max) {
        this.min = min;
        this.max = max;
        buildFormat();
        return this;
    }

    @Override
    public String toString(double val) {
        return toString(Double.valueOf(val));
    }

    @Override
    public String toString(Number object) {
        return format.format(object);
    }

}
