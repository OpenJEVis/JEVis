package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.JFXTextField;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;


public class RenameDialog extends Dialog {

    public RenameDialog(JEVisObject selectedItem) {
        super();
        setTitle(I18n.getInstance().getString("plugin.accounting.renamedialog.title"));
        setHeaderText(I18n.getInstance().getString("plugin.accounting.renamedialog.header"));
        setResizable(true);
        initOwner(JEConfig.getStage());
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        Label nameLabel = new Label(I18n.getInstance().getString("newobject.name.prompt"));
        VBox nameVBox = new VBox(nameLabel);
        nameVBox.setAlignment(Pos.CENTER);
        JFXTextField nameField = new JFXTextField(selectedItem.getName());
        nameField.setMinWidth(250);

        ButtonType okType = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        this.getDialogPane().getButtonTypes().addAll(cancelType, okType);

        Button okButton = (Button) this.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);

        Button cancelButton = (Button) this.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);

        okButton.setOnAction(event -> {
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

        cancelButton.setOnAction(event -> close());

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(8, 0, 8, 0));

        VBox vBox = new VBox(6, new HBox(6, nameVBox, nameField), separator);
        vBox.setPadding(new Insets(6));
        getDialogPane().setContent(vBox);
    }
}
