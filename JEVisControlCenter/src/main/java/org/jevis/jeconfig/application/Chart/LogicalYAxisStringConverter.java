package org.jevis.jeconfig.application.Chart;

import javafx.util.StringConverter;
import org.jevis.jeconfig.tool.I18n;

public class LogicalYAxisStringConverter extends StringConverter<Number> {
    public LogicalYAxisStringConverter() {
    }

    @Override
    public String toString(Number object) {
        if (object.intValue() != object.doubleValue()) return "";
        if (object.intValue() == 0) return I18n.getInstance().getString("plugin.graph.chart.logical.yaxis.off");
        if (object.intValue() == 1) return I18n.getInstance().getString("plugin.graph.chart.logical.yaxis.on");
        return "" + (object.intValue());
    }

    @Override
    public Number fromString(String string) {
        Number val = Double.parseDouble(string);
        return val.intValue();
    }
}
