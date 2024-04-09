package org.jevis.jeconfig.plugin.object.classes;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.jevis.api.JEVisObject;

public class SelectableObject {

    private final SimpleObjectProperty<JEVisObject> object = new SimpleObjectProperty<>(this, "object", null);
    private final SimpleBooleanProperty selected = new SimpleBooleanProperty(this, "selected", true);
    private final SimpleBooleanProperty calculation = new SimpleBooleanProperty(this, "calculation", true);

    public SelectableObject(JEVisObject object, Boolean selected, Boolean calculation) {
        setObject(object);
        setSelected(selected);
        setCalculation(calculation);
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

    public boolean isCalculation() {
        return calculation.get();
    }

    public void setCalculation(boolean calculation) {
        this.calculation.set(calculation);
    }

    public SimpleBooleanProperty calculationProperty() {
        return calculation;
    }
}
