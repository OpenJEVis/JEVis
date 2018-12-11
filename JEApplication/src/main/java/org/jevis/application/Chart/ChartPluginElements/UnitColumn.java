package org.jevis.application.Chart.ChartPluginElements;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisUnit;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.ChartUnits.*;
import org.jevis.application.Chart.data.GraphDataModel;
import org.jevis.application.jevistree.JEVisTree;
import org.jevis.application.jevistree.JEVisTreeRow;
import org.jevis.commons.unit.UnitManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */

public class UnitColumn extends TreeTableColumn<JEVisTreeRow, JEVisUnit> implements ChartPluginColumn {
    public static String COLUMN_ID = "UnitColumn";
    private TreeTableColumn<JEVisTreeRow, JEVisUnit> unitColumn;
    private GraphDataModel data;
    private JEVisTree tree;
    private String columnName;

    public UnitColumn(JEVisTree tree, String columnName) {
        this.tree = tree;
        this.columnName = columnName;
    }

    private ComboBox<String> buildUnitBox(ChartDataModel singleRow) {

        List<String> proNames = new ArrayList<>();

        Boolean isEnergyUnit = false;
        Boolean isVolumeUnit = false;
        Boolean isMassUnit = false;
        Boolean isPressureUnit = false;
        Boolean isVolumeFlowUnit = false;

        JEVisUnit currentUnit = null;
        try {
            if (singleRow.getDataProcessor() != null
                    && singleRow.getDataProcessor().getAttribute("Value") != null
                    && singleRow.getDataProcessor().getAttribute("Value").getDisplayUnit() != null)
                currentUnit = singleRow.getDataProcessor().getAttribute("Value").getDisplayUnit();
            else {
                if (singleRow.getObject() != null
                        && singleRow.getObject().getAttribute("Value") != null
                        && singleRow.getObject().getAttribute("Value").getDisplayUnit() != null)
                    currentUnit = singleRow.getObject().getAttribute("Value").getDisplayUnit();
            }
        } catch (
                JEVisException e) {
        }

        for (EnergyUnit eu : EnergyUnit.values()) {
            if (eu.toString().equals(UnitManager.getInstance().format(currentUnit))) {
                isEnergyUnit = true;
            } else if (UnitManager.getInstance().format(currentUnit).equals("") && currentUnit.getLabel().equals(eu.toString())) {
                isEnergyUnit = true;
            }
        }
        if (isEnergyUnit) for (EnergyUnit eu : EnergyUnit.values()) {
            proNames.add(eu.toString());
        }

        for (VolumeUnit vu : VolumeUnit.values()) {
            if (vu.toString().equals(UnitManager.getInstance().format(currentUnit))) {
                isVolumeUnit = true;
            } else if (UnitManager.getInstance().format(currentUnit).equals("") && currentUnit.getLabel().equals(vu.toString())) {
                isEnergyUnit = true;
            }
        }
        if (isVolumeUnit) for (VolumeUnit vu : VolumeUnit.values()) {
            proNames.add(vu.toString());
        }

        for (MassUnit mu : MassUnit.values()) {
            if (mu.toString().equals(UnitManager.getInstance().format(currentUnit))) {
                isMassUnit = true;
            } else if (UnitManager.getInstance().format(currentUnit).equals("") && currentUnit.getLabel().equals(mu.toString())) {
                isEnergyUnit = true;
            }
        }
        if (isMassUnit) for (MassUnit mu : MassUnit.values()) {
            proNames.add(mu.toString());
        }

        for (PressureUnit pu : PressureUnit.values()) {
            if (pu.toString().equals(UnitManager.getInstance().format(currentUnit))) {
                isPressureUnit = true;
            } else if (UnitManager.getInstance().format(currentUnit).equals("") && currentUnit.getLabel().equals(pu.toString())) {
                isEnergyUnit = true;
            }
        }
        if (isPressureUnit) for (PressureUnit pu : PressureUnit.values()) {
            proNames.add(pu.toString());
        }

        for (VolumeFlowUnit vfu : VolumeFlowUnit.values()) {
            if (vfu.toString().equals(UnitManager.getInstance().format(currentUnit))) {
                isVolumeFlowUnit = true;
            } else if (UnitManager.getInstance().format(currentUnit).equals("") && currentUnit.getLabel().equals(vfu.toString())) {
                isEnergyUnit = true;
            }
        }
        if (isVolumeFlowUnit) {
            for (VolumeFlowUnit vfu : VolumeFlowUnit.values()) {
                proNames.add(vfu.toString());
            }
        }

        if (!isEnergyUnit && !isMassUnit && !isPressureUnit && !isVolumeFlowUnit && !isVolumeUnit) {
            if (singleRow.getUnit() != null)
                proNames.add(singleRow.getUnit().getLabel());
        }

        ComboBox<String> processorBox;
        if (proNames.isEmpty()) {
            processorBox = new ComboBox<>();
        } else processorBox = new ComboBox<>(FXCollections.observableArrayList(proNames));

        processorBox.setPrefWidth(90);
        processorBox.setMinWidth(70);

        if (currentUnit != null) {
            processorBox.getSelectionModel().select(currentUnit.getLabel());
        }

        return processorBox;

    }

