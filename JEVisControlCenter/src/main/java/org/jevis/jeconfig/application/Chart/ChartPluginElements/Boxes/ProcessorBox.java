package org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.tool.I18n;

import java.util.ArrayList;
import java.util.List;

public class ProcessorBox extends ComboBox<JEVisObject> {

    public ProcessorBox(JEVisObject object, JEVisObject selectedObject) {
        final List<JEVisObject> _dataProcessors = new ArrayList<JEVisObject>();
        String rawDataString = I18n.getInstance().getString("graph.processing.raw");

        if (object != null) {
            try {
                _dataProcessors.addAll(getAllChildrenOf(object));

                if (_dataProcessors.isEmpty()) {
                    for (JEVisObject parent : object.getParents()) {
                        _dataProcessors.addAll(getAllChildrenOf(parent));
                    }
                }
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        }
        AlphanumComparator ac = new AlphanumComparator();
        _dataProcessors.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));


        setPrefWidth(160);
        setMinWidth(120);
        ObservableList<JEVisObject> processors = FXCollections.observableArrayList();

        processors.add(object);
        processors.addAll(_dataProcessors);

        setItems(processors);

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
                            if (jeVisObject.equals(object)) text = rawDataString;
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
            getSelectionModel().selectFirst();
        }
    }

    private List<JEVisObject> getAllChildrenOf(JEVisObject parent) throws JEVisException {
        String cleanDataClassName = "Clean Data";
        String forecastDataClassName = "Forecast Data";

        return new ArrayList<>(getAllChildren(parent, cleanDataClassName, forecastDataClassName));
    }

    private List<JEVisObject> getAllChildren(JEVisObject parent, String cleanDataClassName, String forecastDataClassName) throws JEVisException {
        List<JEVisObject> list = new ArrayList<>();

        for (JEVisObject obj : parent.getChildren()) {
            if (obj.getJEVisClassName().equals(cleanDataClassName) || obj.getJEVisClassName().equals(forecastDataClassName)) {
                list.add(obj);
                list.addAll(getAllChildren(obj, cleanDataClassName, forecastDataClassName));
            }
        }

        return list;
    }
}
