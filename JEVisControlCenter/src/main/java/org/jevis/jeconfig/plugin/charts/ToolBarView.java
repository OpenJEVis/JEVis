/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.charts;

import com.jfoenix.controls.*;
import com.jfoenix.skins.JFXComboBoxListViewSkin;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartElements.MultiChartZoomer;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.PickerCombo;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.PresetDateBox;
import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.Charts.Chart;
import org.jevis.jeconfig.application.Chart.Charts.regression.RegressionType;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.dialog.ChartSelectionDialog;
import org.jevis.jeconfig.dialog.LoadAnalysisDialog;
import org.jevis.jeconfig.dialog.Response;
import org.jevis.jeconfig.tool.NumberSpinner;
import org.joda.time.DateTime;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * @author broder
 */
public class ToolBarView {

    private static final Logger logger = LogManager.getLogger(ToolBarView.class);
    private final JEVisDataSource ds;
    private final ChartPlugin chartPlugin;
    private final ObjectRelations objectRelations;
    private AnalysisDataModel model;
    private final JFXComboBox<JEVisObject> listAnalysesComboBox;
    private Boolean _initialized = false;
    private ToggleButton save;
    private ToggleButton loadNew;
    private ToggleButton exportCSV;
    private ToggleButton exportImage;
    private ToggleButton exportPDF;
    private ToggleButton printButton;
    private ToggleButton reload;
    private ToggleButton delete;
    private ToggleButton autoResize;
    private ToggleButton select;
    private ToggleButton disableIcons;
    private ToggleButton zoomOut;
    private ToggleButton infoButton;
    private ToggleButton helpButton;
    private PickerCombo pickerCombo;
    private PresetDateBox presetDateBox;
    private JFXDatePicker pickerDateStart;
    private JFXTimePicker pickerTimeStart;
    private JFXDatePicker pickerDateEnd;
    private JFXTimePicker pickerTimeEnd;

    private final DateHelper dateHelper = new DateHelper();
    private ToolBar toolBar;
    private Boolean changed = false;

    private ToggleButton runUpdateButton;
    private ToggleButton customWorkDay;

    private JEVisDataSource getDs() {
        return ds;
    }

    private ToggleButton addSeriesRunningMean;
    private ImageView pauseIcon;
    private ImageView playIcon;
    private ToggleButton showRawData;
    private ToggleButton showSum;
    private ToggleButton showL1L2;
    private ToggleButton calcRegression;
    private ToggleButton calcFullLoadHours;
    private ToggleButton calcHoursAboveBelow;
    private ToggleButton calcSumAboveBelow;

