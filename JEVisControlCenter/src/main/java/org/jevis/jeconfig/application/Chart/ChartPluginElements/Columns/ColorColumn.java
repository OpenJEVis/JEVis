package org.jevis.jeconfig.application.Chart.ChartPluginElements.Columns;

import com.sun.javafx.scene.control.skin.ColorPickerSkin;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataModel;
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
    public static final Color[] color_list = {
            Color.web("0xFFB300"),    // Vivid Yellow
            Color.web("0x803E75"),    // Strong Purple
            Color.web("0xFF6800"),    // Vivid Orange
            Color.web("0xA6BDD7"),    // Very Light Blue
            Color.web("0xC10020"),    // Vivid Red
            Color.web("0xCEA262"),    // Grayish Yellow
            Color.web("0x817066"),    // Medium Gray

            Color.web("0x007D34"),    // Vivid Green
            Color.web("0xF6768E"),    // Strong Purplish Pink
            Color.web("0x00538A"),    // Strong Blue
            Color.web("0xFF7A5C"),    // Strong Yellowish Pink
            Color.web("0x53377A"),    // Strong Violet
            Color.web("0xFF8E00"),    // Vivid Orange Yellow
            Color.web("0xB32851"),    // Strong Purplish Red
            Color.web("0xF4C800"),    // Vivid Greenish Yellow
            Color.web("0x7F180D"),    // Strong Reddish Brown
            Color.web("0x93AA00"),    // Vivid Yellowish Green
            Color.web("0x593315"),    // Deep Yellowish Brown
            Color.web("0xF13A13"),    // Vivid Reddish Orange
            Color.web("0x232C16"),    // Dark Olive Green
    };
    public static String COLUMN_ID = "ColorColumn";
    private final JEVisDataSource dataSource;
    private TreeTableColumn<JEVisTreeRow, Color> colorColumn;
    private AnalysisDataModel data;
    private List<Color> usedColors = new ArrayList<>();
    private JEVisTree tree;
    private String columnName;
    public static Color STANDARD_COLOR = Color.LIGHTBLUE;

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
        return color_list;
    }

    @Override
    public void setGraphDataModel(AnalysisDataModel analysisDataModel) {
        this.data = analysisDataModel;
        for (ChartDataModel model : data.getSelectedData()) {
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
            ChartDataModel data = getData(param.getValue().getValue());
            return new ReadOnlyObjectWrapper<>(ColorHelper.toColor(data.getColor()));
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Color>, TreeTableCell<JEVisTreeRow, Color>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Color> call(TreeTableColumn<JEVisTreeRow, Color> param) {

                return new TreeTableCell<JEVisTreeRow, Color>() {

                    @Override
                    public void commitEdit(Color newValue) {
                        super.commitEdit(newValue);
                        ChartDataModel data1 = getData(getTreeTableRow().getItem());
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
                                    StackPane stackPane = new StackPane();

                                    ChartDataModel currentDataModel = getData(getTreeTableRow().getItem());

                                    ColorPickerAdv colorPicker = new ColorPickerAdv();

                                    StackPane.setAlignment(stackPane, Pos.CENTER_LEFT);
                                    colorPicker.setValue(item);
                                    if (!usedColors.contains(item)) usedColors.add(item);
                                    colorPicker.setStyle("-fx-color-label-visible: false ;");

                                    colorPicker.setOnAction(event -> commitEdit(colorPicker.getValue()));

                                    colorPicker.setDisable(!currentDataModel.isSelectable());
                                    stackPane.getChildren().setAll(colorPicker);
                                    setGraphic(stackPane);

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

    public Color getNextColor() {
        for (Color color : getColorList()) {
            if (!getUsedColors().contains(color)) {
                getUsedColors().add(color);
                return color;
            }
        }
        return Color.LIGHTBLUE;
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
        return STANDARD_COLOR;
    }
}
