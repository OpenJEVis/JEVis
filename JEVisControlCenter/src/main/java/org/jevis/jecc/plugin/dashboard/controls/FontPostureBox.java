package org.jevis.jecc.plugin.dashboard.controls;


import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.FontPosture;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.jevis.commons.i18n.I18n;

public class FontPostureBox extends ComboBox<FontPosture> {

    public FontPostureBox() {
        super(FXCollections.observableArrayList(FontPosture.values()));

        Callback<ListView<FontPosture>, ListCell<FontPosture>> cellFactory = new Callback<ListView<FontPosture>, ListCell<FontPosture>>() {
            @Override
            public ListCell<FontPosture> call(ListView<FontPosture> param) {
                return new ListCell<FontPosture>() {
                    @Override
                    protected void updateItem(FontPosture item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            switch (item) {
                                case REGULAR:
                                    setText(I18n.getInstance().getString("plugin.dashboard.controls.fontposturebox.regular"));
                                    break;
                                case ITALIC:
                                    setText(I18n.getInstance().getString("plugin.dashboard.controls.fontposturebox.italic"));
                                    break;
                            }
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        };

        //TODO JFX17

        setConverter(new StringConverter<FontPosture>() {
            @Override
            public String toString(FontPosture object) {
                switch (object) {
                    default:
                    case REGULAR:
                        return (I18n.getInstance().getString("plugin.dashboard.controls.fontposturebox.regular"));
                    case ITALIC:
                        return (I18n.getInstance().getString("plugin.dashboard.controls.fontposturebox.italic"));
                }
            }

            @Override
            public FontPosture fromString(String string) {
                return getItems().get(getSelectionModel().getSelectedIndex());
            }
        });
    }
}
