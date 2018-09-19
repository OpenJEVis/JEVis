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

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jevis.api.*;
import org.jevis.application.dialog.DialogHeader;
import org.jevis.commons.dataprocessing.*;
import org.jevis.commons.dataprocessing.Process;
import org.jevis.commons.dataprocessing.function.AggrigatorFunction;
import org.jevis.commons.dataprocessing.function.InputFunction;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.datepicker.DatePicker;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GUI Dialog to configure attributes and there sample.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 * @TODO: rename it to Attribute editor or something?!
 */
public class SampleEditor {

    public static String ICON = "1415314386_Graph.png";
    private boolean _dataChanged = false;
    private SampleEditorExtension _visibleExtension = null;
    private DateTime _from = null;
    private DateTime _until = null;
    private final List<SampleEditorExtension> extensions = new ArrayList<>();
    private JEVisAttribute _attribute;
    private Process _dataProcessor;
    private List<JEVisObject> _dataProcessors = new ArrayList<>();

    /**
     * @param owner
     * @param attribute
     * @return
     */
    public Response show(Stage owner, final JEVisAttribute attribute) {
        final Stage stage = new Stage();

        _attribute = attribute;
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

//        Button export = new Button("Export");
        Button cancel = new Button(I18n.getInstance().getString("attribute.editor.cancel"));
        cancel.setCancelButton(true);

        Region spacer = new Region();
        spacer.setMaxWidth(2000);

        Label startLabel = new Label(I18n.getInstance().getString("attribute.editor.from"));
        DatePicker startdate = new DatePicker();

        startdate.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
//        startdate.getCalendarView().todayButtonTextProperty().set("Today");
        startdate.getCalendarView().setShowWeeks(false);
        startdate.getStylesheets().add(JEConfig.getResource("DatePicker.css"));

        Label endLabel = new Label(I18n.getInstance().getString("attribute.editor.until"));
        DatePicker enddate = new DatePicker();

        enddate.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        enddate.getCalendarView().todayButtonTextProperty().set("Today");
        enddate.getCalendarView().setShowWeeks(true);
        enddate.getStylesheets().add(JEConfig.getResource("DatePicker.css"));

//        SampleTableExtension tabelExtension = new SampleTableExtension(attribute, stage);//Default plugin

//        final List<JEVisSample> samples = attribute.getAllSamples();
        if (attribute.hasSample()) {
            _from = attribute.getTimestampFromLastSample().minus(Duration.standardDays(1));
            _until = attribute.getTimestampFromLastSample();

            startdate = new DatePicker(Locale.getDefault(), _from.toDate());
            startdate.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
            startdate.setSelectedDate(_from.toDate());
            startdate.getCalendarView().setShowWeeks(false);
            startdate.getStylesheets().add(JEConfig.getResource("DatePicker.css"));
            startdate.setMaxWidth(100d);

//            enddate.setSelectedDate(_until.toDate());
            enddate.selectedDateProperty().setValue(_until.toDate());
            enddate.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
            enddate.setSelectedDate(_until.toDate());
            enddate.getCalendarView().setShowWeeks(true);
            enddate.getStylesheets().add(JEConfig.getResource("DatePicker.css"));
            enddate.setMaxWidth(100d);

        }

        Node preclean = buildProcessorBox(attribute.getObject());

        Label timeRangeL = new Label(I18n.getInstance().getString("attribute.editor.timerange"));
        timeRangeL.setStyle("-fx-font-weight: bold");
        GridPane timeSpan = new GridPane();
        timeSpan.setHgap(5);
        timeSpan.setVgap(2);
        timeSpan.add(timeRangeL, 0, 0, 2, 1); // column=1 row=0
        timeSpan.add(startLabel, 0, 1, 1, 1); // column=1 row=0
        timeSpan.add(endLabel, 0, 2, 1, 1); // column=1 row=0

        timeSpan.add(startdate, 1, 1, 1, 1); // column=1 row=0
        timeSpan.add(enddate, 1, 2, 1, 1); // column=1 row=0

        buttonPanel.getChildren().addAll(timeSpan, preclean, spacer, ok, cancel);
        buttonPanel.setAlignment(Pos.BOTTOM_RIGHT);
        buttonPanel.setPadding(new Insets(10, 10, 10, 10));
        buttonPanel.setSpacing(15);//10
        buttonPanel.setMaxHeight(25);
        HBox.setHgrow(spacer, Priority.ALWAYS);
//        HBox.setHgrow(export, Priority.NEVER);
        HBox.setHgrow(ok, Priority.NEVER);
        HBox.setHgrow(cancel, Priority.NEVER);

        extensions.add(new SampleTableExtension(attribute, stage));
        extensions.add(new SampleGraphExtension(attribute)); // we now habe an graph plugin
        extensions.add(new AttributeStatesExtension(attribute));
        extensions.add(new SampleExportExtension(attribute));
        extensions.add(new AttributeUnitExtention(attribute));

        final List<Tab> tabs = new ArrayList<>();

//        boolean fistEx = true;
        for (SampleEditorExtension ex : extensions) {
//            _dataChanged
//            if (fistEx) {
//                System.out.println("is first");
//                ex.setSamples(attribute, samples);
//                ex.update();
//                fistEx = false;
//            }

            Tab tabEditor = new Tab();
            tabEditor.setText(ex.getTitel());
            tabEditor.setContent(ex.getView());
            tabs.add(tabEditor);

        }
        _visibleExtension = extensions.get(0);
        updateSamples(attribute, _from, _until, extensions);

        final TabPane tabPane = new TabPane();
//        tabPane.setMaxWidth(2000);
//        tabPane.setMaxHeight(2000);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(tabs);

//        tabPane.setPrefSize(200, 200);
//        tabPane.getSelectionModel().selectFirst();
        GridPane gp = new GridPane();
        gp.setStyle("-fx-background-color: white;");

        gp.setHgap(0);
        gp.setVgap(0);
        int y = 0;
        gp.add(tabPane, 0, y);

        Node header = DialogHeader.getDialogHeader(ICON, I18n.getInstance().getString("attribute.editor.title"));//new Separator(Orientation.HORIZONTAL),

        root.getChildren().addAll(header, gp, new Separator(Orientation.HORIZONTAL), buttonPanel);
        VBox.setVgrow(buttonPanel, Priority.NEVER);
        VBox.setVgrow(header, Priority.NEVER);

//        ok.setDisable(true);
        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
//                _visibleExtension.sendOKAction();//TODO: send all?
                stage.close();
                for (SampleEditorExtension ex : extensions) {

                    ex.sendOKAction();
                }
            }
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {

            @Override
            public void changed(ObservableValue<? extends Tab> ov, Tab t, Tab t1) {
//                System.out.println("tabPane.getSelectionModel(): " + t1.getText());

                for (SampleEditorExtension ex : extensions) {
                    if (ex.getTitel().equals(t1.getText())) {
                        ex.update();
                        _visibleExtension = ex;
                    }
                }
//                }
            }
        });

        startdate.selectedDateProperty().addListener(new ChangeListener<Date>() {

            @Override
            public void changed(ObservableValue<? extends Date> ov, Date t, Date t1) {
                DateTime from = new DateTime(t1.getTime());
                _from = from;
//                _visibleExtension.setSamples(attribute, attribute.getSamples(_from, _until));
                updateSamples(attribute, _from, _until, extensions);
            }
        });

        enddate.selectedDateProperty().addListener(new ChangeListener<Date>() {

            @Override
            public void changed(ObservableValue<? extends Date> ov, Date t, Date t1) {
                DateTime until = new DateTime(t1.getTime());
                _until = until;
//                _visibleExtension.setSamples(attribute, attribute.getSamples(_from, _until));
                updateSamples(attribute, _from, _until, extensions);
            }
        });

        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                stage.close();
                response = Response.CANCEL;
                stage.close();

            }
        });

        //TODO: replace Workaround.., without it the first tab will be empty
