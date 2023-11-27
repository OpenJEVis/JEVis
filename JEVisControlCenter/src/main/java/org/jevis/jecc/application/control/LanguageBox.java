package org.jevis.jecc.application.control;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.jevis.commons.i18n.I18n;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LanguageBox extends ComboBox<Locale> {

    public LanguageBox() {
        init();
    }

    private void init() {

        List<Locale> availableLanguages = Arrays.asList(Locale.getAvailableLocales());
        ObservableList<Locale> options = FXCollections.observableArrayList(availableLanguages);

        final ComboBox<Locale> comboBox = new ComboBox<Locale>(options);

        //TODO JFX17
        comboBox.setConverter(new StringConverter<Locale>() {
            @Override
            public String toString(Locale object) {
                String text = "";
                if (object != null) {
                    try {
                        text = object.getDisplayLanguage(I18n.getInstance().getLocale());
                    } catch (Exception ignored) {

                    }
                }

                return text;
            }

            @Override
            public Locale fromString(String string) {
                return comboBox.getItems().stream().filter(locale -> locale.getDisplayLanguage(I18n.getInstance().getLocale()).equals(string)).findFirst().orElse(null);
            }
        });

        if (availableLanguages.contains(Locale.getDefault())) {
            comboBox.getSelectionModel().select(Locale.getDefault());
        }
    }
}
