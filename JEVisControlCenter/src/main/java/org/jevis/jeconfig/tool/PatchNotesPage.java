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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
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

import java.util.prefs.Preferences;

/**
 * The PatchNotesPage is an dialog to show recent release changes for the user.#
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class PatchNotesPage {
    private static final Logger logger = LogManager.getLogger(PatchNotesPage.class);
    private final String versionHistory =
            "------Version 3.9.22------\n" +
                    "\n" +
                    "\n" +
                    "JECC - Charts - fixed some threading issues with loading\n\n" +
                    "------Version 3.9.21------\n" +
                    "\n" +
                    "\n" +
                    "JECC - Charts - LoadAnalysisDialog - individual time frames are not working\n" +
                    "JECC - Charts - fixed asynchronous period visualization\n" +
                    "JECC - Charts - sum function fix\n" +
                    "JECC - Charts - allow empty data rows for selection\n" +
                    "JECC - Charts - Logical chart fixed errors with new data model\n" +
                    "JECC - BarChartSerie, ColumnChartSerie, TableSerie, XYChartSerie - fixed status bar messaging\n" +
                    "JECC - HeatMapChart - added logging for better debugging\n" +
                    "JECC - Charts - improved data model logic \n" +
                    "JECC - Charts - new model logic for xy charts\n" +
                    "\n" +
                    "JECC - switched to more compatible pdf viewer\n" +
                    "JECC - AlarmPlugin - fixed sort logic\n" +
                    "\n" +
                    "JEDataCollector - FTPDataSource - fixed null pointer\n" +
                    "JEDataCollector - fixed some null pointer in JEVisCSVParser\n" +
                    "JEDataCollector - prepared for hdd parser\n" +
                    "JEDataCollector - JEVisCSVParser - fixed some null pointers\n" +
                    "JEDataCollector - JEVisCSVParser - fixed some strange row/column logic\n" +
                    "\n" +
                    "JECalc - added asynchronous input data type\n" +
                    "JECalc - fixed null pointer\n" +
                    "\n" +
                    "JECommons - DatabaseHelper - fixed null pointer\n" +
                    "JECommons - changed timezone attribute logic for data importer\n" +
                    "JECommons - DatabaseHelper - fixed some null pointers\n" +
                    "\n" +
                    "Launcher - fixed second preload on run\n" +
                    "\n" +
                    "Classes - prepared for hdd parser\n\n" +
                    "------Version 3.9.20------\n" +
                    "JECC - smaller bugfixes\n" +
                    "JECC - Implemented an taskmanager and support to an central Executor\n" +
                    "JECC - Improved startup time\n" +
                    "JECC - Add missing class description\n" +
                    "JECC - added menu item to top menu for replace\n" +
                    "JECC - added function for recursive search&replace for object names\n" +
                    "JECC - Reset calculations doesn't enable calculations after completion\n" +
                    "JECC - AlarmPlugin - autofit fix\n" +
                    "JECC - AlarmPlugin - added permission warning for confirming alarms\n" +
                    "JECC - Charts - fixed a null pointer for chart title in export\n" +
                    "JECC - Charts - fixed change check when closing note dialog soll\n" +
                    "JECC - Charts - Heatmap - added function for color map (settings dialog, advanced settings, select chart)\n" +
                    "JECC - Charts - new standard time frame 'current'\n" +
                    "JECC - Charts - added custom time frames to the quickbar\n" +
                    "JECC - Charts - right mouse click - show current value in new column\n" +
                    "JECC - Charts - restored scroll bars to table charts\n" +
                    "JECC - Charts - added sort option to xy-chart-based charts\n" +
                    "JECC - Charts - show alarms in charts like limits (sa, da, more info in note dialog)\n" +
                    "JECC - Dashboard - Snap to grid not working\n" +
                    "JECC - Dashboard - Standard timeframe not working\n" +
                    "JECC - Dashboard - Widget navigator show always ID 1\n" +
                    "JECC - Dashboard - Fixed an bug where the update process was running in the GUI thread\n" +
                    "JECC - Dashboard - Add new \"Value Editor\" Widget\n" +
                    "JECC - Dashboard - add option for unit in selection\n" +
                    "JECC - Dashboard - selection combobox has wrong tooltip\n" +
                    "JECC - MeterPlugin - added delete function with permission check\n" +
                    "JECC - MeterPlugin - optimized new dialog, added translation\n" +
                    "JECC - MeterPlugin - added replace dialog\n\n" +
                    "JEAlarm - fixed null pointer in some cases\n" +
                    "JEAlarm - fixed missing scope readout of alarm configurations\n\n" +
                    "JEDataProcessor - Forecast not working with months fixes\n" +
                    "JEDataProcessor - added missing brake in switch statement\n\n" +
                    "------Version 3.9.19------\n" +
                    "JECC - AlarmPlugin - Added button to confirm all alarms\n" +
                    "JECC - AlarmPlugin - fixed some threading issues\n" +
                    "JECC - AlarmPlugin - relocated reload button\n" +
                    "JECC - Dashboard - fixed translation of dashboard timefactory\n" +
                    "JECC - Statusbar - finalized message support\n" +
                    "JECC - Statusbar - added message translations\n" +
                    "\n" +
                    "JECommons - fixed some issues with SampleHandler\n" +
                    "\n" +
                    "JEReport - added logging output for report name + id for missing data\n\n" +
                    "------Version 3.9.18------\n" +
                    "JECC - Dashboard - \"Math.\" button did not work\n" +
                    "JECC - Dashboard - Font size in value widget has no effect \n" +
                    "JECC - AlarmPlugin - sometimes link is not disabled\n\n" +
                    "------Version 3.9.17------\n" +
                    "JECC - Charts - y-axis should autoscale when zooming\n" +
                    "JECC - AlarmPlugin - link should be shown with change in mouse cursor\n" +
                    "JECC - AlarmPlugin - change checkbox for what to show to more distinct control\n" +
                    "JECC - AlarmPlugin - changing date range or what to show while loading doesnt clear all alarm rows\n" +
                    "JECC - AlarmPlugin - make link to data rows configurable\n" +
                    "JECC - ReportWizard - creating new fixed to report period end creates wrong manipulation attribute value\n" +
                    "JECC - Dashboard - zoom to size does not allays work\n" +
                    "JECC - New Version Link not working under Windows\n" +
                    "\n" +
                    "JEReport - ManipulationMode - fixed wrong parsing of sorted data\n\n" +
                    "------Version 3.9.16------\n" +
                    "JECC - fixed translation in Report Wizard\n" +
                    "JECC - CleanDataExtension - fixed wrong value type for value offset\n" +
                    "JECC - CleanDataExtension - added localization for double values\n" +
                    "JECC - Unit Selection Tree - translation\n" +
                    "JECC - Units Selection - m², m³ or min are recognized as milli prefix\n" +
                    "JECC - Charts - CSV-Export should be able to export \"on-the-fly\" data like sum\n" +
                    "JECC - Charts - mouse listener only on plotarea -> impossible to select last datapoint\n" +
                    "JECC - Charts - Saving new Analyses doesn't work\n" +
                    "JECC - Dashboard - Additional layout changes\n" +
                    "JECC - Improved sample editor layout\n" +
                    "JECC - Dashboard - Removed non existing setBackground function call\n" +
                    "JECC - Dashboard - Data Alert in Legend\n" +
                    "JECC - Dashboard - Chart Background Colors\n" +
                    "JECC - Add start time debug function\n" +
                    "\n" +
                    "JECalc - support user corrected values from user data objects for calculation input\n" +
                    "\n" +
                    "JEReport - add support for value intervals from endrecord time backwards\n" +
                    "JEReport - option for continous periods with fixed start\n" +
                    "\n" +
                    "Classes - ReportPeriodConfiguration - fixed translation errors\n\n" +
                    "------Version 3.9.15------\n" +
                    "JECC - Charts - y-axis formatting with locale format\n" +
                    "JECC - Charts - openObject destroys regular analyses\n" +
                    "JECC - Charts - Heatmap - improved mouse precision\n" +
                    "JECC - Charts - Heatmap - Timeframes greater then months are not working right\n" +
                    "JECC - Charts - HeatMap - axes dont scale correctly\n" +
                    "JECC - Charts - disable mouse wheel zooom\n" +
                    "JECC - Dashboard - Behaviour concerning timeframes on changing dashboard\n" +
                    "JECC - Dashboard - missing parts when zooming\n" +
                    "JECC - Dashboard - Fixed the Dashboard zomm level list view.\n" +
                    "JECC - AlarmPlugin - fixed null pointer for no alarms in loaded timeframe\n" +
                    "JECC - ReportPlugin - remove site from single site users\n" +
                    "\n" +
                    "JEDataProcessor - multiplier datetime check only greater not equals\n" +
                    "\n" +
                    "JEReport - fixed some problem with custom workdays\n\n" +
                    "------Version 3.9.14------\n" +
                    "JECC - Charts - supporting opacity in hex color code\n" +
                    "JECC - Charts - forecast doesn't use different color \n" +
                    "JECC - Charts - disabled x-axis zoom slider\n" +
                    "JECC - Charts - improved note marker\n" +
                    "JECC - HeatMap - improved x-axis labeling\n" +
                    "JECC - Logic Chart - opimized scaling\n" +
                    "JECC - ChartDataModel - fixed null pointer in equals method\n\n" +
                    "------Version 3.9.13------\n" +
                    "JECC - Improved startup time by  up to 6 seconds or more\n" +
                    "JECC - New start parameter \"--datasource.ssltrust=always\" to enable self signed certificates\n" +
                    "JECC - Layout changes for the renaming/translation dialog\n" +
                    "JECC - Welcome Page is now disabled by default\n" +
                    "JECC - Minor toolbar layout changes\n" +
                    "JECC - GapFillingEditor - added delete option\n" +
                    "JECC - Charts - LogicalChart - y-axis is not working as intended \n" +
                    "JECC - Charts - sorted charts don't show correct time axis values\n" +
                    "JECC - Charts - HeatMap - tooltip sometimes doesn't clear up \n" +
                    "JECC - Charts - y-axes force zero in range\n" +
                    "JECC - Charts - DateAxis - time formatter formats in english when using german localization fixes #1559\n" +
                    "\n" +
                    "JEReport - custom workdays lead to not ready for aggregated data rows if end date is before start date\n" +
                    "\n" +
                    "JEDataProcessor - added support for deleting limit exceeding values\n" +
                    "\n" +
                    "JECC - Update Alarm Directory to not unique to allow alarm substructure\n\n" +
                    "------Version 3.9.12------\n" +
                    "JECC - Release Notes\n" +
                    "JECC - Charts - Regression dialog needs to be revised\n" +
                    "JECC - Charts - is it possible to deactivate the regression button if only plugins are displayed that do not support them?\n" +
                    "JECC - Charts - Regression dialog -> Selection of the type remove fixes\n" +
                    "JECC - JENotifier Service indicates that it is deactivated even though it is on\n" +
                    "JECC - Charts - \"Day of the week function\" does not always change the diagram\n" +
                    "JECC - PasswordDialog - give fixed size and not resizable fixes\n" +
                    "JECC - Charts - small performance fix\n" +
                    "JECC - Charts - colors are sometimes differing in table and chart fixes\n" +
                    "JECC - charts - old style node markers\n" +
                    "JECC - Charts - Logic Chart - zoom not working\n" +
                    "JECC - Charts - Note Dialog to secondary mouse button\n" +
                    "JECC - Charts - The dialog \"Do you want to save the changes to the analysis\" appears although nothing has been changed\n" +
                    "JECC - Charts - change zoom origin / zoom out to double click primary\n" +
                    "JECC - Charts - disable value markers\n" +
                    "JECC - Charts - Load analysis dialog - Preview optional -> setting in top menu options\n" +
                    "JECC - Charts - Regression dialog needs to be revised\n" +
                    "JECC - Charts - regression type disabled, polynomial as standard\n" +
                    "JECC - Charts - Regression dialog -> remove the selection of the species\n\n" +
                    "------Version 3.9.11------\n" +
                    "JECC - Charts - BubbleChart - missing units in axis labels\n" +
                    "JECC - Charts - migrate base libraries to chartsFX for significant perfomance improvement 30.000 -> 5.000.000 visible values\n" +
                    "JECC - Add version number to statusbar\n" +
                    "JECC - Dashboard - Dashboard links must have a different icon than analysis links bug\n" +
                    "JECC - Dashboard - The size display when loading dashboards should be adjustable on the dashboard\n" +
                    "JECC - SampleEditor - GraphExtension - date selection has no effect\n" +
                    "JECC - EnterDataDialog - changed title\n" +
                    "JECC - EnterDataDialog - changed start time to 00:00:00\n" +
                    "JECC - Alarm Plugin - fixed security exception\n" +
                    "\n" +
                    "JEDataCollector - improved logging\n" +
                    "JEVis - Add JEVisObject name localization - used in renaming dialog";
    CheckBox remember = new CheckBox(I18n.getInstance().getString("welcome.dontshow"));
    private Preferences pref = Preferences.userRoot().node("JEVis.JEConfig.patchNotes");
    private boolean isLoading = true;

    public PatchNotesPage() {

    }

    public void show(Stage owner) {

        final Stage stage = new Stage();
        remember.setSelected(true);

        //TODO show it again if we habe a new version of the Config
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
        stage.setScene(scene);

        TextArea page = new TextArea(versionHistory);

        root.getChildren().add(new Separator(Orientation.HORIZONTAL));
        root.getChildren().add(page);

        root.getChildren().add(new Separator(Orientation.HORIZONTAL));

        VBox.setVgrow(page, Priority.ALWAYS);
        HBox bot = new HBox(10);
        bot.setAlignment(Pos.BOTTOM_RIGHT);
        bot.setSpacing(5);
        bot.setPadding(new Insets(10));
        Button close = new Button(I18n.getInstance().getString("welcome.close"));
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
                storePreference();
            }
        });

        stage.showAndWait();
    }

    private void storePreference() {
        final boolean rememberIT = remember.isSelected();
        pref.put("version", JEConfig.class.getPackage().getImplementationVersion());
        pref.putBoolean("show", !rememberIT);
    }

}
