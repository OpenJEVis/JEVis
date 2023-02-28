package org.jevis.jeconfig.plugin.nonconformities.ui;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.jevis.jeconfig.plugin.action.data.ActionData;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityData;
import org.jevis.jeconfig.plugin.nonconformities.data.TableFilter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;


public class NonconformitiesTable extends TableView<NonconformityData> {


    private static Method columnToFitMethod;

    static {
        try {
            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
            columnToFitMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    ObservableList<NonconformityData> data = FXCollections.observableArrayList();
    FilteredList<NonconformityData> filteredData;
    private DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
    private TableFilter tableFilter = new TableFilter();
    private NonconformityData sumRow = new NonconformityData();
    private DateFilter dateFilter;
    private boolean showSumRow = false;
    private String containsTextFilter = "";

    public NonconformitiesTable(ObservableList<NonconformityData> data) {
        this.data = data;
        this.filteredData = new FilteredList<>(data);
        setItems(filteredData);

        NonconformityData fakeForName = new NonconformityData();

        TableColumn<NonconformityData, String> fromUserCol = new TableColumn(fakeForName.creatorProperty().getName());
        fromUserCol.setCellValueFactory(param -> param.getValue().creatorProperty());
        fromUserCol.setCellFactory(buildShotTextFactory());





        TableColumn<NonconformityData, String> responsiblePropertyCol = new TableColumn(fakeForName.responsiblePersonProperty().getName());
        responsiblePropertyCol.setCellValueFactory(param -> param.getValue().responsiblePersonProperty());

        TableColumn<NonconformityData, Integer> actionNrPropertyCol = new TableColumn(fakeForName.nrProperty().getName());
        actionNrPropertyCol.setCellValueFactory(param -> param.getValue().nrProperty().asObject());

        TableColumn<NonconformityData, String> desciptionPropertyCol = new TableColumn(fakeForName.descriptionProperty().getName());
        desciptionPropertyCol.setCellValueFactory(param -> param.getValue().descriptionProperty());
        desciptionPropertyCol.setCellFactory(buildShotTextFactory());

        TableColumn<NonconformityData, String> causePropertyCol = new TableColumn(fakeForName.causeProperty().getName());
        causePropertyCol.setCellValueFactory(param -> param.getValue().causeProperty());
        causePropertyCol.setCellFactory(buildShotTextFactory());

        TableColumn<NonconformityData, String> immediateMeasuresPropertyCol = new TableColumn(fakeForName.immediateMeasuresProperty().getName());
        immediateMeasuresPropertyCol.setCellValueFactory(param -> param.getValue().immediateMeasuresProperty());
        immediateMeasuresPropertyCol.setCellFactory(buildShotTextFactory());

        TableColumn<NonconformityData, String> correctiveActionsCol = new TableColumn(fakeForName.correctiveActionsProperty().getName());
        correctiveActionsCol.setCellValueFactory(param -> param.getValue().correctiveActionsProperty());
        correctiveActionsCol.setCellFactory(buildShotTextFactory());








        TableColumn<NonconformityData, DateTime> doneDatePropertyCol = new TableColumn(fakeForName.doneDateProperty().getName());
        doneDatePropertyCol.setCellValueFactory(param -> param.getValue().doneDateProperty());
        doneDatePropertyCol.setCellFactory(buildDateTimeFactory());

        TableColumn<NonconformityData, DateTime> createDatePropertyCol = new TableColumn(fakeForName.createDateProperty().getName());
        createDatePropertyCol.setCellValueFactory(param -> param.getValue().createDateProperty());
        createDatePropertyCol.setCellFactory(buildDateTimeFactory());

        TableColumn<NonconformityData, DateTime> plannedDatePropertyCol = new TableColumn(fakeForName.plannedDateProperty().getName());
        plannedDatePropertyCol.setCellValueFactory(param -> param.getValue().plannedDateProperty());
        plannedDatePropertyCol.setCellFactory(buildDateTimeFactory());











        TableColumn<NonconformityData, String> titlePropertyCol = new TableColumn(fakeForName.titleProperty().getName());
        titlePropertyCol.setCellValueFactory(param -> param.getValue().titleProperty());
        titlePropertyCol.setCellFactory(buildShotTextFactory());





        actionNrPropertyCol.setVisible(true);
        fromUserCol.setVisible(false);
        responsiblePropertyCol.setVisible(true);
        desciptionPropertyCol.setVisible(false);
        causePropertyCol.setVisible(false);
        immediateMeasuresPropertyCol.setVisible(false);

        createDatePropertyCol.setVisible(false);
        titlePropertyCol.setVisible(true);

        doneDatePropertyCol.setVisible(true);
        plannedDatePropertyCol.setVisible(true);


        this.tableMenuButtonVisibleProperty().set(true);

        this.getColumns().addAll(actionNrPropertyCol, titlePropertyCol, fromUserCol,
                responsiblePropertyCol, desciptionPropertyCol,
                plannedDatePropertyCol, doneDatePropertyCol, createDatePropertyCol
        );


        this.getColumns().stream().forEach(tableDataTableColumn -> tableDataTableColumn.setSortable(true));
        this.getVisibleLeafColumns().addListener((ListChangeListener<TableColumn<NonconformityData, ?>>) c -> {
            while (c.next()) autoFitTable();
        });


    }

    public void enableSumRow(boolean enable) {
        showSumRow = enable;
        if (enable) {
            sumRow.nrProperty().set(Integer.MAX_VALUE);
            data.add(sumRow);
        } else {
            data.remove(sumRow);
        }
    }

    public void autoFitTable() {
        for (TableColumn<NonconformityData, ?> column : this.getColumns()) {
            try {
                if (getSkin() != null) {
                    columnToFitMethod.invoke(getSkin(), column, -1);
                }
            } catch (Exception e) {
            }
        }
    }

    private Callback<TableColumn<NonconformityData, String>, TableCell<NonconformityData, String>> buildShotTextFactory() {


        return new Callback<TableColumn<NonconformityData, String>, TableCell<NonconformityData, String>>() {
            @Override
            public TableCell<NonconformityData, String> call(TableColumn<NonconformityData, String> param) {
                return new TableCell<NonconformityData, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setTextOverrun(OverrunStyle.ELLIPSIS);
                        setWrapText(false);
                        setGraphic(null);
                        if (!empty) {
                            if (item.contains("\n")) {
                                setText(item.substring(0, item.indexOf("\n")));
                            } else {
                                setText(item);
                            }
                        } else {
                            setText(null);
                        }

                    }
                };
            }
        };

    }


