package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.datetime.CustomPeriodObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.AggregationPeriodBox;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.PickerCombo;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.PresetDateBox;
import org.jevis.jeconfig.application.Chart.ChartTools;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.application.tools.DisabledItemsComboBox;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.plugin.charts.ChartPlugin;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import static org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.AnalysesComboBox.ANALYSIS_CLASS_NAME;
import static org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.AnalysesComboBox.USER_CLASS_NAME;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */
public class LoadAnalysisDialog extends Dialog {
    private static final Logger logger = LogManager.getLogger(LoadAnalysisDialog.class);
    private final ObjectRelations objectRelations;
    private final ChartPlugin chartPlugin;
    private final JFXTextField filterInput = new JFXTextField();
    private final FilteredList<JEVisObject> filteredData;
    private final JFXListView<JEVisObject> analysisListView;
    private final JEVisDataSource ds;
    private final AggregationPeriodBox aggregationBox = new AggregationPeriodBox(AggregationPeriod.NONE);
    private final DisabledItemsComboBox<ManipulationMode> mathBox = getMathBox();
    private final Label startText = new Label(I18n.getInstance().getString("plugin.graph.changedate.startdate") + "  ");
    /**
     * drawOptimization.setOnAction(event -> {
     * HiddenConfig.CHART_PRECISION_ON = drawOptimization.isSelected();
     * });
     **/
    private final Tooltip pickerDateStartTT = new Tooltip(I18n.getInstance().getString("plugin.graph.manipulation.tip.datestart"));
    //pickerTimeStart.setTooltip(new  Tooltip(I18n.getInstance().getString("plugin.graph.manipulation.tip.timestart")));
    private final Tooltip pickerDateEndTT = new Tooltip(I18n.getInstance().getString("plugin.graph.manipulation.tip.dateend"));
    //pickerTimeEnd.setTooltip(new  Tooltip(I18n.getInstance().getString("plugin.graph.manipulation.tip.timeend")));
    private final Tooltip analysisListViewTT = new Tooltip(I18n.getInstance().getString("plugin.graph.manipulation.tip.analysislist"));
    private final Tooltip aggregationTT = new Tooltip(I18n.getInstance().getString("plugin.graph.manipulation.tip.aggregation"));
    private final Tooltip mathBoxTT = new Tooltip(I18n.getInstance().getString("plugin.graph.manipulation.tip.mathBox"));
    private final Tooltip presetDateBoxTT = new Tooltip(I18n.getInstance().getString("plugin.graph.manipulation.tip.presetdate"));
    private final Tooltip customPeriodsComboBoxTT = new Tooltip(I18n.getInstance().getString("plugin.graph.manipulation.tip.customdate"));
    private final Label endText = new Label(I18n.getInstance().getString("plugin.graph.changedate.enddate"));
    private final Label standardSelectionsLabel = new Label(I18n.getInstance().getString("plugin.graph.analysis.label.standard"));
    private final Label customSelectionsLabel = new Label(I18n.getInstance().getString("plugin.graph.analysis.label.custom"));
    private final Label labelAggregation = new Label(I18n.getInstance().getString("plugin.graph.interval.label"));
    private final Label labelMath = new Label(I18n.getInstance().getString("plugin.graph.manipulation.label"));
    private final Label timeRange = new Label(I18n.getInstance().getString("plugin.graph.analysis.label.timerange"));
    private final Preferences previewPref = Preferences.userRoot().node("JEVis.JEConfig.preview");
    private final ButtonType loadType = new ButtonType(I18n.getInstance().getString("plugin.graph.analysis.load"), ButtonBar.ButtonData.OK_DONE);
    private final ButtonType cancelType = new ButtonType(I18n.getInstance().getString("plugin.graph.changedate.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
    private final ButtonType newType = new ButtonType(I18n.getInstance().getString("plugin.graph.analysis.new"), ButtonBar.ButtonData.NEXT_FORWARD);
    private Response response = Response.CANCEL;
    private PickerCombo pickerCombo;
    private JFXDatePicker pickerDateStart;
    private JFXTimePicker pickerTimeStart;
    private JFXDatePicker pickerDateEnd;
    private JFXTimePicker pickerTimeEnd;
    private PresetDateBox presetDateBox;
    private List<CustomPeriodObject> finalListCustomPeriodObjects;
    private JFXComboBox<String> comboBoxCustomPeriods = null;

    public LoadAnalysisDialog(ChartPlugin chartPlugin, JEVisDataSource ds, ObservableList<JEVisObject> analyses) {
        this.ds = ds;
        this.objectRelations = new ObjectRelations(ds);
        this.chartPlugin = chartPlugin;

        setTitle(I18n.getInstance().getString("plugin.graph.loadanalyisdialog.title"));
        setHeaderText(I18n.getInstance().getString("plugin.graph.loadanalyisdialog.header"));
        setResizable(true);
        initOwner(JEConfig.getStage());
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        filteredData = new FilteredList<>(analyses, s -> true);

        filterInput.textProperty().addListener(obs -> {
            String filter = filterInput.getText();
            if (filter == null || filter.isEmpty()) {
                filteredData.setPredicate(s -> true);
            } else {
                if (filter.contains(" ")) {
                    String[] result = filter.split(" ");
                    filteredData.setPredicate(s -> {
                        boolean match = false;
                        String string = (objectRelations.getObjectPath(s) + s.getName()).toLowerCase();
                        for (String value : result) {
                            String subString = value.toLowerCase();
                            if (!string.contains(subString))
                                return false;
                            else match = true;
                        }
                        return match;
                    });
                } else {
                    filteredData.setPredicate(s -> (objectRelations.getObjectPath(s) + s.getName()).toLowerCase().contains(filter.toLowerCase()));
                }
            }
        });
        KeyCombination help = new KeyCodeCombination(KeyCode.F1);
        filterInput.setOnKeyPressed(event -> {
            if (help.match(event)) {
                JEVisHelp.getInstance().toggleHelp();
                event.consume();
            }
        });

        analysisListView = new JFXListView<>();
        analysisListView.setItems(filteredData);
        analysisListView.getSelectionModel().selectedIndexProperty().addListener(
                (observable, oldValue, newValue) ->
                        Platform.runLater(() -> {
                            Button loadButton = (Button) this.getDialogPane().lookupButton(loadType);
                            loadButton.setDisable(analysisListView.getSelectionModel().getSelectedItem() == null);
                            analysisListView.scrollTo(analysisListView.getSelectionModel().getSelectedIndex());
                        }));

        analysisListView.setCellFactory(param -> new ListCell<JEVisObject>() {

            @Override
            protected void updateItem(JEVisObject obj, boolean empty) {
                super.updateItem(obj, empty);
                if (empty || obj == null || obj.getName() == null) {
                    setText("");
                } else {
                    String name = "";
                    try {
                        if (obj.getJEVisClassName().equals(ANALYSIS_CLASS_NAME)) {
                            if (!ChartTools.isMultiSite(ds) && !ChartTools.isMultiDir(ds, obj))
                                name = obj.getName();
                            else {
                                String prefix = "";
                                if (ChartTools.isMultiSite(ds))
                                    prefix += objectRelations.getObjectPath(obj);
                                if (ChartTools.isMultiDir(ds, obj)) {
                                    prefix += objectRelations.getRelativePath(obj);
                                }

                                name = prefix + obj.getName();
                            }
                        } else {
                            if (obj.getJEVisClassName().equals(USER_CLASS_NAME)) {
                                name = I18n.getInstance().getString("plugin.graph.analysis.tempanalysis");
                            }
                        }
                    } catch (Exception e) {
                        logger.error("could not get JEVisClassName", e);
                    }
                    setText(name);
                }
            }
        });

        if (chartPlugin.getDataSettings().getCurrentAnalysis() != null && chartPlugin.getDataSettings().getCurrentAnalysis().getName() != null
                && !chartPlugin.getDataSettings().getCurrentAnalysis().getName().isEmpty())
            analysisListView.getSelectionModel().select(chartPlugin.getDataSettings().getCurrentAnalysis());

        initializeControls();

        updateGridLayout(true);

        JEVisHelp.getInstance().setActiveSubModule(this.getClass().getSimpleName());
        JEVisHelp.getInstance().update();

    }
    //private JFXCheckBox drawOptimization;

    private void initializeControls() {
        comboBoxCustomPeriods = getCustomPeriodsComboBox();

        aggregationBox.getSelectionModel().select(AggregationPeriod.parseAggregationIndex(chartPlugin.getDataSettings().getAggregationPeriod()));
        aggregationBox.setMaxWidth(200);

        comboBoxCustomPeriods.setMaxWidth(200);
        if (chartPlugin.getDataSettings().getAnalysisTimeFrame().getTimeFrame().equals(TimeFrame.CUSTOM_START_END)) {
            for (CustomPeriodObject cpo : finalListCustomPeriodObjects) {
                if (cpo.getObject().getID().equals(chartPlugin.getDataSettings().getAnalysisTimeFrame().getId())) {
                    comboBoxCustomPeriods.getSelectionModel().select(finalListCustomPeriodObjects.indexOf(cpo) + 1);
                }
            }
        } else {
            comboBoxCustomPeriods.getSelectionModel().select(0);
        }

        mathBox.getSelectionModel().select(chartPlugin.getDataSettings().getManipulationMode());
        mathBox.setMaxWidth(200);

        this.getDialogPane().getButtonTypes().addAll(cancelType, newType, loadType);

        Button loadButton = (Button) this.getDialogPane().lookupButton(loadType);
        loadButton.setId("ok-button");
        loadButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.graph.loaddialog.load")));
        loadButton.setDefaultButton(true);
        loadButton.setDisable(true);
        loadButton.setOnAction(actionEvent -> {
            response = Response.LOAD;

            chartPlugin.getDataSettings().setAggregationPeriod(AggregationPeriod.parseAggregationIndex(aggregationBox.getSelectionModel().getSelectedIndex()));
            chartPlugin.getDataSettings().setManipulationMode(mathBox.getSelectionModel().getSelectedItem());
            AnalysisTimeFrame analysisTimeFrame = presetDateBox.getSelectionModel().getSelectedItem();
            if (analysisTimeFrame == null && comboBoxCustomPeriods.getSelectionModel().getSelectedIndex() > 0) {
                analysisTimeFrame = chartPlugin.getDataSettings().getAnalysisTimeFrame();
            } else if (presetDateBox.getSelectionModel().getSelectedItem().getTimeFrame().equals(TimeFrame.CUSTOM)) {
                DateTime start = new DateTime(pickerDateStart.getValue().getYear(), pickerDateStart.getValue().getMonthValue(), pickerDateStart.getValue().getDayOfMonth(),
                        pickerTimeStart.getValue().getHour(), pickerTimeStart.getValue().getMinute(), pickerTimeStart.getValue().getSecond());
                DateTime end = new DateTime(pickerDateEnd.getValue().getYear(), pickerDateEnd.getValue().getMonthValue(), pickerDateEnd.getValue().getDayOfMonth(),
                        pickerTimeEnd.getValue().getHour(), pickerTimeEnd.getValue().getMinute(), pickerTimeEnd.getValue().getSecond());
                analysisTimeFrame.setStart(start);
                analysisTimeFrame.setEnd(end);
            }
            chartPlugin.getDataSettings().setAnalysisTimeFrame(analysisTimeFrame);

            if (analysisListView.getSelectionModel().getSelectedItem() != null) {
                chartPlugin.getDataSettings().setCurrentAnalysis(null);
                chartPlugin.getDataSettings().setCurrentAnalysis(analysisListView.getSelectionModel().getSelectedItem());
            }

            this.close();
        });

        Button newButton = (Button) this.getDialogPane().lookupButton(newType);
        newButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.graph.loaddialog.new")));

        newButton.setOnAction(event -> {
            response = Response.NEW;
            this.close();
        });

        Button cancelButton = (Button) this.getDialogPane().lookupButton(cancelType);
        cancelButton.setId("cancel-button");
        cancelButton.setCancelButton(true);

        cancelButton.setOnAction(event -> {
            this.close();
        });
    }


    private void addListener() {
        analysisListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {

                pickerCombo.updateCellFactory();
                chartPlugin.getDataSettings().setAggregationPeriod(AggregationPeriod.NONE);
                chartPlugin.getDataSettings().setManipulationMode(ManipulationMode.NONE);
                chartPlugin.getToolBarView().resetToolbarSettings();

                if (previewPref.getBoolean("enabled", true)) {
                    AnalysisTimeFrame oldAnalysisTimerFrame = chartPlugin.getDataSettings().getAnalysisTimeFrame();
                    AnalysisTimeFrame preview = new AnalysisTimeFrame(ds, chartPlugin, TimeFrame.PREVIEW);
                    chartPlugin.getDataSettings().setAnalysisTimeFrame(preview);
                    chartPlugin.getDataSettings().setCurrentAnalysis(newValue);

                    chartPlugin.getDataSettings().setAnalysisTimeFrame(oldAnalysisTimerFrame);
                }
            }
        });

        presetDateBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                switch (newValue.getTimeFrame()) {
                    //Custom
                    case CUSTOM:
                        break;
                    //today
                    case CURRENT:
                        chartPlugin.getDataSettings().setAnalysisTimeFrame(new AnalysisTimeFrame(ds, chartPlugin, TimeFrame.CURRENT));
                        updateGridLayout(false);
                        break;
                    //today
                    case TODAY:
                        chartPlugin.getDataSettings().setAnalysisTimeFrame(new AnalysisTimeFrame(ds, chartPlugin, TimeFrame.TODAY));
                        updateGridLayout(false);
                        break;
                    //yesterday
                    case YESTERDAY:
                        chartPlugin.getDataSettings().setAnalysisTimeFrame(new AnalysisTimeFrame(ds, chartPlugin, TimeFrame.YESTERDAY));
                        updateGridLayout(false);
                        break;
                    //last 7 days
                    case LAST_7_DAYS:
                        chartPlugin.getDataSettings().setAnalysisTimeFrame(new AnalysisTimeFrame(ds, chartPlugin, TimeFrame.LAST_7_DAYS));
                        updateGridLayout(false);
                        break;
                    //this Week
                    case THIS_WEEK:
                        chartPlugin.getDataSettings().setAnalysisTimeFrame(new AnalysisTimeFrame(ds, chartPlugin, TimeFrame.THIS_WEEK));
                        updateGridLayout(false);
                        break;
                    //last Week
                    case LAST_WEEK:
                        chartPlugin.getDataSettings().setAnalysisTimeFrame(new AnalysisTimeFrame(ds, chartPlugin, TimeFrame.LAST_WEEK));
                        updateGridLayout(false);
                        break;
                    //last 30 days
                    case LAST_30_DAYS:
                        chartPlugin.getDataSettings().setAnalysisTimeFrame(new AnalysisTimeFrame(ds, chartPlugin, TimeFrame.LAST_30_DAYS));
                        updateGridLayout(false);
                        break;
                    case THIS_MONTH:
                        //last Month
                        chartPlugin.getDataSettings().setAnalysisTimeFrame(new AnalysisTimeFrame(ds, chartPlugin, TimeFrame.THIS_MONTH));
                        updateGridLayout(false);
                        break;
                    case LAST_MONTH:
                        //last Month
                        chartPlugin.getDataSettings().setAnalysisTimeFrame(new AnalysisTimeFrame(ds, chartPlugin, TimeFrame.LAST_MONTH));
                        updateGridLayout(false);
                        break;
                    case THIS_YEAR:
                        //this Year
                        chartPlugin.getDataSettings().setAnalysisTimeFrame(new AnalysisTimeFrame(ds, chartPlugin, TimeFrame.THIS_YEAR));
                        updateGridLayout(false);
                        break;
                    case LAST_YEAR:
                        //last Year
                        chartPlugin.getDataSettings().setAnalysisTimeFrame(new AnalysisTimeFrame(ds, chartPlugin, TimeFrame.LAST_YEAR));
                        updateGridLayout(false);
                        break;
                    case THE_YEAR_BEFORE_LAST:
                        //last Year
                        chartPlugin.getDataSettings().setAnalysisTimeFrame(new AnalysisTimeFrame(ds, chartPlugin, TimeFrame.THE_YEAR_BEFORE_LAST));
                        updateGridLayout(false);
                        break;
                    case CUSTOM_START_END:
                    default:
                        break;
                }
            }
        });

        pickerDateStart.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                DateTime start = chartPlugin.getDataSettings().getAnalysisTimeFrame().getStart();
                DateTime end = chartPlugin.getDataSettings().getAnalysisTimeFrame().getEnd();
                DateTime now = DateTime.now();
                DateTime startDate = null;
                if (start != null) {
                    startDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                            start.getHourOfDay(), start.getMinuteOfHour(), 0, 0);
                } else {
                    startDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                            now.getHourOfDay(), now.getMinuteOfHour(), 0, 0);
                }
                AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(ds, chartPlugin, TimeFrame.CUSTOM);
                analysisTimeFrame.setStart(startDate);
                if (end != null) {
                    analysisTimeFrame.setEnd(end);
                } else {
                    analysisTimeFrame.setEnd(now);
                }
                chartPlugin.getDataSettings().setAnalysisTimeFrame(analysisTimeFrame);
                updateGridLayout(false);
            }
        });

        pickerTimeStart.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                DateTime start = chartPlugin.getDataSettings().getAnalysisTimeFrame().getStart();
                DateTime end = chartPlugin.getDataSettings().getAnalysisTimeFrame().getEnd();
                DateTime now = DateTime.now();
                DateTime startDate = null;
                if (start != null) {
                    startDate = new DateTime(
                            start.getYear(), start.getMonthOfYear(), start.getDayOfMonth(),
                            newValue.getHour(), newValue.getMinute(), 0, 0);
                } else {
                    startDate = new DateTime(
                            now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                            newValue.getHour(), newValue.getMinute(), 0, 0);
                }
                AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(ds, chartPlugin, TimeFrame.CUSTOM);
                analysisTimeFrame.setStart(startDate);
                if (end != null) {
                    analysisTimeFrame.setEnd(end);
                } else {
                    analysisTimeFrame.setEnd(now);
                }
                chartPlugin.getDataSettings().setAnalysisTimeFrame(analysisTimeFrame);
                updateGridLayout(false);
            }
        });

        pickerDateEnd.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                DateTime start = chartPlugin.getDataSettings().getAnalysisTimeFrame().getStart();
                DateTime end = chartPlugin.getDataSettings().getAnalysisTimeFrame().getEnd();
                DateTime now = DateTime.now();
                DateTime endDate = null;
                if (end != null) {
                    endDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                            end.getHourOfDay(), end.getMinuteOfHour(), 59, 999);
                } else {
                    endDate = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(),
                            now.getHourOfDay(), now.getMinuteOfHour(), 59, 999);
                }
                AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(ds, chartPlugin, TimeFrame.CUSTOM);
                if (start != null) {
                    analysisTimeFrame.setStart(start);
                } else {
                    analysisTimeFrame.setStart(now);
                }
                analysisTimeFrame.setEnd(endDate);
                chartPlugin.getDataSettings().setAnalysisTimeFrame(analysisTimeFrame);
                updateGridLayout(false);
            }
        });

        pickerTimeEnd.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                DateTime start = chartPlugin.getDataSettings().getAnalysisTimeFrame().getStart();
                DateTime end = chartPlugin.getDataSettings().getAnalysisTimeFrame().getEnd();
                DateTime now = DateTime.now();
                DateTime endDate = null;
                if (end != null) {
                    endDate = new DateTime(
                            end.getYear(), end.getMonthOfYear(), end.getDayOfMonth(),
                            newValue.getHour(), newValue.getMinute(), 59, 999);
                } else {
                    endDate = new DateTime(
                            now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                            newValue.getHour(), newValue.getMinute(), 59, 999);
                }
                AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(ds, chartPlugin, TimeFrame.CUSTOM);
                if (start != null) {
                    analysisTimeFrame.setStart(start);
                } else {
                    analysisTimeFrame.setStart(now);
                }
                analysisTimeFrame.setEnd(endDate);
                chartPlugin.getDataSettings().setAnalysisTimeFrame(analysisTimeFrame);
                updateGridLayout(false);
            }
        });

        mathBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                chartPlugin.getDataSettings().setManipulationMode(newValue);
                updateGridLayout(false);
            }
        });

        aggregationBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                chartPlugin.getDataSettings().setAggregationPeriod(AggregationPeriod.parseAggregationIndex(newValue.intValue()));
                updateGridLayout(false);
            }
        });

        pickerDateStart.setTooltip(pickerDateStartTT);
        pickerDateEnd.setTooltip(pickerDateEndTT);
        analysisListView.setTooltip(analysisListViewTT);
        aggregationBox.setTooltip(aggregationTT);
        mathBox.setTooltip(mathBoxTT);
        presetDateBox.setTooltip(presetDateBoxTT);
        comboBoxCustomPeriods.setTooltip(customPeriodsComboBoxTT);
    }

    private DisabledItemsComboBox<ManipulationMode> getMathBox() {

        final String keyPreset = I18n.getInstance().getString("plugin.graph.interval.preset");

        final String keyRunningMean = I18n.getInstance().getString("plugin.graph.manipulation.runningmean");
        final String keyCentricRunningMean = I18n.getInstance().getString("plugin.graph.manipulation.centricrunningmean");
        final String keySortedMin = I18n.getInstance().getString("plugin.graph.manipulation.sortedmin");
        final String keySortedMax = I18n.getInstance().getString("plugin.graph.manipulation.sortedmax");
        final String keyCumulate = I18n.getInstance().getString("plugin.graph.manipulation.cumulate");

        DisabledItemsComboBox<ManipulationMode> math = new DisabledItemsComboBox<>();
        List<ManipulationMode> customList = new ArrayList<>();
        customList.add(ManipulationMode.NONE);
        customList.add(ManipulationMode.RUNNING_MEAN);
        customList.add(ManipulationMode.CENTRIC_RUNNING_MEAN);
        customList.add(ManipulationMode.SORTED_MIN);
        customList.add(ManipulationMode.SORTED_MAX);
        customList.add(ManipulationMode.CUMULATE);

        math.setItems(FXCollections.observableArrayList(customList));
        math.getSelectionModel().selectFirst();

        Callback<javafx.scene.control.ListView<ManipulationMode>, ListCell<ManipulationMode>> cellFactory = new Callback<javafx.scene.control.ListView<ManipulationMode>, ListCell<ManipulationMode>>() {
            @Override
            public ListCell<ManipulationMode> call(javafx.scene.control.ListView<ManipulationMode> param) {
                return new ListCell<ManipulationMode>() {
                    @Override
                    protected void updateItem(ManipulationMode manipulationMode, boolean empty) {
                        super.updateItem(manipulationMode, empty);
                        if (empty || manipulationMode == null) {
                            setText("");
                        } else {
                            String text = "";
                            switch (manipulationMode) {
                                case NONE:
                                    text = keyPreset;
                                    break;
                                case RUNNING_MEAN:
                                    text = keyRunningMean;
                                    break;
                                case CENTRIC_RUNNING_MEAN:
                                    text = keyCentricRunningMean;
                                    break;
                                case SORTED_MIN:
                                    text = keySortedMin;
                                    break;
                                case SORTED_MAX:
                                    text = keySortedMax;
                                    break;
                                case CUMULATE:
                                    text = keyCumulate;
                                    break;
                            }
                            setText(text);
                        }
                    }
                };
            }
        };
        math.setCellFactory(cellFactory);
        math.setButtonCell(cellFactory.call(null));

        return math;
    }

    private JFXComboBox<String> getCustomPeriodsComboBox() {

        ObservableList<String> customPeriods = FXCollections.observableArrayList();
        List<JEVisObject> listCalendarDirectories = new ArrayList<>();
        List<JEVisObject> listCustomPeriods = new ArrayList<>();
        List<CustomPeriodObject> listCustomPeriodObjects = new ArrayList<>();

        try {
            try {
                JEVisClass calendarDirectoryClass = ds.getJEVisClass("Calendar Directory");
                listCalendarDirectories.addAll(ds.getObjects(calendarDirectoryClass, false));
            } catch (Exception e) {
                logger.error("Error: could not get calendar directories", e);
            }
            if (listCalendarDirectories.isEmpty()) {
                List<JEVisObject> listBuildings = new ArrayList<>();
                try {
                    JEVisClass building = ds.getJEVisClass("Building");
                    listBuildings = ds.getObjects(building, false);

                    if (!listBuildings.isEmpty()) {
                        JEVisClass calendarDirectoryClass = ds.getJEVisClass("Calendar Directory");
                        if (ds.getCurrentUser().canCreate(listBuildings.get(0).getID())) {

                            JEVisObject calendarDirectory = listBuildings.get(0).buildObject(I18n.getInstance().getString("plugin.calendardir.defaultname"), calendarDirectoryClass);
                            calendarDirectory.commit();
                        }
                    }
                } catch (JEVisException e) {
                    logger.error("Error: could not create new calendar directory", e);
                }

            }
            try {
                listCustomPeriods.addAll(ds.getObjects(ds.getJEVisClass("Custom Period"), false));
            } catch (JEVisException e) {
                logger.error("Error: could not get custom period", e);
            }
        } catch (Exception e) {
        }

        customPeriods.add(I18n.getInstance().getString("plugin.graph.dialog.loadnew.none"));

        for (JEVisObject obj : listCustomPeriods) {
            if (obj != null) {
                CustomPeriodObject cpo = new CustomPeriodObject(obj, new ObjectHandler(ds));
                if (cpo.isVisible()) {
                    listCustomPeriodObjects.add(cpo);
                    customPeriods.add(cpo.getObject().getName());
                }
            }
        }

        JFXComboBox<String> tempBox = new JFXComboBox<>(customPeriods);

        finalListCustomPeriodObjects = listCustomPeriodObjects;
        if (customPeriods.size() > 1) {
            tempBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.equals(oldValue)) {
                    if (newValue.intValue() > 0) {
                        for (CustomPeriodObject cpo : finalListCustomPeriodObjects) {
                            if (finalListCustomPeriodObjects.indexOf(cpo) + 1 == newValue.intValue()) {

                                AnalysisTimeFrame newTimeFrame = new AnalysisTimeFrame(ds, chartPlugin, TimeFrame.CUSTOM_START_END, cpo.getObject());
                                chartPlugin.getDataSettings().setAnalysisTimeFrame(newTimeFrame);
                                updateGridLayout(false);
                            }
                        }
                    }
                }
            });
        } else {
            tempBox.setDisable(true);
        }

        return tempBox;
    }

    public Response getResponse() {
        return response;
    }

    private void updateGridLayout(boolean isInit) {
        if (!isInit) {
            Platform.runLater(this::updateDialog);
        } else {
            updateDialog();
        }
    }

    private void updateDialog() {
        pickerCombo = new PickerCombo(ds, chartPlugin, false);
        pickerCombo.updateCellFactory();
        presetDateBox = pickerCombo.getPresetDateBox();
        pickerDateStart = pickerCombo.getStartDatePicker();
        pickerTimeStart = pickerCombo.getStartTimePicker();
        pickerDateEnd = pickerCombo.getEndDatePicker();
        pickerTimeEnd = pickerCombo.getEndTimePicker();
        filterInput.setPromptText(I18n.getInstance().getString("searchbar.filterinput.prompttext"));
        filterInput.setStyle("-fx-font-weight: bold;");

        Region freeSpace = new Region();
        freeSpace.setPrefWidth(40);

        GridPane gridLayout = new GridPane();
        gridLayout.setPadding(new Insets(10, 10, 10, 10));
        gridLayout.setVgap(10);
//            GridPane.setFillWidth(freeSpace, true);

        /** Column 0 */
        gridLayout.add(filterInput, 0, 0, 1, 1);
        gridLayout.add(analysisListView, 0, 1, 1, 13);

        /** Column 1 **/
        gridLayout.add(freeSpace, 1, 0, 1, 16);

        /** column 2**/
        gridLayout.add(timeRange, 2, 0, 2, 1);
        gridLayout.add(startText, 2, 1);
        gridLayout.add(endText, 2, 3);

        /** Column 3 **/
        gridLayout.add(pickerDateStart, 3, 1);
        gridLayout.add(pickerDateEnd, 3, 3); // column=1 row=0


        /** Column 4 **/
        gridLayout.add(pickerTimeStart, 4, 1);
        gridLayout.add(pickerTimeEnd, 4, 3);

        /** Column 2 - 4 **/
        gridLayout.add(standardSelectionsLabel, 2, 5, 3, 1);
        GridPane.setFillWidth(presetDateBox, true);
        presetDateBox.setMaxWidth(200);
        gridLayout.add(presetDateBox, 2, 6, 3, 1);

        gridLayout.add(customSelectionsLabel, 2, 7, 3, 1);
        GridPane.setFillWidth(comboBoxCustomPeriods, true);
        gridLayout.add(comboBoxCustomPeriods, 2, 8, 3, 1);

        gridLayout.add(labelAggregation, 2, 9, 3, 1);
        GridPane.setFillWidth(aggregationBox, true);
        gridLayout.add(aggregationBox, 2, 10, 3, 1);

        gridLayout.add(labelMath, 2, 11, 3, 1);
        GridPane.setFillWidth(mathBox, true);
        gridLayout.add(mathBox, 2, 12, 3, 1);

        GridPane.setFillWidth(analysisListView, true);
        GridPane.setFillHeight(analysisListView, true);
        if (!ChartTools.isMultiSite(ds) && !ChartTools.isMultiDir(ds)) analysisListView.setMinWidth(600d);
        else analysisListView.setMinWidth(900d);
//            analysisListView.setMaxWidth(600d);
        GridPane.setHgrow(analysisListView, Priority.ALWAYS);
        GridPane.setVgrow(analysisListView, Priority.ALWAYS);

        getDialogPane().setContent(gridLayout);
        getDialogPane().getScene().getWindow().sizeToScene();
        getDialogPane().getScene().getWindow().centerOnScreen();

        addListener();

        JEVisHelp.getInstance().addHelpControl(ChartPlugin.class.getSimpleName(), this.getClass().getSimpleName(),
                JEVisHelp.LAYOUT.HORIZONTAL_TOP_LEFT, pickerDateStart, pickerDateEnd, pickerTimeEnd, analysisListView,
                aggregationBox, mathBox, presetDateBox, comboBoxCustomPeriods);
    }

    public JFXTextField getFilterInput() {
        return filterInput;
    }
}
