package org.jevis.jecc.plugin.meters.cells;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisType;
import org.jevis.jecc.application.table.FileCell;
import org.jevis.jecc.plugin.meters.data.JEVisTypeWrapper;
import org.jevis.jecc.plugin.meters.data.MeterData;

import java.util.Optional;

public class FileColumn extends TableColumn<MeterData, Optional<JEVisSample>> {
    JEVisType jeVisType;
    int width;

    public FileColumn(JEVisType jeVisType, int width, String name) {
        super(name);
        try {

            setId(jeVisType.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }


        this.setCellValueFactory(param -> {
            Optional<JEVisSample> jeVisSample;
            if (param.getValue().getJeVisAttributeJEVisSampleMap().get(new JEVisTypeWrapper(jeVisType)) == null) {
                jeVisSample = Optional.empty();
            } else {
                jeVisSample = param.getValue().getJeVisAttributeJEVisSampleMap().get(new JEVisTypeWrapper(jeVisType)).getOptionalJEVisSample();
            }
            try {
                return new SimpleObjectProperty<Optional<JEVisSample>>(jeVisSample);
            } catch (Exception e) {
                e.printStackTrace();
                return new SimpleObjectProperty<Optional<JEVisSample>>(Optional.empty());
            }


        });

        this.setCellFactory(new FileCell<>());
        this.setStyle("-fx-alignment: CENTER;");
        this.setMinWidth(width);


    }
}
