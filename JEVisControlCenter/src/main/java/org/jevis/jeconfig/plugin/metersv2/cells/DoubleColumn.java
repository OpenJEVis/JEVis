package org.jevis.jeconfig.plugin.metersv2.cells;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisType;
import org.jevis.jeconfig.application.table.ShortColumnCell;
import org.jevis.jeconfig.plugin.metersv2.data.JEVisTypeWrapper;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;

public class DoubleColumn extends TableColumn<MeterData,String> {
    public DoubleColumn(JEVisType jeVisType, int width, String name) {
        super(name);
        this.setCellValueFactory(param -> {
            if (param.getValue().getJeVisAttributeJEVisSampleMap().get(new JEVisTypeWrapper(jeVisType)) != null && param.getValue().getJeVisAttributeJEVisSampleMap().get(new JEVisTypeWrapper(jeVisType)).isPresent()) {
                try {
                    JEVisSample jeVisSample = param.getValue().getJeVisAttributeJEVisSampleMap().get(new JEVisTypeWrapper(jeVisType)).get();
                    return new SimpleStringProperty(String.format("%.2f",jeVisSample.getValueAsDouble()));
                } catch (Exception e) {
                    e.printStackTrace();
                    return new SimpleStringProperty();
                }
            }

            return new SimpleStringProperty();
        });

        this.setCellFactory(new ShortColumnCell<MeterData>());
        //this.setStyle("-fx-alignment: LEFT;");
        this.setMinWidth(width);
        this.setVisible(false);

    }
}
