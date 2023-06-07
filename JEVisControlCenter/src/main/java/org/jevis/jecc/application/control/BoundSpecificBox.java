package org.jevis.jecc.application.control;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import org.jevis.commons.constants.GapFillingBoundToSpecific;
import org.jevis.commons.i18n.I18n;

public class BoundSpecificBox extends MFXComboBox<GapFillingBoundToSpecific> {
    private final ObservableList<GapFillingBoundToSpecific> options = FXCollections.observableArrayList(GapFillingBoundToSpecific.NONE, GapFillingBoundToSpecific.WEEKDAY,
            GapFillingBoundToSpecific.WEEKOFYEAR, GapFillingBoundToSpecific.MONTHOFYEAR);

    public BoundSpecificBox() {
        super();

        //TODO JFX17
        setConverter(new StringConverter<GapFillingBoundToSpecific>() {
            @Override
            public String toString(GapFillingBoundToSpecific object) {

                String text = "";
                switch (object) {
                    default:
                    case NONE:
                        text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.boundtospecific.none");
                        break;
                    case WEEKDAY:
                        text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.boundtospecific.weekday");
                        break;
                    case WEEKOFYEAR:
                        text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.boundtospecific.weekofyear");
                        break;
                    case MONTHOFYEAR:
                        text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.boundtospecific.monthofyear");
                        break;
                }

                return text;
            }

            @Override
            public GapFillingBoundToSpecific fromString(String string) {
                return getItems().get(getSelectedIndex());
            }
        });

        setItems(options);
    }

    public ObservableList<GapFillingBoundToSpecific> getOptions() {
        return options;
    }
}
