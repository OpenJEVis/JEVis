package org.jevis.jecc.application.Chart.ChartElements;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;

import java.util.List;

public class TableHeaderTable extends TableView {
//    private static Method columnToFitMethod;

    static {
//        try {
        //TODO JFX17
//            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
//            columnToFitMethod.setAccessible(true);
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        }
    }

    private final double VALUE_COLUMNS_PREF_SIZE = 200;
    private final double VALUE_COLUMNS_MIN_SIZE = VALUE_COLUMNS_PREF_SIZE - 60;
    private final AlphanumComparator alphanumComparator = new AlphanumComparator();

    public TableHeaderTable(List<XYChartSerie> xyChartSeries) {

        setBorder(null);
//        setStyle(
//                ".table-view:focused {" +
//                        "-fx-padding: 0; " +
//                        "-fx-background-color: transparent, -fx-box-border, -fx-control-inner-background; " +
//                        "-fx-background-insets: -1.4,0,1;" +
//                        "}");

        setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<List<String>, String> dateColumn = new TableColumn<>(I18n.getInstance().getString("sampleeditor.confirmationdialog.column.time"));
        dateColumn.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(param.getValue().get(0))
        );
        getColumns().add(dateColumn);
    }

    public AlphanumComparator getAlphanumComparator() {
        return alphanumComparator;
    }

    public void autoFitTable() {
//        for (Object column : getColumns()) {
//            try {
//                columnToFitMethod.invoke(getSkin(), column, -1);
//            } catch (Exception ignored) {
//            }
//        }
    }

}
