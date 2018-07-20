/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.application.jevistree.plugin;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.jevis.api.*;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.application.jevistree.JEVisTree;
import org.jevis.application.jevistree.JEVisTreeRow;
import org.jevis.application.jevistree.TreePlugin;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.unit.UnitManager;
import org.joda.time.DateTime;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public void selectionFinished() {
        //Will happen if the user peress some kinde of OK button
        System.out.println("selectionFinished()");
        for (Map.Entry<String, ChartDataModel> entrySet : _data.entrySet()) {
            String key = entrySet.getKey();
            ChartDataModel value = entrySet.getValue();
            if (value.getSelected()) {

                System.out.println("key: " + key);
            }

        }

    }

    @Override
    public String getTitle() {
        return rb.getString("graph.title");
    }

    @Override
    public List<TreeTableColumn<JEVisTreeRow, Long>> getColumns() {
        List<TreeTableColumn<JEVisTreeRow, Long>> list = new ArrayList<>();

        TreeTableColumn<JEVisTreeRow, Long> column = new TreeTableColumn();
        column.setEditable(true);
//        textField = new TextField();
//        textField.setText(_title);
//        textField.setEditable(true);
//        column.setGraphic(textField);

        TreeTableColumn<JEVisTreeRow, Color> colorColumn = buildColorColumn(_tree, rb.getString("graph.table.color"));
        TreeTableColumn<JEVisTreeRow, Boolean> selectColumn = buildSelectionColumn(_tree, rb.getString("graph.table.load"));
        TreeTableColumn<JEVisTreeRow, String> selectChartColumn = buildSelectChartcolumn(_tree, rb.getString("graph.table.selectchart"));
        TreeTableColumn<JEVisTreeRow, AGGREGATION> aggregationColumn = buildAggregationColumn(_tree, rb.getString("graph.table.interval"));
        TreeTableColumn<JEVisTreeRow, JEVisObject> dataProcessorColumn = buildDataPorcessorColumn(_tree, rb.getString("graph.table.cleaning"));
        TreeTableColumn<JEVisTreeRow, DateTime> startDateColumn = buildDateColumn(_tree, rb.getString("graph.table.startdate"), DATE_TYPE.START);
        TreeTableColumn<JEVisTreeRow, DateTime> endDateColumn = buildDateColumn(_tree, rb.getString("graph.table.enddate"), DATE_TYPE.END);
        TreeTableColumn<JEVisTreeRow, JEVisUnit> unitColumn = buildUnitColumn(_tree, rb.getString("graph.table.unit"));

        column.getColumns().addAll(selectColumn, selectChartColumn, colorColumn, aggregationColumn, dataProcessorColumn, startDateColumn, endDateColumn, unitColumn);

        list.add(column);

        return list;
    }

    private TreeTableColumn<JEVisTreeRow, String> buildSelectChartcolumn(JEVisTree tree, String columnName) {
        TreeTableColumn<JEVisTreeRow, String> column = new TreeTableColumn(columnName);
        column.setPrefWidth(100);
        getChartsList();
        column.setCellValueFactory(param -> {

            ChartDataModel data = getData(param.getValue().getValue());

            return new ReadOnlyObjectWrapper<>(data.getTitle());
//                return param.getValue().getValue().getJEVisObject();
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, String>, TreeTableCell<JEVisTreeRow, String>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, String> call(TreeTableColumn<JEVisTreeRow, String> param) {

                TreeTableCell<JEVisTreeRow, String> cell = new TreeTableCell<JEVisTreeRow, String>() {

                    @Override
                    public void commitEdit(String newValue) {
                        super.commitEdit(newValue);
                        ChartDataModel data = getData(getTreeTableRow().getItem());
                        data.setTitle(newValue);
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            StackPane hbox = new StackPane();

                            if (getTreeTableRow().getItem() != null && tree != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), columnName)) {
                                ChartDataModel data = getData(getTreeTableRow().getItem());

                                ChoiceBox selectChartBox = buildSelectChartBox(data);

                                final String chartTitle = rb.getString("graph.title");
                                final String addChart = rb.getString("graph.table.addchart");

                                selectChartBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                                    String newValueStr = newValue.toString();
                                    String oldValueStr = oldValue.toString();
                                    if (newValueStr != null || !newValueStr.equals(oldValueStr)) {
                                        String newString = null;
                                        if (newValueStr.equals(addChart)) {
                                            if (chartsList.contains(chartTitle)) {
                                                chartsList.add(chartTitle + " 2");
                                                newString = chartTitle + " 2";
                                            } else {
                                                chartsList.add(chartTitle);
                                                newString = chartTitle;
                                            }
                                        }
                                        if (newString != null) {
                                            commitEdit(newString);
                                            selectChartBox.getSelectionModel().select(newString);
                                        } else
                                            commitEdit(newValue.toString());
                                    }
                                });

                                hbox.getChildren().setAll(selectChartBox);
                                StackPane.setAlignment(selectChartBox, Pos.CENTER_LEFT);

                                selectChartBox.setDisable(!data.isSelectable());

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

    public ObservableList<String> getChartsList() {
        List<String> tempList = new ArrayList<>();
        final String chartTitle = rb.getString("graph.title");
        for (Map.Entry<String, ChartDataModel> mdl : _data.entrySet()) {
            if (!tempList.contains(mdl.getValue().getTitle()) && mdl.getValue().getTitle() != null)
                tempList.add(mdl.getValue().getTitle());
        }
        if (tempList.isEmpty() || !tempList.contains(chartTitle)) {
            tempList.add(chartTitle);
        }

        final String addChart = rb.getString("graph.table.addchart");

        tempList.add(addChart);
        chartsList = FXCollections.observableArrayList(tempList);
        return chartsList;
    }

    private ChoiceBox buildSelectChartBox(ChartDataModel data) {

        ChoiceBox chartsBox = new ChoiceBox();
        chartsBox.setItems(chartsList);
        chartsBox.getSelectionModel().select(data.getTitle());

        return chartsBox;
    }

    private ChartDataModel getData(JEVisTreeRow row) {
//        System.out.println("add" + row.getJEVisObject());
        String id = row.getID();
        if (_data.containsKey(id)) {
            return _data.get(id);
        } else {
            ChartDataModel newData = new ChartDataModel();
            newData.setObject(row.getJEVisObject());
            newData.setAttribute(row.getJEVisAttribute());
            _data.put(id, newData);
            return newData;
        }
    }

    public List<JEVisSample> getSelectedSamples(JEVisObject object) {
        return new ArrayList<>();
    }

    private TreeTableColumn<JEVisTreeRow, Color> buildColorColumn(JEVisTree tree, String columnName) {
        TreeTableColumn<JEVisTreeRow, Color> column = new TreeTableColumn(columnName);
        column.setPrefWidth(130);
        column.setCellValueFactory(param -> {
            ChartDataModel data = getData(param.getValue().getValue());
            return new ReadOnlyObjectWrapper<>(data.getColor());
//                return param.getValue().getValue().getColorProperty();
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
//                        getTreeTableRow().getItem().getColorProperty().setValue(newValue);
                    }

                    @Override
                    protected void updateItem(Color item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                        if (!empty) {
                            StackPane hbox = new StackPane();
                            if (getTreeTableRow().getItem() != null && tree != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), columnName)) {
                                ChartDataModel data = getData(getTreeTableRow().getItem());
                                ColorPicker colorPicker = new ColorPicker();

                                StackPane.setAlignment(hbox, Pos.CENTER_LEFT);
                                colorPicker.setValue(item);
//                                colorPicker.getStylesheets().add("/styles/ColorPicker.css");
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

    private TreeTableColumn<JEVisTreeRow, DateTime> buildDateColumn(JEVisTree tree, String columnName, DATE_TYPE type) {
        TreeTableColumn<JEVisTreeRow, DateTime> column = new TreeTableColumn(columnName);
        column.setPrefWidth(130);
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

//                        getTreeTableRow().getItem().getColorProperty().setValue(newValue);
                    }

                    @Override
                    protected void updateItem(DateTime item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                        if (!empty) {
                            StackPane hbox = new StackPane();
                            if (getTreeTableRow().getItem() != null && tree != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), columnName)) {
                                ChartDataModel data = getData(getTreeTableRow().getItem());
                                DatePicker dp = buildDatePicker(data, type);

                                hbox.getChildren().setAll(dp);
                                StackPane.setAlignment(hbox, Pos.CENTER_LEFT);

                                dp.setOnAction(event -> {
                                    LocalDate ld = dp.getValue();
                                    DateTime jodaTime = new DateTime(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), 0, 0);
                                    commitEdit(jodaTime);
                                });
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

    private ChoiceBox buildAggregateBox(final ChartDataModel data) {
        List<String> aggList = new ArrayList<>();

        final String keyPreset = rb.getString("graph.interval.preset");
        String keyDaily = rb.getString("graph.interval.daily");
        String keyWeekly = rb.getString("graph.interval.weekly");
        String keyMonthly = rb.getString("graph.interval.monthly");
        String keyYearly = rb.getString("graph.interval.yearly");


        aggList.add(keyPreset);
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
//                return param.getValue().getValue().getJEVisObject();
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, AGGREGATION>, TreeTableCell<JEVisTreeRow, AGGREGATION>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, AGGREGATION> call(TreeTableColumn<JEVisTreeRow, AGGREGATION> param) {

                TreeTableCell<JEVisTreeRow, AGGREGATION> cell = new TreeTableCell<JEVisTreeRow, AGGREGATION>() {

                    @Override
                    public void commitEdit(AGGREGATION newValue) {
                        super.commitEdit(newValue);
//                        getTreeTableRow().getItem().getColorProperty().setValue(newValue);
                    }

                    @Override
                    protected void updateItem(AGGREGATION item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
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
//                    JEVisClass dpClass = data.getObject().getDataSource().getJEVisClass("Data Processor");

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
//                        getTreeTableRow().getItem().getObjectSelectedProperty().setValue(newValue);
//                        DataModel data = getData(getTreeTableRow().getItem().getID());
//                        data.setSelected(newValue);
                    }

                    @Override
                    protected void updateItem(JEVisObject item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
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

    private TreeTableColumn<JEVisTreeRow, Boolean> buildSelectionColumn(JEVisTree tree, String columnName) {
        TreeTableColumn<JEVisTreeRow, Boolean> column = new TreeTableColumn(columnName);
        column.setPrefWidth(60);
        column.setEditable(true);

        //replace to use the datamodel
//        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<SelectionTreeRow, Boolean> param) -> param.getValue().getValue().getObjectSelectedProperty());
        column.setCellValueFactory(param -> {
            ChartDataModel data = getData(param.getValue().getValue());
            return new ReadOnlyObjectWrapper<>(data.getSelected());
        });

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
                    }

                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                        if (!empty) {
                            StackPane hbox = new StackPane();
                            CheckBox cbox = new CheckBox();

                            if (getTreeTableRow().getItem() != null && tree != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), columnName)) {
                                ChartDataModel data = getData(getTreeTableRow().getItem());
                                hbox.getChildren().setAll(cbox);
                                StackPane.setAlignment(hbox, Pos.CENTER_LEFT);
                                cbox.setSelected(item);

                                cbox.setOnAction(event -> commitEdit(cbox.isSelected()));

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

                };

                return cell;
            }
        });

        return column;

    }

    private TreeTableColumn<JEVisTreeRow, JEVisUnit> buildUnitColumn(JEVisTree tree, String columnName) {
        TreeTableColumn<JEVisTreeRow, JEVisUnit> column = new TreeTableColumn(columnName);
        column.setPrefWidth(60);
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
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                        if (!empty) {

                            StackPane hbox = new StackPane();

                            if (getTreeTableRow().getItem() != null && tree != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), columnName)) {
                                ChartDataModel data = getData(getTreeTableRow().getItem());
                                ChoiceBox box = buildUnitBox(data);

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

    private ChoiceBox buildUnitBox(ChartDataModel singleRow) {
        JEVisUnit selectedUnit = null;
        if (singleRow.getUnit() != null) selectedUnit = singleRow.getUnit();
        List<String> proNames = new ArrayList<>();

        Boolean isEnergyUnit = false;
        Boolean isVolumeUnit = false;
        JEVisUnit currentUnit = null;
        try {
            if (singleRow.getDataProcessor() != null) {
                currentUnit = singleRow.getDataProcessor().getAttribute("Value").getDisplayUnit();
            } else {
                currentUnit = singleRow.getObject().getAttribute("Value").getDisplayUnit();
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        for (EnergyUnit eu : EnergyUnit.values()) {
            if (eu.toString().equals(UnitManager.getInstance().formate(currentUnit))) {
                isEnergyUnit = true;
            }

        }
        if (isEnergyUnit) for (EnergyUnit eu : EnergyUnit.values()) proNames.add(eu.toString());

        for (VolumeUnit vu : VolumeUnit.values()) {
            if (vu.toString().equals(UnitManager.getInstance().formate(currentUnit))) {
                isVolumeUnit = true;
            }
        }
        if (isVolumeUnit) for (VolumeUnit vu : VolumeUnit.values()) proNames.add(vu.toString());

        Unit _l = NonSI.LITER;
        Unit _m3 = SI.CUBIC_METRE;

        Unit _W = SI.WATT;
        Unit _kW = SI.KILO(SI.WATT);
        Unit _MW = SI.MEGA(SI.WATT);
        Unit _GW = SI.GIGA(SI.WATT);
        Unit _Wh = SI.WATT.times(NonSI.HOUR);
        Unit _kWh = SI.KILO(SI.WATT).times(NonSI.HOUR);
        Unit _MWh = SI.MEGA(SI.WATT).times(NonSI.HOUR);
        Unit _GWh = SI.GIGA(SI.WATT).times(NonSI.HOUR);

        final JEVisUnit l = new JEVisUnitImp(_l);
        final JEVisUnit m3 = new JEVisUnitImp(_m3);

        final JEVisUnit W = new JEVisUnitImp(_W);
        final JEVisUnit kW = new JEVisUnitImp(_kW);
        final JEVisUnit MW = new JEVisUnitImp(_MW);
        final JEVisUnit GW = new JEVisUnitImp(_GW);
        final JEVisUnit Wh = new JEVisUnitImp(_Wh);
        final JEVisUnit kWh = new JEVisUnitImp(_kWh);
        final JEVisUnit MWh = new JEVisUnitImp(_MWh);
        final JEVisUnit GWh = new JEVisUnitImp(_GWh);

        ChoiceBox processorBox = new ChoiceBox(FXCollections.observableArrayList(proNames));

        processorBox.valueProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {
                final String finalNewValue = String.valueOf(newValue);
                switch (finalNewValue) {
                    case "W":
                        singleRow.setUnit(W);
                        break;
                    case "kW":
                        singleRow.setUnit(kW);
                        break;
                    case "MW":
                        singleRow.setUnit(MW);
                        break;
                    case "GW":
                        singleRow.setUnit(GW);
                        break;
                    case "Wh":
                        singleRow.setUnit(Wh);
                        break;
                    case "kWh":
                        singleRow.setUnit(kWh);
                        break;
                    case "MWh":
                        singleRow.setUnit(MWh);
                        break;
                    case "GWh":
                        singleRow.setUnit(GWh);
                        break;
                    case "m³":
                        singleRow.setUnit(m3);
                        break;
                    case "l":
                        singleRow.setUnit(l);
                        break;
                    default:
                        break;
                }
            }
        });

        if (selectedUnit != null)
            processorBox.getSelectionModel().select(UnitManager.getInstance().formate(selectedUnit));
        else processorBox.getSelectionModel().select(UnitManager.getInstance().formate(currentUnit));

        return processorBox;
    }

    private enum EnergyUnit {
        W, kW, MW, GW, Wh, kWh, MWh, GWh
    }

    private enum VolumeUnit {
        l("l"), m3("m³");

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

    public void set_data(Map<String, ChartDataModel> _data) {
        this._data = _data;
    }

    public enum AGGREGATION {

        None, Daily, Weekly, Monthly,
        Yearly
    }

}
