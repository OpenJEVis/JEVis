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
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.map.MapViewPlugin;
import org.jevis.jeconfig.plugin.accounting.AccountingPlugin;
import org.jevis.jeconfig.plugin.alarms.AlarmPlugin;
import org.jevis.jeconfig.plugin.basedata.BaseDataPlugin;
import org.jevis.jeconfig.plugin.browser.ISO50001Browser;
import org.jevis.jeconfig.plugin.charts.ChartPlugin;
import org.jevis.jeconfig.plugin.dashboard.DashBordPlugIn;
import org.jevis.jeconfig.plugin.dtrc.TRCPlugin;
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
        List<Plugin> enabledPlugins = new ArrayList<>();
        //debug

        /**
         * Workaround, Config is always enabled.
         */
//        this._plugins.add(new ObjectPlugin(this._ds, I18n.getInstance().getString("plugin.object.title")));

        try {
            JEVisClass servicesClass = this._ds.getJEVisClass("Service Directory");
            JEVisClass jevisccClass = this._ds.getJEVisClass("Control Center");
            JEVisClass pluginClass = this._ds.getJEVisClass("Control Center Plugin");

            List<JEVisObject> servicesDir = this._ds.getObjects(servicesClass, false);
            if (servicesDir == null || servicesDir.isEmpty()) {
                logger.info("Warning missing ServicesDirectory");
                this._plugins.add(new ObjectPlugin(_ds, I18n.getInstance().getString("plugin.object.title")));
                return;
            }

            List<JEVisObject> controlCenterObj = servicesDir.get(0).getChildren(jevisccClass, true);
            if (controlCenterObj == null || controlCenterObj.isEmpty()) {
                logger.info("Warning missing ControlCenter");
                this._plugins.add(new ObjectPlugin(_ds, I18n.getInstance().getString("plugin.object.title")));
                return;
            }

            List<JEVisObject> pluginObjs = controlCenterObj.get(0).getChildren(pluginClass, true);
            if (pluginObjs == null || pluginObjs.isEmpty()) {
                logger.info("Warning No Plugins installed");
                this._plugins.add(new ObjectPlugin(_ds, I18n.getInstance().getString("plugin.object.title")));
                return;
            }

            if (user.isSysAdmin()) {
                this._plugins.addAll(Arrays.asList(
                        new ObjectPlugin(_ds, I18n.getInstance().getString("plugin.object.title")),
                        new ChartPlugin(this._ds, I18n.getInstance().getString("plugin.graph.title")),
                        new DashBordPlugIn(this._ds, I18n.getInstance().getString("plugin.dashboard.title")),
                        new ReportPlugin(this._ds, I18n.getInstance().getString("plugin.reports.title")),
                        new AlarmPlugin(this._ds, I18n.getInstance().getString("plugin.alarms.title")),
                        new MeterPlugin(this._ds, I18n.getInstance().getString("plugin.meters.title")),
                        new BaseDataPlugin(this._ds, I18n.getInstance().getString("plugin.basedata.title")),
                        new EquipmentPlugin(this._ds, I18n.getInstance().getString("plugin.equipment.title")),
                        new AccountingPlugin(this._ds, I18n.getInstance().getString("plugin.accounting.title")),
                        new TRCPlugin(this._ds)
                ));
            } else {
                for (JEVisObject plugObj : pluginObjs) {
                    try {
                        try {

                            JEVisAttribute enabled = plugObj.getAttribute("Enable");
                            if (enabled == null) {
                                continue;
                            }
                            JEVisSample value = enabled.getLatestSample();
                            if (value != null) {
                                if (value.getValueAsBoolean() || (plugObj.getJEVisClassName().equals(ObjectPlugin.PLUGIN_NAME) && user.isSysAdmin())) {
                                    if (plugObj.getJEVisClassName().equals(ObjectPlugin.PLUGIN_NAME)) {
                                        _plugins.add(new ObjectPlugin(_ds, I18n.getInstance().getString("plugin.object.title")));
                                    } else if (plugObj.getJEVisClassName().equals(ChartPlugin.PLUGIN_NAME)) {
                                        _plugins.add(new ChartPlugin(this._ds, I18n.getInstance().getString("plugin.graph.title")));
                                    } else if (plugObj.getJEVisClassName().equals(DashBordPlugIn.PLUGIN_NAME)) {
                                        _plugins.add(new DashBordPlugIn(this._ds, I18n.getInstance().getString("plugin.dashboard.title")));
                                    } else if (plugObj.getJEVisClassName().equals(ReportPlugin.PLUGIN_NAME)) {
                                        _plugins.add(new ReportPlugin(this._ds, I18n.getInstance().getString("plugin.reports.title")));
                                    } else if (plugObj.getJEVisClassName().equals(AlarmPlugin.PLUGIN_NAME)) {
                                        _plugins.add(new AlarmPlugin(this._ds, I18n.getInstance().getString("plugin.alarms.title")));
                                    } else if (plugObj.getJEVisClassName().equals(MeterPlugin.PLUGIN_NAME)) {
                                        _plugins.add(new MeterPlugin(this._ds, I18n.getInstance().getString("plugin.meters.title")));
                                    } else if (plugObj.getJEVisClassName().equals(BaseDataPlugin.PLUGIN_NAME)) {
                                        _plugins.add(new BaseDataPlugin(this._ds, I18n.getInstance().getString("plugin.basedata.title")));
                                    } else if (plugObj.getJEVisClassName().equals(EquipmentPlugin.PLUGIN_NAME)) {
                                        _plugins.add(new EquipmentPlugin(this._ds, I18n.getInstance().getString("plugin.equipment.title")));
                                    } else if (plugObj.getJEVisClassName().equals(AccountingPlugin.PLUGIN_NAME)) {
                                        _plugins.add(new AccountingPlugin(this._ds, I18n.getInstance().getString("plugin.accounting.title")));
                                    } else if (plugObj.getJEVisClassName().equals(TRCPlugin.PLUGIN_NAME)) {
                                        _plugins.add(new TRCPlugin(this._ds));
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                this._plugins.addAll(enabledPlugins);

            }

            if (this._plugins.isEmpty()) {
                this._plugins.add(new ObjectPlugin(_ds, I18n.getInstance().getString("plugin.object.title")));
            }

            try {
                Comparator<Plugin> pluginComparator = Comparator.comparingInt(Plugin::getPrefTapPos);
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

//        this.toolbar.setStyle("-fx-background-color: #CCFF99;");
        this.toolbar.getStyleClass().add("tool-bar");
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
                            Platform.runLater(plugin::setHasFocus);
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

        this.selectedPluginProperty.addListener((ChangeListener<Plugin>) (observable, oldValue, newValue) -> Platform.runLater(() -> {
//                        toolbar.getChildren().removeAll();
            if (newValue != null) {
                try {
                    Node pluginToolbar = newValue.getToolbar();
                    PluginManager.this.toolbar.getChildren().setAll(pluginToolbar);
                    AnchorPane.setTopAnchor(pluginToolbar, 0.0);
                    AnchorPane.setLeftAnchor(pluginToolbar, 0.0);
                    AnchorPane.setRightAnchor(pluginToolbar, 0.0);
                    AnchorPane.setBottomAnchor(pluginToolbar, 0.0);
                    PluginManager.this.menu.setPlugin(newValue);
                    newValue.setHasFocus();
                    /**
                     * for now we have to disable the function to keep the status over multiple plugins
                     *  because the TaskMonitor will make trouble with the tooltips.
                     */
                    JEVisHelp.getInstance().showHelpTooltips(false);
                    JEVisHelp.getInstance().showInfoTooltips(false);
                    JEVisHelp.getInstance().setActivePlugin(newValue.getClass().getSimpleName());
                } catch (Exception ex) {
                    logger.error("Error while switching plugin: {}", ex, ex);
                }
            }

        }));

        this.tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            try {
                this.getSelectedPlugin().lostFocus();
                this.selectedPluginProperty.setValue(this._plugins.get(newValue.intValue()));
            } catch (Exception ex) {
                logger.error("Error in selection model switch: {}", ex, ex);
            }
        });

        box.getChildren().addAll(this.tabPane);
        selectedPluginProperty.setValue(_plugins.get(0));
        JEVisHelp.getInstance().setActivePlugin(_plugins.getClass().getSimpleName());

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
