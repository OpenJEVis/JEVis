package org.jevis.jeconfig.plugin.metersv2;

import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;

public class NonconformitiesToolbar extends ToolBar {
    private static final Logger logger = LogManager.getLogger(MeterController.class);


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

    private final ToggleButton add = new ToggleButton("", JEConfig.getSVGImage(Icon.PLUS_CIRCLE, iconSize, iconSize));
    private MeterController meterController;

    public NonconformitiesToolbar(MeterController meterController) {

        getItems().add(add);

        add.setOnAction(actionEvent -> meterController.addMeter());
//        this.nonconformitiesController = nonconformitiesController;
//
//        Separator sep1 = new Separator();
//        Separator sep2 = new Separator();
//        getItems().setAll(newNonconformityPlan, nonconformityPlanConfig, deletePlan, reloadButton,
//                sep1, newNonconformity, deleteNonconformity, openForm,
//                sep2, exportPDF);
//
//
//        nonconformityPlanConfig.setOnAction(event -> nonconformitiesController.openPlanSettings());
//        openForm.setOnAction(event -> nonconformitiesController.openDataForm(false));
//        newNonconformityPlan.setOnAction(event -> nonconformitiesController.createNewNonconformityPlan());
//        newNonconformity.setOnAction(event -> nonconformitiesController.createNonconformity());
//        deleteNonconformity.setOnAction(event -> nonconformitiesController.deleteNonconformity());
//        deletePlan.setOnAction(event -> nonconformitiesController.deletePlan());
//
//        setOverview(nonconformitiesController.isOverviewTabProperty().get());
//        nonconformitiesController.isOverviewTabProperty().addListener((observable, oldValue, newValue) -> {
//            logger.debug(newValue);
//            setOverview(newValue);
//        });
//
//
//       // reloadButton.setOnAction(event ->nonconformitiesController.relo );
//        //calender.setOnAction(event -> actionCalendar.showAndWait());
//
//        getItems().stream().filter(node -> node instanceof ToggleButton).forEach(node -> GlobalToolBar.changeBackgroundOnHoverUsingBinding(node));


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
