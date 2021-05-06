package org.jevis.jeconfig.plugin.dtrc;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.jevis.api.JEVisObject;

public class InputItem {
    private final Label label;
    private final ComboBox<JEVisObject> comboBox;

    public InputItem(Label label, ComboBox<JEVisObject> comboBox) {
        this.label = label;
        this.comboBox = comboBox;
    }

    public Label getLabel() {
        return label;
    }

    public ComboBox<JEVisObject> getComboBox() {
        return comboBox;
    }
}
