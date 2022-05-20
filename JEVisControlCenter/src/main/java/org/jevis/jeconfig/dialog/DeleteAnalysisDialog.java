package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.JFXComboBox;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;

public class DeleteAnalysisDialog {
    private static final Logger logger = LogManager.getLogger(DeleteAnalysisDialog.class);
    private final JEVisDataSource ds;
    private final AnalysisDataModel model;
    private final JFXComboBox<JEVisObject> listAnalysesComboBox;

    public DeleteAnalysisDialog(JEVisDataSource ds, AnalysisDataModel model, JFXComboBox<JEVisObject> listAnalysesComboBox) {
        this.ds = ds;
        this.model = model;
        this.listAnalysesComboBox = listAnalysesComboBox;

        deleteCurrentAnalysis();
    }

    private void deleteCurrentAnalysis() {
        Dialog<ButtonType> reallyDelete = new Dialog<>();
        reallyDelete.setTitle(I18n.getInstance().getString("plugin.graph.dialog.delete.title"));
        final ButtonType ok = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.delete.ok"), ButtonBar.ButtonData.YES);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.delete.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        reallyDelete.setContentText(I18n.getInstance().getString("plugin.graph.dialog.delete.message"));
        reallyDelete.getDialogPane().getButtonTypes().addAll(ok, cancel);
        reallyDelete.showAndWait().ifPresent(response -> {
            if (response.getButtonData().getTypeCode().equals(ButtonType.YES.getButtonData().getTypeCode())) {
                try {
                    if (ds.getCurrentUser().canDelete(model.getCurrentAnalysis().getID())) {
                        ds.deleteObject(model.getCurrentAnalysis().getID(), false);
                        model.updateListAnalyses();
                        listAnalysesComboBox.getSelectionModel().selectFirst();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, I18n.getInstance().getString("plugin.graph.dialog.delete.error"), cancel);
                        alert.showAndWait();
                    }
                } catch (JEVisException e) {
                    logger.error("Error: could not delete current analysis", e);
                }
            }
        });

    }
}
