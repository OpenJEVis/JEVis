package org.jevis.jeconfig.application.Chart.Charts;

import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.data.ChartDataModel;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.List;

public interface Chart {

    String getChartName();

    void setTitle(String s);

    Integer getChartId();

    void updateTable(MouseEvent mouseEvent, DateTime valueForDisplay);

    void updateTableZoom(double lowerBound, double upperBound);

    void applyColors();

    de.gsi.chart.Chart getChart();

    ChartType getChartType();

    Region getRegion();

    ObservableList<TableEntry> getTableData();

    Period getPeriod();

    void setRegion(Region region);

    List<ChartDataModel> getChartDataModels();
}
