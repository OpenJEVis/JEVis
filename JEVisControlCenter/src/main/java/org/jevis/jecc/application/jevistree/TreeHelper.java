/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEApplication.
 * <p>
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 * <p>
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jecc.application.jevistree;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import jfxtras.scene.control.LocalTimePicker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.CommonClasses;
import org.jevis.commons.CommonObjectTasks;
import org.jevis.commons.classes.JC;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.datasource.ChannelTools;
import org.jevis.commons.export.ExportMaster;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.commons.utils.ObjectHelper;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.application.Chart.ChartPluginElements.Boxes.TimeZoneBox;
import org.jevis.jecc.application.Chart.ChartPluginElements.PresetDateBox;
import org.jevis.jecc.application.Chart.ChartPluginElements.TreeSelectionDialog;
import org.jevis.jecc.application.jevistree.dialog.NewObject;
import org.jevis.jecc.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jecc.application.jevistree.methods.AutoLimitSetting;
import org.jevis.jecc.application.jevistree.methods.CalculationMethods;
import org.jevis.jecc.application.jevistree.methods.CommonMethods;
import org.jevis.jecc.application.jevistree.methods.DataMethods;
import org.jevis.jecc.application.tools.CalculationNameFormatter;
import org.jevis.jecc.dialog.*;
import org.jevis.jecc.plugin.object.attribute.GapFillingEditor;
import org.jevis.jecc.plugin.object.classes.SelectableObject;
import org.jevis.jecc.plugin.object.extension.calculation.FormulaBox;
import org.jevis.jecc.plugin.object.extension.calculation.VariablesBox;
import org.jevis.jecc.plugin.unit.SamplingRateUI;
import org.jevis.jecc.plugin.unit.UnitSelectUI;
import org.jevis.jecc.tool.ToggleSwitchPlus;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * Collection of common JEVisTree operations
 *
 * @author florian.simon@envidatec.com
 */
public class TreeHelper {

    private static final Logger logger = LogManager.getLogger(TreeHelper.class);
    private final static Pattern lastIntPattern = Pattern.compile("[^0-9]+([0-9]+)$");
    private static long lastSearchIndex = 0L;
    private static String lastSearch = "";

    /**
     * TODO: make it like the other function where the object is an parameter
     *
     * @param tree
     */
    public static void EventDelete(JEVisTree tree, boolean deleteforever) {
        logger.debug("EventDelete");
        if (!tree.getSelectionModel().getSelectedItems().isEmpty()) {
            String question = I18n.getInstance().getString("jevistree.dialog.delete.message") + "\n\n";
            ObservableList<TreeItem<JEVisTreeRow>> items = tree.getSelectionModel().getSelectedItems();
            for (TreeItem<JEVisTreeRow> item : items) {
                question += item.getValue().getJEVisObject().getName() + "\n";
            }

            try {


                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle(I18n.getInstance().getString("jevistree.dialog.delete.title"));
                alert.setHeaderText(null);
                alert.setContentText(question);
                TopMenu.applyActiveTheme(alert.getDialogPane().getScene());

                alert.showAndWait().ifPresent(buttonType -> {
                    if (buttonType.equals(ButtonType.OK)) {
                        final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("plugin.object.waitsave"));

                        Task<Void> delete = new Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                try {
                                    List<JEVisObject> tmpObjects = new ArrayList<>();
                                    List<TreeItem<JEVisTreeRow>> deletedObj = new ArrayList<>();
                                    for (TreeItem<JEVisTreeRow> treeItem : items) {
                                        tmpObjects.add(treeItem.getValue().getJEVisObject());
                                    }


                                    for (JEVisObject object : tmpObjects) {
                                        if (object.getDataSource().getCurrentUser().canDelete(object.getID())) {
                                            Long id = object.getID();

                                            for (TreeItem<JEVisTreeRow> treeItem : items) {
                                                if (treeItem.getValue().getJEVisObject().getID().equals(object.getID())) {
                                                    deletedObj.add(treeItem);
                                                }
                                            }

                                            object.getDataSource().deleteObject(id, deleteforever);


                                        } else {
                                            Platform.runLater(() -> {
                                                Alert alert1 = new Alert(AlertType.WARNING, I18n.getInstance().getString("dialog.warning.title"));
                                                alert1.setContentText(I18n.getInstance().getString("dialog.warning.notallowed"));
                                                TopMenu.applyActiveTheme(alert1.getDialogPane().getScene());
                                                alert1.showAndWait();
                                            });

                                        }
                                    }

                                    /*
                                    for (TreeItem<JEVisTreeRow> treeItem : deletedObj) {
                                        try {
                                            if (treeItem.getParent() != null) {
                                                treeItem.getParent().getChildren().remove(treeItem);
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                    */

                                } catch (Exception ex) {
                                    logger.catching(ex);
                                    CommonDialogs.showError(I18n.getInstance().getString("jevistree.dialog.delete.error.title"),
                                            I18n.getInstance().getString("jevistree.dialog.delete.error.message"), null, ex);
                                }
                                return null;
                            }
                        };
                        delete.setOnSucceeded(event -> {
                                    pForm.getDialogStage().close();

                                    // tree.getSelectionModel().clearSelection();
                                    //tree.getSelectionModel().select(1);
                                    //

                                }
                        );

                        delete.setOnCancelled(event -> {
                            logger.error(I18n.getInstance().getString("plugin.object.waitsave.canceled"));
                            pForm.getDialogStage().hide();
                        });

                        delete.setOnFailed(event -> {
                            logger.error(I18n.getInstance().getString("plugin.object.waitsave.failed"));
                            pForm.getDialogStage().hide();


                            //tree.getSelectionModel().focus(1);
                        });

                        pForm.activateProgressBar(delete);
                        pForm.getDialogStage().show();

                        new Thread(delete).start();

                    } else {
                        // ... user chose CANCEL or closed the dialog
                    }
                });


            } catch (Exception e) {
                logger.error("Could not get JEVis data source. ", e);
            }

        }
    }

    public static void EventDeleteAllCleanAndRaw(JEVisTree tree) {
        logger.debug("EventDeleteAllCleanAndRaw");
        try {
            if (!tree.getSelectionModel().getSelectedItems().isEmpty()) {
                String question = I18n.getInstance().getString("jevistree.dialog.delete.message");
                ObservableList<TreeItem<JEVisTreeRow>> items = tree.getSelectionModel().getSelectedItems();
                for (TreeItem<JEVisTreeRow> item : items) {
                    if (items.indexOf(item) > 0 && items.indexOf(item) < items.size() - 1)
                        question += item.getValue().getJEVisObject().getName();
                }
                question += "?";

                if (tree.getJEVisDataSource().getCurrentUser().canWrite(items.get(0).getValue().getJEVisObject().getID())) {

                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle(I18n.getInstance().getString("jevistree.dialog.deleteCleanAndRaw.title"));
                    alert.setHeaderText(null);
                    alert.setContentText(question);
                    TopMenu.applyActiveTheme(alert.getDialogPane().getScene());
                    GridPane gp = new GridPane();
                    gp.setHgap(4);
                    gp.setVgap(6);
                    Label cleanDataLabel = new Label(I18n.getInstance().getString("tree.treehelper.cleandata.name"));
                    ToggleSwitchPlus cleanData = new ToggleSwitchPlus();
                    cleanData.setSelected(true);
                    Label rawDataLabel = new Label(I18n.getInstance().getString("tree.treehelper.rawdata.name"));
                    ToggleSwitchPlus rawData = new ToggleSwitchPlus();
                    rawData.setSelected(false);

                    Label dateLabelFrom = new Label(I18n.getInstance().getString("tree.treehelper.from"));
                    DatePicker datePickerFrom = new DatePicker();
                    LocalTimePicker timePickerFrom = new LocalTimePicker();
//                    timePickerFrom.set24HourView(true);
//                    timePickerFrom.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));
                    AtomicBoolean changedFrom = new AtomicBoolean(false);

                    datePickerFrom.valueProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != oldValue) {
                            changedFrom.set(true);
                        }
                    });

                    timePickerFrom.localTimeProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != oldValue) {
                            changedFrom.set(true);
                        }
                    });

                    Label dateLabelTo = new Label(I18n.getInstance().getString("tree.treehelper.to"));
                    DatePicker datePickerTo = new DatePicker();
                    LocalTimePicker timePickerTo = new LocalTimePicker();
