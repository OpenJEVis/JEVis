package org.jevis.jecc.application.Chart.ChartPluginElements.SelectionTable;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.api.JEVisUnit;
import org.jevis.jecc.application.Chart.ChartPluginElements.Boxes.ChartUnitBox;
import org.jevis.jecc.application.Chart.data.ChartData;

public class UnitTableCell extends TableCell<ChartData, JEVisUnit> {

    private ChartUnitBox chartUnitBox;


    public UnitTableCell() {
        super();
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

            ChartUnitBox.startEdit(this, null, null, chartUnitBox);
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
