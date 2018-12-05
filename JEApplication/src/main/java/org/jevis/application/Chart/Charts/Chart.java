package org.jevis.application.Chart.Charts;

import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.jevis.application.Chart.ChartElements.TableEntry;
import org.joda.time.DateTime;
import org.joda.time.Period;

public interface Chart {

    String getChartName();

    Integer getChartId();

    void updateTable(MouseEvent mouseEvent, Number valueForDisplay);

    void showNote(MouseEvent mouseEvent);

    void applyColors();

    default String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    Number getValueForDisplay();

    void setValueForDisplay(Number valueForDisplay);

    javafx.scene.chart.Chart getChart();

    Region getRegion();

    void initializeZoom();

    ObservableList<TableEntry> getTableData();

    Period getPeriod();

    DateTime getStartDateTime();

    DateTime getEndDateTime();

}
