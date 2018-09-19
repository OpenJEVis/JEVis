package org.jevis.application.Chart.Charts;

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
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.TableEntry;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.commons.unit.UnitManager;
import org.joda.time.Period;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PieChart implements Chart {
    private static SaveResourceBundle rb = new SaveResourceBundle(AppLocale.BUNDLE_ID, AppLocale.getInstance().getLocale());
    private final Logger logger = LogManager.getLogger(PieChart.class);
    private String chartName;
    private String unit;
    private List<ChartDataModel> chartDataModels;
    private Boolean hideShowIcons;
    private ObservableList<javafx.scene.chart.PieChart.Data> series = FXCollections.emptyObservableList();
    private javafx.scene.chart.PieChart pieChart;
    private List<Color> hexColors = new ArrayList<>();
    private Number valueForDisplay;
    private ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private Region pieChartRegion;

    public PieChart(List<ChartDataModel> chartDataModels, Boolean hideShowIcons, String chartName) {
        this.chartDataModels = chartDataModels;
        this.hideShowIcons = hideShowIcons;
        this.chartName = chartName;
        init();
    }

    private void init() {
        List<Double> listSumsPiePieces = new ArrayList<>();
        List<String> listTableEntryNames = new ArrayList<>();

        chartDataModels.parallelStream().forEach(singleRow -> {

            Double sumPiePiece = 0d;
            for (JEVisSample sample : singleRow.getSamples()) {
                try {
                    sumPiePiece += sample.getValueAsDouble();
                } catch (JEVisException e) {

                }
            }
            Lock lock = new ReentrantLock();
            lock.lock();

            listSumsPiePieces.add(sumPiePiece);
            listTableEntryNames.add(singleRow.getTitle());
            hexColors.add(singleRow.getColor());

            lock.unlock();

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

        if (chartDataModels != null && chartDataModels.size() > 0) {
            unit = UnitManager.getInstance().formate(chartDataModels.get(0).getUnit());
            if (unit.equals("")) unit = rb.getString("plugin.graph.chart.valueaxis.nounit");
        }

        pieChart = new javafx.scene.chart.PieChart(series);
        pieChart.applyCss();

        applyColors();

    }


    @Override
    public String getChartName() {
        return chartName;
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
        return null;
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