package org.jevis.jeconfig.plugin.meters.cells;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisType;
import org.jevis.jeconfig.application.table.JumpCell;
import org.jevis.jeconfig.plugin.meters.data.JEVisTypeWrapper;
import org.jevis.jeconfig.plugin.meters.data.MeterData;
import org.jevis.jeconfig.plugin.meters.data.SampleData;

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
        this.setMinWidth(width);

    }
}
