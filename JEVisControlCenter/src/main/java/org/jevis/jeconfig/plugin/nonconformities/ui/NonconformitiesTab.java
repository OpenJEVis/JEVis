package org.jevis.jeconfig.plugin.nonconformities.ui;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import org.jevis.jeconfig.plugin.nonconformities.data.Nonconformities;

public class NonconformitiesTab extends Tab {

    private Nonconformities plan;
    private NonconformitiesTable nonconformitiesTable;

    public NonconformitiesTab(Nonconformities plan, NonconformitiesTable nonconformitiesTable) {
        super();

        textProperty().bind(plan.getName());
        this.plan = plan;
        this.nonconformitiesTable = nonconformitiesTable;
    }

    public NonconformitiesTab(String text, Node content, Nonconformities plan) {
        super(text, content);
        this.plan = plan;
    }

    public Nonconformities getNonconformities() {
        return plan;
    }

    public void setActionPlan(Nonconformities plan) {
        this.plan = plan;
    }

    public NonconformitiesTable getActionTable() {
        return nonconformitiesTable;
    }
}
