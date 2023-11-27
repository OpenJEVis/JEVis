package org.jevis.jecc.plugin.notes;


import javafx.beans.property.BooleanProperty;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;

import java.util.HashMap;
import java.util.List;

public class TagTableCellField {

    HashMap<String, BooleanProperty> activeTags;

    public TagTableCellField(HashMap<String, BooleanProperty> activeTags) {
        List<NoteTag> noteTagList = NoteTag.getAllTags();
        ContextMenu cm = new ContextMenu();
        cm.setOnHidden(ev -> {
            //System.out.println("Hide");
        });

        activeTags.forEach((tagKey, tagActive) -> {
            CheckBox cb = new CheckBox(tagKey);
            cb.selectedProperty().bindBidirectional(tagActive);
            cb.setOnAction(event -> {
                tagActive.set(cb.isSelected());
            });
            CustomMenuItem cmi = new CustomMenuItem(cb);
            cm.getItems().add(cmi);
        });

        Button tagButton = new Button("Tags");
        tagButton.setContextMenu(cm);
        tagButton.setOnAction(event -> {
            cm.show(tagButton, Side.BOTTOM, 0, 0);
        });
        Button button = new Button();
        button.setOnAction(event -> {
            cm.show(button, Side.BOTTOM, 0, 0);
        });

    }

    public HashMap<String, BooleanProperty> getActiveTags() {
        return activeTags;
    }
}
