package org.jevis.jecc.plugin.meters.cells;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.plugin.meters.data.MeterData;

public class MediumColumn extends TableColumn<MeterData, String> {
    private final JEVisDataSource dataSource;
    private int width;


    public MediumColumn(String s, int width, JEVisDataSource dataSource) {
        super(s);
        this.dataSource = dataSource;
        setId(s);

        this.setCellValueFactory(meterDataStringCellDataFeatures -> {
            return new SimpleStringProperty(getMedium(meterDataStringCellDataFeatures.getValue().getJeVisClass()));
        });
        this.setMinWidth(width);
        this.setStyle("-fx-alignment: CENTER-LEFT;");

    }

    private String getMedium(JEVisClass jeVisClass) {
        try {
            JEVisClass air = dataSource.getJEVisClass("Air Measurement Instrument");
            JEVisClass compressedAir = dataSource.getJEVisClass("Compressed-Air Measurement Instrument");
            JEVisClass electricity = dataSource.getJEVisClass("Electricity Measurement Instrument");
            JEVisClass gas = dataSource.getJEVisClass("Gas Measurement Instrument");
            JEVisClass heat = dataSource.getJEVisClass("Heat Measurement Instrument");
            JEVisClass nitrogen = dataSource.getJEVisClass("Nitrogen Measurement Instrument");
            JEVisClass water = dataSource.getJEVisClass("Water Measurement Instrument");


            if (jeVisClass.equals(air)) {
                return I18n.getInstance().getString("plugin.meters.medium.air");
            } else if (jeVisClass.equals(compressedAir)) {
                return I18n.getInstance().getString("plugin.meters.medium.compressedair");
            } else if (jeVisClass.equals(electricity)) {
                return I18n.getInstance().getString("plugin.meters.medium.electricity");
            } else if (jeVisClass.equals(gas)) {
                return I18n.getInstance().getString("plugin.meters.medium.gas");
            } else if (jeVisClass.equals(heat)) {
                return I18n.getInstance().getString("plugin.meters.medium.heat");
            } else if (jeVisClass.equals(nitrogen)) {
                return I18n.getInstance().getString("plugin.meters.medium.nitrogen");
            } else if (jeVisClass.equals(water)) {
                return I18n.getInstance().getString("plugin.meters.medium.water");
            } else return "N/A";


        } catch (Exception e) {

            return "N/A";
        }

    }
}