    public TreeTableColumn<JEVisTreeRow, JEVisUnit> getUnitColumn() {
        return unitColumn;
    }

    @Override
    public void setGraphDataModel(GraphDataModel graphDataModel) {
        this.data = graphDataModel;
        update();
    }

    @Override
    public void buildColumn() {
        TreeTableColumn<JEVisTreeRow, JEVisUnit> column = new TreeTableColumn(columnName);
        column.setPrefWidth(110);
        column.setEditable(true);
        column.setId(COLUMN_ID);

        column.setCellValueFactory(param -> {
            ChartDataModel data = getData(param.getValue().getValue());
            return new ReadOnlyObjectWrapper<>(data.getUnit());
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, JEVisUnit>, TreeTableCell<JEVisTreeRow, JEVisUnit>>() {


            @Override
            public TreeTableCell<JEVisTreeRow, JEVisUnit> call(TreeTableColumn<JEVisTreeRow, JEVisUnit> param) {


                TreeTableCell<JEVisTreeRow, JEVisUnit> cell = new TreeTableCell<JEVisTreeRow, JEVisUnit>() {
                    @Override
                    public void commitEdit(JEVisUnit unit) {
                        super.commitEdit(unit);
                    }

                    @Override
                    protected void updateItem(JEVisUnit item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(null);
                        setGraphic(null);

                        if (!empty) {
                            try {

                                if (getTreeTableRow().getItem() != null && tree != null
                                        && tree.getFilter().showCell(column, getTreeTableRow().getItem())) {

                                    StackPane stackPane = new StackPane();

                                    ChartDataModel data = getData(getTreeTableRow().getItem());
                                    ComboBox box = buildUnitBox(data);

                                    box.valueProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
                                        if (oldValue == null || newValue != oldValue) {
                                            JEVisUnit jeVisUnit = ChartUnits.parseUnit(String.valueOf(newValue));
                                            commitEdit(jeVisUnit);
                                            data.setUnit(jeVisUnit);
                                        }
                                    });

                                    ImageView imageMarkAll = new ImageView(imgMarkAll);
                                    imageMarkAll.fitHeightProperty().set(12);
                                    imageMarkAll.fitWidthProperty().set(12);

                                    Button tb = new Button("", imageMarkAll);
                                    tb.setTooltip(tooltipMarkAll);

                                    tb.setOnAction(event -> {
                                        JEVisUnit u = ChartUnits.parseUnit(box.getSelectionModel().getSelectedItem().toString());
                                        getData().getSelectedData().forEach(mdl -> {
                                            if (!mdl.getSelectedcharts().isEmpty()) {
                                                mdl.setUnit(u);
                                            }
                                        });

                                        tree.refresh();
                                    });

                                    HBox hbox = new HBox();
                                    hbox.getChildren().addAll(box, tb);
                                    stackPane.getChildren().add(hbox);
                                    StackPane.setAlignment(stackPane, Pos.CENTER_LEFT);

                                    box.setDisable(!data.isSelectable());
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


        this.unitColumn = column;
    }

    @Override
    public GraphDataModel getData() {
        return this.data;
    }

}
