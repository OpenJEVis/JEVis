package org.jevis.jeconfig.plugin.metersv2.cells;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;

public class ObjectNameColumn extends TableColumn<MeterData, String> {
    public ObjectNameColumn(String name, int width) {
        super(name);
        this.setCellValueFactory(meterDataStringCellDataFeatures -> {
            return new SimpleStringProperty(meterDataStringCellDataFeatures.getValue().getName());
        });
        this.setCellFactory(TextFieldTableCell.forTableColumn());
        this.setPrefWidth(width);

    }
}
