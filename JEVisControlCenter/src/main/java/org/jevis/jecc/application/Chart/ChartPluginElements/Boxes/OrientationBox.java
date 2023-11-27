package org.jevis.jecc.application.Chart.ChartPluginElements.Boxes;


import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.application.Chart.ChartSetting;
import org.jevis.jecc.application.Chart.data.ChartModel;

public class OrientationBox extends ComboBox<Orientation> {

    public OrientationBox() {
        super(FXCollections.observableArrayList(Orientation.values()));
        //TODO JFX17

        setConverter(new StringConverter<Orientation>() {
            @Override
            public String toString(Orientation object) {
                String text = "";
                switch (object) {
                    case HORIZONTAL:
                        text += I18n.getInstance().getString("plugin.graph.orientation.horizontal");
                        break;
                    case VERTICAL:
                        text += I18n.getInstance().getString("plugin.graph.orientation.vertical");
                        break;
                }
                return text;
            }

            @Override
            public Orientation fromString(String string) {
                return getItems().get(getSelectionModel().getSelectedIndex());
            }
        });
    }

    public OrientationBox(ChartSetting chartSetting) {
        this();

        this.getSelectionModel().select(chartSetting.getOrientation());

        this.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {
                chartSetting.setOrientation(newValue);
            }
        });
    }

    public OrientationBox(ChartModel chartModel) {
        this();

        this.getSelectionModel().select(chartModel.getOrientation());

        this.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {
                chartModel.setOrientation(newValue);
            }
        });
    }
}
