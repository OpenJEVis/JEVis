/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.application.jevistree.plugin;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisUnit;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.ChartSettings;
import org.jevis.application.Chart.ChartType;
import org.jevis.application.Chart.ChartUnits.ChartUnits;
import org.jevis.application.Chart.ChartUnits.EnergyUnit;
import org.jevis.application.Chart.ChartUnits.MassUnit;
import org.jevis.application.Chart.ChartUnits.VolumeUnit;
import org.jevis.application.Chart.data.GraphDataModel;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.application.jevistree.ColumnFactory;
import org.jevis.application.jevistree.JEVisTree;
import org.jevis.application.jevistree.JEVisTreeRow;
import org.jevis.application.jevistree.TreePlugin;
import org.jevis.application.tools.DisabledItemsComboBox;
import org.jevis.commons.classes.ClassHelper;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.unit.UnitManager;
import org.joda.time.DateTime;

import javax.measure.unit.Unit;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ChartPlugin implements TreePlugin {
    private static final Logger logger = LogManager.getLogger(ChartPlugin.class);
    private final List<ChartType> listChartTypes = Arrays.asList(ChartType.values());
    private final Image img = new Image(ChartPlugin.class.getResourceAsStream("/icons/" + "list-add.png"));
    private final ImageView image = new ImageView(img);
    private final Image imgMarkAll = new Image(ChartPlugin.class.getResourceAsStream("/icons/" + "jetxee-check-sign-and-cross-sign-3.png"));
    private final Color[] color_list = {
            Color.web("0xFFB300"),    // Vivid Yellow
            Color.web("0x803E75"),    // Strong Purple
            Color.web("0xFF6800"),    // Vivid Orange
            Color.web("0xA6BDD7"),    // Very Light Blue
            Color.web("0xC10020"),    // Vivid Red
            Color.web("0xCEA262"),    // Grayish Yellow
            Color.web("0x817066"),    // Medium Gray

            Color.web("0x007D34"),    // Vivid Green
            Color.web("0xF6768E"),    // Strong Purplish Pink
            Color.web("0x00538A"),    // Strong Blue
            Color.web("0xFF7A5C"),    // Strong Yellowish Pink
            Color.web("0x53377A"),    // Strong Violet
            Color.web("0xFF8E00"),    // Vivid Orange Yellow
            Color.web("0xB32851"),    // Strong Purplish Red
            Color.web("0xF4C800"),    // Vivid Greenish Yellow
            Color.web("0x7F180D"),    // Strong Reddish Brown
            Color.web("0x93AA00"),    // Vivid Yellowish Green
            Color.web("0x593315"),    // Deep Yellowish Brown
            Color.web("0xF13A13"),    // Vivid Reddish Orange
            Color.web("0x232C16"),    // Dark Olive Green
    };
    private JEVisTree _tree;
    private SaveResourceBundle rb = new SaveResourceBundle("jeapplication", AppLocale.getInstance().getLocale());
    private final Tooltip tpMarkAll = new Tooltip(rb.getString("plugin.graph.dialog.changesettings.tooltip.forall"));
    private final String chartTitle = rb.getString("graph.title");
    private final String addChart = rb.getString("graph.table.addchart");
    private final List<String> disabledItems = Arrays.asList(rb.getString("plugin.graph.charttype.scatter.name"),
            rb.getString("plugin.graph.charttype.bubble.name"));
    private ObservableList<String> chartsList = FXCollections.observableArrayList();
    private List<Color> usedColors = new ArrayList<>();
    private GraphDataModel _data = new GraphDataModel();

    @Override
    public void setTree(JEVisTree tree) {

        _tree = tree;
    }

    public JEVisTree get_tree() {
        return _tree;
    }

    @Override
    public void selectionFinished() {


    }

    @Override
    public String getTitle() {
        return null;
    }

    private ChartDataModel getData(JEVisTreeRow row) {
        Long id = Long.parseLong(row.getID());
        if (_data != null && _data.getSelectedData() != null && _data.containsId(id)) {
            return _data.get(id);
        } else {
            ChartDataModel newData = new ChartDataModel();
            newData.setObject(row.getJEVisObject());
            try {
                for (JEVisObject obj : row.getJEVisObject().getChildren()) {
                    if (obj.getJEVisClassName().equals("Clean Data"))
                        newData.setDataProcessor(obj);
                    break;
                }
            } catch (JEVisException e) {

            }
            newData.setAttribute(row.getJEVisAttribute());

            _data.getSelectedData().add(newData);

            return newData;
        }
    }

    private DatePicker buildDatePicker(ChartDataModel data, DATE_TYPE type) {

        LocalDate ld = null;

        if (data.getSelectedStart() != null) {
            if (type == DATE_TYPE.START) {
                ld = LocalDate.of(
                        data.getSelectedStart().getYear(),
                        data.getSelectedStart().getMonthOfYear(),
                        data.getSelectedStart().getDayOfMonth()
                );
            } else {
                ld = LocalDate.of(
                        data.getSelectedEnd().getYear(),
                        data.getSelectedEnd().getMonthOfYear(),
                        data.getSelectedEnd().getDayOfMonth()
                );
            }
        }

        DatePicker dp = new DatePicker(ld);

        final Callback<DatePicker, DateCell> dayCellFactory
                = new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        LocalDate ldBeginn = LocalDate.of(
                                data.getAttribute().getTimestampFromFirstSample().getYear(),
                                data.getAttribute().getTimestampFromFirstSample().getMonthOfYear(),
                                data.getAttribute().getTimestampFromFirstSample().getDayOfMonth());
                        LocalDate ldEnd = LocalDate.of(
                                data.getAttribute().getTimestampFromLastSample().getYear(),
                                data.getAttribute().getTimestampFromLastSample().getMonthOfYear(),
                                data.getAttribute().getTimestampFromLastSample().getDayOfMonth());

                        if (data.getAttribute().getTimestampFromFirstSample() != null && item.isBefore(ldBeginn)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }

                        if (data.getAttribute().getTimestampFromFirstSample() != null && item.isAfter(ldEnd)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }

                    }
                };
            }
        };
        dp.setDayCellFactory(dayCellFactory);

        return dp;
    }

    @Override
    public List<TreeTableColumn<JEVisTreeRow, Long>> getColumns() {
        if (_data != null) {
            chartsList = _data.getChartsList();
        }
        List<TreeTableColumn<JEVisTreeRow, Long>> list = new ArrayList<>();

        TreeTableColumn<JEVisTreeRow, Long> column = new TreeTableColumn();
        column.setEditable(true);

        image.fitHeightProperty().set(20);
        image.fitWidthProperty().set(20);

        Button addChart = new Button(rb.getString("graph.table.addchart"), image);

        System.out.println("chartsList: " + chartsList.size());
        if (chartsList.isEmpty()) {
            chartsList.add(chartTitle);
            _data.getCharts().add(new ChartSettings(chartTitle));
        }

        addChart.setOnAction(event -> {
            String newName = chartTitle + " " + chartsList.size();

            _data.getCharts().add(new ChartSettings(newName));
            chartsList.add(newName);
            TreeTableColumn<JEVisTreeRow, Boolean> selectColumn = buildSelectionColumn(_tree, chartsList.size() - 1);
            column.getColumns().add(column.getColumns().size() - 6, selectColumn);
        });

//        addChart.setOnAction(event -> {
//            if (!chartsList.contains(chartTitle)) {
//                chartsList.add(chartTitle);
//                _data.getCharts().add(new ChartSettings(chartTitle));
//
//                TreeTableColumn<JEVisTreeRow, Boolean> selectColumn = buildSelectionColumn(_tree, chartsList.size() - 1);
//
//                column.getColumns().add(column.getColumns().size() - 6, selectColumn);
//            } else {
//                int counter = 0;
//                for (String s : chartsList) if (s.contains(chartTitle)) counter++;
//
//                chartsList.add(chartTitle + " " + counter);
//                _data.getCharts().add(new ChartSettings(chartTitle + " " + counter));
//
//                TreeTableColumn<JEVisTreeRow, Boolean> selectColumn = buildSelectionColumn(_tree, chartsList.size() - 1);
//
//                column.getColumns().add(column.getColumns().size() - 6, selectColumn);
//            }
//        });

        column.setGraphic(addChart);

        TreeTableColumn<JEVisTreeRow, Color> colorColumn = buildColorColumn(_tree, rb.getString("graph.table.color"));

        List<TreeTableColumn> charts = new ArrayList<>();
        for (int i = 0; i < chartsList.size(); i++) {
            TreeTableColumn<JEVisTreeRow, Boolean> selectColumn = buildSelectionColumn(_tree, i);
            charts.add(selectColumn);
        }

        TreeTableColumn<JEVisTreeRow, AggregationPeriod> aggregationColumn = buildAggregationColumn(_tree, rb.getString("graph.table.interval"));
        TreeTableColumn<JEVisTreeRow, JEVisObject> dataProcessorColumn = buildDataPorcessorColumn(_tree, rb.getString("graph.table.cleaning"));
        TreeTableColumn<JEVisTreeRow, DateTime> startDateColumn = buildDateColumn(_tree, rb.getString("graph.table.startdate"), DATE_TYPE.START);
        TreeTableColumn<JEVisTreeRow, DateTime> endDateColumn = buildDateColumn(_tree, rb.getString("graph.table.enddate"), DATE_TYPE.END);
        TreeTableColumn<JEVisTreeRow, JEVisUnit> unitColumn = buildUnitColumn(_tree, rb.getString("graph.table.unit"));

        for (TreeTableColumn ttc : charts) column.getColumns().add(ttc);
        column.getColumns().addAll(colorColumn, aggregationColumn, dataProcessorColumn, startDateColumn, endDateColumn, unitColumn);

        list.add(column);


        return list;
    }

    private TreeTableColumn<JEVisTreeRow, Color> buildColorColumn(JEVisTree tree, String columnName) {
        TreeTableColumn<JEVisTreeRow, Color> column = new TreeTableColumn(columnName);
        column.setPrefWidth(130);
        column.setCellValueFactory(param -> {
            ChartDataModel data = getData(param.getValue().getValue());
            return new ReadOnlyObjectWrapper<>(data.getColor());
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Color>, TreeTableCell<JEVisTreeRow, Color>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Color> call(TreeTableColumn<JEVisTreeRow, Color> param) {

                TreeTableCell<JEVisTreeRow, Color> cell = new TreeTableCell<JEVisTreeRow, Color>() {

                    @Override
                    public void commitEdit(Color newValue) {
                        super.commitEdit(newValue);
                        ChartDataModel data = getData(getTreeTableRow().getItem());
                        data.setColor(newValue);
                        if (!usedColors.contains(newValue)) usedColors.add(newValue);
                    }

                    @Override
                    protected void updateItem(Color item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            StackPane hbox = new StackPane();
                            if (getTreeTableRow().getItem() != null && tree != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), columnName)) {
                                ChartDataModel data = getData(getTreeTableRow().getItem());
                                ColorPicker colorPicker = new ColorPicker();

                                StackPane.setAlignment(hbox, Pos.CENTER_LEFT);
                                colorPicker.setValue(item);
                                if (!usedColors.contains(item)) usedColors.add(item);
                                colorPicker.setStyle("-fx-color-label-visible: false ;");

                                colorPicker.setOnAction(event -> commitEdit(colorPicker.getValue()));

                                colorPicker.setDisable(!data.isSelectable());
                                hbox.getChildren().setAll(colorPicker);
                            }

                            setText(null);
                            setGraphic(hbox);
                        } else {
                            setText(null);
                            setGraphic(null);
                        }

                    }

                };

                return cell;
            }
        });

        return column;

    }

    private TreeTableColumn<JEVisTreeRow, DateTime> buildDateColumn(JEVisTree tree, String columnName, DATE_TYPE type) {
        TreeTableColumn<JEVisTreeRow, DateTime> column = new TreeTableColumn(columnName);
        column.setPrefWidth(160);
        column.setCellValueFactory(param -> {
            try {
                ChartDataModel data = getData(param.getValue().getValue());
                DateTime date;
                if (type == DATE_TYPE.START) {
                    date = data.getSelectedStart();
                } else {
                    date = data.getSelectedEnd();
                }

                return new ReadOnlyObjectWrapper<>(date);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return new ReadOnlyObjectWrapper<>(null);
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, DateTime>, TreeTableCell<JEVisTreeRow, DateTime>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, DateTime> call(TreeTableColumn<JEVisTreeRow, DateTime> param) {

                TreeTableCell<JEVisTreeRow, DateTime> cell = new TreeTableCell<JEVisTreeRow, DateTime>() {

                    @Override
                    public void commitEdit(DateTime newValue) {
                        super.commitEdit(newValue);
                        ChartDataModel data = getData(getTreeTableRow().getItem());

                        if (type == DATE_TYPE.START) {
                            data.setSelectedStart(newValue);
                        } else {
                            data.setSelectedEnd(newValue);
                        }
                    }

                    @Override
                    protected void updateItem(DateTime item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            StackPane stackPane = new StackPane();
                            if (getTreeTableRow().getItem() != null && tree != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), columnName)) {
                                ChartDataModel data = getData(getTreeTableRow().getItem());
                                DatePicker dp = buildDatePicker(data, type);

                                ImageView imageMarkAll = new ImageView(imgMarkAll);
                                imageMarkAll.fitHeightProperty().set(12);
                                imageMarkAll.fitWidthProperty().set(12);

                                Button tb = new Button("", imageMarkAll);

                                tb.setTooltip(tpMarkAll);

                                tb.setOnAction(event -> {
                                    if (type == DATE_TYPE.START) {
                                        LocalDate ld = dp.valueProperty().get();
                                        DateTime newDateTimeStart = new DateTime(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), 0, 0, 0, 0);
                                        _data.getSelectedData().parallelStream().forEach(mdl -> {
                                            if (mdl.getSelected()) {
                                                mdl.setSelectedStart(newDateTimeStart);
                                            }
                                        });
                                    } else if (type == DATE_TYPE.END) {
                                        LocalDate ld = dp.valueProperty().get();
                                        DateTime newDateTimeEnd = new DateTime(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), 23, 59, 59, 999);
                                        _data.getSelectedData().parallelStream().forEach(mdl -> {
                                            if (mdl.getSelected()) {
                                                mdl.setSelectedEnd(newDateTimeEnd);
                                            }
                                        });
                                    }
                                    tree.refresh();
                                });

                                HBox hbox = new HBox();
                                hbox.getChildren().addAll(dp, tb);
                                stackPane.getChildren().add(hbox);
                                StackPane.setAlignment(stackPane, Pos.CENTER_LEFT);

                                dp.setOnAction(event -> {
                                    LocalDate ld = dp.getValue();
                                    DateTime jodaTime = null;
                                    if (type == DATE_TYPE.START) {
                                        jodaTime = new DateTime(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), 0, 0, 0, 0);
                                    } else if (type == DATE_TYPE.END) {
                                        jodaTime = new DateTime(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), 23, 59, 59, 999);
                                    }
                                    commitEdit(jodaTime);
                                });
                            }

                            setText(null);
                            setGraphic(stackPane);
                        } else {
                            setText(null);
                            setGraphic(null);
                        }

                    }

                };

                return cell;
            }
        });

        return column;

    }

    private ChoiceBox buildAggregateBox(final ChartDataModel data) {
        List<String> aggList = new ArrayList<>();

        final String keyPreset = rb.getString("graph.interval.preset");
        String keyHourly = rb.getString("graph.interval.hourly");
        String keyDaily = rb.getString("graph.interval.daily");
        String keyWeekly = rb.getString("graph.interval.weekly");
        String keyMonthly = rb.getString("graph.interval.monthly");
        String keyQuarterly = rb.getString("graph.interval.quarterly");
        String keyYearly = rb.getString("graph.interval.yearly");


        aggList.add(keyPreset);
        aggList.add(keyHourly);
        aggList.add(keyDaily);
        aggList.add(keyWeekly);
        aggList.add(keyMonthly);
        aggList.add(keyQuarterly);
        aggList.add(keyYearly);

        ChoiceBox aggregate = new ChoiceBox();
        aggregate.setItems(FXCollections.observableArrayList(aggList));
        aggregate.getSelectionModel().selectFirst();
        switch (data.getAggregationPeriod()) {
            case NONE:
                aggregate.valueProperty().setValue(keyPreset);
                break;
            case HOURLY:
                aggregate.valueProperty().setValue(keyHourly);
                break;
            case DAILY:
                aggregate.valueProperty().setValue(keyDaily);
                break;
            case WEEKLY:
                aggregate.valueProperty().setValue(keyWeekly);
                break;
            case MONTHLY:
                aggregate.valueProperty().setValue(keyMonthly);
                break;
            case QUARTERLY:
                aggregate.valueProperty().setValue(keyQuarterly);
                break;
            case YEARLY:
                aggregate.valueProperty().setValue(keyYearly);
                break;
        }

        aggregate.valueProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
            //TODO:replace this quick and dirty workaround

            if (newValue.equals(keyPreset)) {
                data.setAggregationPeriod(AggregationPeriod.NONE);
            } else if (newValue.equals(keyHourly)) {
                data.setAggregationPeriod(AggregationPeriod.HOURLY);
            } else if (newValue.equals(keyDaily)) {
                data.setAggregationPeriod(AggregationPeriod.DAILY);
            } else if (newValue.equals(keyWeekly)) {
                data.setAggregationPeriod(AggregationPeriod.WEEKLY);
            } else if (newValue.equals(keyMonthly)) {
                data.setAggregationPeriod(AggregationPeriod.MONTHLY);
            } else if (newValue.equals(keyQuarterly)) {
                data.setAggregationPeriod(AggregationPeriod.QUARTERLY);
            } else if (newValue.equals(keyYearly)) {
                data.setAggregationPeriod(AggregationPeriod.YEARLY);
            }


        });

        return aggregate;
    }

    private TreeTableColumn<JEVisTreeRow, AggregationPeriod> buildAggregationColumn(JEVisTree tree, String columnName) {
        TreeTableColumn<JEVisTreeRow, AggregationPeriod> column = new TreeTableColumn(columnName);
        column.setPrefWidth(100);
        column.setCellValueFactory(param -> {

            ChartDataModel data = getData(param.getValue().getValue());

            return new ReadOnlyObjectWrapper<>(data.getAggregationPeriod());
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, AggregationPeriod>, TreeTableCell<JEVisTreeRow, AggregationPeriod>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, AggregationPeriod> call(TreeTableColumn<JEVisTreeRow, AggregationPeriod> param) {

                TreeTableCell<JEVisTreeRow, AggregationPeriod> cell = new TreeTableCell<JEVisTreeRow, AggregationPeriod>() {

                    @Override
                    public void commitEdit(AggregationPeriod newValue) {
                        super.commitEdit(newValue);
                    }

                    @Override
                    protected void updateItem(AggregationPeriod item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            StackPane hbox = new StackPane();

                            if (getTreeTableRow().getItem() != null && tree != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), columnName)) {
                                ChartDataModel data = getData(getTreeTableRow().getItem());
                                ChoiceBox aggBox = buildAggregateBox(data);

                                hbox.getChildren().setAll(aggBox);
                                StackPane.setAlignment(aggBox, Pos.CENTER_LEFT);

                                aggBox.setDisable(!data.isSelectable());

                            }

                            setText(null);
                            setGraphic(hbox);
                        } else {
                            setText(null);
                            setGraphic(null);
                        }

                    }

                };

                return cell;
            }
        });

        return column;

    }

    private ChoiceBox buildProcessorBox(ChartDataModel data) {
        List<String> proNames = new ArrayList<>();
        final List<JEVisObject> _dataProcessors = new ArrayList<JEVisObject>();
        proNames.add(rb.getString("graph.processing.raw"));

        try {
            JEVisClass dpClass = data.getObject().getDataSource().getJEVisClass("Clean Data");
            _dataProcessors.addAll(data.getObject().getChildren(dpClass, true));
            for (JEVisObject configObject : _dataProcessors) {
                proNames.add(configObject.getName());
            }

        } catch (JEVisException ex) {
            logger.fatal(ex);
        }

        ChoiceBox processorBox = new ChoiceBox();
        processorBox.setItems(FXCollections.observableArrayList(proNames));

        processorBox.valueProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
            //TODO:replace this quick and dirty workaround

            try {

                if (newValue.equals(rb.getString("graph.processing.raw"))) {
                    data.setDataProcessor(null);
                } else {

                    //TODO going by name is not the fine art, replace!
                    for (JEVisObject configObject : _dataProcessors) {
                        if (configObject.getName().equals(newValue)) {
                            data.setDataProcessor(configObject);
                        }

                    }
                }

            } catch (Exception ex) {
                logger.fatal(ex);
            }

        });

        if (!_dataProcessors.isEmpty()) processorBox.getSelectionModel().select(1);
        else processorBox.getSelectionModel().selectFirst();

        return processorBox;
    }

    private TreeTableColumn<JEVisTreeRow, JEVisObject> buildDataPorcessorColumn(JEVisTree tree, String columnName) {
        TreeTableColumn<JEVisTreeRow, JEVisObject> column = new TreeTableColumn(columnName);
        column.setPrefWidth(120);
        column.setEditable(true);

        column.setCellValueFactory(param -> {
            ChartDataModel data = getData(param.getValue().getValue());
            return new ReadOnlyObjectWrapper<>(data.getDataProcessor());
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, JEVisObject>, TreeTableCell<JEVisTreeRow, JEVisObject>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, JEVisObject> call(TreeTableColumn<JEVisTreeRow, JEVisObject> param) {

                TreeTableCell<JEVisTreeRow, JEVisObject> cell = new TreeTableCell<JEVisTreeRow, JEVisObject>() {

                    @Override
                    public void commitEdit(JEVisObject newValue) {
                        super.commitEdit(newValue);
                    }

                    @Override
                    protected void updateItem(JEVisObject item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            StackPane hbox = new StackPane();

                            if (getTreeTableRow().getItem() != null && tree != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), columnName)) {
                                ChartDataModel data = getData(getTreeTableRow().getItem());
                                ChoiceBox box = buildProcessorBox(data);

                                hbox.getChildren().setAll(box);

                                StackPane.setAlignment(hbox, Pos.CENTER_LEFT);

                                box.setDisable(!data.isSelectable());
                            }

                            setText(null);
                            setGraphic(hbox);
                        } else {
                            setText(null);
                            setGraphic(null);
                        }

                    }

                };

                return cell;
            }
        });

        return column;

    }

    private TreeTableColumn<JEVisTreeRow, JEVisUnit> buildUnitColumn(JEVisTree tree, String columnName) {
        TreeTableColumn<JEVisTreeRow, JEVisUnit> column = new TreeTableColumn(columnName);
        column.setPrefWidth(90);
        column.setEditable(true);

        column.setCellValueFactory(param -> {
            ChartDataModel data = getData(param.getValue().getValue());
            return new ReadOnlyObjectWrapper<>(data.getUnit());
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, JEVisUnit>, TreeTableCell<JEVisTreeRow, JEVisUnit>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, JEVisUnit> call(TreeTableColumn<JEVisTreeRow, JEVisUnit> param) {

                TreeTableCell<JEVisTreeRow, JEVisUnit> cell = new TreeTableCell<JEVisTreeRow, JEVisUnit>() {

                    @Override
                    protected void updateItem(JEVisUnit item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {

                            StackPane stackPane = new StackPane();

                            if (getTreeTableRow().getItem() != null && tree != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), columnName)) {
                                ChartDataModel data = getData(getTreeTableRow().getItem());
                                ChoiceBox box = buildUnitBox(data);

                                if (data.getUnit() != null)
                                    if (!data.getUnit().equals(Unit.ONE)) {
                                        String selection = UnitManager.getInstance().formate(data.getUnit());
                                        box.getSelectionModel().select(selection);
                                    } else {
                                        box.getSelectionModel().select(0);
                                    }

                                box.valueProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
                                    if (oldValue == null || newValue != oldValue) {
                                        commitEdit(ChartUnits.parseUnit(String.valueOf(newValue)));
                                    }
                                });

                                ImageView imageMarkAll = new ImageView(imgMarkAll);
                                imageMarkAll.fitHeightProperty().set(12);
                                imageMarkAll.fitWidthProperty().set(12);

                                Button tb = new Button("", imageMarkAll);

                                tb.setTooltip(tpMarkAll);

                                tb.setOnAction(event -> {
                                    JEVisUnit u = ChartUnits.parseUnit(box.getSelectionModel().getSelectedItem().toString());
                                    _data.getSelectedData().parallelStream().forEach(mdl -> {
                                        if (mdl.getSelected()) {
                                            mdl.setUnit(u);
                                        }
                                    });

                                    tree.refresh();
                                });

                                HBox hbox = new HBox();
                                hbox.getChildren().addAll(box, tb);
                                stackPane.getChildren().add(hbox);
                                StackPane.setAlignment(stackPane, Pos.CENTER_LEFT);

                                box.setDisable(!data.isSelectable());
                            }

                            setText(null);
                            setGraphic(stackPane);
                        } else {
                            setText(null);
                            setGraphic(null);
                        }

                    }

                };

                return cell;
            }
        });

        return column;

    }

    private ChoiceBox buildUnitBox(ChartDataModel singleRow) {

        List<String> proNames = new ArrayList<>();

        Boolean isEnergyUnit = false;
        Boolean isVolumeUnit = false;
        Boolean isMassUnit = false;
        JEVisUnit currentUnit = null;
        try {
            if (singleRow.getDataProcessor() != null
                    && singleRow.getDataProcessor().getAttribute("Value") != null
                    && singleRow.getDataProcessor().getAttribute("Value").getDisplayUnit() != null)
                currentUnit = singleRow.getDataProcessor().getAttribute("Value").getDisplayUnit();
            else {
                if (singleRow.getObject() != null
                        && singleRow.getObject().getAttribute("Value") != null
                        && singleRow.getObject().getAttribute("Value").getDisplayUnit() != null)
                    currentUnit = singleRow.getObject().getAttribute("Value").getDisplayUnit();
            }
        } catch (
                JEVisException e) {
        }

        for (
                EnergyUnit eu : EnergyUnit.values()) {
            if (eu.toString().equals(UnitManager.getInstance().formate(currentUnit))) {
                isEnergyUnit = true;
            }

        }
        if (isEnergyUnit) for (
                EnergyUnit eu : EnergyUnit.values())
            proNames.add(eu.toString());

        for (
                VolumeUnit vu : VolumeUnit.values()) {
            if (vu.toString().equals(UnitManager.getInstance().formate(currentUnit))) {
                isVolumeUnit = true;
            }
        }
        if (isVolumeUnit) for (
                VolumeUnit vu : VolumeUnit.values())
            proNames.add(vu.toString());

        for (
                MassUnit mu : MassUnit.values()) {
            if (mu.toString().equals(UnitManager.getInstance().formate(currentUnit))) {
                isMassUnit = true;
            }
        }
        if (isMassUnit) for (
                MassUnit mu : MassUnit.values())
            proNames.add(mu.toString());

        ChoiceBox processorBox = new ChoiceBox(FXCollections.observableArrayList(proNames));

        return processorBox;
    }


    public GraphDataModel getData() {
        return _data;
    }

    public void set_data(GraphDataModel _data) {
        this._data = _data;
        _tree.getColumns().clear();
        _tree.getColumns().addAll(ColumnFactory.buildName());
        for (TreeTableColumn<JEVisTreeRow, Long> column : getColumns()) _tree.getColumns().add(column);
    }

    private TreeTableColumn<JEVisTreeRow, Boolean> buildSelectionColumn(JEVisTree tree, Integer
            selectionColumnIndex) {

        String columnName = chartsList.get(selectionColumnIndex);

        TreeTableColumn<JEVisTreeRow, Boolean> column = new TreeTableColumn(columnName);
        column.setPrefWidth(120);
        column.setEditable(true);

        column.setCellValueFactory(param -> {
            ChartDataModel data = getData(param.getValue().getValue());
            Boolean selectedChart = data.get_selectedCharts().contains(chartsList.get(selectionColumnIndex));
            return new ReadOnlyObjectWrapper<>(data.getSelected() && selectedChart);
        });

        VBox vbox = new VBox();

        TextField textFieldChartName = new TextField(columnName);
        textFieldChartName.setText(columnName);
        textFieldChartName.setEditable(false);

        textFieldChartName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {
                _data.getSelectedData().parallelStream().forEach(mdl -> {
                    if (mdl.getSelected()) {
                        if (mdl.get_selectedCharts().contains(oldValue)) {
                            mdl.get_selectedCharts().set(mdl.get_selectedCharts().indexOf(oldValue), newValue);
                        }
                    }
                });
                AtomicReference<ChartSettings> set = new AtomicReference<>();

                _data.getCharts().parallelStream().forEach(chartSettings -> {
                    if (chartSettings.getName().equals(oldValue)) {
                        set.set(chartSettings);
                    }
                });
                _data.getCharts().remove(set);
                set.get().setName(newValue);

                _data.getCharts().add(set.get());

                chartsList.set(selectionColumnIndex, newValue);
            }
        });

        DisabledItemsComboBox<String> comboBoxChartType = new DisabledItemsComboBox(ChartType.getlistNamesChartTypes());
        comboBoxChartType.setDisabledItems(disabledItems);

        if (_data.getCharts() != null && !_data.getCharts().isEmpty()) {
            if (columnName != null) {
                if (columnName.equals(chartTitle)) {
                    comboBoxChartType.getSelectionModel().select(ChartType.getlistNamesChartTypes().get(0));
                } else {
                    final AtomicReference<Boolean> foundChart = new AtomicReference<>(false);
                    _data.getCharts().forEach(chart -> {
                        if (chart.getName().equals(columnName)) {
                            comboBoxChartType.getSelectionModel().select(ChartType.parseChartIndex(chart.getChartType()));
                            foundChart.set(true);
                        }
                    });
                    if (!foundChart.get()) comboBoxChartType.getSelectionModel().select(0);
                }
            }
        } else {
            comboBoxChartType.getSelectionModel().select(0);
            if (columnName != null) _data.getCharts().add(new ChartSettings(chartTitle));
            else _data.getCharts().add(new ChartSettings(chartTitle));
        }

        comboBoxChartType.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {
                _data.getCharts().parallelStream().forEach(chart -> {
                    if (chart.getName().equals(textFieldChartName.getText())) {
                        ChartType type = ChartType.parseChartType(comboBoxChartType.getSelectionModel().getSelectedIndex());
                        chart.setChartType(type);
                    }
                });
            }
        });

        vbox.getChildren().addAll(textFieldChartName, comboBoxChartType);

        column.setGraphic(vbox);
        column.setText(null);

        List<JEVisClass> visibleClassesFilter = new ArrayList<>();
        try {
            ClassHelper.AddAllInherited(visibleClassesFilter, this._tree.getJEVisDataSource().getJEVisClass("Data"));
        } catch (Exception ex) {

        }

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Boolean>, TreeTableCell<JEVisTreeRow, Boolean>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Boolean> call(TreeTableColumn<JEVisTreeRow, Boolean> param) {

                TreeTableCell<JEVisTreeRow, Boolean> cell = new TreeTableCell<JEVisTreeRow, Boolean>() {

                    @Override
                    public void commitEdit(Boolean newValue) {
                        super.commitEdit(newValue);
                        getTreeTableRow().getItem().getObjectSelectedProperty().setValue(newValue);
                        ChartDataModel data = getData(getTreeTableRow().getItem());
                        data.setSelected(newValue);
                        String selectedChart = chartsList.get(selectionColumnIndex);
                        if (newValue) {
                            if (!data.get_selectedCharts().contains(selectedChart)) {

                                data.get_selectedCharts().add(selectedChart);
                            }
                        } else {
                            data.get_selectedCharts().remove(selectedChart);
                        }
                    }

                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            StackPane hbox = new StackPane();

                            /**
                             * As an workaround we are using the color column for the filter because
                             * the JEVisTree does not yet support dynamic row names
                             */
                            if (getTreeTableRow().getItem() != null
                                    && tree != null
                                    && tree.getFilter().showColumn(getTreeTableRow().getItem(), rb.getString("graph.table.color"))) {

                                ChartDataModel data = getData(getTreeTableRow().getItem());
                                CheckBox cbox = new CheckBox();
                                hbox.getChildren().setAll(cbox);
                                StackPane.setAlignment(hbox, Pos.CENTER_LEFT);
                                cbox.setSelected(item);

                                setTextFieldEditable(textFieldChartName, item);

                                cbox.setOnAction(event -> {
                                    commitEdit(cbox.isSelected());

                                    if (cbox.isSelected()) {
                                        for (Color c : color_list) {
                                            if (!usedColors.contains(c)) {
                                                data.setColor(c);
                                                usedColors.add(c);
                                                Platform.runLater(() -> {
                                                    JEVisTreeRow sobj = new JEVisTreeRow(getTreeTableRow().getTreeItem().getValue().getJEVisObject());
                                                    getTreeTableRow().getTreeItem().setValue(sobj);

                                                });
                                                break;
                                            }
                                        }
                                    } else {
                                        usedColors.remove(data.getColor());
                                        data.setColor(Color.LIGHTBLUE);
                                        Platform.runLater(() -> {
                                            JEVisTreeRow sobj = new JEVisTreeRow(getTreeTableRow().getTreeItem().getValue().getJEVisObject());
                                            getTreeTableRow().getTreeItem().setValue(sobj);

                                        });
                                    }

                                });

                                if (data.getAttribute() != null && data.getAttribute().hasSample()) {
                                    cbox.setDisable(false);
                                } else {
                                    cbox.setDisable(true);
                                }


                            }
                            setText(null);
                            setGraphic(hbox);
                        } else {
                            setText(null);
                            setGraphic(null);
                        }
                    }

                    private void setTextFieldEditable(TextField textFieldChartName, Boolean item) {
                        if (item) {
                            textFieldChartName.setEditable(true);
                        } else {
                            AtomicReference<Boolean> foundSelected = new AtomicReference<>(false);
                            _data.getSelectedData().parallelStream().forEach(mdl -> {
                                if (mdl.getSelected()) {
                                    foundSelected.set(true);
                                }
                            });
                            if (foundSelected.get()) textFieldChartName.setEditable(true);
                        }
                    }
                };
                return cell;
            }
        });

        return column;
    }

//    private void disableRowIfnoData(JEVisObject dataObject) {
//        boolean hasData = false;
//        this._tree.gettree
//        this._tree.getColumns().forEach(columns -> {
//
//        });
//        this.chartsList.forEach();
//    }

    public void selectNone() {
        _data.getSelectedData().parallelStream().forEach(mdl -> {
            if (mdl.getSelected()) {
                mdl.setSelected(false);
            }
        });
        _tree.refresh();
    }


    private enum DATE_TYPE {

        START, END
    }
}
