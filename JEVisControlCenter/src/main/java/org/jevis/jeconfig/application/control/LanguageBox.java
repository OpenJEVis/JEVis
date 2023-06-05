package org.jevis.jeconfig.application.control;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LanguageBox extends MFXComboBox<Locale> {

    public LanguageBox() {
        init();
    }

    private void init() {

        List<Locale> availableLanguages = Arrays.asList(Locale.getAvailableLocales());
        ObservableList<Locale> options = FXCollections.observableArrayList(availableLanguages);

        final MFXComboBox<Locale> comboBox = new MFXComboBox<Locale>(options);

        //TODO JFX17
        comboBox.setConverter(new StringConverter<Locale>() {
            @Override
            public String toString(Locale object) {
                String text = "";
                if (object != null) {
                    try {
                        text = object.getDisplayLanguage();

                    } catch (Exception ignored) {

                    }
                }

                return text;
            }

            @Override
            public Locale fromString(String string) {
                return null;
            }
        });

        if (availableLanguages.contains(Locale.getDefault())) {
            comboBox.selectItem(Locale.getDefault());
        }
    }
}
