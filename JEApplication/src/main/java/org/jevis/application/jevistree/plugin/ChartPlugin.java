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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisUnit;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.application.jevistree.ColumnFactory;
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

//        try {
//            JEVisClass dataDir = null;
//            dataDir = tree.getJEVisDataSource().getJEVisClass("Data Directory");
//            List<JEVisObject> listDataDirs = tree.getJEVisDataSource().getObjects(dataDir, false);
//            JEVisTreeItem newItem = new JEVisTreeItem(tree, listDataDirs.get(0));
//            tree.setRoot(newItem);
//
//        } catch (JEVisException e) {
//            e.printStackTrace();
//        }

        _tree = tree;
    }

    public JEVisTree get_tree() {
        return _tree;
    }

    private final String chartTitle = rb.getString("graph.title");
    private final String addChart = rb.getString("graph.table.addchart");

    @Override
    public List<TreeTableColumn<JEVisTreeRow, Long>> getColumns() {
        List<TreeTableColumn<JEVisTreeRow, Long>> list = new ArrayList<>();

        TreeTableColumn<JEVisTreeRow, Long> column = new TreeTableColumn();
        column.setEditable(true);

        Image img = new Image(ChartPlugin.class.getResourceAsStream("/icons/" + "list-add.png"));
        ImageView image = new ImageView(img);
        image.fitHeightProperty().set(20);
        image.fitWidthProperty().set(20);
        Button addChart = new Button(rb.getString("graph.table.addchart"), image);

        addChart.setOnAction(event -> {
            if (!chartsList.contains(chartTitle)) {
                chartsList.add(chartTitle);

                TreeTableColumn<JEVisTreeRow, Boolean> selectColumn = buildSelectionColumn(_tree, chartsList.size() - 1);

                column.getColumns().add(column.getColumns().size() - 6, selectColumn);
            } else {
                int counter = 0;
                for (String s : chartsList) if (s.contains(chartTitle)) counter++;

                chartsList.add(chartTitle + " " + counter);

                TreeTableColumn<JEVisTreeRow, Boolean> selectColumn = buildSelectionColumn(_tree, chartsList.size() - 1);

                column.getColumns().add(column.getColumns().size() - 6, selectColumn);
            }
        });

        column.setGraphic(addChart);

        TreeTableColumn<JEVisTreeRow, Color> colorColumn = buildColorColumn(_tree, rb.getString("graph.table.color"));

        getChartsList();
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
        //Will happen if the user presses some kind of OK button
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
            newData.setAttribute(row.getJEVisAttribute());
            _data.put(id, newData);
            return newData;
        }
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
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
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

    public ObservableList<String> getChartsList() {
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
        if (tempList.isEmpty()) {
            tempList.add(chartTitle);
        }
        chartsList = FXCollections.observableArrayList(tempList);
        return chartsList;
    }

    private TreeTableColumn<JEVisTreeRow, Boolean> buildSelectionColumn(JEVisTree tree, Integer selectionColumnIndex) {

        String columnName = chartsList.get(selectionColumnIndex);

        TreeTableColumn<JEVisTreeRow, Boolean> column = new TreeTableColumn(columnName);
        column.setPrefWidth(120);
        column.setEditable(true);

        column.setCellValueFactory(param -> {
            ChartDataModel data = getData(param.getValue().getValue());
            Boolean selectedChart = data.get_selectedCharts().contains(chartsList.get(selectionColumnIndex));
            return new ReadOnlyObjectWrapper<>(data.getSelected() && selectedChart);
        });

        TextField tf = new TextField(columnName);
        tf.setText(columnName);
        tf.setEditable(true);

        tf.textProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {
                for (Map.Entry<String, ChartDataModel> entry : _data.entrySet()) {
                    ChartDataModel mdl = entry.getValue();
                    if (mdl.getSelected()) {
                        if (mdl.get_selectedCharts().contains(oldValue)) {
                            mdl.get_selectedCharts().set(mdl.get_selectedCharts().indexOf(oldValue), newValue);
                        }
                    }
                }
                chartsList.set(selectionColumnIndex, newValue);
            }
        });

        column.setGraphic(tf);
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
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                        if (!empty) {
                            StackPane hbox = new StackPane();
                            CheckBox cbox = new CheckBox();

                            if (getTreeTableRow().getItem() != null && tree != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), chartsList.get(selectionColumnIndex))) {
                                ChartDataModel data = getData(getTreeTableRow().getItem());
                                hbox.getChildren().setAll(cbox);
                                StackPane.setAlignment(hbox, Pos.CENTER_LEFT);
                                cbox.setSelected(item);

                                cbox.setOnAction(event -> {
                                    commitEdit(cbox.isSelected());

                                    if (cbox.isSelected()) {
                                        for (Color c : color_list) {
                                            if (!usedColors.contains(c)) {
                                                data.setColor(c);
                                                usedColors.add(c);
                                                break;
                                            }
                                        }
                                    } else {
                                        usedColors.remove(data.getColor());
                                        data.setColor(Color.LIGHTBLUE);
                                    }
                                    getTreeTableView().refresh();
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
        Boolean isMassUnit = false;
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

        for (MassUnit mu : MassUnit.values()) {
            if (mu.toString().equals(UnitManager.getInstance().formate(currentUnit))) {
                isMassUnit = true;
            }
        }
        if (isMassUnit) for (MassUnit mu : MassUnit.values()) proNames.add(mu.toString());

        Unit _kg = SI.KILOGRAM;
        Unit _t = NonSI.METRIC_TON;

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

        final JEVisUnit kg = new JEVisUnitImp(_kg);
        final JEVisUnit t = new JEVisUnitImp(_t);

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
                    case "kg":
                        singleRow.setUnit(kg);
                        break;
                    case "t":
                        singleRow.setUnit(t);
                        break;
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

    private enum MassUnit {
        kg, t
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
        _tree.getColumns().clear();
        _tree.getColumns().addAll(ColumnFactory.buildName(), ColumnFactory.buildID());
        for (TreeTableColumn<JEVisTreeRow, Long> column : getColumns()) _tree.getColumns().add(column);
    }

    public enum AGGREGATION {

        None, Daily, Weekly, Monthly,
        Yearly
    }

}
