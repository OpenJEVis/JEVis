package org.jevis.application.Chart.ChartPluginElements;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.data.GraphDataModel;
import org.jevis.application.jevistree.JEVisTree;
import org.jevis.application.jevistree.JEVisTreeRow;

import java.util.ArrayList;
import java.util.List;

public class DataProcessorColumn extends TreeTableColumn<JEVisTreeRow, JEVisObject> implements ChartPluginColumn {
    public static String COLUMN_ID = "DataProcessorColumn";
    private TreeTableColumn<JEVisTreeRow, JEVisObject> dataProcessorColumn;
    private GraphDataModel data;
    private JEVisTree tree;
    private String columnName;

    /**
     * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
     */

    public DataProcessorColumn(JEVisTree tree, String columnName) {
        this.tree = tree;
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

    private ChoiceBox buildProcessorBox(ChartDataModel data) throws JEVisException {
        List<String> proNames = new ArrayList<>();
        final List<JEVisObject> _dataProcessors = new ArrayList<JEVisObject>();
        proNames.add(rb.getString("graph.processing.raw"));

        if (data.getObject() != null)
            _dataProcessors.addAll(getAllChildrenOf(data.getObject()));
        for (JEVisObject configObject : _dataProcessors) {
            proNames.add(configObject.getName());
        }

        ChoiceBox processorBox = new ChoiceBox();
        processorBox.setPrefWidth(140);
        processorBox.setMinWidth(120);
        processorBox.setItems(FXCollections.observableArrayList(proNames));

        processorBox.valueProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {

            if (newValue.equals(rb.getString("graph.processing.raw"))) {
                data.setDataProcessor(null);
            } else {
                for (JEVisObject configObject : _dataProcessors) {
                    if (configObject.getName().equals(newValue)) {
                        data.setDataProcessor(configObject);
                    }

                }
            }
        });

        if (data.getDataProcessor() != null) processorBox.getSelectionModel().select(1);
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
                                    ChoiceBox box = null;
                                    try {
                                        box = buildProcessorBox(data);
                                    } catch (JEVisException e) {
                                        e.printStackTrace();
                                    }

                                    stackPane.getChildren().setAll(box);

                                    StackPane.setAlignment(stackPane, Pos.CENTER_LEFT);

                                    box.setDisable(!data.isSelectable());
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

        this.dataProcessorColumn = column;
    }

    @Override
    public GraphDataModel getData() {
        return this.data;
    }

}
