package org.jevis.jeconfig.application.Chart.Charts;

import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import org.jetbrains.annotations.NotNull;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.ChartElements.XYChartSerie;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.Chart.data.ChartModel;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.List;

public interface Chart extends Comparable<Chart> {

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

    ObservableList<TableEntry> getTableData();

    Period getPeriod();

    void setPeriod(Period period);

    void setRegion(Region region);

    List<ChartDataRow> getChartDataRows();

    ChartModel getChartModel();

    List<XYChartSerie> getXyChartSerieList();

    AlphanumComparator alphanumComparator = new AlphanumComparator();

    @Override
    default int compareTo(@NotNull Chart o) {
        return alphanumComparator.compare(this.getChartName(), o.getChartName());
    }
}

