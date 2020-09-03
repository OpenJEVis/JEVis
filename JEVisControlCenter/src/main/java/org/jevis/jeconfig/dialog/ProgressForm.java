/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.dialog;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jevis.jeconfig.JEConfig;

/**
 * @author fs
 */
public class ProgressForm {

    private final Stage dialogStage;
    private final ProgressBar pb = new ProgressBar();
    private final TextArea textArea = new TextArea();
    private final StringBuilder stringBuilder = new StringBuilder();

    public ProgressForm(String text) {
        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.initOwner(JEConfig.getStage());
        dialogStage.setResizable(false);
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        // PROGRESS BAR
        pb.setMinWidth(250);
        final Label label = new Label();
        label.setText(text);

        pb.setProgress(-1F);
        textArea.setVisible(false);

        final VBox vb = new VBox();
        final HBox hb = new HBox();
        hb.setPadding(new Insets(4));
        hb.setSpacing(10);
        hb.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT, new Insets(2))));
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().addAll(label, pb);

        vb.getChildren().addAll(hb, textArea);

        VBox.setVgrow(textArea, Priority.ALWAYS);

        Scene scene = new Scene(vb);
        dialogStage.setScene(scene);
    }

    public void activateProgressBar(final Task<?> task) {
        pb.progressProperty().bind(task.progressProperty());
        dialogStage.show();
    }

    public void activateProgressBar() {
        dialogStage.show();
    }

    public void closeProgressBar() {
        dialogStage.close();
    }

    public Stage getDialogStage() {
        return dialogStage;
    }

    public void setProgress(double progress) {
        pb.setProgress(progress);
    }

    public void addMessage(String message) {

        if (!textArea.isVisible()) {
            Platform.runLater(() -> {
                textArea.setVisible(true);
                textArea.setMinHeight(150);
                textArea.setPrefHeight(150);
            });
        }
        if (stringBuilder.toString().length() > 0) {
            stringBuilder.append(System.getProperty("line.separator"));
        }
        stringBuilder.append(message);
        Platform.runLater(() -> {
            textArea.setText(stringBuilder.toString());
            textArea.setScrollTop(Double.MAX_VALUE);
        });
    }
}
