package org.jevis.jeconfig.application.Chart.Charts;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.chart.BubbleType;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.database.SampleHandler;
import org.jevis.jeconfig.application.Chart.ChartElements.Bubble;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.Zoom.ChartPanManager;
import org.jevis.jeconfig.application.Chart.Zoom.JFXChartUtil;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class BubbleChart implements Chart {
    private javafx.scene.chart.BubbleChart<Number, Number> bubbleChart;
    private List<Color> hexColors = new ArrayList<>();
    javafx.scene.chart.BubbleChart<Number, Number> chart;
    private Region chartRegion;
    private ObservableList<TableEntry> tableData = FXCollections.observableArrayList();

    public BubbleChart(List<ChartDataModel> chartDataModels, Boolean showRawData, Boolean showSum, Boolean hideShowIcons, Integer chartId, String chartName) {
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        List<JEVisSample> xList = new ArrayList<>();
        List<JEVisSample> yList = new ArrayList<>();

        AtomicReference<Double> minX = new AtomicReference<>(Double.MAX_VALUE);
        AtomicReference<Double> maxX = new AtomicReference<>(-Double.MAX_VALUE);
        boolean isEnPI = false;
        ChartDataModel chartDataModelY = null;

        for (ChartDataModel model : chartDataModels) {
            for (JEVisSample sample : model.getSamples()) {
                try {
                    if (model.getBubbleType() == BubbleType.X) {
                        if (!sample.getValueAsDouble().equals(0d)) {
                            xList.add(sample);
                            maxX.set(Math.max(maxX.get(), sample.getValueAsDouble()));
                        }
                    } else if (model.getBubbleType() == BubbleType.Y) {
                        yList.add(sample);
                    }
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }

            if (model.getBubbleType() == BubbleType.Y && model.getEnPI()) {
                isEnPI = true;
                chartDataModelY = model;
            }
        }

        Map<Integer, Integer> modifiedX = new HashMap<>();
        Map<Integer, Double> modifiedY = new HashMap<>();
        Map<Integer, List<DateTime>> yDates = new HashMap<>();
        for (int i = 0; i < maxX.get() + 30; i = i + 30) {
            double upperBound = i + 30;
            for (JEVisSample sample : xList) {
                try {
                    if (sample.getValueAsDouble() >= (double) i && sample.getValueAsDouble() < upperBound) {
                        if (modifiedX.get(i) != null) {
                            int old = modifiedX.get(i);
                            modifiedX.remove(i);
                            modifiedX.put(i, old + 1);
                        } else {
                            modifiedX.put(i, 1);
                        }

                        if (yDates.get(i) != null) {
                            List<DateTime> old = yDates.get(i);
                            old.add(xList.get(xList.indexOf(sample)).getTimestamp());
                            yDates.remove(i);
                            yDates.put(i, old);
                        } else {
                            yDates.put(i, new ArrayList<>(Collections.singleton(xList.get(xList.indexOf(sample)).getTimestamp())));
                        }
                    }
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }
        }

        for (Map.Entry<Integer, List<DateTime>> entry : yDates.entrySet()) {
            double value = 0d;
            int size = 0;

            DateTime firstBeforeDate = null;
            DateTime lastDate = null;

            for (DateTime dt : entry.getValue()) {

                for (DateTime dateTime : entry.getValue()) {
                    if (firstBeforeDate == null && yList.size() > 0) {
                        try {
                            firstBeforeDate = yList.get(0).getTimestamp();
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                    } else if (dateTime.isBefore(dt) && dateTime.isAfter(firstBeforeDate)) {
                        firstBeforeDate = dateTime;
                    }
                }

                if (!isEnPI) {
                    for (JEVisSample sample : yList) {
                        try {
                            if (sample.getTimestamp().isAfter(firstBeforeDate) && sample.getTimestamp().isBefore(dt)) {
                                value += sample.getValueAsDouble();
                                size++;
                            }
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (entry.getValue().size() > 0) {
                        lastDate = entry.getValue().get(entry.getValue().size() - 1);
                    }
                }
            }
            if (size > 0 && !isEnPI) {
                value = value / size;
            } else if (chartDataModelY != null && chartDataModelY.getCalculationObject() != null) {
                try {
                    CalcJob calcJob = new CalcJobFactory().getCalcJobForTimeFrame(
                            new SampleHandler(),
                            chartDataModelY.getCalculationObject().getDataSource(),
                            chartDataModelY.getCalculationObject(),
                            firstBeforeDate,
                            lastDate,
                            true);

                    calcJob.execute();
                    List<JEVisSample> results = calcJob.getResults();
                    if (results.size() == 1) {
                        value = results.get(0).getValueAsDouble();
                    }

                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }

            modifiedY.put(entry.getKey(), value);
        }

        List<Bubble> bubbles = new ArrayList<>();
        modifiedX.forEach((aInteger, aInteger2) -> {
            minX.set(Math.min(minX.get(), aInteger));
            bubbles.add(new Bubble(aInteger.doubleValue(), modifiedY.get(aInteger), aInteger2.doubleValue()));
        });

        chart = new javafx.scene.chart.BubbleChart<Number, Number>(xAxis, yAxis);
        chart.setTitle(chartName);

        String xAxisTitle = "";
        String yAxisTitle = "";
        for (ChartDataModel model : chartDataModels) {
            if (model.getBubbleType() == BubbleType.X) {
                xAxisTitle = model.getObject().getName();
            } else if (model.getBubbleType() == BubbleType.Y) {
                yAxisTitle = model.getObject().getName();
            }
        }

        xAxis.setLabel(xAxisTitle);
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(minX.get() - 30);
        xAxis.setUpperBound(maxX.get() + 30);

        yAxis.setLabel(yAxisTitle);
        yAxis.setAutoRanging(true);

        javafx.scene.chart.XYChart.Series series1 = new javafx.scene.chart.XYChart.Series();
//        series1.setName("Arabica");

        for (Bubble bubble : bubbles) {
            XYChart.Data data = new XYChart.Data(bubble.getX(), bubble.getY(), bubble.getSize());
            series1.getData().add(data);
        }

        chart.getData().addAll(series1);
        chart.layout();
    }

    @Override
    public String getChartName() {
        return null;
    }

    @Override
    public void setTitle(String s) {

    }

    @Override
    public Integer getChartId() {
        return null;
    }

    @Override
    public void updateTable(MouseEvent mouseEvent, DateTime valueForDisplay) {

    }

    @Override
    public DateTime getStartDateTime() {
        return null;
    }

    @Override
    public DateTime getEndDateTime() {
        return null;
    }

    @Override
    public void updateChart() {

    }

    @Override
    public void setDataModels(List<ChartDataModel> chartDataModels) {

    }

    @Override
    public void setHideShowIcons(Boolean hideShowIcons) {

    }

    @Override
    public void setChartSettings(ChartSettingsFunction function) {
        //TODO: implement me, see PieChart
    }

    @Override
    public ChartPanManager getPanner() {
        return null;
    }

    @Override
    public JFXChartUtil getJfxChartUtil() {
        return null;
    }

    @Override
    public void setRegion(Region region) {

    }

    @Override
    public void showNote(MouseEvent mouseEvent) {

    }

    @Override
    public void applyColors() {
        for (int i = 0; i < hexColors.size(); i++) {
            Color currentColor = hexColors.get(i);
            String preIdent = ".default-color" + i;
            Node node = bubbleChart.lookup(preIdent + ".chart-series-area-fill");
            Node nodew = bubbleChart.lookup(preIdent + ".chart-series-area-line");
        }
    }

    @Override
    public DateTime getValueForDisplay() {
        return null;
    }

    @Override
    public DateTime getNearest() {
        return null;
    }

    @Override
    public void setValueForDisplay(DateTime valueForDisplay) {

    }

    @Override
    public javafx.scene.chart.Chart getChart() {
        return chart;
    }

    @Override
    public Region getRegion() {
        return null;
    }

    @Override
    public void initializeZoom() {

    }

    @Override
    public ObservableList<TableEntry> getTableData() {
        return tableData;
    }

    @Override
    public Period getPeriod() {
        return null;
    }
}
