package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.AnalysesComboBox;
import org.jevis.jeconfig.application.Chart.ChartTools;
import org.jevis.jeconfig.plugin.charts.ChartPlugin;
import org.jevis.jeconfig.plugin.charts.DataSettings;
import org.jevis.jeconfig.plugin.charts.ToolBarView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SaveAnalysisDialog extends JFXDialog {
    private static final Logger logger = LogManager.getLogger(SaveAnalysisDialog.class);
    private final JEVisDataSource ds;
    private final ObjectRelations objectRelations;
    private final AnalysesComboBox analysesComboBox;
    private JEVisObject currentAnalysisDirectory = null;
    private Response response = Response.CANCEL;

    public SaveAnalysisDialog(StackPane dialogContainer, JEVisDataSource ds, DataSettings dataSettings, ChartPlugin chartPlugin, ToolBarView toolBarView) {
        setDialogContainer(dialogContainer);
        setTransitionType(DialogTransition.NONE);

        this.ds = ds;
        this.objectRelations = new ObjectRelations(ds);
        this.analysesComboBox = toolBarView.getAnalysesComboBox();

        Label newText = new Label(I18n.getInstance().getString("plugin.graph.dialog.new.name"));
        Label directoryText = new Label(I18n.getInstance().getString("plugin.graph.dialog.new.directory"));
        JFXTextField name = new JFXTextField();

        JEVisClass analysesDirectory = null;
        List<JEVisObject> listAnalysesDirectories = null;
        try {
            analysesDirectory = ds.getJEVisClass("Analyses Directory");
            listAnalysesDirectories = ds.getObjects(analysesDirectory, false);
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        JFXComboBox<JEVisObject> parentsDirectories = new JFXComboBox<>(FXCollections.observableArrayList(listAnalysesDirectories));

        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> cellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                return new ListCell<JEVisObject>() {
                    @Override
                    protected void updateItem(JEVisObject obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (empty || obj == null || obj.getName() == null) {
                            setText("");
                        } else {
                            if (!ChartTools.isMultiSite(ds) && !ChartTools.isMultiDir(ds))
                                setText(obj.getName());
                            else {
                                String prefix = "";
                                if (ChartTools.isMultiSite(ds)) {
                                    prefix += objectRelations.getObjectPath(obj);
                                }
                                if (ChartTools.isMultiDir(ds)) {
                                    prefix += objectRelations.getRelativePath(obj);
                                }

                                setText(prefix + obj.getName());
                            }
                        }

                    }
                };
            }
        };
        parentsDirectories.setCellFactory(cellFactory);
        parentsDirectories.setButtonCell(cellFactory.call(null));

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

        final JFXButton ok = new JFXButton(I18n.getInstance().getString("plugin.graph.dialog.new.ok"));
        ok.setDefaultButton(true);
        final JFXButton cancel = new JFXButton(I18n.getInstance().getString("plugin.graph.dialog.new.cancel"));
        cancel.setOnAction(event -> close());

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

        HBox buttonBar = new HBox(6, cancel, ok);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(12));

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(8, 0, 8, 0));

        VBox vBox = new VBox(6, gridLayout, separator, buttonBar);
        setContent(vBox);

        ok.setOnAction(event -> {
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
                try {
                    JEVisClass classAnalysis = ds.getJEVisClass("Analysis");
                    newAnalysisObject = currentAnalysisDirectory.buildObject(name.getText(), classAnalysis);
                    newAnalysisObject.commit();

                } catch (JEVisException e) {
                    e.printStackTrace();
                }
                if (newAnalysisObject != null) {
                    chartPlugin.getAnalysisHandler().saveDataModel(newAnalysisObject, chartPlugin.getDataModel(), toolBarView.getToolBarSettings(), dataSettings);
                    toolBarView.setChanged(false);

                    dataSettings.setCurrentAnalysis(newAnalysisObject);
                    analysesComboBox.updateListAnalyses();

                    toolBarView.updateLayout();

                    response = Response.OK;
                }
            } else {
                JFXAlert dialogOverwrite = new JFXAlert(this.getScene().getWindow());
                dialogOverwrite.setResizable(true);
                dialogOverwrite.setTitle(I18n.getInstance().getString("plugin.graph.dialog.overwrite.title"));
                Label message = new Label(I18n.getInstance().getString("plugin.graph.dialog.overwrite.message"));
                final JFXButton overwrite_ok = new JFXButton(I18n.getInstance().getString("plugin.graph.dialog.overwrite.ok"));
                overwrite_ok.setDefaultButton(true);
                final JFXButton overwrite_cancel = new JFXButton(I18n.getInstance().getString("plugin.graph.dialog.overwrite.cancel"));
                overwrite_cancel.setOnAction(event1 -> dialogOverwrite.close());

                HBox buttonBox = new HBox(6, overwrite_cancel, overwrite_ok);
                buttonBox.setAlignment(Pos.CENTER_RIGHT);

                Separator separator2 = new Separator(Orientation.HORIZONTAL);
                separator2.setPadding(new Insets(8, 0, 8, 0));

                VBox vBox1 = new VBox(6, message, separator2, buttonBox);
                vBox1.setPadding(new Insets(12));

                dialogOverwrite.setContent(vBox1);

                overwrite_ok.setOnAction(event1 -> {
                    if (currentAnalysis.get() != null) {
                        chartPlugin.getAnalysisHandler().saveDataModel(currentAnalysis.get(), chartPlugin.getDataModel(), toolBarView.getToolBarSettings(), dataSettings);
                        toolBarView.setChanged(false);

                        dataSettings.setCurrentAnalysis(currentAnalysis.get());
                        analysesComboBox.updateListAnalyses();
                        toolBarView.updateLayout();

                        response = Response.OK;
                    }
                    dialogOverwrite.close();
                });

                dialogOverwrite.showAndWait();
            }
            close();
        });
    }

    public Response getResponse() {
        return response;
    }
}
