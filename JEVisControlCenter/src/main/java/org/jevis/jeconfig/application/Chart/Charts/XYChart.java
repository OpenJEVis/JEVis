
package org.jevis.jeconfig.application.Chart.Charts;

import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.NumberFormat;
import de.gsi.chart.axes.spi.format.DefaultTimeFormatter;
import de.gsi.chart.renderer.LineStyle;
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
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.jeapi.ws.JEVisDataSourceWS;
import org.jevis.jeapi.ws.JEVisObjectWS;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartElements.*;
import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.Charts.regression.*;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.jevis.commons.dataprocessing.ManipulationMode.RUNNING_MEAN;

public class XYChart implements Chart {
    private static final Logger logger = LogManager.getLogger(XYChart.class);
    int polyRegressionDegree;
    RegressionType regressionType;
    Boolean calcRegression = false;
    Boolean showIcons = true;
    DefaultDateAxis dateAxis = new DefaultDateAxis();
    List<ChartDataRow> chartDataRows;
    Boolean showRawData = false;
    Boolean showSum = false;
    CustomNumericAxis y1Axis = new CustomNumericAxis();
    private final List<Color> hexColors = new ArrayList<>();
    ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    DateTime now = DateTime.now();
    AtomicReference<DateTime> timeStampOfFirstSample = new AtomicReference<>(now);
    AtomicReference<DateTime> timeStampOfLastSample = new AtomicReference<>(new DateTime(1990, 1, 1, 0, 0, 0));
    CustomNumericAxis y2Axis = new CustomNumericAxis();
    Boolean showL1L2 = false;
    de.gsi.chart.Chart chart;
    AnalysisDataModel analysisDataModel;
    Integer chartId;
    ChartType chartType = ChartType.LINE;
    Double minValue = Double.MAX_VALUE;
    Double maxValue = -Double.MAX_VALUE;
    boolean asDuration = false;
    String chartName;
    private final List<String> unitY1 = new ArrayList<>();
    private final List<String> unitY2 = new ArrayList<>();
    List<XYChartSerie> xyChartSerieList = new ArrayList<>();
    private Region areaChartRegion;
    private Period period;
    ManipulationMode addSeriesOfType;
    AtomicBoolean addManipulationToTitle;
    AtomicReference<ManipulationMode> manipulationMode;
    Boolean[] changedBoth;
    private DateTimeFormatter dtfOutLegend = DateTimeFormat.forPattern("EE. dd.MM.yyyy HH:mm");
    WorkDays workDays = new WorkDays(null);
    boolean hasSecondAxis = false;
    private final StringBuilder regressionFormula = new StringBuilder();
    public static final Image taskImage = JEConfig.getImage("Analysis.png");
    public static String JOB_NAME = "Create series";
    ChartSetting chartSetting;
    NumberFormat nf = NumberFormat.getInstance(I18n.getInstance().getLocale());

    public XYChart() {
        init();
    }

    public void createChart(AnalysisDataModel dataModel, List<ChartDataRow> dataRows, ChartSetting chartSetting) {
        this.createChart(dataModel, dataRows, chartSetting, false);
    }

    public void createChart(AnalysisDataModel dataModel, List<ChartDataRow> dataRows, ChartSetting chartSetting, boolean instant) {
        if (!instant) {

            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    try {
                        buildChart(dataModel, dataRows, chartSetting);
                    } catch (Exception e) {
                        this.failed();
                        logger.error("Could not build chart {}", chartSetting.getName(), e);
                    } finally {
                        succeeded();
                    }
                    return null;
                }
            };

