package org.jevis.jeconfig.application.Chart.ChartPluginElements.Columns;

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
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.ChartUnits.*;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */

public class UnitColumn extends TreeTableColumn<JEVisTreeRow, JEVisUnit> implements ChartPluginColumn {
    public static String COLUMN_ID = "UnitColumn";
    private TreeTableColumn<JEVisTreeRow, JEVisUnit> unitColumn;
    private AnalysisDataModel data;
    private JEVisTree tree;
    private String columnName;
    private final JEVisDataSource dataSource;

    public UnitColumn(JEVisTree tree, JEVisDataSource dataSource, String columnName) {
        this.tree = tree;
        this.dataSource = dataSource;
        this.columnName = columnName;
    }

    private ComboBox<String> buildUnitBox(ChartDataRow singleRow) {
        ComboBox<String> processorBox = new ComboBox<>();
        try {
            JEVisClass stringDataClass = null;

            stringDataClass = tree.getJEVisDataSource().getJEVisClass("String Data");

            if (!singleRow.getObject().getJEVisClass().equals(stringDataClass)) {

                List<String> proNames = new ArrayList<>();

                boolean isEnergyUnit = false;
                boolean isVolumeUnit = false;
                boolean isMassUnit = false;
                boolean isPressureUnit = false;
                boolean isVolumeFlowUnit = false;
                Boolean isMoneyUnit = false;

                JEVisUnit currentUnit = singleRow.getUnit();

                for (EnergyUnit eu : EnergyUnit.values()) {
                    if (eu.toString().equals(UnitManager.getInstance().format(currentUnit).replace("·", ""))) {
                        isEnergyUnit = true;
                    } else if (UnitManager.getInstance().format(currentUnit).equals("") && currentUnit.getLabel().equals(eu.toString())) {
                        isEnergyUnit = true;
                    }
                }
                if (isEnergyUnit) for (EnergyUnit eu : EnergyUnit.values()) {
                    proNames.add(eu.toString());
                }

                for (VolumeUnit vu : VolumeUnit.values()) {
                    if (vu.toString().equals(UnitManager.getInstance().format(currentUnit).replace("·", ""))) {
                        isVolumeUnit = true;
                    } else if (UnitManager.getInstance().format(currentUnit).equals("") && currentUnit.getLabel().equals(vu.toString())) {
                        isEnergyUnit = true;
                    }
                }
                if (isVolumeUnit) for (VolumeUnit vu : VolumeUnit.values()) {
                    proNames.add(vu.toString());
                }

                for (MassUnit mu : MassUnit.values()) {
                    if (mu.toString().equals(UnitManager.getInstance().format(currentUnit).replace("·", ""))) {
                        isMassUnit = true;
                    } else if (UnitManager.getInstance().format(currentUnit).equals("") && currentUnit.getLabel().equals(mu.toString())) {
                        isEnergyUnit = true;
                    }
                }
                if (isMassUnit) for (MassUnit mu : MassUnit.values()) {
                    proNames.add(mu.toString());
                }

                for (PressureUnit pu : PressureUnit.values()) {
                    if (pu.toString().equals(UnitManager.getInstance().format(currentUnit).replace("·", ""))) {
                        isPressureUnit = true;
                    } else if (UnitManager.getInstance().format(currentUnit).equals("") && currentUnit.getLabel().equals(pu.toString())) {
                        isEnergyUnit = true;
                    }
                }
                if (isPressureUnit) for (PressureUnit pu : PressureUnit.values()) {
                    proNames.add(pu.toString());
                }

                for (VolumeFlowUnit vfu : VolumeFlowUnit.values()) {
                    if (vfu.toString().equals(UnitManager.getInstance().format(currentUnit).replace("·", ""))) {
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

                for (MoneyUnit mu : MoneyUnit.values()) {
                    if (mu.toString().equals(UnitManager.getInstance().format(currentUnit).replace("·", ""))) {
                        isMoneyUnit = true;
                    } else if (UnitManager.getInstance().format(currentUnit).equals("") && currentUnit.getLabel().equals(mu.toString())) {
                        isMoneyUnit = true;
                    }
                }
                if (isMoneyUnit) for (MoneyUnit mu : MoneyUnit.values()) {
                    proNames.add(mu.toString());
                }


                processorBox.setItems(FXCollections.observableArrayList(proNames));

                processorBox.setPrefWidth(90);
                processorBox.setMinWidth(70);

                if (currentUnit != null) {
                    processorBox.getSelectionModel().select(currentUnit.getLabel());
                }

            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return processorBox;

    }

    public TreeTableColumn<JEVisTreeRow, JEVisUnit> getUnitColumn() {
        return unitColumn;
    }

    @Override
    public void setGraphDataModel(AnalysisDataModel analysisDataModel) {
        this.data = analysisDataModel;
        update();
    }

    @Override
    public void buildColumn() {
        TreeTableColumn<JEVisTreeRow, JEVisUnit> column = new TreeTableColumn(columnName);
        column.setPrefWidth(130);
        column.setEditable(true);
        column.setId(COLUMN_ID);

        column.setCellValueFactory(param -> {
            ChartDataRow data = getData(param.getValue().getValue());
            return new ReadOnlyObjectWrapper<>(data.getUnit());
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, JEVisUnit>, TreeTableCell<JEVisTreeRow, JEVisUnit>>() {


            @Override
            public TreeTableCell<JEVisTreeRow, JEVisUnit> call(TreeTableColumn<JEVisTreeRow, JEVisUnit> param) {


                TreeTableCell<JEVisTreeRow, JEVisUnit> cell = new TreeTableCell<JEVisTreeRow, JEVisUnit>() {
                    @Override
                    public void commitEdit(JEVisUnit unit) {

                        super.commitEdit(unit);
                        ChartDataRow data = getData(getTreeTableRow().getItem());
                        data.setUnit(unit);
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

                                    ChartDataRow data = getData(getTreeTableRow().getItem());
                                    ComboBox box = buildUnitBox(data);

                                    box.valueProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
                                        if (!newValue.equals(oldValue)) {
                                            JEVisUnit jeVisUnit = ChartUnits.parseUnit(newValue);
                                            commitEdit(jeVisUnit);
                                        }
                                    });

                                    ImageView imageMarkAll = new ImageView(imgMarkAll);
                                    imageMarkAll.fitHeightProperty().set(13);
                                    imageMarkAll.fitWidthProperty().set(13);

                                    Button tb = new Button("", imageMarkAll);
                                    tb.setTooltip(tooltipMarkAll);

                                    tb.setOnAction(event -> {
                                        JEVisUnit u = ChartUnits.parseUnit(box.getSelectionModel().getSelectedItem().toString());
                                        getData().getSelectedData().forEach(mdl -> {
                                            if (!mdl.getSelectedcharts().isEmpty()) {
                                                if (mdl.getUnit().getUnit().getStandardUnit() != null && u.getUnit().getStandardUnit() != null && mdl.getUnit().getUnit().getStandardUnit().equals(u.getUnit().getStandardUnit())) {
                                                    mdl.setUnit(u);
                                                } else {
                                                    String unitString = u.getLabel().replace("·", "");
                                                    String modelUnitString = mdl.getUnit().getLabel().replace("·", "");
                                                    List<EnergyUnit> eValues = Arrays.asList(EnergyUnit.values());
                                                    List<String> eVS = new ArrayList<>();
                                                    eValues.forEach(energyUnit -> eVS.add(energyUnit.toString()));
                                                    if (eVS.contains(modelUnitString) && eVS.contains(unitString)) {
                                                        mdl.setUnit(u);
                                                    }
                                                    List<MassUnit> mValues = Arrays.asList(MassUnit.values());
                                                    List<String> mVS = new ArrayList<>();
                                                    mValues.forEach(massUnit -> mVS.add(massUnit.toString()));
                                                    if (mVS.contains(modelUnitString) && mVS.contains(unitString)) {
                                                        mdl.setUnit(u);
                                                    }
                                                    List<VolumeUnit> vValues = Arrays.asList(VolumeUnit.values());
                                                    List<String> vVS = new ArrayList<>();
                                                    vValues.forEach(volumeUnit -> vVS.add(volumeUnit.toString()));
                                                    if (vVS.contains(modelUnitString) && vVS.contains(unitString)) {
                                                        mdl.setUnit(u);
                                                    }
                                                }
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
    public AnalysisDataModel getData() {
        return this.data;
    }

    @Override
    public JEVisDataSource getDataSource() {
        return dataSource;
    }

}
