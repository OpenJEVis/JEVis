package org.jevis.application.Chart.Charts;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.ChartElements.DateValueAxis;
import org.jevis.application.Chart.ChartElements.Note;
import org.jevis.application.Chart.ChartElements.TableEntry;
import org.jevis.application.Chart.ChartElements.XYChartSerie;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.application.dialog.NoteDialog;
import org.jevis.commons.unit.UnitManager;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.PeriodFormat;

import java.text.NumberFormat;
import java.util.*;

public class LineChart implements Chart {
    private static SaveResourceBundle rb = new SaveResourceBundle(AppLocale.BUNDLE_ID, AppLocale.getInstance().getLocale());
    private static final Logger logger = LogManager.getLogger(AreaChart.class);
    private String chartName;
    private List<String> unit = new ArrayList<>();
    private List<ChartDataModel> chartDataModels;
    private Boolean hideShowIcons;
    private ObservableList<XYChart.Series<Number, Number>> series = FXCollections.observableArrayList();
    private javafx.scene.chart.LineChart<Number, Number> lineChart;
    private List<Color> hexColors = new ArrayList<>();
    private Number valueForDisplay;
    private ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private Region lineChartRegion;
    private Period period;

    public LineChart(List<ChartDataModel> chartDataModels, Boolean hideShowIcons, String chartName) {
        this.chartDataModels = chartDataModels;
        this.hideShowIcons = hideShowIcons;
        this.chartName = chartName;
        init();
    }

    private void init() {
        for (ChartDataModel singleRow : chartDataModels) {
            try {

                XYChartSerie serie = new XYChartSerie(singleRow, hideShowIcons);

                hexColors.add(singleRow.getColor());
                series.add(serie.getSerie());
                tableData.add(serie.getTableEntry());
                String currentUnit = UnitManager.getInstance().formate(singleRow.getUnit());
                if (!unit.contains(currentUnit)) unit.add(currentUnit);

            } catch (JEVisException e) {
                e.printStackTrace();
            }
        }

        if (chartDataModels != null && chartDataModels.size() > 0) {
            if (unit.isEmpty()) unit.add(rb.getString("plugin.graph.chart.valueaxis.nounit"));
            period = chartDataModels.get(0).getAttribute().getDisplaySampleRate();
        }

        NumberAxis numberAxis = new NumberAxis();
        Axis dateAxis = new DateValueAxis();

        lineChart = new javafx.scene.chart.LineChart<>(dateAxis, numberAxis, series);
        lineChart.applyCss();

        applyColors();

        lineChart.setTitle(chartName);
        lineChart.setLegendVisible(false);
        lineChart.setCreateSymbols(true);

        lineChart.getXAxis().setAutoRanging(true);

        Period period = new Period(getStartDateTime(), getEndDateTime().plus(getPeriod()).plusSeconds(1)).minus(this.period);
        period = period.minusSeconds(period.getSeconds());
        String overall = period.toString(PeriodFormat.wordBased().withLocale(AppLocale.getInstance().getLocale()));

        lineChart.getXAxis().setLabel(rb.getString("plugin.graph.chart.dateaxis.title") + " " + overall);

        lineChart.getYAxis().setAutoRanging(true);
        String allUnits = "";
        for (String s : unit) {
            if (unit.indexOf(s) == 0) allUnits += s;
            else allUnits += ", " + s;
        }
        lineChart.getYAxis().setLabel(allUnits);

        initializeZoom();
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
    public void initializeZoom() {
        ChartPanManager panner = null;

        lineChart.setOnMouseMoved(mouseEvent -> {
            updateTable(mouseEvent, null);
        });

        panner = new ChartPanManager(lineChart);

        if (panner != null) {
            panner.setMouseFilter(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.SECONDARY
                        || (mouseEvent.getButton() == MouseButton.PRIMARY
                        && mouseEvent.isShortcutDown())) {
                } else {
                    mouseEvent.consume();
                }
            });
            panner.start();
        }

        lineChartRegion = JFXChartUtil.setupZooming(lineChart, mouseEvent -> {

            if (mouseEvent.getButton() != MouseButton.PRIMARY
                    || mouseEvent.isShortcutDown()) {
                mouseEvent.consume();
                if (mouseEvent.isControlDown()) {
                    showNote(mouseEvent);
                }
            }
        });

