package org.jevis.jeconfig.plugin.meters;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.dialog.LocalNameDialog;
import org.jevis.jeconfig.plugin.meters.data.MeterData;
import org.jevis.jeconfig.plugin.meters.event.PrecisionEvent;
import org.jevis.jeconfig.plugin.meters.export.MetersExport;
import org.jevis.jeconfig.plugin.meters.ui.MeterTable;
import org.jevis.jeconfig.plugin.nonconformities.NonconformitiesPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class MetersToolbar extends ToolBar {
    private static final Logger logger = LogManager.getLogger(MeterController.class);


    private final double iconSize = 20;

    private final ToggleButton deleteItem = new ToggleButton("", JEConfig.getSVGImage(Icon.DELETE, iconSize, iconSize));
    private final ToggleButton exportxlsx = new ToggleButton("", JEConfig.getSVGImage(Icon.EXCEL, this.iconSize, this.iconSize));
    private final ToggleButton rename = new ToggleButton("", JEConfig.getSVGImage(Icon.TRANSLATE, this.iconSize, this.iconSize));

    private final ToggleButton add = new ToggleButton("", JEConfig.getSVGImage(Icon.PLUS_CIRCLE, iconSize, iconSize));
    private final ToggleButton switchButton = new ToggleButton("", JEConfig.getSVGImage(Icon.SWITCH, iconSize, iconSize));
    private final ToggleButton increasePrecision = new ToggleButton("", JEConfig.getSVGImage(Icon.DECIMAL_INCREASE, iconSize, iconSize));
    private final ToggleButton decreasePrecision = new ToggleButton("", JEConfig.getSVGImage(Icon.DECIMAL_DECREASE, iconSize, iconSize));

    private final ToggleButton helpButton = JEVisHelp.getInstance().buildHelpButtons(iconSize, iconSize);
    private final ToggleButton infoButton = JEVisHelp.getInstance().buildInfoButtons(iconSize, iconSize);

    private final MeterController meterController;

    public MetersToolbar(MeterController meterController) {
        this.meterController = meterController;

        getItems().addAll(add, exportxlsx, rename, deleteItem, switchButton, increasePrecision, decreasePrecision);


        add.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.meters.tooltip.add")));
        exportxlsx.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.meters.tooltip.exportxlsx")));
        rename.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.meters.tooltip.rename")));
        deleteItem.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.meters.tooltip.delete")));
        switchButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.meters.tooltip.switchmeter")));
        increasePrecision.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.meters.tooltip.increaseprecision")));
        decreasePrecision.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.meters.tooltip.decreaseprecision")));

        add.setOnAction(actionEvent -> meterController.addMeter());
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Excel", ".xlsx"));

        exportxlsx.setOnAction(actionEvent -> {
            File file = fileChooser.showSaveDialog(meterController.getContent().getScene().getWindow());
            MetersExport metersExport = new MetersExport(meterController.getActiveTab().getPlan());
            metersExport.export();
            try {
                metersExport.save(file);
            } catch (IOException ioException) {
                logger.error(ioException);
            }
        });

        switchButton.setOnAction(actionEvent -> {
            logger.debug(meterController.getSelectedItem());

            meterController.openDataForm(meterController.getSelectedItem(), true, false);
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

                MeterTable meterTable = meterController.getActiveTable();
                meterTable.replaceItem(meterData);
                meterTable.refresh();
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


        this.meterController.canDeleteProperty().addListener((observableValue, aBoolean, t1) -> {
            deleteItem.setDisable(!t1);
        });

        this.meterController.canWriteProperty().addListener((observableValue, aBoolean, t1) -> {
            add.setDisable(!t1);
        });

        add.setDisable(!meterController.isCanWrite());
        deleteItem.setDisable(!meterController.isCanDelete());

        getItems().stream().filter(node -> node instanceof ToggleButton).forEach(node -> GlobalToolBar.changeBackgroundOnHoverUsingBinding(node));


        getItems().addAll(JEVisHelp.getInstance().buildSpacerNode(), helpButton, infoButton);
        Platform.runLater(() -> JEVisHelp.getInstance().addHelpItems(NonconformitiesPlugin.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, getItems()));


    }
}
