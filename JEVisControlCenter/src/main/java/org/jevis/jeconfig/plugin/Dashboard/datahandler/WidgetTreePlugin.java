package org.jevis.jeconfig.plugin.Dashboard.datahandler;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.AggregationBox;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Columns.ColorColumn;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeItem;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;
import org.jevis.jeconfig.application.jevistree.TreePlugin;
import org.jevis.jeconfig.plugin.Dashboard.config.DataModelNode;
import org.jevis.jeconfig.plugin.Dashboard.config.DataPointNode;
import org.jevis.jeconfig.tool.I18n;

import java.util.ArrayList;
import java.util.List;

public class WidgetTreePlugin implements TreePlugin {

    public static String COLUMN = "DataModel";
    public static String COLUMN_COLOR = "Color";
    public static String COLUMN_SELECTED = "Selection";
    public static String COLUMN_AGGREGATION = "Aggregation";
    public static String COLUMN_MANIPULATION = "Manipulation";
    public static String COLUMN_DATA_POINT = "Manipulation";
    public static String COLUMN_CLEANING = "datenbereinigung";

    final String keyPreset = I18n.getInstance().getString("plugin.graph.interval.preset");
    final String keyTotal = I18n.getInstance().getString("plugin.graph.manipulation.total");
    final String keyRunningMean = I18n.getInstance().getString("plugin.graph.manipulation.runningmean");
    final String keyCentricRunningMean = I18n.getInstance().getString("plugin.graph.manipulation.centricrunningmean");
    final String keySortedMin = I18n.getInstance().getString("plugin.graph.manipulation.sortedmin");
    final String keySortedMax = I18n.getInstance().getString("plugin.graph.manipulation.sortedmax");
    final String rawDataString = I18n.getInstance().getString("graph.processing.raw");

    private JEVisTree jeVisTree;
    private List<DataPointNode> data = new ArrayList<>();
    private List<DataModelNode> userSelection = new ArrayList<>();

    private JEVisClass cleanDataClass = null;

    public WidgetTreePlugin() {
//        this.data = preset;
    }


