/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class Login {

    public String usernameResult = null;
    public String passwordResult = null;
    public String serverResult = null;
    public JEVisDataSource _ds;

    final TextField txUserName = new TextField();
    final PasswordField txPassword = new PasswordField();

    public JEVisDataSource getDS() throws JEVisException {
        System.out.println("getDS");
        System.out.println("Connect as: " + txUserName.getText() + "/" + txPassword.getText());
        _ds.connect(txUserName.getText(), txPassword.getText());

        System.out.println("Object: " + _ds.getObject(1l));
        return _ds;
    }

    public Login(JEVisDataSource ds, String user, String pw, String server, String langue) {
        _ds = ds;
    }

    public Login(JEVisDataSource _ds) {
        this._ds = _ds;
    }

    public void showLogin(boolean wasWrong) throws JEVisException {
//        GridPane grid = new GridPane();
//        grid.setHgap(10);
//        grid.setVgap(10);
//        grid.setPadding(new Insets(0, 10, 0, 10));
//        final TextField username = new TextField();
//        username.setPromptText("Username");
//        final PasswordField password = new PasswordField();
//        password.setPromptText("Password");
////        final TextField server = new TextField();
////        username.setPromptText("Server");
//
//        grid.add(new Label("Username:"), 0, 0);
//        grid.add(username, 1, 0);
//        grid.add(new Label("Password:"), 0, 1);
//        grid.add(password, 1, 1);
//        Label pww = new Label("Wrong User/Password");
//        if (wasWrong) {
//            pww.setStyle("-fx-text-fill: Color.rgb(210, 39, 30);");
//        } else {
//            pww.setStyle("-fx-text-fill: rgba(0, 100, 100, 0);");
//        }
//
//        grid.add(pww, 0, 2);
//
//        Callback<Void, Void> myCallback = new Callback<Void, Void>() {
//            @Override
//            public Void call(Void param) {
//                usernameResult = username.getText();
//                passwordResult = password.getText();
////                serverResult = server.getText();
//                return null;
//            }
//        };

//        Action response = Dialogs.create()
//                .owner(null)
//                .title("Please log in")
//                .message("Please log in")
//                .showConfirm();
//
//        DialogResponse resp = Dialogs.showCustomDialog(JEConfig.getStage(), grid, "Please log in", "Login", DialogOptions.OK_CANCEL, myCallback);
//        if (resp == DialogResponse.OK) {
//            try {
//                _ds.connect(usernameResult, passwordResult);
//            } catch (Exception ex) {
//                showLogin(true);
//            }
//        } else {
//            System.exit(0);
//        }
    }

    private void validate() {
//        actionLogin.disabledProperty().set(
//                txUserName.getText().trim().isEmpty() || txPassword.getText().trim().isEmpty());
    }

    public void showLoginDialog() {
//        Dialog dlg = new Dialog(null, "Login Dialog");
//
//        // listen to user input on dialog (to enable / disable the login button)
//        ChangeListener<String> changeListener = new ChangeListener<String>() {
//            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//                validate();
//            }
//        };
//        txUserName.textProperty().addListener(changeListener);
//        txPassword.textProperty().addListener(changeListener);
//
//        // layout a custom GridPane containing the input fields and labels
//        final GridPane content = new GridPane();
//        content.setHgap(10);
//        content.setVgap(10);
//
//        content.add(new Label("User name"), 0, 0);
//        content.add(txUserName, 1, 0);
//        GridPane.setHgrow(txUserName, Priority.ALWAYS);
//        content.add(new Label("Password"), 0, 1);
//        content.add(txPassword, 1, 1);
//        GridPane.setHgrow(txPassword, Priority.ALWAYS);
//
//        // create the dialog with a custom graphic and the gridpane above as the
//        // main content region
//        dlg.setResizable(false);
//        dlg.setIconifiable(false);
////        dlg.setGraphic(new ImageView(HelloDialog.class.getResource("login.png").toString()));
//        dlg.setContent(content);
////        dlg.getActions().addAll(actionLogin, Dialog.Actions.CANCEL);
//        dlg.getActions().clear();
//        dlg.getActions().addAll(actionLogin, Dialog.Actions.CANCEL);
//        validate();
//
//        // request focus on the username field by default (so the user can
//        // type immediately without having to click first)
//        Platform.runLater(new Runnable() {
//            public void run() {
//                txUserName.requestFocus();
//            }
//        });
//
//        dlg.show();
    }
}
