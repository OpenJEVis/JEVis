package org.jevis.jeconfig.plugin.action.ui;

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
import org.jevis.jeconfig.plugin.action.data.TableFilter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;


public class ActionTable extends TableView<ActionData> {


    private static Method columnToFitMethod;

    static {
        try {
            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
            columnToFitMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    ObservableList<ActionData> data = FXCollections.observableArrayList();
    FilteredList<ActionData> filteredData;
    private ObservableList<String> status = FXCollections.observableArrayList();
    private ObservableList<String> medium = FXCollections.observableArrayList();
    private ObservableList<String> field = FXCollections.observableArrayList();
    private DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
    private TableFilter tableFilter = new TableFilter();
    private ActionData sumRow = new ActionData();
    private boolean showSumRow = false;

    public ActionTable(ObservableList<ActionData> data) {
        this.data = data;
        this.filteredData = new FilteredList<>(data);
        setItems(filteredData);

        ActionData fakeForName = new ActionData();

        TableColumn<ActionData, String> fromUserCol = new TableColumn(fakeForName.fromUserProperty().getName());
        fromUserCol.setCellValueFactory(param -> param.getValue().fromUserProperty());
        fromUserCol.setCellFactory(buildShotTextFactory());


        TableColumn<ActionData, String> responsiblePropertyCol = new TableColumn(fakeForName.responsibleProperty().getName());
        responsiblePropertyCol.setCellValueFactory(param -> param.getValue().responsibleProperty());

        TableColumn<ActionData, Integer> actionNrPropertyCol = new TableColumn(fakeForName.nrProperty().getName());
        actionNrPropertyCol.setCellValueFactory(param -> param.getValue().nrProperty().asObject());

        TableColumn<ActionData, String> desciptionPropertyCol = new TableColumn(fakeForName.desciptionProperty().getName());
        desciptionPropertyCol.setCellValueFactory(param -> param.getValue().desciptionProperty());
        desciptionPropertyCol.setCellFactory(buildShotTextFactory());

        TableColumn<ActionData, String> notePropertyCol = new TableColumn(fakeForName.noteProperty().getName());
        notePropertyCol.setCellValueFactory(param -> param.getValue().noteProperty());
        notePropertyCol.setCellFactory(buildShotTextFactory());

        TableColumn<ActionData, String> mediaTagsPropertyCol = new TableColumn(fakeForName.mediaTagsProperty().getName());
        mediaTagsPropertyCol.setCellValueFactory(param -> param.getValue().mediaTagsProperty());

        TableColumn<ActionData, String> statusTagsPropertyCol = new TableColumn(fakeForName.statusTagsProperty().getName());
        statusTagsPropertyCol.setCellValueFactory(param -> param.getValue().statusTagsProperty());

        TableColumn<ActionData, DateTime> doneDatePropertyCol = new TableColumn(fakeForName.doneDateProperty().getName());
        doneDatePropertyCol.setCellValueFactory(param -> param.getValue().doneDateProperty());
        doneDatePropertyCol.setCellFactory(buildDateTimeFactory());

        TableColumn<ActionData, DateTime> createDatePropertyCol = new TableColumn(fakeForName.createDateProperty().getName());
        createDatePropertyCol.setCellValueFactory(param -> param.getValue().createDateProperty());
        createDatePropertyCol.setCellFactory(buildDateTimeFactory());

        TableColumn<ActionData, DateTime> plannedDatePropertyCol = new TableColumn(fakeForName.plannedDateProperty().getName());
        plannedDatePropertyCol.setCellValueFactory(param -> param.getValue().plannedDateProperty());
        plannedDatePropertyCol.setCellFactory(buildDateTimeFactory());


        TableColumn<ActionData, String> noteAlternativeMeasuresPropertyCol = new TableColumn(fakeForName.noteAlternativeMeasuresProperty().getName());
        noteAlternativeMeasuresPropertyCol.setCellValueFactory(param -> param.getValue().noteAlternativeMeasuresProperty());
        noteAlternativeMeasuresPropertyCol.setCellFactory(buildShotTextFactory());

        TableColumn<ActionData, String> noteBewertetPropertyCol = new TableColumn(fakeForName.noteBewertetProperty().getName());
        noteBewertetPropertyCol.setCellValueFactory(param -> param.getValue().noteBewertetProperty());
        noteBewertetPropertyCol.setCellFactory(buildShotTextFactory());

        TableColumn<ActionData, String> noteCorrectionPropertyCol = new TableColumn(fakeForName.noteCorrectionProperty().getName());
        noteCorrectionPropertyCol.setCellValueFactory(param -> param.getValue().noteCorrectionProperty());
        noteCorrectionPropertyCol.setCellFactory(buildShotTextFactory());

        TableColumn<ActionData, String> noteBetroffenerProzessPropertyCol = new TableColumn(fakeForName.noteBetroffenerProzessProperty().getName());
        noteBetroffenerProzessPropertyCol.setCellValueFactory(param -> param.getValue().noteBetroffenerProzessProperty());
        noteBetroffenerProzessPropertyCol.setCellFactory(buildShotTextFactory());

        TableColumn<ActionData, String> noteEnergieflussPropertyCol = new TableColumn(fakeForName.noteEnergieflussProperty().getName());
        noteEnergieflussPropertyCol.setCellValueFactory(param -> param.getValue().noteEnergieflussProperty());
        noteEnergieflussPropertyCol.setCellFactory(buildShotTextFactory());

        TableColumn<ActionData, String> noteFollowUpActionPropertyCol = new TableColumn(fakeForName.noteFollowUpActionProperty().getName());
        noteFollowUpActionPropertyCol.setCellValueFactory(param -> param.getValue().noteFollowUpActionProperty());
        noteFollowUpActionPropertyCol.setCellFactory(buildShotTextFactory());

        TableColumn<ActionData, String> titlePropertyCol = new TableColumn(fakeForName.titleProperty().getName());
        titlePropertyCol.setCellValueFactory(param -> param.getValue().titleProperty());
        titlePropertyCol.setCellFactory(buildShotTextFactory());

        TableColumn<ActionData, String> investPropertyCol = new TableColumn(fakeForName.investmentProperty().getName());
        investPropertyCol.setCellValueFactory(param -> param.getValue().investmentProperty());
        //investPropertyCol.setCellFactory(buildShotTextFactory());
        investPropertyCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        investPropertyCol.setCellFactory(new DoubleColumnCell());

        TableColumn<ActionData, String> savingYearPropertyCol = new TableColumn(fakeForName.savingyearProperty().getName());
        savingYearPropertyCol.setCellValueFactory(param -> param.getValue().savingyearProperty());
        savingYearPropertyCol.setCellFactory(buildShotTextFactory());
        savingYearPropertyCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        savingYearPropertyCol.setCellFactory(new DoubleColumnCell());

        actionNrPropertyCol.setVisible(true);
        fromUserCol.setVisible(false);
        responsiblePropertyCol.setVisible(true);
        desciptionPropertyCol.setVisible(false);
        notePropertyCol.setVisible(true);
        createDatePropertyCol.setVisible(false);
        titlePropertyCol.setVisible(true);
        mediaTagsPropertyCol.setVisible(true);
        statusTagsPropertyCol.setVisible(true);
        doneDatePropertyCol.setVisible(true);
        plannedDatePropertyCol.setVisible(true);
        noteAlternativeMeasuresPropertyCol.setVisible(false);
        noteBewertetPropertyCol.setVisible(false);
        noteCorrectionPropertyCol.setVisible(false);
        noteBetroffenerProzessPropertyCol.setVisible(false);
        noteEnergieflussPropertyCol.setVisible(false);
        noteFollowUpActionPropertyCol.setVisible(false);
        investPropertyCol.setVisible(true);
        savingYearPropertyCol.setVisible(true);

        this.tableMenuButtonVisibleProperty().set(true);

        this.getColumns().addAll(actionNrPropertyCol, titlePropertyCol, fromUserCol,
                responsiblePropertyCol, desciptionPropertyCol, notePropertyCol,
                mediaTagsPropertyCol, statusTagsPropertyCol,
                plannedDatePropertyCol, doneDatePropertyCol, createDatePropertyCol, noteAlternativeMeasuresPropertyCol, noteBewertetPropertyCol,
                noteCorrectionPropertyCol, noteBetroffenerProzessPropertyCol, noteEnergieflussPropertyCol, noteFollowUpActionPropertyCol,
                investPropertyCol, savingYearPropertyCol
        );


        this.getColumns().stream().forEach(tableDataTableColumn -> tableDataTableColumn.setSortable(true));
        this.getVisibleLeafColumns().addListener((ListChangeListener<TableColumn<ActionData, ?>>) c -> {
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
        for (TableColumn<ActionData, ?> column : this.getColumns()) {
            try {
                if (getSkin() != null) {
                    columnToFitMethod.invoke(getSkin(), column, -1);
                }
            } catch (Exception e) {
            }
        }
    }

    private Callback<TableColumn<ActionData, String>, TableCell<ActionData, String>> buildShotTextFactory() {


        return new Callback<TableColumn<ActionData, String>, TableCell<ActionData, String>>() {
            @Override
            public TableCell<ActionData, String> call(TableColumn<ActionData, String> param) {
                return new TableCell<ActionData, String>() {
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


    private Callback<TableColumn<ActionData, DateTime>, TableCell<ActionData, DateTime>> buildDateTimeFactory() {
        return new Callback<TableColumn<ActionData, DateTime>, TableCell<ActionData, DateTime>>() {
            @Override
            public TableCell<ActionData, DateTime> call(TableColumn<ActionData, DateTime> param) {
                return new TableCell<ActionData, DateTime>() {
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

    public void filter() {
        System.out.println("Filter: " + status);
        //System.out.println("---------------------------------------------------------------------------------------------");
        //System.out.println("Searchabr: " + searchTextProperty.get());
        //System.out.println("Finter: " + searchTextProperty.get() + " U: " + searchInUser.get() + " O: " + searchInDataRow.get() + " N: " + searchInNote.get());
        //System.out.println("List: " + data.size());
        filteredData.setPredicate(
                new Predicate<ActionData>() {
                    @Override
                    public boolean test(ActionData notesRow) {
                        //System.out.println("Filter.predict: " + notesRow.getTags());
                        try {


                            AtomicBoolean statusMatch = new AtomicBoolean(false);
                            status.forEach(s -> {
                                try {
                                    for (String s1 : notesRow.statusTagsProperty().get().split(";")) {
                                        if (s1.equalsIgnoreCase(s)) {
                                            statusMatch.set(true);
                                        }
                                    }
                                } catch (Exception ex) {

                                }
                            });
                            System.out.println("Status Match: " + statusMatch.get());

                            AtomicBoolean mediumMatch = new AtomicBoolean(false);
                            medium.forEach(s -> {
                                try {
                                    for (String s1 : notesRow.mediaTagsProperty().get().split(";")) {
                                        if (s1.equalsIgnoreCase(s)) {
                                            mediumMatch.set(true);
                                        }
                                    }
                                } catch (Exception ex) {

                                }
                            });

                            AtomicBoolean fieldMatch = new AtomicBoolean(false);
                            field.forEach(s -> {
                                try {
                                    for (String s1 : notesRow.fieldTagsProperty().get().split(";")) {
                                        if (s1.equalsIgnoreCase(s)) {
                                            fieldMatch.set(true);
                                        }
                                    }
                                } catch (Exception ex) {

                                }
                            });


                            System.out.println("statusMatch.get(): " + statusMatch.get() + "  mediumMatch.get():" + mediumMatch.get() + "  fieldMatch.get():" + fieldMatch.get());
                            if (statusMatch.get() && mediumMatch.get() && fieldMatch.get()) {//&& fieldMatch.get()
                                System.out.println("-> true");
                                return true;
                            }

                            System.out.println("-> false");
                            return false;
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
