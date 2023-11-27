package org.jevis.jecc.plugin.action.ui.control;


import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventDispatcher;
import javafx.geometry.Side;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.TextField;
import org.jevis.jecc.plugin.action.data.ActionPlanData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CheckComboBox extends TextField {

    private final List<CheckBox> boxes = new ArrayList<>();
    private final SimpleStringProperty text = new SimpleStringProperty("");

    public CheckComboBox(ObservableList<String> items, String selected) {
        //super(ActionPlanData.listToString(items).replaceAll(";", ", "));
        super(selected.replaceAll(";", ", "));
        setEditable(false);

        EventDispatcher ed = getEventDispatcher();
        this.setOnMouseClicked(event -> {
            getContextMenu().show(this, Side.BOTTOM, 0, 0);
            event.consume();
        });


        ContextMenu cm = new ContextMenu();
        cm.setOnHidden(ev -> {
        });

        List<String> selectedField = Arrays.asList(selected.split(";"));

        for (String s : items) {
            CheckBox cb = new CheckBox(s);
            cb.setSelected(selectedField.contains(s.trim()));

            boxes.add(cb);
            cb.setOnAction(event -> {
                updateValue();
                cm.hide();
            });
            CustomMenuItem cmi = new CustomMenuItem(cb);
            cm.getItems().add(cmi);
        }
        setContextMenu(cm);


    }

    private void updateValue() {
        ObservableList tmptext = FXCollections.observableArrayList();
        for (CheckBox mfxCheckbox : boxes) {
            if (mfxCheckbox.isSelected()) tmptext.add(mfxCheckbox.getText());
        }

        text.set(ActionPlanData.listToString(tmptext).replaceAll(";", ", "));
        setText(text.getValue());
    }

    public ObservableList getValue() {
        ObservableList tmptext = FXCollections.observableArrayList();
        for (CheckBox mfxCheckbox : boxes) {
            if (mfxCheckbox.isSelected()) tmptext.add(mfxCheckbox.getText());
        }
        return tmptext;
    }

}
