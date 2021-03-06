package org.jevis.jeconfig.application.Chart.Charts;

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
import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class BubbleChart extends XYChart {
    private final List<Color> hexColors = new ArrayList<>();
    private final List<Integer> noOfBubbles = new ArrayList<>();
    private final ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private final Map<Integer, Integer> modifiedX = new HashMap<>();
    private final Map<Integer, Double> modifiedY = new HashMap<>();
    private final TreeMap<Double, Double> sampleTreeMap = new TreeMap<>();
    private TableEntry tableEntry;
    private final Double nearest = 0d;
    private String xUnit;
    private String yUnit;

    public BubbleChart() {

        init();
    }

    @Override
    public void buildChart(AnalysisDataModel dataModel, List<ChartDataRow> dataRows, ChartSetting chartSetting) {

        this.analysisDataModel = dataModel;
        this.chartDataRows = dataRows;
        Long groupingInterval = chartSetting.getGroupingInterval();

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

        this.showRawData = dataModel.getShowRawData();
        this.showSum = dataModel.getShowSum();
        this.showL1L2 = dataModel.getShowL1L2();
        this.regressionType = dataModel.getRegressionType();
        this.showIcons = dataModel.getShowIcons();
        this.calcRegression = dataModel.calcRegression();
        this.polyRegressionDegree = dataModel.getPolyRegressionDegree();
        this.chartId = chartSetting.getId();
        this.chartName = chartSetting.getName();
        this.chartType = chartSetting.getChartType();
        this.addSeriesOfType = dataModel.getAddSeries();
        for (ChartDataRow chartDataModel : dataRows) {
            if (chartDataModel.getAxis() == 1) {
                hasSecondAxis = true;
                break;
            }
        }

        if (!chartDataRows.isEmpty()) {
            workDays = new WorkDays(chartDataRows.get(0).getObject());
            workDays.setEnabled(dataModel.isCustomWorkDay());
        }

        hexColors.clear();
        chart.getDatasets().clear();
        tableData.clear();

        changedBoth = new Boolean[]{false, false};

        addManipulationToTitle = new AtomicBoolean(false);
        manipulationMode = new AtomicReference<>(ManipulationMode.NONE);

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
                    if (model.getBubbleType() == BubbleType.X && !sample.getValueAsDouble().equals(0d)) {
                        xList.add(sample);
                        maxX.set(Math.max(maxX.get(), sample.getValueAsDouble()));
                    } else if (model.getBubbleType() == BubbleType.Y && !sample.getValueAsDouble().equals(0d)) {
                        yList.add(sample);
                        if (!hexColors.contains(ColorHelper.toColor(model.getColor()))) {
                            hexColors.add(ColorHelper.toColor(model.getColor()));
                        }
                    }
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }

            if (model.getBubbleType() == BubbleType.Y && model.getEnPI()) {
                isEnPI = true;
                chartDataRowY = model;
            } else {
                if (model.getPeriod().equals(Period.ZERO)) {
                    async = true;
                }
            }
        }

        Map<Integer, List<DateTime>> yDates = new HashMap<>();
        for (int i = -groupingInterval.intValue() / 2; i < maxX.get() + (2 * groupingInterval); i = i + groupingInterval.intValue()) {
            double upperBound = i + groupingInterval;
            int meanX = i + groupingInterval.intValue() / 2;
            for (JEVisSample sample : xList) {
                try {
                    if (sample.getValueAsDouble() >= (double) i && sample.getValueAsDouble() < upperBound) {
                        if (modifiedX.get(meanX) != null) {
                            int old = modifiedX.get(meanX);
                            modifiedX.remove(meanX);
                            modifiedX.put(meanX, old + 1);
                        } else {
                            modifiedX.put(meanX, 1);
                        }

                        if (yDates.get(meanX) != null) {
                            List<DateTime> old = yDates.get(meanX);
                            old.add(xList.get(xList.indexOf(sample)).getTimestamp());
                            yDates.remove(meanX);
                            yDates.put(meanX, old);
                        } else {
                            yDates.put(meanX, new ArrayList<>(Collections.singleton(xList.get(xList.indexOf(sample)).getTimestamp())));
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

                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }

            modifiedY.put(entry.getKey(), value);
        }

        List<Bubble> bubbles = new ArrayList<>();
        List<Double> arrayList = new ArrayList<>();
        AtomicReference<Double> maxSize = new AtomicReference<>((double) 0);
        modifiedX.forEach((aInteger, aInteger2) -> {
            minX.set(Math.min(minX.get(), aInteger));
            minY.set(Math.min(minY.get(), modifiedY.get(aInteger)));
            maxY.set(Math.max(maxY.get(), modifiedY.get(aInteger)));
            maxSize.set(Math.max(maxSize.get(), aInteger2.doubleValue()));
            bubbles.add(new Bubble(aInteger.doubleValue(), modifiedY.get(aInteger), aInteger2.doubleValue()));
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
            getDateAxis().setName(finalXAxisTitle);
            getDateAxis().setUnit(xUnit);
            getDateAxis().setAutoRanging(false);
            getDateAxis().setMin(Math.max(minX.get() - groupingInterval, 0d));
            getDateAxis().setMax(maxX.get() + groupingInterval);
            getDateAxis().setTickUnit(30);

            getY1Axis().setName(finalYAxisTitle);
            getY1Axis().setUnit(yUnit);
            getY1Axis().setAutoRanging(false);
            getY1Axis().setMin(Math.max(minY.get(), 0d) * 0.75);
            getY1Axis().setMax(maxY.get() * 1.25);
            getY1Axis().setForceZeroInRange(false);
        });

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
        XYChartSerie serie = new BubbleChartSerie(singleRow, showIcons, false);

        hexColors.add(ColorHelper.toColor(singleRow.getColor()));

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

        /**
         * check if theres a manipulation for changing the x axis values into duration instead of concrete timestamps
         */

        checkManipulation(singleRow);
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
}
