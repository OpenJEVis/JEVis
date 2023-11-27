package org.jevis.jecc.application.control;


import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;

import java.util.List;

public class KPIVariable extends GridPane {
    private final TextField field = new TextField();
    private final ListView<JEVisObject> listView = new ListView<>();
    private final FilteredList<JEVisObject> filteredData;
    private final Button variableButton = new Button("Get Variable for Formula");
    private final CheckBox useForName = new CheckBox("Use for name");
    private final CheckBox useOneForAll = new CheckBox("Use one for all");
    private final int index;

    public KPIVariable(List<JEVisObject> objects, ObjectRelations objectRelations, int number) {
        super();
        this.index = number;
        setVgap(4);
        setHgap(4);

        field.setPromptText(I18n.getInstance().getString("searchbar.filterinput.prompttext"));

        filteredData = new FilteredList<>(FXCollections.observableArrayList(objects), s -> true);

        field.textProperty().addListener(obs -> {
            String filter = field.getText();
            if (filter == null || filter.length() == 0) {
                filteredData.setPredicate(s -> true);
            } else {
                if (filter.contains(" ")) {
                    String[] result = filter.split(" ");
                    filteredData.setPredicate(s -> {
                        boolean match = false;
                        String string = (objectRelations.getObjectPath(s) + s.getName()).toLowerCase();
                        for (String value : result) {
                            String subString = value.toLowerCase();
                            if (!string.contains(subString))
                                return false;
                            else match = true;
                        }
                        return match;
                    });
                } else {
                    filteredData.setPredicate(s -> (objectRelations.getObjectPath(s) + s.getName()).toLowerCase().contains(filter.toLowerCase()));
                }
            }
        });

        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> listViewCellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(JEVisObject obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (obj == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            String prefix = objectRelations.getObjectPath(obj);

                            setText(prefix + obj.getName());
                        }
                    }
                };
            }
        };
        listView.setCellFactory(listViewCellFactory);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        listView.setMinSize(400, 300);
        listView.setItems(filteredData);

        useForName.setSelected(index == 0);
        useOneForAll.setSelected(false);

        add(variableButton, 0, 0);
        add(useForName, 1, 0);
        add(useOneForAll, 2, 0);
        add(field, 0, 1, 3, 1);
        add(listView, 0, 2, 3, 1);
    }

    public List<JEVisObject> getSelectedItems() {
        return listView.getSelectionModel().getSelectedItems();
    }

    public int getIndex() {
        return index;
    }

    public Button getVariableButton() {
        return variableButton;
    }

    public CheckBox getUseOneForAll() {
        return useOneForAll;
    }

    public CheckBox getUseForName() {
        return useForName;
    }
}
