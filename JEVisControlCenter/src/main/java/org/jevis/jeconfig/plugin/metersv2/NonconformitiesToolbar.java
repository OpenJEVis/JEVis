package org.jevis.jeconfig.plugin.metersv2;

import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.metersv2.export.MetersPlanExport;

import java.awt.event.ActionEvent;
import java.beans.EventHandler;
import java.io.File;
import java.io.IOException;

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
        getItems().add(exportPDF);

        add.setOnAction(actionEvent -> meterController.addMeter());
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Excel",".xlsx"));

        //Adding action on the menu item
        exportPDF.setOnAction(actionEvent -> {
            File file = fileChooser.showSaveDialog(meterController.getContent().getScene().getWindow());
            MetersPlanExport metersPlanExport = new MetersPlanExport(meterController.getActiveTab().getPlan());
            metersPlanExport.export();
            try {
                metersPlanExport.save(file);
            } catch (IOException ioException) {
                logger.error(ioException);
            }
        });



    }
}
