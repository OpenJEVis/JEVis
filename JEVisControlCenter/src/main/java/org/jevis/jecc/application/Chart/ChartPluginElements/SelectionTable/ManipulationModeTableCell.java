package org.jevis.jecc.application.Chart.ChartPluginElements.SelectionTable;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jecc.application.Chart.ChartPluginElements.Boxes.ManipulationModeBox;
import org.jevis.jecc.application.Chart.data.ChartData;

public class ManipulationModeTableCell extends TableCell<ChartData, ManipulationMode> {

    private ManipulationModeBox manipulationModeBox;


    public ManipulationModeTableCell() {
        super();
    }

    public static Callback<TableColumn<ChartData, ManipulationMode>, TableCell<ChartData, ManipulationMode>> forTableColumn() {
        return list -> new ManipulationModeTableCell();
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
            if (manipulationModeBox == null) {
                manipulationModeBox = ManipulationModeBox.createComboBox(this);
            }

            ManipulationModeBox.startEdit(this, null, null, manipulationModeBox);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        ManipulationModeBox.cancelEdit(this, null);
    }

    @Override
    public void updateItem(ManipulationMode item, boolean empty) {
        super.updateItem(item, empty);
        ManipulationModeBox.updateItem(this, null, null, manipulationModeBox);
    }

}
