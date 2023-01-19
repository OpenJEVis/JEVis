package org.jevis.jeconfig.plugin.action.data;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

public class DataExample {


    public SimpleStringProperty vorname = new SimpleStringProperty("Vorname", "Vorname", "Florian");
    public SimpleDoubleProperty alter = new SimpleDoubleProperty("Alter", "Alter", 39.0);

    public List<SimpleDoubleProperty> samples = new ArrayList<>();

    public DataExample() {
        samples.add(new SimpleDoubleProperty(20d));
        samples.add(new SimpleDoubleProperty(21d));
    }

}
