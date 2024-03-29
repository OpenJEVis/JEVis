/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.jevis.jeconfig.application.Chart.ChartPluginElements;

import com.jfoenix.controls.JFXComboBox;
import de.gsi.chart.Chart;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.plugins.AbstractDataFormattingPlugin;
import de.gsi.dataset.DataSet;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.alarm.Alarm;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.jeconfig.application.Chart.ChartElements.Note;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.ChartElements.XYChartSerie;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.Charts.BubbleChart;
import org.jevis.jeconfig.application.Chart.Charts.LogicalChart;
import org.jevis.jeconfig.application.Chart.Charts.TableChart;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

public class DataPointTableViewPointer extends AbstractDataFormattingPlugin {

    private static final Logger logger = LogManager.getLogger(DataPointTableViewPointer.class);
    private final org.jevis.jeconfig.application.Chart.Charts.XYChart currentChart;
    private final List<org.jevis.jeconfig.application.Chart.Charts.Chart> notActiveCharts;
    private final List<XYChartSerie> xyChartSerieList;
    boolean plotArea = true;
    private DateTime timestampOfFirstSample = null;
    private boolean asDuration = false;
    private WorkDays workDays;

    public DataPointTableViewPointer(org.jevis.jeconfig.application.Chart.Charts.Chart chart, List<org.jevis.jeconfig.application.Chart.Charts.Chart> notActive) {
        super();
        this.currentChart = (org.jevis.jeconfig.application.Chart.Charts.XYChart) chart;
        this.notActiveCharts = notActive;
        this.asDuration = this.currentChart.isAsDuration();
        this.xyChartSerieList = this.currentChart.getXyChartSerieList();

        for (ChartDataRow chartDataRow : this.currentChart.getChartDataRows()) {
            workDays = new WorkDays(chartDataRow.getObject());
            break;
        }

        this.timestampOfFirstSample = this.currentChart.getTimeStampOfFirstSample().get();

        EventHandler<MouseEvent> mouseMoveHandler = event -> {
            try {
                plotArea = true;
                updateTable(event);
            } catch (Exception e) {
                logger.error("Error on mouse move handler", e);
            }
        };
        registerInputEventHandler(MouseEvent.MOUSE_MOVED, mouseMoveHandler);

        this.currentChart.getChart().setOnMouseMoved(event -> {
            try {
                plotArea = false;
                updateTable(event);
            } catch (Exception e) {
                logger.error("Error on mouse moved handler", e);
            }
        });
    }

