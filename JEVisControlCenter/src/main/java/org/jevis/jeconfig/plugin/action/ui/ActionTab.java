package org.jevis.jeconfig.plugin.action.ui;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import org.jevis.jeconfig.plugin.action.data.ActionPlanData;

public class ActionTab extends Tab {

    private ActionPlanData plan;
    private ActionTable actionTable;

    public ActionTab(ActionPlanData plan, ActionTable actionTable) {
        super();

        textProperty().bind(plan.getName());
        this.plan = plan;
        this.actionTable = actionTable;
    }

    public ActionTab(String text, Node content, ActionPlanData plan) {
        super(text, content);
        this.plan = plan;
    }

    public ActionPlanData getActionPlan() {
        return plan;
    }

    public void setActionPlan(ActionPlanData plan) {
        this.plan = plan;
    }

    public ActionTable getActionTable() {
        return actionTable;
    }
}
