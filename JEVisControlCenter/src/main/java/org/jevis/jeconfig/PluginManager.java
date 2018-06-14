/**
 * Copyright (C) 2009 - 2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.map.MapViewPlugin;
import org.jevis.jeconfig.plugin.browser.ISO5001Browser;
import org.jevis.jeconfig.plugin.dashboard.DashboardPlugin;
import org.jevis.jeconfig.plugin.graph.view.GraphPluginView;
import org.jevis.jeconfig.plugin.object.ObjectPlugin;
import org.jevis.jeconfig.tool.I18n;

import java.util.ArrayList;
import java.util.List;

/**
 * The PluginManger controls the view of the different Plugins
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class PluginManager {

    private List<Plugin> _plugins = new ArrayList<>();
    private JEVisDataSource _ds;
    private boolean _watermark = true;
    private Plugin _selectedPlugin = null;
    private Number _tabPos = 0;
    private Number _tabPosOld = 0;
    private TabPane tabPane;
    private AnchorPane toolbar = new AnchorPane();
    private ObjectProperty<Plugin> selectedPluginProperty = new SimpleObjectProperty();
    private TopMenu menu;

    public PluginManager(JEVisDataSource _ds) {
        this._ds = _ds;
    }

    public void addPlugin(Plugin plugin) {
        _plugins.add(plugin);
    }

    public void removePlugin(Plugin plugin) {
        _plugins.remove(plugin);
    }

    public List<Plugin> getPlugins() {
        return _plugins;
    }

    /**
     * Add all plugins based on the JEVis usersettings
     *
     * @param user
     */
    public void addPluginsByUserSetting(JEVisObject user) {
        //TODO: load the user an add only the allowed plugins
//        _plugins.add(new ObjectPlugin(_ds, "Resources"));
        _plugins.add(new ObjectPlugin(_ds, I18n.getInstance().getString("plugin.object.title")));
        _plugins.add(new GraphPluginView(_ds, I18n.getInstance().getString("plugin.graph.title")));
        _plugins.add(new DashboardPlugin(_ds));
        _plugins.add(new MapViewPlugin(_ds, I18n.getInstance().getString("plugin.gis.title")));
        _plugins.add(new ISO5001Browser(_ds));
        _plugins.add(new org.jevis.jeconfig.plugin.classes.ClassPlugin(_ds, I18n.getInstance().getString("plugin.classes.title")));
        _plugins.add(new org.jevis.jeconfig.plugin.unit.UnitPlugin(_ds, I18n.getInstance().getString("plugin.units.title")));

//        _plugins.add(new LoytecBrowser(_ds));

    }

    public void setWatermark(boolean water) {
        _watermark = water;
    }

    public Node getView() {
        StackPane box = new StackPane();

        tabPane = new TabPane();
        tabPane.setSide(Side.LEFT);

        toolbar.setStyle("-fx-background-color: #CCFF99;");
//        AnchorPane.setTopAnchor(toolbar, 0.0);
//        AnchorPane.setLeftAnchor(toolbar, 0.0);
//        AnchorPane.setRightAnchor(toolbar, 0.0);
//        AnchorPane.setBottomAnchor(toolbar, 0.0);

        for (Plugin plugin : _plugins) {
            try {
                Tab pluginTab = new Tab(plugin.getName());
                pluginTab.setClosable(false);
                pluginTab.setTooltip(new Tooltip(plugin.getUUID()));
//            pluginTab.setContent(plugin.getView().getNode());
                pluginTab.setContent(plugin.getConntentNode());
                tabPane.getTabs().add(pluginTab);

                pluginTab.setOnClosed(new EventHandler<Event>() {
                    @Override
                    public void handle(Event t) {
                        Plugin plugin = _plugins.get(_tabPosOld.intValue());
                        _plugins.remove(plugin);
                    }
                });

                pluginTab.setGraphic(plugin.getIcon());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        selectedPluginProperty.addListener(new ChangeListener<Plugin>() {

            @Override
            public void changed(ObservableValue<? extends Plugin> observable, Plugin oldValue, Plugin newValue) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
//                        toolbar.getChildren().removeAll();
                        if (newValue != null) {
                            Node pluginToolbar = newValue.getToolbar();

                            toolbar.getChildren().setAll(pluginToolbar);
                            AnchorPane.setTopAnchor(pluginToolbar, 0.0);
                            AnchorPane.setLeftAnchor(pluginToolbar, 0.0);
                            AnchorPane.setRightAnchor(pluginToolbar, 0.0);
                            AnchorPane.setBottomAnchor(pluginToolbar, 0.0);
                            menu.setPlugin(newValue);
                            newValue.setHasFocus();
                        }

                    }
                });
            }
        });
        selectedPluginProperty.setValue(_plugins.get(0));

        tabPane.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
                selectedPluginProperty.setValue(_plugins.get(newValue.intValue()));

            }
        });

        //Watermark is disabled
        //Todo: configure via start parameter
//        if (_watermark) {
//            VBox waterBox = new VBox(); //TODO better load the
//            waterBox.setId("watermark");
//            waterBox.setStyle(null);
//            waterBox.setDisable(true);
//            box.getChildren().addAll(tabPane,
//                    waterBox);
//        } else {
//            box.getChildren().addAll(tabPane);
//        }
        box.getChildren().addAll(tabPane);

        return box;
    }

    Plugin getSelectedPlugin() {
        return selectedPluginProperty.getValue();
//        return _plugins.get(_tabPos.intValue());
    }

    public Node getToolbar() {
        return toolbar;
//        return getSelectedPlugin().getToolbar();
    }

    public void setMenuBar(TopMenu menu) {
        this.menu = menu;
    }
}
