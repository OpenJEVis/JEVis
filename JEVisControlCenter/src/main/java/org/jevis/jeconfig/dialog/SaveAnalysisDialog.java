package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.*;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.PickerCombo;
import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.Chart.ChartSettings;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.plugin.charts.ToolBarView;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class SaveAnalysisDialog extends JFXDialog {
    private static final Logger logger = LogManager.getLogger(SaveAnalysisDialog.class);
    private final JEVisDataSource ds;
    private final AnalysisDataModel model;
    private final ObjectRelations objectRelations;
    private final ToolBarView toolBarView;
    private final PickerCombo pickerCombo;
    private final JFXComboBox<JEVisObject> listAnalysesComboBox;
    private Boolean changed;
    private JEVisObject currentAnalysisDirectory = null;

    public SaveAnalysisDialog(StackPane dialogContainer, JEVisDataSource ds, AnalysisDataModel model, ToolBarView toolBarView) {
        setDialogContainer(dialogContainer);
        setTransitionType(DialogTransition.NONE);

        this.ds = ds;
        this.model = model;
        this.toolBarView = toolBarView;
        this.objectRelations = new ObjectRelations(ds);
        this.pickerCombo = toolBarView.getPickerCombo();
        this.listAnalysesComboBox = toolBarView.getListAnalysesComboBox();
        this.changed = toolBarView.getChanged();

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
                            if (!model.isMultiDir() && !model.isMultiSite())
                                setText(obj.getName());
                            else {
                                String prefix = "";
                                if (model.isMultiSite()) {
                                    prefix += objectRelations.getObjectPath(obj);
                                }
                                if (model.isMultiDir()) {
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
        if (model.getCurrentAnalysis() != null) {
            try {
                if (model.getCurrentAnalysis().getParents() != null && !model.getCurrentAnalysis().getParents().isEmpty()) {
                    parentsDirectories.getSelectionModel().select(model.getCurrentAnalysis().getParents().get(0));
                }
            } catch (JEVisException e) {
                logger.error("Couldn't select current Analysis Directory: " + e);
            }
        }

        if (model.getCurrentAnalysis() != null && model.getCurrentAnalysis().getName() != null && !model.getCurrentAnalysis().getName().equals(""))
            name.setText(model.getCurrentAnalysis().getName());

        name.focusedProperty().addListener((ov, t, t1) -> Platform.runLater(() -> {
            if (name.isFocused() && !name.getText().isEmpty()) {
                name.selectAll();
            }
        }));

        final JFXButton ok = new JFXButton(I18n.getInstance().getString("plugin.graph.dialog.new.ok"));
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
        name.setMinWidth(200);

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
                    saveDataModel(newAnalysisObject, model.getSelectedData(), model.getCharts());

                    model.setCurrentAnalysisNOEVENT(newAnalysisObject);
                    model.updateListAnalyses();
                    model.isGlobalAnalysisTimeFrame(true);
                    toolBarView.updateLayout();
                }
            } else {
                JFXAlert dialogOverwrite = new JFXAlert(this.getScene().getWindow());
                dialogOverwrite.setResizable(true);
                dialogOverwrite.setTitle(I18n.getInstance().getString("plugin.graph.dialog.overwrite.title"));
                Label message = new Label(I18n.getInstance().getString("plugin.graph.dialog.overwrite.message"));
                final JFXButton overwrite_ok = new JFXButton(I18n.getInstance().getString("plugin.graph.dialog.overwrite.ok"));
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
                        saveDataModel(currentAnalysis.get(), model.getSelectedData(), model.getCharts());

                        model.setCurrentAnalysisNOEVENT(currentAnalysis.get());
                        model.updateListAnalyses();
                        toolBarView.updateLayout();
                    }
                    dialogOverwrite.close();
                });

                dialogOverwrite.showAndWait();
            }
            close();
        });
    }

    public static JsonChartSettings getJsonChartSettings(ChartSettings chartSettings) {
        JsonChartSettings jsonChartSettings = new JsonChartSettings();

        if (chartSettings.getAutoSize() != null) {
            jsonChartSettings.setAutoSize(chartSettings.getAutoSize().toString());
        }

        for (ChartSetting cset : chartSettings.getListSettings()) {
            JsonChartSetting set = new JsonChartSetting();
            if (cset.getId() != null) set.setId(cset.getId().toString());
            if (cset.getName() != null) set.setName(cset.getName());
            if (cset.getChartType() != null) set.setChartType(cset.getChartType().toString());
            if (cset.getColorMapping() != null) set.setColorMapping(cset.getColorMapping().toString());
            if (cset.getOrientation() != null) set.setOrientation(cset.getOrientation().toString());
            if (cset.getGroupingInterval() != null) set.setGroupingInterval(cset.getGroupingInterval().toString());
            if (cset.getHeight() != null) set.setHeight(cset.getHeight().toString());

            JsonChartTimeFrame jctf = new JsonChartTimeFrame();
            if (cset.getAnalysisTimeFrame().getTimeFrame() != null) {
                jctf.setTimeframe(cset.getAnalysisTimeFrame().getTimeFrame().toString());
            }
            jctf.setId(String.valueOf(cset.getAnalysisTimeFrame().getId()));

            set.setAnalysisTimeFrame(jctf);

            jsonChartSettings.getListSettings().add(set);
        }
        return jsonChartSettings;
    }

    public static JsonChartDataModel getJsonChartDataModel(Set<ChartDataRow> selectedData) {
        JsonChartDataModel jsonChartDataModel = new JsonChartDataModel();
        List<JsonAnalysisDataRow> jsonDataModels = new ArrayList<>();
        for (ChartDataRow mdl : selectedData) {
            if (!mdl.getSelectedcharts().isEmpty()) {
                JsonAnalysisDataRow json = new JsonAnalysisDataRow();
                json.setName(mdl.getTitle());
                json.setColor(mdl.getColor());
                json.setObject(mdl.getObject().getID().toString());
                if (mdl.getDataProcessor() != null)
                    json.setDataProcessorObject(mdl.getDataProcessor().getID().toString());
                json.setAggregation(mdl.getAggregationPeriod().toString());
                json.setUnit(mdl.getUnit().toJSON());
                json.setSelectedCharts(listToString(mdl.getSelectedcharts()));
                json.setAxis(mdl.getAxis().toString());
                json.setIsEnPI(mdl.getEnPI().toString());
                if (mdl.getCalculationObject() != null)
                    json.setCalculation(mdl.getCalculationObject().getID().toString());
                if (mdl.getBubbleType() != null) {
                    json.setBubbleType(mdl.getBubbleType().toString());
                }

                if (mdl.getChartType() != null) {
                    json.setChartType(mdl.getChartType().toString());
                }

                jsonDataModels.add(json);
            }
        }
        jsonChartDataModel.setListDataRows(jsonDataModels);
        return jsonChartDataModel;
    }

    public static String listToString(List<Integer> listString) {
        if (listString != null) {
            StringBuilder sb = new StringBuilder();
            if (listString.size() > 1) {
                for (Integer i : listString) {
                    int index = listString.indexOf(i);
                    sb.append(i.toString());
                    if (index < listString.size() - 1) sb.append(", ");
                }
            } else if (listString.size() == 1) sb.append(listString.get(0));
            return sb.toString();
        } else return "";
    }

    private void saveDataModel(JEVisObject analysis, Set<ChartDataRow> selectedData, ChartSettings chartSettings) {
        try {
            JEVisAttribute dataModel = analysis.getAttribute("Data Model");
            JEVisAttribute charts = analysis.getAttribute("Charts");

            JEVisAttribute noOfChartsPerScreenAttribute = analysis.getAttribute(AnalysisDataModel.NUMBER_OF_CHARTS_PER_SCREEN_ATTRIBUTE_NAME);
            Long noOfChartsPerScreen = model.getChartsPerScreen();

            JEVisAttribute horizontalPiesAttribute = analysis.getAttribute(AnalysisDataModel.NUMBER_OF_HORIZONTAL_PIES_ATTRIBUTE_NAME);
            Long horizontalPies = model.getHorizontalPies();

            JEVisAttribute horizontalTablesAttribute = analysis.getAttribute(AnalysisDataModel.NUMBER_OF_HORIZONTAL_TABLES_ATTRIBUTE_NAME);
            Long horizontalTables = model.getHorizontalTables();

            JsonChartDataModel jsonChartDataModel = getJsonChartDataModel(selectedData);

            JsonChartSettings jsonChartSettings = getJsonChartSettings(chartSettings);

            if (jsonChartDataModel.toString().length() < 16635 && jsonChartSettings.toString().length() < 16635) {
                DateTime now = DateTime.now();
                String dataModelString = jsonChartDataModel.toString();
                JEVisSample smp = dataModel.buildSample(now, dataModelString);
                JEVisSample smp2 = charts.buildSample(now, jsonChartSettings.toString());
                smp.commit();
                smp2.commit();

                if (noOfChartsPerScreen != null && !noOfChartsPerScreen.equals(0L)) {
                    JEVisSample smp3 = noOfChartsPerScreenAttribute.buildSample(now, noOfChartsPerScreen);
                    smp3.commit();
                }

                if (horizontalPies != null && !horizontalPies.equals(0L)) {
                    JEVisSample smp4 = horizontalPiesAttribute.buildSample(now, horizontalPies);
                    smp4.commit();
                }

                if (horizontalTables != null && !horizontalTables.equals(0L)) {
                    JEVisSample smp5 = horizontalTablesAttribute.buildSample(now, horizontalTables);
                    smp5.commit();
                }

                this.changed = false;
                this.toolBarView.setChanged(false);

                if (this.model.getTemporary()) {
                    try {
                        ds.deleteObject(this.model.getCurrentAnalysis().getID());
                        this.model.updateListAnalyses();
                    } catch (JEVisException e) {
                        logger.error("Could not delete temporary analysis", e);
                    }
                }

                this.model.setTemporary(false);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, I18n.getInstance().getString("plugin.graph.alert.toolong"));
                alert.setResizable(true);
                alert.showAndWait();
            }

        } catch (JEVisException e) {
            logger.error("Error: could not save data model and chart settings", e);
        }
    }
}
