/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecc.application.Chart.data;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.hansolo.fx.charts.tools.ColorMapping;
import javafx.geometry.Orientation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.chart.BubbleType;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.json.JsonAnalysisDataRow;
import org.jevis.commons.json.JsonChartDataModel;
import org.jevis.commons.json.JsonChartSetting;
import org.jevis.commons.json.JsonChartSettings;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.commons.ws.json.JsonUnit;
import org.jevis.jecc.application.Chart.ChartSetting;
import org.jevis.jecc.application.Chart.ChartSettings;
import org.jevis.jecc.application.Chart.ChartType;
import org.jevis.jecc.application.tools.ColorHelper;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static eu.hansolo.fx.charts.tools.ColorMapping.*;

/**
 * @author broder
 */
public class AnalysisDataModel {
    public static final String CHARTS_ATTRIBUTE_NAME = "Charts";
    public static final String DATA_MODEL_ATTRIBUTE_NAME = "Data Model";
    public static final String NUMBER_OF_CHARTS_PER_SCREEN_ATTRIBUTE_NAME = "Number of Charts per Screen";
    public static final String WORKDAY_BEGINNING_ATTRIBUTE_NAME = "Workday Beginning";
    public static final String WORKDAY_END_ATTRIBUTE_NAME = "Workday End";
    public static final String NUMBER_OF_HORIZONTAL_PIES_ATTRIBUTE_NAME = "Number of Horizontal Pies";
    public static final String NUMBER_OF_HORIZONTAL_TABLES_ATTRIBUTE_NAME = "Number of Horizontal Tables";
    public static final String GRAPH_PLUGIN_CLASS_NAME = "Graph Plugin";
    //    //public class GraphDataModel extends Observable {
//
    private static final Logger logger = LogManager.getLogger(AnalysisDataModel.class);
    private final JEVisObject analysis;
    private final JEVisDataSource ds;
    private final Boolean isGlobalAnalysisTimeFrame = true;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Set<ChartDataRow> selectedData = new HashSet<>();
    private ChartSettings charts = new ChartSettings();
    private JsonChartSettings jsonChartSettings = new JsonChartSettings();
    //
    private JsonChartDataModel listAnalysisModel = new JsonChartDataModel();
    private Long horizontalPies = 2L;
    private Long horizontalTables = 3L;
    //
    private Long chartsPerScreen = 2L;
    private WorkDays wd;
    private Boolean autoResize;
    private boolean customWorkday;
    private AggregationPeriod aggregationPeriod;
    private ManipulationMode manipulationMode;


    public AnalysisDataModel(JEVisDataSource ds, JEVisObject analysis) {
        this.ds = ds;
        this.analysis = analysis;
//
//        this.chartPlugin = chartPlugin;
//        this.globalAnalysisTimeFrame = new AnalysisTimeFrame(TimeFrame.TODAY);
        /**
         * objectMapper configuration for backwards compatibility. Can be removed in the future.
         */
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
//        DateHelper dateHelper = new DateHelper(DateHelper.TransformType.TODAY);
//        updateListAnalyses();
//        dateHelper.setWorkDays(wd);
//        this.globalAnalysisTimeFrame.setStart(dateHelper.getStartDate());
//        this.globalAnalysisTimeFrame.setEnd(dateHelper.getEndDate());
//
////        changed.addListener((observable, oldValue, newValue) -> {
////            if (newValue != oldValue && newValue) {
////                changed.set(false);
////
////                selectedData.clear();
////                charts.clear();
////                updateSelectedData();
////
////                update();
////            }
////        });
    }

    //
    public Set<ChartDataRow> getSelectedData() {
        if (selectedData == null || selectedData.isEmpty()) {
            updateSelectedData();
        }

        return this.selectedData;
    }

    //
    public void setSelectedData(Set<ChartDataRow> selectedData) {
        Set<ChartDataRow> data = new HashSet<>();

        selectedData.forEach(chartDataModel -> {
            if (!chartDataModel.getSelectedcharts().isEmpty())
                data.add(chartDataModel);
        });

        this.selectedData = data;

//        chartPlugin.update();
    }

    public void setData(Set<ChartDataRow> data) {
        this.selectedData = data;
    }


