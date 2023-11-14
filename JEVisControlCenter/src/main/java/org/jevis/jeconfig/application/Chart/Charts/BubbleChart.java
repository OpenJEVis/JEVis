package org.jevis.jeconfig.application.Chart.Charts;

import com.ibm.icu.text.NumberFormat;
import de.gsi.chart.marker.DefaultMarker;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.LineStyle;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.spi.DefaultDataSet;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.chart.BubbleType;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartElements.*;
import org.jevis.jeconfig.application.Chart.data.ChartData;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.Chart.data.ChartModel;
import org.jevis.jeconfig.plugin.charts.DataSettings;
import org.jevis.jeconfig.plugin.charts.ToolBarSettings;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class BubbleChart extends XYChart {
    private final List<Integer> noOfBubbles = new ArrayList<>();
    private final ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private final Map<Double, Double> modifiedX = new HashMap<>();
    private final Map<Double, Double> modifiedY = new HashMap<>();
    private final Map<Double, List<Boolean>> visibleSamples = new HashMap<>();
    private final TreeMap<Double, Double> sampleTreeMap = new TreeMap<>();
    private TableEntry tableEntry;
    private final Double nearest = 0d;
    private String xUnit;
    private String yUnit;
    private final List<Bubble> bubbles = new ArrayList<>();

    public BubbleChart(JEVisDataSource ds, ChartModel chartModel) {
        super(ds, chartModel);

        init();
    }

    @Override
    public void buildChart(ToolBarSettings toolBarSettings, DataSettings dataSettings) {
        Double groupingInterval = chartModel.getGroupingInterval();

        double totalJob = chartDataRows.size();

        if (showRawData) {
            totalJob *= 4;
        }

        if (showSum) {
            totalJob += 1;
        }

        if (calcRegression) {
            totalJob += 1;
        }

        JEConfig.getStatusBar().startProgressJob(JOB_NAME, totalJob, I18n.getInstance().getString("plugin.graph.message.startupdate"));

        this.showRawData = toolBarSettings.isShowRawData();
        this.showSum = toolBarSettings.isShowSum();
        this.showL1L2 = toolBarSettings.isShowL1L2();
        this.regressionType = toolBarSettings.getRegressionType();
        this.showIcons = toolBarSettings.isShowIcons();
        this.calcRegression = toolBarSettings.isCalculateRegression();
        this.polyRegressionDegree = toolBarSettings.getPolyRegressionDegree();
        this.chartId = chartModel.getChartId();
        this.chartName = chartModel.getChartName();
        this.chartType = chartModel.getChartType();
        for (ChartData chartData : chartModel.getChartData()) {
            if (chartData.getAxis() == 1) {
                hasSecondYAxis = true;
                break;
            }
        }

        chartDataRows.clear();
        for (ChartData chartData : chartModel.getChartData()) {
            ChartDataRow chartDataRow = new ChartDataRow(ds, chartData);
            chartDataRows.add(chartDataRow);
        }

        if (!chartDataRows.isEmpty()) {
            workDays = new WorkDays(chartDataRows.get(0).getObject());
            workDays.setEnabled(toolBarSettings.isCustomWorkday());
            chartDataRows.forEach(chartDataRow -> {
                chartDataRow.setAggregationPeriod(dataSettings.getAggregationPeriod());
                chartDataRow.setManipulationMode(dataSettings.getManipulationMode());
            });
        }

        hexColors.clear();
        chart.getDatasets().clear();
        tableData.clear();

        changedBoth = new Boolean[]{false, false};

        addManipulationToTitle = false;
        manipulationMode = ManipulationMode.NONE;

        List<JEVisSample> xList = new ArrayList<>();
        List<JEVisSample> yList = new ArrayList<>();

        AtomicReference<Double> minX = new AtomicReference<>(Double.MAX_VALUE);
        AtomicReference<Double> maxX = new AtomicReference<>(-Double.MAX_VALUE);
        AtomicReference<Double> minY = new AtomicReference<>(Double.MAX_VALUE);
        AtomicReference<Double> maxY = new AtomicReference<>(-Double.MAX_VALUE);
        boolean isEnPI = false;
        boolean async = false;
        ChartDataRow chartDataRowY = null;

        for (ChartDataRow model : chartDataRows) {
            for (JEVisSample sample : model.getSamples()) {
                try {
                    if (model.getBubbleType() == BubbleType.X) {
                        xList.add(sample);
                        minX.set(Math.min(minX.get(), sample.getValueAsDouble()));
                        maxX.set(Math.max(maxX.get(), sample.getValueAsDouble()));
                    } else if (model.getBubbleType() == BubbleType.Y && !sample.getValueAsDouble().equals(0d)) {
                        yList.add(sample);
                        if (!hexColors.contains(model.getColor())) {
                            hexColors.add(model.getColor());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (model.getBubbleType() == BubbleType.Y && model.isCalculation()) {
                isEnPI = true;
                chartDataRowY = model;
            } else {
                if (model.getPeriod().equals(Period.ZERO)) {
                    async = true;
                }
            }
        }

        Map<Double, List<DateTime>> yDates = new HashMap<>();
        AtomicReference<Double> startXAxis = new AtomicReference<>(0d);
        if (minX.get() < 0) {
            while (startXAxis.get() > minX.get()) {
                startXAxis.set(startXAxis.get() - groupingInterval);
            }
        } else {
            while (startXAxis.get() + groupingInterval < minX.get()) {
                startXAxis.set(startXAxis.get() + groupingInterval);
            }
        }

        Map<Double, List<JEVisSample>> xWithSamples = new HashMap<>();
        Map<Double, List<JEVisSample>> yWithSamples = new HashMap<>();

        for (double i = startXAxis.get(); i < maxX.get() + (2 * groupingInterval); i = i + groupingInterval) {
            double upperBound = i + groupingInterval;
            for (JEVisSample sample : xList) {
                try {
                    if (sample.getValueAsDouble() >= i && sample.getValueAsDouble() < upperBound) {
                        if (modifiedX.get(i) != null) {
                            double old = modifiedX.get(i);
                            modifiedX.remove(i);
                            modifiedX.put(i, old + 1);

                            List<Boolean> oldBool = visibleSamples.get(i);
                            visibleSamples.remove(i);
                            oldBool.add(Boolean.TRUE);
                            visibleSamples.put(i, oldBool);
                        } else {
                            modifiedX.put(i, 1.0);
                            List<Boolean> list = new ArrayList<>();
                            list.add(Boolean.TRUE);
                            visibleSamples.put(i, list);
                        }

                        if (xWithSamples.get(i) != null) {
                            List<JEVisSample> newList = new ArrayList<>(xWithSamples.get(i));
                            newList.add(sample);
                            xWithSamples.remove(i);
                            xWithSamples.put(i, newList);
                        } else {
                            List<JEVisSample> newList = new ArrayList<>();
                            newList.add(sample);
                            xWithSamples.put(i, newList);
                        }

                        if (yDates.get(i) != null) {
                            List<DateTime> old = yDates.get(i);
                            old.add(xList.get(xList.indexOf(sample)).getTimestamp());
                            yDates.remove(i);
                            yDates.put(i, old);
                        } else {
                            yDates.put(i, new ArrayList<>(Collections.singleton(xList.get(xList.indexOf(sample)).getTimestamp())));
                        }

                        if (yWithSamples.get(i) != null) {
                            List<JEVisSample> newList = new ArrayList<>(yWithSamples.get(i));
                            for (JEVisSample ySample : yList) {
                                if (ySample.getTimestamp().equals(sample.getTimestamp())) {
                                    newList.add(ySample);
                                }
                            }

                            yWithSamples.remove(i);
                            yWithSamples.put(i, newList);
                        } else {
                            List<JEVisSample> newList = new ArrayList<>();
                            for (JEVisSample ySample : yList) {
                                if (ySample.getTimestamp().equals(sample.getTimestamp())) {
                                    newList.add(ySample);
                                }
                            }
                            yWithSamples.put(i, newList);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        for (Map.Entry<Double, List<DateTime>> entry : yDates.entrySet()) {
            double value = 0d;
            int size = 0;

            DateTime firstBeforeDate = null;
            DateTime lastDate = null;

            for (DateTime dt : entry.getValue()) {

                for (DateTime dateTime : entry.getValue()) {
                    if (firstBeforeDate == null && !yList.isEmpty()) {
                        try {
                            firstBeforeDate = yList.get(0).getTimestamp();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (dateTime.isBefore(dt) && dateTime.isAfter(firstBeforeDate)) {
                        firstBeforeDate = dateTime;
                    }
                }

                if (!isEnPI) {
                    for (JEVisSample sample : yList) {
                        try {
                            if (!async) {
                                if ((sample.getTimestamp().isAfter(firstBeforeDate) && sample.getTimestamp().isBefore(dt))
                                        || sample.getTimestamp().equals(dt)) {
                                    value += sample.getValueAsDouble();
                                    size++;
                                }
                            } else {
                                if (sample.getTimestamp().equals(dt)) {
                                    value += sample.getValueAsDouble();
                                    size++;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (!entry.getValue().isEmpty()) {
                        lastDate = entry.getValue().get(entry.getValue().size() - 1);
                    }
                }
            }
            if (size > 0 && !isEnPI) {
                value = value / size;
            } else if (chartDataRowY != null && chartDataRowY.getCalculationObject() != null) {
                try {
                    CalcJob calcJob = new CalcJobFactory().getCalcJobForTimeFrame(
                            new SampleHandler(),
                            chartDataRowY.getCalculationObject().getDataSource(),
                            chartDataRowY.getCalculationObject(),
                            firstBeforeDate,
                            lastDate,
                            true);

                    calcJob.execute();
                    List<JEVisSample> results = calcJob.getResults();
                    if (results.size() == 1) {
                        value = results.get(0).getValueAsDouble();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            modifiedY.put(entry.getKey(), value);
        }

        List<Double> arrayList = new ArrayList<>();
        AtomicReference<Double> maxSize = new AtomicReference<>((double) 0);
        modifiedX.forEach((aInteger, aInteger2) -> {
            minX.set(Math.min(minX.get(), aInteger));
            minY.set(Math.min(minY.get(), modifiedY.get(aInteger)));
            maxY.set(Math.max(maxY.get(), modifiedY.get(aInteger)));
            maxSize.set(Math.max(maxSize.get(), aInteger2.doubleValue()));
            bubbles.add(new Bubble(xWithSamples.get(aInteger), yWithSamples.get(aInteger), aInteger.doubleValue(), modifiedY.get(aInteger), aInteger2.doubleValue(), visibleSamples.get(aInteger)));
            sampleTreeMap.put(aInteger.doubleValue(), modifiedY.get(aInteger));
            arrayList.add(modifiedY.get(aInteger));
        });

        Platform.runLater(() -> {
            chart.setTitle(getChartName());
            chart.setLegendVisible(false);
        });

        String xAxisTitle = "";
        xUnit = "";
        String yAxisTitle = "";
        yUnit = "";
        for (ChartDataRow model : chartDataRows) {
            if (model.getBubbleType() == BubbleType.X) {
                xAxisTitle = model.getObject().getName();
                xUnit = model.getUnitLabel();
            } else if (model.getBubbleType() == BubbleType.Y) {
                yAxisTitle = model.getObject().getName();
                yUnit = model.getUnitLabel();
            }
        }

        String finalXAxisTitle = xAxisTitle;
        String finalYAxisTitle = yAxisTitle;
        Platform.runLater(() -> {
            getPrimaryDateAxis().setName(finalXAxisTitle);
            getPrimaryDateAxis().setUnit(xUnit);
            getPrimaryDateAxis().setAutoRanging(false);
            getPrimaryDateAxis().setMin(minX.get() - ((maxX.get() - minX.get()) * 0.25));
            getPrimaryDateAxis().setMax(maxX.get() + ((maxX.get() - minX.get()) * 0.25));
            getPrimaryDateAxis().setTickUnit(30);

            getY1Axis().setName(finalYAxisTitle);
            getY1Axis().setUnit(yUnit);
            getY1Axis().setAutoRanging(false);
            if (!chartModel.isFixYAxisToZero()) {
                getY1Axis().setMin(minY.get() - ((maxY.get() - minY.get()) * 0.25));
                getY1Axis().setForceZeroInRange(false);
            } else {
                getY1Axis().setMin(0);
                getY1Axis().setForceZeroInRange(true);
            }
            getY1Axis().setMax(maxY.get() + ((maxY.get() - minY.get()) * 0.25));
        });

        tableEntry = new TableEntry(yAxisTitle + " : " + xAxisTitle);
        tableEntry.setColor(hexColors.get(0));
        Double[] item = arrayList.toArray(new Double[arrayList.size()]);
        double[] doubleArray = ArrayUtils.toPrimitive(item);
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(doubleArray);
        Double standardDeviation = descriptiveStatistics.getStandardDeviation();
        Double variance = descriptiveStatistics.getVariance();

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

        DefaultDataSet bubbleDataSet = new DefaultDataSet("bubbles");
        Color color = hexColors.get(0).deriveColor(0, 1, 1, 0.9);
//        bubbleDataSet.setStyle("strokeColor=" + color + "; fillColor=" + color + "markerType=circle;");

        for (Bubble bubble : bubbles) {
            int count = bubbles.indexOf(bubble);
            bubbleDataSet.add(bubble.getX(), bubble.getY(), "bubble " + count);

//            String markerSize = "markerSize=" + 40 * Math.sqrt(bubble.getSize() / maxSize.get()) + "; index="
//                    + count + ";";
            String markerSize = "markerSize=" + 40 * Math.sqrt(bubble.getSize()) + "; index=" + count + ";";

            bubbleDataSet.addDataStyle(count, markerSize);
            bubbleDataSet.addDataStyle(count, markerSize + "; markerColor=" + color + "; markerType=circle;");
        }

        noOfBubbles.add(bubbles.size());

        ErrorDataSetRenderer rendererY1 = new ErrorDataSetRenderer();
        CustomBubbleChartRenderer customBubbleChartRenderer = new CustomBubbleChartRenderer(bubbles);

        rendererY1.setMarkerSize(5);
        rendererY1.setPolyLineStyle(LineStyle.NONE);
        rendererY1.setErrorType(ErrorStyle.NONE);
        rendererY1.setDrawMarker(true);
        rendererY1.setAssumeSortedData(false);
        rendererY1.setMarker(DefaultMarker.DIAMOND);
        rendererY1.setDrawBubbles(false);

        rendererY1.getDatasets().add(bubbleDataSet);
        customBubbleChartRenderer.getDatasets().add(bubbleDataSet);

        if (calcRegression) {
            ErrorDataSetRenderer trendLineRenderer = new ErrorDataSetRenderer();
            trendLineRenderer.setPolyLineStyle(LineStyle.NORMAL);
            trendLineRenderer.setDrawMarker(false);
            trendLineRenderer.setMarkerSize(0);
            trendLineRenderer.setAssumeSortedData(false);

            trendLineRenderer.getDatasets().addAll(drawRegression(bubbleDataSet, color, getChartName()));
            Platform.runLater(() -> chart.getRenderers().setAll(rendererY1, customBubbleChartRenderer, trendLineRenderer));
        } else {
            Platform.runLater(() -> chart.getRenderers().setAll(rendererY1, customBubbleChartRenderer));
        }

    }

    @Override
    public XYChartSerie generateSerie(Boolean[] changedBoth, ChartDataRow singleRow) throws JEVisException {
        XYChartSerie serie = new BubbleChartSerie(chartModel, singleRow, showIcons, false);

        hexColors.add(singleRow.getColor());

        /**
         * check if timestamps are in serie
         */

        if (serie.getTimeStampFromFirstSample().isBefore(timeStampOfFirstSample.get())) {
            timeStampOfFirstSample.set(serie.getTimeStampFromFirstSample());
            changedBoth[0] = true;
        }

        if (serie.getTimeStampFromLastSample().isAfter(timeStampOfLastSample.get())) {
            timeStampOfLastSample.set(serie.getTimeStampFromLastSample());
            changedBoth[1] = true;
        }

        return serie;
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
        return chartId;
    }

    @Override
    public void updateTableZoom(double lowerBound, double upperBound) {

    }

    @Override
    public void setRegion(Region region) {

    }

    @Override
    public List<ChartDataRow> getChartDataRows() {
        return chartDataRows;
    }

    @Override
    public ChartModel getChartModel() {
        return chartModel;
    }

    @Override
    public ObservableList<TableEntry> getTableData() {
        return tableData;
    }

    @Override
    public Period getPeriod() {
        return null;
    }

    public String getyUnit() {
        return yUnit;
    }

    public TreeMap<Double, Double> getSampleTreeMap() {
        return sampleTreeMap;
    }

    public String getxUnit() {
        return xUnit;
    }

    public List<Bubble> getBubbles() {
        return bubbles;
    }

    public NumberFormat getNf() {
        return nf;
    }

    public Map<Double, List<Boolean>> getVisibleSamples() {
        return visibleSamples;
    }
}
