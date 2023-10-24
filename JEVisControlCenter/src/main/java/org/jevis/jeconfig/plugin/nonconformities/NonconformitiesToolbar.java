package org.jevis.jeconfig.plugin.nonconformities;

import javafx.application.Platform;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.plugin.legal.LegalCatasdrePlugin;

public class NonconformitiesToolbar extends ToolBar {
    private static final Logger logger = LogManager.getLogger(NonconformitiesController.class);


    private final double iconSize = 20;

    private final ToggleButton nonconformityPlanConfig = new ToggleButton("", JEConfig.getSVGImage(Icon.SETTINGS, iconSize, iconSize));
    private final ToggleButton openForm = new ToggleButton("", JEConfig.getSVGImage(Icon.PREVIEW, iconSize, iconSize));
    private final ToggleButton newNonconformity = new ToggleButton("", JEConfig.getSVGImage(Icon.PLAYLIST_ADD, iconSize, iconSize));
    private final ToggleButton newNonconformityPlan = new ToggleButton("", JEConfig.getSVGImage(Icon.FOLDER_OPEN, iconSize, iconSize));
    private final ToggleButton deleteNonconformity = new ToggleButton("", JEConfig.getSVGImage(Icon.PLAYLIST_REMOVE, iconSize, iconSize));
    private final ToggleButton reloadButton = new ToggleButton("", JEConfig.getSVGImage(Icon.REFRESH, this.iconSize, this.iconSize));
    private final ToggleButton exportPDF = new ToggleButton("", JEConfig.getSVGImage(Icon.PDF, this.iconSize, this.iconSize));

    private final ToggleButton helpButton = JEVisHelp.getInstance().buildHelpButtons(iconSize, iconSize);
    private final ToggleButton infoButton = JEVisHelp.getInstance().buildInfoButtons(iconSize, iconSize);
    private NonconformitiesController nonconformitiesController;

    public NonconformitiesToolbar(NonconformitiesController nonconformitiesController) {
        this.nonconformitiesController = nonconformitiesController;

        Separator sep1 = new Separator();
        Separator sep2 = new Separator();
        getItems().setAll(newNonconformityPlan, nonconformityPlanConfig, reloadButton,
                sep1, newNonconformity, deleteNonconformity, openForm,
                sep2, exportPDF);


        nonconformityPlanConfig.setOnAction(event -> nonconformitiesController.openPlanSettings());
        openForm.setOnAction(event -> nonconformitiesController.openDataForm(false));
        newNonconformityPlan.setOnAction(event -> nonconformitiesController.createNewNonconformityPlan());
        newNonconformity.setOnAction(event -> nonconformitiesController.createNonconformity());
        deleteNonconformity.setOnAction(event -> nonconformitiesController.deleteNonconformity());


        setOverview(nonconformitiesController.isOverviewTabProperty().get());
        nonconformitiesController.isOverviewTabProperty().addListener((observable, oldValue, newValue) -> {
            logger.debug(newValue);
            setOverview(newValue);
        });

        newNonconformityPlan.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.nonconformities.tooltip.newnonconformityplan")));
        nonconformityPlanConfig.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.nonconformities.tooltip.nonconformityplanconfig")));
        reloadButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.nonconformities.tooltip.reload")));
        newNonconformity.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.nonconformities.tooltip.newnonconformity")));
        deleteNonconformity.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.nonconformities.tooltip.deletenonconformity")));
        openForm.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.nonconformities.tooltip.openform")));
        exportPDF.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.nonconformities.tooltip.exportpdf")));




        getItems().stream().filter(node -> node instanceof ToggleButton).forEach(node -> GlobalToolBar.changeBackgroundOnHoverUsingBinding(node));


        getItems().addAll(JEVisHelp.getInstance().buildSpacerNode(), helpButton, infoButton);
        Platform.runLater(() -> JEVisHelp.getInstance().addHelpItems(NonconformitiesPlugin.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, getItems()));


    }
    private void setOverview(boolean isOverview) {

        nonconformityPlanConfig.setDisable(isOverview);
        newNonconformity.setDisable(isOverview);
        deleteNonconformity.setDisable(isOverview);
        exportPDF.setDisable(true);//Disabled because implementation is missing
        reloadButton.setDisable(true); //Disabled because implementation is missing


    }


}
