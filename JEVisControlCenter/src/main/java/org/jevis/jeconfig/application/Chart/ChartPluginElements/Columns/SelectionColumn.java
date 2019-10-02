package org.jevis.jeconfig.application.Chart.ChartPluginElements.Columns;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.ChartTypeComboBox;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.ChartNameTextField;
import org.jevis.jeconfig.application.Chart.ChartSettings;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;
import org.jevis.jeconfig.application.tools.DisabledItemsComboBox;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */

public class SelectionColumn extends TreeTableColumn<JEVisTreeRow, Boolean> implements ChartPluginColumn {
    private static final Logger logger = LogManager.getLogger(SelectionColumn.class);
    public static String COLUMN_ID = "SelectionColumn";
    private final List<TreeTableColumn<JEVisTreeRow, Boolean>> selectionColumns;
    private final TreeTableColumn<JEVisTreeRow, Long> allColumns;
    private TreeTableColumn<JEVisTreeRow, Boolean> selectionColumn;
    private GraphDataModel data;
    private JEVisTree tree;
    private ColorColumn colorColumn;
    private Integer chartId;
    private final JEVisDataSource dataSource;

    public SelectionColumn(JEVisTree tree, JEVisDataSource dataSource, ColorColumn colorColumn, Integer chartId, List<TreeTableColumn<JEVisTreeRow, Boolean>> selectionColumns, TreeTableColumn<JEVisTreeRow, Long> allColumns) {
        this.tree = tree;
        this.dataSource = dataSource;
        this.colorColumn = colorColumn;
        this.chartId = chartId;
        this.selectionColumns = selectionColumns;
        this.allColumns = allColumns;
    }

    public Integer getChartId() {
        return chartId;
    }

    public void setChartId(Integer chartId) {
        this.chartId = chartId;
    }

    public TreeTableColumn<JEVisTreeRow, Boolean> getSelectionColumn() {
        return selectionColumn;
    }

    @Override
    public void setGraphDataModel(GraphDataModel graphDataModel) {
        this.data = graphDataModel;
        update();
    }

