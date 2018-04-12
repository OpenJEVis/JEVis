/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.application.dialog;

import java.util.Optional;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import org.jevis.api.JEVisDataSource;

/**
 *
 * @author fs
 */
public class FindDialog {

    public static enum Response {

        NO, YES, CANCEL
    };
    private final JEVisDataSource _ds;
    private String _result = "";

    public FindDialog(JEVisDataSource ds) {
        _ds = ds;
    }

    public Response show(Stage owner, String title, String titleLong, String message) {
        Dialog dialog = new TextInputDialog("");
        dialog.setTitle(titleLong);
        dialog.setHeaderText("");

        Optional<String> result = dialog.showAndWait();
        String entered = "none.";

        if (result.isPresent()) {
            _result = result.get();
            return Response.YES;
        }
        return Response.NO;
    }

    public String getResult() {
        return _result;
    }

}
