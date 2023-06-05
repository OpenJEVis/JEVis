package org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.util.StringConverter;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.Chart.data.ChartModel;

public class OrientationBox extends MFXComboBox<Orientation> {

    public OrientationBox() {
        super(FXCollections.observableArrayList(Orientation.values()));
        //TODO JFX17

        setConverter(new StringConverter<Orientation>() {
            @Override
            public String toString(Orientation object) {
                String text = "";
                switch (object) {
                    case HORIZONTAL:
                        setText(I18n.getInstance().getString("plugin.graph.orientation.horizontal"));
                        break;
                    case VERTICAL:
                        setText(I18n.getInstance().getString("plugin.graph.orientation.vertical"));
                        break;
                }
                return text;
            }

            @Override
            public Orientation fromString(String string) {
                return getItems().get(getSelectedIndex());
            }
        });
    }

    public OrientationBox(ChartSetting chartSetting) {
        this();

        this.selectItem(chartSetting.getOrientation());

        this.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {
                chartSetting.setOrientation(newValue);
            }
        });
    }

    public OrientationBox(ChartModel chartModel) {
        this();

        this.selectItem(chartModel.getOrientation());

        this.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {
                chartModel.setOrientation(newValue);
            }
        });
    }
}
