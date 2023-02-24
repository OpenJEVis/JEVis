package org.jevis.jeconfig.plugin.action.ui;

import javafx.scene.control.Dialog;
import javafx.scene.control.TabPane;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.action.data.ActionData;
import org.jevis.jeconfig.plugin.action.data.ActionPlanData;
import org.jevis.jeconfig.plugin.action.ui.tab.*;
import org.jevis.jeconfig.tool.ScreenSize;

public class ActionForm extends Dialog {


    private TabPane tabPane = new TabPane();
    private ActionPlanData actionPlan;
    private DetailsTab detailTab;
    private CapitalTab capitalTab;
    private GeneralTab generalTab;
    private AttachmentTab attachmentTab;
    private CheckListTab checkListTab;

    public ActionForm(ActionPlanData actionPlan, ActionData action) {
        super();
        this.initOwner(JEConfig.getStage());
        this.actionPlan = actionPlan;


        setTitle(I18n.getInstance().getString("actionform.editor.title"));
        setHeaderText(null);
        setResizable(true);
        this.getDialogPane().setPrefWidth(ScreenSize.fitScreenWidth(1000));
        this.getDialogPane().setPrefHeight(ScreenSize.fitScreenHeight(950));

        widthProperty().addListener((observable, oldValue, newValue) -> tabPane.setPrefWidth(newValue.doubleValue() - 50));
        tabPane.setPrefWidth(ScreenSize.fitScreenWidth(1050));

        detailTab = new DetailsTab(action);
        capitalTab = new CapitalTab(action);
        generalTab = new GeneralTab(action);
        attachmentTab = new AttachmentTab(action);
        checkListTab = new CheckListTab(action);

        generalTab.setClosable(false);
        detailTab.setClosable(false);
        attachmentTab.setClosable(false);
        capitalTab.setClosable(false);
        //capitalTab.setDisable(true);
        checkListTab.setClosable(false);

        tabPane.getTabs().addAll(generalTab, detailTab, capitalTab, checkListTab, attachmentTab);
        getDialogPane().setContent(tabPane);

    }


}
