package org.jevis.jeconfig.plugin.metersv2.cells;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TableColumn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisType;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.plugin.metersv2.data.JEVisTypeWrapper;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;
import org.jevis.jeconfig.plugin.metersv2.event.PrecisionEventHandler;

import java.util.Optional;

public class LastRawValue extends TableColumn<MeterData, LastRawValuePojo> {

    private static final Logger logger = LogManager.getLogger(LastRawValue.class);

    private final JEVisTypeWrapper jeVisTypeWrapperDecimalPlaces;

    private final PrecisionEventHandler precisionEventHandler;

    public LastRawValue(String s, JEVisDataSource ds, JEVisType jeVisType, int width, JEVisTypeWrapper jeVisTypeWrapperDecimalPlaces, PrecisionEventHandler precisionEventHandler) {
        super(s);

        this.jeVisTypeWrapperDecimalPlaces = jeVisTypeWrapperDecimalPlaces;
        this.precisionEventHandler = precisionEventHandler;
        try {

            setId(jeVisType.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.setCellValueFactory(meterDataJEVisSampleCellDataFeatures -> {
            long precsion = getPrecsion(meterDataJEVisSampleCellDataFeatures.getValue());

            double value = 0.0;
            String unit = "";
            try {
                TargetHelper th = new TargetHelper(ds, meterDataJEVisSampleCellDataFeatures.getValue().getJeVisObject().getAttribute(jeVisType));
                value = th.getAttribute().get(0).getLatestSample().getValueAsDouble();
                unit = th.getAttribute().get(0).getLatestSample().getUnit().getLabel();
                StringProperty stringProperty = new SimpleStringProperty();
                setStringProperty(stringProperty, value, precsion, unit);

            } catch (Exception e) {
                logger.error(e);
            }
            LastRawValuePojo lastRawValuePojo = new LastRawValuePojo(meterDataJEVisSampleCellDataFeatures.getValue(),value,unit,(int)precsion);


            return new SimpleObjectProperty<LastRawValuePojo>(lastRawValuePojo);
        });

        this.setCellFactory(new LastRawValueCell<>(precisionEventHandler));
        this.setMinWidth(width);


    }

    private void setStringProperty(StringProperty stringProperty, double value, long precision, String unitLabel) {
        stringProperty.set(String.format("%.0" + precision + "f", value) + " " + unitLabel);
    }

    private int getPrecsion(MeterData meterData) {
        try {
            if (meterData.getJeVisAttributeJEVisSampleMap().get(jeVisTypeWrapperDecimalPlaces) == null) return 2;
            Optional<JEVisSample> optionalJEVisSample = (meterData.getJeVisAttributeJEVisSampleMap().get(jeVisTypeWrapperDecimalPlaces).getOptionalJEVisSample());
            if (!optionalJEVisSample.isPresent()) return 2;
            JEVisSample jeVisSample = optionalJEVisSample.get();
            return Math.toIntExact(jeVisSample.getValueAsLong());
        } catch (Exception e) {
            logger.error(e);
            return 2;
        }


    }
}