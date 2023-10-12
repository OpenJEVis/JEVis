package org.jevis.jeconfig.plugin.metersv2.cells;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisType;
import org.jevis.commons.DatabaseHelper;
import org.jevis.jeconfig.application.table.DateTimeColumnCell;
import org.jevis.jeconfig.application.table.ShortColumnCell;
import org.jevis.jeconfig.plugin.metersv2.data.JEVisTypeWrapper;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class DateColumn extends TableColumn<MeterData, DateTime > {
    public DateColumn(String s, JEVisType jeVisType, int width) {
        super(s);
        try {

            setId(jeVisType.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.setCellValueFactory(param -> {
            if (param.getValue().getJeVisAttributeJEVisSampleMap().get(new JEVisTypeWrapper(jeVisType)) != null && param.getValue().getJeVisAttributeJEVisSampleMap().get(new JEVisTypeWrapper(jeVisType)).getOptionalJEVisSample().isPresent()) {
                try {
                    DateTime  dateTime = DatabaseHelper.getObjectAsDate(param.getValue().getJeVisObject(), jeVisType);
                    return new SimpleObjectProperty<>(dateTime);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new SimpleObjectProperty<>();
                }
            }

            return new SimpleObjectProperty<>();
        });

        this.setCellFactory(new DateTimeColumnCell<>());
        this.setStyle("-fx-alignment: CENTER;");
        this.setMinWidth(width);

    }
}