    public void setUserSelection(List<DataPointNode> userSelection) {

        userSelection.forEach(dataModelNode -> {
            try {
                JEVisObject object = jeVisTree.getJEVisDataSource().getObject(dataModelNode.getObjectID());
                if (object != null) {
                    JEVisTreeItem item = this.jeVisTree.getItemForObject(object);
                    if (item != null) {
                        JEVisTreeRow row = item.getValue();
                        row.setDataObject(COLUMN_SELECTED, true);
                        row.setDataObject(COLUMN_COLOR, dataModelNode.getColor());
                        row.setDataObject(COLUMN_AGGREGATION, dataModelNode.getAggregationPeriod());
//                        row.setDataObject(COLUMN_MANIPULATION, dataModelNode.getManipulationMode());
//                        row.setDataObject(COLUMN_MANIPULATION, dataModelNode.getCleanObjectID());
                        row.setDataObject(COLUMN_DATA_POINT, dataModelNode);

                        jeVisTree.openPathToObject(object);
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

    }

    @Override
    public List<TreeTableColumn<JEVisTreeRow, Long>> getColumns() {
        List<TreeTableColumn<JEVisTreeRow, Long>> list = new ArrayList<>();

        TreeTableColumn<JEVisTreeRow, Long> pluginHeader = new TreeTableColumn<>("Daten");
        pluginHeader.setId(COLUMN);

        pluginHeader.getColumns().addAll(buildSelection(), buildColorColumn(), buildManipulationColumn(), buildAggregationColumn(), buildDataProcessorolumn());
        list.add(pluginHeader);

//        list.addAll(buildSelection(), buildColorColumn());
        return list;
    }

    @Override
    public void selectionFinished() {

    }

    @Override
    public String getTitle() {
        return "Title";
    }


    public TreeTableColumn<JEVisTreeRow, JEVisObject> buildDataProcessorolumn() {
        TreeTableColumn<JEVisTreeRow, JEVisObject> column = new TreeTableColumn(COLUMN_CLEANING);
        column.setPrefWidth(80);
        column.setId(COLUMN_CLEANING);


        try {
            cleanDataClass = jeVisTree.getJEVisDataSource().getJEVisClass("Clean Data");
        } catch (Exception ex) {

        }

        column.setCellValueFactory(param -> {
            DataPointNode dataPoint = (DataPointNode) param.getValue().getValue().getDataObject(COLUMN_DATA_POINT, null);

            try {
                if (dataPoint != null && dataPoint.getCleanObjectID() != null) {
                    JEVisObject cleanObject = jeVisTree.getJEVisDataSource().getObject(dataPoint.getCleanObjectID());
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

                            boolean show = jeVisTree.getFilter().showCell(column, getTreeTableRow().getItem());

                            if (show && item != null) {


                                ObservableList<JEVisObject> processors = FXCollections.observableArrayList();


                                JEVisObject rowObject = getTreeTableRow().getTreeItem().getValue().getJEVisObject();

                                try {
                                    processors.add(rowObject);
                                    processors.addAll(rowObject.getChildren(cleanDataClass, true));
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
                                                if (empty || jeVisObject == null) {
                                                    setText("");
                                                } else {
                                                    String text = "";
                                                    if (jeVisObject.getID().equals(rowObject.getID())) {
                                                        text = rawDataString;
                                                    } else {
                                                        text = jeVisObject.getName();
                                                    }
                                                    setText(text);
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
            DataPointNode dataPoint = (DataPointNode) param.getValue().getValue().getDataObject(COLUMN_DATA_POINT, null);
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

                            boolean show = jeVisTree.getFilter().showCell(column, getTreeTableRow().getItem());

                            if (show) {
                                AggregationBox aggregationBox = new AggregationBox(item);


//                                mathBox.setOnAction(event -> {
//                                    try {
//                                        if (getTreeTableRow() != null && getTreeTableRow().getItem() != null) {
//                                            getTreeTableRow().getItem().setDataObject(COLUMN_COLOR, colorPicker.getValue());
//                                        }
//                                    } catch (Exception ex) {
//                                        ex.printStackTrace();
//                                    }
//                                });
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
            DataPointNode dataPoint = (DataPointNode) param.getValue().getValue().getDataObject(COLUMN_DATA_POINT, null);
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

                            boolean show = jeVisTree.getFilter().showCell(column, getTreeTableRow().getItem());

                            if (show) {
                                ComboBox<ManipulationMode> mathBox = new ComboBox<>();

                                List<ManipulationMode> customList = new ArrayList<>();
                                customList.add(ManipulationMode.NONE);
                                customList.add(ManipulationMode.TOTAL);
                                customList.add(ManipulationMode.RUNNING_MEAN);
                                customList.add(ManipulationMode.CENTRIC_RUNNING_MEAN);
                                customList.add(ManipulationMode.SORTED_MIN);
                                customList.add(ManipulationMode.SORTED_MAX);

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
                                                            text = keyPreset;
                                                            break;
                                                        case TOTAL:
                                                            text = keyTotal;
                                                            break;
                                                        case RUNNING_MEAN:
                                                            text = keyRunningMean;
                                                            break;
                                                        case CENTRIC_RUNNING_MEAN:
                                                            text = keyCentricRunningMean;
                                                            break;
                                                        case SORTED_MIN:
                                                            text = keySortedMin;
                                                            break;
                                                        case SORTED_MAX:
                                                            text = keySortedMax;
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
                                mathBox.setItems(FXCollections.observableArrayList(customList));
                                mathBox.getSelectionModel().select(item);


//                                mathBox.setOnAction(event -> {
//                                    try {
//                                        if (getTreeTableRow() != null && getTreeTableRow().getItem() != null) {
//                                            getTreeTableRow().getItem().setDataObject(COLUMN_COLOR, colorPicker.getValue());
//                                        }
//                                    } catch (Exception ex) {
//                                        ex.printStackTrace();
//                                    }
//                                });
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
            Color color = (Color) param.getValue().getValue().getDataObject(COLUMN_COLOR, Color.LIGHTBLUE);
            return new ReadOnlyObjectWrapper<>(color);
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

                            boolean show = jeVisTree.getFilter().showCell(column, getTreeTableRow().getItem());

                            if (show) {
                                ColorPicker colorPicker = new ColorPicker();
                                colorPicker.setStyle("-fx-color-label-visible: false ;");
                                colorPicker.setValue(item);
                                colorPicker.getCustomColors().addAll(ColorColumn.color_list);

                                colorPicker.setOnAction(event -> {
                                    try {
                                        if (getTreeTableRow() != null && getTreeTableRow().getItem() != null) {
                                            getTreeTableRow().getItem().setDataObject(COLUMN_COLOR, colorPicker.getValue());
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                });
                                setGraphic(new BorderPane(colorPicker));
                            }
                        }
                    }

                };

                return cell;
            }
        });

        return column;
    }

    public TreeTableColumn<JEVisTreeRow, Boolean> buildSelection() {
        TreeTableColumn<JEVisTreeRow, Boolean> column = new TreeTableColumn(COLUMN_SELECTED);
        column.setPrefWidth(80);
        column.setId(COLUMN_SELECTED);

        column.setCellValueFactory(param -> {
            Boolean selected = (Boolean) param.getValue().getValue().getDataObject(COLUMN_SELECTED, false);
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

                        if (!empty) {
                            boolean show = jeVisTree.getFilter().showCell(column, getTreeTableRow().getItem());

                            if (show) {
                                CheckBox box = new CheckBox();
                                box.setSelected(item);
                                box.setOnAction(event -> {
                                    try {
                                        if (getTreeTableRow() != null && getTreeTableRow().getItem() != null) {
                                            getTreeTableRow().getItem().setDataObject(COLUMN_SELECTED, box.isSelected());
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


}
