package org.jevis.jeconfig.plugin.action.ui;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.table.SummeryData;
import org.jevis.jeconfig.application.table.SummeryTable;
import org.jevis.jeconfig.plugin.action.data.ActionData;
import org.jevis.jeconfig.plugin.action.data.ActionPlanData;
import org.jevis.jeconfig.plugin.action.data.Medium;
import org.jevis.jeconfig.plugin.action.data.TableFilter;
import org.jevis.jeconfig.plugin.action.ui.control.CurrencyColumnCell;
import org.jevis.jeconfig.plugin.action.ui.control.StringListColumnCell;
import org.jevis.jeconfig.plugin.action.ui.control.TagButton;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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

    private final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
    private final TableFilter tableFilter = new TableFilter();
    private final Statistics statistic;
    private final boolean showSumRow = false;
    private final ActionPlanData actionPlanData;
    private final ActionData fakeForName = new ActionData();
    private final TableColumn<ActionData, String> statusTagsPropertyCol = new TableColumn(fakeForName.statusTagsProperty().getName());
    private final TableColumn<ActionData, String> fromUserCol = new TableColumn(fakeForName.fromUserProperty().getName());
    private final TableColumn<ActionData, String> responsiblePropertyCol = new TableColumn(fakeForName.responsibleProperty().getName());
    private final TableColumn<ActionData, String> desciptionPropertyCol = new TableColumn(fakeForName.desciptionProperty().getName());
    private final TableColumn<ActionData, String> actionNrPropertyCol = new TableColumn(fakeForName.nrProperty().getName());
    private final TableColumn<ActionData, String> fieldTagsPropertyCol = new TableColumn(fakeForName.fieldTagsProperty().getName());
    private final TableColumn<ActionData, Double> savingYearPropertyCol = new TableColumn(fakeForName.npv.get().einsparung.getName());
    private final TableColumn<ActionData, DateTime> doneDatePropertyCol = new TableColumn(fakeForName.doneDateProperty().getName());
    private final TableColumn<ActionData, DateTime> createDatePropertyCol = new TableColumn(I18n.getInstance().getString("plugin.action.created"));
    private final TableColumn<ActionData, String> planNameCol = new TableColumn(I18n.getInstance().getString("plugin.action.filter.plan"));
    private final TableColumn<ActionData, String> notePropertyCol = new TableColumn(fakeForName.noteProperty().getName());
    private final TableColumn<ActionData, String> mediaTagsPropertyCol = new TableColumn(fakeForName.mediaTagsProperty().getName());
    private final TableColumn<ActionData, DateTime> plannedDatePropertyCol = new TableColumn(I18n.getInstance().getString("plugin.action.plandate"));
    private final TableColumn<ActionData, String> noteAlternativeMeasuresPropertyCol = new TableColumn(fakeForName.noteAlternativeMeasuresProperty().getName());
    private final TableColumn<ActionData, String> noteBewertetPropertyCol = new TableColumn(fakeForName.noteBewertetProperty().getName());
    private final TableColumn<ActionData, String> noteCorrectionPropertyCol = new TableColumn(fakeForName.noteCorrectionProperty().getName());
    private final TableColumn<ActionData, String> noteBetroffenerProzessPropertyCol = new TableColumn(fakeForName.noteBetroffenerProzessProperty().getName());
    private final TableColumn<ActionData, String> noteEnergieflussPropertyCol = new TableColumn(fakeForName.noteEnergieflussProperty().getName());
    private final TableColumn<ActionData, String> noteFollowUpActionPropertyCol = new TableColumn(fakeForName.noteFollowUpActionProperty().getName());
    private final TableColumn<ActionData, String> titlePropertyCol = new TableColumn(fakeForName.titleProperty().getName());
    private final TableColumn<ActionData, Double> investPropertyCol = new TableColumn(fakeForName.npv.get().investment.getName());
    private final TableColumn<ActionData, DateTime> runntimePropertyCol = new TableColumn(I18n.getInstance().getString("plugin.action.donedays"));
    private final TableColumn<ActionData, Double> savingTotalPropertyCol = new TableColumn(I18n.getInstance().getString("plugin.action.doneruntime"));
    private final TableColumn<ActionData, Double> enpiDevelopmentPropertyCol = new TableColumn(I18n.getInstance().getString("plugin.action.enpiabechange"));
    private final TableColumn<ActionData, Double> consumptionDevelopmentPropertyCol = new TableColumn(I18n.getInstance().getString("plugin.action.consumption.diff"));
    private final ObservableList<SummeryData> summeryData = FXCollections.observableArrayList();
    ObservableList<ActionData> data = FXCollections.observableArrayList();
    FilteredList<ActionData> filteredData;
    NumberFormat currencyFormat = NumerFormating.getInstance().getCurrencyFormat();
    private ObservableList<String> statusFilter = FXCollections.observableArrayList();
    private ObservableList<String> mediumFilter = FXCollections.observableArrayList();
    private ObservableList<String> fieldFilter = FXCollections.observableArrayList();
    private ObservableList<String> fieldSEU = FXCollections.observableArrayList();
    private ObservableList<String> planFilters = FXCollections.observableArrayList();
    private DateFilter dateFilter;
    private String containsTextFilter = "";


    public ActionTable(ActionPlanData actionPlanData, ObservableList<ActionData> data, Statistics statistic) {
        this.data = data;
        this.actionPlanData = actionPlanData;
        this.filteredData = new FilteredList<>(this.data);
        this.statistic = statistic;
        actionPlanData.setTableView(this);

        SortedList sortedList = new SortedList(this.filteredData);
        setItems(sortedList);
        sortedList.comparatorProperty().bind(this.comparatorProperty());

        setId("Action Table");
        setTableMenuButtonVisible(true);


        fromUserCol.setCellValueFactory(param -> param.getValue().fromUserProperty());
        fromUserCol.setCellFactory(buildShotTextFactory());


        responsiblePropertyCol.setCellValueFactory(param -> param.getValue().responsibleProperty());


        actionNrPropertyCol.setCellValueFactory(param -> param.getValue().nrTextProperty());
        actionNrPropertyCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        actionNrPropertyCol.setSortable(true);
        actionNrPropertyCol.setSortType(TableColumn.SortType.ASCENDING);


        desciptionPropertyCol.setCellValueFactory(param -> param.getValue().desciptionProperty());
        desciptionPropertyCol.setCellFactory(buildShotTextFactory());
        desciptionPropertyCol.setId("Desciption");


        planNameCol.setCellValueFactory(param -> param.getValue().getActionPlan().getName());
        planNameCol.setCellFactory(buildShotTextFactory());


        notePropertyCol.setCellValueFactory(param -> param.getValue().noteProperty());
        notePropertyCol.setCellFactory(buildShotTextFactory());
        notePropertyCol.setId("Note");


        mediaTagsPropertyCol.setCellValueFactory(param -> param.getValue().mediaTagsProperty());
        mediaTagsPropertyCol.setCellFactory(new StringListColumnCell());
        mediaTagsPropertyCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<String>() {
            @Override
            public String toString(String s) {
                return actionPlanData.getMediumByID(s).getName();
            }

            @Override
            public String fromString(String s) {
                return null;
            }
        }));
        mediaTagsPropertyCol.setStyle("-fx-alignment: CENTER;");


        statusTagsPropertyCol.setCellValueFactory(param -> param.getValue().statusTagsProperty());
        statusTagsPropertyCol.setCellFactory(new StringListColumnCell());
        statusTagsPropertyCol.setStyle("-fx-alignment: CENTER;");
        statusTagsPropertyCol.setPrefWidth(150);


        fieldTagsPropertyCol.setCellValueFactory(param -> param.getValue().fieldTagsProperty());
        fieldTagsPropertyCol.setCellFactory(new StringListColumnCell());
        fieldTagsPropertyCol.setStyle("-fx-alignment: CENTER;");


        doneDatePropertyCol.setCellValueFactory(param -> param.getValue().doneDateProperty());
        doneDatePropertyCol.setCellFactory(buildDateTimeFactory());


        createDatePropertyCol.setCellValueFactory(param -> param.getValue().createDateProperty());
        createDatePropertyCol.setCellFactory(buildDateTimeFactory());
        createDatePropertyCol.setMinWidth(95);


        plannedDatePropertyCol.setCellValueFactory(param -> param.getValue().plannedDateProperty());
        plannedDatePropertyCol.setCellFactory(buildDateTimeFactory());


        noteAlternativeMeasuresPropertyCol.setCellValueFactory(param -> param.getValue().noteAlternativeMeasuresProperty());
        noteAlternativeMeasuresPropertyCol.setCellFactory(buildShotTextFactory());


        noteBewertetPropertyCol.setCellValueFactory(param -> param.getValue().noteBewertetProperty());
        noteBewertetPropertyCol.setCellFactory(buildShotTextFactory());


        noteCorrectionPropertyCol.setCellValueFactory(param -> param.getValue().noteCorrectionProperty());
        noteCorrectionPropertyCol.setCellFactory(buildShotTextFactory());


        noteBetroffenerProzessPropertyCol.setCellValueFactory(param -> param.getValue().noteBetroffenerProzessProperty());
        noteBetroffenerProzessPropertyCol.setCellFactory(buildShotTextFactory());


        noteEnergieflussPropertyCol.setCellValueFactory(param -> param.getValue().noteEnergieflussProperty());
        noteEnergieflussPropertyCol.setCellFactory(buildShotTextFactory());


        noteFollowUpActionPropertyCol.setCellValueFactory(param -> param.getValue().noteFollowUpActionProperty());
        noteFollowUpActionPropertyCol.setCellFactory(buildShotTextFactory());


        titlePropertyCol.setCellValueFactory(param -> param.getValue().titleProperty());
        titlePropertyCol.setCellFactory(buildShotTextFactory());
        titlePropertyCol.setId("Title");


        investPropertyCol.setCellValueFactory(param -> param.getValue().npv.get().investment.asObject());
        //investPropertyCol.setCellFactory(buildShotTextFactory());
        investPropertyCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        investPropertyCol.setCellFactory(new CurrencyColumnCell());
        //investPropertyCol.setPrefWidth(180);

        savingYearPropertyCol.setCellValueFactory(param -> param.getValue().npv.get().einsparung.asObject());
        savingYearPropertyCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        savingYearPropertyCol.setCellFactory(new CurrencyColumnCell());
        savingYearPropertyCol.setMinWidth(130);


        runntimePropertyCol.setCellValueFactory(param -> param.getValue().doneDateProperty());
        runntimePropertyCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        runntimePropertyCol.setCellFactory(new Callback<TableColumn<ActionData, DateTime>, TableCell<ActionData, DateTime>>() {
            @Override
            public TableCell<ActionData, DateTime> call(TableColumn<ActionData, DateTime> actionDataDoubleTableColumn) {
                return new TableCell<ActionData, DateTime>() {
                    @Override
                    protected void updateItem(DateTime days, boolean b) {
                        super.updateItem(days, b);
                        if (!b && days != null) {
                            int daysRunning = Days.daysBetween(days.withTimeAtStartOfDay(), DateTime.now().withTimeAtStartOfDay()).getDays();
                            setText(daysRunning + "");
                        } else {
                            setText(null);
                        }


                    }
                };
            }
        });


        savingTotalPropertyCol.setCellValueFactory(param -> param.getValue().npv.get().einsparung.asObject());
        savingTotalPropertyCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        savingTotalPropertyCol.setCellFactory(new Callback<TableColumn<ActionData, Double>, TableCell<ActionData, Double>>() {
            @Override
            public TableCell<ActionData, Double> call(TableColumn<ActionData, Double> actionDataDoubleTableColumn) {
                return new TableCell<ActionData, Double>() {
                    @Override
                    protected void updateItem(Double aDouble, boolean b) {
                        super.updateItem(aDouble, b);
                        if (!b && aDouble != null && getTableRow().getItem() != null) {
                            ActionData actionData = (ActionData) getTableRow().getItem();
                            if (actionData.doneDate.get() != null) {
                                int daysRunning = Days.daysBetween(actionData.doneDate.get().withTimeAtStartOfDay(), DateTime.now().withTimeAtStartOfDay()).getDays();
                                double net = ((daysRunning) * (actionData.consumption.get().diff.get() / 365));
                                setText(NumerFormating.getInstance().getDoubleFormate().format(net) + " kWh");
                            }
                        } else {
                            setText(null);
                        }


                    }
                };
            }
        });
        savingTotalPropertyCol.setMinWidth(130);

        enpiDevelopmentPropertyCol.setCellValueFactory(param -> param.getValue().enpi.get().diffProperty().asObject());
        //savingYearPropertyCol.setCellFactory(buildShotTextFactory());
        enpiDevelopmentPropertyCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        enpiDevelopmentPropertyCol.setMinWidth(150);
        enpiDevelopmentPropertyCol.setCellFactory(new Callback<TableColumn<ActionData, Double>, TableCell<ActionData, Double>>() {
            @Override
            public TableCell<ActionData, Double> call(TableColumn<ActionData, Double> param) {
                return new TableCell<ActionData, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item != null && !empty && getTableRow() != null && getTableRow().getItem() != null) {
                            ActionData actionData = (ActionData) getTableRow().getItem();
                            setText(NumerFormating.getInstance().getDoubleFormate().format(item) + " " + actionData.enpi.get().unitProperty().get());
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });


        consumptionDevelopmentPropertyCol.setCellValueFactory(param -> param.getValue().consumption.get().diffProperty().asObject());
        //consumptionDevelopmentPropertyCol.setCellFactory(buildShotTextFactory());
        consumptionDevelopmentPropertyCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        consumptionDevelopmentPropertyCol.setPrefWidth(200);
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
                                setText(NumerFormating.getInstance().getDoubleFormate().format(0d) + " " + actionData.consumption.get().unitProperty().get());
                            } else {
                                setText(NumerFormating.getInstance().getDoubleFormate().format(item) + " " + actionData.consumption.get().unitProperty().get());
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
        notePropertyCol.setVisible(false);
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

        savingTotalPropertyCol.setVisible(false);
        runntimePropertyCol.setVisible(false);

        //setPrefHeight(1000);
        titlePropertyCol.setPrefWidth(420);
        notePropertyCol.setPrefWidth(220);


        this.getColumns().addAll(actionNrPropertyCol, planNameCol, titlePropertyCol, fromUserCol,
                responsiblePropertyCol, desciptionPropertyCol, notePropertyCol,
                mediaTagsPropertyCol, statusTagsPropertyCol, fieldTagsPropertyCol,
                createDatePropertyCol, plannedDatePropertyCol, doneDatePropertyCol, noteAlternativeMeasuresPropertyCol, noteBewertetPropertyCol,
                noteCorrectionPropertyCol, noteEnergieflussPropertyCol, noteFollowUpActionPropertyCol,
                investPropertyCol, savingYearPropertyCol, enpiDevelopmentPropertyCol, consumptionDevelopmentPropertyCol,
                savingTotalPropertyCol, runntimePropertyCol
        );


        this.getColumns().forEach(tableDataTableColumn -> tableDataTableColumn.setSortable(true));
        this.getVisibleLeafColumns().addListener((ListChangeListener<TableColumn<ActionData, ?>>) c -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved()) autoFitTable();
            }
        });

        getSortOrder().addAll(createDatePropertyCol, actionNrPropertyCol);

        //StringProperty summeryNrProperty = new SimpleStringProperty("Summe:");


        //updateStatusSummery(statusTagsPropertyCol);
        //updateMediumConsumptionSum(consumptionDevelopmentPropertyCol);

        getItems().addListener(new ListChangeListener<ActionData>() {
            @Override
            public void onChanged(Change<? extends ActionData> change) {
                while (change.next()) {

                }
                updateStatisticsTable();
            }
        });
        actionPlanData.getMedium().addListener(new ListChangeListener<Medium>() {
            @Override
            public void onChanged(Change<? extends Medium> change) {
                while (change.next()) {

                }
                updateStatisticsTable();
                //updateMediumConsumptionSum(consumptionDevelopmentPropertyCol);
            }
        });


        actionPlanData.getStatustags().addListener((ListChangeListener<? super String>) c -> {
            while (c.next()) {
            }
            updateStatisticsTable();
        });
    }

    public void updateStatisticsTable() {
        try {
            ObservableMap<TableColumn, StringProperty> summeryRow1 = FXCollections.observableHashMap();
            ObservableMap<TableColumn, StringProperty> summeryRow2 = FXCollections.observableHashMap();
            ObservableMap<TableColumn, StringProperty> summeryRow3 = FXCollections.observableHashMap();
            ObservableMap<TableColumn, StringProperty> summeryRow4 = FXCollections.observableHashMap();

            summeryData.clear();
            summeryData.add(new SummeryData(summeryRow1));
            summeryData.add(new SummeryData(summeryRow2));
            summeryData.add(new SummeryData(summeryRow3));
            summeryData.add(new SummeryData(summeryRow4));


            //summeryFunctionListA.put(actionNrPropertyCol, summeryNrProperty);
            summeryRow1.put(investPropertyCol, statistic.sumInvestStrPropertyProperty());
            summeryRow1.put(savingYearPropertyCol, statistic.sumSavingsStrPropertyProperty());
            summeryRow1.put(consumptionDevelopmentPropertyCol, statistic.sumNPVResultStrPropertyProperty());

            //summeryRow1.put(titlePropertyCol, statistic.textSumSinceImplementationGrossProperty());
            //summeryRow2.put(titlePropertyCol, statistic.textSumSinceImplementationProperty());
            //summeryRow3.put(titlePropertyCol, statistic.getSumCO2Net());
            //summeryRow4.put(titlePropertyCol, statistic.sumGrossCO2Property());

            StringProperty textSumImplement = new SimpleStringProperty(
                    I18n.getInstance().getString("plugin.action.statistics.saveSinceImp")
                            + ": " + SummeryTable.COLUMN_SEPARATOR
                            + NumerFormating.getInstance().getDoubleFormate().format(statistic.getSumSinceImplementation())
                            + SummeryTable.COLUMN_SEPARATOR + " kWh"
            );
            StringProperty textSumImplementGross = new SimpleStringProperty(
                    I18n.getInstance().getString("plugin.action.statistics.saveGrossSinceImp")
                            + ": " + SummeryTable.COLUMN_SEPARATOR
                            + NumerFormating.getInstance().getDoubleFormate().format(statistic.getSumSinceImplementationGross())
                            + SummeryTable.COLUMN_SEPARATOR + " kWh"
            );

            StringProperty textNetCO2Net = new SimpleStringProperty(
                    I18n.getInstance().getString("plugin.action.statistics.saveCO2Net")
                            + ": " + SummeryTable.COLUMN_SEPARATOR
                            + NumerFormating.getInstance().getDoubleFormate().format(statistic.getSumCO2())
                            + SummeryTable.COLUMN_SEPARATOR + " t"
            );
            StringProperty textGrossCO2Net = new SimpleStringProperty(
                    I18n.getInstance().getString("plugin.action.statistics.savesCO2Gross")
                            + ": " + SummeryTable.COLUMN_SEPARATOR
                            + NumerFormating.getInstance().getDoubleFormate().format(statistic.getSumGrossCO2())
                            + SummeryTable.COLUMN_SEPARATOR + " t"
            );


            summeryRow1.put(titlePropertyCol, textSumImplement);
            summeryRow2.put(titlePropertyCol, textSumImplementGross);
            summeryRow3.put(titlePropertyCol, textNetCO2Net);
            summeryRow4.put(titlePropertyCol, textGrossCO2Net);


            AtomicInteger aRow = new AtomicInteger(1);
            statistic.getMediumSumValues().entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .forEach(entry -> {
                        Medium key = entry.getKey();
                        Double aDouble = entry.getValue();
                        if (aDouble != 0) {
                            SummeryData data1 = getOrCreateSummeryData(aRow.get());

                            String text = key.getName() + ": " + SummeryTable.COLUMN_SEPARATOR
                                    + String.format("%s" + SummeryTable.COLUMN_SEPARATOR + "kWh", NumerFormating.getInstance().getDoubleFormate().format(aDouble));
                            data1.getSummeryList().put(consumptionDevelopmentPropertyCol, new SimpleStringProperty(text));
                            aRow.set(aRow.get() + 1);

                            // System.out.println(entry.getKey().getId() + ": " + entry.getValue());
                        }

                    });


            int row = 0;
            for (String s : actionPlanData.getStatustags()) {
                addSummeryForStatus(s, statusTagsPropertyCol, row);
                row++;
            }

        } catch (Exception ex) {
            logger.error(ex, ex);
        }
    }

    public Statistics getStatistic() {
        return statistic;
    }


    @Deprecated
    private void updateMediumConsumptionSum(TableColumn tableColumn) {
        int row = 1;
        for (Medium medium : actionPlanData.getMedium()) {
            addSummeryForMedium(medium.getId(), tableColumn, row);
            row++;
        }
    }

    private void updateStatusSummery(TableColumn tableColumn) {
        int row = 0;
        for (String s : actionPlanData.getStatustags()) {
            addSummeryForStatus(s, tableColumn, row);
            row++;
        }
    }

    public ObservableList<SummeryData> getSummeryData() {
        return summeryData;
    }

    private SummeryData getOrCreateSummeryData(int row) {
        int missingRows = row - summeryData.size() + 1;
        // System.out.println("Get Col: " + row + "/" + (summeryData.size() + 1) + "=" + missingRows);
        if (missingRows > 0) {
            for (int i = 0; i < missingRows; i++) {
                ObservableMap<TableColumn, StringProperty> summeryFunctionList = FXCollections.observableHashMap();
                SummeryData data = new SummeryData(summeryFunctionList);
                summeryData.add(data);
            }
        }
        return summeryData.get(row);
    }

    private void addSummeryForMedium(String medium, TableColumn column, int row) {
        SummeryData data1 = getOrCreateSummeryData(row);
        data1.getSummeryList().put(column, statistic.getMediumSum(medium));
    }

    private void addSummeryForStatus(String status, TableColumn column, int row) {
        SummeryData data1 = getOrCreateSummeryData(row);
        data1.getSummeryList().put(column, statistic.getStatusAmount(status));
    }

    public ObservableList<ActionData> getFilteredList() {
        return filteredData;
    }


    public void autoFitTable() {
        for (TableColumn<ActionData, ?> column : this.getColumns()) {
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

                                try {
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
                                } catch (Exception ex) {
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
                                    return containString.get();
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
