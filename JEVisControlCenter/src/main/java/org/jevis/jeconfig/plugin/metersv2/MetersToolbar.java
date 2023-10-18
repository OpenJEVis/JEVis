package org.jevis.jeconfig.plugin.metersv2;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.dialog.LocalNameDialog;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;
import org.jevis.jeconfig.plugin.metersv2.event.PrecisionEvent;
import org.jevis.jeconfig.plugin.metersv2.export.MetersPlanExport;
import org.jevis.jeconfig.plugin.metersv2.ui.MeterPlanTable;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class MetersToolbar extends ToolBar {
    private static final Logger logger = LogManager.getLogger(MeterController.class);


    private final double iconSize = 20;

    private final ToggleButton nonconformityPlanConfig = new ToggleButton("", JEConfig.getSVGImage(Icon.SETTINGS, iconSize, iconSize));
    private final ToggleButton openForm = new ToggleButton("", JEConfig.getSVGImage(Icon.PREVIEW, iconSize, iconSize));
    private final ToggleButton newNonconformity = new ToggleButton("", JEConfig.getSVGImage(Icon.PLAYLIST_ADD, iconSize, iconSize));
    private final ToggleButton newNonconformityPlan = new ToggleButton("", JEConfig.getSVGImage(Icon.FOLDER_OPEN, iconSize, iconSize));
    private final ToggleButton deleteNonconformity = new ToggleButton("", JEConfig.getSVGImage(Icon.PLAYLIST_REMOVE, iconSize, iconSize));
    private final ToggleButton deleteItem = new ToggleButton("", JEConfig.getSVGImage(Icon.DELETE, iconSize, iconSize));
    private final ToggleButton reloadButton = new ToggleButton("", JEConfig.getSVGImage(Icon.REFRESH, this.iconSize, this.iconSize));
    private final ToggleButton exportPDF = new ToggleButton("", JEConfig.getSVGImage(Icon.EXCEL, this.iconSize, this.iconSize));
    private final ToggleButton rename = new ToggleButton("", JEConfig.getSVGImage(Icon.TRANSLATE, this.iconSize, this.iconSize));

    private final ToggleButton add = new ToggleButton("", JEConfig.getSVGImage(Icon.PLUS_CIRCLE, iconSize, iconSize));
    private final ToggleButton switchButton = new ToggleButton("", JEConfig.getSVGImage(Icon.SWITCH, iconSize, iconSize));
    private final ToggleButton increasePrecision = new ToggleButton("", JEConfig.getSVGImage(Icon.DECIMAL_INCREASE, iconSize, iconSize));
    private final ToggleButton decreasePrecision = new ToggleButton("", JEConfig.getSVGImage(Icon.DECIMAL_DECREASE, iconSize, iconSize));

    //  private final JFXComboBox<Integer> comboPrecision = new JFXComboBox(FXCollections.observableArrayList(1,2,3,4,5));
    private MeterController meterController;

    public MetersToolbar(MeterController meterController) {

        getItems().addAll(add, exportPDF, rename, deleteItem,switchButton,increasePrecision,decreasePrecision);
        getItems().stream().filter(node -> node instanceof ToggleButton).forEach(node -> GlobalToolBar.changeBackgroundOnHoverUsingBinding(node));

        add.setOnAction(actionEvent -> meterController.addMeter());
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Excel", ".xlsx"));

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

        switchButton.setOnAction(actionEvent -> {
            System.out.println(meterController.getSelectedItem());

           meterController.openDataForm(meterController.getSelectedItem(),true,false);
        });




        increasePrecision.setOnAction(actionEvent -> {
            meterController.getActiveTable().getSelectedItems().forEach(meterData -> {
                meterController.getActiveTable().getPrecisionEventHandler().fireEvent(new PrecisionEvent(meterData, PrecisionEvent.TYPE.INCREASE));
            });
        });

        decreasePrecision.setOnAction(actionEvent -> {

            meterController.getActiveTable().getSelectedItems().forEach(meterData -> {
                meterController.getActiveTable().getPrecisionEventHandler().fireEvent(new PrecisionEvent(meterData, PrecisionEvent.TYPE.DECREASE));
            });

        });

        rename.setOnAction(actionEvent -> {
            MeterData meterData = meterController.getSelectedItem();
            LocalNameDialog localNameDialog = new LocalNameDialog(meterData.getJeVisObject());
            LocalNameDialog.Response response = localNameDialog.show();
            if (response.equals(LocalNameDialog.Response.YES)) {

                MeterPlanTable meterPlanTable = meterController.getActiveTable();
                meterPlanTable.replaceItem(meterData);
                meterPlanTable.refresh();
            }
        });

        deleteItem.setOnAction(actionEvent -> {
            MeterData meterData = meterController.getSelectedItem();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, I18n.getInstance().getString("plugin.meters.delete.title"), ButtonType.OK, ButtonType.CANCEL);

            alert.setTitle(I18n.getInstance().getString("plugin.meters.delete.title"));
            alert.setContentText(String.format(I18n.getInstance().getString("plugin.meters.delete.body"), meterData.getName()));
            Optional<ButtonType> result = alert.showAndWait();
            if (result.orElse(ButtonType.CANCEL) != ButtonType.OK) return;
            try {
                meterController.getActiveTable().removeItem(meterData);
                meterData.getJeVisObject().delete();
            } catch (JEVisException jeVisException) {
                logger.error(jeVisException);
            }
            meterController.getActiveTable().refresh();
        });


    }
}
