/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.application.Chart.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.application.Chart.AnalysisTimeFrame;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.ChartSettings;
import org.jevis.application.Chart.ChartType;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.application.jevistree.AlphanumComparator;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.json.JsonAnalysisDataRow;
import org.jevis.commons.json.JsonChartDataModel;
import org.jevis.commons.json.JsonChartSettings;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.ws.json.JsonUnit;
import org.joda.time.DateTime;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author broder
 */
public class GraphDataModel extends Observable {

    private static final Logger logger = LogManager.getLogger(GraphDataModel.class);
    private static SaveResourceBundle rb = new SaveResourceBundle(AppLocale.BUNDLE_ID, AppLocale.getInstance().getLocale());
    private Set<ChartDataModel> selectedData = new HashSet<>();
    private Set<ChartSettings> charts = new HashSet<>();
    private Boolean hideShowIcons = true;
    private Boolean autoResize = true;
    private ObservableList<String> selectedDataNames = FXCollections.observableArrayList(new ArrayList<>());
    private AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame();
    private JEVisDataSource ds;
    private List<JEVisObject> listAnalyses = new ArrayList<>();
    private ObservableList<String> observableListAnalyses = FXCollections.observableArrayList();
    private JsonChartDataModel listAnalysisModel;
    private List<JsonChartSettings> listChartsSettings;
    private LocalTime workdayStart = LocalTime.of(0, 0, 0, 0);
    private LocalTime workdayEnd = LocalTime.of(23, 59, 59, 999999999);
    private JEVisObject currentAnalysis;

    public GraphDataModel(JEVisDataSource ds) {
        this.ds = ds;
    }

