package org.jevis.jecc.plugin.nonconformities.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableViewSkin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.application.table.DateTimeColumnCell;
import org.jevis.jecc.application.table.ShortColumnCell;
import org.jevis.jecc.application.table.StringListColumnCell;
import org.jevis.jecc.plugin.nonconformities.data.NonconformityData;
import org.jevis.jecc.plugin.nonconformities.data.NonconformityPlan;
import org.jevis.jecc.plugin.nonconformities.data.NonconformtiesOverviewData;
import org.jevis.jecc.plugin.nonconformities.data.TableFilter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;


public class NonconformityPlanTable extends TableView<NonconformityData> {

    private static final Logger logger = LogManager.getLogger(NonconformityPlanTable.class);


    private static Method columnToFitMethod;
    private static int DATE_TIME_WIDTH = 120;
    private static int BIG_WIDTH = 200;
    private static int SMALL_WIDTH = 60;

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
    SortedList<NonconformityData> sortedData;
    private DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
    private TableFilter tableFilter = new TableFilter();
    private NonconformityData sumRow = new NonconformityData();
    private DateFilter dateFilter;
    private boolean showSumRow = false;
    private String containsTextFilter = "";
    private ObservableList<String> medium = FXCollections.observableArrayList();
    private ObservableList<String> staus = FXCollections.observableArrayList();
    private ObservableList<String> fields = FXCollections.observableArrayList();
    private ObservableList<String> seu = FXCollections.observableArrayList();