    public ChartSettings getCharts() {
        if (charts == null || charts.getListSettings().isEmpty()) updateCharts();

//        if (selectedData != null && !selectedData.isEmpty() && listAnalysisModel != null) {
//
//            for (ChartSettings chartSettings : charts) {
//                AnalysisTimeFrame newATF = new AnalysisTimeFrame();
//                try {
//                    newATF.setTimeFrame(newATF.parseTimeFrameFromString(chartSettings.getAnalysisTimeFrame().getTimeframe()));
//                    newATF.setId(Long.parseLong(chartSettings.getAnalysisTimeFrame().getId()));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }

        return charts;
    }

//    public void setCharts(ChartSettings charts) {
//        this.charts = charts;
//    }

    private void updateCharts() {
        if (charts == null || charts.getListSettings().isEmpty()) {
            try {
                if (analysis != null) {
                    if (Objects.nonNull(analysis.getAttribute(CHARTS_ATTRIBUTE_NAME))) {
                        if (analysis.getAttribute(CHARTS_ATTRIBUTE_NAME).hasSample()) {
                            String str = analysis.getAttribute(CHARTS_ATTRIBUTE_NAME).getLatestSample().getValueAsString();
                            try {
                                if (str.startsWith("[")) {
                                    jsonChartSettings = new JsonChartSettings();
                                    jsonChartSettings.setListSettings(Arrays.asList(objectMapper.readValue(str, JsonChartSetting[].class)));

                                } else {
                                    jsonChartSettings = objectMapper.readValue(str, JsonChartSettings.class);
                                }
                            } catch (Exception e) {
                                logger.error("Error: could not read chart settings", e);
                            }
                        }
                    }
                    if (Objects.nonNull(analysis.getAttribute(NUMBER_OF_CHARTS_PER_SCREEN_ATTRIBUTE_NAME))) {
                        JEVisSample chartPerScreenSample = analysis.getAttribute(NUMBER_OF_CHARTS_PER_SCREEN_ATTRIBUTE_NAME).getLatestSample();
                        if (chartPerScreenSample != null) {
                            setChartsPerScreen(chartPerScreenSample.getValueAsLong());
                        } else {
                            JEVisClass graphServiceClass = ds.getJEVisClass(GRAPH_PLUGIN_CLASS_NAME);
                            List<JEVisObject> graphServiceList = ds.getObjects(graphServiceClass, true);
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
                    if (Objects.nonNull(analysis.getAttribute(NUMBER_OF_HORIZONTAL_PIES_ATTRIBUTE_NAME))) {
                        JEVisSample noOfHorizontalPies = analysis.getAttribute(NUMBER_OF_HORIZONTAL_PIES_ATTRIBUTE_NAME).getLatestSample();
                        if (noOfHorizontalPies != null) {
                            setHorizontalPies(noOfHorizontalPies.getValueAsLong());
                        } else {
                            JEVisClass graphServiceClass = ds.getJEVisClass(GRAPH_PLUGIN_CLASS_NAME);
                            List<JEVisObject> graphServiceList = ds.getObjects(graphServiceClass, true);
                            for (JEVisObject graphPlugin : graphServiceList) {
                                JEVisAttribute noOfHorizontalPiesAttribute = graphPlugin.getAttribute(NUMBER_OF_HORIZONTAL_PIES_ATTRIBUTE_NAME);
                                if (noOfHorizontalPiesAttribute != null) {
                                    JEVisSample lastSample = noOfHorizontalPiesAttribute.getLatestSample();
                                    if (lastSample != null) {
                                        setHorizontalPies(lastSample.getValueAsLong());
                                    }
                                }
                            }
                        }
                    }
                    if (Objects.nonNull(analysis.getAttribute(NUMBER_OF_HORIZONTAL_TABLES_ATTRIBUTE_NAME))) {
                        JEVisSample noOfHorizontalTables = analysis.getAttribute(NUMBER_OF_HORIZONTAL_TABLES_ATTRIBUTE_NAME).getLatestSample();
                        if (noOfHorizontalTables != null) {
                            setHorizontalTables(noOfHorizontalTables.getValueAsLong());
                        } else {
                            JEVisClass graphServiceClass = ds.getJEVisClass(GRAPH_PLUGIN_CLASS_NAME);
                            List<JEVisObject> graphServiceList = ds.getObjects(graphServiceClass, true);
                            for (JEVisObject graphPlugin : graphServiceList) {
                                JEVisAttribute noOfHorizontalTablesAttribute = graphPlugin.getAttribute(NUMBER_OF_HORIZONTAL_TABLES_ATTRIBUTE_NAME);
                                if (noOfHorizontalTablesAttribute != null) {
                                    JEVisSample lastSample = noOfHorizontalTablesAttribute.getLatestSample();
                                    if (lastSample != null) {
                                        setHorizontalTables(lastSample.getValueAsLong());
                                    }
                                }
                            }
                        }
                    }

                    wd = new WorkDays(analysis);
                    wd.setEnabled(isCustomWorkday());
                }
            } catch (JEVisException e) {
                logger.error("Error: could not get analysis model", e);
            }

            ChartSettings chartSettings = new ChartSettings();

            if (this.jsonChartSettings != null && !this.jsonChartSettings.getListSettings().isEmpty()) {
                boolean needsIds = false;

                if (jsonChartSettings.getAutoSize() != null) {
                    chartSettings.setAutoSize(Boolean.parseBoolean(jsonChartSettings.getAutoSize()));
                    setAutoResize(chartSettings.getAutoSize());
                }

                for (JsonChartSetting settings : this.jsonChartSettings.getListSettings()) {
                    ChartSetting newSettings = new ChartSetting("");
                    if (settings.getId() != null) {
                        newSettings.setId(Integer.parseInt(settings.getId()));
                    } else {
                        needsIds = true;
                    }

                    newSettings.setName(settings.getName());

                    if (settings.getChartType() != null) {
                        newSettings.setChartType(ChartType.parseChartType(settings.getChartType()));
                    } else newSettings.setChartType(ChartType.LINE);

                    if (settings.getColorMapping() != null) {
                        newSettings.setColorMapping(parseColorMapping(settings.getColorMapping()));
                    } else newSettings.setColorMapping(GREEN_YELLOW_RED);

                    if (settings.getOrientation() != null) {
                        newSettings.setOrientation(parseOrientation(settings.getOrientation()));
                    } else newSettings.setOrientation(Orientation.HORIZONTAL);

                    if (settings.getGroupingInterval() != null) {
                        newSettings.setGroupingInterval(Double.parseDouble(settings.getGroupingInterval()));
                    } else newSettings.setGroupingInterval(30d);

                    if (settings.getMinFractionDigits() != null) {
                        newSettings.setMinFractionDigits(Integer.parseInt(settings.getMinFractionDigits()));
                    } else newSettings.setMinFractionDigits(2);

                    if (settings.getMaxFractionDigits() != null) {
                        newSettings.setMaxFractionDigits(Integer.parseInt(settings.getMaxFractionDigits()));
                    } else newSettings.setMaxFractionDigits(2);

                    if (settings.getHeight() != null) {
                        newSettings.setHeight(Double.parseDouble(settings.getHeight()));
                    }

                    if (settings.getFilterEnabled() != null) {
                        newSettings.setFilterEnabled(Boolean.parseBoolean(settings.getFilterEnabled()));
                    } else newSettings.setFilterEnabled(Boolean.FALSE);

                    chartSettings.getListSettings().add(newSettings);
                }

                if (needsIds) {
                    AlphanumComparator ac = new AlphanumComparator();
                    chartSettings.getListSettings().sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));
                    for (ChartSetting set : chartSettings.getListSettings())
                        set.setId(chartSettings.getListSettings().indexOf(set));
                }

                charts = chartSettings;
            }
        }
    }

