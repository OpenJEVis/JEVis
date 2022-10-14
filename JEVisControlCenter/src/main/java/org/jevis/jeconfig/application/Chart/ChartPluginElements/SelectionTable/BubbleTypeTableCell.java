package org.jevis.jeconfig.application.Chart.ChartPluginElements.SelectionTable;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.commons.chart.BubbleType;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.BubbleTypeComboBox;
import org.jevis.jeconfig.application.Chart.data.ChartData;

public class BubbleTypeTableCell extends TableCell<ChartData, BubbleType> {

    private BubbleTypeComboBox bubbleTypeComboBox;


    public BubbleTypeTableCell() {
        super();
    }

    public static Callback<TableColumn<ChartData, BubbleType>, TableCell<ChartData, BubbleType>> forTableColumn() {
        return list -> new BubbleTypeTableCell();
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
            if (bubbleTypeComboBox == null) {
                bubbleTypeComboBox = BubbleTypeComboBox.createComboBox(this);
            }

            BubbleTypeComboBox.startEdit(this, null, null, bubbleTypeComboBox);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        BubbleTypeComboBox.cancelEdit(this, null);
    }

    @Override
    public void updateItem(BubbleType item, boolean empty) {
        super.updateItem(item, empty);
        BubbleTypeComboBox.updateItem(this, null, null, bubbleTypeComboBox);
    }

}
