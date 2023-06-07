package org.jevis.jecc.plugin.meters;

import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.TopMenu;

public class MeterRenameDialog extends Dialog {

    public MeterRenameDialog(RegisterTableRow selectedItem) throws JEVisException {
        super();
        setTitle(I18n.getInstance().getString("plugin.meters.renamedialog.title"));
        setHeaderText(I18n.getInstance().getString("plugin.meters.renamedialog.header"));
        setResizable(true);
        initOwner(ControlCenter.getStage());
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        GridPane gp = new GridPane();
        gp.setVgap(6);
        gp.setHgap(6);

        Label nameLabel = new Label(I18n.getInstance().getString("newobject.name.prompt"));
        MFXTextField nameField = new MFXTextField(selectedItem.getObject().getName());
        nameField.setMinWidth(250);

        Label dataRowLabel = new Label(I18n.getInstance().getString("plugin.alarm.table.objectname"));

        JEVisAttribute onlineIdType = selectedItem.getObject().getAttribute("Online ID");
        TargetHelper targetHelper = new TargetHelper(selectedItem.getObject().getDataSource(), onlineIdType);

        if (!targetHelper.getObject().isEmpty()) {
            JEVisObject targetObject = targetHelper.getObject().get(0);

            MFXTextField targetTextField = new MFXTextField(targetObject.getName());
            targetTextField.setMinWidth(250);

            int row = 0;
            gp.add(nameLabel, 0, row);
            gp.add(nameField, 1, row);
            row++;

            gp.add(dataRowLabel, 0, row);
            gp.add(targetTextField, 1, row);


            ButtonType okType = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

            this.getDialogPane().getButtonTypes().addAll(cancelType, okType);

            Button okButton = (Button) this.getDialogPane().lookupButton(okType);
            okButton.setDefaultButton(true);

            Button cancelButton = (Button) this.getDialogPane().lookupButton(cancelType);
            cancelButton.setCancelButton(true);

            okButton.setOnAction(event -> {
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

            cancelButton.setOnAction(event -> close());

            Separator separator = new Separator(Orientation.HORIZONTAL);
            separator.setPadding(new Insets(8, 0, 8, 0));

            VBox vBox = new VBox(6, gp, separator);
            vBox.setPadding(new Insets(6));
            getDialogPane().setContent(vBox);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, I18n.getInstance().getString("dialog.regression.type.none") + " " + I18n.getInstance().getString("jevistree.dialog.copy.link"));
            alert.showAndWait();
            close();
        }
    }
}
