
package org.jevis.jeconfig.application.Chart.Charts;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.jeapi.ws.JEVisDataSourceWS;
import org.jevis.jeapi.ws.JEVisObjectWS;
import org.jevis.jeconfig.application.Chart.ChartElements.DateValueAxis;
import org.jevis.jeconfig.application.Chart.ChartElements.Note;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.ChartElements.XYChartSerie;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisAreaChart;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisChart;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.regression.RegressionType;
import org.jevis.jeconfig.application.Chart.Zoom.ChartPanManager;
import org.jevis.jeconfig.application.Chart.Zoom.JFXChartUtil;
import org.jevis.jeconfig.application.Chart.data.RowNote;
import org.jevis.jeconfig.dialog.NoteDialog;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.measure.unit.Unit;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.jevis.commons.dataprocessing.ManipulationMode.RUNNING_MEAN;

public class XYChart implements Chart {
    private static final Logger logger = LogManager.getLogger(XYChart.class);
    private final Boolean showRawData;
    private final Boolean showSum;
    final int polyRegressionDegree;
    final RegressionType regressionType;
    final Boolean calcRegression;
    Boolean hideShowIcons;
    //ObservableList<MultiAxisAreaChart.Series<Number, Number>> series = FXCollections.observableArrayList();
    List<Color> hexColors = new ArrayList<>();
    ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    AtomicReference<DateTime> timeStampOfFirstSample = new AtomicReference<>(DateTime.now());
    AtomicReference<DateTime> timeStampOfLastSample = new AtomicReference<>(new DateTime(2001, 1, 1, 0, 0, 0));
    NumberAxis y1Axis = new NumberAxis();
    NumberAxis y2Axis = new NumberAxis();
    Axis dateAxis = new DateValueAxis();
    List<ChartDataModel> chartDataModels;
    MultiAxisChart chart;
    Double minValue = Double.MAX_VALUE;
    Double maxValue = -Double.MAX_VALUE;
    boolean asDuration = false;
    private String chartName;
    private List<String> unitY1 = new ArrayList<>();
    private List<String> unitY2 = new ArrayList<>();
    List<XYChartSerie> xyChartSerieList = new ArrayList<>();
    private DateTime valueForDisplay;
    private Region areaChartRegion;
    private Period period;
    private ManipulationMode addSeriesOfType;
    private AtomicBoolean addManipulationToTitle;
    private AtomicReference<ManipulationMode> manipulationMode;
    private Boolean[] changedBoth;
    private DateTimeFormatter dtfOutLegend = DateTimeFormat.forPattern("EE. dd.MM.yyyy HH:mm");
    private ChartSettingsFunction chartSettingsFunction = new ChartSettingsFunction() {
        @Override
        public void applySetting(javafx.scene.chart.Chart chart) {

        }
    };
    private WorkDays workDays = new WorkDays(null);
    private ChartPanManager panner;
    private JFXChartUtil jfxChartUtil;
    private DateTime nearest;

    public XYChart(List<ChartDataModel> chartDataModels, Boolean showRawData, Boolean showSum, Boolean hideShowIcons, Boolean calcRegression, RegressionType regressionType, int polyRegressionDegree, ManipulationMode addSeriesOfType, Integer chartId, String chartName) {
        this.chartDataModels = chartDataModels;
        this.showRawData = showRawData;
        this.showSum = showSum;
        this.regressionType = regressionType;
        this.hideShowIcons = hideShowIcons;
        this.calcRegression = calcRegression;
        this.polyRegressionDegree = polyRegressionDegree;
        this.chartName = chartName;
        this.addSeriesOfType = addSeriesOfType;
        if (!chartDataModels.isEmpty()) {
            workDays = new WorkDays(chartDataModels.get(0).getObject());
        }

        init();
    }

