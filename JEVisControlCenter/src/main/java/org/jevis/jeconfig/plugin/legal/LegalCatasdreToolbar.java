package org.jevis.jeconfig.plugin.legal;

import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.nonconformities.NonconformitiesController;

public class LegalCatasdreToolbar extends ToolBar {
    private static final Logger logger = LogManager.getLogger(NonconformitiesController.class);


    private final double iconSize = 20;

    private final ToggleButton nonconformityPlanConfig = new ToggleButton("", JEConfig.getSVGImage(Icon.SETTINGS, iconSize, iconSize));
    private final ToggleButton openForm = new ToggleButton("", JEConfig.getSVGImage(Icon.PREVIEW, iconSize, iconSize));
    private final ToggleButton newNonconformity = new ToggleButton("", JEConfig.getSVGImage(Icon.PLAYLIST_ADD, iconSize, iconSize));
    private final ToggleButton newNonconformityPlan = new ToggleButton("", JEConfig.getSVGImage(Icon.FOLDER_OPEN, iconSize, iconSize));
    private final ToggleButton deleteNonconformity = new ToggleButton("", JEConfig.getSVGImage(Icon.PLAYLIST_REMOVE, iconSize, iconSize));
    private final ToggleButton deletePlan = new ToggleButton("", JEConfig.getSVGImage(Icon.DELETE, iconSize, iconSize));
    private final ToggleButton reloadButton = new ToggleButton("", JEConfig.getSVGImage(Icon.REFRESH, this.iconSize, this.iconSize));
    private final ToggleButton exportPDF = new ToggleButton("", JEConfig.getSVGImage(Icon.PDF, this.iconSize, this.iconSize));
    private final ToggleButton calender = new ToggleButton("", JEConfig.getSVGImage(Icon.CALENDAR, this.iconSize, this.iconSize));
    private LegalCadastreController legalCadastreController;

    public LegalCatasdreToolbar(LegalCadastreController legalCadastreController) {
        this.legalCadastreController = legalCadastreController;

        Separator sep1 = new Separator();
        Separator sep2 = new Separator();
        getItems().setAll(newNonconformityPlan, nonconformityPlanConfig, deletePlan, reloadButton,
                sep1, newNonconformity, deleteNonconformity, openForm,
                sep2, exportPDF);


        nonconformityPlanConfig.setOnAction(event -> legalCadastreController.openPlanSettings());
        openForm.setOnAction(event -> legalCadastreController.openDataForm(false));
        newNonconformityPlan.setOnAction(event -> legalCadastreController.createNewPlan());
        newNonconformity.setOnAction(event -> legalCadastreController.createItem());
        deleteNonconformity.setOnAction(event -> legalCadastreController.deleteItem());
        deletePlan.setOnAction(event -> legalCadastreController.deletePlan());


        getItems().stream().filter(node -> node instanceof ToggleButton).forEach(node -> GlobalToolBar.changeBackgroundOnHoverUsingBinding(node));
        nonconformityPlanConfig.setDisable(false);
        newNonconformity.setDisable(false);
        deleteNonconformity.setDisable(false);
        deletePlan.setDisable(false);
        exportPDF.setDisable(true);//Disabled because implementation is missing
        reloadButton.setDisable(true);

    }

    private void setOverview(boolean isOverview) {

        nonconformityPlanConfig.setDisable(isOverview);
        newNonconformity.setDisable(isOverview);
        deleteNonconformity.setDisable(isOverview);
        deletePlan.setDisable(isOverview);
        exportPDF.setDisable(true);//Disabled because implementation is missing
        reloadButton.setDisable(true); //Disabled because implementation is missing
        //newPlan.setDisable(isOverview);
        //openForm.setDisable(isOverview);
        //reloadButton.setDisable(isOverview);

    }


}
