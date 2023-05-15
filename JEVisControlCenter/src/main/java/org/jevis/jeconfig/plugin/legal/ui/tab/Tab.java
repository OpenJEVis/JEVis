package org.jevis.jeconfig.plugin.legal.ui.tab;


import javafx.scene.Node;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.controlsfx.control.NotificationPane;
import org.jevis.jeconfig.plugin.legal.data.ObligationData;

public abstract class Tab extends javafx.scene.control.Tab {
    protected NotificationPane notificationPane = new NotificationPane();

    public Tab(String s) {
        super(s);
    }

    public Tab() {
    }

    public Tab(String s, Node node) {
        super(s, node);
    }

    public abstract void initTab(ObligationData data);

    public abstract void updateView(ObligationData data);

    void add(GridPane pane, int column, int row, int colspan, int rowspan, Priority priority, Node node) {
        pane.add(node, column, row, colspan, rowspan);
        GridPane.setHgrow(node, priority);
    }
}
