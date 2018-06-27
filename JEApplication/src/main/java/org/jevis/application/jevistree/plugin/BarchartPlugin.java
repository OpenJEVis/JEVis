/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.application.jevistree.plugin;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jevis.api.*;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.application.jevistree.JEVisTree;
import org.jevis.application.jevistree.JEVisTreeRow;
import org.jevis.application.jevistree.TreePlugin;
import org.jevis.commons.dataprocessing.BasicProcess;
import org.jevis.commons.dataprocessing.BasicProcessOption;
import org.jevis.commons.dataprocessing.Process;
import org.jevis.commons.dataprocessing.ProcessOptions;
import org.jevis.commons.dataprocessing.function.AggrigatorFunction;
import org.jevis.commons.dataprocessing.function.InputFunction;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class BarchartPlugin implements TreePlugin {

    private JEVisTree _tree;

    private Map<Long, List<JEVisSample>> _samples = new HashMap<>();
    private Map<String, DataModel> _data = new HashMap<>();
    private TextField textField;
    private SaveResourceBundle rb = new SaveResourceBundle("jeapplication", AppLocale.getInstance().getLocale());
    private String _title = rb.getString("graph.title");

    private enum AGGREGATION {

        None, Daily, Weekly, Monthly,
        Yearly
    }

    private enum DATE_TYPE {

        START, END
    }

    @Override
    public void setTree(JEVisTree tree) {
        _tree = tree;
    }

    @Override
    public List<TreeTableColumn<JEVisTreeRow, Long>> getColumns() {
        List<TreeTableColumn<JEVisTreeRow, Long>> list = new ArrayList<>();

        TreeTableColumn<JEVisTreeRow, Long> column = new TreeTableColumn();
        column.setEditable(true);
        textField = new TextField();
        textField.setText(_title);
        textField.setEditable(true);
        column.setGraphic(textField);

        TreeTableColumn<JEVisTreeRow, Color> colorColumn = buildColorColumn(_tree, rb.getString("graph.table.color"));
        TreeTableColumn<JEVisTreeRow, Boolean> selectColumn = buildSelectionColumn(_tree, rb.getString("graph.table.load"));
        TreeTableColumn<JEVisTreeRow, AGGREGATION> aggregationColumn = buildAggregationColumn(_tree, rb.getString("graph.table.interval"));
        TreeTableColumn<JEVisTreeRow, JEVisObject> dataProcessorColumn = buildDataPorcessorColumn(_tree, rb.getString("graph.table.cleaning"));
        TreeTableColumn<JEVisTreeRow, DateTime> startDateColumn = buildDateColumn(_tree, rb.getString("graph.table.startdate"), DATE_TYPE.START);
        TreeTableColumn<JEVisTreeRow, DateTime> endDateColumn = buildDateColumn(_tree, rb.getString("graph.table.enddate"), DATE_TYPE.END);
        TreeTableColumn<JEVisTreeRow, JEVisUnit> unitColumn = buildUnitColumn(_tree, rb.getString("graph.table.unit"));

        column.getColumns().addAll(selectColumn, colorColumn, aggregationColumn, dataProcessorColumn, startDateColumn, endDateColumn, unitColumn);

        list.add(column);

        return list;
    }

    @Override
    public void selectionFinished() {
        //Will happen if the user peress some kinde of OK button
        System.out.println("selectionFinished()");
        for (Map.Entry<String, DataModel> entrySet : _data.entrySet()) {
            String key = entrySet.getKey();
            DataModel value = entrySet.getValue();
            if (value.getSelected()) {
                value.setTitle(getTitel());
                System.out.println("key: " + key);
            }

        }

    }

    public void setTitle(String title) {
        _title = title;
    }

    @Override
    public String getTitel() {
        return textField.getText();
    }

    public List<JEVisSample> getSelectedSamples(JEVisObject object) {
        return new ArrayList<>();
    }

    private DataModel getData(JEVisTreeRow row) {
//        System.out.println("add" + row.getJEVisObject());
        String id = row.getID();
        if (_data.containsKey(id)) {
            return _data.get(id);
        } else {
            DataModel newData = new DataModel();
            newData.setObject(row.getJEVisObject());
            newData.setAttribute(row.getJEVisAttribute());
            _data.put(id, newData);
            return newData;
        }
    }

    private TreeTableColumn<JEVisTreeRow, Color> buildColorColumn(JEVisTree tree, String columnName) {
        TreeTableColumn<JEVisTreeRow, Color> column = new TreeTableColumn(columnName);
        column.setPrefWidth(130);
        column.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<JEVisTreeRow, Color>, ObservableValue<Color>>() {

            @Override
            public ObservableValue<Color> call(TreeTableColumn.CellDataFeatures<JEVisTreeRow, Color> param) {
                DataModel data = getData(param.getValue().getValue());
                return new ReadOnlyObjectWrapper<>(data.getColor());
//                return param.getValue().getValue().getColorProperty();
            }
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Color>, TreeTableCell<JEVisTreeRow, Color>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Color> call(TreeTableColumn<JEVisTreeRow, Color> param) {

                TreeTableCell<JEVisTreeRow, Color> cell = new TreeTableCell<JEVisTreeRow, Color>() {

                    @Override
                    public void commitEdit(Color newValue) {
                        super.commitEdit(newValue);
                        DataModel data = getData(getTreeTableRow().getItem());
                        data.setColor(newValue);
//                        getTreeTableRow().getItem().getColorProperty().setValue(newValue);
                    }

                    @Override
                    protected void updateItem(Color item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                        if (!empty) {
                            StackPane hbox = new StackPane();
                            if (getTreeTableRow().getItem() != null && tree != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), columnName)) {
                                DataModel data = getData(getTreeTableRow().getItem());
                                ColorPicker colorPicker = new ColorPicker();

                                StackPane.setAlignment(hbox, Pos.CENTER_LEFT);
                                colorPicker.setValue(item);
//                                colorPicker.getStylesheets().add("/styles/ColorPicker.css");
                                colorPicker.setStyle("-fx-color-label-visible: false ;");

                                colorPicker.setOnAction(new EventHandler<ActionEvent>() {

                                    @Override
                                    public void handle(ActionEvent event) {
                                        commitEdit(colorPicker.getValue());
                                    }
                                });

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

    private DatePicker buildDatePicker(DataModel data, DATE_TYPE type) {

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
        column.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<JEVisTreeRow, DateTime>, ObservableValue<DateTime>>() {

            @Override
            public ObservableValue<DateTime> call(TreeTableColumn.CellDataFeatures<JEVisTreeRow, DateTime> param) {
                try {
                    DataModel data = getData(param.getValue().getValue());
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
            }
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, DateTime>, TreeTableCell<JEVisTreeRow, DateTime>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, DateTime> call(TreeTableColumn<JEVisTreeRow, DateTime> param) {

                TreeTableCell<JEVisTreeRow, DateTime> cell = new TreeTableCell<JEVisTreeRow, DateTime>() {

                    @Override
                    public void commitEdit(DateTime newValue) {
                        super.commitEdit(newValue);
                        DataModel data = getData(getTreeTableRow().getItem());

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
                                DataModel data = getData(getTreeTableRow().getItem());
                                DatePicker dp = buildDatePicker(data, type);

                                hbox.getChildren().setAll(dp);
                                StackPane.setAlignment(hbox, Pos.CENTER_LEFT);

                                dp.setOnAction(new EventHandler<ActionEvent>() {

                                    @Override
                                    public void handle(ActionEvent event) {
                                        LocalDate ld = dp.getValue();
                                        DateTime jodaTime = new DateTime(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), 0, 0);
                                        commitEdit(jodaTime);
                                    }
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

    private ChoiceBox buildAggregateBox(final DataModel data) {
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
        switch (data.getAggrigation()) {
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

        aggrigate.valueProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                //TODO:replace this quick and dirty workaround

                if (newValue.equals(keyPreset)) {
                    data.setAggrigation(AGGREGATION.None);
                } else if (newValue.equals(keyDaily)) {
                    data.setAggrigation(AGGREGATION.Daily);
                } else if (newValue.equals(keyWeekly)) {
                    data.setAggrigation(AGGREGATION.Weekly);
                } else if (newValue.equals(keyMonthly)) {
                    data.setAggrigation(AGGREGATION.Monthly);
                } else if (newValue.equals(keyYearly)) {
                    data.setAggrigation(AGGREGATION.Yearly);
                }


            }
        });

        return aggrigate;
    }

    private TreeTableColumn<JEVisTreeRow, AGGREGATION> buildAggregationColumn(JEVisTree tree, String columnName) {
        TreeTableColumn<JEVisTreeRow, AGGREGATION> column = new TreeTableColumn(columnName);
        column.setPrefWidth(100);
        column.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<JEVisTreeRow, AGGREGATION>, ObservableValue<AGGREGATION>>() {

            @Override
            public ObservableValue<AGGREGATION> call(TreeTableColumn.CellDataFeatures<JEVisTreeRow, AGGREGATION> param) {

                DataModel data = getData(param.getValue().getValue());

                return new ReadOnlyObjectWrapper<>(data.getAggrigation());
//                return param.getValue().getValue().getJEVisObject();
            }
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
                                DataModel data = getData(getTreeTableRow().getItem());
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

    private ChoiceBox buildProcessorBox(DataModel data) {
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
            Logger.getLogger(BarchartPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }

        ChoiceBox processorBox = new ChoiceBox();
        processorBox.setItems(FXCollections.observableArrayList(proNames));
        processorBox.getSelectionModel().selectFirst();
        processorBox.valueProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println("Select GUI Tpye: " + newValue);
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
                    Logger.getLogger(BarchartPlugin.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });

        return processorBox;
    }

    private TreeTableColumn<JEVisTreeRow, JEVisObject> buildDataPorcessorColumn(JEVisTree tree, String columnName) {
        TreeTableColumn<JEVisTreeRow, JEVisObject> column = new TreeTableColumn(columnName);
        column.setPrefWidth(120);
        column.setEditable(true);

        column.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<JEVisTreeRow, JEVisObject>, ObservableValue<JEVisObject>>() {

            @Override
            public ObservableValue<JEVisObject> call(TreeTableColumn.CellDataFeatures<JEVisTreeRow, JEVisObject> param) {
                DataModel data = getData(param.getValue().getValue());
                return new ReadOnlyObjectWrapper<>(data.getDataProcessor());
            }
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, JEVisObject>, TreeTableCell<JEVisTreeRow, JEVisObject>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, JEVisObject> call(TreeTableColumn<JEVisTreeRow, JEVisObject> param) {

                TreeTableCell<JEVisTreeRow, JEVisObject> cell = new TreeTableCell<JEVisTreeRow, JEVisObject>() {

                    @Override
                    public void commitEdit(JEVisObject newValue) {
                        super.commitEdit(newValue);
//                        getTreeTableRow().getItem().getObjectSelecedProperty().setValue(newValue);
//                        DataModel data = getData(getTreeTableRow().getItem().getID());
//                        data.setSelected(newValue);
                    }

                    @Override
                    protected void updateItem(JEVisObject item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                        if (!empty) {
                            StackPane hbox = new StackPane();

                            if (getTreeTableRow().getItem() != null && tree != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), columnName)) {
                                DataModel data = getData(getTreeTableRow().getItem());
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
//        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<SelectionTreeRow, Boolean> param) -> param.getValue().getValue().getObjectSelecedProperty());
        column.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<JEVisTreeRow, Boolean>, ObservableValue<Boolean>>() {

            @Override
            public ObservableValue<Boolean> call(TreeTableColumn.CellDataFeatures<JEVisTreeRow, Boolean> param) {
                DataModel data = getData(param.getValue().getValue());
                return new ReadOnlyObjectWrapper<>(data.getSelected());
            }
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Boolean>, TreeTableCell<JEVisTreeRow, Boolean>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Boolean> call(TreeTableColumn<JEVisTreeRow, Boolean> param) {

                TreeTableCell<JEVisTreeRow, Boolean> cell = new TreeTableCell<JEVisTreeRow, Boolean>() {

                    @Override
                    public void commitEdit(Boolean newValue) {
                        super.commitEdit(newValue);
                        getTreeTableRow().getItem().getObjectSelecedProperty().setValue(newValue);
                        DataModel data = getData(getTreeTableRow().getItem());
                        data.setSelected(newValue);
                    }

                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                        if (!empty) {
                            StackPane hbox = new StackPane();
                            CheckBox cbox = new CheckBox();

                            if (getTreeTableRow().getItem() != null && tree != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), columnName)) {
                                DataModel data = getData(getTreeTableRow().getItem());
                                hbox.getChildren().setAll(cbox);
                                StackPane.setAlignment(hbox, Pos.CENTER_LEFT);
                                cbox.setSelected(item);

                                cbox.setOnAction(new EventHandler<ActionEvent>() {

                                    @Override
                                    public void handle(ActionEvent event) {
                                        commitEdit(cbox.isSelected());
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

        //replace to use the datamodel
//        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<SelectionTreeRow, Boolean> param) -> param.getValue().getValue().getObjectSelecedProperty());
        column.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<JEVisTreeRow, JEVisUnit>, ObservableValue<JEVisUnit>>() {

            @Override
            public ObservableValue<JEVisUnit> call(TreeTableColumn.CellDataFeatures<JEVisTreeRow, JEVisUnit> param) {
                DataModel data = getData(param.getValue().getValue());
                return new ReadOnlyObjectWrapper<>(data.getUnit());
            }
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, JEVisUnit>, TreeTableCell<JEVisTreeRow, JEVisUnit>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, JEVisUnit> call(TreeTableColumn<JEVisTreeRow, JEVisUnit> param) {

                TreeTableCell<JEVisTreeRow, JEVisUnit> cell = new TreeTableCell<JEVisTreeRow, JEVisUnit>() {

                    @Override
                    public void commitEdit(JEVisUnit newValue) {
                        super.commitEdit(newValue);
                        DataModel data = getData(getTreeTableRow().getItem());
                        data.setUnit(newValue);
                    }

                    @Override
                    protected void updateItem(JEVisUnit item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                        if (!empty) {
                            StackPane hbox = new StackPane();
                            Button unitButton = new Button();
                            unitButton.setPrefWidth(50);

                            if (getTreeTableRow().getItem() != null && tree != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), columnName)) {

                                DataModel data = getData(getTreeTableRow().getItem());

                                if (data.getUnit() != null) {
                                    unitButton.setText(data.getUnit().toString());
                                }

                                hbox.getChildren().setAll(unitButton);
                                StackPane.setAlignment(unitButton, Pos.CENTER_LEFT);

                                unitButton.setOnAction(new EventHandler<ActionEvent>() {

                                    @Override
                                    public void handle(ActionEvent event) {
                                        AttributeSettingsDialog dialog = new AttributeSettingsDialog();
                                        try {
                                            dialog.show((Stage) unitButton.getScene().getWindow(), data.getAttribute());
                                            commitEdit(data.getAttribute().getDisplayUnit());
                                            unitButton.setText(data.getAttribute().getDisplayUnit().toString());
                                        } catch (JEVisException ex) {
                                            Logger.getLogger(BarchartPlugin.class.getName()).log(Level.SEVERE, null, ex);
                                        }

                                    }
                                });

                                if (data.getAttribute() != null && data.getAttribute().hasSample()) {
                                    unitButton.setDisable(false);
                                } else {
                                    unitButton.setDisable(true);
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

    public Map<String, BarchartPlugin.DataModel> getSelectedData() {
        return _data;
    }

    public class DataModel {

        private TableEntry tableEntry;
        private String _title;
        private DateTime _selectedStart;
        private DateTime _selectedEnd;
        private JEVisObject _object;
        private JEVisAttribute _attribute;
        private Color _color = Color.LIGHTBLUE;
        private boolean _selected = false;
        private Process _task = null;
        private AGGREGATION aggrigation = AGGREGATION.None;
        private JEVisObject _dataProcessorObject = null;
        private List<JEVisSample> samples = new ArrayList<>();
        private TreeMap<Double, JEVisSample> sampleMap = new TreeMap<>();
        private boolean _somethineChanged = true;
        private JEVisUnit _unit;

        public DataModel() {
        }

        public JEVisUnit getUnit() {
            try {
                if (getAttribute() != null) {
                    return getAttribute().getDisplayUnit();
                }

//            return _unit;
            } catch (JEVisException ex) {
                Logger.getLogger(BarchartPlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }

        public void setUnit(JEVisUnit _unit) {
            _somethineChanged = true;
            this._unit = _unit;
        }

        public List<JEVisSample> getSamples() {
            System.out.println("getSamples()");

            if (_somethineChanged) {
                _somethineChanged = false;
                samples = new ArrayList<>();

                try {
                    JEVisDataSource ds = _object.getDataSource();
                    Process aggrigate = null;
                    if (aggrigation == AGGREGATION.None) {

                    } else if (aggrigation == AGGREGATION.Daily) {
                        aggrigate = new BasicProcess();
                        aggrigate.setJEVisDataSource(ds);
                        aggrigate.setID("Dynamic");
                        aggrigate.setFunction(new AggrigatorFunction());
//                        aggrigate.addOption(Options.PERIOD, Period.days(1).toString());
                        aggrigate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.days(1).toString()));
                    } else if (aggrigation == AGGREGATION.Monthly) {
                        aggrigate = new BasicProcess();
                        aggrigate.setJEVisDataSource(ds);
                        aggrigate.setID("Dynamic");
                        aggrigate.setFunction(new AggrigatorFunction());
//                        aggrigate.addOption(Options.PERIOD, Period.months(1).toString());
                        aggrigate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.months(1).toString()));
                    } else if (aggrigation == AGGREGATION.Weekly) {
                        aggrigate = new BasicProcess();
                        aggrigate.setJEVisDataSource(ds);
                        aggrigate.setID("Dynamic");
                        aggrigate.setFunction(new AggrigatorFunction());
//                        aggrigate.addOption(Options.PERIOD, Period.weeks(1).toString());
                        aggrigate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.weeks(1).toString()));
                    } else if (aggrigation == AGGREGATION.Yearly) {
//                        System.out.println("year.....  " + Period.years(1).toString());
                        aggrigate = new BasicProcess();
                        aggrigate.setJEVisDataSource(ds);
                        aggrigate.setID("Dynamic");
                        aggrigate.setFunction(new AggrigatorFunction());
//                        aggrigate.addOption(Options.PERIOD, Period.years(1).toString());
                        aggrigate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.years(1).toString()));
                    }

//                    Process dataPorceessor = null;
                    if (getDataProcessor() != null) {
//                        dataPorceessor = ProcessChains.getProcessChain(getDataProcessor());
                        _object = getDataProcessor();
                        _attribute = _object.getAttribute("Value");
                    }
//                    if (dataPorceessor == null) {
                        if (aggrigate != null) {
                            Process input = new BasicProcess();
                            input.setJEVisDataSource(ds);
                            input.setID("Dynamic Input");
                            input.setFunction(new InputFunction());

                            input.getOptions().add(new BasicProcessOption(InputFunction.ATTRIBUTE_ID, _attribute.getName()));
                            input.getOptions().add(new BasicProcessOption(InputFunction.OBJECT_ID, _attribute.getObject().getID() + ""));
//                            input.getOptions().put(InputFunction.ATTRIBUTE_ID, getAttribute().getName());
//                            input.getOptions().put(InputFunction.OBJECT_ID, getAttribute().getObject().getID() + "");
                            aggrigate.setSubProcesses(Arrays.asList(input));
                            samples.addAll(aggrigate.getResult());
                        } else {
                            samples.addAll(getAttribute().getSamples(getSelectedStart(), getSelectedEnd()));
//                            samples.addAll(getAttribute().getAllSamples());
                        }

//                    } else if (aggrigate != null) {
//                        aggrigate.setSubProcesses(Arrays.asList(dataPorceessor));
//                        samples.addAll(aggrigate.getResult());
//                    } else {
//                        samples.addAll(dataPorceessor.getResult());
//                    }

                } catch (Exception ex) {
                    //TODO: exeption handling
                    ex.printStackTrace();
                }
            }

            return samples;
        }

        public TableEntry getTableEntry() {
            return tableEntry;
        }

        public void setTableEntry(TableEntry tableEntry) {
            this.tableEntry = tableEntry;
        }

        public TreeMap<Double, JEVisSample> getSampleMap() {
            return sampleMap;
        }

        public void setSampleMap(TreeMap<Double, JEVisSample> sampleMap) {
            this.sampleMap = sampleMap;
        }

        public JEVisObject getDataProcessor() {
            _somethineChanged = true;
            return _dataProcessorObject;
        }

        public void setDataProcessor(JEVisObject _dataProcessor) {
            this._dataProcessorObject = _dataProcessor;
        }

        public AGGREGATION getAggrigation() {
            return aggrigation;
        }

        public void setAggrigation(AGGREGATION aggrigation) {
            _somethineChanged = true;
            this.aggrigation = aggrigation;
        }

        public boolean getSelected() {

            return _selected;
        }

        public String getTitle() {
            return _title;
        }

        public void setTitle(String _title) {
            this._title = _title;
        }

        public void setSelected(boolean selected) {
            _selected = selected;
            System.out.println("is selectec: " + _object.getName() + "   unit: " + getUnit());
        }

        public DateTime getSelectedStart() {

            if (_selectedStart != null) {
                return _selectedStart;
            } else if (getAttribute() != null) {
                _selectedStart = getAttribute().getTimestampFromFirstSample();
                return _selectedStart;
            } else {
                return null;
            }

//            if (_selectedStart != null && getAttribute() != null) {
//                System.out.print("-");
////                System.out.println("getSelectedStart1 " + getAttribute().getTimestampFromFirstSample());
//                return getAttribute().getTimestampFromFirstSample();
//            }
//            System.out.print(".");
////            System.out.println("getSelectedStart2 " + _selectedStart);
//            return _selectedStart;
        }

        public void setSelectedStart(DateTime selectedStart) {
            if (_selectedEnd == null || !_selectedEnd.equals(selectedStart)) {
                _somethineChanged = true;
            }
            this._selectedStart = selectedStart;
        }

        public DateTime getSelectedEnd() {
            if (_selectedEnd != null) {
                return _selectedEnd;
            } else if (getAttribute() != null) {
                _selectedEnd = getAttribute().getTimestampFromLastSample();
                return _selectedEnd;
            } else {
                return null;
            }

//            if (_selectedEnd != null && getAttribute() != null) {
//                return getAttribute().getTimestampFromLastSample();
//            }
//
//            return _selectedEnd;
        }

        public void setSelectedEnd(DateTime selectedEnd) {
            if (_selectedEnd == null || !_selectedEnd.equals(selectedEnd)) {
                _somethineChanged = true;
            }
            this._selectedEnd = selectedEnd;
        }

        public JEVisObject getObject() {
            return _object;
        }

        public void setObject(JEVisObject _object) {
//            System.out.println("new DataModel: " + _object);
            this._object = _object;
        }

        public JEVisAttribute getAttribute() {

            if (_attribute == null) {
//                System.out.println("att is null");
                try {
                    if (getObject().getJEVisClassName().equals("Data") || getObject().getJEVisClassName().equals("Clean Data")) {
                        JEVisAttribute values = getObject().getAttribute("Value");
                        _attribute = values;
                    }
//                    return values;
                } catch (Exception ex) {
                    Logger.getLogger(BarchartPlugin.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            return _attribute;
        }

        public void setAttribute(JEVisAttribute _attribute) {
            this._attribute = _attribute;
        }

        public Color getColor() {
            return _color;
        }

        public void setColor(Color _color) {
            this._color = _color;
        }

        public boolean isSelectable() {
            return getAttribute() != null && getAttribute().hasSample();
        }

    }

}
