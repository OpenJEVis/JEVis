package org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes;

import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.application.tools.DisabledItemsComboBox;

public class ManipulationModeBox extends DisabledItemsComboBox<String> {


    public ManipulationModeBox(ManipulationMode manipulationMode) {
        super(ManipulationMode.getListNamesManipulationModes());

        getSelectionModel().select(ManipulationMode.parseManipulationIndex(manipulationMode));
    }

    private static String getItemText(Cell<ManipulationMode> cell) {
        return ManipulationMode.getListNamesManipulationModes().get(ManipulationMode.parseManipulationIndex(cell.getItem()));
    }

    public static ManipulationModeBox createComboBox(final Cell<ManipulationMode> cell) {
        ManipulationMode item = cell.getItem();
        final ManipulationModeBox comboBox = new ManipulationModeBox(item);

        // Use onAction here rather than onKeyReleased (with check for Enter),
        // as otherwise we encounter RT-34685
        comboBox.setOnAction(event -> {

            cell.commitEdit(ManipulationMode.parseManipulationIndex(comboBox.getSelectionModel().getSelectedIndex()));
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

    public static void startEdit(final Cell<ManipulationMode> cell,
                                 final HBox hbox,
                                 final Node graphic,
                                 final ManipulationModeBox comboBox) {
        if (comboBox != null) {
            comboBox.getSelectionModel().select(ManipulationMode.parseManipulationIndex(cell.getItem()));
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

    public static void cancelEdit(Cell<ManipulationMode> cell, Node graphic) {
        cell.setText(getItemText(cell));
        cell.setGraphic(graphic);
    }

    public static void updateItem(final Cell<ManipulationMode> cell,
                                  final HBox hbox,
                                  final Node graphic,
                                  final ManipulationModeBox comboBox) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else {
            if (cell.isEditing()) {
                if (comboBox != null) {
                    comboBox.getSelectionModel().select(ManipulationMode.parseManipulationIndex(cell.getItem()));
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

