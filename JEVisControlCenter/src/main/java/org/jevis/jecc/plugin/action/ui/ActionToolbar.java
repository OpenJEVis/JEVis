package org.jevis.jecc.plugin.action.ui;


import javafx.application.Platform;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.GlobalToolBar;
import org.jevis.jecc.Icon;
import org.jevis.jecc.application.tools.JEVisHelp;
import org.jevis.jecc.plugin.action.ActionController;
import org.jevis.jecc.plugin.action.ActionPlugin;
import org.jevis.jecc.plugin.action.data.ActionPlanData;
import org.jevis.jecc.plugin.action.data.ActionPlanOverviewData;

public class ActionToolbar extends ToolBar {

    private final double iconSize = 20;
    private final ToggleButton actionPlanConfig = new ToggleButton("", ControlCenter.getSVGImage(Icon.SETTINGS, iconSize, iconSize));
    private final ToggleButton openForm = new ToggleButton("", ControlCenter.getSVGImage(Icon.PREVIEW, iconSize, iconSize));
    private final ToggleButton newAction = new ToggleButton("", ControlCenter.getSVGImage(Icon.PLUS, iconSize, iconSize));
    private final ToggleButton newPlan = new ToggleButton("", ControlCenter.getSVGImage(Icon.FOLDER_OPEN, iconSize, iconSize));
    private final ToggleButton deleteAction = new ToggleButton("", ControlCenter.getSVGImage(Icon.DELETE, iconSize, iconSize));
    private final ToggleButton deletePlan = new ToggleButton("", ControlCenter.getSVGImage(Icon.DELETE, iconSize, iconSize));
    private final ToggleButton reloadButton = new ToggleButton("", ControlCenter.getSVGImage(Icon.REFRESH, this.iconSize, this.iconSize));
    private final ToggleButton exportPDF = new ToggleButton("", ControlCenter.getSVGImage(Icon.EXCEL, this.iconSize, this.iconSize));
    private final ToggleButton calender = new ToggleButton("", ControlCenter.getSVGImage(Icon.EXCEL, this.iconSize, this.iconSize));
    private final ToggleButton helpButton = JEVisHelp.getInstance().buildHelpButtons(iconSize, iconSize);
    private final ToggleButton infoButton = JEVisHelp.getInstance().buildInfoButtons(iconSize, iconSize);
    private final ActionController actionController;

    public ActionToolbar(ActionController actionController) {
        this.actionController = actionController;
        //hamburger.getChildren().add(new Label("Test"));

        Separator sep1 = new Separator();
        Separator sep2 = new Separator();
        getItems().setAll(newPlan, actionPlanConfig, reloadButton,
                sep1, newAction, deleteAction, openForm,
                sep2, exportPDF,
                JEVisHelp.getInstance().buildSpacerNode(), helpButton, infoButton);

        actionPlanConfig.setOnAction(event -> actionController.openPlanSettings());
        openForm.setOnAction(event -> actionController.openDataForm());
        newPlan.setOnAction(event -> actionController.createNewPlan());
        newAction.setOnAction(event -> actionController.createNewAction());
        deleteAction.setOnAction(event -> actionController.deleteAction());
        deletePlan.setOnAction(event -> actionController.deletePlan());
        exportPDF.setOnAction(event -> actionController.exportPDF());
        reloadButton.setOnAction(event -> actionController.reload());
        //calender.setOnAction(event -> actionCalendar.showAndWait());

        actionPlanConfig.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.action.toolbar.tip.panconfig")));
        openForm.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.action.toolbar.tip.openaction")));
        newPlan.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.action.toolbar.tip.newplan")));
        newAction.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.action.toolbar.tip.newaction")));
        deleteAction.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.action.toolbar.tip.deleteaction")));
        deletePlan.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.action.toolbar.tip.deleteplan")));
        reloadButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.action.toolbar.tip.reload")));
        exportPDF.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.action.toolbar.tip.exportpdf")));


        setOverview(actionController.isOverviewTabProperty().get());
        actionController.isOverviewTabProperty().addListener((observable, oldValue, newValue) -> {
            setOverview(newValue);
        });

        getItems().stream().filter(node -> node instanceof ToggleButton).forEach(node -> GlobalToolBar.changeBackgroundOnHoverUsingBinding(node));

        Platform.runLater(() -> JEVisHelp.getInstance().addHelpItems(ActionPlugin.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, getItems()));

    }

    private void setOverview(boolean isOverview) {
        deletePlan.setDisable(true);
        deleteAction.setDisable(true);
        //newPlan.setDisable(true);

        try {
            if (actionController.getActiveActionPlan() != null) {
                JEVisDataSource ds = ControlCenter.getDataSource();
                ActionPlanData plan = actionController.getActiveActionPlan();

                if (plan instanceof ActionPlanOverviewData) {
                    deletePlan.setDisable(true);
                } else {
                    deletePlan.setDisable(!ds.getCurrentUser().canDelete(plan.getObject().getID()));
                    deleteAction.setDisable(!ds.getCurrentUser().canDelete(actionController.getSelectedData().getObject().getID()));
                }


            }
            //newPlan.setDisable(!da.getCurrentUser().canCreate(plan.getObject().getID()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        actionPlanConfig.setDisable(isOverview);
        newAction.setDisable(isOverview);
        //exportPDF.setDisable(true);

        //exportPDF.setDisable(true);//Disabled because implementation is missing
        //reloadButton.setDisable(true); //Disabled because implementation is missing
        //newPlan.setDisable(isOverview);
        //openForm.setDisable(isOverview);
        //reloadButton.setDisable(isOverview);

    }


}
