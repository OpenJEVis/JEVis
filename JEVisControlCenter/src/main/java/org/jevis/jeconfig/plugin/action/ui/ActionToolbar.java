package org.jevis.jeconfig.plugin.action.ui;

import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.action.ActionController;

public class ActionToolbar extends ToolBar {

    private final double iconSize = 20;
    private final ToggleButton actionPlanConfig = new ToggleButton("", JEConfig.getSVGImage(Icon.SETTINGS, iconSize, iconSize));
    private final ToggleButton openForm = new ToggleButton("", JEConfig.getSVGImage(Icon.PREVIEW, iconSize, iconSize));
    private final ToggleButton newAction = new ToggleButton("", JEConfig.getSVGImage(Icon.PLAYLIST_ADD, iconSize, iconSize));
    private final ToggleButton newPlan = new ToggleButton("", JEConfig.getSVGImage(Icon.FOLDER_OPEN, iconSize, iconSize));
    private final ToggleButton deleteAction = new ToggleButton("", JEConfig.getSVGImage(Icon.PLAYLIST_REMOVE, iconSize, iconSize));
    private final ToggleButton deletePlan = new ToggleButton("", JEConfig.getSVGImage(Icon.DELETE, iconSize, iconSize));
    private final ToggleButton reloadButton = new ToggleButton("", JEConfig.getSVGImage(Icon.REFRESH, this.iconSize, this.iconSize));
    private final ToggleButton exportPDF = new ToggleButton("", JEConfig.getSVGImage(Icon.PDF, this.iconSize, this.iconSize));
    private final ToggleButton calender = new ToggleButton("", JEConfig.getSVGImage(Icon.CALENDAR, this.iconSize, this.iconSize));
    private ActionController actionController;

    public ActionToolbar(ActionController actionController) {
        this.actionController = actionController;

        Separator sep1 = new Separator();
        Separator sep2 = new Separator();
        getItems().setAll(newPlan, actionPlanConfig, deletePlan, reloadButton,
                sep1, newAction, deleteAction, openForm,
                sep2, exportPDF);

        actionPlanConfig.setOnAction(event -> actionController.openPlanSettings());
        openForm.setOnAction(event -> actionController.openDataForm());
        newPlan.setOnAction(event -> actionController.createNewPlan());
        newAction.setOnAction(event -> actionController.createNewAction());
        deleteAction.setOnAction(event -> actionController.deleteAction());
        deletePlan.setOnAction(event -> actionController.deletePlan());
        //calender.setOnAction(event -> actionCalendar.showAndWait());

        setOverview(actionController.isOverviewTabProperty().get());
        actionController.isOverviewTabProperty().addListener((observable, oldValue, newValue) -> {
            setOverview(newValue);
        });

        getItems().stream().filter(node -> node instanceof ToggleButton).forEach(node -> GlobalToolBar.changeBackgroundOnHoverUsingBinding(node));

    }

    private void setOverview(boolean isOverview) {

        actionPlanConfig.setDisable(isOverview);
        newAction.setDisable(isOverview);
        deleteAction.setDisable(isOverview);
        deletePlan.setDisable(isOverview);
        exportPDF.setDisable(true);//Disabled because implementation is missing
        reloadButton.setDisable(true); //Disabled because implementation is missing
        //newPlan.setDisable(isOverview);
        //openForm.setDisable(isOverview);
        //reloadButton.setDisable(isOverview);

    }

}
