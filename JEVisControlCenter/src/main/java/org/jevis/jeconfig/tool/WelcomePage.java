/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEConfig.
 *
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.tool;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class WelcomePage {

    private Preferences pref = Preferences.userRoot().node("JEVis.JEConfig.Welcome");
    CheckBox remember = new CheckBox("Don't show this again");
    private boolean isLoading = true;

    public WelcomePage(Stage owner, URI welcomepage) {

        final Stage stage = new Stage();

        //TODO show it again if we habe a new version of the Config
        if (!pref.getBoolean("show", true)) {
            return;
        }

        stage.setTitle("Welcome");
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

        final WebView page = new WebView();
        final WebEngine webEngine = page.getEngine();
        webEngine.setJavaScriptEnabled(true);

        webEngine.loadContent("<html>Loading Welcome Page....</html>", "text/html");

        webEngine.getLoadWorker().stateProperty().addListener(
                new ChangeListener<State>() {
                    public void changed(ObservableValue ov, State oldState, State newState) {
                        if (newState == State.SUCCEEDED) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    //does not work, some multy thred probems i gues
//                                    isLoading = false;
//                                    stage.showAndWait();

                                }

                            });
                        }
                    }
                });

//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });
//        new Thread() {
//
//            @Override
//            public void run() {
//
//            }
//        }.start();
        root.getChildren().add(new Separator(Orientation.HORIZONTAL));
        root.getChildren().add(page);

        root.getChildren().add(new Separator(Orientation.HORIZONTAL));

        HBox bot = new HBox(10);
        bot.setAlignment(Pos.BOTTOM_RIGHT);
        bot.setSpacing(5);
        bot.setPadding(new Insets(10));
        Button close = new Button("Close");
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

        try {
            webEngine.load(welcomepage.toURL().toExternalForm());
            stage.showAndWait();

        } catch (MalformedURLException ex) {
            Logger.getLogger(WelcomePage.class.getName()).log(Level.SEVERE, null, ex);

        }

    }

    private void storePreference() {
        final boolean rememberIT = remember.isSelected();

        pref.putBoolean("show", !rememberIT);
    }

}
