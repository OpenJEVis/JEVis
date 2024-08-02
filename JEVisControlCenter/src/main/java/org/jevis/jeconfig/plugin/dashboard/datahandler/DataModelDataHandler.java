package org.jevis.jeconfig.plugin.dashboard.datahandler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.datetime.PeriodComparator;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.Chart.ChartTools;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.data.*;
import org.jevis.jeconfig.application.jevistree.plugin.SimpleTargetPlugin;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config.DataModelNode;
import org.jevis.jeconfig.plugin.dashboard.config.DataPointNode;
import org.jevis.jeconfig.plugin.dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.plugin.dashboard.timeframe.LastPeriod;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrame;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrameFactory;
import org.jevis.jeconfig.plugin.dashboard.widget.ChartWidget;
import org.jevis.jeconfig.sample.tableview.SampleTable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;

import javax.swing.event.EventListenerList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataModelDataHandler {

    public final static String TYPE = "SimpleDataHandler";
    private static final Logger logger = LogManager.getLogger(DataModelDataHandler.class);
    private final JEVisDataSource jeVisDataSource;
    private final DashboardControl dashboardControl;
    private final String widgetType;
    private final Map<String, JEVisAttribute> attributeMap = new HashMap<>();
    private final BooleanProperty enableMultiSelect = new SimpleBooleanProperty(false);
    private final StringProperty unitProperty = new SimpleStringProperty("");
    private final SimpleTargetPlugin simpleTargetPlugin = new SimpleTargetPlugin();
    private final List<ChartDataRow> chartDataRows = new ArrayList<>();
    private final ObjectProperty<Interval> durationProperty = new SimpleObjectProperty<>();
    private final TimeFrameFactory timeFrameFactory;
    private final List<TimeFrame> timeFrameFactories = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final PeriodComparator periodComparator = new PeriodComparator();
    private final EventListenerList listeners = new EventListenerList();
    private final List<AggregationPeriod> initialAggregation = new ArrayList<>();
    private final DataModel dataModel = new DataModel();
    private final AnalysisHandler analysisHandler = new AnalysisHandler();
    public ObjectProperty<DateTime> lastUpdate = new SimpleObjectProperty<>();
    private boolean fixedTimeFrame = false;
    //    private DataModelNode dataModelNode = new DataModelNode();
    private boolean autoAggregation = false;
    private boolean forcedInterval = false;
    private TimeFrame timeFrame;
    private WorkDays wd;
    private Interval forcedZeroInterval;

    public DataModelDataHandler(JEVisDataSource jeVisDataSource, DashboardControl dashboardControl, WidgetPojo config, String id) {
        this.jeVisDataSource = jeVisDataSource;
        this.dashboardControl = dashboardControl;
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        this.widgetType = id;

        try {
            if (config != null) {
                JsonNode configNode = config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE);
                if (configNode != null && configNode.get("type").asText().equals(TYPE)) {
                    DataModelNode dataModelNode = this.mapper.treeToValue(configNode, DataModelNode.class);
                    this.dataModel.getChartModels().add(createChartModelFromOldDataModel(dataModelNode));
                    if (!dataModelNode.getForcedInterval().isEmpty()) {
                        forcedInterval = true;
                        setForcedPeriod(dataModelNode.getForcedInterval());
                    }
                } else if (configNode != null && configNode.get("type").asText().equals(AnalysisHandler.TYPE)) {
                    analysisHandler.jsonToModel(configNode, dataModel);

                    for (ChartModel chartModel : dataModel.getChartModels()) {
                        for (ChartData chartData : chartModel.getChartData()) {
                            try {
                                JEVisObject object = jeVisDataSource.getObject(chartData.getId());
                                chartData.setObjectName(object);
                            } catch (Exception ignored) {
                            }
                        }
                    }

                    forcedInterval = true;
                    setForcedPeriod(dataModel.getForcedInterval());
                }

                setFixedTimeframe(config.getFixedTimeframe());
            }
        } catch (Exception ex) {
            logger.error(ex);
        }

        if (this.dataModel.getChartModels().isEmpty()) {
            this.dataModel.getChartModels().add(new ChartModel());
        }

        setData(this.dataModel);

        this.timeFrameFactory = new TimeFrameFactory(jeVisDataSource);
        this.timeFrameFactories.addAll(this.timeFrameFactory.getAll(dashboardControl.getActiveDashboard().getDashboardObject()));
    }

    public static String generateValueKey(JEVisAttribute attribute) {
        return attribute.getObjectID() + ":" + attribute.getName();
    }

    private void setFixedTimeframe(boolean fixedTimeframe) {
        this.fixedTimeFrame = fixedTimeframe;
    }

    /**
     * Set if the date in the interval will use the auto aggregation
     * [if -> then]
     * Day -> Display Interval
     * Week -> Hourly
     * Month -> Daily
     * Year -> Weekly
     *
     * @param interval
     */
    private AggregationPeriod getAggregationPeriod(Interval interval) {
        AggregationPeriod aggregationPeriod = AggregationPeriod.NONE;

        /** less then an week take original **/
        if (interval.toDuration().getStandardDays() < 6) {
            aggregationPeriod = AggregationPeriod.NONE;
        }
        /** less then an month take hour **/
        else if (interval.toDuration().getStandardDays() < 27) {
            aggregationPeriod = AggregationPeriod.HOURLY;
        }
        /** less than year take day **/
        else if (interval.toDuration().getStandardDays() < 364) {
            aggregationPeriod = AggregationPeriod.DAILY;
        }
        /** more than an year take week **/
        else {
            aggregationPeriod = AggregationPeriod.WEEKLY;
        }
        return aggregationPeriod;
    }

    public void debug() {
        System.out.println("----------------------------------------");
        try {
            System.out.println("Json: " + mapper.writeValueAsString(toJsonNode()));
        } catch (Exception ex) {

        }
        System.out.println("----");
        System.out.println("Interval start: " + this.durationProperty.getValue().getStart());
        System.out.println("Interval end  : " + this.durationProperty.getValue().getEnd());
        System.out.println("Models: " + dataModel.getChartModels().size());
        System.out.println("forcedInterval: " + forcedInterval);
        System.out.println("Interval Factory: " + timeFrame);
        System.out.println("isAutoAggregation: " + autoAggregation);
//        getDataModel().forEach(chartDataModel -> {
//            System.out.println("model: " + chartDataModel.getObject().getID() + " " + chartDataModel.getObject().getName());
//            System.out.println("EnPI: " + chartDataModel.getEnPI());
//            System.out.println("model.datasize: " + chartDataModel.getSamples().size());
//            chartDataModel.getSamples().forEach(jeVisSample -> {
//                System.out.println("S: " + jeVisSample);
//            });
//        });
        System.out.println("----");


        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        TabPane tabPane = new TabPane();
        chartDataRows.forEach(chartDataRow -> {
            SampleTable sampleTable = new SampleTable(chartDataRow.getAttribute(), DateTimeZone.getDefault(), chartDataRow.getSamples());
            Tab tab = new Tab(chartDataRow.getObject().getName() + ":" + chartDataRow.getAttribute().getName(), sampleTable);
            tabPane.getTabs().add(tab);
        });
        alert.setGraphic(null);
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(tabPane);
        TopMenu.applyActiveTheme(alert.getDialogPane().getScene());
        alert.setResizable(true);
        alert.show();

    }

    public List<DateTime> getMaxTimeStamps() {
        List<DateTime> dateTimes = new ArrayList<>();
        for (ChartDataRow chartDataRow : this.chartDataRows) {
            List<JEVisSample> samples = chartDataRow.getSamples();
            try {
                dateTimes.add(samples.get(samples.size() - 1).getTimestamp());
            } catch (Exception ex) {

            }
        }
        return dateTimes;
    }

    public JEVisDataSource getJeVisDataSource() {
        return this.jeVisDataSource;
    }

    public void setAutoAggregation(boolean enable) {

        this.autoAggregation = enable;

        this.chartDataRows.forEach(chartDataRow -> {
            chartDataRow.setAbsolute(enable);
            chartDataRow.setSomethingChanged(true);
        });

    }

    public TimeFrame getTimeFrameFactory() {
        if (this.dataModel.getForcedInterval() == null || this.dataModel.getForcedInterval().isEmpty()) {
            return null;
        }

        for (TimeFrame timeFrame : this.timeFrameFactories) {
            if (timeFrame.getID().equals(this.dataModel.getForcedInterval())) {
                return timeFrame;
            }
        }

        try {
            LastPeriod lastPeriod = new LastPeriod(Period.parse(this.dataModel.getForcedInterval()));
            return lastPeriod;
        } catch (Exception ex) {
            logger.error(ex);
        }

        return null;
    }

    private void setInterval(Interval interval) {
        if (!this.dataModel.getForcedInterval().isEmpty()) {

            TimeFrame timeFrame = getTimeFrameFactory();
            if (timeFrame != null) {
                interval = timeFrame.getInterval(interval.getEnd(), isFixedTimeFrame());
                if (interval.getEndMillis() - interval.getStartMillis() == 0) {
                    this.setForcedZeroInterval(interval);
                    interval = dashboardControl.getActiveTimeFrame().getInterval(interval.getEnd(), isFixedTimeFrame());
                }
            } else {
                logger.error("Widget DataModel is not configured, using selected.");
            }
        }

        this.setDuration(interval);

        for (ChartDataRow chartDataRow : getChartDataRows()) {
            try {
                int i = getChartDataRows().indexOf(chartDataRow);
                AggregationPeriod initialAggregation = this.initialAggregation.get(i);

                /**
                 * we may need this for meter changes usecase
                 * **/

                //CleanDataObject.getPeriodForDate(, )

                Period objectPeriod = new Period(chartDataRow.getAttribute().getObject().getAttribute("Period").getLatestSample().getValueAsString());
                Period userPeriod = new Period();
                switch (getAggregationPeriod(interval)) {
                    case NONE:
                        userPeriod = Period.ZERO;
                        break;
                    case MINUTELY:
                        userPeriod = Period.minutes(1);
                        break;
                    case QUARTER_HOURLY:
                        userPeriod = Period.minutes(15);
                        break;
                    case HOURLY:
                        userPeriod = Period.hours(1);
                        break;
                    case DAILY:
                        userPeriod = Period.days(1);
                        break;
                    case WEEKLY:
                        userPeriod = Period.weeks(1);
                        break;
                    case MONTHLY:
                        userPeriod = Period.months(1);
                        break;
                    case QUARTERLY:
                        userPeriod = Period.months(3);
                        break;
                    case YEARLY:
                        userPeriod = Period.years(1);
                        break;
                    case THREEYEARS:
                        userPeriod = Period.years(3);
                        break;
                    case FIVEYEARS:
                        userPeriod = Period.years(5);
                        break;
                    case TENYEARS:
                        userPeriod = Period.years(10);
                        break;
                }


                if (periodComparator.compare(userPeriod, objectPeriod) < 0) {
                    // check if data row period is bigger than requested period
                    chartDataRow.setAggregationPeriod(AggregationPeriod.NONE);
                } else if (initialAggregation == AggregationPeriod.NONE && widgetType.equals(ChartWidget.WIDGET_ID)
                        && chartDataRow.getChartType() != ChartType.HEAT_MAP && dataModel.getChartModels().get(0).getChartType() != ChartType.HEAT_MAP) {
                    // exception for chart widgets for better visualisation and performance
                    AggregationPeriod aggregationPeriod = getAggregationPeriod(interval);
                    chartDataRow.setAggregationPeriod(aggregationPeriod);
                } else {
                    // use user specified aggregation if above isn't valid
                    chartDataRow.setAggregationPeriod(initialAggregation);
                }
            } catch (NullPointerException ex) {
                logger.error("Null pointer in {}", chartDataRow);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }

    private Boolean isFixedTimeFrame() {
        return this.fixedTimeFrame;
    }

    private void setForcedZeroInterval(Interval interval) {
        this.forcedZeroInterval = interval;
    }

    public Interval getDuration() {
        return durationProperty.get();
    }

    public void setDuration(Interval durationProperty) {
        this.durationProperty.set(durationProperty);
    }

    public ObjectProperty<Interval> durationProperty() {
        return durationProperty;
    }

    public JsonNode toJsonNode() {
        return analysisHandler.toJsonNode(dataModel);
    }

    public Map<String, JEVisAttribute> getAttributeMap() {
        return this.attributeMap;
    }

    public List<ChartDataRow> getChartDataRows() {
        return this.chartDataRows;
    }

    public void addEventListener(SampleHandlerEventListener listener) {

        if (this.listeners.getListeners(JEVisEventListener.class).length > 0) {
        }

        this.listeners.add(SampleHandlerEventListener.class, listener);
    }

    public void removeEventListener(SampleHandlerEventListener listener) {
        this.listeners.remove(SampleHandlerEventListener.class, listener);
    }

    public SampleHandlerEventListener[] getEventListener() {
        return this.listeners.getListeners(SampleHandlerEventListener.class);
    }

    private synchronized void notifyListeners(SampleHandlerEvent event) {
        logger.debug("SampleHandlerEvent: {}", event);
        for (SampleHandlerEventListener l : this.listeners.getListeners(SampleHandlerEventListener.class)) {
            l.fireEvent(event);
        }
    }

    public void update(Interval interval) {
        logger.debug("Update Samples: {}", this.durationProperty.getValue());

        setData(dataModel);

        setInterval(interval);

        this.chartDataRows.forEach(chartDataModel -> {

            DateTime start = getDuration().getStart();
            DateTime end = getDuration().getEnd();

            if (chartDataModel.getAggregationPeriod() != AggregationPeriod.NONE
                    && chartDataModel.getAggregationPeriod() != AggregationPeriod.MINUTELY
                    && chartDataModel.getAggregationPeriod() != AggregationPeriod.QUARTER_HOURLY
                    && chartDataModel.getAggregationPeriod() != AggregationPeriod.HOURLY) {
                if (wd == null) {
                    wd = new WorkDays(chartDataModel.getObject());
                }

                start = start.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
                end = end.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);

                if (wd.getWorkdayEnd().isBefore(wd.getWorkdayStart())) {
                    start = start.plusDays(1);
                }
            }

            chartDataModel.setSelectedStart(start);
            chartDataModel.setSelectedEnd(end);
            List<JEVisSample> samples = chartDataModel.getSamples();

            if (forcedZeroInterval != null) {
                if (!samples.isEmpty()) {
                    chartDataModel.setSamples(samples.subList(samples.size() - 1, samples.size()));
                }
            }
            logger.debug("New samples for: {} = {}", chartDataModel.getObject().getID(), samples.size());
        });

        this.lastUpdate.setValue(new DateTime());
        notifyListeners(new SampleHandlerEvent(this, SampleHandlerEvent.TYPE.UPDATE));
    }

    public void setMultiSelect(boolean enable) {
        this.enableMultiSelect.set(enable);
    }

    public StringProperty getUnitProperty() {
        return this.unitProperty;
    }

    public boolean isForcedInterval() {
        return forcedInterval;
    }

    public void setForcedInterval(boolean forcedInterval) {
        this.forcedInterval = forcedInterval;
    }

    public String getForcedPeriod() {
        return dataModel.getForcedInterval();
    }

    public void setForcedPeriod(String forcedPeriod) {
        this.dataModel.setForcedInterval(forcedPeriod);
    }

    public void setForcedPeriod(TimeFrame forcedPeriod) {
        this.dataModel.setForcedInterval(forcedPeriod.getID());
        setForcedInterval(true);
    }

    public void setData(DataModel dataModel) {
        this.chartDataRows.clear();
        this.attributeMap.clear();
        this.initialAggregation.clear();

        for (ChartModel chartModel : dataModel.getChartModels()) {
            for (ChartData chartData : chartModel.getChartData()) {
                try {
                    ChartDataRow chartDataRow = new ChartDataRow(jeVisDataSource, chartData);
                    initialAggregation.add(chartData.getAggregationPeriod());

                    this.chartDataRows.add(chartDataRow);
                    this.attributeMap.put(generateValueKey(chartDataRow.getAttribute()), chartDataRow.getAttribute());

                    if (autoAggregation) {
                        chartDataRow.setAbsolute(true);
                    }
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }
    }


    public ChartModel createChartModelFromOldDataModel(DataModelNode dataModelNode) {

        ChartModel chartModel = new ChartModel();

        for (DataPointNode dataPointNode : dataModelNode.getData()) {
            try {
                JEVisObject object = this.jeVisDataSource.getObject(dataPointNode.getObjectID());
                if (dataPointNode.getCleanObjectID() != null && dataPointNode.getCleanObjectID() > 0) {
                    object = this.jeVisDataSource.getObject(dataPointNode.getCleanObjectID());
                }


                if (object != null) {
                    JEVisAttribute jeVisAttribute = object.getAttribute(dataPointNode.getAttribute());
                    if (jeVisAttribute != null) {
                        ChartData chartData = new ChartData();

                        chartData.setId(jeVisAttribute.getObject().getID());
                        chartData.setObjectName(jeVisAttribute.getObject());
                        chartData.setAttributeString(dataPointNode.getAttribute());

                        if (dataPointNode.getManipulationMode() != null) {
                            chartData.setManipulationMode(dataPointNode.getManipulationMode());
                        }
                        if (dataPointNode.getManipulationMode() != null) {
                            chartData.setAggregationPeriod(dataPointNode.getAggregationPeriod());
                        }
                        chartData.setAxis(dataPointNode.getAxis());

                        if (dataPointNode.getColor() != null) {
                            chartData.setColor(dataPointNode.getColor());
                        } else {
                            chartData.setColor(Color.LIGHTBLUE);
                        }

                        if (dataPointNode.getChartType() != null) {
                            chartData.setChartType(dataPointNode.getChartType());
                        } else {
                            chartData.setChartType(ChartType.LINE);
                        }

                        chartData.setName(dataPointNode.getName());

                        if (dataPointNode.getUnit() != null) {
                            chartData.setUnit(dataPointNode.getUnit());
                        } else {
                            chartData.setUnit(jeVisAttribute.getDisplayUnit());
                        }

                        chartData.setCalculation(dataPointNode.isEnpi());

                        if (chartData.isCalculation()) {
                            chartData.setCalculationId(ChartTools.getCalculationId(jeVisDataSource, chartData.getId()));
                        }

                        chartData.setDecimalDigits(dataPointNode.getDecimalDigits());

                        if (dataPointNode.getCustomCSS() != null) {
                            chartData.setCss(dataPointNode.getCustomCSS());
                        }

                        chartData.setBubbleType(dataPointNode.getBubbleType());

                        chartModel.getChartData().add(chartData);
                    } else {
                        logger.error("Attribute does not exist: {}", dataPointNode.getAttribute());
                    }


                } else {
                    logger.error("Object not found: {}", dataPointNode.getObjectID());
                }
            } catch (Exception ex) {
                logger.error("Error '{}' in line {}: {}", ex.getMessage(), ex.getStackTrace()[0].getLineNumber(), ex.getStackTrace()[0], ex);
            }

        }

        return chartModel;
    }

    public DashboardControl getDashboardControl() {
        return dashboardControl;
    }

    public DataModel getDataModel() {
        return dataModel;
    }
}
