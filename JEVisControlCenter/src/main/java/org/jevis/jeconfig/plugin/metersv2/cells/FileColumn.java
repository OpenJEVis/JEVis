package org.jevis.jeconfig.plugin.metersv2.cells;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisType;
import org.jevis.jeconfig.application.table.FileCell;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;

import java.util.Optional;

public class FileColumn extends TableColumn<MeterData,Optional<JEVisSample>>{
JEVisType jeVisType;
int width;

    public FileColumn(JEVisType jeVisType, int width, String name) {
        super(name);


        this.setCellValueFactory(param -> {
            Optional<JEVisSample> jeVisSample;
            if (param.getValue().getJeVisAttributeJEVisSampleMap().get(jeVisType) == null) {
                jeVisSample = Optional.empty();
            } else {
                jeVisSample = param.getValue().getJeVisAttributeJEVisSampleMap().get(jeVisType);
            }
            try {
                return new SimpleObjectProperty<Optional<JEVisSample>>(jeVisSample);
            } catch (Exception e) {
                e.printStackTrace();
                return new SimpleObjectProperty<Optional<JEVisSample>>(Optional.empty());
            }


        });

        this.setCellFactory(new FileCell<>());
        this.setStyle("-fx-alignment: LEFT;");
        this.setMinWidth(width);
        this.setVisible(true);

    }
}
