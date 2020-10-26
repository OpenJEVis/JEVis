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

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.map.MapViewPlugin;
import org.jevis.jeconfig.plugin.alarms.AlarmPlugin;
import org.jevis.jeconfig.plugin.basedata.BaseDataPlugin;
import org.jevis.jeconfig.plugin.browser.ISO50001Browser;
import org.jevis.jeconfig.plugin.charts.GraphPluginView;
import org.jevis.jeconfig.plugin.dashboard.DashBordPlugIn;
import org.jevis.jeconfig.plugin.equipment.EquipmentPlugin;
import org.jevis.jeconfig.plugin.meters.MeterPlugin;
import org.jevis.jeconfig.plugin.object.ObjectPlugin;
import org.jevis.jeconfig.plugin.reports.ReportPlugin;

import java.util.*;

/**
 * The PluginManger controls the view of the different Plugins
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class PluginManager {

    private static final Logger logger = LogManager.getLogger(PluginManager.class);
    private final List<Plugin> _plugins = new ArrayList<>();
    private final JEVisDataSource _ds;
    private final Plugin _selectedPlugin = null;
    private final Number _tabPos = 0;
    private final Number _tabPosOld = 0;
    private final AnchorPane toolbar = new AnchorPane();
    private final SimpleObjectProperty selectedPluginProperty = new SimpleObjectProperty();
    private TopMenu menu;
    private final TabPane tabPane = new TabPane();

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
        plugins.add(new AlarmPlugin(this._ds, I18n.getInstance().getString("plugin.alarms.title")));
        plugins.add(new MeterPlugin(this._ds, I18n.getInstance().getString("plugin.meters.title")));
        plugins.add(new BaseDataPlugin(this._ds, I18n.getInstance().getString("plugin.basedata.title")));
        plugins.add(new EquipmentPlugin(this._ds, I18n.getInstance().getString("plugin.equipment.title")));
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
                                                List<JEVisObject> allReports = this._ds.getObjects(reportClass, true);
                                                if (allReports.size() == 0) {
                                                    continue;
                                                }
                                            } else if (plugObj.getJEVisClassName().equals(AlarmPlugin.PLUGIN_NAME)) {
                                                JEVisClass alarmClass = this._ds.getJEVisClass(AlarmPlugin.ALARM_CONFIG_CLASS);
                                                List<JEVisObject> allAlarms = this._ds.getObjects(alarmClass, true);
                                                if (allAlarms.size() == 0) {
                                                    continue;
                                                }
                                            } else if (plugObj.getJEVisClassName().equals(MeterPlugin.PLUGIN_NAME)) {
                                                JEVisClass measurementInstrumentClass = this._ds.getJEVisClass(MeterPlugin.MEASUREMENT_INSTRUMENT_CLASS);
                                                List<JEVisObject> allMeasurementInstruments = this._ds.getObjects(measurementInstrumentClass, true);
                                                if (allMeasurementInstruments.size() == 0) {
                                                    continue;
                                                }
                                            } else if (plugObj.getJEVisClassName().equals(BaseDataPlugin.PLUGIN_NAME)) {
                                                JEVisClass baseDataClass = this._ds.getJEVisClass(BaseDataPlugin.BASE_DATA_CLASS);
                                                List<JEVisObject> allBaseData = this._ds.getObjects(baseDataClass, false);
                                                if (allBaseData.size() == 0) {
                                                    continue;
                                                }
                                            } else if (plugObj.getJEVisClassName().equals(EquipmentPlugin.PLUGIN_NAME)) {
                                                JEVisClass equipmentClass = this._ds.getJEVisClass(EquipmentPlugin.EQUIPMENT_CLASS);
                                                List<JEVisObject> allEquipment = this._ds.getObjects(equipmentClass, false);
                                                if (allEquipment.size() == 0) {
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

                /**
                 * Special case Alarming beginning
                 */
                if (plugin instanceof AlarmPlugin) {
                    AlarmPlugin alarmPlugin = (AlarmPlugin) plugin;
                    Timeline flasher = new Timeline(

                            new KeyFrame(Duration.seconds(0.5), e -> {
                                pluginTab.setStyle("-tab-text-color: black;");
                            }),

                            new KeyFrame(Duration.seconds(1.0), e -> {
                                pluginTab.setStyle("-tab-text-color: red;");
                            })
                    );
                    flasher.setCycleCount(Animation.INDEFINITE);

                    alarmPlugin.hasAlarmsProperty().addListener((observable, oldValue, newValue) -> {
                        Platform.runLater(() -> {
                            if (newValue) {
                                flasher.play();
                            } else {
                                flasher.stop();
                                pluginTab.setStyle("-tab-text-color: black;");
                            }
                        });
                    });
                    /** Start Loading Alarms in the background after an delay **/
                    Timer updateTimer = new Timer(true);
                    updateTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> plugin.setHasFocus());
                        }
                    }, 4000);

                }

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

        this.tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            this.getSelectedPlugin().lostFocus();
            this.selectedPluginProperty.setValue(this._plugins.get(newValue.intValue()));
        });

        box.getChildren().addAll(this.tabPane);
        selectedPluginProperty.setValue(_plugins.get(0));


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
