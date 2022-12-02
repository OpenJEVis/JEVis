package org.jevis.jeconfig.application.Chart.Charts;

import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.NumberFormat;
import de.gsi.chart.axes.spi.format.DefaultTimeFormatter;
import de.gsi.chart.renderer.LineStyle;
import de.gsi.chart.renderer.Renderer;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.chart.ui.geometry.Side;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.spi.DoubleDataSet;
import de.gsi.dataset.spi.DoubleErrorDataSet;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.commons.unit.ChartUnits.ChartUnits;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.jeapi.ws.JEVisDataSourceWS;
import org.jevis.jeapi.ws.JEVisObjectWS;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartElements.*;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.Charts.regression.*;
import org.jevis.jeconfig.application.Chart.data.ChartData;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.Chart.data.ChartModel;
import org.jevis.jeconfig.application.Chart.data.ValueWithDateTime;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.jevis.jeconfig.plugin.charts.DataSettings;
import org.jevis.jeconfig.plugin.charts.ToolBarSettings;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.jevis.commons.dataprocessing.ManipulationMode.RUNNING_MEAN;

public class XYChart implements Chart {
    public static final Image taskImage = JEConfig.getImage("Analysis.png");
    private static final Logger logger = LogManager.getLogger(XYChart.class);
    public static String JOB_NAME = "Create series";
    final JEVisDataSource ds;
    final List<Color> hexColors = new ArrayList<>();
    private final List<String> unitY1 = new ArrayList<>();
    private final List<String> unitY2 = new ArrayList<>();
    private final StringBuilder regressionFormula = new StringBuilder();
    int polyRegressionDegree;
    RegressionType regressionType;
    Boolean calcRegression = false;
    Boolean showIcons = true;
    DefaultDateAxis primaryDateAxis = new DefaultDateAxis();
    List<ChartDataRow> chartDataRows;
    Boolean showRawData = false;
    Boolean showSum = false;
    CustomNumericAxis y1Axis = new CustomNumericAxis();
    ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    DateTime now = DateTime.now();
    AtomicReference<DateTime> timeStampOfFirstSample = new AtomicReference<>(now);
    AtomicReference<DateTime> timeStampOfLastSample = new AtomicReference<>(new DateTime(1990, 1, 1, 0, 0, 0));
    CustomNumericAxis y2Axis = new CustomNumericAxis();
    Boolean showL1L2 = false;
    de.gsi.chart.Chart chart;
    Integer chartId;
    ChartType chartType = ChartType.LINE;
    Double minValue = Double.MAX_VALUE;
    Double maxValue = -Double.MAX_VALUE;
    boolean asDuration = false;
    String chartName;
    Boolean addManipulationToTitle;
    ManipulationMode manipulationMode;
    Boolean[] changedBoth;
    WorkDays workDays = new WorkDays(null);
    boolean hasSecondYAxis = false;
    DefaultDateAxis secondaryDateAxis = new DefaultDateAxis();
    NumberFormat nf = NumberFormat.getInstance(I18n.getInstance().getLocale());
    ChartModel chartModel;
    List<XYChartSerie> xyChartSerieList = new ArrayList<>();
    private Region areaChartRegion;
    private Period period;
    private DateTimeFormatter dtfOutLegend = DateTimeFormat.forPattern("EE. dd.MM.yyyy HH:mm");
    private XYChartSerie y1SumSerie;
    private XYChartSerie y2SumSerie;

    public XYChart(JEVisDataSource ds, ChartModel chartModel) {
        this.ds = ds;
        this.chartModel = chartModel;
        init();
    }

    public void createChart(ToolBarSettings toolBarSettings, DataSettings dataSettings) {
        this.createChart(toolBarSettings, dataSettings, false);
    }

    public void createChart(ToolBarSettings toolBarSettings, DataSettings dataSettings, boolean instant) {
        this.createChart(new ArrayList<>(), toolBarSettings, dataSettings, instant);
    }

    public void createChart(List<ChartDataRow> chartDataRows, ToolBarSettings toolBarSettings, DataSettings dataSettings, boolean instant) {
        this.chartDataRows = chartDataRows;
        if (!instant) {

            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    try {
                        buildChart(toolBarSettings, dataSettings);
                    } catch (Exception e) {
                        this.failed();
                        logger.error("Could not build chart {}", XYChart.this.chartModel.getChartName(), e);
                    } finally {
                        succeeded();
                    }
                    return null;
                }
            };

