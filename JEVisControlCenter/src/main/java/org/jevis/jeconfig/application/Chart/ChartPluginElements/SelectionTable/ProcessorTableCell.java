package org.jevis.jeconfig.application.Chart.ChartPluginElements.SelectionTable;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.ProcessorBox;
import org.jevis.jeconfig.application.Chart.data.ChartData;

public class ProcessorTableCell extends TableCell<ChartData, Long> {

    private static JEVisDataSource ds;
    private ProcessorBox processorBox;


    public ProcessorTableCell() {
        super();
    }

    public static Callback<TableColumn<ChartData, Long>, TableCell<ChartData, Long>> forTableColumn(JEVisDataSource ds) {
        ProcessorTableCell.ds = ds;
        return list -> new ProcessorTableCell();
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
            if (processorBox == null) {
                processorBox = ProcessorBox.createComboBox(ds, this);
            }

            ProcessorBox.startEdit(ds, this, null, null, processorBox);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        ProcessorBox.cancelEdit(ds, this, null);
    }

    @Override
    public void updateItem(Long item, boolean empty) {
        super.updateItem(item, empty);
        ProcessorBox.updateItem(ds, this, null, null, processorBox);
    }

}
