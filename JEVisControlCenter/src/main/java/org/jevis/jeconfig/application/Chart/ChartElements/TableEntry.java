/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.application.Chart.ChartElements;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;

/**
 * @author broder
 */
public class TableEntry {

    private final SimpleStringProperty date;
    private final SimpleStringProperty value;
    private final SimpleStringProperty name;
    private final SimpleObjectProperty<Color> color;
    private final SimpleStringProperty min;
    private final SimpleStringProperty max;
    private final SimpleStringProperty avg;
    private final SimpleStringProperty sum;
    private final SimpleStringProperty note;
    private final SimpleStringProperty period;

    public TableEntry(String name) {
        this.name = new SimpleStringProperty(name);
        this.date = new SimpleStringProperty("-");
        this.value = new SimpleStringProperty("-");
//        this.color = new SimpleStringProperty("-");
        this.color = new SimpleObjectProperty<>(Color.BLUE);

        this.avg = new SimpleStringProperty();
        this.min = new SimpleStringProperty();
        this.max = new SimpleStringProperty();
        this.sum = new SimpleStringProperty();
        this.note = new SimpleStringProperty();
        this.period = new SimpleStringProperty();
    }

    public String getPeriod() {
        return period.get();
    }

    public void setPeriod(String period) {
        this.period.set(period);
    }

    public SimpleStringProperty periodProperty() {
        return period;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public String getDate() {
        return date.get();
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    public String getNote() {
        return note.get();
    }

    public void setNote(String note) {
        this.note.set(note);
    }

    public SimpleStringProperty noteProperty() {
        return note;
    }

    public Color getColor() {
        return color.getValue();
    }

    public void setColor(Color name) {
        this.color.set(name);
    }

    public String getMin() {
        return min.get();
    }

    public void setMin(String min) {
        this.min.set(min);
    }

    public SimpleStringProperty minProperty() {
        return min;
    }

    public String getMax() {
        return max.get();
    }

    public void setMax(String max) {
        this.max.set(max);
    }

    public SimpleStringProperty maxProperty() {
        return max;
    }

    public String getAvg() {
        return avg.get();
    }

    public void setAvg(String avg) {
        this.avg.set(avg);
    }

    public SimpleStringProperty avgProperty() {
        return avg;
    }

    public String getSum() {
        return sum.get();
    }

    public void setSum(String sum) {
        this.sum.set(sum);
    }

    public SimpleStringProperty sumProperty() {
        return sum;
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
