package org.jevis.jeconfig.plugin.nonconformities.ui;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityData;
import org.jevis.jeconfig.plugin.nonconformities.data.TableFilter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Method;


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
    private ObservableList<String> status = FXCollections.observableArrayList();
    private ObservableList<String> medium = FXCollections.observableArrayList();
    private ObservableList<String> field = FXCollections.observableArrayList();
    private DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
    private TableFilter tableFilter = new TableFilter();
    private NonconformityData sumRow = new NonconformityData();
    private boolean showSumRow = false;

    public NonconformitiesTable(ObservableList<NonconformityData> data) {
        this.data = data;
        this.filteredData = new FilteredList<>(data);
        setItems(filteredData);

        NonconformityData fakeForName = new NonconformityData();

        TableColumn<NonconformityData, String> fromUserCol = new TableColumn(fakeForName.creatorProperty().getName());
        fromUserCol.setCellValueFactory(param -> param.getValue().creatorProperty());
        fromUserCol.setCellFactory(buildShotTextFactory());





        TableColumn<NonconformityData, String> responsiblePropertyCol = new TableColumn(fakeForName.responsiblePerson.getName());
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

    public void setFilterStatus(ObservableList<String> status) {
        this.status = status;
    }

    public void setFilterMedium(ObservableList<String> medium) {
        this.medium = medium;
    }

    public void setFilterField(ObservableList<String> field) {
        this.field = field;
    }

//    public void filter() {
//        System.out.println("Filter: " + status);
//        //System.out.println("---------------------------------------------------------------------------------------------");
//        //System.out.println("Searchabr: " + searchTextProperty.get());
//        //System.out.println("Finter: " + searchTextProperty.get() + " U: " + searchInUser.get() + " O: " + searchInDataRow.get() + " N: " + searchInNote.get());
//        //System.out.println("List: " + data.size());
//        filteredData.setPredicate(
//                new Predicate<NonconformityData>() {
//                    @Override
//                    public boolean test(NonconformityData notesRow) {
//                        //System.out.println("Filter.predict: " + notesRow.getTags());
//                        try {
//
//
//                            AtomicBoolean statusMatch = new AtomicBoolean(false);
//                            status.forEach(s -> {
//                                try {
//                                    for (String s1 : notesRow.statusTagsProperty().get().split(";")) {
//                                        if (s1.equalsIgnoreCase(s)) {
//                                            statusMatch.set(true);
//                                        }
//                                    }
//                                } catch (Exception ex) {
//
//                                }
//                            });
//                            System.out.println("Status Match: " + statusMatch.get());
//
//                            AtomicBoolean mediumMatch = new AtomicBoolean(false);
//                            medium.forEach(s -> {
//                                try {
//                                    for (String s1 : notesRow.mediaTagsProperty().get().split(";")) {
//                                        if (s1.equalsIgnoreCase(s)) {
//                                            mediumMatch.set(true);
//                                        }
//                                    }
//                                } catch (Exception ex) {
//
//                                }
//                            });
//
//                            AtomicBoolean fieldMatch = new AtomicBoolean(false);
//                            field.forEach(s -> {
//                                try {
//                                    for (String s1 : notesRow.fieldTagsProperty().get().split(";")) {
//                                        if (s1.equalsIgnoreCase(s)) {
//                                            fieldMatch.set(true);
//                                        }
//                                    }
//                                } catch (Exception ex) {
//
//                                }
//                            });
//
//
//                            System.out.println("statusMatch.get(): " + statusMatch.get() + "  mediumMatch.get():" + mediumMatch.get() + "  fieldMatch.get():" + fieldMatch.get());
//                            if (statusMatch.get() && mediumMatch.get() && fieldMatch.get()) {//&& fieldMatch.get()
//                                System.out.println("-> true");
//                                return true;
//                            }
//
//                            System.out.println("-> false");
//                            return false;
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                        }
//
//                        return false;
//                    }
//                });
//        //Platform.runLater(() -> autoFitTable(tableView));
//        Platform.runLater(() -> sort());
//
//    }


}
