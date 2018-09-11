package org.jevis.jeconfig.plugin.scada.data;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.Map;
import java.util.Optional;

public class ConfigSheet {

    public ConfigSheet() {
    }


    public PropertySheet getSheet(Map<String, Property> properties) {
        ObservableList<PropertySheet.Item> list = FXCollections.observableArrayList();
        properties.forEach((key, propertyConfig) -> {
            list.add(new CustomPropertyItem(propertyConfig));
        });


        PropertySheet propertySheet = new PropertySheet(list);
        propertySheet.setMode(PropertySheet.Mode.CATEGORY);
//        propertySheet.setPrefHeight(500);
        propertySheet.setSearchBoxVisible(false);
        propertySheet.setModeSwitcherVisible(false);


        return propertySheet;
    }

    public static class Property {
        private String name;
        private String category;
        private Object object;
        private String description;

        public Property(String name, String category, Object object, String description) {
            this.name = name;
            this.category = category;
            this.object = object;
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }
    }

    class CustomPropertyItem implements PropertySheet.Item {
        private Property config;

        public CustomPropertyItem(Property config) {
            this.config = config;
        }

        @Override
        public Class<?> getType() {
            return config.getObject().getClass();
        }

        @Override
        public String getCategory() {
            return config.getCategory();
        }

        @Override
        public String getName() {
            return config.getName();
        }

        @Override
        public String getDescription() {
            return config.getDescription();
        }

        @Override
        public Object getValue() {
            return config.getObject();
        }

        @Override
        public void setValue(Object value) {
            config.setObject(value);
        }

        @Override
        public Optional<ObservableValue<? extends Object>> getObservableValue() {
//            ObservableValue ovalue = new SimpleStringProperty(getValue().toString());
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
