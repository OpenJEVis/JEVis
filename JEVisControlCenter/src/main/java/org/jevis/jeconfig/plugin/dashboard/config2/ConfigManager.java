package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashBordPlugIn;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrameFactory;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrames;
import org.jevis.jeconfig.plugin.dashboard.widget.Size;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;
import org.jevis.jeconfig.plugin.dashboard.widget.Widgets;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.jevis.jeconfig.plugin.dashboard.config2.JsonNames.Dashboard.*;

public class ConfigManager {


    private final JEVisDataSource jeVisDataSource;
    private ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(ConfigManager.class);
    private final TimeFrames timeFrames;
    private JEVisObject dashboardObject = null;

    public ConfigManager(JEVisDataSource dataSource) {
        this.jeVisDataSource = dataSource;
        this.timeFrames = new TimeFrames(this.jeVisDataSource);
    }

    public JsonNode readDashboardFile(JEVisObject dashboardObject) {
        try {
            this.dashboardObject = dashboardObject;
            JEVisSample lastConfigSample = dashboardObject.getAttribute(DashBordPlugIn.ATTRIBUTE_DATA_MODEL_FILE).getLatestSample();
            JEVisFile file = lastConfigSample.getValueAsFile();
            JsonNode jsonNode = this.mapper.readTree(file.getBytes());
            return jsonNode;

        } catch (Exception ex) {
            logger.error(ex);
            logger.error("Missing Json File configuration");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(I18n.getInstance().getString("plugin.dashboard.load.error.file.header"));
            alert.setContentText(I18n.getInstance().getString("plugin.dashboard.load.error.file.content"));

            alert.showAndWait();
            return null;
        }
    }

