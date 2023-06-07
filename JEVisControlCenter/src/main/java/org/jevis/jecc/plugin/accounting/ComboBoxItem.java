package org.jevis.jecc.plugin.accounting;

import org.jevis.api.JEVisObject;

public class ComboBoxItem {
    private final String name;
    private final JEVisObject object;
    private final boolean selectable;

    public ComboBoxItem(JEVisObject object, boolean selectable) {
        this.object = object;
        this.name = object.getName();
        this.selectable = selectable;
    }

    public ComboBoxItem(String name, boolean selectable) {
        this.object = null;
        this.name = name;
        this.selectable = selectable;
    }


    public String getName() {
        return name;
    }

    public JEVisObject getObject() {
        return object;
    }

    public boolean isSelectable() {
        return selectable;
    }

    @Override
    public String toString() {
        return name;
    }
}
