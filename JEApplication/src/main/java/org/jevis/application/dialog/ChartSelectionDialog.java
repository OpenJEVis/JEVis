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
package org.jevis.application.dialog;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jevis.api.JEVisDataSource;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.application.jevistree.JEVisTree;
import org.jevis.application.jevistree.JEVisTreeFactory;
import org.jevis.application.jevistree.TreePlugin;
import org.jevis.application.jevistree.plugin.ChartDataModel;
import org.jevis.application.jevistree.plugin.ChartPlugin;
import org.jevis.application.object.tree.UserSelection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ChartSelectionDialog {

    private SaveResourceBundle rb = new SaveResourceBundle("jeapplication", AppLocale.getInstance().getLocale());

    private Response _response = Response.CANCEL;

    private final JEVisDataSource _ds;
    private final String ICON = "1404313956_evolution-tasks.png";
    private Map<String, ChartDataModel> data = new HashMap<>();
    private Stage stage;
    private boolean init = true;
    private JEVisTree _tree;
    private ObservableList<String> chartsList = FXCollections.observableArrayList();

    public ChartSelectionDialog(JEVisDataSource ds) {
        _ds = ds;
    }

    public Response show(Stage owner) {
        _response = Response.CANCEL;

        if (stage != null) {
            stage.close();
            stage = null;
        }

        stage = new Stage();

        stage.setTitle(rb.getString("graph.selection.title"));

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.initOwner(owner);

        stage.setWidth(1124);
        stage.setHeight(768);
        stage.setResizable(false);

        TabPane tabpane = new TabPane();

        Tab tabConfiguration = new Tab(rb.getString("graph.tabs.configuration"));
        tabConfiguration.closableProperty().setValue(false);

        VBox root = new VBox();

        Node headerNode = DialogHeader.getDialogHeader(ICON, rb.getString("graph.selection.header"));

        Separator sep = new Separator(Orientation.HORIZONTAL);

        AnchorPane treePane = new AnchorPane();

        JEVisTree tree = getTree();
        treePane.getChildren().setAll(tree);
        AnchorPane.setTopAnchor(tree, 0d);
        AnchorPane.setRightAnchor(tree, 0d);
        AnchorPane.setBottomAnchor(tree, 0d);
        AnchorPane.setLeftAnchor(tree, 0d);

        HBox buttonBox = new HBox(10);
        Region spacer = new Region();
        Button ok = new Button(rb.getString("graph.selection.load"));
        ok.setDefaultButton(true);

        HBox.setHgrow(ok, Priority.NEVER);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setMargin(ok, new Insets(10));
        buttonBox.getChildren().setAll(spacer, ok);
        root.getChildren().setAll(headerNode, treePane, sep, buttonBox);

        VBox.setVgrow(treePane, Priority.ALWAYS);
        VBox.setVgrow(sep, Priority.NEVER);
        VBox.setVgrow(buttonBox, Priority.NEVER);

//        stage.getIcons().setAll(ResourceLoader.getImage(ICON, 64, 64).getImage());
        ChartPlugin bp = null;
        for (TreePlugin plugin : tree.getPlugins()) {
            if (plugin instanceof ChartPlugin) {
                bp = (ChartPlugin) plugin;
            }
        }

        if (!data.isEmpty()) bp.set_data(data);

        tabConfiguration.setContent(root);

        Tab tabChartsSettings = new Tab(rb.getString("graph.tabs.charts"));
        tabChartsSettings.closableProperty().setValue(false);

        VBox vboxCharts = new VBox();

        TabPane tabPaneCharts = new TabPane();

        getChartsList();
        for (String s : chartsList) {
            tabPaneCharts.getTabs().add(getChartTab(s));
        }

        chartsList.addListener((ListChangeListener<? super String>) c -> {
            if (c.wasAdded() || c.wasRemoved() || c.wasUpdated()) {
                tabPaneCharts.getTabs().clear();
                for (String s : chartsList) {
                    tabPaneCharts.getTabs().add(getChartTab(s));
                }
            }
        });

        vboxCharts.getChildren().add(tabPaneCharts);
        tabChartsSettings.setContent(vboxCharts);

        tabpane.getTabs().addAll(tabConfiguration, tabChartsSettings);
        Scene scene = new Scene(tabpane);
        stage.setScene(scene);

        List<UserSelection> listUS = new ArrayList<>();
        for (Map.Entry<String, ChartDataModel> entry : data.entrySet()) {
            ChartDataModel mdl = entry.getValue();
            if (mdl.getSelected()) listUS.add(new UserSelection(UserSelection.SelectionType.Object, mdl.getObject()));
        }

        if (!listUS.isEmpty()) _tree.openUserSelection(listUS);

        final ChartPlugin finalBp = bp;
        ok.setOnAction(event -> {
            tree.setUserSelectionEnded();
            _response = Response.OK;

            data = finalBp.getSelectedData();

            stage.hide();
        });

        stage.showAndWait();

        return _response;
    }

    private Tab getChartTab(String s) {
        final String currentChart = s;
        Tab newTab = new Tab(s);
        newTab.setClosable(false);

        GridPane gp = new GridPane();

        Label labelName = new Label(rb.getString("graph.tabs.tab.name"));
        TextField textFieldName = new TextField();
        textFieldName.setText(s);

        textFieldName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {
                for (String ch : chartsList) {
                    if (ch.contains(currentChart)) ch = newValue;
                }
            }
        });

        Label labelChartType = new Label(rb.getString("graph.tabs.tab.charttype"));

        ObservableList<String> listChartTypes = FXCollections.observableArrayList();
        ComboBox<String> boxChartType = new ComboBox<>(listChartTypes);

        gp.add(labelName, 0, 1);
        gp.add(textFieldName, 1, 1);

        gp.add(labelChartType, 0, 3);
        gp.add(boxChartType, 1, 3);

        newTab.setContent(gp);

        return newTab;
    }

    public ObservableList<String> getChartsList() {
        List<String> tempList = new ArrayList<>();
        for (Map.Entry<String, ChartDataModel> mdl : data.entrySet()) {
            if (!tempList.contains(mdl.getValue().getTitle())) tempList.add(mdl.getValue().getTitle());
        }

        chartsList = FXCollections.observableArrayList(tempList);
        return chartsList;
    }

    public JEVisTree getTree() {
        if (!init) {
            return _tree;
        }

        _tree = JEVisTreeFactory.buildDefaultGraphTree(_ds);
        init = false;

        return _tree;
    }

    public enum Response {
        OK, CANCEL
    }

    public Map<String, ChartDataModel> getSelectedData() {
        return data;
    }

    public void setData(Map<String, ChartDataModel> data) {
        this.data = data;
    }
}
