package org.jevis.jeconfig.plugin.action.ui;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.action.data.ActionData;
import org.jevis.jeconfig.plugin.action.data.ActionPlanData;
import org.jevis.jeconfig.plugin.action.data.TableFilter;
import org.jevis.jeconfig.plugin.action.ui.control.CurrencyColumnCell;
import org.jevis.jeconfig.plugin.action.ui.control.StringListColumnCell;
import org.jevis.jeconfig.plugin.action.ui.control.TagButton;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;


public class ActionTable extends TableView<ActionData> {

    private static final Logger logger = LogManager.getLogger(ActionTable.class);

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
    private ObservableList<String> statusFilter = FXCollections.observableArrayList();
    private ObservableList<String> mediumFilter = FXCollections.observableArrayList();
    private ObservableList<String> fieldFilter = FXCollections.observableArrayList();
    private ObservableList<String> fieldSEU = FXCollections.observableArrayList();
    private ObservableList<String> planFilters = FXCollections.observableArrayList();
    private DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
    private TableFilter tableFilter = new TableFilter();
    // private ActionData sumRow = new ActionData();
    private DateFilter dateFilter;
    private boolean showSumRow = false;
    private String containsTextFilter = "";

    NumberFormat currencyFormat = NumberFormat.getNumberInstance();

