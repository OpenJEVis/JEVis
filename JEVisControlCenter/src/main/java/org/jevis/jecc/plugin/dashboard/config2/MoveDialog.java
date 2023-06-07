package org.jevis.jecc.plugin.dashboard.config2;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.plugin.dashboard.DashboardControl;

public class MoveDialog extends Dialog<ButtonType> {


    public MoveDialog(Window owner, DashboardControl control) {
        super();
        this.initOwner(owner);
        this.getDialogPane().getButtonTypes().setAll();

        GridPane gp = new GridPane();
        gp.setPadding(new Insets(8, 8, 8, 8));
        gp.setHgap(8);
        gp.setVgap(8);

        MFXButton leftButton = new MFXButton("<");
        MFXButton rightButton = new MFXButton(">");
        MFXButton downButton = new MFXButton("down");
        MFXButton upButton = new MFXButton("^");

        TextField pixels = new TextField(control.getActiveDashboard().getxGridInterval() + "");
        pixels.setMaxWidth(35);

        gp.add(upButton, 1, 0);
        gp.add(leftButton, 0, 1);
        gp.add(pixels, 1, 1);
        gp.add(rightButton, 2, 1);
        gp.add(downButton, 1, 2);
        getDialogPane().setContent(gp);


        upButton.setOnAction(event -> {
            control.moveSelected(Double.parseDouble(pixels.getText()), 0, 0, 0);
        });
        downButton.setOnAction(event -> {
            control.moveSelected(0, Double.parseDouble(pixels.getText()), 0, 0);
        });

        leftButton.setOnAction(event -> {
            control.moveSelected(0, 0, Double.parseDouble(pixels.getText()), 0);
        });
        rightButton.setOnAction(event -> {
            control.moveSelected(0, 0, 0, Double.parseDouble(pixels.getText()));
        });


        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("jevistree.dialog.new.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        getDialogPane().getButtonTypes().addAll(cancel);
        this.setOnCloseRequest(event -> {
            this.hide();
        });
    }


}