        JFXChartUtil.addDoublePrimaryClickAutoRangeHandler(lineChart);

    }

    @Override
    public String getChartName() {
        return chartName;
    }

    @Override
    public void updateTable(MouseEvent mouseEvent, Number valueForDisplay) {
        Point2D mouseCoordinates = null;
        if (mouseEvent != null) mouseCoordinates = new Point2D(mouseEvent.getSceneX(), mouseEvent.getSceneY());
        Double x = null;
        if (valueForDisplay == null) {

            x = lineChart.getXAxis().sceneToLocal(mouseCoordinates).getX();

            if (x != null) {
                valueForDisplay = lineChart.getXAxis().getValueForDisplay(x);

            }
        }
        if (valueForDisplay != null) {
            setValueForDisplay(valueForDisplay);
            tableData = FXCollections.emptyObservableList();
            Number finalValueForDisplay = valueForDisplay;
            chartDataModels.parallelStream().forEach(singleRow -> {
                if (Objects.isNull(chartName) || chartName.equals("") || singleRow.getSelectedcharts().contains(chartName)) {
                    try {
                        TreeMap<Double, JEVisSample> sampleTreeMap = singleRow.getSampleMap();
                        Double higherKey = sampleTreeMap.higherKey(finalValueForDisplay.doubleValue());
                        Double lowerKey = sampleTreeMap.lowerKey(finalValueForDisplay.doubleValue());

                        Double nearest = higherKey;
                        if (nearest == null) nearest = lowerKey;

                        if (lowerKey != null && higherKey != null) {
                            Double lower = Math.abs(lowerKey - finalValueForDisplay.doubleValue());
                            Double higher = Math.abs(higherKey - finalValueForDisplay.doubleValue());
                            if (lower < higher) {
                                nearest = lowerKey;
                            }
                        }

                        NumberFormat nf = NumberFormat.getInstance();
                        nf.setMinimumFractionDigits(2);
                        nf.setMaximumFractionDigits(2);
                        Double valueAsDouble = sampleTreeMap.get(nearest).getValueAsDouble();
                        String note = sampleTreeMap.get(nearest).getNote();
                        Note formattedNote = new Note(note, singleRow.getColor());
                        String formattedDouble = nf.format(valueAsDouble);
                        TableEntry tableEntry = singleRow.getTableEntry();
                        tableEntry.setDate(new DateTime(Math.round(nearest)).toString(DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss")));
                        tableEntry.setNote(formattedNote.getNote());
                        String unit = UnitManager.getInstance().formate(singleRow.getUnit());
                        tableEntry.setValue(formattedDouble + " " + unit);
                        tableEntry.setPeriod(getPeriod().toString(PeriodFormat.wordBased().withLocale(AppLocale.getInstance().getLocale())));
                        tableData.add(tableEntry);
                    } catch (Exception ex) {
                    }
                }
            });
        }

    }

    @Override
    public void showNote(MouseEvent mouseEvent) {

        Point2D mouseCoordinates = new Point2D(mouseEvent.getSceneX(), mouseEvent.getSceneY());
        Double x = lineChart.getXAxis().sceneToLocal(mouseCoordinates).getX();

        if (x != null) {
            Map<String, String> map = new HashMap<>();
            Number valueForDisplay = null;
            valueForDisplay = lineChart.getXAxis().getValueForDisplay(x);
            for (ChartDataModel singleRow : chartDataModels) {
                if (Objects.isNull(chartName) || chartName.equals("") || singleRow.getSelectedcharts().contains(chartName)) {
                    try {
                        Double higherKey = singleRow.getSampleMap().higherKey(valueForDisplay.doubleValue());
                        Double lowerKey = singleRow.getSampleMap().lowerKey(valueForDisplay.doubleValue());

                        Double nearest = higherKey;
                        if (nearest == null) nearest = lowerKey;

                        if (lowerKey != null && higherKey != null) {
                            Double lower = Math.abs(lowerKey - valueForDisplay.doubleValue());
                            Double higher = Math.abs(higherKey - valueForDisplay.doubleValue());
                            if (lower < higher) {
                                nearest = lowerKey;
                            }

                            String note = singleRow.getSampleMap().get(nearest).getNote();

                            String title = "";
                            title += singleRow.getObject().getName();

                            map.put(title, note);
                        }
                    } catch (Exception ex) {
                        logger.error("Error: could not get note", ex);
                    }
                }
            }

            NoteDialog nd = new NoteDialog(map);

            nd.showAndWait().ifPresent(response -> {
                if (response.getButtonData().getTypeCode() == ButtonType.OK.getButtonData().getTypeCode()) {

                } else if (response.getButtonData().getTypeCode() == ButtonType.CANCEL.getButtonData().getTypeCode()) {

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

            Node node = lineChart.lookup(preIdent + ".chart-series-line");
            node.setStyle("-fx-stroke: " + hexColor + "; -fx-stroke-width: 2px; ");
        }
    }

    @Override
    public Number getValueForDisplay() {
        return valueForDisplay;
    }

    @Override
    public void setValueForDisplay(Number valueForDisplay) {
        this.valueForDisplay = valueForDisplay;
    }

    @Override
    public javafx.scene.chart.Chart getChart() {
        return lineChart;
    }

    @Override
    public Region getRegion() {
        return lineChartRegion;
    }

}