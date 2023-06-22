package org.jevis.jeconfig.plugin.legal.ui;

import com.jfoenix.controls.JFXButton;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.table.DateTimeColumnCell;
import org.jevis.jeconfig.application.table.HyperlinkCell;
import org.jevis.jeconfig.application.table.ShortColumnCell;
import org.jevis.jeconfig.application.table.SummeryData;
import org.jevis.jeconfig.plugin.dashboard.config2.SankeyDataRow;
import org.jevis.jeconfig.plugin.legal.data.IndexOfLegalProvisions;
import org.jevis.jeconfig.plugin.legal.data.ObligationData;
import org.jevis.jeconfig.plugin.legal.data.TableFilter;
import org.jevis.jeconfig.tool.DragResizeMod;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;


public class IndexOfLegalProvisionsTable extends TableView<ObligationData> {

    private static final Logger logger = LogManager.getLogger(IndexOfLegalProvisionsTable.class);


    private static Method columnToFitMethod;

    static {
        try {
            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
            columnToFitMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    ObservableList<ObligationData> data = FXCollections.observableArrayList();
    FilteredList<ObligationData> filteredData;

    SortedList<ObligationData> sortedData;
    private DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
    private TableFilter tableFilter = new TableFilter();
    private ObligationData sumRow = new ObligationData();
    private DateFilter dateFilter;
    private String relevantFilter;
    private boolean showSumRow = false;
    private String containsTextFilter = "";
    private ObservableList<String> categories = FXCollections.observableArrayList();
    private ObservableList<String> relevance = FXCollections.observableArrayList();
    private ObservableList<String> scope = FXCollections.observableArrayList();
    private ObservableList<String> seu = FXCollections.observableArrayList();

    private static int DATE_TIME_WIDTH = 120;
    private static int BIG_WIDTH = 200;
    private static int VERY_BIG_WIDTH = 400;
    private static int SMALL_WIDTH = 60;

    public static final String ALL = I18n.getInstance().getString("plugin.Legalcadastre.relevanzFilter.all");
    public static final String ONLY_RELVANT = I18n.getInstance().getString("plugin.Legalcadastre.relevanzFilter.onlyrelevant");
    public static final String ONLY_NOT_RELEVANT = I18n.getInstance().getString("plugin.Legalcadastre.relevanzFilter.onlynotrelevant");

    private final ObservableList<SummeryData> summeryData = FXCollections.observableArrayList();



    public IndexOfLegalProvisionsTable(IndexOfLegalProvisions indexOfLegalProvisions, ObservableList<ObligationData> data, BooleanProperty updateProperty) {
        this.data = data;
        this.filteredData = new FilteredList<>(data);
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(this.comparatorProperty());
        //sortedData.setAll(filteredData);
        setItems(sortedData);
        setId("Action Table");

        data.addListener(new ListChangeListener<ObligationData>() {
            @Override
            public void onChanged(Change<? extends ObligationData> c) {
                while (c.next()) ;
                logger.debug("Daten in tabelle: {} + geändert: {}", indexOfLegalProvisions.getName(), c.getList());
            }
        });

        indexOfLegalProvisions.prefixProperty().addListener((observable, oldValue, newValue) -> {
            filter();
        });


        ObligationData fakeForName = new ObligationData();

        TableColumn<ObligationData, String> nrCol = new TableColumn(fakeForName.nrProperty().getName());
        nrCol.setCellValueFactory(param -> param.getValue().getNrAsStringProperty());
        nrCol.setCellFactory(new ShortColumnCell<ObligationData>());
        nrCol.setStyle("-fx-alignment: LEFT;");
        nrCol.setMinWidth(SMALL_WIDTH);


        TableColumn<ObligationData, String> legislationCol = new TableColumn(fakeForName.titleProperty().getName());
        legislationCol.setCellValueFactory(param -> param.getValue().titleProperty());
        legislationCol.setCellFactory(new ShortColumnCell<ObligationData>());
        legislationCol.setStyle("-fx-alignment: LEFT;");
        legislationCol.setMinWidth(BIG_WIDTH);


        TableColumn<ObligationData, String> designationCol = new TableColumn(fakeForName.designationProperty().getName());
        designationCol.setCellValueFactory(param -> param.getValue().designationProperty());
        designationCol.setCellFactory(new ShortColumnCell<ObligationData>());
        designationCol.setStyle("-fx-alignment: LEFT;");
        designationCol.setMinWidth(VERY_BIG_WIDTH);

        TableColumn<ObligationData, String> descriptionCol = new TableColumn(fakeForName.descriptionProperty().getName());
        descriptionCol.setCellValueFactory(param -> param.getValue().descriptionProperty());
        descriptionCol.setCellFactory(new ShortColumnCell<ObligationData>());
        descriptionCol.setStyle("-fx-alignment: LEFT;");
        descriptionCol.setMinWidth(VERY_BIG_WIDTH);


        TableColumn<ObligationData, DateTime> issueDateCol = new TableColumn(fakeForName.issueDateProperty().getName());
        issueDateCol.setCellValueFactory(param -> param.getValue().issueDateProperty());
        issueDateCol.setCellFactory(new DateTimeColumnCell<ObligationData>());
        issueDateCol.setStyle("-fx-alignment: CENTER;");
        issueDateCol.setMinWidth(DATE_TIME_WIDTH);

        TableColumn<ObligationData, DateTime> activeVersionCol = new TableColumn(fakeForName.currentVersionDateProperty().getName());
        activeVersionCol.setCellValueFactory(param -> param.getValue().currentVersionDateProperty());
        activeVersionCol.setCellFactory(new DateTimeColumnCell<ObligationData>());
        activeVersionCol.setStyle("-fx-alignment: CENTER;");
        activeVersionCol.setMinWidth(DATE_TIME_WIDTH);

        TableColumn<ObligationData, Boolean> relevanceCol = new TableColumn(fakeForName.relevantProperty().getName());
        relevanceCol.setCellValueFactory(param -> param.getValue().relevantProperty());
        relevanceCol.setCellFactory(CheckBoxTableCell.forTableColumn(relevanceCol));
        relevanceCol.setStyle("-fx-alignment: CENTER;");
        relevanceCol.setMinWidth(SMALL_WIDTH);


        TableColumn<ObligationData, DateTime> dateOfExaminationCol = new TableColumn(fakeForName.dateOfExaminationProperty().getName());
        dateOfExaminationCol.setCellValueFactory(param -> param.getValue().dateOfExaminationProperty());
        dateOfExaminationCol.setCellFactory(new DateTimeColumnCell<ObligationData>());
        dateOfExaminationCol.setStyle("-fx-alignment: CENTER;");
        dateOfExaminationCol.setMinWidth(DATE_TIME_WIDTH);

        TableColumn<ObligationData, String> importanceForTheCompanyCol = new TableColumn(fakeForName.importanceForTheCompanyProperty().getName());
        importanceForTheCompanyCol.setCellValueFactory(param -> param.getValue().importanceForTheCompanyProperty());
        importanceForTheCompanyCol.setCellFactory(new ShortColumnCell<ObligationData>());
        importanceForTheCompanyCol.setStyle("-fx-alignment: LEFT;");
        importanceForTheCompanyCol.setMinWidth(BIG_WIDTH);


        TableColumn<ObligationData, String> linkCol = new TableColumn(fakeForName.linkToVersionProperty().getName());
        linkCol.setCellValueFactory(param -> param.getValue().linkToVersionProperty());
        linkCol.setCellFactory(new HyperlinkCell<ObligationData>());
        linkCol.setStyle("-fx-alignment: CENTER;");
        linkCol.setMinWidth(VERY_BIG_WIDTH);


        TableColumn<ObligationData, String> categoryCol = new TableColumn<>(fakeForName.categoryProperty().getName());
        categoryCol.setCellValueFactory(param -> param.getValue().categoryProperty());
        categoryCol.setCellFactory(new ShortColumnCell<ObligationData>());
        categoryCol.setStyle("-fx-alignment: CENTER;");
        categoryCol.setMinWidth(SMALL_WIDTH);

        TableColumn<ObligationData, String> scopeCol = new TableColumn<>(fakeForName.scopeProperty().getName());
        scopeCol.setCellValueFactory(param -> param.getValue().scopeProperty());
        scopeCol.setCellFactory(new ShortColumnCell<ObligationData>());
        scopeCol.setStyle("-fx-alignment: CENTER;");
        scopeCol.setMinWidth(SMALL_WIDTH);


        nrCol.setVisible(true);
        legislationCol.setVisible(true);
        designationCol.setVisible(true);
        descriptionCol.setVisible(false);
        issueDateCol.setVisible(true);
        activeVersionCol.setVisible(true);

        relevanceCol.setVisible(true);
        dateOfExaminationCol.setVisible(true);

        importanceForTheCompanyCol.setVisible(false);


        this.tableMenuButtonVisibleProperty().set(true);
        this.getSortOrder().add(nrCol);


        this.getColumns().addAll(nrCol, legislationCol, designationCol, descriptionCol, issueDateCol, activeVersionCol, relevanceCol, dateOfExaminationCol, importanceForTheCompanyCol, linkCol, scopeCol, categoryCol
        );
        this.getColumns().add(buildMoveColumn());


        this.getColumns().stream().forEach(tableDataTableColumn -> tableDataTableColumn.setSortable(true));
        this.getVisibleLeafColumns().addListener((ListChangeListener<TableColumn<ObligationData, ?>>) c -> {
            while (c.next()) autoFitTable();
        });


        Statistics statistics = new Statistics(sortedData, updateProperty);


        int numberOfCategory = indexOfLegalProvisions.getCategories().size();
        int numberOfScope = indexOfLegalProvisions.getScopes().size();




        if (numberOfCategory > 2 || numberOfScope > 2) {
            if (numberOfCategory > numberOfScope) {
                for (int i = numberOfCategory; i < 0; i++) {
                    buildRow(indexOfLegalProvisions, legislationCol, relevanceCol, categoryCol, scopeCol, statistics, i);


                }
            }else {
                for (int i = numberOfScope; i < 0; i++) {
                    buildRow(indexOfLegalProvisions, legislationCol, relevanceCol, categoryCol, scopeCol, statistics, i);
                }
            }



        }else {
            for (int i = 0; i < 2; i++) {
                buildRow(indexOfLegalProvisions, legislationCol, relevanceCol, categoryCol, scopeCol, statistics, i);
            }
        }

        ObservableMap<TableColumn, StringProperty> summeryRow2 = FXCollections.observableHashMap();
        ObservableMap<TableColumn, StringProperty> summeryRow3 = FXCollections.observableHashMap();




    }


    private void buildRow(IndexOfLegalProvisions indexOfLegalProvisions, TableColumn<ObligationData, String> legislationCol, TableColumn<ObligationData, Boolean> relevanceCol, TableColumn<ObligationData, String> categoryCol, TableColumn<ObligationData, String> scopeCol, Statistics statistics, int i) {
        ObservableMap<TableColumn, StringProperty> summeryRow = FXCollections.observableHashMap();
        System.out.println(i);

        switch (i) {
            case 0:
                summeryRow.put(legislationCol, statistics.getAll(I18n.getInstance().getString("plugin.indexoflegalprovisions.all")));
                summeryRow.put(relevanceCol, statistics.getRelevant(I18n.getInstance().getString("plugin.indexoflegalprovisions.relevant"), true));
                break;
            case 1:
                summeryRow.put(relevanceCol, statistics.getRelevant(I18n.getInstance().getString("plugin.indexoflegalprovisions.notrrelevant"), false));
                break;
        }

        String category = indexOfLegalProvisions.getCategories().get(i);
        String scope = indexOfLegalProvisions.getScopes().get(i);
        if (category != null) {
            summeryRow.put(categoryCol, statistics.getCategory(category+": ", category));
        }
        if (scope != null) {
            summeryRow.put(scopeCol, statistics.getScope(scope+": ", scope));
        }
        System.out.println(summeryRow);

        summeryData.add(new SummeryData(summeryRow));
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
        for (TableColumn<ObligationData, ?> column : this.getColumns()) {
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
        this.categories = medium;
    }


    public void filter() {
        filteredData.setPredicate(
                new Predicate<ObligationData>() {
                    @Override
                    public boolean test(ObligationData notesRow) {
                        try {
                            if (notesRow.isDeleted()) {
                                return false;
                            }


                            if (dateFilter != null) {
                                if (!dateFilter.show(notesRow)) return false;
                            }
                            AtomicBoolean categoryMatch = new AtomicBoolean(false);
                            if (!categories.contains("*")) {
                                categories.forEach(s -> {
                                    try {
                                        for (String s1 : notesRow.getCategory().split(";")) {
                                            if (s1.equalsIgnoreCase(s)) {
                                                categoryMatch.set(true);
                                            }
                                        }
                                    } catch (Exception ex) {

                                    }
                                });
                                if (!categoryMatch.get()) return false;
                            }
                            AtomicBoolean scopeMatch = new AtomicBoolean(false);
                            if (!scope.contains("*")) {
                                scope.forEach(s -> {
                                    try {
                                        for (String s1 : notesRow.getScope().split(";")) {
                                            if (s1.equalsIgnoreCase(s)) {
                                                scopeMatch.set(true);
                                            }
                                        }
                                    } catch (Exception ex) {

                                    }
                                });
                                if (!scopeMatch.get()) return false;
                            }

                            AtomicBoolean relevanceMatch = new AtomicBoolean(false);

                            if (!relevance.contains("*")) {
                                relevance.forEach(s -> {
                                    try {
                                        if (s.equals(I18n.getInstance().getString("plugin.Legalcadastre.legislation.relvant"))) {
                                            if (notesRow.getRelevant()) {
                                                relevanceMatch.set(true);
                                            }
                                        } else if (s.equals(I18n.getInstance().getString("plugin.Legalcadastre.legislation.notrelvant"))) {
                                            if (!notesRow.getRelevant()) {
                                                relevanceMatch.set(true);
                                            }
                                        }
                                    } catch (Exception ex) {

                                    }
                                });
                                if (!relevanceMatch.get()) return false;
                            }

                            AtomicBoolean fieldMatch = new AtomicBoolean(false);
                            AtomicBoolean containString = new AtomicBoolean(false);
                            if (containsTextFilter != null || containsTextFilter.isEmpty()) {
                                if (notesRow.title.get().toLowerCase().contains(containsTextFilter.toLowerCase())
                                        || notesRow.title.get().toLowerCase().contains(containsTextFilter.toLowerCase())
                                        || notesRow.getDescription().toLowerCase().contains(containsTextFilter.toLowerCase())) {
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


    public ObservableList<String> getCategories() {
        return categories;
    }

    public void setCategories(ObservableList<String> categories) {
        this.categories = categories;
    }

    public ObservableList<String> getRelevance() {
        return relevance;
    }

    public void setRelevance(ObservableList<String> relevance) {
        this.relevance = relevance;
    }

    public ObservableList<String> getScope() {
        return scope;
    }

    public void setScope(ObservableList<String> scope) {
        this.scope = scope;
    }

    public ObservableList<String> getSeu() {
        return seu;
    }

    public void setSeu(ObservableList<String> seu) {
        this.seu = seu;
    }

    public TableColumn<ObligationData, ObligationData> buildMoveColumn() {

        Callback treeTableColumnCallback = new Callback<TableColumn<ObligationData, ObligationData>, TableCell<ObligationData, ObligationData>>() {
            @Override
            public TableCell<ObligationData, ObligationData> call(TableColumn<ObligationData, ObligationData> param) {
                TableCell<ObligationData, ObligationData> cell = new TableCell<ObligationData, ObligationData>() {
                    @Override
                    protected void updateItem(ObligationData item, boolean empty) {


                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            HBox hBox = new HBox();
                            JFXButton jfxButtonMoveUp = new JFXButton("", JEConfig.getSVGImage(Icon.ARROW_UP, 10, 10));
                            JFXButton jfxButtonMoveDown = new JFXButton("", JEConfig.getSVGImage(Icon.ARROW_DOWN, 10, 10));
                            hBox.setSpacing(3);
                            hBox.getChildren().addAll(jfxButtonMoveUp, jfxButtonMoveDown);

                            jfxButtonMoveDown.setOnAction(actionEvent -> {


                                int index = getItems().indexOf(item);
                                if (getItems().get(index).getNr() >= getItems().size()) return;
                                ObligationData swap = getItems().get(index + 1);

                                item.setNr(item.getNr() + 1);
                                swap.setNr(swap.getNr() - 1);
                                item.commit();
                                swap.commit();
                                this.getTableView().getSortOrder().set(0, getColumns().get(0));
                                getItems().sorted();
                                refresh();

                            });

                            jfxButtonMoveUp.setOnAction(actionEvent -> {


                                int index = getItems().indexOf(item);
                                if (getItems().get(index).getNr() < 2) return;
                                ObligationData swap = getItems().get(index - 1);

                                item.setNr(item.getNr() - 1);
                                item.commit();
                                swap.commit();
                                swap.setNr(swap.getNr() + 1);
                                this.getTableView().getSortOrder().set(0, getColumns().get(0));
                                getItems().sorted();
                                refresh();


                            });

                            setText("");
                            setGraphic(hBox);
                        }


                    }
                };
                return cell;
            }
        };

        Callback valueFactory = new Callback<TableColumn.CellDataFeatures<SankeyDataRow, SankeyDataRow>, ObservableValue<SankeyDataRow>>() {
            @Override
            public ObservableValue<SankeyDataRow> call(TableColumn.CellDataFeatures<SankeyDataRow, SankeyDataRow> param) {
                try {
                    return new SimpleObjectProperty<>(param.getValue());
                } catch (Exception ex) {
                    logger.error(ex);
                }

                return new SimpleObjectProperty<>(null);
            }
        };


        TableColumn<ObligationData, ObligationData> column = new TableColumn<>(I18n.getInstance().getString("plugin.indexoflegalprovisions.position"));
        column.setId("Move");
        column.setCellValueFactory(valueFactory);
        column.setCellFactory(treeTableColumnCallback);

        return column;
    }

    public String getRelevantFilter() {
        return relevantFilter;
    }

    public void setRelevantFilter(String relevantFilter) {
        this.relevantFilter = relevantFilter;
    }

    public ObservableList<SummeryData> getSummeryData() {
        return summeryData;
    }
}
