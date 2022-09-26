package org.jevis.jeconfig.application.Chart.ChartPluginElements.Columns;

import com.sun.javafx.scene.control.skin.ColorPickerSkin;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.application.Chart.ChartElements.ColorTable;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.control.ColorPickerAdv;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;
import org.jevis.jeconfig.application.tools.ColorHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */

public class ColorColumn extends TreeTableColumn<JEVisTreeRow, Color> implements ChartPluginColumn {
    public static String COLUMN_ID = "ColorColumn";
    private final JEVisDataSource dataSource;
    private TreeTableColumn<JEVisTreeRow, Color> colorColumn;
    private AnalysisDataModel data;
    private final List<Color> usedColors = new ArrayList<>();
    private final JEVisTree tree;
    private final String columnName;

    public ColorColumn(JEVisTree tree, JEVisDataSource dataSource, String columnName) {
        this.tree = tree;
        this.dataSource = dataSource;
        this.columnName = columnName;
    }


    public TreeTableColumn<JEVisTreeRow, Color> getColorColumn() {
        return colorColumn;
    }

    public List<Color> getUsedColors() {
        return usedColors;
    }

    public Color[] getColorList() {
        return ColorTable.color_list;
    }

    @Override
    public void setGraphDataModel(AnalysisDataModel analysisDataModel) {
        this.data = analysisDataModel;
        for (ChartDataRow model : data.getSelectedData()) {
            if (!this.usedColors.contains(model.getColor())) this.usedColors.add(ColorHelper.toColor(model.getColor()));
        }
        update();
    }

    @Override
    public void buildColumn() {
        TreeTableColumn<JEVisTreeRow, Color> column = new TreeTableColumn(columnName);
        column.setPrefWidth(80);
        column.setId(COLUMN_ID);
        column.setCellValueFactory(param -> {
            ChartDataRow data = getData(param.getValue().getValue());
            return new ReadOnlyObjectWrapper<>(ColorHelper.toColor(data.getColor()));
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Color>, TreeTableCell<JEVisTreeRow, Color>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Color> call(TreeTableColumn<JEVisTreeRow, Color> param) {

                return new TreeTableCell<JEVisTreeRow, Color>() {

                    @Override
                    public void commitEdit(Color newValue) {
                        super.commitEdit(newValue);
                        ChartDataRow data1 = getData(getTreeTableRow().getItem());
                        data1.setColor(ColorHelper.toRGBCode(newValue));
                        if (!usedColors.contains(newValue)) usedColors.add(newValue);
                    }

                    @Override
                    protected void updateItem(Color item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(null);
                        setGraphic(null);

                        if (!empty) {
                            try {
                                if (getTreeTableRow().getItem() != null
                                        && tree != null
                                        && tree.getFilter().showCell(column, getTreeTableRow().getItem())) {
                                    HBox hBox = new HBox();

                                    ChartDataRow currentDataModel = getData(getTreeTableRow().getItem());

                                    ColorPickerAdv colorPicker = new ColorPickerAdv();

                                    hBox.setAlignment(Pos.CENTER);
                                    colorPicker.setValue(item);
                                    if (!usedColors.contains(item)) usedColors.add(item);
                                    colorPicker.setStyle("-fx-color-label-visible: false ;");

                                    colorPicker.setOnAction(event -> commitEdit(colorPicker.getValue()));

                                    colorPicker.setDisable(!currentDataModel.isSelectable());
                                    hBox.getChildren().setAll(colorPicker);
                                    setGraphic(hBox);

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                };
            }
        });

        this.colorColumn = column;
    }

    public class CustomColorPickerSkin extends ColorPickerSkin {

        public CustomColorPickerSkin(ColorPicker colorPicker) {
            super(colorPicker);
        }

        @Override
        public Node getPopupContent() {
            return super.getPopupContent();
        }
    }

    public void removeUsedColor(Color color) {
        getUsedColors().remove(color);
    }

    @Override
    public AnalysisDataModel getData() {
        return this.data;
    }

    @Override
    public JEVisDataSource getDataSource() {
        return dataSource;
    }

    public Color getStandardColor() {
        return ColorTable.STANDARD_COLOR;
    }
}
