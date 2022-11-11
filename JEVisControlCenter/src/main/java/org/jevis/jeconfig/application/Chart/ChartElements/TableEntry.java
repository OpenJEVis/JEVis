/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.application.Chart.ChartElements;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;
import org.jevis.jeconfig.application.Chart.data.ValueWithDateTime;

/**
 * @author broder
 */
public class TableEntry {

    private final SimpleStringProperty date = new SimpleStringProperty(this, "date", "-");
    private final SimpleStringProperty value = new SimpleStringProperty(this, "value", "-");
    private final SimpleStringProperty name = new SimpleStringProperty(this, "name", "-");
    private final SimpleObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color", Color.BLUE);
    private final SimpleObjectProperty<ValueWithDateTime> min = new SimpleObjectProperty<>(this, "min", new ValueWithDateTime(0d));
    private final SimpleObjectProperty<ValueWithDateTime> max = new SimpleObjectProperty<>(this, "max", new ValueWithDateTime(0d));
    private final SimpleStringProperty avg = new SimpleStringProperty(this, "avg", "-");
    private final SimpleStringProperty enpi = new SimpleStringProperty(this, "enpi", "-");
    private final SimpleStringProperty sum = new SimpleStringProperty(this, "sum", "-");
    private final SimpleStringProperty note = new SimpleStringProperty(this, "note", "");
    private final SimpleStringProperty period = new SimpleStringProperty(this, "period", "-");
    private final SimpleStringProperty xValue = new SimpleStringProperty(this, "xValue", "-");
    private final SimpleStringProperty yValue = new SimpleStringProperty(this, "yValue", "-");
    private final SimpleStringProperty standardDeviation = new SimpleStringProperty(this, "standardDeviation", "-");
    private final SimpleStringProperty variance = new SimpleStringProperty(this, "variance", "-");

    public TableEntry(String name) {
        setName(name);
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

    public ValueWithDateTime getMin() {
        return min.get();
    }

    public void setMin(ValueWithDateTime min) {
        this.min.set(min);
    }

    public SimpleObjectProperty<ValueWithDateTime> minProperty() {
        return min;
    }

    public ValueWithDateTime getMax() {
        return max.get();
    }

    public void setMax(ValueWithDateTime max) {
        this.max.set(max);
    }

    public SimpleObjectProperty<ValueWithDateTime> maxProperty() {
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

    public String getEnpi() {
        return enpi.get();
    }

    public void setEnpi(String enpi) {
        this.enpi.set(enpi);
    }

    public SimpleStringProperty enpiProperty() {
        return enpi;
    }

    public String getxValue() {
        return xValue.get();
    }

    public void setxValue(String xValue) {
        this.xValue.set(xValue);
    }

    public SimpleStringProperty xValueProperty() {
        return xValue;
    }

    public String getyValue() {
        return yValue.get();
    }

    public void setyValue(String yValue) {
        this.yValue.set(yValue);
    }

    public SimpleStringProperty yValueProperty() {
        return yValue;
    }

    public String getStandardDeviation() {
        return standardDeviation.get();
    }

    public void setStandardDeviation(String standardDeviation) {
        this.standardDeviation.set(standardDeviation);
    }

    public SimpleStringProperty standardDeviationProperty() {
        return standardDeviation;
    }

    public String getVariance() {
        return variance.get();
    }

    public void setVariance(String variance) {
        this.variance.set(variance);
    }

    public SimpleStringProperty varianceProperty() {
        return variance;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof TableEntry && getName().equals(((TableEntry) obj).getName()));
    }
}
