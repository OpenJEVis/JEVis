/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig.tool;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextArea;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;

import java.util.prefs.Preferences;

/**
 * The PatchNotesPage is an dialog to show recent release changes for the user.#
 */
public class PatchNotesPage {
    private static final Logger logger = LogManager.getLogger(PatchNotesPage.class);
    private final String versionHistory =
            "------Version 3.9.61------\n" +
                    "JECC - AccountingPlugin - added input fallback with zero\n" +
                    "JECC - AccountingPlugin - added plugin tooltip\n" +
                    "JECC - AccountingPlugin - fixed input labeling for ungrouped inputs\n" +
                    "JECC - AccountingPlugin - fixed input template naming\n" +
                    "JECC - AccountingPlugin - fixed xlsx output for null uni\n" +
                    "JECC - AccountingPlugin - improved reload\n" +
                    "JECC - AccountingPlugin - minor bugfixes\n" +
                    "JECC - AccountingPlugin - sort contracts\n" +
                    "JECC - BaseDataPlugin - added new function for multisite users seeing sites in tabbed view\n" +
                    "JECC - BaseDataPlugin - split value column to enter data, value and timestamp\n" +
                    "JECC - ChartPlugin - TableChartV - fixed null pointer for text data\n" +
                    "JECC - ChartPlugin - forcing new analysis to save after creation\n" +
                    "JECC - ChartPlugin - improved renderer loading\n" +
                    "JECC - Config - added boot to plugin\n" +
                    "JECC - DashboardPlugin - fixed auto aggregation for charts affecting other widget types\n" +
                    "JECC - FXLogin - added language boot option\n" +
                    "JECC - Loytec Assistant - 2 different modes Browser, Setup Assistant\n" +
                    "JECC - Loytec Assistant - sample rate will be set\n" +
                    "JECC - ObjectPlugin - ObjectTable - added clean data parent to source column\n" +
                    "JECC - ObjectPlugin - ObjectTable - added columns for min/max ts \n" +
                    "JECC - ObjectPlugin - ObjectTable - added two column for min/max ts of current request\n" +
                    "JECC - ObjectPlugin - ObjectTable - improved attribute visuals\n" +
                    "JECC - ObjectRelations - changed separator to \\\n" +
                    "JECC - Statusbar - improved task logging\n" +
                    "JECC - TRCPlugin - improved calculation input handling\n" +
                    "JECC - TRCPlugin - minor bugfixes\n" +
                    "JECC - TablePlugin - fixed filter input for new tab tabpanes\n" +
                    "JECC - TablePlugin - improved performance with cell factory optimization\n" +
                    "JECC - TablePlugin - split last value column to value and timestamp\n" +
                    "JECC - TablePlugins - added reload translation title\n" +
                    "\n" +
                    "JEDataProcessor - GapsAndLimits - fixed division result for broken rational values resulting in exception\n" +
                    "\n" +
                    "JEStatus - Wireless Logic Status error handling for tariff does not exist\n" +
                    "JEStatus - added Wireless Logic Status and Sims Offline\n" +
                    "\n" +
                    "JEWebService - added jecc distribution method\n" +
                    "JEWebService - added runtime distribution method\n" +
                    "JEWebService - get jecc version from compiled path\n\n" +
                    "------Version 3.9.60------\n" +
                    "JECC - AlarmPlugin - added pagination to tableview\n" +
                    "JECC - ChartPlugin - LoadAnalysisDialog - highlighted and focused filter\n" +
                    "JECC - ChildrenEditorExtension - added duplicate check\n" +
                    "JECC - ChildrenEditorExtension - added including/excluding filter option\n" +
                    "JECC - ChildrenEditorExtension - added source column\n" +
                    "JECC - ChildrenEditorExtension - fixed datapoint recognition\n" +
                    "JECC - DashBoardToolbar - fixed dashboard list sorting\n" +
                    "JECC - EquipmentPlugin - fixed column width for target column\n" +
                    "JECC - OPCUA Browser updated Error Message Windows\n" +
                    "JECC - ObjectPlugin - ChildrenEditorExtension - added padding for better visibility\n" +
                    "JECC - ObjectPlugin - ChildrenEditorExtension - using calculation expression translation\n" +
                    "JECC - SelectTargetDialog - improved size\n" +
                    "JECC - OPCUA Browser finish Message\n" +
                    "JECC - OPCUA Browser target Link\n" +
                    "\n" +
                    "JECommons - CalcMethods - moved and improved calculation expression translation to commons\n" +
                    "\n" +
                    "JEDataProcessor - Gaps - fixed sample cache for counter\n" +
                    "JEDataProcessor - GapsAndLimits - added null pointer check\n" +
                    "JEDataProcessor - fixed 15 minutes bug\n" +
                    "\n" +
                    "JEStatus - DataServerTable - added opc data server\n\n" +
                    "------Version 3.9.59------\n" +
                    "JECC - CalculationExtension - fixed null pointer for unset pickers\n" +
                    "JECC - ChartPlugin - Displaying of kWh as W does not work\n" +
                    "JECC - ChartPlugin - added power button for base load\n" +
                    "JECC - ChartPlugin - fixed null pointer for cases where no data processor is selected\n" +
                    "JECC - DashboardPlugin - minor change regarding Widget-Tree bug\n" +
                    "JECC - DashboardPlugin - LinkerWidget - scroll to selected link on opening\n" +
                    "\n" +
                    "JEAPIWS - fixed a bug where the login would be done twice\n" +
                    "JEAPIWS - fixed a nullpointer\n" +
                    "\n" +
                    "JECommons - fixed some problem with custom workdays\n" +
                    "\n" +
                    "JEDataProcessor - MathDataObject - fixed missing timezone\n" +
                    "JEDataProcessor - detect and dismiss values if the increasing from the previous value to the next value is unrealistic fixes #184\n" +
                    "JEDataProcessor - fixed some aggregation problems for utc timezone\n" +
                    "JEDataProcessor - improved config check\n" +
                    "\n" +
                    "JEVisCore - CleanData - added new attribute translation\n" +
                    "JEVisCore - Classes - updated data & clean data\n" +
                    "JEVisCore - updated log4j lib\n" +
                    "JEVisCore - www - removed google api font, changed copy right note\n" +
                    "\n" +
                    "JEWebService - fixed a bug where new user right would reduce performance by reloading\n" +
                    "JEWebservice - added a cached access control\n" +
                    "JEWebservice - fixed a bug where the access cached used the wrong datasource\n" +
                    "JEWebservice - fixed a bug where the user can not change his own password\n" +
                    "JEWebservice - improved relationship queries for user rights\n" +
                    "JEWebservice - optimized SQL query\n" +
                    "JEWebservice - optimized some code for performance\n" +
                    "JEWebservice - updated dependencies mainly msql. also set commons-io to the main pom to have the same version\n" +
                    "JEWebservice - added filtered query for relationships.\n" +
                    "JEWebservice - adjusted the user object to the new cache\n" +
                    "JEWebservice - adjusted the user object to the new cache\n" +
                    "JEWebservice - changed version to 1.9.5\n" +
                    "JEWebservice - fixed nullpointer\n" +
                    "JEWebservice - insertSample performance optimisation\n" +
                    "JEWebservice - replaced lists class because of dependencies\n" +
                    "JEWebservice - switched to a other Base64 decoder because of dependencies\n" +
                    "JEWebservice/API - Attribute performance improvement\n" +
                    "\n" +
                    "MYSQL - Add new Index to Object, Attribute and Relationship table\n\n" +
                    "------Version 3.9.58------\n" +
                    "JEVisCore - updated log4j version to address critical zero-day log4jshell issue\n" +
                    "\n" +
                    "JECC - AccountingPlugin - set min size for auto generated variable name labels\n" +
                    "JECC - ChartPlugin - If reactive power (kvar) is displayed, there is no sum of reactive work (kvarh)\n" +
                    "JECC - ChartPlugin - XYChart - start timestamp of analsysis timeframe on X-axis is not correct\n" +
                    "JECC - ChildrenEditorExtension - added date selection\n" +
                    "JECC - ChildrenEditorExtension - added export visible columns to xlsx function\n" +
                    "JECC - ChildrenEditorExtension - added menu to show/hide columns\n" +
                    "JECC - ChildrenEditorExtension - added sample count for value attributes\n" +
                    "JECC - ChildrenEditorExtension - added sample editor on double click\n" +
                    "JECC - ChildrenEditorExtension - added translations\n" +
                    "JECC - ChildrenEditorExtension - changed name\n" +
                    "JECC - ChildrenEditorExtension - fixed invalid attributes for parents\n" +
                    "JECC - ChildrenEditorExtension - sample editor is now loading selected time frame\n" +
                    "JECC - DashboardPlugin - ValueWidget - allow fraction digits configuration for percentage values\n" +
                    "JECC - DashboardPlugin - ValueWidget - changed rounding method to half-up\n" +
                    "JECC - DashboardPlugin - fixed null pointer for old percent configuration\n" +
                    "JECC - ObjectPlugin - added alarm wizard\n" +
                    "JECC - ObjectPlugin - added time range to delete calculation/clean data functions (only single mode yet)\n" +
                    "\n" +
                    "JEAPI-WS - HTTPConnection - improved return message \n" +
                    "JEAlarm - AlarmProcess - improved null pointer handling for async data\n" +
                    "JEAlarm - AlarmProcess - now supports only l2 step configurations\n" +
                    "\n" +
                    "JECommons - AbstractCliApp - fixed null pointer on cancel job for not-dataserver object\n" +
                    "JECommons - AggregatorFunction - fixed index out of bounds exception\n" +
                    "JECommons - Calculations - improved exception handling\n" +
                    "JECommons - GapsAndLimits - fixed super rare null pointer for replacement value generation\n" +
                    "\n" +
                    "JEDataProcessor - AggregationAlignmentStep - fixed upscaling on first value\n" +
                    "JEDataProcessor - set default timezone to utc to circumvent timezone shifting problems\n" +
                    "\n" +
                    "JEStatus - ServiceStatus - added column for average service cycle runtime\n\n" +
                    "------Version 3.9.57------\n" +
                    "JECC - ChartPlugin - Automatic \"Math.\" detection\n" +
                    "JECC - ChartPlugin - advanced setting for changing min/max fraction digits for each chart\n" +
                    "JECC - Dashboard - Added additional error handling\n" +
                    "JECC - Increased the time between connection alive checks\n" +
                    "JECC - Moved changelog to menu  \n" +
                    "JECC - ObjectPlugin - Limit setup now uses current locale for formatting\n" +
                    "JECC - ObjectPlugin - SampleTable - fixed incorrect sizing\n" +
                    "JECC - implemented workaround for white login screen\n" +
                    "\n" +
                    "DataCollector - CSV-Driver added debug messages\n" +
                    "DataCollector - EMail-Driver added debug massages\n" +
                    "JEAPI-WS - isConnectionAlive will now reuse the same connection\n" +
                    "\n" +
                    "JECommons - CleanDataObject - fixed constructor for non-service calls\n" +
                    "JECommons - fixed aggregation problem \n" +
                    "\n" +
                    "JEDataProcessor - DifferentialStep - fixed problem\n" +
                    "JEDataProcessor - MathDataObject - fixed period offset calculation\n" +
                    "JEDataProcessor - MathDataObject - fixed some null pointer\n" +
                    "JEDataProcessor - MathObjects - fixed some period problems\n" +
                    "JEDataProcessor - fixed aggregation period for yearly\n" +
                    "JEDataProcessor - fixed some problems with monthly & yearly aggregation\n" +
                    "JEDataProcessor - hours to 15 minutes fix\n" +
                    "\n" +
                    "JEReport - ReportLinkProperty - fixed calculation setting for data links\n\n" +
                    "------Version 3.9.56------\n" +
                    "JECC - Add Icon for ImageWidget\n" +
                    "JECC - Add an exeption log to the recalculation result\n" +
                    "JECC - ChartPlugin - Fixed tooltip help\n" +
                    "JECC - ChartPlugin - New tooltips text\n" +
                    "JECC - Catch TreeContextMenu exeptions \n" +
                    "JECC - Changed order so that Role Extension is the default for Roles\n" +
                    "JECC - ChartDataRow - fixed user data values not showing up in charts\n" +
                    "JECC - ChartPlugin - Add user and tag to user note\n" +
                    "JECC - ChartPlugin - added missing icons\n" +
                    "JECC - ChartPlugin - added three magic buttons for full load hours, etc.\n" +
                    "JECC - ChartPlugin - analyses are unknowingly deleted\n" +
                    "JECC - Configuration - Add context menu content in toolbar\n" +
                    "JECC - DashboardPlugin - Fixed layout bug after PDF export\n" +
                    "JECC - DashboardPlugin - Set PDF export to landscape\n" +
                    "JECC - Fixed an bug where User Note could not be created\n" +
                    "JECC - Improved the Picture preview size\n" +
                    "JECC - Increased the default Role Table size\n" +
                    "JECC - Moved the Folder statistics to its own extension\n" +
                    "JECC - NotePlugin - Add filter function\n" +
                    "JECC - NotePlugin - Add user and tags\n" +
                    "JECC - NotesPlugin - added plugin to PluginManager\n" +
                    "JECC - Plugin tab icon size is wrong\n" +
                    "JECC - Removed debug messages\n" +
                    "JECC - SampleTableExtension - improved sample creation for data filling\n" +
                    "JECC - TopMenu - added thread configuration for expert mode\n" +
                    "\n" +
                    "JEDataProcessor - CleanDataObject - fixed last date problem\n" +
                    "JEDataProcessor - PrepareForecast - fixed interval creation sample result date\n" +
                    "JEDataProcessor - added additional math data option\n" +
                    "JEDataProcessor - fixed some async problems\n" +
                    "JEDataProcessor - fixed some math data period delays > months\n" +
                    "JEDataProcessor - fixed some period calculation errors for maximum day values\n" +
                    "\n" +
                    "JEReport/JENotifier - Attachment now also works on windows systems\n" +
                    "JEReport/JENotifier - Delete report file when the program exits\n\n";

