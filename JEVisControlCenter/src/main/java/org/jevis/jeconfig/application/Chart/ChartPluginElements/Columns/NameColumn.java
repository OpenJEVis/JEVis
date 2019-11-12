package org.jevis.jeconfig.application.Chart.ChartPluginElements.Columns;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;

public class NameColumn extends TreeTableColumn<JEVisTreeRow, JEVisObject> implements ChartPluginColumn {
    private static final Logger logger = LogManager.getLogger(DataProcessorColumn.class);
    public static String COLUMN_ID = "NameColumn";
    private final JEVisDataSource dataSource;
    private TreeTableColumn<JEVisTreeRow, String> nameColumn;
    private AnalysisDataModel data;
    private JEVisTree tree;
    private String columnName;

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
        TreeTableColumn<JEVisTreeRow, String> column = new TreeTableColumn(columnName);
        column.setPrefWidth(180);
        column.setEditable(true);
        column.setId(COLUMN_ID);

        column.setCellValueFactory(param -> {
            ChartDataModel data = getData(param.getValue().getValue());
            return new ReadOnlyObjectWrapper<>(data.getTitle());
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, String>, TreeTableCell<JEVisTreeRow, String>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, String> call(TreeTableColumn<JEVisTreeRow, String> param) {

                TreeTableCell<JEVisTreeRow, String> cell = new TreeTableCell<JEVisTreeRow, String>() {

                    @Override
                    public void commitEdit(String newValue) {
                        super.commitEdit(newValue);

                        ChartDataModel data = getData(getTreeTableRow().getItem());
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

                                    ChartDataModel data = getData(getTreeTableRow().getItem());
                                    TextField nameField = new TextField();
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
