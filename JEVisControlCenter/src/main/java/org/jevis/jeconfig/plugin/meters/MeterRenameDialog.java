package org.jevis.jeconfig.plugin.meters;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;

public class MeterRenameDialog extends JFXDialog {

    public MeterRenameDialog(StackPane dialogContainer, RegisterTableRow selectedItem) throws JEVisException {
        super();
        setDialogContainer(dialogContainer);
        setTransitionType(DialogTransition.NONE);

        GridPane gp = new GridPane();
        gp.setVgap(6);
        gp.setHgap(6);

        Label nameLabel = new Label(I18n.getInstance().getString("newobject.name.prompt"));
        JFXTextField nameField = new JFXTextField(selectedItem.getObject().getName());
        nameField.setMinWidth(250);

        Label dataRowLabel = new Label(I18n.getInstance().getString("plugin.alarm.table.objectname"));

        JEVisAttribute onlineIdType = selectedItem.getObject().getAttribute("Online ID");
        TargetHelper targetHelper = new TargetHelper(selectedItem.getObject().getDataSource(), onlineIdType);

        if (!targetHelper.getObject().isEmpty()) {
            JEVisObject targetObject = targetHelper.getObject().get(0);

            JFXTextField targetTextField = new JFXTextField(targetObject.getName());
            targetTextField.setMinWidth(250);

            int row = 0;
            gp.add(nameLabel, 0, row);
            gp.add(nameField, 1, row);
            row++;

            gp.add(dataRowLabel, 0, row);
            gp.add(targetTextField, 1, row);


            final JFXButton ok = new JFXButton(I18n.getInstance().getString("newobject.ok"));
            ok.setDefaultButton(true);
            final JFXButton cancel = new JFXButton(I18n.getInstance().getString("newobject.cancel"));
            cancel.setCancelButton(true);

            ok.setOnAction(event -> {
                try {
                    selectedItem.getObject().setName(nameField.getText());
                    selectedItem.getObject().commit();

                    targetObject.setName(targetTextField.getText());
                    targetObject.commit();
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

            VBox vBox = new VBox(6, gp, separator, buttonBar);
            vBox.setPadding(new Insets(6));
            setContent(vBox);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, I18n.getInstance().getString("dialog.regression.type.none") + " " + I18n.getInstance().getString("jevistree.dialog.copy.link"));
            alert.showAndWait();
            close();
        }
    }
}