    JFXCheckBox remember = new JFXCheckBox(I18n.getInstance().getString("welcome.dontshow"));
    private final Preferences pref = Preferences.userRoot().node("JEVis.JEConfig.patchNotes");
    private final boolean isLoading = true;

    public PatchNotesPage() {

    }

    public void show(Stage owner) {

        final Stage stage = new Stage();
        remember.setSelected(true);
        remember.setVisible(false);//we dont use it anymore for now

        //TODO show it again if we have a new version of the Config
        if (!pref.getBoolean("show", true)) {
            if (pref.get("version", "").equals(JEConfig.class.getPackage().getImplementationVersion())) {
                return;
            }
        }

        stage.setTitle("Release Notes");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setWidth(950);
        stage.setHeight(550);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);

//        BorderPane root = new BorderPane();
        VBox root = new VBox(0);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root);
        TopMenu.applyActiveTheme(scene);
        stage.setScene(scene);

        JFXTextArea page = new JFXTextArea(versionHistory);

        root.getChildren().add(new Separator(Orientation.HORIZONTAL));
        root.getChildren().add(page);

        root.getChildren().add(new Separator(Orientation.HORIZONTAL));

        VBox.setVgrow(page, Priority.ALWAYS);
        HBox bot = new HBox(10);
        bot.setAlignment(Pos.BOTTOM_RIGHT);
        bot.setSpacing(5);
        bot.setPadding(new Insets(10));
        JFXButton close = new JFXButton(I18n.getInstance().getString("welcome.close"));
        close.setCancelButton(true);
        close.setDefaultButton(true);

        Region spacer = new Region();


        bot.getChildren().addAll(remember, spacer, close);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setHgrow(remember, Priority.NEVER);
        HBox.setHgrow(close, Priority.NEVER);

        root.getChildren().add(bot);

        close.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                stage.hide();
                //we dont use it anymore for now
                //storePreference();
            }
        });

        stage.showAndWait();
    }

    private void storePreference() {
        final boolean rememberIT = remember.isSelected();
        try {
            pref.put("version", JEConfig.class.getPackage().getImplementationVersion());
            pref.putBoolean("show", !rememberIT);
        } catch (Exception ex) {
            logger.warn("Could not load Preference: {}", ex.getMessage());
        }
    }

}
