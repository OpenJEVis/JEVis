/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.connectionencoder;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import org.jevis.commons.cli.ConnectionEncoder;

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
    private TextField host;

    @FXML
    private TextField port;

    @FXML
    private TextField result;

    @FXML
    private TextField schema;

    @FXML
    private TextField user;

    @FXML
    private PasswordField passwd;

    @FXML
    private Button CopyCon;

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
