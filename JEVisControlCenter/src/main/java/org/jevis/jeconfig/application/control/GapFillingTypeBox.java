package org.jevis.jeconfig.application.control;

import com.jfoenix.controls.JFXComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jevis.commons.constants.GapFillingType;
import org.jevis.commons.i18n.I18n;

public class GapFillingTypeBox extends JFXComboBox<GapFillingType> {
    private final ObservableList<GapFillingType> options = FXCollections.observableArrayList(GapFillingType.NONE, GapFillingType.INTERPOLATION, GapFillingType.AVERAGE,
            GapFillingType.DEFAULT_VALUE, GapFillingType.STATIC, GapFillingType.MINIMUM, GapFillingType.MAXIMUM, GapFillingType.MEDIAN, GapFillingType.DELETE);

    public GapFillingTypeBox() {
        super();

        Callback<ListView<GapFillingType>, ListCell<GapFillingType>> cellFactoryTypeBox = new Callback<javafx.scene.control.ListView<GapFillingType>, ListCell<GapFillingType>>() {
            @Override
            public ListCell<GapFillingType> call(javafx.scene.control.ListView<GapFillingType> param) {
                return new ListCell<GapFillingType>() {
                    @Override
                    protected void updateItem(GapFillingType type, boolean empty) {
                        super.updateItem(type, empty);
                        if (empty || type == null) {
                            setText("");
                        } else {
                            String text = "";
                            switch (type) {
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
                            setText(text);
                        }
                    }
                };
            }
        };
        setCellFactory(cellFactoryTypeBox);
        setButtonCell(cellFactoryTypeBox.call(null));
        setItems(options);
    }

    public ObservableList<GapFillingType> getOptions() {
        return options;
    }
}
