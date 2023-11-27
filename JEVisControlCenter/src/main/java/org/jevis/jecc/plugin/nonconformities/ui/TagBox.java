package org.jevis.jecc.plugin.nonconformities.ui;


import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;

import java.awt.*;
import java.util.HashMap;

public class TagBox extends Menu {

    javafx.scene.control.MenuItem selectAllMenuItem = new javafx.scene.control.MenuItem("Alle Auswählen");
    javafx.scene.control.MenuItem deselectAllMenuItem = new javafx.scene.control.MenuItem("Alle Abwählen");
    Button tagButton = new Button("Tags");

    public TagBox(ObservableList<String> allTags, HashMap<String, BooleanProperty> aktiveTags) throws HeadlessException {


    }


    private void createMenuItems() {

    }

}
