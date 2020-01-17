package org.jevis.jeconfig.application.Chart.Charts;

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.application.Chart.ChartElements.ColumnChartSerie;
import org.jevis.jeconfig.application.Chart.ChartElements.Note;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.Zoom.ChartPanManager;
import org.jevis.jeconfig.application.Chart.Zoom.JFXChartUtil;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.RowNote;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.jevis.jeconfig.dialog.NoteDialog;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

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
    DefaultNumericAxis y1Axis = new DefaultNumericAxis();
    DefaultNumericAxis y2Axis = new DefaultNumericAxis();
    private String chartName;
    private String unit;
    private List<ChartDataModel> chartDataModels;
    private Boolean hideShowIcons;
    private List<ColumnChartSerie> columnChartSerieList = new ArrayList<>();
    private XYChart columnChart;
    private List<Color> hexColors = new ArrayList<>();
    private DateTime valueForDisplay;
    private ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private Region barChartRegion;
    private Period period;
    private Region areaChartRegion;
    private AtomicReference<ManipulationMode> manipulationMode;
    private ChartPanManager panner;
    private DefaultNumericAxis catAxis = new DefaultNumericAxis();
    private DateTime nearest;
    private boolean hasSecondAxis = false;
    private ChartType chartType = ChartType.COLUMN;

    public ColumnChart(AnalysisDataModel analysisDataModel, List<ChartDataModel> chartDataModels, Integer chartId, String chartName) {
        this.chartDataModels = chartDataModels;
        this.showRawData = analysisDataModel.getShowRawData();
        this.showSum = analysisDataModel.getShowSum();
        this.hideShowIcons = analysisDataModel.getShowIcons();
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
                    if (singleRow.getAxis() == 1) {
                        hasSecondAxis = true;
                    }
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

        DefaultNumericAxis numberAxis1 = new DefaultNumericAxis();
        DefaultNumericAxis numberAxis2 = new DefaultNumericAxis();

        columnChart = new XYChart(catAxis, numberAxis1);

        if (hasSecondAxis) {

        }

        columnChart.legendVisibleProperty().set(false);
        columnChart.getPlugins().add(new Zoomer());

        columnChart.setTitle(chartName);
        columnChart.setLegendVisible(false);
        columnChart.getXAxis().setAutoRanging(true);

        columnChart.getXAxis().setName(unit);

        addSeriesToChart();

    }

    private void addSeriesToChart() {
        ErrorDataSetRenderer rendererY1 = new ErrorDataSetRenderer();
        ErrorDataSetRenderer rendererY2 = new ErrorDataSetRenderer();
        for (ColumnChartSerie columnChartSerie : columnChartSerieList) {

            if (!hasSecondAxis || columnChartSerie.getyAxis() == 0) {
                rendererY1.getDatasets().add(columnChartSerie.getDataSet());
            } else if (columnChartSerie.getyAxis() == 1) {
                rendererY2.getDatasets().add(columnChartSerie.getDataSet());
            }
            Platform.runLater(() -> tableData.add(columnChartSerie.getTableEntry()));
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
//        panner = null;
//
//        getChart().setOnMouseMoved(mouseEvent -> {
//            updateTable(mouseEvent, null);
//        });
//
//        panner = new ChartPanManager((MultiAxisChart<?, ?>) getChart());
//
//        panner.setMouseFilter(mouseEvent -> {
//            if (mouseEvent.getButton() != MouseButton.SECONDARY
//                    && (mouseEvent.getButton() != MouseButton.PRIMARY
//                    || !mouseEvent.isShortcutDown())) {
//                mouseEvent.consume();
//            }
//        });
//        panner.start();
//
//        JFXChartUtil jfxChartUtil = new JFXChartUtil(timeStampOfFirstSample.get().getMillis(), timeStampOfLastSample.get().getMillis());
//        areaChartRegion = jfxChartUtil.setupZooming((MultiAxisChart<?, ?>) getChart(), mouseEvent -> {
//
//            if (mouseEvent.getButton() != MouseButton.PRIMARY
//                    || mouseEvent.isShortcutDown()) {
//                mouseEvent.consume();
//                if (mouseEvent.isControlDown()) {
//                    showNote(mouseEvent);
//                }
//            }
//        });
//
//        jfxChartUtil.addDoublePrimaryClickAutoRangeHandler((MultiAxisChart<?, ?>) getChart());

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
    public void setDataModels(List<ChartDataModel> chartDataModels) {
        this.chartDataModels = chartDataModels;
    }

    @Override
    public void setShowIcons(Boolean showIcons) {
        this.hideShowIcons = showIcons;
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

//            x = ((MultiAxisChart) getChart()).getXAxis().sceneToLocal(Objects.requireNonNull(mouseCoordinates)).getX();
//
//            stringForDisplay = ((CategoryAxis) ((MultiAxisChart) getChart()).getXAxis()).getValueForDisplay(x);

            if (stringForDisplay != null) {
                valueForDisplay = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(stringForDisplay);
            }
        }
        if (valueForDisplay != null) {
            setValueForDisplay(valueForDisplay);
            DateTime finalValueForDisplay = valueForDisplay;
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);

            columnChartSerieList.parallelStream().forEach(serie -> {
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

                    if (sample != null) {
                        Double valueAsDouble = sample.getValueAsDouble();
                        Note formattedNote = new Note(sample, serie.getSingleRow().getNoteSamples().get(sample.getTimestamp()));
                        String formattedDouble = nf.format(valueAsDouble);

                        DateTime finalNearest = nearest;
                        Platform.runLater(() -> tableEntry.setDate(finalNearest
                                .toString(DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss"))));
                        Platform.runLater(() -> tableEntry.setNote(formattedNote.getNoteAsString()));
                        String unit = serie.getUnit();
                        Platform.runLater(() -> tableEntry.setValue(formattedDouble + " " + unit));
                    }
                } catch (Exception ex) {
                }

            });
        }
    }

    @Override
    public void updateTableZoom(double lowerBound, double upperBound) {

    }

    @Override
    public void showNote(MouseEvent mouseEvent) {
        if (manipulationMode.get().equals(ManipulationMode.NONE)) {

            Point2D mouseCoordinates = new Point2D(mouseEvent.getSceneX(), mouseEvent.getSceneY());
//            double x = ((MultiAxisChart) getChart()).getXAxis().sceneToLocal(mouseCoordinates).getX();

            Map<String, RowNote> map = new HashMap<>();
            DateTime valueForDisplay = null;
//            valueForDisplay = ((DateAxis) ((MultiAxisChart) getChart()).getXAxis()).getDateTimeForDisplay(x);

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
    public de.gsi.chart.XYChart getChart() {
        return columnChart;
    }

    @Override
    public ChartType getChartType() {
        return chartType;
    }

    @Override
    public Region getRegion() {
        return barChartRegion;
    }


}