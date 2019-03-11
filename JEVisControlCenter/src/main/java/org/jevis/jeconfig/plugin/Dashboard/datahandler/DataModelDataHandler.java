package org.jevis.jeconfig.plugin.Dashboard.datahandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Columns.ColorColumn;
import org.jevis.jeconfig.application.jevistree.Finder;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeFactory;
import org.jevis.jeconfig.application.jevistree.SearchFilterBar;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.jevistree.plugin.SimpleTargetPlugin;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.jevis.jeconfig.plugin.Dashboard.wizzard.Page;
import org.jevis.jeconfig.tool.Layouts;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DataModelDataHandler extends SampleHandler {
    public final static String TYPE = "SimpleDataHandler";
    private static final Logger logger = LogManager.getLogger(DataModelDataHandler.class);
    public ObjectProperty<DateTime> lastUpdate = new SimpleObjectProperty<>();
    public UUID uuid = UUID.randomUUID();

    Map<String, JEVisAttribute> attributeMap = new HashMap<>();
    private BooleanProperty enableMultiSelect = new SimpleBooleanProperty(false);
    private StringProperty unitProperty = new SimpleStringProperty("");
    private SimpleTargetPlugin simpleTargetPlugin = new SimpleTargetPlugin();
    private ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private List<ChartDataModel> chartDataModels = new ArrayList<>();

    public DataModelDataHandler(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource);
    }

    public DataModelDataHandler(JEVisDataSource jeVisDataSource, JsonNode configNode) {
        super(jeVisDataSource);
        logger.error("Has UUID: {}", uuid.toString());


        JsonNode attributeNodes = configNode.get("data");
        if (attributeNodes.isArray()) {
            AtomicInteger index = new AtomicInteger(-1);
            for (final JsonNode userSelection : attributeNodes) {
                try {
                    index.set(index.get() + 1);
                    long objectID = userSelection.get("object").asLong();
                    String attribute = userSelection.get("attribute").asText("Value");
                    logger.error("Add attribute: {}:{}", objectID, attribute);
                    JEVisObject jevisobject = jeVisDataSource.getObject(objectID);
                    if (jevisobject != null) {
                        JEVisAttribute jeVisAttribute = jevisobject.getAttribute(attribute);
                        if (jeVisAttribute != null) {
                            ChartDataModel chartDataModel = new ChartDataModel(jevisobject.getDataSource());
                            List<Integer> list = new ArrayList<>();
                            list.add(0);

                            chartDataModel.setSelectedCharts(list);
                            chartDataModel.setObject(jeVisAttribute.getObject());
                            chartDataModel.setAttribute(jeVisAttribute);
                            chartDataModel.setManipulationMode(ManipulationMode.TOTAL);
                            chartDataModel.setAggregationPeriod(AggregationPeriod.HOURLY);
                            chartDataModel.setColor(ColorColumn.color_list[index.get()]);

                            chartDataModels.add(chartDataModel);

                            attributeMap.put(generateValueKey(jeVisAttribute), jeVisAttribute);
                        } else {
                            logger.error("Attribute does not exist: {}", attribute);
                        }

                    } else {
                        logger.error("Object not found: {}", objectID);
                    }
                } catch (Exception ex) {
                    logger.error("Error while loading data: {}", ex);
                }
            }
        } else {
            logger.error("Error: user selection is not an array");
        }

    }

    public static String generateValueKey(JEVisAttribute attribute) {
        return attribute.getObjectID() + ":" + attribute.getName();
    }

    public void setInterval(Interval interval) {
        this.durationProperty.setValue(interval);
    }

    @Override
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

    @Override

    public void update() {
        logger.error("Update Samples: {} -> {}", uuid.toString(), durationProperty.getValue());
        logger.error("AttributeMap: {}", attributeMap.size());

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

    @Override
    public void setUserSelectionDone() {
        System.out.println("Selection Done");
//        simpleTargetPlugin.getUserSelection().forEach(userSelection -> {
//            System.out.println("Userselect: " + userSelection.getSelectedObject() + "  att: " + userSelection.getSelectedAttribute());
//            String key = generateValueKey(userSelection.getSelectedAttribute());
//            valueMap.put(key, new ArrayList<>());
//            attributeMap.put(key, userSelection.getSelectedAttribute());
//        });
    }

    @Override
    public Page getPage() {
        AnchorPane anchorPane = new AnchorPane();


        JEVisTree tree = JEVisTreeFactory.buildBasicDefault(getDataSource(), false);
        tree.getPlugins().add(simpleTargetPlugin);
        tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        simpleTargetPlugin.setAllowMultiSelection(enableMultiSelect.getValue());
        simpleTargetPlugin.setMode(SimpleTargetPlugin.MODE.ATTRIBUTE);

        ObservableList<JEVisTreeFilter> filterTypes = FXCollections.observableArrayList();
        filterTypes.setAll(SelectTargetDialog.buildAllAttributesFilter());

        Finder finder = new Finder(tree);
        SearchFilterBar searchBar = new SearchFilterBar(tree, filterTypes, finder);

        enableMultiSelect.addListener((observable, oldValue, newValue) -> {
            simpleTargetPlugin.setAllowMultiSelection(newValue);

        });

        anchorPane.getChildren().addAll(tree, searchBar);
        Layouts.setAnchor(tree, 1.0);
        AnchorPane.setBottomAnchor(tree, 40.0);
        AnchorPane.setLeftAnchor(searchBar, 1.0);
        AnchorPane.setBottomAnchor(searchBar, 1.0);
        AnchorPane.setRightAnchor(searchBar, 1.0);


        Page page = new Page() {
            @Override
            public Node getNode() {
                return anchorPane;
            }

            @Override
            public boolean isSkipable() {
                return false;
            }
        };

        return page;
    }
}
