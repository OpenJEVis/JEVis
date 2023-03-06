package org.jevis.jeconfig.plugin.action.ui;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventDispatcher;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import org.jevis.jeconfig.plugin.action.data.ActionPlanData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JFXCheckComboBox extends JFXTextField {

    private List<JFXCheckBox> boxes = new ArrayList<>();
    private SimpleStringProperty text = new SimpleStringProperty("");

    public JFXCheckComboBox(ObservableList<String> items, String selected) {
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
            JFXCheckBox cb = new JFXCheckBox(s);
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
        for (JFXCheckBox jfxCheckBox : boxes) {
            if (jfxCheckBox.isSelected()) tmptext.add(jfxCheckBox.getText());
        }

        text.set(ActionPlanData.listToString(tmptext).replaceAll(";", ", "));
        setText(text.getValue());
    }

    public ObservableList getValue() {
        ObservableList tmptext = FXCollections.observableArrayList();
        for (JFXCheckBox jfxCheckBox : boxes) {
            if (jfxCheckBox.isSelected()) tmptext.add(jfxCheckBox.getText());
        }
        return tmptext;
    }

}
