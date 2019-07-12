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
        this.labelOn.setVisible(false);
        this.labelOff.setVisible(true);
        selectedProperty().setValue(false);

        selectedProperty().addListener((observable, oldValue, newValue) -> {
            this.labelOn.setVisible(newValue);
            this.labelOff.setVisible(!newValue);
        });

        add(this.labelOn, 0, 0);
        add(this.labelOff, 0, 0);
        add(this.button, 1, 0);

        GridPane.setHgrow(this.labelOn, Priority.NEVER);
        GridPane.setHgrow(this.labelOff, Priority.NEVER);
        GridPane.setHalignment(this.button, HPos.LEFT);

    }

    public void setLabels(String onText, String offText) {
        this.labelOn.setText(onText);
        this.labelOff.setText(offText);
    }

    public final BooleanProperty selectedProperty() {
        return this.button.selectedProperty();
    }

    public final boolean isSelected() {
        return this.button.isSelected();
    }

    public final void setSelected(boolean value) {
        this.button.selectedProperty().setValue(value);
    }

}
