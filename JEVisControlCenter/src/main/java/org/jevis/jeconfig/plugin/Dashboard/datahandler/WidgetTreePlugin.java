package org.jevis.jeconfig.plugin.Dashboard.datahandler;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Columns.ColorColumn;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;
import org.jevis.jeconfig.application.jevistree.TreePlugin;
import org.jevis.jeconfig.plugin.Dashboard.config.DataPointNode;

import java.util.ArrayList;
import java.util.List;

public class WidgetTreePlugin implements TreePlugin {

    public static String COLUMN = "DataModel";
    public static String COLUMN_COLOR = "Color";
    public static String COLUMN_SELECTED = "Selection";
    private JEVisTree jeVisTree;
    private List<DataPointNode> data = new ArrayList<>();


    public WidgetTreePlugin() {
//        this.data = preset;
    }

    @Override
    public void setTree(JEVisTree tree) {
        this.jeVisTree = tree;

    }

    @Override
    public List<TreeTableColumn<JEVisTreeRow, Long>> getColumns() {
        List<TreeTableColumn<JEVisTreeRow, Long>> list = new ArrayList<>();

        TreeTableColumn<JEVisTreeRow, Long> pluginHeader = new TreeTableColumn<>("Main");
        pluginHeader.setId(COLUMN);

        pluginHeader.getColumns().addAll(buildSelection(), buildColorColumn());
        list.add(pluginHeader);

//        list.addAll(buildSelection(), buildColorColumn());
        return list;
    }

    @Override
    public void selectionFinished() {

    }

    @Override
    public String getTitle() {
        return "Title";
    }

    public TreeTableColumn<JEVisTreeRow, Color> buildColorColumn() {
        TreeTableColumn<JEVisTreeRow, Color> column = new TreeTableColumn(COLUMN_COLOR);
        column.setPrefWidth(80);
        column.setId(COLUMN_COLOR);

        column.setCellValueFactory(param -> {
            Color color = (Color) param.getValue().getValue().getDataObject(COLUMN_COLOR, Color.LIGHTBLUE);
            return new ReadOnlyObjectWrapper<>(color);
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Color>, TreeTableCell<JEVisTreeRow, Color>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Color> call(TreeTableColumn<JEVisTreeRow, Color> param) {

                TreeTableCell<JEVisTreeRow, Color> cell = new TreeTableCell<JEVisTreeRow, Color>() {

                    @Override
                    protected void updateItem(Color item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(null);
                        setGraphic(null);

                        if (!empty) {

                            boolean show = jeVisTree.getFilter().showCell(column, getTreeTableRow().getItem());

                            if (show) {
                                ColorPicker colorPicker = new ColorPicker();
                                colorPicker.setStyle("-fx-color-label-visible: false ;");
                                colorPicker.setValue(item);
                                colorPicker.getCustomColors().addAll(ColorColumn.color_list);

                                colorPicker.setOnAction(event -> {
                                    try {
                                        if (getTreeTableRow() != null && getTreeTableRow().getItem() != null) {
                                            getTreeTableRow().getItem().setDataObject(COLUMN_COLOR, colorPicker.getValue());
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                });
                                setGraphic(new BorderPane(colorPicker));
                            }
                        }
                    }

                };

                return cell;
            }
        });

        return column;
    }

    public TreeTableColumn<JEVisTreeRow, Boolean> buildSelection() {
        TreeTableColumn<JEVisTreeRow, Boolean> column = new TreeTableColumn(COLUMN_SELECTED);
        column.setPrefWidth(80);
        column.setId(COLUMN_SELECTED);

        column.setCellValueFactory(param -> {
            Boolean selected = (Boolean) param.getValue().getValue().getDataObject(COLUMN_SELECTED, false);
            return new ReadOnlyObjectWrapper<>(selected);
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, Boolean>, TreeTableCell<JEVisTreeRow, Boolean>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, Boolean> call(TreeTableColumn<JEVisTreeRow, Boolean> param) {
                return new TreeTableCell<JEVisTreeRow, Boolean>() {

                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(null);
                        setGraphic(null);

                        if (!empty) {
                            boolean show = jeVisTree.getFilter().showCell(column, getTreeTableRow().getItem());

                            if (show) {
                                CheckBox box = new CheckBox();
                                box.setSelected(item);
                                box.setOnAction(event -> {
                                    try {
                                        if (getTreeTableRow() != null && getTreeTableRow().getItem() != null) {
                                            getTreeTableRow().getItem().setDataObject(COLUMN_SELECTED, box.isSelected());
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                });

                                setGraphic(new BorderPane(box));
                            }

                        }

                    }
                };
            }
        });

        return column;
    }


}
