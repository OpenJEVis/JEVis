package org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes;

import com.jfoenix.controls.JFXComboBox;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.ChartUnits.MoneyUnit;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;

import java.util.ArrayList;
import java.util.List;

public class ChartUnitBox extends JFXComboBox<JEVisUnit> {

    public ChartUnitBox(ChartDataRow chartDataRow) {
        this(chartDataRow.getUnit());

        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                JEVisUnit jeVisUnit = newValue;
                chartDataRow.setUnit(jeVisUnit);
            }
        });
    }

    public ChartUnitBox(JEVisUnit currentUnit) {
        super();

        List<JEVisUnit> proNames = new ArrayList<>();
        QuantityUnits qu = new QuantityUnits();

        boolean isTimeUnit = qu.isTimeUnit(currentUnit);
        boolean isEnergyUnit = qu.isEnergyUnit(currentUnit);
        boolean isVolumeUnit = qu.isVolumeUnit(currentUnit);
        boolean isMassUnit = qu.isMassUnit(currentUnit);
        boolean isPressureUnit = qu.isPressureUnit(currentUnit);
        boolean isVolumeFlowUnit = qu.isVolumeFlowUnit(currentUnit);
        boolean isMassFlowUnit = qu.isMassFlowUnit(currentUnit);
        boolean isMoneyUnit = qu.isMoneyUnit(currentUnit);

        if (isTimeUnit) {
            proNames.addAll(qu.getTimeUnits());
        }

        if (isEnergyUnit) {
            proNames.addAll(qu.getEnergyUnits());
        }

        if (isVolumeUnit) {
            proNames.addAll(qu.getVolumeUnits());
        }

        if (isMassUnit) {
            proNames.addAll(qu.getMassUnits());
        }

        if (isPressureUnit) {
            proNames.addAll(qu.getPressureUnits());
        }

        if (isVolumeFlowUnit) {
            proNames.addAll(qu.getVolumeFlowUnits());
        }

        if (isMassFlowUnit) {
            proNames.addAll(qu.getMassFlowUnits());
        }

        if (isMoneyUnit) for (MoneyUnit mu : MoneyUnit.values()) {
            proNames.addAll(qu.getVolumeUnits());
        }

        if (proNames.isEmpty() && currentUnit != null && !currentUnit.getLabel().equals("")) {
            proNames.add(currentUnit);
        }

        setItems(FXCollections.observableArrayList(proNames));

        setPrefWidth(90);
        setMinWidth(70);

        if (currentUnit != null) {
            getSelectionModel().select(currentUnit);
        }

    }

    private static String getItemText(Cell<JEVisUnit> cell) {
        if (cell.getItem() != null) {
            return cell.getItem().getLabel();
        }

        return "";
    }

    public static ChartUnitBox createUnitBox(final Cell<JEVisUnit> cell) {
        JEVisUnit item = cell.getItem();
        final ChartUnitBox chartUnitBox = new ChartUnitBox(item);

        // Use onAction here rather than onKeyReleased (with check for Enter),
        // as otherwise we encounter RT-34685
        chartUnitBox.setOnAction(event -> {

            cell.commitEdit(chartUnitBox.getSelectionModel().getSelectedItem());
            event.consume();
        });
        chartUnitBox.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cell.cancelEdit();
                t.consume();
            }
        });
        return chartUnitBox;
    }

    public static void startEdit(final Cell<JEVisUnit> cell,
                                 final HBox hbox,
                                 final Node graphic,
                                 final ChartUnitBox chartUnitBox) {
        if (chartUnitBox != null) {
            chartUnitBox.getSelectionModel().select(cell.getItem());
        }
        cell.setText(null);

        if (graphic != null) {
            hbox.getChildren().setAll(graphic, chartUnitBox);
            cell.setGraphic(hbox);
        } else {
            cell.setGraphic(chartUnitBox);
        }

        // requesting focus so that key input can immediately go into the
        // TextField (see RT-28132)
        chartUnitBox.requestFocus();
    }

    public static void cancelEdit(Cell<JEVisUnit> cell, Node graphic) {
        cell.setText(getItemText(cell));
        cell.setGraphic(graphic);
    }

    public static void updateItem(final Cell<JEVisUnit> cell,
                                  final HBox hbox,
                                  final Node graphic,
                                  final ChartUnitBox chartUnitBox) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else {
            if (cell.isEditing()) {
                if (chartUnitBox != null) {
                    chartUnitBox.getSelectionModel().select(cell.getItem());
                }
                cell.setText(null);

                if (graphic != null) {
                    hbox.getChildren().setAll(graphic, chartUnitBox);
                    cell.setGraphic(hbox);
                } else {
                    cell.setGraphic(chartUnitBox);
                }
            } else {
                cell.setText(getItemText(cell));
                cell.setGraphic(graphic);
            }
        }
    }
}
