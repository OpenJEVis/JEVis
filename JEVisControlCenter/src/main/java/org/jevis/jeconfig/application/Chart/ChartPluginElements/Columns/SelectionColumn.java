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
import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;
import org.jevis.jeconfig.application.tools.ColorHelper;

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
    private AnalysisDataModel data;
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
    public void setGraphDataModel(AnalysisDataModel analysisDataModel) {
        this.data = analysisDataModel;
        update();
    }

    @Override
    public void buildColumn() {

        AtomicReference<String> chartName = new AtomicReference<>();
        ChartSetting currentChartSetting = new ChartSetting("");

        for (ChartSetting set : getData().getCharts().getListSettings()) {
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

                                    checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                                        try {
                                            boolean wasSelected = false;
                                            Color oldColor = null;
                                            for (ChartDataModel mdl : getData().getSelectedData()) {
                                                if (currentDataModel.equals(mdl) && !mdl.getSelectedcharts().isEmpty()) {
                                                    wasSelected = true;
                                                    oldColor = ColorHelper.toColor(mdl.getColor());
                                                    break;
                                                }
                                            }

                                            commitEdit(checkBox.isSelected());

                                            if (newValue) {
                                                /**
                                                 * if the checkbox is selected, get a color for it
                                                 */
                                                if (!wasSelected) {
                                                    currentDataModel.setColor(ColorHelper.toRGBCode(colorColumn.getNextColor()));
                                                } else if (oldColor != null) {
                                                    currentDataModel.setColor(ColorHelper.toRGBCode(oldColor));
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
                                                    if (!dataModel.getSelectedcharts().isEmpty() && currentDataModel.equals(dataModel)) {
                                                        foundOther = true;
                                                        break;
                                                    }
                                                }

                                                if (!foundOther) {
                                                    /**
                                                     * if the box is unselected and no other selected, remove the color
                                                     */
                                                    colorColumn.removeUsedColor(ColorHelper.toColor(currentDataModel.getColor()));
                                                    currentDataModel.setColor(ColorHelper.toRGBCode(colorColumn.getStandardColor()));
                                                    Platform.runLater(() -> {
                                                        JEVisTreeRow sobj = new JEVisTreeRow(getTreeTableRow().getTreeItem().getValue().getJEVisObject());
                                                        getTreeTableRow().getTreeItem().setValue(sobj);
                                                    });

                                                }
                                            }
                                        } catch (Exception ex) {
                                            logger.error(ex);
                                        }
                                    });

                                    setFieldsEditable(textFieldChartName, comboBoxChartType, checkBox.isSelected());

                                    setText(null);
                                    setGraphic(stackPane);
                                }
                            } catch (Exception e) {
                                logger.error(e);
                            }
                        }
                    }

                    private void setFieldsEditable(TextField textFieldChartName, ChartTypeComboBox comboBoxChartType, Boolean item) {
                        if (item) {
                            textFieldChartName.setEditable(true);
                            textFieldChartName.setDisable(false);
                            comboBoxChartType.setDisable(false);
                        } else {
                            boolean foundSelected = false;
                            for (ChartDataModel mdl : getData().getSelectedData()) {
                                for (Integer i : mdl.getSelectedcharts()) {
                                    if (i.equals(chartId)) {
                                        foundSelected = true;
                                        break;
                                    }
                                }
                            }
                            if (foundSelected) {
                                textFieldChartName.setEditable(true);
                                textFieldChartName.setDisable(false);
                                comboBoxChartType.setDisable(false);
                            } else {
                                textFieldChartName.setEditable(false);
                                textFieldChartName.setDisable(true);
                                comboBoxChartType.setDisable(true);
                            }
                        }
                    }
                };
            }
        });

        this.selectionColumn = column;
    }

    @Override
    public AnalysisDataModel getData() {
        return this.data;
    }

    @Override
    public JEVisDataSource getDataSource() {
        return dataSource;
    }

}