            JEConfig.getStatusBar().addTask(XYChart.class.getName(), task, taskImage, true);
        } else {
            buildChart(toolBarSettings, dataSettings);
        }
    }

    public void buildChart(ToolBarSettings toolBarSettings, DataSettings dataSettings) {

        try {
            DefaultDateAxis.setSite(CommonMethods.getFirstParentalObjectOfClass(dataSettings.getCurrentAnalysis(), "Building"));
        } catch (Exception e) {
            logger.error("Could not set site for date axis", e);
        }

        CustomStringConverter tickLabelFormatter1 = new CustomStringConverter(this.chartModel.getMinFractionDigits(), this.chartModel.getMaxFractionDigits());
        CustomStringConverter tickLabelFormatter2 = new CustomStringConverter(this.chartModel.getMinFractionDigits(), this.chartModel.getMaxFractionDigits());
        Platform.runLater(() -> {
            y1Axis.setTickLabelFormatter(tickLabelFormatter1);

            y2Axis.setTickLabelFormatter(tickLabelFormatter2);
        });

        this.nf.setMinimumFractionDigits(this.chartModel.getMinFractionDigits());
        this.nf.setMaximumFractionDigits(this.chartModel.getMaxFractionDigits());

        this.showRawData = toolBarSettings.isShowRawData();
        this.showSum = toolBarSettings.isShowSum();
        this.showL1L2 = toolBarSettings.isShowL1L2();
        this.regressionType = toolBarSettings.getRegressionType();
        this.showIcons = toolBarSettings.isShowIcons();
        this.calcRegression = toolBarSettings.isCalculateRegression();
        this.polyRegressionDegree = toolBarSettings.getPolyRegressionDegree();
        this.chartId = this.chartModel.getChartId();
        this.chartName = this.chartModel.getChartName();
        this.chartType = this.chartModel.getChartType();

        if (chartDataRows.isEmpty()) {
            chartModel.getChartData().forEach(chartData -> chartDataRows.add(new ChartDataRow(ds, chartData)));
        }

        for (ChartDataRow chartDataRow : chartDataRows) {
            if (chartDataRow.getAxis() == 1) {
                hasSecondYAxis = true;
                break;
            }
        }

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


        if (!chartDataRows.isEmpty()) {
            workDays = new WorkDays(chartDataRows.get(0).getObject());
            workDays.setEnabled(toolBarSettings.isCustomWorkday());
        }

        hexColors.clear();
        chart.getDatasets().clear();
        tableData.clear();

        changedBoth = new Boolean[]{false, false};

        generatePeriod();

        addManipulationToTitle = false;
        manipulationMode = dataSettings.getManipulationMode();

        asDuration = dataSettings.getManipulationMode().equals(ManipulationMode.SORTED_MIN)
                || dataSettings.getManipulationMode().equals(ManipulationMode.SORTED_MAX);

        addManipulationToTitle = (dataSettings.getManipulationMode().equals(RUNNING_MEAN)
                || dataSettings.getManipulationMode().equals(ManipulationMode.CENTRIC_RUNNING_MEAN));

        ChartDataRow sumModelY1 = null;
        ChartDataRow sumModelY2 = null;

        if (chartType != ChartType.BUBBLE) {
            for (ChartDataRow chartDataRow : chartDataRows) {
                try {
                    if (showRawData && chartDataRow.getDataProcessor() != null) {
                        xyChartSerieList.add(generateSerie(changedBoth, getRawDataModel(chartModel, chartDataRow)));
                    }

                    xyChartSerieList.add(generateSerie(changedBoth, chartDataRow));

                    if (chartDataRow.hasForecastData()) {
                        try {
                            XYChartSerie forecast = new XYChartSerie(this.chartModel, chartDataRow, showIcons, true);

                            hexColors.add(ColorHelper.colorToBrighter(chartDataRow.getColor()));
                            xyChartSerieList.add(forecast);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (showSum && sumModelY1 == null && chartDataRow.getAxis() == 0) {
                        sumModelY1 = chartDataRow.clone();
                    }

                    if (showSum && sumModelY2 == null && chartDataRow.getAxis() == 1) {
                        sumModelY2 = chartDataRow.clone();
                    }

                } catch (Exception e) {
                    logger.error("Error: Cant create series for data rows: ", e);
                }
            }
        } else {

        }

        if (chartModel.getChartType() == ChartType.STACKED_AREA || chartModel.getChartType() == ChartType.STACKED_COLUMN
                || chartModel.getChartData().stream().anyMatch(chartData -> chartData.getChartType() == ChartType.STACKED_AREA || chartData.getChartType() == ChartType.STACKED_COLUMN)) {

            boolean hasY1 = xyChartSerieList.stream().anyMatch(chartSerie -> chartSerie.getSingleRow().getAxis() == 0 &&
                    (((chartModel.getChartType() == ChartType.STACKED_AREA || chartModel.getChartType() == ChartType.STACKED_COLUMN)
                            && chartSerie.getSingleRow().getChartType() == ChartType.DEFAULT)
                            || chartSerie.getSingleRow().getChartType() == ChartType.STACKED_AREA
                            || chartSerie.getSingleRow().getChartType() == ChartType.STACKED_COLUMN));
            boolean hasY2 = xyChartSerieList.stream().anyMatch(chartSerie -> chartSerie.getSingleRow().getAxis() == 1 &&
                    (((chartModel.getChartType() == ChartType.STACKED_AREA || chartModel.getChartType() == ChartType.STACKED_COLUMN)
                            && chartSerie.getSingleRow().getChartType() == ChartType.DEFAULT)
                            || chartSerie.getSingleRow().getChartType() == ChartType.STACKED_AREA
                            || chartSerie.getSingleRow().getChartType() == ChartType.STACKED_COLUMN));

            if (hasY1) {
                List<XYChartSerie> y1Series = xyChartSerieList.stream().filter(serie -> serie.getSingleRow().getAxis() == 0 && (serie.getSingleRow().getChartType() == ChartType.DEFAULT ||
                        serie.getSingleRow().getChartType() == ChartType.STACKED_AREA || serie.getSingleRow().getChartType() == ChartType.STACKED_COLUMN)).collect(Collectors.toList());

                String name = "~" + I18n.getInstance().getString("plugin.graph.chart.header.stacked.y1");
                TableEntry sumEntry = new TableEntry(name);
                sumEntry.setColor(Color.BLACK);
                ChartDataRow firstY1Row = y1Series.get(0).getSingleRow();
                sumEntry.setPeriod(y1Series.get(0).getTableEntry().getPeriod());
                y1SumSerie = new XYChartSerie();
                ChartDataRow chartDataRow = new ChartDataRow(ds, new ChartData());
                JEVisUnit unit = firstY1Row.getUnit();
                chartDataRow.setColor(Color.BLACK);
                chartDataRow.setChartType(ChartType.LINE);
                chartDataRow.setUnit(unit);
                chartDataRow.setAxis(0);
                chartDataRow.setScaleFactor(firstY1Row.getScaleFactor());
                chartDataRow.setTimeFactor(firstY1Row.getTimeFactor());
                chartDataRow.setPeriod(firstY1Row.getPeriod());
                chartDataRow.setFormatString(firstY1Row.getFormatString());
                chartDataRow.setSomethingChanged(false);
                y1SumSerie.setSingleRow(chartDataRow);
                DoubleDataSet noteDataSet = new DoubleDataSet(name);
                y1SumSerie.setNoteDataSet(noteDataSet);

                TreeMap<DateTime, JEVisSample> sampleMap = new TreeMap<>();
                double min = Double.MAX_VALUE;
                double max = -Double.MAX_VALUE;
                double avg = 0d;
                double sum = 0d;

                y1SumSerie.setTableEntry(sumEntry);
                y1SumSerie.setSampleMap(sampleMap);
                AlphanumComparator ac = new AlphanumComparator();
                y1Series.sort((o1, o2) -> ac.compare(o2.getTableEntryName(), o1.getTableEntryName()));
                List<XYChartSerie> otherSeries = y1Series.stream().filter(y1Serie -> y1Series.indexOf(y1Serie) > 0).collect(Collectors.toList());
                List<DoubleDataSet> dataSets = new ArrayList<>();
                for (XYChartSerie serie : y1Series) {
                    DoubleDataSet ds = serie.getValueDataSet();
                    DoubleDataSet newDS = new DoubleDataSet(ds.getName());
                    newDS.setStyle(ds.getStyle());

                    for (int i = 0; i < ds.getDataCount(); i++) {
                        double x = ds.get(DataSet.DIM_X, i);
                        DateTime ts = new DateTime(new Double(x * 1000d).longValue());

                        double currentY = ds.get(DataSet.DIM_Y, i);
                        List<Double> otherYs = new ArrayList<>();

                        for (XYChartSerie otherSerie : otherSeries) {
                            double otherY = otherSerie.getValueDataSet().get(DataSet.DIM_Y, i);
                            otherYs.add(otherY);
                        }

                        for (Double aDouble : otherYs) {
                            currentY += aDouble;
                        }

                        if (sampleMap.get(ts) == null) {
                            VirtualSample jeVisSample = new VirtualSample(ts, currentY);
                            jeVisSample.setNote("");
                            sampleMap.put(ts, jeVisSample);

                            min = Math.min(min, currentY);
                            max = Math.max(max, currentY);
                            sum += currentY;
                        }

                        newDS.add(x, currentY);
                    }
                    dataSets.add(newDS);

                    if (!otherSeries.isEmpty()) {
                        otherSeries.remove(0);
                    }
                }

                List<JEVisSample> samples = new ArrayList<>();
                sampleMap.forEach((key, value) -> samples.add(value));
                y1SumSerie.getSingleRow().setSamples(samples);
                y1SumSerie.setValueDataSet(dataSets.get(0));

                ValueWithDateTime minVWD = new ValueWithDateTime(min);
                minVWD.setUnit(unit);
                sumEntry.setMin(minVWD);
                y1SumSerie.setMinValue(minVWD);

                ValueWithDateTime maxVWD = new ValueWithDateTime(max);
                maxVWD.setUnit(unit);
                sumEntry.setMax(maxVWD);
                y1SumSerie.setMaxValue(maxVWD);

                avg = sum / sampleMap.size();
                sumEntry.setAvg(nf.format(avg) + " " + unit.getLabel());

                QuantityUnits qu = new QuantityUnits();
                boolean isQuantity = qu.isQuantityUnit(unit);
                if (isQuantity) {
                    sumEntry.setSum(nf.format(sum) + " " + unit.getLabel());
                } else {
                    if (qu.isSumCalculable(unit) && y1SumSerie.getSingleRow().getManipulationMode().equals(ManipulationMode.NONE)) {
                        try {
                            JEVisUnit sumUnit = qu.getSumUnit(unit);
                            ChartUnits cu = new ChartUnits();
                            double newScaleFactor = cu.scaleValue(unit.toString(), sumUnit.toString());
                            JEVisUnit inputUnit = firstY1Row.getAttribute().getInputUnit();
                            JEVisUnit sumUnitOfInputUnit = qu.getSumUnit(inputUnit);

                            if (qu.isDiffPrefix(sumUnitOfInputUnit, sumUnit)) {
                                sum = sum * newScaleFactor / y1SumSerie.getSingleRow().getTimeFactor();
                            } else {
                                sum = sum / y1SumSerie.getSingleRow().getScaleFactor() / y1SumSerie.getSingleRow().getTimeFactor();
                            }

                            Double finalSum1 = sum;
                            sumEntry.setSum(nf.format(finalSum1) + " " + sumUnit.getLabel());
                        } catch (Exception e) {
                            logger.error("Couldn't calculate periods");
                            sumEntry.setSum("- " + unit.getLabel());
                        }
                    } else {
                        sumEntry.setSum("- " + unit.getLabel());
                    }
                }

                for (XYChartSerie xyChartSerie : y1Series) {
                    xyChartSerie.setValueDataSet(dataSets.get(y1Series.indexOf(xyChartSerie)));
                }
            }

            if (hasY2) {
                List<XYChartSerie> y2Series = xyChartSerieList.stream().filter(serie -> serie.getSingleRow().getAxis() == 1 && (serie.getSingleRow().getChartType() == ChartType.DEFAULT ||
                        serie.getSingleRow().getChartType() == ChartType.STACKED_AREA || serie.getSingleRow().getChartType() == ChartType.STACKED_COLUMN)).collect(Collectors.toList());

                String name = "~" + I18n.getInstance().getString("plugin.graph.chart.header.stacked.y2");
                TableEntry sumEntry = new TableEntry(name);
                sumEntry.setColor(Color.BLACK);
                ChartDataRow firstY2Row = y2Series.get(0).getSingleRow();
                sumEntry.setPeriod(y2Series.get(0).getTableEntry().getPeriod());
                y2SumSerie = new XYChartSerie();
                ChartDataRow chartDataRow = new ChartDataRow(ds, new ChartData());
                JEVisUnit unit = firstY2Row.getUnit();
                chartDataRow.setColor(Color.BLACK);
                chartDataRow.setChartType(ChartType.LINE);
                chartDataRow.setUnit(unit);
                chartDataRow.setAxis(1);
                chartDataRow.setScaleFactor(firstY2Row.getScaleFactor());
                chartDataRow.setTimeFactor(firstY2Row.getTimeFactor());
                chartDataRow.setPeriod(firstY2Row.getPeriod());
                chartDataRow.setFormatString(firstY2Row.getFormatString());
                chartDataRow.setSomethingChanged(false);
                y2SumSerie.setSingleRow(chartDataRow);
                DoubleDataSet noteDataSet = new DoubleDataSet(name);
                y2SumSerie.setNoteDataSet(noteDataSet);

                TreeMap<DateTime, JEVisSample> sampleMap = new TreeMap<>();
                double min = Double.MAX_VALUE;
                double max = -Double.MAX_VALUE;
                double avg = 0d;
                double sum = 0d;

                y2SumSerie.setTableEntry(sumEntry);
                y2SumSerie.setSampleMap(sampleMap);

                AlphanumComparator ac = new AlphanumComparator();
                y2Series.sort((o1, o2) -> ac.compare(o2.getTableEntryName(), o1.getTableEntryName()));
                List<XYChartSerie> otherSeries = y2Series.stream().filter(xyChartSerie -> y2Series.indexOf(xyChartSerie) > 0).collect(Collectors.toList());
                List<DoubleDataSet> dataSets = new ArrayList<>();
                for (XYChartSerie serie : y2Series) {
                    DoubleDataSet ds = serie.getValueDataSet();
                    DoubleDataSet newDS = new DoubleDataSet(ds.getName());
                    newDS.setStyle(ds.getStyle());

                    for (int i = 0; i < ds.getDataCount(); i++) {
                        double x = ds.get(DataSet.DIM_X, i);
                        DateTime ts = new DateTime(new Double(x * 1000d).longValue());

                        double currentY = ds.get(DataSet.DIM_Y, i);
                        List<Double> otherYs = new ArrayList<>();

                        for (XYChartSerie otherSerie : otherSeries) {
                            double otherY = otherSerie.getValueDataSet().get(DataSet.DIM_Y, i);
                            otherYs.add(otherY);
                        }

                        for (Double aDouble : otherYs) {
                            currentY += aDouble;
                        }

                        if (sampleMap.get(ts) == null) {
                            VirtualSample jeVisSample = new VirtualSample(ts, currentY);
                            jeVisSample.setNote("");
                            sampleMap.put(ts, jeVisSample);

                            min = Math.min(min, currentY);
                            max = Math.max(max, currentY);
                            sum += currentY;
                        }

                        newDS.add(x, currentY);
                    }
                    dataSets.add(newDS);

                    if (!otherSeries.isEmpty()) {
                        otherSeries.remove(0);
                    }
                }

                List<JEVisSample> samples = new ArrayList<>();
                sampleMap.forEach((key, value) -> samples.add(value));
                y2SumSerie.getSingleRow().setSamples(samples);
                y2SumSerie.setValueDataSet(dataSets.get(0));

                ValueWithDateTime minVWD = new ValueWithDateTime(min);
                minVWD.setUnit(unit);
                sumEntry.setMin(minVWD);
                y2SumSerie.setMinValue(minVWD);

                ValueWithDateTime maxVWD = new ValueWithDateTime(max);
                maxVWD.setUnit(unit);
                sumEntry.setMax(maxVWD);
                y2SumSerie.setMaxValue(maxVWD);

                avg = sum / sampleMap.size();
                sumEntry.setAvg(nf.format(avg) + " " + unit.getLabel());

                QuantityUnits qu = new QuantityUnits();
                boolean isQuantity = qu.isQuantityUnit(unit);
                if (isQuantity) {
                    sumEntry.setSum(nf.format(sum) + " " + unit.getLabel());
                } else {
                    if (qu.isSumCalculable(unit) && y2SumSerie.getSingleRow().getManipulationMode().equals(ManipulationMode.NONE)) {
                        try {
                            JEVisUnit sumUnit = qu.getSumUnit(unit);
                            ChartUnits cu = new ChartUnits();
                            double newScaleFactor = cu.scaleValue(unit.toString(), sumUnit.toString());
                            JEVisUnit inputUnit = firstY2Row.getAttribute().getInputUnit();
                            JEVisUnit sumUnitOfInputUnit = qu.getSumUnit(inputUnit);

                            if (qu.isDiffPrefix(sumUnitOfInputUnit, sumUnit)) {
                                sum = sum * newScaleFactor / y2SumSerie.getSingleRow().getTimeFactor();
                            } else {
                                sum = sum / y2SumSerie.getSingleRow().getScaleFactor() / y2SumSerie.getSingleRow().getTimeFactor();
                            }

                            Double finalSum1 = sum;
                            sumEntry.setSum(nf.format(finalSum1) + " " + sumUnit.getLabel());
                        } catch (Exception e) {
                            logger.error("Couldn't calculate periods");
                            sumEntry.setSum("- " + unit.getLabel());
                        }
                    } else {
                        sumEntry.setSum("- " + unit.getLabel());
                    }
                }


                for (XYChartSerie xyChartSerie : y2Series) {
                    xyChartSerie.setValueDataSet(dataSets.get(y2Series.indexOf(xyChartSerie)));
                }
            }
        }

        if (asDuration) {
            Platform.runLater(() -> this.primaryDateAxis.setTimeAxis(true));

            DefaultTimeFormatter axisLabelFormatter = new DefaultTimeFormatter(this.primaryDateAxis) {

                @Override
                public String toString(Number utcValueSeconds) {
                    return labelCache.computeIfAbsent(utcValueSeconds, this::getStr);
                }

                private String getStr(final Number utcValueSeconds) {
                    long longUTCSeconds = utcValueSeconds.longValue();
                    int nanoSeconds = (int) ((utcValueSeconds.doubleValue() - longUTCSeconds) * 1e9);
                    if (nanoSeconds < 0) { // Correctly Handle dates before EPOCH
                        longUTCSeconds -= 1;
                        nanoSeconds += (int) 1e9;
                    }
                    final LocalDateTime dateTime = LocalDateTime.ofEpochSecond(longUTCSeconds, nanoSeconds,
                            getTimeZoneOffset());

//                    DateTime ts = new DateTime(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(),
//                            dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond());
                    Double utcValueMillis = longUTCSeconds * 1000d;
                    DateTime ts = new DateTime(utcValueMillis.longValue());

                    return (ts.getMillis() - timeStampOfFirstSample.get().getMillis()) / 1000 / 60 / 60 + " h";
                }
            };
            Platform.runLater(() -> this.primaryDateAxis.setAxisLabelFormatter(axisLabelFormatter));

        } else {
            Platform.runLater(() -> this.primaryDateAxis.setTimeAxis(true));

            CustomTimeFormatter axisLabelFormatter = new CustomTimeFormatter(this.primaryDateAxis);

            Platform.runLater(() -> this.primaryDateAxis.setAxisLabelFormatter(axisLabelFormatter));

            Platform.runLater(() -> this.secondaryDateAxis.setTimeAxis(true));

            CustomTimeFormatter secondaryAxisLabelFormatter = new CustomTimeFormatter(this.secondaryDateAxis);

            Platform.runLater(() -> this.secondaryDateAxis.setAxisLabelFormatter(secondaryAxisLabelFormatter));
        }

        List<ChartDataRow> sumModels = new ArrayList<>();
        sumModels.add(sumModelY1);
        sumModels.add(sumModelY2);

        if (showSum && chartDataRows.size() > 1 && (sumModelY1 != null || sumModelY2 != null) && chartType != ChartType.TABLE_V) {
            createSumModels(chartModel, sumModels);
        }

        addSeriesToChart();

        generateXAxis(changedBoth);

        generateYAxis();

        Platform.runLater(() -> {
            getChart().setTitle(getUpdatedChartName());
            updateTable(null, timeStampOfFirstSample.get());
        });

    }

    private void createSumModels(ChartModel chartModel, List<ChartDataRow> sumModels) {
        try {
            long sumId = 9999999999L;
            List<ChartDataRow> oldModels = new ArrayList<>();
            chartModel.getChartData().forEach(chartData -> {
                if (chartData.getId() == sumId) {
                    ChartDataRow chartDataRow = new ChartDataRow(ds, chartData);
                    oldModels.add(chartDataRow);
                }
            });
            chartModel.getChartData().removeAll(oldModels);

            for (ChartDataRow sumModel : sumModels) {
                int index = sumModels.indexOf(sumModel);
                JsonObject json = new JsonObject();
                json.setId(sumId);
                json.setName("~" + I18n.getInstance().getString("plugin.graph.table.sum"));
                if (index == 0) {
                    json.setName(json.getName() + " " + I18n.getInstance().getString("plugin.graph.chartplugin.axisbox.y1"));
                } else {
                    json.setName(json.getName() + " " + I18n.getInstance().getString("plugin.graph.chartplugin.axisbox.y2"));
                }
                JEVisObject test = new JEVisObjectWS((JEVisDataSourceWS) chartDataRows.get(0).getObject().getDataSource(), json);

                sumModel.setId(test.getID());
                sumModel.setName(json.getName());
                sumModel.setAxis(index);
                if (index == 0) {
                    sumModel.setColor(Color.BLACK);
                } else {
                    sumModel.setColor(Color.SADDLEBROWN);
                }
                Map<DateTime, JEVisSample> sumSamples = new HashMap<>();
                boolean hasData = false;
                int moreThanOne = 0;
                for (ChartDataRow model : chartDataRows) {
                    if (model.getAxis() == index) {
                        hasData = true;
                        moreThanOne++;
                        for (JEVisSample jeVisSample : model.getSamples()) {
                            try {
                                DateTime ts = jeVisSample.getTimestamp();
                                Double value = jeVisSample.getValueAsDouble();
                                if (!sumSamples.containsKey(ts)) {
                                    JEVisSample smp = new VirtualSample(ts, value);
                                    smp.setNote("sum");
                                    sumSamples.put(ts, smp);
                                } else {
                                    JEVisSample smp = sumSamples.get(ts);

                                    smp.setValue(smp.getValueAsDouble() + value);
                                }
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                ArrayList<JEVisSample> arrayList = new ArrayList<>(sumSamples.values());
                arrayList.sort((o1, o2) -> {
                    try {
                        if (o1.getTimestamp().isBefore(o2.getTimestamp())) {
                            return -1;
                        } else if (o1.getTimestamp().equals(o2.getTimestamp())) {
                            return 0;
                        } else {
                            return 1;
                        }
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                    return -1;
                });

                sumModel.setSamples(arrayList);
                sumModel.setSomethingChanged(false);

                try {
                    if (hasData && moreThanOne > 1) {
                        chartModel.getChartData().add(sumModel);
                        xyChartSerieList.add(generateSerie(changedBoth, sumModel));
                    }
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }
        } catch (JEVisException e) {
            logger.error("Could not generate sum of data rows: ", e);
        }
    }

    private ChartDataRow getRawDataModel(ChartModel dataModel, ChartDataRow singleRow) {
        ChartDataRow newModel = singleRow.clone();
        newModel.setDataProcessor(null);
        newModel.setAttribute(null);
        newModel.setSamples(null);
        newModel.setUnit("");
        newModel.setColor(newModel.getColor().darker());
        newModel.setName(newModel.getName() + " - " + I18n.getInstance().getString("graph.processing.raw"));

        singleRow.setAxis(0);
        newModel.setAxis(1);

        dataModel.getChartData().add(newModel);
        return newModel;
    }

    public void init() {

        initializeChart();

        getChart().setStyle("-fx-font-size: " + 12 + "px;");

        getChart().setAnimated(false);

        getChart().setLegendVisible(false);
    }

    public void addSeriesToChart() {
        ErrorDataSetRenderer rendererAreaY1 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererAreaY2 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererAreaX2Y1 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererAreaX2Y2 = new ErrorDataSetRenderer();
        rendererAreaY1.setPolyLineStyle(LineStyle.AREA);
        rendererAreaY1.setDrawMarker(false);
        rendererAreaY1.getAxes().add(y1Axis);
        rendererAreaX2Y1.setPolyLineStyle(LineStyle.AREA);
        rendererAreaX2Y1.setDrawMarker(false);
        rendererAreaX2Y1.getAxes().addAll(secondaryDateAxis, y1Axis);
        rendererAreaY2.setPolyLineStyle(LineStyle.AREA);
        rendererAreaY2.setDrawMarker(false);
        rendererAreaY2.getAxes().add(y2Axis);
        rendererAreaX2Y2.setPolyLineStyle(LineStyle.AREA);
        rendererAreaX2Y2.setDrawMarker(false);
        rendererAreaX2Y2.getAxes().addAll(secondaryDateAxis, y2Axis);

        ErrorDataSetRenderer rendererLogicalY1 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererLogicalY2 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererLogicalX2Y1 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererLogicalX2Y2 = new ErrorDataSetRenderer();
        rendererLogicalY1.setPolyLineStyle(LineStyle.HISTOGRAM_FILLED);
        rendererLogicalY1.setDrawMarker(false);
        rendererLogicalY1.getAxes().add(y1Axis);
        rendererLogicalX2Y1.setPolyLineStyle(LineStyle.HISTOGRAM_FILLED);
        rendererLogicalX2Y1.setDrawMarker(false);
        rendererLogicalX2Y1.getAxes().addAll(secondaryDateAxis, y1Axis);
        rendererLogicalY2.setPolyLineStyle(LineStyle.HISTOGRAM_FILLED);
        rendererLogicalY2.setDrawMarker(false);
        rendererLogicalY2.getAxes().add(y2Axis);
        rendererLogicalX2Y2.setPolyLineStyle(LineStyle.HISTOGRAM_FILLED);
        rendererLogicalX2Y2.setDrawMarker(false);
        rendererLogicalX2Y2.getAxes().addAll(secondaryDateAxis, y2Axis);

        ErrorDataSetRenderer rendererBarY1 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererBarY2 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererBarX2Y1 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererBarX2Y2 = new ErrorDataSetRenderer();
        rendererBarY1.setPolyLineStyle(LineStyle.NONE);
        rendererBarY1.setDrawBars(true);
        rendererBarY1.setDrawMarker(false);
        rendererBarY1.getAxes().add(y1Axis);
        rendererBarX2Y1.setPolyLineStyle(LineStyle.NONE);
        rendererBarX2Y1.setDrawBars(true);
        rendererBarX2Y1.setDrawMarker(false);
        rendererBarX2Y1.getAxes().addAll(secondaryDateAxis, y1Axis);
        rendererBarY2.setPolyLineStyle(LineStyle.NONE);
        rendererBarY2.setDrawBars(true);
        rendererBarY2.setDrawMarker(false);
        rendererBarY2.getAxes().add(y2Axis);
        rendererBarX2Y2.setPolyLineStyle(LineStyle.NONE);
        rendererBarX2Y2.setDrawBars(true);
        rendererBarX2Y2.setDrawMarker(false);
        rendererBarX2Y2.getAxes().addAll(secondaryDateAxis, y2Axis);

        ErrorDataSetRenderer rendererColumnY1 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererColumnY2 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererColumnX2Y1 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererColumnX2Y2 = new ErrorDataSetRenderer();
        rendererColumnY1.setPolyLineStyle(LineStyle.NONE);
        rendererColumnY1.setDrawBars(true);
        rendererColumnY1.setDrawMarker(false);
        rendererColumnY1.getAxes().add(y1Axis);
        rendererColumnX2Y1.setPolyLineStyle(LineStyle.NONE);
        rendererColumnX2Y1.setDrawBars(true);
        rendererColumnX2Y1.setDrawMarker(false);
        rendererColumnX2Y1.getAxes().addAll(secondaryDateAxis, y1Axis);
        rendererColumnY2.setPolyLineStyle(LineStyle.NONE);
        rendererColumnY2.setDrawBars(true);
        rendererColumnY2.setDrawMarker(false);
        rendererColumnY2.getAxes().add(y2Axis);
        rendererColumnX2Y2.setPolyLineStyle(LineStyle.NONE);
        rendererColumnX2Y2.setDrawBars(true);
        rendererColumnX2Y2.setDrawMarker(false);
        rendererColumnX2Y2.getAxes().addAll(secondaryDateAxis, y2Axis);
        if (chartModel.getChartType() == ChartType.STACKED_AREA || chartModel.getChartType() == ChartType.STACKED_COLUMN
                || (chartModel.getChartData().stream().anyMatch(chartData -> chartData.getChartType() == ChartType.STACKED_AREA || chartData.getChartType() == ChartType.STACKED_COLUMN))) {
            rendererColumnY1.setShiftBar(false);
            rendererColumnY2.setShiftBar(false);
        }

        ErrorDataSetRenderer rendererScatterY1 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererScatterY2 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererScatterX2Y1 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererScatterX2Y2 = new ErrorDataSetRenderer();
        rendererScatterY1.setPolyLineStyle(LineStyle.NONE);
        rendererScatterY1.getAxes().add(y1Axis);
        rendererScatterX2Y1.setPolyLineStyle(LineStyle.NONE);
        rendererScatterX2Y1.getAxes().addAll(secondaryDateAxis, y1Axis);
        rendererScatterY2.setPolyLineStyle(LineStyle.NONE);
        rendererScatterY2.getAxes().add(y2Axis);
        rendererScatterX2Y2.setPolyLineStyle(LineStyle.NONE);
        rendererScatterX2Y2.getAxes().addAll(secondaryDateAxis, y2Axis);

        ErrorDataSetRenderer rendererLineY1 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererLineX2Y1 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererLineY2 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererLineX2Y2 = new ErrorDataSetRenderer();
        rendererLineY1.setPolyLineStyle(LineStyle.NORMAL);
        rendererLineY1.setDrawMarker(false);
        rendererLineY1.getAxes().add(y1Axis);
        rendererLineX2Y1.setPolyLineStyle(LineStyle.NORMAL);
        rendererLineX2Y1.setDrawMarker(false);
        rendererLineX2Y1.getAxes().addAll(secondaryDateAxis, y1Axis);
        rendererLineY2.setPolyLineStyle(LineStyle.NORMAL);
        rendererLineY2.setDrawMarker(false);
        rendererLineY2.getAxes().add(y2Axis);
        rendererLineX2Y2.setPolyLineStyle(LineStyle.NORMAL);
        rendererLineX2Y2.setDrawMarker(false);
        rendererLineX2Y2.getAxes().addAll(secondaryDateAxis, y2Axis);

        CustomMarkerRenderer labelledMarkerRenderer = new CustomMarkerRenderer(xyChartSerieList);
        labelledMarkerRenderer.getAxes().add(y1Axis);
        ColumnChartLabelRenderer columnChartLabelRenderer = new ColumnChartLabelRenderer(chartModel, xyChartSerieList);

        ErrorDataSetRenderer trendLineRenderer = new ErrorDataSetRenderer();
        trendLineRenderer.setPolyLineStyle(LineStyle.NORMAL);
        trendLineRenderer.setDrawMarker(false);
        trendLineRenderer.setMarkerSize(0);
        trendLineRenderer.setAssumeSortedData(false);

        if (chartModel.getChartType() != ChartType.STACKED_AREA && chartModel.getChartType() != ChartType.STACKED_COLUMN
                && (chartModel.getChartData().stream().noneMatch(chartData -> chartData.getChartType() == ChartType.STACKED_AREA || chartData.getChartType() == ChartType.STACKED_COLUMN))) {
            xyChartSerieList.sort(Comparator.comparingDouble(XYChartSerie::getSortCriteria));
        } else {
            AlphanumComparator ac = new AlphanumComparator();
            xyChartSerieList.sort((o1, o2) -> ac.compare(o1.getTableEntryName(), o2.getTableEntryName()));
        }
        AtomicBoolean hastCustomIntervals = new AtomicBoolean(false);

        for (XYChartSerie xyChartSerie : xyChartSerieList) {
            int index = xyChartSerieList.indexOf(xyChartSerie);

            boolean isCurrentlyCustomInterval = false;
            if (xyChartSerie.getSingleRow().isIntervalEnabled()) {
                isCurrentlyCustomInterval = true;
                hastCustomIntervals.set(true);
                xyChartSerie.setXAxis(secondaryDateAxis);
            } else xyChartSerie.setXAxis(primaryDateAxis);

            ErrorDataSetRenderer rendererY1;
            ErrorDataSetRenderer rendererY2;

            ChartType ct;
            if (xyChartSerie.getSingleRow().getChartType() == null || xyChartSerie.getSingleRow().getChartType() == ChartType.DEFAULT) {
                ct = this.chartType;
            } else {
                ct = xyChartSerie.getSingleRow().getChartType();
            }

            switch (ct) {
                case AREA:
                case STACKED_AREA:
                    if (!isCurrentlyCustomInterval) {
                        rendererY1 = rendererAreaY1;
                        rendererY2 = rendererAreaY2;
                    } else {
                        rendererY1 = rendererAreaX2Y1;
                        rendererY2 = rendererAreaX2Y2;
                    }
                    break;
                case LOGICAL:
                    if (!isCurrentlyCustomInterval) {
                        rendererY1 = rendererLogicalY1;
                        rendererY2 = rendererLogicalY2;
                    } else {
                        rendererY1 = rendererLogicalX2Y1;
                        rendererY2 = rendererLogicalX2Y2;
                    }
                    break;
                case BAR:
                    if (!isCurrentlyCustomInterval) {
                        rendererY1 = rendererBarY1;
                        rendererY2 = rendererBarY2;
                    } else {
                        rendererY1 = rendererBarX2Y1;
                        rendererY2 = rendererBarX2Y2;
                    }
                    break;
                case COLUMN:
                case STACKED_COLUMN:
                    if (!isCurrentlyCustomInterval) {
                        rendererY1 = rendererColumnY1;
                        rendererY2 = rendererColumnY2;
                    } else {
                        rendererY1 = rendererColumnX2Y1;
                        rendererY2 = rendererColumnX2Y2;
                    }
                    break;
                case SCATTER:
                    if (!isCurrentlyCustomInterval) {
                        rendererY1 = rendererScatterY1;
                        rendererY2 = rendererScatterY2;
                    } else {
                        rendererY1 = rendererScatterX2Y1;
                        rendererY2 = rendererScatterX2Y2;
                    }
                    break;
                case LINE:
                default:
                    if (!isCurrentlyCustomInterval) {
                        rendererY1 = rendererLineY1;
                        rendererY2 = rendererLineY2;
                    } else {
                        rendererY1 = rendererLineX2Y1;
                        rendererY2 = rendererLineX2Y2;
                    }
                    break;
            }

            if (showSum && index < xyChartSerieList.size() - 2) {
                if (xyChartSerie.getyAxis() == 0) {
                    xyChartSerie.addValueDataSetRenderer(rendererY1);

                    rendererY1.getDatasets().addAll(drawL1L2(xyChartSerie));
                } else {
                    xyChartSerie.addValueDataSetRenderer(rendererY2);

                    rendererY2.getDatasets().addAll(drawL1L2(xyChartSerie));
                }
                trendLineRenderer.getDatasets().addAll(drawRegression(xyChartSerie));
            } else if (!hasSecondYAxis && xyChartSerie.getyAxis() == 0) {
                xyChartSerie.addValueDataSetRenderer(rendererY1);

                rendererY1.getDatasets().addAll(drawL1L2(xyChartSerie));
                trendLineRenderer.getDatasets().addAll(drawRegression(xyChartSerie));
            } else {
                if (xyChartSerie.getyAxis() == 0) {
                    xyChartSerie.addValueDataSetRenderer(rendererY1);

                    rendererY1.getDatasets().addAll(drawL1L2(xyChartSerie));
                } else {
                    xyChartSerie.addValueDataSetRenderer(rendererY2);

                    rendererY2.getDatasets().addAll(drawL1L2(xyChartSerie));
                }
            }

            if (showIcons && chartType != null && chartType.equals(ChartType.COLUMN)) {
                if (xyChartSerie.getSingleRow().getSamples().size() <= 60) {
                    xyChartSerie.addValueDataSetRenderer(columnChartLabelRenderer);
                }
                xyChartSerie.addNoteDataSetRenderer(labelledMarkerRenderer);
            } else if (showIcons) {
                xyChartSerie.addNoteDataSetRenderer(labelledMarkerRenderer);
            } else if (chartType != null && chartType.equals(ChartType.COLUMN)) {
                if (xyChartSerie.getSingleRow().getSamples().size() <= 60) {
                    xyChartSerie.addValueDataSetRenderer(columnChartLabelRenderer);
                }
            }

            xyChartSerie.setShownInRenderer(true);
            tableData.add(xyChartSerie.getTableEntry());
        }

        AlphanumComparator ac = new AlphanumComparator();

        List<Renderer> allRenderer = new ArrayList<>();

        allRenderer.add(rendererAreaY1);
        allRenderer.add(rendererLogicalY1);
        allRenderer.add(rendererColumnY1);
        allRenderer.add(rendererBarY1);
        allRenderer.add(rendererScatterY1);
        allRenderer.add(rendererLineY1);

        if (hastCustomIntervals.get()) {
            allRenderer.add(rendererAreaX2Y1);
            allRenderer.add(rendererLogicalX2Y1);
            allRenderer.add(rendererColumnX2Y1);
            allRenderer.add(rendererBarX2Y1);
            allRenderer.add(rendererScatterX2Y1);
            allRenderer.add(rendererLineX2Y1);
        }

        if (hasSecondYAxis) {
            allRenderer.add(rendererAreaY2);
            allRenderer.add(rendererLogicalY2);
            allRenderer.add(rendererColumnY2);
            allRenderer.add(rendererBarY2);
            allRenderer.add(rendererScatterY2);
            allRenderer.add(rendererLineY2);

            if (hastCustomIntervals.get()) {
                allRenderer.add(rendererAreaX2Y2);
                allRenderer.add(rendererLogicalX2Y2);
                allRenderer.add(rendererColumnX2Y2);
                allRenderer.add(rendererBarX2Y2);
                allRenderer.add(rendererScatterX2Y2);
                allRenderer.add(rendererLineX2Y2);
            }
        }

        if (calcRegression && showIcons && chartType != null && chartType.equals(ChartType.COLUMN)) {
            allRenderer.add(trendLineRenderer);
            allRenderer.add(labelledMarkerRenderer);
            allRenderer.add(columnChartLabelRenderer);
        } else if (chartType != null && chartType.equals(ChartType.COLUMN) && showIcons) {
            allRenderer.add(labelledMarkerRenderer);
            allRenderer.add(columnChartLabelRenderer);
        } else if (showIcons) {
            allRenderer.add(labelledMarkerRenderer);
        }

        if (y1SumSerie != null) {
            xyChartSerieList.add(y1SumSerie);
            tableData.add(y1SumSerie.getTableEntry());
        }

        if (y2SumSerie != null) {
            xyChartSerieList.add(y2SumSerie);
            tableData.add(y2SumSerie.getTableEntry());
        }

        Platform.runLater(() -> chart.getRenderers().addAll(allRenderer));
        Platform.runLater(() -> tableData.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName())));
    }

    private List<DataSet> drawRegression(XYChartSerie xyChartSerie) {
        return drawRegression(xyChartSerie.getValueDataSet(), xyChartSerie.getSingleRow().getColor(),
                xyChartSerie.getSingleRow().getName());
    }

    List<DataSet> drawRegression(DataSet input, Color color, String title) {
        List<DataSet> list = new ArrayList<>();

        if (calcRegression) {

            TrendLine trendLineObs = null;
            PolynomialCurveFitter fitter = null;

            switch (regressionType) {
                case NONE:
                    break;
                case POLY:
                    fitter = PolynomialCurveFitter.create(polyRegressionDegree);
                    break;
                case EXP:
                    trendLineObs = new ExpTrendLine();
                    break;
                case LOG:
                    trendLineObs = new LogTrendLine();
                    break;
                case POW:
                    trendLineObs = new PowerTrendLine();
                    break;
            }

            if (trendLineObs != null) {
                double[] x = new double[input.getDataCount()];
                double[] y = new double[input.getDataCount()];

                for (int i = 0; i < input.getDataCount(); i++) {
                    if (input instanceof DoubleDataSet) {
                        DoubleDataSet doubleDataSet = (DoubleDataSet) input;
                        x[i] = doubleDataSet.getX(i);
                        y[i] = doubleDataSet.getY(i);
                    } else if (input instanceof DoubleErrorDataSet) {
                        DoubleErrorDataSet doubleErrorDataSet = (DoubleErrorDataSet) input;
                        x[i] = doubleErrorDataSet.getX(i);
                        y[i] = doubleErrorDataSet.getY(i);
                    }
                }

                trendLineObs.setValues(y, x);

                DoubleDataSet set = new DoubleDataSet("regression");
                Color darker = color.darker();
                set.setStyle("strokeColor=" + darker + "; fillColor= " + darker + ";");

                for (int i = 0; i < x.length; i++) {
                    set.add(x[i], trendLineObs.predict(i));
                }

                list.add(set);
            } else if (fitter != null) {
                DoubleDataSet set = new DoubleDataSet("regression");
                final WeightedObservedPoints obs = new WeightedObservedPoints();
                for (int i = 0; i < input.getDataCount(); i++) {

                    if (input instanceof DoubleDataSet) {
                        DoubleDataSet doubleDataSet = (DoubleDataSet) input;
                        obs.add(doubleDataSet.getX(i), doubleDataSet.getY(i));
                    } else if (input instanceof DoubleErrorDataSet) {
                        DoubleErrorDataSet doubleErrorDataSet = (DoubleErrorDataSet) input;
                        obs.add(doubleErrorDataSet.getX(i), doubleErrorDataSet.getY(i));
                    }
                }

                Color darker = color.darker();
                set.setStyle("strokeColor=" + darker + "; fillColor= " + darker + ";");

                final double[] coefficient = fitter.fit(obs.toList());

                for (int i = 0; i < input.getDataCount(); i++) {

                    double result = 0d;
                    for (int power = coefficient.length - 1; power >= 0; power--) {

                        if (input instanceof DoubleDataSet) {
                            DoubleDataSet doubleDataSet = (DoubleDataSet) input;
                            result += coefficient[power] * (Math.pow(doubleDataSet.getX(i), power));
                        } else if (input instanceof DoubleErrorDataSet) {
                            DoubleErrorDataSet doubleErrorDataSet = (DoubleErrorDataSet) input;
                            result += coefficient[power] * (Math.pow(doubleErrorDataSet.getX(i), power));
                        }
                    }

                    if (input instanceof DoubleDataSet) {
                        DoubleDataSet doubleDataSet = (DoubleDataSet) input;
                        set.add(doubleDataSet.getX(i), result);
                    } else if (input instanceof DoubleErrorDataSet) {
                        DoubleErrorDataSet doubleErrorDataSet = (DoubleErrorDataSet) input;
                        set.add(doubleErrorDataSet.getX(i), result);
                    }
                }

                regressionFormula.append(title);
                regressionFormula.append(System.getProperty("line.separator"));
                regressionFormula.append("f(x) = ");
                DecimalFormat formatter = new DecimalFormat();
                formatter.setMaximumSignificantDigits(4);
                formatter.setSignificantDigitsUsed(true);

                for (int p = coefficient.length - 1; p >= 0; p--) {
                    if (p > 0) {
                        regressionFormula.append(" ");
                    }

                    if (p > 0) {
                        regressionFormula.append(formatter.format(coefficient[p]));
                    } else {
                        regressionFormula.append(formatter.format(coefficient[p]));
                    }

                    if (p > 0) {
                        if (p > 1) {
                            regressionFormula.append(" * x^");
                            regressionFormula.append(p);
                        } else {
                            regressionFormula.append(" * x");
                        }
                        regressionFormula.append(" + ");
                    }
                }
                regressionFormula.append(System.getProperty("line.separator"));

                list.add(set);
            }
        }

        return list;
    }

    public List<DoubleDataSet> drawL1L2(XYChartSerie xyChartSerie) {
        List<DoubleDataSet> list = new ArrayList<>();
        try {
            if (showL1L2 && xyChartSerie.getSingleRow().getDataProcessor() != null) {
                CleanDataObject cleanDataObject = new CleanDataObject(xyChartSerie.getSingleRow().getDataProcessor());
                xyChartSerie.getSingleRow().updateScaleFactor();
                Double scaleFactor = xyChartSerie.getSingleRow().getScaleFactor();
                if (cleanDataObject.getLimitsEnabled()) {
                    List<JsonLimitsConfig> limitsConfigs = cleanDataObject.getLimitsConfig();
                    for (int i = 0; i < limitsConfigs.size(); i++) {
                        JsonLimitsConfig limitsConfig = limitsConfigs.get(i);
                        String max = limitsConfig.getMax();
                        if (max != null && !max.equals("")) {
                            double maxValue = Double.parseDouble(max) * scaleFactor;
                            DoubleDataSet doubles = new DoubleDataSet("max");
                            doubles.add(xyChartSerie.getValueDataSet().getX(0), maxValue);
                            doubles.add(xyChartSerie.getValueDataSet().getX(xyChartSerie.getValueDataSet().getDataCount() - 1), maxValue);
                            Color brighter = xyChartSerie.getSingleRow().getColor().brighter();
                            doubles.setStyle("strokeColor=" + brighter + "; fillColor= " + brighter + ";strokeDashPattern=25, 20, 5, 20");
                            list.add(doubles);
                        }

                        String min = limitsConfig.getMin();
                        if (min != null && !min.equals("")) {
                            double minValue = Double.parseDouble(min) * scaleFactor;
                            DoubleDataSet doubles = new DoubleDataSet("min");
                            doubles.add(xyChartSerie.getValueDataSet().getX(0), minValue);
                            doubles.add(xyChartSerie.getValueDataSet().getX(xyChartSerie.getValueDataSet().getDataCount() - 1), minValue);
                            Color brighter = xyChartSerie.getSingleRow().getColor().brighter();
                            doubles.setStyle("strokeColor=" + brighter + "; fillColor= " + brighter + ";strokeDashPattern=25, 20, 5, 20");
                            list.add(doubles);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Could not create data sets for l1/l2.", e);
        }
        return list;
    }

    public void initializeChart() {

        de.gsi.chart.XYChart xyChart = new de.gsi.chart.XYChart(primaryDateAxis, y1Axis);
        xyChart.getRenderers().clear();
        xyChart.setLegend(null);
        xyChart.legendVisibleProperty().set(false);
        xyChart.getToolBar().setVisible(false);

        y1Axis.setForceZeroInRange(true);
        y1Axis.setAutoGrowRanging(true);
        y1Axis.setAutoRanging(true);

        y2Axis.setForceZeroInRange(true);
        y2Axis.setAutoRanging(true);
        y2Axis.setAutoGrowRanging(true);

        y1Axis.setAnimated(false);
        y1Axis.setName("");

        y2Axis.setAnimated(false);
        y2Axis.setSide(Side.RIGHT);
        y2Axis.setName("");

        secondaryDateAxis.setName("");
        secondaryDateAxis.setAnimated(false);
        secondaryDateAxis.setSide(Side.TOP);

        setChart(xyChart);
    }

    public XYChartSerie generateSerie(Boolean[] changedBoth, ChartDataRow singleRow) throws JEVisException {
        XYChartSerie serie = new XYChartSerie(chartModel, singleRow, showIcons, false);

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
    public void setTitle(String chartName) {
        this.chartName = chartName;
        getChart().setTitle(getUpdatedChartName());
    }

    public void generateYAxis() {

        for (XYChartSerie serie : xyChartSerieList) {
            if (serie.getUnit() != null) {
                String currentUnit = UnitManager.getInstance().format(serie.getUnit());
                if (currentUnit != null && !currentUnit.equals("") && serie.getyAxis() == 0) {
                    if (!unitY1.contains(currentUnit)) {
                        unitY1.add(currentUnit);
                    }
                } else if (currentUnit != null && !currentUnit.equals("") && serie.getyAxis() == 1) {
                    if (!unitY2.contains(currentUnit)) {
                        unitY2.add(currentUnit);
                    }
                }
            } else {
                logger.warn("Row has no unit");
            }
        }

        if (chartDataRows != null && chartDataRows.size() > 0) {

            if (unitY1.isEmpty() && unitY2.isEmpty()) {
                unitY1.add(I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit"));
                unitY2.add(I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit"));
            } else if (unitY1.isEmpty()) {
                unitY1.add(I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit"));
            } else if (unitY2.isEmpty()) {
                unitY2.add(I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit"));
            }
        }

        StringBuilder allUnitsY1 = new StringBuilder();
        for (String s : unitY1) {
            if (unitY1.indexOf(s) == 0) allUnitsY1.append(s);
            else allUnitsY1.append(", ").append(s);
        }

        StringBuilder allUnitsY2 = new StringBuilder();
        for (String s : unitY2) {
            if (unitY2.indexOf(s) == 0) allUnitsY2.append(s);
            else allUnitsY2.append(", ").append(s);
        }

        if (!unitY1.isEmpty()) Platform.runLater(() -> y1Axis.setUnit(allUnitsY1.toString()));
        if (!unitY2.isEmpty()) Platform.runLater(() -> y2Axis.setUnit(allUnitsY2.toString()));

    }

    public void generateXAxis(Boolean[] changedBoth) {
//        dateAxis.setAutoRanging(false);
//        dateAxis.setUpperBound((double) chartDataModels.get(0).getSelectedEnd().getMillis());
//        dateAxis.setLowerBound((double) chartDataModels.get(0).getSelectedStart().getMillis());

//        setTickUnitFromPeriod(dateAxis);

//        if (!asDuration) dateAxis.setAsDuration(false);
//        else {
//            dateAxis.setAsDuration(true);
//            dateAxis.setFirstTS(timeStampOfFirstSample.get());
//        }
        Platform.runLater(() -> {
            primaryDateAxis.setName("");
            secondaryDateAxis.setName("");
        });


        updateXAxisLabel(timeStampOfFirstSample.get(), timeStampOfLastSample.get());
    }

    public void updateXAxisLabel(DateTime firstTS, DateTime lastTS) {
        DateTime start = firstTS;
        DateTime end = lastTS;
        try {
            AggregationPeriod aggregationPeriod = chartDataRows.stream().findFirst().map(ChartDataRow::getAggregationPeriod).orElse(AggregationPeriod.NONE);
            if (workDays != null && workDays.isCustomWorkDay() && workDays.getWorkdayEnd().isBefore(workDays.getWorkdayStart()) && new Interval(start, end).toDuration().getStandardDays() > 5) {
                switch (aggregationPeriod) {
                    case NONE:
                    case MINUTELY:
                    case QUARTER_HOURLY:
                        dtfOutLegend = DateTimeFormat.forPattern("EE. dd.MM.yyyy HH:mm");
                        break;
                    case HOURLY:
                    case DAILY:
                    case WEEKLY:
                    case MONTHLY:
                    case QUARTERLY:
                    case YEARLY:
                    case THREEYEARS:
                    case FIVEYEARS:
                    case TENYEARS:
                        start = start.plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
                        end = end.plusDays(1).withHourOfDay(23).withMinuteOfHour(23).withSecondOfMinute(59);
                        dtfOutLegend = DateTimeFormat.forPattern("EE. dd.MM.yyyy");
                        break;
                }
            }
        } catch (Exception e) {
            logger.error("Could not determine period", e);
        }

        String overall = String.format("%s %s %s",
                dtfOutLegend.print(start),
                I18n.getInstance().getString("plugin.graph.chart.valueaxis.until"),
                dtfOutLegend.print(end));

        Platform.runLater(() -> primaryDateAxis.setUnit(I18n.getInstance().getString("plugin.graph.chart.dateaxis.title") + " " + overall));
    }

    @Override
    public ObservableList<TableEntry> getTableData() {
        return tableData;
    }

    private void generatePeriod() {
        if (period == null) {
            if (chartDataRows != null && chartDataRows.size() > 0) {
                ChartDataRow chartDataRow = chartDataRows.get(0);
                if (chartDataRow.getSamples().size() > 1) {
                    try {
                        List<JEVisSample> samples = chartDataRow.getSamples();

                        switch (chartDataRow.getAggregationPeriod()) {
                            case QUARTER_HOURLY:
                                period = Period.minutes(15);
                                break;
                            case HOURLY:
                                period = Period.hours(1);
                                break;
                            case DAILY:
                                period = Period.days(1);
                                break;
                            case WEEKLY:
                                period = Period.weeks(1);
                                break;
                            case MONTHLY:
                                period = Period.months(1);
                                break;
                            case QUARTERLY:
                                period = Period.hours(3);
                                break;
                            case YEARLY:
                                period = Period.years(1);
                                break;
                            case THREEYEARS:
                                period = Period.years(3);
                                break;
                            case FIVEYEARS:
                                period = Period.years(5);
                                break;
                            case TENYEARS:
                                period = Period.years(10);
                                break;
                            case NONE:
                            default:
                                period = new Period(samples.get(0).getTimestamp(),
                                        samples.get(1).getTimestamp());
                                break;
                        }

                        timeStampOfFirstSample.set(samples.get(0).getTimestamp());
                        timeStampOfLastSample.set(samples.get(samples.size() - 1).getTimestamp());

                        changedBoth[0] = true;
                        changedBoth[1] = true;
                    } catch (Exception e) {
                        logger.error("Could not get period from samples", e);
                    }
                }
            }
        }
    }

    @Override
    public Period getPeriod() {
        return period;
    }

    @Override
    public void setPeriod(Period period) {
        this.period = period;
    }

    String getUpdatedChartName() {
        String newName = chartName;
        switch (manipulationMode) {
            case CENTRIC_RUNNING_MEAN:
                String centricrunningmean = I18n.getInstance().getString("plugin.graph.manipulation.centricrunningmean");
                if (!newName.contains(centricrunningmean))
                    newName += " [" + centricrunningmean + "]";
                break;
            case RUNNING_MEAN:
                String runningmean = I18n.getInstance().getString("plugin.graph.manipulation.runningmean");
                newName += " [" + runningmean + "]";
                break;
            case MAX:
                break;
            case MIN:
                break;
            case AVERAGE:
                break;
            case NONE:
                break;
            case SORTED_MAX:
                String sortedmax = I18n.getInstance().getString("plugin.graph.manipulation.sortedmax");
                if (!newName.contains(sortedmax))
                    newName += " [" + sortedmax + "]";
                break;
            case SORTED_MIN:
                String sortedmin = I18n.getInstance().getString("plugin.graph.manipulation.sortedmin");
                if (!newName.contains(sortedmin))
                    newName += " [" + sortedmin + "]";
                break;
            case MEDIAN:
                break;
        }
        return newName;
    }

    @Override
    public String getChartName() {
        return chartName;
    }

    @Override
    public Integer getChartId() {
        return chartId;
    }

    @Override
    public void updateTable(MouseEvent mouseEvent, DateTime valueForDisplay) {
        Point2D mouseCoordinates = null;
        if (mouseEvent != null) mouseCoordinates = new Point2D(mouseEvent.getSceneX(), mouseEvent.getSceneY());
        Double x = null;
        if (valueForDisplay == null) {

//            x = ((MultiAxisChart) getChart()).getXAxis().sceneToLocal(Objects.requireNonNull(mouseCoordinates)).getX();
//
//            valueForDisplay = ((DateAxis) ((MultiAxisChart) getChart()).getXAxis()).getDateTimeForDisplay(x);

        }
        if (valueForDisplay != null) {
            DateTime finalValueForDisplay = valueForDisplay;

            xyChartSerieList.forEach(serie -> {
                try {

                    TableEntry tableEntry = serie.getTableEntry();
                    TreeMap<DateTime, JEVisSample> sampleTreeMap = serie.getSampleMap();

                    DateTime nearest = null;
                    if (sampleTreeMap.get(finalValueForDisplay) != null) {
                        nearest = finalValueForDisplay;
                    } else {
                        nearest = sampleTreeMap.lowerKey(finalValueForDisplay);
                    }

                    JEVisSample sample = sampleTreeMap.get(nearest);

                    Note formattedNote = new Note(sample, serie.getSingleRow().getNoteSamples().get(sample.getTimestamp()), serie.getSingleRow().getAlarms(false).get(sample.getTimestamp()));

                    if (workDays != null && period != null && workDays.getWorkdayEnd().isBefore(workDays.getWorkdayStart())
                            && (period.getDays() > 0
                            || period.getWeeks() > 0
                            || period.getMonths() > 0
                            || period.getYears() > 0)) {
                        nearest = nearest.plusDays(1);
                    }

                    DateTime finalNearest = nearest;
                    if (!asDuration) {
                        Platform.runLater(() -> {
                            if (finalNearest != null) {
                                tableEntry.setDate(finalNearest
                                        .toString(serie.getSingleRow().getFormatString()));
                            } else tableEntry.setValue("-");
                        });
                    } else {
                        Platform.runLater(() -> tableEntry.setDate((finalNearest.getMillis() -
                                timeStampOfFirstSample.get().getMillis()) / 1000 / 60 / 60 + " h"));
                    }
                    Platform.runLater(() -> tableEntry.setNote(formattedNote.getNoteAsString()));
                    String unit = serie.getUnit();

                    if (!sample.getNote().contains("Zeros")) {
                        Double valueAsDouble = null;
                        String formattedDouble = null;
                        if (!serie.getSingleRow().isStringData()) {
                            valueAsDouble = sample.getValueAsDouble();
                            formattedDouble = nf.format(valueAsDouble);
                            String finalFormattedDouble = formattedDouble;
                            Platform.runLater(() -> tableEntry.setValue(finalFormattedDouble + " " + unit));
                        } else {
                            Platform.runLater(() -> {
                                try {
                                    tableEntry.setValue(sample.getValueAsString() + " " + unit);
                                } catch (JEVisException e) {
                                    e.printStackTrace();
                                }
                            });
                        }

                    } else Platform.runLater(() -> tableEntry.setValue("- " + unit));

//                    tableEntry.setPeriod(getPeriod().toString(PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale())));

                } catch (Exception ex) {
//                    ex.printStackTrace();
                }

            });
        }
    }

    @Override
    public void updateTableZoom(double lowerBound, double upperBound) {
        Double lb = lowerBound * 1000;
        Double ub = upperBound * 1000;
        DateTime lower = new DateTime(lb.longValue());
        DateTime upper = new DateTime(ub.longValue());

        xyChartSerieList.forEach(serie -> {
            try {

                ValueWithDateTime min = new ValueWithDateTime(Double.MAX_VALUE);
                ValueWithDateTime max = new ValueWithDateTime(-Double.MAX_VALUE);
                double avg = 0.0;
                Double sum = 0.0;
                long zeroCount = 0;

                List<JEVisSample> samples = serie.getSingleRow().getSamples();
                List<JEVisSample> newList = new ArrayList<>();
                JEVisUnit unit = serie.getSingleRow().getUnit();
                min.setUnit(unit);
                max.setUnit(unit);

                for (JEVisSample smp : samples) {
                    if ((smp.getTimestamp().equals(lower) || smp.getTimestamp().isAfter(lower)) && (smp.getTimestamp().isBefore(upper) || smp.getTimestamp().equals(upper))) {

                        newList.add(smp);
                        DateTime ts = smp.getTimestamp();
                        Double currentValue = smp.getValueAsDouble();

                        if (!smp.getNote().contains("Zeros")) {
                            min.minCheck(ts, currentValue);
                            max.maxCheck(ts, currentValue);
                            sum += currentValue;
                        } else {
                            zeroCount++;
                        }
                    }
                }

                if (manipulationMode == ManipulationMode.CUMULATE) {
                    avg = max.getValue() / samples.size();
                    sum = max.getValue();
                }

                Double finalSum = sum;
                long finalZeroCount = zeroCount;
                double finalAvg = avg;
                try {
                    serie.updateTableEntry(newList, unit, min, max, finalAvg, finalSum, finalZeroCount, true);
                } catch (JEVisException e) {
                    logger.error("Could not update Table Entry for {}", serie.getSingleRow().getObject().getName(), e);
                }


            } catch (Exception ex) {
            }

        });

        Platform.runLater(() -> updateXAxisLabel(lower, upper));
    }

    @Override
    public void applyColors() {
//        ObservableList<Color> colors = FXCollections.observableArrayList(hexColors);
//        DefaultRenderColorScheme.strokeColorProperty().setValue(colors);

    }

    @Override
    public de.gsi.chart.Chart getChart() {
        if (chart == null) {
            initializeChart();
        }

        return chart;
    }

    @Override
    public void setChart(de.gsi.chart.Chart chart) {
        this.chart = chart;
    }

    @Override
    public ChartType getChartType() {
        return chartType;
    }

    @Override
    public Region getRegion() {
        return areaChartRegion;
    }

    @Override
    public void setRegion(Region region) {
        areaChartRegion = region;
    }

    @Override
    public List<ChartDataRow> getChartDataRows() {
        return chartDataRows;
    }

    @Override
    public ChartModel getChartModel() {
        return chartModel;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public List<Color> getHexColors() {
        return hexColors;
    }

    public List<String> getUnitY1() {
        return unitY1;
    }

    public AtomicReference<DateTime> getTimeStampOfFirstSample() {
        return timeStampOfFirstSample;
    }

    public boolean isAsDuration() {
        return asDuration;
    }

    public List<XYChartSerie> getXyChartSerieList() {
        return xyChartSerieList;
    }

    public CustomNumericAxis getY1Axis() {
        return y1Axis;
    }

    public CustomNumericAxis getY2Axis() {
        return y2Axis;
    }

    public DefaultDateAxis getPrimaryDateAxis() {
        return primaryDateAxis;
    }

    public Boolean getShowIcons() {
        return showIcons;
    }

    public StringBuilder getRegressionFormula() {
        return regressionFormula;
    }
}