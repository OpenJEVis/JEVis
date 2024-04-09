package org.jevis.jeconfig.application.table;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SummeryTable extends TableView<SummeryData> implements TableFindScrollbar {

    public static final String COLUMN_SEPARATOR = "#";
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


        autoFitTable();
    }


    private void initColumns() {
        AtomicInteger col = new AtomicInteger(0);
        dataTable.getColumns().forEach(o -> {
            col.set(col.get() + 1);
            TableColumn column = (TableColumn) o;

            TableColumn<SummeryData, String> cellCopie = new TableColumn(column.getText());
            cellCopie.setCellValueFactory(param -> param.getValue().getProperty(column));

            cellCopie.setStyle("-fx-alignment: top-right;-fx-font-weight: bold; -fx-text-alignment: right;");

            cellCopie.setSortable(false);
            //cellCopie.setSortType(TableColumn.SortType.ASCENDING);
            cellCopie.setMinWidth(column.getMinWidth());
            cellCopie.setPrefWidth(column.getPrefWidth());
            cellCopie.setMaxWidth(column.getMaxWidth());
            cellCopie.setMinWidth(column.getWidth());
            Map<Integer, DoubleProperty> innerColumns = new HashMap<>();

            cellCopie.setCellFactory(tc -> {
                TableCell cell = new TableCell<String, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            //setText(item);

                            if (item.contains(COLUMN_SEPARATOR)) {
                                HBox hBox = new HBox();
                                VBox vBox = new VBox(hBox);
                                vBox.setFillWidth(true);
                                //vBox.setAlignment(Pos.CENTER_RIGHT);
                                hBox.setSpacing(3);
                                hBox.setAlignment(Pos.CENTER_RIGHT);
                                hBox.setFillHeight(true);

                                String[] parts = item.split(COLUMN_SEPARATOR);
                                for (int i = 0; i < parts.length; i++) {
                                    if (!innerColumns.containsKey(i)) {
                                        innerColumns.put(i, new SimpleDoubleProperty(0));
                                    }

                                    Label iLabel = new Label(parts[i]);
                                    AtomicInteger atomicInteger = new AtomicInteger(i);
                                    iLabel.setMinWidth(innerColumns.get(atomicInteger.get()).get());

                                    iLabel.widthProperty().addListener((observableValue, number, t1) -> {
                                        if (t1.doubleValue() > innerColumns.get(atomicInteger.get()).get()) {
                                            innerColumns.get(atomicInteger.get()).set(t1.doubleValue());
                                        }
                                    });
                                    innerColumns.get(atomicInteger.get()).addListener((observableValue, number, t1) -> {
                                        iLabel.setMinWidth(t1.doubleValue());
                                    });

                                    iLabel.setAlignment(Pos.CENTER_RIGHT);
                                    hBox.getChildren().add(iLabel);
                                }
                                setText(null);
                                setGraphic(hBox);

                            } else {
                                setText(item);
                                setGraphic(null);
                            }

                        }
                    }
                };
                return cell;
            });

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

    public ObjectProperty<EventHandler<ScrollToEvent<Integer>>> getScrollToProperty() {
        return this.getScrollToProperty();
    }


}
