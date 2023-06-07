package org.jevis.jecc.application.Chart.ChartPluginElements.Boxes;

import eu.hansolo.fx.charts.tools.ColorMapping;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.application.Chart.ChartSetting;
import org.jevis.jecc.application.Chart.data.ChartModel;

public class ColorMappingBox extends MFXComboBox<ColorMapping> {
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

        //TODO JFX17

        setConverter(new StringConverter<ColorMapping>() {
            @Override
            public String toString(ColorMapping object) {
                switch (object) {
                    default:
                    case LIME_YELLOW_RED:
                        return (I18n.getInstance().getString("plugin.graph.colormapping.limeyellowred"));
                    case BLUE_CYAN_GREEN_YELLOW_RED:
                        return (I18n.getInstance().getString("plugin.graph.colormapping.bluecyangreenyellowred"));
                    case INFRARED_1:
                        return (I18n.getInstance().getString("plugin.graph.colormapping.infrared1"));
                    case INFRARED_2:
                        return (I18n.getInstance().getString("plugin.graph.colormapping.infrared2"));
                    case INFRARED_3:
                        return (I18n.getInstance().getString("plugin.graph.colormapping.infrared3"));
                    case INFRARED_4:
                        return (I18n.getInstance().getString("plugin.graph.colormapping.infrared4"));
                    case BLUE_GREEN_RED:
                        return (I18n.getInstance().getString("plugin.graph.colormapping.bluegreenred"));
                    case BLUE_BLACK_RED:
                        return (I18n.getInstance().getString("plugin.graph.colormapping.blueblackred"));
                    case BLUE_YELLOW_RED:
                        return (I18n.getInstance().getString("plugin.graph.colormapping.blueyellowred"));
                    case BLUE_TRANSPARENT_RED:
                        return (I18n.getInstance().getString("plugin.graph.colormapping.bluetransparentred"));
                    case GREEN_BLACK_RED:
                        return (I18n.getInstance().getString("plugin.graph.colormapping.greenblackred"));
                    case GREEN_YELLOW_RED:
                        return (I18n.getInstance().getString("plugin.graph.colormapping.greenyellowred"));
                    case RAINBOW:
                        return (I18n.getInstance().getString("plugin.graph.colormapping.rainbow"));
                    case BLACK_WHITE:
                        return (I18n.getInstance().getString("plugin.graph.colormapping.blackwhite"));
                    case WHITE_BLACK:
                        return (I18n.getInstance().getString("plugin.graph.colormapping.whiteblack"));
                }
            }

            @Override
            public ColorMapping fromString(String string) {
                return null;
            }
        });
    }

    public ColorMappingBox(ChartSetting chartSetting) {
        this();

        this.selectItem(chartSetting.getColorMapping());

        this.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {
                chartSetting.setColorMapping(newValue);
            }
        });
    }

    public ColorMappingBox(ChartModel chartModel) {
        this();

        this.selectItem(chartModel.getColorMapping());

        this.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {
                chartModel.setColorMapping(newValue);
            }
        });
    }

}
