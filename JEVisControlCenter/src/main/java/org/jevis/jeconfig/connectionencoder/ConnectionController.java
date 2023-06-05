/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.connectionencoder;

import com.jfoenix.controls.JFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import org.jevis.commons.cli.ConnectionEncoder;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author fs
 */
public class ConnectionController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        onChanged(null);
    }

    @FXML
    private MFXTextField host;

    @FXML
    private MFXTextField port;

    @FXML
    private MFXTextField result;

    @FXML
    private MFXTextField schema;

    @FXML
    private MFXTextField user;

    @FXML
    private JFXPasswordField passwd;

    @FXML
    private MFXButton CopyCon;

    @FXML
    void onChanged(KeyEvent event) {
        String hostS = host.getText().isEmpty() ? host.getPromptText() : host.getText();
        String portS = port.getText().isEmpty() ? port.getPromptText() : port.getText();
        String schemaS = schema.getText().isEmpty() ? schema.getPromptText() : schema.getText();
        String userS = user.getText().isEmpty() ? user.getPromptText() : user.getText();
        String pwS = passwd.getText().isEmpty() ? passwd.getPromptText() : passwd.getText();

        result.setText(ConnectionEncoder.encode(hostS, portS, schemaS, userS, pwS));
    }

    @FXML
    void onCopy(ActionEvent event) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(result.getText());
        clipboard.setContent(content);
    }

}
