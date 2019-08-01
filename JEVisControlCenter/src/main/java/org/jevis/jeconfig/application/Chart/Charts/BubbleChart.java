package org.jevis.jeconfig.application.Chart.Charts;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.chart.BubbleType;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.database.SampleHandler;
import org.jevis.jeconfig.application.Chart.ChartElements.Bubble;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisBubbleChart;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.regression.RegressionType;
import org.jevis.jeconfig.application.Chart.Zoom.ChartPanManager;
import org.jevis.jeconfig.application.Chart.Zoom.JFXChartUtil;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class BubbleChart implements Chart {
    private final RegressionType regressionType;
    private final Boolean calcRegression;
    private final Integer polyRegressionDegree;
    private List<Color> hexColors = new ArrayList<>();
    private List<Integer> noOfBubbles = new ArrayList<>();
    MultiAxisBubbleChart<Number, Number> chart;
    private Region chartRegion;
    private ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private final Map<Integer, Integer> modifiedX;
    private final Map<Integer, Double> modifiedY;
    private final TreeMap<Double, Double> sampleTreeMap = new TreeMap<>();
    private TableEntry tableEntry;
    private Double nearest = 0d;
    private String xUnit;
    private String yUnit;

    public BubbleChart(List<ChartDataModel> chartDataModels, Boolean showRawData, Boolean showSum, Boolean hideShowIcons, Boolean calcRegression, RegressionType regressionType, Integer polyRegressionDegree, Integer chartId, String chartName) {
        this.regressionType = regressionType;
        this.calcRegression = calcRegression;
        this.polyRegressionDegree = polyRegressionDegree;

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        List<JEVisSample> xList = new ArrayList<>();
        List<JEVisSample> yList = new ArrayList<>();

        AtomicReference<Double> minX = new AtomicReference<>(Double.MAX_VALUE);
        AtomicReference<Double> maxX = new AtomicReference<>(-Double.MAX_VALUE);
        AtomicReference<Double> minY = new AtomicReference<>(Double.MAX_VALUE);
        AtomicReference<Double> maxY = new AtomicReference<>(-Double.MAX_VALUE);
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
                        if (!hexColors.contains(model.getColor())) {
                            hexColors.add(model.getColor());
                        }
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

        modifiedX = new HashMap<>();
        modifiedY = new HashMap<>();
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
        List<Double> arrayList = new ArrayList<>();
        modifiedX.forEach((aInteger, aInteger2) -> {
            minX.set(Math.min(minX.get(), aInteger));
            minY.set(Math.min(minY.get(), modifiedY.get(aInteger)));
            maxY.set(Math.max(maxY.get(), modifiedY.get(aInteger)));
            bubbles.add(new Bubble(aInteger.doubleValue(), modifiedY.get(aInteger), aInteger2.doubleValue()));
            sampleTreeMap.put(aInteger.doubleValue(), modifiedY.get(aInteger));
            arrayList.add(modifiedY.get(aInteger));
        });

        chart = new MultiAxisBubbleChart<Number, Number>(xAxis, yAxis, null);
        if (calcRegression) {
            chart.setRegression(0, regressionType, polyRegressionDegree);
        }
        chart.setTitle(chartName);
        chart.setLegendVisible(false);

        String xAxisTitle = "";
        xUnit = "";
        String yAxisTitle = "";
        yUnit = "";
        for (ChartDataModel model : chartDataModels) {
            if (model.getBubbleType() == BubbleType.X) {
                xAxisTitle = model.getObject().getName();
                xUnit = model.getUnitLabel();
            } else if (model.getBubbleType() == BubbleType.Y) {
                yAxisTitle = model.getObject().getName();
                yUnit = model.getUnitLabel();
            }
        }

        xAxis.setLabel(xAxisTitle);
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(Math.max(minX.get() - 30, 0d));
        xAxis.setUpperBound(maxX.get() + 30);
        xAxis.setTickUnit(30);

        yAxis.setLabel(yAxisTitle);
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(Math.max(minY.get(), 0d) * 0.75);
        yAxis.setUpperBound(maxY.get() * 1.25);
        yAxis.setForceZeroInRange(false);

        tableEntry = new TableEntry(yAxisTitle + " : " + xAxisTitle);
        tableEntry.setColor(hexColors.get(0));
        Double[] item = arrayList.toArray(new Double[arrayList.size()]);
        double[] doubleArray = ArrayUtils.toPrimitive(item);
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(doubleArray);
        Double standardDeviation = descriptiveStatistics.getStandardDeviation();
        Double variance = descriptiveStatistics.getVariance();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);

        if (standardDeviation.isNaN()) {
            tableEntry.setStandardDeviation("-");
        } else {
            tableEntry.setStandardDeviation(nf.format(standardDeviation));
        }
        if (variance.isNaN()) {
            tableEntry.setVariance("-");
        } else {
            tableEntry.setVariance(nf.format(variance));
        }

        tableData.add(tableEntry);
        MultiAxisBubbleChart.Series series1 = new MultiAxisBubbleChart.Series();
//        series1.setName("Arabica");

        for (Bubble bubble : bubbles) {
            MultiAxisBubbleChart.Data data = new MultiAxisBubbleChart.Data(bubble.getX(), bubble.getY(), bubble.getSize());

            series1.getData().add(data);
        }

        noOfBubbles.add(bubbles.size());

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
        Point2D mouseCoordinates = null;
        if (mouseEvent != null) mouseCoordinates = new Point2D(mouseEvent.getSceneX(), mouseEvent.getSceneY());

        double x = ((MultiAxisBubbleChart) getChart()).getXAxis().sceneToLocal(Objects.requireNonNull(mouseCoordinates)).getX();

        Double displayValue = ((NumberAxis) ((MultiAxisBubbleChart) getChart()).getXAxis()).getValueForDisplay(x).doubleValue();

        if (displayValue != null) {
            setValueForDisplay(valueForDisplay);
            double finalDisplayValue = displayValue;
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);

            try {

                nearest = null;
                if (sampleTreeMap.get(finalDisplayValue) != null) {
                    nearest = finalDisplayValue;
                } else {
                    nearest = sampleTreeMap.lowerKey(finalDisplayValue);
                }

                Double yValue = sampleTreeMap.get(nearest);
                AtomicReference<Double> xValue = new AtomicReference<>(finalDisplayValue);
                sampleTreeMap.forEach((aDouble, aDouble2) -> {
                    if (aDouble2.equals(yValue)) {
                        xValue.set(aDouble);
                    }
                });

                String formattedX = nf.format(xValue.get());
                String formattedY = nf.format(yValue);
                if (!xUnit.equals("")) {
                    tableEntry.setxValue(formattedX + " " + xUnit);
                } else {
                    tableEntry.setxValue(formattedX);
                }
                if (!yUnit.equals("")) {
                    tableEntry.setyValue(formattedY + " " + yUnit);
                } else {
                    tableEntry.setyValue(formattedY);
                }

            } catch (Exception ex) {
            }
        }
    }

    @Override
    public void updateTableZoom(Long lowerBound, Long upperBound) {

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
            currentColor = currentColor.deriveColor(0, 1, 1, 0.7);
            String hexColor = toRGBCode(currentColor);
            int noOfBubbles = this.noOfBubbles.get(i);
            String preIdent = "";
            for (int j = 0; j < noOfBubbles; j++) {
                preIdent = ".chart-bubble.series" + i + ".data" + j + ".default-color0";

                Node node = chart.lookup(preIdent);
                if (node != null) {
                    node.setStyle("-fx-bubble-fill: " + hexColor + ";");
                }
            }
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