    public void saveDashboard(DashboardPojo dashboardPojo, List<Widget> widgets) throws IOException, JEVisException {

        ObjectNode dashboardNode = toJson(dashboardPojo, widgets);

//        System.out.println("---------\n" + this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dashboardNode) + "\n-----------------");
        if (this.dashboardObject != null) {
            JEVisAttribute dataModel = this.dashboardObject.getAttribute(DashBordPlugIn.ATTRIBUTE_DATA_MODEL_FILE);
            JEVisFileImp jsonFile = new JEVisFileImp("dashboard.json", this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dashboardNode).getBytes());
            JEVisSample newSample = dataModel.buildSample(new DateTime(), jsonFile);
            newSample.commit();
        }

    }

    public ObjectNode toJson(DashboardPojo dashboardPojo, List<Widget> widgets) {

        try {

            ObjectNode dashBoardNode = this.mapper.createObjectNode();
//            this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            dashBoardNode
                    .put(JSON_VERSION, "1.1")
                    .put(BACKGROUND_COLOR, dashboardPojo.getBackgroundColor().toString())
                    .put(SHOW_GRID, dashboardPojo.getShowGrid())
                    .put(X_GRID_INTERVAL, dashboardPojo.getxGridInterval())
                    .put(Y_GRID_INTERVAL, dashboardPojo.getyGridInterval())
                    .put(UPDATE_RATE, dashboardPojo.getUpdateRate())
                    .put(ZOOM_FACTOR, dashboardPojo.getZoomFactor())
                    .put(WIDTH, dashboardPojo.getSize().getWidth())
                    .put(HEIGHT, dashboardPojo.getSize().getHeight())
                    .put(DEFAULT_PERIOD, dashboardPojo.getTimeFrame().getID());

//            try {
//                if (timeFrame != null) {
//
//                } else {
//                    dashBoardNode.put(DEFAULT_PERIOD, Period.weeks(1).toString());
//                }
//
//            } catch (Exception ex) {
//                dashBoardNode.put(DEFAULT_PERIOD, Period.weeks(1).toString());
//                logger.error(ex);
//            }

//

//            List<ObjectNode> widgetNodes = new ArrayList<>();
            ArrayNode widgetArray = dashBoardNode.putArray(WIDGET_NODE);
            for (Widget widget : widgets) {
                try {
                    ObjectNode widgetObjectNode = widget.toNode();
                    if (widgetObjectNode != null) {
//                        widgetNodes.add(widgetObjectNode);
                        widgetArray.add(widgetObjectNode);
                    }
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }


            return dashBoardNode;


        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }

        return null;
    }

    public DashboardPojo loadDashboard(JsonNode jsonNode) {

        DashboardPojo dashboardPojo = new DashboardPojo();
        dashboardPojo.setTitle("New dashboard");


        if (jsonNode == null) return dashboardPojo;

        if (this.dashboardObject != null) {
            dashboardPojo.setTitle(this.dashboardObject.getName());
        }


        try {

            try {
                dashboardPojo.setVersion(jsonNode.get(JSON_VERSION).asText("1.0"));
            } catch (Exception ex) {
                dashboardPojo.setVersion("0.9");
                logger.error("Could not parse {}: {}", JSON_VERSION, ex);
            }


            try {
                String defaultPeriodStrg = jsonNode.get(DEFAULT_PERIOD).asText(Period.days(2).toString());
                for (TimeFrameFactory timeFrameFactory : this.timeFrames.getAll()) {
//                    System.out.println("tf: " + timeFrameFactory.getID());
                    if (timeFrameFactory.getID().equals(defaultPeriodStrg)) {
                        dashboardPojo.setTimeFrame(timeFrameFactory);
                    }
                }
                if (dashboardPojo.getTimeFrame() == null) {
                    logger.error("Missing Timeframe: {}  ", defaultPeriodStrg);

                }


            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", DEFAULT_PERIOD, ex);
                dashboardPojo.setTimeFrame(this.timeFrames.week());
            }


            try {
                Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
                Size newSize = new Size(jsonNode.get("height").asDouble(primaryScreenBounds.getHeight() - 80)
                        , jsonNode.get("width").asDouble(primaryScreenBounds.getWidth() - 35));
                logger.error("------ josn.size: {}/{}  {}", jsonNode.get("width"), jsonNode.get("height"), newSize);


                dashboardPojo.setSize(newSize);
            } catch (Exception ex) {
                logger.error("Could not parse Size: {}", ex);
            }

            try {
                dashboardPojo.setBackgroundColor(Color.valueOf(jsonNode.get(BACKGROUND_COLOR).asText(Color.TRANSPARENT.toString())));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", BACKGROUND_COLOR, ex);
            }

            try {
                dashboardPojo.setxGridInterval(jsonNode.get(X_GRID_INTERVAL).asDouble(50d));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", X_GRID_INTERVAL, ex);
            }

            try {
                dashboardPojo.setyGridInterval(jsonNode.get(Y_GRID_INTERVAL).asDouble(50d));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", Y_GRID_INTERVAL, ex);
            }

            try {
                dashboardPojo.setZoomFactor(jsonNode.get(ZOOM_FACTOR).asDouble(1d));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", ZOOM_FACTOR, ex);
            }
            try {
                dashboardPojo.setUpdateRate(jsonNode.get(UPDATE_RATE).asInt(900));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", UPDATE_RATE, ex);
            }
            try {
                dashboardPojo.setSnapToGrid(jsonNode.get(SNAP_TO_GRID).asBoolean(true));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", SNAP_TO_GRID, ex);
            }
            try {
                dashboardPojo.setShowGrid(jsonNode.get(SHOW_GRID).asBoolean(true));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", SHOW_GRID, ex);
            }
            JsonNode widgets = jsonNode.get(WIDGET_NODE);

            if (widgets.isArray()) {
                for (final JsonNode objNode : widgets) {
                    WidgetPojo newConfig = new WidgetPojo(objNode);
                    dashboardPojo.getWidgetList().add(newConfig);
                    logger.debug("Add Widget: {}", newConfig);
                }
            }


        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }

        return dashboardPojo;
    }

    public List<Widget> createWidgets(DashboardControl control, List<WidgetPojo> widgetConfigList) {
        List<Widget> widgetList = new ArrayList<>();
        widgetConfigList.forEach(widgetPojo -> {
            Widget newWidget = createWidget(control, widgetPojo);
            if (newWidget != null) {
                widgetList.add(newWidget);
            }

        });
        return widgetList;
    }

    public Widget createWidget(DashboardControl control, WidgetPojo widget) {
        for (Widget availableWidget : Widgets.getAvabableWidgets(control, widget)) {
            try {
                if (availableWidget.typeID().equalsIgnoreCase(widget.getType())) {
//                    widget.setType(availableWidget.getId());
                    availableWidget.init();

                    return availableWidget;
                }
            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        return null;
    }

    public void setBackgroundImage(JEVisObject analysisObject, java.io.File file) {
        try {
            JEVisAttribute bgFile = analysisObject.getAttribute(DashBordPlugIn.ATTRIBUTE_BACKGROUND);
            JEVisFileImp jeVisFileImp = new JEVisFileImp(file.getName(), file);
            JEVisSample jeVisSample = bgFile.buildSample(new DateTime(), jeVisFileImp);
            jeVisSample.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ObjectProperty<Image> getBackgroundImage(JEVisObject analysisObject) {
        ObjectProperty<Image> imageBoardBackground = new SimpleObjectProperty<>(Image.class, "Dash Board Color", JEConfig.getImage("transPixel.png"));

        Task<Image> imageLoadTask = new Task<Image>() {
            @Override
            public Image call() throws InterruptedException {
                try {
//                    logger.error("getBackgroundImage: {}", analysisObject);
//                    if (analysisObject == null) {
//                        return JEConfig.getImage("transPixel.png");
//                    }
                    JEVisAttribute bgFile = analysisObject.getAttribute(DashBordPlugIn.ATTRIBUTE_BACKGROUND);
                    if (bgFile != null && bgFile.hasSample()) {
                        JEVisSample backgroundImage = bgFile.getLatestSample();
                        if (backgroundImage != null) {
                            JEVisFile imageFile = backgroundImage.getValueAsFile();
                            InputStream in = new ByteArrayInputStream(imageFile.getBytes());
                            return new Image(in);
                        }
                    }


                } catch (Exception ex) {
                    logger.error("Could load background image: {}", ex);
                }
                throw new InterruptedException("could load background image");
            }
        };

        imageLoadTask.setOnSucceeded(e -> imageBoardBackground.setValue(imageLoadTask.getValue()));
        new Thread(imageLoadTask).start();

        return imageBoardBackground;
    }


    public DashboardPojo createEmptyDashboard() {
        DashboardPojo empty = new DashboardPojo();
        TimeFrameFactory factory = this.timeFrames.day();
        empty.setTimeFrame(factory);
        empty.setInterval(new Interval(new DateTime(), new DateTime()));
        empty.setJevisObject(null);
        return empty;
    }
}
