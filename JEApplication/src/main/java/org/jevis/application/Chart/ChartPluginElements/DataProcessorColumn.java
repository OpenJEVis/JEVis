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
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.data.GraphDataModel;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.application.jevistree.JEVisTree;
import org.jevis.application.jevistree.JEVisTreeRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class DataProcessorColumn extends TreeTableColumn<JEVisTreeRow, JEVisObject> implements ChartPluginColumn {
    public static String COLUMN_ID = "DataProcessorColumn";
    private SaveResourceBundle rb = new SaveResourceBundle("jeapplication", AppLocale.getInstance().getLocale());
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

    private ChoiceBox buildProcessorBox(ChartDataModel data) throws JEVisException {
        List<String> proNames = new ArrayList<>();
        final List<JEVisObject> _dataProcessors = new ArrayList<JEVisObject>();
        proNames.add(rb.getString("graph.processing.raw"));

        JEVisClass dpClass = data.getObject().getDataSource().getJEVisClass("Clean Data");
        _dataProcessors.addAll(data.getObject().getChildren(dpClass, true));
        for (JEVisObject configObject : _dataProcessors) {
            proNames.add(configObject.getName());
        }

        ChoiceBox processorBox = new ChoiceBox();
        processorBox.setPrefWidth(120);
        processorBox.setMinWidth(100);
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
        this.data.addObserver(this);
        update();
    }

    @Override
    public void buildColumn() {
        TreeTableColumn<JEVisTreeRow, JEVisObject> column = new TreeTableColumn(columnName);
        column.setPrefWidth(140);
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
                        if (!empty) {
                            StackPane hbox = new StackPane();

                            if (getTreeTableRow().getItem() != null && tree != null
                                    && tree.getFilter().showCell(column, getTreeTableRow().getItem())) {
                                ChartDataModel data = getData(getTreeTableRow().getItem());
                                ChoiceBox box = null;
                                try {
                                    box = buildProcessorBox(data);
                                } catch (JEVisException e) {
                                    e.printStackTrace();
                                }

                                hbox.getChildren().setAll(box);

                                StackPane.setAlignment(hbox, Pos.CENTER_LEFT);

                                box.setDisable(!data.isSelectable());
                            }

                            setText(null);
                            setGraphic(hbox);
                        } else {
                            setText(null);
                            setGraphic(null);
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
    public void update(Observable o, Object arg) {
        update();
    }
}
