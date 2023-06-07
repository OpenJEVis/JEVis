package org.jevis.jecc.plugin.action.ui.control;

import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventDispatcher;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import org.jevis.jecc.plugin.action.data.ActionPlanData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MFXCheckComboBox extends MFXTextField {

    private List<MFXCheckbox> boxes = new ArrayList<>();
    private SimpleStringProperty text = new SimpleStringProperty("");

    public MFXCheckComboBox(ObservableList<String> items, String selected) {
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
            MFXCheckbox cb = new MFXCheckbox(s);
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
        for (MFXCheckbox mfxCheckbox : boxes) {
            if (mfxCheckbox.isSelected()) tmptext.add(mfxCheckbox.getText());
        }

        text.set(ActionPlanData.listToString(tmptext).replaceAll(";", ", "));
        setText(text.getValue());
    }

    public ObservableList getValue() {
        ObservableList tmptext = FXCollections.observableArrayList();
        for (MFXCheckbox mfxCheckbox : boxes) {
            if (mfxCheckbox.isSelected()) tmptext.add(mfxCheckbox.getText());
        }
        return tmptext;
    }

}
