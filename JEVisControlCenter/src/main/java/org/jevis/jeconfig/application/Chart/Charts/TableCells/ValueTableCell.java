package org.jevis.jeconfig.application.Chart.Charts.TableCells;

import com.ibm.icu.text.NumberFormat;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.application.Chart.ChartElements.TableHSample;
import org.jevis.jeconfig.application.tools.ColorHelper;

public class ValueTableCell<T> implements Callback<TableColumn<TableHSample, Double>, TableCell<TableHSample, Double>> {
    private static final Logger logger = LogManager.getLogger(ValueTableCell.class);

    public ValueTableCell() {
        super();
    }


    @Override
    public TableCell<TableHSample, Double> call(TableColumn<TableHSample, Double> tableColumn) {
        TableCell<TableHSample, Double> tableCell = new TableCell<TableHSample, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    TableHSample sample = (TableHSample) getTableRow().getItem();
                    if (sample != null) {
                        String content = getString(sample.getSampleValue(), sample.getNf(), sample.getSampleUnit());
                        setText(content);

                        if (sample.getSampleColor() != null) {
                            setStyle("-fx-text-fill: " + ColorHelper.toRGBCode(sample.getSampleColor()));
                        }
                    }
                }


            }
        };

        return tableCell;


    }

    private String getString(double value, NumberFormat nf, String unitLabel) {
        return (nf.format(value) + " " + unitLabel);
    }
}
