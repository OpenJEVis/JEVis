/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.dialog;

import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.JEConfig;

import java.util.Optional;

/**
 *
 * @author fs
 */
public class FindDialog {

    public Response show(String title, String titleLong, String message) {
        Dialog dialog = new TextInputDialog("");
        dialog.initOwner(JEConfig.getStage());
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

    private final JEVisDataSource _ds;
    private String _result = "";

    public FindDialog(JEVisDataSource ds) {
        _ds = ds;
    }

    public enum Response {

        NO, YES, CANCEL
    }

    public String getResult() {
        return _result;
    }

}
