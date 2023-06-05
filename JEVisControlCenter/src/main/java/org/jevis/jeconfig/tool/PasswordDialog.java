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

import com.jfoenix.controls.JFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeapi.ws.JEVisDataSourceWS;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.dialog.DialogHeader;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class PasswordDialog {
    private static final Logger logger = LogManager.getLogger(PasswordDialog.class);
    public static String ICON = "1415303685_lock-128.png";

    private final Label passOldL = new Label(I18n.getInstance().getString("tool.dialog.passworddialog.label.oldpassword"));
    private final Label passL = new Label(I18n.getInstance().getString("tool.dialog.passworddialog.label.newpassword"));

    private Response response = Response.CANCEL;
    private final Label confirmL = new Label(I18n.getInstance().getString("tool.dialog.passworddialog.label.confirmpassword"));
    private final MFXButton ok = new MFXButton(I18n.getInstance().getString("tool.dialog.passworddialog.button.ok"));
    private final JFXPasswordField newPass = new JFXPasswordField();
    private final JFXPasswordField oldPass = new JFXPasswordField();
    private final JFXPasswordField confirmNew = new JFXPasswordField();
    private boolean correctOldPass = false;

    /**
     * @param owner
     * @param oldUser can be null, if not null and its the same as the current user request old password
     * @return
     */
    public Response show(Stage owner, JEVisObject oldUser) {
        logger.info("Change password dialog");
        final Stage stage = new Stage();

        stage.setTitle(I18n.getInstance().getString("tool.dialog.passworddialog.title"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);

        VBox root = new VBox();

        Scene scene = new Scene(root);
        TopMenu.applyActiveTheme(scene);
        stage.setScene(scene);
        stage.setWidth(400);
        stage.setHeight(300);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);

        HBox buttonPanel = new HBox();

        ok.setDefaultButton(true);

        MFXButton cancel = new MFXButton(I18n.getInstance().getString("tool.dialog.passworddialog.button.cancel"));
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
        try {
            if (oldUser != null && oldUser.getJEVisClass().getName().equals("User")) {
                gp.add(passOldL, 0, y);
                gp.add(oldPass, 1, y);
            } else {
                correctOldPass = true;
            }
        } catch (Exception ex) {
            logger.error(ex);
        }


        gp.add(passL, 0, ++y);
        gp.add(newPass, 1, y);
        gp.add(confirmL, 0, ++y);
        gp.add(confirmNew, 1, y);

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
//                logger.info("Size: h:" + stage.getHeight() + " w:" + stage.getWidth())

                stage.close();
//                isOK.setValue(true);
                response = Response.YES;

            }
        });

        oldPass.setOnKeyReleased(event -> {
            try {
                if (oldUser != null) {
                    String currentUsername = oldUser.getDataSource().getCurrentUser().getAccountName();
                    JEVisDataSourceWS dsWS = (JEVisDataSourceWS) oldUser.getDataSource();
                    correctOldPass = dsWS.confirmPassword(currentUsername, oldPass.getText());
                }
            } catch (Exception ex) {
                logger.error(ex);
            }
        });

        newPass.setOnKeyReleased(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                checkPW();
            }
        });

        confirmNew.setOnKeyReleased(new EventHandler<KeyEvent>() {

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

        newPass.requestFocus();
        stage.showAndWait();
        logger.info("return {}", response);

        return response;
    }

    public enum Response {

        YES, CANCEL
    }

    public String getPassword() {
        return newPass.getText();
    }


    private void checkPW() {
        if (!newPass.getText().isEmpty() && !confirmNew.getText().isEmpty()) {
            if (newPass.getText().equals(confirmNew.getText()) && correctOldPass) {
                ok.setDisable(false);
            }
        }

    }
}
