package org.jevis.jeconfig.plugin.metersv2.cells;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TableColumn;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisType;
import org.jevis.commons.driver.DataSource;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.application.table.ShortColumnCell;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;
import org.jevis.jeconfig.tool.DragResizeMod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class LastRawValue extends TableColumn<MeterData, String> {

    public LastRawValue(String s, JEVisDataSource ds, JEVisType jeVisType, int width, IntegerProperty preciosnProperty) {

        super(s);
        try {

            setId(jeVisType.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.setCellValueFactory(meterDataJEVisSampleCellDataFeatures -> {

            try {
                TargetHelper th = new TargetHelper(ds, meterDataJEVisSampleCellDataFeatures.getValue().getJeVisObject().getAttribute(jeVisType));
                if (th.getAttribute().size() == 0) return new SimpleStringProperty("");
                if (!th.getAttribute().get(0).hasSample()) return new SimpleStringProperty("");

                double value = th.getAttribute().get(0).getLatestSample().getValueAsDouble();
                String unit = th.getAttribute().get(0).getLatestSample().getUnit().getLabel();
                StringProperty stringProperty = new SimpleStringProperty();
                setStringProperty(stringProperty, value, preciosnProperty.get(), unit);
                preciosnProperty.addListener((observableValue, number, t1) -> {
                    setStringProperty(stringProperty, value, preciosnProperty.get(), unit);

                });

                return stringProperty;
            } catch (Exception e) {

            }


            return new SimpleStringProperty("");
        });

        this.setCellFactory(new ShortColumnCell<MeterData>());
        //this.setStyle("-fx-alignment: LEFT;");
        this.setMinWidth(width);
        this.setVisible(true);


    }

    private void setStringProperty(StringProperty stringProperty, double value, int precision, String unitLabel) {
//
//        BigDecimal bd = BigDecimal.valueOf(value);
//        bd = bd.setScale(precision, RoundingMode.HALF_UP);
//        stringProperty.set(bd.doubleValue() + " " + unitLabel);

        stringProperty.set(String.format("%.0"+precision+"f",value)+" "+unitLabel);
    }
}
