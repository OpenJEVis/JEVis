/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.application.Chart.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.json.JsonAnalysisDataRow;
import org.jevis.commons.json.JsonChartDataModel;
import org.jevis.commons.json.JsonChartSettings;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.ws.json.JsonUnit;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.ChartSettings;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.jevistree.AlphanumComparator;
import org.jevis.jeconfig.tool.I18n;
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
    private Set<ChartDataModel> selectedData = new HashSet<>();
    private List<ChartSettings> charts = new ArrayList<>();
    private Boolean hideShowIcons = true;
    private ManipulationMode addSeries = ManipulationMode.NONE;
    private Boolean autoResize = true;
    private AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(AnalysisTimeFrame.TimeFrame.last7Days);
    private JEVisDataSource ds;
    private ObservableList<JEVisObject> observableListAnalyses = FXCollections.observableArrayList();
    private JsonChartDataModel listAnalysisModel = new JsonChartDataModel();
    private List<JsonChartSettings> listChartsSettings = new ArrayList<>();
    private LocalTime workdayStart = LocalTime.of(0, 0, 0, 0);
    private LocalTime workdayEnd = LocalTime.of(23, 59, 59, 999999999);
    private JEVisObject currentAnalysis = null;
    private Boolean multipleDirectories = false;

    public GraphDataModel(JEVisDataSource ds) {
        this.ds = ds;
    }

    public Set<ChartDataModel> getSelectedData() {
        if (selectedData == null || selectedData.isEmpty()) {

            updateSelectedData();

            //JsonChartDataModel jsonChartDataModel = getListAnalysisModel();

            if (selectedData != null && !selectedData.isEmpty() && listAnalysisModel != null && listAnalysisModel.getAnalysisTimeFrame() != null
                    && listAnalysisModel.getAnalysisTimeFrame().getTimeframe() != null) {
                AnalysisTimeFrame newATF = new AnalysisTimeFrame();
                try {
                    newATF.setTimeFrame(newATF.parseTimeFrameFromString(listAnalysisModel.getAnalysisTimeFrame().getTimeframe()));
                    newATF.setId(Long.parseLong(listAnalysisModel.getAnalysisTimeFrame().getId()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                setAnalysisTimeFrame(newATF);
            }
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

        System.gc();

        setChanged();

        notifyObservers();

    }

    public void updateSelectedData() {
        Set<ChartDataModel> selectedData = new HashSet<>();

        JsonChartDataModel jsonChartDataModel = getListAnalysisModel();

        if (jsonChartDataModel != null) {
            Map<String, ChartDataModel> data = new HashMap<>();

            for (JsonAnalysisDataRow mdl : jsonChartDataModel.getListAnalyses()) {
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
                    newData.setObject(obj);

                    newData.setSelectedStart(start);
                    newData.setSelectedEnd(end);

                    newData.setColor(Color.valueOf(mdl.getColor()));
                    newData.setTitle(mdl.getName());
                    if (mdl.getDataProcessorObject() != null) newData.setDataProcessor(obj_dp);
                    newData.getAttribute();
                    newData.setAggregationPeriod(AggregationPeriod.parseAggregation(mdl.getAggregation()));
                    newData.setSomethingChanged(true);
                    newData.setSelectedCharts(stringToList(mdl.getSelectedCharts()));
                    newData.setUnit(unit);
                    if (mdl.getAxis() != null) newData.setAxis(Integer.parseInt(mdl.getAxis()));
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

            if (jsonChartDataModel.getAnalysisTimeFrame() != null) {
                try {
                    AnalysisTimeFrame newATF = new AnalysisTimeFrame();
                    newATF.setTimeFrame(newATF.parseTimeFrameFromString(jsonChartDataModel.getAnalysisTimeFrame().getTimeframe()));
                    newATF.setId(Long.parseLong(jsonChartDataModel.getAnalysisTimeFrame().getId()));
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
    }

    public List<ChartSettings> getCharts() {
        if (charts == null) updateCharts();

        return charts;
    }

    public void setCharts(List<ChartSettings> charts) {
        this.charts = charts;
    }

    private void updateCharts() {
        if (charts == null || charts.isEmpty()) {
            try {
                if (getCurrentAnalysis() != null) {
                    if (Objects.nonNull(getCurrentAnalysis().getAttribute("Charts"))) {
//                        ds.reloadAttribute(getCurrentAnalysis().getAttribute("Charts"));
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

            List<ChartSettings> chartSettings = new ArrayList<>();

            if (listChartsSettings != null && !listChartsSettings.isEmpty()) {
                boolean needsIds = false;
                for (JsonChartSettings settings : listChartsSettings) {
                    ChartSettings newSettings = new ChartSettings("");
                    if (settings.getId() != null)
                        newSettings.setId(Integer.parseInt(settings.getId()));
                    else needsIds = true;

                    newSettings.setName(settings.getName());
                    newSettings.setChartType(ChartType.parseChartType(settings.getChartType()));

                    if (settings.getHeight() != null)
                        newSettings.setHeight(Double.parseDouble(settings.getHeight()));
                    chartSettings.add(newSettings);
                }

                if (needsIds) {
                    AlphanumComparator ac = new AlphanumComparator();
                    chartSettings.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));
                    for (ChartSettings set : chartSettings) set.setId(chartSettings.indexOf(set));
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

    public ManipulationMode getAddSeries() {
        return addSeries;
    }

    public void setAddSeries(ManipulationMode addSeries) {
        this.addSeries = addSeries;

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
                case preview:
                    try {
                        AtomicReference<DateTime> start = new AtomicReference<>(DateTime.now().minusDays(1));
                        AtomicReference<DateTime> end = new AtomicReference<>(DateTime.now());
                        getSelectedData().forEach(chartDataModel -> {
                            JEVisAttribute valueAtt = chartDataModel.getAttribute();
                            if (valueAtt != null) {
                                if (valueAtt.getTimestampFromLastSample().isBefore(end.get()))
                                    end.set(valueAtt.getTimestampFromLastSample());
                            }
                        });

                        start.set(end.get().minusDays(1));

                        getSelectedData().forEach(chartDataModel -> {
                            JEVisAttribute valueAtt = chartDataModel.getAttribute();
                            if (valueAtt != null) {
                                if (valueAtt.getTimestampFromFirstSample().isAfter(start.get()))
                                    start.set(valueAtt.getTimestampFromFirstSample());
                            }
                        });

                        getSelectedData().forEach(chartDataModel -> {
                            if (!chartDataModel.getSelectedcharts().isEmpty()) {
                                chartDataModel.setSelectedStart(start.get());
                                chartDataModel.setSelectedEnd(end.get());
                                chartDataModel.setSomethingChanged(true);
                            }
                        });
                    } catch (Exception e) {
                        logger.error("Error: " + e);
                    }
                    break;
            }
        }
    }

    private void setMinMaxForDateHelper(DateHelper dateHelper) {
        DateTime min = null;
        DateTime max = null;
        for (ChartDataModel mdl : selectedData) {
            if (!mdl.getSelectedcharts().isEmpty()) {
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
            if (!chartDataModel.getSelectedcharts().isEmpty()) {
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
            JEVisObject site = observableListAnalyses.get(0).getParents().get(0).getParents().get(0);
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

            if (listAnalysesDirectories.size() > 1) {
                multipleDirectories = true;
            }
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
                    JEVisObject analysesDir = listBuildings.get(0).buildObject(I18n.getInstance().getString("plugin.graph.analysesdir.defaultname"), analysesDirectory);
                    analysesDir.commit();
                }
            } catch (JEVisException e) {
                logger.error("Error: could not create new analyses directory", e);
            }

        }
        try {
            observableListAnalyses.clear();
            observableListAnalyses.addAll(ds.getObjects(ds.getJEVisClass("Analysis"), false));

        } catch (JEVisException e) {
            logger.error("Error: could not get analysis", e);
        }

        AlphanumComparator ac = new AlphanumComparator();
        if (!multipleDirectories) observableListAnalyses.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));
        else {
            observableListAnalyses.sort((o1, o2) -> {

                String prefix1 = "";
                String prefix2 = "";

                try {
                    JEVisObject secondParent1 = o1.getParents().get(0).getParents().get(0);
                    JEVisClass buildingClass = ds.getJEVisClass("Building");
                    JEVisClass organisationClass = ds.getJEVisClass("Organization");

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
                    JEVisClass buildingClass = ds.getJEVisClass("Building");
                    JEVisClass organisationClass = ds.getJEVisClass("Organization");

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

            if (charts == null || charts.isEmpty()) {
                updateCharts();
            }
            if (getCurrentAnalysis() != null) {
                if (Objects.nonNull(getCurrentAnalysis().getAttribute("Data Model"))) {
//                    ds.reloadAttribute(getCurrentAnalysis().getAttribute("Data Model"));
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

    public ObservableList<JEVisObject> getObservableListAnalyses() {
        if (observableListAnalyses.isEmpty()) updateListAnalyses();
        return observableListAnalyses;
    }

    public LocalTime getWorkdayStart() {
        return workdayStart;
    }

    public LocalTime getWorkdayEnd() {
        return workdayEnd;
    }

    public JEVisObject getCurrentAnalysis() {

        return currentAnalysis;
    }

    public void setCurrentAnalysis(JEVisObject currentAnalysis) {
        this.currentAnalysis = currentAnalysis;

        try {
            ds.reloadAttribute(currentAnalysis.getAttribute("Data Model"));
        } catch (Exception ex) {
            logger.error(ex);
        }

        if (observableListAnalyses == null || observableListAnalyses.isEmpty()) updateListAnalyses();
//        if (listAnalysisModel == null) {
        getListAnalysisModel();
        updateSelectedData();
//        }
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
                    for (ChartSettings set : charts) {
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

    public Boolean getMultipleDirectories() {
        return multipleDirectories;
    }
}
