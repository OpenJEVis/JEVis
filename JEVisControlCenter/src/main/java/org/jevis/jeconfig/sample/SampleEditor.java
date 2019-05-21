/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.sample;

import com.jfoenix.controls.JFXDatePicker;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.dataprocessing.SampleGenerator;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.dialog.DialogHeader;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * GUI Dialog to configure attributes and there sample.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 * @TODO: rename it to Attribute editor or something?!
 */
public class SampleEditor {
    private static final Logger logger = LogManager.getLogger(SampleEditor.class);
    public static String ICON = "1415314386_Graph.png";
    private final Button ok = new Button(I18n.getInstance().getString("attribute.editor.save"));
    private final List<SampleEditorExtension> extensions = new ArrayList<>();
    private List<JEVisSample> samples = new ArrayList<>();
    private JEVisAttribute _attribute;
    private List<JEVisObject> _dataProcessors = new ArrayList<>();
    private AggregationPeriod _period = AggregationPeriod.NONE;
    private Response response = Response.CANCEL;
    private BooleanProperty disableEditing = new SimpleBooleanProperty(false);
    private LocalTime workdayStart = LocalTime.of(0, 0, 0, 0);
    private LocalTime workdayEnd = LocalTime.of(23, 59, 59, 999999999);

    private JFXDatePicker startDate = new JFXDatePicker();
    private JFXDatePicker endDate = new JFXDatePicker();
    private int lastDataSettings = 0;

    //    private DateTime _from;
//    private DateTime _until;
    private boolean initial = true;
    private SampleEditorExtension activExtensions;

    /**
     * @param owner
     * @param attribute
     * @return
     */
    public Response show(Window owner, final JEVisAttribute attribute) {
        final Stage stage = new Stage();

        _attribute = attribute;
        try {
            _attribute.getDataSource().reloadAttribute(_attribute);
        } catch (Exception ex) {
            logger.error("Update failed", ex);
        }


        stage.setTitle(I18n.getInstance().getString("attribute.editor.title"));
        stage.initModality(Modality.NONE);
        stage.initOwner(owner);

        VBox root = new VBox();
        root.setMaxWidth(2000);

        final Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setWidth(740);
        stage.setHeight(800);
        stage.setMaxWidth(2000);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);

        Screen screen = Screen.getPrimary();
        if (screen.getBounds().getHeight() < 740) {
            stage.setWidth(screen.getBounds().getHeight());
        }

        HBox buttonPanel = new HBox();

        ok.setDefaultButton(true);

        Button cancel = new Button(I18n.getInstance().getString("attribute.editor.cancel"));
        cancel.setCancelButton(true);

        Region spacer = new Region();
        spacer.setMaxWidth(2000);

        Label startLabel = new Label(I18n.getInstance().getString("attribute.editor.from"));


        startDate.setMaxWidth(120);
        endDate.setMaxWidth(120);


