/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.application.Chart.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.dialog.ProgressDialog;
import org.jevis.api.*;
import org.jevis.commons.chart.BubbleType;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.datetime.CustomPeriodObject;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.json.JsonAnalysisDataRow;
import org.jevis.commons.json.JsonChartDataModel;
import org.jevis.commons.json.JsonChartSettings;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.ws.json.JsonUnit;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Columns.ColorColumn;
import org.jevis.jeconfig.application.Chart.ChartSettings;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.application.jevistree.AlphanumComparator;
import org.jevis.jeconfig.plugin.graph.view.GraphPluginView;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author broder
 */
public class GraphDataModel {
    //public class GraphDataModel extends Observable {

    private static final Logger logger = LogManager.getLogger(GraphDataModel.class);
    public static final String CHARTS_ATTRIBUTE_NAME = "Charts";
    public static final String NUMBER_OF_CHARTS_PER_SCREEN_ATTRIBUTE_NAME = "Number of Charts per Screen";
    public static final String WORKDAY_BEGINNING_ATTRIBUTE_NAME = "Workday Beginning";
    public static final String WORKDAY_END_ATTRIBUTE_NAME = "Workday End";
    public static final String ANALYSES_DIRECTORY_CLASS_NAME = "Analyses Directory";
    public static final String NUMBER_OF_HORIZONTAL_PIES_ATTRIBUTE_NAME = "Number of Horizontal Pies";
    public static final String NUMBER_OF_HORIZONTAL_TABLES_ATTRIBUTE_NAME = "Number of Horizontal Tables";
    public static final String BUILDING_CLASS_NAME = "Building";
    public static final String ANALYSIS_CLASS_NAME = "Analysis";
    public static final String ORGANIZATION_CLASS_NAME = "Organization";
    public static final String DATA_MODEL_ATTRIBUTE_NAME = "Data Model";
    public static final String GRAPH_PLUGIN_CLASS_NAME = "Graph Plugin";
    private final GraphPluginView graphPluginView;
    private Set<ChartDataModel> selectedData = new HashSet<>();
    private List<ChartSettings> charts = new ArrayList<>();
    private Boolean hideShowIcons = true;
    private ManipulationMode addSeries = ManipulationMode.NONE;
    private Boolean autoResize = true;
    private JEVisDataSource ds;
    private ObservableList<JEVisObject> observableListAnalyses = FXCollections.observableArrayList();
    private JsonChartDataModel listAnalysisModel = new JsonChartDataModel();
    private List<JsonChartSettings> listChartsSettings = new ArrayList<>();
    private LocalTime workdayStart = LocalTime.of(0, 0, 0, 0);
    private LocalTime workdayEnd = LocalTime.of(23, 59, 59, 999999999);
    private JEVisObject currentAnalysis = null;
    private Boolean multipleDirectories = false;
    private Long chartsPerScreen = 2L;
    private Long horizontalPies = 2L;
    private Long horizontalTables = 3L;
    private Boolean isGlobalAnalysisTimeFrame = true;
    private AnalysisTimeFrame globalAnalysisTimeFrame;
    private SimpleBooleanProperty changed = new SimpleBooleanProperty(false);
    private Boolean showRawData = false;
    private Boolean showSum = false;
    private Boolean runUpdate = false;
    private Long finalSeconds = 60L;

