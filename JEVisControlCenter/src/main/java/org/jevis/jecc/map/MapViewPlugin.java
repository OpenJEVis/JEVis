/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecc.map;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.GlobalToolBar;
import org.jevis.jecc.Icon;
import org.jevis.jecc.Plugin;
import org.jevis.jecc.application.jevistree.plugin.MapPlugin;
import org.jevis.jecc.dialog.MapSelectionDialog;
import org.jxmapviewer.JXMapViewer;

import javax.swing.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author broder
 */
public class MapViewPlugin implements Plugin {
    private static final Logger logger = LogManager.getLogger(MapViewPlugin.class);
    private final StringProperty name = new SimpleStringProperty(I18n.getInstance().getString("plugin.map.title"));
    private final StringProperty id = new SimpleStringProperty("*NO_ID*");
    private final BooleanProperty firstStartProperty = new SimpleBooleanProperty(true);
    private final String tooltip = I18n.getInstance().getString("pluginmanager.mapview.tooltip");
    private JEVisDataSource ds;
    private JXMapViewer mapViewer;
    private ToolBar toolBar;
    private TableView table;
    private ComboBox comboBox;

    private Map<String, GPSRoute> routeData;

    public MapViewPlugin(JEVisDataSource ds, String name) {
        this.ds = ds;
        this.name.set(name);
    }

    @Override
    public void setHasFocus() {
        if (firstStartProperty.getValue()) {
            firstStartProperty.setValue(Boolean.FALSE);
            MapCreator mapCreator = new MapCreator(mapViewer);
            mapCreator.drawEmtyMap();
        }

    }

    @Override
    public void lostFocus() {

    }

    @Override
    public void openObject(Object object) {

    }

    @Override
    public int getPrefTapPos() {
        return 90;
    }

    @Override
    public String getClassName() {
        return "Map Plugin";
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public void setName(String name) {
        this.name.set(name);
    }

    @Override
    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public String getUUID() {
        return id.get();
    }

    @Override
    public void setUUID(String id) {
        this.id.set(id);
    }

    @Override
    public String getToolTip() {
        return tooltip;
    }

    @Override
    public StringProperty uuidProperty() {
        return id;
    }

    @Override
    public Node getMenu() {
        return null;
    }

    @Override
    public Node getToolbar() {
        if (toolBar == null) {
            toolBar = new ToolBar();
            toolBar.setId("ObjectPlugin.Toolbar");

            //load basic stuff
            double iconSize = 20;
            ToggleButton newB = new ToggleButton("", ControlCenter.getImage("list-add.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(newB);
            ToggleButton save = new ToggleButton("", ControlCenter.getImage("save.gif", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);
            ToggleButton delete = new ToggleButton("", ControlCenter.getImage("list-remove.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);
            Separator sep1 = new Separator();
            save.setDisable(true);
            newB.setDisable(true);
            delete.setDisable(true);

//load new stuff
            Button select = new Button(I18n.getInstance().getString("plugin.map.select"));
            select.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    MapSelectionDialog selectionDialog = new MapSelectionDialog(ds);

                    if (selectionDialog.show() == MapSelectionDialog.Response.OK) {

                        Set<MapPlugin.DataModel> selectedData = new HashSet<>();
                        for (Map.Entry<String, MapPlugin.DataModel> entrySet : selectionDialog.getSelectedData().entrySet()) {
                            MapPlugin.DataModel value = entrySet.getValue();
                            if (value.getSelected()) {
                                selectedData.add(value);
                            }
                        }
                        logger.info("calc data");
                        DataCalculator dataCalc = new DataCalculator();
                        List<GPSRoute> calcRoues = dataCalc.calcRoues(selectedData);

                        //draw the map
                        logger.info("draw map");
                        MapCreator mapCreator = new MapCreator(mapViewer);
                        mapCreator.drawMap(calcRoues);

                        logger.info("create table");
                        ObservableList<GPSSample> data
                                = FXCollections.observableArrayList();

                        logger.info("size: " + calcRoues.get(0).getGpsSample().size());
                        data.addAll(calcRoues.get(0).getGpsSample());

                        table.setItems(data);

                        ObservableList<String> options = FXCollections.observableArrayList();
                        for (GPSRoute route : calcRoues) {
                            routeData.put(route.getName(), route);
                            options.add(route.getName());
                        }
                        comboBox.setItems(options);
                        comboBox.getSelectionModel().selectFirst();
                        comboBox.valueProperty().addListener(new ChangeListener<String>() {
                            @Override
                            public void changed(ObservableValue ov, String t, String t1) {
                                logger.info(ov);
                                logger.info(t);
                                logger.info(t1);
                                GPSRoute route = routeData.get(t1);
                                ObservableList<GPSSample> data
                                        = FXCollections.observableArrayList();

                                logger.info("size: " + calcRoues.get(0).getGpsSample().size());
                                data.addAll(route.getGpsSample());
                                table.setItems(data);
                            }
                        });
                    }
                }

            });

            Button defaultMap = new Button(I18n.getInstance().getString("plugin.map.default"));
            defaultMap.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    MapCreator mapCreator = new MapCreator(mapViewer);
                    mapCreator.drawDefaultMap();
                }

            });

            toolBar.getItems().addAll(save, newB, delete, sep1, select, defaultMap);
        }
        return toolBar;
    }

