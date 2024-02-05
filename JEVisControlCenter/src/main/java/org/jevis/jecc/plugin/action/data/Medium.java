package org.jevis.jecc.plugin.action.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import org.joda.time.DateTime;

public class Medium {
    @Expose
    @SerializedName("id")
    private final SimpleStringProperty id = new SimpleStringProperty(Long.toString(DateTime.now().getMillis()));
    @Expose
    @SerializedName("name")
    private final SimpleStringProperty name = new SimpleStringProperty("");
    @Expose
    @SerializedName("co2")
    private final SimpleDoubleProperty co2 = new SimpleDoubleProperty(0.366);

    public Medium() {
    }

    public Medium(String id, String name, double co2) {
        this.id.set(id);
        this.name.set(name);
        this.co2.set(co2);
    }

    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public SimpleStringProperty idProperty() {
        return id;
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

    public double getCo2() {
        return co2.get();
    }

    public void setCo2(double co2) {
        this.co2.set(co2);
    }

    public SimpleDoubleProperty co2Property() {
        return co2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Medium) {
            return this.getId().equals(((Medium) obj).getId());
        } else {
            return false;
        }

    }
}
