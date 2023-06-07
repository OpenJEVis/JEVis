package org.jevis.jecc.application.Chart.ChartPluginElements.SelectionTable;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.jevis.jecc.application.Chart.data.ChartData;
import org.jevis.jecc.application.control.ColorPickerAdv;

public class ColorTableCell extends TableCell<ChartData, Color> {

    private ColorPickerAdv colorPicker;


    public ColorTableCell() {
        super();
    }

    public static Callback<TableColumn<ChartData, Color>, TableCell<ChartData, Color>> forTableColumn() {
        return list -> new ColorTableCell();
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
            if (colorPicker == null) {
                colorPicker = ColorPickerAdv.createColorPicker(this);
            }

            ColorPickerAdv.startEdit(this, null, null, colorPicker);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        ColorPickerAdv.cancelEdit(this, null);
    }

    @Override
    public void updateItem(Color item, boolean empty) {
        super.updateItem(item, empty);
        ColorPickerAdv.updateItem(this, null, null, colorPicker);
    }

}
