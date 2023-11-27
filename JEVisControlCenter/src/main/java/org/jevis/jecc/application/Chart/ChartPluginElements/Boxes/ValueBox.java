package org.jevis.jecc.application.Chart.ChartPluginElements.Boxes;


import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;

import java.text.NumberFormat;

public class ValueBox extends HBox {
    private final static NumberFormat nf = NumberFormat.getInstance(I18n.getInstance().getLocale());

    public ValueBox(JEVisSample sample) {
        this.setSpacing(6);

        TextField valueField = new TextField();
        TextField unitField = new TextField();

        if (sample != null) {
            try {
                valueField.setText(nf.format(sample.getValueAsDouble()));
                Tooltip timestampTooltip = new Tooltip(sample.getTimestamp().toString("yyyy-MM-dd HH:mm:ss"));
                valueField.setTooltip(timestampTooltip);
                unitField.setText(sample.getUnit().getLabel());
            } catch (Exception ignored) {
            }
        }

        this.getChildren().setAll(valueField, unitField);

        this.setMinWidth(40);
    }

    private static String getItemText(Cell<JEVisSample> cell) {
        try {
            return nf.format(cell.getItem().getValueAsDouble()) + " " + cell.getItem().getUnit().getLabel();
        } catch (Exception ignored) {
        }
        return "";
    }

    public static ValueBox createComboBox(final Cell<JEVisSample> cell) {
        JEVisSample item = cell.getItem();
        final ValueBox valueBox = new ValueBox(item);

        // Use onAction here rather than onKeyReleased (with check for Enter),
        // as otherwise we encounter RT-34685
//        valueBox.setOnAction(event -> {
//
//            cell.commitEdit(valueBox.getSelectionModel().getSelectedIndex());
//            event.consume();
//        });
        valueBox.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cell.cancelEdit();
                t.consume();
            }
        });
        return valueBox;
    }

    public static void startEdit(final Cell<JEVisSample> cell,
                                 final HBox hbox,
                                 final Node graphic,
                                 final ValueBox valueBox) {
        if (valueBox != null) {
//            valueBox.getSelectionModel().select(cell.getItem());
        }
        cell.setText(null);

        if (graphic != null) {
            hbox.getChildren().setAll(graphic, valueBox);
            cell.setGraphic(hbox);
        } else {
            cell.setGraphic(valueBox);
        }

        // requesting focus so that key input can immediately go into the
        // TextField (see RT-28132)
        valueBox.requestFocus();
    }

    public static void cancelEdit(Cell<JEVisSample> cell, Node graphic) {
        cell.setText(getItemText(cell));
        cell.setGraphic(graphic);

        if (!cell.isEmpty() && cell.getItem() != null) {
            try {
                Tooltip timeStampTooltip = new Tooltip(cell.getItem().getTimestamp().toString("yyyy-MM-dd HH:mm:ss"));
                cell.setTooltip(timeStampTooltip);
            } catch (Exception ignored) {
            }

        }
    }

    public static void updateItem(final Cell<JEVisSample> cell,
                                  final HBox hbox,
                                  final Node graphic,
                                  final ValueBox valueBox) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else {
            if (cell.isEditing()) {
                if (valueBox != null) {
//                    valueBox.getSelectionModel().select(cell.getItem());
                }
                cell.setText(null);

                if (graphic != null) {
                    hbox.getChildren().setAll(graphic, valueBox);
                    cell.setGraphic(hbox);
                } else {
                    cell.setGraphic(valueBox);
                }
            } else {
                cell.setText(getItemText(cell));
                cell.setGraphic(graphic);
                try {
                    Tooltip timeStampTooltip = new Tooltip(cell.getItem().getTimestamp().toString("yyyy-MM-dd HH:mm:ss"));
                    cell.setTooltip(timeStampTooltip);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
