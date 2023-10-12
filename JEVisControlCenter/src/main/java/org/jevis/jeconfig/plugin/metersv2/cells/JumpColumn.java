package org.jevis.jeconfig.plugin.metersv2.cells;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisType;
import org.jevis.jeconfig.application.table.FileCell;
import org.jevis.jeconfig.application.table.JumpCell;
import org.jevis.jeconfig.plugin.metersv2.data.JEVisTypeWrapper;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;
import org.jevis.jeconfig.plugin.metersv2.data.SampleData;


import java.util.Optional;

public class JumpColumn extends TableColumn<MeterData, SampleData> {

    public JumpColumn(String s, JEVisType jeVisType, int width, JEVisDataSource ds) {
        super(s);
        try {

            setId(jeVisType.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.setCellValueFactory(meterDataOptionalCellDataFeatures -> {
            return new SimpleObjectProperty<>(meterDataOptionalCellDataFeatures.getValue().getJeVisAttributeJEVisSampleMap().get(new JEVisTypeWrapper(jeVisType)));
        });

        this.setCellFactory(new JumpCell<>(ds));
        //this.setStyle("-fx-alignment: LEFT;");
        this.setMinWidth(width);

    }
}
