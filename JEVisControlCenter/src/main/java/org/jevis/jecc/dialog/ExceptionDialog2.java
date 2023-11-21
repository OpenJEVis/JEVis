package org.jevis.jecc.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;
import org.jevis.commons.i18n.I18n;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionDialog2 {

    public static void showException(Window parent, Exception ex) {

        String header = ex.toString();
        String cause = ex.toString();
        if (ex.getCause() != null) {
            cause = ex.getCause().getMessage();
        }

        showException(parent,
                I18n.getInstance().getString("dialog.error.title"),
                header, cause, ex);
    }

    public static void showException(Window parent, String title, String header, String content, Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);


        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();
//        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
//        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

// Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        alert.initOwner(parent);
        alert.setWidth(500);
        alert.setHeight(500);
        alert.showAndWait();
    }

}
