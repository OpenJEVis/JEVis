package org.jevis.jeconfig.plugin.action.ui;

import com.jfoenix.controls.JFXCheckBox;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TagButton extends Button {

    ObservableList<String> selectedTags = FXCollections.observableArrayList();
    HashMap<String, BooleanProperty> activeTags = new HashMap<>();
    ObservableList<String> allTags = FXCollections.observableArrayList();

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
            updateValue();
        });

        MenuItem deselectAllMenuItem = new MenuItem(I18n.getInstance().getString("plugin.notes.contextmenu.selectnone"));
        deselectAllMenuItem.setOnAction(event -> {
            boxes.forEach(jfxCheckBox -> jfxCheckBox.setSelected(false));
            updateValue();
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
                System.out.println("Event: " + event);
                /*
                if (cb.isSelected()) {
                    selectedTags.add(s);
                } else {
                    selectedTags.remove(s);
                }
                */
                updateValue();
            });
            CustomMenuItem cmi = new CustomMenuItem(cb);
            cm.getItems().add(cmi);
        });


        setContextMenu(cm);
        setOnAction(event -> {
            cm.show(this, Side.BOTTOM, 0, 0);
        });
        updateButton();
    }

    private void updateValue() {

        List<String> selected = new ArrayList();
        boxes.forEach(jfxCheckBox -> {
            System.out.println("jfxCheckBox: " + jfxCheckBox.isSelected() + "  " + jfxCheckBox.getText());
            if (jfxCheckBox.isSelected()) selected.add(jfxCheckBox.getText());

        });
        System.out.println("selected: " + selected);
        if (selected.isEmpty()) {
            selectedTags.clear();
        } else {
            selectedTags.setAll(selected);
        }
        updateButton();

    }

    private void updateButton() {
        Platform.runLater(() -> {
            if (selectedTags.size() == allTags.size()) {
                System.out.println("All selected");
                setStyle("-fx-border-color: #51aaa5;");
                setGraphic(JEConfig.getSVGImage(Icon.FILTER_ALT_OFF, 20, 20));
            } else {
                System.out.println("User select");
                setStyle("-fx-border-color: green;");
                setGraphic(JEConfig.getSVGImage(Icon.FILTER_ALT, 20, 20));
            }
        });

    }

    public ObservableList<String> getSelectedTags() {
        return selectedTags;
    }
}
