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
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.jeconfig.map.MapViewPlugin;
import org.jevis.jeconfig.plugin.browser.ISO50001Browser;
import org.jevis.jeconfig.plugin.graph.view.GraphPluginView;
import org.jevis.jeconfig.plugin.object.ObjectPlugin;
import org.jevis.jeconfig.plugin.scada.SCADAPlugin;
import org.jevis.jeconfig.tool.I18n;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The PluginManger controls the view of the different Plugins
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class PluginManager {

    private static final Logger logger = LogManager.getLogger(PluginManager.class);
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
     * Fetch all Installed Plugins from Source and from JEVis Server
     * <p>
     * NOTE: Dynamic JEVis Server plugins are not supported for now
     *
     * @return
     */
    public List<Plugin> getInstalledPlugins() {
        List<Plugin> plugins = new ArrayList<>();
//        plugins.add(new ObjectPlugin(_ds, I18n.getInstance().getString("plugin.object.title")));
        plugins.add(new GraphPluginView(_ds, I18n.getInstance().getString("plugin.graph.title")));
        plugins.add(new SCADAPlugin(_ds));
        plugins.add(new ISO50001Browser(_ds));
        plugins.add(new org.jevis.jeconfig.plugin.classes.ClassPlugin(_ds, I18n.getInstance().getString("plugin.classes.title")));
        plugins.add(new org.jevis.jeconfig.plugin.unit.UnitPlugin(_ds, I18n.getInstance().getString("plugin.units.title")));
        plugins.add(new MapViewPlugin(_ds, I18n.getInstance().getString("plugin.map.title")));
//        plugins.add(new LoytecBrowser(_ds));

        return plugins;
    }

    /**
     * Add all plugins based on the JEVis usersettings
     *
     * @param user
     */
    public void addPluginsByUserSetting(JEVisUser user) {
        List<Plugin> plugins = getInstalledPlugins();
        List<Plugin> enabledPlugins = new ArrayList<>();
        //debug

        /**
         * Workaround, Config is always enabled.
         */
        _plugins.add(new ObjectPlugin(_ds, I18n.getInstance().getString("plugin.object.title")));

        try {
            JEVisClass servicesClass = _ds.getJEVisClass("Service Directory");
            JEVisClass jevisccClass = _ds.getJEVisClass("Control Center");
            JEVisClass pluginClass = _ds.getJEVisClass("Control Center Plugin");

            List<JEVisObject> servicesDir = _ds.getObjects(servicesClass, false);
            if (servicesDir == null || servicesDir.isEmpty()) {
                logger.info("Warning missing ServicesDirectory");
                return;
            }

            List<JEVisObject> controlCenterObj = servicesDir.get(0).getChildren(jevisccClass, true);
            if (controlCenterObj == null || controlCenterObj.isEmpty()) {
                logger.info("Warning missing ControlCenter");
                return;
            }

            List<JEVisObject> pluginObjs = controlCenterObj.get(0).getChildren(pluginClass, true);
            if (pluginObjs == null || pluginObjs.isEmpty()) {
                logger.info("Warning No Plugins installed");
                return;
            }

            if (user.isSysAdmin()) {
                _plugins.addAll(getInstalledPlugins());
            } else {
                for (JEVisObject plugObj : pluginObjs) {
                    try {
                        for (Plugin plugin : plugins) {
                            try {
                                if (plugin.getClassName().equals(plugObj.getJEVisClassName())) {
                                    JEVisAttribute enabled = plugObj.getAttribute("Enable");
                                    if (enabled == null) {
                                        continue;
                                    }
                                    JEVisSample value = enabled.getLatestSample();
                                    if (value != null) {
                                        if (value.getValueAsBoolean()) {
                                            enabledPlugins.add(plugin);
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                _plugins.addAll(enabledPlugins);

            }

            try {
                Collections.swap(_plugins, 0, 1);
            } catch (Exception e) {
                //workaround to get graph plugin to first position
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
                pluginTab.setTooltip(new Tooltip(plugin.getToolTip()));
//            pluginTab.setContent(plugin.getView().getNode());
                pluginTab.setContent(plugin.getContentNode());
                tabPane.getTabs().add(pluginTab);

                pluginTab.setOnClosed(t -> {
                    Plugin plugin1 = _plugins.get(_tabPosOld.intValue());
                    _plugins.remove(plugin1);
                });

                pluginTab.setGraphic(plugin.getIcon());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        selectedPluginProperty.addListener(new ChangeListener<Plugin>() {

            @Override
            public void changed(ObservableValue<? extends Plugin> observable, Plugin oldValue, Plugin newValue) {
                Platform.runLater(() -> {
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

                });
            }
        });
        selectedPluginProperty.setValue(_plugins.get(0));

        tabPane.getSelectionModel().selectedIndexProperty().addListener((ov, oldValue, newValue) -> selectedPluginProperty.setValue(_plugins.get(newValue.intValue())));

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