    @Override
    public void buildColumn() {

        AtomicReference<String> chartName = new AtomicReference<>();
        ChartSettings currentChartSetting = new ChartSettings("");

        for (ChartSettings set : getData().getCharts()) {
            if (set.getId().equals(getChartId())) {
                currentChartSetting = set;
                break;
            }
        }

        TreeTableColumn<JEVisTreeRow, Boolean> column = new TreeTableColumn<>("selection" + getChartId());
        column.setId(COLUMN_ID);
        column.setPrefWidth(120);
        column.setEditable(true);
        column.setResizable(false);

        column.setCellValueFactory(param -> {
            ChartDataModel data = getData(param.getValue().getValue());

            boolean selectedChart = false;
            for (Integer i : data.getSelectedcharts()) {
                if (i.equals(getChartId())) {
                    selectedChart = true;
                    break;
                }
            }

            return new ReadOnlyObjectWrapper<>(selectedChart);
        });

        VBox vbox = new VBox();

        ChartNameTextField textFieldChartName = new ChartNameTextField(currentChartSetting);
        textFieldChartName.setEditable(false);
        textFieldChartName.setDisable(true);

        ChartTypeComboBox comboBoxChartType = new ChartTypeComboBox(currentChartSetting);
        comboBoxChartType.setDisable(true);
        comboBoxChartType.setPrefWidth(114);

        vbox.getChildren().addAll(textFieldChartName, comboBoxChartType);

        column.setGraphic(vbox);

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Boolean>, TreeTableCell<JEVisTreeRow, Boolean>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Boolean> call(TreeTableColumn<JEVisTreeRow, Boolean> param) {

                return new TreeTableCell<JEVisTreeRow, Boolean>() {


                    @Override
                    public void commitEdit(Boolean newValue) {
                        super.commitEdit(newValue);
                        getTreeTableRow().getItem().setSelected(newValue);
                        ChartDataModel data1 = getData(getTreeTableRow().getItem());

                        if (newValue) {
                            if (!data1.getSelectedcharts().contains(getChartId())) {

                                data1.getSelectedcharts().add(getChartId());
                            }
                        } else {
                            Integer toBeRemoved = null;
                            for (Integer i : data1.getSelectedcharts()) {
                                if (i.equals(getChartId())) {
                                    toBeRemoved = i;
                                    break;
                                }
                            }
                            data1.getSelectedcharts().remove(toBeRemoved);
                        }

                    }

                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(null);
                        setGraphic(null);

                        if (!empty) {
                            try {
                                if (getTreeTableRow().getItem() != null
                                        && tree != null
                                        && tree.getFilter().showCell(column, getTreeTableRow().getItem())) {

                                    CheckBox checkBox = new CheckBox();
                                    checkBox.setSelected(item);
                                    StackPane stackPane = new StackPane();

                                    ChartDataModel currentDataModel = getData(getTreeTableRow().getItem());

                                    stackPane.getChildren().setAll(checkBox);
                                    StackPane.setAlignment(stackPane, Pos.CENTER_LEFT);

                                    checkBox.setOnAction(event -> {
                                        try {
                                            boolean isSelected = false;
                                            Color currentColor = null;
                                            for (ChartDataModel dataModel : data.getSelectedData()) {
                                                if (currentDataModel.getObject().equals(dataModel.getObject())
                                                        && (currentDataModel.getDataProcessor() != null
                                                        && currentDataModel.getDataProcessor().equals(dataModel.getDataProcessor()))) {
                                                    isSelected = true;
                                                    currentColor = dataModel.getColor();
                                                    break;
                                                }
                                            }

                                            commitEdit(checkBox.isSelected());

                                            if (checkBox.isSelected()) {
                                                /**
                                                 * if the checkbox is selected, get a color for it
                                                 */
                                                if (!isSelected) {
                                                    currentDataModel.setColor(colorColumn.getNextColor());
                                                } else if (currentColor != null) {
                                                    currentDataModel.setColor(currentColor);
                                                }
                                                Platform.runLater(() -> {
                                                    JEVisTreeRow sobj = new JEVisTreeRow(getTreeTableRow().getTreeItem().getValue().getJEVisObject());
                                                    getTreeTableRow().getTreeItem().setValue(sobj);
                                                });
                                            } else {
                                                /**
                                                 * check for other data rows within this chart
                                                 */
                                                boolean foundOther = false;
                                                for (ChartDataModel dataModel : data.getSelectedData()) {
                                                    if (currentDataModel.getObject().equals(dataModel.getObject()) && currentDataModel.getDataProcessor().equals(dataModel.getDataProcessor())) {
                                                        foundOther = true;
                                                        break;
                                                    }
                                                }

                                                if (!foundOther) {
                                                    /**
                                                     * if the box is unselected and no other selected, remove the color
                                                     */
                                                    colorColumn.removeUsedColor(currentDataModel.getColor());
                                                    currentDataModel.setColor(colorColumn.getStandardColor());
                                                    Platform.runLater(() -> {
                                                        JEVisTreeRow sobj = new JEVisTreeRow(getTreeTableRow().getTreeItem().getValue().getJEVisObject());
                                                        getTreeTableRow().getTreeItem().setValue(sobj);
                                                    });


//                                                    /**
//                                                     * remove the chart column from the tree and data model
//                                                     */
//
//                                                    if (getData().getCharts().size() > 1) {
//                                                        getTableColumn().setVisible(false);
//
//                                                        /**
//                                                         * remove the ChartSettings from the data model
//                                                         */
//                                                        ChartSettings toBeRemoved = null;
//                                                        Integer removeId = null;
//                                                        for (ChartSettings settings : getData().getCharts()) {
//                                                            if (settings.getId().equals(getChartId())) {
//                                                                toBeRemoved = settings;
//                                                                removeId = settings.getId();
//                                                                break;
//                                                            }
//                                                        }
//                                                        getData().getCharts().remove(toBeRemoved);
//
//                                                        /**
//                                                         *  for all remaining ChartSettings decrease the ids for chart indices greater than the removed chart
//                                                         */
//                                                        List<ChartSettings> charts = getData().getCharts();
//                                                        for (ChartSettings chartSettings : charts) {
//                                                            int oldId = chartSettings.getId();
//                                                            if (oldId > getChartId()) {
//                                                                chartSettings.setId(oldId - 1);
//                                                            }
//                                                        }
//
//                                                        /**
//                                                         * apply the changed chart ids to the data rows
//                                                         */
//                                                        for (ChartDataModel chartDataModel : getData().getSelectedData()) {
//                                                            List<Integer> selectedCharts = chartDataModel.getSelectedcharts();
//                                                            List<Integer> newList = new ArrayList<>();
//                                                            for (Integer integer : selectedCharts) {
//                                                                if (integer < getChartId()) {
//                                                                    newList.add(integer);
//                                                                } else {
//                                                                    newList.add(integer - 1);
//                                                                }
//                                                            }
//                                                            chartDataModel.setSelectedCharts(newList);
//                                                        }
//                                                        getTableColumn().getParentColumn().getColumns().remove(getTableColumn());
//
//                                                        List<TreeTableColumn<JEVisTreeRow, Boolean>> tobeRemovedColumn = new ArrayList<>();
//                                                        for (int i = 0; i < selectionColumns.size(); i++) {
//
//                                                            TreeTableColumn<JEVisTreeRow, Boolean> jeVisTreeRowLongTreeTableColumn = selectionColumns.get(i);
//                                                            if (i > getChartId()) {
//                                                                tobeRemovedColumn.add(jeVisTreeRowLongTreeTableColumn);
//                                                            }
//                                                        }
//                                                        selectionColumns.removeAll(tobeRemovedColumn);
//                                                        allColumns.getColumns().removeAll(tobeRemovedColumn);
//
//                                                        for (int i = removeId; i < getData().getCharts().size(); i++) {
//                                                            SelectionColumn selectColumn = new SelectionColumn(tree, dataSource, colorColumn, i, selectionColumns, allColumns);
//                                                            selectColumn.setGraphDataModel(getData());
//                                                            selectionColumns.add(selectColumn.getSelectionColumn());
//                                                            allColumns.getColumns().add(allColumns.getColumns().size() - ChartPluginTree.NO_OF_COLUMNS, selectColumn.getSelectionColumn());
//                                                        }
//
//                                                        tree.refresh();
//                                                    }
                                                }
                                            }
                                        } catch (Exception ex) {
                                            logger.error(ex);
                                        }
                                    });

                                    setFieldsEditable(textFieldChartName, comboBoxChartType, checkBox.isSelected());

//                                    checkBox.setDisable(!data.isSelectable());

                                    setText(null);
                                    setGraphic(stackPane);
                                }
                            } catch (Exception e) {
                                logger.error(e);
                            }
                        }
                    }

                    private void setFieldsEditable(TextField textFieldChartName1, DisabledItemsComboBox<String> comboBoxChartType1, Boolean item) {
                        if (item) {
                            textFieldChartName1.setEditable(true);
                            textFieldChartName1.setDisable(false);
                            comboBoxChartType1.setDisable(false);
                        } else {
                            AtomicReference<Boolean> foundSelected = new AtomicReference<>(false);
                            getData().getSelectedData().forEach(mdl -> {
                                for (Integer i : mdl.getSelectedcharts()) {
                                    if (i.equals(chartId)) {
                                        foundSelected.set(true);
                                    }
                                }
                            });
                            if (foundSelected.get()) {
                                textFieldChartName1.setEditable(true);
                                textFieldChartName1.setDisable(false);
                                comboBoxChartType1.setDisable(false);
                            } else {
                                textFieldChartName1.setEditable(false);
                                textFieldChartName1.setDisable(true);
                                comboBoxChartType1.setDisable(true);
                            }
                        }
                    }
                };
            }
        });

        this.selectionColumn = column;
    }

    @Override
    public GraphDataModel getData() {
        return this.data;
    }

    @Override
    public JEVisDataSource getDataSource() {
        return dataSource;
    }

}
