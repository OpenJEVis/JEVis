package org.jevis.jecc.application.Chart.ChartPluginElements.Boxes;


import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.application.Chart.data.ChartDataRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AxisBox extends ComboBox<Integer> {

    private static final String y1 = I18n.getInstance().getString("plugin.graph.chartplugin.axisbox.y1");
    private static final String y2 = I18n.getInstance().getString("plugin.graph.chartplugin.axisbox.y2");

    public AxisBox(final ChartDataRow data) {
        this(data.getAxis());

        this.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 0) {
                data.setAxis(0);
            } else if (newValue.intValue() == 1) {
                data.setAxis(1);
            }
        });
    }

    public AxisBox(Integer axis) {
        List<String> axisList = new ArrayList<>();

        axisList.add(y1);
        axisList.add(y2);

        setItems(FXCollections.observableArrayList(Arrays.asList(0, 1)));

        Callback<ListView<Integer>, ListCell<Integer>> cellFactory = new Callback<ListView<Integer>, ListCell<Integer>>() {
            @Override
            public ListCell<Integer> call(ListView<Integer> l) {
                return new ListCell<Integer>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            if (item == 0) {
                                setText(y1);
                            } else if (item == 1) {
                                setText(y2);
                            }
                        }
                    }
                };
            }
        };

        setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                if (object == 0) {
                    return (y1);
                } else if (object == 1) {
                    return (y2);
                }
                return null;
            }

            @Override
            public Integer fromString(String string) {
                if (string.equals(y1)) {
                    return 0;
                } else if (string.equals(y2)) {
                    return 1;
                } else return 0;
            }
        });


        this.setMinWidth(40);

        this.getSelectionModel().selectFirst();
        switch (axis) {
            case 0:
                this.getSelectionModel().selectFirst();
                break;
            case 1:
                this.getSelectionModel().select(1);
                break;
        }
    }

    private static String getItemText(Cell<Integer> cell) {
        switch (cell.getItem()) {
            case 1:
                return y2;
            case 0:
            default:
                return y1;
        }
    }

    public static AxisBox createComboBox(final Cell<Integer> cell) {
        Integer item = cell.getItem();
        final AxisBox comboBox = new AxisBox(item);

        // Use onAction here rather than onKeyReleased (with check for Enter),
        // as otherwise we encounter RT-34685
        comboBox.setOnAction(event -> {

            cell.commitEdit(comboBox.getSelectionModel().getSelectedIndex());
            event.consume();
        });
        comboBox.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cell.cancelEdit();
                t.consume();
            }
        });
        return comboBox;
    }

    public static void startEdit(final Cell<Integer> cell,
                                 final HBox hbox,
                                 final Node graphic,
                                 final AxisBox comboBox) {
        if (comboBox != null) {
            comboBox.getSelectionModel().select(cell.getItem());
        }
        cell.setText(null);

        if (graphic != null) {
            hbox.getChildren().setAll(graphic, comboBox);
            cell.setGraphic(hbox);
        } else {
            cell.setGraphic(comboBox);
        }

        // requesting focus so that key input can immediately go into the
        // TextField (see RT-28132)
        comboBox.requestFocus();
    }

    public static void cancelEdit(Cell<Integer> cell, Node graphic) {
        cell.setText(getItemText(cell));
        cell.setGraphic(graphic);
    }

    public static void updateItem(final Cell<Integer> cell,
                                  final HBox hbox,
                                  final Node graphic,
                                  final AxisBox comboBox) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else {
            if (cell.isEditing()) {
                if (comboBox != null) {
                    comboBox.getSelectionModel().select(cell.getItem());
                }
                cell.setText(null);

                if (graphic != null) {
                    hbox.getChildren().setAll(graphic, comboBox);
                    cell.setGraphic(hbox);
                } else {
                    cell.setGraphic(comboBox);
                }
            } else {
                cell.setText(getItemText(cell));
                cell.setGraphic(graphic);
            }
        }
    }
}
