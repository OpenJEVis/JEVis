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
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.application.Chart.ChartElements.XYChartSerie;
import org.jevis.jeconfig.application.Chart.data.RowNote;
import org.jevis.jeconfig.dialog.NoteDialog;
import org.jevis.jeconfig.plugin.charts.GraphPluginView;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.jevis.commons.constants.NoteConstants.User.USER_VALUE;

public class DataPointNoteDialog extends AbstractDataFormattingPlugin {

    private final List<XYChartSerie> xyChartSerieList;
    private final GraphPluginView graphPluginView;
    private final EventHandler<MouseEvent> mouseMoveHandler = this::updateTable;

    public DataPointNoteDialog(List<XYChartSerie> xyChartSerieList, GraphPluginView graphPluginView) {
        this.xyChartSerieList = xyChartSerieList;
        this.graphPluginView = graphPluginView;

        registerInputEventHandler(MouseEvent.MOUSE_CLICKED, mouseMoveHandler);
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

        for (final DataPointNoteDialog.DataPoint dataPoint : findNeighborPoints(xyChart, xValue)) {
            // Point2D displayPoint = toDisplayPoint(chart.getYAxis(),
            // (X)dataPoint.x , dataPoint.y);
            if (getChart().getFirstAxis(Orientation.HORIZONTAL) instanceof Axis) {
                final double x = xyChart.getXAxis().getDisplayPosition(dataPoint.x);
                final double y = xyChart.getYAxis().getDisplayPosition(dataPoint.y);
                final Point2D displayPoint = new Point2D(x, y);
                dataPoint.distanceFromMouse = displayPoint.distance(mouseLocation);
                if (displayPoint.distance(mouseLocation) <= 1000 && (nearestDataPoint == null
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

    private String formatDataPoint(final DataPoint dataPoint) {
        return String.format("DataPoint@(%.3f,%.3f)", dataPoint.x, dataPoint.y);
        // return formatData(dataPoint.chart.getYAxis(), dataPoint.x,
        // dataPoint.y);
    }

    protected String formatLabel(DataPoint dataPoint) {
        return String.format("'%s'\n%s", dataPoint.label, formatDataPoint(dataPoint));
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
        if (event.isControlDown()) {

            final Bounds plotAreaBounds = getChart().getPlotArea().getBoundsInLocal();
            final DataPoint dataPoint = findDataPoint(event, plotAreaBounds);

            Map<String, RowNote> map = new HashMap<>();

            if (dataPoint != null) {
                Double v = dataPoint.getX() * 1000d;
                DateTime nearest = new DateTime(v.longValue());

                for (XYChartSerie serie : xyChartSerieList) {
                    if (serie.getSingleRow().getManipulationMode().equals(ManipulationMode.NONE)) {
                        try {

                            JEVisSample nearestSample = serie.getSampleMap().get(nearest);

                            String title = "";
                            title += serie.getSingleRow().getObject().getName();

                            JEVisObject dataObject;
                            if (serie.getSingleRow().getDataProcessor() != null)
                                dataObject = serie.getSingleRow().getDataProcessor();
                            else dataObject = serie.getSingleRow().getObject();

                            String userNote = getUserNoteForTimeStamp(nearestSample, nearestSample.getTimestamp());
                            String userValue = getUserValueForTimeStamp(nearestSample, nearestSample.getTimestamp());

                            RowNote rowNote = new RowNote(dataObject, nearestSample, serie.getSingleRow().getNoteSamples().get(nearestSample.getTimestamp()), title, userNote, userValue, serie.getUnit(), serie.getSingleRow().getScaleFactor());

                            map.put(title, rowNote);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                NoteDialog nd = new NoteDialog(map);

                nd.showAndWait().ifPresent(response -> {
                    if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                        saveUserEntries(nd.getNoteMap());

                        Dialog<ButtonType> wantToReload = new Dialog<>();
                        wantToReload.setTitle(I18n.getInstance().getString("plugin.graph.dialog.reload.title"));
                        final ButtonType ok = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.reload.ok"), ButtonBar.ButtonData.YES);
                        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.reload.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

                        wantToReload.setContentText(I18n.getInstance().getString("plugin.graph.dialog.reload.message"));
                        wantToReload.getDialogPane().getButtonTypes().addAll(ok, cancel);
                        Platform.runLater(() -> wantToReload.showAndWait().ifPresent(response2 -> {
                            if (response2.getButtonData().getTypeCode().equals(ButtonType.YES.getButtonData().getTypeCode())) {
                                graphPluginView.handleRequest(Constants.Plugin.Command.RELOAD);
                            }
                        }));
                    }
                });
            }
        }
    }

    private String getUserValueForTimeStamp(JEVisSample nearestSample, DateTime timeStamp) {
        String userValue = "";

        try {
            if (nearestSample.getAttribute() != null) {
                JEVisObject obj = nearestSample.getAttribute().getObject();
                JEVisObject correspondingUserDataObject = null;

                final JEVisClass userDataClass = obj.getDataSource().getJEVisClass("User Data");
                List<JEVisObject> listParents = obj.getParents();
                for (JEVisObject parent : listParents) {
                    for (JEVisObject child : parent.getChildren()) {
                        if (child.getJEVisClass().equals(userDataClass) && child.getName().contains(obj.getName())) {
                            correspondingUserDataObject = child;
                            break;
                        }
                    }
                }
                if (correspondingUserDataObject != null) {
                    try {
                        JEVisAttribute userDataAttribute = correspondingUserDataObject.getAttribute("Value");
                        List<JEVisSample> listSamples = userDataAttribute.getSamples(timeStamp, timeStamp);
                        if (listSamples.size() == 1) {
                            for (JEVisSample smp : listSamples) {
                                return smp.getValueAsString();
                            }
                        }
                    } catch (JEVisException ignored) {

                    }
                }
            }
        } catch (JEVisException ignored) {

        }
        return userValue;
    }

    private String getUserNoteForTimeStamp(JEVisSample nearestSample, DateTime timeStamp) {
        String userNote = "";

        try {
            if (nearestSample.getAttribute() != null) {
                JEVisObject obj = nearestSample.getAttribute().getObject();
                JEVisObject correspondingNoteObject = null;

                final JEVisClass dataNoteClass = obj.getDataSource().getJEVisClass("Data Notes");
                List<JEVisObject> listParents = obj.getParents();
                for (JEVisObject parent : listParents) {
                    for (JEVisObject child : parent.getChildren()) {
                        if (child.getJEVisClass().equals(dataNoteClass) && child.getName().contains(obj.getName())) {
                            correspondingNoteObject = child;
                            break;
                        }
                    }
                }
                if (correspondingNoteObject != null) {
                    try {
                        JEVisAttribute userNoteAttribute = correspondingNoteObject.getAttribute("User Notes");
                        List<JEVisSample> listSamples = userNoteAttribute.getSamples(timeStamp, timeStamp);
                        if (listSamples.size() == 1) {
                            for (JEVisSample smp : listSamples) {
                                return smp.getValueAsString();
                            }
                        }
                    } catch (JEVisException ignored) {

                    }
                }
            }
        } catch (JEVisException ignored) {

        }
        return userNote;
    }

    private void saveUserEntries(Map<String, RowNote> noteMap) {
        for (Map.Entry<String, RowNote> entry : noteMap.entrySet()) {
            if (entry.getValue().getChanged()) {
                try {
                    JEVisObject obj = entry.getValue().getDataObject();
                    JEVisUser currentUser = obj.getDataSource().getCurrentUser();
                    //if (obj.getDataSource().getCurrentUser().canWrite(obj.getID())) {

                    JEVisSample sample = entry.getValue().getSample();
                    DateTime timeStamp = sample.getTimestamp();
                    String newUserNote = entry.getValue().getUserNote();
                    String newUserValue = entry.getValue().getUserValue();
                    List<JEVisObject> listParents = obj.getParents();

                    JEVisObject correspondingNoteObject = null;
                    final JEVisClass dataNoteClass = obj.getDataSource().getJEVisClass("Data Notes");
                    boolean foundNoteObject = false;
                    for (JEVisObject parent : listParents) {
                        for (JEVisObject child : parent.getChildren()) {
                            if (child.getJEVisClass().equals(dataNoteClass)) {
                                correspondingNoteObject = child;
                                foundNoteObject = true;
                                break;
                            }
                        }
                    }

                    if (!foundNoteObject) {
                        for (JEVisObject parent : listParents) {
                            correspondingNoteObject = parent.buildObject(obj.getName() + " Notes", dataNoteClass);
                            correspondingNoteObject.commit();
                        }
                    }

                    if (correspondingNoteObject != null) {
                        try {
                            JEVisAttribute userNoteAttribute = correspondingNoteObject.getAttribute("User Notes");

                            List<JEVisSample> listSamples;
                            if (userNoteAttribute.hasSample()) {
                                listSamples = userNoteAttribute.getSamples(timeStamp, timeStamp);

                                if (!newUserNote.equals("") && listSamples.size() == 1) {
                                    listSamples.get(0).setValue(newUserNote);
                                    listSamples.get(0).commit();
                                } else if (!newUserNote.equals("")) {
                                    JEVisSample newSample = userNoteAttribute.buildSample(timeStamp.toDateTimeISO(), newUserNote);
                                    newSample.commit();
                                } else {
                                    userNoteAttribute.deleteSamplesBetween(timeStamp.toDateTimeISO(), timeStamp.toDateTimeISO());
                                }
                            } else {
                                if (!newUserNote.equals("")) {
                                    JEVisSample newSample = userNoteAttribute.buildSample(timeStamp.toDateTimeISO(), newUserNote);
                                    newSample.commit();
                                }

                            }

                            if (!newUserNote.equals("") && !sample.getNote().contains("userNotes")) {
                                List<JEVisSample> unmodifiedSamples = obj
                                        .getDataSource()
                                        .getObject(obj.getID())
                                        .getAttribute("Value")
                                        .getSamples(timeStamp, timeStamp);

                                if (unmodifiedSamples.size() == 1 && currentUser.canWrite(obj.getID())) {
                                    JEVisSample unmodifiedSample = unmodifiedSamples.get(0);
                                    String note = unmodifiedSample.getNote();
                                    note += ",userNotes";
                                    unmodifiedSample.setNote(note);
                                    unmodifiedSample.commit();
                                }
                            } else if (newUserNote.equals("") && sample.getNote().contains("userNotes") && currentUser.canWrite(obj.getID())) {
                                List<JEVisSample> unmodifiedSamples = obj
                                        .getDataSource()
                                        .getObject(obj.getID())
                                        .getAttribute("Value")
                                        .getSamples(timeStamp, timeStamp);

                                if (unmodifiedSamples.size() == 1) {
                                    JEVisSample unmodifiedSample = unmodifiedSamples.get(0);
                                    String note = unmodifiedSample.getNote();
                                    note = note.replace(",userNotes", "");
                                    unmodifiedSample.setNote(note);
                                    unmodifiedSample.commit();
                                }
                            }

                        } catch (JEVisException ignored) {

                        }
                    }

                    JEVisObject correspondingUserDataObject = null;
                    final JEVisClass userDataClass = obj.getDataSource().getJEVisClass("User Data");
                    boolean foundUserDataObject = false;
                    for (JEVisObject parent : listParents) {
                        for (JEVisObject child : parent.getChildren()) {
                            if (child.getJEVisClass().equals(userDataClass)) {
                                correspondingUserDataObject = child;
                                foundUserDataObject = true;
                                break;
                            }
                        }
                    }

                    if (!foundUserDataObject) {
                        for (JEVisObject parent : listParents) {
                            if (currentUser.canCreate(parent.getID())) {
                                correspondingUserDataObject = parent.buildObject(obj.getName() + " User Data", userDataClass);
                                correspondingUserDataObject.commit();
                            }

                        }
                    }

                    if (correspondingUserDataObject != null && currentUser.canWrite(correspondingUserDataObject.getID())) {
                        try {
                            JEVisAttribute userDataValueAttribute = correspondingUserDataObject.getAttribute("Value");

                            List<JEVisSample> listSamples;
                            if (userDataValueAttribute.hasSample()) {
                                listSamples = userDataValueAttribute.getSamples(timeStamp, timeStamp);

                                if (!newUserValue.equals("") && listSamples.size() == 1) {
                                    listSamples.get(0).setValue(Double.parseDouble(newUserValue));
                                    listSamples.get(0).commit();
                                } else if (!newUserValue.equals("")) {
                                    JEVisSample newSample = userDataValueAttribute.buildSample(timeStamp.toDateTimeISO(), Double.parseDouble(newUserValue));
                                    newSample.commit();
                                } else {
                                    userDataValueAttribute.deleteSamplesBetween(timeStamp.toDateTimeISO(), timeStamp.toDateTimeISO());
                                }
                            } else {
                                if (!newUserValue.equals("")) {
                                    JEVisSample newSample = userDataValueAttribute.buildSample(timeStamp.toDateTimeISO(), Double.parseDouble(newUserValue));
                                    newSample.commit();
                                }
                            }

                            if (!newUserValue.equals("") && !sample.getNote().contains(USER_VALUE)) {
                                List<JEVisSample> unmodifiedSamples = obj
                                        .getDataSource()
                                        .getObject(obj.getID())
                                        .getAttribute("Value")
                                        .getSamples(timeStamp, timeStamp);

                                if (unmodifiedSamples.size() == 1) {
                                    JEVisSample unmodifiedSample = unmodifiedSamples.get(0);
                                    String note = unmodifiedSample.getNote();
                                    note += "," + USER_VALUE;
                                    unmodifiedSample.setNote(note);
                                    unmodifiedSample.commit();
                                }
                            } else if (newUserValue.equals("") && sample.getNote().contains(USER_VALUE)) {
                                List<JEVisSample> unmodifiedSamples = obj
                                        .getDataSource()
                                        .getObject(obj.getID())
                                        .getAttribute("Value")
                                        .getSamples(timeStamp, timeStamp);

                                if (unmodifiedSamples.size() == 1) {
                                    JEVisSample unmodifiedSample = unmodifiedSamples.get(0);
                                    String note = unmodifiedSample.getNote();
                                    note = note.replace("," + USER_VALUE, "");
                                    unmodifiedSample.setNote(note);
                                    unmodifiedSample.commit();
                                }
                            }

                        } catch (JEVisException ignored) {

                        }
                    }
                    //} else {

                    //}

                } catch (Exception ignored) {
                    ignored.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert1 = new Alert(Alert.AlertType.WARNING, I18n.getInstance().getString("dialog.warning.title"));
                        alert1.setContentText(I18n.getInstance().getString("dialog.warning.notallowed"));
                        alert1.showAndWait();
                    });
                }
            }
        }
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
