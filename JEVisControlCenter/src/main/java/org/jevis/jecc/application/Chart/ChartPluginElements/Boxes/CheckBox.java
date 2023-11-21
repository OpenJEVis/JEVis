package org.jevis.jecc.application.Chart.ChartPluginElements.Boxes;

import com.jfoenix.controls.JFXCheckBox;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.jevis.commons.i18n.I18n;

public class CheckBox extends JFXCheckBox {

    public CheckBox() {
        super();
    }

    public CheckBox(Boolean b) {
        this();
        this.setSelected(b);
    }

    private static String getItemText(Cell<Boolean> cell) {
        if (cell.getItem() != null) {
            if (cell.getItem()) {
                return I18n.getInstance().getString("plugin.graph.elements.checkbox.yes");
            } else {
                I18n.getInstance().getString("plugin.graph.elements.checkbox.no");
            }
        }

        return "";
    }

    public static CheckBox createComboBox(final Cell<Boolean> cell) {
        Boolean item = cell.getItem();
        final CheckBox checkBox = new CheckBox(item);

        // Use onAction here rather than onKeyReleased (with check for Enter),
        // as otherwise we encounter RT-34685
        checkBox.setOnAction(event -> {

            cell.commitEdit(checkBox.isSelected());
            event.consume();
        });
        checkBox.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cell.cancelEdit();
                t.consume();
            }
        });
        return checkBox;
    }

    public static void startEdit(final Cell<Boolean> cell,
                                 final HBox hbox,
                                 final Node graphic,
                                 final CheckBox checkBox) {
        if (checkBox != null) {
            checkBox.setSelected(cell.getItem());
        }
        cell.setText(null);

        if (graphic != null) {
            hbox.getChildren().setAll(graphic, checkBox);
            cell.setGraphic(hbox);
        } else {
            cell.setGraphic(checkBox);
        }

        // requesting focus so that key input can immediately go into the
        // TextField (see RT-28132)
        checkBox.requestFocus();
    }

    public static void cancelEdit(Cell<Boolean> cell, Node graphic) {
        cell.setText(getItemText(cell));
        cell.setGraphic(graphic);
    }

    public static void updateItem(final Cell<Boolean> cell,
                                  final HBox hbox,
                                  final Node graphic,
                                  final CheckBox checkBox) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else {
            if (cell.isEditing()) {
                if (checkBox != null) {
                    checkBox.setSelected(cell.getItem());
                }
                cell.setText(null);

                if (graphic != null) {
                    hbox.getChildren().setAll(graphic, checkBox);
                    cell.setGraphic(hbox);
                } else {
                    cell.setGraphic(checkBox);
                }
            } else {
                cell.setText(getItemText(cell));
                cell.setGraphic(graphic);
            }
        }
    }
}

