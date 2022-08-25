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
            "------Version 3.9.65------\n" +
                    "JECC - ChartPlugin - TableChartV - added filter option\n" +
                    "JECC - DashboardPlugin - Add default values to GaugePojo\n" +
                    "JECC - DashboardPlugin - Add icon for side editor\n" +
                    "JECC - DashboardPlugin - Add localisation to the widget name\n" +
                    "JECC - DashboardPlugin - Add missing Classes\n" +
                    "JECC - DashboardPlugin - Catch missing configuration parameters\n" +
                    "JECC - DashboardPlugin - Fixed a bug where a new Widget did not display the title\n" +
                    "JECC - DashboardPlugin - Fixed an bug where disabling the limit or procent did not update\n" +
                    "JECC - DashboardPlugin - Fixed some bug in the side pane editor\n" +
                    "JECC - DashboardPlugin - Gauge / Linear gauge fix for negative values\n" +
                    "JECC - DashboardPlugin - Gauge config tab overhaul\n" +
                    "JECC - DashboardPlugin - Improvemed zoom behavior when zoom is set to fit to screen\n" +
                    "JECC - DashboardPlugin - LinearGauge - Last Value\n" +
                    "JECC - DashboardPlugin - LinearGauge - updated tick label spacing\n" +
                    "JECC - DashboardPlugin - Performance improvement and reduced memory usage\n" +
                    "JECC - DashboardPlugin - Removed widget icons\n" +
                    "JECC - DashboardPlugin - Shape config tab overhaul\n" +
                    "JECC - DashboardPlugin - The side panel editor allows now to edit the title\n" +
                    "JECC - DashboardPlugin - The side panel editor can now be toggled to stay visible or be hidden\n" +
                    "JECC - DashboardPlugin - The side panel editor show now the selected input data\n" +
                    "JECC - DashboardPlugin - bug fix some Widgets did not refresh on commit\n" +
                    "JECC - DashboardPlugin - implemented Shape Widget\n" +
                    "JECC - DashboardPlugin - update config tab GUI\n" +
                    "JECC - DashboardPlugin - Last Value\n" +
                    "JECC - FXLogin - Removed border form the statusbox and changed the text\n" +
                    "JECC - reworked icon design\n" +
                    "\n" +
                    "JEAlarm - added AlarmPeriod.NONE as functionality, using last alarm as start for period\n" +
                    "\n" +
                    "JEStatus - added object name to report status\n\n" +
                    "------Version 3.9.64------\n" +
                    "JECC - AlarmPlugin - added alarm type check and respective button for limit configuration or alarm configuration\n" +
                    "JECC - AlarmPlugin - fixed attribute editor node\n" +
                    "JECC - AlarmPlugin - fixed null pointer in task\n" +
                    "JECC - AttributeEditor - fixed initialization problems in derived classes\n" +
                    "JECC - CalculationExtension - Fixed a bug where the wrong period was displayed\n" +
                    "JECC - ChartPlugin - fixed recurring adding of custom periods in preset date box\n" +
                    "JECC - DashboardPlugin - Int the DataModel with the static Widget ID.\n" +
                    "JECC - DashboardPlugin - Removed a redundant UpdateConfig all which even when the Widget was not ready.\n" +
                    "JECC - FXLogin - fixed login progress text\n" +
                    "JECC - Fixed merged properties\n" +
                    "JECC - JEVisTreeContextMenu - added base data to go to source\n" +
                    "JECC - JEVisTreeContextMenu - added math data to recalc\n" +
                    "JECC - LocalNameDialog - fixed null pointer\n" +
                    "JECC - Reference Data - History dialog does not fit into screen\n" +
                    "JECC - TrashBin - Fixed a bug where the GUI would show a fresh delete\n" +
                    "JECC - TrashBin - Fixed deleted Object not in bin\n" +
                    "JECC - add null pointer check\n" +
                    "JECC - removed multiple system.out \n" +
                    "JECC - DashboardPlugin - GaugeWidget - fixed null pointer\n" +
                    "\n" +
                    "JEAlarm - fixed missing attribute check\n" +
                    "\n" +
                    "JEDataProcessor - MathStep - implemented custom period\n" +
                    "JEDataProcessor - fixed problem with custom workday and math data objects\n" +
                    "\n" +
                    "JEStatus - added report table for report stati\n" +
                    "JEStatus - improved report status\n" +
                    "JEStatus - mirrored html status to html file attribute\n" +
                    "\n" +
                    "Notifier - fixed null pointer on several occasions\n\n" +
                    "------Version 3.9.63------\n" +
                    "JECC - AccountingPlugin - added write check for visible tabs\n" +
                    "JECC - AccountingPlugin - added zooming \n" +
                    "JECC - AccountingPlugin - fixed user permissions for sys admin\n" +
                    "JECC - Add a note to the changed password\n" +
                    "JECC - Add new calculation check dialog\n" +
                    "JECC - AlarmPlugin - adde a button to each alarm, to directly see / change the alarmsettings\n" +
                    "JECC - CalculationNameFormatter - added ' to bad chars in variable names\n" +
                    "JECC - ChartPlugin - BubbleChart - allowing negative values on x axis\n" +
                    "JECC - ChartPlugin - HeatMapChart - added config\n" +
                    "JECC - ChartPlugin - bugfixes\n" +
                    "JECC - ChartPlugin - disabled overlay close on load analysis dialog\n" +
                    "JECC - ChartPlugin - fixed alarm map displaying too much information\n" +
                    "JECC - ChartPlugin - fixed disabled quick selection box\n" +
                    "JECC - ChartPlugin - fixed disabled toolbar working properly\n" +
                    "JECC - ChartPlugin - fixed memory leak\n" +
                    "JECC - DashboardPlugin - Fixed Load/Save Dialog says Analysis instead of Dashboard\n" +
                    "JECC - DashboardPlugin - changed zoom behaving for dynamic zoom levels\n" +
                    "JECC - DashboardPlugin - fixed fir to height zoom level\n" +
                    "JECC - DashboardPlugin - removed debug message\n" +
                    "JECC - DashboardPlugin - implemented new Widget Gauge\n" +
                    "JECC - DashboardPlugin - implemented new Widget Gauge added missing files\n" +
                    "JECC - DashboardPlugin - ChartWidget - fixed null pointer for holiday manager\n" +
                    "JECC - DashboardPlugin - DashBoardToolbar - fixed null pointer for JEVisHelp\n" +
                    "JECC - DashboardPlugin - unlock icon not working\n" +
                    "JECC - DashboardPlugin - fixed HeatMapChart display\n" +
                    "JECC - DataDialog - added scroll option for small resolutions\n" +
                    "JECC - Dialog - EnterDataDialog - fixed opening\n" +
                    "JECC - EnterDataDialog - added missing translation \n" +
                    "JECC - FXLogin - added check to message box\n" +
                    "JECC - Fixed and bug where the login messages crash the login\n" +
                    "JECC - Holidays - fixed null pointer\n" +
                    "JECC - Holidays - fixed wrong calendar on multi-site systems\n" +
                    "JECC - I18n - prepared arabic language\n" +
                    "JECC - JEVisTree - Fixed an bug where JEVisAttributes would be casted as JEVisObject\n" +
                    "JECC - Login - changed pw\n" +
                    "JECC - Login - improved auto-login with info text and traditional background\n" +
                    "JECC - Loytec Assistant - bug fix backnet\n" +
                    "JECC - Loytec Assistant - implemented to import bacnet trends\n" +
                    "JECC - Made the dashboard side config page permanent instep of hover\n" +
                    "JECC - NotesPlugin - added translations\n" +
                    "JECC - ObjectPlugin - AlarmEditor - fixed null pointer\n" +
                    "JECC - ObjectPlugin - FileEditor - added option for json files\n" +
                    "JECC - ObjectPlugin - FileEditor - fixed extension recognition\n" +
                    "JECC - ObjectPlugin - ObjectTable - improved memory performance on large data structures\n" +
                    "JECC - ObjectPlugin - RenameDialog - added warning for missing user rights\n" +
                    "JECC - ObjectPlugin - SampleTable - added expert tools for base and user data\n" +
                    "JECC - ObjectPlugin - SampleTable - increased width of note column\n" +
                    "JECC - ObjectPlugin - TreeHelper - added range to move sample ts by period\n" +
                    "JECC - ObjectPlugin - TreeHelper - fixed delete option\n" +
                    "JECC - ObjectPlugin - implemented basic json viewer\n" +
                    "JECC - ProgressForm - increased size a bit\n" +
                    "JECC - TRCPlugin - added data input option for OutputView\n" +
                    "JECC - TRCPlugin - fixed TemplateCalculationFormulaDialog not scrollable\n" +
                    "JECC - TablePlugin - added fraction digits configuration\n" +
                    "JECC - TablePlugin - added path column\n" +
                    "JECC - TablePlugin - fixed autofit for multisite\n" +
                    "JECC - TablePlugin - improved EnterDataDialog\n" +
                    "JECC - Themes - improved dark theme settings\n" +
                    "JECC - addeed Nullpointer Check and try catch\n" +
                    "JECC - all file chooser fixing invalid chars\n" +
                    "JECC - bugfix load Dashboard dialog now shows laod / new Dashboard instead of Analysis\n" +
                    "JECC - fix Translation Dialog no Enter required to commit change on Translation\n" +
                    "JECC - fixed dialog background\n" +
                    "\n" +
                    "DataCollector - added JEVisServer data source\n" +
                    "DataCollector - Fixed a bug where the Vida350 driver did not use the port\n" +
                    "\n" +
                    "JEStatus - Fixed merge mistake, with duplicate WirelessLogicStatus\n" +
                    "\n" +
                    "DataCollector - Fixed null pointer in a non-existing dynamic path\n" +
                    "DataCollector - Fixed null pointer in unchecked string split. This should fix the DWD readout problem\n" +
                    "DataCollector - Fixed timeout from msec to seconds \n" +
                    "DataCollector - JEDataCollector - Add checks and fallbacks for the isReady function\n" +
                    "DataCollector - JEDataCollector - Fixed a bug where HTTP auth did not work\n" +
                    "DataCollector - JEDataCollector - Fixed a bug where the VIDA350DataSource did not set the channel object\n" +
                    "DataCollector - JEDataCollector - Fixed a bug where the second channel would add the port a second time to the url\n" +
                    "\n" +
                    "JECommons - AggregatorFunction - fixed wrong quantity check for aggregation\n" +
                    "JECommons - AlarmProcess - standby time working properly\n" +
                    "JECommons - Calculation - fixed user data input\n" +
                    "JECommons - CommonMethods - added period dependency on delete clean data for math objects\n" +
                    "JECommons - ConnectionFactory - removed deprecated driver class \n" +
                    "JECommons - DataProcessing - DifferentialStep - fixed weekly aggregation \n" +
                    "JECommons - DateHelper - added the year before last to calendar presets\n" +
                    "JECommons - GapsAndLimits - fixed cache not working properly for median\n" +
                    "JECommons - MathDataObject - added safety to ready check \n" +
                    "JECommons - MathStep - added sum functionality \n" +
                    "JECommons - PeriodHelper - added calendar week to output formatting\n" +
                    "JECommons - WorkDays - added workaround for objects not in buildings\n" +
                    "JECommons - fixed some work day aggregation problems\n" +
                    "\n" +
                    "JEDataProcessor - MathStep - fixed formula processing \n" +
                    "JEDataProcessor - ProcessManager - fixed Math Object ready check\n" +
                    "JEDataProcessor - aggregation daily fixed last day\n" +
                    "JEDataProcessor - fixed bug with not differential monthly values upscaling\n" +
                    "\n" +
                    "JEStatus - Add additional null pointer checks.\n" +
                    "JEStatus - WirelessLogicRequest - using httpurlconnection instead of curl for get request\n" +
                    "JEStatus - Wirelesslogic added Translation for Customer Field 1-4\n" +
                    "JEStatus - add null pointer check for targets.\n" +
                    "JEStatus - added Customer Infos for offline Sim Cards\n" +
                    "JEStatus - change error to warning\n" +
                    "JEStatus - logging improvement\n" +
                    "\n" +
                    "JEWebService - Add a permission check for deleting the Sys Admin attribute\n\n" +
                    "------Version 3.9.62------\n" +
                    "JECC - AccountingPlugin - added write check for visible tabs\n" +
                    "JECC - ChartPlugin - bugfixes\n" +
                    "JECC - ChartPlugin - disabled overlay close on load analysis dialog\n" +
                    "JECC - ChartPlugin - fixed alarm map displaying too much information\n" +
                    "JECC - ChartPlugin - fixed disabled quick selection box\n" +
                    "JECC - ChartPlugin - fixed disabled toolbar working properly \n" +
                    "JECC - ChartPlugin - fixed memory leak\n" +
                    "JECC - ObjectPlugin - AlarmEditor - fixed null pointer\n" +
                    "JECC - ObjectPlugin - FileEditor - added option for json files\n" +
                    "JECC - ObjectPlugin - FileEditor - fixed extension recognition\n" +
                    "JECC - ObjectPlugin - ObjectTable - improved memory performance on large data structures\n" +
                    "JECC - ObjectPlugin - RenameDialog - added warning for missing user rights\n" +
                    "JECC - ObjectPlugin - SampleTable - added expert tools for base and user data\n" +
                    "JECC - ObjectPlugin - SampleTable - increased width of note column\n" +
                    "JECC - ObjectPlugin - implemented basic json viewer\n" +
                    "JECC - ProgressForm - increased size\n" +
                    "JECC - TRCPlugin - added data input option for OutputView\n" +
                    "JECC - TablePlugin - added fraction digits configuration\n" +
                    "JECC - TablePlugin - added path column\n" +
                    "JECC - TablePlugin - fixed autofit for multisite\n" +
                    "JECC - TablePlugin - improved EnterDataDialog\n" +
                    "\n" +
                    "Classes - Measurement Instruments - fixed attribute inheritance and translation\n" +
                    "Classes - added missing daily report setting\n" +
                    "\n" +
                    "JECommons - AggregatorFunction - fixed wrong quantity check for aggregation\n" +
                    "JECommons - AlarmProcess - standby time working properly\n" +
                    "JECommons - Calculation - fixed user data input \n" +
                    "JECommons - DateHelper - added the year before last to calendar presets\n" +
                    "JECommons - GapsAndLimits - fixed cache not working properly for median\n" +
                    "JECommons - MathStep - added sum functionality \n" +
                    "JECommons - WorkDays - added workaround for objects not in buildings\n" +
                    "\n" +
                    "JEStatus - WirelessLogicRequest - using httpurlconnection instead of curl for get request\n" +
                    "JEStatus - improved logging\n\n" +
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
