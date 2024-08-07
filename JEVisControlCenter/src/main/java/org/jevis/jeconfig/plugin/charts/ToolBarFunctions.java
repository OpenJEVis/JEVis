package org.jevis.jeconfig.plugin.charts;

import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.LocalTimeStringConverter;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.Chart.Charts.regression.RegressionType;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.dialog.BaseLoadDialog;
import org.jevis.jeconfig.dialog.ValuesDialog;
import org.jevis.jeconfig.sample.DaySchedule;
import org.jevis.jeconfig.tool.NumberSpinner;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

public class ToolBarFunctions {
    private static final Logger logger = LogManager.getLogger(ToolBarFunctions.class);
    private final JEVisDataSource ds;
    private final DataSettings dataSettings;
    private final ToolBarSettings toolBarSettings;
    private final ChartPlugin chartPlugin;

    public ToolBarFunctions(JEVisDataSource ds, DataSettings dataSettings, ToolBarSettings toolBarSettings, ChartPlugin chartPlugin) {
        this.ds = ds;
        this.dataSettings = dataSettings;
        this.toolBarSettings = toolBarSettings;
        this.chartPlugin = chartPlugin;
    }

    protected void calcRegression() {
        if (!toolBarSettings.isCalculateRegression()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

            Label polyDegreeLabel = new Label(I18n.getInstance().getString("plugin.graph.toolbar.regression.degree"));
            NumberSpinner polyDegreeNumberSpinner = new NumberSpinner(new BigDecimal(1), new BigDecimal(1));
            polyDegreeNumberSpinner.setMin(new BigDecimal(1));
            polyDegreeNumberSpinner.setMax(new BigDecimal(11));

//            Label regressionTypeLabel = new Label(I18n.getInstance().getString("plugin.graph.toolbar.regression.type"));
            Label regressionTypeLabel = new Label(I18n.getInstance().getString("dialog.regression.type.poly"));
//            RegressionBox regressionTypeComboBox = new RegressionBox();

            GridPane gridPane = new GridPane();
            gridPane.setVgap(4);
            gridPane.setHgap(4);

            gridPane.add(regressionTypeLabel, 0, 0);
//            gridPane.add(regressionTypeComboBox, 1, 0);

            gridPane.add(polyDegreeLabel, 0, 1);
            gridPane.add(polyDegreeNumberSpinner, 1, 1);

//            regressionTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//                if (!newValue.equals(oldValue)) {
//                    if (newValue.equals(RegressionType.POLY)) {
//                        gridPane.add(polyDegreeLabel, 0, 1);
//                        gridPane.add(polyDegreeNumberSpinner, 1, 1);
//                    } else {
//                        gridPane.getChildren().removeAll(polyDegreeLabel, polyDegreeNumberSpinner);
//                    }
//                }
//            });

            alert.getDialogPane().setContent(gridPane);

            alert.showAndWait().ifPresent(buttonType -> {
                if (buttonType.getButtonData().isDefaultButton()) {
                    toolBarSettings.setPolyRegressionDegree(polyDegreeNumberSpinner.getNumber().toBigInteger().intValue());
//                    model.setRegressionType(regressionTypeComboBox.getSelectionModel().getSelectedItem());
                    toolBarSettings.setRegressionType(RegressionType.POLY);
                    toolBarSettings.setCalculateRegression(!toolBarSettings.isCalculateRegression());
                }
            });
        } else {
            toolBarSettings.setPolyRegressionDegree(-1);
            toolBarSettings.setRegressionType(RegressionType.NONE);
            toolBarSettings.setCalculateRegression(!toolBarSettings.isCalculateRegression());
        }
    }

