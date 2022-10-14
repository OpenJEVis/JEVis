package org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes;

import com.jfoenix.controls.JFXComboBox;
import eu.hansolo.fx.charts.tools.ColorMapping;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.Chart.data.ChartModel;

public class ColorMappingBox extends JFXComboBox<ColorMapping> {
    public ColorMappingBox() {
        super(FXCollections.observableArrayList(ColorMapping.values()));

        Callback<ListView<ColorMapping>, ListCell<ColorMapping>> callback = new Callback<ListView<ColorMapping>, ListCell<ColorMapping>>() {
            @Override
            public ListCell<ColorMapping> call(ListView<ColorMapping> param) {

                return new ListCell<ColorMapping>() {
                    @Override
                    protected void updateItem(ColorMapping item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(null);
                        setGraphic(null);

                        if (item != null && !empty) {
                            switch (item) {
                                case LIME_YELLOW_RED:
                                    setText(I18n.getInstance().getString("plugin.graph.colormapping.limeyellowred"));
                                    break;
                                case BLUE_CYAN_GREEN_YELLOW_RED:
                                    setText(I18n.getInstance().getString("plugin.graph.colormapping.bluecyangreenyellowred"));
                                    break;
                                case INFRARED_1:
                                    setText(I18n.getInstance().getString("plugin.graph.colormapping.infrared1"));
                                    break;
                                case INFRARED_2:
                                    setText(I18n.getInstance().getString("plugin.graph.colormapping.infrared2"));
                                    break;
                                case INFRARED_3:
                                    setText(I18n.getInstance().getString("plugin.graph.colormapping.infrared3"));
                                    break;
                                case INFRARED_4:
                                    setText(I18n.getInstance().getString("plugin.graph.colormapping.infrared4"));
                                    break;
                                case BLUE_GREEN_RED:
                                    setText(I18n.getInstance().getString("plugin.graph.colormapping.bluegreenred"));
                                    break;
                                case BLUE_BLACK_RED:
                                    setText(I18n.getInstance().getString("plugin.graph.colormapping.blueblackred"));
                                    break;
                                case BLUE_YELLOW_RED:
                                    setText(I18n.getInstance().getString("plugin.graph.colormapping.blueyellowred"));
                                    break;
                                case BLUE_TRANSPARENT_RED:
                                    setText(I18n.getInstance().getString("plugin.graph.colormapping.bluetransparentred"));
                                    break;
                                case GREEN_BLACK_RED:
                                    setText(I18n.getInstance().getString("plugin.graph.colormapping.greenblackred"));
                                    break;
                                case GREEN_YELLOW_RED:
                                    setText(I18n.getInstance().getString("plugin.graph.colormapping.greenyellowred"));
                                    break;
                                case RAINBOW:
                                    setText(I18n.getInstance().getString("plugin.graph.colormapping.rainbow"));
                                    break;
                                case BLACK_WHITE:
                                    setText(I18n.getInstance().getString("plugin.graph.colormapping.blackwhite"));
                                    break;
                                case WHITE_BLACK:
                                    setText(I18n.getInstance().getString("plugin.graph.colormapping.whiteblack"));
                                    break;
                            }
                        }
                    }
                };
            }
        };

        setCellFactory(callback);
        setButtonCell(callback.call(null));
    }

    public ColorMappingBox(ChartSetting chartSetting) {
        this();

        this.getSelectionModel().select(chartSetting.getColorMapping());

        this.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {
                chartSetting.setColorMapping(newValue);
            }
        });
    }

    public ColorMappingBox(ChartModel chartModel) {
        this();

        this.getSelectionModel().select(chartModel.getColorMapping());

        this.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {
                chartModel.setColorMapping(newValue);
            }
        });
    }

}
