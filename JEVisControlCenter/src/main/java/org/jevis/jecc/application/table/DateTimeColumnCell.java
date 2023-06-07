package org.jevis.jecc.application.table;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeColumnCell<T> implements Callback<TableColumn<T, DateTime>, TableCell<T, DateTime>> {

    public static DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Override
    public TableCell<T, DateTime> call(TableColumn<T, DateTime> tDateTimeTableColumn) {
        return new TableCell<T, DateTime>() {
            @Override
            protected void updateItem(DateTime item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(fmt.print(item));
                }
            }
        };
    }
}
