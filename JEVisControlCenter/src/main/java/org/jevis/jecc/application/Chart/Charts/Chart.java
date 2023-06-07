package org.jevis.jecc.application.Chart.Charts;

import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import org.jetbrains.annotations.NotNull;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jecc.application.Chart.ChartElements.TableEntry;
import org.jevis.jecc.application.Chart.ChartElements.XYChartSerie;
import org.jevis.jecc.application.Chart.ChartType;
import org.jevis.jecc.application.Chart.data.ChartDataRow;
import org.jevis.jecc.application.Chart.data.ChartModel;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.List;

public interface Chart extends Comparable<Chart> {

    AlphanumComparator alphanumComparator = new AlphanumComparator();

    String getChartName();

    void setTitle(String s);

    Integer getChartId();

    void updateTable(MouseEvent mouseEvent, DateTime valueForDisplay);

    void updateTableZoom(double lowerBound, double upperBound);

    void applyColors();

    de.gsi.chart.Chart getChart();

    void setChart(de.gsi.chart.Chart chart);

    ChartType getChartType();

    Region getRegion();

    void setRegion(Region region);

    ObservableList<TableEntry> getTableData();

    Period getPeriod();

    void setPeriod(Period period);

    List<ChartDataRow> getChartDataRows();

    ChartModel getChartModel();

    List<XYChartSerie> getXyChartSerieList();

    @Override
    default int compareTo(@NotNull Chart o) {
        return alphanumComparator.compare(this.getChartName(), o.getChartName());
    }
}

