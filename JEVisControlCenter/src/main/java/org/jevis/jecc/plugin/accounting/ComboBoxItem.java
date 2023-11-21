package org.jevis.jecc.plugin.accounting;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jevis.api.JEVisObject;

import java.util.Objects;

public class ComboBoxItem {
    private final JEVisObject object;
    private final boolean selectable;
    private final StringProperty nameStringProperty = new SimpleStringProperty("");

    public ComboBoxItem(JEVisObject object, boolean selectable) {
        this.object = object;
        nameStringProperty.set(object.getName());
        this.selectable = selectable;
    }

    public ComboBoxItem(String name, boolean selectable) {
        this.object = null;
        nameStringProperty.set(name);
        this.selectable = selectable;
    }

    public void updateName() {
        setName(this.object.getName());
    }

    public String getName() {
        return getNameProperty().get();
    }

    public void setName(String name) {
        getNameProperty().set(name);
    }

    public StringProperty getNameProperty() {
        return nameStringProperty;
    }

    public JEVisObject getObject() {
        return object;
    }

    public boolean isSelectable() {
        return selectable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(object);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComboBoxItem that = (ComboBoxItem) o;
        return selectable == that.selectable && Objects.equals(object, that.object);
    }

    @Override
    public String toString() {
        return getName();
    }
}
