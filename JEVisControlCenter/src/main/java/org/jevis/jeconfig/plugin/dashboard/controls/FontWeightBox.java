package org.jevis.jeconfig.plugin.dashboard.controls;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.jevis.commons.i18n.I18n;

public class FontWeightBox extends MFXComboBox<FontWeight> {

    public FontWeightBox() {
        super(FXCollections.observableArrayList(FontWeight.values()));

        Callback<ListView<FontWeight>, ListCell<FontWeight>> cellFactory = new Callback<ListView<FontWeight>, ListCell<FontWeight>>() {
            @Override
            public ListCell<FontWeight> call(ListView<FontWeight> param) {
                return new ListCell<FontWeight>() {
                    @Override
                    protected void updateItem(FontWeight item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            switch (item) {
                                case THIN:
                                    setText(I18n.getInstance().getString("plugin.dashboard.controls.fontweightbox.thin"));
                                    break;
                                case EXTRA_LIGHT:
                                    setText(I18n.getInstance().getString("plugin.dashboard.controls.fontweightbox.extralight"));
                                    break;
                                case LIGHT:
                                    setText(I18n.getInstance().getString("plugin.dashboard.controls.fontweightbox.light"));
                                    break;
                                case NORMAL:
                                    setText(I18n.getInstance().getString("plugin.dashboard.controls.fontweightbox.normal"));
                                    break;
                                case MEDIUM:
                                    setText(I18n.getInstance().getString("plugin.dashboard.controls.fontweightbox.medium"));
                                    break;
                                case SEMI_BOLD:
                                    setText(I18n.getInstance().getString("plugin.dashboard.controls.fontweightbox.semibold"));
                                    break;
                                case BOLD:
                                    setText(I18n.getInstance().getString("plugin.dashboard.controls.fontweightbox.bold"));
                                    break;
                                case EXTRA_BOLD:
                                    setText(I18n.getInstance().getString("plugin.dashboard.controls.fontweightbox.extrabold"));
                                    break;
                                case BLACK:
                                    setText(I18n.getInstance().getString("plugin.dashboard.controls.fontweightbox.black"));
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
        setConverter(new StringConverter<FontWeight>() {
            @Override
            public String toString(FontWeight object) {
                switch (object) {
                    case THIN:
                        return (I18n.getInstance().getString("plugin.dashboard.controls.fontweightbox.thin"));
                    case EXTRA_LIGHT:
                        return (I18n.getInstance().getString("plugin.dashboard.controls.fontweightbox.extralight"));
                    case LIGHT:
                        return (I18n.getInstance().getString("plugin.dashboard.controls.fontweightbox.light"));
                    default:
                    case NORMAL:
                        return (I18n.getInstance().getString("plugin.dashboard.controls.fontweightbox.normal"));
                    case MEDIUM:
                        return (I18n.getInstance().getString("plugin.dashboard.controls.fontweightbox.medium"));
                    case SEMI_BOLD:
                        return (I18n.getInstance().getString("plugin.dashboard.controls.fontweightbox.semibold"));
                    case BOLD:
                        return (I18n.getInstance().getString("plugin.dashboard.controls.fontweightbox.bold"));
                    case EXTRA_BOLD:
                        return (I18n.getInstance().getString("plugin.dashboard.controls.fontweightbox.extrabold"));
                    case BLACK:
                        return (I18n.getInstance().getString("plugin.dashboard.controls.fontweightbox.black"));
                }
            }

            @Override
            public FontWeight fromString(String string) {
                return getItems().get(getSelectedIndex());
            }
        });
    }

}