    @Override
    public void updateToolbar() {

    }

    @Override
    public JEVisDataSource getDataSource() {
        return ds;
    }

    @Override
    public void setDataSource(JEVisDataSource ds) {
        this.ds = ds;
    }

    @Override
    public boolean supportsRequest(int cmdType) {
        //TODO: implement
        return false;
    }

    @Override
    public void handleRequest(int cmdType) {
    }

    private void createSwingContent(final SwingNode swingNode) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                mapViewer = new JXMapViewer();
                swingNode.setContent(mapViewer);
            }

        });
    }

    @Override
    public Node getContentNode() {
//        BorderPane border = new BorderPane();

        //set the map view center
        final SwingNode swingNode = new SwingNode();

        createSwingContent(swingNode);

        table = new TableView();
        table.setStyle("-fx-background-color: transparent;");

        table.setEditable(false);
        TableColumn firstNameCol = new TableColumn(I18n.getInstance().getString("plugin.map.table.long"));
        TableColumn lastNameCol = new TableColumn(I18n.getInstance().getString("plugin.map.table.lat"));
        TableColumn emailCol = new TableColumn(I18n.getInstance().getString("plugin.map.table.date"));

        table.widthProperty().multiply(1.2);

        firstNameCol.setCellValueFactory(
                new PropertyValueFactory<GPSSample, String>("longitude"));
        firstNameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.25));

        lastNameCol.setCellValueFactory(
                new PropertyValueFactory<GPSSample, String>("latitude"));
        lastNameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.25));

        emailCol.setCellValueFactory(
                new PropertyValueFactory<GPSSample, String>("date"));
        emailCol.prefWidthProperty().bind(table.widthProperty().multiply(0.5));

        table.getColumns().addAll(firstNameCol, lastNameCol, emailCol);

        ObservableList<String> options
                = FXCollections.observableArrayList(
                "Example 1",
                "Example 2"
        );
        comboBox = new ComboBox<>(options);
        comboBox.setPrefWidth(Double.MAX_VALUE);
        StackPane stack = new StackPane(comboBox);

        BorderPane leftPane = new BorderPane();
        leftPane.setCenter(swingNode);

        BorderPane rightPane = new BorderPane();
        rightPane.setCenter(table);
        rightPane.setTop(stack);

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(leftPane, rightPane);
        splitPane.setDividerPositions(0.7);

//        border.setStyle(
//                "-fx-background-color: " + Constants.Color.LIGHT_GREY2);
        return splitPane;
    }

    @Override
    public Region getIcon() {
        return ControlCenter.getSVGImage(Icon.MAP, Plugin.IconSize, Plugin.IconSize, Icon.CSS_PLUGIN);
    }

    @Override
    public void fireCloseEvent() {
    }
}
