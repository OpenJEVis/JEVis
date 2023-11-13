package org.jevis.jeconfig.plugin.dashboard.datahandler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.data.ChartData;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.Chart.data.ChartModel;
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
import java.util.*;

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
    public ObjectProperty<DateTime> lastUpdate = new SimpleObjectProperty<>();
    private boolean fixedTimeFrame = false;
    private DataModelNode dataModelNode = new DataModelNode();
    private boolean autoAggregation = false;
    private boolean forcedInterval = false;
    private String forcedPeriod;
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
                if (configNode != null) {
                    this.dataModelNode = this.mapper.treeToValue(configNode, DataModelNode.class);
                } else {
                    this.dataModelNode = new DataModelNode();
                }

                this.fixedTimeFrame = config.isFixedTimeframe();
            } else {
                this.dataModelNode = new DataModelNode();
            }
        } catch (Exception ex) {
            logger.error(ex);
        }

        if (!this.dataModelNode.getForcedInterval().isEmpty()) {
            this.forcedInterval = true;
            try {
                this.forcedPeriod = this.dataModelNode.getForcedInterval();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        setData(this.dataModelNode.getData());
        this.timeFrameFactory = new TimeFrameFactory(jeVisDataSource);
        this.timeFrameFactories.addAll(this.timeFrameFactory.getAll(dashboardControl.getActiveDashboard().getDashboardObject()));
    }

    public static Double getManipulatedData(DataModelNode dataModelNode, List<JEVisSample> samples, ChartDataRow dataModel) {
        Double value = 0d;
        if (samples.size() == 1) {
            try {
                value = samples.get(0).getValueAsDouble();
            } catch (JEVisException e) {
                logger.error("Could not get value for datarow {}:{}", dataModel.getObject().getName(), dataModel.getObject().getID(), e);
            }
        } else {
            try {
                QuantityUnits qu = new QuantityUnits();
                boolean isQuantity = qu.isQuantityUnit(dataModel.getUnit());
                isQuantity = qu.isQuantityIfCleanData(dataModel.getAttribute(), isQuantity);


                Double min = Double.MAX_VALUE;
                Double max = Double.MIN_VALUE;
                List<Double> listMedian = new ArrayList<>();

                DateTime dateTime = null;

                List<JsonSample> listManipulation = new ArrayList<>();
                for (JEVisSample sample : samples) {
                    Double currentValue = sample.getValueAsDouble();
                    value += currentValue;
                    min = Math.min(min, currentValue);
                    max = Math.max(max, currentValue);
                    listMedian.add(currentValue);

                    if (dateTime == null) dateTime = new DateTime(sample.getTimestamp());
                }
                if (!isQuantity) {
                    value = value / samples.size();
                }

                DataPointNode dataPointNode = getDataPointNodeForChartDataRow(dataModelNode, dataModel);

                if (dataPointNode != null)
                    switch (dataPointNode.getManipulationMode()) {
                        case AVERAGE:
                            value = value / (double) samples.size();
                            break;
                        case MIN:
                            value = min;
                            break;
                        case MAX:
                            value = max;
                            break;
                        case MEDIAN:
                            if (listMedian.size() > 1)
                                listMedian.sort(Comparator.naturalOrder());
                            value = listMedian.get((listMedian.size() - 1) / 2);
                            break;
                    }

            } catch (Exception ex) {
                logger.error("Error in quantity check: {}", ex, ex);
            }
        }

        return value;

    }

    public static String generateValueKey(JEVisAttribute attribute) {
        return attribute.getObjectID() + ":" + attribute.getName();
    }

    private static DataPointNode getDataPointNodeForChartDataRow(DataModelNode dataModelNode, ChartDataRow dataModel) {
        for (DataPointNode dataPointNode : dataModelNode.getData()) {
            long objectId = dataPointNode.getObjectID();
            if (dataPointNode.getCleanObjectID() != null) {
                objectId = dataPointNode.getCleanObjectID();
            }
            if (objectId == dataModel.getId()
                    && dataPointNode.getAttribute().equals(dataModel.getAttributeString())) {
                return dataPointNode;
            }
        }
        return null;
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
        System.out.println("Models: " + getDataModel().size());
        System.out.println("forcedInterval: " + forcedInterval);
        System.out.println("Interval Factoy: " + timeFrame);
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
        getDataModel().forEach(chartDataModel -> {
            SampleTable sampleTable = new SampleTable(chartDataModel.getAttribute(), DateTimeZone.getDefault(), chartDataModel.getSamples());
            Tab tab = new Tab(chartDataModel.getObject().getName() + ":" + chartDataModel.getAttribute().getName(), sampleTable);
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
        this.dataModelNode.getData().forEach(dataPointNode -> dataPointNode.setAbsolute(enable));

        this.chartDataRows.forEach(chartDataRow -> {
            chartDataRow.setAbsolute(enable);
            chartDataRow.setSomethingChanged(true);
        });

    }

    public TimeFrame getTimeFrameFactory() {
        if (this.forcedPeriod == null) {
            return null;
        }

        for (TimeFrame timeFrame : this.timeFrameFactories) {
            if (timeFrame.getID().equals(this.forcedPeriod)) {
                return timeFrame;
            }
        }

        try {
            LastPeriod lastPeriod = new LastPeriod(Period.parse(this.forcedPeriod));
            return lastPeriod;
        } catch (Exception ex) {
            logger.error(ex);
        }

        return null;
    }

    public void setInterval(Interval interval) {
        if (this.forcedInterval) {

            TimeFrame timeFrame = getTimeFrameFactory();
            if (timeFrame != null) {
                interval = timeFrame.getInterval(interval.getEnd(), this.fixedTimeFrame);
                if (interval.getEndMillis() - interval.getStartMillis() == 0) {
                    this.setForcedZeroInterval(interval);
                    interval = dashboardControl.getActiveTimeFrame().getInterval(interval.getEnd(), this.fixedTimeFrame);
                }
            } else {
                logger.error("Widget DataModel is not configured, using selected.");
            }
        }

        this.setDuration(interval);

        for (ChartDataRow chartDataRow : getDataModel()) {
            try {
                int i = getDataModel().indexOf(chartDataRow);
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
                } else if (initialAggregation == AggregationPeriod.NONE && widgetType.equals(ChartWidget.WIDGET_ID)) {
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
        ArrayNode dataArrayNode = JsonNodeFactory.instance.arrayNode();

        this.dataModelNode.getData().forEach(dataPointNode -> {
            try {
                logger.debug("Add attribute: {}:{}", dataPointNode.getObjectID(), dataPointNode.getAttribute());

                ObjectNode dataNode = JsonNodeFactory.instance.objectNode();
                dataNode.put("objectID", dataPointNode.getObjectID());
                dataNode.put("attribute", dataPointNode.getAttribute());

                dataNode.put("enpi", dataPointNode.isEnpi());

                dataNode.put("name", dataPointNode.getName());

                if (dataPointNode.getChartType() != null) {
                    dataNode.put("chartType", dataPointNode.getChartType().toString());
                } else {
                    dataNode.put("chartType", ChartType.LINE.toString());
                }
                dataNode.put("aggregationPeriod", dataPointNode.getAggregationPeriod().toString());
                dataNode.put("manipulationMode", dataPointNode.getManipulationMode().toString());
                dataNode.put("absolute", dataPointNode.isAbsolute());

                if (dataPointNode.getUnit() != null) {
                    dataNode.put("unit", dataPointNode.getUnit());
                }

                if (dataPointNode.getCleanObjectID() != null) {
                    dataNode.put("cleanObjectID", dataPointNode.getCleanObjectID());
                }

                dataNode.put("color", dataPointNode.getColor().toString());
                dataNode.put("axis", dataPointNode.getAxis().toString());
                dataNode.put("decimalDigits", dataPointNode.getDecimalDigits());
                dataNode.put("bubbleType", dataPointNode.getBubbleType().toString());

                if (dataPointNode.getCustomCSS() != null) {
                    dataNode.put("customCSS", dataPointNode.getCustomCSS());
                }

                dataArrayNode.add(dataNode);
            } catch (Exception ex) {
                logger.error(ex);
            }
        });
        ObjectNode dataHandlerNode = JsonNodeFactory.instance.objectNode();
        dataHandlerNode.set("data", dataArrayNode);
        dataHandlerNode.set("type", JsonNodeFactory.instance.textNode(TYPE));
        if (this.forcedInterval) {
            dataHandlerNode.put("forcedInterval", this.forcedPeriod);
        }

        return dataHandlerNode;

    }

    public Map<String, JEVisAttribute> getAttributeMap() {
        return this.attributeMap;
    }

    public List<ChartDataRow> getDataModel() {
        return this.chartDataRows;
    }

    public DataModelNode getDateNode() {
        return this.dataModelNode;
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
        logger.error("SampleHandlerEvent: {}", event);
        for (SampleHandlerEventListener l : this.listeners.getListeners(SampleHandlerEventListener.class)) {
            l.fireEvent(event);
        }
    }

    public void update() {
        logger.debug("Update Samples: {}", this.durationProperty.getValue());
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
        return forcedPeriod;
    }

    public void setForcedPeriod(String forcedPeriod) {
        this.forcedPeriod = forcedPeriod;
    }

    public void setForcedPeriod(TimeFrame forcedPeriod) {
        this.forcedPeriod = forcedPeriod.getID();
        setForcedInterval(true);
    }

    public void setData(List<DataPointNode> data) {
        this.dataModelNode.setData(data);
        this.chartDataRows.clear();
        this.attributeMap.clear();

        for (DataPointNode dataPointNode : this.dataModelNode.getData()) {
            try {
                JEVisObject object = this.jeVisDataSource.getObject(dataPointNode.getObjectID());
                if (dataPointNode.getCleanObjectID() != null && dataPointNode.getCleanObjectID() > 0) {
                    object = this.jeVisDataSource.getObject(dataPointNode.getCleanObjectID());
                }

                if (object != null) {
                    JEVisAttribute jeVisAttribute = object.getAttribute(dataPointNode.getAttribute());
                    if (jeVisAttribute != null) {
                        ChartDataRow chartDataRow = new ChartDataRow(object.getDataSource());

                        /** add fake start date so the model does not ty to load the last 7 days **/
                        chartDataRow.setSelectedStart(new DateTime(1990, 1, 1, 1, 1, 1));
                        chartDataRow.setSelectedEnd(new DateTime(1990, 1, 1, 1, 1, 2));
                        chartDataRow.setAbsolute(dataPointNode.isAbsolute());

                        chartDataRow.setId(jeVisAttribute.getObject().getID());
                        chartDataRow.setObjectName(jeVisAttribute.getObject());
                        chartDataRow.setAttributeString(dataPointNode.getAttribute());
                        chartDataRow.setAttribute(jeVisAttribute);

                        if (dataPointNode.getManipulationMode() != null) {
                            chartDataRow.setManipulationMode(dataPointNode.getManipulationMode());
                            //Test
                        }

                        if (dataPointNode.getAggregationPeriod() != null) {
                            chartDataRow.setAggregationPeriod(dataPointNode.getAggregationPeriod());
                        }
                        initialAggregation.add(dataPointNode.getAggregationPeriod());
                        chartDataRow.setAxis(dataPointNode.getAxis());

                        if (dataPointNode.getColor() != null) {
                            chartDataRow.setColor(dataPointNode.getColor());
                        } else {
                            chartDataRow.setColor(Color.LIGHTBLUE);
                        }

                        if (dataPointNode.getChartType() != null) {
                            chartDataRow.setChartType(dataPointNode.getChartType());
                        } else {
                            chartDataRow.setChartType(ChartType.LINE);
                        }

                        chartDataRow.setName(dataPointNode.getName());

                        if (dataPointNode.getUnit() != null) {
                            chartDataRow.setUnit(dataPointNode.getUnit());
                        } else {
                            chartDataRow.setUnit(chartDataRow.getAttribute().getDisplayUnit());
                        }

                        this.chartDataRows.add(chartDataRow);
                        this.attributeMap.put(generateValueKey(jeVisAttribute), jeVisAttribute);

                        chartDataRow.setCalculation(dataPointNode.isEnpi());

                        if (autoAggregation) {
                            chartDataRow.setAbsolute(true);
                        }

                        chartDataRow.setBubbleType(dataPointNode.getBubbleType());
                        chartDataRow.setDecimalDigits(dataPointNode.getDecimalDigits());

                        if (dataPointNode.getCustomCSS() != null) {
                            chartDataRow.setCustomCSS(dataPointNode.getCustomCSS());
                        }


                    } else {
                        logger.error("Attribute does not exist: {}", dataPointNode.getAttribute());
                    }


                } else {
                    logger.error("Object not found: {}", dataPointNode.getObjectID());
                }
            } catch (Exception ex) {
//                logger.error("Error '{}' in line {}: ", ex.getMessage(), ex.getStackTrace()[0].getLineNumber(), ex.getStackTrace()[0]);
                ex.printStackTrace();
            }

        }
    }

    public ChartModel getChartModel() {
        ChartModel chartModel = new ChartModel();

        chartModel.setChartId(0);
        chartModel.setFilterEnabled(false);
        for (DataPointNode dataPointNode : this.dataModelNode.getData()) {
            ChartData chartData = new ChartData();
            chartData.setName(dataPointNode.getName());

            if (dataPointNode.getCleanObjectID() != null) {
                chartData.setId(dataPointNode.getCleanObjectID());
            } else {
                chartData.setId(dataPointNode.getObjectID());
            }
            try {
                chartData.setObjectName(jeVisDataSource.getObject(chartData.getId()));
            } catch (Exception ignored) {
            }

            chartData.setAttributeString(dataPointNode.getAttribute());

            if (dataPointNode.getUnit() != null) {
                chartData.setUnit(dataPointNode.getUnit());
            } else {
                try {
                    chartData.setUnit(jeVisDataSource.getObject(chartData.getId()).getAttribute(chartData.getAttributeString()).getDisplayUnit());
                } catch (Exception ignored) {
                }
            }
            chartData.setChartType(dataPointNode.getChartType());
            chartData.setColor(dataPointNode.getColor());
            chartData.setAggregationPeriod(dataPointNode.getAggregationPeriod());
            chartData.setManipulationMode(dataPointNode.getManipulationMode());
            chartData.setAxis(dataPointNode.getAxis());
            chartData.setDecimalDigits(dataPointNode.getDecimalDigits());
            chartData.setBubbleType(dataPointNode.getBubbleType());
            chartData.setCalculation(dataPointNode.isEnpi());
            chartData.setCss(dataPointNode.getCustomCSS());

            chartModel.getChartData().add(chartData);
        }

        return chartModel;
    }

    public void setChartModel(ChartModel chartModel) {
        List<DataPointNode> dataPointNodes = new ArrayList<>();
        chartModel.getChartData().forEach(chartData -> {
            DataPointNode dataPointNode = new DataPointNode();
            dataPointNode.setChartType(chartData.getChartType());
            dataPointNode.setAttribute(chartData.getAttributeString());
            dataPointNode.setColor(chartData.getColor());
            dataPointNode.setAxis(chartData.getAxis());
            dataPointNode.setDecimalDigits(chartData.getDecimalDigits());
            dataPointNode.setAggregationPeriod(chartData.getAggregationPeriod());
            dataPointNode.setCleanObjectID(chartData.getId());
            dataPointNode.setCustomCSS(chartData.getCss());
            dataPointNode.setManipulationMode(chartData.getManipulationMode());
            dataPointNode.setName(chartData.getName());
            dataPointNode.setObjectID(chartData.getId());
            dataPointNode.setUnit(chartData.getUnit().getLabel());
            dataPointNode.setEnpi(chartData.isCalculation());
            dataPointNode.setBubbleType(chartData.getBubbleType());

            if (autoAggregation) {
                dataPointNode.setAbsolute(true);
            }

            dataPointNodes.add(dataPointNode);
        });

        this.dataModelNode.setData(dataPointNodes);
        this.chartDataRows.clear();
        this.attributeMap.clear();

        for (DataPointNode dataPointNode : this.dataModelNode.getData()) {
            try {
                JEVisObject object = this.jeVisDataSource.getObject(dataPointNode.getObjectID());
                if (dataPointNode.getCleanObjectID() != null && dataPointNode.getCleanObjectID() > 0) {
                    object = this.jeVisDataSource.getObject(dataPointNode.getCleanObjectID());
                }


                if (object != null) {
                    JEVisAttribute jeVisAttribute = object.getAttribute(dataPointNode.getAttribute());
                    if (jeVisAttribute != null) {
                        ChartDataRow chartDataRow = new ChartDataRow(object.getDataSource());

                        /** add fake start date so the model does not ty to load the last 7 days **/
                        chartDataRow.setSelectedStart(new DateTime(1990, 1, 1, 1, 1, 1));
                        chartDataRow.setSelectedEnd(new DateTime(1990, 1, 1, 1, 1, 2));
                        chartDataRow.setAbsolute(dataPointNode.isAbsolute());

                        chartDataRow.setId(jeVisAttribute.getObject().getID());
                        chartDataRow.setObjectName(jeVisAttribute.getObject());
                        chartDataRow.setAttribute(jeVisAttribute);
                        chartDataRow.setAttributeString(dataPointNode.getAttribute());

                        if (dataPointNode.getManipulationMode() != null) {
                            chartDataRow.setManipulationMode(dataPointNode.getManipulationMode());
                        }
                        if (dataPointNode.getManipulationMode() != null) {
                            chartDataRow.setAggregationPeriod(dataPointNode.getAggregationPeriod());
                        }
                        initialAggregation.add(dataPointNode.getAggregationPeriod());
                        chartDataRow.setAxis(dataPointNode.getAxis());

                        if (dataPointNode.getColor() != null) {
                            chartDataRow.setColor(dataPointNode.getColor());
                        } else {
                            chartDataRow.setColor(Color.LIGHTBLUE);
                        }

                        if (dataPointNode.getChartType() != null) {
                            chartDataRow.setChartType(dataPointNode.getChartType());
                        } else {
                            chartDataRow.setChartType(ChartType.LINE);
                        }

                        chartDataRow.setName(dataPointNode.getName());

                        if (dataPointNode.getUnit() != null) {
                            chartDataRow.setUnit(dataPointNode.getUnit());
                        } else {
                            chartDataRow.setUnit(chartDataRow.getAttribute().getDisplayUnit());
                        }

                        this.chartDataRows.add(chartDataRow);
                        this.attributeMap.put(generateValueKey(jeVisAttribute), jeVisAttribute);

                        chartDataRow.setCalculation(dataPointNode.isEnpi());

                        if (autoAggregation) {
                            chartDataRow.setAbsolute(true);
                        }

                        chartDataRow.setDecimalDigits(dataPointNode.getDecimalDigits());

                        if (dataPointNode.getCustomCSS() != null) {
                            chartDataRow.setCustomCSS(dataPointNode.getCustomCSS());
                        }

                        chartDataRow.setBubbleType(dataPointNode.getBubbleType());


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
    }

    public DashboardControl getDashboardControl() {
        return dashboardControl;
    }
}
