package org.jevis.jecc.plugin.loadprofile.controls;

import com.jfoenix.controls.JFXTextField;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.jevis.commons.i18n.I18n;

import java.text.NumberFormat;

public class ValueField extends HBox {
    private final static NumberFormat nf = NumberFormat.getInstance(I18n.getInstance().getLocale());

    public ValueField(Object value) {
        this.setSpacing(6);

        JFXTextField valueField = new JFXTextField();

        if (value != null) {
            try {
                valueField.setText(nf.format(value));
            } catch (Exception ignored) {
            }
        }

        this.getChildren().setAll(valueField);

        this.setMinWidth(40);
    }

    private static String getItemText(Cell<Object> cell) {
        try {
            return nf.format(cell.getItem());
        } catch (Exception ignored) {
        }
        return "";
    }

    public static ValueField createComboBox(final Cell<Object> cell) {
        Object item = cell.getItem();
        final ValueField valueBox = new ValueField(item);

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

    public static void startEdit(final Cell<Object> cell,
                                 final HBox hbox,
                                 final Node graphic,
                                 final ValueField valueField) {
        if (valueField != null) {
//            valueField.getSelectionModel().select(cell.getItem());
        }
        cell.setText(null);

        if (graphic != null) {
            hbox.getChildren().setAll(graphic, valueField);
            cell.setGraphic(hbox);
        } else {
            cell.setGraphic(valueField);
        }

        // requesting focus so that key input can immediately go into the
        // TextField (see RT-28132)
        valueField.requestFocus();
    }

    public static void cancelEdit(Cell<Object> cell, Node graphic) {
        cell.setText(getItemText(cell));
        cell.setGraphic(graphic);
    }

    public static void updateItem(final Cell<Object> cell,
                                  final HBox hbox,
                                  final Node graphic,
                                  final ValueField valueField) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else {
            if (cell.isEditing()) {
                if (valueField != null) {
//                    valueField.getSelectionModel().select(cell.getItem());
                }
                cell.setText(null);

                if (graphic != null) {
                    hbox.getChildren().setAll(graphic, valueField);
                    cell.setGraphic(hbox);
                } else {
                    cell.setGraphic(valueField);
                }
            } else {
                cell.setText(getItemText(cell));
                cell.setGraphic(graphic);
            }
        }
    }
}
