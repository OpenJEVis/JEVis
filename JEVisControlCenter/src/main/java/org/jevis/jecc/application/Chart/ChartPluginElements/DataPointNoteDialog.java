/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.jevis.jecc.application.Chart.ChartPluginElements;

import de.gsi.chart.Chart;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.plugins.AbstractDataFormattingPlugin;
import de.gsi.dataset.DataSet;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.chart.BubbleType;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jecc.Constants;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.application.Chart.ChartElements.Bubble;
import org.jevis.jecc.application.Chart.ChartElements.XYChartSerie;
import org.jevis.jecc.application.Chart.Charts.BubbleChart;
import org.jevis.jecc.application.Chart.data.ChartDataRow;
import org.jevis.jecc.application.Chart.data.RowNote;
import org.jevis.jecc.application.application.I18nWS;
import org.jevis.jecc.dialog.NoteDialog;
import org.jevis.jecc.plugin.charts.ChartPlugin;
import org.jevis.jecc.plugin.notes.NoteTag;
import org.jevis.jecc.sample.tableview.SampleTable;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.jevis.commons.constants.NoteConstants.User.USER_VALUE;

public class DataPointNoteDialog extends AbstractDataFormattingPlugin {

    private static final Logger logger = LogManager.getLogger(DataPointNoteDialog.class);
    private final DoubleValidator validator = DoubleValidator.getInstance();
    private final java.text.NumberFormat nf;
    private List<XYChartSerie> xyChartSerieList;
    private org.jevis.jecc.application.Chart.Charts.Chart chart;
    private ChartPlugin chartPlugin;
    private final EventHandler<MouseEvent> noteHandler = event -> {
        if (event.getButton() == MouseButton.SECONDARY) {
            updateTable(event);
            event.consume();
        }
    };

