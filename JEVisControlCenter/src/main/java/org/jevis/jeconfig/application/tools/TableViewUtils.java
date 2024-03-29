package org.jevis.jeconfig.application.tools;

import com.google.common.util.concurrent.AtomicDouble;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.jevis.jeconfig.application.Chart.ChartElements.TableSample;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class TableViewUtils {

    /**
     * Workaround Offset, this will be added to an column header name if we calculate the width before it was rendered
     * TODO: somehow calculate this value
     */
    public static double COLUMN_OFFSET_WIDTH = 35;


    /**
     * This function will set the column size so its minimum is based on the header text
     *
     * @param col
     */
    public static void setColumnMinSize(TableColumn<?, ?> col) {
        Text text = new Text(col.getText());
        text.setFont(new Label().getFont());
        double textWidth = text.getBoundsInLocal().getWidth();
//        System.out.println("Text.width: " + textWidth + " or " + text.getLayoutBounds().getWidth());
        col.setPrefWidth(textWidth + COLUMN_OFFSET_WIDTH);
    }

    public static void addCustomTableMenu(TableView<TableSample> tableView, HashMap<TableColumn<TableSample, ?>, String> columnTitles) {

        // enable table menu
        tableView.setTableMenuButtonVisible(true);

        // get the table  header row
        TableHeaderRow tableHeaderRow = getTableHeaderRow((TableViewSkin) tableView.getSkin());

        // get context menu via reflection
        ContextMenu contextMenu = getContextMenu(tableHeaderRow);

        // setting the preferred height for the table header row
        // if the preferred height isn't set, then the table header would disappear if there are no visible columns
        // and with it the table menu button
        // by setting the preferred height the header will always be visible
        // note: this may need adjustments in case you have different heights in columns (eg when you use grouping)
        double defaultHeight = tableHeaderRow.getHeight();
        tableHeaderRow.setPrefHeight(defaultHeight);

        // modify the table menu
        contextMenu.getItems().clear();

        addCustomMenuItems(contextMenu, tableView, columnTitles);
    }

    private static void addCustomMenuItems(ContextMenu cm, TableView<TableSample> table, HashMap<TableColumn<TableSample, ?>, String> columnTitles) {

        // create new context menu
        CustomMenuItem cmi;

        // select all item
        Label showAll = new Label("Show all");
        showAll.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                for (TableColumn<TableSample, ?> obj : table.getColumns()) {
                    obj.setVisible(true);
                }
            }

        });

        cmi = new CustomMenuItem(showAll);
        cmi.setHideOnClick(false);
        cm.getItems().add(cmi);

        // separator
        cm.getItems().add(new SeparatorMenuItem());

        // menu item for each of the available columns
        for (TableColumn<?, ?> obj : table.getColumns()) {

            String customTitle = columnTitles.get(obj);

            CheckBox cb = new CheckBox(obj.getText());

            if (customTitle != null) {
                cb.setText(customTitle);
            }

            cb.selectedProperty().bindBidirectional(obj.visibleProperty());

            cmi = new CustomMenuItem(cb);
            cmi.setHideOnClick(false);

            cm.getItems().add(cmi);
        }

    }

    /**
     * Make table menu button visible and replace the context menu with a custom context menu via reflection.
     * The preferred height is modified so that an empty header row remains visible. This is needed in case you remove all columns, so that the menu button won't disappear with the row header.
     * IMPORTANT: Modification is only possible AFTER the table has been made visible, otherwise you'd get a NullPointerException
     *
     * @param tableView
     */
    public static void addCustomTableMenu(TableView<TableSample> tableView) {

        // enable table menu
        tableView.setTableMenuButtonVisible(true);

        // get the table  header row
        TableHeaderRow tableHeaderRow = getTableHeaderRow((TableViewSkin) tableView.getSkin());

        // get context menu via reflection
        ContextMenu contextMenu = getContextMenu(tableHeaderRow);

        // setting the preferred height for the table header row
        // if the preferred height isn't set, then the table header would disappear if there are no visible columns
        // and with it the table menu button
        // by setting the preferred height the header will always be visible
        // note: this may need adjustments in case you have different heights in columns (eg when you use grouping)
        double defaultHeight = tableHeaderRow.getHeight();
        tableHeaderRow.setPrefHeight(defaultHeight);

        // modify the table menu
        contextMenu.getItems().clear();

        addCustomMenuItems(contextMenu, tableView);

    }

    /**
     * Create a menu with custom items. The important thing is that the menu remains open while you click on the menu items.
     *
     * @param cm
     * @param table
     */
    private static void addCustomMenuItems(ContextMenu cm, TableView<TableSample> table) {

        // create new context menu
        CustomMenuItem cmi;

        // select all item
        Label showAll = new Label("Show all");
        showAll.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                for (TableColumn<TableSample, ?> obj : table.getColumns()) {
                    obj.setVisible(true);
                }
            }

        });

        cmi = new CustomMenuItem(showAll);
        cmi.setHideOnClick(false);
        cm.getItems().add(cmi);

        // deselect all item
        Label hideAll = new Label("Hide all");
        hideAll.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                for (TableColumn<?, ?> obj : table.getColumns()) {
                    obj.setVisible(false);
                }
            }

        });

        cmi = new CustomMenuItem(hideAll);
        cmi.setHideOnClick(false);
        cm.getItems().add(cmi);

        // separator
        cm.getItems().add(new SeparatorMenuItem());

        // menu item for each of the available columns
        for (TableColumn<?, ?> obj : table.getColumns()) {

            CheckBox cb = new CheckBox(obj.getText());
            cb.selectedProperty().bindBidirectional(obj.visibleProperty());

            cmi = new CustomMenuItem(cb);
            cmi.setHideOnClick(false);

            cm.getItems().add(cmi);
        }

    }

    /**
     * Find the TableHeaderRow of the TableViewSkin
     *
     * @param tableSkin
     * @return
     */
    private static TableHeaderRow getTableHeaderRow(TableViewSkin<?> tableSkin) {

        // get all children of the skin
        ObservableList<Node> children = tableSkin.getChildren();

        // find the TableHeaderRow child
        for (Node node : children) {

            if (node instanceof TableHeaderRow) {
                return (TableHeaderRow) node;
            }

        }
        return null;
    }

    /**
     * Get the table menu, i. e. the ContextMenu of the given TableHeaderRow via
     * reflection
     *
     * @param headerRow
     * @return
     */
    private static ContextMenu getContextMenu(TableHeaderRow headerRow) {

        try {

            // get columnPopupMenu field
            Field privateContextMenuField = TableHeaderRow.class.getDeclaredField("columnPopupMenu");

            // make field public
            privateContextMenuField.setAccessible(true);

            // get field

            return (ContextMenu) privateContextMenuField.get(headerRow);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static void setColumnMinSize(List<TableColumn<TableSample, ?>> columns) {
        columns.forEach(TableViewUtils::setColumnMinSize);
    }

    public static void growColumns(TableView<?> view, List<TableColumn<?, ?>> columns) {
        double expectedColWidth = 0;
        for (int i = 0; i < view.getColumns().size(); i++) {
            TableColumn<?, ?> col = view.getColumns().get(i);
            if (col.isVisible()) {
                expectedColWidth += col.getPrefWidth();
            }
        }

        if (expectedColWidth < view.getWidth() && !columns.isEmpty()) {
            double freePart = (view.getWidth() - expectedColWidth) / columns.size();
//            System.out.println("freePart: " + freePart);
            for (TableColumn<?, ?> col : columns) {
                col.setPrefWidth(col.getPrefWidth() + freePart);
            }
        }

    }

    public static void allToMin(TableView<?> view) {
        view.getColumns().forEach(col -> col.setPrefWidth(col.getMinWidth()));
    }

    public static void allToMinButColumn(TableView<?> view, List<TableColumn<?, ?>> column) {
        boolean needResize = false;
        double totalColSize = 0;
        for (int i = 0; i < view.getColumns().size(); i++) {
            TableColumn<?, ?> col = view.getColumns().get(i);
//            System.out.println("Col: " + col.getText());
            totalColSize += col.getPrefWidth();
        }


        if (totalColSize > view.getWidth()) {
            for (int i = 0; i < view.getColumns().size(); i++) {
                TableColumn<?, ?> col = view.getColumns().get(i);
                for (TableColumn tc : column) {
                    if (!col.equals(tc)) {
                        col.setPrefWidth(col.getMinWidth());
                    }
                }
            }
            growColumns(view, column);
        }

    }

    /**
     * @param view
     * @param prioColumns
     */
    public static void customResize(TableView<?> view, List<TableColumn> prioColumns) {
        AtomicDouble width = new AtomicDouble();
        view.getColumns().forEach(col -> {
            width.addAndGet(col.getWidth());
        });
        double tableWidth = view.getWidth();
//        System.out.println("Table.width: " + tableWidth);
//        System.out.println("Total.colum.width: " + width);

        double expectedColWidth = 0;
        for (int i = 0; i < view.getColumns().size(); i++) {
            TableColumn<?, ?> col = view.getColumns().get(i);
//            System.out.println("col.getGraphic(): " + col.getGraphic() + "    text:" + col.getText());
            Text text = new Text(col.getText());
            text.setFont(new Label().getFont());
            double textWidth = text.getBoundsInLocal().getWidth();
//            System.out.println("Text.width: " + textWidth + " or " + text.getLayoutBounds().getWidth());
            col.setPrefWidth(textWidth + COLUMN_OFFSET_WIDTH);
            expectedColWidth += col.getPrefWidth();

        }

        if (expectedColWidth < tableWidth && !prioColumns.isEmpty()) {
//            System.out.println("Free column space to give: " + expectedColWidth + "-" + expectedColWidth + "=");
            double freePart = (tableWidth - expectedColWidth) / prioColumns.size();
//            System.out.println("freePart: " + freePart);
            for (int i = 0; i < prioColumns.size(); i++) {
                TableColumn<?, ?> col = prioColumns.get(i);
                col.setPrefWidth(col.getPrefWidth() + freePart - (COLUMN_OFFSET_WIDTH * prioColumns.size()));
            }
        }


    }

    /**
     * @param table
     */
    public static void addAutoScrollbarResize(TableView<TableSample> table) {
        ScrollBar scrollBar = getTableScrollBar(table, Orientation.HORIZONTAL);
//        scrollBar.visibleProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println("ScrollbarEvent: " + newValue);
//            if (!oldValue && newValue) {
//                table.setPrefHeight(table.getPrefHeight() + 35);
//            } else if (oldValue && !newValue) {
//                table.setPrefHeight(table.getPrefHeight() - 35);
//            }
//        });

        Platform.runLater(() -> {
            scrollBar.setMax(0);
            scrollBar.setVisible(false);
        });

//        if (scrollBar.isVisible()) {
//            System.out.println("Scrollbar is visible");
//            Platform.runLater(() -> {
//                System.out.println("Table.size: " + table.getHeight());
//                table.resize(table.getWidth(), table.getHeight() + 35);
//                System.out.println("Table.size: " + table.getHeight());
//            });
////            table.setPrefHeight(table.getPrefHeight() + 35);
//        } else {
//            System.out.println("Scrollbar is not visible");
////            tabls.setPrefHeight(table.getPrefHeight() - 35);
//        }

    }

    public static ScrollBar getTableScrollBar(TableView<TableSample> table, Orientation orientation) {

        ScrollBar sbar = new ScrollBar();

        for (Node node : table.lookupAll(".scroll-bar")) {
//            System.out.println("ScollbarNode: " + node);
            if (node instanceof ScrollBar) {
//                System.out.println("found scrollbar");
                if (((ScrollBar) node).getOrientation().equals(orientation)) {
                    sbar = (ScrollBar) node;
                }

            }
        }
        return sbar;
    }

    public static void setColumnDataMinSize(TableView<?> view, TableColumn<?, ?> column) {

        view.getItems().addListener(new ListChangeListener<Object>() {
            @Override
            public void onChanged(Change<?> c) {
                for (Object column : view.getColumns()) {
                    try {
                        Method fitTosize = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
                        fitTosize.setAccessible(true);
                        fitTosize.invoke(view.getSkin(), column, -1);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    public static void resizeColumnToFitContent(TableView<?> tableView, TableColumn<TableSample, ?> tc, int maxRows) {
        if (!tc.isResizable()) return;

//        final TableColumn<T, ?> col = tc;
        List<?> items = tableView.getItems();
        if (items == null || items.isEmpty()) return;

        Callback/*<TableColumn<T, ?>, TableCell<T,?>>*/ cellFactory = tc.getCellFactory();
        if (cellFactory == null) return;

        TableCell cell = (TableCell) cellFactory.call(tc);
        if (cell == null) return;

        // set this property to tell the TableCell we want to know its actual
        // preferred width, not the width of the associated TableColumnBase
        cell.getProperties().put("deferToParentPrefWidth", Boolean.TRUE);

        // determine cell padding
        double padding = 10;
        Node n = cell.getSkin() == null ? null : cell.getSkin().getNode();
        if (n instanceof Region) {
            Region r = (Region) n;
            padding = r.snappedLeftInset() + r.snappedRightInset();
        }

        int rows = maxRows == -1 ? items.size() : Math.min(items.size(), maxRows);
        double maxWidth = 0;
        for (int row = 0; row < rows; row++) {
            cell.updateTableColumn(tc);
            cell.updateTableView(tableView);
            cell.updateIndex(row);

            if ((cell.getText() != null && !cell.getText().isEmpty()) || cell.getGraphic() != null) {
//                getChildren().add(cell); fs
                cell.applyCss();
                maxWidth = Math.max(maxWidth, cell.prefWidth(-1));
//                getChildren().remove(cell); fs
            }
        }

        // dispose of the cell to prevent it retaining listeners (see RT-31015)
        cell.updateIndex(-1);

        // RT-36855 - take into account the column header text / graphic widths.
        // Magic 10 is to allow for sort arrow to appear without text truncation.
//        TableColumnHeader header = tableView.getTableHeaderRow().getColumnHeaderFor(tc);
//        double headerTextWidth = Utils.computeTextWidth(header.label.getFont(), tc.getText(), -1);
//        Node graphic = header.label.getGraphic();
//        double headerGraphicWidth = graphic == null ? 0 : graphic.prefWidth(-1) + header.label.getGraphicTextGap();
//        double headerWidth = headerTextWidth + headerGraphicWidth + 10 + header.snappedLeftInset() + header.snappedRightInset();
//        maxWidth = Math.max(maxWidth, headerWidth);

        // RT-23486
        maxWidth += padding;
        if (tableView.getColumnResizePolicy() == TableView.CONSTRAINED_RESIZE_POLICY) {
            maxWidth = Math.max(maxWidth, tc.getWidth());
        }

        tc.impl_setWidth(maxWidth);
    }

}