    private ColorMapping parseColorMapping(String colorMappingString) {
        ColorMapping colorMapping = GREEN_YELLOW_RED;
        switch (colorMappingString) {
            case "LIME_YELLOW_RED":
                colorMapping = LIME_YELLOW_RED;
                break;
            case "BLUE_CYAN_GREEN_YELLOW_RED":
                colorMapping = BLUE_CYAN_GREEN_YELLOW_RED;
                break;
            case "INFRARED_1":
                colorMapping = INFRARED_1;
                break;
            case "INFRARED_2":
                colorMapping = INFRARED_2;
                break;
            case "INFRARED_3":
                colorMapping = INFRARED_3;
                break;
            case "INFRARED_4":
                colorMapping = INFRARED_4;
                break;
            case "BLUE_GREEN_RED":
                colorMapping = BLUE_GREEN_RED;
                break;
            case "BLUE_BLACK_RED":
                colorMapping = BLUE_BLACK_RED;
                break;
            case "BLUE_YELLOW_RED":
                colorMapping = BLUE_YELLOW_RED;
                break;
            case "BLUE_TRANSPARENT_RED":
                colorMapping = BLUE_TRANSPARENT_RED;
                break;
            case "GREEN_BLACK_RED":
                colorMapping = GREEN_BLACK_RED;
                break;
            case "GREEN_YELLOW_RED":
                colorMapping = GREEN_YELLOW_RED;
                break;
            case "RAINBOW":
                colorMapping = RAINBOW;
                break;
            case "BLACK_WHITE":
                colorMapping = BLACK_WHITE;
                break;
            case "WHITE_BLACK":
                colorMapping = WHITE_BLACK;
                break;
        }

        return colorMapping;
    }

