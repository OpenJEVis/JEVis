package org.jevis.jeconfig.plugin.Dashboard.config;

import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.Optional;

public class CustomPropertyItem implements PropertySheet.Item {


    private String key;
    private String description = "";
    private String group = "";
    private Class bean;
    private String name = "";
    private Object object;


    public CustomPropertyItem(String key, String name, String group, String description, Class bean, Object object) {
        this.key = key;
        this.description = description;
        this.group = group;
        this.bean = bean;
        this.name = name;
        this.object = object;
    }

    @Override
    public Class<?> getType() {
        return bean;
    }

    @Override
    public String getCategory() {
        return group;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Object getValue() {
        return object;
    }

    @Override
    public void setValue(Object value) {
        object = value;
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
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
