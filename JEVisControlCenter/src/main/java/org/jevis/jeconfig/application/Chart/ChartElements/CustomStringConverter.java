/*****************************************************************************
 *                                                                           *
 * BI Common - convert Number <-> String                                     *
 *                                                                           *
 * modified: 2017-04-25 Harald Braeuning                                     *
 *                                                                           *
 ****************************************************************************/

package org.jevis.jeconfig.application.Chart.ChartElements;

import com.ibm.icu.text.NumberFormat;
import de.gsi.chart.utils.NumberFormatter;
import javafx.util.StringConverter;
import org.jevis.commons.i18n.I18n;

/**
 * @author braeun
 */
public class CustomStringConverter extends StringConverter<Number> implements NumberFormatter {

    private final NumberFormat format = NumberFormat.getInstance(I18n.getInstance().getLocale());
    private int precision = 6;

    public CustomStringConverter() {
        this(6);
    }

    public CustomStringConverter(int precision) {
        this.precision = precision;
        buildFormat();
    }

    private void buildFormat() {
        this.format.setMinimumFractionDigits(precision);
        this.format.setMaximumFractionDigits(precision);
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
        return precision;
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
        this.precision = precision;
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
