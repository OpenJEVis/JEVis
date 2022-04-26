package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;


public class RenameDialog extends JFXDialog {

    public RenameDialog(StackPane dialogContainer, JEVisObject selectedItem) {
        super();
        setDialogContainer(dialogContainer);
        setTransitionType(DialogTransition.NONE);

        Label nameLabel = new Label(I18n.getInstance().getString("newobject.name.prompt"));
        VBox nameVBox = new VBox(nameLabel);
        nameVBox.setAlignment(Pos.CENTER);
        JFXTextField nameField = new JFXTextField(selectedItem.getName());
        nameField.setMinWidth(250);

        final JFXButton ok = new JFXButton(I18n.getInstance().getString("newobject.ok"));
        ok.setDefaultButton(true);
        final JFXButton cancel = new JFXButton(I18n.getInstance().getString("newobject.cancel"));
        cancel.setCancelButton(true);

        ok.setOnAction(event -> {
            try {
                if (selectedItem.getDataSource().getCurrentUser().canWrite(selectedItem.getID())) {
                    selectedItem.setName(nameField.getText());
                    selectedItem.commit();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, I18n.getInstance().getString("jevistree.dialog.rename.permission.denied.message"));
                    alert.showAndWait();
                }
            } catch (JEVisException e) {
                e.printStackTrace();
            }

            close();
        });

        cancel.setOnAction(event -> close());

        HBox buttonBar = new HBox(6, cancel, ok);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(12));

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(8, 0, 8, 0));

        VBox vBox = new VBox(6, new HBox(6, nameVBox, nameField), separator, buttonBar);
        vBox.setPadding(new Insets(6));
        setContent(vBox);
    }
}
