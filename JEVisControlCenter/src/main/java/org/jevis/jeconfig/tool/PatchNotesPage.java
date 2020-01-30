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
