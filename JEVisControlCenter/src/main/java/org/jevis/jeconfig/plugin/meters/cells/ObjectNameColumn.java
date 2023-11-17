package org.jevis.jeconfig.plugin.meters.cells;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import org.jevis.jeconfig.plugin.meters.data.MeterData;

public class ObjectNameColumn extends TableColumn<MeterData, String> {
    public ObjectNameColumn(String name, int width) {
        super(name);
        this.setCellValueFactory(meterDataStringCellDataFeatures -> {
            return new SimpleStringProperty(meterDataStringCellDataFeatures.getValue().getName());
        });
        this.setCellFactory(TextFieldTableCell.forTableColumn());
        this.setPrefWidth(width);
        this.setStyle("-fx-alignment: CENTER-LEFT;");

    }
}
