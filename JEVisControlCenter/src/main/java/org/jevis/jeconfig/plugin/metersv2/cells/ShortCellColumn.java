package org.jevis.jeconfig.plugin.metersv2.cells;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisType;
import org.jevis.jeconfig.application.table.ShortColumnCell;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;

public class ShortCellColumn extends TableColumn<MeterData,String>{
    JEVisType jeVisType;
    int width;

    public ShortCellColumn(JEVisType jeVisType, int width, String name) {
        super(name);
        this.setCellValueFactory(param -> {
            if (param.getValue().getJeVisAttributeJEVisSampleMap().get(jeVisType) != null && param.getValue().getJeVisAttributeJEVisSampleMap().get(jeVisType).isPresent()) {
                try {
                    JEVisSample jeVisSample = param.getValue().getJeVisAttributeJEVisSampleMap().get(jeVisType).get();
                    return new SimpleStringProperty(jeVisSample.getValueAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            return null;
        });

        this.setCellFactory(new ShortColumnCell<MeterData>());
        this.setStyle("-fx-alignment: LEFT;");
        this.setMinWidth(width);
        this.setVisible(false);



    }


}
