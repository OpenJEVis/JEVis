package org.jevis.jeconfig.plugin.charts;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableViewSkin;
import javafx.util.Callback;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.application.tools.TableViewUtils;

import java.lang.reflect.Method;
import java.util.Collections;

public class ValuesTable extends TableView<Values> {

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
    private final TableColumn<Values, String> nameCol;
    private final AlphanumComparator alphanumComparator = new AlphanumComparator();

    public ValuesTable(final ObservableList<Values> tableData) {
        setBorder(null);
        setStyle(
                ".table-view:focused {" +
                        "-fx-padding: 0; " +
                        "-fx-background-color: transparent, -fx-box-border, -fx-control-inner-background; " +
                        "-fx-background-insets: -1.4,0,1;" +
                        "}");

        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tableData.sort((o1, o2) -> alphanumComparator.compare(o1.getName(), o2.getName()));


        /**
         * Table Column 0
         */
        nameCol = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.name"));
        nameCol.setCellValueFactory(new PropertyValueFactory<Values, String>("name"));

        nameCol.setCellFactory(new Callback<TableColumn<Values, String>, TableCell<Values, String>>() {
            @Override
            public TableCell<Values, String> call(TableColumn<Values, String> param) {
                return new TableCell<Values, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        super.setText(item);
                        super.setGraphic(null);

                        this.setTooltip(new Tooltip(item));
                    }
                };
            }
        });
        nameCol.setSortable(true);
        nameCol.setPrefWidth(500);
        nameCol.setMinWidth(100);

        setItems(tableData);

        /**
         * Table Column 1
         */
        TableColumn<Values, String> minCol = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.min"));
        minCol.setCellValueFactory(new PropertyValueFactory<Values, String>("min"));
        minCol.setStyle("-fx-alignment: CENTER-RIGHT");
        minCol.setSortable(true);
        minCol.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
        minCol.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

        /**
         * Table Column 2
         */
        TableColumn<Values, String> maxCol = new TableColumn<Values, String>(I18n.getInstance().getString("plugin.graph.table.max"));
        maxCol.setCellValueFactory(new PropertyValueFactory<Values, String>("max"));
        maxCol.setStyle("-fx-alignment: CENTER-RIGHT");
        maxCol.setSortable(true);
        maxCol.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
        maxCol.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

        /**
         * Table Column 3
         */
        TableColumn<Values, String> avgCol = new TableColumn<Values, String>(I18n.getInstance().getString("plugin.graph.table.avg"));
        avgCol.setCellValueFactory(new PropertyValueFactory<Values, String>("avg"));
        avgCol.setStyle("-fx-alignment: CENTER-RIGHT");
        avgCol.setSortable(true);
        avgCol.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
        avgCol.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

        /**
         * Table Column 4
         */
        TableColumn<Values, String> sumCol = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.sum"));
        sumCol.setCellValueFactory(new PropertyValueFactory<Values, String>("sum"));
        sumCol.setStyle("-fx-alignment: CENTER-RIGHT");
        sumCol.setSortable(true);
        sumCol.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
        sumCol.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

        /**
         * Table Column 5
         */
        TableColumn<Values, String> sampleCountCol = new TableColumn<>(I18n.getInstance().getString("plugin.object.attribute.overview.totalsamplecount"));
        sampleCountCol.setCellValueFactory(new PropertyValueFactory<Values, String>("count"));
        sampleCountCol.setStyle("-fx-alignment: CENTER-RIGHT");
        sampleCountCol.setSortable(true);
        sampleCountCol.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
        sampleCountCol.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

        /**
         * Table Column 6
         */
        TableColumn<Values, String> zerosCountCol = new TableColumn<>(I18n.getInstance().getString("plugin.object.attribute.overview.zerovalues"));
        zerosCountCol.setCellValueFactory(new PropertyValueFactory<Values, String>("zeros"));
        zerosCountCol.setStyle("-fx-alignment: CENTER-RIGHT");
        zerosCountCol.setSortable(true);
        zerosCountCol.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
        zerosCountCol.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

        getColumns().addAll(nameCol, minCol, maxCol, avgCol, sumCol, sampleCountCol, zerosCountCol);
        setTableMenuButtonVisible(true);

        widthProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(this::updateColumnCaptionWidths);
        });

    }

    public void autoFitTable() {
        for (Object column : getColumns()) {
            try {
                columnToFitMethod.invoke(getSkin(), column, -1);
            } catch (Exception ignored) {
            }
        }
    }

    public void updateColumnCaptionWidths() {
        TableViewUtils.allToMin(this);
        TableViewUtils.growColumns(this, Collections.singletonList(nameCol));
    }
}