    private Orientation parseOrientation(String orientationString) {
        Orientation orientation = Orientation.HORIZONTAL;
        switch (orientationString) {
            case "HORIZONTAL":
                orientation = Orientation.HORIZONTAL;
                break;
            case "VERTICAL":
                orientation = Orientation.VERTICAL;
                break;
        }

        return orientation;
    }

//    public void update() {
////        final String loading = I18n.getInstance().getString("graph.progress.message");
////
////        Service<Void> service = new Service<Void>() {
////            @Override
////            protected Task<Void> createTask() {
////                return new Task<Void>() {
////                    @Override
////                    protected Void call() {
////                        updateMessage(loading);
////
////                        return null;
////                    }
////                };
////            }
////        };
////        ProgressDialog pd = new ProgressDialog(service);
////        pd.setHeaderText(I18n.getInstance().getString("graph.progress.header"));
////        pd.setTitle(I18n.getInstance().getString("graph.progress.title"));
////        Button cancelButton = new Button(I18n.getInstance().getString("attribute.editor.cancel"));
////        cancelButton.setOnAction(event -> service.cancel());
////        pd.getDialogPane().setContent(cancelButton);
////
////        service.start();
//        selectedData.clear();
//        charts.clear();
//        updateSelectedData();
//
//        chartPlugin.update();
//    }


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

    public ChartDataRow get(Long id) {
        ChartDataRow out = null;
        for (ChartDataRow chartDataRow : getSelectedData()) {
            if (chartDataRow.getObject().getID().equals(id)) {
                return chartDataRow;
            }
        }
        return out;
    }


    public JsonChartDataModel getAnalysisModel() {

        JsonChartDataModel tempModel = null;
        try {
//            ds.reloadAttributes();

            if (charts == null || charts.getListSettings().isEmpty()) {
                updateCharts();
            }
            if (analysis != null) {
                if (Objects.nonNull(analysis.getAttribute(DATA_MODEL_ATTRIBUTE_NAME))) {
//                    ds.reloadAttribute(getCurrentAnalysis().getAttribute("Data Model"));
                    if (analysis.getAttribute(DATA_MODEL_ATTRIBUTE_NAME).hasSample()) {
                        String str = analysis.getAttribute(DATA_MODEL_ATTRIBUTE_NAME).getLatestSample().getValueAsString();
                        try {
                            if (str.startsWith("[")) {
                                tempModel = new JsonChartDataModel();
                                List<JsonAnalysisDataRow> listOld = Arrays.asList(objectMapper.readValue(str, JsonAnalysisDataRow[].class));
                                tempModel.setListDataRows(listOld);
                            } else {
                                try {
                                    tempModel = objectMapper.readValue(str, JsonChartDataModel.class);
                                } catch (Exception e) {
                                    logger.error(e);
                                    tempModel = new JsonChartDataModel();
                                    tempModel.getListDataRows().add(objectMapper.readValue(str, JsonAnalysisDataRow.class));
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Error: could not read data model", e);
                        }
                    }
                }

                wd = new WorkDays(analysis);
                wd.setEnabled(isCustomWorkday());
            }
        } catch (JEVisException e) {
            logger.error("Error: could not get analysis model", e);
        }

        listAnalysisModel = tempModel;

        return listAnalysisModel;
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
                    for (ChartSetting set : charts.getListSettings()) {
                        if (set.getName().equals(str))
                            idList.add(set.getId());
                    }
                }
            }

            return idList;
        } else return new ArrayList<>();
    }

