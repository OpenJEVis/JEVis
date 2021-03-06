/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.JFXTextArea;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.jevis.jeconfig.JEConfig;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author fs
 */
public abstract class CommonDialogs {

    /**
     *
     * @param titel
     * @param header
     * @param ex
     */
    public static void showError(String titel, String header, String message, Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(JEConfig.getStage());
        alert.setTitle(titel);
        alert.setHeaderText(header);
        alert.setContentText(ex.getMessage());

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        JFXTextArea textArea = new JFXTextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }
}
