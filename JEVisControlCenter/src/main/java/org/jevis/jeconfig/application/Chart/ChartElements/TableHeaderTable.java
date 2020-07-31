package org.jevis.jeconfig.application.Chart.ChartElements;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class TableHeaderTable extends TableView<ObservableList<String>> {
    private static Method columnToFitMethod;

    static {
        try {
            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
            columnToFitMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private final double VALUE_COLUMNS_PREF_SIZE = 200;
    private final double VALUE_COLUMNS_MIN_SIZE = VALUE_COLUMNS_PREF_SIZE - 60;
    private final AlphanumComparator alphanumComparator = new AlphanumComparator();

    public TableHeaderTable(List<XYChartSerie> xyChartSeries) {

        setBorder(null);
        setStyle(
                ".table-view:focused {" +
                        "-fx-padding: 0; " +
                        "-fx-background-color: transparent, -fx-box-border, -fx-control-inner-background; " +
                        "-fx-background-insets: -1.4,0,1;" +
                        "}");

        setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<ObservableList<String>, String> dateColumn = new TableColumn<>(I18n.getInstance().getString("sampleeditor.confirmationdialog.column.time"));
        dateColumn.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(param.getValue().get(0))
        );
        getColumns().add(dateColumn);
    }

    public AlphanumComparator getAlphanumComparator() {
        return alphanumComparator;
    }

    public void autoFitTable() {
        for (Object column : getColumns()) {
            try {
                columnToFitMethod.invoke(getSkin(), column, -1);
            } catch (IllegalAccessException | InvocationTargetException ignored) {
            }
        }
    }

}