    public NonconformityPlanTable(NonconformityPlan nonconformityPlan, ObservableList<NonconformityData> data) {
        this.data = data;
        this.filteredData = new FilteredList<>(data);
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(this.comparatorProperty());
        //sortedData.setAll(filteredData);
        setItems(sortedData);
        setId("Action Table");

        data.addListener(new ListChangeListener<NonconformityData>() {
            @Override
            public void onChanged(Change<? extends NonconformityData> c) {
                while (c.next()) ;
                logger.debug("Daten in tabelle: {} + geändert: {}", nonconformityPlan.getName(), c.getList());
            }
        });

        nonconformityPlan.prefixProperty().addListener((observable, oldValue, newValue) -> {
            filter();
        });


        NonconformityData fakeForName = new NonconformityData();

        TableColumn<NonconformityData, String> fromUserCol = new TableColumn(fakeForName.creatorProperty().getName());
        fromUserCol.setCellValueFactory(param -> param.getValue().creatorProperty());
        fromUserCol.setCellFactory(new ShortColumnCell<NonconformityData>());
        fromUserCol.setStyle("-fx-alignment: LEFT;");
        fromUserCol.setMinWidth(BIG_WIDTH);


        TableColumn<NonconformityData, String> responsiblePropertyCol = new TableColumn(fakeForName.responsiblePersonProperty().getName());
        responsiblePropertyCol.setCellValueFactory(param -> param.getValue().responsiblePersonProperty());
        responsiblePropertyCol.setStyle("-fx-alignment: LEFT;");
        responsiblePropertyCol.setMinWidth(BIG_WIDTH);

        TableColumn<NonconformityData, String> actionNrPropertyCol = new TableColumn(fakeForName.nrProperty().getName());
        actionNrPropertyCol.setCellValueFactory(param -> param.getValue().getPrefixPlusNumber());
        actionNrPropertyCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        actionNrPropertyCol.setMinWidth(SMALL_WIDTH);


        TableColumn<NonconformityData, String> desciptionPropertyCol = new TableColumn(fakeForName.descriptionProperty().getName());
        desciptionPropertyCol.setCellValueFactory(param -> param.getValue().descriptionProperty());
        desciptionPropertyCol.setStyle("-fx-alignment: LEFT;");
        desciptionPropertyCol.setCellFactory(new ShortColumnCell<NonconformityData>());


        TableColumn<NonconformityData, String> causePropertyCol = new TableColumn(fakeForName.causeProperty().getName());
        causePropertyCol.setCellValueFactory(param -> param.getValue().causeProperty());
        causePropertyCol.setStyle("-fx-alignment: LEFT;");
        causePropertyCol.setCellFactory(new ShortColumnCell<NonconformityData>());

        TableColumn<NonconformityData, String> immediateMeasuresPropertyCol = new TableColumn(fakeForName.immediateMeasuresProperty().getName());
        immediateMeasuresPropertyCol.setCellValueFactory(param -> param.getValue().immediateMeasuresProperty());
        immediateMeasuresPropertyCol.setStyle("-fx-alignment: LEFT;");
        immediateMeasuresPropertyCol.setCellFactory(new ShortColumnCell<NonconformityData>());

        TableColumn<NonconformityData, String> correctiveActionsCol = new TableColumn(fakeForName.correctiveActionsProperty().getName());
        correctiveActionsCol.setCellValueFactory(param -> param.getValue().correctiveActionsProperty());
        correctiveActionsCol.setStyle("-fx-alignment: LEFT;");
        correctiveActionsCol.setCellFactory(new ShortColumnCell<NonconformityData>());

        TableColumn<NonconformityData, String> mediaTagsPropertyCol = new TableColumn(fakeForName.mediumProperty().getName());
        mediaTagsPropertyCol.setCellValueFactory(param -> param.getValue().mediumProperty());
        mediaTagsPropertyCol.setCellFactory(new StringListColumnCell());
        mediaTagsPropertyCol.setStyle("-fx-alignment: CENTER;");
        mediaTagsPropertyCol.setMinWidth(BIG_WIDTH);


        TableColumn<NonconformityData, String> fieldTagsPropertyCol = new TableColumn(fakeForName.fieldTagsProperty().getName());
        fieldTagsPropertyCol.setCellValueFactory(param -> param.getValue().getfieldAsString());
        fieldTagsPropertyCol.setCellFactory(new ShortColumnCell<NonconformityData>());
        fieldTagsPropertyCol.setStyle("-fx-alignment: CENTER;");
        fieldTagsPropertyCol.setMinWidth(BIG_WIDTH);

        TableColumn<NonconformityData, String> seuTagsPropertyCol = new TableColumn(fakeForName.seuProperty().getName());
        seuTagsPropertyCol.setCellValueFactory(param -> param.getValue().seuProperty());
        seuTagsPropertyCol.setCellFactory(new StringListColumnCell());
        seuTagsPropertyCol.setStyle("-fx-alignment: CENTER;");
        seuTagsPropertyCol.setMinWidth(BIG_WIDTH);


        TableColumn<NonconformityData, String> actionsCol = new TableColumn(fakeForName.actionProperty().getName());
        actionsCol.setCellValueFactory(param -> param.getValue().actionProperty());
        actionsCol.setCellFactory(new ShortColumnCell<NonconformityData>());
        actionsCol.setStyle("-fx-alignment: CENTER;");
        actionsCol.setMinWidth(BIG_WIDTH);

        TableColumn<NonconformityData, String> planNameCol = new TableColumn(I18n.getInstance().getString("plugin.nonconformities.location"));
        planNameCol.setCellValueFactory(param -> param.getValue().getNonconformityPlan().getName());
        planNameCol.setCellFactory(new ShortColumnCell<NonconformityData>());
        planNameCol.setStyle("-fx-alignment: LEFT;");
        planNameCol.setMinWidth(BIG_WIDTH);


        TableColumn<NonconformityData, DateTime> doneDatePropertyCol = new TableColumn(fakeForName.doneDateProperty().getName());
        doneDatePropertyCol.setCellValueFactory(param -> param.getValue().doneDateProperty());
        doneDatePropertyCol.setCellFactory(new DateTimeColumnCell<NonconformityData>());
        doneDatePropertyCol.setStyle("-fx-alignment: LEFT;");
        doneDatePropertyCol.setMinWidth(DATE_TIME_WIDTH);

        TableColumn<NonconformityData, DateTime> createDatePropertyCol = new TableColumn(fakeForName.createDateProperty().getName());
        createDatePropertyCol.setCellValueFactory(param -> param.getValue().createDateProperty());
        createDatePropertyCol.setCellFactory(new DateTimeColumnCell<NonconformityData>());
        createDatePropertyCol.setStyle("-fx-alignment: LEFT;");
        createDatePropertyCol.setMinWidth(DATE_TIME_WIDTH);

        TableColumn<NonconformityData, DateTime> plannedDatePropertyCol = new TableColumn(fakeForName.deadLineProperty().getName());
        plannedDatePropertyCol.setCellValueFactory(param -> param.getValue().deadLineProperty());
        plannedDatePropertyCol.setCellFactory(new DateTimeColumnCell<NonconformityData>());
        plannedDatePropertyCol.setStyle("-fx-alignment: LEFT;");
        plannedDatePropertyCol.setMinWidth(DATE_TIME_WIDTH);


        TableColumn<NonconformityData, String> titlePropertyCol = new TableColumn(fakeForName.titleProperty().getName());
        titlePropertyCol.setCellValueFactory(param -> param.getValue().titleProperty());
        titlePropertyCol.setStyle("-fx-alignment: LEFT;");
        titlePropertyCol.setCellFactory(new ShortColumnCell<NonconformityData>());
        titlePropertyCol.setMinWidth(BIG_WIDTH);

        this.getSortOrder().addAll(planNameCol, actionNrPropertyCol);


        actionNrPropertyCol.setVisible(true);
        fromUserCol.setVisible(false);
        responsiblePropertyCol.setVisible(true);
        desciptionPropertyCol.setVisible(false);
        causePropertyCol.setVisible(false);
        immediateMeasuresPropertyCol.setVisible(false);

        createDatePropertyCol.setVisible(true);
        titlePropertyCol.setVisible(true);

        doneDatePropertyCol.setVisible(true);
        plannedDatePropertyCol.setVisible(true);
        planNameCol.setVisible(nonconformityPlan instanceof NonconformtiesOverviewData);


        this.tableMenuButtonVisibleProperty().set(true);


        this.getColumns().addAll(actionNrPropertyCol, planNameCol, titlePropertyCol, fromUserCol,
                responsiblePropertyCol, desciptionPropertyCol,
                createDatePropertyCol, plannedDatePropertyCol, doneDatePropertyCol, mediaTagsPropertyCol, actionsCol, fieldTagsPropertyCol, seuTagsPropertyCol
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

    public TableFilter getTableFilter() {
        return tableFilter;
    }

    public void setTextFilter(String containsText) {
        this.containsTextFilter = containsText;
    }

    public void setDateFilter(DateFilter filter) {
        this.dateFilter = filter;
    }

    public void setFilterMedium(ObservableList<String> medium) {
        this.medium = medium;
    }


    public void filter() {
        filteredData.setPredicate(
                new Predicate<NonconformityData>() {
                    @Override
                    public boolean test(NonconformityData notesRow) {
                        try {
                            if (notesRow.isDeleted()) {
                                return false;
                            }


                            if (dateFilter != null) {
                                if (!dateFilter.show(notesRow)) return false;
                            }

                            AtomicBoolean mediumMatch = new AtomicBoolean(false);
                            if (!medium.contains("*")) {
                                medium.forEach(s -> {
                                    try {
                                        for (String s1 : notesRow.getMedium().split(";")) {
                                            if (s1.equalsIgnoreCase(s)) {
                                                mediumMatch.set(true);
                                            }
                                        }
                                    } catch (Exception ex) {

                                    }
                                });
                                if (!mediumMatch.get()) return false;
                            }


                            AtomicBoolean fieldMatch = new AtomicBoolean(false);


                            if (!fields.contains("*")) {
                                fields.forEach(s -> {
                                    try {
                                        for (String s1 : notesRow.getFieldTags()) {
                                            if (s1.equalsIgnoreCase(s)) {
                                                fieldMatch.set(true);
                                            }
                                        }
                                    } catch (Exception ex) {

                                    }
                                });
                                if (!fieldMatch.get()) return false;
                            }

                            AtomicBoolean seuMatch = new AtomicBoolean(false);
                            if (!seu.contains("*")) {
                                seu.forEach(s -> {
                                    try {
                                        if (s.equalsIgnoreCase(notesRow.getSeu())) {
                                            seuMatch.set(true);
                                        }

                                    } catch (Exception ex) {

                                    }
                                });
                                if (!seuMatch.get()) return false;
                            }


                            AtomicBoolean statusMatch = new AtomicBoolean(false);
                            if (!staus.contains("*")) {

                                try {
                                    if (notesRow.getDoneDate() != null && staus.contains(NonconformityPlan.CLOSE)) {
                                        statusMatch.set(true);
                                    } else if (notesRow.getDoneDate() == null && staus.contains(NonconformityPlan.OPEN)) {
                                        statusMatch.set(true);
                                    }
                                } catch (Exception ex) {

                                }
                                if (!statusMatch.get()) return false;
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
        Platform.runLater(() -> sort());

    }


    public ObservableList<String> getMedium() {
        return medium;
    }

    public void setMedium(ObservableList<String> medium) {
        this.medium = medium;
    }

    public ObservableList<String> getStaus() {
        return staus;
    }

    public void setStaus(ObservableList<String> staus) {
        this.staus = staus;
    }

    public ObservableList<String> getFields() {
        return fields;
    }

    public void setFields(ObservableList<String> fields) {
        this.fields = fields;
    }

    public ObservableList<String> getSeu() {
        return seu;
    }

    public void setSeu(ObservableList<String> seu) {
        this.seu = seu;
    }
}
