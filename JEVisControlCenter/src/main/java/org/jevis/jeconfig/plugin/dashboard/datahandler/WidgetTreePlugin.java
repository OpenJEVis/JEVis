package org.jevis.jeconfig.plugin.dashboard.datahandler;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.unit.ChartUnits.*;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.utils.Benchmark;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.AggregationBox;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.ChartTypeComboBox;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.control.ColorPickerAdv;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeItem;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;
import org.jevis.jeconfig.application.jevistree.TreePlugin;
import org.jevis.jeconfig.plugin.dashboard.config.DataPointNode;

import javax.measure.unit.Unit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WidgetTreePlugin implements TreePlugin {

    public static String COLUMN = "DataModel";
    public static String COLUMN_COLOR = I18n.getInstance().getString("plugin.dashboard.datatree.color");
    public static String COLUMN_CHART_TYPE = I18n.getInstance().getString("graph.tabs.tab.charttype");
    public static String COLUMN_SELECTED = I18n.getInstance().getString("plugin.dashboard.datatree.selection");
    public static String COLUMN_ENPI = I18n.getInstance().getString("plugin.dashboard.datatree.math");
    public static String COLUMN_AGGREGATION = I18n.getInstance().getString("plugin.graph.interval.label");
    public static String COLUMN_MANIPULATION = I18n.getInstance().getString("plugin.graph.manipulation.label");
    public static String DATA_MODEL_NODE = "DataModelNode";
    public static String COLUMN_CLEANING = I18n.getInstance().getString("graph.table.cleaning");
    public static String COLUMN_UNIT = I18n.getInstance().getString("graph.table.unit");

    final String keyPreset = I18n.getInstance().getString("plugin.graph.interval.preset");
    final String keyTotal = I18n.getInstance().getString("plugin.graph.manipulation.total");
    final String keyRunningMean = I18n.getInstance().getString("plugin.graph.manipulation.runningmean");
    final String keyCentricRunningMean = I18n.getInstance().getString("plugin.graph.manipulation.centricrunningmean");
    final String keySortedMin = I18n.getInstance().getString("plugin.graph.manipulation.sortedmin");
    final String keySortedMax = I18n.getInstance().getString("plugin.graph.manipulation.sortedmax");
    final String rawDataString = I18n.getInstance().getString("graph.processing.raw");

    private JEVisTree jeVisTree;
    private JEVisClass cleanDataClass = null;

    private final List<JEVisTreeItem> selectedTreeItems = new ArrayList<>();

    private Map<Long, Long> targetCalcMap = new HashMap<>();

    private final List<ManipulationMode> customList = new ArrayList<ManipulationMode>() {
        {
            add(ManipulationMode.NONE);
            add(ManipulationMode.RUNNING_MEAN);
            add(ManipulationMode.CENTRIC_RUNNING_MEAN);
            add(ManipulationMode.SORTED_MIN);
            add(ManipulationMode.SORTED_MAX);
        }
    };


    public WidgetTreePlugin() {
//        this.data = preset;
    }

    public List<DataPointNode> getUserSelection() {
        final List<DataPointNode> data = new ArrayList<>();

        selectedTreeItems.forEach(jeVisTreeItem -> {
            DataPointNode node = (DataPointNode) jeVisTreeItem.getValue().getDataObject(DATA_MODEL_NODE, null);
            if (node != null) {
                data.add(node);
            }
        });
//        this.jeVisTree.getItems().forEach(jeVisTreeItem -> {
//            JEVisTreeRow row = jeVisTreeItem.getValue();
//            boolean isSelected = (Boolean) row.getDataObject(COLUMN_SELECTED, false);
//            if (isSelected) {
//                DataPointNode node = (DataPointNode) row.getDataObject(DATA_MODEL_NODE, null);
//                if (node != null) {
//                    data.add(node);
//                }
//
//            }
//
//
//        });
        return data;
    }

    public void setUserSelection(List<DataPointNode> userSelection) {

        userSelection.forEach(dataModelNode -> {
            try {
                JEVisObject object = this.jeVisTree.getJEVisDataSource().getObject(dataModelNode.getObjectID());
                if (object != null) {
                    JEVisTreeItem item = this.jeVisTree.getItemForObject(object);
                    if (item != null) {
                        selectedTreeItems.add(item);

                        JEVisTreeRow row = item.getValue();
                        row.setDataObject(COLUMN_SELECTED, true);
                        row.setDataObject(DATA_MODEL_NODE, dataModelNode);

                        this.jeVisTree.openPathToObject(object);
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

    }


    @Override
    public void setTree(JEVisTree tree) {
        this.jeVisTree = tree;
        try {
            Benchmark benchmark = new Benchmark();
            this.targetCalcMap = getENPICalcMap();
            this.cleanDataClass = tree.getJEVisDataSource().getJEVisClass("Clean Data");
            benchmark.printBechmark("Loading ENPI IDs");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public List<TreeTableColumn<JEVisTreeRow, Long>> getColumns() {
        List<TreeTableColumn<JEVisTreeRow, Long>> list = new ArrayList<>();

        TreeTableColumn<JEVisTreeRow, Long> pluginHeader = new TreeTableColumn<>("Daten");
        pluginHeader.setId(COLUMN);

        pluginHeader.getColumns().addAll(
                buildSelection(),
                buildChartType(),
                buildColorColumn(),
                buildManipulationColumn(),
                buildAggregationColumn(),
                buildDataProcessorColumn(),
                buildENIPColumn(),
                buildUnitColumn());
        list.add(pluginHeader);

        return list;
    }

    private TreeTableColumn<JEVisTreeRow, ChartType> buildChartType() {
        TreeTableColumn<JEVisTreeRow, ChartType> column = new TreeTableColumn<>(COLUMN_CHART_TYPE);
        column.setPrefWidth(114);
        column.setEditable(true);
        column.setId(COLUMN_CHART_TYPE);

        column.setCellValueFactory(param -> {
            DataPointNode dataPoint = getDataPointNode(param.getValue().getValue());
            if (dataPoint != null && dataPoint.getChartType() != null) {
                return new ReadOnlyObjectWrapper<>(dataPoint.getChartType());
            }
            return new ReadOnlyObjectWrapper<>(ChartType.LINE);
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, ChartType>, TreeTableCell<JEVisTreeRow, ChartType>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, ChartType> call(TreeTableColumn<JEVisTreeRow, ChartType> param) {


                TreeTableCell<JEVisTreeRow, ChartType> cell = new TreeTableCell<JEVisTreeRow, ChartType>() {
                    @Override
                    public void commitEdit(ChartType chartType) {

                        super.commitEdit(chartType);
                        DataPointNode dataPoint = getDataPointNode(getTreeTableRow().getItem());
                        dataPoint.setChartType(chartType);
                    }

                    @Override
                    protected void updateItem(ChartType item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(null);
                        setGraphic(null);

                        if (!empty) {
                            try {
                                boolean show = WidgetTreePlugin.this.jeVisTree.getFilter().showCell(column, getTreeTableRow().getItem());

                                if (show) {

                                    ChartTypeComboBox comboBoxChartType = new ChartTypeComboBox(item);
                                    comboBoxChartType.setPrefWidth(114);

                                    comboBoxChartType.valueProperty().addListener((observable, oldValue, newValue) -> {
                                        if (oldValue == null || newValue != oldValue) {
                                            ChartType type = ChartType.parseChartType(comboBoxChartType.getSelectionModel().getSelectedIndex());
                                            commitEdit(type);
                                        }
                                    });

                                    setGraphic(comboBoxChartType);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                };

                return cell;
            }
        });
        return column;
    }

    private TreeTableColumn<JEVisTreeRow, JEVisUnit> buildUnitColumn() {
        TreeTableColumn<JEVisTreeRow, JEVisUnit> column = new TreeTableColumn<>(COLUMN_UNIT);
        column.setPrefWidth(130);
        column.setEditable(true);
        column.setId(COLUMN_UNIT);

        column.setCellValueFactory(param -> {
            DataPointNode dataPoint = getDataPointNode(param.getValue().getValue());
            if (dataPoint != null && dataPoint.getUnit() != null) {
                return new ReadOnlyObjectWrapper<>(ChartUnits.parseUnit(dataPoint.getUnit()));
            }
            return new ReadOnlyObjectWrapper<>(new JEVisUnitImp(Unit.ONE));
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, JEVisUnit>, TreeTableCell<JEVisTreeRow, JEVisUnit>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, JEVisUnit> call(TreeTableColumn<JEVisTreeRow, JEVisUnit> param) {


                TreeTableCell<JEVisTreeRow, JEVisUnit> cell = new TreeTableCell<JEVisTreeRow, JEVisUnit>() {
                    @Override
                    public void commitEdit(JEVisUnit unit) {

                        super.commitEdit(unit);
                        DataPointNode dataPoint = getDataPointNode(getTreeTableRow().getItem());
                        dataPoint.setUnit(UnitManager.getInstance().format(unit).replace("·", ""));
                    }

                    @Override
                    protected void updateItem(JEVisUnit item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(null);
                        setGraphic(null);

                        if (!empty) {
                            try {
                                boolean show = WidgetTreePlugin.this.jeVisTree.getFilter().showCell(column, getTreeTableRow().getItem());

                                if (show) {

                                    DataPointNode dataPoint = getDataPointNode(getTreeTableRow().getItem());
                                    ComboBox<String> box = buildUnitBox(dataPoint);

                                    box.valueProperty().addListener((observable, oldValue, newValue) -> {
                                        if (!newValue.equals(oldValue)) {
                                            JEVisUnit jeVisUnit = ChartUnits.parseUnit(newValue);
                                            commitEdit(jeVisUnit);
                                        }
                                    });

                                    setGraphic(box);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                };

                return cell;
            }
        });
        return column;
    }

    private ComboBox<String> buildUnitBox(DataPointNode singleRow) {
        ComboBox<String> processorBox = new ComboBox<>();
        try {
            JEVisClass stringDataClass = null;

            stringDataClass = jeVisTree.getJEVisDataSource().getJEVisClass("String Data");
            JEVisObject object = jeVisTree.getJEVisDataSource().getObject(singleRow.getCleanObjectID());
            if (object == null) {
                object = jeVisTree.getJEVisDataSource().getObject(singleRow.getObjectID());
            }
//
            JEVisAttribute attribute = object.getAttribute(singleRow.getAttribute());

            if (!object.getJEVisClass().equals(stringDataClass)) {

                List<String> proNames = new ArrayList<>();

                boolean isEnergyUnit = false;
                boolean isVolumeUnit = false;
                boolean isMassUnit = false;
                boolean isPressureUnit = false;
                boolean isVolumeFlowUnit = false;
                Boolean isMoneyUnit = false;

                JEVisUnit currentUnit;
                if (singleRow.getUnit() != null) {
                    currentUnit = ChartUnits.parseUnit(singleRow.getUnit());
                } else {
                    currentUnit = attribute.getDisplayUnit();
                }

                for (EnergyUnit eu : EnergyUnit.values()) {
                    if (eu.toString().equals(UnitManager.getInstance().format(currentUnit).replace("·", ""))) {
                        isEnergyUnit = true;
                    } else if (UnitManager.getInstance().format(currentUnit).equals("") && currentUnit.getLabel().equals(eu.toString())) {
                        isEnergyUnit = true;
                    }
                }
                if (isEnergyUnit) for (EnergyUnit eu : EnergyUnit.values()) {
                    proNames.add(eu.toString());
                }

                for (VolumeUnit vu : VolumeUnit.values()) {
                    if (vu.toString().equals(UnitManager.getInstance().format(currentUnit).replace("·", ""))) {
                        isVolumeUnit = true;
                    } else if (UnitManager.getInstance().format(currentUnit).equals("") && currentUnit.getLabel().equals(vu.toString())) {
                        isEnergyUnit = true;
                    }
                }
                if (isVolumeUnit) for (VolumeUnit vu : VolumeUnit.values()) {
                    proNames.add(vu.toString());
                }

                for (MassUnit mu : MassUnit.values()) {
                    if (mu.toString().equals(UnitManager.getInstance().format(currentUnit).replace("·", ""))) {
                        isMassUnit = true;
                    } else if (UnitManager.getInstance().format(currentUnit).equals("") && currentUnit.getLabel().equals(mu.toString())) {
                        isEnergyUnit = true;
                    }
                }
                if (isMassUnit) for (MassUnit mu : MassUnit.values()) {
                    proNames.add(mu.toString());
                }

                for (PressureUnit pu : PressureUnit.values()) {
                    if (pu.toString().equals(UnitManager.getInstance().format(currentUnit).replace("·", ""))) {
                        isPressureUnit = true;
                    } else if (UnitManager.getInstance().format(currentUnit).equals("") && currentUnit.getLabel().equals(pu.toString())) {
                        isEnergyUnit = true;
                    }
                }
                if (isPressureUnit) for (PressureUnit pu : PressureUnit.values()) {
                    proNames.add(pu.toString());
                }

                for (VolumeFlowUnit vfu : VolumeFlowUnit.values()) {
                    if (vfu.toString().equals(UnitManager.getInstance().format(currentUnit).replace("·", ""))) {
                        isVolumeFlowUnit = true;
                    } else if (UnitManager.getInstance().format(currentUnit).equals("") && currentUnit.getLabel().equals(vfu.toString())) {
                        isEnergyUnit = true;
                    }
                }
                if (isVolumeFlowUnit) {
                    for (VolumeFlowUnit vfu : VolumeFlowUnit.values()) {
                        proNames.add(vfu.toString());
                    }
                }

                if (!isEnergyUnit && !isMassUnit && !isPressureUnit && !isVolumeFlowUnit && !isVolumeUnit) {
                    if (singleRow.getUnit() != null)
                        proNames.add(singleRow.getUnit());
                }

                for (MoneyUnit mu : MoneyUnit.values()) {
                    if (mu.toString().equals(UnitManager.getInstance().format(currentUnit).replace("·", ""))) {
                        isMoneyUnit = true;
                    } else if (UnitManager.getInstance().format(currentUnit).equals("") && currentUnit.getLabel().equals(mu.toString())) {
                        isMoneyUnit = true;
                    }
                }
                if (isMoneyUnit) for (MoneyUnit mu : MoneyUnit.values()) {
                    proNames.add(mu.toString());
                }


                processorBox.setItems(FXCollections.observableArrayList(proNames));

                processorBox.setPrefWidth(90);
                processorBox.setMinWidth(70);

                if (currentUnit != null) {
                    processorBox.getSelectionModel().select(currentUnit.getLabel());
                }

            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return processorBox;

    }

    @Override
    public void selectionFinished() {

    }

    @Override
    public String getTitle() {
        return "Title";
    }


    public TreeTableColumn<JEVisTreeRow, JEVisObject> buildDataProcessorColumn() {
        TreeTableColumn<JEVisTreeRow, JEVisObject> column = new TreeTableColumn(COLUMN_CLEANING);
        column.setPrefWidth(80);
        column.setId(COLUMN_CLEANING);


        column.setCellValueFactory(param -> {
//            DataPointNode dataPoint = (DataPointNode) param.getValue().getValue().getDataObject(DATA_MODEL_NODE, null);
            DataPointNode dataPoint = getDataPointNode(param.getValue().getValue());

            try {
                if (dataPoint != null && dataPoint.getCleanObjectID() != null) {
                    JEVisObject cleanObject = this.jeVisTree.getJEVisDataSource().getObject(dataPoint.getCleanObjectID());
                    if (cleanObject != null) {
                        return new ReadOnlyObjectWrapper<>(cleanObject);
                    }
                }
                return new ReadOnlyObjectWrapper<>(param.getValue().getValue().getJEVisObject());


            } catch (Exception ex) {
                ex.printStackTrace();
                return new ReadOnlyObjectWrapper<>(null);
            }

        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, JEVisObject>, TreeTableCell<JEVisTreeRow, JEVisObject>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, JEVisObject> call(TreeTableColumn<JEVisTreeRow, JEVisObject> param) {

                TreeTableCell<JEVisTreeRow, JEVisObject> cell = new TreeTableCell<JEVisTreeRow, JEVisObject>() {

                    @Override
                    protected void updateItem(JEVisObject item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(null);
                        setGraphic(null);

                        if (!empty) {

                            boolean show = WidgetTreePlugin.this.jeVisTree.getFilter().showCell(column, getTreeTableRow().getItem());

                            if (show && item != null) {

                                ObservableList<JEVisObject> processors = FXCollections.observableArrayList();


                                JEVisObject rawObject = getTreeTableRow().getTreeItem().getValue().getJEVisObject();
                                try {
                                    processors.add(rawObject);
                                    processors.addAll(rawObject.getChildren(WidgetTreePlugin.this.cleanDataClass, true));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }

                                ComboBox<JEVisObject> processorBox = new ComboBox<>();
                                processorBox.setPrefWidth(180);
                                processorBox.setMinWidth(120);

                                processorBox.setItems(processors);


                                Callback<javafx.scene.control.ListView<JEVisObject>, ListCell<JEVisObject>> cellFactory = new Callback<javafx.scene.control.ListView<JEVisObject>, ListCell<JEVisObject>>() {
                                    @Override
                                    public ListCell<JEVisObject> call(javafx.scene.control.ListView<JEVisObject> param) {
                                        return new ListCell<JEVisObject>() {
                                            @Override
                                            protected void updateItem(JEVisObject jeVisObject, boolean empty) {
                                                super.updateItem(jeVisObject, empty);
                                                try {
                                                    if (empty || jeVisObject == null) {
                                                        setText("");
                                                    } else {
                                                        String text = "";
                                                        if (jeVisObject.getID().equals(rawObject.getID())) {
                                                            text = WidgetTreePlugin.this.rawDataString;
                                                        } else {
                                                            text = jeVisObject.getName();
                                                        }
                                                        setText(text);
                                                    }
                                                } catch (Exception ex) {
                                                    setText("Error");
                                                }
                                            }
                                        };
                                    }
                                };
                                processorBox.setCellFactory(cellFactory);
                                processorBox.setButtonCell(cellFactory.call(null));


                                if (item != null) {
                                    processorBox.getSelectionModel().select(item);
                                } else {
                                    processorBox.getSelectionModel().selectFirst();
                                }

                                processorBox.setOnAction(event -> {
                                    if (getTreeTableRow() != null && getTreeTableRow().getItem() != null) {
//                                        System.out.println("getDataPointNode(getTreeTableRow()): " + getDataPointNode(getTreeTableRow()));
//                                        System.out.println("processorBox.getSelectionModel().getSelectedItem(): " + processorBox.getSelectionModel().getSelectedItem());
//                                        System.out.println("+.getid: " + processorBox.getSelectionModel().getSelectedItem().getID());
                                        getDataPointNode(getTreeTableRow()).setCleanObjectID(processorBox.getSelectionModel().getSelectedItem().getID());
                                    }
                                });

                                setGraphic(new BorderPane(processorBox));
                            }
                        }
                    }

                };

                return cell;
            }
        });

        return column;
    }

    public TreeTableColumn<JEVisTreeRow, AggregationPeriod> buildAggregationColumn() {
        TreeTableColumn<JEVisTreeRow, AggregationPeriod> column = new TreeTableColumn(COLUMN_AGGREGATION);
        column.setPrefWidth(80);
        column.setId(COLUMN_AGGREGATION);

        column.setCellValueFactory(param -> {
//            DataPointNode dataPoint = (DataPointNode) param.getValue().getValue().getDataObject(DATA_MODEL_NODE, null);
            DataPointNode dataPoint = getDataPointNode(param.getValue().getValue());
            if (dataPoint != null) {
                return new ReadOnlyObjectWrapper<>(dataPoint.getAggregationPeriod());
            } else {
                return new ReadOnlyObjectWrapper<>(AggregationPeriod.NONE);
            }

        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, AggregationPeriod>, TreeTableCell<JEVisTreeRow, AggregationPeriod>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, AggregationPeriod> call(TreeTableColumn<JEVisTreeRow, AggregationPeriod> param) {

                TreeTableCell<JEVisTreeRow, AggregationPeriod> cell = new TreeTableCell<JEVisTreeRow, AggregationPeriod>() {

                    @Override
                    protected void updateItem(AggregationPeriod item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(null);
                        setGraphic(null);

                        if (!empty) {

                            boolean show = WidgetTreePlugin.this.jeVisTree.getFilter().showCell(column, getTreeTableRow().getItem());

                            if (show) {
                                AggregationBox aggregationBox = new AggregationBox(item);


                                aggregationBox.setOnAction(event -> {
                                    try {
                                        if (getTreeTableRow() != null && getTreeTableRow().getItem() != null) {
                                            getDataPointNode(getTreeTableRow()).setAggregationPeriod(aggregationBox.getValue());
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                });
                                setGraphic(new BorderPane(aggregationBox));
                            }
                        }
                    }

                };

                return cell;
            }
        });

        return column;
    }

    public TreeTableColumn<JEVisTreeRow, ManipulationMode> buildManipulationColumn() {
        TreeTableColumn<JEVisTreeRow, ManipulationMode> column = new TreeTableColumn(COLUMN_MANIPULATION);
        column.setPrefWidth(80);
        column.setId(COLUMN_MANIPULATION);

        column.setCellValueFactory(param -> {
//            DataPointNode dataPoint = (DataPointNode) param.getValue().getValue().getDataObject(DATA_MODEL_NODE, null);
            DataPointNode dataPoint = getDataPointNode(param.getValue().getValue());
            if (dataPoint != null) {
                return new ReadOnlyObjectWrapper<>(dataPoint.getManipulationMode());
            } else {
                return new ReadOnlyObjectWrapper<>(ManipulationMode.NONE);
            }

        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, ManipulationMode>, TreeTableCell<JEVisTreeRow, ManipulationMode>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, ManipulationMode> call(TreeTableColumn<JEVisTreeRow, ManipulationMode> param) {

                TreeTableCell<JEVisTreeRow, ManipulationMode> cell = new TreeTableCell<JEVisTreeRow, ManipulationMode>() {

                    @Override
                    protected void updateItem(ManipulationMode item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(null);
                        setGraphic(null);

                        if (!empty) {

                            boolean show = WidgetTreePlugin.this.jeVisTree.getFilter().showCell(column, getTreeTableRow().getItem());

                            if (show) {
                                ComboBox<ManipulationMode> mathBox = new ComboBox<>();


                                Callback<javafx.scene.control.ListView<ManipulationMode>, ListCell<ManipulationMode>> cellFactory = new Callback<javafx.scene.control.ListView<ManipulationMode>, ListCell<ManipulationMode>>() {
                                    @Override
                                    public ListCell<ManipulationMode> call(javafx.scene.control.ListView<ManipulationMode> param) {
                                        return new ListCell<ManipulationMode>() {
                                            @Override
                                            protected void updateItem(ManipulationMode manipulationMode, boolean empty) {
                                                super.updateItem(manipulationMode, empty);
                                                if (empty || manipulationMode == null) {
                                                    setText("");
                                                } else {
                                                    String text = "";
                                                    switch (manipulationMode) {
                                                        case NONE:
                                                            text = WidgetTreePlugin.this.keyPreset;
                                                            break;
                                                        case RUNNING_MEAN:
                                                            text = WidgetTreePlugin.this.keyRunningMean;
                                                            break;
                                                        case CENTRIC_RUNNING_MEAN:
                                                            text = WidgetTreePlugin.this.keyCentricRunningMean;
                                                            break;
                                                        case SORTED_MIN:
                                                            text = WidgetTreePlugin.this.keySortedMin;
                                                            break;
                                                        case SORTED_MAX:
                                                            text = WidgetTreePlugin.this.keySortedMax;
                                                            break;
                                                    }
                                                    setText(text);
                                                }
                                            }
                                        };
                                    }
                                };
                                mathBox.setCellFactory(cellFactory);
                                mathBox.setButtonCell(cellFactory.call(null));
                                mathBox.setItems(FXCollections.observableArrayList(WidgetTreePlugin.this.customList));
                                mathBox.getSelectionModel().select(item);


                                mathBox.setOnAction(event -> {
                                    try {
                                        if (getTreeTableRow() != null && getTreeTableRow().getItem() != null) {
                                            getDataPointNode(getTreeTableRow()).setManipulationMode(mathBox.getValue());
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                });
                                setGraphic(new BorderPane(mathBox));
                            }
                        }
                    }

                };

                return cell;
            }
        });

        return column;
    }

    public TreeTableColumn<JEVisTreeRow, Color> buildColorColumn() {
        TreeTableColumn<JEVisTreeRow, Color> column = new TreeTableColumn(COLUMN_COLOR);
        column.setPrefWidth(80);
        column.setId(COLUMN_COLOR);

        column.setCellValueFactory(param -> {
            DataPointNode dataPoint = getDataPointNode(param.getValue().getValue());
            if (dataPoint != null) {
                return new ReadOnlyObjectWrapper<>(dataPoint.getColor());
            } else {
                return new ReadOnlyObjectWrapper<>(Color.LIGHTBLUE);
            }
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Color>, TreeTableCell<JEVisTreeRow, Color>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Color> call(TreeTableColumn<JEVisTreeRow, Color> param) {

                TreeTableCell<JEVisTreeRow, Color> cell = new TreeTableCell<JEVisTreeRow, Color>() {

                    @Override
                    protected void updateItem(Color item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(null);
                        setGraphic(null);

                        if (!empty) {

                            boolean show = WidgetTreePlugin.this.jeVisTree.getFilter().showCell(column, getTreeTableRow().getItem());

                            if (show) {
                                ColorPickerAdv colorPicker = new ColorPickerAdv();
//                                colorPicker.setStyle("-fx-colr-label-visible: false ;");
                                colorPicker.setValue(item);
//                                colorPicker.getCustomColors().addAll(ColorColumn.color_list);

                                colorPicker.setOnAction(event -> {
                                    try {
                                        if (getTreeTableRow() != null && getTreeTableRow().getItem() != null) {
                                            getDataPointNode(getTreeTableRow()).setColor(colorPicker.getValue());
                                            setItem(colorPicker.getValue());
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                });
                                HBox hBox = new HBox(colorPicker);
                                hBox.setAlignment(Pos.CENTER);
                                setGraphic(hBox);
                            }
                        }
                    }

                };

                return cell;
            }
        });

        return column;
    }

    private DataPointNode getDataPointNode(TreeTableRow<JEVisTreeRow> row) {
        return getDataPointNode(row.getItem());
    }

    private DataPointNode getDataPointNode(JEVisTreeRow row) throws NullPointerException {


        DataPointNode dataPointNode;

        if (row != null && row.getDataObject(DATA_MODEL_NODE, null) != null) {
            dataPointNode = (DataPointNode) row.getDataObject(DATA_MODEL_NODE, null);

//            return dataPointNode;
        } else {
            dataPointNode = new DataPointNode();

            dataPointNode.setObjectID(row.getJEVisObject().getID());
            try {
                List<JEVisObject> cleanObjects = row.getJEVisObject().getChildren(this.cleanDataClass, true);
                if (!cleanObjects.isEmpty()) {
                    dataPointNode.setCleanObjectID(cleanObjects.get(0).getID());
                }
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }


            dataPointNode.setColor(Color.LIGHTBLUE);
            dataPointNode.setManipulationMode(ManipulationMode.NONE);
            dataPointNode.setAggregationPeriod(AggregationPeriod.NONE);
            dataPointNode.setAttribute("Value");


            row.setDataObject(DATA_MODEL_NODE, dataPointNode);

        }

        return dataPointNode;


//        System.out.println("row: " + row);
//        System.out.println("1: " + row.getDataObject(DATA_MODEL_NODE, ""));
//        DataPointNode dataPointNode = (DataPointNode) row.getDataObject(DATA_MODEL_NODE, null);
//        if (dataPointNode == null) {
//            dataPointNode = new DataPointNode();
//            dataPointNode.setObjectID(row.getJEVisObject().getID());
//            try {
//                List<JEVisObject> cleanObjects = row.getJEVisObject().getChildren(this.cleanDataClass, true);
//                if (!cleanObjects.isEmpty()) {
//                    dataPointNode.setCleanObjectID(cleanObjects.get(0).getID());
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//
//
//            dataPointNode.setColor(Color.LIGHTBLUE);
//            dataPointNode.setManipulationMode(ManipulationMode.NONE);
//            dataPointNode.setAggregationPeriod(AggregationPeriod.NONE);
//            dataPointNode.setAttribute("Value");
//
//
//            row.setDataObject(DATA_MODEL_NODE, dataPointNode);
//        }
//
//
//        return dataPointNode;
    }

    public TreeTableColumn<JEVisTreeRow, Boolean> buildSelection() {
        TreeTableColumn<JEVisTreeRow, Boolean> column = new TreeTableColumn(COLUMN_SELECTED);
        column.setPrefWidth(80);
        column.setId(COLUMN_SELECTED);

        column.setCellValueFactory(param -> {
//            Boolean selected = (Boolean) param.getValue().getValue().getDataObject(COLUMN_SELECTED, false);
//            Boolean selected = (Boolean) param.getValue().getValue().getDataObject(COLUMN_SELECTED, false);

            Boolean selected = selectedTreeItems.contains(param.getValue());

//            DataPointNode dataPoint = getDataPointNode(param.getValue().getValue());
            return new ReadOnlyObjectWrapper<>(selected);
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Boolean>, TreeTableCell<JEVisTreeRow, Boolean>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Boolean> call(TreeTableColumn<JEVisTreeRow, Boolean> param) {
                return new TreeTableCell<JEVisTreeRow, Boolean>() {

                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(null);
                        setGraphic(null);

                        if (item != null || !empty) {
                            boolean show = WidgetTreePlugin.this.jeVisTree.getFilter().showCell(column, getTreeTableRow().getItem());
                            //create an NodeForAll colmuns
//                            getDataPointNode(getTreeTableRow());//hmmm

                            if (show) {
                                CheckBox box = new CheckBox();
                                box.setSelected(item);
//                                box.setSelected((Boolean) getTreeTableRow().getItem().getDataObject(COLUMN_SELECTED, false));
                                box.setOnAction(event -> {
                                    try {
                                        if (box.isSelected()) {
                                            selectedTreeItems.add((JEVisTreeItem) getTreeTableRow().getTreeItem());
                                        } else {
                                            selectedTreeItems.remove(getTreeTableRow().getTreeItem());
                                        }

//                                        if (getTreeTableRow() != null && getTreeTableRow().getItem() != null) {
//                                            getTreeTableRow().getItem().setDataObject(COLUMN_SELECTED, box.isSelected());
//                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                });

                                setGraphic(new BorderPane(box));
                            }

                        }

                    }
                };
            }
        });

        return column;
    }

    public TreeTableColumn<JEVisTreeRow, Boolean> buildENIPColumn() {
        TreeTableColumn<JEVisTreeRow, Boolean> column = new TreeTableColumn(COLUMN_ENPI);
        column.setPrefWidth(80);
        column.setId(COLUMN_ENPI);

        column.setCellValueFactory(param -> {
//            DataPointNode dataPoint = (DataPointNode) param.getValue().getValue().getDataObject(DATA_MODEL_NODE, null);
            DataPointNode dataPoint = getDataPointNode(param.getValue().getValue());

            if (dataPoint != null && (dataPoint.getCalculationID() != null && dataPoint.getCalculationID() > 0l)) {
//                System.out.println("ENPI: " + dataPoint.getCalculationID());
                return new SimpleBooleanProperty(true);
            } else {
                return new SimpleBooleanProperty(false);
            }

        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Boolean>, TreeTableCell<JEVisTreeRow, Boolean>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Boolean> call(TreeTableColumn<JEVisTreeRow, Boolean> param) {
                return new TreeTableCell<JEVisTreeRow, Boolean>() {

                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(null);
                        setGraphic(null);

                        if (!empty) {
                            boolean show = WidgetTreePlugin.this.jeVisTree.getFilter().showCell(column, getTreeTableRow().getItem());

                            if (show) {
                                CheckBox box = new CheckBox();
                                box.setSelected(item);
                                box.setOnAction(event -> {
                                    try {
                                        if (getTreeTableRow() != null && getTreeTableRow().getItem() != null) {

                                            if (box.isSelected()) {
                                                //System.out.println("b1: "+WidgetTreePlugin.this.targetCalcMap.containsKey(getDataPointNode(getTreeTableRow()).getObjectID()));
                                                if (WidgetTreePlugin.this.targetCalcMap.containsKey(getDataPointNode(getTreeTableRow()).getObjectID())) {
                                                    getDataPointNode(getTreeTableRow()).setCalculationID(WidgetTreePlugin.this.targetCalcMap.get(getDataPointNode(getTreeTableRow()).getObjectID()));

                                                    //System.out.println("set for: "+getDataPointNode(getTreeTableRow()));
                                                    //System.out.println("calcID: "+WidgetTreePlugin.this.targetCalcMap.get(getDataPointNode(getTreeTableRow()).getObjectID()));
                                                }

                                            } else {
                                                getDataPointNode(getTreeTableRow()).setCleanObjectID(0l);
                                            }


                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                });

                                setGraphic(new BorderPane(box));
                            }

                        }

                    }
                };
            }
        });

        return column;
    }


    private Map<Long, Long> getENPICalcMap() throws JEVisException {
        Map<Long, Long> calcAndResult = new HashMap<>();

        JEVisClass calculation = this.jeVisTree.getJEVisDataSource().getJEVisClass("Calculation");
        JEVisClass outputClass = this.jeVisTree.getJEVisDataSource().getJEVisClass("Output");

        for (JEVisObject calculationObj : this.jeVisTree.getJEVisDataSource().getObjects(calculation, true)) {
            try {
                List<JEVisObject> outputs = calculationObj.getChildren(outputClass, true);

                if (outputs != null && !outputs.isEmpty()) {
                    // System.out.println("output: "+outputs);
                    for (JEVisObject output : outputs) {
                        JEVisAttribute tartgetAttribute = output.getAttribute("Output");
                        TargetHelper th = new TargetHelper(this.jeVisTree.getJEVisDataSource(), tartgetAttribute);
                        if (th != null && th.getObject() != null && !th.getObject().isEmpty()) {
                            calcAndResult.put(th.getObject().get(0).getID(), calculationObj.getID());
                            //System.out.println("Add "+output);
                        }
                    }


                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return calcAndResult;
    }

}
