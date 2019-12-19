package org.jevis.jeconfig.application.Chart.Charts;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.application.Chart.ChartElements.ColumnChartSerie;
import org.jevis.jeconfig.application.Chart.ChartElements.DateAxis;
import org.jevis.jeconfig.application.Chart.ChartElements.Note;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisBarChart;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisChart;
import org.jevis.jeconfig.application.Chart.Charts.jfx.CategoryAxis;
import org.jevis.jeconfig.application.Chart.Charts.jfx.NumberAxis;
import org.jevis.jeconfig.application.Chart.Zoom.ChartPanManager;
import org.jevis.jeconfig.application.Chart.Zoom.JFXChartUtil;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.RowNote;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.jevis.jeconfig.dialog.NoteDialog;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.PeriodFormat;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ColumnChart implements Chart {
    private static final Logger logger = LogManager.getLogger(ColumnChart.class);
    private final Integer chartId;
    private final Boolean showRawData;
    private final Boolean showSum;
    AtomicReference<DateTime> timeStampOfFirstSample = new AtomicReference<>(DateTime.now());
    AtomicReference<DateTime> timeStampOfLastSample = new AtomicReference<>(new DateTime(2001, 1, 1, 0, 0, 0));
    NumberAxis y1Axis = new NumberAxis();
    NumberAxis y2Axis = new NumberAxis();
    private String chartName;
    private String unit;
    private List<ChartDataModel> chartDataModels;
    private Boolean hideShowIcons;
    private List<ColumnChartSerie> columnChartSerieList = new ArrayList<>();
    private MultiAxisBarChart columnChart;
    private List<Color> hexColors = new ArrayList<>();
    private DateTime valueForDisplay;
    private ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private Region barChartRegion;
    private Period period;
    private Region areaChartRegion;
    private AtomicReference<ManipulationMode> manipulationMode;
    private ChartPanManager panner;
    private CategoryAxis catAxis = new CategoryAxis();
    private DateTime nearest;

    public ColumnChart(AnalysisDataModel analysisDataModel, List<ChartDataModel> chartDataModels, Integer chartId, String chartName) {
        this.chartDataModels = chartDataModels;
        this.showRawData = analysisDataModel.getShowRawData();
        this.showSum = analysisDataModel.getShowSum();
        this.hideShowIcons = analysisDataModel.getHideShowIcons();
        this.chartId = chartId;
        this.chartName = chartName;
        init();
    }

    private void init() {
        manipulationMode = new AtomicReference<>(ManipulationMode.NONE);

        for (ChartDataModel singleRow : chartDataModels) {
            if (!singleRow.getSelectedcharts().isEmpty()) {
                try {
                    ColumnChartSerie serie = new ColumnChartSerie(singleRow, hideShowIcons, false);
                    columnChartSerieList.add(serie);
                    hexColors.add(ColorHelper.toColor(singleRow.getColor()));

                    if (singleRow.hasForecastData()) {
                        try {
                            ColumnChartSerie forecast = new ColumnChartSerie(singleRow, hideShowIcons, true);

                            hexColors.add(ColorHelper.toColor(ColorHelper.colorToBrighter(singleRow.getColor())));
                            columnChartSerieList.add(forecast);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }
        }

        if (chartDataModels != null && chartDataModels.size() > 0) {
            unit = UnitManager.getInstance().format(chartDataModels.get(0).getUnit());
            if (unit.equals("")) unit = I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit");
        }

        NumberAxis numberAxis1 = new NumberAxis();
        NumberAxis numberAxis2 = new NumberAxis();

        columnChart = new MultiAxisBarChart(catAxis, numberAxis1, numberAxis2);

        columnChart.setTitle(chartName);
        columnChart.setLegendVisible(false);
        columnChart.getXAxis().setAutoRanging(true);

        columnChart.getXAxis().setLabel(unit);

        addSeriesToChart();

    }

    private void addSeriesToChart() {
        for (ColumnChartSerie columnChartSerie : columnChartSerieList) {
            Platform.runLater(() -> {
                columnChart.getData().add(columnChartSerie.getSerie());
                tableData.add(columnChartSerie.getTableEntry());
            });
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
    public void setChartSettings(ChartSettingsFunction function) {
        //TODO: implement me, see PieChart
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

        JFXChartUtil jfxChartUtil = new JFXChartUtil(timeStampOfFirstSample.get().getMillis(), timeStampOfLastSample.get().getMillis());
        areaChartRegion = jfxChartUtil.setupZooming((MultiAxisChart<?, ?>) getChart(), mouseEvent -> {

            if (mouseEvent.getButton() != MouseButton.PRIMARY
                    || mouseEvent.isShortcutDown()) {
                mouseEvent.consume();
                if (mouseEvent.isControlDown()) {
                    showNote(mouseEvent);
                }
            }
        });

        jfxChartUtil.addDoublePrimaryClickAutoRangeHandler((MultiAxisChart<?, ?>) getChart());

    }

    @Override
    public DateTime getStartDateTime() {
        return timeStampOfFirstSample.get();
    }

    @Override
    public DateTime getEndDateTime() {
        return timeStampOfLastSample.get();
    }

    @Override
    public void updateChart() {
        for (ChartDataModel chartDataModel : chartDataModels) {
            JEVisAttribute att = chartDataModel.getAttribute();
            if (att != null) {
                try {
                    att.getDataSource().reloadAttribute(att);
                } catch (JEVisException e) {
                    logger.error("Could not reload Attribute: " + att.getObject().getName() + ":" + att.getObject().getID() + ":" + att.getName());
                }
            }
        }


        manipulationMode = new AtomicReference<>(ManipulationMode.NONE);

//        series.clear();
        hexColors.clear();
        tableData.clear();

        for (ChartDataModel singleRow : chartDataModels) {
            if (!singleRow.getSelectedcharts().isEmpty()) {
                try {
                    ColumnChartSerie serie = new ColumnChartSerie(singleRow, hideShowIcons, false);

                    hexColors.add(ColorHelper.toColor(singleRow.getColor()));
//                    series.add(serie.getSerie());
                    tableData.add(serie.getTableEntry());

                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }
        }

        if (chartDataModels != null && chartDataModels.size() > 0) {
            unit = UnitManager.getInstance().format(chartDataModels.get(0).getUnit());
            if (unit.equals("")) unit = I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit");

        }

        columnChart.applyCss();

        columnChart.setTitle(chartName);
//        columnChart.getXAxis().setTickLabelRotation(-90d);
        columnChart.getXAxis().setLabel(unit);
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
    public ChartPanManager getPanner() {
        return panner;
    }

    @Override
    public JFXChartUtil getJfxChartUtil() {
        return null;
    }

    @Override
    public void setRegion(Region region) {
        barChartRegion = region;
    }

    @Override
    public void checkForY2Axis() {
        try {
            boolean hasY2Axis = false;
            for (ColumnChartSerie serie : columnChartSerieList) {
                if (serie.getyAxis() == 1) {
                    hasY2Axis = true;
                    break;
                }
            }

            if (!hasY2Axis) y2Axis.setVisible(false);
            else y2Axis.setVisible(true);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    @Override
    public void applyBounds() {

    }

    @Override
    public List<ChartDataModel> getChartDataModels() {
        return chartDataModels;
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
    public void updateTable(MouseEvent mouseEvent, DateTime valueForDisplay) {
        Point2D mouseCoordinates = null;
        if (mouseEvent != null) mouseCoordinates = new Point2D(mouseEvent.getSceneX(), mouseEvent.getSceneY());
        Double x = null;
        String stringForDisplay = null;
        if (valueForDisplay == null) {

            x = ((MultiAxisChart) getChart()).getXAxis().sceneToLocal(Objects.requireNonNull(mouseCoordinates)).getX();

            valueForDisplay = ((DateAxis) ((MultiAxisChart) getChart()).getXAxis()).getDateTimeForDisplay(x);

        }
        if (valueForDisplay != null) {
            setValueForDisplay(valueForDisplay);
            for (ColumnChartSerie serie : columnChartSerieList) {
                try {
                    TableEntry tableEntry = serie.getTableEntry();
                    TreeMap<DateTime, JEVisSample> sampleTreeMap = serie.getSampleMap();

                    DateTime nearest = sampleTreeMap.lowerKey(valueForDisplay);

                    JEVisSample sample = sampleTreeMap.get(nearest);

                    if (sample != null) {
                        NumberFormat nf = NumberFormat.getInstance();
                        nf.setMinimumFractionDigits(2);
                        nf.setMaximumFractionDigits(2);
                        Double valueAsDouble = sample.getValueAsDouble();
                        Note formattedNote = new Note(sample, serie.getSingleRow().getNoteSamples().get(sample.getTimestamp()));
                        String formattedDouble = nf.format(valueAsDouble);

                        boolean asDuration = false;
                        if (!asDuration) {
                            Platform.runLater(() -> tableEntry.setDate(nearest
                                    .toString(DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss"))));
                        } else {
                            Platform.runLater(() -> tableEntry.setDate((nearest.getMillis() -
                                    timeStampOfFirstSample.get().getMillis()) / 1000 / 60 / 60 + " h"));
                        }
                        Platform.runLater(() -> tableEntry.setNote(formattedNote.getNoteAsString()));
                        String unit = serie.getUnit();
                        Platform.runLater(() -> tableEntry.setValue(formattedDouble + " " + unit));
                        Platform.runLater(() -> tableEntry.setPeriod(getPeriod().toString(PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale()))));
                    }
                } catch (Exception ex) {
                }

            }
        }
    }

    @Override
    public void updateTableZoom(Long lowerBound, Long upperBound) {

    }

    @Override
    public void showNote(MouseEvent mouseEvent) {
        if (manipulationMode.get().equals(ManipulationMode.NONE)) {

            Point2D mouseCoordinates = new Point2D(mouseEvent.getSceneX(), mouseEvent.getSceneY());
            double x = ((MultiAxisChart) getChart()).getXAxis().sceneToLocal(mouseCoordinates).getX();

            Map<String, RowNote> map = new HashMap<>();
            DateTime valueForDisplay = null;
            valueForDisplay = ((DateAxis) ((MultiAxisChart) getChart()).getXAxis()).getDateTimeForDisplay(x);

            for (ColumnChartSerie serie : columnChartSerieList) {
                try {
                    nearest = serie.getSampleMap().lowerKey(valueForDisplay);

                    if (nearest != null) {

                        JEVisSample nearestSample = serie.getSampleMap().get(nearest);

                        String title = "";
                        title += serie.getSingleRow().getObject().getName();

                        JEVisObject dataObject;
                        if (serie.getSingleRow().getDataProcessor() != null)
                            dataObject = serie.getSingleRow().getDataProcessor();
                        else dataObject = serie.getSingleRow().getObject();

                        String userNote = "";
                        JEVisSample noteSample = serie.getSingleRow().getNoteSamples().get(nearestSample.getTimestamp());

                        if (noteSample != null) {
                            userNote = noteSample.getValueAsString();
                        }

                        //String userNote = getUserNoteForTimeStamp(nearestSample, nearestSample.getTimestamp());

                        String userValue = getUserValueForTimeStamp(nearestSample, nearestSample.getTimestamp());

                        RowNote rowNote = new RowNote(dataObject, nearestSample, serie.getSingleRow().getNoteSamples().get(nearestSample.getTimestamp()), title, userNote, userValue, serie.getUnit(), serie.getSingleRow().getScaleFactor());

                        map.put(title, rowNote);
                    }
                } catch (Exception ex) {
                    logger.error("Error: could not get note", ex);
                }
            }

            NoteDialog nd = new NoteDialog(map);

            nd.showAndWait().ifPresent(response -> {
                if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                    saveUserEntries(nd.getNoteMap());
                }
            });
        }
    }

    @Override
    public void applyColors() {
        for (int i = 0; i < hexColors.size(); i++) {
            Color currentColor = hexColors.get(i);
            String hexColor = ColorHelper.toRGBCode(currentColor);
            for (Node n : columnChart.lookupAll(".default-color" + i + ".chart-bar")) {
                n.setStyle("-fx-bar-fill: " + hexColor + ";");
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
    public org.jevis.jeconfig.application.Chart.Charts.jfx.Chart getChart() {
        return columnChart;
    }

    @Override
    public Region getRegion() {
        return barChartRegion;
    }


}