//        tabPane.getSelectionModel().selectLast();
//        tabPane.getSelectionModel().selectFirst();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                tabPane.getSelectionModel().selectLast();
                tabPane.getSelectionModel().selectFirst();
            }
        });

        stage.showAndWait();

        return response;
    }

    private AGGREGATION _mode = AGGREGATION.None;

    //    final Label passL = new Label("New Password:");
//    final Label confirmL = new Label("Comfirm Password:");
//    final PasswordField pass = new PasswordField();
//    final PasswordField comfirm = new PasswordField();
    final Button ok = new Button(I18n.getInstance().getString("attribute.editor.save"));

    List<JEVisSample> samples = new ArrayList<>();

    private Response response = Response.CANCEL;

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
            Logger.getLogger(SampleTableExtension.class.getName()).log(Level.SEVERE, null, ex);
        }

        ChoiceBox processorBox = new ChoiceBox();
        processorBox.setItems(FXCollections.observableArrayList(proNames));
        processorBox.getSelectionModel().selectFirst();
        processorBox.valueProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                //TODO:replace this quick and dirty workaround

                try {
//                    JEVisClass dpClass = parentObj.getDataSource().getJEVisClass("Data Processor");

                    if (newValue.equals("None")) {
                        _dataProcessor = null;
                        update();
                    } else {

                        //TODO going by name is not the fine art, replace!
                        for (JEVisObject configObject : _dataProcessors) {
                            if (configObject.getName().equals(newValue)) {
                                _dataProcessor = ProcessChains.getProcessChain(configObject);

                                update();
                            }

                        }
                    }

                } catch (JEVisException ex) {
                    Logger.getLogger(SampleTableExtension.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        List<String> aggList = new ArrayList<>();
        aggList.add(I18n.getInstance().getString("plugin.object.attribute.sampleeditor.aggregation.none"));
        aggList.add(I18n.getInstance().getString("plugin.object.attribute.sampleeditor.aggregation.hourly"));
        aggList.add(I18n.getInstance().getString("plugin.object.attribute.sampleeditor.aggregation.daily"));
        aggList.add(I18n.getInstance().getString("plugin.object.attribute.sampleeditor.aggregation.weekly"));
        aggList.add(I18n.getInstance().getString("plugin.object.attribute.sampleeditor.aggregation.monthly"));
        aggList.add(I18n.getInstance().getString("plugin.object.attribute.sampleeditor.aggregation.yearly"));

        ChoiceBox aggrigate = new ChoiceBox();
        aggrigate.setItems(FXCollections.observableArrayList(aggList));
        aggrigate.getSelectionModel().selectFirst();
        aggrigate.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                switch (newValue.intValue()) {
                    case 0:
                        _mode = AGGREGATION.None;
                        break;
                    case 1:
                        _mode = AGGREGATION.Hourly;
                        break;
                    case 2:
                        _mode = AGGREGATION.Daily;
                        break;
                    case 3:
                        _mode = AGGREGATION.Weekly;
                        break;
                    case 4:
                        _mode = AGGREGATION.Monthly;
                        break;
                    case 5:
                        _mode = AGGREGATION.Yearly;
                        break;
                }
                update();
            }
        });

        processorBox.setMinWidth(150);
        aggrigate.setMinWidth(150);