//    public void selectNone() {
//        getSelectedData().forEach(mdl -> {
//            mdl.setSelectedCharts(new ArrayList<>());
//            mdl.setColor(ColorTable.STANDARD_COLOR);
//        });
//    }
//
//    private ManipulationMode globalManipulationMode = ManipulationMode.NONE;

    public void updateSelectedData() {

        selectedData.clear();

        JsonChartDataModel jsonChartDataModel = getAnalysisModel();

        if (jsonChartDataModel != null) {
            Map<String, ChartDataRow> data = new HashMap<>();

            for (JsonAnalysisDataRow mdl : jsonChartDataModel.getListDataRows()) {
                ChartDataRow newData = new ChartDataRow(ds);

                try {
//                    Long id = Long.parseLong(mdl.getObject());
//                    Long id_dp = null;
//                    if (mdl.getDataProcessorObject() != null) id_dp = Long.parseLong(mdl.getDataProcessorObject());
//                    JEVisObject obj = ds.getObject(id);
//                    JEVisObject obj_dp = null;
//                    if (mdl.getDataProcessorObject() != null) {
//                        obj_dp = ds.getObject(id_dp);
//                    }
//
//                    newData.setObject(obj);

                    newData.setId(Long.parseLong(mdl.getObject()));
                    if (mdl.getDataProcessorObject() != null)
                        newData.setId(Long.parseLong(mdl.getDataProcessorObject()));

                    JEVisUnit unit = new JEVisUnitImp(objectMapper.readValue(mdl.getUnit(), JsonUnit.class));

                    if (mdl.getColor() != null) {
                        newData.setColor(ColorHelper.toColor(mdl.getColor()));
                    }
                    newData.setName(mdl.getName());

                    newData.getAttribute();
                    if (getAggregationPeriod() != null) {
                        newData.setAggregationPeriod(getAggregationPeriod());
                    } else {
                        newData.setAggregationPeriod(AggregationPeriod.parseAggregation(mdl.getAggregation()));
                    }
                    if (getManipulationMode() != null) {
                        newData.setManipulationMode(getManipulationMode());
                    }

                    newData.setSomethingChanged(true);
                    newData.setSelectedCharts(stringToList(mdl.getSelectedCharts()));
                    newData.setUnit(unit);
                    if (mdl.getAxis() != null) newData.setAxis(Integer.parseInt(mdl.getAxis()));

                    if (mdl.getIsEnPI() != null) newData.setCalculation(Boolean.parseBoolean(mdl.getIsEnPI()));

                    if (mdl.getBubbleType() != null) {
                        newData.setBubbleType(BubbleType.parseBubbleType(mdl.getBubbleType()));
                    }

                    if (mdl.getChartType() != null) {
                        ChartType chartType = ChartType.parseChartType(mdl.getChartType());
                        newData.setChartType(chartType);
                    } else {
                        newData.setChartType(ChartType.DEFAULT);
                    }

                    newData.setCustomWorkDay(isCustomWorkday());

                    data.put(String.valueOf(newData.getId()), newData);
                } catch (IOException e) {
                    logger.error("Error: could not parse unit", e);
                }
            }

            for (Map.Entry<String, ChartDataRow> entrySet : data.entrySet()) {
                ChartDataRow value = entrySet.getValue();
                if (!value.getSelectedcharts().isEmpty()) {
                    selectedData.add(value);
                }
            }
        }

        if (isGlobalAnalysisTimeFrame()) {
//            setGlobalAnalysisTimeFrame(selectedData);
        }
    }