//                    timePickerTo.set24HourView(true);
//                    timePickerTo.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));
                    AtomicBoolean changedTo = new AtomicBoolean(false);

                    datePickerTo.valueProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != oldValue) {
                            changedTo.set(true);
                        }
                    });

                    datePickerTo.valueProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != oldValue) {
                            changedTo.set(true);
                        }
                    });

                    gp.add(rawDataLabel, 0, 0);
                    gp.add(rawData, 1, 0);
                    gp.add(cleanDataLabel, 0, 1);
                    gp.add(cleanData, 1, 1);
                    gp.add(dateLabelFrom, 0, 2);
                    gp.add(datePickerFrom, 1, 2);
                    gp.add(timePickerFrom, 2, 2);
                    gp.add(dateLabelTo, 0, 3);
                    gp.add(datePickerTo, 1, 3);
                    gp.add(timePickerTo, 2, 3);

                    alert.getDialogPane().setContent(gp);

                    alert.showAndWait().ifPresent(buttonType -> {
                        if (buttonType.equals(ButtonType.OK)) {
                            try {

                                final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("jevistree.dialog.deleteCleanAndRaw.title") + "...");

                                Task<Void> reload = new Task<Void>() {
                                    @Override
                                    protected Void call() {
                                        for (TreeItem<JEVisTreeRow> item : items) {
                                            if (!changedFrom.get() && !changedTo.get()) {
                                                DataMethods.deleteAllSamples(pForm, item.getValue().getJEVisObject(),
                                                        rawData.selectedProperty().get(),
                                                        cleanData.selectedProperty().get());
                                            } else if (changedFrom.get() && !changedTo.get()) {
                                                DateTime dateTimeFrom = new DateTime(
                                                        datePickerFrom.valueProperty().get().getYear(),
                                                        datePickerFrom.valueProperty().get().getMonthValue(),
                                                        datePickerFrom.valueProperty().get().getDayOfMonth(),
                                                        timePickerFrom.getLocalTime().getHour(),
                                                        timePickerFrom.getLocalTime().getMinute(),
                                                        timePickerFrom.getLocalTime().getSecond(), DateTimeZone.getDefault());

                                                DataMethods.deleteAllSamples(pForm, item.getValue().getJEVisObject(),
                                                        dateTimeFrom,
                                                        null,
                                                        rawData.selectedProperty().get(),
                                                        cleanData.selectedProperty().get());
                                            } else if (!changedFrom.get() && changedTo.get()) {
                                                DateTime dateTimeTo = new DateTime(
                                                        datePickerTo.valueProperty().get().getYear(),
                                                        datePickerTo.valueProperty().get().getMonthValue(),
                                                        datePickerTo.valueProperty().get().getDayOfMonth(),
                                                        timePickerTo.getLocalTime().getHour(),
                                                        timePickerTo.getLocalTime().getMinute(),
                                                        timePickerTo.getLocalTime().getSecond(), DateTimeZone.getDefault());

                                                DataMethods.deleteAllSamples(pForm, item.getValue().getJEVisObject(),
                                                        null,
                                                        dateTimeTo,
                                                        rawData.selectedProperty().get(),
                                                        cleanData.selectedProperty().get());
                                            } else {
                                                DateTime dateTimeFrom = new DateTime(
                                                        datePickerFrom.valueProperty().get().getYear(),
                                                        datePickerFrom.valueProperty().get().getMonthValue(),
                                                        datePickerFrom.valueProperty().get().getDayOfMonth(),
                                                        timePickerFrom.getLocalTime().getHour(),
                                                        timePickerFrom.getLocalTime().getMinute(),
                                                        timePickerFrom.getLocalTime().getSecond(), DateTimeZone.getDefault());

                                                DateTime dateTimeTo = new DateTime(
                                                        datePickerTo.valueProperty().get().getYear(),
                                                        datePickerTo.valueProperty().get().getMonthValue(),
                                                        datePickerTo.valueProperty().get().getDayOfMonth(),
                                                        timePickerTo.getLocalTime().getHour(),
                                                        timePickerTo.getLocalTime().getMinute(),
                                                        timePickerTo.getLocalTime().getSecond(), DateTimeZone.getDefault());

                                                DataMethods.deleteAllSamples(pForm, item.getValue().getJEVisObject(),
                                                        dateTimeFrom,
                                                        dateTimeTo,
                                                        rawData.selectedProperty().get(),
                                                        cleanData.selectedProperty().get());
                                            }
                                        }

                                        return null;
                                    }
                                };
                                reload.setOnSucceeded(event -> pForm.getDialogStage().close());

                                reload.setOnCancelled(event -> {
                                    logger.debug("Delete all samples Cancelled");
                                    pForm.getDialogStage().hide();
                                });

                                reload.setOnFailed(event -> {
                                    logger.debug("Delete all samples failed");
                                    pForm.getDialogStage().hide();
                                });

                                pForm.activateProgressBar(reload);
                                pForm.getDialogStage().show();

                                new Thread(reload).start();

                            } catch (Exception ex) {
                                logger.catching(ex);
                                CommonDialogs.showError(I18n.getInstance().getString("jevistree.dialog.delete.error.title"),
                                        I18n.getInstance().getString("jevistree.dialog.delete.error.message"), null, ex);
                            }
                        } else {
                            // ... user chose CANCEL or closed the dialog
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        Alert alert1 = new Alert(AlertType.WARNING, I18n.getInstance().getString("dialog.warning.title"));
                        alert1.setContentText(I18n.getInstance().getString("dialog.warning.notallowed"));
                        TopMenu.applyActiveTheme(alert1.getDialogPane().getScene());
                        alert1.showAndWait();
                    });

                }
            }
        } catch (JEVisException e) {
            logger.error("Could not get JEVis data source.", e);
        }
    }

    public static void EventDeleteAllDependencies(JEVisTree tree) {
        logger.debug("EventDeleteAllDependencies");
        try {
            if (!tree.getSelectionModel().getSelectedItems().isEmpty()) {
                JEVisDataSource ds = tree.getJEVisDataSource();
                Task<List<JEVisObject>> listTask = new Task<List<JEVisObject>>() {
                    @Override
                    protected List<JEVisObject> call() {

                        List<JEVisObject> dependentObjects = new ArrayList<>();

                        try {
                            Map<JEVisObject, List<JEVisObject>> calculationMap = org.jevis.commons.utils.CommonMethods.createCalculationMap(ds);

                            List<JEVisObject> selectedItems = new ArrayList<>();
                            ObservableList<JEVisTreeItem> items = tree.getSelectionModel().getSelectedItems();
                            items.forEach(jeVisTreeItem -> selectedItems.add(jeVisTreeItem.getValue().getJEVisObject()));
                            dependentObjects = org.jevis.commons.utils.CommonMethods.getAllDependentObjects(calculationMap, selectedItems);
                        } catch (Exception e) {

                        }

                        return dependentObjects;
                    }
                };

                listTask.setOnSucceeded(workerStateEvent -> {
                    try {
                        List<JEVisObject> jeVisObjects = listTask.get();
                        List<SelectableObject> objects = new ArrayList<>();
                        ChannelTools channelTools = new ChannelTools();
                        channelTools.createChannelMaps(ds);

                        jeVisObjects.forEach(object -> {
                            Long target = tree.getTargetAndChannel().get(object.getID());
                            boolean selected = target != null && target != -1L;
                            boolean isCalculation = tree.getCalculationIDs().get(object.getID()) != null;
                            objects.add(new SelectableObject(object, !selected, isCalculation));
                        });

                        TableView<SelectableObject> tableView = new TableView<>();

                        TableColumn<SelectableObject, Boolean> selectionColumn = new TableColumn<>();
                        selectionColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectionColumn));
                        selectionColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));

                        TableColumn<SelectableObject, JEVisObject> objectColumn = new TableColumn<>();
                        StringConverter<JEVisObject> converter = new StringConverter<JEVisObject>() {
                            @Override
                            public String toString(JEVisObject object) {
                                String s = "";
                                if (object != null) {

                                    try {
                                        if (object.getJEVisClassName().equals(JC.Data.CleanData.name) || object.getJEVisClassName().equals(JC.Data.ForecastData.name)
                                                || object.getJEVisClassName().equals(JC.Data.MathData.name)) {
                                            JEVisObject firstParentalDataObject = CommonMethods.getFirstParentalDataObject(object);
                                            s += firstParentalDataObject.getName() + " \\ ";
                                        }
                                    } catch (Exception ignored) {
                                    }

                                    s += object.getName() + " : " + object.getID();
                                }
                                return s;
                            }

                            @Override
                            public JEVisObject fromString(String s) {
                                if (s != null) {
                                    int i = s.indexOf(";");
                                    String idString = s.substring(i + 1);
                                    Long id = Long.parseLong(idString);

                                    try {
                                        return ds.getObject(id);
                                    } catch (Exception e) {
                                        logger.error("Exception: ", e);
                                    }
                                }
                                return null;
                            }
                        };
                        objectColumn.setCellFactory(TextFieldTableCell.forTableColumn(converter));
                        objectColumn.setCellValueFactory(new PropertyValueFactory<>("object"));

                        TableColumn<SelectableObject, Boolean> calculationColumn = new TableColumn<>(I18n.getInstance().getString("plugin.graph.chart.selectiondialog.calculation"));
                        calculationColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectionColumn));
                        calculationColumn.setCellValueFactory(new PropertyValueFactory<>("calculation"));
                        calculationColumn.setEditable(false);

                        tableView.getColumns().setAll(selectionColumn, objectColumn, calculationColumn);

                        tableView.setItems(FXCollections.observableArrayList(objects));

                        Dialog<ButtonType> confirm = new Dialog<>();
                        confirm.setTitle(I18n.getInstance().getString("plugin.objects.dialog.deletedependencies.title"));
                        confirm.setHeaderText(I18n.getInstance().getString("plugin.objects.dialog.deletedependencies.header"));
                        confirm.setResizable(true);
                        confirm.initOwner(ControlCenter.getStage());
                        confirm.initModality(Modality.APPLICATION_MODAL);
                        Stage stage = (Stage) confirm.getDialogPane().getScene().getWindow();
                        TopMenu.applyActiveTheme(stage.getScene());
                        stage.setAlwaysOnTop(true);

                        ButtonType okType = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
                        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

                        confirm.getDialogPane().getButtonTypes().addAll(cancelType, okType);

                        Button okButton = (Button) confirm.getDialogPane().lookupButton(okType);
                        okButton.setDefaultButton(true);

                        Button cancelButton = (Button) confirm.getDialogPane().lookupButton(cancelType);
                        cancelButton.setCancelButton(true);

                        Label dateLabelFrom = new Label(I18n.getInstance().getString("tree.treehelper.from"));
                        DatePicker datePickerFrom = new DatePicker();
                        LocalTimePicker timePickerFrom = new LocalTimePicker();
                        // timePickerFrom.set24HourView(true);
                        // timePickerFrom.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));
                        AtomicBoolean changedFrom = new AtomicBoolean(false);

                        datePickerFrom.valueProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue != oldValue) {
                                changedFrom.set(true);
                            }
                        });

                        timePickerFrom.localTimeProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue != oldValue) {
                                changedFrom.set(true);
                            }
                        });

                        okButton.setOnAction(event -> {
                            try {
                                DateTime dateTimeFrom = new DateTime(
                                        datePickerFrom.valueProperty().get().getYear(),
                                        datePickerFrom.valueProperty().get().getMonthValue(),
                                        datePickerFrom.valueProperty().get().getDayOfMonth(),
                                        timePickerFrom.localTimeProperty().get().getHour(),
                                        timePickerFrom.localTimeProperty().get().getMinute(),
                                        timePickerFrom.localTimeProperty().get().getSecond(), DateTimeZone.getDefault());


                                for (SelectableObject dependency : objects) {
                                    if (dependency.isSelected()) {
                                        logger.info("deleting samples from {}:{} starting with {}", dependency.getObject().getName(), dependency.getObject().getID(), dateTimeFrom.toString());

                                        try {
                                            org.jevis.commons.utils.CommonMethods.deleteAllSamples(dependency.getObject(), dateTimeFrom, new DateTime(), true, true);
                                        } catch (Exception e) {
                                            logger.error("Could not delete samples from {}:{}:Value", dependency.getObject().getName(), dependency.getObject().getID());
                                        }
                                    }
                                }

                                confirm.close();
                            } catch (Exception e) {

                            }
                        });

                        cancelButton.setOnAction(event -> {
                            confirm.close();
                        });

                        HBox dateSelectionBox = new HBox(6, dateLabelFrom, datePickerFrom, timePickerFrom);

                        VBox contentPane = new VBox(8, dateSelectionBox, tableView);

                        confirm.getDialogPane().setContent(contentPane);
                        confirm.show();

                    } catch (InterruptedException e) {
                        logger.error("InterruptedException: ", e);
                    } catch (ExecutionException e) {
                        logger.error("ExecutionException: ", e);
                    }

                });

                ControlCenter.getStatusBar().addTask(TreeHelper.class.getName(), listTask, null, true);
            }

        } catch (Exception e) {

        }
    }

    public static void EventDeleteAllCalculations(JEVisTree tree) {
        logger.debug("EventDeleteAllCalculations");
        try {
            if (!tree.getSelectionModel().getSelectedItems().isEmpty()) {
                String question = I18n.getInstance().getString("jevistree.dialog.delete.message");
                ObservableList<TreeItem<JEVisTreeRow>> items = tree.getSelectionModel().getSelectedItems();
                for (TreeItem<JEVisTreeRow> item : items) {
                    if (items.indexOf(item) > 0 && items.indexOf(item) < items.size() - 1)
                        question += item.getValue().getJEVisObject().getName();
                }
                question += "?";

                if (tree.getJEVisDataSource().getCurrentUser().canWrite(items.get(0).getValue().getJEVisObject().getID())) {

                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle(I18n.getInstance().getString("jevistree.dialog.deleteCalculations.title"));
                    alert.setHeaderText(null);
                    alert.setContentText(question);
                    TopMenu.applyActiveTheme(alert.getDialogPane().getScene());
                    GridPane gp = new GridPane();
                    gp.setHgap(4);
                    gp.setVgap(6);

                    Label dateLabelFrom = new Label(I18n.getInstance().getString("tree.treehelper.from"));
                    DatePicker datePickerFrom = new DatePicker();
                    LocalTimePicker timePickerFrom = new LocalTimePicker();
//                    timePickerFrom.set24HourView(true);
//                    timePickerFrom.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));
                    AtomicBoolean changedFrom = new AtomicBoolean(false);

                    datePickerFrom.valueProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != oldValue) {
                            changedFrom.set(true);
                        }
                    });

                    timePickerFrom.localTimeProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != oldValue) {
                            changedFrom.set(true);
                        }
                    });

                    Label dateLabelTo = new Label(I18n.getInstance().getString("tree.treehelper.to"));
                    DatePicker datePickerTo = new DatePicker();
                    LocalTimePicker timePickerTo = new LocalTimePicker();
