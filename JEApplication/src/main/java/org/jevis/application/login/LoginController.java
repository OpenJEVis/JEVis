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

import org.jevis.application.connection.Connection;
import org.jevis.application.connection.ConnectionData;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class LoginController {

    private Model model = Model.getInstance();

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button cancelButton;

    @FXML
    private ProgressBar loadBar;

    @FXML
    private Button loginButton;

    @FXML
    private ImageView loginImageHeader;

    @FXML
    private ImageView loginImageLang;

    @FXML
    private Label loginLabelLanguage;

    @FXML
    private Label loginLabelPassword;

    @FXML
    private Label loginLabelProgress;

    @FXML
    private Label loginLabelServer;

    @FXML
    private Label loginLabelStatus;

    @FXML
    private Label loginLabelUserName;

    @FXML
    private Label loginLabelLangCode;

    @FXML
    private ComboBox<String> loginLanguage;

    @FXML
    private Label loginMessage;

    @FXML
    private AnchorPane loginPane;

    @FXML
    private PasswordField loginPass;

    @FXML
    private ComboBox<String> loginServer;

    @FXML
    private TextField loginUser;

    @FXML
    private ImageView symbolConnect;

    @FXML
    private ImageView symbolUser;

    @FXML
    private Label userName;

    /**
     * supportedLocales holds the available language options
     */
    Locale[] supportedLocales = {Locale.ENGLISH, Locale.GERMAN, Locale.FRENCH};

    /**
     * Initializes the UI
     */
    @FXML
    void initialize() {
        assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'login.fxml'.";
        assert loadBar != null : "fx:id=\"loadBar\" was not injected: check your FXML file 'login.fxml'.";
        assert loginButton != null : "fx:id=\"loginButton\" was not injected: check your FXML file 'login.fxml'.";
        assert loginMessage != null : "fx:id=\"loginMessage\" was not injected: check your FXML file 'login.fxml'.";
        assert loginPass != null : "fx:id=\"loginPass\" was not injected: check your FXML file 'login.fxml'.";
        assert loginServer != null : "fx:id=\"loginServer\" was not injected: check your FXML file 'login.fxml'.";
        assert loginUser != null : "fx:id=\"loginUser\" was not injected: check your FXML file 'login.fxml'.";

        // Set the available languages
        for (Locale locale : supportedLocales) {
            loginLanguage.getItems().add(locale.getDisplayLanguage());
        }

        // Start with users default language
        Locale sysLanguage = Locale.getDefault();
        loginLanguage.getSelectionModel().select(sysLanguage.getDisplayLanguage());
        resources = ResourceBundle.getBundle("lang.login", sysLanguage);

		// Set up connections
        // Connections
        Connection dummyconnection = new Connection();
        dummyconnection.data.setName("OpenJEVis Testserver");
        dummyconnection.data.setAddress("openjevis.org");
        dummyconnection.data.setPort("3306");
        dummyconnection.data.setDb("jevis");
        dummyconnection.data.setDbUser("jevis");
        dummyconnection.data.setDbPass("jevistest");

	//	loginMessage.textProperty().bind(dummyconnection.data.getStatusProperty());
		// model.getConnections()
        // model.getConnections().addConnection(dummyconnection);
        model.getConnections().add(dummyconnection);

        for (Connection connection : model.getConnections()) {
            loginServer.getItems().add(connection.data.getName().getValueSafe());
        }
        // Set connection default to first entry
        loginServer.getSelectionModel().select(0);
        model.setActiveConnection(model.getConnections().get(0));

        // Set the logo
        loginImageHeader.setImage(new Image("/images/openjevislogo.png"));

		// Set language logo
//		loadBar.setProgress(0.2);
        loadBar.progressProperty().bind(model.getLoadProgressProperty());
        model.getUserNameProperty().bind(loginUser.textProperty());
        model.getPasswordProperty().bind(loginPass.textProperty());

        // Run the connection changed listener once
        ConnectionChangeListener ccl = new ConnectionChangeListener();
        ccl.changed(null, null, null);

        // Attach the connection listener
        model.getActiveConnection().data.getStatusProperty().addListener(new ConnectionChangeListener());

    }

    /**
     * Handles change of language
     *
     * @param event
     */
    @FXML
    private void handleChangeLanguage(javafx.event.ActionEvent event) {

        Locale lang = new Locale(supportedLocales[loginLanguage.getSelectionModel().getSelectedIndex()].getLanguage());

        // New resource bundle
        ResourceBundle.clearCache();
        Locale.setDefault(lang);
        resources = ResourceBundle.getBundle("lang.login", lang);
        initText();
        loginLabelLangCode.setText(lang.getLanguage());
    }

    /**
     * Sets the language specific text on initialization and language change
     */
    @FXML
    private void initText() {
        loginLabelServer.setText(resources.getString("login.server.label"));
        loginLabelUserName.setText(resources.getString("login.user.label"));
        loginLabelPassword.setText(resources.getString("login.pass.label"));
        loginLabelLanguage.setText(resources.getString("login.language"));
        loginLabelStatus.setText(resources.getString("login.status.label"));
        loginLabelProgress.setText(resources.getString("login.load.label"));
        loginButton.setText(resources.getString("login.login"));
        cancelButton.setText(resources.getString("login.cancel"));
    }

    /**
     * Handles the login
     *
     * @param event
     */
    @FXML
    private void handleLogin(javafx.event.ActionEvent event) {

        // Process Login
        Integer i = loginServer.getSelectionModel().getSelectedIndex();
        model.setActiveConnection(model.getConnections().get(i));

        // Connect
        model.getActiveConnection().connect();

        // Login
        model.getActiveConnection().logIn(loginUser.textProperty().get(), loginPass.textProperty().get());

        LoginPreloader a = new LoginPreloader();
        a.hidePreloader();
    }

    /**
     * Handles canceling the login dialogue
     *
     * @param event
     */
    @FXML
    private void handleCancel(javafx.event.ActionEvent event) {
        System.exit(0);

    }

    @FXML
    void handleServerDialogue(MouseEvent event) {
        System.out.println("Server Dialogue not implemented yet!");
    }

    @FXML
    void handleLanguageDialogue(MouseEvent event) {
        System.out.println("Language Dialogue not implemented yet!");
    }

    /**
     * @param event
     *
     * Check conditions to activate login-button
     */
    @FXML
    void credentialCheck(KeyEvent event) {
        if (loginUser.getText().length() > 0 && loginPass.getText().length() > 0) {
            loginButton.disableProperty().set(false);
        } else {
            loginButton.disableProperty().set(true);
        }
    }

    @FXML
    void nextAction(ActionEvent event) {
        if (loginUser.getText().length() == 0) {
            loginUser.requestFocus();
        } else if (loginPass.getText().length() == 0) {
            loginPass.requestFocus();
        } else {
            loginButton.fire();
        }
    }

    /**
     * @author Bjoern Kiencke
     *
     * Change listener for the active connection - update gui elements like
     * connection icon & user name
     */
    private final class ConnectionChangeListener implements ChangeListener<Object> {

        public void changed(ObservableValue<?> arg0, Object arg1, Object arg2) {
            // Set the icons and the login message
            if (model.getActiveConnection().data.getStatus() == ConnectionData.NOT_DEFINED) {
                symbolConnect.setImage(new Image("images/icons/status/network-disconnected.png"));
                symbolUser.setImage(new Image("images/icons/status/user-inactive.png"));
                userName.setDisable(true);
                userName.setText("Not logged in");
                loginMessage.setText("Not Defined");
                loginPane.setCursor(Cursor.DEFAULT);
                loginPane.setDisable(false);
            }
            if (model.getActiveConnection().data.getStatus() == ConnectionData.DEFINITION_FAIL) {
                symbolConnect.setImage(new Image("images/icons/status/network-disconnected.png"));
                symbolUser.setImage(new Image("images/icons/status/user-inactive.png"));
                userName.setDisable(true);
                userName.setText("Not logged in");
                loginMessage.setText("Connection definition failure");
                loginPane.setCursor(Cursor.DEFAULT);
                loginPane.setDisable(false);
            }
            if (model.getActiveConnection().data.getStatus() == ConnectionData.DEFINED) {
                symbolConnect.setImage(new Image("images/icons/status/network-disconnected.png"));
                symbolUser.setImage(new Image("images/icons/status/user-inactive.png"));
                userName.setDisable(true);
                userName.setText("Not logged in");
                loginMessage.setText("Connection defined");
                loginPane.setCursor(Cursor.DEFAULT);
                loginPane.setDisable(false);
            }
            if (model.getActiveConnection().data.getStatus() == ConnectionData.CONNECTION_FAIL) {
                symbolConnect.setImage(new Image("images/icons/status/network-disconnected.png"));
                symbolUser.setImage(new Image("images/icons/status/user-inactive.png"));
                userName.setDisable(true);
                userName.setText("Not logged in");
                loginMessage.setText("Connection failed");
                loginPane.setCursor(Cursor.DEFAULT);
                loginPane.setDisable(false);
            }
            if (model.getActiveConnection().data.getStatus() == ConnectionData.CONNECTING) {
                symbolConnect.setImage(new Image("images/icons/status/network-connecting.png"));
                symbolUser.setImage(new Image("images/icons/status/user-inactive.png"));
                userName.setDisable(true);
                userName.setText("Not logged in");
                loginMessage.setText("Connecting");
                loginPane.setCursor(Cursor.WAIT);
                loginPane.setDisable(false);
            }
            if (model.getActiveConnection().data.getStatus() == ConnectionData.CONNECTED) {
                symbolConnect.setImage(new Image("images/icons/status/network-connected.png"));
                symbolUser.setImage(new Image("images/icons/status/user-inactive.png"));
                userName.setDisable(true);
                userName.setText("Not logged in");
                loginMessage.setText("Connected to DB");
                loginPane.setCursor(Cursor.DEFAULT);
                loginPane.setDisable(false);
            }
            if (model.getActiveConnection().data.getStatus() == ConnectionData.LOGIN_FAIL) {
                symbolConnect.setImage(new Image("images/icons/status/network-connected.png"));
                symbolUser.setImage(new Image("images/icons/status/user-inactive.png"));
                userName.setDisable(true);
                userName.setText("Not logged in");
                loginMessage.setText("Login failure");
                loginPane.setCursor(Cursor.DEFAULT);
                loginPane.setDisable(false);
            }
            if (model.getActiveConnection().data.getStatus() == ConnectionData.LOGGING_IN) {
                symbolConnect.setImage(new Image("images/icons/status/network-ssl-connecting.png"));
                symbolUser.setImage(new Image("images/icons/status/user.png"));
                userName.setDisable(false);
                userName.setText("...");
                loginMessage.setText("Logging in");
                loginPane.setCursor(Cursor.WAIT);
                loginPane.setDisable(true);
            }
            if (model.getActiveConnection().data.getStatus() == ConnectionData.LOGGED_IN) {
                symbolConnect.setImage(new Image("images/icons/status/network-ssl-connected.png"));
                symbolUser.setImage(new Image("images/icons/status/user.png"));
                userName.setDisable(false);
                userName.setText(model.getActiveConnection().getUserName());
                loginMessage.setText("Logged in!");
                loginPane.setCursor(Cursor.DEFAULT);
                loginPane.setDisable(true);
            }
        }
    }
}
