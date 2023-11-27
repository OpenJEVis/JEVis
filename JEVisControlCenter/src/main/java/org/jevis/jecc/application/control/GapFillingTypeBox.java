package org.jevis.jecc.application.control;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.jevis.commons.constants.GapFillingType;
import org.jevis.commons.i18n.I18n;

public class GapFillingTypeBox extends ComboBox<GapFillingType> {
    private final ObservableList<GapFillingType> options = FXCollections.observableArrayList(GapFillingType.NONE, GapFillingType.INTERPOLATION, GapFillingType.AVERAGE,
            GapFillingType.DEFAULT_VALUE, GapFillingType.STATIC, GapFillingType.MINIMUM, GapFillingType.MAXIMUM, GapFillingType.MEDIAN, GapFillingType.DELETE);

    public GapFillingTypeBox() {
        super();

        //TODO JFX17

        setConverter(new StringConverter<GapFillingType>() {
            @Override
            public String toString(GapFillingType object) {
                String text = "";
                switch (object) {
                    case NONE:
                        text = I18n.getInstance().getString("plugin.alarm.table.translation.none");
                        break;
                    case STATIC:
                        text = I18n.getInstance().getString("graph.dialog.note.text.limit2.static");
                        break;
                    case INTERPOLATION:
                        text = I18n.getInstance().getString("graph.dialog.note.text.limit2.interpolation");
                        break;
                    case DEFAULT_VALUE:
                        text = I18n.getInstance().getString("graph.dialog.note.text.limit2.default");
                        break;
                    case MINIMUM:
                        text = I18n.getInstance().getString("graph.dialog.note.text.limit2.min");
                        break;
                    case MAXIMUM:
                        text = I18n.getInstance().getString("graph.dialog.note.text.limit2.max");
                        break;
                    case MEDIAN:
                        text = I18n.getInstance().getString("graph.dialog.note.text.limit2.median");
                        break;
                    case AVERAGE:
                        text = I18n.getInstance().getString("graph.dialog.note.text.limit2.average");
                        break;
                    case DELETE:
                        text = I18n.getInstance().getString("graph.dialog.note.text.limit2.delete");
                        break;
                }
                return text;
            }

            @Override
            public GapFillingType fromString(String string) {
                return getItems().get(getSelectionModel().getSelectedIndex());
            }
        });

        setItems(options);
    }

    public ObservableList<GapFillingType> getOptions() {
        return options;
    }
}
