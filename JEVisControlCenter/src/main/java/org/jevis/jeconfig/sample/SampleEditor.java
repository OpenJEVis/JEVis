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
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.dataprocessing.SampleGenerator;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.AggregationBox;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.ProcessorBox;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.TimeZoneBox;
import org.jevis.jeconfig.dialog.DialogHeader;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

    private JFXDatePicker startDatePicker = new JFXDatePicker();
    private JFXDatePicker endDatePicker = new JFXDatePicker();
    private int lastDataSettings = 0;

    //    private DateTime _from;
//    private DateTime _until;
    private boolean initial = true;
    private SampleEditorExtension activExtensions;
    private DateTimeZone dateTimeZone = DateTimeZone.getDefault();

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
        stage.setWidth(780);
        stage.setHeight( ScreenSize.fitScreenHeight(800));
        stage.setMaxWidth(2000);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);

        ok.setDefaultButton(true);

        Button cancel = new Button(I18n.getInstance().getString("attribute.editor.cancel"));
        cancel.setCancelButton(true);

        Region spacer = new Region();
        spacer.setMaxWidth(2000);

        Label startLabel = new Label(I18n.getInstance().getString("attribute.editor.from"));


        startDatePicker.setMaxWidth(120);
        endDatePicker.setMaxWidth(120);


        Label endLabel = new Label(I18n.getInstance().getString("attribute.editor.until"));
        if (attribute.hasSample()) {
            try {
                DateTime from = attribute.getTimestampFromLastSample().minusDays(1);
                DateTime until = attribute.getTimestampFromLastSample().plusDays(1);

                startDatePicker.valueProperty().set(LocalDate.of(from.getYear(), from.getMonthOfYear(), from.getDayOfMonth()));
                endDatePicker.valueProperty().set(LocalDate.of(until.getYear(), until.getMonthOfYear(), until.getDayOfMonth()));

                WorkDays wd = new WorkDays(attribute.getObject());
                if (wd.getWorkdayStart() != null) workdayStart = wd.getWorkdayStart();
                if (wd.getWorkdayEnd() != null) workdayEnd = wd.getWorkdayEnd();
            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        //Node preClean = buildProcessorBox();

        ProcessorBox processorBox = new ProcessorBox(_attribute.getObject(), null);
        processorBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                _attribute = newValue.getAttribute("Value");
                updateSamples(startDatePicker, endDatePicker);
            } catch (Exception ex) {
                logger.fatal(ex);
            }
        });

        AggregationBox aggregationBox = new AggregationBox(AggregationPeriod.NONE);
        aggregationBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            _period = newValue;
            updateSamples(startDatePicker, endDatePicker);
        });

        processorBox.setMinWidth(150);
        aggregationBox.setMinWidth(150);

        Label dataProcessorHeader = new Label(I18n.getInstance().getString("attribute.editor.dataprocessing"));
        dataProcessorHeader.setStyle("-fx-font-weight: bold");
        Label dataProcessorLable = new Label(I18n.getInstance().getString("attribute.editor.processor"));
        Label aggregationLabel = new Label(I18n.getInstance().getString("attribute.editor.aggregate"));

        Button config = new Button();
        config.setGraphic(JEConfig.getImage("Service Manager.png", 16, 16));



        Label timeZoneLabel = new Label("Timezone");
        timeZoneLabel.setStyle("-fx-font-weight: bold");
        TimeZoneBox timeZoneBox = new TimeZoneBox();
//        GridPane bottomBox = new GridPane();
//        bottomBox.setHgap(5);
//        bottomBox.setVgap(2);
//        bottomBox.add(timeZoneLabel, 0, 0, 2, 1);
//        bottomBox.add(timeZoneBox, 0, 1, 1, 1);
//        bottomBox.add(new Region(), 0, 2, 1, 1); // column=1 row=0

//        bottomBox.setPadding(new Insets(10));
        timeZoneBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            dateTimeZone = newValue;
            updateSamples(startDatePicker, endDatePicker);
        });

        Label timeRangeL = new Label(I18n.getInstance().getString("attribute.editor.timerange"));
        timeRangeL.setStyle("-fx-font-weight: bold");
