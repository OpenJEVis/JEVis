package org.jevis.jeconfig.application.Chart.ChartPluginElements.tabs;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartElements.ColorTable;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.ChartTypeComboBox;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.ColorMappingBox;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.OrientationBox;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.SelectionTable.Table;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.TreeSelectionDialog;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.data.ChartData;
import org.jevis.jeconfig.application.Chart.data.ChartModel;
import org.jevis.jeconfig.application.jevistree.methods.DataMethods;
import org.jevis.jeconfig.dialog.Response;
import org.jevis.jeconfig.tool.NumberSpinner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ChartTab extends Tab {
    private static final Logger logger = LogManager.getLogger(ChartTab.class);
    private final JEVisDataSource ds;
    private final NumberSpinner minFractionDigits;
    private final NumberSpinner maxFractionDigits;
    private final StackPane dialogContainer;
    private final Label minFractionDigitsLabel = new Label(I18n.getInstance().getString("plugin.graph.chart.selectiondialog.minfractiondigits"));
    private final Label maxFractionDigitsLabel = new Label(I18n.getInstance().getString("plugin.graph.chart.selectiondialog.maxfractiondigits"));
    private final Label labelChartType = new Label(I18n.getInstance().getString("graph.tabs.tab.charttype"));
    private final ChartTypeComboBox chartTypeComboBox;
    private final Label labelGroupingInterval = new Label(I18n.getInstance().getString("graph.tabs.tab.groupinginterval"));
    private final Table chartTable;
    private final GridPane chartSettings = new GridPane();
    private final VBox vBox = new VBox();
    private final ToolBar tableMenu;
    private final NumberSpinner groupingInterval;
    private final List<JEVisClass> dataProcessorClasses = new ArrayList<>();
    private final List<JEVisClass> allDataClasses = new ArrayList<>();
    private final List<Color> usedColors = new ArrayList<>();
    private final int iconSize = 12;
    private final ToggleButton newB = new ToggleButton("", JEConfig.getSVGImage(Icon.PLUS, this.iconSize, this.iconSize));
    private final ToggleButton delete = new ToggleButton("", JEConfig.getSVGImage(Icon.DELETE, this.iconSize, this.iconSize));
    private final Label orientationLabel = new Label(I18n.getInstance().getString("plugin.graph.tabs.tab.orientation"));
    private final OrientationBox orientationBox;
    private final Label labelColorMapping = new Label(I18n.getInstance().getString("plugin.graph.tabs.tab.colormapping"));
    private final ColorMappingBox colorMappingBox;
    private ChartModel chartModel;
    private final ChangeListener<BigDecimal> groupingIntervalChangeListener = (observable, oldValue, newValue) -> {
        if (chartModel != null && !newValue.equals(oldValue)) {
            this.chartModel.setGroupingInterval(newValue.doubleValue());
        }
    };
    private final ChangeListener<BigDecimal> minFractionDigitsChangeListener = (observable, oldValue, newValue) -> {
        if (chartModel != null && !newValue.equals(oldValue)) {
            chartModel.setMinFractionDigits(newValue.intValue());
        }
    };
    private final ChangeListener<BigDecimal> maxFractionChangeListener = (observable, oldValue, newValue) -> {
        if (chartModel != null && !newValue.equals(oldValue)) {
            chartModel.setMaxFractionDigits(newValue.intValue());
        }
    };
    private JEVisClass dataClass;
    private JEVisClass cleanDataClass;
    private JEVisClass mathDataClass;
    private JEVisClass baseDataClass;

    public ChartTab(StackPane dialogContainer, JEVisDataSource ds, ChartModel chartModel) {
        super();
        this.ds = ds;
        this.chartModel = chartModel;
        this.dialogContainer = dialogContainer;
        this.tableMenu = new ToolBar(newB, delete);
        this.chartTable = new Table(dialogContainer, chartModel);
        boolean hasCustomIntervalEnabled = chartModel.getChartData().stream().anyMatch(ChartData::isIntervalEnabled);
        setIntervalStartColumnVisible(hasCustomIntervalEnabled);
        setIntervalEndColumnVisible(hasCustomIntervalEnabled);

        initializeClasses(ds);

        setClosable(true);

        chartSettings.setHgap(7);
        chartSettings.setVgap(7);
        chartSettings.setPadding(new Insets(4, 4, 4, 4));

        Label nameLabel = new Label();
        nameLabel.textProperty().bind(chartModel.chartNameProperty());
        setGraphic(nameLabel);

        JFXTextField chartNameTextField = new JFXTextField();
        nameLabel.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                chartNameTextField.setText(nameLabel.getText());
                setGraphic(chartNameTextField);
                chartNameTextField.selectAll();
                chartNameTextField.requestFocus();
            }
        });

        chartNameTextField.setOnAction(event -> {
            chartModel.setChartName(chartNameTextField.getText());
            setGraphic(nameLabel);
        });

        chartNameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                chartModel.setChartName(chartNameTextField.getText());
                setGraphic(nameLabel);
            }
        });

        chartTypeComboBox = new ChartTypeComboBox(chartModel);
        chartTypeComboBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, s, t1) -> {
            ChartType chartType = ChartType.parseChartType(t1.intValue());
            chartModel.setChartType(chartType);

            updateChartSettings(chartModel, chartSettings);
        });

        Double gi = chartModel.getGroupingInterval();
        if (gi == null) {
            gi = 30d;
        }
        groupingInterval = new NumberSpinner(new BigDecimal(gi), new BigDecimal(0.1));

        colorMappingBox = new ColorMappingBox(chartModel);

        orientationBox = new OrientationBox(chartModel);

        int minFracs = chartModel.getMinFractionDigits();
        minFractionDigits = new NumberSpinner(new BigDecimal(minFracs), new BigDecimal(1));

        int maxFracs = chartModel.getMaxFractionDigits();
        maxFractionDigits = new NumberSpinner(new BigDecimal(maxFracs), new BigDecimal(1));

        updateChartSettings(chartModel, chartSettings);

        chartTable.getItems().setAll(chartModel.getChartData());
        VBox.setVgrow(chartTable, Priority.ALWAYS);

        vBox.getChildren().setAll(chartSettings, tableMenu, chartTable);
        vBox.setSpacing(6);
        vBox.setPadding(new Insets(4, 4, 4, 4));
        VBox.setVgrow(chartSettings, Priority.ALWAYS);
        VBox.setVgrow(chartTable, Priority.ALWAYS);

        newB.setOnAction(actionEvent -> {

            List<JEVisClass> filterClasses = new ArrayList<>();
            filterClasses.add(dataClass);
            filterClasses.add(cleanDataClass);
            filterClasses.add(mathDataClass);
            filterClasses.add(baseDataClass);

            TreeSelectionDialog selectTargetDialog = new TreeSelectionDialog(dialogContainer, ds, filterClasses, SelectionMode.MULTIPLE);

            selectTargetDialog.setOnDialogClosed(event -> {
                try {
                    if (selectTargetDialog.getResponse() == Response.OK) {

                        for (JEVisObject object : selectTargetDialog.getTreeView().getSelectedObjects()) {

                            ChartData chartData = new ChartData();
                            chartData.setId(object.getID());
                            chartData.setObjectName(object);
                            chartData.setAttributeString("Value");
                            chartData.setUnit(object.getAttribute("Value").getDisplayUnit());
                            chartData.setColor(ColorTable.getNextColor(usedColors));
                            chartData.setChartType(ChartType.DEFAULT);

                            chartModel.getChartData().add(chartData);
                            chartTable.getItems().add(chartData);
                        }
                    }
                } catch (Exception ex) {
                    logger.error(ex);
                }
            });
            selectTargetDialog.show();
        });

        delete.setOnAction(actionEvent -> {
            ObservableList<ChartData> selectedItems = chartTable.getSelectionModel().getSelectedItems();
            chartModel.getChartData().removeAll(selectedItems);
            chartTable.getItems().removeAll(selectedItems);

            chartTable.refresh();
        });

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(vBox);

        setContent(borderPane);
    }

    private void updateChartSettings(ChartModel chartModel, GridPane chartSettings) {

        int row = 0;
        chartSettings.getChildren().clear();

        chartSettings.add(labelChartType, 0, row);
        chartSettings.add(chartTypeComboBox, 1, row);
        row++;

        chartSettings.add(minFractionDigitsLabel, 0, row);
        chartSettings.add(minFractionDigits, 1, row);
        row++;

        chartSettings.add(maxFractionDigitsLabel, 0, row);
        chartSettings.add(maxFractionDigits, 1, row);
        row++;

        if (chartModel.getChartType() == ChartType.HEAT_MAP) {
            chartSettings.add(labelColorMapping, 0, row);
            chartSettings.add(colorMappingBox, 1, row);
            row++;
        }

        if (chartModel.getChartType() == ChartType.TABLE) {
            chartSettings.add(orientationLabel, 0, row);
            chartSettings.add(orientationBox, 1, row);
            row++;
        }

        if (chartModel.getChartType() == ChartType.BUBBLE) {
            chartSettings.add(labelGroupingInterval, 0, row);
            chartSettings.add(groupingInterval, 1, row);

            setBubbleTypeColumnVisible(true);

            row++;
        } else {
            setBubbleTypeColumnVisible(false);
        }
    }

    private void enableListener() {
        groupingInterval.numberProperty().addListener(groupingIntervalChangeListener);
        minFractionDigits.numberProperty().addListener(minFractionDigitsChangeListener);
        maxFractionDigits.numberProperty().addListener(maxFractionChangeListener);
    }

    private void disableListener() {
        groupingInterval.numberProperty().removeListener(groupingIntervalChangeListener);
        minFractionDigits.numberProperty().removeListener(minFractionDigitsChangeListener);
        maxFractionDigits.numberProperty().addListener(maxFractionChangeListener);
    }

    private void initializeClasses(JEVisDataSource ds) {
        try {
            dataClass = ds.getJEVisClass("Data");
            cleanDataClass = ds.getJEVisClass("Clean Data");
            mathDataClass = ds.getJEVisClass("Math Data");
            baseDataClass = ds.getJEVisClass("Base Data");

            dataProcessorClasses.add(cleanDataClass);
            dataProcessorClasses.add(mathDataClass);
            dataProcessorClasses.add(baseDataClass);

            allDataClasses.add(dataClass);
            allDataClasses.addAll(dataProcessorClasses);
        } catch (JEVisException e) {
            logger.error("Could not initialize Classes", e);
        }
    }

    void setButtonText(JFXButton targetButton, JEVisObject target) {

        try {
            if (target != null) {

                StringBuilder bText = new StringBuilder();

                JEVisClass cleanData = ds.getJEVisClass("Clean Data");

                if (target.getJEVisClass().equals(cleanData)) {
                    JEVisObject firstParentalDataObject = DataMethods.getFirstParentalDataObject(target);
                    if (firstParentalDataObject != null) {
                        bText.append("[");
                        bText.append(firstParentalDataObject.getID());
                        bText.append("] ");
                        bText.append(firstParentalDataObject.getName());
                        bText.append(" / ");

                    }
                }

                bText.append("[");
                bText.append(target.getID());
                bText.append("] ");
                bText.append(target.getName());

                Platform.runLater(() -> targetButton.setText(bText.toString()));
            }

        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void setCommonChartSettingsVisible(boolean visible) {
        if (visible) {
            Platform.runLater(() -> vBox.getChildren().setAll(chartSettings, tableMenu, chartTable));
        } else {
            Platform.runLater(() -> vBox.getChildren().setAll(tableMenu, chartTable));
        }
    }

    public void setObjectNameColumnVisible(boolean visible) {
        chartTable.getObjectNameColumn().setVisible(visible);
    }

    public void setNameColumnVisible(boolean visible) {
        chartTable.getNameColumn().setVisible(visible);
    }

    public void setChartTypeColumnVisible(boolean visible) {
        chartTable.getChartTypeColumn().setVisible(visible);
    }

    public void setColorColumnVisible(boolean visible) {
        chartTable.getColorColumn().setVisible(visible);
    }

    public void setUnitColumnVisible(boolean visible) {
        chartTable.getUnitColumn().setVisible(visible);
    }

    public void setIntervalColumnVisible(boolean visible) {
        chartTable.getIntervalEnabledColumn().setVisible(visible);
    }

    public void setIntervalStartColumnVisible(boolean visible) {
        chartTable.getIntervalStartColumn().setVisible(visible);
    }

    public void setIntervalEndColumnVisible(boolean visible) {
        chartTable.getIntervalEndColumn().setVisible(visible);
    }

    public void setAggregationPeriodColumnVisible(boolean visible) {
        chartTable.getAggregationPeriodColumn().setVisible(visible);
    }

    public void setManipulationModeColumnVisible(boolean visible) {
        chartTable.getManipulationModeColumn().setVisible(visible);
    }

    public void setAxisColumnVisible(boolean visible) {
        chartTable.getAxisColumn().setVisible(visible);
    }

    public void setBubbleTypeColumnVisible(boolean visible) {
        chartTable.getBubbleTypeColumn().setVisible(visible);
    }

    public void setMathColumnVisible(boolean visible) {
        chartTable.getMathColumn().setVisible(visible);
    }

    public void setCssColumnVisible(boolean visible) {
        chartTable.getCssColumn().setVisible(visible);
    }

    public void setMenuVisible(boolean visible) {
        chartTable.setTableMenuButtonVisible(visible);
    }
}
