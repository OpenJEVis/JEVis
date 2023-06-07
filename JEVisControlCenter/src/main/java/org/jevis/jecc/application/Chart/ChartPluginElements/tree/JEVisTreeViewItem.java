package org.jevis.jecc.application.Chart.ChartPluginElements.tree;

import javafx.beans.property.SimpleObjectProperty;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;

public class JEVisTreeViewItem {

    private final SimpleObjectProperty<ItemType> itemType = new SimpleObjectProperty<>(this, "itemType", ItemType.OBJECT);
    private final SimpleObjectProperty<JEVisObject> object = new SimpleObjectProperty<>(this, "object", null);
    private final SimpleObjectProperty<JEVisAttribute> attribute = new SimpleObjectProperty<>(this, "attribute", null);

    public JEVisTreeViewItem(JEVisObject object) {
        this.itemType.set(ItemType.OBJECT);
        this.object.set(object);
    }

    public JEVisTreeViewItem(JEVisAttribute attribute) {
        this.itemType.set(ItemType.ATTRIBUTE);
        this.attribute.set(attribute);
    }

    public ItemType getItemType() {
        return itemType.get();
    }

    public void setItemType(ItemType itemType) {
        this.itemType.set(itemType);
    }

    public SimpleObjectProperty<ItemType> itemTypeProperty() {
        return itemType;
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

    public JEVisAttribute getAttribute() {
        return attribute.get();
    }

    public void setAttribute(JEVisAttribute attribute) {
        this.attribute.set(attribute);
    }

    public SimpleObjectProperty<JEVisAttribute> attributeProperty() {
        return attribute;
    }

    public enum ItemType {OBJECT, ATTRIBUTE}
}

