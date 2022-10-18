package org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes;

import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.jevis.commons.chart.BubbleType;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.tools.DisabledItemsComboBox;

public class BubbleTypeComboBox extends DisabledItemsComboBox<String> {

    public BubbleTypeComboBox(BubbleType bubbleType) {
        super(ChartType.getListNamesChartTypes());

        this.getSelectionModel().select(BubbleType.parseBubbleIndex(bubbleType));

    }

    private static String getItemText(Cell<BubbleType> cell) {
        return BubbleType.getListNamesBubbleTypes().get(BubbleType.parseBubbleIndex(cell.getItem()));
    }

    public static BubbleTypeComboBox createComboBox(final Cell<BubbleType> cell) {
        BubbleType item = cell.getItem();
        final BubbleTypeComboBox comboBox = new BubbleTypeComboBox(item);

        // Use onAction here rather than onKeyReleased (with check for Enter),
        // as otherwise we encounter RT-34685
        comboBox.setOnAction(event -> {

            cell.commitEdit(BubbleType.parseBubbleIndex(comboBox.getSelectionModel().getSelectedIndex()));
            event.consume();
        });
        comboBox.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cell.cancelEdit();
                t.consume();
            }
        });
        return comboBox;
    }

    public static void startEdit(final Cell<BubbleType> cell,
                                 final HBox hbox,
                                 final Node graphic,
                                 final BubbleTypeComboBox comboBox) {
        if (comboBox != null) {
            comboBox.getSelectionModel().select(BubbleType.parseBubbleIndex(cell.getItem()));
        }
        cell.setText(null);

        if (graphic != null) {
            hbox.getChildren().setAll(graphic, comboBox);
            cell.setGraphic(hbox);
        } else {
            cell.setGraphic(comboBox);
        }

        // requesting focus so that key input can immediately go into the
        // TextField (see RT-28132)
        comboBox.requestFocus();
    }

    public static void cancelEdit(Cell<BubbleType> cell, Node graphic) {
        cell.setText(getItemText(cell));
        cell.setGraphic(graphic);
    }

    public static void updateItem(final Cell<BubbleType> cell,
                                  final HBox hbox,
                                  final Node graphic,
                                  final BubbleTypeComboBox comboBox) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else {
            if (cell.isEditing()) {
                if (comboBox != null) {
                    comboBox.getSelectionModel().select(BubbleType.parseBubbleIndex(cell.getItem()));
                }
                cell.setText(null);

                if (graphic != null) {
                    hbox.getChildren().setAll(graphic, comboBox);
                    cell.setGraphic(hbox);
                } else {
                    cell.setGraphic(comboBox);
                }
            } else {
                cell.setText(getItemText(cell));
                cell.setGraphic(graphic);
            }
        }
    }
}
