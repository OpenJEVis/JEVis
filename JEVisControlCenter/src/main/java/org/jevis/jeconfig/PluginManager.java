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
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.map.MapViewPlugin;
import org.jevis.jeconfig.plugin.accounting.AccountingPlugin;
import org.jevis.jeconfig.plugin.action.ActionPlugin;
import org.jevis.jeconfig.plugin.alarms.AlarmPlugin;
import org.jevis.jeconfig.plugin.basedata.BaseDataPlugin;
import org.jevis.jeconfig.plugin.browser.ISO50001Plugin;
import org.jevis.jeconfig.plugin.charts.ChartPlugin;
import org.jevis.jeconfig.plugin.dashboard.DashBordPlugIn;
import org.jevis.jeconfig.plugin.dtrc.TRCPlugin;
import org.jevis.jeconfig.plugin.equipment.EquipmentPlugin;
import org.jevis.jeconfig.plugin.legal.LegalCatasdrePlugin;
import org.jevis.jeconfig.plugin.meters.MeterPlugin;
import org.jevis.jeconfig.plugin.nonconformities.NonconformitiesPlugin;
import org.jevis.jeconfig.plugin.notes.NotesPlugin;
import org.jevis.jeconfig.plugin.object.ObjectPlugin;
import org.jevis.jeconfig.plugin.reports.ReportPlugin;
import org.jevis.jeconfig.tool.DraggableTab;

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
    private final SimpleObjectProperty<Plugin> selectedPluginProperty = new SimpleObjectProperty<>();
    private TopMenu menu;
    private final TabPane tabPane = new TabPane();

    public PluginManager(JEVisDataSource _ds) {
        this._ds = _ds;
        //TabPaneDetacher.create().makeTabsDetachable(tabPane);
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
        plugins.add(new ISO50001Plugin(this._ds, I18n.getInstance().getString("plugin.iso50001.title")));
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

        try {
            JEVisClass pluginClass = this._ds.getJEVisClass("Control Center Plugin");

            List<JEVisObject> pluginObjs = _ds.getObjects(pluginClass, true);
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
                        new NotesPlugin(this._ds, I18n.getInstance().getString("plugin.notes.title")),
                        new MeterPlugin(this._ds, I18n.getInstance().getString("plugin.meters.title")),
                        new BaseDataPlugin(this._ds, I18n.getInstance().getString("plugin.basedata.title")),
                        new EquipmentPlugin(this._ds, I18n.getInstance().getString("plugin.equipment.title")),
                        new ISO50001Plugin(this._ds, I18n.getInstance().getString("plugin.iso50001.title")),
                        new AccountingPlugin(this._ds, I18n.getInstance().getString("plugin.accounting.title")),
                        new ActionPlugin(this._ds, I18n.getInstance().getString("plugin.action.name")),
                        new NonconformitiesPlugin(this._ds, I18n.getInstance().getString("plugin.nonconformities.name")),
                        new LegalCatasdrePlugin(this._ds, I18n.getInstance().getString("plugin.Legalcadastre.name")),
                        new TRCPlugin(this._ds)
                ));
                return;
            } else {
                for (JEVisObject plugObj : pluginObjs) {
                    try {
                            JEVisAttribute enabled = plugObj.getAttribute("Enable");
                            if (enabled == null) {
                                continue;
                            }
                            JEVisSample value = enabled.getLatestSample();
                            if (value != null) {
                                if (value.getValueAsBoolean() || (plugObj.getJEVisClassName().equals(ObjectPlugin.PLUGIN_NAME) && user.isSysAdmin())) {
                                    if (plugObj.getJEVisClassName().equals(ObjectPlugin.PLUGIN_NAME)) {
                                        enabledPlugins.add(new ObjectPlugin(_ds, I18n.getInstance().getString("plugin.object.title")));
                                    } else if (plugObj.getJEVisClassName().equals(ChartPlugin.PLUGIN_NAME)) {
                                        enabledPlugins.add(new ChartPlugin(this._ds, I18n.getInstance().getString("plugin.graph.title")));
                                    } else if (plugObj.getJEVisClassName().equals(DashBordPlugIn.PLUGIN_NAME)) {
                                        enabledPlugins.add(new DashBordPlugIn(this._ds, I18n.getInstance().getString("plugin.dashboard.title")));
                                    } else if (plugObj.getJEVisClassName().equals(ReportPlugin.PLUGIN_NAME)) {
                                        enabledPlugins.add(new ReportPlugin(this._ds, I18n.getInstance().getString("plugin.reports.title")));
                                    } else if (plugObj.getJEVisClassName().equals(AlarmPlugin.PLUGIN_NAME)) {
                                        enabledPlugins.add(new AlarmPlugin(this._ds, I18n.getInstance().getString("plugin.alarms.title")));
                                    } else if (plugObj.getJEVisClassName().equals(NotesPlugin.PLUGIN_NAME)) {
                                        enabledPlugins.add(new NotesPlugin(this._ds, I18n.getInstance().getString("plugin.notes.title")));
                                    } else if (plugObj.getJEVisClassName().equals(MeterPlugin.PLUGIN_NAME)) {
                                        enabledPlugins.add(new MeterPlugin(this._ds, I18n.getInstance().getString("plugin.meters.title")));
                                    } else if (plugObj.getJEVisClassName().equals(BaseDataPlugin.PLUGIN_NAME)) {
                                        enabledPlugins.add(new BaseDataPlugin(this._ds, I18n.getInstance().getString("plugin.basedata.title")));
                                    } else if (plugObj.getJEVisClassName().equals(EquipmentPlugin.PLUGIN_NAME)) {
                                        enabledPlugins.add(new EquipmentPlugin(this._ds, I18n.getInstance().getString("plugin.equipment.title")));
                                    } else if (plugObj.getJEVisClassName().equals(ISO50001Plugin.PLUGIN_NAME)) {
                                        enabledPlugins.add(new ISO50001Plugin(this._ds, I18n.getInstance().getString("plugin.iso50001.title")));
                                    } else if (plugObj.getJEVisClassName().equals(AccountingPlugin.PLUGIN_NAME)) {
                                        enabledPlugins.add(new AccountingPlugin(this._ds, I18n.getInstance().getString("plugin.accounting.title")));
                                    } else if (plugObj.getJEVisClassName().equals(ActionPlugin.PLUGIN_NAME)) {
                                        enabledPlugins.add(new ActionPlugin(this._ds, I18n.getInstance().getString("plugin.action.name")));
                                    } else if (plugObj.getJEVisClassName().equals(NonconformitiesPlugin.PLUGIN_NAME)) {
                                        enabledPlugins.add(new NonconformitiesPlugin(this._ds, I18n.getInstance().getString("plugin.nonconformities.name")));
                                    } else if (plugObj.getJEVisClassName().equals(LegalCatasdrePlugin.PLUGIN_NAME)) {
                                        enabledPlugins.add(new LegalCatasdrePlugin(this._ds, I18n.getInstance().getString("plugin.indexoflegalprovisions.name")));
                                    } else if (plugObj.getJEVisClassName().equals(TRCPlugin.PLUGIN_NAME)) {
                                        enabledPlugins.add(new TRCPlugin(this._ds));
                                    }
                                }
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
            } catch (Exception ignored) {
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setWatermark(boolean water) {
        boolean _watermark = water;
    }

    public Node getView() {

        this.tabPane.setSide(Side.LEFT);

//        this.toolbar.setStyle("-fx-background-color: #CCFF99;");
        this.toolbar.getStyleClass().add("tool-bar");
        /* magic number based on the biggest toolbar, so it is not changing size wile switching plugin*/
        this.toolbar.setMinHeight(55);
        this.toolbar.setMaxHeight(55);
//        AnchorPane.setTopAnchor(toolbar, 0.0);
//        AnchorPane.setLeftAnchor(toolbar, 0.0);
//        AnchorPane.setRightAnchor(toolbar, 0.0);
//        AnchorPane.setBottomAnchor(toolbar, 0.0);

        for (Plugin plugin : this._plugins) {
            System.out.println(plugin);
            try {
                DraggableTab pluginTab = new DraggableTab(plugin.getName(), plugin.getIcon(), plugin);
                //Tab pluginTab = new Tab(plugin.getName());
                //pluginTab.setGraphic(plugin.getIcon());
                pluginTab.setClosable(false);
                pluginTab.setTooltip(new Tooltip(plugin.getToolTip()));

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
                    /** Start Loading Alarms in the background after a delay **/
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


            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        this.selectedPluginProperty.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
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
                     * for now, we have to disable the function to keep the status over multiple plugins
                     *  because the TaskMonitor will make trouble with the tooltips.
                     */
                    JEVisHelp.getInstance().showHelpTooltips(false);
                    JEVisHelp.getInstance().showInfoTooltips(false);
                    JEVisHelp.getInstance().setActivePlugin(newValue.getClass().getSimpleName());
                } catch (Exception ex) {
                    logger.error("Error while switching plugin: {}", ex, ex);
                }

                menu.setPlugin(newValue);
            }

        }));

        this.tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            this.getSelectedPlugin().lostFocus();
            if (newValue instanceof DraggableTab) {
                this.selectedPluginProperty.setValue(((DraggableTab) newValue).getPlugin());
            }
        });

        selectedPluginProperty.setValue(_plugins.get(0));
        JEVisHelp.getInstance().setActivePlugin(_plugins.getClass().getSimpleName());

        return tabPane;
    }

    Plugin getSelectedPlugin() {
        return this.selectedPluginProperty.getValue();
    }

    public Node getToolbar() {
        return this.toolbar;
//        return getSelectedPlugin().getToolbar();
    }

    public void setMenuBar(TopMenu menu) {
        this.menu = menu;
    }
}
