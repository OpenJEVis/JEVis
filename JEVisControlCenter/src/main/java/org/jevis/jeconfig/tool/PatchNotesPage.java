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
 * The WelcomePage is an dialog to show an URL as an welcome for the user.#
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class PatchNotesPage {
    private static final Logger logger = LogManager.getLogger(PatchNotesPage.class);
    private final String versionHistory = "------Version 3.9.12------\n" +
            "JECC - Release Notes\n" +
            "JECC - Diagrams - Regression dialog needs to be revised\n" +
            "JECC - Analysis - is it possible to deactivate the regression button if only plugins are displayed that do not support them?\n" +
            "JECC - Analysis - Regression dialog -> Selection of the type remove fixes\n" +
            "JECC - JENotifier Service indicates that it is deactivated even though it is on\n" +
            "JECC - Charts - \"Day of the week function\" does not always change the diagram\n" +
            "JECC - PasswordDialog - give fixed size and not resizable fixes\n" +
            "JECC - Charts - small performance fix\n" +
            "JECC - Charts - colors are sometimes differing in table and chart fixes\n\n" +
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
