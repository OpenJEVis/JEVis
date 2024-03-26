package org.jevis.jecc.application.Chart.ChartPluginElements.SelectionTable;

import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.jevis.api.JEVisUnit;
import org.jevis.jecc.Icon;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.application.Chart.ChartPluginElements.Boxes.ChartUnitBox;
import org.jevis.jecc.application.Chart.data.ChartData;

public class UnitTableCell extends TableCell<ChartData, JEVisUnit> {

    private ChartUnitBox chartUnitBox;
    private final Button forAll;
    private final HBox hBox = new HBox(4);


    public UnitTableCell() {
        super();

        forAll = new Button("", ControlCenter.getSVGImage(Icon.CHECK, 12, 12));
        forAll.setOnAction(actionEvent -> {
            JEVisUnit selectedItem = chartUnitBox.getSelectionModel().getSelectedItem();
            getTableColumn().getTableView().getItems().forEach(chartData -> chartData.setUnit(selectedItem));
            getTableColumn().getTableView().refresh();
        });

        setGraphic(forAll);
    }

    public static Callback<TableColumn<ChartData, JEVisUnit>, TableCell<ChartData, JEVisUnit>> forTableColumn() {
        return list -> new UnitTableCell();
    }

    @Override
    public void startEdit() {
        if (!isEditable()
                || !getTableView().isEditable()
                || !getTableColumn().isEditable()) {
            return;
        }
        super.startEdit();

        if (isEditing()) {
            if (chartUnitBox == null) {
                chartUnitBox = ChartUnitBox.createUnitBox(this);

            }

            ChartUnitBox.startEdit(this, hBox, forAll, chartUnitBox);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        ChartUnitBox.cancelEdit(this, null);
    }

    @Override
    public void updateItem(JEVisUnit item, boolean empty) {
        super.updateItem(item, empty);
        ChartUnitBox.updateItem(this, null, null, chartUnitBox);
    }

}
