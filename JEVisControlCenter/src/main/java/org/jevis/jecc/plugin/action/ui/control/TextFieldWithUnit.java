package org.jevis.jecc.plugin.action.ui.control;


import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class TextFieldWithUnit extends HBox {

    private final TextField textField = new TextField();
    private final TextField unitField = new TextField();

    public TextFieldWithUnit() {
        super();
        setSpacing(2);

        getChildren().addAll(textField, unitField);
        HBox.setHgrow(textField, Priority.ALWAYS);
        HBox.setHgrow(unitField, Priority.NEVER);

        unitField.setPrefWidth(50);
        textField.setAlignment(Pos.BASELINE_RIGHT);
        unitField.setAlignment(Pos.BASELINE_LEFT);
    }

    public void setEditable(boolean edible) {
        textField.setEditable(edible);
        unitField.setEditable(edible);
    }

    public TextField getTextField() {
        return textField;
    }

    public TextField getUnitField() {
        return unitField;
    }
}
