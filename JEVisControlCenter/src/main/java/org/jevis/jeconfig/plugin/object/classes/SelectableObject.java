package org.jevis.jeconfig.plugin.object.classes;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.jevis.api.JEVisObject;

public class SelectableObject {

    private final SimpleObjectProperty<JEVisObject> object = new SimpleObjectProperty<>(this, "object", null);
    private final SimpleBooleanProperty selected = new SimpleBooleanProperty(this, "selected", true);

    public SelectableObject(JEVisObject object, Boolean selected) {
        setObject(object);
        setSelected(selected);
    }

    public JEVisObject getObject() {
        return object.get();
    }

    public void setObject(JEVisObject object) {
        this.object.set(object);
    }

    public SimpleObjectProperty<JEVisObject> objectProperty() {
        return object;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public SimpleBooleanProperty selectedProperty() {
        return selected;
    }
}