        Label endLabel = new Label(I18n.getInstance().getString("attribute.editor.until"));
        if (attribute.hasSample()) {
            try {
                DateTime from = attribute.getTimestampFromLastSample().minusDays(1);
                DateTime until = attribute.getTimestampFromLastSample();

                startDate.valueProperty().set(LocalDate.of(from.getYear(), from.getMonthOfYear(), from.getDayOfMonth()));
                endDate.valueProperty().set(LocalDate.of(until.getYear(), until.getMonthOfYear(), until.getDayOfMonth()));

                WorkDays wd = new WorkDays(attribute.getObject());
                if (wd.getWorkdayStart() != null) workdayStart = wd.getWorkdayStart();
                if (wd.getWorkdayEnd() != null) workdayEnd = wd.getWorkdayEnd();
            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        Node preClean = buildProcessorBox(attribute.getObject());

        Label timeRangeL = new Label(I18n.getInstance().getString("attribute.editor.timerange"));
        timeRangeL.setStyle("-fx-font-weight: bold");
        GridPane timeSpan = new GridPane();
        timeSpan.setHgap(5);
        timeSpan.setVgap(2);
        timeSpan.add(timeRangeL, 0, 0, 2, 1); // column=1 row=0
        timeSpan.add(startLabel, 0, 1, 1, 1); // column=1 row=0
        timeSpan.add(endLabel, 0, 2, 1, 1); // column=1 row=0

        timeSpan.add(startDate, 1, 1, 1, 1); // column=1 row=0
        timeSpan.add(endDate, 1, 2, 1, 1); // column=1 row=0

        buttonPanel.getChildren().addAll(timeSpan, preClean, spacer, ok, cancel);
        buttonPanel.setAlignment(Pos.BOTTOM_RIGHT);
        buttonPanel.setPadding(new Insets(10, 10, 10, 10));
        buttonPanel.setSpacing(15);//10
        buttonPanel.setMaxHeight(25);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setHgrow(ok, Priority.NEVER);
        HBox.setHgrow(cancel, Priority.NEVER);

        SampleTableExtension sampleTableExtension = new SampleTableExtension(attribute, stage);
        extensions.add(sampleTableExtension);
        activExtensions = sampleTableExtension;

        /** graph makes only if the data are numbers **/
        try {
            if (attribute.getPrimitiveType() == JEVisConstants.PrimitiveType.LONG || attribute.getPrimitiveType() == JEVisConstants.PrimitiveType.DOUBLE) {
                extensions.add(new SampleGraphExtension(attribute));
            }
        } catch (Exception ex) {
            logger.error(ex);
        }

        extensions.add(new AttributeStatesExtension(attribute));
        extensions.add(new SampleExportExtension(attribute));
        extensions.add(new AttributeUnitExtension(attribute));

        final List<Tab> tabs = new ArrayList<>();

        for (SampleEditorExtension ex : extensions) {
            Tab tabEditor = new Tab();
            tabEditor.setText(ex.getTitle());
            tabEditor.setContent(ex.getView());
            tabs.add(tabEditor);
        }

        disableEditing.addListener((observable, oldValue, newValue) -> {
            extensions.forEach(sampleEditorExtension -> {
                logger.info("Diabled editing in: " + sampleEditorExtension.getTitle());
                sampleEditorExtension.disableEditing(newValue);
            });
        });


        final TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(tabs);


        Node header = DialogHeader.getDialogHeader(ICON, I18n.getInstance().getString("attribute.editor.title"));//new Separator(Orientation.HORIZONTAL),

        root.getChildren().addAll(header, tabPane, new Separator(Orientation.HORIZONTAL), buttonPanel);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        VBox.setVgrow(buttonPanel, Priority.NEVER);
        VBox.setVgrow(header, Priority.NEVER);

        ok.setOnAction(t -> {
            stage.close();
            for (SampleEditorExtension ex : extensions) {
                ex.sendOKAction();
            }
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener((ov, t, t1) -> {
            for (SampleEditorExtension ex : extensions) {
                if (ex.getTitle().equals(t1.getText())) {
                    System.out.println("Tab chaned: " + ex.getClass());
                    activExtensions = ex;
                    ex.update();
                }
            }
        });


        if (attribute.hasSample()) {
            DateTime lastSample = attribute.getTimestampFromLastSample();
            endDate.setValue(LocalDate.of(lastSample.getYear(), lastSample.getMonthOfYear(), lastSample.getDayOfMonth()));
            DateTime startDateTMP = lastSample.minusDays(1);
            startDate.setValue(LocalDate.of(startDateTMP.getYear(), startDateTMP.getMonthOfYear(), startDateTMP.getDayOfMonth()));
        }


        startDate.valueProperty().addListener((observable, oldValue, newValue) -> {
//            _from = new DateTime(newValue.getYear(), newValue.getMonth().getValue(), newValue.getDayOfMonth(), 0, 0);
//            updateSamples(startDate, _until);
            updateSamples(startDate, endDate);
        });
        endDate.valueProperty().addListener((observable, oldValue, newValue) -> {
//            _until = new DateTime(newValue.getYear(), newValue.getMonth().getValue(), newValue.getDayOfMonth(), 23, 59, 59, 999);
//            updateSamples(_from, _until);
            updateSamples(startDate, endDate);
        });

        cancel.setOnAction(t -> {
            stage.close();
            response = Response.CANCEL;
            stage.close();

        });


        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                updateSamples(startDate, endDate);
            }
        });

        stage.showAndWait();

        return response;
    }

    private void disableEditing(boolean disable) {
        disableEditing.setValue(true);
    }

