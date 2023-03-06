package org.jevis.jeconfig.plugin.action.ui;

import com.jfoenix.controls.JFXTextField;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class TextFieldWithUnit extends HBox {

    private JFXTextField jfxTextField = new JFXTextField();
    private JFXTextField unitField = new JFXTextField();

    public TextFieldWithUnit() {
        super();
        setSpacing(2);

        getChildren().addAll(jfxTextField, unitField);
        HBox.setHgrow(jfxTextField, Priority.ALWAYS);
        HBox.setHgrow(unitField, Priority.NEVER);

        unitField.setPrefWidth(50);
        jfxTextField.setAlignment(Pos.BASELINE_RIGHT);
        unitField.setAlignment(Pos.BASELINE_LEFT);
    }

    public void setEditable(boolean edible) {
        jfxTextField.setEditable(edible);
        unitField.setEditable(edible);
    }

    public JFXTextField getTextField() {
        return jfxTextField;
    }

    public JFXTextField getUnitField() {
        return unitField;
    }
}