    public ActionTable(ActionPlanData actionPlanData, ObservableList<ActionData> data) {
        this.data = data;
        this.filteredData = new FilteredList<>(this.data);

        SortedList sortedList = new SortedList(this.filteredData);
        setItems(sortedList);
        sortedList.comparatorProperty().bind(this.comparatorProperty());

        setId("Action Table");
        setTableMenuButtonVisible(true);

        ActionData fakeForName = new ActionData();
        TableColumn<ActionData, String> fromUserCol = new TableColumn(fakeForName.fromUserProperty().getName());
        fromUserCol.setCellValueFactory(param -> param.getValue().fromUserProperty());
        fromUserCol.setCellFactory(buildShotTextFactory());


        TableColumn<ActionData, String> responsiblePropertyCol = new TableColumn(fakeForName.responsibleProperty().getName());
        responsiblePropertyCol.setCellValueFactory(param -> param.getValue().responsibleProperty());

        TableColumn<ActionData, String> actionNrPropertyCol = new TableColumn(fakeForName.nrProperty().getName());
        actionNrPropertyCol.setCellValueFactory(param -> param.getValue().nrTextProperty());
        actionNrPropertyCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        actionNrPropertyCol.setSortable(true);
        actionNrPropertyCol.setSortType(TableColumn.SortType.ASCENDING);


        TableColumn<ActionData, String> desciptionPropertyCol = new TableColumn(fakeForName.desciptionProperty().getName());
        desciptionPropertyCol.setCellValueFactory(param -> param.getValue().desciptionProperty());
        desciptionPropertyCol.setCellFactory(buildShotTextFactory());

        TableColumn<ActionData, String> planNameCol = new TableColumn(I18n.getInstance().getString("plugin.action.filter.plan"));
        planNameCol.setCellValueFactory(param -> param.getValue().getActionPlan().getName());
        planNameCol.setCellFactory(buildShotTextFactory());

        TableColumn<ActionData, String> notePropertyCol = new TableColumn(fakeForName.noteProperty().getName());
        notePropertyCol.setCellValueFactory(param -> param.getValue().noteProperty());
        notePropertyCol.setCellFactory(buildShotTextFactory());

        TableColumn<ActionData, String> mediaTagsPropertyCol = new TableColumn(fakeForName.mediaTagsProperty().getName());
        mediaTagsPropertyCol.setCellValueFactory(param -> param.getValue().mediaTagsProperty());
        mediaTagsPropertyCol.setCellFactory(new StringListColumnCell());
        mediaTagsPropertyCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<ActionData, String> statusTagsPropertyCol = new TableColumn(fakeForName.statusTagsProperty().getName());
        statusTagsPropertyCol.setCellValueFactory(param -> param.getValue().statusTagsProperty());
        statusTagsPropertyCol.setCellFactory(new StringListColumnCell());
        statusTagsPropertyCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<ActionData, String> fieldTagsPropertyCol = new TableColumn(fakeForName.fieldTagsProperty().getName());
        fieldTagsPropertyCol.setCellValueFactory(param -> param.getValue().fieldTagsProperty());
        fieldTagsPropertyCol.setCellFactory(new StringListColumnCell());
        fieldTagsPropertyCol.setStyle("-fx-alignment: CENTER;");

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

        TableColumn<ActionData, Double> investPropertyCol = new TableColumn(fakeForName.npv.get().investment.getName());
        investPropertyCol.setCellValueFactory(param -> param.getValue().npv.get().investment.asObject());
        //investPropertyCol.setCellFactory(buildShotTextFactory());
        investPropertyCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        investPropertyCol.setCellFactory(new CurrencyColumnCell());

        TableColumn<ActionData, Double> savingYearPropertyCol = new TableColumn(fakeForName.npv.get().einsparung.getName());
        savingYearPropertyCol.setCellValueFactory(param -> param.getValue().npv.get().einsparung.asObject());
        //savingYearPropertyCol.setCellFactory(buildShotTextFactory());
        savingYearPropertyCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        savingYearPropertyCol.setCellFactory(new CurrencyColumnCell());


        TableColumn<ActionData, Double> enpiDevelopmentPropertyCol = new TableColumn(I18n.getInstance().getString("plugin.action.enpiabechange"));
        enpiDevelopmentPropertyCol.setCellValueFactory(param -> param.getValue().enpi.get().diffProperty().asObject());
        //savingYearPropertyCol.setCellFactory(buildShotTextFactory());
        enpiDevelopmentPropertyCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        enpiDevelopmentPropertyCol.setCellFactory(new Callback<TableColumn<ActionData, Double>, TableCell<ActionData, Double>>() {
            @Override
            public TableCell<ActionData, Double> call(TableColumn<ActionData, Double> param) {
                return new TableCell<ActionData, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item != null && !empty && getTableRow() != null && getTableRow().getItem() != null) {
                            ActionData actionData = (ActionData) getTableRow().getItem();
                            setText(currencyFormat.format(item) + " " + actionData.enpi.get().unitProperty().get());
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });

        TableColumn<ActionData, Double> consumptionDevelopmentPropertyCol = new TableColumn(I18n.getInstance().getString("plugin.action.consumption.diff"));
        consumptionDevelopmentPropertyCol.setCellValueFactory(param -> param.getValue().consumption.get().diffProperty().asObject());
        //consumptionDevelopmentPropertyCol.setCellFactory(buildShotTextFactory());
        consumptionDevelopmentPropertyCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        //consumptionDevelopmentPropertyCol.setCellFactory(new CurrencyColumnCell());
        consumptionDevelopmentPropertyCol.setCellFactory(new Callback<TableColumn<ActionData, Double>, TableCell<ActionData, Double>>() {
            @Override
            public TableCell<ActionData, Double> call(TableColumn<ActionData, Double> param) {
                return new TableCell<ActionData, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item != null && !empty && getTableRow() != null && getTableRow().getItem() != null) {
                            ActionData actionData = (ActionData) getTableRow().getItem();
                            if (item.equals(-0d)) {
                                setText(currencyFormat.format(0d) + " " + actionData.consumption.get().unitProperty().get());
                            } else {
                                setText(currencyFormat.format(item) + " " + actionData.consumption.get().unitProperty().get());
                            }
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });

        //planNameCol.setVisible(actionPlanData instanceof ActionPlanOverviewData);
        actionNrPropertyCol.setVisible(true);
        fromUserCol.setVisible(false);
        responsiblePropertyCol.setVisible(true);
        desciptionPropertyCol.setVisible(false);
        notePropertyCol.setVisible(true);
        createDatePropertyCol.setVisible(true);
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
        fieldTagsPropertyCol.setVisible(false);
        enpiDevelopmentPropertyCol.setVisible(false);
        consumptionDevelopmentPropertyCol.setVisible(true);

        //setPrefHeight(1000);
        titlePropertyCol.setPrefWidth(370);
        notePropertyCol.setPrefWidth(220);


        this.getColumns().addAll(actionNrPropertyCol, planNameCol, titlePropertyCol, fromUserCol,
                responsiblePropertyCol, desciptionPropertyCol, notePropertyCol,
                mediaTagsPropertyCol, statusTagsPropertyCol, fieldTagsPropertyCol,
                createDatePropertyCol, plannedDatePropertyCol, doneDatePropertyCol, noteAlternativeMeasuresPropertyCol, noteBewertetPropertyCol,
                noteCorrectionPropertyCol, noteEnergieflussPropertyCol, noteFollowUpActionPropertyCol,
                investPropertyCol, savingYearPropertyCol, enpiDevelopmentPropertyCol, consumptionDevelopmentPropertyCol
        );


        this.getColumns().forEach(tableDataTableColumn -> tableDataTableColumn.setSortable(true));
        this.getVisibleLeafColumns().addListener((ListChangeListener<TableColumn<ActionData, ?>>) c -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved()) autoFitTable();
            }
        });

