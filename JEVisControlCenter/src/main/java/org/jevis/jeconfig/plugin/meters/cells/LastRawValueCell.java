package org.jevis.jeconfig.plugin.meters.cells;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.plugin.meters.event.PrecisionEvent;
import org.jevis.jeconfig.plugin.meters.event.PrecisionEventHandler;

public class LastRawValueCell<T> implements Callback<TableColumn<T, LastRawValuePojo>, TableCell<T, LastRawValuePojo>> {
    private static final Logger logger = LogManager.getLogger(LastRawValueCell.class);

    private final PrecisionEventHandler precisionEventHandler;

    public LastRawValueCell(PrecisionEventHandler precisionEventHandler) {
        this.precisionEventHandler = precisionEventHandler;
    }


    @Override
    public TableCell<T, LastRawValuePojo> call(TableColumn<T, LastRawValuePojo> tMeterDataTableColumn) {

        TableCell<T, LastRawValuePojo> tableCell = new TableCell<T, LastRawValuePojo>() {
            @Override
            protected void updateItem(LastRawValuePojo item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    logger.debug(item);
                    String content = getString(item.getValue(), item.getPrecision(), item.getUnitLabel());
                    setText(content);

                    precisionEventHandler.addEventListener(event -> {
                        PrecisionEvent precisionEvent = event;
                        if (precisionEvent.getSource().equals(item.getMeterData())) {
                            if (precisionEvent.getType().equals(PrecisionEvent.TYPE.INCREASE)) {
                                item.setPrecision(item.getPrecision() + 1);
                            } else if (precisionEvent.getType().equals(PrecisionEvent.TYPE.DECREASE)) {
                                item.setPrecision(item.getPrecision() - 1);
                            }
                        }
                        setText(getString(item.getValue(), item.getPrecision(), item.getUnitLabel()));
                    });
                }


            }
        };

        return tableCell;


    }

    private String getString(double value, int precision, String unitLabel) {
        return (String.format("%.0" + precision + "f", value) + " " + unitLabel);
    }
}
