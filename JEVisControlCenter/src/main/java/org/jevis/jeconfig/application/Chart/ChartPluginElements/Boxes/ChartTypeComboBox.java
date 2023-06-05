package org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes;

import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.data.ChartModel;
import org.jevis.jeconfig.application.tools.DisabledItemsComboBox;

public class ChartTypeComboBox extends DisabledItemsComboBox<String> {

    public ChartTypeComboBox(ChartSetting currentChartSetting) {
        super(ChartType.getListNamesChartTypes());

        this.getSelectionModel().selectIndex(ChartType.parseChartIndex(currentChartSetting.getChartType()));

        this.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                ChartType type = ChartType.parseChartType(this.getSelectionModel().getSelectedIndex());
                currentChartSetting.setChartType(type);
            }
        });
    }

    public ChartTypeComboBox(ChartType chartType) {
        super(ChartType.getListNamesChartTypes());

        this.getSelectionModel().selectIndex(ChartType.parseChartIndex(chartType));

    }

    public ChartTypeComboBox(ChartModel chartModel) {
        this(chartModel.getChartType());
    }


    private static String getItemText(Cell<ChartType> cell) {
        return ChartType.getListNamesChartTypes().get(ChartType.parseChartIndex(cell.getItem()));
    }

    public static ChartTypeComboBox createComboBox(final Cell<ChartType> cell) {
        ChartType item = cell.getItem();
        final ChartTypeComboBox comboBox = new ChartTypeComboBox(item);

        // Use onAction here rather than onKeyReleased (with check for Enter),
        // as otherwise we encounter RT-34685
        comboBox.setOnAction(event -> {

            cell.commitEdit(ChartType.parseChartType(comboBox.getSelectionModel().getSelectedIndex()));
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

    public static void startEdit(final Cell<ChartType> cell,
                                 final HBox hbox,
                                 final Node graphic,
                                 final ChartTypeComboBox comboBox) {
        if (comboBox != null) {
            comboBox.getSelectionModel().selectIndex(ChartType.parseChartIndex(cell.getItem()));
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

    public static void cancelEdit(Cell<ChartType> cell, Node graphic) {
        cell.setText(getItemText(cell));
        cell.setGraphic(graphic);
    }

    public static void updateItem(final Cell<ChartType> cell,
                                  final HBox hbox,
                                  final Node graphic,
                                  final ChartTypeComboBox comboBox) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else {
            if (cell.isEditing()) {
                if (comboBox != null) {
                    comboBox.getSelectionModel().selectIndex(ChartType.parseChartIndex(cell.getItem()));
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