//        aggrigate.prefWidthProperty().bind(processorBox.prefWidthProperty());
//        aggrigate.prefHeightProperty()
//        Bindings.add(aggrigate.prefWidthProperty(), processorBox.prefWidthProperty());

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

//        grid.add(settingL, 0, 1, 1, 1); // column=1 row=0
        grid.add(aggregation, 0, 2, 1, 1); // column=1 row=0

//        grid.add(hbox, 1, 1, 1, 1); // column=1 row=0
        grid.add(aggrigate, 1, 2, 1, 1); // column=1 row=0

        return grid;
    }

    /**
     * @param att
     * @param from
     * @param until
     * @param extensions
     */
    private void updateSamples(final JEVisAttribute att, final DateTime from, final DateTime until, List<SampleEditorExtension> extensions) {
        try {
            samples.clear();

            _from = from;
            _until = until;

            if (_dataProcessor != null) {
                ProcessOptions.setStartEnd(_dataProcessor, _from, _until, true, true);
                _dataProcessor.restResult();
            }

            Process aggrigate = null;
            if (_mode == AGGREGATION.None) {

            } else if (_mode == AGGREGATION.Hourly) {
                aggrigate = new BasicProcess();
                aggrigate.setJEVisDataSource(att.getDataSource());
                aggrigate.setID("Dynamic");
                aggrigate.setFunction(new AggrigatorFunction());

                aggrigate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.hours(1).toString()));
            } else if (_mode == AGGREGATION.Daily) {
                aggrigate = new BasicProcess();
                aggrigate.setJEVisDataSource(att.getDataSource());
                aggrigate.setID("Dynamic");
                aggrigate.setFunction(new AggrigatorFunction());

                aggrigate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.days(1).toString()));
            } else if (_mode == AGGREGATION.Monthly) {
                aggrigate = new BasicProcess();
                aggrigate.setJEVisDataSource(att.getDataSource());
                aggrigate.setID("Dynamic");
                aggrigate.setFunction(new AggrigatorFunction());
                aggrigate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.months(1).toString()));
