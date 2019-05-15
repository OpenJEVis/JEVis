package org.jevis.jeconfig.application.Chart.Charts;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
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
import org.jevis.jeconfig.application.Chart.ChartElements.*;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisChart;
import org.jevis.jeconfig.application.Chart.Zoom.ChartPanManager;
import org.jevis.jeconfig.application.Chart.Zoom.JFXChartUtil;
import org.jevis.jeconfig.application.Chart.data.RowNote;
import org.jevis.jeconfig.dialog.NoteDialog;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.PeriodFormat;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class BarChart implements Chart {
    private static final Logger logger = LogManager.getLogger(BarChart.class);
    private final Integer chartId;
    AtomicReference<DateTime> timeStampOfFirstSample = new AtomicReference<>(DateTime.now());
    AtomicReference<DateTime> timeStampOfLastSample = new AtomicReference<>(new DateTime(2001, 1, 1, 0, 0, 0));
    NumberAxis y1Axis = new NumberAxis();
    NumberAxis y2Axis = new NumberAxis();
    private String chartName;
    private String unit;
    private List<ChartDataModel> chartDataModels;
    private Boolean hideShowIcons;
    private List<XYChartSerie> xyChartSerieList = new ArrayList<>();
    private javafx.scene.chart.BarChart barChart;
    private ObservableList<XYChart.Series<Number, String>> series = FXCollections.observableArrayList();
    private List<Color> hexColors = new ArrayList<>();
    private DateTime valueForDisplay;
    private ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private Region barChartRegion;
    private Period period;
    private Region areaChartRegion;
    private boolean asDuration = false;
    private AtomicReference<ManipulationMode> manipulationMode;
    private ChartPanManager panner;

    public BarChart(List<ChartDataModel> chartDataModels, Boolean hideShowIcons, Integer chartId, String chartName) {
        this.chartDataModels = chartDataModels;
        this.hideShowIcons = hideShowIcons;
        this.chartId = chartId;
        this.chartName = chartName;
        init();
    }

    private void init() {
        manipulationMode = new AtomicReference<>(ManipulationMode.NONE);

        chartDataModels.forEach(singleRow -> {
            if (!singleRow.getSelectedcharts().isEmpty()) {
                try {
                    BarChartSerie serie = new BarChartSerie(singleRow, hideShowIcons);


                    hexColors.add(singleRow.getColor());
                    series.add(serie.getSerie());
                    tableData.add(serie.getTableEntry());

                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }
        });

        if (chartDataModels != null && chartDataModels.size() > 0) {
            unit = UnitManager.getInstance().format(chartDataModels.get(0).getUnit());
            if (unit.equals("")) unit = I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit");
        }

        NumberAxis numberAxis = new NumberAxis();
        CategoryAxis catAxis = new CategoryAxis();

        barChart = new javafx.scene.chart.BarChart(numberAxis, catAxis, series);
        barChart.applyCss();

        applyColors();

        barChart.setTitle(chartName);
        barChart.setLegendVisible(false);
        barChart.getXAxis().setAutoRanging(true);
        //barChart.getXAxis().setLabel(I18n.getInstance().getString("plugin.graph.chart.dateaxis.title"));
        barChart.getXAxis().setTickLabelRotation(-90);
        barChart.getXAxis().setLabel(unit);

        //initializeZoom();
        setTimer();
    }

    private void setTimer() {
        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {

                        try {
                            TimeUnit.SECONDS.sleep(60);
                            //System.out.println("Reloading");
                            Platform.runLater(BarChart.this::updateChart);
                        } catch (InterruptedException e) {
                            logger.error("Sleep interrupted: " + e);
                        }
                        succeeded();

                        return null;
                    }
                };
            }
        };

        service.start();
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

        JFXChartUtil jfxChartUtil = new JFXChartUtil();
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
        return chartDataModels.get(0).getSelectedStart();
    }

    @Override
    public DateTime getEndDateTime() {
        return chartDataModels.get(0).getSelectedEnd();
    }

    @Override
    public void updateChart() {
        chartDataModels.forEach(singleRow -> {
            JEVisAttribute att = singleRow.getAttribute();
            if (att != null) {
                try {
                    att.getDataSource().reloadAttribute(att);
                } catch (JEVisException e) {
                    logger.error("Could not reload Attribute: " + att.getObject().getName() + ":" + att.getObject().getID() + ":" + att.getName());
                }
            }
        });


        manipulationMode = new AtomicReference<>(ManipulationMode.NONE);

        series.clear();
        hexColors.clear();
        tableData.clear();

        chartDataModels.forEach(singleRow -> {
            if (!singleRow.getSelectedcharts().isEmpty()) {
                try {
                    BarChartSerie serie = new BarChartSerie(singleRow, hideShowIcons);


                    hexColors.add(singleRow.getColor());
                    series.add(serie.getSerie());
                    tableData.add(serie.getTableEntry());

                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }
        });

        if (chartDataModels != null && chartDataModels.size() > 0) {
            unit = UnitManager.getInstance().format(chartDataModels.get(0).getUnit());
            if (unit.equals("")) unit = I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit");

        }

        barChart.applyCss();
        applyColors();

        barChart.setTitle(chartName);
        barChart.getXAxis().setLabel(unit);
        setTimer();
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

            valueForDisplay = ((DateValueAxis) ((MultiAxisChart) getChart()).getXAxis()).getDateTimeForDisplay(x);

        }
        if (valueForDisplay != null) {
            setValueForDisplay(valueForDisplay);
            DateTime finalValueForDisplay = valueForDisplay;
            xyChartSerieList.parallelStream().forEach(serie -> {
                try {
                    TableEntry tableEntry = serie.getTableEntry();
                    TreeMap<DateTime, JEVisSample> sampleTreeMap = serie.getSampleMap();

                    DateTime nearest = sampleTreeMap.lowerKey(finalValueForDisplay);

                    NumberFormat nf = NumberFormat.getInstance();
                    nf.setMinimumFractionDigits(2);
                    nf.setMaximumFractionDigits(2);
                    Double valueAsDouble = sampleTreeMap.get(nearest).getValueAsDouble();
                    JEVisSample sample = sampleTreeMap.get(nearest);
                    Note formattedNote = new Note(sample);
                    String formattedDouble = nf.format(valueAsDouble);

                    if (!asDuration) {
                        tableEntry.setDate(nearest
                                .toString(DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss")));
                    } else {
                        tableEntry.setDate((nearest.getMillis() -
                                timeStampOfFirstSample.get().getMillis()) / 1000 / 60 / 60 + " h");
                    }
                    tableEntry.setNote(formattedNote.getNoteAsString());
                    String unit = serie.getUnit();
                    tableEntry.setValue(formattedDouble + " " + unit);
                    tableEntry.setPeriod(getPeriod().toString(PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale())));
                } catch (Exception ex) {
                }

            });
        }
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
            String hexColor = toRGBCode(currentColor);
            String preIdent = ".default-color" + i;
            Node node = barChart.lookup(preIdent + ".chart-bar");
            node.setStyle("-fx-bar-fill: " + hexColor + ";");
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
        return barChart;
    }

    @Override
    public Region getRegion() {
        return barChartRegion;
    }


}