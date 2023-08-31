package org.jevis.jeconfig.plugin.metersv2.cells;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisType;
import org.jevis.jeconfig.application.table.FileCell;
import org.jevis.jeconfig.application.table.JumpCell;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;


import java.util.Optional;

public class JumpColumn extends TableColumn<MeterData, Optional<JEVisSample>> {

    public JumpColumn(String s, JEVisType jeVisType, int width, JEVisDataSource ds) {
        super(s);

        this.setCellValueFactory(meterDataOptionalCellDataFeatures -> {
            return new SimpleObjectProperty<>(meterDataOptionalCellDataFeatures.getValue().getJeVisAttributeJEVisSampleMap().get(jeVisType));
        });

        this.setCellFactory(new JumpCell<>(ds));
        this.setStyle("-fx-alignment: LEFT;");
        this.setMinWidth(width);
        this.setVisible(true);
    }
}
