package org.jevis.jeconfig.plugin.nonconformities.ui.tab;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityData;

public interface Tab {

    public void initTab(NonconformityData data);

    public void updateView(NonconformityData data);

    default void add(GridPane pane, int column, int row, int colspan, int rowspan, Priority priority, Node node) {
        pane.add(node, column, row, colspan, rowspan);
        GridPane.setHgrow(node, priority);
    }
}