    private Callback<TableColumn<NonconformityData, DateTime>, TableCell<NonconformityData, DateTime>> buildDateTimeFactory() {
        return new Callback<TableColumn<NonconformityData, DateTime>, TableCell<NonconformityData, DateTime>>() {
            @Override
            public TableCell<NonconformityData, DateTime> call(TableColumn<NonconformityData, DateTime> param) {
                return new TableCell<NonconformityData, DateTime>() {
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
        };

    }

    public TableFilter getTableFilter() {
        return tableFilter;
    }
    public void setTextFilter(String containsText) {
        this.containsTextFilter = containsText;
    }

    public void setDateFilter(DateFilter filter) {
        this.dateFilter = filter;
    }



    public void filter() {
        filteredData.setPredicate(
                new Predicate<NonconformityData>() {
                    @Override
                    public boolean test(NonconformityData notesRow) {
                        //System.out.println("Filter.predict: " + notesRow.getTags());
                        try {


                            if (dateFilter != null) {
                                if (!dateFilter.show(notesRow)) return false;
                            }


                            AtomicBoolean containString = new AtomicBoolean(false);
                            if (containsTextFilter != null || containsTextFilter.isEmpty()) {
                                if (notesRow.title.get().toLowerCase().contains(containsTextFilter.toLowerCase())
                                        || notesRow.getResponsiblePerson().toLowerCase().contains(containsTextFilter.toLowerCase())
                                        || notesRow.getCreator().toLowerCase().contains(containsTextFilter.toLowerCase())
                                        || notesRow.title.get().toLowerCase().contains(containsTextFilter.toLowerCase())
                                        || notesRow.getCause().toLowerCase().contains(containsTextFilter.toLowerCase())
                                        || notesRow.getDescription().toLowerCase().contains(containsTextFilter.toLowerCase())
                                        || notesRow.getImmediateMeasures().toLowerCase().contains(containsTextFilter.toLowerCase())
                                        || notesRow.getCorrectiveActions().toLowerCase().contains(containsTextFilter.toLowerCase())) {
                                    containString.set(true);
                                }

                                //TODO: may also check if column is visible
                                if (!containString.get()) return false;
                            }


                            return true;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        return false;
                    }
                });
        //Platform.runLater(() -> autoFitTable(tableView));
        Platform.runLater(() -> sort());

    }



}
