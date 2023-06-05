package org.jevis.jeconfig.plugin.nonconformities.ui;

import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class TextFieldWithUnit extends HBox {

    private MFXTextField MFXTextField = new MFXTextField();
    private MFXTextField unitField = new MFXTextField();

    public TextFieldWithUnit() {
        super();
        setSpacing(2);

        getChildren().addAll(MFXTextField, unitField);
        HBox.setHgrow(MFXTextField, Priority.ALWAYS);
        HBox.setHgrow(unitField, Priority.NEVER);

        unitField.setPrefWidth(50);
        MFXTextField.setAlignment(Pos.BASELINE_RIGHT);
        unitField.setAlignment(Pos.BASELINE_LEFT);
    }

    public MFXTextField getTextField() {
        return MFXTextField;
    }

    public MFXTextField getUnitField() {
        return unitField;
    }
}
