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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.dialog.DialogHeader;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class PasswordDialog {
    private static final Logger logger = LogManager.getLogger(PasswordDialog.class);
    public static String ICON = "1415303685_lock-128.png";

    final Label passL = new Label(I18n.getInstance().getString("tool.dialog.passworddialog.label.newpassword"));

    private Response response = Response.CANCEL;
    final Label confirmL = new Label(I18n.getInstance().getString("tool.dialog.passworddialog.label.confirmpassword"));
    final Button ok = new Button(I18n.getInstance().getString("tool.dialog.passworddialog.button.ok"));
    final PasswordField pass = new PasswordField();
    final PasswordField comfirm = new PasswordField();

    /**
     *
     * @param owner
     * @param heir
     * @param ds
     * @return
     */
    public Response show(Stage owner) {
        logger.info("Change password dialog");
        final Stage stage = new Stage();

        stage.setTitle(I18n.getInstance().getString("tool.dialog.passworddialog.title"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);

        VBox root = new VBox();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setWidth(350);
        stage.setHeight(260);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);

        HBox buttonPanel = new HBox();

        ok.setDefaultButton(true);

        Button cancel = new Button(I18n.getInstance().getString("tool.dialog.passworddialog.button.cancel"));
        cancel.setCancelButton(true);

        buttonPanel.getChildren().addAll(ok, cancel);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setPadding(new Insets(10, 10, 10, 10));
        buttonPanel.setSpacing(10);
        buttonPanel.setMaxHeight(25);

//        final ImageView warning = new ImageView(JEConfig.getImage("1415304498_alert.png"));
//        warning.setVisible(false);
//        nameF.setPromptText("Enter new name here");
        GridPane gp = new GridPane();
        gp.setPadding(new Insets(10));
        gp.setHgap(10);
        gp.setVgap(5);
        int y = 0;
        gp.add(passL, 0, y);
        gp.add(pass, 1, y);
        gp.add(confirmL, 0, ++y);
        gp.add(comfirm, 1, y);

        Separator sep = new Separator(Orientation.HORIZONTAL);
        sep.setMinHeight(10);
        Node header = DialogHeader.getDialogHeader(ICON, I18n.getInstance().getString("tool.dialog.passworddialog.header"));

        root.getChildren().addAll(header, new Separator(Orientation.HORIZONTAL), gp, buttonPanel);
        VBox.setVgrow(buttonPanel, Priority.NEVER);
        VBox.setVgrow(header, Priority.NEVER);

        ok.setDisable(true);
        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
//                logger.info("Size: h:" + stage.getHeight() + " w:" + stage.getWidth());
                stage.close();
//                isOK.setValue(true);
                response = Response.YES;

            }
        });

        pass.setOnKeyReleased(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                checkPW();
            }
        });

        comfirm.setOnKeyReleased(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                checkPW();
            }
        });

        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                stage.close();
                response = Response.CANCEL;

            }
        });

        pass.requestFocus();
        stage.showAndWait();
        logger.info("return " + response);

        return response;
    }

    public enum Response {

        YES, CANCEL
    }

    public String getPassword() {
        return pass.getText();
    }

    private void checkPW() {
        if (!pass.getText().isEmpty() && !comfirm.getText().isEmpty()) {
            if (pass.getText().equals(comfirm.getText())) {
                ok.setDisable(false);
            }
        }

    }
}
