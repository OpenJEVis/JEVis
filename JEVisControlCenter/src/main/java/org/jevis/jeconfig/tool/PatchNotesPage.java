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
            "------Version 3.9.69------\n" +
                    "JECC - Accounting - Changing the name of a supplier will now take immediate effect\n" +
                    "JECC - Accounting - The new energy price will now be displayed after changing it\n" +
                    "JECC - AccountingPlugin - changed editor for double/long values to normal enter data dialog\n" +
                    "JECC - AccountingPlugin - fixed giant numbers bug\n" +
                    "JECC - AccountingPlugin - fixed translation attribute\n" +
                    "JECC - AccountingPlugin - improved boxes for energy supplier, energy grid operators and energymeteringpointoperators\n" +
                    "JECC - ActionPlugin - Action form will now formate numbers after typing\n" +
                    "JECC - ActionPlugin - Actions without any date will now be shown even if a date filter is set\n" +
                    "JECC - ActionPlugin - Add 0 years selection as possibility to allow disabling NVV\n" +
                    "JECC - ActionPlugin - Add Excel export prototype\n" +
                    "JECC - ActionPlugin - Add Table und Avtion export \n" +
                    "JECC - ActionPlugin - Add a check while calculation the sum for the media\n" +
                    "JECC - ActionPlugin - Add reload function\n" +
                    "JECC - ActionPlugin - Add static funktion to get EnPI object name\n" +
                    "JECC - ActionPlugin - Add status as sum fields \n" +
                    "JECC - ActionPlugin - Add summery table\n" +
                    "JECC - ActionPlugin - Add translations\n" +
                    "JECC - ActionPlugin - Allow text wrap\n" +
                    "JECC - ActionPlugin - Chaged pref width to better fit the content\n" +
                    "JECC - ActionPlugin - Changed default values for new action\n" +
                    "JECC - ActionPlugin - Changed export layout \n" +
                    "JECC - ActionPlugin - Changed titles \n" +
                    "JECC - ActionPlugin - Changed toolbar icons\n" +
                    "JECC - ActionPlugin - Changed translation\n" +
                    "JECC - ActionPlugin - Empty EnPI selection is now possible\n" +
                    "JECC - ActionPlugin - EnPI is now inverted\n" +
                    "JECC - ActionPlugin - EnPi and consumption switch GUI positions\n" +
                    "JECC - ActionPlugin - Enabled export button\n" +
                    "JECC - ActionPlugin - Export dialog\n" +
                    "JECC - ActionPlugin - Fixed NPV calculation\n" +
                    "JECC - ActionPlugin - Fixed a bug where a changed npc year did not start to update the ui\n" +
                    "JECC - ActionPlugin - Fixed a bug where always the last EnPI was selected\n" +
                    "JECC - ActionPlugin - Fixed a bug where consumption would have currency unit\n" +
                    "JECC - ActionPlugin - Fixed a bug where saving did not work because of an infinite number\n" +
                    "JECC - ActionPlugin - Fixed a bug where sum where not calculated based on the filter\n" +
                    "JECC - ActionPlugin - Fixed a bug where the Sum-Table is not shown\n" +
                    "JECC - ActionPlugin - Fixed a bug where the size of the summary was not correct\n" +
                    "JECC - ActionPlugin - Fixed amortisation text\n" +
                    "JECC - ActionPlugin - Hide Overview tab if only one plan exists\n" +
                    "JECC - ActionPlugin - Made the time selection range dynamic\n" +
                    "JECC - ActionPlugin - Minor changes\n" +
                    "JECC - ActionPlugin - More visible warning for deleting a plan\n" +
                    "JECC - ActionPlugin - Moved Summary Data & Table\n" +
                    "JECC - ActionPlugin - Removed Debug\n" +
                    "JECC - ActionPlugin - Removed delete plan button fom toolbar \n" +
                    "JECC - ActionPlugin - Removed log messages\n" +
                    "JECC - ActionPlugin - Replaced old table summery with new table based version \n" +
                    "JECC - ActionPlugin - Set new default values for the date filter\n" +
                    "JECC - ActionPlugin - The time filter 'all' allowes to select start and end time\n" +
                    "JECC - ActionPlugin - Warn the user more if the deletes a plan\n" +
                    "JECC - ActionPlugin - add EmptyObject\n" +
                    "JECC - ActionPlugin - add EnPI translations\n" +
                    "JECC - ActionPlugin - add Excel icon\n" +
                    "JECC - ActionPlugin - add a lot more layout to the export\n" +
                    "JECC - ActionPlugin - add additional null pointer checks\n" +
                    "JECC - ActionPlugin - add missing classes\n" +
                    "JECC - ActionPlugin - beta version of the Excel export\n" +
                    "JECC - ActionPlugin - changed date field order\n" +
                    "JECC - ActionPlugin - debug workaround stuff\n" +
                    "JECC - ActionPlugin - disabled PDF prototype\n" +
                    "JECC - ActionPlugin - disabled scrollbar for the summary\n" +
                    "JECC - ActionPlugin - revenged the update process\n" +
                    "JECC - ActionPlugin - some columns will now keep there size if fit to size is called\n" +
                    "JECC - ActionPlugin - the done date cann now only lie in the future\n" +
                    "JECC - ActionPlugin, NonconformitiesPlugin, LegalPlugin are now using JECommons GsonBuilder.java\n" +
                    "JECC - AlarmPlugin - improved analysis linking\n" +
                    "JECC - Catch more expressions while switching between plugins\n" +
                    "JECC - ChartPlugin - ChartDataRow - fixed limits not working on calculated values\n" +
                    "JECC - ChartPlugin - ChartDataRow - fixed null pointer\n" +
                    "JECC - ChartPlugin - LoadAnalysisDialog - open with full grid layout\n" +
                    "JECC - ChartPlugin - LoadAnalysisDialog - open with full grid layout\n" +
                    "JECC - ChartPlugin - SaveAnalysisDialog - fixed permission denied message\n" +
                    "JECC - ChartPlugin - SelectionDialog - fixed changing data row for existing entry not working as intendet\n" +
                    "JECC - ChartPlugin - Show Raw Values - fixed some problems with function, still some problem just click more to solve\n" +
                    "JECC - ChartPlugin - Table(V) - Add Visible Column Selector\n" +
                    "JECC - ChartPlugin - Table(V) - Better Name\n" +
                    "JECC - ChartPlugin - Table(V) - Decimal Place per Column\n" +
                    "JECC - ChartPlugin - Table(V) - Filter Prompt Text\n" +
                    "JECC - ChartPlugin - Table(V) - Last Column is cut off\n" +
                    "JECC - ChartPlugin - Table(V) - TS Column size fixes\n" +
                    "JECC - ChartPlugin - TableChart - improved code, new features for multi column consolidation\n" +
                    "JECC - ChartPlugin - TableChartV - added setting for column/row sums\n" +
                    "JECC - ChartPlugin - TableChartV - changed content style\n" +
                    "JECC - ChartPlugin - TableChartV - improved filter \n" +
                    "JECC - ChartPlugin - TableChartV - improved filter visibility\n" +
                    "JECC - ChartPlugin - added String Data as base Data class option for selection dialog\n" +
                    "JECC - ChartPlugin - fixed analysis overwrite dialog\n" +
                    "JECC - ChartPlugin - fixed data row title being ignored for table header\n" +
                    "JECC - ChartPlugin - fixed dialogs not closable\n" +
                    "JECC - ChartPlugin - fixed overwrite dialog\n" +
                    "JECC - ChartPlugin - fixed wrong aggregation on startup\n" +
                    "JECC - ChartPlugin - improved axis zoom for stacked charts\n" +
                    "JECC - ChartPlugin - improved display for big values\n" +
                    "JECC - ChartPlugin - improved math box\n" +
                    "JECC - ChartPlugin - improved min/max axis value finding\n" +
                    "JECC - DashboardPlugin - Now removes special character from file name\n" +
                    "JECC - DashboardPlugin - Time Frame Widget - can display count of samples\n" +
                    "JECC - DashboardPlugin - Time Frame Widget - fix for always set as unsaved\n" +
                    "JECC - DashboardPlugin - TimeFrameWidget - fixed javafx threading\n" +
                    "JECC - DashboardPlugin - TimeFrameWidget - fixed null pointer\n" +
                    "JECC - DashboardPlugin - add missing SampleHandlerEvent and SampleHandlerEventListener\n" +
                    "JECC - DashboardPlugin - added missing attributes to datamodel\n" +
                    "JECC - DashboardPlugin - fix save bug\n" +
                    "JECC - DashboardPlugin - fixed math not working properly\n" +
                    "JECC - DashboardPlugin - ignore unknown properties\n" +
                    "JECC - DashboardPlugin - timeframe - format fix\n" +
                    "JECC - DashboardPlugin - Time Frame Widget - update Section Tableview\n" +
                    "JECC - DashboardPlugin - TimeFrameWidget - Removed System out\n" +
                    "JECC - DashboardPlugin - TimeFrameWidget - add localisation for Timeframe Table\n" +
                    "JECC - DashboardPlugin - TimeFrameWidget - added th option to link it directly to DataSource\n" +
                    "JECC - DashboardPlugin - TimeFrameWidget - correct font will be used + translation fix\n" +
                    "JECC - DashboardPlugin - TimeFrameWidget - fix did not save TimeFrameWidget\n" +
                    "JECC - DashboardPlugin - TimeFrameWidget - implemented options to show different kind of start / end DateTimes\n" +
                    "JECC - DashboardPlugin - TimeFrameWidget - improvement getMaxTimeStamp of datasource\n" +
                    "JECC - DashboardPlugin - TimeFrameWidget - removed System outs\n" +
                    "JECC - DashboardPlugin - TimeFrameWidget fix\n" +
                    "JECC - DashboardPlugin - fire event on sample handler update\n" +
                    "JECC - FXLogin - Add additional checks\n" +
                    "JECC - FXLogin - Add 5 seconds to the login activity log to prevent duplicated activities timestamp in ws\n" +
                    "JECC - FXLogin - fixed error message\n" +
                    "JECC - LegalPlugin - Implement Sum Table\n" +
                    "JECC - LegalPlugin - Renamed Plugin / classes\n" +
                    "JECC - LegalPlugin - add new Icon\n" +
                    "JECC - LegalPlugin - added Class names / attributes to helper class\n" +
                    "JECC - LegalPlugin - added Translations\n" +
                    "JECC - LegalPlugin - added class files\n" +
                    "JECC - LegalPlugin - added translations\n" +
                    "JECC - LegalPlugin - adjust Width of Cell/ Combobox\n" +
                    "JECC - LegalPlugin - fix for sum table\n" +
                    "JECC - LegalPlugin - fix translations\n" +
                    "JECC - LegalPlugin - hyperlink cell fix, Table Col changed position\n" +
                    "JECC - LegalPlugin - init gui at focus\n" +
                    "JECC - LegalPlugin - merge fix for plugin manager\n" +
                    "JECC - LegalPlugin - refactored LegalCadastre -> Index of Legal Provisions\n" +
                    "JECC - LegalPlugin - removed Overview Tab, updated Filter\n" +
                    "JECC - LegalPlugin - removed old files\n" +
                    "JECC - LegalPlugin - renamed Legislation -> Obligation\n" +
                    "JECC - LegalPlugin - update Icons\n" +
                    "JECC - LegalPlugin - update JEVis class\n" +
                    "JECC - LegalPlugin - update JEVis class Names\n" +
                    "JECC - LegalPlugin - update Summary will be updated if ObligationData is saved\n" +
                    "JECC - LegalPlugin - update Translation\n" +
                    "JECC - LegalPlugin - update Translations\n" +
                    "JECC - LegalPlugin - update translation files\n" +
                    "JECC - NonconformitiesPlugin - Complete Date Can not be in the future\n" +
                    "JECC - NonconformitiesPlugin - Removed duplicated translations\n" +
                    "JECC - NonconformitiesPlugin - UI Update, translation fixes\n" +
                    "JECC - NonconformitiesPlugin - add added Sum Table\n" +
                    "JECC - NonconformitiesPlugin - add translations\n" +
                    "JECC - NonconformitiesPlugin - added Filter For ALL, update Sum if Table Item is added / Removed\n" +
                    "JECC - NonconformitiesPlugin - can be placed under building, fixed translations\n" +
                    "JECC - NonconformitiesPlugin - changed Cell Classes to be generic\n" +
                    "JECC - NonconformitiesPlugin - removed old files\n" +
                    "JECC - NonconformitiesPlugin - update Summary will be updated if ObligationData is saved\n" +
                    "JECC - NonconformitiesPlugin - updated Table Cell Orientation\n" +
                    "JECC - NonconformitiesPlugin, legal - wrap text for text areas, translation fixes\n" +
                    "JECC - NotesPlugin - disable add button in case no note is selected\n" +
                    "JECC - NotesPlugin - fix Window close, target pre-selected\n" +
                    "JECC - NotesPlugin - fixed double entries not showing\n" +
                    "JECC - NotesPlugin - fixed sorting \n" +
                    "JECC - ObjectPlugin - EnterDataDialog - added missing translations\n" +
                    "JECC - ObjectPlugin - EnterDataDialog - fixed show more dialog not closing\n" +
                    "JECC - ObjectPlugin - EnterDataDialog - improved size\n" +
                    "JECC - ObjectPlugin - RevolutionPi Assistant implemented\n" +
                    "JECC - ObjectPlugin - added Math/Forecast/Base -Data to eligable new link objects\n" +
                    "JECC - ObjectPlugin - added function to reset dependencies manually\n" +
                    "JECC - ObjectPlugin - fixed copy dialog not working with changed object name\n" +
                    "JECC - ObjectPlugin - reset calculations, fixed deleting user data\n" +
                    "JECC - Removed font size from tableview\n" +
                    "JECC - Rename fix\n" +
                    "JECC - ReportWizardDialog - fixed period configuration not showing up\n" +
                    "JECC - RevPiAssistant - fix for no target selected\n" +
                    "JECC - SQl Driver - add debug mode settings\n" +
                    "JECC - SaveDialogs - added permission check\n" +
                    "JECC - TRCPlugin - added custom period support\n" +
                    "JECC - TRCPlugin - fixed interval selector not working properly\n" +
                    "JECC - TRCPlugin - fixed missing delete button\n" +
                    "JECC - Templates - BuildingObject - fixed wrong naming\n" +
                    "JECC - Templates - added building template\n" +
                    "JECC - The PasswordEdior now adds a password instead of replacing it \n" +
                    "JECC - WebService - Fixed a bug where deleted object are not filtered out\n" +
                    "JECC - WebService - revert hotfix because it make other problems\n" +
                    "JECC - added Attribute Parameter Config Class HTTP Channel\n" +
                    "JECC - added GUI for Parameter Confguration of HTTP Channel\n" +
                    "JECC - added LegalCadastre Plugin\n" +
                    "JECC - delete comments\n" +
                    "JECC - fix saved Parameter as Json File instead of JSON String\n" +
                    "JECC - fixed math data not clearing up\n" +
                    "JECC - fixed wrong resource name for version check\n" +
                    "JECC - nonconformities - update JEVis class Names\n" +
                    "JECC - update translation\n" +
                    "\n" +
                    "Classes - Accounting - Governmental Dues - added new Attributes\n" +
                    "Classes - Add SQl Driver classes\n" +
                    "Classes - Add SQl Driver translations\n" +
                    "Classes - AlarmConfiguration.json - changed description and attribute names\n" +
                    "Classes - DataNotes.json - inherit String Data\n" +
                    "Classes - Filled empty fields\n" +
                    "Classes - Fixed typo\n" +
                    "Classes - Forecast Data.json - added switch for keeping values \n" +
                    "Classes - JsonParser - update JEVis Class\n" +
                    "Classes - Removed backup files\n" +
                    "Classes - Removed inhered types\n" +
                    "Classes - Update CalculationMethods.java \n" +
                    "Classes - Update ChartDataRow.java\n" +
                    "Classes - Update ChartPlugin.java\n" +
                    "Classes - Update ForecastDataObject.java\n" +
                    "Classes - Update Launcher.java\n" +
                    "Classes - Update PrepareForecast.java\n" +
                    "Classes - User Group Directory - allow user group directory under itself\n" +
                    "Classes - User Roles - fixed translation\n" +
                    "Classes - added attribute to AlarmConfiguration for limits\n" +
                    "Classes - added measurement instrument installation location\n" +
                    "Classes - added measurement instrument verification documentation\n" +
                    "Classes - added missing directory inheritance\n" +
                    "Classes - added missing translation files for action plan directory v2 and deviations directory\n" +
                    "Classes - removed wrong char in name\n" +
                    "\n" +
                    "DataCollector - Add SQl Driver\n" +
                    "DataCollector - EmailDriver - Replaced com.sun with org.eclipse implementation\n" +
                    "DataCollector - EmailDriver - Revert replaced com.sun with org.eclipse implementation\n" +
                    "DataCollector - EmailDriver - Support for SMTP server without authentication \n" +
                    "DataCollector - HTTP-Driver - Date Time Format is in selected Timezone\n" +
                    "DataCollector - HTTP-Driver - added Chunk-size(s) which will set end-date = start-date + chuck-size, will iterate until end-date = current date\n" +
                    "DataCollector - HTTP-Driver - removed offset from import Result\n" +
                    "DataCollector - HTTPDataSource - implemented replacement of HTTP Parameters defined in JSON File\n" +
                    "DataCollector - JSON Parser - added JSON Parser Module\n" +
                    "DataCollector - JsonParser - add Unit Tests\n" +
                    "DataCollector - JsonParser - added improved test, include mockito for data mocking\n" +
                    "DataCollector - JsonParser - implement value Parsing to designated data Type\n" +
                    "DataCollector - JsonParser - improved Test Cases\n" +
                    "DataCollector - JsonParser - refactor code\n" +
                    "DataCollector - MSCONS - Stop Parsing if file is not MSCONS File\n" +
                    "DataCollector - MSCONS - Test Cases added\n" +
                    "DataCollector - MSCONS - add JEVis Classes for MSCONS Parser\n" +
                    "DataCollector - MSCONS - add MSONS Parser Objects to DataCollectorTypes.java\n" +
                    "DataCollector - MSCONS - add missing file for Unit Test\n" +
                    "DataCollector - MSCONS - add pom.xml for MSCONS Parser\n" +
                    "DataCollector - MSCONS - changed attribute from Receiver to Recipient\n" +
                    "DataCollector - MSCONS - changed names of variables\n" +
                    "DataCollector - MSCONS - implement MSCONS Parser\n" +
                    "DataCollector - MSCONS - stop parsing if file is not MSCONS\n" +
                    "DataCollector - MSCONS - update JEVis Class\n" +
                    "DataCollector - RevolutionPi - update api request\n" +
                    "DataCollector - RevolutionPiServer - implemented conversion to double if target object is a data object\n" +
                    "DataCollector - RevolutionPiServer - only adds Staus Codes to Status Log if Status Code != 0\n" +
                    "DataCollector - SQL-Driver\n" +
                    "DataCollector - SQL-Driver - Changed debug Main class init\n" +
                    "DataCollector - SQL-Driver - Fixed dependency\n" +
                    "DataCollector - SQL-Driver - Fixed pom settings\n" +
                    "DataCollector - SQL-Driver - Fixed typo\n" +
                    "DataCollector - SQL-Driver - FixedlLog message \n" +
                    "DataCollector - SQL-Driver - Implement time zone support via attribute\n" +
                    "DataCollector - SQL-Driver - Trim spaces in setting to prevent user error\n" +
                    "DataCollector - SQL-Driver - User errror checks\n" +
                    "DataCollector - SQL-Driver - fields\n" +
                    "DataCollector - SQL-Driver - fix\n" +
                    "DataCollector - add MSCONS Module to pom\n" +
                    "\n" +
                    "JEALarm - AlarmProcess - fixed missing alarms in clean data clean data children\n" +
                    "\n" +
                    "JEAPI - Using the setName funktion will now also set the default localized name\n" +
                    "\n" +
                    "JEAPI-WS - Add http proxy support \n" +
                    "JEAPI-WS - Fixed a bug where some http connection would not be released again.\n" +
                    "JEAPI-WS - Fixed null pointer\n" +
                    "JEAPI-WS - JEVisClassWS - fixed null pointer for some classes \n" +
                    "JEAPI-WS - Reduce server request form object which the user has no access to. Reload will force a new check\n" +
                    "\n" +
                    "JEAlarm - added gap to alarming\n" +
                    "JEAlarm - fixed wrong alarm configuration used in some cases\n" +
                    "JEAlarm - improved system alarming\n" +
                    "JEAlarm - new functionality for limits in notification system\n" +
                    "\n" +
                    "JECalc - Launcher - show current jobs\n" +
                    "\n" +
                    "JECommons - GsonBuilder moved to JECommons\n" +
                    "JECommons - Parameter - add TimeZone to Http Parameter Configuration\n" +
                    "JECommons - UnitFormula - fixed regex not recognizing decimals\n" +
                    "JECommons - VarFiller added CurrentTs for VarFiller Variables\n" +
                    "JECommons - added Parameter and ParameterHelper for HttP Channel Parameters\n" +
                    "JECommons - added new Attribute of HTTP Channel into JC\n" +
                    "\n" +
                    "JEDataCollector - Launcher - show current jobs\n" +
                    "JEDataCollector - Rev PI - added Status log\n" +
                    "JEDataCollector - Rev PI - removed unused variables\n" +
                    "JEDataCollector - Revert packaging\n" +
                    "JEDataCollector - add common VarFiller\n" +
                    "JEDataCollector - add deleted option to ws request\n" +
                    "JEDataCollector - added Datacollector for Rev PI\n" +
                    "JEDataCollector - moved sql driver dependency into jedatacollector\n" +
                    "\n" +
                    "JEDataProcessor - Forecast - fixed problems with timezoneshifts\n" +
                    "JEDataProcessor - Forecast - fixed some problems\n" +
                    "JEDataProcessor - Gap Filling - fixed async to aligned data\n" +
                    "JEDataProcessor - Launcher - show current jobs\n" +
                    "JEDataProcessor - MathData - fixed wrong steps for incomplete cycle\n" +
                    "JEDataProcessor - WorkDays - fixed day aggregation not working as intended\n" +
                    "JEDataProcessor - fixed PeriodHelper for uncommon periods\n" +
                    "JEDataProcessor - fixed some forecast issues\n" +
                    "JEDataProcessor - gap filling using local time\n" +
                    "JEDataProcessor - gap identification supports partial gaps\n" +
                    "JEDataProcessor - improved cleaning type determination\n" +
                    "\n" +
                    "JEReport - PeriodicIntervalCalc - fixed some interval problems for new overwrite period function\n" +
                    "JEReport - ReportExecutor - fixed disabled pdf option not working\n" +
                    "JEReport - ReportLinkProperty - added override schedule function\n" +
                    "JEReport - added new relative interval configuration options\n" +
                    "JEReport - fixed date alignment for period overwrite none\n" +
                    "JEReport - fixed relative period with aggregation not working properly\n" +
                    "\n" +
                    "JEWebservice - Updateing a password will now update the user cache\n" +
                    "JEWebservice - enabled delete objects. bug is stell there\n" +
                    "JEWebservice - fixed inheritance bug on filtered class call\n" +
                    "\n" +
                    "Notifier - Notifier-EP - revert mail lib\n\n" +
                    "------Version 3.9.68------\n" +
                    "JECC - AccountingPlugin - add check if the accounting directory is missing\n" +
                    "JECC - AccountingPlugin - fixed giant numbers bug\n" +
                    "JECC - AccountingPlugin - fixed order\n" +
                    "JECC - AccountingPlugin - fixed overflow\n" +
                    "JECC - AccountingPlugin - fixed sorting algorithm missing no formula input formulas for multi input formula input sorting of output\n" +
                    "JECC - ActionPlugin - \"-0\" will now be formatted as 0\n" +
                    "JECC - ActionPlugin - Action Plans can now be created under buildings\n" +
                    "JECC - ActionPlugin - Add ActionPlugin translations\n" +
                    "JECC - ActionPlugin - Add CurrencyColumnCell.java Column\n" +
                    "JECC - ActionPlugin - Add Date Filter\n" +
                    "JECC - ActionPlugin - Add Default status\n" +
                    "JECC - ActionPlugin - Add Detail tab\n" +
                    "JECC - ActionPlugin - Add Plugin class\n" +
                    "JECC - ActionPlugin - Add Plugin class and icon\n" +
                    "JECC - ActionPlugin - Add Plugin to the PluginManager\n" +
                    "JECC - ActionPlugin - Add SEU sorting\n" +
                    "JECC - ActionPlugin - Add action plan\n" +
                    "JECC - ActionPlugin - Add but not enabled transparent scrollbar\n" +
                    "JECC - ActionPlugin - Add capital value tab\n" +
                    "JECC - ActionPlugin - Add columne to display double money\n" +
                    "JECC - ActionPlugin - Add custom SEU and Nr Prefix\n" +
                    "JECC - ActionPlugin - Add delete/archive funktion\n" +
                    "JECC - ActionPlugin - Add double formatter\n" +
                    "JECC - ActionPlugin - Add editable function\n" +
                    "JECC - ActionPlugin - Add enable/disabled function for enpi\n" +
                    "JECC - ActionPlugin - Add energy fields \n" +
                    "JECC - ActionPlugin - Add enpi and consumption \n" +
                    "JECC - ActionPlugin - Add filter bar \n" +
                    "JECC - ActionPlugin - Add json based update \n" +
                    "JECC - ActionPlugin - Add load json function\n" +
                    "JECC - ActionPlugin - Add localized done status\n" +
                    "JECC - ActionPlugin - Add missing bindings for check lists\n" +
                    "JECC - ActionPlugin - Add new plugin icons\n" +
                    "JECC - ActionPlugin - Add number prefix\n" +
                    "JECC - ActionPlugin - Add plugin tooltip and help\n" +
                    "JECC - ActionPlugin - Add sum table panel\n" +
                    "JECC - ActionPlugin - Add time filter selector\n" +
                    "JECC - ActionPlugin - Add unit to consumption\n" +
                    "JECC - ActionPlugin - Add value field + unit UI element\n" +
                    "JECC - ActionPlugin - Author is now first + second name per default\n" +
                    "JECC - ActionPlugin - Changed consumption and add free selection\n" +
                    "JECC - ActionPlugin - Changed order of plugins\n" +
                    "JECC - ActionPlugin - Changed plugin order\n" +
                    "JECC - ActionPlugin - Changed the postion of the plugin\n" +
                    "JECC - ActionPlugin - Delete Action will now be disabled if permission is missing.\n" +
                    "JECC - ActionPlugin - Disabled fractions for now.\n" +
                    "JECC - ActionPlugin - Disabled not implemented actions\n" +
                    "JECC - ActionPlugin - Done status sets now done date\n" +
                    "JECC - ActionPlugin - Enable automatic text wrap\n" +
                    "JECC - ActionPlugin - Fixed SumTablePane to show all media\n" +
                    "JECC - ActionPlugin - Fixed TagButton events\n" +
                    "JECC - ActionPlugin - Fixed a bug where investment and saving was missing\n" +
                    "JECC - ActionPlugin - Fixed a bug where the \"Action Plan Directory v2\" is used instead of the correct \"Action Directory\"\n" +
                    "JECC - ActionPlugin - Fixed a bug where the period was not set\n" +
                    "JECC - ActionPlugin - Fixed a bug where the plugin would load without\n" +
                    "JECC - ActionPlugin - Fixed a bug where the table could not be sorted \n" +
                    "JECC - ActionPlugin - Fixed a bug with the all selected TagButton function\n" +
                    "JECC - ActionPlugin - Fixed action data loading\n" +
                    "JECC - ActionPlugin - Fixed action form bug\n" +
                    "JECC - ActionPlugin - Fixed bug there the icon did not change of everything is selected\n" +
                    "JECC - ActionPlugin - Fixed filtering\n" +
                    "JECC - ActionPlugin - Fixed json initialization related bugs\n" +
                    "JECC - ActionPlugin - Fixed null pointer\n" +
                    "JECC - ActionPlugin - Fixed toolbar icon size\n" +
                    "JECC - ActionPlugin - Fixed typo\n" +
                    "JECC - ActionPlugin - Format money fields\n" +
                    "JECC - ActionPlugin - Improved the placeholder loading fpr the vpv data\n" +
                    "JECC - ActionPlugin - Medium is now single select\n" +
                    "JECC - ActionPlugin - Migration to json data structure\n" +
                    "JECC - ActionPlugin - Moved Tabs to separate classes\n" +
                    "JECC - ActionPlugin - Moved enpi and consumption to separate class\n" +
                    "JECC - ActionPlugin - Moved some field between general and details tab\n" +
                    "JECC - ActionPlugin - Moved the GUI controls in package\n" +
                    "JECC - ActionPlugin - New Plan will now have default settings\n" +
                    "JECC - ActionPlugin - Pojo Example\n" +
                    "JECC - ActionPlugin - Removed EnPI from Sum Panel\n" +
                    "JECC - ActionPlugin - Removed debug\n" +
                    "JECC - ActionPlugin - Removed debug msg\n" +
                    "JECC - ActionPlugin - Removed investment sum\n" +
                    "JECC - ActionPlugin - Removed unused column\n" +
                    "JECC - ActionPlugin - Renamed ActionPlan data class \n" +
                    "JECC - ActionPlugin - Reorganized UI\n" +
                    "JECC - ActionPlugin - Replaced old fakeName binding\n" +
                    "JECC - ActionPlugin - Search will now compare filter in lowercase\n" +
                    "JECC - ActionPlugin - Table to display NPV Data\n" +
                    "JECC - ActionPlugin - The Action Plan is now called Location in the overview table\n" +
                    "JECC - ActionPlugin - The freeObject is now static\n" +
                    "JECC - ActionPlugin - add DoubleConverter factory\n" +
                    "JECC - ActionPlugin - add SEU filter\n" +
                    "JECC - ActionPlugin - add new default table and list css\n" +
                    "JECC - ActionPlugin - add translations\n" +
                    "JECC - ActionPlugin - changed format to display comma with space as separator\n" +
                    "JECC - ActionPlugin - fixed chartdatarow setter\n" +
                    "JECC - ActionPlugin - implemented Sum of Tab / completed / not completed\n" +
                    "JECC - ActionPlugin - migrate the action data model to json.\n" +
                    "JECC - ActionPlugin - removed error print\n" +
                    "JECC - ActionPlugin - what's wrong with git why are this ignored\n" +
                    "JECC - Add the gson factory for the common JEVis Json use case\n" +
                    "JECC - AlarmPlugin - Add cell for doubles\n" +
                    "JECC - AlarmPlugin - Add color to the scrollpane backgound.\n" +
                    "JECC - AlarmPlugin - Removed not needed table settings\n" +
                    "JECC - BaseDataPlugin - fixed last value not working as intended\n" +
                    "JECC - BaseDataPlugin - improved dialog texts\n" +
                    "JECC - ChartDataRow - null pointer hotfix\n" +
                    "JECC - ChartPlugin - ChartDataRow - fixed timefactor for hourly energy units\n" +
                    "JECC - ChartPlugin - added benchmark for loading analyses\n" +
                    "JECC - ChartPlugin - fixed MathData unsupported\n" +
                    "JECC - ChartPlugin - fixed ToolBarFunctions like base load calculation not working properly\n" +
                    "JECC - ChartPlugin - fixed calculation setting not working as intended\n" +
                    "JECC - ChartPlugin - fixed chart export to csv/xlsx\n" +
                    "JECC - ChartPlugin - fixed disable icons button not working\n" +
                    "JECC - ChartPlugin - fixed old data rows showing up\n" +
                    "JECC - ChartPlugin - hotfix\n" +
                    "JECC - ChartPlugin - should load temporary analysis correctly\n" +
                    "JECC - ConfigurationPlugin - improved ReportWizardDialog\n" +
                    "JECC - Dashboard - Sankey - can show % and Value at the same time, fix - Top Items part of the name not visible\n" +
                    "JECC - Dashboard - Sankey - fix reload bug\n" +
                    "JECC - Dashboard - Sankey - fix textColor, fix for update Data Row\n" +
                    "JECC - Dashboard - Sankey - implemented error tolerance\n" +
                    "JECC - Dashboard - Sankey - removed option to toggle show flow direct\n" +
                    "JECC - Dashboard - Sankey add translation, Error Message on no Data Available on Root Items\n" +
                    "JECC - Dashboard - Sankey add translations, vgrow on tableview\n" +
                    "JECC - Dashboard - SankeyWidget TextSize adjustable & level offset\n" +
                    "JECC - NewObjectDialog - added options to templates\n" +
                    "JECC - NonconformitiesPlugin - Checklist Data is now separate Class, medium\n" +
                    "JECC - NonconformitiesPlugin - add Medium + Create Date To Form\n" +
                    "JECC - NonconformitiesPlugin - added Overview Tab\n" +
                    "JECC - NonconformitiesPlugin - added SEU field\n" +
                    "JECC - NonconformitiesPlugin - added SEU filter\n" +
                    "JECC - NonconformitiesPlugin - added SEU, Field to Plan config, added translations\n" +
                    "JECC - NonconformitiesPlugin - added additional error condition\n" +
                    "JECC - NonconformitiesPlugin - added config window for Nonconfomities\n" +
                    "JECC - NonconformitiesPlugin - added filter status, filed added fileds action, SEU, fields\n" +
                    "JECC - NonconformitiesPlugin - added german translation, put tabs in package\n" +
                    "JECC - NonconformitiesPlugin - added missing files\n" +
                    "JECC - NonconformitiesPlugin - added missing files & fix some translations\n" +
                    "JECC - NonconformitiesPlugin - added new Translations\n" +
                    "JECC - NonconformitiesPlugin - config plan update ui layout, added Plan column to overview\n" +
                    "JECC - NonconformitiesPlugin - fix filter bug showing always the nr column\n" +
                    "JECC - NonconformitiesPlugin - fix for filed tags, ui layout table\n" +
                    "JECC - NonconformitiesPlugin - fix translations\n" +
                    "JECC - NonconformitiesPlugin - hotfix seu filter\n" +
                    "JECC - NonconformitiesPlugin - if filter select all return \"*\"\n" +
                    "JECC - NonconformitiesPlugin - implement 3. Layer List to be sortable\n" +
                    "JECC - NonconformitiesPlugin - implement reload\n" +
                    "JECC - NonconformitiesPlugin - moved plugin to pos 4\n" +
                    "JECC - NonconformitiesPlugin - prevent dialog close if requirements are not\n" +
                    "JECC - NonconformitiesPlugin - removed System outs, fix translations\n" +
                    "JECC - NonconformitiesPlugin - renamed JEVis Object Nonconformties to NonconformityPlan\n" +
                    "JECC - NonconformitiesPlugin - replaced Combobox with JFX Combobox\n" +
                    "JECC - NonconformitiesPlugin - replaced Pop Up Window with Notification Pane\n" +
                    "JECC - NonconformitiesPlugin - show only not deleted objects\n" +
                    "JECC - NonconformitiesPluginPlugin - implemented\n" +
                    "JECC - ObjectPlugin - Add filter icons\n" +
                    "JECC - ObjectPlugin - EnterDataDialog - improved labelling to reduce user difficulties\n" +
                    "JECC - ObjectPlugin - Fixed scaling for the svg icons\n" +
                    "JECC - ObjectPlugin - Loytec Assistant - Readout Period from Loytec Controller\n" +
                    "JECC - ObjectPlugin - fixed cleaning of child objects not working as intended\n" +
                    "JECC - ObjectPlugin - fixed move timestamps dialog\n" +
                    "JECC - ObjectPlugin - fixed replace in object name not working for new localization\n" +
                    "JECC - ObjectPlugin - fixed tree selection always opening everything on no selection\n" +
                    "JECC - ObjectPlugin - in copy dialog set the local name for new object for active language\n" +
                    "JECC - ObjectPlugin -ReportWizardDialog - fixed copied links not keeping link settings\n" +
                    "JECC - ObjectPlugin -ReportWizardDialog - fixed problem with copied links missing settings\n" +
                    "JECC - ObjectPlugin -ReportWizardDialog - fixed update status on new links\n" +
                    "JECC - TRCPlugin - TemplateHandler - added missing id check\n" +
                    "JECC - TRCPlugin - fixed calculation logic\n" +
                    "JECC - TablePlugin - enabled export to xlsx\n" +
                    "JECC - translation fix\n" +
                    "\n" +
                    "Classes - BaseData - added value is a quantity and differential attribute\n" +
                    "DataCollector - JEVisDataServer - fixed last readout unused\n" +
                    "\n" +
                    "JECommons - CalcJobFactory - more logging\n" +
                    "JECommons - DataProcessing - JsonSampleGenerator - fixed custom workday issues\n" +
                    "\n" +
                    "JEDataProcessor - Allow periods smaller then minutes for aggregation\n" +
                    "JEDataProcessor - DifferentialStep - fixed problem with weekly diff values\n" +
                    "JEDataProcessor - fixed period interval \n" +
                    "JEDataProcessor - fixed some problems with weekly aggregation\n" +
                    "\n" +
                    "JEReport - PeriodicIntervalCalc - fixed custom workday issues\n" +
                    "JEReport - Report - increased exception handling\n" +
                    "JEReport - ReportLauncher - removed ReportPolicy, using AbstractCLI functions\n" +
                    "JEReport - ReportLinkProperty - fixed no data message not showing correct parental data object name\n" +
                    "\n" +
                    "JEWebservice - Add Overview\n" +
                    "JEWebservice - Add basic JFX like CheckComboBox\n" +
                    "JEWebservice - Check for relativ paths\n" +
                    "JEWebservice - Disabled debug messages\n" +
                    "JEWebservice - Fixed a bug create date field set the done date\n" +
                    "JEWebservice - Fixed a bug where a single broken file stop the ws from sending\n" +
                    "JEWebservice - Fixed a bug where the WS translation would not load all files at once.\n" +
                    "JEWebservice - Fixed an overview loading bug\n" +
                    "JEWebservice - Fixed the reload of an action\n" +
                    "JEWebservice - Toolbar will now be limited for the overview tab\n" +
                    "JEWebservice - add new plan translation\n" +
                    "JEWebservice - moved toolbar to gui package\n\n" +
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
                    "JEWebservice - ResourceSample - fixed user exception for files\n\n";


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
