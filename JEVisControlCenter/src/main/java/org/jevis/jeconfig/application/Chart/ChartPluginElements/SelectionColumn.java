package org.jevis.jeconfig.application.Chart.ChartPluginElements;

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
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.ChartSettings;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;
import org.jevis.jeconfig.application.tools.DisabledItemsComboBox;
import org.jevis.jeconfig.tool.I18n;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */

public class SelectionColumn extends TreeTableColumn<JEVisTreeRow, Boolean> implements ChartPluginColumn {
    public static String COLUMN_ID = "SelectionColumn";
    private TreeTableColumn<JEVisTreeRow, Boolean> selectionColumn;
    private GraphDataModel data;
    private JEVisTree tree;
    private ColorColumn colorColumn;
    private Integer chartId;

    public SelectionColumn(JEVisTree tree, ColorColumn colorColumn, Integer chartId) {
        this.tree = tree;
        this.colorColumn = colorColumn;
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
        ChartSettings currentChartSetting = null;

        for (ChartSettings set : getData().getCharts()) {
            if (set.getId() == chartId) {
                currentChartSetting = set;
                break;
            }
        }

        TreeTableColumn<JEVisTreeRow, Boolean> column = new TreeTableColumn("selection" + chartId);
        column.setId(COLUMN_ID);
        column.setPrefWidth(120);
        column.setEditable(true);
        column.setResizable(false);

        column.setCellValueFactory(param -> {
            ChartDataModel data = getData(param.getValue().getValue());

            boolean selectedChart = false;
            for (int i : data.getSelectedcharts()) {
                if (i == chartId) {
                    selectedChart = true;
                    break;
                }
            }

            return new ReadOnlyObjectWrapper<>(selectedChart);
        });

        VBox vbox = new VBox();

        TextField textFieldChartName = new TextField(chartName.get());
        textFieldChartName.setPrefWidth(120);
        textFieldChartName.setText(currentChartSetting.getName());
        textFieldChartName.setEditable(false);
        textFieldChartName.setDisable(true);

        DisabledItemsComboBox<String> comboBoxChartType = new DisabledItemsComboBox(ChartType.getlistNamesChartTypes());
        comboBoxChartType.setDisable(true);
        List<String> disabledItems = Arrays.asList(I18n.getInstance().getString("plugin.graph.charttype.scatter.name"),
                I18n.getInstance().getString("plugin.graph.charttype.bubble.name"));
        comboBoxChartType.setDisabledItems(disabledItems);

        comboBoxChartType.getSelectionModel().select(ChartType.parseChartIndex(currentChartSetting.getChartType()));

        /**
         * Adding a listener for the Chart name
         */

        textFieldChartName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {

                getData().getCharts().get(chartId).setName(newValue);

            }
        });

        /**
         * Adding a Listener for the Chart Type
         */

        comboBoxChartType.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {

                ChartType type = ChartType.parseChartType(comboBoxChartType.getSelectionModel().getSelectedIndex());
                getData().getCharts().get(chartId).setChartType(type);

            }
        });

        vbox.getChildren().addAll(textFieldChartName, comboBoxChartType);

        column.setGraphic(vbox);

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Boolean>, TreeTableCell<JEVisTreeRow, Boolean>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Boolean> call(TreeTableColumn<JEVisTreeRow, Boolean> param) {

                TreeTableCell<JEVisTreeRow, Boolean> cell = new TreeTableCell<JEVisTreeRow, Boolean>() {


                    @Override
                    public void commitEdit(Boolean newValue) {
                        super.commitEdit(newValue);
                        getTreeTableRow().getItem().getObjectSelectedProperty().setValue(newValue);
                        ChartDataModel data = getData(getTreeTableRow().getItem());

                        if (newValue) {
                            if (!data.getSelectedcharts().contains(chartId)) {

                                data.getSelectedcharts().add(chartId);
                            }
                        } else {
                            Integer toBeRemoved = null;
                            for (Integer i : data.getSelectedcharts()) {
                                if (i == chartId) {
                                    toBeRemoved = i;
                                    break;
                                }
                            }
                            data.getSelectedcharts().remove(toBeRemoved);
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

                                    CheckBox cbox = new CheckBox();
                                    cbox.setSelected(item);
                                    StackPane stackPane = new StackPane();

                                    ChartDataModel data = getData(getTreeTableRow().getItem());

                                    stackPane.getChildren().setAll(cbox);
                                    StackPane.setAlignment(stackPane, Pos.CENTER_LEFT);

                                    cbox.setOnAction(event -> {
                                        try {
                                            commitEdit(cbox.isSelected());

                                            if (cbox.isSelected()) {
                                                /**
                                                 * if the choicebox is selected, get a color for it
                                                 */
                                                for (Color c : colorColumn.getColorList()) {
                                                    if (!colorColumn.getUsedColors().contains(c)) {
                                                        data.setColor(c);
                                                        colorColumn.getUsedColors().add(c);
                                                        Platform.runLater(() -> {
                                                            JEVisTreeRow sobj = new JEVisTreeRow(getTreeTableRow().getTreeItem().getValue().getJEVisObject());
                                                            getTreeTableRow().getTreeItem().setValue(sobj);
                                                        });
                                                        break;
                                                    }
                                                }
                                            } else {
                                                /**
                                                 * check for other data rows within this chart
                                                 */
                                                AtomicReference<Boolean> foundOther = new AtomicReference<>(false);
                                                getData().getSelectedData().forEach(chartDataModel -> {
                                                    for (int i : chartDataModel.getSelectedcharts()) {
                                                        if (i == chartId) {
                                                            foundOther.set(true);
                                                            break;
                                                        }
                                                    }
                                                });

                                                if (!foundOther.get()) {
                                                    /**
                                                     * if the box is unselected and no other selected, remove the color
                                                     */
                                                    colorColumn.getUsedColors().remove(data.getColor());
                                                    data.setColor(Color.LIGHTBLUE);
                                                    Platform.runLater(() -> {
                                                        JEVisTreeRow sobj = new JEVisTreeRow(getTreeTableRow().getTreeItem().getValue().getJEVisObject());
                                                        getTreeTableRow().getTreeItem().setValue(sobj);
                                                    });

                                                    /**
                                                     * remove the chart column from the tree and data model
                                                     */

                                                    if (getData().getCharts().size() > 1) {
                                                        getTableColumn().setVisible(false);

                                                        ChartSettings toBeRemoved = null;
                                                        for (ChartSettings settings : getData().getCharts()) {
                                                            if (settings.getId() == chartId) {
                                                                toBeRemoved = settings;
                                                                break;
                                                            }
                                                        }
                                                        getData().getCharts().remove(toBeRemoved);

                                                        getTableColumn().getParentColumn().getColumns().remove(getTableColumn());
                                                    }
                                                }
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    });

                                    setFieldsEditable(textFieldChartName, comboBoxChartType, cbox.isSelected());

                                    cbox.setDisable(!data.isSelectable());

                                    setText(null);
                                    setGraphic(stackPane);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    private void setFieldsEditable(TextField textFieldChartName, DisabledItemsComboBox<String> comboBoxChartType, Boolean item) {
                        if (item) {
                            textFieldChartName.setEditable(true);
                            textFieldChartName.setDisable(false);
                            comboBoxChartType.setDisable(false);
                        } else {
                            AtomicReference<Boolean> foundSelected = new AtomicReference<>(false);
                            getData().getSelectedData().forEach(mdl -> {
                                for (int i : mdl.getSelectedcharts()) {
                                    if (i == chartId) {
                                        foundSelected.set(true);
                                    }
                                }
                            });
                            if (foundSelected.get()) {
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
                return cell;
            }
        });

        this.selectionColumn = column;
    }

    @Override
    public GraphDataModel getData() {
        return this.data;
    }

}
