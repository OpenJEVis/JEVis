package org.jevis.jecc.application.Chart.ChartPluginElements.Boxes;

import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.application.Chart.data.ValueWithDateTime;
import org.joda.time.DateTime;

import java.text.NumberFormat;

public class ValueWithDateTimeBox extends HBox {
    private final static NumberFormat nf = NumberFormat.getInstance(I18n.getInstance().getLocale());

    public ValueWithDateTimeBox(ValueWithDateTime value) {
        this.setSpacing(6);

        MFXTextField valueField = new MFXTextField();

        if (value != null) {
            try {
                String text = nf.format(value.getValue());

                if (value.getValue() == Double.MAX_VALUE || value.getValue() == -Double.MAX_VALUE) {
                    text = "-";
                }

                StringBuilder stringBuilder = new StringBuilder();
                for (DateTime dateTime : value.getDateTime()) {
                    if (value.getDateTime().indexOf(dateTime) > 0) stringBuilder.append("\n");
                    stringBuilder.append(dateTime.toString("yyyy-MM-dd HH:mm:ss"));
                }
                Tooltip timeStampTooltip = new Tooltip(stringBuilder.toString());
                valueField.setTooltip(timeStampTooltip);
                if (value.getUnit() != null && !value.getUnit().getLabel().equals("")) {
                    text += " " + value.getUnit().getLabel();
                }

                valueField.setText(text);
            } catch (Exception ignored) {
            }
        } else valueField.setText("-");

        this.getChildren().setAll(valueField);

        this.setMinWidth(40);
    }

    private static String getItemText(Cell<ValueWithDateTime> cell) {
        try {


            String text = "-";

            if (cell.getItem() != null && cell.getItem().getValue() != Double.MAX_VALUE && cell.getItem().getValue() != -Double.MAX_VALUE) {
                text = nf.format(cell.getItem().getValue());
            }

            if (cell.getItem().getUnit() != null && !cell.getItem().getUnit().getLabel().equals("")) {
                text += " " + cell.getItem().getUnit().getLabel();
            }

            return text;
        } catch (Exception ignored) {
        }
        return "";
    }

    public static ValueWithDateTimeBox createComboBox(final Cell<ValueWithDateTime> cell) {
        ValueWithDateTime item = cell.getItem();
        final ValueWithDateTimeBox valueBox = new ValueWithDateTimeBox(item);

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

    public static void startEdit(final Cell<ValueWithDateTime> cell,
                                 final HBox hbox,
                                 final Node graphic,
                                 final ValueWithDateTimeBox valueBox) {
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

    public static void cancelEdit(Cell<ValueWithDateTime> cell, Node graphic) {
        cell.setText(getItemText(cell));
        cell.setGraphic(graphic);

        if (!cell.isEmpty() && cell.getItem() != null) {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                for (DateTime dateTime : cell.getItem().getDateTime()) {
                    if (cell.getItem().getDateTime().indexOf(dateTime) > 0) stringBuilder.append("\n");
                    stringBuilder.append(dateTime.toString("yyyy-MM-dd HH:mm:ss"));
                }
                Tooltip timeStampTooltip = new Tooltip(stringBuilder.toString());
                cell.setTooltip(timeStampTooltip);
            } catch (Exception ignored) {
            }

        }
    }

    public static void updateItem(final Cell<ValueWithDateTime> cell,
                                  final HBox hbox,
                                  final Node graphic,
                                  final ValueWithDateTimeBox valueBox) {
        if (cell.isEmpty()) {
            cell.setText("-");
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
                    StringBuilder stringBuilder = new StringBuilder();
                    for (DateTime dateTime : cell.getItem().getDateTime()) {
                        if (cell.getItem().getDateTime().indexOf(dateTime) > 0) stringBuilder.append("\n");
                        stringBuilder.append(dateTime.toString("yyyy-MM-dd HH:mm:ss"));
                    }
                    Tooltip timeStampTooltip = new Tooltip(stringBuilder.toString());
                    cell.setTooltip(timeStampTooltip);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
