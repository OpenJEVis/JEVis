package org.jevis.jeconfig.application.Chart.ChartPluginElements.Columns;

import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.dialog.ChartSelectionDialog;
import org.jevis.jeconfig.plugin.charts.ChartPlugin;

public class NameColumn extends TreeTableColumn<JEVisTreeRow, JEVisObject> implements ChartPluginColumn {
    private static final Logger logger = LogManager.getLogger(DataProcessorColumn.class);
    public static String COLUMN_ID = "NameColumn";
    private final JEVisDataSource dataSource;
    private TreeTableColumn<JEVisTreeRow, String> nameColumn;
    private AnalysisDataModel data;
    private final JEVisTree tree;
    private final String columnName;

    /**
     * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
     */

    public NameColumn(JEVisTree tree, JEVisDataSource dataSource, String columnName) {
        this.tree = tree;
        this.dataSource = dataSource;
        this.columnName = columnName;
    }

    public TreeTableColumn<JEVisTreeRow, String> getNameColumn() {
        return nameColumn;
    }

    @Override
    public void setGraphDataModel(AnalysisDataModel analysisDataModel) {
        this.data = analysisDataModel;
        update();
    }

    @Override
    public void buildColumn() {
        TreeTableColumn<JEVisTreeRow, String> column = new TreeTableColumn();
        column.setPrefWidth(180);
        column.setEditable(true);
        column.setId(COLUMN_ID);

        column.setCellValueFactory(param -> {
            ChartDataRow data = getData(param.getValue().getValue());
            return new ReadOnlyObjectWrapper<>(data.getTitle());
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, String>, TreeTableCell<JEVisTreeRow, String>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, String> call(TreeTableColumn<JEVisTreeRow, String> param) {

                TreeTableCell<JEVisTreeRow, String> cell = new TreeTableCell<JEVisTreeRow, String>() {

                    @Override
                    public void commitEdit(String newValue) {
                        super.commitEdit(newValue);

                        ChartDataRow data = getData(getTreeTableRow().getItem());
                        data.setTitle(newValue);

                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(null);
                        setGraphic(null);

                        if (!empty) {
                            try {
                                if (getTreeTableRow().getItem() != null && tree != null
                                        && tree.getFilter().showCell(column, getTreeTableRow().getItem())) {
                                    StackPane stackPane = new StackPane();

                                    ChartDataRow data = getData(getTreeTableRow().getItem());
                                    JFXTextField nameField = new JFXTextField();
                                    if (data.getTitle() != null) {
                                        nameField.setText(data.getTitle());
                                    }
                                    nameField.textProperty().addListener((observable, oldValue, newValue) -> commitEdit(newValue));

                                    stackPane.getChildren().setAll(nameField);

                                    StackPane.setAlignment(stackPane, Pos.CENTER_LEFT);

                                    nameField.setDisable(!data.isSelectable());
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
            //label.setTooltip(new  JFXTooltip(I18n.getInstance().getString("graph.table.name.tip")));
            nameColumn.setGraphic(label);
            JEVisHelp.getInstance().addHelpControl(ChartPlugin.class.getSimpleName(), ChartSelectionDialog.class.getSimpleName(), JEVisHelp.LAYOUT.HORIZONTAL_TOP_CENTERED, label);

        });

        this.nameColumn = column;
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
