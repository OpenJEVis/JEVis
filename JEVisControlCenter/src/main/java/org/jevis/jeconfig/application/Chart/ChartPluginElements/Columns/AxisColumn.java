package org.jevis.jeconfig.application.Chart.ChartPluginElements.Columns;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.AxisBox;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.dialog.ChartSelectionDialog;
import org.jevis.jeconfig.plugin.charts.GraphPluginView;

public class AxisColumn extends TreeTableColumn<JEVisTreeRow, Integer> implements ChartPluginColumn {
    public static String COLUMN_ID = "AxisColumn";
    private TreeTableColumn<JEVisTreeRow, Integer> axisColumn;
    private AnalysisDataModel data;
    private JEVisTree tree;
    private String columnName;
    private final JEVisDataSource dataSource;

    public AxisColumn(JEVisTree tree, JEVisDataSource dataSource, String columnName) {
        this.tree = tree;
        this.dataSource = dataSource;
        this.columnName = columnName;
    }


    public TreeTableColumn<JEVisTreeRow, Integer> getAxisColumn() {
        return axisColumn;
    }

    @Override
    public void setGraphDataModel(AnalysisDataModel analysisDataModel) {
        this.data = analysisDataModel;

        update();
    }

    @Override
    public void buildColumn() {
        TreeTableColumn<JEVisTreeRow, Integer> column = new TreeTableColumn();
        column.setPrefWidth(80);
        column.setMinWidth(70);
        column.setId(COLUMN_ID);

        column.setCellValueFactory(param -> {

            ChartDataRow data = getData(param.getValue().getValue());

            return new ReadOnlyObjectWrapper<>(data.getAxis());
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Integer>, TreeTableCell<JEVisTreeRow, Integer>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Integer> call(TreeTableColumn<JEVisTreeRow, Integer> param) {

                TreeTableCell<JEVisTreeRow, Integer> cell = new TreeTableCell<JEVisTreeRow, Integer>() {

                    @Override
                    public void commitEdit(Integer newValue) {
                        super.commitEdit(newValue);
                    }

                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(null);
                        setGraphic(null);

                        if (!empty) {
                            try {
                                if (getTreeTableRow().getItem() != null && tree != null
                                        && tree.getFilter().showCell(column, getTreeTableRow().getItem())) {
                                    StackPane stackPane = new StackPane();

                                    ChartDataRow data = getData(getTreeTableRow().getItem());

                                    AxisBox axisBox = new AxisBox(data);

                                    ImageView imageMarkAll = new ImageView(imgMarkAll);
                                    imageMarkAll.fitHeightProperty().set(13);
                                    imageMarkAll.fitWidthProperty().set(13);

                                    Button tb = new Button("", imageMarkAll);

                                    tb.setTooltip(tooltipMarkAll);

                                    tb.setOnAction(event -> {
                                        Integer selection = axisBox.getChoiceBox().getSelectionModel().getSelectedIndex();
                                        getData().getSelectedData().forEach(mdl -> {
                                            if (!mdl.getSelectedcharts().isEmpty()) {
                                                mdl.setAxis(selection);
                                            }
                                        });

                                        tree.refresh();
                                    });

                                    HBox hbox = new HBox();
                                    hbox.getChildren().addAll(axisBox.getChoiceBox(), tb);
                                    stackPane.getChildren().add(hbox);
                                    StackPane.setAlignment(stackPane, Pos.CENTER_LEFT);

                                    axisBox.getChoiceBox().setDisable(!data.isSelectable());
                                    tb.setDisable(!data.isSelectable());
                                    setGraphic(stackPane);
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

        Platform.runLater(() -> {
            Label label = new Label(columnName);
            label.setTooltip(new Tooltip(I18n.getInstance().getString("graph.table.axis.tip")));
            axisColumn.setGraphic(label);
            JEVisHelp.getInstance().addHelpControl(GraphPluginView.class.getSimpleName(), ChartSelectionDialog.class.getSimpleName(), JEVisHelp.LAYOUT.HORIZONTAL_TOP_CENTERED, label);

        });

        this.axisColumn = column;
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

