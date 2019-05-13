package org.jevis.jeconfig.plugin.Dashboard.datahandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.*;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeFactory;
import org.jevis.jeconfig.application.jevistree.plugin.SimpleTargetPlugin;
import org.jevis.jeconfig.plugin.Dashboard.config.DataModelNode;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataModelDataHandler {

    public final static String TYPE = "SimpleDataHandler";
    private static final Logger logger = LogManager.getLogger(DataModelDataHandler.class);
    private final JEVisDataSource jeVisDataSource;
    public ObjectProperty<DateTime> lastUpdate = new SimpleObjectProperty<>();
    private Map<String, JEVisAttribute> attributeMap = new HashMap<>();
    private BooleanProperty enableMultiSelect = new SimpleBooleanProperty(false);
    private StringProperty unitProperty = new SimpleStringProperty("");
    private SimpleTargetPlugin simpleTargetPlugin = new SimpleTargetPlugin();
    private List<ChartDataModel> chartDataModels = new ArrayList<>();
    private ObjectProperty<Interval> durationProperty = new SimpleObjectProperty<>();
    private DataModelNode dataModelNode = new DataModelNode();
    private boolean autoAggregation = true;


    public DataModelDataHandler(JEVisDataSource jeVisDataSource, JsonNode configNode) {
        this.jeVisDataSource = jeVisDataSource;

        try {
            ObjectMapper mapper = new ObjectMapper();
            DataModelNode dataModelNode = mapper.treeToValue(configNode, DataModelNode.class);
            this.dataModelNode = dataModelNode;
        } catch (Exception ex) {
            logger.error(ex);
        }

        dataModelNode.getData().forEach(dataPointNode -> {
            try {
                logger.debug("Add attribute: {}:{}", dataPointNode.getObjectID(), dataPointNode.getAttribute());
                JEVisObject jevisobject = jeVisDataSource.getObject(dataPointNode.getObjectID());
                JEVisObject cleanObject = jeVisDataSource.getObject(dataPointNode.getCleanObjectID());
                if (jevisobject != null) {
                    JEVisAttribute jeVisAttribute = jevisobject.getAttribute(dataPointNode.getAttribute());
                    if (jeVisAttribute != null) {
                        ChartDataModel chartDataModel = new ChartDataModel(jevisobject.getDataSource());
                        List<Integer> list = new ArrayList<>();
                        list.add(0);

                        /** add fake start date so the model does not ty to load the last 7 days **/
                        chartDataModel.setSelectedStart(new DateTime(1000, 1, 1, 1, 1, 1));
                        chartDataModel.setSelectedEnd(new DateTime(1000, 1, 1, 1, 1, 2));

                        chartDataModel.setSelectedCharts(list);
                        chartDataModel.setObject(jeVisAttribute.getObject());
                        chartDataModel.setAttribute(jeVisAttribute);
                        if (cleanObject != null) {
                            chartDataModel.setDataProcessor(cleanObject);
                            chartDataModel.setAttribute(cleanObject.getAttribute(dataPointNode.getAttribute()));
                        }

                        chartDataModel.setManipulationMode(dataPointNode.getManipulationMode());
                        chartDataModel.setAggregationPeriod(dataPointNode.getAggregationPeriod());


                        if (dataPointNode.getColor() != null) {
                            chartDataModel.setColor(dataPointNode.getColor());
                        } else {
                            chartDataModel.setColor(Color.LIGHTBLUE);
                        }


                        chartDataModels.add(chartDataModel);


                        attributeMap.put(generateValueKey(jeVisAttribute), jeVisAttribute);

                        if (dataPointNode.isEnpi()) {
                            chartDataModel.setEnPI(dataPointNode.isEnpi());
                            chartDataModel.setCalculationObject(dataPointNode.getCalculationID().toString());
                        }

                    } else {
                        logger.error("Attribute does not exist: {}", dataPointNode.getAttribute());
                    }


                } else {
                    logger.error("Object not found: {}", dataPointNode.getObjectID());
                }
            } catch (Exception ex) {
                logger.error("Error in line {}: ", ex.getStackTrace()[0].getLineNumber(), ex.getStackTrace()[0]);
            }

        });
    }

    /**
     * Set if the date in the interval will use the auto aggregation
     * [if -> then]
     * Day -> Display Interval
     * Week -> Hourly
     * Month -> Daily
     * Year -> Weekly
     *
     * @param enable
     */
    public void setAutoAggregation(boolean enable) {
        autoAggregation = enable;
    }

    public static String generateValueKey(JEVisAttribute attribute) {
        return attribute.getObjectID() + ":" + attribute.getName();
    }


    public Tab getConfigTab() {
        Tab tab = new Tab(I18n.getInstance().getString("plugin.dashboard.widget.config.tab.datamodel"));

        WidgetTreePlugin widgetTreePlugin = new WidgetTreePlugin();

        JEVisTree tree = JEVisTreeFactory.buildDefaultWidgetTree(jeVisDataSource, widgetTreePlugin);
        tab.setContent(tree);
        widgetTreePlugin.setUserSelection(dataModelNode.getData());


        return tab;

    }

    public void setInterval(Interval interval) {
        this.durationProperty.setValue(interval);
        getDataModel().forEach(chartDataModel -> {
            AggregationPeriod aggregationPeriod = AggregationPeriod.NONE;
            ManipulationMode manipulationMode = ManipulationMode.NONE;
            if (autoAggregation) {

                /** less then an week take original **/
                if (interval.toDuration().getStandardDays() < 6) {
                    aggregationPeriod = AggregationPeriod.NONE;
                }
                /** less then an month take hour **/
                else if (interval.toDuration().getStandardDays() < 32) {
                    aggregationPeriod = AggregationPeriod.HOURLY;
                    manipulationMode = ManipulationMode.TOTAL;
                }
                /** less than year take day **/
                else if (interval.toDuration().getStandardDays() < 364) {
                    aggregationPeriod = AggregationPeriod.DAILY;
                    manipulationMode = ManipulationMode.TOTAL;
                }
                /** more than an year take week **/
                else {
                    aggregationPeriod = AggregationPeriod.WEEKLY;
                    manipulationMode = ManipulationMode.TOTAL;
                }
                chartDataModel.setAggregationPeriod(aggregationPeriod);
                chartDataModel.setManipulationMode(manipulationMode);
            }


        });
    }

    public JsonNode toJsonNode() {
        ArrayNode dataArrayNode = JsonNodeFactory.instance.arrayNode();
        attributeMap.forEach((s, jeVisAttribute) -> {
            ObjectNode dataNode = JsonNodeFactory.instance.objectNode();
            dataNode.put("object", jeVisAttribute.getObjectID());
            dataNode.put("attribute", jeVisAttribute.getName());
            dataArrayNode.add(dataNode);
        });

        ObjectNode dataHandlerNode = JsonNodeFactory.instance.objectNode();
        dataHandlerNode.set("data", dataArrayNode);
        dataHandlerNode.set("type", JsonNodeFactory.instance.textNode(TYPE));


        return dataArrayNode;

    }

    public Map<String, JEVisAttribute> getAttributeMap() {
        return attributeMap;
    }

    public List<ChartDataModel> getDataModel() {
        return chartDataModels;
    }

    public void update() {
        logger.debug("Update Samples: {}", durationProperty.getValue());
//        logger.error("AttributeMap: {}", attributeMap.size());

        chartDataModels.forEach(chartDataModel -> {

            chartDataModel.setSelectedStart(durationProperty.getValue().getStart());
            chartDataModel.setSelectedEnd(durationProperty.getValue().getEnd());

        });

        lastUpdate.setValue(new DateTime());
    }

    public void setMultiSelect(boolean enable) {
        this.enableMultiSelect.set(enable);
    }


    public StringProperty getUnitProperty() {
        return unitProperty;
    }


    public static Double getTotal(List<JEVisSample> samples) {
        Double total = 0d;
        for (JEVisSample jeVisSample : samples) {
            try {
                total += jeVisSample.getValueAsDouble();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return total;

    }
}