    public Set<ChartDataModel> getSelectedData() {
        if (selectedData == null || selectedData.isEmpty()) {

            updateSelectedData();

            if (selectedData != null && !selectedData.isEmpty() && getListAnalysisModel() != null && getListAnalysisModel().getAnalysisTimeFrame() != null
                    && getListAnalysisModel().getAnalysisTimeFrame().getTimeframe() != null) {
                AnalysisTimeFrame newATF = new AnalysisTimeFrame();
                try {
                    newATF.setTimeFrame(newATF.parseTimeFrameFromString(getListAnalysisModel().getAnalysisTimeFrame().getTimeframe()));
                    newATF.setId(Long.parseLong(getListAnalysisModel().getAnalysisTimeFrame().getId()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                setAnalysisTimeFrame(newATF);
            }
        }

        return this.selectedData;
    }

    public void setSelectedData(Set<ChartDataModel> selectedData) {
        this.selectedData = selectedData;

        updateSelectedDataNames();

        setChanged();
        notifyObservers();
    }

    private void updateSelectedDataNames() {
        selectedDataNames.clear();

        if (getSelectedData() != null) {
            for (ChartDataModel mdl : getSelectedData()) {
                if (mdl.getSelected()) {
                    boolean found = false;
                    for (String chartName : mdl.getSelectedcharts()) {
                        for (ChartSettings set : getCharts()) {
                            if (chartName.equals(set.getName())) {
                                if (!selectedDataNames.contains(set.getName())) {

                                    selectedDataNames.add(set.getName());
                                    found = true;
                                }
                            }
                        }
                        if (!found) {
                            if (!selectedDataNames.contains(chartName)) {
                                getCharts().add(new ChartSettings(chartName));
                                selectedDataNames.add(chartName);
                            }
                        }
                    }
                }
            }
        }

        AlphanumComparator ac = new AlphanumComparator();
        selectedDataNames.sort(ac);
    }

    public void updateSelectedData() {
        Set<ChartDataModel> selectedData = new HashSet<>();

        if (getListAnalysisModel() != null) {
            Map<String, ChartDataModel> data = new HashMap<>();

            for (JsonAnalysisDataRow mdl : getListAnalysisModel().getListAnalyses()) {
                ChartDataModel newData = new ChartDataModel();

                try {
                    Long id = Long.parseLong(mdl.getObject());
                    Long id_dp = null;
                    if (mdl.getDataProcessorObject() != null) id_dp = Long.parseLong(mdl.getDataProcessorObject());
                    JEVisObject obj = ds.getObject(id);
                    JEVisObject obj_dp = null;
                    if (mdl.getDataProcessorObject() != null) obj_dp = ds.getObject(id_dp);
                    JEVisUnit unit = new JEVisUnitImp(new Gson().fromJson(mdl.getUnit(), JsonUnit.class));
                    DateTime start;
                    start = DateTime.parse(mdl.getSelectedStart());
                    DateTime end;
                    end = DateTime.parse(mdl.getSelectedEnd());
                    Boolean selected = Boolean.parseBoolean(mdl.getSelected());
                    newData.setObject(obj);

                    newData.setSelectedStart(start);
                    newData.setSelectedEnd(end);

                    newData.setColor(Color.valueOf(mdl.getColor()));
                    newData.setTitle(mdl.getName());
                    if (mdl.getDataProcessorObject() != null) newData.setDataProcessor(obj_dp);
                    newData.getAttribute();
                    newData.setAggregationPeriod(AggregationPeriod.parseAggregation(mdl.getAggregation()));
                    newData.setSelected(selected);
                    newData.setSomethingChanged(true);
                    newData.setSelectedCharts(stringToList(mdl.getSelectedCharts()));
                    newData.setUnit(unit);
                    data.put(obj.getID().toString(), newData);
                } catch (JEVisException e) {
                    logger.error("Error: could not get chart data model", e);
                }
            }

            for (Map.Entry<String, ChartDataModel> entrySet : data.entrySet()) {
                ChartDataModel value = entrySet.getValue();
                if (value.getSelected()) {
                    selectedData.add(value);
                }
            }

            if (getListAnalysisModel().getAnalysisTimeFrame() != null) {
                try {
                    AnalysisTimeFrame newATF = new AnalysisTimeFrame();
                    newATF.setTimeFrame(newATF.parseTimeFrameFromString(getListAnalysisModel().getAnalysisTimeFrame().getTimeframe()));
                    newATF.setId(Long.parseLong(getListAnalysisModel().getAnalysisTimeFrame().getId()));
                    analysisTimeFrame = newATF;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        this.selectedData = selectedData;
    }

    public void updateSamples() {
        selectedData.forEach(chartDataModel -> {
            chartDataModel.setSomethingChanged(true);
            chartDataModel.getSamples();
        });

        setChanged();
        notifyObservers();
    }

    public Set<ChartSettings> getCharts() {
        if (charts == null) updateCharts();

        return charts;
    }

    public void setCharts(Set<ChartSettings> charts) {
        this.charts = charts;
    }

    private void updateCharts() {
        if (charts == null || charts.isEmpty()) {
            try {
                //ds.reloadAttributes();
                if (getCurrentAnalysis() != null) {
                    if (Objects.nonNull(getCurrentAnalysis().getAttribute("Charts"))) {
                        ds.reloadAttribute(getCurrentAnalysis().getAttribute("Charts"));
                        if (getCurrentAnalysis().getAttribute("Charts").hasSample()) {
                            String str = getCurrentAnalysis().getAttribute("Charts").getLatestSample().getValueAsString();
                            try {
                                if (str.startsWith("[")) {
                                    listChartsSettings = new Gson().fromJson(str, new TypeToken<List<JsonChartSettings>>() {
                                    }.getType());

                                } else {
                                    listChartsSettings = new ArrayList<>();
                                    listChartsSettings.add(new Gson().fromJson(str, JsonChartSettings.class));
                                }
                            } catch (Exception e) {
                                logger.error("Error: could not read chart settings", e);
                            }
                        }
                    }
                    updateWorkDays();
                }
            } catch (JEVisException e) {
                logger.error("Error: could not get analysis model", e);
            }

            Map<String, ChartSettings> chartSettingsHashMap = new HashMap<>();
            Set<ChartSettings> chartSettings = new HashSet<>();

            if (listChartsSettings != null && !listChartsSettings.isEmpty()) {
                for (JsonChartSettings settings : listChartsSettings) {
                    ChartSettings newSettings = new ChartSettings("");
                    newSettings.setName(settings.getName());
                    newSettings.setChartType(ChartType.parseChartType(settings.getChartType()));

                    if (settings.getHeight() != null)
                        newSettings.setHeight(Double.parseDouble(settings.getHeight()));
                    chartSettingsHashMap.put(newSettings.getName(), newSettings);
                }

                for (Map.Entry<String, ChartSettings> entrySet : chartSettingsHashMap.entrySet()) {
                    ChartSettings value = entrySet.getValue();
                    chartSettings.add(value);
                }
                charts = chartSettings;
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

    public Boolean getHideShowIcons() {
        return hideShowIcons;
    }

    public void setHideShowIcons(Boolean hideShowIcons) {
        this.hideShowIcons = hideShowIcons;

        setChanged();
        notifyObservers();
    }

    public Boolean getAutoResize() {
        return autoResize;
    }

    public void setAutoResize(Boolean resize) {
        this.autoResize = resize;

        setChanged();
        notifyObservers();
    }

    public ObservableList<String> getChartsList() {

        return selectedDataNames;
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
        AtomicReference<ChartDataModel> out = new AtomicReference<>();
        getSelectedData().forEach(chartDataModel -> {
            if (chartDataModel.getObject().getID().equals(id)) {
                out.set(chartDataModel);
            }
        });
        return out.get();
    }

    public AnalysisTimeFrame getAnalysisTimeFrame() {
        return analysisTimeFrame;
    }

    public void setAnalysisTimeFrame(AnalysisTimeFrame analysisTimeFrame) {
        this.analysisTimeFrame = analysisTimeFrame;
        /**
         * analysisTimeFrame is used in the updateStartEndToDataModel function
         */

        if (selectedData != null && !selectedData.isEmpty()) {
            DateHelper dateHelper = new DateHelper();
            setMinMaxForDateHelper(dateHelper);
            if (getWorkdayStart() != null) dateHelper.setStartTime(getWorkdayStart());
            if (getWorkdayEnd() != null) dateHelper.setEndTime(getWorkdayEnd());

            switch (analysisTimeFrame.getTimeFrame()) {
                //Custom
                case custom:
                    break;
                //today
                case today:
                    dateHelper.setType(DateHelper.TransformType.TODAY);
                    updateStartEndToDataModel(dateHelper);
                    break;
                //last 7 days
                case last7Days:
                    dateHelper.setType(DateHelper.TransformType.LAST7DAYS);
                    updateStartEndToDataModel(dateHelper);
                    break;
                //last 30 days
                case last30Days:
                    dateHelper.setType(DateHelper.TransformType.LAST30DAYS);
                    updateStartEndToDataModel(dateHelper);
                    break;
                //yesterday
                case yesterday:
                    dateHelper.setType(DateHelper.TransformType.YESTERDAY);
                    updateStartEndToDataModel(dateHelper);
                    break;
                //last Week days
                case lastWeek:
                    dateHelper.setType(DateHelper.TransformType.LASTWEEK);
                    updateStartEndToDataModel(dateHelper);
                    break;
                case lastMonth:
                    //last Month
                    dateHelper.setType(DateHelper.TransformType.LASTMONTH);
                    updateStartEndToDataModel(dateHelper);
                    break;
                case customStartEnd:
                    if (analysisTimeFrame.getId() != 0l) {
                        try {
                            dateHelper.setType(DateHelper.TransformType.CUSTOM_PERIOD);
                            CustomPeriodObject cpo = new CustomPeriodObject(ds.getObject(analysisTimeFrame.getId()), new ObjectHandler(ds));
                            dateHelper.setCustomPeriodObject(cpo);

                            updateStartEndToDataModel(dateHelper);
                        } catch (Exception e) {
                            logger.error("Error getting custom period object: " + e);
                        }
                        break;
                    }
            }
        }
    }

    private void setMinMaxForDateHelper(DateHelper dateHelper) {
        DateTime min = null;
        DateTime max = null;
        for (ChartDataModel mdl : selectedData) {
            if (mdl.getSelected()) {
                JEVisAttribute att = mdl.getAttribute();

                DateTime min_check = new DateTime(
                        att.getTimestampFromFirstSample().getYear(),
                        att.getTimestampFromFirstSample().getMonthOfYear(),
                        att.getTimestampFromFirstSample().getDayOfMonth(),
                        att.getTimestampFromFirstSample().getHourOfDay(),
                        att.getTimestampFromFirstSample().getMinuteOfHour(),
                        att.getTimestampFromFirstSample().getSecondOfMinute());

                DateTime max_check = new DateTime(
                        att.getTimestampFromLastSample().getYear(),
                        att.getTimestampFromLastSample().getMonthOfYear(),
                        att.getTimestampFromLastSample().getDayOfMonth(),
                        att.getTimestampFromLastSample().getHourOfDay(),
                        att.getTimestampFromLastSample().getMinuteOfHour(),
                        att.getTimestampFromLastSample().getSecondOfMinute());

                if (min == null || min_check.isBefore(min)) min = min_check;
                if (max == null || max_check.isAfter(max)) max = max_check;
            }
        }

        if (min != null && max != null) {
            dateHelper.setMinStartDateTime(min);
            dateHelper.setMaxEndDateTime(max);
        }

    }

    private void updateStartEndToDataModel(DateHelper dh) {
        DateTime start = dh.getStartDate();
        DateTime end = dh.getEndDate();

//        Disabled for now....

//        DateTime start;
//        DateTime end;
//        if (dh.getStartDate().isAfter(dh.getMinStartDateTime())) start = dh.getStartDate();
//        else start = dh.getMinStartDateTime();
//        if (dh.getEndDate().isBefore(dh.getMaxEndDateTime())) end = dh.getEndDate();
//        else {
//            end = dh.getMaxEndDateTime();
//            if (end != null && getAnalysisTimeFrame() != null && getAnalysisTimeFrame().getTimeFrame() != null) {
//                switch (getAnalysisTimeFrame().getTimeFrame()) {
//                    case today:
//                        start = new DateTime(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth(),
//                                getWorkdayStart().getHour(), getWorkdayStart().getMinute(), getWorkdayStart().getSecond());
//                        if (getWorkdayStart().isAfter(getWorkdayEnd())) start = start.minusDays(1);
//                        break;
//                    case last7Days:
//                        start = new DateTime(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth(),
//                                getWorkdayStart().getHour(), getWorkdayStart().getMinute(), getWorkdayStart().getSecond())
//                                .minusDays(6);
//
//                        if (getWorkdayStart().isAfter(getWorkdayEnd())) start = start.minusDays(1);
//                        break;
//                    case last30Days:
//
//                        start = new DateTime(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth(),
//                                getWorkdayStart().getHour(), getWorkdayStart().getMinute(), getWorkdayStart().getSecond())
//                                .minusDays(29);
//
//                        if (getWorkdayStart().isAfter(getWorkdayEnd())) start = start.minusDays(1);
//                        break;
//                    case yesterday:
//                        start = end;
//                        if (getWorkdayStart().isAfter(getWorkdayEnd())) start = start.minusDays(1);
//                        break;
//                    case lastWeek:
//                        start = new DateTime(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth(), getWorkdayStart().getHour(),
//                                getWorkdayStart().getMinute(), getWorkdayStart().getSecond())
//                                .minusDays(end.getDayOfWeek() - 1).minusWeeks(1);
//                        if (getWorkdayStart().isAfter(getWorkdayEnd())) start = start.minusDays(1);
//                        break;
//                    case lastMonth:
//                        start = new DateTime(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth(), getWorkdayStart().getHour(),
//                                getWorkdayStart().getMinute(), getWorkdayStart().getSecond())
//                                .minusMonths(1).minusDays(end.getDayOfMonth() - 1);
//                        if (getWorkdayStart().isAfter(getWorkdayEnd())) start = start.minusDays(1);
//                        break;
//                }
//            }
//        }
//        DateTime finalStart = start;
//        getSelectedData().forEach(chartDataModel -> {
//            if (chartDataModel.getSelected()) {
//                chartDataModel.setSelectedStart(finalStart);
//                chartDataModel.setSelectedEnd(end);
//                chartDataModel.setSomethingChanged(true);
//            }
//        });

        getSelectedData().forEach(chartDataModel -> {
            if (chartDataModel.getSelected()) {
                chartDataModel.setSelectedStart(start);
                chartDataModel.setSelectedEnd(end);
                chartDataModel.setSomethingChanged(true);
            }
        });
    }

    private void updateWorkDays() {
        try {
            JEVisObject site = getCurrentAnalysis().getParents().get(0).getParents().get(0);
            LocalTime start = null;
            LocalTime end = null;
            try {
                JEVisAttribute attStart = site.getAttribute("Workday Beginning");
                JEVisAttribute attEnd = site.getAttribute("Workday End");
                if (attStart.hasSample()) {
                    String startStr = attStart.getLatestSample().getValueAsString();
                    DateTime dtStart = DateTime.parse(startStr);
                    start = LocalTime.of(dtStart.getHourOfDay(), dtStart.getMinuteOfHour(), 0, 0);
                }
                if (attEnd.hasSample()) {
                    String endStr = attEnd.getLatestSample().getValueAsString();
                    DateTime dtEnd = DateTime.parse(endStr);
                    end = LocalTime.of(dtEnd.getHourOfDay(), dtEnd.getMinuteOfHour(), 59, 999999999);
                }
            } catch (Exception e) {
            }

            if (start != null && end != null) {
                workdayStart = start;
                workdayEnd = end;
            }
        } catch (Exception e) {

        }
    }

    public void updateWorkDaysFirstRun() {
        try {
            JEVisObject site = listAnalyses.get(0).getParents().get(0).getParents().get(0);
            LocalTime start = null;
            LocalTime end = null;
            try {
                JEVisAttribute attStart = site.getAttribute("Workday Beginning");
                JEVisAttribute attEnd = site.getAttribute("Workday End");
                if (attStart.hasSample()) {
                    String startStr = attStart.getLatestSample().getValueAsString();
                    DateTime dtStart = DateTime.parse(startStr);
                    start = LocalTime.of(dtStart.getHourOfDay(), dtStart.getMinuteOfHour(), 0, 0);
                }
                if (attEnd.hasSample()) {
                    String endStr = attEnd.getLatestSample().getValueAsString();
                    DateTime dtEnd = DateTime.parse(endStr);
                    end = LocalTime.of(dtEnd.getHourOfDay(), dtEnd.getMinuteOfHour(), 59, 999999999);
                }
            } catch (Exception e) {
            }

            if (start != null && end != null) {
                workdayStart = start;
                workdayEnd = end;
            }
        } catch (Exception e) {

        }
    }

    public void updateListAnalyses() {
        List<JEVisObject> listAnalysesDirectories = new ArrayList<>();
        try {
            JEVisClass analysesDirectory = ds.getJEVisClass("Analyses Directory");
            listAnalysesDirectories = ds.getObjects(analysesDirectory, false);
        } catch (JEVisException e) {
            logger.error("Error: could not get analyses directories", e);
        }
        if (listAnalysesDirectories.isEmpty()) {
            List<JEVisObject> listBuildings = new ArrayList<>();
            try {
                JEVisClass building = ds.getJEVisClass("Building");
                listBuildings = ds.getObjects(building, false);

                if (!listBuildings.isEmpty()) {
                    JEVisClass analysesDirectory = ds.getJEVisClass("Analyses Directory");
                    JEVisObject analysesDir = listBuildings.get(0).buildObject(rb.getString("plugin.graph.analysesdir.defaultname"), analysesDirectory);
                    analysesDir.commit();
                }
            } catch (JEVisException e) {
                logger.error("Error: could not create new analyses directory", e);
            }

        }
        try {
//            ds.reloadAttributes();
            listAnalyses = ds.getObjects(ds.getJEVisClass("Analysis"), false);
        } catch (JEVisException e) {
            logger.error("Error: could not get analysis", e);
        }
        observableListAnalyses.clear();
        for (JEVisObject obj : listAnalyses) {
            observableListAnalyses.add(obj.getName());
        }
        Collections.sort(observableListAnalyses, new AlphanumComparator());
    }

    public JsonChartDataModel getListAnalysisModel() {

        JsonChartDataModel tempModel = null;
        try {
//            ds.reloadAttributes();

            if (charts == null || charts.isEmpty()) {
                updateCharts();
            }
            if (getCurrentAnalysis() != null) {
                if (Objects.nonNull(getCurrentAnalysis().getAttribute("Data Model"))) {
                    ds.reloadAttribute(getCurrentAnalysis().getAttribute("Data Model"));
                    if (getCurrentAnalysis().getAttribute("Data Model").hasSample()) {
                        String str = getCurrentAnalysis().getAttribute("Data Model").getLatestSample().getValueAsString();
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
                updateWorkDays();
            }
        } catch (JEVisException e) {
            logger.error("Error: could not get analysis model", e);
        }

        this.listAnalysisModel = tempModel;

        return listAnalysisModel;
    }

    public void setJEVisObjectForCurrentAnalysis(String s) {
        JEVisObject currentAnalysis = null;
        if (listAnalyses == null || listAnalyses.isEmpty()) updateListAnalyses();
        for (JEVisObject obj : listAnalyses) {
            if (obj.getName().equals(s)) {
                currentAnalysis = obj;
            }
        }
        this.currentAnalysis = currentAnalysis;

        if (listAnalysisModel == null) {
            getListAnalysisModel();
            updateSelectedData();
        }
    }

    public void removeUnusedCharts() {
        Set<ChartSettings> chartsNew = new HashSet<>();
        selectedData.forEach(chartDataModel -> {
            if (chartDataModel.getSelected())
                chartDataModel.getSelectedcharts().forEach(s -> {
                    charts.forEach(chartSettings -> {
                        chartsNew.add(chartSettings);
                    });
                });
        });
        charts = chartsNew;
    }

    public ObservableList<String> getObservableListAnalyses() {
        updateListAnalyses();
        return observableListAnalyses;
    }

    public ObservableList<String> getListAnalyses() {
        return observableListAnalyses;
    }

    public LocalTime getWorkdayStart() {
        return workdayStart;
    }

    public LocalTime getWorkdayEnd() {
        return workdayEnd;
    }

    public JEVisObject getCurrentAnalysis() {
//        if (currentAnalysis == null) {
//            updateListAnalyses();
//            if (!observableListAnalyses.isEmpty())
//                setJEVisObjectForCurrentAnalysis(observableListAnalyses.get(0));
//        }
        return currentAnalysis;
    }

    public void setCurrentAnalysis(JEVisObject currentAnalysis) {
        this.currentAnalysis = currentAnalysis;
    }


    private List<String> stringToList(String s) {
        if (Objects.nonNull(s)) {
            List<String> tempList = new ArrayList<>(Arrays.asList(s.split(", ")));
            for (String str : tempList) if (str.contains(", ")) str.replace(", ", "");
            return tempList;
        } else return new ArrayList<>();
    }

    public void selectNone() {
        getSelectedData().forEach(mdl -> {
            mdl.setSelected(false);
        });
    }

    public AggregationPeriod getAggregationPeriod() {
        if (getSelectedData() != null && !getSelectedData().isEmpty()) {
            for (ChartDataModel chartDataModel : getSelectedData()) {
                return chartDataModel.getAggregationPeriod();
            }
        }
        return AggregationPeriod.NONE;
    }

    public void setAggregationPeriod(AggregationPeriod aggregationPeriod) {
        if (getSelectedData() != null && !getSelectedData().isEmpty()) {
            getSelectedData().forEach(chartDataModel -> chartDataModel.setAggregationPeriod(aggregationPeriod));
        }
    }
}
