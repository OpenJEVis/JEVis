package org.jevis.jeconfig.plugin.dashboard.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class UserConfigConfig {

    public static Map<String, Object> customDataMap = new LinkedHashMap<>();

    public UserConfigConfig(Map<String, Object> map) {
        this.customDataMap = map;
    }


    public PropertySheet getSheet() {
        ObservableList<PropertySheet.Item> list = FXCollections.observableArrayList();
        for (String key : customDataMap.keySet())
            list.add(new CustomPropertyItem(key));

        PropertySheet propertySheet = new PropertySheet(list);
        propertySheet.setMode(PropertySheet.Mode.CATEGORY);
        propertySheet.setPrefHeight(500);

        return propertySheet;
    }

    class CustomPropertyItem implements PropertySheet.Item {
        private String key;
        private String category, name;

        public CustomPropertyItem(String key) {
            this.key = key;
            String[] skey = key.split("#");
            category = skey[0];
            name = skey[1];
        }

        @Override
        public Class<?> getType() {
            return customDataMap.get(key).getClass();
        }

        @Override
        public String getCategory() {
            return category;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public Object getValue() {
            return customDataMap.get(key);
        }

        @Override
        public void setValue(Object value) {
            customDataMap.put(key, value);
        }

        @Override
        public Optional<ObservableValue<? extends Object>> getObservableValue() {
            ObservableValue ovalue = new SimpleStringProperty(getValue().toString());
            return Optional.empty();
        }

        @Override
        public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
            return Optional.empty();
        }

        @Override
        public boolean isEditable() {
            return true;
        }
    }
}
