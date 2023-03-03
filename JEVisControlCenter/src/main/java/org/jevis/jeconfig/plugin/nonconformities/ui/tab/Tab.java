package org.jevis.jeconfig.plugin.nonconformities.ui.tab;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.controlsfx.control.NotificationPane;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityData;
import org.jevis.jeconfig.plugin.nonconformities.ui.NonconformityForm;

public abstract class Tab extends javafx.scene.control.Tab{
   NotificationPane notificationPane = new NotificationPane();

    public Tab(String s) {
        super(s);
    }

    public Tab() {
    }

    public Tab(String s, Node node) {
        super(s, node);
    }

    public abstract void initTab(NonconformityData data);

    public abstract void updateView(NonconformityData data);

    void add(GridPane pane, int column, int row, int colspan, int rowspan, Priority priority, Node node) {
        pane.add(node, column, row, colspan, rowspan);
        GridPane.setHgrow(node, priority);
    }
    public void showNotification(String text, Node icon) {
        notificationPane.show(text,icon);
        notificationPane.setOnHiding(event -> {
            this.getTabPane().getTabs().forEach(tab -> {
                NotificationPane content = (NotificationPane) tab.getContent();
                content.hide();
            });

        });
    }
    public void closeNotefication() {
        notificationPane.hide();
    }
}