//                    timePickerTo.set24HourView(true);
//                    timePickerTo.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));
                    AtomicBoolean changedTo = new AtomicBoolean(false);

                    datePickerTo.valueProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != oldValue) {
                            changedTo.set(true);
                        }
                    });

                    datePickerTo.valueProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != oldValue) {
                            changedTo.set(true);
                        }
                    });


                    gp.add(dateLabelFrom, 0, 0);
                    gp.add(datePickerFrom, 1, 0);
                    gp.add(timePickerFrom, 2, 0);
                    gp.add(dateLabelTo, 0, 1);
                    gp.add(datePickerTo, 1, 1);
                    gp.add(timePickerTo, 2, 1);

                    alert.getDialogPane().setContent(gp);

                    alert.showAndWait().ifPresent(buttonType -> {
                        if (buttonType.equals(ButtonType.OK)) {
                            try {

                                final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("jevistree.dialog.deleteCleanAndRaw.title") + "...");

                                Task<Void> reload = new Task<Void>() {
                                    @Override
                                    protected Void call() {
                                        for (TreeItem<JEVisTreeRow> item : items) {
                                            if (!changedFrom.get() && !changedTo.get()) {
                                                CalculationMethods.deleteAllCalculations(pForm, item.getValue().getJEVisObject(), null, null);
                                            } else if (changedFrom.get() && !changedTo.get()) {
                                                DateTime dateTimeFrom = new DateTime(
                                                        datePickerFrom.valueProperty().get().getYear(),
                                                        datePickerFrom.valueProperty().get().getMonthValue(),
                                                        datePickerFrom.valueProperty().get().getDayOfMonth(),
                                                        timePickerFrom.getLocalTime().getHour(),
                                                        timePickerFrom.getLocalTime().getMinute(),
                                                        timePickerFrom.getLocalTime().getSecond(), DateTimeZone.getDefault());

                                                CalculationMethods.deleteAllCalculations(pForm, item.getValue().getJEVisObject(),
                                                        dateTimeFrom,
                                                        null);
                                            } else if (!changedFrom.get() && changedTo.get()) {
                                                DateTime dateTimeTo = new DateTime(
                                                        datePickerTo.valueProperty().get().getYear(),
                                                        datePickerTo.valueProperty().get().getMonthValue(),
                                                        datePickerTo.valueProperty().get().getDayOfMonth(),
                                                        timePickerTo.getLocalTime().getHour(),
                                                        timePickerTo.getLocalTime().getMinute(),
                                                        timePickerTo.getLocalTime().getSecond(), DateTimeZone.getDefault());

                                                CalculationMethods.deleteAllCalculations(pForm, item.getValue().getJEVisObject(),
                                                        null,
                                                        dateTimeTo);
                                            } else {
                                                DateTime dateTimeFrom = new DateTime(
                                                        datePickerFrom.valueProperty().get().getYear(),
                                                        datePickerFrom.valueProperty().get().getMonthValue(),
                                                        datePickerFrom.valueProperty().get().getDayOfMonth(),
                                                        timePickerFrom.getLocalTime().getHour(),
                                                        timePickerFrom.getLocalTime().getMinute(),
                                                        timePickerFrom.getLocalTime().getSecond(), DateTimeZone.getDefault());

                                                DateTime dateTimeTo = new DateTime(
                                                        datePickerTo.valueProperty().get().getYear(),
                                                        datePickerTo.valueProperty().get().getMonthValue(),
                                                        datePickerTo.valueProperty().get().getDayOfMonth(),
                                                        timePickerTo.getLocalTime().getHour(),
                                                        timePickerTo.getLocalTime().getMinute(),
                                                        timePickerTo.getLocalTime().getSecond(), DateTimeZone.getDefault());

                                                CalculationMethods.deleteAllCalculations(pForm, item.getValue().getJEVisObject(),
                                                        dateTimeFrom,
                                                        dateTimeTo);
                                            }
                                        }

                                        return null;
                                    }
                                };
                                reload.setOnSucceeded(event -> pForm.getDialogStage().close());

                                reload.setOnCancelled(event -> {
                                    logger.debug("Delete all samples Cancelled");
                                    pForm.getDialogStage().hide();
                                });

                                reload.setOnFailed(event -> {
                                    logger.debug("Delete all samples failed");
                                    pForm.getDialogStage().hide();
                                });

                                pForm.activateProgressBar(reload);
                                pForm.getDialogStage().show();

                                ControlCenter.getStatusBar().addTask("DeleteAllCalculations", reload, null, true);

                            } catch (Exception ex) {
                                logger.catching(ex);
                                CommonDialogs.showError(I18n.getInstance().getString("jevistree.dialog.delete.error.title"),
                                        I18n.getInstance().getString("jevistree.dialog.delete.error.message"), null, ex);
                            }
                        } else {
                            // ... user chose CANCEL or closed the dialog
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        Alert alert1 = new Alert(AlertType.WARNING, I18n.getInstance().getString("dialog.warning.title"));
                        alert1.setContentText(I18n.getInstance().getString("dialog.warning.notallowed"));
                        TopMenu.applyActiveTheme(alert1.getDialogPane().getScene());
                        alert1.showAndWait();
                    });

                }
            }
        } catch (JEVisException e) {
            logger.error("Could not get JEVis data source.", e);
        }
    }

    public static void EventCreateMultiplierAndDifferential(JEVisTree tree) {
        logger.debug("EventCreateMultiplierAndDifferential");
        try {
            if (!tree.getSelectionModel().getSelectedItems().isEmpty()) {
                ObservableList<TreeItem<JEVisTreeRow>> items = tree.getSelectionModel().getSelectedItems();


                if (tree.getJEVisDataSource().getCurrentUser().canWrite(items.get(0).getValue().getJEVisObject().getID())) {

                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle(I18n.getInstance().getString("jevistree.dialog.setMultiplierAndDifferential.title"));
                    alert.setHeaderText(null);
                    TopMenu.applyActiveTheme(alert.getDialogPane().getScene());
                    GridPane gp = new GridPane();
                    gp.setHgap(4);
                    gp.setVgap(6);
                    Label multiplierLabel = new Label("Multiplier");
                    TextField multiplier = new TextField();
                    Label differentialLabel = new Label("Differential");
                    ToggleSwitchPlus differential = new ToggleSwitchPlus();
                    differential.setSelected(false);
                    Label dateLabel = new Label("Date");
                    DatePicker datePicker = new DatePicker();
                    LocalTimePicker timePicker = new LocalTimePicker();
//                    timePicker.set24HourView(true);
//                    timePicker.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

                    gp.add(differentialLabel, 0, 0);
                    gp.add(differential, 1, 0, 2, 1);
                    gp.add(multiplierLabel, 0, 1);
                    gp.add(multiplier, 1, 1, 2, 1);
                    gp.add(dateLabel, 0, 2);
                    gp.add(datePicker, 1, 2);
                    gp.add(timePicker, 2, 2);

                    alert.getDialogPane().setContent(gp);

                    alert.showAndWait().ifPresent(buttonType -> {
                        if (buttonType.equals(ButtonType.OK)) {
                            try {
                                BigDecimal multiplierValue = new BigDecimal(multiplier.getText());
                                Boolean differentialValue = differential.isSelected();

                                DateTime dateTime = new DateTime(
                                        datePicker.valueProperty().get().getYear(),
                                        datePicker.valueProperty().get().getMonthValue(),
                                        datePicker.valueProperty().get().getDayOfMonth(),
                                        timePicker.getLocalTime().getHour(),
                                        timePicker.getLocalTime().getMinute(),
                                        timePicker.getLocalTime().getSecond(), DateTimeZone.getDefault()); // is this timezone correct?

                                final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("jevistree.dialog.setMultiplierAndDifferential.title") + "...");

                                Task<Void> set = new Task<Void>() {
                                    @Override
                                    protected Void call() {
                                        for (TreeItem<JEVisTreeRow> item : items) {
                                            DataMethods.setAllMultiplierAndDifferential(pForm, item.getValue().getJEVisObject(), multiplierValue, differentialValue, dateTime);
                                        }

                                        return null;
                                    }
                                };
                                set.setOnSucceeded(event -> pForm.getDialogStage().close());

                                set.setOnCancelled(event -> {
                                    logger.debug("Setting all multiplier and differential switches cancelled");
                                    pForm.getDialogStage().hide();
                                });

                                set.setOnFailed(event -> {
                                    logger.debug("Setting all multiplier and differential switches failed");
                                    pForm.getDialogStage().hide();
                                });

                                pForm.activateProgressBar(set);
                                pForm.getDialogStage().show();

                                new Thread(set).start();

                            } catch (Exception ex) {
                                logger.catching(ex);
                                CommonDialogs.showError(I18n.getInstance().getString("jevistree.dialog.delete.error.title"),
                                        I18n.getInstance().getString("jevistree.dialog.delete.error.message"), null, ex);
                            }
                        } else {
                            // ... user chose CANCEL or closed the dialog
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        Alert alert1 = new Alert(AlertType.WARNING, I18n.getInstance().getString("dialog.warning.title"));
                        alert1.setContentText(I18n.getInstance().getString("dialog.warning.notallowed"));
                        TopMenu.applyActiveTheme(alert1.getDialogPane().getScene());
                        alert1.showAndWait();
                    });

                }
            }
        } catch (JEVisException e) {
            logger.error("Could not get JEVis data source.", e);
        }
    }

    public static void EventSetUnitAndPeriodRecursive(JEVisTree tree) {
        logger.debug("EventSetUnitAndPeriodRecursive");
        try {
            if (!tree.getSelectionModel().getSelectedItems().isEmpty()) {
                ObservableList<TreeItem<JEVisTreeRow>> items = tree.getSelectionModel().getSelectedItems();

                if (tree.getJEVisDataSource().getCurrentUser().canWrite(items.get(0).getValue().getJEVisObject().getID())) {

                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle(I18n.getInstance().getString("jevistree.dialog.setUnitAndPeriodRecursive.title"));
                    TopMenu.applyActiveTheme(alert.getDialogPane().getScene());
                    alert.setHeaderText(null);
                    alert.setResizable(true);
                    GridPane gp = new GridPane();
                    gp.setHgap(4);
                    gp.setVgap(6);

                    final Label l_prefixL = new Label(I18n.getInstance().getString("attribute.editor.unit.prefix"));
                    final Label l_unitL = new Label(I18n.getInstance().getString("attribute.editor.unit.unit"));
                    final Label l_example = new Label(I18n.getInstance().getString("attribute.editor.unit.symbol"));
                    final Label l_SampleRate = new Label(I18n.getInstance().getString("attribute.editor.unit.samplingrate"));

                    CheckBox setUnit = new CheckBox("Unit");
                    setUnit.setSelected(true);
                    CheckBox setPeriod = new CheckBox("Period");
                    CheckBox setNewTypePeriod = new CheckBox("New Type Period");
                    setPeriod.setSelected(true);
                    setNewTypePeriod.setSelected(true);

                    DatePicker pickerDate = new DatePicker();
                    LocalTimePicker pickerTime = new LocalTimePicker();
                    pickerDate.setPrefWidth(120d);
                    pickerTime.setPrefWidth(110d);
//                    pickerTime.set24HourView(true);
//                    pickerTime.setConverter(new LocalTimeStringConverter(FormatStyle.MEDIUM));
                    pickerDate.valueProperty().setValue(LocalDate.of(1990, 1, 1));
                    pickerTime.setLocalTime(LocalTime.of(0, 0, 0));
                    HBox dateBox = new HBox(4, pickerDate, pickerTime);

                    gp.add(setUnit, 0, 0, 2, 1);
                    gp.add(l_prefixL, 0, 1);
                    gp.add(l_unitL, 0, 2);
                    gp.add(l_example, 0, 3);
                    gp.add(setPeriod, 0, 4, 2, 1);
                    gp.add(setNewTypePeriod, 0, 5);
                    gp.add(l_SampleRate, 0, 6);

                    final JEVisDataSource ds = items.get(0).getValue().getJEVisObject().getDataSource();
                    final JEVisObject object = DataMethods.getFirstCleanObject(items.get(0).getValue().getJEVisObject());

                    JEVisAttribute valueAtt = object.getAttribute("Value");
                    JEVisAttribute periodAtt = object.getAttribute("Period");

                    UnitSelectUI unitUI = new UnitSelectUI(ds, valueAtt.getInputUnit());
                    unitUI.getPrefixBox().setPrefWidth(95);
                    unitUI.getUnitButton().setPrefWidth(95);
                    unitUI.getSymbolField().setPrefWidth(95);
                    SamplingRateUI periodUI = new SamplingRateUI(new Period(periodAtt.getLatestSample().getValueAsString()));

                    gp.add(unitUI.getPrefixBox(), 1, 1);
                    gp.add(unitUI.getUnitButton(), 1, 2);
                    gp.add(unitUI.getSymbolField(), 1, 3);
                    gp.add(dateBox, 1, 5);
                    gp.add(periodUI, 1, 6);

                    alert.getDialogPane().setContent(gp);

                    alert.showAndWait().ifPresent(buttonType -> {
                        if (buttonType.equals(ButtonType.OK)) {
                            try {
                                boolean unit = setUnit.isSelected();
                                boolean period = setPeriod.isSelected();
                                boolean newType = setNewTypePeriod.isSelected();

                                final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("jevistree.dialog.setUnitAndPeriodRecursive.title") + "...");

                                Task<Void> set = new Task<Void>() {
                                    @Override
                                    protected Void call() {
                                        DateTime datetime = new DateTime(
                                                pickerDate.valueProperty().get().getYear(),
                                                pickerDate.valueProperty().get().getMonthValue(),
                                                pickerDate.valueProperty().get().getDayOfMonth(),
                                                pickerTime.getLocalTime().getHour(),
                                                pickerTime.getLocalTime().getMinute(),
                                                pickerTime.getLocalTime().getSecond(),
                                                DateTimeZone.getDefault());

                                        for (TreeItem<JEVisTreeRow> item : items) {
                                            try {
                                                DataMethods.setUnitAndPeriod(pForm, item.getValue().getJEVisObject(),
                                                        unit, unitUI, period, newType, datetime, periodUI);
                                            } catch (JEVisException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        return null;
                                    }
                                };
                                set.setOnSucceeded(event -> pForm.getDialogStage().close());

                                set.setOnCancelled(event -> {
                                    logger.debug("Setting all units and periods cancelled");
                                    pForm.getDialogStage().hide();
                                });

                                set.setOnFailed(event -> {
                                    logger.debug("Setting all units and periods failed");
                                    pForm.getDialogStage().hide();
                                });

                                pForm.activateProgressBar(set);
                                pForm.getDialogStage().show();

                                new Thread(set).start();

                            } catch (Exception ex) {
                                logger.catching(ex);
                                CommonDialogs.showError(I18n.getInstance().getString("jevistree.dialog.delete.error.title"),
                                        I18n.getInstance().getString("jevistree.dialog.delete.error.message"), null, ex);
                            }
                        } else {
                            // ... user chose CANCEL or closed the dialog
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        Alert alert1 = new Alert(AlertType.WARNING, I18n.getInstance().getString("dialog.warning.title"));
                        alert1.setContentText(I18n.getInstance().getString("dialog.warning.notallowed"));
                        TopMenu.applyActiveTheme(alert1.getDialogPane().getScene());
                        alert1.showAndWait();
                    });

                }
            }
        } catch (JEVisException e) {
            logger.error("Could not get JEVis data source.", e);
        }
    }

    public static void EventSetLimitsRecursive(JEVisTree tree) {
        logger.error("EventSetLimitsRecursive");
        try {
            if (!tree.getSelectionModel().getSelectedItems().isEmpty()) {
                ObservableList<TreeItem<JEVisTreeRow>> items = tree.getSelectionModel().getSelectedItems();


                if (tree.getJEVisDataSource().getCurrentUser().canWrite(items.get(0).getValue().getJEVisObject().getID())) {

                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle(I18n.getInstance().getString("jevistree.dialog.setLimitsRecursive.title"));
                    alert.setHeaderText(null);
                    TopMenu.applyActiveTheme(alert.getDialogPane().getScene());
                    GridPane gp = new GridPane();
                    gp.setHgap(4);
                    gp.setVgap(6);

                    CheckBox auto = new CheckBox("Automatische Festlegung");

                    Label presetDateLabel = new Label("Zeitbereich zur Bestimmung");
                    PresetDateBox presetDateBox = new PresetDateBox(tree.getJEVisDataSource(), null);
                    CheckBox minIsZero = new CheckBox("Untere Grenze ist immer 0");

                    Label limit1MinSubLabel = new Label("Toleranz der unteren Grenze [%]");
                    TextField limit1MinSub = new TextField("0");
                    Label limit1MaxAddLabel = new Label("Toleranz der oberen Grenze [%]");
                    TextField limit1MaxAdd = new TextField("15");

                    Label limit1MinTimesXLimit2MinLabel = new Label("Vielfaches des niedrigesten Wertes des Zeitbereichs");
                    TextField limit1MinTimesXLimit2Min = new TextField("2");

                    Label limit1MaxTimesXLimit2MaxLabel = new Label("Vielfaches des höchsten Wertes des Zeitbereichs");
                    TextField limit1MaxTimesXLimit2Max = new TextField("2");

                    Label limit1MinLabel = new Label("Limit 1 Min");
                    TextField limit1Min = new TextField();
                    Label limit1MaxLabel = new Label("Limit 1 Max");
                    TextField limit1Max = new TextField();

                    Label limit2MinLabel = new Label("Limit 2 Min");
                    TextField limit2Min = new TextField();
                    Label limit2MaxLabel = new Label("Limit 2 Max");
                    TextField limit2Max = new TextField();

                    auto.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            presetDateBox.setDisable(false);
                            minIsZero.setDisable(false);

                            limit1MinSubLabel.setDisable(false);
                            limit1MinSub.setDisable(false);
                            limit1MaxAddLabel.setDisable(false);
                            limit1MaxAdd.setDisable(false);

                            limit1MinTimesXLimit2MinLabel.setDisable(false);
                            limit1MinTimesXLimit2Min.setDisable(false);
                            limit1MaxTimesXLimit2MaxLabel.setDisable(false);
                            limit1MaxTimesXLimit2Max.setDisable(false);

                            limit1MinLabel.setDisable(true);
                            limit1Min.setDisable(true);
                            limit1MaxLabel.setDisable(true);
                            limit1Max.setDisable(true);

                            limit2MinLabel.setDisable(true);
                            limit2Min.setDisable(true);
                            limit2MaxLabel.setDisable(true);
                            limit2Max.setDisable(true);
                        } else {
                            presetDateBox.setDisable(true);
                            minIsZero.setDisable(true);

                            limit1MinSubLabel.setDisable(true);
                            limit1MinSub.setDisable(true);
                            limit1MaxAddLabel.setDisable(true);
                            limit1MaxAdd.setDisable(true);

                            limit1MinTimesXLimit2MinLabel.setDisable(true);
                            limit1MinTimesXLimit2Min.setDisable(true);
                            limit1MaxTimesXLimit2MaxLabel.setDisable(true);
                            limit1MaxTimesXLimit2Max.setDisable(true);

                            limit1MinLabel.setDisable(false);
                            limit1Min.setDisable(false);
                            limit1MaxLabel.setDisable(false);
                            limit1Max.setDisable(false);

                            limit2MinLabel.setDisable(false);
                            limit2Min.setDisable(false);
                            limit2MaxLabel.setDisable(false);
                            limit2Max.setDisable(false);
                        }
                    });

                    minIsZero.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            limit1MinSubLabel.setDisable(true);
                            limit1MinSub.setDisable(true);
                            limit1MinTimesXLimit2MinLabel.setDisable(true);
                            limit1MinTimesXLimit2Min.setDisable(true);
                        } else {
                            limit1MinSubLabel.setDisable(false);
                            limit1MinSub.setDisable(false);
                            limit1MinTimesXLimit2MinLabel.setDisable(false);
                            limit1MinTimesXLimit2Min.setDisable(false);
                        }
                    });

                    auto.setSelected(true);
                    minIsZero.setSelected(true);

                    int row = 0;

                    gp.add(auto, 0, row, 2, 1);
                    row++;

                    gp.add(new Separator(Orientation.HORIZONTAL), 0, row, 2, 1);
                    row++;

                    gp.add(presetDateLabel, 0, row);
                    gp.add(presetDateBox, 1, row);
                    row++;

                    gp.add(minIsZero, 0, row, 2, 1);
                    row++;

                    gp.add(limit1MinSubLabel, 0, row);
                    gp.add(limit1MinSub, 1, row);
                    row++;

                    gp.add(limit1MaxAddLabel, 0, row);
                    gp.add(limit1MaxAdd, 1, row);
                    row++;

                    gp.add(limit1MinTimesXLimit2MinLabel, 0, row);
                    gp.add(limit1MinTimesXLimit2Min, 1, row);
                    row++;

                    gp.add(limit1MaxTimesXLimit2MaxLabel, 0, row);
                    gp.add(limit1MaxTimesXLimit2Max, 1, row);
                    row++;

                    gp.add(new Separator(Orientation.HORIZONTAL), 0, row, 2, 1);
                    row++;

                    gp.add(limit1MinLabel, 0, row);
                    gp.add(limit1Min, 1, row);
                    row++;

                    gp.add(limit1MaxLabel, 0, row);
                    gp.add(limit1Max, 1, row);
                    row++;

                    gp.add(new Separator(Orientation.HORIZONTAL), 0, row, 2, 1);
                    row++;

                    gp.add(limit2MinLabel, 0, row);
                    gp.add(limit2Min, 1, row);
                    row++;

                    gp.add(limit2MaxLabel, 0, row);
                    gp.add(limit2Max, 1, row);

                    alert.getDialogPane().setContent(gp);

                    alert.showAndWait().ifPresent(buttonType -> {
                        if (buttonType.equals(ButtonType.OK)) {
                            try {
                                if (!auto.isSelected()) {
                                    BigDecimal limit1MinValue = new BigDecimal(limit1Min.getText());
                                    BigDecimal limit1MaxValue = new BigDecimal(limit1Max.getText());
                                    BigDecimal limit2MinValue = new BigDecimal(limit2Min.getText());
                                    BigDecimal limit2MaxValue = new BigDecimal(limit2Max.getText());

                                    List<JsonLimitsConfig> list = new ArrayList<>();

                                    JsonLimitsConfig newConfig1 = new JsonLimitsConfig();
                                    newConfig1.setName(I18n.getInstance().getString("newobject.title1"));
                                    newConfig1.setMin(limit1MinValue.toString());
                                    newConfig1.setMax(limit1MaxValue.toString());

                                    list.add(newConfig1);

                                    JsonLimitsConfig newConfig2 = new JsonLimitsConfig();
                                    newConfig2.setName(I18n.getInstance().getString("newobject.title2"));
                                    newConfig2.setMin(limit2MinValue.toString());
                                    newConfig2.setMax(limit2MaxValue.toString());

                                    list.add(newConfig2);

                                    final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("jevistree.dialog.setLimitsRecursive.title") + "...");

                                    Task<Void> set = new Task<Void>() {
                                        @Override
                                        protected Void call() {
                                            for (TreeItem<JEVisTreeRow> item : items) {
                                                DataMethods.setLimits(pForm, item.getValue().getJEVisObject(), list);
                                            }

                                            return null;
                                        }
                                    };
                                    set.setOnSucceeded(event -> pForm.getDialogStage().close());

                                    set.setOnCancelled(event -> {
                                        logger.debug("Setting all limits cancelled");
                                        pForm.getDialogStage().hide();
                                    });

                                    set.setOnFailed(event -> {
                                        logger.debug("Setting all limits failed");
                                        pForm.getDialogStage().hide();
                                    });

                                    pForm.activateProgressBar(set);
                                    pForm.getDialogStage().show();

                                    new Thread(set).start();
                                } else {
                                    AutoLimitSetting autoLimitSetting = new AutoLimitSetting(presetDateBox.getStartDate(), presetDateBox.getEndDate(), minIsZero.isSelected(),
                                            new BigDecimal(limit1MinSub.getText()), new BigDecimal(limit1MaxAdd.getText()),
                                            new BigDecimal(limit1MinTimesXLimit2Min.getText()),
                                            new BigDecimal(limit1MaxTimesXLimit2Max.getText()));

                                    final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("jevistree.dialog.setLimitsRecursive.title") + "...");

                                    Task<Void> set = new Task<Void>() {
                                        @Override
                                        protected Void call() {
                                            for (TreeItem<JEVisTreeRow> item : items) {
                                                DataMethods.setAutoLimits(pForm, item.getValue().getJEVisObject(), autoLimitSetting);
                                            }

                                            return null;
                                        }
                                    };
                                    set.setOnSucceeded(event -> pForm.getDialogStage().close());

                                    set.setOnCancelled(event -> {
                                        logger.debug("Setting all limits cancelled");
                                        pForm.getDialogStage().hide();
                                    });

                                    set.setOnFailed(event -> {
                                        logger.debug("Setting all limits failed");
                                        pForm.getDialogStage().hide();
                                    });

                                    pForm.activateProgressBar(set);
                                    pForm.getDialogStage().show();

                                    new Thread(set).start();
                                }

                            } catch (Exception ex) {
                                logger.catching(ex);
                                CommonDialogs.showError(I18n.getInstance().getString("jevistree.dialog.delete.error.title"),
                                        I18n.getInstance().getString("jevistree.dialog.delete.error.message"), null, ex);
                            }
                        } else {
                            // ... user chose CANCEL or closed the dialog
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        Alert alert1 = new Alert(AlertType.WARNING, I18n.getInstance().getString("dialog.warning.title"));
                        alert1.setContentText(I18n.getInstance().getString("dialog.warning.notallowed"));
                        TopMenu.applyActiveTheme(alert1.getDialogPane().getScene());
                        alert1.showAndWait();
                    });

                }
            }
        } catch (JEVisException e) {
            logger.error("Could not get JEVis data source.", e);
        }
    }

    public static void EventSetSubstitutionSettingsRecursive(JEVisTree tree) {
        logger.error("EventSetSubstitutionSettingsRecursive");
        try {
            if (!tree.getSelectionModel().getSelectedItems().isEmpty()) {
                ObservableList<TreeItem<JEVisTreeRow>> items = tree.getSelectionModel().getSelectedItems();


                if (tree.getJEVisDataSource().getCurrentUser().canWrite(items.get(0).getValue().getJEVisObject().getID())) {

                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle(I18n.getInstance().getString("jevistree.dialog.setSubstitutionSettingsRecursive.title"));
                    alert.setHeaderText(null);
                    TopMenu.applyActiveTheme(alert.getDialogPane().getScene());

                    TabPane tabPane = new TabPane();
                    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

                    List<JsonGapFillingConfig> defaultConfig = GapFillingEditor.createDefaultConfig();

                    for (JsonGapFillingConfig config : defaultConfig) {
                        Tab newTab = new Tab(config.getName());
                        tabPane.getTabs().add(newTab);
                        GapFillingEditor.fillTab(newTab, config);
                    }

                    alert.getDialogPane().setContent(tabPane);

                    alert.showAndWait().ifPresent(buttonType -> {
                        if (buttonType.equals(ButtonType.OK)) {
                            try {

                                final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("jevistree.dialog.setLimitsRecursive.title") + "...");

                                Task<Void> set = new Task<Void>() {
                                    @Override
                                    protected Void call() {
                                        for (TreeItem<JEVisTreeRow> item : items) {
                                            DataMethods.setSubstitutionSettings(pForm, item.getValue().getJEVisObject(), defaultConfig);
                                        }

                                        return null;
                                    }
                                };
                                set.setOnSucceeded(event -> pForm.getDialogStage().close());

                                set.setOnCancelled(event -> {
                                    logger.debug("Setting all limits cancelled");
                                    pForm.getDialogStage().hide();
                                });

                                set.setOnFailed(event -> {
                                    logger.debug("Setting all limits failed");
                                    pForm.getDialogStage().hide();
                                });

                                pForm.activateProgressBar(set);
                                pForm.getDialogStage().show();

                                new Thread(set).start();

                            } catch (Exception ex) {
                                logger.catching(ex);
                                CommonDialogs.showError(I18n.getInstance().getString("jevistree.dialog.delete.error.title"),
                                        I18n.getInstance().getString("jevistree.dialog.delete.error.message"), null, ex);
                            }
                        } else {
                            // ... user chose CANCEL or closed the dialog
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        Alert alert1 = new Alert(AlertType.WARNING, I18n.getInstance().getString("dialog.warning.title"));
                        alert1.setContentText(I18n.getInstance().getString("dialog.warning.notallowed"));
                        TopMenu.applyActiveTheme(alert1.getDialogPane().getScene());
                        alert1.showAndWait();
                    });

                }
            } else {
                logger.error("Selection is empty");
            }
        } catch (JEVisException e) {
            logger.error("Could not get JEVis data source.", e);
        }
    }

    public static void openPath(JEVisTree tree, List<JEVisObject> toOpen, TreeItem<JEVisTreeRow> root, JEVisObject target) {
//        logger.info("OpenPath: " + root.getValue().getID());
//        logger.trace("OpenPath: {}", target.getID());
        for (TreeItem<JEVisTreeRow> child : root.getChildren()) {
            for (JEVisObject findObj : toOpen) {
//                logger.trace("OpenPath2: toOpen: {} in: {}", findObj.getID(), child.getValue().getJEVisObject().getID());
                if (findObj.getID().equals(child.getValue().getJEVisObject().getID())) {
                    child.expandedProperty().setValue(Boolean.TRUE);
                    openPath(tree, toOpen, child, target);
                }
                if (target.getID().equals(child.getValue().getJEVisObject().getID())) {
                    tree.getSelectionModel().select(child);
                    try {
                        VirtualFlow flow = (VirtualFlow) tree.getChildrenUnmodifiable().get(1);
                        int selected = tree.getSelectionModel().getSelectedIndex();
                        //TODO JFX17 Testen
                        flow.scrollTo(selected);
                    } catch (Exception ex) {

                    }

                }
            }

        }
    }

    /**
     * Find and select the JEVisObject in the JEVisTree. WARNING this function
     * will go over all Items wich getChildren which will load them all.
     *
     * @param tree
     * @param startObj
     * @param findObj
     */
    public static void selectNode(JEVisTree tree, TreeItem<JEVisObject> startObj, JEVisObject findObj) {
        for (TreeItem<JEVisObject> item : startObj.getChildren()) {
            if (Objects.equals(item.getValue().getID(), findObj.getID())) {
                tree.getSelectionModel().select(item);
            } else {
                selectNode(tree, item, findObj);
            }
        }
    }

    public static void EventOpenObject(JEVisTree tree, KeyCombination keyCombination) {
        try {
            JEVisDataSource ds = tree.getJEVisDataSource();

            if (keyCombination == null || keyCombination.equals(JEVisTreeFactory.findNode)) {
                FindDialog dia = new FindDialog(ds);
                FindDialog.Response response = dia.show(I18n.getInstance().getString("jevistree.dialog.find.title")
                        , I18n.getInstance().getString("jevistree.dialog.find.message")
                        , "");

                if (response == FindDialog.Response.YES) {
                    try {
                        JEVisObject findObj = ds.getObject(Long.parseLong(dia.getResult()));
                        logger.trace("Found Object: " + findObj);
                        if (findObj != null) {
                            List<JEVisObject> toOpen = org.jevis.commons.utils.ObjectHelper.getAllParents(findObj);
                            toOpen.add(findObj);
                            logger.trace("Open Path: {}", Arrays.toString(toOpen.toArray()));

                            TreeHelper.openPath(tree, toOpen, tree.getRoot(), findObj);

                        }
                    } catch (NumberFormatException nfe) {
                        try {
                            List<JEVisObject> allObjects = ds.getObjects();
                            for (JEVisObject object : allObjects) {
                                if (object.getName().contains(dia.getResult())) {
                                    List<JEVisObject> toOpen = ObjectHelper.getAllParents(object);
                                    toOpen.add(object);
                                    logger.trace("Open Path: {}", Arrays.toString(toOpen.toArray()));

                                    TreeHelper.openPath(tree, toOpen, tree.getRoot(), object);
                                    lastSearchIndex = allObjects.indexOf(object);
                                    lastSearch = dia.getResult();
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle(I18n.getInstance().getString("jevistree.dialog.find.error.title"));
                            alert.setHeaderText("");
                            TopMenu.applyActiveTheme(alert.getDialogPane().getScene());
                            String s = I18n.getInstance().getString("jevistree.dialog.find.error.message");
                            alert.setContentText(s);
                            alert.show();
                        }
                    }

                }
            } else {
                try {
                    if (lastSearchIndex > 0 && !lastSearch.equals("")) {
                        List<JEVisObject> allObjects = ds.getObjects();
                        for (JEVisObject object : allObjects.subList((int) lastSearchIndex, allObjects.size() - 1)) {
                            if (object.getName().contains(lastSearch)) {
                                List<JEVisObject> toOpen = ObjectHelper.getAllParents(object);
                                toOpen.add(object);
                                logger.trace("Open Path: {}", Arrays.toString(toOpen.toArray()));

                                TreeHelper.openPath(tree, toOpen, tree.getRoot(), object);
                                lastSearchIndex = allObjects.indexOf(object) + 1;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle(I18n.getInstance().getString("jevistree.dialog.find.error.title"));
                    alert.setHeaderText("");
                    TopMenu.applyActiveTheme(alert.getDialogPane().getScene());
                    String s = I18n.getInstance().getString("jevistree.dialog.find.error.message");
                    alert.setContentText(s);
                    alert.show();
                }
            }

        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(I18n.getInstance().getString("jevistree.dialog.find.error.title"));
            alert.setHeaderText("");
            TopMenu.applyActiveTheme(alert.getDialogPane().getScene());
            String s = I18n.getInstance().getString("jevistree.dialog.find.error.message");
            alert.setContentText(s);
            alert.show();
            ex.printStackTrace();
        }
    }

    public static void moveObject(final List<JEVisObject> moveObj, final JEVisObject targetObj) {
        logger.debug("EventMoveObject");


        for (JEVisObject obj : moveObj) {
            try {
                // remove other parent relationships
                try {
                    //From Child to Parent
                    for (JEVisRelationship rel : obj.getRelationships()) {
                        if (rel.isType(JEVisConstants.ObjectRelationship.PARENT) && rel.getStartObject().equals(obj)) {
                            obj.deleteRelationship(rel);
                        }
                        if (rel.isType(JEVisConstants.ObjectRelationship.DELETED_PARENT) && rel.getStartObject().equals(obj)) {
                            obj.deleteRelationship(rel);
                        }
                    }


                } catch (Exception ex) {
                    logger.error("Error while deleting old parentship", ex, ex);
                }

                try {
                    if (obj.getDeleteTS() != null) {
                        obj.setDeleteTS(null);
                        obj.commit();

                        /* the server will also set die children delete_ts to null.
                         *   We could reload the but simply set this info is faster
                         */
                        List<JEVisObject> children = org.jevis.commons.utils.CommonMethods.getChildrenRecursive(obj);
                        children.forEach(jeVisObject -> {
                            try {
                                jeVisObject.setDeleteTS(null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

                    }
                } catch (Exception ex) {
                    logger.error("Error while restoring object", ex, ex);
                }

                try {
                    JEVisRelationship newRel = obj.buildRelationship(targetObj, JEVisConstants.ObjectRelationship.PARENT, JEVisConstants.Direction.FORWARD);
                } catch (Exception ex) {
                    logger.error("Error while creating new parentship", ex, ex);
                }


            } catch (Exception ex) {
                logger.catching(ex);
                CommonDialogs.showError(I18n.getInstance().getString("jevistree.dialog.move.error.title"),
                        I18n.getInstance().getString("jevistree.dialog.move.error.message"), null, ex);
            }
        }


    }

    public static void buildLink(List<JEVisObject> linkSrcObjs, final JEVisObject targetParent, String selectedLinkName) {
        try {

            for (JEVisObject linkSrcObj : linkSrcObjs) {
                String linkName = selectedLinkName;
                if (linkSrcObjs.size() > 1) {
                    linkName = linkSrcObj.getName();
                }

                JEVisObject newLinkObj = targetParent.buildObject(linkName, targetParent.getDataSource().getJEVisClass(CommonClasses.LINK.NAME));
                newLinkObj.commit();
                logger.debug("new LinkObject: " + newLinkObj);
                CommonObjectTasks.createLink(newLinkObj, linkSrcObj);
            }

        } catch (JEVisException ex) {
            logger.error(ex, ex);
        } catch (Exception ex) {
            logger.fatal(ex, ex);
        }
    }

    public static void EventReload(JEVisObject object, JEVisTreeItem jeVisTreeItem) {
        final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("jevistree.menu.reload") + "...");

        Task<Void> reload = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    object.getDataSource().reloadAttribute(object);
                    object.getDataSource().reloadObject(object);
                    Platform.runLater(() -> {
                        JEVisTreeRow sobj = new JEVisTreeRow(object);
                        jeVisTreeItem.setValue(sobj);
                    });
                } catch (JEVisException e) {
                    logger.error("Could not reload object.");
                }

                return null;
            }
        };
        reload.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                pForm.getDialogStage().close();
            }
        });

        reload.setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                logger.debug("Reload Cancel");
                pForm.getDialogStage().hide();
            }
        });

        reload.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                logger.debug("Reload failed");
                pForm.getDialogStage().hide();
            }
        });

        pForm.activateProgressBar(reload);
        pForm.getDialogStage().show();

        new Thread(reload).start();


    }

    public static void EventDrop(final JEVisTree tree, List<JEVisObject> dragObj, JEVisObject targetParent, CopyObjectDialog.DefaultAction mode) {
        try {

            boolean permissionsOK = true;
            boolean isOwnChild = false;
            for (JEVisObject obj : dragObj) {
                if (targetParent.getID() != null && tree.getJEVisDataSource().getCurrentUser().canCreate(targetParent.getID())) {

                    logger.trace("EventDrop");
                    if (isOwnChildCheck(obj, targetParent)) {
                        isOwnChild = true;
                    }

                    //boolean isOwnParen = isOwnParentCheck(dragObj, targetParent);
                    logger.error("Is ownChild: {}", isOwnChild);
                } else {
                    permissionsOK = false;
                }
            }

            if (permissionsOK) {
                CopyObjectDialog dia = new CopyObjectDialog(dragObj, targetParent, mode);

                boolean finalIsOwnChild = isOwnChild;
                dia.setOnCloseRequest(dialogEvent -> {
                    if (dia.getResponse() == CopyObjectDialog.Response.MOVE) {
                        moveObject(dragObj, targetParent);
                    } else if (dia.getResponse() == CopyObjectDialog.Response.LINK) {
                        buildLink(dragObj, targetParent, dia.getCreateName());
                    } else if (dia.getResponse() == CopyObjectDialog.Response.COPY) {
                        boolean recursion = dia.isRecursion();
                        recursion = !finalIsOwnChild && recursion;
                        logger.warn("Warning recursion detected disable recursion: {}", recursion);

                        copyObject(dragObj, targetParent, dia.getCreateName(), dia.isIncludeData(), dia.isIncludeValues(), recursion, dia.getCreateCount());
                    }
                });

                dia.showAndWait();
            } else {
                Platform.runLater(() -> {
                    Alert alert1 = new Alert(AlertType.WARNING, I18n.getInstance().getString("dialog.warning.title"));
                    alert1.setContentText(I18n.getInstance().getString("dialog.warning.notallowed"));
                    TopMenu.applyActiveTheme(alert1.getDialogPane().getScene());
                    alert1.showAndWait();
                });
            }


        } catch (JEVisException e) {
            logger.error("Could not get jevis data source.", e);
        }
    }

    /**
     * Check if target is an parent of the object or self
     *
     * @param dragObject   object to copy/move
     * @param targetParent target parent object
     * @return true if parent loop
     */
    /**
     * public static boolean isOwnParentCheck(JEVisObject dragObject, JEVisObject targetParent) {
     * if (targetParent.getID().equals(dragObject.getID())) {
     * return true;
     * }
     * <p>
     * try {
     * for (JEVisObject obj : dragObject.getChildren()) {
     * if (isOwnParentCheck(obj, targetParent)) {
     * return true;
     * }
     * }
     * } catch (Exception ex) {
     * logger.warn("Error in parent check: {}", ex, ex);
     * }
     * <p>
     * return false;
     * <p>
     * }
     **/

    public static boolean isOwnChildCheck(JEVisObject dragObject, JEVisObject targetParent) {
        if (targetParent.getID().equals(dragObject.getID())) {
            return true;
        }
        try {
            for (JEVisObject obj : dragObject.getChildren()) {
                if (isOwnChildCheck(obj, targetParent)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            logger.warn("Error in parent check: {}", ex, ex);
        }
        return false;

    }


    public static void copyObjectUnder(JEVisObject toCopyObj, final JEVisObject newParent, String newName,
                                       boolean includeData, boolean includeValues, boolean recursive) throws JEVisException {
        logger.debug("-> copyObjectUnder ([{}]{}) under ([{}]{})", toCopyObj.getID(), toCopyObj.getName(), newParent.getID(), newParent.getName());

        JEVisObject newObject = newParent.buildObject(newName, toCopyObj.getJEVisClass());
        Map<String, String> commitLangMap = toCopyObj.getLocalNameList();
        if (commitLangMap.containsKey(I18n.getInstance().getLocale().getLanguage())) {
            commitLangMap.replace((I18n.getInstance().getLocale().getLanguage()), newName);
        } else {
            commitLangMap.put(I18n.getInstance().getLocale().getLanguage(), newName);
        }
        newObject.setLocalNames(commitLangMap);
        newObject.commit();

        for (JEVisAttribute originalAtt : toCopyObj.getAttributes()) {
            logger.debug("Copy attribute: {}", originalAtt);
            JEVisAttribute newAtt = newObject.getAttribute(originalAtt.getType());
            newAtt.setDisplaySampleRate(originalAtt.getDisplaySampleRate());
            newAtt.setDisplayUnit(originalAtt.getDisplayUnit());
            newAtt.setInputSampleRate(originalAtt.getInputSampleRate());
            newAtt.setInputUnit(originalAtt.getInputUnit());
            newAtt.commit();

            if (includeData || includeValues || originalAtt.hasSample()) {
                try {
                    if (originalAtt.hasSample()) {
                        if (!includeValues && newAtt.getName().equals(CleanDataObject.AttributeName.VALUE.getAttributeName())) {
                            continue;
                        }

                        List<JEVisSample> newSamples = new ArrayList<>();
                        for (JEVisSample sample : originalAtt.getAllSamples()) {
                            try {
                                if (originalAtt.getPrimitiveType() == JEVisConstants.PrimitiveType.FILE) {
                                    JEVisFile tmpFile = sample.getValueAsFile();
                                    newSamples.add(newAtt.buildSample(sample.getTimestamp(), tmpFile, sample.getNote()));
                                } else {
                                    newSamples.add(newAtt.buildSample(sample.getTimestamp(), sample.getValue(), sample.getNote()));
                                }
                            } catch (Exception ex) {
                                logger.error("Error while coping samples for: {}", originalAtt, ex);
                            }
                        }
                        logger.debug("Add samples: {}", newSamples.size());
                        newAtt.addSamples(newSamples);
                    }
                } catch (Exception ex) {
                    logger.error("Error while coping attribute: {}", originalAtt, ex);
                }
            }
        }

        //TODO: we need an recursive check to avoid an endless loop
        //Also copy the children if chosen
        if (recursive) {
            logger.debug("recursive is enabled");
            for (JEVisObject otherChild : toCopyObj.getChildren()) {
                copyObjectUnder(otherChild, newObject, otherChild.getName(), includeData, includeValues, recursive);
            }
        }

    }

    public static void copyObject(final List<JEVisObject> toCopyObjs, final JEVisObject newParent, String selectedName,
                                  boolean includeData, boolean includeValues, boolean recursive, int createCount) {
        try {
            for (JEVisObject toCopyObj : toCopyObjs) {
                logger.debug("-> Copy ([{}]{}) under ([{}]{})", toCopyObj.getID(), toCopyObj.getName(), newParent.getID(), newParent.getName());

                final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("jevistree.menu.copy") + "...");

                Task<Void> upload = new Task<Void>() {
                    @Override
                    protected Void call() {

                        try {
                            String name = toCopyObj.getName();
                            if (toCopyObjs.size() == 1) name = selectedName;


                            for (int i = 0; i < createCount; i++) {
                                String newName = name;
                                if (createCount > 1) {
                                    newName += (" " + (i + 1));
                                }
                                copyObjectUnder(toCopyObj, newParent, newName, includeData, includeValues, recursive);
                            }

                        } catch (Exception ex) {
                            logger.catching(ex);
                            CommonDialogs.showError(I18n.getInstance().getString("jevistree.dialog.copy.error.title"),
                                    I18n.getInstance().getString("jevistree.dialog.copy.error.message"), null, ex);
                            failed();
                        }
                        return null;
                    }
                };
                upload.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        pForm.getDialogStage().close();
                    }
                });

                upload.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        logger.error("Upload Cancel");
                        pForm.getDialogStage().hide();
                    }
                });

                upload.setOnFailed(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        logger.error("Upload failed");
                        pForm.getDialogStage().hide();
                    }
                });

                pForm.activateProgressBar(upload);
                pForm.getDialogStage().show();

                new Thread(upload).start();

            }


        } catch (Exception ex) {
            logger.catching(ex);
            CommonDialogs.showError(I18n.getInstance().getString("jevistree.dialog.copy.error.title"),
                    I18n.getInstance().getString("jevistree.dialog.copy.error.message"), null, ex);
        }
    }

    /**
     * Opens the new Object Dialog.
     *
     * @param tree
     * @param parent
     */
    public static void EventNew(final JEVisTree tree, JEVisObject parent) {
        NewObject.NewObject(tree, parent);
    }

    public static void EventExportTree(JEVisObject obj) throws JEVisException {
        List<JEVisTreeFilter> allFilter = new ArrayList<>();
        JEVisTreeFilter basicFilter = SelectTargetDialog.buildAllDataAndCleanDataFilter();
        allFilter.add(basicFilter);

        List<UserSelection> userSelection = new ArrayList<>();
        userSelection.add(new UserSelection(UserSelection.SelectionType.Object, obj));
        SelectTargetDialog dia = new SelectTargetDialog(allFilter, basicFilter, null, SelectionMode.SINGLE, obj.getDataSource(), userSelection);

        dia.setOnCloseRequest(event -> {
            SelectTargetDialog.Response response = dia.getResponse();
            if (response == SelectTargetDialog.Response.OK) {
                List<JEVisObject> objects = new ArrayList<>();

                for (UserSelection us : dia.getUserSelection()) {
                    objects.add(us.getSelectedObject());
                }

                try {
                    ExportMaster em = new ExportMaster();
                    em.setObject(objects, true);
                    em.createTemplate(obj);


                    DirectoryChooser fileChooser = new DirectoryChooser();

                    fileChooser.setTitle("Open Resource File");
//                fileChooser.getExtensionFilters().addAll();
                    File selectedFile = fileChooser.showDialog(null);
                    if (selectedFile != null) {
                        em.export(selectedFile);
                    }

                } catch (IOException io) {

                }
            }
        });
    }

    public static void createCalcInput(JEVisObject calcObject, JEVisAttribute currentTarget, VariablesBox variablesBox, FormulaBox formulaBox) throws
            JEVisException {
        logger.debug("Event Create new Input");

        List<JEVisClass> classes = new ArrayList<>();

        for (String className : TreeSelectionDialog.allData) {
            classes.add(calcObject.getDataSource().getJEVisClass(className));
        }

        List<UserSelection> openList = new ArrayList<>();
        TargetHelper th = new TargetHelper(calcObject.getDataSource(), currentTarget);
        if (!th.getAttribute().isEmpty()) {
            for (JEVisAttribute att : th.getAttribute())
                openList.add(new UserSelection(UserSelection.SelectionType.Attribute, att, null, null));
        } else if (!th.getObject().isEmpty()) {
            for (JEVisObject obj : th.getObject())
                openList.add(new UserSelection(UserSelection.SelectionType.Object, obj));
        }

        TreeSelectionDialog selectTargetDialog = new TreeSelectionDialog(calcObject.getDataSource(), classes, SelectionMode.MULTIPLE, openList, true);
        selectTargetDialog.setOnCloseRequest(event -> {
            try {
                if (selectTargetDialog.getResponse() == Response.OK) {
                    if (selectTargetDialog.getUserSelection() != null && !selectTargetDialog.getUserSelection().isEmpty()) {
                        JEVisClass cleanDataClass = null;

                        cleanDataClass = calcObject.getDataSource().getJEVisClass("Clean Data");
                        JEVisClass dataClass = calcObject.getDataSource().getJEVisClass("Data");
                        JEVisClass baseDataClass = calcObject.getDataSource().getJEVisClass("Base Data");
                        for (UserSelection us : selectTargetDialog.getUserSelection()) {
                            JEVisObject correspondingCleanObject = null;
                            if ((us.getSelectedObject().getJEVisClass().equals(dataClass) || us.getSelectedObject().getJEVisClass().equals(baseDataClass))) {
                                List<JEVisObject> children = us.getSelectedObject().getChildren(cleanDataClass, false);
                                if (!children.isEmpty()) {
                                    correspondingCleanObject = children.get(0);
                                }
                            }

                            String inputName = CalculationNameFormatter.createVariableName(us.getSelectedObject());
                            JEVisClass inputClass = calcObject.getDataSource().getJEVisClass(JC.Input.name);
                            JEVisObject newInputObj = calcObject.buildObject(inputName, inputClass);
                            newInputObj.setLocalNames(us.getSelectedObject().getLocalNameList());
                            newInputObj.commit();

                            DateTime now = new DateTime();
                            JEVisAttribute aIdentifier = newInputObj.getAttribute("Identifier");
                            JEVisSample newSample = aIdentifier.buildSample(now, inputName);
                            newSample.commit();

                            JEVisAttribute aInputData = newInputObj.getAttribute("Input Data");
                            JEVisAttribute inputDataTypeAtt = newInputObj.getAttribute("Input Data Type");

                            JEVisAttribute targetAtt = us.getSelectedAttribute();
                            if (targetAtt == null) {
                                targetAtt = us.getSelectedObject().getAttribute(CleanDataObject.AttributeName.VALUE.getAttributeName());
                            }

                            TargetHelper th2 = null;
                            if (correspondingCleanObject == null) {
                                th2 = new TargetHelper(us.getSelectedObject().getDataSource(), us.getSelectedObject(), targetAtt);
                            } else {
                                targetAtt = correspondingCleanObject.getAttribute(CleanDataObject.AttributeName.VALUE.getAttributeName());
                                th2 = new TargetHelper(us.getSelectedObject().getDataSource(), correspondingCleanObject, targetAtt);
                            }

                            if (th2.isValid() && th2.targetObjectAccessible()) {
                                logger.info("Target Is valid");
                                JEVisSample newTarget = aInputData.buildSample(now, th2.getSourceString());
                                newTarget.commit();
                                JEVisSample periodicInputData = inputDataTypeAtt.buildSample(new DateTime(), "PERIODIC");
                                periodicInputData.commit();
                            } else {
                                logger.info("Target is not valid");
                            }
                        }
                    }
                    if (formulaBox != null) {
                        formulaBox.updateVariables();
                    }

                    if (variablesBox != null) {
                        variablesBox.listVariables(calcObject);
                    }
                }
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        });
        selectTargetDialog.show();
    }

    /**
     * Opens the new Object dialog for the currently selected node in the tree
     *
     * @param tree
     */
    public static void EventNew(final JEVisTree tree) {
        final TreeItem<JEVisTreeRow> parent = ((TreeItem<JEVisTreeRow>) tree.getSelectionModel().getSelectedItem());
        EventNew(tree, parent.getValue().getJEVisObject());
    }

    public static void EventSetEnableAll(JEVisTree tree, boolean b) {
        logger.debug("EventSetEnable:" + b);
        try {
            if (!tree.getSelectionModel().getSelectedItems().isEmpty()) {
                String question = "";
                if (b) {
                    question = I18n.getInstance().getString("jevistree.dialog.enable.message");
                } else {
                    question = I18n.getInstance().getString("jevistree.dialog.disable.message");
                }
                ObservableList<TreeItem<JEVisTreeRow>> items = tree.getSelectionModel().getSelectedItems();
                for (TreeItem<JEVisTreeRow> item : items) {
                    if (items.indexOf(item) > 0 && items.indexOf(item) < items.size() - 1)
                        question += item.getValue().getJEVisObject().getName();
                }
                question += "?";

                if (tree.getJEVisDataSource().getCurrentUser().canWrite(items.get(0).getValue().getJEVisObject().getID())) {

                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle(I18n.getInstance().getString("jevistree.dialog.enable.title"));
                    alert.setHeaderText(null);
                    TopMenu.applyActiveTheme(alert.getDialogPane().getScene());
                    VBox vBox = new VBox();
                    Label qLabel = new Label(question);
                    qLabel.setWrapText(true);

                    JEVisDataSource ds = items.get(0).getValue().getJEVisObject().getDataSource();
                    List<JEVisClass> jeVisClasses = ds.getJEVisClasses();
                    List<String> jeVisClassesStrings = new ArrayList<>();
                    for (JEVisClass jeVisClass : jeVisClasses) {
                        try {
                            jeVisClassesStrings.add(jeVisClass.getName());
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                    }
                    AlphanumComparator ac = new AlphanumComparator();
                    jeVisClassesStrings.sort(ac);
                    jeVisClassesStrings.add(0, "All");
                    ComboBox<String> jeVisClassComboBox = new ComboBox<>(FXCollections.observableList(jeVisClassesStrings));
                    jeVisClassComboBox.getSelectionModel().selectFirst();
                    vBox.getChildren().addAll(qLabel, jeVisClassComboBox);

                    alert.getDialogPane().setContent(vBox);

                    alert.showAndWait().ifPresent(buttonType -> {
                        if (buttonType.equals(ButtonType.OK)) {
                            try {

                                final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("jevistree.dialog.enable.title") + "...");

                                Task<Void> reload = new Task<Void>() {
                                    @Override
                                    protected Void call() {
                                        for (TreeItem<JEVisTreeRow> item : items) {
                                            CommonMethods.setEnabled(pForm, item.getValue().getJEVisObject(), jeVisClassComboBox.getSelectionModel().getSelectedItem(), b);
                                        }

                                        return null;
                                    }
                                };
                                reload.setOnSucceeded(event -> pForm.getDialogStage().close());

                                reload.setOnCancelled(event -> {
                                    logger.debug("Set enabled Cancelled");
                                    pForm.getDialogStage().hide();
                                });

                                reload.setOnFailed(event -> {
                                    logger.debug("Set enabled failed");
                                    pForm.getDialogStage().hide();
                                });

                                pForm.activateProgressBar(reload);
                                pForm.getDialogStage().show();

                                new Thread(reload).start();

                            } catch (Exception ex) {
                                logger.catching(ex);
                                CommonDialogs.showError(I18n.getInstance().getString("jevistree.dialog.delete.error.title"),
                                        I18n.getInstance().getString("jevistree.dialog.delete.error.message"), null, ex);
                            }
                        } else {
                            // ... user chose CANCEL or closed the dialog
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        Alert alert1 = new Alert(AlertType.WARNING, I18n.getInstance().getString("dialog.warning.title"));
                        TopMenu.applyActiveTheme(alert1.getDialogPane().getScene());
                        alert1.setContentText(I18n.getInstance().getString("dialog.warning.notallowed"));
                        alert1.showAndWait();
                    });

                }
            }
        } catch (JEVisException e) {
            logger.error("Could not get JEVis data source.", e);
        }
    }

    public static void EventDeleteBrokenTS(JEVisTree tree) {
        logger.debug("EventDeleteBrokenTS");
        try {
            if (!tree.getSelectionModel().getSelectedItems().isEmpty()) {
                String question = I18n.getInstance().getString("jevistree.dialog.delete.message");
                ObservableList<TreeItem<JEVisTreeRow>> items = tree.getSelectionModel().getSelectedItems();
                for (TreeItem<JEVisTreeRow> item : items) {
                    if (items.indexOf(item) > 0 && items.indexOf(item) < items.size() - 1)
                        question += item.getValue().getJEVisObject().getName();
                }
                question += "?";

                if (tree.getJEVisDataSource().getCurrentUser().canWrite(items.get(0).getValue().getJEVisObject().getID())) {

                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle(I18n.getInstance().getString("jevistree.dialog.deleteCleanAndRaw.title"));
                    alert.setHeaderText(null);
                    alert.setContentText(question);
                    TopMenu.applyActiveTheme(alert.getDialogPane().getScene());

                    alert.showAndWait().ifPresent(buttonType -> {
                        if (buttonType.equals(ButtonType.OK)) {
                            try {

                                final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("jevistree.dialog.deleteCleanAndRaw.title") + "...");

                                Task<Void> reload = new Task<Void>() {
                                    @Override
                                    protected Void call() {
                                        for (TreeItem<JEVisTreeRow> item : items) {
                                            deleteBrokenTSSamples(item.getValue().getJEVisObject());
                                        }

                                        return null;
                                    }
                                };
                                reload.setOnSucceeded(event -> pForm.getDialogStage().close());

                                reload.setOnCancelled(event -> {
                                    logger.debug("Delete all samples Cancelled");
                                    pForm.getDialogStage().hide();
                                });

                                reload.setOnFailed(event -> {
                                    logger.debug("Delete all samples failed");
                                    pForm.getDialogStage().hide();
                                });

                                pForm.activateProgressBar(reload);
                                pForm.getDialogStage().show();

                                new Thread(reload).start();

                            } catch (Exception ex) {
                                logger.catching(ex);
                                CommonDialogs.showError(I18n.getInstance().getString("jevistree.dialog.delete.error.title"),
                                        I18n.getInstance().getString("jevistree.dialog.delete.error.message"), null, ex);
                            }
                        } else {
                            // ... user chose CANCEL or closed the dialog
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        Alert alert1 = new Alert(AlertType.WARNING, I18n.getInstance().getString("dialog.warning.title"));
                        alert1.setContentText(I18n.getInstance().getString("dialog.warning.notallowed"));
                        TopMenu.applyActiveTheme(alert1.getDialogPane().getScene());
                        alert1.showAndWait();
                    });

                }
            }
        } catch (JEVisException e) {
            logger.error("Could not get JEVis data source.", e);
        }
    }

    private static void deleteBrokenTSSamples(JEVisObject object) {
        try {
            JEVisAttribute value = object.getAttribute(CleanDataObject.AttributeName.VALUE.getAttributeName());
            if (value != null) {
                if ((object.getJEVisClassName().equals("Clean Data"))
                        || (object.getJEVisClassName().equals("Data"))) {
                    List<JEVisSample> list = value.getSamples(new DateTime(2019, 8, 1, 0, 0, 0), DateTime.now());
                    logger.info("Found {} samples.", list.size());
                    List<JEVisSample> toBeDeleted = new ArrayList<>();
                    list.forEach(jeVisSample -> {
                        try {
                            if (jeVisSample.getTimestamp().getSecondOfMinute() != 0) {
                                toBeDeleted.add(jeVisSample);
                            }
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                    });
                    logger.info("{} samples have wrong timestamp.", toBeDeleted.size());

                    toBeDeleted.parallelStream().forEach(jeVisSample -> {
                        try {
                            value.deleteSamplesBetween(jeVisSample.getTimestamp(), jeVisSample.getTimestamp());
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
            for (JEVisObject child : object.getChildren()) {
                deleteBrokenTSSamples(child);
            }
        } catch (JEVisException e) {
            logger.error("Could not delete value samples for {}:{}", object.getName(), object.getID());
        }
    }

    public static void EventMoveAllToDiffCleanTS(JEVisTree tree) {
        Alert warning = new Alert(AlertType.WARNING);
        TopMenu.applyActiveTheme(warning.getDialogPane().getScene());

        TextField textField = new TextField();
        Label message = new Label("You really sure you know what you're doing? Move all data/clean data samples between start and end their period x field");

        Label dateLabelFrom = new Label(I18n.getInstance().getString("tree.treehelper.from"));
        DatePicker datePickerFrom = new DatePicker();
        LocalTimePicker timePickerFrom = new LocalTimePicker();
//        timePickerFrom.set24HourView(true);
//        timePickerFrom.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));
        AtomicBoolean changedFrom = new AtomicBoolean(false);

        datePickerFrom.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                changedFrom.set(true);
            }
        });

        timePickerFrom.localTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                changedFrom.set(true);
            }
        });

        Label dateLabelTo = new Label(I18n.getInstance().getString("tree.treehelper.to"));
        DatePicker datePickerTo = new DatePicker();
        LocalTimePicker timePickerTo = new LocalTimePicker();
//        timePickerTo.set24HourView(true);
//        timePickerTo.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));
        AtomicBoolean changedTo = new AtomicBoolean(false);

        datePickerTo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                changedTo.set(true);
            }
        });

        datePickerTo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                changedTo.set(true);
            }
        });

        CheckBox correctUTC = new CheckBox("Correct UTC diff");
        CheckBox dataObjectsCheckBox = new CheckBox("Data Objects");
        CheckBox cleanDataObjectsCheckBox = new CheckBox("Clean Data Objects");
        CheckBox onlyConversionToDiffObjectsCheckBox = new CheckBox("Only Conversion To Diff Clean Objects");
        CheckBox allObjects = new CheckBox("All Objects");
        Label tzInputLabel = new Label("Input Timezone");
        Label tzOutputLabel = new Label("Output Timezone");
        TimeZoneBox timeZoneBoxInput = new TimeZoneBox();
        TimeZoneBox timeZoneBoxOutput = new TimeZoneBox();

        HBox inputBox = new HBox(6, tzInputLabel, timeZoneBoxInput);
        HBox outputBox = new HBox(6, tzOutputLabel, timeZoneBoxOutput);


        Label periodLabel = new Label("Period count");
        HBox periodBox = new HBox(6, periodLabel, textField);

        HBox fromBox = new HBox(6, dateLabelFrom, datePickerFrom, timePickerFrom);
        HBox toBox = new HBox(6, dateLabelTo, datePickerTo, timePickerTo);

        VBox vBox = new VBox(6, message, periodBox, fromBox, toBox, correctUTC, dataObjectsCheckBox, cleanDataObjectsCheckBox, onlyConversionToDiffObjectsCheckBox, allObjects, inputBox, outputBox);
        warning.getDialogPane().setContent(vBox);

        ObservableList<TreeItem<JEVisTreeRow>> items = tree.getSelectionModel().getSelectedItems();

        warning.showAndWait().ifPresent(buttonType -> {
            if (buttonType.equals(ButtonType.OK)) {
                JEVisDataSource ds = tree.getJEVisDataSource();
                TextArea textArea = new TextArea();
                textArea.setPrefRowCount(20);
                Alert info = new Alert(AlertType.INFORMATION);
                info.getDialogPane().setContent(textArea);
                info.show();


                try {
                    JEVisClass dataClass = ds.getJEVisClass("Data");
                    JEVisClass cleanDataClass = ds.getJEVisClass("Clean Data");

                    if (!correctUTC.isSelected()) {

                        Long periodMultiplier = Long.parseLong(textField.getText());
                        Long periodIncrease = Math.abs(periodMultiplier);
                        List<JEVisObject> objectsToWorkWith = new ArrayList<>();
                        List<JEVisObject> dataObjects = CalculationMethods.getAllRawDataRec(items.get(0).getValue().getJEVisObject(), dataClass);
                        if (dataObjectsCheckBox.isSelected()) {
                            objectsToWorkWith.addAll(dataObjects);
                        }

                        List<JEVisObject> ctdObjects = new ArrayList<>();
                        for (JEVisObject dataObject : dataObjects) {
                            JEVisAttribute valueAttribute = dataObject.getAttribute("Value");
                            Platform.runLater(() -> textArea.setText(warning.getContentText() + "\n" + dataObject.getName() + ":" + dataObject.getID() + " Check"));

                            List<JEVisObject> cleanDataChildren = dataObject.getChildren(cleanDataClass, false);
                            if (cleanDataObjectsCheckBox.isSelected()) {
                                objectsToWorkWith.addAll(cleanDataChildren);
                            }

                            if (!cleanDataChildren.isEmpty() && valueAttribute != null) {
                                JEVisObject cleanDataObject = cleanDataChildren.get(0);
                                JEVisAttribute ctdAttribute = cleanDataObject.getAttribute(CleanDataObject.AttributeName.CONVERSION_DIFFERENTIAL.getAttributeName());
                                if (ctdAttribute != null) {
                                    JEVisSample latestSample = ctdAttribute.getLatestSample();
                                    if (latestSample != null && latestSample.getValueAsBoolean()) {
                                        ctdObjects.add(dataObject);
                                        Platform.runLater(() -> textArea.setText(warning.getContentText() + "\n" + dataObject.getName() + ":" + dataObject.getID() + " added to list"));
                                    }
                                }
                            }
                        }

                        if (onlyConversionToDiffObjectsCheckBox.isSelected()) {
                            objectsToWorkWith.clear();
                            objectsToWorkWith.addAll(ctdObjects);
                        }

                        final String formatStr = "yyyy-MM-dd HH:mm:ss";
                        for (JEVisObject dataObject : objectsToWorkWith) {
                            Platform.runLater(() -> textArea.setText(textArea.getText() + "\n" + dataObject.getName() + ":" + dataObject.getID() + " moving samples"));

                            JEVisAttribute value = dataObject.getAttribute("Value");
                            if (value != null) {
                                List<JEVisSample> allSamples = new ArrayList<>();

                                if (!changedFrom.get() && !changedTo.get()) {
                                    allSamples = value.getAllSamples();
                                } else if (changedFrom.get() && !changedTo.get()) {
                                    DateTime dateTimeFrom = new DateTime(
                                            datePickerFrom.valueProperty().get().getYear(),
                                            datePickerFrom.valueProperty().get().getMonthValue(),
                                            datePickerFrom.valueProperty().get().getDayOfMonth(),
                                            timePickerFrom.getLocalTime().getHour(),
                                            timePickerFrom.getLocalTime().getMinute(),
                                            timePickerFrom.getLocalTime().getSecond(), DateTimeZone.getDefault());

                                    allSamples = value.getSamples(dateTimeFrom, new DateTime(2500, 1, 1, 0, 0, 0, 0));
                                } else if (!changedFrom.get() && changedTo.get()) {
                                    DateTime dateTimeTo = new DateTime(
                                            datePickerTo.valueProperty().get().getYear(),
                                            datePickerTo.valueProperty().get().getMonthValue(),
                                            datePickerTo.valueProperty().get().getDayOfMonth(),
                                            timePickerTo.getLocalTime().getHour(),
                                            timePickerTo.getLocalTime().getMinute(),
                                            timePickerTo.getLocalTime().getSecond(), DateTimeZone.getDefault());

                                    allSamples = value.getSamples(new DateTime(1900, 1, 1, 0, 0, 0, 0), dateTimeTo);
                                } else {
                                    DateTime dateTimeFrom = new DateTime(
                                            datePickerFrom.valueProperty().get().getYear(),
                                            datePickerFrom.valueProperty().get().getMonthValue(),
                                            datePickerFrom.valueProperty().get().getDayOfMonth(),
                                            timePickerFrom.getLocalTime().getHour(),
                                            timePickerFrom.getLocalTime().getMinute(),
                                            timePickerFrom.getLocalTime().getSecond(), DateTimeZone.getDefault());

                                    DateTime dateTimeTo = new DateTime(
                                            datePickerTo.valueProperty().get().getYear(),
                                            datePickerTo.valueProperty().get().getMonthValue(),
                                            datePickerTo.valueProperty().get().getDayOfMonth(),
                                            timePickerTo.getLocalTime().getHour(),
                                            timePickerTo.getLocalTime().getMinute(),
                                            timePickerTo.getLocalTime().getSecond(), DateTimeZone.getDefault());

                                    allSamples = value.getSamples(dateTimeFrom, dateTimeTo);
                                }


                                List<JEVisSample> virtualSamples = new ArrayList<>();

                                for (JEVisSample sample : allSamples) {
                                    DateTime oldTS = sample.getTimestamp();
                                    DateTime movedTimeStamp = null;
                                    Period p = CleanDataObject.getPeriodForDate(dataObject, oldTS);

                                    if (p.equals(Period.years(1))) {
                                        if (periodMultiplier > 0)
                                            movedTimeStamp = oldTS.plusYears(periodIncrease.intValue()).withMonthOfYear(oldTS.getMonthOfYear()).withDayOfMonth(oldTS.getDayOfMonth()).withHourOfDay(oldTS.getHourOfDay()).withMinuteOfHour(oldTS.getMinuteOfHour()).withSecondOfMinute(oldTS.getSecondOfMinute()).withMillisOfSecond(oldTS.getMillisOfSecond());
                                        else if (periodMultiplier < 0)
                                            movedTimeStamp = oldTS.minusYears(periodIncrease.intValue()).withMonthOfYear(oldTS.getMonthOfYear()).withDayOfMonth(oldTS.getDayOfMonth()).withHourOfDay(oldTS.getHourOfDay()).withMinuteOfHour(oldTS.getMinuteOfHour()).withSecondOfMinute(oldTS.getSecondOfMinute()).withMillisOfSecond(oldTS.getMillisOfSecond());
                                    } else if (p.equals(Period.months(1))) {
                                        if (periodMultiplier > 0)
                                            movedTimeStamp = oldTS.plusMonths(periodIncrease.intValue()).withDayOfMonth(oldTS.getDayOfMonth()).withHourOfDay(oldTS.getHourOfDay()).withMinuteOfHour(oldTS.getMinuteOfHour()).withSecondOfMinute(oldTS.getSecondOfMinute()).withMillisOfSecond(oldTS.getMillisOfSecond());
                                        else if (periodMultiplier < 0)
                                            movedTimeStamp = oldTS.minusMonths(periodIncrease.intValue()).withDayOfMonth(oldTS.getDayOfMonth()).withHourOfDay(oldTS.getHourOfDay()).withMinuteOfHour(oldTS.getMinuteOfHour()).withSecondOfMinute(oldTS.getSecondOfMinute()).withMillisOfSecond(oldTS.getMillisOfSecond());
                                    } else {
                                        if (periodMultiplier > 0)
                                            movedTimeStamp = oldTS.plusMillis(Math.toIntExact(p.toStandardDuration().getMillis() * periodIncrease));
                                        else if (periodMultiplier < 0)
                                            movedTimeStamp = oldTS.minusMillis(Math.toIntExact(p.toStandardDuration().getMillis() * periodIncrease));
                                    }

                                    JEVisSample virtualSample = new VirtualSample(movedTimeStamp, sample.getValueAsDouble());
                                    virtualSample.setNote(sample.getNote());
                                    DateTime finalMovedTimeStamp = movedTimeStamp;
                                    Platform.runLater(() -> textArea.setText(textArea.getText() + "\n" + dataObject.getName() + ":" + dataObject.getID() + " found ts: " + oldTS.toString(formatStr) + " new ts: " + finalMovedTimeStamp.toString(formatStr)));
                                    virtualSamples.add(virtualSample);
                                }

                                List<JEVisSample> finalAllSamples = allSamples;
                                Platform.runLater(() -> textArea.setText(textArea.getText() + "\n" + dataObject.getName() + ":" + dataObject.getID() + " found " + finalAllSamples.size() + " samples, created " + virtualSamples.size() + " new samples"));

                                if (allSamples.size() == virtualSamples.size()) {
                                    if (!changedFrom.get() && !changedTo.get()) {
                                        value.deleteAllSample();
                                    } else if (changedFrom.get() && !changedTo.get()) {
                                        DateTime dateTimeFrom = new DateTime(
                                                datePickerFrom.valueProperty().get().getYear(),
                                                datePickerFrom.valueProperty().get().getMonthValue(),
                                                datePickerFrom.valueProperty().get().getDayOfMonth(),
                                                timePickerFrom.getLocalTime().getHour(),
                                                timePickerFrom.getLocalTime().getMinute(),
                                                timePickerFrom.getLocalTime().getSecond(), DateTimeZone.getDefault());

                                        value.deleteSamplesBetween(dateTimeFrom, new DateTime(2500, 1, 1, 0, 0, 0, 0));
                                    } else if (!changedFrom.get() && changedTo.get()) {
                                        DateTime dateTimeTo = new DateTime(
                                                datePickerTo.valueProperty().get().getYear(),
                                                datePickerTo.valueProperty().get().getMonthValue(),
                                                datePickerTo.valueProperty().get().getDayOfMonth(),
                                                timePickerTo.getLocalTime().getHour(),
                                                timePickerTo.getLocalTime().getMinute(),
                                                timePickerTo.getLocalTime().getSecond(), DateTimeZone.getDefault());

                                        value.deleteSamplesBetween(new DateTime(1900, 1, 1, 0, 0, 0, 0), dateTimeTo);
                                    } else {
                                        DateTime dateTimeFrom = new DateTime(
                                                datePickerFrom.valueProperty().get().getYear(),
                                                datePickerFrom.valueProperty().get().getMonthValue(),
                                                datePickerFrom.valueProperty().get().getDayOfMonth(),
                                                timePickerFrom.getLocalTime().getHour(),
                                                timePickerFrom.getLocalTime().getMinute(),
                                                timePickerFrom.getLocalTime().getSecond(), DateTimeZone.getDefault());

                                        DateTime dateTimeTo = new DateTime(
                                                datePickerTo.valueProperty().get().getYear(),
                                                datePickerTo.valueProperty().get().getMonthValue(),
                                                datePickerTo.valueProperty().get().getDayOfMonth(),
                                                timePickerTo.getLocalTime().getHour(),
                                                timePickerTo.getLocalTime().getMinute(),
                                                timePickerTo.getLocalTime().getSecond(), DateTimeZone.getDefault());

                                        value.deleteSamplesBetween(dateTimeFrom, dateTimeTo);
                                    }
                                    value.addSamples(virtualSamples);
                                    Platform.runLater(() -> textArea.setText(textArea.getText() + "\n" + dataObject.getName() + ":" + dataObject.getID() + " finished moving samples"));
                                }
                            }
                        }
                    } else {
                        logger.debug("Setting default timezone to UTC");
                        TimeZone defaultTimeZone = TimeZone.getDefault();
                        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
                        DateTimeZone defaultDateTimeZone = DateTimeZone.getDefault();
                        DateTimeZone.setDefault(DateTimeZone.UTC);

                        List<JEVisObject> allDataObjects = new ArrayList<>();
                        if (allObjects.isSelected()) {
                            allDataObjects.addAll(getAllObjectsRecursive(items.get(0).getValue().getJEVisObject()));
                        } else {
                            allDataObjects.addAll(CalculationMethods.getAllRawDataRec(items.get(0).getValue().getJEVisObject(), dataClass));
                            allDataObjects.addAll(CalculationMethods.getAllRawDataRec(items.get(0).getValue().getJEVisObject(), cleanDataClass));
                        }

                        for (JEVisObject object : allDataObjects) {
                            final String formatStr = "yyyy-MM-dd HH:mm:ss";
//                            Platform.runLater(() -> textArea.setText(warning.getContentText() + "\n" + object.getName() + ":" + object.getID() + " moving samples"));
                            logger.info(object.getName() + ":" + object.getID() + " moving samples");

                            List<JEVisAttribute> allAttributes = new ArrayList<>();
                            if (allObjects.isSelected()) {
                                allAttributes.addAll(object.getAttributes());
                            } else {
                                JEVisAttribute valueAtt = object.getAttribute("Value");
                                allAttributes.add(valueAtt);
                            }

                            for (JEVisAttribute att : allAttributes) {
                                try {

                                    int primitiveType = att.getPrimitiveType();

                                    if (primitiveType != 3 && primitiveType != 7) {
                                        List<JEVisSample> allSamples = att.getAllSamples();
                                        List<JEVisSample> newSamples = new ArrayList<>();

                                        for (JEVisSample sample : allSamples) {
                                            try {
                                                DateTime oldTS = sample.getTimestamp().withZoneRetainFields(timeZoneBoxInput.getValue());
                                                DateTime movedTimeStamp = oldTS.withZone(timeZoneBoxOutput.getValue());

                                                JEVisSample newSample = att.buildSample(movedTimeStamp, sample.getValue(), sample.getNote());

                                                DateTime finalMovedTimeStamp = movedTimeStamp;
//                                    Platform.runLater(() -> textArea.setText(warning.getContentText() + "\n" + object.getName() + ":" + object.getID() + " found ts: " + oldTS.toString(formatStr) + " new ts: " + finalMovedTimeStamp.toString(formatStr)));
                                                logger.info(object.getName() + ":" + object.getID() + " found ts: " + oldTS.toString(formatStr) + " new ts: " + finalMovedTimeStamp.toString(formatStr));
                                                newSamples.add(newSample);
                                            } catch (Exception e) {
                                                logger.error("Could not generate sampel {}", sample, e);
                                            }
                                        }

//                                Platform.runLater(() -> textArea.setText(warning.getContentText() + "\n" + object.getName() + ":" + object.getID() + " found " + allSamples.size() + " samples, created " + newSamples.size() + " new samples"));
                                        logger.info(object.getName() + ":" + object.getID() + " found " + allSamples.size() + " samples, created " + newSamples.size() + " new samples");

                                        if (allSamples.size() == newSamples.size()) {
                                            att.deleteAllSample();
                                            att.addSamples(newSamples);
//                                    Platform.runLater(() -> textArea.setText(warning.getContentText() + "\n" + object.getName() + ":" + object.getID() + " finished moving samples"));
                                            logger.info(object.getName() + ":" + object.getID() + " finished moving samples");
                                        }
                                    }
                                } catch (Exception e) {
                                    logger.error("Could not generate samples for attribute {}", att, e);
                                }
                            }
                        }

                        TimeZone.setDefault(defaultTimeZone);
                        DateTimeZone.setDefault(defaultDateTimeZone);
                    }

                } catch (JEVisException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public static List<JEVisObject> getAllObjectsRecursive(JEVisObject parent) throws JEVisException {
        List<JEVisObject> list = new ArrayList<>();

        list.add(parent);

        for (JEVisObject child : parent.getChildren()) {
            list.addAll(getAllObjectsRecursive(child));
        }
        return list;
    }
}
