package org.jevis.jeconfig.dialog;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.json.JsonAnalysisDataRow;
import org.jevis.commons.json.JsonChartDataModel;
import org.jevis.commons.json.JsonChartSettings;
import org.jevis.commons.json.JsonChartTimeFrame;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.PickerCombo;
import org.jevis.jeconfig.application.Chart.ChartSettings;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class SaveAnalysisDialog {
    private static final Logger logger = LogManager.getLogger(SaveAnalysisDialog.class);
    private final JEVisDataSource ds;
    private final AnalysisDataModel model;
    private final ObjectRelations objectRelations;
    private PickerCombo pickerCombo;
    private ComboBox<JEVisObject> listAnalysesComboBox;
    private Boolean changed;
    private JEVisObject currentAnalysisDirectory = null;

    public SaveAnalysisDialog(JEVisDataSource ds, AnalysisDataModel model, PickerCombo pickerCombo, ComboBox<JEVisObject> listAnalysesComboBox, Boolean changed) {
        this.ds = ds;
        this.model = model;
        this.objectRelations = new ObjectRelations(ds);
        this.pickerCombo = pickerCombo;
        this.listAnalysesComboBox = listAnalysesComboBox;
        this.changed = changed;

        saveCurrentAnalysis();
    }

    private void saveCurrentAnalysis() {

        Dialog<ButtonType> newAnalysis = new Dialog<>();
        newAnalysis.setTitle(I18n.getInstance().getString("plugin.graph.dialog.new.title"));
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
                            if (!model.getMultipleDirectories())
                                setText(obj.getName());
                            else {
                                String prefix = objectRelations.getObjectPath(obj);

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

        final ButtonType ok = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.new.ok"), ButtonBar.ButtonData.OK_DONE);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.new.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

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

        newAnalysis.getDialogPane().setContent(gridLayout);
        newAnalysis.getDialogPane().getButtonTypes().addAll(ok, cancel);
        newAnalysis.getDialogPane().setPrefWidth(450d);
        newAnalysis.initOwner(JEConfig.getStage());

        newAnalysis.showAndWait()
                .ifPresent(response -> {
                    if (response.getButtonData().getTypeCode() == ButtonType.OK.getButtonData().getTypeCode()) {
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
                            saveDataModel(newAnalysisObject, model.getSelectedData(), model.getCharts());

                            model.setCurrentAnalysis(newAnalysisObject);
                            pickerCombo.updateCellFactory();
                            model.updateListAnalyses();
                            model.isGlobalAnalysisTimeFrame(true);
                            listAnalysesComboBox.getSelectionModel().select(model.getCurrentAnalysis());
                        } else {

                            Dialog<ButtonType> dialogOverwrite = new Dialog<>();
                            dialogOverwrite.setResizable(true);
                            dialogOverwrite.setTitle(I18n.getInstance().getString("plugin.graph.dialog.overwrite.title"));
                            dialogOverwrite.getDialogPane().setContentText(I18n.getInstance().getString("plugin.graph.dialog.overwrite.message"));
                            final ButtonType overwrite_ok = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.overwrite.ok"), ButtonBar.ButtonData.OK_DONE);
                            final ButtonType overwrite_cancel = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.overwrite.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

                            dialogOverwrite.getDialogPane().getButtonTypes().addAll(overwrite_ok, overwrite_cancel);

                            dialogOverwrite.showAndWait().ifPresent(overwrite_response -> {
                                if (overwrite_response.getButtonData().getTypeCode() == ButtonType.OK.getButtonData().getTypeCode()) {
                                    if (currentAnalysis.get() != null) {
                                        saveDataModel(currentAnalysis.get(), model.getSelectedData(), model.getCharts());

                                        model.updateListAnalyses();
                                        model.setCurrentAnalysis(currentAnalysis.get());
                                        pickerCombo.updateCellFactory();
                                        listAnalysesComboBox.getSelectionModel().select(model.getCurrentAnalysis());
                                    }
                                } else {

                                }
                            });

                        }

                    }
                });
    }

    private void saveDataModel(JEVisObject analysis, Set<ChartDataModel> selectedData, List<ChartSettings> chartSettings) {
        try {
            JEVisAttribute dataModel = analysis.getAttribute("Data Model");
            JEVisAttribute charts = analysis.getAttribute("Charts");

            JEVisAttribute noOfChartsPerScreenAttribute = analysis.getAttribute(AnalysisDataModel.NUMBER_OF_CHARTS_PER_SCREEN_ATTRIBUTE_NAME);
            Long noOfChartsPerScreen = model.getChartsPerScreen();

            JEVisAttribute horizontalPiesAttribute = analysis.getAttribute(AnalysisDataModel.NUMBER_OF_HORIZONTAL_PIES_ATTRIBUTE_NAME);
            Long horizontalPies = model.getHorizontalPies();

            JEVisAttribute horizontalTablesAttribute = analysis.getAttribute(AnalysisDataModel.NUMBER_OF_HORIZONTAL_TABLES_ATTRIBUTE_NAME);
            Long horizontalTables = model.getHorizontalTables();

            JsonChartDataModel jsonChartDataModel = new JsonChartDataModel();
            List<JsonAnalysisDataRow> jsonDataModels = new ArrayList<>();
            for (ChartDataModel mdl : selectedData) {
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
                    jsonDataModels.add(json);
                }
            }
            jsonChartDataModel.setListDataRows(jsonDataModels);

            List<JsonChartSettings> jsonChartSettings = new ArrayList<>();
            for (ChartSettings cset : chartSettings) {
                JsonChartSettings set = new JsonChartSettings();
                if (cset.getId() != null) set.setId(cset.getId().toString());
                set.setName(cset.getName());
                set.setChartType(cset.getChartType().toString());
                set.setHeight(cset.getHeight().toString());

                JsonChartTimeFrame jctf = new JsonChartTimeFrame();
                jctf.setTimeframe(cset.getAnalysisTimeFrame().getTimeFrame().toString());
                jctf.setId(String.valueOf(cset.getAnalysisTimeFrame().getId()));

                set.setAnalysisTimeFrame(jctf);

                jsonChartSettings.add(set);
            }

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

                changed = false;
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

    private String listToString(List<Integer> listString) {
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
}
