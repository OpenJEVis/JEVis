package org.jevis.jeconfig.plugin.action.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import org.jevis.commons.i18n.I18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TagButton extends JFXButton {

    ObservableList<String> selectedTags = FXCollections.observableArrayList();
    HashMap<String, BooleanProperty> activeTags = new HashMap<>();
    ObservableList<String> allTags = FXCollections.observableArrayList();

    private ChangeListener<List<String>> limitListener;
    private List<JFXCheckBox> boxes = new ArrayList<>();

    public TagButton(String text, ObservableList<String> entrys, ObservableList<String> items) {
        super(text);
        this.allTags = entrys;
        this.selectedTags.addAll(items);

        this.allTags.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                updateList();
            }
        });
        updateList();
    }

    private void updateList() {
        MenuItem selectAllMenuItem = new MenuItem(I18n.getInstance().getString("plugin.notes.contextmenu.selectall"));
        selectAllMenuItem.setOnAction(event -> {
            boxes.forEach(jfxCheckBox -> jfxCheckBox.setSelected(true));
        });

        MenuItem deselectAllMenuItem = new MenuItem(I18n.getInstance().getString("plugin.notes.contextmenu.selectnone"));
        deselectAllMenuItem.setOnAction(event -> {
            boxes.forEach(jfxCheckBox -> jfxCheckBox.setSelected(false));
        });

        ContextMenu cm = new ContextMenu();
        cm.setOnHidden(ev -> {
        });
        cm.getItems().addAll(selectAllMenuItem, deselectAllMenuItem);

        allTags.forEach(s -> {
            JFXCheckBox cb = new JFXCheckBox(s);
            cb.setSelected(true);
            boxes.add(cb);
            cb.setOnAction(event -> {
                if (cb.isSelected()) {
                    selectedTags.add(s);
                } else {
                    selectedTags.remove(s);
                }
            });
            CustomMenuItem cmi = new CustomMenuItem(cb);
            cm.getItems().add(cmi);
        });


        setContextMenu(cm);
        setOnAction(event -> {
            cm.show(this, Side.BOTTOM, 0, 0);
        });
    }

    public ObservableList<String> getSelectedTags() {
        return selectedTags;
    }
}
