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

import com.jfoenix.controls.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.chart.BubbleType;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.ChartTypeComboBox;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.ColorMappingBox;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.OrientationBox;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.ChartNameTextField;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.PickerCombo;
import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeFactory;
import org.jevis.jeconfig.application.jevistree.TreePlugin;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.jevistree.plugin.ChartPluginTree;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.tool.NumberSpinner;
import org.jevis.jeconfig.tool.ToggleSwitchPlus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ChartSelectionDialog extends JFXDialog {

    private static final Logger logger = LogManager.getLogger(ChartSelectionDialog.class);
    private final JEVisDataSource _ds;
    private final TabPane tabPaneCharts;
    private AnalysisDataModel data;
    private final boolean init = true;
    private JEVisTree tree;
    //    private ObservableList<String> chartsList = FXCollections.observableArrayList();
    private ChartPluginTree chartPlugin = null;
    private Long defaultChartsPerScreen;
    private Response response = Response.CANCEL;

    /**
     * @param ds
     * @param data
     */
    public ChartSelectionDialog(StackPane dialogContainer, JEVisDataSource ds, AnalysisDataModel data) {
        super();

        setDialogContainer(dialogContainer);

        this._ds = ds;
        this.data = data;

        this.tree = JEVisTreeFactory.buildDefaultGraphTree(ds, data);

        response = Response.CANCEL;

        //1180 for the columns

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

        for (ChartSetting settings : data.getCharts().getListSettings()) {
            tabPaneCharts.getTabs().add(createChartTab(settings));
        }

        vBoxAdvancedSettings.getChildren().add(tabPaneCharts);
        tabChartsSettings.setContent(vBoxAdvancedSettings);

        mainTabPane.getTabs().addAll(tabConfiguration, tabChartsSettings);

        VBox vBox = new VBox(4);

        Separator sep = new Separator(Orientation.HORIZONTAL);
        VBox.setVgrow(sep, Priority.NEVER);

        HBox buttonBox = new HBox(10);
        VBox.setVgrow(buttonBox, Priority.NEVER);

        Region spacer = new Region();
        JFXButton cancel = new JFXButton(I18n.getInstance().getString("graph.dialog.cancel"));
        JFXButton ok = new JFXButton(I18n.getInstance().getString("graph.selection.load"));
        JFXButton removeAllSelections = new JFXButton(I18n.getInstance().getString("graph.selection.removeselections"));

        removeAllSelections.setOnAction(event -> {
            try {
                chartPlugin.selectNone();
            } catch (Exception ignored) {
            }
        });

        cancel.setCancelButton(true);
        ok.setDefaultButton(true);

        HBox.setHgrow(removeAllSelections, Priority.NEVER);
        HBox.setHgrow(cancel, Priority.NEVER);
        HBox.setHgrow(ok, Priority.NEVER);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setMargin(removeAllSelections, new Insets(10));
        HBox.setMargin(cancel, new Insets(10));
        HBox.setMargin(ok, new Insets(10));

        buttonBox.getChildren().setAll(tree.getSearchFilterBar(), spacer, removeAllSelections, cancel, ok);

        vBox.getChildren().addAll(mainTabPane, sep, buttonBox);

        setContent(vBox);

        if (data != null && data.getSelectedData() != null && !data.getSelectedData().isEmpty()) {
            List<UserSelection> listUS = new ArrayList<>();
            for (ChartDataRow cdm : data.getSelectedData()) {
                for (int i : cdm.getSelectedcharts()) {
                    for (ChartSetting set : data.getCharts().getListSettings()) {
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
            response = Response.OK;
            this.close();
        });

        cancel.setOnAction(event -> {
            tree.setUserSelectionEnded();
            tree = null;
            response = Response.CANCEL;
            this.close();
        });

        chartPlugin.addedChartProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                tabPaneCharts.getTabs().clear();
                tabPaneCharts.getTabs().add(getCommonTab());
                for (ChartSetting settings : data.getCharts().getListSettings()) {
                    tabPaneCharts.getTabs().add(createChartTab(settings));
                }
            }
        });

        JEVisHelp.getInstance().setActiveSubModule(ChartSelectionDialog.class.getSimpleName());
        JEVisHelp.getInstance().update();

        removeEmptyCharts();
    }

    private void removeEmptyCharts() {
        List<ChartSetting> toBeRemoved = new ArrayList<>();

        data.getCharts().getListSettings().forEach(chartSettings -> {
            boolean hasData = data.getSelectedData().stream().anyMatch(model -> model.getSelectedcharts().contains(chartSettings.getId()));
            if (!hasData) {
                toBeRemoved.add(chartSettings);
            }
        });

        data.getCharts().getListSettings().removeAll(toBeRemoved);
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

        Label labelHorizontalPies = new Label(I18n.getInstance().getString("graph.tabs.tab.horizontalpies"));
        Long numberOfHorizontalPies = data.getHorizontalPies();
        NumberSpinner horizontalPies = new NumberSpinner(new BigDecimal(numberOfHorizontalPies), new BigDecimal(1));
        horizontalPies.numberProperty().addListener((observable, oldValue, newValue) -> data.setHorizontalPies(newValue.longValue()));

        Label labelHorizontalTables = new Label(I18n.getInstance().getString("graph.tabs.tab.horizontaltables"));
        Long numberOfHorizontalTables = data.getHorizontalTables();
        NumberSpinner horizontalTables = new NumberSpinner(new BigDecimal(numberOfHorizontalTables), new BigDecimal(1));
        horizontalTables.numberProperty().addListener((observable, oldValue, newValue) -> data.setHorizontalTables(newValue.longValue()));

        int row = 0;
        gridPane.add(labelChartsPerScreen, 0, row);
        gridPane.add(chartsPerScreen, 1, row);
        row++;

        gridPane.add(labelHorizontalPies, 0, row);
        gridPane.add(horizontalPies, 1, row);
        row++;

        gridPane.add(labelHorizontalTables, 0, row);
        gridPane.add(horizontalTables, 1, row);
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

    private Tab createChartTab(ChartSetting cset) {
        Tab newTab = new Tab(I18n.getInstance().getString("graph.table.selectchart") + cset.getId() + 1);
        newTab.setClosable(false);
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(7);
        gridPane.setVgap(7);
        gridPane.setPadding(new Insets(4, 4, 4, 4));

        List<ChartDataRow> correspondingDataModels = new ArrayList<>();
        data.getSelectedData().forEach(chartDataModel -> {
            if (chartDataModel.getSelectedcharts().contains(cset.getId())) correspondingDataModels.add(chartDataModel);
        });

        PickerCombo pickerCombo = new PickerCombo(data, correspondingDataModels, false);
        final JFXComboBox<AnalysisTimeFrame> presetDateBox = pickerCombo.getPresetDateBox();
        final JFXDatePicker pickerDateStart = pickerCombo.getStartDatePicker();
        final JFXTimePicker pickerTimeStart = pickerCombo.getStartTimePicker();
        final JFXDatePicker pickerDateEnd = pickerCombo.getEndDatePicker();
        final JFXTimePicker pickerTimeEnd = pickerCombo.getEndTimePicker();

        final Label labelName = new Label(I18n.getInstance().getString("graph.tabs.tab.name"));
        final ChartNameTextField chartNameTextField = new ChartNameTextField(cset);

        final Label labelChartType = new Label(I18n.getInstance().getString("graph.tabs.tab.charttype"));
        final ChartTypeComboBox chartTypeComboBox = new ChartTypeComboBox(cset);

        final Label labelGroupingInterval = new Label(I18n.getInstance().getString("graph.tabs.tab.groupinginterval"));
        Long gi = cset.getGroupingInterval();
        if (gi == null) {
            gi = 30L;
        }
        final NumberSpinner groupingInterval = new NumberSpinner(new BigDecimal(gi), new BigDecimal(1));
        groupingInterval.numberProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                cset.setGroupingInterval(newValue.longValue());
            }
        });

        Label labelColorMapping = null;
        ColorMappingBox colorMappingBox = null;
        if (cset.getChartType() == ChartType.HEAT_MAP) {
            labelColorMapping = new Label(I18n.getInstance().getString("plugin.graph.tabs.tab.colormapping"));
            colorMappingBox = new ColorMappingBox(cset);
        }

        Label orientationLabel = null;
        OrientationBox orientationBox = null;
        if (cset.getChartType() == ChartType.TABLE) {
            orientationLabel = new Label(I18n.getInstance().getString("plugin.graph.tabs.tab.orientation"));
            orientationBox = new OrientationBox(cset);
        }

        final Label startText = new Label(I18n.getInstance().getString("plugin.graph.changedate.startdate") + "  ");
        final Label endText = new Label(I18n.getInstance().getString("plugin.graph.changedate.enddate"));
        final Label presetDateBoxLabel = new Label(I18n.getInstance().getString("plugin.graph.analysis.label.standard"));

        FlowPane flowPane = new FlowPane();
        flowPane.setPadding(new Insets(4, 4, 4, 4));
        for (ChartDataRow model : correspondingDataModels) {
            GridPane gp = new GridPane();
            gp.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.THIN)));
            gp.setHgap(4);
            gp.setVgap(4);
            gp.setPadding(new Insets(4, 0, 4, 0));
            final Label modelLabel = new Label(model.getObject().getName());
            final Label isEnPILabel = new Label(I18n.getInstance().getString("plugin.graph.chart.selectiondialog.usecalc"));
            final Label calculationLabel = new Label(I18n.getInstance().getString("plugin.graph.chart.selectiondialog.calculation"));
            final Label bubbleTypeLabel = new Label("Bubble Type");

            final JFXComboBox<BubbleType> bubbleTypeComboBox = new JFXComboBox<>(FXCollections.observableArrayList(BubbleType.X, BubbleType.Y));

            bubbleTypeComboBox.getSelectionModel().select(model.getBubbleType());

            bubbleTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                model.setBubbleType(newValue);
            });

            SimpleStringProperty targetProperty = new SimpleStringProperty();

            if (model.getCalculationObject() != null) {
                targetProperty.set(model.getCalculationObject().getID().toString());
            }

            final ToggleSwitchPlus isEnPI = new ToggleSwitchPlus();
            isEnPI.selectedProperty().setValue(model.getEnPI());
            isEnPI.selectedProperty().addListener((observable, oldValue, newValue) -> model.setEnPI(isEnPI.isSelected()));
            HBox targetSelector = getTargetSelector(targetProperty.get(), model);

            int row = 0;
            gp.add(modelLabel, 0, row);
            row++;

            gp.add(isEnPILabel, 0, row);
            gp.add(isEnPI, 1, row);
            row++;

            gp.add(calculationLabel, 0, row);
            gp.add(targetSelector, 1, row);
            row++;

            gp.add(bubbleTypeLabel, 0, row);
            gp.add(bubbleTypeComboBox, 1, row);


            flowPane.getChildren().add(gp);
        }


        int row = 0;
        gridPane.add(labelName, 0, row);
        gridPane.add(chartNameTextField, 1, row);
        row++;

        gridPane.add(labelChartType, 0, row);
        gridPane.add(chartTypeComboBox, 1, row);
        row++;

        if (cset.getChartType() == ChartType.HEAT_MAP) {
            gridPane.add(labelColorMapping, 0, row);
            gridPane.add(colorMappingBox, 1, row);
            row++;
        }

        if (cset.getChartType() == ChartType.TABLE) {
            gridPane.add(orientationLabel, 0, row);
            gridPane.add(orientationBox, 1, row);
            row++;
        }

        if (cset.getChartType() == ChartType.BUBBLE) {
            gridPane.add(labelGroupingInterval, 0, row);
            gridPane.add(groupingInterval, 1, row);
            row++;
        }

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
        row++;

        gridPane.add(flowPane, 0, row, 3, 2);


        scrollPane.setContent(gridPane);
        newTab.setContent(scrollPane);

        return newTab;
    }


    public AnalysisDataModel getSelectedData() {
        return data;
    }

    public void setData(AnalysisDataModel data) {
        this.data = data;
    }

    public ChartPluginTree getChartPlugin() {
        return chartPlugin;
    }

    private HBox getTargetSelector(String value, ChartDataRow model) {
        HBox limitDataBox = new HBox();
        JFXButton treeButton = new JFXButton(I18n
                .getInstance().getString("plugin.object.attribute.target.button"),
                JEConfig.getImage("folders_explorer.png", 18, 18));

        JFXButton gotoButton = new JFXButton(I18n.getInstance().getString("plugin.object.attribute.target.goto"),
                JEConfig.getImage("1476393792_Gnome-Go-Jump-32.png", 18, 18));//icon
        gotoButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.attribute.target.goto.tooltip")));

        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        limitDataBox.setSpacing(10);
        limitDataBox.getChildren().setAll(treeButton, gotoButton, rightSpacer);

        if (value != null && !value.equals("")) {
            try {

                TargetHelper th = new TargetHelper(_ds, value);

                if (th.isValid() && th.targetAccessible()) {

                    StringBuilder bText = new StringBuilder();

                    for (JEVisObject obj : th.getObject()) {
                        int index = th.getObject().indexOf(obj);
                        if (index > 0) bText.append("; ");

                        bText.append("[");
                        bText.append(obj.getID());
                        bText.append("] ");
                        bText.append(obj.getName());

                        if (th.hasAttribute()) {

                            bText.append(" - ");
                            bText.append(th.getAttribute().get(index).getName());

                        }
                    }

                    treeButton.setText(bText.toString());
                }

            } catch (Exception ex) {
                logger.catching(ex);
            }
        }

        treeButton.setOnAction(event -> {
            TargetHelper th = null;
            if (value != null && !value.equals("")) {
                th = new TargetHelper(_ds, value);
            }

            List<JEVisTreeFilter> allFilter = new ArrayList<>();
            JEVisTreeFilter allDataFilter = SelectTargetDialog.buildClassFilter(_ds, "Calculation");
            allFilter.add(allDataFilter);

            List<UserSelection> openList = new ArrayList<>();
            if (th != null && !th.getAttribute().isEmpty()) {
                for (JEVisAttribute att : th.getAttribute())
                    openList.add(new UserSelection(UserSelection.SelectionType.Attribute, att, null, null));
            } else if (th != null && !th.getObject().isEmpty()) {
                for (JEVisObject obj : th.getObject())
                    openList.add(new UserSelection(UserSelection.SelectionType.Object, obj));
            }

            SelectTargetDialog selectTargetDialog = new SelectTargetDialog(getDialogContainer(), allFilter, allDataFilter, null, SelectionMode.SINGLE, _ds, openList);

            selectTargetDialog.setOnDialogClosed(event1 -> {
                if (selectTargetDialog.getResponse() == SelectTargetDialog.Response.OK) {
                    logger.trace("Selection Done");

                    StringBuilder newTarget = new StringBuilder();
                    List<UserSelection> selections = selectTargetDialog.getUserSelection();
                    for (UserSelection us : selections) {
                        int index = selections.indexOf(us);
                        if (index > 0) newTarget.append(";");

                        newTarget.append(us.getSelectedObject().getID());
                    }

                    treeButton.setText(newTarget.toString());
                    model.setCalculationObject(newTarget.toString());
                }
            });
            selectTargetDialog.show();
        });
        return limitDataBox;
    }

    public Response getResponse() {
        return response;
    }
}