    public GraphDataModel(JEVisDataSource ds, GraphPluginView graphPluginView) {
        this.ds = ds;
        this.graphPluginView = graphPluginView;
        this.globalAnalysisTimeFrame = new AnalysisTimeFrame(TimeFrame.TODAY);
        DateHelper dateHelper = new DateHelper(DateHelper.TransformType.TODAY);
        if (getWorkdayStart() != null) dateHelper.setStartTime(getWorkdayStart());
        if (getWorkdayEnd() != null) dateHelper.setEndTime(getWorkdayEnd());
        this.globalAnalysisTimeFrame.setStart(dateHelper.getStartDate());
        this.globalAnalysisTimeFrame.setEnd(dateHelper.getEndDate());

        this.changed.addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue && newValue) {
                this.changed.set(false);

                if (getCurrentAnalysis() != null && !getCurrentAnalysis().getName().equals("Temp")) {
                    this.selectedData = new HashSet<>();
                    this.charts = new ArrayList<>();
                    getSelectedData();
                }
                update();
            }
        });
    }

    public Set<ChartDataModel> getSelectedData() {
        if (this.selectedData == null || this.selectedData.isEmpty()) {

            updateSelectedData();

        }

        return this.selectedData;
    }

    public void setSelectedData(Set<ChartDataModel> selectedData) {
        Set<ChartDataModel> data = new HashSet<>();

        selectedData.forEach(chartDataModel -> {
            if (!chartDataModel.getSelectedcharts().isEmpty())
                data.add(chartDataModel);
        });

        this.selectedData = data;

        update();
    }

    public void setData(Set<ChartDataModel> data) {
        this.selectedData = data;
    }

    public void update() {
        final String loading = I18n.getInstance().getString("graph.progress.message");
        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() {
                        updateMessage(loading);
                        Platform.runLater(() -> GraphDataModel.this.graphPluginView.update(true));
                        return null;
                    }
                };
            }
        };
        ProgressDialog pd = new ProgressDialog(service);
        pd.setHeaderText(I18n.getInstance().getString("graph.progress.header"));
        pd.setTitle(I18n.getInstance().getString("graph.progress.title"));
        pd.getDialogPane().setContent(null);

        service.start();
    }

    private AggregationPeriod globalAggregationPeriod = AggregationPeriod.NONE;

    public void updateSamples() {
        this.selectedData.forEach(chartDataModel -> {
            chartDataModel.setSomethingChanged(true);
            chartDataModel.getSamples();
        });
    }

    public List<ChartSettings> getCharts() {
        if (this.charts == null || this.charts.isEmpty()) updateCharts();

//        if (selectedData != null && !selectedData.isEmpty() && listAnalysisModel != null) {
//
//            for (ChartSettings chartSettings : charts) {
//                AnalysisTimeFrame newATF = new AnalysisTimeFrame();
//                try {
//                    newATF.setActiveTimeFrame(newATF.parseTimeFrameFromString(chartSettings.getAnalysisTimeFrame().getTimeframe()));
//                    newATF.setId(Long.parseLong(chartSettings.getAnalysisTimeFrame().getId()));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }

        return this.charts;
    }

    public void setCharts(List<ChartSettings> charts) {
        this.charts = charts;
    }

    private void updateCharts() {
        if (this.charts == null || this.charts.isEmpty()) {
            try {
                if (getCurrentAnalysis() != null) {
                    if (Objects.nonNull(getCurrentAnalysis().getAttribute(CHARTS_ATTRIBUTE_NAME))) {
//                        ds.reloadAttribute(getCurrentAnalysis().getAttribute("Charts"));
                        if (getCurrentAnalysis().getAttribute(CHARTS_ATTRIBUTE_NAME).hasSample()) {
                            String str = getCurrentAnalysis().getAttribute(CHARTS_ATTRIBUTE_NAME).getLatestSample().getValueAsString();
                            try {
                                if (str.startsWith("[")) {
                                    this.listChartsSettings = new Gson().fromJson(str, new TypeToken<List<JsonChartSettings>>() {
                                    }.getType());

                                } else {
                                    this.listChartsSettings = new ArrayList<>();
                                    this.listChartsSettings.add(new Gson().fromJson(str, JsonChartSettings.class));
                                }
                            } catch (Exception e) {
                                logger.error("Error: could not read chart settings", e);
                            }
                        }
                    }
                    if (Objects.nonNull(getCurrentAnalysis().getAttribute(NUMBER_OF_CHARTS_PER_SCREEN_ATTRIBUTE_NAME))) {
                        JEVisSample chartPerScreenSample = getCurrentAnalysis().getAttribute(NUMBER_OF_CHARTS_PER_SCREEN_ATTRIBUTE_NAME).getLatestSample();
                        if (chartPerScreenSample != null) {
                            setChartsPerScreen(chartPerScreenSample.getValueAsLong());
                        } else {
                            JEVisClass graphServiceClass = this.ds.getJEVisClass(GRAPH_PLUGIN_CLASS_NAME);
                            List<JEVisObject> graphServiceList = this.ds.getObjects(graphServiceClass, true);
                            for (JEVisObject graphPlugin : graphServiceList) {
                                JEVisAttribute chartsPerScreenAttribute = graphPlugin.getAttribute(NUMBER_OF_CHARTS_PER_SCREEN_ATTRIBUTE_NAME);
                                if (chartsPerScreenAttribute != null) {
                                    JEVisSample lastSample = chartsPerScreenAttribute.getLatestSample();
                                    if (lastSample != null) {
                                        setChartsPerScreen(lastSample.getValueAsLong());
                                    }
                                }
                            }
                        }
                    }

                    WorkDays wd = new WorkDays(getCurrentAnalysis());
                    if (wd.getWorkdayStart() != null) this.workdayStart = wd.getWorkdayStart();
                    if (wd.getWorkdayEnd() != null) this.workdayEnd = wd.getWorkdayEnd();
                }
            } catch (JEVisException e) {
                logger.error("Error: could not get analysis model", e);
            }

            List<ChartSettings> chartSettings = new ArrayList<>();

            if (this.listChartsSettings != null && !this.listChartsSettings.isEmpty()) {
                boolean needsIds = false;
                for (JsonChartSettings settings : this.listChartsSettings) {
                    ChartSettings newSettings = new ChartSettings("");
                    if (settings.getId() != null) {
                        newSettings.setId(Integer.parseInt(settings.getId()));
                    } else {
                        needsIds = true;
                    }

                    newSettings.setName(settings.getName());
                    newSettings.setChartType(ChartType.parseChartType(settings.getChartType()));

                    if (settings.getHeight() != null) {
                        newSettings.setHeight(Double.parseDouble(settings.getHeight()));
                    }

                    chartSettings.add(newSettings);
                }

                if (needsIds) {
                    AlphanumComparator ac = new AlphanumComparator();
                    chartSettings.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));
                    for (ChartSettings set : chartSettings) set.setId(chartSettings.indexOf(set));
                }

                this.charts = chartSettings;
            }
        }
    }

    public ChartSettings getChartSetting(String name) {
        for (ChartSettings cset : getCharts()) {
            if (cset.getName().equals(name)) {
                return cset;
            }
        }
        return null;

    }

    private Service<Void> service = new Service<Void>() {
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        TimeUnit.SECONDS.sleep(GraphDataModel.this.finalSeconds);
                        Platform.runLater(() -> {
                            GraphDataModel.this.graphPluginView.handleRequest(Constants.Plugin.Command.RELOAD);
                        });

                    } catch (InterruptedException e) {
                        logger.warn("Reload Service stopped.");
                        cancelled();
                    }
                    succeeded();

                    return null;
                }
            };
        }
    };

    public void stopTimer() {
        this.service.cancel();
        this.service.reset();
    }

    public Boolean getHideShowIcons() {
        return this.hideShowIcons;
    }

    public void setHideShowIcons(Boolean hideShowIcons) {
        this.hideShowIcons = hideShowIcons;

        update();
    }

    public void setHideShowIconsNO_EVENT(Boolean hideShowIcons) {
        this.hideShowIcons = hideShowIcons;
    }

    public ManipulationMode getAddSeries() {
        return this.addSeries;
    }

    public void setAddSeries(ManipulationMode addSeries) {
        this.addSeries = addSeries;

        update();
    }

    public Boolean getAutoResize() {
        return this.autoResize;
    }

    public void setAutoResize(Boolean resize) {
        this.autoResize = resize;

        if (this.autoResize.equals(Boolean.TRUE)) {
            update();
        }
    }

    public void setAutoResizeNO_EVENT(Boolean resize) {
        this.autoResize = resize;
    }

    public Boolean getShowSum() {
        return this.showSum;
    }

    public void setShowSum(Boolean show) {
        this.showSum = show;

        if (show) {
            update();
        } else {
            this.graphPluginView.handleRequest(Constants.Plugin.Command.RELOAD);
        }
    }

    public void setTimer() {
        if (this.service.isRunning()) {
            this.service.cancel();
            this.service.reset();
        }
        Period p = null;
        for (ChartDataModel chartDataModel : getSelectedData()) {
            List<JEVisSample> samples = chartDataModel.getSamples();
            if (samples.size() > 0) {
                try {
                    p = new Period(samples.get(0).getTimestamp(), samples.get(1).getTimestamp());
                } catch (JEVisException e) {
                    logger.error(e);
                }
                break;
            }
        }
        if (p != null) {
            Long seconds = null;
            try {
                seconds = p.toStandardDuration().getStandardSeconds();
                seconds = seconds / 2;
            } catch (Exception e) {
                logger.error(e);
            }
            if (seconds == null) seconds = 60L;

            Alert warning = new Alert(Alert.AlertType.INFORMATION, I18n.getInstance().getString("plugin.graph.toolbar.timer.settimer")
                    + " " + seconds + " " + I18n.getInstance().getString("plugin.graph.toolbar.timer.settimer2"), ButtonType.OK);
            warning.showAndWait();

            this.finalSeconds = seconds;
            this.service.start();
        }
    }

    public Boolean getRunUpdate() {
        return this.runUpdate;
    }

    public void setShowSumNO_EVENT(Boolean show) {
        this.showSum = show;
    }

    public Boolean getShowRawData() {
        return this.showRawData;
    }

    public void setShowRawData(Boolean show) {
        this.showRawData = show;

        if (show) {
            update();
        } else {
            this.graphPluginView.handleRequest(Constants.Plugin.Command.RELOAD);
        }
    }

    public void setShowRawDataNO_EVENT(Boolean show) {
        this.showRawData = show;
    }

    public boolean containsId(Long id) {
        if (!getSelectedData().isEmpty()) {
            AtomicBoolean found = new AtomicBoolean(false);
            getSelectedData().forEach(chartDataModel -> {
                if (chartDataModel.getObject().getID().equals(id)) {
                    found.set(true);
                }
            });
            return found.get();
        } else return false;
    }

    public ChartDataModel get(Long id) {
        ChartDataModel out = null;
        for (ChartDataModel chartDataModel : getSelectedData()) {
            if (chartDataModel.getObject().getID().equals(id)) {
                return chartDataModel;
            }
        }
        return out;
    }

    public void setRunUpdate(Boolean run) {
        this.runUpdate = run;
    }

    public void resetToolbarSettings() {
        setHideShowIconsNO_EVENT(true);
        setShowRawDataNO_EVENT(false);
        setShowSumNO_EVENT(false);
        setAutoResizeNO_EVENT(true);
        setRunUpdate(false);
        if (this.service.isRunning()) {
            this.service.cancel();
            this.service.reset();
        }
    }

    public void setAnalysisTimeFrameForAllModels(AnalysisTimeFrame analysisTimeFrame) {

        for (ChartSettings chartSettings : this.charts) {
            chartSettings.setAnalysisTimeFrame(analysisTimeFrame);

            List<ChartDataModel> chartDataModels = new ArrayList<>();
            getSelectedData().forEach(chartDataModel -> {
                if (chartDataModel.getSelectedcharts().contains(chartSettings.getId()))
                    chartDataModels.add(chartDataModel);
            });
            DateHelper dateHelper = new DateHelper();
            dateHelper.setMinMaxForDateHelper(chartDataModels);

            setAnalysisTimeFrameForModels(chartDataModels, dateHelper, analysisTimeFrame);

        }

        this.globalAnalysisTimeFrame = analysisTimeFrame;
        isGlobalAnalysisTimeFrame(true);
        this.changed.set(true);
    }

    public void setAnalysisTimeFrameForModels(List<ChartDataModel> chartDataModels, DateHelper dateHelper, AnalysisTimeFrame analysisTimeFrame) {
        if (this.selectedData != null && !this.selectedData.isEmpty()) {
            isGlobalAnalysisTimeFrame(false);

            if (getWorkdayStart() != null) dateHelper.setStartTime(getWorkdayStart());
            if (getWorkdayEnd() != null) dateHelper.setEndTime(getWorkdayEnd());

            switch (analysisTimeFrame.getTimeFrame()) {
                //Custom
                case CUSTOM:
                    if (analysisTimeFrame.getStart() != null && analysisTimeFrame.getEnd() != null) {
                        for (ChartDataModel model : chartDataModels) {
                            setChartDataModelStartAndEnd(model, analysisTimeFrame.getStart(), analysisTimeFrame.getEnd());
                        }
                    }
                    break;
                //today
                case TODAY:
                    dateHelper.setType(DateHelper.TransformType.TODAY);
                    updateStartEndToDataModel(chartDataModels, dateHelper);
                    analysisTimeFrame.setStart(dateHelper.getStartDate());
                    analysisTimeFrame.setEnd(dateHelper.getEndDate());
                    break;
                //yesterday
                case YESTERDAY:
                    dateHelper.setType(DateHelper.TransformType.YESTERDAY);
                    updateStartEndToDataModel(chartDataModels, dateHelper);
                    analysisTimeFrame.setStart(dateHelper.getStartDate());
                    analysisTimeFrame.setEnd(dateHelper.getEndDate());
                    break;
                //last 7 days
                case LAST_7_DAYS:
                    dateHelper.setType(DateHelper.TransformType.LAST7DAYS);
                    updateStartEndToDataModel(chartDataModels, dateHelper);
                    analysisTimeFrame.setStart(dateHelper.getStartDate());
                    analysisTimeFrame.setEnd(dateHelper.getEndDate());
                    break;
                //last Week days
                case LAST_WEEK:
                    dateHelper.setType(DateHelper.TransformType.LASTWEEK);
                    updateStartEndToDataModel(chartDataModels, dateHelper);
                    analysisTimeFrame.setStart(dateHelper.getStartDate());
                    analysisTimeFrame.setEnd(dateHelper.getEndDate());
                    break;
                //last 30 days
                case LAST_30_DAYS:
                    dateHelper.setType(DateHelper.TransformType.LAST30DAYS);
                    updateStartEndToDataModel(chartDataModels, dateHelper);
                    analysisTimeFrame.setStart(dateHelper.getStartDate());
                    analysisTimeFrame.setEnd(dateHelper.getEndDate());
                    break;
                case LAST_MONTH:
                    //last Month
                    dateHelper.setType(DateHelper.TransformType.LASTMONTH);
                    updateStartEndToDataModel(chartDataModels, dateHelper);
                    analysisTimeFrame.setStart(dateHelper.getStartDate());
                    analysisTimeFrame.setEnd(dateHelper.getEndDate());
                    break;
                case THIS_YEAR:
                    //last Month
                    dateHelper.setType(DateHelper.TransformType.THISYEAR);
                    updateStartEndToDataModel(chartDataModels, dateHelper);
                    analysisTimeFrame.setStart(dateHelper.getStartDate());
                    analysisTimeFrame.setEnd(dateHelper.getEndDate());
                    break;
                case LAST_YEAR:
                    //last Month
                    dateHelper.setType(DateHelper.TransformType.LASTYEAR);
                    updateStartEndToDataModel(chartDataModels, dateHelper);
                    analysisTimeFrame.setStart(dateHelper.getStartDate());
                    analysisTimeFrame.setEnd(dateHelper.getEndDate());
                    break;
                case CUSTOM_START_END:
                    if (analysisTimeFrame.getId() != 0L) {
                        try {
                            dateHelper.setType(DateHelper.TransformType.CUSTOM_PERIOD);
                            CustomPeriodObject cpo = new CustomPeriodObject(this.ds.getObject(analysisTimeFrame.getId()), new ObjectHandler(this.ds));
                            dateHelper.setCustomPeriodObject(cpo);

                            updateStartEndToDataModel(chartDataModels, dateHelper);
                            analysisTimeFrame.setStart(dateHelper.getStartDate());
                            analysisTimeFrame.setEnd(dateHelper.getEndDate());
                        } catch (Exception e) {
                            logger.error("Error getting custom period object: " + e);
                        }
                    }
                    break;
                case PREVIEW:
                    try {
                        AtomicReference<DateTime> start = new AtomicReference<>(DateTime.now().minusDays(1));
                        AtomicReference<DateTime> end = new AtomicReference<>(DateTime.now());

                        for (ChartDataModel chartDataModel : chartDataModels) {
                            JEVisAttribute valueAtt = chartDataModel.getAttribute();
                            if (valueAtt != null) {
                                if (valueAtt.getTimestampFromLastSample().isBefore(end.get()))
                                    end.set(valueAtt.getTimestampFromLastSample());
                            }

                            start.set(end.get().minusDays(1));

                            if (valueAtt != null) {
                                if (valueAtt.getTimestampFromFirstSample().isAfter(start.get()))
                                    start.set(valueAtt.getTimestampFromFirstSample());
                            }
                        }

                        for (ChartDataModel chartDataModel : chartDataModels) {
                            if (!chartDataModel.getSelectedcharts().isEmpty()) {
                                setChartDataModelStartAndEnd(chartDataModel, start.get(), end.get());
                            }
                        }
                        analysisTimeFrame.setStart(start.get());
                        analysisTimeFrame.setEnd(end.get());

                    } catch (Exception e) {
                        logger.error("Error: " + e);
                    }
                    break;
            }
        }
    }

    private void setChartDataModelStartAndEnd(ChartDataModel model, DateTime start, DateTime end) {
        model.setSelectedStart(start);
        model.setSelectedEnd(end);
        model.setSomethingChanged(true);
    }

    private void updateStartEndToDataModel(List<ChartDataModel> chartDataModels, DateHelper dh) {
        DateTime start = dh.getStartDate();
        DateTime end = dh.getEndDate();

        for (ChartDataModel chartDataModel : chartDataModels) {
            if (!chartDataModel.getSelectedcharts().isEmpty()) {
                setChartDataModelStartAndEnd(chartDataModel, start, end);
            }
        }
    }

    public void updateListAnalyses() {
        List<JEVisObject> listAnalysesDirectories = new ArrayList<>();

        try {
            JEVisClass analysesDirectory = this.ds.getJEVisClass(ANALYSES_DIRECTORY_CLASS_NAME);
            listAnalysesDirectories = this.ds.getObjects(analysesDirectory, false);

            if (listAnalysesDirectories.size() > 1) {
                this.multipleDirectories = true;
            }

            if (listAnalysesDirectories.size() > 0
                    && this.workdayStart.equals(LocalTime.of(0, 0, 0, 0))
                    && this.workdayEnd.equals(LocalTime.of(23, 59, 59, 999999999))) {
                updateWorkdayTimesFromJEVisObject(listAnalysesDirectories.get(0));
            }
        } catch (JEVisException e) {
            logger.error("Error: could not get analyses directories", e);
        }
        if (listAnalysesDirectories.isEmpty()) {
            List<JEVisObject> listBuildings = new ArrayList<>();
            try {
                JEVisClass building = this.ds.getJEVisClass(BUILDING_CLASS_NAME);
                listBuildings = this.ds.getObjects(building, false);

                if (!listBuildings.isEmpty()) {
                    JEVisClass analysesDirectory = this.ds.getJEVisClass(ANALYSES_DIRECTORY_CLASS_NAME);
                    JEVisObject analysesDir = listBuildings.get(0).buildObject(I18n.getInstance().getString("plugin.graph.analysesdir.defaultname"), analysesDirectory);
                    analysesDir.commit();
                }
            } catch (JEVisException e) {
                logger.error("Error: could not create new analyses directory", e);
            }

        }
        try {
            this.observableListAnalyses.clear();
            this.observableListAnalyses.addAll(this.ds.getObjects(this.ds.getJEVisClass(ANALYSIS_CLASS_NAME), false));

        } catch (JEVisException e) {
            logger.error("Error: could not get analysis", e);
        }

        AlphanumComparator ac = new AlphanumComparator();
        if (!this.multipleDirectories)
            this.observableListAnalyses.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));
        else {
            this.observableListAnalyses.sort((o1, o2) -> {

                String prefix1 = "";
                String prefix2 = "";

                try {
                    JEVisObject secondParent1 = o1.getParents().get(0).getParents().get(0);
                    JEVisClass buildingClass = this.ds.getJEVisClass(BUILDING_CLASS_NAME);
                    JEVisClass organisationClass = this.ds.getJEVisClass(ORGANIZATION_CLASS_NAME);

                    if (secondParent1.getJEVisClass().equals(buildingClass)) {
                        try {
                            JEVisObject organisationParent = secondParent1.getParents().get(0).getParents().get(0);
                            if (organisationParent.getJEVisClass().equals(organisationClass)) {

                                prefix1 += organisationParent.getName() + " / " + secondParent1.getName() + " / ";
                            }
                        } catch (JEVisException e) {
                            logger.error("Could not get Organization parent of " + secondParent1.getName() + ":" + secondParent1.getID());

                            prefix1 += secondParent1.getName() + " / ";
                        }
                    } else if (secondParent1.getJEVisClass().equals(organisationClass)) {

                        prefix1 += secondParent1.getName() + " / ";

                    }

                } catch (Exception e) {
                }
                prefix1 = prefix1 + o1.getName();

                try {
                    JEVisObject secondParent2 = o2.getParents().get(0).getParents().get(0);
                    JEVisClass buildingClass = this.ds.getJEVisClass(BUILDING_CLASS_NAME);
                    JEVisClass organisationClass = this.ds.getJEVisClass(ORGANIZATION_CLASS_NAME);

                    if (secondParent2.getJEVisClass().equals(buildingClass)) {
                        try {
                            JEVisObject organisationParent = secondParent2.getParents().get(0).getParents().get(0);
                            if (organisationParent.getJEVisClass().equals(organisationClass)) {

                                prefix2 += organisationParent.getName() + " / " + secondParent2.getName() + " / ";
                            }
                        } catch (JEVisException e) {
                            logger.error("Could not get Organization parent of " + secondParent2.getName() + ":" + secondParent2.getID());

                            prefix2 += secondParent2.getName() + " / ";
                        }
                    } else if (secondParent2.getJEVisClass().equals(organisationClass)) {

                        prefix2 += secondParent2.getName() + " / ";

                    }

                } catch (Exception e) {
                }
                prefix2 = prefix2 + o2.getName();

                return ac.compare(prefix1, prefix2);
            });
        }
    }

    public JsonChartDataModel getListAnalysisModel() {

        JsonChartDataModel tempModel = null;
        try {
//            ds.reloadAttributes();

            if (this.charts == null || this.charts.isEmpty()) {
                updateCharts();
            }
            if (getCurrentAnalysis() != null) {
                if (Objects.nonNull(getCurrentAnalysis().getAttribute(DATA_MODEL_ATTRIBUTE_NAME))) {
//                    ds.reloadAttribute(getCurrentAnalysis().getAttribute("Data Model"));
                    if (getCurrentAnalysis().getAttribute(DATA_MODEL_ATTRIBUTE_NAME).hasSample()) {
                        String str = getCurrentAnalysis().getAttribute(DATA_MODEL_ATTRIBUTE_NAME).getLatestSample().getValueAsString();
                        try {
                            if (str.startsWith("[")) {
                                tempModel = new JsonChartDataModel();
                                List<JsonAnalysisDataRow> listOld = new Gson().fromJson(str, new TypeToken<List<JsonAnalysisDataRow>>() {
                                }.getType());
                                tempModel.setListDataRows(listOld);
                            } else {
                                try {
                                    tempModel = new Gson().fromJson(str, new TypeToken<JsonChartDataModel>() {
                                    }.getType());
                                } catch (Exception e) {
                                    logger.error(e);
                                    tempModel = new JsonChartDataModel();
                                    tempModel.getListAnalyses().add(new Gson().fromJson(str, JsonAnalysisDataRow.class));
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Error: could not read data model", e);
                        }
                    }
                }
                if (Objects.nonNull(getCurrentAnalysis().getAttribute(NUMBER_OF_HORIZONTAL_PIES_ATTRIBUTE_NAME))) {
                    if (getCurrentAnalysis().getAttribute(NUMBER_OF_HORIZONTAL_PIES_ATTRIBUTE_NAME).hasSample()) {
                        Long no = getCurrentAnalysis().getAttribute(NUMBER_OF_HORIZONTAL_PIES_ATTRIBUTE_NAME).getLatestSample().getValueAsLong();
                        if (no != null) {
                            setHorizontalPies(no);
                        }
                    }
                }
                if (Objects.nonNull(getCurrentAnalysis().getAttribute(NUMBER_OF_HORIZONTAL_TABLES_ATTRIBUTE_NAME))) {
                    if (getCurrentAnalysis().getAttribute(NUMBER_OF_HORIZONTAL_TABLES_ATTRIBUTE_NAME).hasSample()) {
                        Long no = getCurrentAnalysis().getAttribute(NUMBER_OF_HORIZONTAL_TABLES_ATTRIBUTE_NAME).getLatestSample().getValueAsLong();
                        if (no != null) {
                            setHorizontalPies(no);
                        }
                    }
                }

                updateWorkdayTimesFromJEVisObject(getCurrentAnalysis());
            }
        } catch (JEVisException e) {
            logger.error("Error: could not get analysis model", e);
        }

        this.listAnalysisModel = tempModel;

        return this.listAnalysisModel;
    }

    private void updateWorkdayTimesFromJEVisObject(JEVisObject jeVisObject) {
        WorkDays wd = new WorkDays(jeVisObject);
        if (wd.getWorkdayStart() != null && wd.getWorkdayEnd() != null) {
            this.workdayStart = wd.getWorkdayStart();
            this.workdayEnd = wd.getWorkdayEnd();
        }
    }

    public ObservableList<JEVisObject> getObservableListAnalyses() {
        if (this.observableListAnalyses.isEmpty()) updateListAnalyses();
        return this.observableListAnalyses;
    }

    public LocalTime getWorkdayStart() {
        if (this.observableListAnalyses.size() > 0
                && this.workdayStart.equals(LocalTime.of(0, 0, 0, 0))
                && this.workdayEnd.equals(LocalTime.of(23, 59, 59, 999999999))) {
            updateWorkdayTimesFromJEVisObject(this.observableListAnalyses.get(0));
        }
        return this.workdayStart;
    }

    public LocalTime getWorkdayEnd() {
        if (this.observableListAnalyses.size() > 0
                && this.workdayStart.equals(LocalTime.of(0, 0, 0, 0))
                && this.workdayEnd.equals(LocalTime.of(23, 59, 59, 999999999))) {
            updateWorkdayTimesFromJEVisObject(this.observableListAnalyses.get(0));
        }
        return this.workdayEnd;
    }

    public JEVisObject getCurrentAnalysis() {

        return this.currentAnalysis;
    }

    public void setCurrentAnalysis(JEVisObject currentAnalysis) {
        this.currentAnalysis = currentAnalysis;

        if (currentAnalysis != null) {
            try {
                this.ds.reloadAttribute(currentAnalysis.getAttribute(DATA_MODEL_ATTRIBUTE_NAME));
            } catch (Exception ex) {
                logger.error(ex);
            }

            if (this.observableListAnalyses == null || this.observableListAnalyses.isEmpty()) updateListAnalyses();
//        if (listAnalysisModel == null) {
            getListAnalysisModel();
            updateSelectedData();
//        }
        }
    }


    private List<Integer> stringToList(String s) {
        if (Objects.nonNull(s)) {
            List<String> tempList = new ArrayList<>(Arrays.asList(s.split(", ")));
            List<Integer> idList = new ArrayList<>();
            for (String str : tempList) if (str.contains(", ")) str.replace(", ", "");

            try {
                for (String str : tempList) {
                    idList.add(Integer.parseInt(str));
                }
            } catch (Exception e) {
                logger.error("Old data model. Starting comparison.");
                AlphanumComparator ac = new AlphanumComparator();
                tempList.sort((o1, o2) -> ac.compare(o1, o2));

                for (String str : tempList) {
                    for (ChartSettings set : this.charts) {
                        if (set.getName().equals(str))
                            idList.add(set.getId());
                    }
                }
            }

            return idList;
        } else return new ArrayList<>();
    }

    public void selectNone() {
        getSelectedData().forEach(mdl -> {
            mdl.setSelectedCharts(new ArrayList<>());
            mdl.setColor(ColorColumn.STANDARD_COLOR);
        });
    }

    private ManipulationMode globalManipulationMode = ManipulationMode.NONE;

    public void updateSelectedData() {
        Set<ChartDataModel> selectedData = new HashSet<>();

        JsonChartDataModel jsonChartDataModel = getListAnalysisModel();

        if (jsonChartDataModel != null) {
            Map<String, ChartDataModel> data = new HashMap<>();

            for (JsonAnalysisDataRow mdl : jsonChartDataModel.getListAnalyses()) {
                ChartDataModel newData = new ChartDataModel(this.ds);

                try {
                    Long id = Long.parseLong(mdl.getObject());
                    Long id_dp = null;
                    if (mdl.getDataProcessorObject() != null) id_dp = Long.parseLong(mdl.getDataProcessorObject());
                    JEVisObject obj = this.ds.getObject(id);
                    JEVisObject obj_dp = null;
                    if (mdl.getDataProcessorObject() != null) obj_dp = this.ds.getObject(id_dp);
                    JEVisUnit unit = new JEVisUnitImp(new Gson().fromJson(mdl.getUnit(), JsonUnit.class));
                    newData.setObject(obj);

                    newData.setColor(Color.valueOf(mdl.getColor()));
                    newData.setTitle(mdl.getName());
                    if (mdl.getDataProcessorObject() != null) newData.setDataProcessor(obj_dp);
                    newData.getAttribute();
                    if (getAggregationPeriod() != null) {
                        newData.setAggregationPeriod(getAggregationPeriod());
                    } else {
                        newData.setAggregationPeriod(AggregationPeriod.parseAggregation(mdl.getAggregation()));
                    }
                    if (getManipulationMode() != null) {
                        newData.setManipulationMode(getManipulationMode());
                    } else {
                        /**
                         * implement in json
                         */
                    }
                    newData.setSomethingChanged(true);
                    newData.setSelectedCharts(stringToList(mdl.getSelectedCharts()));
                    newData.setUnit(unit);
                    if (mdl.getAxis() != null) newData.setAxis(Integer.parseInt(mdl.getAxis()));

                    if (mdl.getIsEnPI() != null) newData.setEnPI(Boolean.parseBoolean(mdl.getIsEnPI()));
                    if (mdl.getCalculation() != null) {
                        newData.setCalculationObject(mdl.getCalculation());
                    }

                    if (mdl.getBubbleType() != null) {
                        newData.setBubbleType(BubbleType.parseBubbleType(mdl.getBubbleType()));
                    }

                    if (this.isGlobalAnalysisTimeFrame) {
                        newData.setSelectedStart(this.globalAnalysisTimeFrame.getStart());
                        newData.setSelectedEnd(this.globalAnalysisTimeFrame.getEnd());
                    }

                    data.put(obj.getID().toString(), newData);
                } catch (JEVisException e) {
                    logger.error("Error: could not get chart data model", e);
                }
            }

            for (Map.Entry<String, ChartDataModel> entrySet : data.entrySet()) {
                ChartDataModel value = entrySet.getValue();
                if (!value.getSelectedcharts().isEmpty()) {
                    selectedData.add(value);
                }
            }
        }
        this.selectedData = selectedData;
    }

    public AggregationPeriod getAggregationPeriod() {
        return this.globalAggregationPeriod;
    }

    public void setAggregationPeriod(AggregationPeriod aggregationPeriod) {
        this.globalAggregationPeriod = aggregationPeriod;
        if (this.selectedData != null && !this.selectedData.isEmpty()) {
            this.selectedData.forEach(chartDataModel -> chartDataModel.setAggregationPeriod(aggregationPeriod));
        }
    }

    public ManipulationMode getManipulationMode() {
        return this.globalManipulationMode;
    }

    public void setManipulationMode(ManipulationMode manipulationMode) {
        this.globalManipulationMode = manipulationMode;
        if (this.selectedData != null && !this.selectedData.isEmpty()) {
            this.selectedData.forEach(chartDataModel -> chartDataModel.setManipulationMode(manipulationMode));
        }
    }

    public Boolean getMultipleDirectories() {
        return this.multipleDirectories;
    }

    public Long getChartsPerScreen() {
        if (this.chartsPerScreen == null) {
            try {
                JEVisClass graphPluginClass = this.ds.getJEVisClass(GRAPH_PLUGIN_CLASS_NAME);
                List<JEVisObject> graphPlugins = this.ds.getObjects(graphPluginClass, true);
                if (!graphPlugins.isEmpty()) {
                    JEVisAttribute chartsPerScreenAttribute = graphPlugins.get(0).getAttribute(NUMBER_OF_CHARTS_PER_SCREEN_ATTRIBUTE_NAME);
                    if (chartsPerScreenAttribute != null) {
                        JEVisSample latestSample = chartsPerScreenAttribute.getLatestSample();
                        if (latestSample != null) {
                            this.chartsPerScreen = Long.parseLong(latestSample.getValueAsString());
                        }
                    }
                }
            } catch (JEVisException e) {
                logger.error("Could not get JEVisClass for Graph Plugin");
            }
        }
        if (this.chartsPerScreen == null || this.chartsPerScreen.equals(0L)) this.chartsPerScreen = 2L;
        return this.chartsPerScreen;
    }

    public void setChartsPerScreen(Long chartsPerScreen) {
        this.chartsPerScreen = chartsPerScreen;
    }

    public Long getHorizontalPies() {
        if (this.horizontalPies == null) {
            try {
                JEVisClass graphPluginClass = this.ds.getJEVisClass(GRAPH_PLUGIN_CLASS_NAME);
                List<JEVisObject> graphPlugins = this.ds.getObjects(graphPluginClass, true);
                if (!graphPlugins.isEmpty()) {
                    JEVisAttribute horizontalPiesAttribute = graphPlugins.get(0).getAttribute(NUMBER_OF_HORIZONTAL_PIES_ATTRIBUTE_NAME);
                    if (horizontalPiesAttribute != null) {
                        JEVisSample latestSample = horizontalPiesAttribute.getLatestSample();
                        if (latestSample != null) {
                            this.horizontalPies = Long.parseLong(latestSample.getValueAsString());
                        }
                    }
                }
            } catch (JEVisException e) {
                logger.error("Could not get JEVisClass for Graph Plugin");
            }
        }
        if (this.horizontalPies == null || this.horizontalPies.equals(0L)) this.horizontalPies = 2L;
        return this.horizontalPies;
    }

    public Long getHorizontalTables() {
        if (this.horizontalTables == null) {
            try {
                JEVisClass graphPluginClass = this.ds.getJEVisClass(GRAPH_PLUGIN_CLASS_NAME);
                List<JEVisObject> graphPlugins = this.ds.getObjects(graphPluginClass, true);
                if (!graphPlugins.isEmpty()) {
                    JEVisAttribute horizontalTablesAttribute = graphPlugins.get(0).getAttribute(NUMBER_OF_HORIZONTAL_TABLES_ATTRIBUTE_NAME);
                    if (horizontalTablesAttribute != null) {
                        JEVisSample latestSample = horizontalTablesAttribute.getLatestSample();
                        if (latestSample != null) {
                            this.horizontalTables = Long.parseLong(latestSample.getValueAsString());
                        }
                    }
                }
            } catch (JEVisException e) {
                logger.error("Could not get JEVisClass for Graph Plugin");
            }
        }
        if (this.horizontalTables == null || this.horizontalTables.equals(0L)) this.horizontalTables = 3L;
        return this.horizontalTables;
    }

    public void setHorizontalTables(Long horizontalTables) {
        this.horizontalTables = horizontalTables;
    }

    public void setHorizontalPies(Long horizontalPies) {
        this.horizontalPies = horizontalPies;
    }

    public Boolean isglobalAnalysisTimeFrame() {
        return this.isGlobalAnalysisTimeFrame;
    }

    public void isGlobalAnalysisTimeFrame(Boolean isglobalAnalysisTimeFrame) {
        this.isGlobalAnalysisTimeFrame = isglobalAnalysisTimeFrame;
    }

    public AnalysisTimeFrame getGlobalAnalysisTimeFrame() {
        return this.globalAnalysisTimeFrame;
    }

    public void setGlobalAnalysisTimeFrame(AnalysisTimeFrame globalAnalysisTimeFrame) {
        this.globalAnalysisTimeFrame = globalAnalysisTimeFrame;
        this.changed.set(true);
    }

    public void setGlobalAnalysisTimeFrameNOEVENT(AnalysisTimeFrame globalAnalysisTimeFrame) {
        this.globalAnalysisTimeFrame = globalAnalysisTimeFrame;
    }

    public boolean isChanged() {
        return this.changed.get();
    }

    public void setChanged(boolean changed) {
        this.changed.set(changed);
    }

    public SimpleBooleanProperty changedProperty() {
        return this.changed;
    }


}

