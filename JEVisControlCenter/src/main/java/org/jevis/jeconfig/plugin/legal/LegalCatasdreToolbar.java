package org.jevis.jeconfig.plugin.legal;

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
import org.jevis.jeconfig.plugin.action.ActionPlugin;
import org.jevis.jeconfig.plugin.nonconformities.NonconformitiesController;

public class LegalCatasdreToolbar extends ToolBar {
    private static final Logger logger = LogManager.getLogger(LegalCatasdreToolbar.class);


    private final double iconSize = 20;

    private final ToggleButton legalPlanConfig = new ToggleButton("", JEConfig.getSVGImage(Icon.SETTINGS, iconSize, iconSize));
    private final ToggleButton openForm = new ToggleButton("", JEConfig.getSVGImage(Icon.PREVIEW, iconSize, iconSize));
    private final ToggleButton newLegal = new ToggleButton("", JEConfig.getSVGImage(Icon.PLAYLIST_ADD, iconSize, iconSize));
    private final ToggleButton newLegalPlan = new ToggleButton("", JEConfig.getSVGImage(Icon.FOLDER_OPEN, iconSize, iconSize));
    private final ToggleButton deleteLegal = new ToggleButton("", JEConfig.getSVGImage(Icon.PLAYLIST_REMOVE, iconSize, iconSize));

    private final ToggleButton helpButton = JEVisHelp.getInstance().buildHelpButtons(iconSize, iconSize);
    private final ToggleButton infoButton = JEVisHelp.getInstance().buildInfoButtons(iconSize, iconSize);
    private LegalCadastreController legalCadastreController;

    public LegalCatasdreToolbar(LegalCadastreController legalCadastreController) {
        this.legalCadastreController = legalCadastreController;

        Separator sep1 = new Separator();
        Separator sep2 = new Separator();
        getItems().setAll(newLegalPlan, legalPlanConfig,
                sep1, newLegal, deleteLegal, openForm);


        legalPlanConfig.setOnAction(event -> legalCadastreController.openPlanSettings());
        openForm.setOnAction(event -> legalCadastreController.openDataForm(false));
        newLegalPlan.setOnAction(event -> legalCadastreController.createNewPlan());
        newLegal.setOnAction(event -> legalCadastreController.createItem());
        deleteLegal.setOnAction(event -> legalCadastreController.deleteItem());

        legalPlanConfig.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.legal.tooltip.legalplanconfig")));
        openForm.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.legal.tooltip.openform")));
        newLegalPlan.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.legal.tooltip.newlegalplan")));
        newLegal.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.legal.tooltip.newlegal")));
        deleteLegal.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.legal.tooltip.deletelegal")));






        getItems().stream().filter(node -> node instanceof ToggleButton).forEach(node -> GlobalToolBar.changeBackgroundOnHoverUsingBinding(node));


        getItems().addAll(JEVisHelp.getInstance().buildSpacerNode(), helpButton, infoButton);
        Platform.runLater(() -> JEVisHelp.getInstance().addHelpItems(LegalCatasdrePlugin.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, getItems()));
        legalPlanConfig.setDisable(false);
        newLegal.setDisable(false);
        deleteLegal.setDisable(false);

    }

    private void setOverview(boolean isOverview) {

        legalPlanConfig.setDisable(isOverview);
        newLegal.setDisable(isOverview);
        deleteLegal.setDisable(isOverview);


    }


}
