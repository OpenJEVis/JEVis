package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.plugin.unit.UnitSelectUI;

public class UnitDialog extends JFXDialog {
    private static final Logger logger = LogManager.getLogger(UnitDialog.class);

    public UnitDialog(StackPane parent, JEVisAttribute attribute, JFXTextField ubutton) throws JEVisException {
        super();
        setDialogContainer(parent);

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

        JFXButton ok = new JFXButton(I18n.getInstance().getString("graph.dialog.ok"));
        ok.setDefaultButton(true);
        ok.setOnAction(event -> {
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

        JFXButton cancel = new JFXButton(I18n.getInstance().getString("graph.dialog.cancel"));
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> close());

        Separator separator = new Separator(Orientation.HORIZONTAL);
        gp.add(separator, 0, 4, 2, 1);

        gp.add(cancel, 0, 5);
        gp.add(ok, 1, 5);

        setContent(gp);
        setMinSize(270, 140);
    }
}
