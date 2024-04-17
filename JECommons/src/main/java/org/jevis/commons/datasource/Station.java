package org.jevis.commons.datasource;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.jevis.api.JEVisFile;
import org.jevis.commons.driver.dwd.Attribute;
import org.joda.time.DateTime;

import java.util.List;

public class Station {

    private final SimpleLongProperty id = new SimpleLongProperty(this, "id", -1);
    private final SimpleStringProperty name = new SimpleStringProperty(this, "name", "");
    private final SimpleObjectProperty<DateTime> from = new SimpleObjectProperty<>(this, "from");
    private final SimpleObjectProperty<DateTime> to = new SimpleObjectProperty<>(this, "to");
    private final SimpleLongProperty height = new SimpleLongProperty(this, "height", 0);
    private final SimpleDoubleProperty geoWidth = new SimpleDoubleProperty(this, "geoWidth");
    private final SimpleDoubleProperty geoHeight = new SimpleDoubleProperty(this, "geoHeight");
    private final SimpleStringProperty state = new SimpleStringProperty(this, "state", "");
    private final SimpleMapProperty<Attribute, List<String>> intervalPath = new SimpleMapProperty<>(this, "intervalPath", FXCollections.observableHashMap());
    private final SimpleMapProperty<Attribute, StationData> stationData = new SimpleMapProperty<>(this, "stationData", FXCollections.observableHashMap());
    private JEVisFile descriptionFile;

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

    public DateTime getFrom() {
        return from.get();
    }

    public void setFrom(DateTime from) {
        this.from.set(from);
    }

    public SimpleObjectProperty<DateTime> fromProperty() {
        return from;
    }

    public DateTime getTo() {
        return to.get();
    }

    public void setTo(DateTime to) {
        this.to.set(to);
    }

    public SimpleObjectProperty<DateTime> toProperty() {
        return to;
    }

    public long getHeight() {
        return height.get();
    }

    public void setHeight(long height) {
        this.height.set(height);
    }

    public SimpleLongProperty heightProperty() {
        return height;
    }

    public double getGeoWidth() {
        return geoWidth.get();
    }

    public void setGeoWidth(double geoWidth) {
        this.geoWidth.set(geoWidth);
    }

    public SimpleDoubleProperty geoWidthProperty() {
        return geoWidth;
    }

    public double getGeoHeight() {
        return geoHeight.get();
    }

    public void setGeoHeight(double geoHeight) {
        this.geoHeight.set(geoHeight);
    }

    public SimpleDoubleProperty geoHeightProperty() {
        return geoHeight;
    }

    public String getState() {
        return state.get();
    }

    public void setState(String state) {
        this.state.set(state);
    }

    public SimpleStringProperty stateProperty() {
        return state;
    }

    public ObservableMap<Attribute, List<String>> getIntervalPath() {
        return intervalPath.get();
    }

    public void setIntervalPath(SimpleMapProperty<Attribute, List<String>> intervalPath) {
        this.intervalPath.set(intervalPath);
    }

    public SimpleMapProperty<Attribute, List<String>> intervalPathProperty() {
        return intervalPath;
    }

    public ObservableMap<Attribute, StationData> getStationData() {
        return stationData.get();
    }

    public void setStationData(ObservableMap<Attribute, StationData> stationData) {
        this.stationData.set(stationData);
    }

    public SimpleMapProperty<Attribute, StationData> stationDataProperty() {
        return stationData;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Station) {
            Station otherStation = (Station) obj;
            return getId() == otherStation.getId();
        }

        return false;
    }

    public JEVisFile getDescriptionFile() {
        return descriptionFile;
    }

    public void setDescriptionFile(JEVisFile descriptionFile) {
        this.descriptionFile = descriptionFile;
    }
}
