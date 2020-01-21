/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.jevis.jeconfig.application.Chart.ChartPluginElements;

import de.gsi.chart.Chart;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.plugins.AbstractDataFormattingPlugin;
import de.gsi.dataset.DataSet;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.application.Chart.ChartElements.Note;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.ChartElements.XYChartSerie;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.Charts.LogicalChart;
import org.jevis.jeconfig.application.Chart.Charts.TableChart;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DataPointTableViewPointer extends AbstractDataFormattingPlugin {

    private final ObservableList<TableEntry> tableData;
    private final List<TreeMap<DateTime, JEVisSample>> sampleTreeMaps;
    private final SimpleObjectProperty<DateTime> valueForDisplay = new SimpleObjectProperty<>();
    private final org.jevis.jeconfig.application.Chart.Charts.XYChart currentChart;
    private final List<org.jevis.jeconfig.application.Chart.Charts.Chart> notActiveCharts;
    private List<Map<DateTime, JEVisSample>> noteMaps;
    private DateTime timestampFromFirstSample = null;
    private boolean asDuration = false;
    private final EventHandler<MouseEvent> mouseMoveHandler = this::updateTable;
    private final List<XYChartSerie> xyChartSerieList;

    public DataPointTableViewPointer(org.jevis.jeconfig.application.Chart.Charts.Chart chart, List<org.jevis.jeconfig.application.Chart.Charts.Chart> notActive) {
        this.currentChart = (org.jevis.jeconfig.application.Chart.Charts.XYChart) chart;
        this.notActiveCharts = notActive;
        this.tableData = this.currentChart.getTableData();
        this.sampleTreeMaps = this.currentChart.getValueTreeMaps();
        this.noteMaps = this.currentChart.getNoteMaps();
        this.asDuration = this.currentChart.isAsDuration();
        this.xyChartSerieList = this.currentChart.getXyChartSerieList();
        this.timestampFromFirstSample = this.currentChart.getTimeStampOfFirstSample().get();

        registerInputEventHandler(MouseEvent.MOUSE_MOVED, mouseMoveHandler);
    }

    public SimpleObjectProperty<DateTime> valueForDisplayProperty() {
        return valueForDisplay;
    }

    private DataPoint findDataPoint(final MouseEvent event, final Bounds plotAreaBounds) {
        if (!plotAreaBounds.contains(event.getX(), event.getY())) {
            return null;
        }

        final Point2D mouseLocation = getLocationInPlotArea(event);
        DataPoint nearestDataPoint = null;

        Chart chart = getChart();
        return findNearestDataPointWithinPickingDistance(chart, mouseLocation);
    }

    private DataPoint findNearestDataPointWithinPickingDistance(final Chart chart, final Point2D mouseLocation) {
        DataPoint nearestDataPoint = null;
        if (!(chart instanceof XYChart)) {
            return null;
        }
        final XYChart xyChart = (XYChart) chart;
        // final double xValue = toDataPoint(xyChart.getYAxis(),
        // mouseLocation).getXValue().doubleValue();
        // TODO: iterate through all axes, renderer and datasets
        final double xValue = xyChart.getXAxis().getValueForDisplay(mouseLocation.getX());

        for (final DataPointTableViewPointer.DataPoint dataPoint : findNeighborPoints(xyChart, xValue)) {
            // Point2D displayPoint = toDisplayPoint(chart.getYAxis(),
            // (X)dataPoint.x , dataPoint.y);
            if (getChart().getFirstAxis(Orientation.HORIZONTAL) instanceof Axis) {
                final double x = xyChart.getXAxis().getDisplayPosition(dataPoint.x);
                final double y = xyChart.getYAxis().getDisplayPosition(dataPoint.y);
                final Point2D displayPoint = new Point2D(x, y);
                dataPoint.distanceFromMouse = displayPoint.distance(mouseLocation);
                if (displayPoint.distance(mouseLocation) <= 10000 && (nearestDataPoint == null
                        || dataPoint.distanceFromMouse < nearestDataPoint.distanceFromMouse)) {
                    nearestDataPoint = dataPoint;
                }
            }
        }
        return nearestDataPoint;
    }

    /**
     * Handles series that have data sorted or not sorted with respect to X coordinate.
     *
     * @param dataSet   data set
     * @param searchedX x coordinate
     * @return return neighouring data points
     */
    private Pair<DataPoint, DataPoint> findNeighborPoints(final DataSet dataSet, final double searchedX) {
        int prevIndex = -1;
        int nextIndex = -1;
        double prevX = Double.MIN_VALUE;
        double nextX = Double.MAX_VALUE;

        final int nDataCount = dataSet.getDataCount(DataSet.DIM_X);
        for (int i = 0, size = nDataCount; i < size; i++) {
            final double currentX = dataSet.get(DataSet.DIM_X, i);

            if (currentX < searchedX) {
                if (prevX < currentX) {
                    prevIndex = i;
                    prevX = currentX;
                }
            } else if (nextX > currentX) {
                nextIndex = i;
                nextX = currentX;
            }
        }
        final DataPoint prevPoint = prevIndex == -1 ? null
                : new DataPoint(getChart(), dataSet.get(DataSet.DIM_X, prevIndex),
                dataSet.get(DataSet.DIM_Y, prevIndex), getDataLabelSafe(dataSet, prevIndex));
        final DataPoint nextPoint = nextIndex == -1 || nextIndex == prevIndex ? null
                : new DataPoint(getChart(), dataSet.get(DataSet.DIM_X, nextIndex),
                dataSet.get(DataSet.DIM_X, nextIndex), getDataLabelSafe(dataSet, nextIndex));

        return new Pair<>(prevPoint, nextPoint);
    }

    private List<DataPoint> findNeighborPoints(final XYChart chart, final double searchedX) {
        final List<DataPoint> points = new LinkedList<>();
        for (final DataSet dataSet : chart.getAllDatasets()) {
            final Pair<DataPoint, DataPoint> neighborPoints = findNeighborPoints(dataSet, searchedX);
            if (neighborPoints.getKey() != null) {
                points.add(neighborPoints.getKey());
            }
            if (neighborPoints.getValue() != null) {
                points.add(neighborPoints.getValue());
            }
        }
        return points;
    }

    protected String getDataLabelSafe(final DataSet dataSet, final int index) {
        String lable = dataSet.getDataLabel(index);
        if (lable == null) {
            return getDefaultDataLabel(dataSet, index);
        }
        return lable;
    }

    /**
     * Returns label of a data point specified by the index. The label can be used as a category name if
     * CategoryStepsDefinition is used or for annotations displayed for data points.
     *
     * @param index data point index
     * @return label of a data point specified by the index or <code>null</code> if none label has been specified for
     * this data point.
     */
    protected String getDefaultDataLabel(final DataSet dataSet, final int index) {
        return String.format("%s (%d, %s, %s)", dataSet.getName(), index,
                dataSet.get(DataSet.DIM_X, index), dataSet.get(DataSet.DIM_Y, index));
    }


    private void updateTable(final MouseEvent event) {
        final Bounds plotAreaBounds = getChart().getPlotArea().getBoundsInLocal();
        final DataPoint dataPoint = findDataPoint(event, plotAreaBounds);

        if (dataPoint != null) {
            Double v = dataPoint.getX() * 1000d;
            DateTime nearest = new DateTime(v.longValue());

            updateTable(nearest);

            if (!notActiveCharts.isEmpty()) {
                notActiveCharts.forEach(chart -> {
                    if (!chart.getChartType().equals(ChartType.PIE)
                            && !chart.getChartType().equals(ChartType.BAR)
                            && !chart.getChartType().equals(ChartType.BUBBLE)
                            && !chart.getChartType().equals(ChartType.TABLE)) {
                        chart.getChart().getPlugins().forEach(chartPlugin -> {
                            if (chartPlugin instanceof DataPointTableViewPointer) {
                                ((DataPointTableViewPointer) chartPlugin).updateTable(nearest);
                            }
                        });
                    } else if (chart.getChartType().equals(ChartType.TABLE)) {

                        TableChart tableChart = (TableChart) chart;
                        tableChart.updateTable(null, nearest);
                        tableChart.setBlockDatePickerEvent(true);
                        TableTopDatePicker tableTopDatePicker = tableChart.getTableTopDatePicker();
                        ComboBox<DateTime> datePicker = tableTopDatePicker.getDatePicker();
                        Platform.runLater(() -> {
                            datePicker.getSelectionModel().select(nearest);
                            tableChart.setBlockDatePickerEvent(false);
                        });
                    }
                });
            }
        }
    }

    public void updateTable(DateTime nearest) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);

        tableData.forEach(tableEntry -> {
            try {
                int i = tableData.indexOf(tableEntry);
                XYChartSerie xyChartSerie = xyChartSerieList.get(i);
                TreeMap<DateTime, JEVisSample> sampleTreeMap = sampleTreeMaps.get(i);
                Map<DateTime, JEVisSample> noteMap = noteMaps.get(i);

                DateTime dateTime = nearest;
                if (currentChart instanceof LogicalChart) {
                    dateTime = sampleTreeMap.lowerKey(nearest);
                }
                DateTime finalDateTime = dateTime;

                JEVisSample sample = sampleTreeMap.get(finalDateTime);

                Note formattedNote = new Note(sample, noteMap.get(sample.getTimestamp()));

                if (!asDuration) {
                    Platform.runLater(() -> {
                        tableEntry.setDate(finalDateTime
                                .toString(DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss")));
                    });
                } else {
                    Platform.runLater(() -> tableEntry.setDate((finalDateTime.getMillis() -
                            timestampFromFirstSample.getMillis()) / 1000 / 60 / 60 + " h"));
                }
                Platform.runLater(() -> tableEntry.setNote(formattedNote.getNoteAsString()));
                String unit = xyChartSerie.getUnit();

                if (!sample.getNote().contains("Zeros")) {
                    Double valueAsDouble = null;
                    String formattedDouble = null;

                    if (!xyChartSerie.getSingleRow().isStringData()) {
                        try {
                            valueAsDouble = sample.getValueAsDouble();
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
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

            } catch (Exception ignored) {
            }
        });
    }

    protected class DataPoint {

        protected final Chart chart;
        protected final double x;
        protected final double y;
        protected final String label;
        protected double distanceFromMouse;

        protected DataPoint(final Chart chart, final double x, final double y, final String label) {
            this.chart = chart;
            this.x = x;
            this.y = y;
            this.label = label;
        }

        public Chart getChart() {
            return chart;
        }

        public double getDistanceFromMouse() {
            return distanceFromMouse;
        }

        public String getLabel() {
            return label;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

    }
}