    public DataPointNoteDialog(JEVisAttribute att, SampleTable table) {
        this.nf = java.text.NumberFormat.getInstance(I18n.getInstance().getLocale());
        Dialog<ButtonType> dialog = new Dialog<>();

        final ButtonType ok = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(ok, cancel);

        Label timeStampLabel = new Label(I18n.getInstance().getString("alarms.table.captions.timestamp"));

        Label nameValue = new Label();
        try {
            nameValue.setText(I18nWS.getInstance().getAttributeName(att));
        } catch (JEVisException e) {
            logger.error("Could not get translated attribute name of attribute {} of object {}:{}", att.getName(), att.getObject().getName(), att.getObject().getID(), e);
            nameValue.setText(att.getName());
        }
        MFXTextField value = new MFXTextField();
        MFXTextField unit = new MFXTextField();

        HBox valueBox = new HBox(4, nameValue, value, unit);
        valueBox.setAlignment(Pos.CENTER);
        HBox timeStampBox = new HBox(4, timeStampLabel);
        timeStampBox.setAlignment(Pos.CENTER);
        VBox vBox = new VBox(4, timeStampBox, valueBox);
        vBox.setAlignment(Pos.CENTER);

        dialog.getDialogPane().setContent(vBox);

        getUnitName(att, unit);

        unit.setDisable(true);

        value.textProperty().addListener((observable, oldValue, newValue) -> {
            DoubleValidator validator = DoubleValidator.getInstance();
            try {
                double parsedValue = validator.validate(newValue, I18n.getInstance().getLocale());
            } catch (Exception e) {
                value.setText(oldValue);
            }
        });

        table.getItems().forEach(tableSample -> {
            if (tableSample.isSelected()) {
                try {
                    JEVisSample jevisSample = tableSample.getJevisSample();
                    timeStampLabel.setText(I18n.getInstance().getString("alarms.table.captions.timestamp") + ": "
                            + jevisSample.getTimestamp().toString("yyyy-MM-dd HH:mm:ss"));

                    Double userValueForTimeStampAsDouble = getUserValueForTimeStampAsDouble(jevisSample, jevisSample.getTimestamp());

                    if (userValueForTimeStampAsDouble != null) {
                        String userValueForTimeStamp = nf.format(userValueForTimeStampAsDouble);
                        value.setText(userValueForTimeStamp);
                    }

                } catch (Exception ex) {
                    logger.error("Error while setting user value", ex);
                }

                dialog.showAndWait().ifPresent(response -> {
                    if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                        Task task = new Task() {
                            @Override
                            protected Object call() throws Exception {
                                try {
                                    if (value.getText().length() > 0) {
                                        saveUserEntry(att, tableSample.getJevisSample(), validator.validate(value.getText(), I18n.getInstance().getLocale()));
                                    } else {
                                        saveUserEntry(att, tableSample.getJevisSample(), null);
                                    }
                                } catch (Exception ex) {
                                    logger.error("Error while creating user value samples", ex);
                                }
                                return null;
                            }
                        };

                        ControlCenter.getStatusBar().addTask(this.getClass().getName(), task, null, true);
                    }
                });
            }
        });
    }

    public DataPointNoteDialog(JEVisAttribute att, DateTime[] minMax) {
        this.nf = java.text.NumberFormat.getInstance(I18n.getInstance().getLocale());
        Dialog<ButtonType> dialog = new Dialog<>();

        final ButtonType ok = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(ok, cancel);

        Label timeStampLabel = new Label(I18n.getInstance().getString("alarms.table.captions.timestamp") + ": " +
                minMax[0].toString("yyyy-MM-dd HH:mm:ss") + " - " + minMax[1].toString("yyyy-MM-dd HH:mm:ss"));

        MFXTextField value = new MFXTextField();
        MFXTextField unit = new MFXTextField();

        Label nameValue = new Label();
        try {
            nameValue.setText(I18nWS.getInstance().getAttributeName(att));
        } catch (JEVisException e) {
            logger.error("Could not get translated attribute name of attribute {} of object {}:{}", att.getName(), att.getObject().getName(), att.getObject().getID(), e);
            nameValue.setText(att.getName());
        }

        HBox valueBox = new HBox(4, nameValue, value, unit);
        valueBox.setAlignment(Pos.CENTER);
        HBox timeStampBox = new HBox(4, timeStampLabel);
        timeStampBox.setAlignment(Pos.CENTER);
        VBox vBox = new VBox(4, timeStampBox, valueBox);
        vBox.setAlignment(Pos.CENTER);

        dialog.getDialogPane().setContent(vBox);

        getUnitName(att, unit);

        unit.setDisable(true);

        value.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double parsedValue = validator.validate(newValue, I18n.getInstance().getLocale());
            } catch (Exception e) {
                value.setText(oldValue);
            }
        });

        dialog.showAndWait().ifPresent(response -> {
            if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {

                Task task = new Task() {
                    @Override
                    protected Object call() throws Exception {
                        att.getSamples(minMax[0], minMax[1]).forEach(jeVisSample -> {
                            try {
                                if (value.getText().length() > 0) {
                                    saveUserEntry(att, jeVisSample, validator.validate(value.getText(), I18n.getInstance().getLocale()));
                                } else {
                                    saveUserEntry(att, jeVisSample, null);
                                }
                            } catch (Exception ex) {
                                logger.error("Error while creating user value samples", ex);
                            }
                        });
                        return null;
                    }
                };

                ControlCenter.getStatusBar().addTask(this.getClass().getName(), task, null, true);
            }
        });
    }

    public DataPointNoteDialog(List<XYChartSerie> xyChartSerieList, ChartPlugin chartPlugin) {
        this(xyChartSerieList, chartPlugin, null);
    }

    public DataPointNoteDialog(List<XYChartSerie> xyChartSerieList, ChartPlugin chartPlugin, org.jevis.jecc.application.Chart.Charts.Chart chart) {
        this.xyChartSerieList = xyChartSerieList;
        this.chartPlugin = chartPlugin;
        this.chart = chart;
        this.nf = java.text.NumberFormat.getInstance(I18n.getInstance().getLocale());
        this.nf.setMinimumFractionDigits(chart.getChartModel().getMinFractionDigits());
        this.nf.setMaximumFractionDigits(chart.getChartModel().getMaxFractionDigits());

        registerInputEventHandler(MouseEvent.MOUSE_CLICKED, noteHandler);
    }

    private void getUnitName(JEVisAttribute att, MFXTextField unit) {
        try {
            if (att.getObject().getJEVisClassName().equals("Clean Data")) {
                if (att.getDisplayUnit() != null && !att.getInputUnit().getLabel().isEmpty()) {
                    unit.setText(UnitManager.getInstance().format(att.getDisplayUnit().getLabel()));
                } else {
                    unit.setText(UnitManager.getInstance().format(att.getInputUnit().getLabel()));
                }
            } else {
                List<JEVisObject> children = att.getObject().getChildren();
                if (children.size() == 1) {
                    JEVisObject cleanObject = children.get(0);
                    JEVisAttribute valueAttribute = cleanObject.getAttribute("Value");
                    if (valueAttribute != null && valueAttribute.getDisplayUnit() != null && !valueAttribute.getInputUnit().getLabel().isEmpty()) {
                        unit.setText(UnitManager.getInstance().format(valueAttribute.getDisplayUnit().getLabel()));
                    } else if (valueAttribute != null) {
                        unit.setText(UnitManager.getInstance().format(valueAttribute.getInputUnit().getLabel()));
                    }
                }
            }
        } catch (JEVisException e) {
            logger.error("Could not get unit from attribute {} of object {}:{}", att.getName(), att.getObject().getName(), att.getObject().getID(), e);
        }
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

        final int nDataCount = dataSet.getDataCount();
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
        final Bounds plotAreaBounds = getChart().getPlotArea().getBoundsInLocal();
        final DataPoint dataPoint = findDataPoint(event, plotAreaBounds);

        Map<String, RowNote> map = new HashMap<>();

        if (dataPoint != null) {
            boolean isBubbleChart = chart instanceof BubbleChart;

            double x = dataPoint.getX();
            if (!isBubbleChart) {
                Double v = x * 1000d;
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

                            RowNote rowNote = new RowNote(dataObject, nearestSample, serie.getSingleRow().getNoteSamples().get(nearestSample.getTimestamp()), title, userNote, userValue, serie.getUnit(), serie.getSingleRow().getScaleFactor(), serie.getSingleRow().getAlarms(false).get(nearestSample.getTimestamp()));

                            map.put(title, rowNote);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            } else {
                BubbleChart bubbleChart = (BubbleChart) chart;
                List<Bubble> bubbles = bubbleChart.getBubbles();
                ChartDataRow chartDataRow = bubbleChart.getChartDataRows().stream().filter(dataRow -> dataRow.getBubbleType() == BubbleType.X).findFirst().orElse(null);

                for (Bubble bubble : bubbles) {
                    if (bubble.getX() == x) {
                        for (JEVisSample xSample : bubble.getXSamples()) {
                            try {

                                JEVisSample ySample = null;
                                for (JEVisSample sample : bubble.getYSamples()) {
                                    if (sample.getTimestamp().equals(xSample.getTimestamp())) {
                                        ySample = sample;
                                        break;
                                    }
                                }

                                String title = xSample.getTimestamp().toString("yyyy-MM-dd HH:mm:ss");
                                RowNote rowNote = new RowNote(title, xSample, ySample);

                                map.put(title, rowNote);
                            } catch (Exception e) {
                                logger.error("Could not create note row for xSample {}", xSample);
                            }
                        }
                    }
                }
            }

            NoteDialog nd = new NoteDialog(nf, map);

            nd.showAndWait().ifPresent(response -> {
                if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                    if (saveUserEntries(nd.getNoteMap())) {
                        Dialog<ButtonType> wantToReload = new Dialog<>();
                        wantToReload.setTitle(I18n.getInstance().getString("plugin.graph.dialog.reload.title"));
                        final ButtonType ok = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.reload.ok"), ButtonBar.ButtonData.YES);
                        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.reload.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

                        wantToReload.setContentText(I18n.getInstance().getString("plugin.graph.dialog.reload.message"));
                        wantToReload.getDialogPane().getButtonTypes().addAll(ok, cancel);
                        Platform.runLater(() -> wantToReload.showAndWait().ifPresent(response2 -> {
                            if (response2.getButtonData().getTypeCode().equals(ButtonType.YES.getButtonData().getTypeCode())) {
                                chartPlugin.handleRequest(Constants.Plugin.Command.RELOAD);
                            }
                        }));
                    }
                }
            });
        }
    }

    private String getUserValueForTimeStamp(JEVisSample nearestSample, DateTime timeStamp) {
        String userValue = "";

        Double userValueForTimeStampAsDouble = getUserValueForTimeStampAsDouble(nearestSample, timeStamp);

        if (userValueForTimeStampAsDouble != null) {
            return userValueForTimeStampAsDouble.toString();
        }

        return userValue;
    }

    private Double getUserValueForTimeStampAsDouble(JEVisSample nearestSample, DateTime timeStamp) {
        Double userValue = null;

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
                                return smp.getValueAsDouble();
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

    private boolean saveUserEntries(Map<String, RowNote> noteMap) {
        boolean savedValues = false;
        for (Map.Entry<String, RowNote> entry : noteMap.entrySet()) {
            if (entry.getValue().getChanged()) {
                savedValues = true;
                try {
                    JEVisObject obj = entry.getValue().getDataObject();
                    JEVisUser currentUser = obj.getDataSource().getCurrentUser();
                    //if (obj.getDataSource().getCurrentUser().canWrite(obj.getID())) {

                    JEVisSample sample = entry.getValue().getYSample();
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
                            if (currentUser.canCreate(parent.getID(), dataNoteClass.getName())) {
                                correspondingNoteObject = parent.buildObject(obj.getName() + " Notes", dataNoteClass);
                                correspondingNoteObject.commit();
                            } else {
                                warningNotAllowed();
                            }
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

                            /* Login info for note**/
                            try {
                                JEVisAttribute userAttribute = correspondingNoteObject.getAttribute("User");
                                List<JEVisSample> sampleList = userAttribute.getSamples(timeStamp, timeStamp);

                                String userName = userAttribute.getDataSource().getCurrentUser().getAccountName();

                                if (sampleList.isEmpty()) {
                                    JEVisSample uSample = userAttribute.buildSample(timeStamp, userName);
                                    uSample.commit();
                                } else {
                                    sampleList.get(0).setValue(userName);
                                    sampleList.get(0).commit();
                                }

                            } catch (Exception ex) {

                            }

                            /* Tag for note */
                            try {
                                JEVisAttribute userAttribute = correspondingNoteObject.getAttribute("Tag");
                                List<JEVisSample> sampleList = userAttribute.getSamples(timeStamp, timeStamp);

                                /* Todo: let the user configure it using the TagTableCellField*/
                                String tags = NoteTag.TAG_EVENT.getId();

                                if (sampleList.isEmpty()) {
                                    JEVisSample uSample = userAttribute.buildSample(timeStamp, tags);
                                    uSample.commit();
                                } else {
                                    sampleList.get(0).setValue(tags);
                                    sampleList.get(0).commit();
                                }

                            } catch (Exception ex) {

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
                            if (currentUser.canCreate(parent.getID(), userDataClass.getName())) {
                                correspondingUserDataObject = parent.buildObject(obj.getName() + " User Data", userDataClass);
                                correspondingUserDataObject.commit();
                            } else {
                                warningNotAllowed();
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
                } catch (Exception e) {
                    warningNotAllowed();
                }
            }
        }
        return savedValues;
    }

    private void warningNotAllowed() {
        Platform.runLater(() -> {
            Alert alert1 = new Alert(Alert.AlertType.WARNING, I18n.getInstance().getString("dialog.warning.title"));
            alert1.setContentText(I18n.getInstance().getString("dialog.warning.notallowed"));
            alert1.showAndWait();
        });
    }

    private void saveUserEntry(JEVisAttribute att, JEVisSample sample, Double value) {

        try {
            JEVisObject obj = att.getObject();
            DateTime timeStamp = sample.getTimestamp();
            JEVisSample correspondingCleanDataSample = null;
            JEVisUser currentUser = obj.getDataSource().getCurrentUser();

            List<JEVisObject> listParents = obj.getParents();

            JEVisObject correspondingUserDataObject = null;
            final JEVisClass userDataClass = obj.getDataSource().getJEVisClass("User Data");
            boolean foundUserDataObject = false;

            if (obj.getJEVisClassName().equals("Clean Data")) {
                for (JEVisObject parent : listParents) {
                    for (JEVisObject child : parent.getChildren()) {
                        if (child.getJEVisClass().equals(userDataClass)) {
                            correspondingUserDataObject = child;
                            foundUserDataObject = true;
                            break;
                        }
                    }
                }
            } else if (obj.getJEVisClassName().equals("Data")) {
                for (JEVisObject child : obj.getChildren()) {
                    if (child.getJEVisClass().equals(userDataClass)) {
                        correspondingUserDataObject = child;
                        foundUserDataObject = true;
                        break;
                    }
                }
            }

            if (!foundUserDataObject && obj.getJEVisClassName().equals("Clean Data")) {
                for (JEVisObject parent : listParents) {
                    if (currentUser.canCreate(parent.getID(), userDataClass.getName())) {
                        correspondingUserDataObject = parent.buildObject(obj.getName() + " User Data", userDataClass);
                        correspondingUserDataObject.commit();
                    } else warningNotAllowed();

                }
            } else if (!foundUserDataObject && obj.getJEVisClassName().equals("Data")) {
                if (currentUser.canCreate(obj.getID(), userDataClass.getName())) {
                    JEVisClass cleanDataClass = obj.getDataSource().getJEVisClass("Clean Data");
                    List<JEVisObject> cleanObjects = obj.getChildren(cleanDataClass, true);
                    if (cleanObjects.size() == 1) {
                        correspondingUserDataObject = obj.buildObject(cleanObjects.get(0).getName() + " User Data", userDataClass);
                        correspondingUserDataObject.commit();
                    }
                } else warningNotAllowed();
            }

            JEVisClass cleanDataClass = obj.getDataSource().getJEVisClass("Clean Data");

            if (obj.getJEVisClassName().equals("Data")) {
                List<JEVisObject> cleanObjects = obj.getChildren(cleanDataClass, true);
                if (cleanObjects.size() == 1) {
                    List<JEVisSample> samples = cleanObjects.get(0).getAttribute("Value").getSamples(timeStamp, timeStamp);
                    if (samples.size() == 1) {
                        correspondingCleanDataSample = samples.get(0);
                    }
                }
            } else if (obj.getJEVisClassName().equals("Clean Data")) {
                List<JEVisSample> samples = obj.getAttribute("Value").getSamples(timeStamp, timeStamp);
                if (samples.size() == 1) {
                    correspondingCleanDataSample = samples.get(0);
                }
            }

            if (correspondingUserDataObject != null && currentUser.canWrite(correspondingUserDataObject.getID())) {
                try {
                    JEVisAttribute userDataValueAttribute = correspondingUserDataObject.getAttribute("Value");

                    List<JEVisSample> listSamples;
                    if (userDataValueAttribute.hasSample()) {
                        listSamples = userDataValueAttribute.getSamples(timeStamp, timeStamp);

                        if (value != null && listSamples.size() == 1) {
                            listSamples.get(0).setValue(value);
                            listSamples.get(0).commit();
                        } else if (value != null) {
                            JEVisSample newSample = userDataValueAttribute.buildSample(timeStamp.toDateTimeISO(), value, sample.getNote());
                            newSample.commit();
                        } else {
                            userDataValueAttribute.deleteSamplesBetween(timeStamp.toDateTimeISO(), timeStamp.toDateTimeISO());
                        }
                    } else {
                        if (value != null) {
                            JEVisSample newSample = userDataValueAttribute.buildSample(timeStamp.toDateTimeISO(), value, sample.getNote());
                            newSample.commit();
                        }
                    }

                    if (value != null && correspondingCleanDataSample != null && !correspondingCleanDataSample.getNote().contains(USER_VALUE)) {

                        String note = correspondingCleanDataSample.getNote();
                        note += "," + USER_VALUE;
                        correspondingCleanDataSample.setNote(note);
                        correspondingCleanDataSample.commit();
                    } else if (value == null && correspondingCleanDataSample != null && correspondingCleanDataSample.getNote().contains(USER_VALUE)) {

                        String note = correspondingCleanDataSample.getNote();
                        note = note.replace("," + USER_VALUE, "");
                        correspondingCleanDataSample.setNote(note);
                        correspondingCleanDataSample.commit();
                    }

                } catch (JEVisException ignored) {

                }
            }

        } catch (Exception e) {
            warningNotAllowed();
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