//                aggrigate.addOption(Options.PERIOD, Period.months(1).toString());
            } else if (_mode == AGGREGATION.Weekly) {
                aggrigate = new BasicProcess();
                aggrigate.setJEVisDataSource(att.getDataSource());
                aggrigate.setID("Dynamic");
                aggrigate.setFunction(new AggrigatorFunction());
                aggrigate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.weeks(1).toString()));
//                aggrigate.addOption(Options.PERIOD, Period.weeks(1).toString());
            } else if (_mode == AGGREGATION.Yearly) {
                System.out.println("year.....  " + Period.years(1).toString());
                aggrigate = new BasicProcess();
                aggrigate.setJEVisDataSource(att.getDataSource());
                aggrigate.setID("Dynamic");
                aggrigate.setFunction(new AggrigatorFunction());
                aggrigate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.years(1).toString()));
//                aggrigate.addOption(Options.PERIOD, Period.years(1).toString());
            }

            if (_dataProcessor == null) {
                if (aggrigate != null) {
                    Process input = new BasicProcess();
                    input.setJEVisDataSource(att.getDataSource());
                    input.setID("Dynamic Input");
                    input.setFunction(new InputFunction());

                    input.getOptions().add(new BasicProcessOption(InputFunction.ATTRIBUTE_ID, _attribute.getName()));
                    input.getOptions().add(new BasicProcessOption(InputFunction.OBJECT_ID, _attribute.getObject().getID() + ""));
//                    input.getOptions().put(InputFunction.ATTRIBUTE_ID, _attribute.getName());
//                    input.getOptions().put(InputFunction.OBJECT_ID, _attribute.getObject().getID() + "");
                    aggrigate.setSubProcesses(Arrays.asList(input));
                    samples.addAll(aggrigate.getResult());
                } else {
                    samples.addAll(att.getSamples(from, until));
                }

            } else if (aggrigate != null) {
                aggrigate.setSubProcesses(Arrays.asList(_dataProcessor));
                samples.addAll(aggrigate.getResult());
            } else {
                samples.addAll(_dataProcessor.getResult());
            }

            for (SampleEditorExtension ex : extensions) {
                ex.setSamples(att, samples);
            }

            _dataChanged = true;
            _visibleExtension.update();
        } catch (JEVisException ex) {
            ex.printStackTrace();
        }
    }

    public enum Response {

        YES, CANCEL
    }

    private void update() {
        updateSamples(_attribute, _from, _until, extensions);
    }

    private enum AGGREGATION {

        None, Hourly, Daily, Weekly, Monthly,
        Yearly
    }

}
