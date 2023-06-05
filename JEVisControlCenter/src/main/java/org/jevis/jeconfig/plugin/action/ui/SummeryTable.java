package org.jevis.jeconfig.plugin.action.ui;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

public class SummeryTable extends TableView<SummeryData> {

    private static Method columnToFitMethod;

    static {
        try {
            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
            columnToFitMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private final TableView dataTable;


    public SummeryTable(TableView dataTable) {
        this.dataTable = dataTable;
        this.setPrefHeight(130);
        this.getStylesheets().add("/styles/SummeryTable.css");

        this.setWidth(dataTable.getWidth());

        initColumns();

        Platform.runLater(() -> {
            TableColumn colum = (TableColumn) dataTable.getColumns().get(0);
            colum.setMinWidth(80);
        });

        dataTable.getColumns().addListener((ListChangeListener) c -> {
            while (c.next()) {
            }
            this.getColumns().clear();
            initColumns();
        });

        getItems().addListener(new ListChangeListener<SummeryData>() {
            @Override
            public void onChanged(Change<? extends SummeryData> c) {
                while (c.next()) {
                }
                setMinHeight(getItems().size() * 27);
            }
        });
        autoFitTable();
    }

    private void initColumns() {
        AtomicInteger col = new AtomicInteger(0);
        dataTable.getColumns().forEach(o -> {
            col.set(col.get() + 1);
            TableColumn column = (TableColumn) o;

            TableColumn<SummeryData, String> cellCopie = new TableColumn(column.getText());
            // actionNrPropertyCol.setCellValueFactory;
            cellCopie.setCellValueFactory(param -> param.getValue().getProperty(column));
            //cellCopie.setCellValueFactory(param -> param.getValue().getFunction(column).getValue());

            /* To Calculate the text wight and set the DataColumn minWight

            Text helper = new Text();
                                helper.setFont(getFont());
                                helper.setText(item);
                                helper.prefWidth(-1);
                                helper.setWrappingWidth(0);
                                helper.setLineSpacing(0);
                                System.out.println("ddddddddddddddd: " + Math.ceil(helper.getLayoutBounds().getWidth()));
             */

            /*
            cellCopie.setCellFactory(new Callback<TableColumn<SummeryData, String>, TableCell<SummeryData, String>>() {
                @Override
                public TableCell<SummeryData, String> call(TableColumn<SummeryData, String> param) {
                    return new TableCell<SummeryData, String>() {


                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if (!empty) {
                                setText(item);
                            } else {
                                setText(null);
                            }


                        }
                    };
                }
            });


             */

            cellCopie.setStyle("-fx-alignment: top-right;-fx-font-weight: bold; -fx-text-alignment: right;");
            cellCopie.setSortable(false);
            //cellCopie.setSortType(TableColumn.SortType.ASCENDING);
            cellCopie.setMinWidth(column.getMinWidth());
            cellCopie.setPrefWidth(column.getPrefWidth());
            cellCopie.setMaxWidth(column.getMaxWidth());
            cellCopie.setMinWidth(column.getWidth());


            column.widthProperty().addListener((observable, oldValue, newValue) -> {
                cellCopie.setMinWidth(newValue.doubleValue());
                cellCopie.setMaxWidth(newValue.doubleValue());
                try {
                    //  columnToFitMethod.invoke(getSkin(), cellCopie, -1);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });


            cellCopie.visibleProperty().bindBidirectional(column.visibleProperty());
            this.getColumns().add(cellCopie);

        });

    }

    public void autoFitTable() {
        if (true) return;
        for (TableColumn<?, ?> column : this.getColumns()) {
            try {
                if (column.getId().equals("Note") || column.getId().equals("Title") || column.getId().equals("Desciption")) {
                    /*ignore this columns for now, there are to big to autoresize*/
                } else {
                    if (getSkin() != null) {
                        columnToFitMethod.invoke(getSkin(), column, -1);
                    }
                }


            } catch (Exception e) {
            }
        }
    }


}