    private final ChangeListener<JEVisObject> analysisComboBoxChangeListener = (observable, oldValue, newValue) -> {
        if ((oldValue == null) || (Objects.nonNull(newValue))) {

            if (changed) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setResizable(true);
                Label text = new Label(I18n.getInstance().getString("plugin.graph.dialog.changed.text"));
                text.setWrapText(true);
                alert.getDialogPane().setContent(text);

                alert.showAndWait().ifPresent(buttonType -> {
                    if (buttonType.equals(ButtonType.OK)) {
                        changed = false;
                        getGraphPluginView().handleRequest(Constants.Plugin.Command.SAVE);
                    }
                });
            }
            model.setTemporary(false);
            model.setCurrentAnalysis(newValue);
            model.resetToolbarSettings();
            model.setGlobalAnalysisTimeFrame(model.getGlobalAnalysisTimeFrame());
            Platform.runLater(this::updateLayout);
            changed = false;
        }
    };

    public ToolBarView(AnalysisDataModel model, JEVisDataSource ds, ChartPlugin chartPlugin) {
        this.model = model;
        this.ds = ds;
        this.objectRelations = new ObjectRelations(ds);
        this.chartPlugin = chartPlugin;

        listAnalysesComboBox = new JFXComboBox<>(model.getObservableListAnalyses());
        listAnalysesComboBox.setPrefWidth(300);
        setCellFactoryForComboBox();
    }

    public ToolBar getToolbar(JEVisDataSource ds) {
        if (toolBar == null) {
            toolBar = new ToolBar();
            toolBar.setId("ObjectPlugin.Toolbar");

            updateLayout();
        }

        return toolBar;
    }


    public void addAnalysisComboBoxListener() {
        listAnalysesComboBox.valueProperty().addListener(analysisComboBoxChangeListener);
    }

    public void removeAnalysisComboBoxListener() {
        listAnalysesComboBox.valueProperty().removeListener(analysisComboBoxChangeListener);
    }

    private void resetZoom() {
        for (Map.Entry<Integer, Chart> entry : chartPlugin.getAllCharts().entrySet()) {
            Integer integer = entry.getKey();
            Chart chart = entry.getValue();
            if (chart.getChart() != null) {
                for (de.gsi.chart.plugins.ChartPlugin chartPlugin : chart.getChart().getPlugins()) {
                    if (chartPlugin instanceof MultiChartZoomer) {
                        MultiChartZoomer multiChartZoomer = (MultiChartZoomer) chartPlugin;
                        multiChartZoomer.setFollowUpZoom(false);
                        Platform.runLater(multiChartZoomer::zoomOrigin);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void addSeriesRunningMean() {
        model.setAddSeries(ManipulationMode.RUNNING_MEAN);
    }

    private void setCellFactoryForComboBox() {
        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> cellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                return new ListCell<JEVisObject>() {
                    @Override
                    protected void updateItem(JEVisObject obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (obj == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            if (!model.isMultiSite() && !model.isMultiDir())
                                setText(obj.getName());
                            else {
                                String prefix = "";
                                if (model.isMultiSite()) {
                                    prefix += objectRelations.getObjectPath(obj);
                                }
                                if (model.isMultiDir()) {
                                    prefix += objectRelations.getRelativePath(obj);
                                }

                                setText(prefix + obj.getName());
                            }
                        }

                    }
                };
            }
        };

        listAnalysesComboBox.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.list")));
        listAnalysesComboBox.setCellFactory(cellFactory);
        listAnalysesComboBox.setButtonCell(cellFactory.call(null));
        listAnalysesComboBox.setId("Graph Analysis List");

        listAnalysesComboBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                JFXComboBoxListViewSkin<?> skin = (JFXComboBoxListViewSkin<?>) listAnalysesComboBox.getSkin();
                if (skin != null) {
                    ListView<?> popupContent = (ListView<?>) skin.getPopupContent();
                    if (popupContent != null) {
                        popupContent.scrollTo(model.getObservableListAnalyses().indexOf(model.getCurrentAnalysis()));
                    }
                }
            });
        });
        JEVisHelp.getInstance().addHelpControl(ChartPlugin.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, listAnalysesComboBox);
    }

    private void loadNewDialog() {

        LoadAnalysisDialog dialog = new LoadAnalysisDialog(chartPlugin.getDialogContainer(), ds, model);

        dialog.show();

        dialog.setOnDialogClosed(event -> {
            JEVisHelp.getInstance().deactivatePluginModule();
            if (dialog.getResponse() == Response.NEW) {

                getGraphPluginView().handleRequest(Constants.Plugin.Command.NEW);
            } else if (dialog.getResponse() == Response.LOAD) {

                final Preferences previewPref = Preferences.userRoot().node("JEVis.JEConfig.preview");
                if (!previewPref.getBoolean("enabled", true)) {
                    model.setAnalysisTimeFrameForAllModels(model.getGlobalAnalysisTimeFrame());
                }
            }
        });
    }

    private void hideShowIconsInGraph() {
        model.setShowIcons(!model.getShowIcons());
    }

    private void autoResizeInGraph() {
        model.setAutoResize(!model.getAutoResize());
    }

    private void showRawDataInGraph() {
        model.setShowRawData(!model.getShowRawData());
    }

    private void showSumInGraph() {
        model.setShowSum(!model.getShowSum());
    }

    private void showL1L2InGraph() {
        model.setShowL1L2(!model.getShowL1L2());
    }

    private void calcRegression() {
        if (!model.calcRegression()) {
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
                    model.setPolyRegressionDegree(polyDegreeNumberSpinner.getNumber().toBigInteger().intValue());
//                    model.setRegressionType(regressionTypeComboBox.getSelectionModel().getSelectedItem());
                    model.setRegressionType(RegressionType.POLY);
                    model.setCalcRegression(!model.calcRegression());
                }
            });
        } else {
            model.setPolyRegressionDegree(-1);
            model.setRegressionType(RegressionType.NONE);
            model.setCalcRegression(!model.calcRegression());
        }
    }

    private void calcFullLoadHours() {
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
        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTitle()));

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
                result = param.getValue().getSum() / param.getValue().getMax();
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
        fullLoadHours.getItems().addAll(model.getSelectedData());

        AlphanumComparator ac = new AlphanumComparator();
        fullLoadHours.getItems().sort((o1, o2) -> ac.compare(o1.getTitle(), o2.getTitle()));

        infoBox.getDialogPane().setContent(fullLoadHours);
        infoBox.show();
    }

    private void calcHoursAboveBelow() {
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
                            TableView<HoursAbove> fullLoadHours = new TableView<>();

                            TableColumn<HoursAbove, String> nameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.name"));
                            nameColumn.setSortable(true);
                            nameColumn.setPrefWidth(400);
                            nameColumn.setMinWidth(100);
                            nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getChartDataRow().getTitle()));

                            nameColumn.setCellFactory(new Callback<TableColumn<HoursAbove, String>, TableCell<HoursAbove, String>>() {
                                @Override
                                public TableCell<HoursAbove, String> call(TableColumn<HoursAbove, String> param) {
                                    return new TableCell<HoursAbove, String>() {
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

                            TableColumn<HoursAbove, String> belowColumn = new TableColumn<>(I18n.getInstance().getString("dialog.calchoursabovebelow.below"));
                            belowColumn.setStyle("-fx-alignment: CENTER");
                            belowColumn.setSortable(true);
                            belowColumn.setPrefWidth(200);
                            belowColumn.setMinWidth(150);
                            belowColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getBelow()));

                            belowColumn.setCellFactory(new Callback<TableColumn<HoursAbove, String>, TableCell<HoursAbove, String>>() {
                                @Override
                                public TableCell<HoursAbove, String> call(TableColumn<HoursAbove, String> param) {
                                    return new TableCell<HoursAbove, String>() {
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

                            TableColumn<HoursAbove, String> aboveColumn = new TableColumn<>(I18n.getInstance().getString("dialog.calchoursabovebelow.above"));
                            aboveColumn.setStyle("-fx-alignment: CENTER");
                            aboveColumn.setSortable(true);
                            aboveColumn.setPrefWidth(200);
                            aboveColumn.setMinWidth(150);
                            aboveColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getAbove()));

                            aboveColumn.setCellFactory(new Callback<TableColumn<HoursAbove, String>, TableCell<HoursAbove, String>>() {
                                @Override
                                public TableCell<HoursAbove, String> call(TableColumn<HoursAbove, String> param) {
                                    return new TableCell<HoursAbove, String>() {
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

                            fullLoadHours.getColumns().addAll(nameColumn, belowColumn, aboveColumn);

                            List<HoursAbove> hoursAbove = new ArrayList<>();
                            model.getSelectedData().forEach(chartDataRow -> hoursAbove.add(new HoursAbove(chartDataRow, limit)));

                            AlphanumComparator ac = new AlphanumComparator();
                            hoursAbove.sort((o1, o2) -> ac.compare(o1.getChartDataRow().getTitle(), o2.getChartDataRow().getTitle()));
                            fullLoadHours.getItems().addAll(hoursAbove);

                            Platform.runLater(() -> {
                                Alert infoBox = new Alert(Alert.AlertType.INFORMATION);
                                infoBox.setResizable(true);
                                infoBox.setTitle(I18n.getInstance().getString("dialog.calchoursabovebelow.title"));
                                infoBox.setHeaderText(I18n.getInstance().getString("dialog.calchoursabovebelow.headertext") + " " + limit);
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

                JEConfig.getStatusBar().addTask(ToolBarView.class.getName(), task, JEConfig.getImage("hoursabove.png"), true);
            }
        });
    }

    private void calcSumAboveBelow() {
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

                            TableView<SumsAbove> fullLoadHours = new TableView<>();

                            TableColumn<SumsAbove, String> nameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.name"));
                            nameColumn.setSortable(true);
                            nameColumn.setPrefWidth(400);
                            nameColumn.setMinWidth(100);
                            nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getChartDataRow().getTitle()));

                            nameColumn.setCellFactory(new Callback<TableColumn<SumsAbove, String>, TableCell<SumsAbove, String>>() {
                                @Override
                                public TableCell<SumsAbove, String> call(TableColumn<SumsAbove, String> param) {
                                    return new TableCell<SumsAbove, String>() {
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

                            TableColumn<SumsAbove, String> belowColumn = new TableColumn<>(I18n.getInstance().getString("dialog.calchoursabovebelow.below"));
                            belowColumn.setStyle("-fx-alignment: CENTER-RIGHT");
                            belowColumn.setSortable(true);
                            belowColumn.setPrefWidth(200);
                            belowColumn.setMinWidth(150);
                            belowColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getBelow()));

                            belowColumn.setCellFactory(new Callback<TableColumn<SumsAbove, String>, TableCell<SumsAbove, String>>() {
                                @Override
                                public TableCell<SumsAbove, String> call(TableColumn<SumsAbove, String> param) {
                                    return new TableCell<SumsAbove, String>() {
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

                            TableColumn<SumsAbove, String> aboveColumn = new TableColumn<>(I18n.getInstance().getString("dialog.calchoursabovebelow.above"));
                            aboveColumn.setStyle("-fx-alignment: CENTER-RIGHT");
                            aboveColumn.setSortable(true);
                            aboveColumn.setPrefWidth(200);
                            aboveColumn.setMinWidth(150);
                            aboveColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getAbove()));

                            aboveColumn.setCellFactory(new Callback<TableColumn<SumsAbove, String>, TableCell<SumsAbove, String>>() {
                                @Override
                                public TableCell<SumsAbove, String> call(TableColumn<SumsAbove, String> param) {
                                    return new TableCell<SumsAbove, String>() {
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

                            fullLoadHours.getColumns().addAll(nameColumn, belowColumn, aboveColumn);

                            List<SumsAbove> hoursAbove = new ArrayList<>();
                            model.getSelectedData().forEach(chartDataRow -> hoursAbove.add(new SumsAbove(chartDataRow, limit)));

                            AlphanumComparator ac = new AlphanumComparator();
                            hoursAbove.sort((o1, o2) -> ac.compare(o1.getChartDataRow().getTitle(), o2.getChartDataRow().getTitle()));
                            fullLoadHours.getItems().addAll(hoursAbove);

                            Platform.runLater(() -> {
                                Alert infoBox = new Alert(Alert.AlertType.INFORMATION);
                                infoBox.setResizable(true);
                                infoBox.setTitle(I18n.getInstance().getString("dialog.calcsumabovebelow.title"));
                                infoBox.setHeaderText(I18n.getInstance().getString("dialog.calcsumabovebelow.headertext") + " " + limit);
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

    public JFXComboBox<JEVisObject> getListAnalysesComboBox() {
        return listAnalysesComboBox;
    }

    private void changeSettings() {
        ChartSelectionDialog dia = new ChartSelectionDialog(chartPlugin.getDialogContainer(), ds, model);

        dia.setOnDialogClosed(event -> {
            if (dia.getResponse() == Response.OK) {

                model.setCharts(dia.getChartPlugin().getData().getCharts());
                model.setSelectedData(dia.getChartPlugin().getData().getSelectedData());
                changed = true;
            }
            JEVisHelp.getInstance().deactivatePluginModule();
        });

        dia.show();
    }


    public void selectFirst() {
        if (!_initialized) {
            model.updateListAnalyses();
        }
        listAnalysesComboBox.getSelectionModel().selectFirst();
    }

    public void select(JEVisObject obj) {
        getListAnalysesComboBox().getSelectionModel().select(obj);
    }

    public void setDisableToolBarIcons(boolean bool) {
        listAnalysesComboBox.setDisable(bool);
        save.setDisable(bool);
        loadNew.setDisable(bool);
        exportCSV.setDisable(bool);
        exportImage.setDisable(bool);
        exportPDF.setDisable(bool);
        printButton.setDisable(bool);
        reload.setDisable(bool);
        runUpdateButton.setDisable(bool);
        delete.setDisable(bool);
        autoResize.setDisable(bool);
        select.setDisable(bool);
        showRawData.setDisable(bool);
        showSum.setDisable(bool);
        showL1L2.setDisable(bool);
        calcRegression.setDisable(bool);
        calcFullLoadHours.setDisable(bool);
        calcHoursAboveBelow.setDisable(bool);
        calcSumAboveBelow.setDisable(bool);
        customWorkDay.setDisable(bool);
        disableIcons.setDisable(bool);
        zoomOut.setDisable(bool);
        presetDateBox.setDisable(bool);
        pickerDateStart.setDisable(bool);
        pickerDateEnd.setDisable(bool);
        pickerTimeStart.setDisable(bool);
        pickerTimeEnd.setDisable(bool);
    }

    public PickerCombo getPickerCombo() {
        return pickerCombo;
    }

    public void updateLayout() {
        Platform.runLater(() -> {

            removeAnalysisComboBoxListener();

            if (model.getCurrentAnalysis() != null) {
                listAnalysesComboBox.getSelectionModel().select(model.getCurrentAnalysis());
            }

            if (!listAnalysesComboBox.getItems().isEmpty()) {

                dateHelper.setWorkDays(model.getWorkDays());
            }

            toolBar.getItems().clear();
            pickerCombo = new PickerCombo(model, null, true);
            pickerCombo.updateCellFactory();
            presetDateBox = pickerCombo.getPresetDateBox();
            pickerDateStart = pickerCombo.getStartDatePicker();
            pickerTimeStart = pickerCombo.getStartTimePicker();
            pickerDateEnd = pickerCombo.getEndDatePicker();
            pickerTimeEnd = pickerCombo.getEndTimePicker();

            JEVisHelp.getInstance().addHelpControl(ChartPlugin.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, presetDateBox, pickerDateStart, pickerTimeStart);

            createToolbarIcons();

            Separator sep1 = new Separator();
            Separator sep2 = new Separator();
            Separator sep3 = new Separator();
            Separator sep4 = new Separator();

            boolean isRegressionPossible = false;
            boolean isAdditionalCalcPossible = false;

            for (ChartSetting chartSetting : model.getCharts().getListSettings()) {
                if (chartSetting.getChartType() != ChartType.TABLE && chartSetting.getChartType() != ChartType.HEAT_MAP
                        && chartSetting.getChartType() != ChartType.BAR && chartSetting.getChartType() != ChartType.PIE)
                    isRegressionPossible = true;
            }

            QuantityUnits qu = new QuantityUnits();
            for (ChartDataRow chartDataRow : model.getSelectedData()) {
                if (qu.isSumCalculable(chartDataRow.getUnit())) {
                    isAdditionalCalcPossible = true;
                    break;
                }
            }

            if (!JEConfig.getExpert()) {
                toolBar.getItems().addAll(listAnalysesComboBox,
                        sep1, presetDateBox, pickerDateStart, pickerDateEnd, customWorkDay,
                        sep2, reload, zoomOut,
                        sep3, loadNew, save, delete, select, exportCSV, exportImage, exportPDF, printButton,
                        sep4);

                if (isRegressionPossible) {
                    toolBar.getItems().add(calcRegression);
                }

                if (isAdditionalCalcPossible) {
                    toolBar.getItems().addAll(calcFullLoadHours, calcHoursAboveBelow, calcSumAboveBelow);
                }

                toolBar.getItems().addAll(showL1L2, showSum, disableIcons, autoResize, runUpdateButton);
            } else {
                toolBar.getItems().addAll(listAnalysesComboBox,
                        sep1, presetDateBox, pickerDateStart, pickerDateEnd, customWorkDay,
                        sep2, reload, zoomOut,
                        sep3, loadNew, save, delete, select, exportCSV, exportImage, exportPDF, printButton,
                        sep4);

                if (isRegressionPossible) {
                    toolBar.getItems().add(calcRegression);
                }

                if (isAdditionalCalcPossible) {
                    toolBar.getItems().addAll(calcFullLoadHours, calcHoursAboveBelow, calcSumAboveBelow);
                }

                toolBar.getItems().addAll(showL1L2, showRawData, showSum, disableIcons, autoResize, runUpdateButton);
            }

            //TODO: the plugin name must be the same the the Plugin.getName() function

            toolBar.getItems().addAll(JEVisHelp.getInstance().buildSpacerNode(), helpButton, infoButton);
            JEVisHelp.getInstance().addHelpItems(ChartPlugin.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, toolBar.getItems());

            addAnalysisComboBoxListener();
            pickerCombo.addListener();
            startToolbarIconListener();

        });
    }

    private void startToolbarIconListener() {
        reload.selectedProperty().addListener((observable, oldValue, newValue) -> chartPlugin.handleRequest(Constants.Plugin.Command.RELOAD));

        runUpdateButton.setOnAction(action -> {
            if (runUpdateButton.isSelected()) {
                model.setRunUpdate(true);
                runUpdateButton.setGraphic(pauseIcon);
                model.setTimer();
            } else {
                model.setRunUpdate(false);
                runUpdateButton.setGraphic(playIcon);
                model.stopTimer();
            }
        });

        exportCSV.setOnAction(action -> {
            ChartExportCSV ge = null;
            if (chartPlugin.isZoomed()) {
                ge = new ChartExportCSV(
                        ds,
                        model,
                        new DateTime(chartPlugin.getxAxisLowerBound().longValue() * 1000),
                        new DateTime(chartPlugin.getxAxisUpperBound().longValue() * 1000));
            } else {
                ge = new ChartExportCSV(
                        ds,
                        model,
                        model.getGlobalAnalysisTimeFrame().getStart(),
                        model.getGlobalAnalysisTimeFrame().getEnd());
            }

            try {
                ge.export();
            } catch (FileNotFoundException | UnsupportedEncodingException | JEVisException e) {
                logger.error("Error: could not export to file.", e);
            }
        });

        exportImage.setOnAction(action -> {
            ChartExportImage exportImage = new ChartExportImage(model);

            if (exportImage.getDestinationFile() != null) {

                exportImage.export(chartPlugin.getvBox());
            }

        });

        exportPDF.setOnAction(event -> {
            ChartExportPDF exportPDF = new ChartExportPDF(model, false);

            if (exportPDF.getDestinationFile() != null) {

                exportPDF.export(chartPlugin.getvBox());
            }
        });

        printButton.setOnAction(event -> {

            ChartExportPDF exportPDF = new ChartExportPDF(model, true);

            if (exportPDF.getDestinationFile() != null) {

                exportPDF.export(chartPlugin.getvBox());
            }

        });

        save.setOnAction(action -> chartPlugin.handleRequest(Constants.Plugin.Command.SAVE));

        loadNew.setOnAction(event -> {
            loadNewDialog();
        });

        zoomOut.setOnAction(event -> resetZoom());

        select.setOnAction(event -> {
            changeSettings();
        });

        delete.setOnAction(event -> getGraphPluginView().handleRequest(Constants.Plugin.Command.DELETE));

        showRawData.setOnAction(event -> showRawDataInGraph());

        showSum.setOnAction(event -> showSumInGraph());

        showL1L2.setOnAction(event -> showL1L2InGraph());

        calcRegression.setOnAction(event -> calcRegression());

        calcFullLoadHours.setOnAction(event -> calcFullLoadHours());

        calcHoursAboveBelow.setOnAction(event -> calcHoursAboveBelow());

        calcSumAboveBelow.setOnAction(event -> calcSumAboveBelow());

        customWorkDay.setOnAction(event -> customWorkDay());

        disableIcons.setOnAction(event -> hideShowIconsInGraph());

        addSeriesRunningMean.setOnAction(event -> addSeriesRunningMean());

        autoResize.setOnAction(event -> autoResizeInGraph());

    }

    private void customWorkDay() {
        model.setCustomWorkDay(!model.isCustomWorkDay());
    }

    private void createToolbarIcons() {
        double iconSize = 20;

        save = new ToggleButton("", JEConfig.getImage("save.gif", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);

        Tooltip saveTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.save"));
        save.setTooltip(saveTooltip);

        loadNew = new ToggleButton("", JEConfig.getImage("1390343812_folder-open.png", iconSize, iconSize));
        Tooltip loadNewTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.loadNew"));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(loadNew);
        loadNew.setTooltip(loadNewTooltip);

        exportCSV = new ToggleButton("", JEConfig.getImage("export-csv.png", iconSize, iconSize));
        Tooltip exportCSVTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.exportCSV"));
        exportCSV.setTooltip(exportCSVTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(exportCSV);

        exportImage = new ToggleButton("", JEConfig.getImage("export-image.png", iconSize, iconSize));
        Tooltip exportImageTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.exportImage"));
        exportImage.setTooltip(exportImageTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(exportImage);

        exportPDF = new ToggleButton("", JEConfig.getImage("pdf_24_2133056.png", iconSize, iconSize));
        Tooltip exportPDFTooltip = new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.exportPDF"));
        exportPDF.setTooltip(exportPDFTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(exportPDF);

        printButton = new ToggleButton("", JEConfig.getImage("Print_1493286.png", iconSize, iconSize));
        Tooltip printTooltip = new Tooltip(I18n.getInstance().getString("plugin.reports.toolbar.tooltip.print"));
        printButton.setTooltip(printTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(printButton);

        reload = new ToggleButton("", JEConfig.getImage("1403018303_Refresh.png", iconSize, iconSize));
        Tooltip reloadTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.reload"));
        reload.setTooltip(reloadTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);

        pauseIcon = JEConfig.getImage("pause_32.png", iconSize, iconSize);
        playIcon = JEConfig.getImage("play_32.png", iconSize, iconSize);

        runUpdateButton = new ToggleButton("", playIcon);
        Tooltip runUpdateTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.runupdate"));
        runUpdateButton.setTooltip(runUpdateTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(runUpdateButton);
        runUpdateButton.setSelected(model.getRunUpdate());
        runUpdateButton.styleProperty().bind(
                Bindings
                        .when(runUpdateButton.hoverProperty())
                        .then(
                                new SimpleStringProperty("-fx-background-insets: 1 1 1;"))
                        .otherwise(Bindings
                                .when(runUpdateButton.selectedProperty())
                                .then("-fx-background-insets: 1 1 1;")
                                .otherwise(
                                        new SimpleStringProperty("-fx-background-color: transparent;-fx-background-insets: 0 0 0;"))));

        delete = new ToggleButton("", JEConfig.getImage("if_trash_(delete)_16x16_10030.gif", iconSize, iconSize));
        Tooltip deleteTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.delete"));
        delete.setTooltip(deleteTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);

        autoResize = new ToggleButton("", JEConfig.getImage("if_full_screen_61002.png", iconSize, iconSize));
        Tooltip autoResizeTip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.autosize"));
        autoResize.setTooltip(autoResizeTip);
        autoResize.setSelected(model.getAutoResize());
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(autoResize);
        autoResize.styleProperty().bind(
                Bindings
                        .when(autoResize.hoverProperty())
                        .then(
                                new SimpleStringProperty("-fx-background-insets: 1 1 1;"))
                        .otherwise(Bindings
                                .when(autoResize.selectedProperty())
                                .then("-fx-background-insets: 1 1 1;")
                                .otherwise(
                                        new SimpleStringProperty("-fx-background-color: transparent;-fx-background-insets: 0 0 0;"))));

        select = new ToggleButton("", JEConfig.getImage("Data.png", iconSize, iconSize));
        Tooltip selectTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.select"));
        select.setTooltip(selectTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(select);

        showRawData = new ToggleButton("", JEConfig.getImage("raw_199316.png", iconSize, iconSize));
        Tooltip showRawDataTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.showrawdata"));
        showRawData.setTooltip(showRawDataTooltip);
        showRawData.setSelected(model.getShowRawData());
        showRawData.styleProperty().bind(
                Bindings
                        .when(showRawData.hoverProperty())
                        .then(
                                new SimpleStringProperty("-fx-background-insets: 1 1 1;"))
                        .otherwise(Bindings
                                .when(showRawData.selectedProperty())
                                .then("-fx-background-insets: 1 1 1;")
                                .otherwise(
                                        new SimpleStringProperty("-fx-background-color: transparent;-fx-background-insets: 0 0 0;"))));

        showSum = new ToggleButton("", JEConfig.getImage("Sum_132399.png", iconSize, iconSize));
        Tooltip showSumTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.showsum"));
        showSum.setTooltip(showSumTooltip);
        showSum.setSelected(model.getShowSum());
        showSum.styleProperty().bind(
                Bindings
                        .when(showSum.hoverProperty())
                        .then(
                                new SimpleStringProperty("-fx-background-insets: 1 1 1;"))
                        .otherwise(Bindings
                                .when(showSum.selectedProperty())
                                .then("-fx-background-insets: 1 1 1;")
                                .otherwise(
                                        new SimpleStringProperty("-fx-background-color: transparent;-fx-background-insets: 0 0 0;"))));

        showL1L2 = new ToggleButton("", JEConfig.getImage("l1l2.png", iconSize, iconSize));
        Tooltip showL1L2Tooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.showl1l2"));
        showL1L2.setTooltip(showL1L2Tooltip);
        showL1L2.setSelected(model.getShowL1L2());
        showL1L2.styleProperty().bind(
                Bindings
                        .when(showL1L2.hoverProperty())
                        .then(
                                new SimpleStringProperty("-fx-background-insets: 1 1 1;"))
                        .otherwise(Bindings
                                .when(showL1L2.selectedProperty())
                                .then("-fx-background-insets: 1 1 1;")
                                .otherwise(
                                        new SimpleStringProperty("-fx-background-color: transparent;-fx-background-insets: 0 0 0;"))));

        calcRegression = new ToggleButton("", JEConfig.getImage("regression.png", iconSize, iconSize));
        Tooltip calcRegressionTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.calcregression"));
        calcRegression.setTooltip(calcRegressionTooltip);
        calcRegression.setSelected(model.calcRegression());
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(calcRegression);

        calcFullLoadHours = new ToggleButton("", JEConfig.getImage("fullload.png", iconSize, iconSize));
        Tooltip calcFullLoadHoursTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.calcfullloadhours"));
        calcFullLoadHours.setTooltip(calcFullLoadHoursTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(calcFullLoadHours);

        calcHoursAboveBelow = new ToggleButton("", JEConfig.getImage("hoursabove.png", iconSize, iconSize));
        Tooltip calcHoursAboveBelowTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.calchoursabovebelow"));
        calcHoursAboveBelow.setTooltip(calcHoursAboveBelowTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(calcHoursAboveBelow);

        calcSumAboveBelow = new ToggleButton("", JEConfig.getImage("sumabove.png", iconSize, iconSize));
        Tooltip calcSumAboveBelowTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.calcsumabovebelow"));
        calcSumAboveBelow.setTooltip(calcSumAboveBelowTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(calcSumAboveBelow);

        customWorkDay = new ToggleButton("", JEConfig.getImage("iconfinder_calendar-clock_299096.png", iconSize, iconSize));
        Tooltip customWorkDayTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.customworkday"));
        customWorkDay.setTooltip(customWorkDayTooltip);
        customWorkDay.setSelected(model.isCustomWorkDay());
        customWorkDay.styleProperty().bind(
                Bindings
                        .when(customWorkDay.hoverProperty())
                        .then(
                                new SimpleStringProperty("-fx-background-insets: 1 1 1;"))
                        .otherwise(Bindings
                                .when(customWorkDay.selectedProperty())
                                .then("-fx-background-insets: 1 1 1;")
                                .otherwise(
                                        new SimpleStringProperty("-fx-background-color: transparent;-fx-background-insets: 0 0 0;"))));

        disableIcons = new ToggleButton("", JEConfig.getImage("1415304498_alert.png", iconSize, iconSize));
        Tooltip disableIconsTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.disableicons"));
        disableIcons.setTooltip(disableIconsTooltip);
        disableIcons.setSelected(model.getShowIcons());
        disableIcons.styleProperty().bind(
                Bindings
                        .when(disableIcons.hoverProperty())
                        .then(
                                new SimpleStringProperty("-fx-background-insets: 1 1 1;"))
                        .otherwise(Bindings
                                .when(disableIcons.selectedProperty())
                                .then("-fx-background-insets: 1 1 1;")
                                .otherwise(
                                        new SimpleStringProperty("-fx-background-color: transparent;-fx-background-insets: 0 0 0;"))));

        addSeriesRunningMean = new ToggleButton("", JEConfig.getImage("1415304498_alert.png", iconSize, iconSize));
        Tooltip addSeriesRunningMeanTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.disableicons"));
        addSeriesRunningMean.setTooltip(addSeriesRunningMeanTooltip);
        addSeriesRunningMean.styleProperty().bind(
                Bindings
                        .when(addSeriesRunningMean.hoverProperty())
                        .then(
                                new SimpleStringProperty("-fx-background-insets: 1 1 1;"))
                        .otherwise(Bindings
                                .when(addSeriesRunningMean.selectedProperty())
                                .then("-fx-background-insets: 1 1 1;")
                                .otherwise(
                                        new SimpleStringProperty("-fx-background-color: transparent;-fx-background-insets: 0 0 0;"))));


        zoomOut = new ToggleButton("", JEConfig.getImage("ZoomOut.png", iconSize, iconSize));
        Tooltip zoomOutTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.zoomout"));
        zoomOut.setTooltip(zoomOutTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(zoomOut);

        helpButton = JEVisHelp.getInstance().buildHelpButtons(iconSize, iconSize);
        infoButton = JEVisHelp.getInstance().buildInfoButtons(iconSize, iconSize);


        if (!_initialized) {
            save.setDisable(false);
            delete.setDisable(false);

            setDisableToolBarIcons(true);

            _initialized = true;
        }
    }

    public Boolean getChanged() {
        return changed;
    }

    public void setChanged(Boolean changed) {
        this.changed = changed;
    }

    public ChartPlugin getGraphPluginView() {
        return chartPlugin;
    }
}