//        GridPane timeSpan = new GridPane();
//        timeSpan.setHgap(5);
//        timeSpan.setVgap(2);
//        timeSpan.setMaxWidth(250d);
//        timeSpan.add(timeRangeL, 0, 0, 2, 1); // column=1 row=0
//        timeSpan.add(startLabel, 0, 1, 1, 1); // column=1 row=0
//        timeSpan.add(endLabel, */0, 2, 1, 1); // column=1 row=0

//        grid.add(header, 0, 0, 2, 1); // column=1 row=0
//        grid.add(aggregation, 0, 2, 1, 1); // column=1 row=0
//        grid.add(aggregationBox, 1, 2, 1, 1); // column=1 row=0
//        grid.add(dataProcessor, 0, 3, 1, 1);
//        grid.add(processorBox, 1, 3, 1, 1);
//        grid.setMaxWidth(250d);
        GridPane.setHgrow(aggregationBox,Priority.ALWAYS);
        GridPane.setHgrow(processorBox,Priority.ALWAYS);
//
//        timeSpan.add(startDatePicker, 1, 1, 1, 1); // column=1 row=0
//        timeSpan.add(endDatePicker, 1, 2, 1, 1); // column=1 row=0

        HBox buttonBox = new HBox(15,spacer, ok, cancel);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(spacer,Priority.ALWAYS);

        aggregationBox.setMinWidth(150);
        dataProcessorLable.setMinWidth(150);
        timeZoneBox.setMaxWidth(200);

        GridPane buttonGridPane= new GridPane();
        buttonGridPane.setPadding(new Insets(10));
        buttonGridPane.setHgap(15);
        buttonGridPane.setVgap(5);

        buttonGridPane.add(timeRangeL,0,0,2,1);
        buttonGridPane.add(dataProcessorHeader,2,0,2,1);
        buttonGridPane.add(timeZoneLabel,4,0,2,1);

        buttonGridPane.addRow(1,startLabel, startDatePicker,aggregationLabel,aggregationBox,timeZoneBox);
        buttonGridPane.addRow(2,endLabel, endDatePicker,dataProcessorLable,processorBox);
        buttonGridPane.add(buttonBox,0,3,6,1);

//        buttonGridPane.addColumn(0,header,aggregationLabel,aggregationBox);
//        buttonGridPane.addColumn(0,new Region(),dataProcessorLable,processorBox);
//        buttonGridPane.addColumn(1,preClean);
//        buttonGridPane.addColumn(2,bottomBox);
//        buttonGridPane.add(buttonBox,0,1,3,1);

       //HBox buttonPanel = new HBox();

       // buttonPanel.getChildren().addAll(timeSpan, preClean, spacer, ok, cancel);
        //buttonPanel.setAlignment(Pos.BOTTOM_RIGHT);
       // buttonPanel.setPadding(new Insets(10));
        //buttonPanel.setSpacing(15);//10
        //buttonPanel.setMaxHeight(25);
        GridPane.setHgrow(buttonBox,Priority.ALWAYS);
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
                logger.info("Disabled editing in: " + sampleEditorExtension.getTitle());
                sampleEditorExtension.disableEditing(newValue);
            });
        });

        final TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(tabs);

        Node header = DialogHeader.getDialogHeader(ICON, I18n.getInstance().getString("attribute.editor.title"));//new Separator(Orientation.HORIZONTAL),

        root.getChildren().addAll(header, tabPane, new Separator(Orientation.HORIZONTAL), buttonPanel, bottomBox);
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
                    logger.info("Tab changed: " + ex.getClass());
                    activExtensions = ex;
                    ex.update();
                }
            }
        });

        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
