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
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisUnit;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.ChartSettings;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.application.jevistree.*;
import org.jevis.application.tools.DisabledItemsComboBox;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.unit.UnitManager;
import org.joda.time.DateTime;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ChartPlugin implements TreePlugin {

    private JEVisTree _tree;

    private Map<String, ChartDataModel> _data = new HashMap<>();
    private SaveResourceBundle rb = new SaveResourceBundle("jeapplication", AppLocale.getInstance().getLocale());
    private ObservableList<String> chartsList = FXCollections.observableArrayList();
    private final List<ChartSettings.ChartType> listChartTypes = Arrays.asList(ChartSettings.ChartType.values());

    private enum DATE_TYPE {

        START, END
    }

    @Override
    public void setTree(JEVisTree tree) {

        _tree = tree;
    }

    public JEVisTree get_tree() {
        return _tree;
    }

    private final String chartTitle = rb.getString("graph.title");
    private final String addChart = rb.getString("graph.table.addchart");

    final Image img = new Image(ChartPlugin.class.getResourceAsStream("/icons/" + "list-add.png"));
    final ImageView image = new ImageView(img);
    final Image imgMarkAll = new Image(ChartPlugin.class.getResourceAsStream("/icons/" + "jetxee-check-sign-and-cross-sign-3.png"));

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
    private List<Color> usedColors = new ArrayList<>();

    @Override
    public void selectionFinished() {
        for (Map.Entry<String, ChartDataModel> entrySet : _data.entrySet()) {
            String key = entrySet.getKey();
            ChartDataModel value = entrySet.getValue();
            if (value.getSelected()) {
            }

        }

    }

    @Override
    public String getTitle() {
        return null;
    }

    private ChartDataModel getData(JEVisTreeRow row) {
        String id = row.getID();
        if (_data.containsKey(id)) {
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
            _data.put(id, newData);
            return newData;
        }
    }

    final Tooltip tpMarkAll = new Tooltip(rb.getString("plugin.graph.dialog.changesettings.tooltip.forall"));

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
        getChartsList();
        List<TreeTableColumn<JEVisTreeRow, Long>> list = new ArrayList<>();

        TreeTableColumn<JEVisTreeRow, Long> column = new TreeTableColumn();
        column.setEditable(true);

        image.fitHeightProperty().set(20);
        image.fitWidthProperty().set(20);

        Button addChart = new Button(rb.getString("graph.table.addchart"), image);

        addChart.setOnAction(event -> {
            if (!chartsList.contains(chartTitle)) {
                chartsList.add(chartTitle);
                charts.put(chartTitle, new ChartSettings(chartTitle));

                TreeTableColumn<JEVisTreeRow, Boolean> selectColumn = buildSelectionColumn(_tree, chartsList.size() - 1);

                column.getColumns().add(column.getColumns().size() - 6, selectColumn);
            } else {
                int counter = 0;
                for (String s : chartsList) if (s.contains(chartTitle)) counter++;

                chartsList.add(chartTitle + " " + counter);
                charts.put(chartTitle + " " + counter, new ChartSettings(chartTitle + " " + counter));

                TreeTableColumn<JEVisTreeRow, Boolean> selectColumn = buildSelectionColumn(_tree, chartsList.size() - 1);

                column.getColumns().add(column.getColumns().size() - 6, selectColumn);
            }
        });

        column.setGraphic(addChart);

        TreeTableColumn<JEVisTreeRow, Color> colorColumn = buildColorColumn(_tree, rb.getString("graph.table.color"));

        List<TreeTableColumn> charts = new ArrayList<>();
        for (int i = 0; i < chartsList.size(); i++) {

            TreeTableColumn<JEVisTreeRow, Boolean> selectColumn = buildSelectionColumn(_tree, i);

            charts.add(selectColumn);
        }

        TreeTableColumn<JEVisTreeRow, AGGREGATION> aggregationColumn = buildAggregationColumn(_tree, rb.getString("graph.table.interval"));
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
                                        _data.entrySet().parallelStream().forEach(mdl -> {
                                            ChartDataModel cdm = mdl.getValue();
                                            if (cdm.getSelected()) {
                                                cdm.setSelectedStart(newDateTimeStart);
                                            }
                                        });
                                    } else if (type == DATE_TYPE.END) {
                                        LocalDate ld = dp.valueProperty().get();
                                        DateTime newDateTimeEnd = new DateTime(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), 23, 59, 59, 999);
                                        _data.entrySet().parallelStream().forEach(mdl -> {
                                            ChartDataModel cdm = mdl.getValue();
                                            if (cdm.getSelected()) {
                                                cdm.setSelectedEnd(newDateTimeEnd);
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
        String keyYearly = rb.getString("graph.interval.yearly");


        aggList.add(keyPreset);
        aggList.add(keyHourly);
        aggList.add(keyDaily);
        aggList.add(keyWeekly);
        aggList.add(keyMonthly);
        aggList.add(keyYearly);

        ChoiceBox aggrigate = new ChoiceBox();
        aggrigate.setItems(FXCollections.observableArrayList(aggList));
        aggrigate.getSelectionModel().selectFirst();
        switch (data.getAggregation()) {
            case None:
                aggrigate.valueProperty().setValue(keyPreset);
                break;
            case Hourly:
                aggrigate.valueProperty().setValue(keyHourly);
                break;
            case Daily:
                aggrigate.valueProperty().setValue(keyDaily);
                break;
            case Weekly:
                aggrigate.valueProperty().setValue(keyWeekly);
                break;
            case Monthly:
                aggrigate.valueProperty().setValue(keyMonthly);
                break;
            case Yearly:
                aggrigate.valueProperty().setValue(keyYearly);
                break;
        }

        aggrigate.valueProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
            //TODO:replace this quick and dirty workaround

            if (newValue.equals(keyPreset)) {
                data.setAggregation(AGGREGATION.None);
            } else if (newValue.equals(keyHourly)) {
                data.setAggregation(AGGREGATION.Hourly);
            } else if (newValue.equals(keyDaily)) {
                data.setAggregation(AGGREGATION.Daily);
            } else if (newValue.equals(keyWeekly)) {
                data.setAggregation(AGGREGATION.Weekly);
            } else if (newValue.equals(keyMonthly)) {
                data.setAggregation(AGGREGATION.Monthly);
            } else if (newValue.equals(keyYearly)) {
                data.setAggregation(AGGREGATION.Yearly);
            }


        });

        return aggrigate;
    }

    private TreeTableColumn<JEVisTreeRow, AGGREGATION> buildAggregationColumn(JEVisTree tree, String columnName) {
        TreeTableColumn<JEVisTreeRow, AGGREGATION> column = new TreeTableColumn(columnName);
        column.setPrefWidth(100);
        column.setCellValueFactory(param -> {

            ChartDataModel data = getData(param.getValue().getValue());

            return new ReadOnlyObjectWrapper<>(data.getAggregation());
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, AGGREGATION>, TreeTableCell<JEVisTreeRow, AGGREGATION>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, AGGREGATION> call(TreeTableColumn<JEVisTreeRow, AGGREGATION> param) {

                TreeTableCell<JEVisTreeRow, AGGREGATION> cell = new TreeTableCell<JEVisTreeRow, AGGREGATION>() {

                    @Override
                    public void commitEdit(AGGREGATION newValue) {
                        super.commitEdit(newValue);
                    }

                    @Override
                    protected void updateItem(AGGREGATION item, boolean empty) {
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
            Logger.getLogger(ChartPlugin.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(ChartPlugin.class.getName()).log(Level.SEVERE, null, ex);
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

    public void getChartsList() {
        List<String> tempList = new ArrayList<>();

        for (Map.Entry<String, ChartDataModel> entry : _data.entrySet()) {
            ChartDataModel mdl = entry.getValue();
            if (mdl.getSelected()) {
                for (String s : mdl.get_selectedCharts()) {
                    if (!tempList.contains(s))
                        tempList.add(s);
                }
            }
        }
        if (charts.isEmpty()) {

            if (tempList.isEmpty()) {
                tempList.add(chartTitle);
                charts.put(chartTitle, new ChartSettings(chartTitle));
            }
        }

        AlphanumComparator ac = new AlphanumComparator();
        tempList.sort(ac);
        chartsList = FXCollections.observableArrayList(tempList);
    }

    private Map<String, ChartSettings> charts = new HashMap<>();

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
                                        commitEdit(parseUnit(String.valueOf(newValue)));
                                    }
                                });

                                ImageView imageMarkAll = new ImageView(imgMarkAll);
                                imageMarkAll.fitHeightProperty().set(12);
                                imageMarkAll.fitWidthProperty().set(12);

                                Button tb = new Button("", imageMarkAll);

                                tb.setTooltip(tpMarkAll);

                                tb.setOnAction(event -> {
                                    JEVisUnit u = parseUnit(box.getSelectionModel().getSelectedItem().toString());
                                    _data.entrySet().parallelStream().forEach(mdl -> {
                                        ChartDataModel cdm = mdl.getValue();
                                        if (cdm.getSelected()) {
                                            cdm.setUnit(u);
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

    private JEVisUnit parseUnit(String unit) {
        JEVisUnit result = null;
        Unit _kg = SI.KILOGRAM;
        Unit _t = NonSI.METRIC_TON;

        Unit _l = NonSI.LITER;
        Unit _l2 = NonSI.LITRE;
        Unit _m3 = SI.CUBIC_METRE;

        Unit _W = SI.WATT;
        Unit _kW = SI.KILO(SI.WATT);
        Unit _MW = SI.MEGA(SI.WATT);
        Unit _GW = SI.GIGA(SI.WATT);
        Unit _Wh = SI.WATT.times(NonSI.HOUR);
        Unit _kWh = SI.KILO(SI.WATT).times(NonSI.HOUR);
        Unit _MWh = SI.MEGA(SI.WATT).times(NonSI.HOUR);
        Unit _GWh = SI.GIGA(SI.WATT).times(NonSI.HOUR);

        final JEVisUnit kg = new JEVisUnitImp(_kg);
        final JEVisUnit t = new JEVisUnitImp(_t);

        final JEVisUnit l = new JEVisUnitImp(_l);
        final JEVisUnit l2 = new JEVisUnitImp(_l2);
        final JEVisUnit m3 = new JEVisUnitImp(_m3);

        final JEVisUnit W = new JEVisUnitImp(_W);
        final JEVisUnit kW = new JEVisUnitImp(_kW);
        final JEVisUnit MW = new JEVisUnitImp(_MW);
        final JEVisUnit GW = new JEVisUnitImp(_GW);
        final JEVisUnit Wh = new JEVisUnitImp(_Wh);
        final JEVisUnit kWh = new JEVisUnitImp(_kWh);
        final JEVisUnit MWh = new JEVisUnitImp(_MWh);
        final JEVisUnit GWh = new JEVisUnitImp(_GWh);

        switch (unit) {
            case "kg":
                result = kg;
                break;
            case "t":
                result = t;
                break;
            case "W":
                result = W;
                break;
            case "kW":
                result = kW;
                break;
            case "MW":
                result = MW;
                break;
            case "GW":
                result = GW;
                break;
            case "Wh":
                result = Wh;
                break;
            case "kWh":
                result = kWh;
                break;
            case "MWh":
                result = MWh;
                break;
            case "GWh":
                result = GWh;
                break;
            case "m³":
                result = m3;
                break;
            case "L":
                result = l;
                break;
            default:
                break;
        }
        return result;
    }

    private enum EnergyUnit {
        W, kW, MW, GW, Wh, kWh, MWh, GWh
    }

    private enum MassUnit {
        kg, t
    }

    private enum VolumeUnit {
        L("L"), m3("m³");

        private final String name;

        VolumeUnit(String s) {
            this.name = s;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public Map<String, ChartDataModel> getSelectedData() {
        return _data;
    }

    private final List<String> disabledItems = Arrays.asList(rb.getString("plugin.graph.charttype.scatter.name"),
            rb.getString("plugin.graph.charttype.bubble.name"));

    public void set_data(Map<String, ChartDataModel> _data) {
        this._data = _data;
        _tree.getColumns().clear();
        _tree.getColumns().addAll(ColumnFactory.buildName());
        for (TreeTableColumn<JEVisTreeRow, Long> column : getColumns()) _tree.getColumns().add(column);
    }

    public Map<String, ChartSettings> getCharts() {
        return charts;
    }

    public enum AGGREGATION {

        None, Hourly, Daily, Weekly, Monthly,
        Yearly
    }

    public void setCharts(Map<String, ChartSettings> charts) {
        this.charts = charts;
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
                _data.entrySet().parallelStream().forEach(entry -> {
                    ChartDataModel mdl = entry.getValue();
                    if (mdl.getSelected()) {
                        if (mdl.get_selectedCharts().contains(oldValue)) {
                            mdl.get_selectedCharts().set(mdl.get_selectedCharts().indexOf(oldValue), newValue);
                        }
                    }
                });
                charts.entrySet().parallelStream().forEach(chart -> {
                    if (chart.getValue().getName().contains(oldValue)) {
                        charts.remove(chart.getKey());

                        ChartSettings set = chart.getValue();
                        set.setName(newValue);
                        charts.put(newValue, set);

                    }
                });
                chartsList.set(selectionColumnIndex, newValue);
            }
        });

        DisabledItemsComboBox<String> comboBoxChartType = new DisabledItemsComboBox(getlistNamesChartTypes());
        comboBoxChartType.setDisabledItems(disabledItems);

        if (charts != null && !charts.isEmpty()) {
            if (columnName != null) {
                if (columnName.equals(chartTitle)) {
                    comboBoxChartType.getSelectionModel().select(getlistNamesChartTypes().get(0));
                } else {
                    final Boolean[] foundChart = {false};
                    charts.entrySet().forEach(chart -> {
                        ChartSettings settings = chart.getValue();
                        if (settings.getName().equals(columnName)) {
                            comboBoxChartType.getSelectionModel().select(parseChartIndex(settings.getChartType()));
                            foundChart[0] = true;
                        }
                    });
                    if (!foundChart[0]) comboBoxChartType.getSelectionModel().select(0);
                }
            }
        } else {
            comboBoxChartType.getSelectionModel().select(0);
            if (columnName != null) charts.put(columnName, new ChartSettings(chartTitle));
            else charts.put(chartTitle, new ChartSettings(chartTitle));
        }

        comboBoxChartType.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {
                charts.entrySet().parallelStream().forEach(chart -> {
                    ChartSettings settings = chart.getValue();
                    if (settings.getName().equals(textFieldChartName.getText())) {
                        ChartSettings.ChartType type = parseChartType(comboBoxChartType.getSelectionModel().getSelectedIndex());
                        settings.setChartType(type);
                    }
                });
            }
        });

        vbox.getChildren().addAll(textFieldChartName, comboBoxChartType);

        column.setGraphic(vbox);
        column.setText(null);

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
                            CheckBox cbox = new CheckBox();

                            if (getTreeTableRow().getItem() != null && tree != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), chartsList.get(selectionColumnIndex))) {
                                ChartDataModel data = getData(getTreeTableRow().getItem());
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
                            _data.entrySet().parallelStream().forEach(mdl -> {
                                if (mdl.getValue().getSelected()) {
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

    private ObservableList<String> getlistNamesChartTypes() {
        List<String> tempList = new ArrayList<>();
        for (ChartSettings.ChartType ct : listChartTypes) {
            switch (ct.toString()) {
                case ("AREA"):
                    tempList.add(rb.getString("plugin.graph.charttype.area.name"));
                    break;
                case ("LINE"):
                    tempList.add(rb.getString("plugin.graph.charttype.line.name"));
                    break;
                case ("BAR"):
                    tempList.add(rb.getString("plugin.graph.charttype.bar.name"));
                    break;
                case ("BUBBLE"):
                    tempList.add(rb.getString("plugin.graph.charttype.bubble.name"));
                    break;
                case ("SCATTER"):
                    tempList.add(rb.getString("plugin.graph.charttype.scatter.name"));
                    break;
                case ("PIE"):
                    tempList.add(rb.getString("plugin.graph.charttype.pie.name"));
                    break;
                default:
                    break;
            }
        }
        return FXCollections.observableArrayList(tempList);
    }

    private ChartSettings.ChartType parseChartType(Integer chartTypeIndex) {
        switch (chartTypeIndex) {
            case (0):
                return ChartSettings.ChartType.AREA;
            case (1):
                return ChartSettings.ChartType.LINE;
            case (2):
                return ChartSettings.ChartType.BAR;
            case (3):
                return ChartSettings.ChartType.BUBBLE;
            case (4):
                return ChartSettings.ChartType.SCATTER;
            case (5):
                return ChartSettings.ChartType.PIE;
            default:
                return ChartSettings.ChartType.AREA;
        }
    }

    private ChartSettings.ChartType parseChartType(String chartType) {
        switch (chartType) {
            case ("AREA"):
                return ChartSettings.ChartType.AREA;
            case ("LINE"):
                return ChartSettings.ChartType.LINE;
            case ("BAR"):
                return ChartSettings.ChartType.BAR;
            case ("BUBBLE"):
                return ChartSettings.ChartType.BUBBLE;
            case ("SCATTER"):
                return ChartSettings.ChartType.SCATTER;
            case ("PIE"):
                return ChartSettings.ChartType.PIE;
            default:
                return ChartSettings.ChartType.AREA;
        }
    }

    private Integer parseChartIndex(ChartSettings.ChartType chartType) {

        switch (chartType.toString()) {
            case ("AREA"):
                return 0;
            case ("LINE"):
                return 1;
            case ("BAR"):
                return 2;
            case ("BUBBLE"):
                return 3;
            case ("SCATTER"):
                return 4;
            case ("PIE"):
                return 5;
            default:
                return 0;
        }
    }

    public void selectNone() {
        _data.entrySet().parallelStream().forEach(entry -> {
            ChartDataModel mdl = entry.getValue();
            if (mdl.getSelected()) {
                mdl.setSelected(false);
            }
        });
        _tree.refresh();
    }
}
