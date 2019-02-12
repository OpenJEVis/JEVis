package org.jevis.jeconfig.plugin.Dashboard.config;

import javafx.beans.value.WritableValue;

public class WidgetConfigProperty<T> {

    private final String name;
    private final String id;
    private final String category;
    private final String description;
    private final WritableValue writableValue;
    private T t;

    public WidgetConfigProperty(String id, String category, String name, String description, WritableValue writableValue) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.description = description;
        this.writableValue = writableValue;
    }

    public T get() {
        return this.t;
    }

    public void set(T t1) {
        this.t = t1;
    }

    public String getCategory() {
        return category;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public WritableValue getWritableValue() {
        return writableValue;
    }

    public String getDescription() {
        return description;
    }
}
