package org.jevis.jecc.dialog;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.application.Chart.ChartPluginElements.Boxes.AnalysesComboBox;
import org.jevis.jecc.application.Chart.ChartTools;
import org.jevis.jecc.plugin.charts.ChartPlugin;
import org.jevis.jecc.plugin.charts.DataSettings;
import org.jevis.jecc.plugin.charts.ToolBarView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SaveAnalysisDialog extends Dialog {
    private static final Logger logger = LogManager.getLogger(SaveAnalysisDialog.class);
    private final JEVisDataSource ds;
    private final ObjectRelations objectRelations;
    private final AnalysesComboBox analysesComboBox;
    private JEVisObject currentAnalysisDirectory = null;
    private Response response = Response.CANCEL;

    public SaveAnalysisDialog(JEVisDataSource ds, DataSettings dataSettings, ChartPlugin chartPlugin, ToolBarView toolBarView) {
        setTitle(I18n.getInstance().getString("plugin.graph.saveanalysis.title"));
        setHeaderText(I18n.getInstance().getString("plugin.graph.saveanalysis.header"));
        setResizable(true);
        initOwner(ControlCenter.getStage());
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        this.ds = ds;
        this.objectRelations = new ObjectRelations(ds);
        this.analysesComboBox = toolBarView.getAnalysesComboBox();

        Label newText = new Label(I18n.getInstance().getString("plugin.graph.dialog.new.name"));
        Label directoryText = new Label(I18n.getInstance().getString("plugin.graph.dialog.new.directory"));
        TextField name = new TextField();

        JEVisClass analysesDirectory = null;
        List<JEVisObject> listAnalysesDirectories = null;
        try {
            analysesDirectory = ds.getJEVisClass("Analyses Directory");
            listAnalysesDirectories = ds.getObjects(analysesDirectory, false);
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        ComboBox<JEVisObject> parentsDirectories = new ComboBox<>(FXCollections.observableArrayList(listAnalysesDirectories));

        //TODO JFX17
        parentsDirectories.setConverter(new StringConverter<JEVisObject>() {
            @Override
            public String toString(JEVisObject object) {
                String text = "";
                if (object != null) {
                    if (!ChartTools.isMultiSite(ds) && !ChartTools.isMultiDir(ds, object))
                        text = object.getName();
                    else {
                        String prefix = "";
                        if (ChartTools.isMultiSite(ds)) {
                            prefix += objectRelations.getObjectPath(object);
                        }
                        if (ChartTools.isMultiDir(ds, object)) {
                            prefix += objectRelations.getRelativePath(object);
                        }

                        text = prefix + object.getName();
                    }
                }

                return text;
            }

            @Override
            public JEVisObject fromString(String string) {
                JEVisObject returnObject = null;

                for (JEVisObject jeVisObject : parentsDirectories.getItems()) {
                    String text = "";
                    if (!ChartTools.isMultiSite(ds) && !ChartTools.isMultiDir(ds, jeVisObject))
                        text = jeVisObject.getName();
                    else {
                        String prefix = "";
                        if (ChartTools.isMultiSite(ds)) {
                            prefix += objectRelations.getObjectPath(jeVisObject);
                        }
                        if (ChartTools.isMultiDir(ds, jeVisObject)) {
                            prefix += objectRelations.getRelativePath(jeVisObject);
                        }

                        text = prefix + jeVisObject.getName();
                    }

                    if (text.equals(string)) {
                        returnObject = jeVisObject;
                        break;
                    }
                }

                return returnObject;
            }
        });

        parentsDirectories.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                currentAnalysisDirectory = newValue;
            }
        });

        parentsDirectories.getSelectionModel().selectFirst();
        if (dataSettings.getCurrentAnalysis() != null) {
            try {
                if (dataSettings.getCurrentAnalysis().getParents() != null && !dataSettings.getCurrentAnalysis().getParents().isEmpty()) {
                    parentsDirectories.getSelectionModel().select(dataSettings.getCurrentAnalysis().getParents().get(0));
                }
            } catch (JEVisException e) {
                logger.error("Couldn't select current Analysis Directory: " + e);
            }
        }

        if (dataSettings.getCurrentAnalysis() != null && dataSettings.getCurrentAnalysis().getName() != null && !dataSettings.getCurrentAnalysis().getName().equals(""))
            name.setText(dataSettings.getCurrentAnalysis().getName());

        name.focusedProperty().addListener((ov, t, t1) -> Platform.runLater(() -> {
            if (name.isFocused() && !name.getText().isEmpty()) {
                name.selectAll();
            }
        }));

        ButtonType okType = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        this.getDialogPane().getButtonTypes().addAll(cancelType, okType);

        Button okButton = (Button) this.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);

        Button cancelButton = (Button) this.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(event -> close());

        GridPane gridLayout = new GridPane();
        gridLayout.setPadding(new Insets(10, 10, 10, 10));
        gridLayout.setVgap(10);

        gridLayout.add(directoryText, 0, 0);
        gridLayout.add(parentsDirectories, 0, 1, 2, 1);
        GridPane.setFillWidth(parentsDirectories, true);
        parentsDirectories.setMinWidth(200);

        gridLayout.add(newText, 0, 2);
        gridLayout.add(name, 0, 3, 2, 1);
        GridPane.setFillWidth(name, true);
        name.setMinWidth(350);

        getDialogPane().setContent(gridLayout);

        okButton.setOnAction(event -> {
            List<String> check = new ArrayList<>();
            AtomicReference<JEVisObject> currentAnalysis = new AtomicReference<>();
            try {
                currentAnalysisDirectory.getChildren().forEach(jeVisObject -> {
                    if (!check.contains(jeVisObject.getName())) {
                        check.add(jeVisObject.getName());
                    }
                });
                currentAnalysisDirectory.getChildren().forEach(jeVisObject -> {
                    if (jeVisObject.getName().equals(name.getText())) currentAnalysis.set(jeVisObject);
                });
            } catch (JEVisException e) {
                logger.error("Error in current analysis directory: " + e);
            }
            if (!check.contains(name.getText())) {
                JEVisObject newAnalysisObject = null;
                boolean createOk = true;
                try {
                    createOk = ds.getCurrentUser().canCreate(currentAnalysisDirectory.getID());
                    if (!createOk) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, I18n.getInstance().getString("plugin.object.permission.create.denied"));
                        alert.initOwner(ControlCenter.getStage());
                        alert.initModality(Modality.APPLICATION_MODAL);
                        Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
                        TopMenu.applyActiveTheme(stageAlert.getScene());
                        stageAlert.setAlwaysOnTop(true);
                        alert.showAndWait();
                        this.close();
                    }

                    if (createOk) {
                        JEVisClass classAnalysis = ds.getJEVisClass("Analysis");
                        newAnalysisObject = currentAnalysisDirectory.buildObject(name.getText(), classAnalysis);
                        newAnalysisObject.commit();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (createOk && newAnalysisObject != null) {
                    chartPlugin.getAnalysisHandler().saveDataModel(newAnalysisObject, chartPlugin.getDataModel());
                    toolBarView.setChanged(false);

                    analysesComboBox.updateListAnalyses();
                    dataSettings.setCurrentAnalysis(newAnalysisObject);

                    toolBarView.updateLayout();

                    response = Response.OK;
                }
            } else {
                Alert dialogOverwrite = new Alert(Alert.AlertType.CONFIRMATION);
                dialogOverwrite.setResizable(true);
                dialogOverwrite.setTitle(I18n.getInstance().getString("plugin.graph.dialog.overwrite.title"));
                dialogOverwrite.initOwner(ControlCenter.getStage());
                dialogOverwrite.initModality(Modality.APPLICATION_MODAL);
                Stage stageOverwrite = (Stage) dialogOverwrite.getDialogPane().getScene().getWindow();
                TopMenu.applyActiveTheme(stageOverwrite.getScene());
                stageOverwrite.setAlwaysOnTop(true);

                Label message = new Label(I18n.getInstance().getString("plugin.graph.dialog.overwrite.message"));

                ButtonType okOverwriteType = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.overwrite.ok"), ButtonBar.ButtonData.OK_DONE);
                ButtonType cancelOverwriteType = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.overwrite.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

                dialogOverwrite.getDialogPane().getButtonTypes().setAll(cancelOverwriteType, okOverwriteType);

                Button okOverwriteButton = (Button) dialogOverwrite.getDialogPane().lookupButton(okOverwriteType);
                okOverwriteButton.setDefaultButton(true);

                Button cancelOverwriteButton = (Button) dialogOverwrite.getDialogPane().lookupButton(cancelOverwriteType);
                cancelOverwriteButton.setCancelButton(true);
                cancelOverwriteButton.setOnAction(event2 -> dialogOverwrite.close());

                dialogOverwrite.getDialogPane().setContent(message);

                okOverwriteButton.setOnAction(event1 -> {
                    if (currentAnalysis.get() != null) {
                        chartPlugin.getAnalysisHandler().saveDataModel(currentAnalysis.get(), chartPlugin.getDataModel());
                        toolBarView.setChanged(false);

                        analysesComboBox.updateListAnalyses();
                        dataSettings.setCurrentAnalysis(currentAnalysis.get());
                        toolBarView.updateLayout();

                        response = Response.OK;
                    }
                    dialogOverwrite.close();
                });

                boolean writeOk = true;
                try {
                    writeOk = ds.getCurrentUser().canWrite(currentAnalysis.get().getID());
                    if (!writeOk) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, I18n.getInstance().getString("plugin.object.permission.write.denied"));
                        alert.initOwner(ControlCenter.getStage());
                        alert.initModality(Modality.APPLICATION_MODAL);
                        Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
                        TopMenu.applyActiveTheme(stageAlert.getScene());
                        stageAlert.setAlwaysOnTop(true);
                        alert.showAndWait();
                        this.close();
                    }
                } catch (Exception e) {
                    logger.error("Could not do write check", e);
                }

                if (writeOk) {
                    dialogOverwrite.show();
                }
            }
            close();
        });
    }

    public Response getResponse() {
        return response;
    }
}