    public void init() {
        initializeChart();
        if (calcRegression) {
            chart.setRegression(0, regressionType, polyRegressionDegree);
        }

        hexColors.clear();
        chart.getData().clear();
        tableData.clear();

        changedBoth = new Boolean[]{false, false};

        addManipulationToTitle = new AtomicBoolean(false);
        manipulationMode = new AtomicReference<>(ManipulationMode.NONE);

        ChartDataModel sumModel = null;
        for (ChartDataModel singleRow : chartDataModels) {
            if (!singleRow.getSelectedcharts().isEmpty()) {
                try {
                    if (showRawData && singleRow.getDataProcessor() != null) {
                        ChartDataModel newModel = singleRow.clone();
                        newModel.setDataProcessor(null);
                        newModel.setAttribute(null);
                        newModel.setSamples(null);
                        newModel.setUnit(null);
                        newModel.setColor(newModel.getColor().darker());

                        singleRow.setAxis(0);
                        newModel.setAxis(1);
                        xyChartSerieList.add(generateSerie(changedBoth, newModel));
                    }

                    xyChartSerieList.add(generateSerie(changedBoth, singleRow));

                    if (showSum && sumModel == null) {
                        sumModel = singleRow.clone();
                    }

                } catch (JEVisException e) {
                    logger.error("Error: Cant create series for data rows: ", e);
                }
            }
        }

        if (showSum && chartDataModels.size() > 1) {
            try {
                JsonObject json = new JsonObject();
                json.setId(9999999999L);
                json.setName("Summe");
                JEVisObject test = new JEVisObjectWS((JEVisDataSourceWS) sumModel.getObject().getDataSource(), json);
                sumModel.setObject(test);
                sumModel.setAxis(1);
                sumModel.setColor(Color.BLACK);
                Map<DateTime, JEVisSample> sumSamples = new HashMap<>();
                for (ChartDataModel model : chartDataModels) {
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
                ArrayList arrayList = new ArrayList<>(sumSamples.values());
                arrayList.sort(new Comparator<JEVisSample>() {
                    @Override
                    public int compare(JEVisSample o1, JEVisSample o2) {
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
                    }
                });

                sumModel.setSamples(arrayList);
                sumModel.setSomethingChanged(false);
            } catch (JEVisException e) {
                logger.error("Could not generate sum of data rows: ", e);
            }
            try {
                xyChartSerieList.add(generateSerie(changedBoth, sumModel));

                Platform.runLater(() -> {
                    chart.getData().forEach(serie -> ((MultiAxisChart.Series) serie).getData().forEach(numberNumberData -> {
                        MultiAxisChart.Data node = (MultiAxisChart.Data) numberNumberData;
                        node.setExtraValue(0);
                    }));
                });
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        }

        if (asDuration) {
            ((DateValueAxis) dateAxis).setAsDuration(true);
            ((DateValueAxis) dateAxis).setTimeStampFromFirstSample(timeStampOfFirstSample.get());
        }

        generateXAxis(changedBoth);
        dateAxis.setAutoRanging(true);

        generateYAxis();

        getChart().setStyle("-fx-font-size: " + 12 + "px;");

        getChart().setAnimated(false);

        applyColors();

        getChart().setTitle(getUpdatedChartName());

        getChart().setLegendVisible(false);
        //((javafx.scene.chart.XYChart)getChart()).setCreateSymbols(true);

        chartSettingsFunction.applySetting(getChart());

        initializeZoom();

        Platform.runLater(() -> updateTable(null, timeStampOfFirstSample.get()));
    }

    public void initializeChart() {
        setChart(new MultiAxisAreaChart(dateAxis, y1Axis, y2Axis));
    }

    public XYChartSerie generateSerie(Boolean[] changedBoth, ChartDataModel singleRow) throws JEVisException {
        XYChartSerie serie = new XYChartSerie(singleRow, hideShowIcons);

        hexColors.add(singleRow.getColor());
        chart.getData().add(serie.getSerie());
        tableData.add(serie.getTableEntry());

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

    void checkManipulation(ChartDataModel singleRow) throws JEVisException {
        asDuration = singleRow.getManipulationMode().equals(ManipulationMode.SORTED_MIN)
                || singleRow.getManipulationMode().equals(ManipulationMode.SORTED_MAX);

        if (singleRow.getManipulationMode().equals(RUNNING_MEAN)
                || singleRow.getManipulationMode().equals(ManipulationMode.CENTRIC_RUNNING_MEAN)) {
            addManipulationToTitle.set(true);
        } else addManipulationToTitle.set(false);

        manipulationMode.set(singleRow.getManipulationMode());

        if (!addSeriesOfType.equals(ManipulationMode.NONE)) {
            ManipulationMode oldMode = singleRow.getManipulationMode();
            singleRow.setManipulationMode(addSeriesOfType);
            XYChartSerie serie2 = new XYChartSerie(singleRow, hideShowIcons);

            hexColors.add(singleRow.getColor().darker());
            chart.getData().add(serie2.getSerie());
            tableData.add(serie2.getTableEntry());

            singleRow.setManipulationMode(oldMode);
        }
    }

    @Override
    public void setTitle(String chartName) {
        this.chartName = chartName;
        getChart().setTitle(getUpdatedChartName());
    }


    @Override
    public void setChartSettings(ChartSettingsFunction function) {
        chartSettingsFunction = function;
    }

    public void generateYAxis() {
        y1Axis.setAutoRanging(true);
        y2Axis.setAutoRanging(true);

        for (XYChartSerie serie : xyChartSerieList) {
            if (serie.getUnit() != null) {
                String currentUnit = UnitManager.getInstance().format(serie.getUnit());
                if (currentUnit.equals("") || currentUnit.equals(Unit.ONE.toString()))
                    currentUnit = serie.getUnit();
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

        if (chartDataModels != null && chartDataModels.size() > 0) {

            if (unitY1.isEmpty() && unitY2.isEmpty())
                unitY1.add(I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit"));

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

        if (!unitY1.isEmpty()) y1Axis.setLabel(allUnitsY1.toString());
        if (!unitY2.isEmpty()) y2Axis.setLabel(allUnitsY2.toString());

        checkForY2Axis();
    }

    public void generateXAxis(Boolean[] changedBoth) {
        dateAxis.setAutoRanging(true);
        if (!asDuration) ((DateValueAxis) dateAxis).setAsDuration(false);
        else {
            ((DateValueAxis) dateAxis).setAsDuration(true);
            ((DateValueAxis) dateAxis).setTimeStampFromFirstSample(timeStampOfFirstSample.get());
        }

        Period realPeriod = Period.minutes(15);
        if (chartDataModels != null && chartDataModels.size() > 0) {

            if (chartDataModels.get(0).getSamples().size() > 1) {
                try {
                    List<JEVisSample> samples = chartDataModels.get(0).getSamples();
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

        dateAxis.setLabel(I18n.getInstance().getString("plugin.graph.chart.dateaxis.title") + " " + overall);
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
    public void initializeZoom() {
        panner = null;

        getChart().setOnMouseMoved(mouseEvent -> {
            updateTable(mouseEvent, null);
        });

        panner = new ChartPanManager((MultiAxisChart<?, ?>) getChart());

        panner.setMouseFilter(mouseEvent -> {
            if (mouseEvent.getButton() != MouseButton.SECONDARY
                    && (mouseEvent.getButton() != MouseButton.PRIMARY
                    || !mouseEvent.isShortcutDown())) {
                mouseEvent.consume();
            }
        });
        panner.start();

        jfxChartUtil = new JFXChartUtil();

        jfxChartUtil.addDoublePrimaryClickAutoRangeHandler((MultiAxisChart<?, ?>) getChart());

    }

    @Override
    public JFXChartUtil getJfxChartUtil() {
        return jfxChartUtil;
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
        timeStampOfFirstSample.set(DateTime.now());
        timeStampOfLastSample.set(new DateTime(2001, 1, 1, 0, 0, 0));
        changedBoth = new Boolean[]{false, false};
        //xyChartSerieList.clear();
        //series.clear();
        //tableData.clear();
        unitY1.clear();
        unitY2.clear();
        //hexColors.clear();
        int chartDataModelsSize = chartDataModels.size();
        int xyChartSerieListSize = xyChartSerieList.size();

        if (chartDataModelsSize > 0) {
            if (chartDataModelsSize <= xyChartSerieListSize) {
                /**
                 * if the new chart data model contains fewer or equal times the chart series as the old one
                 */

                if (chartDataModelsSize < xyChartSerieListSize && !showRawData) {
                    /**
                     * remove the series which are in excess
                     */

                    xyChartSerieList.subList(chartDataModelsSize, xyChartSerieListSize).clear();
                    chart.getData().subList(chartDataModelsSize, xyChartSerieListSize).clear();
                    hexColors.subList(chartDataModelsSize, xyChartSerieListSize).clear();
                    tableData.subList(chartDataModelsSize, xyChartSerieListSize).clear();

                }

                timeStampOfFirstSample = new AtomicReference<>(DateTime.now());
                timeStampOfLastSample = new AtomicReference<>(new DateTime(2001, 1, 1, 0, 0, 0));
                updateXYChartSeries();

            } else {
                /**
                 * if there are more new data rows then old ones
                 */
                timeStampOfFirstSample = new AtomicReference<>(DateTime.now());
                timeStampOfLastSample = new AtomicReference<>(new DateTime(2001, 1, 1, 0, 0, 0));

                updateXYChartSeries();

                for (int i = xyChartSerieListSize; i < chartDataModelsSize; i++) {
                    try {
                        xyChartSerieList.add(generateSerie(changedBoth, chartDataModels.get(i)));
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                }
            }

            Platform.runLater(() -> {
                chart.getData().forEach(serie -> ((MultiAxisChart.Series) serie).getData().forEach(numberNumberData -> {
                    if (((MultiAxisChart.Data) numberNumberData).getNode() != null)
                        if (((MultiAxisChart.Data) numberNumberData).getNode().getClass().equals(HBox.class)) {
                            ((MultiAxisChart.Data) numberNumberData).getNode().setVisible(hideShowIcons);
                        }
                }));
            });

            applyColors();

            generateXAxis(changedBoth);
            generateYAxis();

            getChart().setTitle(getUpdatedChartName());
            getChart().layout();
        }

    }

    private void updateXYChartSeries() {
        for (int i = 0; i < xyChartSerieList.size(); i++) {
            XYChartSerie xyChartSerie = xyChartSerieList.get(i);

            ChartDataModel model = null;
            if (!showRawData) {
                model = chartDataModels.get(i);
            } else {
                for (ChartDataModel mdl : chartDataModels) {
                    if (mdl.getObject().equals(xyChartSerie.getSingleRow().getObject())) {
                        model = mdl;
                        break;
                    }
                }
            }

            if (model != null) {
                hexColors.set(i, model.getColor());
                xyChartSerie.setSingleRow(model);
                try {
                    xyChartSerie.generateSeriesFromSamples();
                } catch (JEVisException e) {
                    e.printStackTrace();
                }

                tableData.set(i, xyChartSerie.getTableEntry());

                if (xyChartSerie.getTimeStampFromFirstSample().isBefore(timeStampOfFirstSample.get())) {
                    timeStampOfFirstSample.set(xyChartSerie.getTimeStampFromFirstSample());
                    changedBoth[0] = true;
                }

                if (xyChartSerie.getTimeStampFromLastSample().isAfter(timeStampOfLastSample.get())) {
                    timeStampOfLastSample.set(xyChartSerie.getTimeStampFromLastSample());
                    changedBoth[1] = true;
                }

                try {
                    checkManipulation(model);
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }
        }
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
    public Integer getChartId() {
        return null;
    }

    @Override
    public void updateTable(MouseEvent mouseEvent, DateTime valueForDisplay) {
        Point2D mouseCoordinates = null;
        if (mouseEvent != null) mouseCoordinates = new Point2D(mouseEvent.getSceneX(), mouseEvent.getSceneY());
        Double x = null;
        if (valueForDisplay == null) {

            x = ((MultiAxisChart) getChart()).getXAxis().sceneToLocal(Objects.requireNonNull(mouseCoordinates)).getX();

            valueForDisplay = ((DateValueAxis) ((MultiAxisChart) getChart()).getXAxis()).getDateTimeForDisplay(x);

        }
        if (valueForDisplay != null) {
            setValueForDisplay(valueForDisplay);
            DateTime finalValueForDisplay = valueForDisplay;
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);

            xyChartSerieList.parallelStream().forEach(serie -> {
                try {

                    TableEntry tableEntry = serie.getTableEntry();
                    TreeMap<DateTime, JEVisSample> sampleTreeMap = serie.getSampleMap();

                    nearest = null;
                    if (sampleTreeMap.get(finalValueForDisplay) != null) {
                        nearest = finalValueForDisplay;
                    } else {
                        nearest = sampleTreeMap.lowerKey(finalValueForDisplay);
                    }

                    JEVisSample sample = sampleTreeMap.get(nearest);


                    Note formattedNote = new Note(sample);

                    if (!asDuration) {
                        tableEntry.setDate(nearest
                                .toString(DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss")));
                    } else {
                        tableEntry.setDate((nearest.getMillis() -
                                timeStampOfFirstSample.get().getMillis()) / 1000 / 60 / 60 + " h");
                    }
                    tableEntry.setNote(formattedNote.getNoteAsString());
                    String unit = serie.getUnit();

                    if (!sample.getNote().contains("Empty")) {
                        Double valueAsDouble = null;
                        String formattedDouble = null;
                        if (!serie.getSingleRow().isStringData()) {
                            valueAsDouble = sample.getValueAsDouble();
                            formattedDouble = nf.format(valueAsDouble);
                            tableEntry.setValue(formattedDouble + " " + unit);
                        } else {
                            tableEntry.setValue(sample.getValueAsString() + " " + unit);
                        }

                    } else tableEntry.setValue("- " + unit);

//                    tableEntry.setPeriod(getPeriod().toString(PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale())));

                } catch (Exception ex) {
                }

            });
        }
    }

    @Override
    public void updateTableZoom(Long lowerBound, Long upperBound) {
        DateTime lower = new DateTime(lowerBound);
        DateTime upper = new DateTime(upperBound);

        xyChartSerieList.parallelStream().forEach(serie -> {
            try {

                TableEntry tableEntry = serie.getTableEntry();
                TreeMap<DateTime, JEVisSample> sampleTreeMap = serie.getSampleMap();

                double min = Double.MAX_VALUE;
                double max = -Double.MAX_VALUE;
                double avg = 0.0;
                Double sum = 0.0;

                List<JEVisSample> samples = serie.getSingleRow().getSamples();
                List<JEVisSample> newList = new ArrayList<>();
                JEVisUnit unit = serie.getSingleRow().getUnit();

                for (JEVisSample smp : samples) {
                    if ((smp.getTimestamp().equals(lower) || smp.getTimestamp().isAfter(lower)) && smp.getTimestamp().isBefore(upper)) {

                        newList.add(smp);
                        Double currentValue = smp.getValueAsDouble();

                        min = Math.min(min, currentValue);
                        max = Math.max(max, currentValue);
                        sum += currentValue;
                    }
                }

                double finalMin = min;
                double finalMax = max;
                Double finalSum = sum;
                Platform.runLater(() -> {
                    try {
                        serie.updateTableEntry(newList, unit, finalMin, finalMax, avg, finalSum);
                    } catch (JEVisException e) {
                        logger.error("Could not update Table Entry for {}", serie.getSingleRow().getObject().getName(), e);
                    }
                });


            } catch (Exception ex) {
            }

        });

        Platform.runLater(() -> updateXAxisLabel(lower, upper));
    }

    @Override
    public void showNote(MouseEvent mouseEvent) {
        if (manipulationMode.get().equals(ManipulationMode.NONE)) {

            Point2D mouseCoordinates = new Point2D(mouseEvent.getSceneX(), mouseEvent.getSceneY());
            double x = ((MultiAxisChart) getChart()).getXAxis().sceneToLocal(mouseCoordinates).getX();

            Map<String, RowNote> map = new HashMap<>();
            DateTime valueForDisplay = null;
            valueForDisplay = ((DateValueAxis) ((MultiAxisChart) getChart()).getXAxis()).getDateTimeForDisplay(x);

            for (XYChartSerie serie : xyChartSerieList) {
                try {
                    DateTime nearest = serie.getSampleMap().lowerKey(valueForDisplay);

                    if (nearest != null) {

                        JEVisSample nearestSample = serie.getSampleMap().get(nearest);

                        String title = "";
                        title += serie.getSingleRow().getObject().getName();

                        JEVisObject dataObject;
                        if (serie.getSingleRow().getDataProcessor() != null)
                            dataObject = serie.getSingleRow().getDataProcessor();
                        else dataObject = serie.getSingleRow().getObject();

                        String userNote = getUserNoteForTimeStamp(nearestSample, nearestSample.getTimestamp());

                        RowNote rowNote = new RowNote(dataObject, nearestSample, title, userNote);

                        map.put(title, rowNote);
                    }
                } catch (Exception ex) {
                    logger.error("Error: could not get note", ex);
                }
            }

            NoteDialog nd = new NoteDialog(map);

            nd.showAndWait().ifPresent(response -> {
                if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                    saveUserNotes(nd.getNoteMap());
                }
            });
        }
    }


    @Override
    public void applyColors() {

        for (int i = 0; i < hexColors.size(); i++) {
            Color currentColor = hexColors.get(i);
            Color brighter = currentColor.deriveColor(1, 1, 50, 0.3);
            String hexColor = toRGBCode(currentColor);
            String hexBrighter = toRGBCode(brighter) + "55";
            String preIdent = ".default-color" + i;
            Node node = getChart().lookup(preIdent + ".chart-series-area-fill");
            Node nodew = getChart().lookup(preIdent + ".chart-series-area-line");

            if (node != null) {
                node.setStyle("-fx-fill: linear-gradient(" + hexColor + "," + hexBrighter + ");"
                        + "  -fx-background-insets: 0 0 -1 0, 0, 1, 2;"
                        + "  -fx-background-radius: 3px, 3px, 2px, 1px;");
            }
            if (nodew != null) {
                nodew.setStyle("-fx-stroke: " + hexColor + "; -fx-stroke-width: 2px; ");
            }
        }
    }

    @Override
    public DateTime getValueForDisplay() {
        return valueForDisplay;
    }

    @Override
    public void setValueForDisplay(DateTime valueForDisplay) {
        this.valueForDisplay = valueForDisplay;
    }

    @Override
    public javafx.scene.chart.Chart getChart() {
        return chart;
    }

    public void setChart(MultiAxisChart chart) {
        this.chart = chart;
    }

    @Override
    public Region getRegion() {
        return areaChartRegion;
    }

    private void checkForY2Axis() {
        boolean hasY2Axis = false;
        for (XYChartSerie serie : xyChartSerieList) {
            if (serie.getyAxis() == 1) {
                hasY2Axis = true;
                break;
            }
        }
        if (!hasY2Axis) y2Axis.setVisible(false);
        else y2Axis.setVisible(true);
    }

    @Override
    public ChartPanManager getPanner() {
        return panner;
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

    public DateTime getNearest() {
        return nearest;
    }
}