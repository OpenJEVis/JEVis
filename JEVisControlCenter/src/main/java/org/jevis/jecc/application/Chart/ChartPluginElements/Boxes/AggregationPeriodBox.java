package org.jevis.jecc.application.Chart.ChartPluginElements.Boxes;

import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.jecc.application.tools.DisabledItemsComboBox;

public class AggregationPeriodBox extends DisabledItemsComboBox<String> {


    public AggregationPeriodBox(AggregationPeriod aggregationPeriod) {
        super(AggregationPeriod.getListNamesAggregationPeriods());

        getSelectionModel().select(AggregationPeriod.parseAggregationIndex(aggregationPeriod));
    }

    private static String getItemText(Cell<AggregationPeriod> cell) {
        return AggregationPeriod.getListNamesAggregationPeriods().get(AggregationPeriod.parseAggregationIndex(cell.getItem()));
    }

    public static AggregationPeriodBox createComboBox(final Cell<AggregationPeriod> cell) {
        AggregationPeriod item = cell.getItem();
        final AggregationPeriodBox comboBox = new AggregationPeriodBox(item);

        // Use onAction here rather than onKeyReleased (with check for Enter),
        // as otherwise we encounter RT-34685
        comboBox.setOnAction(event -> {

            cell.commitEdit(AggregationPeriod.parseAggregationIndex(comboBox.getSelectionModel().getSelectedIndex()));
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

    public static void startEdit(final Cell<AggregationPeriod> cell,
                                 final HBox hbox,
                                 final Node graphic,
                                 final AggregationPeriodBox comboBox) {
        if (comboBox != null) {
            comboBox.getSelectionModel().select(AggregationPeriod.parseAggregationIndex(cell.getItem()));
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

    public static void cancelEdit(Cell<AggregationPeriod> cell, Node graphic) {
        cell.setText(getItemText(cell));
        cell.setGraphic(graphic);
    }

    public static void updateItem(final Cell<AggregationPeriod> cell,
                                  final HBox hbox,
                                  final Node graphic,
                                  final AggregationPeriodBox comboBox) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else {
            if (cell.isEditing()) {
                if (comboBox != null) {
                    comboBox.getSelectionModel().select(AggregationPeriod.parseAggregationIndex(cell.getItem()));
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