            JEConfig.getStatusBar().addTask(XYChart.class.getName(), task, taskImage, true);
        } else {
            buildChart(dataModel, dataRows, chartSetting);
        }
    }

    public void buildChart(AnalysisDataModel dataModel, List<ChartDataRow> dataRows, ChartSetting chartSetting) {
        this.analysisDataModel = dataModel;
        this.chartDataRows = dataRows;
        this.chartSetting = chartSetting;

        CustomStringConverter tickLabelFormatter1 = new CustomStringConverter(chartSetting.getMinFractionDigits(), chartSetting.getMaxFractionDigits());
        CustomStringConverter tickLabelFormatter2 = new CustomStringConverter(chartSetting.getMinFractionDigits(), chartSetting.getMaxFractionDigits());
        y1Axis.setTickLabelFormatter(tickLabelFormatter1);
        y2Axis.setTickLabelFormatter(tickLabelFormatter2);

        this.nf.setMinimumFractionDigits(chartSetting.getMinFractionDigits());
        this.nf.setMaximumFractionDigits(chartSetting.getMaxFractionDigits());

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

        generatePeriod();

        addManipulationToTitle = new AtomicBoolean(false);
        manipulationMode = new AtomicReference<>(ManipulationMode.NONE);

        ChartDataRow sumModelY1 = null;
        ChartDataRow sumModelY2 = null;

        if (chartType != ChartType.BUBBLE) {
            for (ChartDataRow singleRow : chartDataRows) {
                if (!singleRow.getSelectedcharts().isEmpty()) {
                    try {
                        if (showRawData && singleRow.getDataProcessor() != null) {
                            xyChartSerieList.add(generateSerie(changedBoth, getRawDataModel(dataModel, singleRow)));
                        }

                        xyChartSerieList.add(generateSerie(changedBoth, singleRow));

                        if (singleRow.hasForecastData()) {
                            try {
                                XYChartSerie forecast = new XYChartSerie(chartSetting, singleRow, showIcons, true);

                                hexColors.add(ColorHelper.toColor(ColorHelper.colorToBrighter(singleRow.getColor())));
                                xyChartSerieList.add(forecast);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if (showSum && sumModelY1 == null) {
                            sumModelY1 = singleRow.clone();
                        }

                        if (showSum && sumModelY2 == null) {
                            sumModelY2 = singleRow.clone();
                        }

                    } catch (Exception e) {
                        logger.error("Error: Cant create series for data rows: ", e);
                    }
                }
            }
        } else {

        }

        if (asDuration) {
            Platform.runLater(() -> this.dateAxis.setTimeAxis(true));

            DefaultTimeFormatter axisLabelFormatter = new DefaultTimeFormatter(this.dateAxis) {

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
            Platform.runLater(() -> this.dateAxis.setAxisLabelFormatter(axisLabelFormatter));

        } else {
            Platform.runLater(() -> this.dateAxis.setTimeAxis(true));

            CustomTimeFormatter axisLabelFormatter = new CustomTimeFormatter(this.dateAxis);
            Instant instant = Instant.now();
            ZoneId systemZone = ZoneId.systemDefault();
            ZoneOffset currentOffsetForMyZone = systemZone.getRules().getOffset(instant);
            axisLabelFormatter.setTimeZoneOffset(currentOffsetForMyZone);

            Platform.runLater(() -> this.dateAxis.setAxisLabelFormatter(axisLabelFormatter));
        }

        List<ChartDataRow> sumModels = new ArrayList<>();
        sumModels.add(sumModelY1);
        sumModels.add(sumModelY2);

        if (showSum && chartDataRows.size() > 1 && sumModelY1 != null && chartType != ChartType.TABLE_V) {
            createSumModels(dataModel, sumModels);
        }

        addSeriesToChart();

        generateXAxis(changedBoth);

        generateYAxis();

        Platform.runLater(() -> {
            getChart().setTitle(getUpdatedChartName());
            updateTable(null, timeStampOfFirstSample.get());
        });

    }

    private void createSumModels(AnalysisDataModel dataModel, List<ChartDataRow> sumModels) {
        try {
            for (ChartDataRow sumModel : sumModels) {
                int index = sumModels.indexOf(sumModel);
                JsonObject json = new JsonObject();
                json.setId(9999999999L);
                json.setName("~" + I18n.getInstance().getString("plugin.graph.table.sum"));
                if (index == 0) {
                    json.setName(json.getName() + " " + I18n.getInstance().getString("plugin.graph.chartplugin.axisbox.y1"));
                } else {
                    json.setName(json.getName() + " " + I18n.getInstance().getString("plugin.graph.chartplugin.axisbox.y2"));
                }
                JEVisObject test = new JEVisObjectWS((JEVisDataSourceWS) chartDataRows.get(0).getObject().getDataSource(), json);

                sumModel.setObject(test);
                sumModel.setTitle(json.getName());
                sumModel.setAxis(index);
                if (index == 0) {
                    sumModel.setColor(ColorHelper.toRGBCode(Color.BLACK));
                } else {
                    sumModel.setColor(ColorHelper.toRGBCode(Color.SADDLEBROWN));
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
                        dataModel.getSelectedData().add(sumModel);
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

    private ChartDataRow getRawDataModel(AnalysisDataModel dataModel, ChartDataRow singleRow) {
        ChartDataRow newModel = singleRow.clone();
        newModel.setDataProcessor(null);
        newModel.setAttribute(null);
        newModel.setSamples(null);
        newModel.setUnit(null);
        newModel.setColor(ColorHelper.toRGBCode(ColorHelper.toColor(newModel.getColor()).darker()));
        newModel.setTitle(newModel.getTitle() + " - " + I18n.getInstance().getString("graph.processing.raw"));

        singleRow.setAxis(0);
        newModel.setAxis(1);

        dataModel.getSelectedData().add(newModel);
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
        rendererAreaY1.setPolyLineStyle(LineStyle.AREA);
        rendererAreaY1.setDrawMarker(false);
        rendererAreaY1.getAxes().add(y1Axis);
        rendererAreaY2.setPolyLineStyle(LineStyle.AREA);
        rendererAreaY2.setDrawMarker(false);
        rendererAreaY2.getAxes().add(y2Axis);

        ErrorDataSetRenderer rendererLogicalY1 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererLogicalY2 = new ErrorDataSetRenderer();
        rendererLogicalY1.setPolyLineStyle(LineStyle.HISTOGRAM_FILLED);
        rendererLogicalY1.setDrawMarker(false);
        rendererLogicalY1.getAxes().add(y1Axis);
        rendererLogicalY2.setPolyLineStyle(LineStyle.HISTOGRAM_FILLED);
        rendererLogicalY2.setDrawMarker(false);
        rendererLogicalY2.getAxes().add(y2Axis);

        ErrorDataSetRenderer rendererBarY1 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererBarY2 = new ErrorDataSetRenderer();
        rendererBarY1.setPolyLineStyle(LineStyle.NONE);
        rendererBarY1.setDrawBars(true);
        rendererBarY1.setDrawMarker(false);
        rendererBarY1.getAxes().add(y1Axis);
        rendererBarY2.setPolyLineStyle(LineStyle.NONE);
        rendererBarY2.setDrawBars(true);
        rendererBarY2.setDrawMarker(false);
        rendererBarY2.getAxes().add(y2Axis);

        ErrorDataSetRenderer rendererColumnY1 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererColumnY2 = new ErrorDataSetRenderer();
        rendererColumnY1.setPolyLineStyle(LineStyle.NONE);
        rendererColumnY1.setDrawBars(true);
        rendererColumnY1.setDrawMarker(false);
        rendererColumnY1.getAxes().add(y1Axis);
        rendererColumnY2.setPolyLineStyle(LineStyle.NONE);
        rendererColumnY2.setDrawBars(true);
        rendererColumnY2.setDrawMarker(false);
        rendererColumnY2.getAxes().add(y2Axis);

        ErrorDataSetRenderer rendererScatterY1 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererScatterY2 = new ErrorDataSetRenderer();
        rendererScatterY1.setPolyLineStyle(LineStyle.NONE);
        rendererScatterY1.getAxes().add(y1Axis);
        rendererScatterY2.setPolyLineStyle(LineStyle.NONE);
        rendererScatterY2.getAxes().add(y2Axis);

        ErrorDataSetRenderer rendererLineY1 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererLineY2 = new ErrorDataSetRenderer();
        rendererLineY1.setPolyLineStyle(LineStyle.NORMAL);
        rendererLineY1.setDrawMarker(false);
        rendererLineY1.getAxes().add(y1Axis);
        rendererLineY2.setPolyLineStyle(LineStyle.NORMAL);
        rendererLineY2.setDrawMarker(false);
        rendererLineY2.getAxes().add(y2Axis);

        CustomMarkerRenderer labelledMarkerRenderer = new CustomMarkerRenderer(xyChartSerieList);
        labelledMarkerRenderer.getAxes().add(y1Axis);
        ColumnChartLabelRenderer columnChartLabelRenderer = new ColumnChartLabelRenderer(chartSetting, xyChartSerieList);

        ErrorDataSetRenderer trendLineRenderer = new ErrorDataSetRenderer();
        trendLineRenderer.setPolyLineStyle(LineStyle.NORMAL);
        trendLineRenderer.setDrawMarker(false);
        trendLineRenderer.setMarkerSize(0);
        trendLineRenderer.setAssumeSortedData(false);

        xyChartSerieList.sort(Comparator.comparingDouble(XYChartSerie::getSortCriteria));

        for (XYChartSerie xyChartSerie : xyChartSerieList) {
            int index = xyChartSerieList.indexOf(xyChartSerie);

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
                    rendererY1 = rendererAreaY1;
                    rendererY2 = rendererAreaY2;
                    break;
                case LOGICAL:
                    rendererY1 = rendererLogicalY1;
                    rendererY2 = rendererLogicalY2;
                    break;
                case BAR:
                    rendererY1 = rendererBarY1;
                    rendererY2 = rendererBarY2;
                    break;
                case COLUMN:
                    rendererY1 = rendererColumnY1;
                    rendererY2 = rendererColumnY2;
                    break;
                case SCATTER:
                    rendererY1 = rendererScatterY1;
                    rendererY2 = rendererScatterY2;
                    break;
                case LINE:
                default:
                    rendererY1 = rendererLineY1;
                    rendererY2 = rendererLineY2;
                    break;
            }

            if (showSum && index < xyChartSerieList.size() - 2) {
                rendererY1.getDatasets().add(xyChartSerie.getValueDataSet());
                rendererY1.getDatasets().addAll(drawL1L2(xyChartSerie));
                trendLineRenderer.getDatasets().addAll(drawRegression(xyChartSerie));
            } else if (!hasSecondAxis || xyChartSerie.getyAxis() == 0) {
                rendererY1.getDatasets().add(xyChartSerie.getValueDataSet());
                rendererY1.getDatasets().addAll(drawL1L2(xyChartSerie));
                trendLineRenderer.getDatasets().addAll(drawRegression(xyChartSerie));
            } else {
                rendererY2.getDatasets().add(xyChartSerie.getValueDataSet());
                rendererY2.getDatasets().addAll(drawL1L2(xyChartSerie));
            }

            if (showIcons && chartType != null && chartType.equals(ChartType.COLUMN)) {
                if (xyChartSerie.getSingleRow().getSamples().size() <= 60) {
                    columnChartLabelRenderer.getDatasets().addAll(xyChartSerie.getValueDataSet());
                }
                labelledMarkerRenderer.getDatasets().addAll(xyChartSerie.getNoteDataSet());
            } else if (showIcons) {
                labelledMarkerRenderer.getDatasets().addAll(xyChartSerie.getNoteDataSet());
            } else if (chartType != null && chartType.equals(ChartType.COLUMN)) {
                if (xyChartSerie.getSingleRow().getSamples().size() <= 60) {
                    columnChartLabelRenderer.getDatasets().addAll(xyChartSerie.getValueDataSet());
                }
            }

            tableData.add(xyChartSerie.getTableEntry());
        }

        AlphanumComparator ac = new AlphanumComparator();
        Platform.runLater(() -> chart.getRenderers().addAll(rendererAreaY1, rendererLogicalY1, rendererColumnY1, rendererBarY1, rendererScatterY1, rendererLineY1));
        if (hasSecondAxis) {
            Platform.runLater(() -> chart.getRenderers().addAll(rendererAreaY2, rendererLogicalY2, rendererColumnY2, rendererBarY2, rendererScatterY2, rendererLineY2));
        }

        if (calcRegression && showIcons && chartType != null && chartType.equals(ChartType.COLUMN)) {
            Platform.runLater(() -> chart.getRenderers().addAll(trendLineRenderer, labelledMarkerRenderer, columnChartLabelRenderer));
        } else if (chartType != null && chartType.equals(ChartType.COLUMN) && showIcons) {
            Platform.runLater(() -> chart.getRenderers().addAll(labelledMarkerRenderer, columnChartLabelRenderer));
        } else if (showIcons) {
            Platform.runLater(() -> chart.getRenderers().addAll(labelledMarkerRenderer));
        }

        Platform.runLater(() -> tableData.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName())));
    }

    private List<DataSet> drawRegression(XYChartSerie xyChartSerie) {
        return drawRegression(xyChartSerie.getValueDataSet(), ColorHelper.toColor(xyChartSerie.getSingleRow().getColor()),
                xyChartSerie.getSingleRow().getTitle());
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
                            Color brighter = ColorHelper.toColor(xyChartSerie.getSingleRow().getColor()).brighter();
                            doubles.setStyle("strokeColor=" + brighter + "; fillColor= " + brighter + ";strokeDashPattern=25, 20, 5, 20");
                            list.add(doubles);
                        }

                        String min = limitsConfig.getMin();
                        if (min != null && !min.equals("")) {
                            double minValue = Double.parseDouble(min) * scaleFactor;
                            DoubleDataSet doubles = new DoubleDataSet("min");
                            doubles.add(xyChartSerie.getValueDataSet().getX(0), minValue);
                            doubles.add(xyChartSerie.getValueDataSet().getX(xyChartSerie.getValueDataSet().getDataCount() - 1), minValue);
                            Color brighter = ColorHelper.toColor(xyChartSerie.getSingleRow().getColor()).brighter();
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

        setChart(new de.gsi.chart.XYChart(dateAxis, y1Axis));

        Platform.runLater(() -> chart.getRenderers().clear());
        chart.setLegend(null);
        chart.legendVisibleProperty().set(false);
        chart.getToolBar().setVisible(false);

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
    }

    public XYChartSerie generateSerie(Boolean[] changedBoth, ChartDataRow singleRow) throws JEVisException {
        XYChartSerie serie = new XYChartSerie(chartSetting, singleRow, showIcons, false);

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

    void checkManipulation(ChartDataRow singleRow) throws JEVisException {
        asDuration = singleRow.getManipulationMode().equals(ManipulationMode.SORTED_MIN)
                || singleRow.getManipulationMode().equals(ManipulationMode.SORTED_MAX);

        addManipulationToTitle.set(singleRow.getManipulationMode().equals(RUNNING_MEAN)
                || singleRow.getManipulationMode().equals(ManipulationMode.CENTRIC_RUNNING_MEAN));

        manipulationMode.set(singleRow.getManipulationMode());

        if (!addSeriesOfType.equals(ManipulationMode.NONE)) {
            ManipulationMode oldMode = singleRow.getManipulationMode();
            singleRow.setManipulationMode(addSeriesOfType);
            XYChartSerie serie2 = new XYChartSerie(chartSetting, singleRow, showIcons, false);

            hexColors.add(ColorHelper.toColor(singleRow.getColor()).darker());

            chart.getDatasets().add(serie2.getValueDataSet());

            tableData.add(serie2.getTableEntry());

            singleRow.setManipulationMode(oldMode);
        }
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
                if (serie.getyAxis() == 0) {
                    if (!unitY1.contains(currentUnit)) {
                        unitY1.add(currentUnit);
                    }
                } else if (serie.getyAxis() == 1) {
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
        Platform.runLater(() -> dateAxis.setName(""));

        updateXAxisLabel(timeStampOfFirstSample.get(), timeStampOfLastSample.get());
    }

    public void updateXAxisLabel(DateTime firstTS, DateTime lastTS) {
        DateTime start = firstTS;
        DateTime end = lastTS;
        try {
            AggregationPeriod aggregationPeriod = chartDataRows.stream().findFirst().map(ChartDataRow::getAggregationPeriod).orElse(AggregationPeriod.NONE);
            if (analysisDataModel.isCustomWorkDay() && workDays != null && workDays.getWorkdayEnd(start).isBefore(workDays.getWorkdayStart(start)) && new Interval(start, end).toDuration().getStandardDays() > 5) {
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

        Platform.runLater(() -> dateAxis.setUnit(I18n.getInstance().getString("plugin.graph.chart.dateaxis.title") + " " + overall));
    }

    @Override
    public void setRegion(Region region) {
        areaChartRegion = region;
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
        switch (manipulationMode.get()) {
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

                    if (workDays != null && period != null && workDays.getWorkdayEnd(nearest).isBefore(workDays.getWorkdayStart(nearest))
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

                double min = Double.MAX_VALUE;
                double max = -Double.MAX_VALUE;
                double avg = 0.0;
                Double sum = 0.0;
                long zeroCount = 0;

                List<JEVisSample> samples = serie.getSingleRow().getSamples();
                List<JEVisSample> newList = new ArrayList<>();
                JEVisUnit unit = serie.getSingleRow().getUnit();

                for (JEVisSample smp : samples) {
                    if ((smp.getTimestamp().equals(lower) || smp.getTimestamp().isAfter(lower)) && (smp.getTimestamp().isBefore(upper) || smp.getTimestamp().equals(upper))) {

                        newList.add(smp);
                        Double currentValue = smp.getValueAsDouble();

                        if (!smp.getNote().contains("Zeros")) {
                            min = Math.min(min, currentValue);
                            max = Math.max(max, currentValue);
                            sum += currentValue;
                        } else {
                            zeroCount++;
                        }
                    }
                }

                if (manipulationMode.get().equals(ManipulationMode.CUMULATE)) {
                    avg = max / samples.size();
                    sum = max;
                }

                double finalMin = min;
                double finalMax = max;
                Double finalSum = sum;
                long finalZeroCount = zeroCount;
                double finalAvg = avg;
                try {
                    serie.updateTableEntry(newList, unit, finalMin, finalMax, finalAvg, finalSum, finalZeroCount);
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
        return chart;
    }

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
    public List<ChartDataRow> getChartDataRows() {
        return chartDataRows;
    }

    @Override
    public ChartSetting getChartSetting() {
        return chartSetting;
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

    public DefaultDateAxis getDateAxis() {
        return dateAxis;
    }

    public Boolean getShowIcons() {
        return showIcons;
    }

    public StringBuilder getRegressionFormula() {
        return regressionFormula;
    }
}