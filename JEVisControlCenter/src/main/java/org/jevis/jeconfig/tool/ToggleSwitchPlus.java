package org.jevis.jeconfig.tool;

import javafx.beans.property.BooleanProperty;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.controlsfx.control.ToggleSwitch;

/**
 * Advanced ToggleButton with an localized on/off text
 */
public class ToggleSwitchPlus extends GridPane {

    private Label labelOn = new Label(I18n.getInstance().getString("extension.calc.button.toggle.activate"));
    private Label labelOff = new Label(I18n.getInstance().getString("extension.calc.button.toggle.deactivate"));
    private ToggleSwitch button = new ToggleSwitch();

    public ToggleSwitchPlus() {
        super();
        setHgap(0);
        labelOn.setVisible(false);
        labelOff.setVisible(true);
        selectedProperty().setValue(false);

        selectedProperty().addListener((observable, oldValue, newValue) -> {
            labelOn.setVisible(newValue);
            labelOff.setVisible(!newValue);
        });

        add(labelOn, 0, 0);
        add(labelOff, 0, 0);
        add(button, 1, 0);

        GridPane.setHgrow(labelOn, Priority.NEVER);
        GridPane.setHgrow(labelOff, Priority.NEVER);
        GridPane.setHalignment(button, HPos.LEFT);

    }

    public final BooleanProperty selectedProperty() {
        return button.selectedProperty();
    }

    public final boolean isSelected() {
        return button.isSelected();
    }

    public final void setSelected(boolean value) {
        button.selectedProperty().setValue(value);
    }

}
