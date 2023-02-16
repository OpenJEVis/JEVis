package org.jevis.jeconfig.plugin.nonconformities;

import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;

public class NonconformitiesToolbar extends ToolBar {

    private final double iconSize = 20;
    private final ToggleButton openForm = new ToggleButton("", JEConfig.getSVGImage(Icon.PREVIEW, iconSize, iconSize));
    private final ToggleButton newNonconformity = new ToggleButton("", JEConfig.getSVGImage(Icon.PLAYLIST_ADD, iconSize, iconSize));
    private final ToggleButton newNonconformities = new ToggleButton("", JEConfig.getSVGImage(Icon.FOLDER_OPEN, iconSize, iconSize));
    private final ToggleButton deleteNonconformity = new ToggleButton("", JEConfig.getSVGImage(Icon.PLAYLIST_REMOVE, iconSize, iconSize));
    private final ToggleButton deletePlan = new ToggleButton("", JEConfig.getSVGImage(Icon.DELETE, iconSize, iconSize));
    private final ToggleButton reloadButton = new ToggleButton("", JEConfig.getSVGImage(Icon.REFRESH, this.iconSize, this.iconSize));
    private final ToggleButton exportPDF = new ToggleButton("", JEConfig.getSVGImage(Icon.PDF, this.iconSize, this.iconSize));
    private final ToggleButton calender = new ToggleButton("", JEConfig.getSVGImage(Icon.CALENDAR, this.iconSize, this.iconSize));
    private NonconformitiesController nonconformitiesController;

    public NonconformitiesToolbar(NonconformitiesController nonconformitiesController) {
        this.nonconformitiesController = nonconformitiesController;

        Separator sep1 = new Separator();
        Separator sep2 = new Separator();
        getItems().setAll(newNonconformities, deletePlan, reloadButton,
                sep1, newNonconformity, deleteNonconformity, openForm,
                sep2, exportPDF);

        openForm.setOnAction(event -> nonconformitiesController.openDataForm());
        newNonconformities.setOnAction(event -> nonconformitiesController.createNewNonconformities());
        newNonconformity.setOnAction(event -> nonconformitiesController.createNonconformity());
        deleteNonconformity.setOnAction(event -> nonconformitiesController.deleteNonconformity());
        deletePlan.setOnAction(event -> nonconformitiesController.deletePlan());
       // reloadButton.setOnAction(event ->nonconformitiesController.relo );
        //calender.setOnAction(event -> actionCalendar.showAndWait());

        getItems().stream().filter(node -> node instanceof ToggleButton).forEach(node -> GlobalToolBar.changeBackgroundOnHoverUsingBinding(node));


    }

}
