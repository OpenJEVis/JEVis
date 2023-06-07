package org.jevis.jecc.plugin.charts;

import javafx.beans.property.SimpleStringProperty;

public class Values {

    private final SimpleStringProperty name = new SimpleStringProperty(this, "name");
    private final SimpleStringProperty min = new SimpleStringProperty(this, "min");
    private final SimpleStringProperty max = new SimpleStringProperty(this, "max");
    private final SimpleStringProperty avg = new SimpleStringProperty(this, "avg");
    private final SimpleStringProperty sum = new SimpleStringProperty(this, "sum");

    private final SimpleStringProperty count = new SimpleStringProperty(this, "count");

    private final SimpleStringProperty zeros = new SimpleStringProperty(this, "zeros");

    public Values(String name) {
        setName(name);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public SimpleStringProperty nameProperty() {
        return name;
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

    public String getCount() {
        return count.get();
    }

    public void setCount(String count) {
        this.count.set(count);
    }

    public SimpleStringProperty countProperty() {
        return count;
    }

    public String getZeros() {
        return zeros.get();
    }

    public void setZeros(String zeros) {
        this.zeros.set(zeros);
    }

    public SimpleStringProperty zerosProperty() {
        return zeros;
    }
}