    protected void calcFullLoadHours() {
        Alert infoBox = new Alert(Alert.AlertType.INFORMATION);
        infoBox.setResizable(true);
        infoBox.setTitle(I18n.getInstance().getString("dialog.fullloadhours.title"));
        infoBox.setHeaderText(I18n.getInstance().getString("dialog.fullloadhours.headertext"));
        TableView<ChartDataRow> fullLoadHours = new TableView<>();

        NumberFormat nf = NumberFormat.getInstance(I18n.getInstance().getLocale());
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        TableColumn<ChartDataRow, String> nameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.name"));
        nameColumn.setSortable(true);
        nameColumn.setPrefWidth(400);
        nameColumn.setMinWidth(100);
        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));

        nameColumn.setCellFactory(new Callback<TableColumn<ChartDataRow, String>, TableCell<ChartDataRow, String>>() {
            @Override
            public TableCell<ChartDataRow, String> call(TableColumn<ChartDataRow, String> param) {
                return new TableCell<ChartDataRow, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText("");
                        setGraphic(null);

                        if (item != null && !empty) {
                            setText(item);
                        }
                    }
                };
            }
        });


        TableColumn<ChartDataRow, String> valueColumn = new TableColumn<>(I18n.getInstance().getString("dialog.fullloadhours.title"));
        valueColumn.setStyle("-fx-alignment: CENTER");
        valueColumn.setSortable(true);
        valueColumn.setPrefWidth(125);
        valueColumn.setMinWidth(75);
        valueColumn.setCellValueFactory(param -> {
            Double result = 0d;

            if (param.getValue().getSum() != null && param.getValue().getMax() != null && !param.getValue().getMax().equals(0d)) {
                result = param.getValue().getSum() / param.getValue().getMax().getValue();
            }

            return new SimpleObjectProperty<>(nf.format(result));
        });

        valueColumn.setCellFactory(new Callback<TableColumn<ChartDataRow, String>, TableCell<ChartDataRow, String>>() {
            @Override
            public TableCell<ChartDataRow, String> call(TableColumn<ChartDataRow, String> param) {
                return new TableCell<ChartDataRow, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText("");
                        setGraphic(null);

                        if (item != null && !empty) {
                            setText(item);
                        }
                    }
                };
            }
        });

        fullLoadHours.getColumns().addAll(nameColumn, valueColumn);
        chartPlugin.getAllCharts().forEach((integer, chart) -> chart.getChartDataRows().forEach(chartDataRow -> {
            fullLoadHours.getItems().add(chartDataRow);
        }));


        AlphanumComparator ac = new AlphanumComparator();
        fullLoadHours.getItems().sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));

        infoBox.getDialogPane().setContent(fullLoadHours);
        infoBox.show();
    }

    protected void calcBaseLoad() {
        Dialog infoBox = new Dialog();
        infoBox.setTitle(I18n.getInstance().getString("plugin.graph.baseload.title"));
        infoBox.setHeaderText(I18n.getInstance().getString("plugin.graph.baseload.header"));
        infoBox.setResizable(true);
        infoBox.initOwner(JEConfig.getStage());
        infoBox.initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) infoBox.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        Label baseLoadTimeFrame = new Label(I18n.getInstance().getString("dialog.baseload.timeframe"));

        JFXDatePicker baseLoadStartDate = new JFXDatePicker(LocalDate.now());
        baseLoadStartDate.setPrefWidth(120d);

        JFXTimePicker baseLoadStartTime = new JFXTimePicker(LocalTime.now());
        baseLoadStartTime.setPrefWidth(100d);
        baseLoadStartTime.setMaxWidth(100d);
        baseLoadStartTime.set24HourView(true);
        baseLoadStartTime.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        JFXDatePicker baseLoadEndDate = new JFXDatePicker(LocalDate.now());
        baseLoadEndDate.setPrefWidth(120d);

        JFXTimePicker baseLoadEndTime = new JFXTimePicker(LocalTime.now());
        baseLoadEndTime.setPrefWidth(100d);
        baseLoadEndTime.setMaxWidth(100d);
        baseLoadEndTime.set24HourView(true);
        baseLoadEndTime.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        Label baseLoadBinding = new Label(I18n.getInstance().getString("dialog.baseload.repeatingtimeframe"));
        ObservableList<Integer> list = FXCollections.observableArrayList(0, 1, 2, 3, 4);
        JFXComboBox<Integer> boundSpecificBox = new JFXComboBox<>(list);

        Callback<ListView<Integer>, ListCell<Integer>> cellFactoryBoundToSpecificBox = new Callback<javafx.scene.control.ListView<Integer>, ListCell<Integer>>() {
            @Override
            public ListCell<Integer> call(javafx.scene.control.ListView<Integer> param) {
                return new ListCell<Integer>() {
                    @Override
                    protected void updateItem(Integer no, boolean empty) {
                        super.updateItem(no, empty);
                        if (empty || no == null) {
                            setText("");
                        } else {
                            String text = "";
                            switch (no) {
                                case 0:
                                    text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.boundtospecific.none");
                                    break;
                                case 1:
                                    text = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.day");
                                    break;
                                case 2:
                                    text = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.week");
                                    break;
                                case 3:
                                    text = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.month");
                                    break;
                                case 4:
                                    text = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.year");
                                    break;
                            }
                            setText(text);
                        }
                    }
                };
            }
        };
        boundSpecificBox.setCellFactory(cellFactoryBoundToSpecificBox);
        boundSpecificBox.setButtonCell(cellFactoryBoundToSpecificBox.call(null));
        boundSpecificBox.getSelectionModel().select(0);

        Label resultTimeFrame = new Label(I18n.getInstance().getString("dialog.baseload.resulttimeframe"));

        JFXDatePicker resultStartDate = new JFXDatePicker(dataSettings.getAnalysisTimeFrame().getLocalStartDate());
        resultStartDate.setPrefWidth(120d);

        JFXTimePicker resultStartTime = new JFXTimePicker(dataSettings.getAnalysisTimeFrame().getLocalStartTime());
        resultStartTime.setPrefWidth(100d);
        resultStartTime.setMaxWidth(100d);
        resultStartTime.set24HourView(true);
        resultStartTime.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        JFXDatePicker resultEndDate = new JFXDatePicker(dataSettings.getAnalysisTimeFrame().getLocalEndDate());
        resultEndDate.setPrefWidth(120d);

        JFXTimePicker resultEndTime = new JFXTimePicker(dataSettings.getAnalysisTimeFrame().getLocalEndTime());
        resultEndTime.setPrefWidth(100d);
        resultEndTime.setMaxWidth(100d);
        resultEndTime.set24HourView(true);
        resultEndTime.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        GridPane gp = new GridPane();
        gp.setVgap(6);
        gp.setHgap(6);

        int row = 0;
        gp.add(baseLoadTimeFrame, 0, row, 4, 1);
        row++;

        gp.add(baseLoadStartDate, 0, row);
        gp.add(baseLoadStartTime, 1, row);
        gp.add(baseLoadEndDate, 2, row);
        gp.add(baseLoadEndTime, 3, row);
        row++;

        gp.add(baseLoadBinding, 0, row, 4, 1);
        row++;

        gp.add(boundSpecificBox, 0, row, 2, 1);
        row++;

        gp.add(resultTimeFrame, 0, row, 4, 1);
        row++;

        gp.add(resultStartDate, 0, row);
        gp.add(resultStartTime, 1, row);
        gp.add(resultEndDate, 2, row);
        gp.add(resultEndTime, 3, row);

        ButtonType okType = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        infoBox.getDialogPane().getButtonTypes().addAll(cancelType, okType);

        Button okButton = (Button) infoBox.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);

        Button cancelButton = (Button) infoBox.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(8, 0, 8, 0));

        VBox vBox = new VBox(6, gp, separator);
        vBox.setPadding(new Insets(12));

        okButton.setOnAction(event -> {
            try {
                BaseLoadSetting setting = new BaseLoadSetting();
                setting.setBaseLoadStart(baseLoadStartDate.getValue(), baseLoadStartTime.getValue());
                setting.setBaseLoadEnd(baseLoadEndDate.getValue(), baseLoadEndTime.getValue());
                setting.setRepeatType(boundSpecificBox.getSelectionModel().getSelectedItem());
                setting.setResultStart(resultStartDate.getValue(), resultStartTime.getValue());
                setting.setResultEnd(resultEndDate.getValue(), resultEndTime.getValue());

                BaseLoadDialog dialog = new BaseLoadDialog(ds, setting, chartPlugin.getAllCharts());

                dialog.show();

            } catch (Exception e) {
                logger.error(e);
            }
            infoBox.close();
        });

        cancelButton.setOnAction(event -> infoBox.close());

        infoBox.getDialogPane().setContent(vBox);
        infoBox.show();
    }

    protected void calcValues() {
        Dialog infoBox = new Dialog();
        infoBox.setTitle(I18n.getInstance().getString("plugin.graph.values.title"));
        infoBox.setHeaderText(I18n.getInstance().getString("plugin.graph.values.header"));
        infoBox.setResizable(true);
        infoBox.initOwner(JEConfig.getStage());
        infoBox.initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) infoBox.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        final ValuesSetting valuesSetting = new ValuesSetting();

        Label resultTimeFrame = new Label(I18n.getInstance().getString("dialog.baseload.resulttimeframe"));

        JFXDatePicker resultStartDate = new JFXDatePicker(dataSettings.getAnalysisTimeFrame().getLocalStartDate());
        resultStartDate.setPrefWidth(120d);

        JFXTimePicker resultStartTime = new JFXTimePicker(dataSettings.getAnalysisTimeFrame().getLocalStartTime());
        resultStartTime.setPrefWidth(100d);
        resultStartTime.setMaxWidth(100d);
        resultStartTime.set24HourView(true);
        resultStartTime.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        JFXDatePicker resultEndDate = new JFXDatePicker(dataSettings.getAnalysisTimeFrame().getLocalEndDate());
        resultEndDate.setPrefWidth(120d);

        JFXTimePicker resultEndTime = new JFXTimePicker(dataSettings.getAnalysisTimeFrame().getLocalEndTime());
        resultEndTime.setPrefWidth(100d);
        resultEndTime.setMaxWidth(100d);
        resultEndTime.set24HourView(true);
        resultEndTime.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        GridPane gp = new GridPane();
        gp.setVgap(6);
        gp.setHgap(6);

        int row = 0;

        gp.add(resultTimeFrame, 0, row, 4, 1);
        row++;

        gp.add(resultStartDate, 0, row);
        gp.add(resultStartTime, 1, row);
        gp.add(resultEndDate, 2, row);
        gp.add(resultEndTime, 3, row);
        row++;

        JFXCheckBox insideBox = new JFXCheckBox(I18n.getInstance().getString("graph.dialog.buttons.values.inside"));
        insideBox.selectedProperty().addListener((observableValue, aBoolean, t1) -> valuesSetting.setInside(t1));
        insideBox.setSelected(true);
        gp.add(insideBox, 0, row, 2, 1);
        row++;

        Button forAllStart = new Button("", JEConfig.getSVGImage(Icon.ARROW_DOWN, 12, 12));
        Button forAllEnd = new Button("", JEConfig.getSVGImage(Icon.ARROW_DOWN, 12, 12));
        gp.add(forAllStart, 1, row);
        gp.add(forAllEnd, 3, row);
        GridPane.setHalignment(forAllStart, HPos.RIGHT);
        GridPane.setHalignment(forAllEnd, HPos.RIGHT);
        row++;

        for (int i = 1; i < 8; i++) {
            DaySchedule daySchedule = new DaySchedule(i);
            valuesSetting.getDaySchedule().put(i, daySchedule);

            gp.add(daySchedule.getDayButton(), 0, row);
            gp.add(daySchedule.getStartVBox(), 1, row);
            gp.add(daySchedule.getStart(), 2, row);
            gp.add(daySchedule.getEndVBox(), 3, row);
            gp.add(daySchedule.getEnd(), 4, row);
            row++;
        }

        forAllStart.setOnAction(actionEvent -> {
            LocalTime value = valuesSetting.getDaySchedule().get(1).getStart().getValue();
            for (int i = 2; i < 8; i++) {
                int finalI = i;
                Platform.runLater(() -> valuesSetting.getDaySchedule().get(finalI).getStart().setValue(value));
            }
        });

        forAllEnd.setOnAction(actionEvent -> {
            LocalTime value = valuesSetting.getDaySchedule().get(1).getEnd().getValue();
            for (int i = 2; i < 8; i++) {
                int finalI = i;
                Platform.runLater(() -> valuesSetting.getDaySchedule().get(finalI).getEnd().setValue(value));
            }
        });

        ButtonType okType = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        infoBox.getDialogPane().getButtonTypes().addAll(cancelType, okType);

        Button okButton = (Button) infoBox.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);

        Button cancelButton = (Button) infoBox.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(8, 0, 8, 0));

        VBox vBox = new VBox(6, gp, separator);
        vBox.setPadding(new Insets(12));

        okButton.setOnAction(event -> {
            try {
                valuesSetting.setResultStart(resultStartDate.getValue(), resultStartTime.getValue());
                valuesSetting.setResultEnd(resultEndDate.getValue(), resultEndTime.getValue());

                ValuesDialog dialog = new ValuesDialog(ds, valuesSetting, chartPlugin.getAllCharts());

                dialog.show();

            } catch (Exception e) {
                logger.error(e);
            }
            infoBox.close();
        });

        cancelButton.setOnAction(event -> infoBox.close());

        infoBox.getDialogPane().setContent(vBox);
        infoBox.show();
    }

    protected void calcSumAboveBelow() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(I18n.getInstance().getString("dialog.calchoursabovebelow.entervalue"));

        Label limitLabel = new Label(I18n.getInstance().getString("plugin.scada.element.setting.label.lowerlimit.limitvalue"));
        JFXTextField limitField = new JFXTextField();
        DoubleValidator validator = DoubleValidator.getInstance();
        limitField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double parsedValue = validator.validate(newValue, I18n.getInstance().getLocale());
            } catch (Exception e) {
                limitField.setText(oldValue);
            }
        });

        GridPane gridPane = new GridPane();
        gridPane.setVgap(4);
        gridPane.setHgap(12);

        gridPane.add(limitLabel, 0, 0);
        gridPane.add(limitField, 1, 0);

        alert.getDialogPane().setContent(gridPane);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType.getButtonData().isDefaultButton() && !limitField.getText().isEmpty()) {
                Task task = new Task() {
                    @Override
                    protected Object call() throws Exception {
                        try {
                            Double limit = validator.validate(limitField.getText());

                            TableView<ValuesAbove> fullLoadHours = new TableView<>();

                            TableColumn<ValuesAbove, String> nameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.name"));
                            nameColumn.setSortable(true);
                            nameColumn.setPrefWidth(400);
                            nameColumn.setMinWidth(100);
                            nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getChartDataRow().getName()));

                            nameColumn.setCellFactory(new Callback<TableColumn<ValuesAbove, String>, TableCell<ValuesAbove, String>>() {
                                @Override
                                public TableCell<ValuesAbove, String> call(TableColumn<ValuesAbove, String> param) {
                                    return new TableCell<ValuesAbove, String>() {
                                        @Override
                                        protected void updateItem(String item, boolean empty) {
                                            super.updateItem(item, empty);
                                            setText("");
                                            setGraphic(null);

                                            if (item != null && !empty) {
                                                setText(item);
                                            }
                                        }
                                    };
                                }
                            });

                            TableColumn<ValuesAbove, String> belowValueColumn = new TableColumn<>(I18n.getInstance().getString("dialog.calchoursabovebelow.below"));
                            belowValueColumn.setStyle("-fx-alignment: CENTER-RIGHT");
                            belowValueColumn.setSortable(true);
                            belowValueColumn.setPrefWidth(200);
                            belowValueColumn.setMinWidth(150);
                            belowValueColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getBelowValue()));

                            belowValueColumn.setCellFactory(new Callback<TableColumn<ValuesAbove, String>, TableCell<ValuesAbove, String>>() {
                                @Override
                                public TableCell<ValuesAbove, String> call(TableColumn<ValuesAbove, String> param) {
                                    return new TableCell<ValuesAbove, String>() {
                                        @Override
                                        protected void updateItem(String item, boolean empty) {
                                            super.updateItem(item, empty);
                                            setText("");
                                            setGraphic(null);

                                            if (item != null && !empty) {
                                                setText(item);
                                            }
                                        }
                                    };
                                }
                            });

                            TableColumn<ValuesAbove, String> aboveValueColumn = new TableColumn<>(I18n.getInstance().getString("dialog.calchoursabovebelow.above"));
                            aboveValueColumn.setStyle("-fx-alignment: CENTER-RIGHT");
                            aboveValueColumn.setSortable(true);
                            aboveValueColumn.setPrefWidth(200);
                            aboveValueColumn.setMinWidth(150);
                            aboveValueColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getAboveValue()));

                            aboveValueColumn.setCellFactory(new Callback<TableColumn<ValuesAbove, String>, TableCell<ValuesAbove, String>>() {
                                @Override
                                public TableCell<ValuesAbove, String> call(TableColumn<ValuesAbove, String> param) {
                                    return new TableCell<ValuesAbove, String>() {
                                        @Override
                                        protected void updateItem(String item, boolean empty) {
                                            super.updateItem(item, empty);
                                            setText("");
                                            setGraphic(null);

                                            if (item != null && !empty) {
                                                setText(item);
                                            }
                                        }
                                    };
                                }
                            });

                            TableColumn<ValuesAbove, String> belowDurationColumn = new TableColumn<>(I18n.getInstance().getString("dialog.calchoursabovebelow.below"));
                            belowDurationColumn.setStyle("-fx-alignment: CENTER");
                            belowDurationColumn.setSortable(true);
                            belowDurationColumn.setPrefWidth(200);
                            belowDurationColumn.setMinWidth(150);
                            belowDurationColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getBelowDuration()));

                            belowDurationColumn.setCellFactory(new Callback<TableColumn<ValuesAbove, String>, TableCell<ValuesAbove, String>>() {
                                @Override
                                public TableCell<ValuesAbove, String> call(TableColumn<ValuesAbove, String> param) {
                                    return new TableCell<ValuesAbove, String>() {
                                        @Override
                                        protected void updateItem(String item, boolean empty) {
                                            super.updateItem(item, empty);
                                            setText("");
                                            setGraphic(null);

                                            if (item != null && !empty) {
                                                JFXTextArea jfxTextArea = new JFXTextArea(item);
                                                jfxTextArea.setWrapText(true);
                                                setGraphic(jfxTextArea);
                                            }
                                        }
                                    };
                                }
                            });

                            TableColumn<ValuesAbove, String> aboveDurationColumn = new TableColumn<>(I18n.getInstance().getString("dialog.calchoursabovebelow.above"));
                            aboveDurationColumn.setStyle("-fx-alignment: CENTER");
                            aboveDurationColumn.setSortable(true);
                            aboveDurationColumn.setPrefWidth(200);
                            aboveDurationColumn.setMinWidth(150);
                            aboveDurationColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getAboveDuration()));

                            aboveDurationColumn.setCellFactory(new Callback<TableColumn<ValuesAbove, String>, TableCell<ValuesAbove, String>>() {
                                @Override
                                public TableCell<ValuesAbove, String> call(TableColumn<ValuesAbove, String> param) {
                                    return new TableCell<ValuesAbove, String>() {
                                        @Override
                                        protected void updateItem(String item, boolean empty) {
                                            super.updateItem(item, empty);
                                            setText("");
                                            setGraphic(null);

                                            if (item != null && !empty) {
                                                JFXTextArea jfxTextArea = new JFXTextArea(item);
                                                jfxTextArea.setWrapText(true);
                                                setGraphic(jfxTextArea);
                                            }
                                        }
                                    };
                                }
                            });

                            fullLoadHours.getColumns().addAll(nameColumn, belowValueColumn, belowDurationColumn, aboveValueColumn, aboveDurationColumn);

                            List<ValuesAbove> hoursAbove = new ArrayList<>();
                            chartPlugin.getAllCharts().forEach((integer, chart) -> chart.getChartDataRows().forEach(chartDataRow -> {
                                hoursAbove.add(new ValuesAbove(chartDataRow, limit));
                            }));

                            AlphanumComparator ac = new AlphanumComparator();
                            hoursAbove.sort((o1, o2) -> ac.compare(o1.getChartDataRow().getName(), o2.getChartDataRow().getName()));
                            fullLoadHours.getItems().addAll(hoursAbove);

                            Platform.runLater(() -> {
                                Alert infoBox = new Alert(Alert.AlertType.INFORMATION);
                                infoBox.setResizable(true);
                                infoBox.setTitle(I18n.getInstance().getString("dialog.calcsumabovebelow.title") + "\n" + I18n.getInstance().getString("dialog.calchoursabovebelow.title"));
                                infoBox.setHeaderText(I18n.getInstance().getString("dialog.calcsumabovebelow.headertext") + " " + limit + "\n" + I18n.getInstance().getString("dialog.calchoursabovebelow.headertext") + " " + limit);
                                infoBox.getDialogPane().setContent(fullLoadHours);
                                infoBox.show();
                            });

                        } catch (Exception e) {
                            this.failed();
                            logger.error("Could not calculate times", e);
                        } finally {
                            succeeded();
                        }
                        return null;
                    }
                };

                JEConfig.getStatusBar().addTask(ToolBarView.class.getName(), task, JEConfig.getImage("sumabove.png"), true);
            }
        });
    }
}