//    public Long getChartsPerScreen() {
//        if (chartsPerScreen == null) {
//            try {
//                JEVisClass graphPluginClass = ds.getJEVisClass(GRAPH_PLUGIN_CLASS_NAME);
//                List<JEVisObject> graphPlugins = ds.getObjects(graphPluginClass, true);
//                if (!graphPlugins.isEmpty()) {
//                    JEVisAttribute chartsPerScreenAttribute = graphPlugins.get(0).getAttribute(NUMBER_OF_CHARTS_PER_SCREEN_ATTRIBUTE_NAME);
//                    if (chartsPerScreenAttribute != null) {
//                        JEVisSample latestSample = chartsPerScreenAttribute.getLatestSample();
//                        if (latestSample != null) {
//                            chartsPerScreen = Long.parseLong(latestSample.getValueAsString());
//                        }
//                    }
//                }
//            } catch (JEVisException e) {
//                logger.error("Could not get JEVisClass for Graph Plugin");
//            }
//        }
//        if (chartsPerScreen == null || chartsPerScreen.equals(0L)) chartsPerScreen = 2L;
//        return chartsPerScreen;
//    }
//
//    public void setChartsPerScreen(Long chartsPerScreen) {
//        this.chartsPerScreen = chartsPerScreen;
//    }
//
//    public Long getHorizontalPies() {
//        if (horizontalPies == null) {
//            try {
//                JEVisClass graphPluginClass = ds.getJEVisClass(GRAPH_PLUGIN_CLASS_NAME);
//                List<JEVisObject> graphPlugins = ds.getObjects(graphPluginClass, true);
//                if (!graphPlugins.isEmpty()) {
//                    JEVisAttribute horizontalPiesAttribute = graphPlugins.get(0).getAttribute(NUMBER_OF_HORIZONTAL_PIES_ATTRIBUTE_NAME);
//                    if (horizontalPiesAttribute != null) {
//                        JEVisSample latestSample = horizontalPiesAttribute.getLatestSample();
//                        if (latestSample != null) {
//                            horizontalPies = Long.parseLong(latestSample.getValueAsString());
//                        }
//                    }
//                }
//            } catch (JEVisException e) {
//                logger.error("Could not get JEVisClass for Graph Plugin");
//            }
//        }
//        if (horizontalPies == null || horizontalPies.equals(0L)) horizontalPies = 2L;
//        return horizontalPies;
//    }
//
//    public Long getHorizontalTables() {
//        if (horizontalTables == null) {
//            try {
//                JEVisClass graphPluginClass = ds.getJEVisClass(GRAPH_PLUGIN_CLASS_NAME);
//                List<JEVisObject> graphPlugins = ds.getObjects(graphPluginClass, true);
//                if (!graphPlugins.isEmpty()) {
//                    JEVisAttribute horizontalTablesAttribute = graphPlugins.get(0).getAttribute(NUMBER_OF_HORIZONTAL_TABLES_ATTRIBUTE_NAME);
//                    if (horizontalTablesAttribute != null) {
//                        JEVisSample latestSample = horizontalTablesAttribute.getLatestSample();
//                        if (latestSample != null) {
//                            horizontalTables = Long.parseLong(latestSample.getValueAsString());
//                        }
//                    }
//                }
//            } catch (JEVisException e) {
//                logger.error("Could not get JEVisClass for Graph Plugin");
//            }
//        }
//        if (horizontalTables == null || horizontalTables.equals(0L)) horizontalTables = 3L;
//        return horizontalTables;
//    }
//
//    public void setHorizontalTables(Long horizontalTables) {
//        this.horizontalTables = horizontalTables;
//    }
//
//    public void setHorizontalPies(Long horizontalPies) {
//        this.horizontalPies = horizontalPies;
//    }


    public Boolean isGlobalAnalysisTimeFrame() {
        return isGlobalAnalysisTimeFrame;
    }

    public Long getChartsPerScreen() {
        return chartsPerScreen;
    }

    public void setChartsPerScreen(Long chartsPerScreen) {
        this.chartsPerScreen = chartsPerScreen;
    }

    public Long getHorizontalPies() {
        return horizontalPies;
    }

    public void setHorizontalPies(Long horizontalPies) {
        this.horizontalPies = horizontalPies;
    }

    public Long getHorizontalTables() {
        return horizontalTables;
    }

    public void setHorizontalTables(Long horizontalTables) {
        this.horizontalTables = horizontalTables;
    }

    public Boolean getAutoResize() {
        return autoResize;
    }

    public void setAutoResize(Boolean autoResize) {
        this.autoResize = autoResize;
    }

    public boolean isCustomWorkday() {
        return customWorkday;
    }

    public void setCustomWorkday(boolean customWorkday) {
        this.customWorkday = customWorkday;
    }

    public AggregationPeriod getAggregationPeriod() {
        return aggregationPeriod;
    }

    public void setAggregationPeriod(AggregationPeriod aggregationPeriod) {
        this.aggregationPeriod = aggregationPeriod;
    }

    public ManipulationMode getManipulationMode() {
        return manipulationMode;
    }

    public void setManipulationMode(ManipulationMode manipulationMode) {
        this.manipulationMode = manipulationMode;
    }
}

