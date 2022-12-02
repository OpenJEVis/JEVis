package org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes;

import com.jfoenix.controls.JFXComboBox;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.commons.utils.CommonMethods;

import java.util.ArrayList;
import java.util.List;

public class ProcessorBox extends JFXComboBox<JEVisObject> {
    final static String RAW_DATA_STRING = I18n.getInstance().getString("graph.processing.raw");

    public ProcessorBox(JEVisDataSource ds, Long id) {
        final List<JEVisObject> dataProcessors = new ArrayList<>();
        JEVisObject selectedObject = null;
        JEVisObject dataObject = null;
        try {
            selectedObject = ds.getObject(id);

            if (selectedObject != null && (selectedObject.getJEVisClassName().equals("Data") || selectedObject.getJEVisClassName().equals("Base Data"))) {
                dataProcessors.add(selectedObject);
                dataProcessors.addAll(getAllChildrenOf(selectedObject));
            } else if (selectedObject != null) {
                dataObject = CommonMethods.getFirstParentalDataObject(selectedObject);
                dataProcessors.add(dataObject);
                dataProcessors.addAll(getAllChildrenOf(dataObject));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        AlphanumComparator ac = new AlphanumComparator();
        dataProcessors.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));

        setPrefWidth(160);
        setMinWidth(120);

        getItems().setAll(dataProcessors);

        JEVisObject finalDataObject = dataObject;
        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> cellFactory = new Callback<javafx.scene.control.ListView<JEVisObject>, ListCell<JEVisObject>>() {
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
                            if (jeVisObject.equals(finalDataObject)) text = RAW_DATA_STRING;
                            else text = jeVisObject.getName();
                            setText(text);
                        }
                    }
                };
            }
        };
        setCellFactory(cellFactory);
        setButtonCell(cellFactory.call(null));

        if (selectedObject != null) getSelectionModel().select(selectedObject);
        else {
            JEVisObject firstCleanDataObject = null;
            for (JEVisObject processor : dataProcessors) {
                try {
                    if (processor.getJEVisClassName().equals("Clean Data")) {
                        firstCleanDataObject = processor;
                        break;
                    }
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }
            if (firstCleanDataObject != null) {
                getSelectionModel().select(firstCleanDataObject);
            } else {
                getSelectionModel().select(selectedObject);
            }
        }
    }

    private static String getItemText(JEVisDataSource ds, Cell<Long> cell) {
        String text = "";
        try {
            JEVisObject object = ds.getObject(cell.getItem());
            if (object.getJEVisClassName().equals("Data")) text = RAW_DATA_STRING;
            else text = object.getName();
        } catch (Exception ignored) {
        }
        return text;
    }

    public static ProcessorBox createComboBox(final JEVisDataSource ds, final Cell<Long> cell) {
        Long item = cell.getItem();
        final ProcessorBox comboBox = new ProcessorBox(ds, item);

        // Use onAction here rather than onKeyReleased (with check for Enter),
        // as otherwise we encounter RT-34685
        comboBox.setOnAction(event -> {

            cell.commitEdit(comboBox.getSelectionModel().getSelectedItem().getID());
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

    public static void startEdit(JEVisDataSource ds, final Cell<Long> cell,
                                 final HBox hbox,
                                 final Node graphic,
                                 final ProcessorBox comboBox) {
        if (comboBox != null) {
            try {
                comboBox.getSelectionModel().select(ds.getObject(cell.getItem()));
            } catch (Exception e) {

            }
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

    public static void cancelEdit(JEVisDataSource ds, Cell<Long> cell, Node graphic) {
        cell.setText(getItemText(ds, cell));
        cell.setGraphic(graphic);
    }

    public static void updateItem(JEVisDataSource ds, final Cell<Long> cell,
                                  final HBox hbox,
                                  final Node graphic,
                                  final ProcessorBox comboBox) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else {
            if (cell.isEditing()) {
                if (comboBox != null) {
                    try {
                        comboBox.getSelectionModel().select(ds.getObject(cell.getItem()));
                    } catch (Exception e) {

                    }
                }
                cell.setText(null);

                if (graphic != null) {
                    hbox.getChildren().setAll(graphic, comboBox);
                    cell.setGraphic(hbox);
                } else {
                    cell.setGraphic(comboBox);
                }
            } else {
                cell.setText(getItemText(ds, cell));
                cell.setGraphic(graphic);
            }
        }
    }

    private List<JEVisObject> getAllChildrenOf(JEVisObject parent) throws JEVisException {
        String cleanDataClassName = "Clean Data";
        String forecastDataClassName = "Forecast Data";
        String mathDataClassName = "Math Data";

        return new ArrayList<>(getAllChildren(parent, cleanDataClassName, forecastDataClassName, mathDataClassName));
    }

    private List<JEVisObject> getAllChildren(JEVisObject parent, String cleanDataClassName, String forecastDataClassName, String mathDataClassName) throws JEVisException {
        List<JEVisObject> list = new ArrayList<>();

        for (JEVisObject obj : parent.getChildren()) {
            if (obj.getJEVisClassName().equals(cleanDataClassName) || obj.getJEVisClassName().equals(forecastDataClassName)
                    || obj.getJEVisClassName().equals(mathDataClassName)) {
                list.add(obj);
                list.addAll(getAllChildren(obj, cleanDataClassName, forecastDataClassName, mathDataClassName));
            }
        }

        return list;
    }
}
