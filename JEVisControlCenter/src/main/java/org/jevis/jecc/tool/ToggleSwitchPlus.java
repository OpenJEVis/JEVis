package org.jevis.jecc.tool;

import io.github.palexdev.materialfx.controls.MFXToggleButton;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.jevis.commons.i18n.I18n;

/**
 * Advanced ToggleButton with localized on/off text
 */
public class ToggleSwitchPlus extends GridPane {

    private final String labelOn = I18n.getInstance().getString("extension.calc.button.toggle.activate");
    private final String labelOff = I18n.getInstance().getString("extension.calc.button.toggle.deactivate");
    private final Label buttonLabel = new Label();
    private final MFXToggleButton button = new MFXToggleButton();

    public ToggleSwitchPlus() {
        super();
        setHgap(4);

        final Label test1 = new Label(labelOn);
        final Label test2 = new Label(labelOff);
        test1.setFont(buttonLabel.getFont());
        test2.setFont(buttonLabel.getFont());
        double newWidth1 = test1.getLayoutBounds().getWidth();
        double newWidth2 = test2.getLayoutBounds().getWidth();

        buttonLabel.setMinWidth(Math.max(newWidth1, newWidth2));

        selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                buttonLabel.setText(labelOn);
            } else {
                buttonLabel.setText(labelOff);
            }
        });

        selectedProperty().setValue(false);

        add(this.buttonLabel, 0, 0);
        add(this.button, 1, 0);

        GridPane.setHgrow(this.buttonLabel, Priority.NEVER);
        GridPane.setHalignment(this.button, HPos.LEFT);
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
