/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.ChartTypeComboBox;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.ChartNameTextField;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.PickerCombo;
import org.jevis.jeconfig.application.Chart.ChartSettings;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeFactory;
import org.jevis.jeconfig.application.jevistree.TreePlugin;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.plugin.ChartPluginTree;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.NumberSpinner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ChartSelectionDialog {

    private static final Logger logger = LogManager.getLogger(ChartSelectionDialog.class);
    private final JEVisDataSource _ds;
    private Response _response = Response.CANCEL;
    private GraphDataModel data;
    private Stage stage;
    private boolean init = true;
    private JEVisTree tree;
    //    private ObservableList<String> chartsList = FXCollections.observableArrayList();
    private ChartPluginTree chartPlugin = null;
    private Long defaultChartsPerScreen;
    private TabPane tabPaneCharts;

    /**
     * @param ds
     * @param data
     */
    public ChartSelectionDialog(JEVisDataSource ds, GraphDataModel data) {
        this._ds = ds;
        this.data = data;

        this.tree = JEVisTreeFactory.buildDefaultGraphTree(ds, data);

    }

    public Response show() {
        _response = Response.CANCEL;

        if (stage != null) {
            stage.close();
            stage = null;
        }

        stage = new Stage();

        stage.setTitle(I18n.getInstance().getString("graph.selection.title"));

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.initOwner(JEConfig.getStage());

        //1180 for the columns

        double maxScreenWidth = Screen.getPrimary().getBounds().getMaxX();
        stage.setWidth(maxScreenWidth - 20);

        stage.setHeight(768);
        stage.setResizable(true);

        TabPane mainTabPane = new TabPane();
        VBox.setVgrow(mainTabPane, Priority.ALWAYS);

        Tab tabConfiguration = new Tab(I18n.getInstance().getString("graph.tabs.configuration"));
        tabConfiguration.closableProperty().setValue(false);

        VBox selectionBox = new VBox();

        String ICON = "1404313956_evolution-tasks.png";
        Node headerNode = DialogHeader.getDialogHeader(ICON, I18n.getInstance().getString("graph.selection.header"));

        for (TreePlugin plugin : tree.getPlugins()) {
            if (plugin instanceof ChartPluginTree) {
                chartPlugin = (ChartPluginTree) plugin;
//                if (data != null && data.getSelectedData() != null && !data.getSelectedData().isEmpty()) {
                //chartPlugin.setData(data);
//                }
            }
        }

        selectionBox.getChildren().setAll(headerNode, tree);
        VBox.setVgrow(tree, Priority.ALWAYS);

        tabConfiguration.setContent(selectionBox);

        Tab tabChartsSettings = new Tab(I18n.getInstance().getString("graph.tabs.charts"));
        tabChartsSettings.closableProperty().setValue(false);

        VBox vBoxAdvancedSettings = new VBox();

        tabPaneCharts = new TabPane();
        VBox.setVgrow(tabPaneCharts, Priority.ALWAYS);

//        chartsList = data.getChartsList();chartPlugin

        tabPaneCharts.getTabs().add(getCommonTab());

        for (ChartSettings settings : data.getCharts()) {
            tabPaneCharts.getTabs().add(createChartTab(settings));
        }

        vBoxAdvancedSettings.getChildren().add(tabPaneCharts);
        tabChartsSettings.setContent(vBoxAdvancedSettings);

        mainTabPane.getTabs().addAll(tabConfiguration, tabChartsSettings);

        VBox root = new VBox();

        Separator sep = new Separator(Orientation.HORIZONTAL);
        VBox.setVgrow(sep, Priority.NEVER);

        HBox buttonBox = new HBox(10);
        VBox.setVgrow(buttonBox, Priority.NEVER);

        Region spacer = new Region();
        Button ok = new Button(I18n.getInstance().getString("graph.selection.load"));
        Button removeAllSelections = new Button(I18n.getInstance().getString("graph.selection.removeselections"));

        removeAllSelections.setOnAction(event -> {
            try {
                chartPlugin.selectNone();
            } catch (Exception ignored) {
            }
        });

        ok.setDefaultButton(true);

        HBox.setHgrow(removeAllSelections, Priority.NEVER);
        HBox.setHgrow(ok, Priority.NEVER);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setMargin(removeAllSelections, new Insets(10));
        HBox.setMargin(ok, new Insets(10));

        buttonBox.getChildren().setAll(tree.getSearchFilterBar(), spacer, removeAllSelections, ok);

        root.getChildren().addAll(mainTabPane, sep, buttonBox);

        Scene scene = new Scene(root);
        stage.setScene(scene);

        if (data != null && data.getSelectedData() != null && !data.getSelectedData().isEmpty()) {
            List<UserSelection> listUS = new ArrayList<>();
            for (ChartDataModel cdm : data.getSelectedData()) {
                for (int i : cdm.getSelectedcharts()) {
                    for (ChartSettings set : data.getCharts()) {
                        if (set.getId() == i)
                            listUS.add(new UserSelection(UserSelection.SelectionType.Object, cdm.getObject()));
                    }
                }
            }

            if (!listUS.isEmpty()) tree.openUserSelectionNoChildren(listUS);
            else {
                openFirstDataDir();
            }
        } else {
            openFirstDataDir();
        }

        ok.setOnAction(event -> {
            tree.setUserSelectionEnded();
            tree = null;

            _response = Response.OK;

            stage.close();
            stage = null;

        });

        chartPlugin.addedChartProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                tabPaneCharts.getTabs().clear();
                tabPaneCharts.getTabs().add(getCommonTab());
                for (ChartSettings settings : data.getCharts()) {
                    tabPaneCharts.getTabs().add(createChartTab(settings));
                }
            }
        });

        stage.showAndWait();

        return _response;
    }

    private Tab getCommonTab() {
        Tab commonTab = new Tab(I18n.getInstance().getString("graph.tabs.tab.common"));
        commonTab.setClosable(false);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(5);

        Label labelChartsPerScreen = new Label(I18n.getInstance().getString("graph.tabs.tab.chartsperscreen"));

        Long numberOfChartsPerScreen = data.getChartsPerScreen();

        NumberSpinner chartsPerScreen = new NumberSpinner(new BigDecimal(numberOfChartsPerScreen), new BigDecimal(1));

        chartsPerScreen.numberProperty().addListener((observable, oldValue, newValue) -> data.setChartsPerScreen(newValue.longValue()));

        int row = 0;
        gridPane.add(labelChartsPerScreen, 0, row);
        gridPane.add(chartsPerScreen, 1, row);
        row++;

        commonTab.setContent(gridPane);
        return commonTab;
    }

    private void openFirstDataDir() {
        List<UserSelection> listUS = new ArrayList<>();
        JEVisObject firstDataDir = null;
        try {
            JEVisClass classDataDirectory = _ds.getJEVisClass("Data Directory");
            List<JEVisObject> listDataDirectories = _ds.getObjects(classDataDirectory, false);
            if (!listDataDirectories.isEmpty()) firstDataDir = listDataDirectories.get(0);
        } catch (JEVisException e) {

        }
        if (firstDataDir != null) listUS.add(new UserSelection(UserSelection.SelectionType.Object, firstDataDir));
        if (!listUS.isEmpty()) tree.openUserSelection(listUS);
    }

    private Tab createChartTab(ChartSettings cset) {
        Tab newTab = new Tab(cset.getId().toString());
        newTab.setClosable(false);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(7);
        gridPane.setVgap(7);
        gridPane.setPadding(new Insets(4, 4, 4, 4));

        List<ChartDataModel> correspondingDataModels = new ArrayList<>();
        data.getSelectedData().forEach(chartDataModel -> {
            if (chartDataModel.getSelectedcharts().contains(cset.getId())) correspondingDataModels.add(chartDataModel);
        });

        PickerCombo pickerCombo = new PickerCombo(data, correspondingDataModels);
        final ComboBox<TimeFrame> presetDateBox = pickerCombo.getPresetDateBox();
        final JFXDatePicker pickerDateStart = pickerCombo.getStartDatePicker();
        final JFXTimePicker pickerTimeStart = pickerCombo.getStartTimePicker();
        final JFXDatePicker pickerDateEnd = pickerCombo.getEndDatePicker();
        final JFXTimePicker pickerTimeEnd = pickerCombo.getEndTimePicker();

        final Label labelName = new Label(I18n.getInstance().getString("graph.tabs.tab.name"));
        final ChartNameTextField chartNameTextField = new ChartNameTextField(cset);

        final Label labelChartType = new Label(I18n.getInstance().getString("graph.tabs.tab.charttype"));

        final ChartTypeComboBox chartTypeComboBox = new ChartTypeComboBox(cset);

        final Label startText = new Label(I18n.getInstance().getString("plugin.graph.changedate.startdate") + "  ");
        final Label endText = new Label(I18n.getInstance().getString("plugin.graph.changedate.enddate"));
        final Label presetDateBoxLabel = new Label(I18n.getInstance().getString("plugin.graph.analysis.label.standard"));

        int row = 0;
        gridPane.add(labelName, 0, row);
        gridPane.add(chartNameTextField, 1, row);
        row++;

        gridPane.add(labelChartType, 0, row);
        gridPane.add(chartTypeComboBox, 1, row);
        row++;

        gridPane.add(startText, 0, row);
        gridPane.add(pickerTimeStart, 1, row);
        gridPane.add(pickerDateStart, 2, row);
        row++;

        gridPane.add(endText, 0, row);
        gridPane.add(pickerTimeEnd, 1, row);
        gridPane.add(pickerDateEnd, 2, row);
        row++;

        gridPane.add(presetDateBoxLabel, 0, row);
        gridPane.add(presetDateBox, 1, row);


        newTab.setContent(gridPane);

        return newTab;
    }


    public GraphDataModel getSelectedData() {
        return data;
    }

    public void setData(GraphDataModel data) {
        this.data = data;
    }

    public ChartPluginTree getChartPlugin() {
        return chartPlugin;
    }


}
