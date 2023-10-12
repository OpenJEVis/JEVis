package org.jevis.jeconfig.plugin.metersv2.cells;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import org.jevis.api.JEVisException;
import org.jevis.jeconfig.application.table.ShortColumnCell;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;

public class MediumColumn extends TableColumn<MeterData, String> {
    private int width;

    public MediumColumn(String s, int width) {
        super(s);
        setId(s);


        this.setCellFactory(new ShortColumnCell<>());
        this.setCellValueFactory(meterDataStringCellDataFeatures -> {
            try {
                return new SimpleStringProperty(meterDataStringCellDataFeatures.getValue().getJeVisClass().getName());
            } catch (JEVisException e) {
                e.printStackTrace();
            }
            return new SimpleStringProperty("N/A");
        });
        this.setMinWidth(width);

    }
}
