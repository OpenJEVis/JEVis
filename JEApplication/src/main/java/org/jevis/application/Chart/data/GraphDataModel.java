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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.application.Chart.AnalysisTimeFrame;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.ChartSettings;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.application.jevistree.AlphanumComparator;
import org.jevis.commons.json.JsonAnalysisDataRow;
import org.jevis.commons.json.JsonChartDataModel;
import org.jevis.commons.json.JsonChartSettings;
import org.joda.time.DateTime;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author broder
 */
public class GraphDataModel extends Observable {

    private static SaveResourceBundle rb = new SaveResourceBundle(AppLocale.BUNDLE_ID, AppLocale.getInstance().getLocale());
    private final Logger logger = LogManager.getLogger(GraphDataModel.class);
    private Set<ChartDataModel> selectedData = new HashSet<>();
    private Set<ChartSettings> charts = new HashSet<>();
    private Boolean hideShowIcons = true;
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
    private String nameCurrentAnalysis;

    public GraphDataModel(JEVisDataSource ds) {
        this.ds = ds;
    }


    public Set<ChartDataModel> getSelectedData() {
        return selectedData;
    }

    public void setSelectedData(Set<ChartDataModel> selectedData) {
        this.selectedData = selectedData;
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

        setChanged();
        notifyObservers();
    }

    public Set<ChartSettings> getCharts() {
        return charts;
    }

    public void setCharts(Set<ChartSettings> charts) {
        this.charts = charts;
    }

    public Boolean getHideShowIcons() {
        return hideShowIcons;
    }

    public void setHideShowIcons(Boolean hideShowIcons) {
        this.hideShowIcons = hideShowIcons;
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
    }

    public void getListAnalysis() {
        try {
            ds.reloadAttributes();
            if (currentAnalysis == null) {
                updateListAnalyses();
                if (!observableListAnalyses.isEmpty())
                    setJEVisObjectForCurrentAnalysis(observableListAnalyses.get(0));
            }
            if (currentAnalysis != null) {
                if (Objects.nonNull(currentAnalysis.getAttribute("Data Model"))) {
                    if (currentAnalysis.getAttribute("Data Model").hasSample()) {
                        String str = currentAnalysis.getAttribute("Data Model").getLatestSample().getValueAsString();
                        try {
                            if (str.startsWith("[")) {
                                listAnalysisModel = new JsonChartDataModel();
                                List<JsonAnalysisDataRow> listOld = new Gson().fromJson(str, new TypeToken<List<JsonAnalysisDataRow>>() {
                                }.getType());
                                listAnalysisModel.setListDataRows(listOld);
                            } else {
                                try {
                                    listAnalysisModel = new Gson().fromJson(str, new TypeToken<JsonChartDataModel>() {
                                    }.getType());
                                } catch (Exception e) {
                                    logger.error(e);
                                    listAnalysisModel = new JsonChartDataModel();
                                    listAnalysisModel.getListAnalyses().add(new Gson().fromJson(str, JsonAnalysisDataRow.class));
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Error: could not read data model", e);
                        }
                    }
                }
                if (Objects.nonNull(currentAnalysis.getAttribute("Charts"))) {
                    if (currentAnalysis.getAttribute("Charts").hasSample()) {
                        String str = currentAnalysis.getAttribute("Charts").getLatestSample().getValueAsString();
                        try {
                            if (str.endsWith("]")) {
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
                try {
                    JEVisObject site = currentAnalysis.getParents().get(0).getParents().get(0);
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
        } catch (JEVisException e) {
            logger.error("Error: could not get analysis model", e);
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
        return listAnalysisModel;
    }

    public void setJEVisObjectForCurrentAnalysis(String s) {
        JEVisObject currentAnalysis = null;
        for (JEVisObject obj : listAnalyses) {
            if (obj.getName().equals(s)) {
                currentAnalysis = obj;
            }
        }
        this.currentAnalysis = currentAnalysis;
    }

    public ObservableList<String> getObservableListAnalyses() {
        return observableListAnalyses;
    }

    public List<JsonChartSettings> getListChartsSettings() {
        return listChartsSettings;
    }

    public LocalTime getWorkdayStart() {
        return workdayStart;
    }

    public LocalTime getWorkdayEnd() {
        return workdayEnd;
    }

    public List<JEVisObject> getListAnalyses() {
        return listAnalyses;
    }

    public String getNameCurrentAnalysis() {
        this.nameCurrentAnalysis = currentAnalysis.getName();
        return nameCurrentAnalysis;
    }

    public void setNameCurrentAnalysis(String nameCurrentAnalysis) {
        this.nameCurrentAnalysis = nameCurrentAnalysis;
    }

    public JEVisObject getCurrentAnalysis() {
        return currentAnalysis;
    }

    public void setCurrentAnalysis(JEVisObject currentAnalysis) {
        this.currentAnalysis = currentAnalysis;
    }
}
