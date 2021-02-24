package org.jevis.jeconfig.application.Chart.ChartPluginElements.Columns;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.ProcessorBox;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.dialog.ChartSelectionDialog;
import org.jevis.jeconfig.plugin.charts.ChartPlugin;

public class DataProcessorColumn extends TreeTableColumn<JEVisTreeRow, JEVisObject> implements ChartPluginColumn {
    public static String COLUMN_ID = "DataProcessorColumn";
    private TreeTableColumn<JEVisTreeRow, JEVisObject> dataProcessorColumn;
    private static final Logger logger = LogManager.getLogger(DataProcessorColumn.class);
    private AnalysisDataModel data;
    private final JEVisTree tree;
    private final String columnName;
    private final JEVisDataSource dataSource;

    /**
     * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
     */

    public DataProcessorColumn(JEVisTree tree, JEVisDataSource dataSource, String columnName) {
        this.tree = tree;
        this.dataSource = dataSource;
        this.columnName = columnName;
    }

    public TreeTableColumn<JEVisTreeRow, JEVisObject> getDataProcessorColumn() {
        return dataProcessorColumn;
    }

    @Override
    public void setGraphDataModel(AnalysisDataModel analysisDataModel) {
        this.data = analysisDataModel;
        update();
    }

    @Override
    public void buildColumn() {
        TreeTableColumn<JEVisTreeRow, JEVisObject> column = new TreeTableColumn();
        column.setPrefWidth(180);
        column.setEditable(true);
        column.setId(COLUMN_ID);

        column.setCellValueFactory(param -> {
            ChartDataRow data = getData(param.getValue().getValue());
            return new ReadOnlyObjectWrapper<>(data.getDataProcessor());
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, JEVisObject>, TreeTableCell<JEVisTreeRow, JEVisObject>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, JEVisObject> call(TreeTableColumn<JEVisTreeRow, JEVisObject> param) {

                TreeTableCell<JEVisTreeRow, JEVisObject> cell = new TreeTableCell<JEVisTreeRow, JEVisObject>() {

                    @Override
                    public void commitEdit(JEVisObject newValue) {
                        super.commitEdit(newValue);

                        ChartDataRow data = getData(getTreeTableRow().getItem());
                        if (newValue.equals(data.getObject())) {
                            data.setDataProcessor(null);
                        } else {
                            data.setDataProcessor(newValue);
                        }
                    }

                    @Override
                    protected void updateItem(JEVisObject item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(null);
                        setGraphic(null);

                        if (!empty) {
                            try {
                                if (getTreeTableRow().getItem() != null && tree != null
                                        && tree.getFilter().showCell(column, getTreeTableRow().getItem())) {
                                    StackPane stackPane = new StackPane();

                                    ChartDataRow data = getData(getTreeTableRow().getItem());
                                    ProcessorBox box = new ProcessorBox(data.getObject(), data.getDataProcessor());

                                    box.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> commitEdit(newValue));

                                    stackPane.getChildren().setAll(box);

                                    StackPane.setAlignment(stackPane, Pos.CENTER_LEFT);

                                    box.setDisable(!data.isSelectable());
                                    setGraphic(stackPane);
                                }
                            } catch (Exception e) {
                                logger.error("could not build column item: " + e);
                            }
                        }

                    }

                };

                return cell;
            }
        });

        Platform.runLater(() -> {
            Label label = new Label(columnName);
            label.setTooltip(new Tooltip(I18n.getInstance().getString("graph.table.cleaning.tip")));
            dataProcessorColumn.setGraphic(label);
            JEVisHelp.getInstance().addHelpControl(ChartPlugin.class.getSimpleName(), ChartSelectionDialog.class.getSimpleName(), JEVisHelp.LAYOUT.HORIZONTAL_TOP_CENTERED, label);

        });

        this.dataProcessorColumn = column;
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