    private DataPoint findNearestDataPointWithinPickingDistance(final Chart chart, final Point2D mouseLocation, Axis axis) {
        DataPoint nearestDataPoint = null;
        if (!(chart instanceof XYChart)) {
            return null;
        }

        final XYChart xyChart = (XYChart) chart;
        Axis xAxis;
        if (axis != null) {
            xAxis = axis;
        } else {
            xAxis = xyChart.getXAxis();
        }

        final double xValue = xAxis.getValueForDisplay(mouseLocation.getX());

        for (final DataPointTableViewPointer.DataPoint dataPoint : findNeighborPoints(xyChart, xValue)) {
            if (getChart().getFirstAxis(Orientation.HORIZONTAL) != null) {
                final double x = xAxis.getDisplayPosition(dataPoint.x);
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
        for (int i = 0; i < nDataCount; i++) {
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
        try {
            Bounds areaBounds = null;
            if (plotArea)
                areaBounds = getChart().getPlotArea().getBoundsInLocal();
            else if (getChart() != null) {
                areaBounds = getChart().getBoundsInLocal();
            }

            if (areaBounds == null || !areaBounds.contains(event.getX(), event.getY())) {
                return;
            }

            final Point2D mouseLocation = getLocationInPlotArea(event);

            if (mouseLocation != null && currentChart.getChartType() != ChartType.BUBBLE) {

                updateTable(mouseLocation);

                if (!notActiveCharts.isEmpty()) {
                    for (org.jevis.jeconfig.application.Chart.Charts.Chart chart : notActiveCharts) {
                        if (chart.getChart() != null && !chart.getChartType().equals(ChartType.PIE)
                                && !chart.getChartType().equals(ChartType.BAR)
                                && !chart.getChartType().equals(ChartType.TABLE)) {
                            chart.getChart().getPlugins().forEach(chartPlugin -> {
                                if (chartPlugin instanceof DataPointTableViewPointer) {
                                    ((DataPointTableViewPointer) chartPlugin).updateTable(mouseLocation);
                                }
                            });
                        } else if (chart.getChartType().equals(ChartType.TABLE)) {
                            final DataPoint dataPoint = findNearestDataPointWithinPickingDistance(getChart(), mouseLocation, null);
                            if (dataPoint == null) continue;

                            Double v = dataPoint.getX() * 1000d;
                            DateTime nearest = new DateTime(v.longValue());

                            TableChart tableChart = (TableChart) chart;
                            tableChart.updateTable(null, nearest);
                            tableChart.setBlockDatePickerEvent(true);
                            TableTopDatePicker tableTopDatePicker = tableChart.getTableTopDatePicker();
                            JFXComboBox<DateTime> datePicker = tableTopDatePicker.getDatePicker();
                            Platform.runLater(() -> {
                                datePicker.getSelectionModel().select(nearest);
                                tableChart.setBlockDatePickerEvent(false);
                            });
                        }
                    }
                }
            } else {
                final DataPoint dataPoint = findNearestDataPointWithinPickingDistance(getChart(), mouseLocation, null);
                if (dataPoint != null) {
                    Double v = dataPoint.getX();
                    updateTable(v);
                }
            }
        } catch (Exception e) {
            logger.error("Error while updating table", e);
        }
    }

    public void updateTable(final Point2D mouseLocation) {

        Period period = this.currentChart.getPeriod();

        for (XYChartSerie xyChartSerie : xyChartSerieList) {
            try {
                final DataPoint dataPoint = findNearestDataPointWithinPickingDistance(getChart(), mouseLocation, xyChartSerie.getXAxis());

                if (dataPoint == null) continue;

                Double v = dataPoint.getX() * 1000d;
                DateTime nearest = new DateTime(v.longValue());

                TableEntry tableEntry = xyChartSerie.getTableEntry();
                TreeMap<DateTime, JEVisSample> sampleTreeMap = xyChartSerie.getSampleMap();
                Map<DateTime, JEVisSample> noteMap = xyChartSerie.getSingleRow().getNoteSamples();
                Map<DateTime, Alarm> alarmMap = xyChartSerie.getSingleRow().getAlarms(false);

                DateTime dateTime = nearest;
                if (currentChart instanceof LogicalChart) {
                    dateTime = sampleTreeMap.lowerKey(nearest);
                }

                JEVisSample sample = sampleTreeMap.get(dateTime);

                Note formattedNote = new Note(sample, noteMap.get(sample.getTimestamp()), alarmMap.get(sample.getTimestamp()));

                if (workDays != null && period != null && workDays.getWorkdayEnd().isBefore(workDays.getWorkdayStart())
                        && (period.getDays() > 0
                        || period.getWeeks() > 0
                        || period.getMonths() > 0
                        || period.getYears() > 0)) {
                    dateTime = dateTime.plusDays(1);
                }

                DateTime finalDateTime = dateTime;

                if (!asDuration) {
                    Platform.runLater(() -> tableEntry.setDate(finalDateTime
                            .toString(xyChartSerie.getSingleRow().getFormatString())));
                } else {
                    Platform.runLater(() -> tableEntry.setDate((finalDateTime.getMillis() -
                            timestampOfFirstSample.getMillis()) / 1000 / 60 / 60 + " h"));
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

                        formattedDouble = xyChartSerie.getNf().format(valueAsDouble);
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
        }
    }

    public void updateTable(Double nearest) {

        try {
            BubbleChart currentChart = (BubbleChart) this.currentChart;

            TreeMap<Double, Double> sampleTreeMap = currentChart.getSampleTreeMap();
            TableEntry tableEntry = currentChart.getTableData().get(0);

            Double yValue = sampleTreeMap.get(nearest);
            AtomicReference<Double> xValue = new AtomicReference<>(nearest);
            sampleTreeMap.forEach((aDouble, aDouble2) -> {
                if (aDouble2.equals(yValue)) {
                    xValue.set(aDouble);
                }
            });

            String formattedX = currentChart.getNf().format(xValue.get());
            String formattedY = currentChart.getNf().format(yValue);
            String xUnit = currentChart.getxUnit();
            String yUnit = currentChart.getyUnit();
            if (!xUnit.equals("")) {
                Platform.runLater(() -> tableEntry.setxValue(formattedX + " " + xUnit));
            } else {
                Platform.runLater(() -> tableEntry.setxValue(formattedX));
            }
            if (!yUnit.equals("")) {
                Platform.runLater(() -> tableEntry.setyValue(formattedY + " " + yUnit));
            } else {
                Platform.runLater(() -> tableEntry.setyValue(formattedY));
            }

        } catch (Exception ex) {
        }
    }

    protected static class DataPoint {

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
