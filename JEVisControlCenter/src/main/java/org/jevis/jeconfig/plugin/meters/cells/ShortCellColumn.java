package org.jevis.jeconfig.plugin.meters.cells;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisType;
import org.jevis.jeconfig.plugin.meters.data.JEVisTypeWrapper;
import org.jevis.jeconfig.plugin.meters.data.MeterData;

public class ShortCellColumn extends TableColumn<MeterData, String> {

    public ShortCellColumn(JEVisType jeVisType, int width, String name) {
        super(name);
        try {

            setId(jeVisType.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.setCellValueFactory(param -> {
            if (param.getValue().getJeVisAttributeJEVisSampleMap().get(new JEVisTypeWrapper(jeVisType)) != null && param.getValue().getJeVisAttributeJEVisSampleMap().get(new JEVisTypeWrapper(jeVisType)).getOptionalJEVisSample().isPresent()) {
                try {
                    JEVisSample jeVisSample = param.getValue().getJeVisAttributeJEVisSampleMap().get(new JEVisTypeWrapper(jeVisType)).getOptionalJEVisSample().get();
                    return new SimpleStringProperty(jeVisSample.getValueAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                    return new SimpleStringProperty();
                }
            }

            return new SimpleStringProperty();
        });

        this.setStyle("-fx-alignment: CENTER;");
        this.setMinWidth(width);


    }


}
