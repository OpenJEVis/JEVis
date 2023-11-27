package org.jevis.jecc.application.Chart.ChartPluginElements.Boxes;


import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.CheckBox;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.jevis.commons.i18n.I18n;

public class CheckBoxForTable extends CheckBox {

    public CheckBoxForTable() {
        super();
    }

    public CheckBoxForTable(Boolean b) {
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

    public static CheckBoxForTable createComboBox(final Cell<Boolean> cell) {
        Boolean item = cell.getItem();
        final CheckBoxForTable checkBoxForTable = new CheckBoxForTable(item);

        // Use onAction here rather than onKeyReleased (with check for Enter),
        // as otherwise we encounter RT-34685
        checkBoxForTable.setOnAction(event -> {

            cell.commitEdit(checkBoxForTable.isSelected());
            event.consume();
        });
        checkBoxForTable.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cell.cancelEdit();
                t.consume();
            }
        });
        return checkBoxForTable;
    }

    public static void startEdit(final Cell<Boolean> cell,
                                 final HBox hbox,
                                 final Node graphic,
                                 final CheckBoxForTable checkBoxForTable) {
        if (checkBoxForTable != null) {
            checkBoxForTable.setSelected(cell.getItem());
        }
        cell.setText(null);

        if (graphic != null) {
            hbox.getChildren().setAll(graphic, checkBoxForTable);
            cell.setGraphic(hbox);
        } else {
            cell.setGraphic(checkBoxForTable);
        }

        // requesting focus so that key input can immediately go into the
        // TextField (see RT-28132)
        checkBoxForTable.requestFocus();
    }

    public static void cancelEdit(Cell<Boolean> cell, Node graphic) {
        cell.setText(getItemText(cell));
        cell.setGraphic(graphic);
    }

    public static void updateItem(final Cell<Boolean> cell,
                                  final HBox hbox,
                                  final Node graphic,
                                  final CheckBoxForTable checkBoxForTable) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else {
            if (cell.isEditing()) {
                if (checkBoxForTable != null) {
                    checkBoxForTable.setSelected(cell.getItem());
                }
                cell.setText(null);

                if (graphic != null) {
                    hbox.getChildren().setAll(graphic, checkBoxForTable);
                    cell.setGraphic(hbox);
                } else {
                    cell.setGraphic(checkBoxForTable);
                }
            } else {
                cell.setText(getItemText(cell));
                cell.setGraphic(graphic);
            }
        }
    }
}

