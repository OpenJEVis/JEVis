package org.jevis.commons.datasource;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.joda.time.DateTime;

import java.util.Map;

public class StationData {
    private final SimpleLongProperty id = new SimpleLongProperty(this, "id", -1);

    private final SimpleStringProperty name = new SimpleStringProperty(this, "name", "");
    private final SimpleIntegerProperty column = new SimpleIntegerProperty(this, "column");

    private final SimpleMapProperty<DateTime, Map<String, String>> data = new SimpleMapProperty<>(this, "data", FXCollections.observableHashMap());

    public long getId() {
        return id.get();
    }

    public void setId(long id) {
        this.id.set(id);
    }

    public SimpleLongProperty idProperty() {
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

    public int getColumn() {
        return column.get();
    }

    public void setColumn(int column) {
        this.column.set(column);
    }

    public SimpleIntegerProperty columnProperty() {
        return column;
    }

    public ObservableMap<DateTime, Map<String, String>> getData() {
        return data.get();
    }

    public void setData(ObservableMap<DateTime, Map<String, String>> data) {
        this.data.set(data);
    }

    public SimpleMapProperty<DateTime, Map<String, String>> dataProperty() {
        return data;
    }
}
