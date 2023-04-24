package org.jevis.jeconfig.plugin.action.ui;

import javafx.beans.property.SimpleBooleanProperty;

public class CheckBoxData {
    private String text = "";
    private SimpleBooleanProperty isSelected = new SimpleBooleanProperty(false);

    public CheckBoxData(String text, boolean isSelected) {
        this.text = text;
        this.isSelected.set(isSelected);
    }

    public String text() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isIsSelected() {
        return isSelected.get();
    }

    public SimpleBooleanProperty isSelectedProperty() {
        return isSelected;
    }

}