    private Node buildProcessorBox(final JEVisObject parentObj) {
        List<String> proNames = new ArrayList<>();
        proNames.add("Raw Data");

        try {
            JEVisClass dpClass = parentObj.getDataSource().getJEVisClass("Data Processor");

            if (dpClass != null) {
                _dataProcessors = parentObj.getChildren(dpClass, true);
                if (_dataProcessors != null) {

                    for (JEVisObject configObject : _dataProcessors) {
                        proNames.add(configObject.getName());
                    }
                }
            }

        } catch (Exception ex) {
            logger.error(ex);
        }

        ChoiceBox<String> processorBox = new ChoiceBox<>();
        processorBox.setItems(FXCollections.observableArrayList(proNames));
        processorBox.getSelectionModel().selectFirst();
        processorBox.valueProperty().addListener((observable, oldValue, newValue) -> {

            try {
                if (newValue.equals("None")) {
                    disableEditing.setValue(true);
                    updateSamples(startDate, endDate);
                } else {
                    for (JEVisObject configObject : _dataProcessors) {
                        if (configObject.getName().equals(newValue)) {
                            updateSamples(startDate, endDate);
                        }
                    }
                }

            } catch (Exception ex) {
                logger.fatal(ex);
            }
        });

        List<String> aggList = new ArrayList<>();
        aggList.add(I18n.getInstance().getString("plugin.object.attribute.sampleeditor.aggregationPeriod.none"));
        aggList.add(I18n.getInstance().getString("plugin.object.attribute.sampleeditor.aggregationPeriod.hourly"));
        aggList.add(I18n.getInstance().getString("plugin.object.attribute.sampleeditor.aggregationPeriod.daily"));
        aggList.add(I18n.getInstance().getString("plugin.object.attribute.sampleeditor.aggregationPeriod.weekly"));
        aggList.add(I18n.getInstance().getString("plugin.object.attribute.sampleeditor.aggregationPeriod.monthly"));
        aggList.add(I18n.getInstance().getString("plugin.object.attribute.sampleeditor.aggregationPeriod.quarterly"));
        aggList.add(I18n.getInstance().getString("plugin.object.attribute.sampleeditor.aggregationPeriod.yearly"));

        ChoiceBox<String> aggregate = new ChoiceBox<String>();
        aggregate.setItems(FXCollections.observableArrayList(aggList));
        aggregate.getSelectionModel().selectFirst();
        aggregate.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !Objects.equals(newValue, oldValue)) {
                disableEditing.setValue(true);

                switch (newValue.intValue()) {
                    case 0:
                        _period = AggregationPeriod.NONE;
                        logger.info("Processor disable");
                        disableEditing.setValue(false);/** only original DB data can be edited **/
                        break;
                    case 1:
                        _period = AggregationPeriod.HOURLY;
                        break;
                    case 2:
                        _period = AggregationPeriod.DAILY;
                        break;
                    case 3:
                        _period = AggregationPeriod.WEEKLY;
                        break;
                    case 4:
                        _period = AggregationPeriod.MONTHLY;
                        break;
                    case 5:
                        _period = AggregationPeriod.QUARTERLY;
                        break;
                    case 6:
                        _period = AggregationPeriod.YEARLY;
                        break;
                }
                updateSamples(startDate, endDate);
            }
        });

        processorBox.setMinWidth(150);
        aggregate.setMinWidth(150);

        HBox hbox = new HBox(2);

        Label header = new Label(I18n.getInstance().getString("attribute.editor.dataprocessing"));
        header.setStyle("-fx-font-weight: bold");
        Label settingL = new Label(I18n.getInstance().getString("attribute.editor.setting"));
        Label aggregation = new Label(I18n.getInstance().getString("attribute.editor.aggregate"));

        Button config = new Button();
        config.setGraphic(JEConfig.getImage("Service Manager.png", 16, 16));

        hbox.getChildren().addAll(processorBox);//, config);

        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(2);
        grid.add(header, 0, 0, 2, 1); // column=1 row=0
        grid.add(aggregation, 0, 2, 1, 1); // column=1 row=0
        grid.add(aggregate, 1, 2, 1, 1); // column=1 row=0

        return grid;
    }

    private void updateSamples(JFXDatePicker startDate, JFXDatePicker endDate) {

        updateSamples(new DateTime(startDate.getValue().getYear(), startDate.getValue().getMonth().getValue(), startDate.getValue().getDayOfMonth(), 0, 0)
                , new DateTime(endDate.getValue().getYear(), endDate.getValue().getMonth().getValue(), endDate.getValue().getDayOfMonth(), 23, 59, 59, 999));
    }

    private int getLastDataSettings(final DateTime from, final DateTime until, AggregationPeriod period) {
        return from.hashCode() * until.hashCode() * period.hashCode();
    }

    /**
     * @param from
     * @param until
     */
    private void updateSamples(final DateTime from, final DateTime until) {

        try {

            if (lastDataSettings != getLastDataSettings(from, until, _period)) {
                DateTime _from = new DateTime(from.getYear(), from.getMonthOfYear(), from.getDayOfMonth(),
                        workdayStart.getHour(), workdayStart.getMinute(), workdayStart.getSecond(), workdayStart.getNano());
                DateTime _until = new DateTime(until.getYear(), until.getMonthOfYear(), until.getDayOfMonth(),
                        workdayEnd.getHour(), workdayEnd.getMinute(), workdayEnd.getSecond(), workdayEnd.getNano() / 1000000);

                if (workdayStart.isAfter(workdayEnd)) {
                    _from = _from.minusDays(1);
                }


                SampleGenerator sg;
                if (_period.equals(AggregationPeriod.NONE))
                    sg = new SampleGenerator(_attribute.getDataSource(), _attribute.getObject(), _attribute, _from, _until, ManipulationMode.NONE, _period);
                else
                    sg = new SampleGenerator(_attribute.getDataSource(), _attribute.getObject(), _attribute, _from, _until, ManipulationMode.TOTAL, _period);

                samples = sg.generateSamples();
                samples = sg.getAggregatedSamples(samples);

                for (SampleEditorExtension extension : extensions) {
                    Platform.runLater(() -> {
                        try {
                            extension.setSamples(_attribute, samples);
//                        extension.update();
                        } catch (Exception excp) {
                            logger.error(extension);
                        }
                    });

                }
            }

            if (activExtensions != null) {
                activExtensions.update();
            }

            lastDataSettings = getLastDataSettings(from, until, _period);

        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
    }


    public enum Response {

        YES, CANCEL
    }
}
