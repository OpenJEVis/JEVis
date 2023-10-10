package org.jevis.jeconfig.plugin.metersv2.cells;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;


public class PathColumnColumn extends TableColumn<MeterData, String> {
    public PathColumnColumn(ObjectRelations objectRelations, int width, String name) {
        super(name);
        this.setCellValueFactory(param -> {
            return new SimpleStringProperty(objectRelations.getObjectPath(param.getValue().getJeVisObject()));
        });
        this.setCellFactory(TextFieldTableCell.forTableColumn());
        this.setPrefWidth(width);
        this.setVisible(true);
    }
}

