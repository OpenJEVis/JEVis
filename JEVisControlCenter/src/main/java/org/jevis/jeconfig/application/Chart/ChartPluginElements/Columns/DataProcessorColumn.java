package org.jevis.jeconfig.application.Chart.ChartPluginElements.Columns;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;
import org.jevis.jeconfig.tool.I18n;

import java.util.ArrayList;
import java.util.List;

public class DataProcessorColumn extends TreeTableColumn<JEVisTreeRow, JEVisObject> implements ChartPluginColumn {
    public static String COLUMN_ID = "DataProcessorColumn";
    private TreeTableColumn<JEVisTreeRow, JEVisObject> dataProcessorColumn;
    private static final Logger logger = LogManager.getLogger(DataProcessorColumn.class);
    private GraphDataModel data;
    private JEVisTree tree;
    private String columnName;
    private final JEVisDataSource dataSource;

    /**
     * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
     */

    public DataProcessorColumn(JEVisTree tree, JEVisDataSource dataSource, String columnName) {
        this.tree = tree;
        this.dataSource = dataSource;
        this.columnName = columnName;
    }

    List<JEVisObject> getAllChildrenOf(JEVisObject parent) throws JEVisException {
        List<JEVisObject> list = new ArrayList<>();
        String cleanDataClassName = "Clean Data";

        list = getAllChildren(parent, cleanDataClassName);

        return list;
    }

    private List<JEVisObject> getAllChildren(JEVisObject parent, String cleanDataClassName) throws JEVisException {
        List<JEVisObject> list = new ArrayList<>();

        for (JEVisObject obj : parent.getChildren()) {
            if (obj.getJEVisClassName().equals(cleanDataClassName)) {
                list.add(obj);
                list.addAll(getAllChildren(obj, cleanDataClassName));
            }
        }

        return list;
    }

    private ComboBox<JEVisObject> buildProcessorBox(ChartDataModel data) throws JEVisException {

        final List<JEVisObject> _dataProcessors = new ArrayList<JEVisObject>();
        String rawDataString = I18n.getInstance().getString("graph.processing.raw");

        if (data.getObject() != null)
            _dataProcessors.addAll(getAllChildrenOf(data.getObject()));

        ComboBox<JEVisObject> processorBox = new ComboBox<>();
        processorBox.setPrefWidth(140);
        processorBox.setMinWidth(120);
        ObservableList<JEVisObject> processors = FXCollections.observableArrayList();

        processors.add(data.getObject());
        processors.addAll(_dataProcessors);

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
                            if (jeVisObject.equals(data.getObject())) text = rawDataString;
                            else text = jeVisObject.getName();
                            setText(text);
                        }
                    }
                };
            }
        };
        processorBox.setCellFactory(cellFactory);
        processorBox.setButtonCell(cellFactory.call(null));

        if (data.getDataProcessor() != null) processorBox.getSelectionModel().select(data.getDataProcessor());
        else processorBox.getSelectionModel().selectFirst();

        return processorBox;
    }

    public TreeTableColumn<JEVisTreeRow, JEVisObject> getDataProcessorColumn() {
        return dataProcessorColumn;
    }

    @Override
    public void setGraphDataModel(GraphDataModel graphDataModel) {
        this.data = graphDataModel;
        update();
    }

    @Override
    public void buildColumn() {
        TreeTableColumn<JEVisTreeRow, JEVisObject> column = new TreeTableColumn(columnName);
        column.setPrefWidth(160);
        column.setEditable(true);
        column.setId(COLUMN_ID);

        column.setCellValueFactory(param -> {
            ChartDataModel data = getData(param.getValue().getValue());
            return new ReadOnlyObjectWrapper<>(data.getDataProcessor());
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, JEVisObject>, TreeTableCell<JEVisTreeRow, JEVisObject>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, JEVisObject> call(TreeTableColumn<JEVisTreeRow, JEVisObject> param) {

                TreeTableCell<JEVisTreeRow, JEVisObject> cell = new TreeTableCell<JEVisTreeRow, JEVisObject>() {

                    @Override
                    public void commitEdit(JEVisObject newValue) {
                        super.commitEdit(newValue);

                        ChartDataModel data = getData(getTreeTableRow().getItem());
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

                                    ChartDataModel data = getData(getTreeTableRow().getItem());
                                    ComboBox<JEVisObject> box = null;
                                    try {
                                        box = buildProcessorBox(data);

                                        box.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> commitEdit(newValue));
                                    } catch (JEVisException e) {
                                        logger.error("Could not build processor box: " + e);
                                    }

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

        this.dataProcessorColumn = column;
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
