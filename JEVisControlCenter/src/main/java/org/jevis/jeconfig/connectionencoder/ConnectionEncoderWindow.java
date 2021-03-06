/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.connectionencoder;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;

/**
 * @author fs
 */
public class ConnectionEncoderWindow {
    private static final Logger logger = LogManager.getLogger(ConnectionEncoderWindow.class);

    @FXML
    private JFXTextField host;

    @FXML
    private JFXTextField port;

    @FXML
    private JFXTextField result;

    @FXML
    private JFXTextField schema;

    @FXML
    private JFXTextField user;

    @FXML
    private JFXPasswordField passwd;

    @FXML
    private JFXButton CopyCon;

    @FXML
    void onChanged(KeyEvent event) {
        logger.info("onChange");
    }

    @FXML
    void onCopy(ActionEvent event) {
        logger.info("OnCopy");
    }

    public ConnectionEncoderWindow(Stage stage) {
        try {
            Stage newWindow = new Stage();
            Parent root = FXMLLoader.load(JEConfig.class.getResource("/fxml/ConnectionCreator.fxml"));//JEConfig.class.getResource
            Scene scene = new Scene(root, 376, 317);
            TopMenu.applyActiveTheme(scene);
            newWindow.setTitle("Connection Creator");
            newWindow.setScene(scene);
            newWindow.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
