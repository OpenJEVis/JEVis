package org.jevis.jeconfig.plugin.action.ui;

import com.jfoenix.controls.JFXCheckBox;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

    public final static String ALL = "*";
    ObservableList<String> selectedTags = FXCollections.observableArrayList();
    HashMap<String, BooleanProperty> activeTags = new HashMap<>();
    ObservableList<String> allTags = FXCollections.observableArrayList();

    private List<JFXCheckBox> boxes = new ArrayList<>();
    private BooleanProperty allSelected = new SimpleBooleanProperty(true);

    public TagButton(String text, ObservableList<String> entry, ObservableList<String> selected) {
        super(text);
        this.allTags = entry;
        this.selectedTags.addAll(selected);

        allSelected.set(allTags.size() == selected.size());
        this.allTags.addListener((ListChangeListener<String>) c -> updateList());


        updateList();
    }

    public ObservableList<String> selectedTags() {
        return selectedTags;
    }

    public ObservableList<String> allTags() {
        return allTags;
    }

    public void updateList() {
        boxes.clear();

        MenuItem selectAllMenuItem = new MenuItem(I18n.getInstance().getString("plugin.notes.contextmenu.selectall"));
        selectAllMenuItem.setOnAction(event -> {
            boxes.forEach(jfxCheckBox -> jfxCheckBox.setSelected(true));
            allSelected.set(true);
            updateValue();
        });

        MenuItem deselectAllMenuItem = new MenuItem(I18n.getInstance().getString("plugin.notes.contextmenu.selectnone"));
        deselectAllMenuItem.setOnAction(event -> {
            boxes.forEach(jfxCheckBox -> jfxCheckBox.setSelected(false));
            allSelected.set(false);
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
                allSelected.set(false);
                updateValue();
            });
            CustomMenuItem cmi = new CustomMenuItem(cb);
            cm.getItems().add(cmi);
        });


        setContextMenu(cm);
        setOnAction(event -> {
            cm.show(this, Side.BOTTOM, 0, 0);
        });
        Platform.runLater(() -> {
            updateValue();
        });

    }

    private void updateValue() {

        List<String> selected = new ArrayList();
        boxes.forEach(jfxCheckBox -> {
            //System.out.println("jfxCheckBox: " + jfxCheckBox.isSelected() + "  " + jfxCheckBox.getText());
            if (jfxCheckBox.isSelected()) selected.add(jfxCheckBox.getText());

        });
        System.out.println("## selectedBoxes" + selected);

        if (allSelected.get()) {
            selectedTags.clear();
            selectedTags.add(ALL);
        } else if (selected.isEmpty()) {
            selectedTags.clear();
        } else {
            if (selected.size() == allTags.size()) allSelected.set(true);
            selectedTags.setAll(selected);
        }
        updateButton();

    }

    private void updateButton() {
        Platform.runLater(() -> {
            if (isAllSelected()) {
                setStyle("-fx-border-color: #51aaa5;");
                setGraphic(JEConfig.getSVGImage(Icon.FILTER_ALT_OFF, 20, 20));
            } else {
                setStyle("-fx-border-color: green;");
                setGraphic(JEConfig.getSVGImage(Icon.FILTER_ALT, 20, 20));
            }
        });

    }

    public boolean isAllSelected() {
        return allSelected.get();
    }

    public BooleanProperty allSelectedProperty() {
        return allSelected;
    }

    public ObservableList<String> getSelectedTags() {
        return selectedTags;
    }
}
