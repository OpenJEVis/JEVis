package org.jevis.jecc.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.application.Chart.ChartPluginElements.Boxes.AnalysesComboBox;
import org.jevis.jecc.application.Chart.data.AnalysisHandler;
import org.jevis.jecc.plugin.charts.DataSettings;

public class DeleteAnalysisDialog {
    private static final Logger logger = LogManager.getLogger(DeleteAnalysisDialog.class);
    private final JEVisDataSource ds;
    private final DataSettings dataSettings;
    private final AnalysesComboBox analysesComboBox;

    public DeleteAnalysisDialog(JEVisDataSource ds, DataSettings dataSettings, AnalysesComboBox analysesComboBox) {
        this.ds = ds;
        this.dataSettings = dataSettings;
        this.analysesComboBox = analysesComboBox;

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
                    if (dataSettings.getCurrentAnalysis().getJEVisClassName().equals("User")) {
                        JEVisAttribute analysisFileAttribute = ds.getCurrentUser().getUserObject().getAttribute(AnalysisHandler.ANALYSIS_FILE_ATTRIBUTE_NAME);
                        if (analysisFileAttribute.hasSample()) {
                            int currentIndex = analysesComboBox.getItems().indexOf(dataSettings.getCurrentAnalysis());
                            analysisFileAttribute.deleteSamplesBetween(analysisFileAttribute.getTimestampFromLastSample(), analysisFileAttribute.getTimestampFromLastSample());

                            updateAnalysisBox(currentIndex);
                        }
                    } else if (ds.getCurrentUser().canDelete(dataSettings.getCurrentAnalysis().getID())) {
                        int currentIndex = analysesComboBox.getItems().indexOf(dataSettings.getCurrentAnalysis());
                        ds.deleteObject(dataSettings.getCurrentAnalysis().getID(), false);

                        updateAnalysisBox(currentIndex);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, I18n.getInstance().getString("plugin.graph.dialog.delete.error"), cancel);
                        alert.showAndWait();
                    }
                } catch (Exception e) {
                    logger.error("Error: could not delete current analysis", e);
                }
            }
        });

    }

    private void updateAnalysisBox(int currentIndex) {
        analysesComboBox.updateListAnalyses();
        analysesComboBox.getSelectionModel().select(currentIndex - 1);
    }
}
