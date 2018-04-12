/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.application.jevistree.plugin;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;

/**
 *
 * @author broder
 */
public class TableEntry {

    private final SimpleStringProperty date;
    private final SimpleStringProperty value;
    private final SimpleStringProperty name;
    private final SimpleObjectProperty<Color> color;

    public TableEntry(String name) {
        this.name = new SimpleStringProperty(name);
        this.date = new SimpleStringProperty("-");
        this.value = new SimpleStringProperty("-");
//        this.color = new SimpleStringProperty("-");
        this.color = new SimpleObjectProperty<>(Color.BLUE);
    }

    public String getName() {
        return name.get();
    }

    public String getValue() {
        return value.get();
    }

    public String getDate() {
        return date.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public Color getColor() {
        return color.getValue();
    }

    public void setColor(Color name) {
        this.color.set(name);
    }

    public SimpleStringProperty dateProperty() {
        return date;
    }

    public SimpleStringProperty valueProperty() {
        return value;
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleObjectProperty<Color> colorProperty() {
        return color;
    }
}
