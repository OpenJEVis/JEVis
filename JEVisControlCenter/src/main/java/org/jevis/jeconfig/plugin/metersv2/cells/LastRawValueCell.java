package org.jevis.jeconfig.plugin.metersv2.cells;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.jeconfig.plugin.metersv2.event.PrecisionEvent;
import org.jevis.jeconfig.plugin.metersv2.event.PrecisionEventHandler;

public class LastRawValueCell<T> implements Callback<TableColumn<T, LastRawValuePojo>, TableCell<T, LastRawValuePojo>> {


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
                    System.out.println(item);
                    String content = getString(item.getValue(), item.getPrecision(), item.getUnitLabel());
                    setText(content);

                    precisionEventHandler.addEventListener(event -> {
                        PrecisionEvent precisionEvent = (PrecisionEvent) event;
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
