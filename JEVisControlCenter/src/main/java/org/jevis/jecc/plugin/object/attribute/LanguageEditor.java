/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecc.plugin.object.attribute;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jecc.plugin.object.extension.GenericAttributeExtension;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * @author br
 */
public class LanguageEditor implements AttributeEditor {
    private static final Logger logger = LogManager.getLogger(LanguageEditor.class);

    private final JEVisAttribute _attribute;
    private final HBox _editor = new HBox(5);
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private JEVisSample _newSample;
    private Locale orgLoca = Locale.getDefault();


    public LanguageEditor(JEVisAttribute att) {
        _attribute = att;

        try {
            if (_attribute.getLatestSample() != null || !_attribute.getLatestSample().getValueAsString().isEmpty()) {
                orgLoca = LocaleUtils.toLocale(_attribute.getLatestSample().getValueAsString());
            }
        } catch (NullPointerException np) {
            try {
                _newSample = _attribute.buildSample(new DateTime(), orgLoca.getLanguage());
                _changed.setValue(Boolean.TRUE);
            } catch (Exception ex) {
                logger.catching(ex);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.catching(ex);
        }

        buildGUI();
    }

    /**
     * Move to a common placed because is also used elsewhere
     **/
    public static ObservableList<Locale> getEnumList() {
        List<String> list = Arrays.asList(new String[]{"de", "en", "es", "fr", "hi", "zh", "ar", "bn", "ru", "pt", "ur", "ja", "tr", "ko", "uk", "th", "it"});
        ObservableList<Locale> enumList = FXCollections.observableArrayList();
        try {
            String[] langs = Locale.getISOLanguages();
            for (String lang : langs) {
                if (list.contains(lang)) {
                    System.out.println(lang);
                    enumList.add(LocaleUtils.toLocale(lang));
                }
            }

        } catch (Exception ex) {
            logger.catching(ex);
        }
        FXCollections.sort(enumList, new Comparator<Locale>() {
            @Override
            public int compare(Locale o1, Locale o2) {
                //o1 ist kleiner als o2 = -1
                //o1=o2 = 0
                //o1 ist groeßer als o2 = 1
                return o1.getDisplayName().compareTo(o2.getDisplayName());

            }
        });
        return enumList;
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            _editor.getChildren().clear();
            buildGUI();
        });
    }

    private void buildGUI() {
        ObservableList<Locale> enumList = FXCollections.observableArrayList();
        enumList.addAll(getEnumList());

//        MFXComboBox picker = new MFXComboBox(enumList);
        MFXComboBox<Locale> picker = new MFXComboBox<>(enumList);
        picker.setFloatMode(FloatMode.DISABLED);

        //TODO JFX17
        picker.setConverter(new StringConverter<Locale>() {
            @Override
            public String toString(Locale object) {
                return object.getDisplayName();
            }

            @Override
            public Locale fromString(String string) {
                return picker.getItems().get(picker.getSelectedIndex());
            }
        });

        picker.selectItem(orgLoca);

        picker.setPrefWidth(GenericAttributeExtension.editorWidth.getValue());

        picker.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            try {
                Locale local = (Locale) newValue;
                _newSample = _attribute.buildSample(new DateTime(), local.getLanguage());
                _changed.setValue(Boolean.TRUE);
            } catch (JEVisException ex) {
                logger.fatal(ex);
            }
        });

        _editor.setPrefWidth(GenericAttributeExtension.editorWidth.getValue());
        Region spacer = new Region();
//        HBox box = new HBox();
//        HBox.setHgrow(picker, Priority.NEVER);
//        HBox.setHgrow(spacer, Priority.ALWAYS);
//        box.getChildren().addAll(spacer,picker);
        _editor.getChildren().setAll(picker);

    }

    @Override
    public boolean hasChanged() {
        return _changed.getValue();
    }

    @Override
    public void commit() throws JEVisException {
        if (_newSample != null) {
            _newSample.commit();
            _changed.setValue(false);
        }
    }

    @Override
    public Node getEditor() {
        return _editor;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }

    @Override
    public void setReadOnly(boolean canRead) {
        _editor.setDisable(canRead);
    }

    @Override
    public JEVisAttribute getAttribute() {
        return _attribute;
    }


    @Override
    public boolean isValid() {
        //TODO: implement validation
        return true;
    }
}
