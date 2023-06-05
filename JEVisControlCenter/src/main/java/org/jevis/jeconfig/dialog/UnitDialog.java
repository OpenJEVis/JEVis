package org.jevis.jeconfig.dialog;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.plugin.unit.UnitSelectUI;

public class UnitDialog extends Dialog {
    private static final Logger logger = LogManager.getLogger(UnitDialog.class);

    public UnitDialog(JEVisAttribute attribute, MFXTextField ubutton) throws JEVisException {
        super();
        setTitle(I18n.getInstance().getString("plugin.configuration.unitdialog.title"));
        setHeaderText(I18n.getInstance().getString("plugin.configuration.unitdialog.header"));
        setResizable(true);
        initOwner(JEConfig.getStage());
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        JEVisDataSource ds = attribute.getDataSource();

        GridPane gp = new GridPane();
        gp.setPadding(new Insets(8));
        gp.setHgap(6);
        gp.setVgap(6);

        final Label l_prefixL = new Label(I18n.getInstance().getString("attribute.editor.unit.prefix"));
        final Label l_unitL = new Label(I18n.getInstance().getString("attribute.editor.unit.unit"));
        final Label l_example = new Label(I18n.getInstance().getString("attribute.editor.unit.symbol"));

        gp.add(l_prefixL, 0, 1);
        gp.add(l_unitL, 0, 2);
        gp.add(l_example, 0, 3);

        UnitSelectUI unitUI = new UnitSelectUI(ds, attribute.getInputUnit());
        unitUI.getPrefixBox().setPrefWidth(95);
        unitUI.getUnitButton().setPrefWidth(95);
        unitUI.getSymbolField().setPrefWidth(95);

        gp.add(unitUI.getPrefixBox(), 1, 1);
        gp.add(unitUI.getUnitButton(), 1, 2);
        gp.add(unitUI.getSymbolField(), 1, 3);

        ButtonType okType = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        this.getDialogPane().getButtonTypes().addAll(cancelType, okType);

        Button okButton = (Button) this.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);
        okButton.setOnAction(event -> {
            try {
                attribute.setDisplayUnit(unitUI.getUnit());
                attribute.setInputUnit(unitUI.getUnit());
                attribute.commit();
            } catch (JEVisException e) {
                logger.error("Could not change unit", e);
            }
            Platform.runLater(() -> ubutton.setText(UnitManager.getInstance().format(unitUI.getUnit())));
            close();
        });

        Button cancelButton = (Button) this.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(event -> close());

        Separator separator = new Separator(Orientation.HORIZONTAL);
        gp.add(separator, 0, 4, 2, 1);

        getDialogPane().setContent(gp);
        getDialogPane().setMinSize(270, 140);
    }

    public UnitDialog(JEVisAttribute attribute, MFXComboBox<JEVisUnit> unitListView) throws JEVisException {
        super();
        setTitle(I18n.getInstance().getString("plugin.configuration.unitdialog.title"));
        setHeaderText(I18n.getInstance().getString("plugin.configuration.unitdialog.header"));
        setResizable(true);
        initOwner(JEConfig.getStage());
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        JEVisDataSource ds = attribute.getDataSource();

        GridPane gp = new GridPane();
        gp.setPadding(new Insets(8));
        gp.setHgap(6);
        gp.setVgap(6);

        final Label l_prefixL = new Label(I18n.getInstance().getString("attribute.editor.unit.prefix"));
        final Label l_unitL = new Label(I18n.getInstance().getString("attribute.editor.unit.unit"));
        final Label l_example = new Label(I18n.getInstance().getString("attribute.editor.unit.symbol"));

        gp.add(l_prefixL, 0, 1);
        gp.add(l_unitL, 0, 2);
        gp.add(l_example, 0, 3);

        UnitSelectUI unitUI = new UnitSelectUI(ds, attribute.getInputUnit());
        unitUI.getPrefixBox().setPrefWidth(95);
        unitUI.getUnitButton().setPrefWidth(95);
        unitUI.getSymbolField().setPrefWidth(95);

        gp.add(unitUI.getPrefixBox(), 1, 1);
        gp.add(unitUI.getUnitButton(), 1, 2);
        gp.add(unitUI.getSymbolField(), 1, 3);

        ButtonType okType = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        this.getDialogPane().getButtonTypes().addAll(cancelType, okType);

        Button okButton = (Button) this.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);
        okButton.setOnAction(event -> {
            try {
                attribute.setDisplayUnit(unitUI.getUnit());
                attribute.setInputUnit(unitUI.getUnit());
                attribute.commit();
            } catch (JEVisException e) {
                logger.error("Could not change unit", e);
            }
            Platform.runLater(() -> {
                if (!unitListView.getItems().contains(unitUI.getUnit())) {
                    UnitManager.getInstance().getFavoriteJUnits().add(unitUI.getUnit());
                    unitListView.getItems().add(unitUI.getUnit());
                }
                unitListView.selectItem(unitUI.getUnit());
            });
            close();
        });

        Button cancelButton = (Button) this.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(event -> {
            Platform.runLater(() -> {
                unitListView.getSelectionModel().selectFirst();
            });
            close();
        });

        Separator separator = new Separator(Orientation.HORIZONTAL);
        gp.add(separator, 0, 4, 2, 1);

        getDialogPane().setContent(gp);
        getDialogPane().setMinSize(270, 140);
    }
}
