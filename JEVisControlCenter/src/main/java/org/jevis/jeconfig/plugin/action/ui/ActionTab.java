package org.jevis.jeconfig.plugin.action.ui;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import org.jevis.jeconfig.plugin.action.data.ActionPlan;

public class ActionTab extends Tab {

    private ActionPlan plan;
    private ActionTable actionTable;

    public ActionTab(ActionPlan plan, ActionTable actionTable) {
        super();

        textProperty().bind(plan.getName());
        this.plan = plan;
        this.actionTable = actionTable;
    }

    public ActionTab(String text, Node content, ActionPlan plan) {
        super(text, content);
        this.plan = plan;
    }

    public ActionPlan getActionPlan() {
        return plan;
    }

    public void setActionPlan(ActionPlan plan) {
        this.plan = plan;
    }

    public ActionTable getActionTable() {
        return actionTable;
    }
}
