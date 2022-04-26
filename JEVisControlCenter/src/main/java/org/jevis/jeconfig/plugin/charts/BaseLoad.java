package org.jevis.jeconfig.plugin.charts;

import javafx.beans.property.SimpleStringProperty;

public class BaseLoad {

    private final SimpleStringProperty name = new SimpleStringProperty("-");
    private final SimpleStringProperty min = new SimpleStringProperty();
    private final SimpleStringProperty max = new SimpleStringProperty();
    private final SimpleStringProperty avg = new SimpleStringProperty();
    private final SimpleStringProperty sum = new SimpleStringProperty();

    public BaseLoad(String name) {
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
}
