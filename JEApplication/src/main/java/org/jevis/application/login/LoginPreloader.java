/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEApplication.
 *
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 *
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.application.login;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import org.jevis.application.application.PreloadedApplication;
import org.jevis.application.connection.ConnectionData;

/**
 * @author Björn Kiencke
 *
 * Show a preloader with log in functionality
 */
public class LoginPreloader extends Preloader {

    /**
     * @author Björn Kiencke <bjoern.kiencke@openjevis.org>
     */
    // MVC
    private Model model = Model.getInstance();

    public void hidePreloader() {
        mayBeHide();
    }

    @Override
    public void start(Stage stage) throws Exception {

        // Set the stage central in model
        model.setStage(stage);

        FXMLLoader fxmlLoader = new FXMLLoader();

        try {
            // Try to load system default language
            fxmlLoader.setResources(ResourceBundle.getBundle("lang.login", Locale.getDefault()));
        } catch (Exception ex) {
            // Load English
            fxmlLoader.setResources(ResourceBundle.getBundle("lang.login", Locale.ENGLISH));
        }

        // Load the fxml
        Parent root = (Parent) fxmlLoader.load(this.getClass().getResource("/fxml/login.fxml").openStream());
        Scene scene = new Scene(root);

		// Set the controller
//		controller = fxmlLoader.getController();
        // Set the application icon
        model.getStage().getIcons().add(new Image("/images/icon.png"));

        // Set application title
        model.getStage().setTitle("JEVis Energy Monitoring");

        // Set size and make window fix size
        model.getStage().setMaxHeight(400);
        model.getStage().setMaxWidth(600);
        model.getStage().setMinHeight(400);
        model.getStage().setMinWidth(600);
        model.getStage().setResizable(false);
        model.getStage().setScene(scene);

        // test
        Map<String, String> param = new HashMap<String, String>();
        param = getParameters().getNamed();
        System.out.println("PORT: " + param.get("port"));

        // Curtain fall
        model.getStage().show();
    }

    /**
     * Fallback main method
     *
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Hides the login in case of successful login
     */
    private void mayBeHide() {
        // ?
        if (model.getStage().isShowing()) {
            // If connection established
            if (model.getActiveConnection().data.getStatus() == ConnectionData.LOGGED_IN) {
                // Start the main application with the given connection
                model.getConsumer().startMainApplication(model.getActiveConnection());
                Platform.runLater(new Runnable() {
                    public void run() {
                        // hide this stage
                        model.getStage().hide();
                    }
                });
            }
        }
    }

    @Override
    public void handleProgressNotification(ProgressNotification pn) {
        // Set the progress
        model.setLoadProgress(pn.getProgress());
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification evt) {
        if (evt.getType() == StateChangeNotification.Type.BEFORE_START) {
            // Set link to the main application
            model.setConsumer((PreloadedApplication) evt.getApplication());

            // hide preloader if connection established
            mayBeHide();
        }
    }
}
