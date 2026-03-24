package org.jevis.jeconfig.application.Chart.ChartElements;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;
import org.joda.time.DateTime;

import java.lang.reflect.Method;
import java.util.List;

/**
 * A {@link TableView} that serves as the column-header and data table for the vertical
 * table chart ({@link org.jevis.jeconfig.application.Chart.Charts.TableChartV}).
 * <p>
 * The first column (timestamp) is created in the constructor; additional series columns
 * are added externally by the chart implementation. Provides {@link #autoFitTable()} to
 * resize all columns to their content width using the package-private
 * {@code TableViewSkin.resizeColumnToFitContent} method via reflection.
 * <p>
 * If the reflection look-up fails at class initialisation time (e.g. on non-Oracle JVMs),
 * {@link #autoFitTable()} becomes a no-op and a log error is emitted.
 */
public class TableHeaderTable extends TableView {
    private static final Logger logger = LogManager.getLogger(TableHeaderTable.class);
    private static Method columnToFitMethod;

    static {
        try {
            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
            columnToFitMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            logger.error("Could not find TableViewSkin.resizeColumnToFitContent via reflection — autoFitTable will be a no-op", e);
        }
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

        TableColumn<TableSample, DateTime> dateColumn = new TableColumn<>(I18n.getInstance().getString("sampleeditor.confirmationdialog.column.time"));
        dateColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getTimeStamp()));
        getColumns().add(dateColumn);
    }

    public AlphanumComparator getAlphanumComparator() {
        return alphanumComparator;
    }

    /**
     * Resizes every column to its content width by invoking the internal
     * {@code TableViewSkin.resizeColumnToFitContent} method via reflection.
     * <p>
     * This is a best-effort operation — if the skin has not yet been attached or the
     * reflection look-up failed at class init, the call is silently skipped.
     */
    public void autoFitTable() {
        for (Object column : getColumns()) {
            try {
                columnToFitMethod.invoke(getSkin(), column, -1);
            } catch (Exception ignored) {
            }
        }
    }

}
