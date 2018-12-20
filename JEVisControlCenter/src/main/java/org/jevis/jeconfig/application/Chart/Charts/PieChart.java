package org.jevis.jeconfig.application.Chart.Charts;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class PieChart implements Chart {
    private static final Logger logger = LogManager.getLogger(PieChart.class);
    private final Integer chartId;
    private String chartName;
    private String unit;
    private List<ChartDataModel> chartDataModels;
    private Boolean hideShowIcons;
    private ObservableList<javafx.scene.chart.PieChart.Data> series = FXCollections.observableArrayList();
    private javafx.scene.chart.PieChart pieChart;
    private List<Color> hexColors = new ArrayList<>();
    private Number valueForDisplay;
    private ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private Region pieChartRegion;
    private Period period;

    public PieChart(List<ChartDataModel> chartDataModels, Boolean hideShowIcons, Integer chartId, String chartName) {
        this.chartDataModels = chartDataModels;
        this.hideShowIcons = hideShowIcons;
        this.chartName = chartName;
        this.chartId = chartId;
        init();
    }

    private void init() {
        List<Double> listSumsPiePieces = new ArrayList<>();
        List<String> listTableEntryNames = new ArrayList<>();

        if (chartDataModels != null && chartDataModels.size() > 0) {
            unit = UnitManager.getInstance().format(chartDataModels.get(0).getUnit());
            if (unit.equals("")) unit = I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit");
            period = chartDataModels.get(0).getAttribute().getDisplaySampleRate();
        }


        chartDataModels.forEach(singleRow -> {
            if (!singleRow.getSelectedcharts().isEmpty()) {
                Double sumPiePiece = 0d;
                for (JEVisSample sample : singleRow.getSamples()) {
                    try {
                        sumPiePiece += sample.getValueAsDouble();
                    } catch (JEVisException e) {

                    }
                }


                listSumsPiePieces.add(sumPiePiece);
                listTableEntryNames.add(singleRow.getTitle());
                hexColors.add(singleRow.getColor());
            }
        });

        Double whole = 0d;
        List<Double> listPercentages = new ArrayList<>();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        for (Double d : listSumsPiePieces) whole += d;
        for (Double d : listSumsPiePieces) listPercentages.add(d / whole);
        for (String name : listTableEntryNames) {
            String seriesName = name + " - " + nf.format(listSumsPiePieces.get(listTableEntryNames.indexOf(name)))
                    + " " + unit + " (" + nf.format(listPercentages.get(listTableEntryNames.indexOf(name)) * 100) + " %)";

            javafx.scene.chart.PieChart.Data data = new javafx.scene.chart.PieChart.Data(seriesName, listSumsPiePieces.get(listTableEntryNames.indexOf(name)));
            series.add(data);

        }

        pieChart = new javafx.scene.chart.PieChart(series);
        pieChart.applyCss();

        applyColors();

        pieChart.setTitle(chartName);
        pieChart.setLegendVisible(false);
    }

    @Override
    public DateTime getStartDateTime() {
        return chartDataModels.get(0).getSelectedStart();
    }

    @Override
    public DateTime getEndDateTime() {
        return chartDataModels.get(0).getSelectedEnd();
    }

    @Override
    public void updateChart() {

    }

    @Override
    public void setDataModels(List<ChartDataModel> chartDataModels) {
        this.chartDataModels = chartDataModels;
    }

    @Override
    public void setHideShowIcons(Boolean hideShowIcons) {
        this.hideShowIcons = hideShowIcons;
    }

    @Override
    public String getChartName() {
        return chartName;
    }

    @Override
    public void setTitle(String s) {

    }

    @Override
    public Integer getChartId() {
        return null;
    }

    @Override
    public void updateTable(MouseEvent mouseEvent, Number valueForDisplay) {

    }

    @Override
    public void showNote(MouseEvent mouseEvent) {

    }

    @Override
    public void applyColors() {
        for (int i = 0; i < hexColors.size(); i++) {
            Color currentColor = hexColors.get(i);
            String hexColor = toRGBCode(currentColor);
            String preIdent = ".default-color" + i;
            Node node = pieChart.lookup(preIdent + ".chart-pie");
            node.setStyle("-fx-pie-color: " + hexColor + ";");
        }
    }

    @Override
    public ObservableList<TableEntry> getTableData() {
        return tableData;
    }

    @Override
    public Period getPeriod() {
        return period;
    }

    @Override
    public Number getValueForDisplay() {
        return null;
    }

    @Override
    public void setValueForDisplay(Number valueForDisplay) {
        this.valueForDisplay = valueForDisplay;
    }

    @Override
    public javafx.scene.chart.Chart getChart() {
        return pieChart;
    }

    @Override
    public Region getRegion() {
        return pieChartRegion;
    }

    @Override
    public void initializeZoom() {

    }

}