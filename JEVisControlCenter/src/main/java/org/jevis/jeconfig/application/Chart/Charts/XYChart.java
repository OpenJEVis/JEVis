
package org.jevis.jeconfig.application.Chart.Charts;

import com.ibm.icu.text.DecimalFormat;
import de.gsi.chart.renderer.LineStyle;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.chart.ui.geometry.Side;
import de.gsi.dataset.spi.DoubleDataSet;
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
import org.jevis.commons.database.ObjectHandler;
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
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.regression.*;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.NumberFormat;
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
    private Boolean showRawData = false;
    private Boolean showSum = false;
    CustomNumericAxis y1Axis = new CustomNumericAxis();
    private final List<Color> hexColors = new ArrayList<>();
    ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    DateTime now = DateTime.now();
    AtomicReference<DateTime> timeStampOfFirstSample = new AtomicReference<>(now);
    AtomicReference<DateTime> timeStampOfLastSample = new AtomicReference<>(new DateTime(2001, 1, 1, 0, 0, 0));
    CustomNumericAxis y2Axis = new CustomNumericAxis();
    private Boolean showL1L2 = false;
    de.gsi.chart.Chart chart;
    private AnalysisDataModel analysisDataModel;
    private Integer chartId;
    private ChartType chartType = ChartType.LINE;
    Double minValue = Double.MAX_VALUE;
    Double maxValue = -Double.MAX_VALUE;
    boolean asDuration = false;
    private String chartName;
    private final List<String> unitY1 = new ArrayList<>();
    private final List<String> unitY2 = new ArrayList<>();
    List<XYChartSerie> xyChartSerieList = new ArrayList<>();
    private Region areaChartRegion;
    private Period period;
    private ManipulationMode addSeriesOfType;
    private AtomicBoolean addManipulationToTitle;
    private AtomicReference<ManipulationMode> manipulationMode;
    private Boolean[] changedBoth;
    private final DateTimeFormatter dtfOutLegend = DateTimeFormat.forPattern("EE. dd.MM.yyyy HH:mm");
    private WorkDays workDays = new WorkDays(null);
    private boolean hasSecondAxis = false;
    private final StringBuilder regressionFormula = new StringBuilder();
    private final Image taskImage = JEConfig.getImage("Analysis.png");
    public static String JOB_NAME = "Create series";

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

    private void buildChart(AnalysisDataModel dataModel, List<ChartDataRow> dataRows, ChartSetting chartSetting) {
        this.analysisDataModel = dataModel;
        this.chartDataRows = dataRows;

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

        ChartDataRow sumModelY1 = null;
        ChartDataRow sumModelY2 = null;
        for (ChartDataRow singleRow : chartDataRows) {
            if (!singleRow.getSelectedcharts().isEmpty()) {
                try {
                    if (showRawData && singleRow.getDataProcessor() != null) {
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
                        xyChartSerieList.add(generateSerie(changedBoth, newModel));
                    }

                    xyChartSerieList.add(generateSerie(changedBoth, singleRow));

                    if (singleRow.hasForecastData()) {
                        try {
                            XYChartSerie forecast = new XYChartSerie(singleRow, showIcons, true);

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

        if (asDuration) {
            Platform.runLater(() -> this.dateAxis.setTimeAxis(true));

            CustomTimeFormatter axisLabelFormatter = new CustomTimeFormatter(this.dateAxis) {

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

                    DateTime ts = new DateTime(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(),
                            dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond());

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

        if (showSum && chartDataRows.size() > 1 && sumModelY1 != null) {
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

        addSeriesToChart();

        generateXAxis(changedBoth);

        generateYAxis();

        Platform.runLater(() -> {
            getChart().setTitle(getUpdatedChartName());
            updateTable(null, timeStampOfFirstSample.get());
        });

    }

    public void init() {
        initializeChart();

        getChart().setStyle("-fx-font-size: " + 12 + "px;");

        getChart().setAnimated(false);

        getChart().setLegendVisible(false);
    }

    public void addSeriesToChart() {
        ErrorDataSetRenderer rendererY1 = new ErrorDataSetRenderer();

        ErrorDataSetRenderer rendererY2 = new ErrorDataSetRenderer();

        CustomMarkerRenderer labelledMarkerRenderer = new CustomMarkerRenderer(xyChartSerieList);

        ErrorDataSetRenderer trendLineRenderer = new ErrorDataSetRenderer();
        trendLineRenderer.setPolyLineStyle(LineStyle.NORMAL);
        trendLineRenderer.setDrawMarker(false);
        trendLineRenderer.setMarkerSize(0);

        switch (chartType) {
            case AREA:
                rendererY1.setPolyLineStyle(LineStyle.AREA);
                rendererY1.setDrawMarker(false);
                rendererY2.setPolyLineStyle(LineStyle.AREA);
                rendererY2.setDrawMarker(false);
                break;
            case LOGICAL:
                rendererY1.setPolyLineStyle(LineStyle.HISTOGRAM_FILLED);
                rendererY1.setDrawMarker(false);
                rendererY2.setPolyLineStyle(LineStyle.HISTOGRAM_FILLED);
                rendererY2.setDrawMarker(false);
                break;
            case BAR:
                rendererY1.setPolyLineStyle(LineStyle.NONE);
                rendererY1.setDrawBars(true);
                rendererY1.setDrawMarker(false);
                rendererY2.setPolyLineStyle(LineStyle.NONE);
                rendererY2.setDrawBars(true);
                rendererY2.setDrawMarker(false);
                break;
            case COLUMN:
                rendererY1.setPolyLineStyle(LineStyle.NONE);
                rendererY1.setDrawBars(true);
                rendererY1.setDrawMarker(false);
                rendererY2.setPolyLineStyle(LineStyle.NONE);
                rendererY2.setDrawBars(true);
                rendererY2.setDrawMarker(false);
                break;
            case BUBBLE:
                break;
            case SCATTER:
                rendererY1.setPolyLineStyle(LineStyle.NONE);
                rendererY2.setPolyLineStyle(LineStyle.NONE);
                break;
            case PIE:
                break;
            case TABLE:
                break;
            case HEAT_MAP:
                break;
            case LINE:
            default:
                rendererY1.setPolyLineStyle(LineStyle.NORMAL);
                rendererY1.setDrawMarker(false);
                rendererY2.setPolyLineStyle(LineStyle.NORMAL);
                rendererY2.setDrawMarker(false);
                break;
        }

        for (XYChartSerie xyChartSerie : xyChartSerieList) {
            int index = xyChartSerieList.indexOf(xyChartSerie);

            if (showSum && index < xyChartSerieList.size() - 2) {
                rendererY1.getDatasets().add(xyChartSerie.getValueDataSet());
                rendererY1.getDatasets().addAll(drawL1L2(xyChartSerie));
                trendLineRenderer.getDatasets().addAll(drawRegression(xyChartSerie));
            } else if (!hasSecondAxis || xyChartSerie.getyAxis() == 0) {
                rendererY1.getDatasets().add(xyChartSerie.getValueDataSet());
                rendererY1.getDatasets().addAll(drawL1L2(xyChartSerie));
                trendLineRenderer.getDatasets().addAll(drawRegression(xyChartSerie));
            } else {
                rendererY2.getAxes().add(y2Axis);
                rendererY2.getDatasets().add(xyChartSerie.getValueDataSet());
                rendererY2.getDatasets().addAll(drawL1L2(xyChartSerie));
                trendLineRenderer.getDatasets().addAll(drawRegression(xyChartSerie));
            }

            if (showIcons) {
                labelledMarkerRenderer.getDatasets().addAll(xyChartSerie.getNoteDataSet());
            }

            tableData.add(xyChartSerie.getTableEntry());
        }

        AlphanumComparator ac = new AlphanumComparator();
        Platform.runLater(() -> chart.getRenderers().add(rendererY1));
        Platform.runLater(() -> chart.getRenderers().add(rendererY2));

        Platform.runLater(() -> tableData.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName())));

        if (calcRegression) {
            Platform.runLater(() -> chart.getRenderers().add(trendLineRenderer));
        }

        if (showIcons) {
            Platform.runLater(() -> chart.getRenderers().add(labelledMarkerRenderer));
        }

    }

    private List<DoubleDataSet> drawRegression(XYChartSerie xyChartSerie) {
        List<DoubleDataSet> list = new ArrayList<>();

        if (calcRegression) {
            DoubleDataSet input = xyChartSerie.getValueDataSet();

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
                    x[i] = input.getX(i);
                    y[i] = input.getY(i);
                }

                trendLineObs.setValues(y, x);

                DoubleDataSet set = new DoubleDataSet("regression");
                Color darker = ColorHelper.toColor(xyChartSerie.getSingleRow().getColor()).darker();
                set.setStyle("strokeColor=" + darker + "; fillColor= " + darker + ";");

                for (int i = 0; i < x.length; i++) {
                    set.add(x[i], trendLineObs.predict(i));
                }

                list.add(set);
            } else if (fitter != null) {
                DoubleDataSet set = new DoubleDataSet("regression");
                final WeightedObservedPoints obs = new WeightedObservedPoints();
                for (int i = 0; i < input.getDataCount(); i++) {
                    obs.add(input.getX(i), input.getY(i));
                }

                Color darker = ColorHelper.toColor(xyChartSerie.getSingleRow().getColor()).darker();
                set.setStyle("strokeColor=" + darker + "; fillColor= " + darker + ";");

                final double[] coefficient = fitter.fit(obs.toList());

                for (int i = 0; i < input.getDataCount(); i++) {

                    double result = 0d;
                    for (int power = coefficient.length - 1; power >= 0; power--) {
                        result += coefficient[power] * (Math.pow(input.getX(i), power));
                    }

                    set.add(input.getX(i), result);
                }

                regressionFormula.append(xyChartSerie.getSingleRow().getTitle());
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
                CleanDataObject cleanDataObject = new CleanDataObject(xyChartSerie.getSingleRow().getDataProcessor(), new ObjectHandler(xyChartSerie.getSingleRow().getObject().getDataSource()));
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

        chart.getRenderers().clear();
        chart.setLegend(null);
        chart.legendVisibleProperty().set(false);
        chart.getToolBar().setVisible(false);
    }

    public XYChartSerie generateSerie(Boolean[] changedBoth, ChartDataRow singleRow) throws JEVisException {
        XYChartSerie serie = new XYChartSerie(singleRow, showIcons, false);

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
            XYChartSerie serie2 = new XYChartSerie(singleRow, showIcons, false);

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

        CustomStringConverter tickLabelFormatter1 = new CustomStringConverter(2);

        CustomStringConverter tickLabelFormatter2 = new CustomStringConverter(2);

        Platform.runLater(() -> {
            y1Axis.setForceZeroInRange(true);
            y1Axis.setAutoGrowRanging(true);
            y1Axis.setAutoRanging(true);

            y2Axis.setForceZeroInRange(true);
            y2Axis.setAutoRanging(true);
            y2Axis.setAutoGrowRanging(true);

            y1Axis.setTickLabelFormatter(tickLabelFormatter1);
            y1Axis.setAnimated(false);
            y1Axis.setName("");

            y2Axis.setTickLabelFormatter(tickLabelFormatter2);
            y2Axis.setAnimated(false);
            y2Axis.setSide(Side.RIGHT);
            y2Axis.setName("");
        });

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

        Period realPeriod = Period.minutes(15);
        if (chartDataRows != null && chartDataRows.size() > 0) {

            if (chartDataRows.get(0).getSamples().size() > 1) {
                try {
                    List<JEVisSample> samples = chartDataRows.get(0).getSamples();
                    period = new Period(samples.get(0).getTimestamp(),
                            samples.get(1).getTimestamp());
                    timeStampOfFirstSample.set(samples.get(0).getTimestamp());
                    timeStampOfLastSample.set(samples.get(samples.size() - 1).getTimestamp());
                    realPeriod = new Period(samples.get(0).getTimestamp(),
                            samples.get(1).getTimestamp());
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }
        }

        updateXAxisLabel(timeStampOfFirstSample.get(), timeStampOfLastSample.get());
    }

    public void updateXAxisLabel(DateTime firstTS, DateTime lastTS) {

        String overall = String.format("%s %s %s",
                dtfOutLegend.print(firstTS),
                I18n.getInstance().getString("plugin.graph.chart.valueaxis.until"),
                dtfOutLegend.print(lastTS));

        Platform.runLater(() -> dateAxis.setUnit(I18n.getInstance().getString("plugin.graph.chart.dateaxis.title") + " " + overall));
    }

    private Period removeWorkdayInterval(DateTime workStart, DateTime workEnd) {
//        System.out.println("workStart.before÷ " + workStart + "|" + workEnd);
        if (workDays.getWorkdayStart().isAfter(workDays.getWorkdayEnd())) {
            workStart = workStart.plusDays(1);
        }

        workStart = workStart.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        DateTime workEnd2 = workEnd.plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
//        System.out.println("workStart.after÷ " + workStart + "|" + workEnd2);
        return new Period(workStart, workEnd2);
    }

    @Override
    public void setRegion(Region region) {
        areaChartRegion = region;
    }

    @Override
    public ObservableList<TableEntry> getTableData() {
        return tableData;
    }

    @Override
    public Period getPeriod() {
        return period;
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
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);

            xyChartSerieList.parallelStream().forEach(serie -> {
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

                    Note formattedNote = new Note(sample, serie.getSingleRow().getNoteSamples().get(sample.getTimestamp()), serie.getSingleRow().getAlarms().get(sample.getTimestamp()));

                    DateTime finalNearest = nearest;
                    if (!asDuration) {
                        Platform.runLater(() -> {
                            if (finalNearest != null) {
                                tableEntry.setDate(finalNearest
                                        .toString(DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss")));
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

        xyChartSerieList.parallelStream().forEach(serie -> {
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