//            _from = new DateTime(newValue.getYear(), newValue.getMonth().getValue(), newValue.getDayOfMonth(), 0, 0);
//            updateSamples(startDate, _until);
            updateSamples(startDatePicker, endDatePicker);
        });
        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
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
                if (startDatePicker != null && endDatePicker != null) {
                    updateSamples(startDatePicker, endDatePicker);
                }
            }
        });

        stage.showAndWait();

        return response;
    }

    private void disableEditing(boolean disable) {
        disableEditing.setValue(true);
    }

    private Node buildProcessorBox() {

        ProcessorBox processorBox = new ProcessorBox(_attribute.getObject(), null);
        processorBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                _attribute = newValue.getAttribute("Value");
                updateSamples(startDatePicker, endDatePicker);
            } catch (Exception ex) {
                logger.fatal(ex);
            }
        });

        AggregationBox aggregationBox = new AggregationBox(AggregationPeriod.NONE);
        aggregationBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            _period = newValue;
            updateSamples(startDatePicker, endDatePicker);
        });

        processorBox.setMinWidth(150);
        aggregationBox.setMinWidth(150);

        Label header = new Label(I18n.getInstance().getString("attribute.editor.dataprocessing"));
        header.setStyle("-fx-font-weight: bold");
        Label dataProcessor = new Label(I18n.getInstance().getString("attribute.editor.processor"));
        Label aggregation = new Label(I18n.getInstance().getString("attribute.editor.aggregate"));

        Button config = new Button();
        config.setGraphic(JEConfig.getImage("Service Manager.png", 16, 16));

        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(2);
        grid.add(header, 0, 0, 2, 1); // column=1 row=0
        grid.add(aggregation, 0, 2, 1, 1); // column=1 row=0
        grid.add(aggregationBox, 1, 2, 1, 1); // column=1 row=0
        grid.add(dataProcessor, 0, 3, 1, 1);
        grid.add(processorBox, 1, 3, 1, 1);
        grid.setMaxWidth(250d);
        GridPane.setHgrow(aggregationBox,Priority.ALWAYS);
        GridPane.setHgrow(processorBox,Priority.ALWAYS);

        return grid;
    }

    private void updateSamples(JFXDatePicker startDate, JFXDatePicker endDate) {

        if (startDate != null && endDate != null && startDate.getValue() != null && endDate.getValue() != null) {
            updateSamples(
                    new DateTime(startDate.getValue().getYear(), startDate.getValue().getMonth().getValue(), startDate.getValue().getDayOfMonth(),
                            0, 0).withZone(dateTimeZone),
                    new DateTime(endDate.getValue().getYear(), endDate.getValue().getMonth().getValue(), endDate.getValue().getDayOfMonth(),
                            23, 59, 59, 999).withZone(dateTimeZone));
        }
    }

    private int getLastDataSettings(final JEVisAttribute _attribute, final DateTimeZone dateTimeZone, final DateTime from, final DateTime until, AggregationPeriod period) {
        return _attribute.hashCode() * dateTimeZone.hashCode() * from.hashCode() * until.hashCode() * period.hashCode();
    }

    /**
     * @param from
     * @param until
     */
    private void updateSamples(final DateTime from, final DateTime until) {

        try {

            if (lastDataSettings != getLastDataSettings(_attribute, dateTimeZone, from, until, _period)) {
                DateTime _from = new DateTime(from.getYear(), from.getMonthOfYear(), from.getDayOfMonth(),
                        workdayStart.getHour(), workdayStart.getMinute(), workdayStart.getSecond(), workdayStart.getNano());
                DateTime _until = new DateTime(until.getYear(), until.getMonthOfYear(), until.getDayOfMonth(),
                        workdayEnd.getHour(), workdayEnd.getMinute(), workdayEnd.getSecond(), workdayEnd.getNano() / 1000000);

                if (workdayStart.isAfter(workdayEnd)) {
                    _from = _from.minusDays(1);
                }

                SampleGenerator sg = new SampleGenerator(_attribute.getDataSource(), _attribute.getObject(), _attribute, _from, _until, ManipulationMode.NONE, _period);

                samples = sg.getAggregatedSamples();

                for (SampleEditorExtension extension : extensions) {
                    Platform.runLater(() -> {
                        try {
                            extension.setDateTimeZone(dateTimeZone);
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

            lastDataSettings = getLastDataSettings(_attribute, dateTimeZone, from, until, _period);

        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
    }


    public enum Response {

        YES, CANCEL
    }
}