        getSortOrder().addAll(createDatePropertyCol, actionNrPropertyCol);


    }

    public void enableSumRow(boolean enable) {
        showSumRow = enable;
        if (enable) {
            //sumRow.nrProperty().set(Integer.MAX_VALUE);
            // data.add(sumRow);
        } else {
            // data.remove(sumRow);
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
        this.statusFilter = status;
        filter();
    }

    public void setDateFilter(DateFilter filter) {
        this.dateFilter = filter;
        filter();
    }

    public void setFilterMedium(ObservableList<String> medium) {
        this.mediumFilter = medium;
        filter();
    }

    public void setFilterField(ObservableList<String> field) {
        this.fieldFilter = field;
        filter();
    }

    public void setFilterSEU(ObservableList<String> field) {
        this.fieldSEU = field;
        filter();
    }

    public void setTextFilter(String containsText) {
        this.containsTextFilter = containsText;
        filter();
    }

    public void setPlanFilter(ObservableList<String> planNames) {
        planFilters = planNames;
        filter();
    }

    public void filter() {
        //System.out.println("Start Table filter for: " + getItems().size() + "  " + filteredData.size() + " Plan Filter: " + planFilters);
        //if (true) return;

        Platform.runLater(() -> {
            filteredData.setPredicate(
                    new Predicate<ActionData>() {
                        @Override
                        public boolean test(ActionData notesRow) {
                            //System.out.println("-----" + notesRow.nr);
                            //if (true) return true; // hotfix

                            //String debugJson = GsonBuilder.createDefaultBuilder().create().toJson(notesRow);
                            try {

                                if (notesRow.isDeletedProperty().get()) return false;
                                //System.out.println("Filter.pass.delete");

                                //System.out.println("Plan Filter: " + planFilters);
                                if (planFilters != null && !planFilters.isEmpty() && !planFilters.contains(TagButton.ALL)) {
                                    if (!planFilters.contains(notesRow.getActionPlan().getName().get())) return false;
                                }
                                // System.out.println("Filter.pass.plan");


                                AtomicBoolean statusMatch = new AtomicBoolean(false);
                                if (statusFilter != null && !statusFilter.contains(TagButton.ALL)) {
                                    statusFilter.forEach(s -> {
                                        try {
                                            if (s.equals(TagButton.ALL)) {
                                                statusMatch.set(true);
                                                return;
                                            }

                                            for (String s1 : notesRow.statusTagsProperty().get().split(";")) {
                                                if (s1.equalsIgnoreCase(s)) {
                                                    statusMatch.set(true);
                                                }
                                            }
                                        } catch (Exception ex) {

                                        }
                                    });
                                    if (!statusMatch.get()) return false;
                                }
                                //System.out.println("Filter.pass.status");


                                if (mediumFilter != null && !mediumFilter.contains(TagButton.ALL)) {
                                    if (mediumFilter != null) {
                                        AtomicBoolean mediumMatch = new AtomicBoolean(false);
                                        mediumFilter.forEach(s -> {
                                            try {
                                                //System.out.println("Medium: " + s + " in " + notesRow.mediaTagsProperty());
                                                for (String s1 : notesRow.mediaTagsProperty().get().split(";")) {
                                                    if (s1.equalsIgnoreCase(s)) {
                                                        mediumMatch.set(true);
                                                    }
                                                }
                                            } catch (Exception ex) {

                                            }
                                        });
                                        if (!mediumMatch.get()) return false;
                                    }
                                }
                                //System.out.println("Filter.pass.medium");

                                //System.out.println("Filter.field: " + fieldFilter + "  in  " + notesRow.fieldTagsProperty().get());
                                if (fieldFilter != null && !fieldFilter.contains(TagButton.ALL)) {
                                    AtomicBoolean fieldMatch = new AtomicBoolean(false);
                                    fieldFilter.forEach(s -> {
                                        try {
                                            for (String s1 : notesRow.fieldTagsProperty().get().split(";")) {
                                                if (s1.equalsIgnoreCase(s)) {
                                                    fieldMatch.set(true);
                                                }
                                            }
                                        } catch (Exception ex) {

                                        }
                                    });
                                    if (!fieldMatch.get()) return false;
                                }
                                // System.out.println("Filter.pass.field");

                                //System.out.println("Filter.fieldSEU: " + fieldSEU + "  in  " + notesRow.seuTagsProperty().get());
                                if (fieldSEU != null && !fieldSEU.contains(TagButton.ALL)) {
                                    AtomicBoolean fieldMatch = new AtomicBoolean(false);
                                    fieldSEU.forEach(s -> {
                                        try {
                                            for (String s1 : notesRow.seuTagsProperty().get().split(";")) {
                                                if (s1.equalsIgnoreCase(s)) {
                                                    fieldMatch.set(true);
                                                }
                                            }
                                        } catch (Exception ex) {

                                        }
                                    });
                                    if (!fieldMatch.get()) return false;
                                }
                                //System.out.println("Filter.pass.fieldSEU");

                                if (dateFilter != null) {
                                    if (!dateFilter.show(notesRow)) return false;
                                }
                                //System.out.println("Filter.pass.date");


                                AtomicBoolean containString = new AtomicBoolean(false);
                                if (containsTextFilter != null || containsTextFilter.isEmpty()) {
                                    if (notesRow.title.get().toLowerCase().contains(containsTextFilter.toLowerCase())
                                            || notesRow.responsible.get().toLowerCase().contains(containsTextFilter.toLowerCase())
                                            || notesRow.note.get().toLowerCase().contains(containsTextFilter.toLowerCase())
                                            || notesRow.title.get().toLowerCase().contains(containsTextFilter.toLowerCase())
                                            || notesRow.desciption.get().toLowerCase().contains(containsTextFilter.toLowerCase())
                                            || notesRow.noteAlternativeMeasures.get().toLowerCase().contains(containsTextFilter.toLowerCase())
                                            || notesRow.noteCorrection.get().toLowerCase().contains(containsTextFilter.toLowerCase())
                                            || notesRow.noteBetroffenerProzess.get().toLowerCase().contains(containsTextFilter.toLowerCase())
                                            || notesRow.noteFollowUpAction.get().toLowerCase().contains(containsTextFilter.toLowerCase())
                                            || notesRow.getActionPlan().getName().get().toLowerCase().contains(containsTextFilter.toLowerCase())
                                            || notesRow.noteBewertet.get().toLowerCase().contains(containsTextFilter.toLowerCase())) {

                                        containString.set(true);
                                    }

                                    //TODO: may also check if column is visible
                                    if (!containString.get()) return false;
                                }

                                //System.out.println("Return true");
                                return true;
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            return false;
                        }
                    });
            //Platform.runLater(() -> autoFitTable(tableView));
            //Platform.runLater(() -> sort());

        });


    }


}
