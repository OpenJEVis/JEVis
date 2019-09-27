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
import org.jevis.jeconfig.plugin.dashboard.DashBordPlugIn;
import org.jevis.jeconfig.plugin.graph.view.GraphPluginView;
import org.jevis.jeconfig.plugin.object.ObjectPlugin;
import org.jevis.jeconfig.plugin.reports.ReportPlugin;
import org.jevis.jeconfig.tool.I18n;

import java.util.ArrayList;
import java.util.Comparator;
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
    private Plugin _selectedPlugin = null;
    private Number _tabPos = 0;
    private Number _tabPosOld = 0;
    private AnchorPane toolbar = new AnchorPane();
    private SimpleObjectProperty selectedPluginProperty = new SimpleObjectProperty();
    private TopMenu menu;
    private TabPane tabPane = new TabPane();

    public PluginManager(JEVisDataSource _ds) {
        this._ds = _ds;
    }

    public void addPlugin(Plugin plugin) {
        this._plugins.add(plugin);
    }

    public void removePlugin(Plugin plugin) {
        this._plugins.remove(plugin);
    }

    public List<Plugin> getPlugins() {
        return this._plugins;
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
        plugins.add(new GraphPluginView(this._ds, I18n.getInstance().getString("plugin.graph.title")));
        plugins.add(new ReportPlugin(this._ds, I18n.getInstance().getString("plugin.reports.title")));
        plugins.add(new DashBordPlugIn(this._ds, I18n.getInstance().getString("plugin.dashboard.title")));

//        plugins.add(new SCADAPlugin(_ds));
        plugins.add(new ISO50001Browser(this._ds));
        plugins.add(new org.jevis.jeconfig.plugin.classes.ClassPlugin(this._ds, I18n.getInstance().getString("plugin.classes.title")));
        plugins.add(new org.jevis.jeconfig.plugin.unit.UnitPlugin(this._ds, I18n.getInstance().getString("plugin.units.title")));
        plugins.add(new MapViewPlugin(this._ds, I18n.getInstance().getString("plugin.map.title")));

//        plugins.add(new LoytecBrowser(_ds));

        return plugins;
    }


    public void openInPlugin(String plugInName, Object object) {
        this._plugins.forEach(plugin -> {
            if (plugin.getClassName().equals(plugInName)) {
                plugin.openObject(object);
                this.tabPane.getTabs().forEach(tab -> {
                    if (tab.getText().equals(plugin.getName())) {
                        this.tabPane.getSelectionModel().select(tab);
                    }
                });
            }
        });
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
        this._plugins.add(new ObjectPlugin(this._ds, I18n.getInstance().getString("plugin.object.title")));

        try {
            JEVisClass servicesClass = this._ds.getJEVisClass("Service Directory");
            JEVisClass jevisccClass = this._ds.getJEVisClass("Control Center");
            JEVisClass pluginClass = this._ds.getJEVisClass("Control Center Plugin");

            List<JEVisObject> servicesDir = this._ds.getObjects(servicesClass, false);
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
                this._plugins.addAll(plugins);
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
                                            /** Workaround to manage the dashboard and report access,
                                             *  hide the dashboard/report if the user has no analyses.
                                             */
                                            if (plugObj.getJEVisClassName().equals(DashBordPlugIn.PLUGIN_NAME)) {
                                                JEVisClass scadaAnalysis = this._ds.getJEVisClass(DashBordPlugIn.CLASS_ANALYSIS);
                                                List<JEVisObject> allAnalyses = this._ds.getObjects(scadaAnalysis, false);
                                                if (allAnalyses.size() == 0) {
                                                    continue;
                                                }
                                            } else if (plugObj.getJEVisClassName().equals(ReportPlugin.PLUGIN_NAME)) {
                                                JEVisClass reportClass = this._ds.getJEVisClass(ReportPlugin.REPORT_CLASS);
                                                List<JEVisObject> allReports = this._ds.getObjects(reportClass, false);
                                                if (allReports.size() == 0) {
                                                    continue;
                                                }
                                            }

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
                this._plugins.addAll(enabledPlugins);

            }


            try {
                Comparator<Plugin> pluginComparator = (o1, o2) -> {

                    if (o1.getPrefTapPos() < o2.getPrefTapPos()) {
                        return -1;
                    } else if (o1.getPrefTapPos() > o2.getPrefTapPos()) {
                        return 1;
                    } else {
                        return 0;
                    }
                };
                this._plugins.sort(pluginComparator);

//                        Collections.swap(_plugins, 0, 1);
            } catch (Exception e) {
                //workaround to get graph plugin to first position
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setWatermark(boolean water) {
        boolean _watermark = water;
    }

    public Node getView() {
        StackPane box = new StackPane();


        this.tabPane.setSide(Side.LEFT);

        this.toolbar.setStyle("-fx-background-color: #CCFF99;");
//        AnchorPane.setTopAnchor(toolbar, 0.0);
//        AnchorPane.setLeftAnchor(toolbar, 0.0);
//        AnchorPane.setRightAnchor(toolbar, 0.0);
//        AnchorPane.setBottomAnchor(toolbar, 0.0);

        for (Plugin plugin : this._plugins) {
            try {
                Tab pluginTab = new Tab(plugin.getName());
                pluginTab.setClosable(false);
                pluginTab.setTooltip(new Tooltip(plugin.getToolTip()));
//            pluginTab.setContent(plugin.getView().getNode());
                pluginTab.setContent(plugin.getContentNode());
                this.tabPane.getTabs().add(pluginTab);

                pluginTab.setOnClosed(t -> {
                    Plugin plugin1 = this._plugins.get(this._tabPosOld.intValue());
                    this._plugins.remove(plugin1);
                });

                pluginTab.setGraphic(plugin.getIcon());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        this.selectedPluginProperty.addListener(new ChangeListener<Plugin>() {

            @Override
            public void changed(ObservableValue<? extends Plugin> observable, Plugin oldValue, Plugin newValue) {
                Platform.runLater(() -> {
//                        toolbar.getChildren().removeAll();
                    if (newValue != null) {
                        Node pluginToolbar = newValue.getToolbar();

                        PluginManager.this.toolbar.getChildren().setAll(pluginToolbar);
                        AnchorPane.setTopAnchor(pluginToolbar, 0.0);
                        AnchorPane.setLeftAnchor(pluginToolbar, 0.0);
                        AnchorPane.setRightAnchor(pluginToolbar, 0.0);
                        AnchorPane.setBottomAnchor(pluginToolbar, 0.0);
                        PluginManager.this.menu.setPlugin(newValue);
                        newValue.setHasFocus();
                    }

                });
            }
        });
        this.selectedPluginProperty.setValue(this._plugins.get(0));

        this.tabPane.getSelectionModel().selectedIndexProperty().addListener((ov, oldValue, newValue) -> this.selectedPluginProperty.setValue(this._plugins.get(newValue.intValue())));

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
        box.getChildren().addAll(this.tabPane);

        return box;
    }

    Plugin getSelectedPlugin() {
        return (Plugin) this.selectedPluginProperty.getValue();
//        return _plugins.get(_tabPos.intValue());
    }

    public Node getToolbar() {
        return this.toolbar;
//        return getSelectedPlugin().getToolbar();
    }

    public void setMenuBar(TopMenu menu) {
        this.menu = menu;
    }
}
