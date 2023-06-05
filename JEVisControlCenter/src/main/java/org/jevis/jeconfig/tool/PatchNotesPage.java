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

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextArea;
import io.github.palexdev.materialfx.controls.MFXButton;
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
            "------Version 3.9.67------\n" +
                    "JECC - AccountingPlugin - Data are not updated after changing the period \n" +
                    "JECC - AccountingPlugin - OutputView - fixed sort order for multi formula input formulas\n" +
                    "JECC - AccountingPlugin - added ranging values\n" +
                    "JECC - AccountingPlugin - added zooming \n" +
                    "JECC - AccountingPlugin - fixed date interval for fixed time restrictions\n" +
                    "JECC - AccountingPlugin - improved box pathing\n" +
                    "JECC - AccountingPlugin - improved reload on save\n" +
                    "JECC - AccountingPlugin - missing formula input calculation\n" +
                    "JECC - AccountingPlugin - time discreet value generation\n" +
                    "JECC - AlarmPlugin - fixed await all tasks check for list\n" +
                    "JECC - AlarmPlugin - to accept alarms now execute permission is required\n" +
                    "JECC - CalculationExtension - UI does not check for \"#\"\n" +
                    "JECC - ChartPlugin - \"updata data\"-button shows no effect, even if new clean data exists\n" +
                    "JECC - ChartPlugin - AnalysesComboBox - fixed name not showing up under some circumstances\n" +
                    "JECC - ChartPlugin - AnalysesComboBox - more checks \n" +
                    "JECC - ChartPlugin - Automatic Colors\n" +
                    "JECC - ChartPlugin - BubbleChart - It should be configurable in the advanced settings if one wants to start the y-axis at 0 or not (dynamic)\n" +
                    "JECC - ChartPlugin - BubbleChart - default note dialog (rmb) shows bubble content\n" +
                    "JECC - ChartPlugin - CSV export not working in new analysis\n" +
                    "JECC - ChartPlugin - Calculation of data incl./excl. a time should show the sum as kWh if kW is analyzed\n" +
                    "JECC - ChartPlugin - ChartDataRow - fixed null pointer\n" +
                    "JECC - ChartPlugin - ChartTab - Table - added processor column for clean/math/forecast data selection\n" +
                    "JECC - ChartPlugin - ChartTab - fixed used colors not working properly\n" +
                    "JECC - ChartPlugin - ChartTab - improved columns on chart type\n" +
                    "JECC - ChartPlugin - Charts - Show sum of data rows does not work\n" +
                    "JECC - ChartPlugin - DataPointTableViewPointer - fixed second x-axis mouse moved event\n" +
                    "JECC - ChartPlugin - FavoriteAnalysisHandler - automatic alphanumeric sorting when adding\n" +
                    "JECC - ChartPlugin - Heatmap does not show all data in some cases\n" +
                    "JECC - ChartPlugin - If kW is choosen, but the period is >1h the diagram should show kWh\n" +
                    "JECC - ChartPlugin - Limit check does counts the time correctly\n" +
                    "JECC - ChartPlugin - LoadAnalysisDialog - fixed aggregation not working \n" +
                    "JECC - ChartPlugin - NewSelectionDialog - allowing dynamic data row changing\n" +
                    "JECC - ChartPlugin - NewSelectionDialog - fixed dialog on open\n" +
                    "JECC - ChartPlugin - NewSelectionDialog - fixed problem with closing new chart tab\n" +
                    "JECC - ChartPlugin - NewSelectionDialog - moved CommonTab to the back\n" +
                    "JECC - ChartPlugin - No Name after Save\n" +
                    "JECC - ChartPlugin - ProcessorBox - added TableCell functions\n" +
                    "JECC - ChartPlugin - ProcessorTableCell - added table cell\n" +
                    "JECC - ChartPlugin - Regression function only considers data from the left axis\n" +
                    "JECC - ChartPlugin - Saving of new analyses does not work\n" +
                    "JECC - ChartPlugin - Selection Dialog Search\n" +
                    "JECC - ChartPlugin - Settings Dialog Cancel not Working properly\n" +
                    "JECC - ChartPlugin - StackedChart - fixed null pointer in case of secondary y-axis\n" +
                    "JECC - ChartPlugin - StackedCharts - fixed sum calculation for units which are not quantities\n" +
                    "JECC - ChartPlugin - Table (V) does not support text data\n" +
                    "JECC - ChartPlugin - Table (V) filter function considers the decimal separators\n" +
                    "JECC - ChartPlugin - Table - added commit on unit change\n" +
                    "JECC - ChartPlugin - TableChartV - added filter option\n" +
                    "JECC - ChartPlugin - TableChartV - changed header \n" +
                    "JECC - ChartPlugin - TableChartV - fixed filter column name mismatch\n" +
                    "JECC - ChartPlugin - TableChartV - fixed null pointer\n" +
                    "JECC - ChartPlugin - TableChartV - fixed sort order on filter\n" +
                    "JECC - ChartPlugin - ValuesTable - changed column name for zero values\n" +
                    "JECC - ChartPlugin - ValuesTable - fixed column header text\n" +
                    "JECC - ChartPlugin - XYChart - fixed null pointer on missing values for stacked charts\n" +
                    "JECC - ChartPlugin - added ColorTable\n" +
                    "JECC - ChartPlugin - added data heirs to standard selection\n" +
                    "JECC - ChartPlugin - added default unit \n" +
                    "JECC - ChartPlugin - added mass flow units\n" +
                    "JECC - ChartPlugin - added new magic button to calculate values for time range\n" +
                    "JECC - ChartPlugin - added stacked charts\n" +
                    "JECC - ChartPlugin - minor bugfixes\n" +
                    "JECC - ChartPlugin - csv export - fixed missing name\n" +
                    "JECC - ChartPlugin - different periods in one chart confuse mouse position listener\n" +
                    "JECC - ChartPlugin - disabling data in charts does not adjust the y-axis propperly after zoom-in\n" +
                    "JECC - ChartPlugin - double click on color in header allows hiding datarow\n" +
                    "JECC - ChartPlugin - favorites - fixed name problem\n" +
                    "JECC - ChartPlugin - favorites - initial commit\n" +
                    "JECC - ChartPlugin - favorites - menu not closing after ok\n" +
                    "JECC - ChartPlugin - fixed LogicalChart problem with wrong id\n" +
                    "JECC - ChartPlugin - fixed axis in logic chart\n" +
                    "JECC - ChartPlugin - fixed bug when not selecting an analysis and clicking on load\n" +
                    "JECC - ChartPlugin - fixed double.max values appearing in header table\n" +
                    "JECC - ChartPlugin - fixed export function\n" +
                    "JECC - ChartPlugin - fixed favorite analysis missing settings\n" +
                    "JECC - ChartPlugin - fixed min max calculation for stacked header entries\n" +
                    "JECC - ChartPlugin - fixed missing dates in dashboard link\n" +
                    "JECC - ChartPlugin - fixed name bug in selection dialog\n" +
                    "JECC - ChartPlugin - fixed null pointer in ColumnChart and TableChartV\n" +
                    "JECC - ChartPlugin - fixed null pointer in heat map chart\n" +
                    "JECC - ChartPlugin - fixed null pointer on saving anlysis\n" +
                    "JECC - ChartPlugin - fixed problem in chart selection dialog \n" +
                    "JECC - ChartPlugin - fixed problem when directly jumping from dashboard to chart plugin\n" +
                    "JECC - ChartPlugin - fixed saving too late when switching analysis after changing something\n" +
                    "JECC - ChartPlugin - fixed secondary yaxis problem\n" +
                    "JECC - ChartPlugin - fixed some aggregation problem \n" +
                    "JECC - ChartPlugin - fixed some favorite analysis bugs\n" +
                    "JECC - ChartPlugin - fixed some problem with auto kW to kWh conversion\n" +
                    "JECC - ChartPlugin - fixed some problem with chart type recognition\n" +
                    "JECC - ChartPlugin - fixed some unit selection bug\n" +
                    "JECC - ChartPlugin - fixed temporary analysis disabled on some users \n" +
                    "JECC - ChartPlugin - fixed temporary analysis not showing name in some cases\n" +
                    "JECC - ChartPlugin - fixed unit bug\n" +
                    "JECC - ChartPlugin - fixed wrong date on 0-1 hours in date presets\n" +
                    "JECC - ChartPlugin - improved height scaling\n" +
                    "JECC - ChartPlugin - improved quick analysis selection box performance\n" +
                    "JECC - ChartPlugin - let the data rows show different time frames\n" +
                    "JECC - ChartPlugin - limit zoom to period to avoid \"-\" in table header fixes \n" +
                    "JECC - ChartPlugin - limiting PieChart to 10 data rows\n" +
                    "JECC - ChartPlugin - mouse hover on min/max shows timestamp(s) (up to 40)\n" +
                    "JECC - ChartPlugin - removed new residence\n" +
                    "JECC - ChartPlugin - small minor appearance changes\n" +
                    "JECC - ChartPlugin - stacked charts - sorte data rows by name\n" +
                    "JECC - ChartPlugin - stacked charts - added period\n" +
                    "JECC - ChartPlugin - stacked charts - changed sum description\n" +
                    "JECC - ChartPlugin - y-Axis does not consider the \"sum of data rows\"-values (after zoom-in)\n" +
                    "JECC - ChartPlugin - zoom not working\n" +
                    "JECC - DashboardPlugin - Add an open start page button\n" +
                    "JECC - DashboardPlugin - Add default values to GaugePojo\n" +
                    "JECC - DashboardPlugin - Add feature to delete dashboards \n" +
                    "JECC - DashboardPlugin - Add icon for side editor\n" +
                    "JECC - DashboardPlugin - Add localisation to the widget name\n" +
                    "JECC - DashboardPlugin - Add missing Classes\n" +
                    "JECC - DashboardPlugin - Add text overrun for the dashboad list\n" +
                    "JECC - DashboardPlugin - Add the feature to select multiple widgets with a drag mouse gesture\n" +
                    "JECC - DashboardPlugin - Catch missing configuration parameters\n" +
                    "JECC - DashboardPlugin - Changed translation\n" +
                    "JECC - DashboardPlugin - Edit function will be only be visible in edit mode\n" +
                    "JECC - DashboardPlugin - Fixed a bug where a new Widget did not display the value\n" +
                    "JECC - DashboardPlugin - Fixed a bug where part of the disabled toolbar was shown\n" +
                    "JECC - DashboardPlugin - Fixed an bug where disabling the limit or procent did not update\n" +
                    "JECC - DashboardPlugin - Fixed config is show even if no widegt is selected\n" +
                    "JECC - DashboardPlugin - Fixed key events do not work for new widgets\n" +
                    "JECC - DashboardPlugin - Fixed some bug in the side pane editor\n" +
                    "JECC - DashboardPlugin - Fixed typo in the title field\n" +
                    "JECC - DashboardPlugin - Fixes Problem with Adding an Image (Bug)\n" +
                    "JECC - DashboardPlugin - Gauge / Linear gauge fix for negative values\n" +
                    "JECC - DashboardPlugin - Gauge config tab overhaul\n" +
                    "JECC - DashboardPlugin - Gauge, LinearGauge, Shape shows warning if no data available\n" +
                    "JECC - DashboardPlugin - Improvement the zoom behavior is the zoom is set to fit to screen\n" +
                    "JECC - DashboardPlugin - Last Value\n" +
                    "JECC - DashboardPlugin - Linear Gauge - Major Tick Step Distance adjustable\n" +
                    "JECC - DashboardPlugin - LinearGauge - Last Value\n" +
                    "JECC - DashboardPlugin - LinearGauge - updated tick label spacing\n" +
                    "JECC - DashboardPlugin - LinkerWidget - fixed anlaysis paths not working properly\n" +
                    "JECC - DashboardPlugin - No option to set text in title widget as bold \n" +
                    "JECC - DashboardPlugin - No option to set text in title widget as underlined\n" +
                    "JECC - DashboardPlugin - Performance improvement and reduced memory usage\n" +
                    "JECC - DashboardPlugin - Pie plugin does not remember the digits setting fixes \n" +
                    "JECC - DashboardPlugin - Plus Minus Widgets\n" +
                    "JECC - DashboardPlugin - Removed widget icons\n" +
                    "JECC - DashboardPlugin - Shape - implemented step distance\n" +
                    "JECC - DashboardPlugin - Shape Widget fix translation\n" +
                    "JECC - DashboardPlugin - Shape config tab overhaul\n" +
                    "JECC - DashboardPlugin - Slider / Plus Minus font color changeable\n" +
                    "JECC - DashboardPlugin - TableWidget will now show the chart model title\n" +
                    "JECC - DashboardPlugin - The dashboard fast selection will now update if re reload button is used\n" +
                    "JECC - DashboardPlugin - The side pane editor allows now to edit the title\n" +
                    "JECC - DashboardPlugin - The side pane editor can now be toggled to stay visible or be hidden\n" +
                    "JECC - DashboardPlugin - The side pane editor show now the selected input data\n" +
                    "JECC - DashboardPlugin - TimeFrame Widget Added\n" +
                    "JECC - DashboardPlugin - Timeframe Widget - fixed translation\n" +
                    "JECC - DashboardPlugin - Timeframe Widget - remove sample handler from Widgets -> use of parent sampleHandler\n" +
                    "JECC - DashboardPlugin - Timeframe Widget set hgrow / vgrow for grid pane\n" +
                    "JECC - DashboardPlugin - Update the add icon\n" +
                    "JECC - DashboardPlugin - ValueWidget - last value fix\n" +
                    "JECC - DashboardPlugin - ValueWidget - temporary removed setIntervalForLastValue causing too many errors\n" +
                    "JECC - DashboardPlugin - added Battery Widget\n" +
                    "JECC - DashboardPlugin - added Slider, Toggle Switch, Plus Minus Widgets\n" +
                    "JECC - DashboardPlugin - added opacity for Shape widget\n" +
                    "JECC - DashboardPlugin - changed DatePicker Color of Toolbar\n" +
                    "JECC - DashboardPlugin - changed Icons to Icons from Fontawsome\n" +
                    "JECC - DashboardPlugin - changed icons for Google Material Icons\n" +
                    "JECC - DashboardPlugin - enabled table menu for aggregation/manipulation columns\n" +
                    "JECC - DashboardPlugin - fixed some Widgets did not refresh on commit\n" +
                    "JECC - DashboardPlugin - fixed wrong translation string\n" +
                    "JECC - DashboardPlugin - implemented Shape Widget\n" +
                    "JECC - DashboardPlugin - improved code for overrun\n" +
                    "JECC - DashboardPlugin - moved convert_to_percent to Helper Class\n" +
                    "JECC - DashboardPlugin - removed methode setLastInterval, adjusted default size\n" +
                    "JECC - DashboardPlugin - set hgrow for Table View in config tab\n" +
                    "JECC - DashboardPlugin - unit fix\n" +
                    "JECC - DashboardPlugin - updated config tab GUI\n" +
                    "JECC - EnterDataDialog - added confirmation dialog for resetting dependent data row (need appropriate rights)\n" +
                    "JECC - EnterDataDialog - fixed period check for differential raw values\n" +
                    "JECC - FXLogin - Add missing translation for missing login messages\n" +
                    "JECC - FXLogin - Removed border form the statusbox. and changed the text\n" +
                    "JECC - FXLogin - Removed shadows from login message box\n" +
                    "JECC - FXLogin - Update button shows wrong text\n" +
                    "JECC - FXLogin - changed blue to grey for header/footer\n" +
                    "JECC - FXLogin - fixed typo\n" +
                    "JECC - OPCExtension - object creation sets period to default value (needs readout from linx?)\n" +
                    "JECC - ObjectPlugin - Allow to copy the Unit in the AttributeCopyDialog\n" +
                    "JECC - ObjectPlugin - CalculationMethods - added all children to delete list\n" +
                    "JECC - ObjectPlugin - Fixed a bug where a moved object will be present in the bin\n" +
                    "JECC - ObjectPlugin - GapFillingEditor - fixed non standard decimal entry\n" +
                    "JECC - ObjectPlugin - Hotfix for LocalNameDialog\n" +
                    "JECC - ObjectPlugin - Loytec Assistant select parent if child is selected\n" +
                    "JECC - ObjectPlugin - Report Wizard - changed attribute reference\n" +
                    "JECC - ObjectPlugin - Report Wizard - fix Error message for invalid template\n" +
                    "JECC - ObjectPlugin - Report Wizard - new reference for JEVis Attribute , Error message for invalid template\n" +
                    "JECC - ObjectPlugin - ReportWizardDialog - Wizard works with String Data\n" +
                    "JECC - ObjectPlugin - ReportWizardDialog - fixed all attribute filter\n" +
                    "JECC - ObjectPlugin - ReportWizardDialog Error Window if JEVis Object cannot parsed to ReportLink\n" +
                    "JECC - ObjectPlugin - ReportWizardDialog fix ReportPeriodBox only shows if period fixed\n" +
                    "JECC - ObjectPlugin - ReportWizardDialog updated (edit,add,delete) after report is created\n" +
                    "JECC - ObjectPlugin - RoleExtention - improved path display for groups\n" +
                    "JECC - ObjectPlugin - Translation in rename dialog rename language you logged in as, in Wizards add all available local names\n" +
                    "JECC - ObjectPlugin - Tree - Bin is now sorted by deleting time\n" +
                    "JECC - ObjectPlugin - Tree - the bin will now always be the last element in the tree.\n" +
                    "JECC - ObjectPlugin - TreeSelectionDialog - added filter box\n" +
                    "JECC - ObjectPlugin - TreeSelectionDialog - fixed target selection showing on parent item\n" +
                    "JECC - ObjectPlugin - add values in between improvements for localization of value and more date options\n" +
                    "JECC - ObjectPlugin - fixed some null pointer in RangingValueEditor\n" +
                    "JECC - ObjectPlugin - go to source now works with all channels\n" +
                    "JECC - Statusbar - Process Monitor is not translated into English\n" +
                    "JECC - Statusbar - fixed status tooltip\n" +
                    "JECC - TRCPlugin - Ranging Values support - initial commit\n" +
                    "JECC - TRCPlugin - added constant function for calculation outputs\n" +
                    "JECC - TRCPlugin - allowing analysis links for single value template outputs\n" +
                    "JECC - TRCPlugin - enabled Tooltips for TemplateOutputs\n" +
                    "JECC - TRCPlugin - fixed selection problem for base data input\n" +
                    "JECC - TRCPlugin - fixed sort order for outputs\n" +
                    "JECC - TRCPlugin - fixed time dependent value generation for formulas\n" +
                    "JECC - TRCPlugin - improved input naming\n" +
                    "JECC - TRCPlugin - improved input sorting\n" +
                    "JECC - TRCPlugin - integration of ranging values \n" +
                    "JECC - Themes - improved dark theme settings\n" +
                    "JECC - Update themes for new icon set\n" +
                    "JECC - ValueWidget - removed double webservice request\n" +
                    "JECC - Widget radar chart added\n" +
                    "JECC - changed Icons to Icons from Fontawsome\n" +
                    "JECC - changed Plugin Icons\n" +
                    "\n" +
                    "Classes - added new attribute to user object for favorites\n" +
                    "Classes - rework attribute period\n" +
                    "\n" +
                    "DataCollector - DWDWDParser - udpated dependencies\n" +
                    "DataCollector - HTTPDataSource - updated dependencies\n" +
                    "DataCollector - JEDataCollector - updated dependencies\n" +
                    "DataCollector - cleaned up poms\n" +
                    "DataCollector - sFTPDataSource - fixed some null pointer\n" +
                    "\n" +
                    "JEAPI - Implementation of getParent function\n" +
                    "JEAPI - add getParent function\n" +
                    "\n" +
                    "JEAPI-WS - updated dependencies\n" +
                    "\n" +
                    "JEAlarm - added AlarmPeriod.NONE as functionality, using last alarm as start for period\n" +
                    "\n" +
                    "JECommons - Add a JEVisClass printer to create the reference class\n" +
                    "JECommons - Add a new reference class for all JEVisClasses \n" +
                    "JECommons - DataProcessing - simple sample generator\n" +
                    "JECommons - MathFunction - fixed missing values for avg with aggregation \n" +
                    "JECommons - ObjectRelations - fixed relative path problem for multi directory systems\n" +
                    "JECommons - TargetHelper - fixed bug\n" +
                    "JECommons - updated dependencies\n" +
                    "\n" +
                    "JEDataProcessor - MathDataObject - fixed ready check for timestamps equal\n" +
                    "JEDataProcessor - fixed bug for period alignment on periods greater then days\n" +
                    "\n" +
                    "JEReport - ReportLinkProperty - added parental data object name to no data message\n" +
                    "JEReport - supports custom workdays for sanity check\n" +
                    "JEReport - updated dependencies\n" +
                    "\n" +
                    "JEStatus - added object name to report status\n" +
                    "\n" +
                    "JEWebService - ResourceSample - fixed exception permissions for alarm status \n" +
                    "JEWebService - ResourceSample - fixed wrong read file check\n" +
                    "JEWebservice - ResourceSample - fixed user exception for files\n\n" +
                    "------Version 3.9.66------\n" +
                    "JECC - AccountingPlugin - added zooming \n" +
                    "JECC - Add missing translation for missing login messages\n" +
                    "JECC - AlarmPlugin - to accept alarms you must have full write access to the alarm objects\n" +
                    "JECC - ChartPlugin - if aggregation is deselected, the time filter is set to Individual with a time range from now to now\n" +
                    "JECC - ChartPlugin - BubbleChart - default note dialog (rmb) shows bubble content\n" +
                    "JECC - ChartPlugin - CSV export not working in new analysis fixes\n" +
                    "JECC - ChartPlugin - ChartDataRow - fixed null pointer\n" +
                    "JECC - ChartPlugin - ChartTab - improved columns on chart type\n" +
                    "JECC - ChartPlugin - DataPointTableViewPointer - fixed second x-axis mouse moved event\n" +
                    "JECC - ChartPlugin - NewSelectionDialog - allowing dynamic data row changing\n" +
                    "JECC - ChartPlugin - NewSelectionDialog - fixed problem with closing new chart tab\n" +
                    "JECC - ChartPlugin - NewSelectionDialog - moved CommonTab to the back\n" +
                    "JECC - ChartPlugin - Saving of new analyses does not work fixes\n" +
                    "JECC - ChartPlugin - Table (V) does not support text data  \n" +
                    "JECC - ChartPlugin - Table (V) filter function considers the decimal separators\n" +
                    "JECC - ChartPlugin - TableChartV - changed header \n" +
                    "JECC - ChartPlugin - TableChartV - fixed filter column name mismatch\n" +
                    "JECC - ChartPlugin - TableChartV - fixed null pointer\n" +
                    "JECC - ChartPlugin - TableChartV - fixed sort order on filter\n" +
                    "JECC - ChartPlugin - added ColorTable\n" +
                    "JECC - ChartPlugin - added new magic button to calculate values for time range\n" +
                    "JECC - ChartPlugin - different periods in one chart confuse mouse position listener\n" +
                    "JECC - ChartPlugin - fixed bug when not selecting an analysis and clicking on load\n" +
                    "JECC - ChartPlugin - fixed name bug in selection dialog\n" +
                    "JECC - ChartPlugin - fixed problem in chart selection dialog \n" +
                    "JECC - ChartPlugin - fixed some problem with chart type recognition\n" +
                    "JECC - ChartPlugin - improved height scaling\n" +
                    "JECC - ChartPlugin - let the data rows show different time frames\n" +
                    "JECC - ChartPlugin - removed new residence\n" +
                    "JECC - ChartPlugin - small minor appearance changes\n" +
                    "JECC - ChartPlugin - load in the wrong time range when creating\n" +
                    "JECC - DashboardPlugin - Add an open start page button\n" +
                    "JECC - DashboardPlugin - Add feature to delete dashboards \n" +
                    "JECC - DashboardPlugin - Add text overrun for the dashboad list\n" +
                    "JECC - DashboardPlugin - Add the feature to select multiple widgets with a drag mouse gesture.\n" +
                    "JECC - DashboardPlugin - Changed translation\n" +
                    "JECC - DashboardPlugin - Edit function will be only be visible in edit mode\n" +
                    "JECC - DashboardPlugin - Gauge, LinearGauge, Shape shows warning if no data available\n" +
                    "JECC - DashboardPlugin - Problem with Adding an Image (Bug)\n" +
                    "JECC - DashboardPlugin - TableWidget will now show the chart model title\n" +
                    "JECC - DashboardPlugin - The dashboard fast selection will now update if re reload button is used\n" +
                    "JECC - DashboardPlugin - Timeframe Widget - remove sample handler from Widgets -> use of parent sampleHandler\n" +
                    "JECC - DashboardPlugin - Timeframe Widget - set hgrow / vgrow for grid pane\n" +
                    "JECC - DashboardPlugin - Update the add icon\n" +
                    "JECC - DashboardPlugin - Widget radar chart added\n" +
                    "JECC - DashboardPlugin - a bug where part of the disabled toolbar was not hidden\n" +
                    "JECC - DashboardPlugin - added Battery Widget\n" +
                    "JECC - DashboardPlugin - added opacity for Shape widget\n" +
                    "JECC - DashboardPlugin - changed DatePicker Color of Toolbar\n" +
                    "JECC - DashboardPlugin - config is show even if no widegt is selected\n" +
                    "JECC - DashboardPlugin - improved code for overrun\n" +
                    "JECC - DashboardPlugin - key events do not work for new widgets\n" +
                    "JECC - DashboardPlugin - moved convert_to_percent to Helper Class\n" +
                    "JECC - DashboardPlugin - set hgrow for Table View in config tab\n" +
                    "JECC - DashboardPlugin - Timeframe Widget - fixed translation\n" +
                    "JECC - EnterDataDialog - added confirmation dialog for resetting dependent data row (need appropriate rights)\n" +
                    "JECC - FXLogin - add new flags\n" +
                    "JECC - FXLogin - changed blue to grey for header/footer\n" +
                    "JECC - ObjectPlugin - The menu items in the top navigation under Edit do not work\n" +
                    "JECC - ObjectPlugin - Fixed a bug where a moved object will be present in the bin\n" +
                    "JECC - ObjectPlugin - GapFillingEditor - fixed non standard decimal entry\n" +
                    "JECC - ObjectPlugin - OPC Extension - assistant select parent if child is selected\n" +
                    "JECC - ObjectPlugin - OPC Extension - object creation sets period to default value (needs readout from linx?)\n" +
                    "JECC - ObjectPlugin - Report Wizard - changed attribute reference\n" +
                    "JECC - ObjectPlugin - Report Wizard - fix Error message for invalid template\n" +
                    "JECC - ObjectPlugin - Report Wizard - new reference for JEVis Attribute , Error message for invalid template\n" +
                    "JECC - ObjectPlugin - ReportWizardDialog - fixed all attribute filter\n" +
                    "JECC - ObjectPlugin - ReportWizardDialog - Error Window if JEVis Object cannot parsed to ReportLink\n" +
                    "JECC - ObjectPlugin - ReportWizardDialog - fix ReportPeriodBox only shows if period fixed\n" +
                    "JECC - ObjectPlugin - ReportWizardDialog - updated (edit,add,delete) after report is created\n" +
                    "JECC - ObjectPlugin - Tree - Bin is now sorted by deleting time\n" +
                    "JECC - ObjectPlugin - Tree - the bin will now always be the last element in the tree\n" +
                    "JECC - ObjectPlugin - TreeSelectionDialog - added filter box\n" +
                    "JECC - ObjectPlugin - TreeSelectionDialog - fixed target selection showing on parent item\n" +
                    "JECC - ObjectPlugin - Update Toolbar Tooltips\n" +
                    "JECC - ObjectPlugin - add values in between improvements for localization of value and more date options\n" +
                    "JECC - ObjectPlugin - if a single i18n fails, the whole translation fails -> that should be caught\n" +
                    "JECC - Removed shadows from login message box\n" +
                    "JECC - Removed some comments\n" +
                    "JECC - Themes - improved dark theme settings\n" +
                    "JECC - Translation in rename dialog rename language you logged in as, in Wizards add all available local names\n" +
                    "JECC - Update button shows wrong text\n" +
                    "JECC - Update themes for new icon set\n" +
                    "JECC - changed Plugin Icons\n" +
                    "JECC - duplicate user settings fix\n" +
                    "JECC - moved to original place\n" +
                    "\n" +
                    "Classes - Update Analysis.json\n" +
                    "Classes - added missing period attribute to String Data\n" +
                    "Classes - rework attribute period\n" +
                    "\n" +
                    "JEAPI - Implementation of getParent function\n" +
                    "JEAPI - add getParent function\n" +
                    "\n" +
                    "JECommons - Add a JEVisClass printer to create the reference class\n" +
                    "JECommons - Add a new reference class for all JEVisClasses \n" +
                    "JECommons - TargetHelper - fixed bug\n" +
                    "\n" +
                    "JEDataProcessor - MathDataObject - fixed ready check for timestamps equal\n" +
                    "JEDataProcessor - fixed bug for period alignment on periods greater then days\n" +
                    "\n" +
                    "JEReport - ReportLinkProperty - added parental data object name to no data message\n" +
                    "JEReport - supports custom workdays for sanity check\n" +
                    "\n" +
                    "JEWebService - Launcher Page in JEWebservice\n" +
                    "JEWebService - fixed forgotten comment\n\n" +
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
        MFXButton close = new MFXButton(I18n.getInstance().getString("welcome.close"));
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
