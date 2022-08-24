package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.jeapi.ws.JEVisDataSourceWS;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.control.SaveUnderDialog;
import org.jevis.jeconfig.plugin.dashboard.DashBordPlugIn;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.common.WidgetIDs;
import org.jevis.jeconfig.plugin.dashboard.config.BackgroundMode;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrame;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrameFactory;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;
import org.jevis.jeconfig.plugin.dashboard.widget.Widgets;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.jevis.jeconfig.plugin.dashboard.config2.JsonNames.Dashboard.*;

public class ConfigManager {


    private final StackPane dialogPane;
    private final JEVisDataSource jeVisDataSource;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(ConfigManager.class);
    private final TimeFrameFactory timeFrameFactory;
    private JEVisObject dashboardObject = null;
    private final ObjectRelations objectRelations;

    public ConfigManager(StackPane dialogPane, JEVisDataSource dataSource) {
        this.dialogPane = dialogPane;
        this.jeVisDataSource = dataSource;
        this.objectRelations = new ObjectRelations(jeVisDataSource);
        this.timeFrameFactory = new TimeFrameFactory(this.jeVisDataSource);
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);

    }

    public JsonNode readDashboardFile(JEVisObject dashboardObject) throws Exception {
        this.dashboardObject = dashboardObject;
        if (dashboardObject.getDataSource() instanceof JEVisDataSourceWS) {
            dashboardObject.getDataSource().reloadAttribute(dashboardObject);
        }
        JEVisSample lastConfigSample = dashboardObject.getAttribute(DashBordPlugIn.ATTRIBUTE_DATA_MODEL_FILE).getLatestSample();
        JEVisFile file = lastConfigSample.getValueAsFile();
        JsonNode jsonNode = this.mapper.readTree(file.getBytes());
        return jsonNode;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }


    public void saveDashboard(DashboardPojo dashboardPojo, List<Widget> widgets, String filename, java.io.File wallpaper) throws IOException, JEVisException {
        ObjectNode dashboardNode = toJson(dashboardPojo, widgets);
        JEVisObject dashboardObject = dashboardPojo.getDashboardObject();

        logger.debug("---------\n {} \n-----------------", this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dashboardNode));
        if (this.dashboardObject != null) {
            JEVisAttribute dataModel = dashboardObject.getAttribute(DashBordPlugIn.ATTRIBUTE_DATA_MODEL_FILE);
            JEVisFileImp jsonFile = new JEVisFileImp(
                    filename + "_" + DateTime.now().toString("yyyyMMddHHmm") + ".json"
                    , this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dashboardNode).getBytes(StandardCharsets.UTF_8));
            JEVisSample newSample = dataModel.buildSample(new DateTime(), jsonFile);
            newSample.commit();


            if (wallpaper != null) {
                setBackgroundImage(dashboardObject, wallpaper);
            }
        }
    }

    public ObjectNode toJson(DashboardPojo dashboardPojo, List<Widget> widgets) {

        try {

            ObjectNode dashBoardNode = this.mapper.createObjectNode();
//            this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            dashBoardNode
                    .put(JSON_VERSION, "1.2")
                    .put(BACKGROUND_COLOR, dashboardPojo.getBackgroundColor().toString())
                    .put(BACKGROUND_MODE, dashboardPojo.getBackgroundMode())
//                    .put(SHOW_GRID, dashboardPojo.getShowGrid())
                    .put(SNAP_TO_GRID, false)//dashboardPojo.getSnapToGrid()
                    .put("Show Grid", false)//old Version Fallback
                    .put(X_GRID_INTERVAL, dashboardPojo.getxGridInterval())
                    .put(Y_GRID_INTERVAL, dashboardPojo.getyGridInterval())
                    .put(UPDATE_RATE, dashboardPojo.getUpdateRate())
                    .put(ZOOM_FACTOR, dashboardPojo.getZoomFactor())
                    .put(WIDTH, dashboardPojo.getSize().getWidth())
                    .put(HEIGHT, dashboardPojo.getSize().getHeight())
                    .put(DEFAULT_PERIOD, dashboardPojo.getTimeFrame().getID());

            ArrayNode widgetArray = dashBoardNode.putArray(WIDGET_NODE);
            for (Widget widget : widgets) {
                try {
                    ObjectNode widgetObjectNode = widget.toNode();
                    if (widgetObjectNode != null) {
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
                if (dashboardObject != null) {
                    this.timeFrameFactory.setActiveDashboard(dashboardObject);
                }

                for (TimeFrame timeFrame : this.timeFrameFactory.getAll()) {
//                    System.out.println("tf: " + timeFrameFactory.getID());
                    if (timeFrame.getID().equals(defaultPeriodStrg)) {
                        dashboardPojo.setTimeFrame(timeFrame);
                    }
                }

                if (dashboardPojo.getTimeFrame() == null) {
                    logger.error("Missing Timeframe: {}  ", defaultPeriodStrg);
                }


            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", DEFAULT_PERIOD, ex);
                dashboardPojo.setTimeFrame(this.timeFrameFactory.week());
            }


            try {
                /** Now we use 1920 x 1080 (minus the boarders) as default for all **/
                Size newSize = new Size(jsonNode.get("height").asDouble(886.0)
                        , jsonNode.get("width").asDouble(1863.0));


                //Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
                //Size newSize = new Size(jsonNode.get("height").asDouble(primaryScreenBounds.getHeight() - 80)
                //       , jsonNode.get("width").asDouble(primaryScreenBounds.getWidth() - 35));
                logger.debug("------ josn.size: {}/{}  {}", jsonNode.get("width"), jsonNode.get("height"), newSize);


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
                dashboardPojo.setBackgroundMode(jsonNode.get(BACKGROUND_MODE).asText(BackgroundMode.defaultMode));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", BACKGROUND_MODE, ex);
            }

            try {
                dashboardPojo.setxGridInterval(jsonNode.get(X_GRID_INTERVAL).asDouble(25d));
                //dashboardPojo.setxGridInterval(25d);//tmp workaround
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", X_GRID_INTERVAL, ex);
            }

            try {
                dashboardPojo.setyGridInterval(jsonNode.get(Y_GRID_INTERVAL).asDouble(25d));
                //dashboardPojo.setyGridInterval(25d);//tmp workaround
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

            try {
                dashboardPojo.setZoomFactor(jsonNode.get(ZOOM_FACTOR).asDouble(1.0d));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", ZOOM_FACTOR, ex);
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
            try {
                Widget newWidget = createWidget(control, widgetPojo);
                if (newWidget != null) {
                    /** workaround for old dashboard where the uuid did not exist **/
                    if (newWidget.getConfig().getUuid() <= 0) {
                        newWidget.getConfig().setUuid(WidgetIDs.getNetxFreeeID(widgetList));
                    }
                    newWidget.init();
//                newWidget.updateConfig();
                    widgetList.add(newWidget);
                }
            } catch (Exception ex) {
                logger.error(ex);
            }

        });

        return widgetList;
    }

    public Widget createWidget(DashboardControl control, WidgetPojo widget) {
        try {
            return Widgets.createWidget(widget.getType(), control, widget);
        } catch (Exception ex) {
            ex.printStackTrace();
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
        if (analysisObject == null) {
            return null;
        }


//        logger.debug("getBackgroundImage: {}", analysisObject.getID());


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
                    logger.error("Could not load background image: {}", ex, ex);
                }
                throw new InterruptedException("could not load background image");
            }
        };

        imageLoadTask.setOnSucceeded(e -> imageBoardBackground.setValue(imageLoadTask.getValue()));
        new Thread(imageLoadTask).start();

        return imageBoardBackground;
    }


    public DashboardPojo createEmptyDashboard() {
        DashboardPojo empty = new DashboardPojo();
        TimeFrame factory = this.timeFrameFactory.day();
        empty.setTimeFrame(factory);
        empty.setInterval(new Interval(new DateTime(), new DateTime()));
        empty.setJevisObject(null);
        return empty;
    }


    public void openSaveUnder(DashboardPojo dashboardPojo, ObservableList<Widget> widgetList, File wallpaper) {
        try {
            logger.error("openSaveUnder: {},{},{},{}", dashboardPojo, wallpaper);
            JEVisClass dashboardClass = jeVisDataSource.getJEVisClass(DashBordPlugIn.CLASS_ANALYSIS);

            SaveUnderDialog saveUnderDialog = new SaveUnderDialog(dialogPane, jeVisDataSource, dashboardPojo.getDashboardObject(), dashboardClass, dashboardPojo.getTitle(), (target, sameObject) -> {
                logger.error("Start save");
                try {

                    JEVisAttribute bgFile = null;
                    if (dashboardPojo != null && dashboardPojo.getDashboardObject() != null) {
                        bgFile = dashboardPojo.getDashboardObject().getAttribute(DashBordPlugIn.ATTRIBUTE_BACKGROUND);
                    }

                    logger.error("Wallpaper: {},{},{}", sameObject, wallpaper, bgFile);
                    if (!sameObject && wallpaper == null && bgFile != null && bgFile.hasSample()) {
                        JEVisSample wallPaperCopy = target.getAttribute(DashBordPlugIn.ATTRIBUTE_BACKGROUND).buildSample(new DateTime(), bgFile.getLatestSample().getValueAsFile());
                        wallPaperCopy.commit();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                dashboardPojo.setTitle(target.getName());
                dashboardPojo.setJevisObject(target);

                try {
                    saveDashboard(dashboardPojo, widgetList, target.getName(), wallpaper);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return true;
            });
            saveUnderDialog.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        /**

         Dialog<ButtonType> newAnalysis = new Dialog<>();
         newAnalysis.setTitle(I18n.getInstance().getString("plugin.graph.dialog.new.title"));
         Label newText = new Label(I18n.getInstance().getString("plugin.graph.dialog.new.name"));
         Label directoryText = new Label(I18n.getInstance().getString("plugin.graph.dialog.new.directory"));
         JFXTextField name = new JFXTextField();

         JEVisClass analysesDirectory = null;
         List<JEVisObject> listAnalysesDirectories = null;
         AtomicBoolean hasMultiDirs = new AtomicBoolean(false);
         try {
         analysesDirectory = jeVisDataSource.getJEVisClass("Analyses Directory");
         listAnalysesDirectories = jeVisDataSource.getObjects(analysesDirectory, false);
         hasMultiDirs.set(listAnalysesDirectories.size() > 1);

         } catch (JEVisException e) {
         e.printStackTrace();
         }

         ObjectProperty<JEVisObject> currentAnalysisDirectory = new SimpleObjectProperty<>(null);
         JFXComboBox<JEVisObject> parentsDirectories = new JFXComboBox<>(FXCollections.observableArrayList(listAnalysesDirectories));

         Callback<ListView<JEVisObject>, ListCell<JEVisObject>> cellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
        @Override public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
        return new ListCell<JEVisObject>() {
        @Override protected void updateItem(JEVisObject obj, boolean empty) {
        super.updateItem(obj, empty);
        if (empty || obj == null || obj.getName() == null) {
        setText("");
        } else {
        if (!hasMultiDirs.get())
        setText(obj.getName());
        else {
        String prefix = objectRelations.getObjectPath(obj);

        setText(prefix + obj.getName());
        }
        }

        }
        };
        }
        };
         parentsDirectories.setCellFactory(cellFactory);
         parentsDirectories.setButtonCell(cellFactory.call(null));

         parentsDirectories.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
         if (newValue != null && newValue != oldValue) {
         currentAnalysisDirectory.setValue(newValue);
         }
         });
         parentsDirectories.getSelectionModel().selectFirst();


         if (dashboardPojo != null) {
         try {
         if (dashboardPojo.getDashboardObject().getParents() != null) {
         JEVisObject parenObj = dashboardPojo.getDashboardObject().getParents().get(0);
         parentsDirectories.getSelectionModel().select(parenObj);
         }
         } catch (Exception e) {
         logger.error("Couldn't select current Analysis Directory: " + e);
         }

         name.setText(dashboardPojo.getName());

         }

         name.focusedProperty().addListener((ov, t, t1) -> Platform.runLater(() -> {
         if (name.isFocused() && !name.getText().isEmpty()) {
         name.selectAll();
         }
         }));

         final JFXButtonType ok = new JFXButtonType(I18n.getInstance().getString("plugin.graph.dialog.new.ok"), JFXButtonBar.ButtonData.OK_DONE);
         final JFXButtonType cancel = new JFXButtonType(I18n.getInstance().getString("plugin.graph.dialog.new.cancel"), JFXButtonBar.ButtonData.CANCEL_CLOSE);

         GridPane gridLayout = new GridPane();
         gridLayout.setPadding(new Insets(10, 10, 10, 10));
         gridLayout.setVgap(10);

         gridLayout.add(directoryText, 0, 0);
         gridLayout.add(parentsDirectories, 0, 1, 2, 1);
         GridPane.setFillWidth(parentsDirectories, true);
         parentsDirectories.setMinWidth(200);
         gridLayout.add(newText, 0, 2);
         gridLayout.add(name, 0, 3, 2, 1);
         GridPane.setFillWidth(name, true);
         name.setMinWidth(200);

         newAnalysis.getDialogPane().setContent(gridLayout);
         newAnalysis.getDialogPane().getButtonTypes().addAll(ok, cancel);
         newAnalysis.getDialogPane().setPrefWidth(450d);
         newAnalysis.initOwner(JEConfig.getStage());

         /**
         *
         */

        /**
         newAnalysis.showAndWait()
         .ifPresent(response -> {
         if (response.getButtonData().getTypeCode() == JFXButtonType.OK.getButtonData().getTypeCode()) {
         List<String> check = new ArrayList<>();
         AtomicReference<JEVisObject> currentAnalysis = new AtomicReference<>();
         try {
         currentAnalysisDirectory.getValue().getChildren().forEach(jeVisObject -> {
         if (!check.contains(jeVisObject.getName())) {
         check.add(jeVisObject.getName());
         }
         });
         currentAnalysisDirectory.getValue().getChildren().forEach(jeVisObject -> {
         if (jeVisObject.getName().equals(name.getText())) currentAnalysis.set(jeVisObject);
         });
         } catch (JEVisException e) {
         logger.error("Error in current analysis directory: " + e);
         }


         if (check.contains(name.getText())) {
         Dialog<ButtonType> dialogOverwrite = new Dialog<>();
         dialogOverwrite.setTitle(I18n.getInstance().getString("plugin.graph.dialog.overwrite.title"));
         dialogOverwrite.getDialogPane().setContentText(I18n.getInstance().getString("plugin.graph.dialog.overwrite.message"));
         final JFXButtonType overwrite_ok = new JFXButtonType(I18n.getInstance().getString("plugin.graph.dialog.overwrite.ok"), JFXButtonBar.ButtonData.OK_DONE);
         final JFXButtonType overwrite_cancel = new JFXButtonType(I18n.getInstance().getString("plugin.graph.dialog.overwrite.cancel"), JFXButtonBar.ButtonData.CANCEL_CLOSE);

         dialogOverwrite.getDialogPane().getButtonTypes().addAll(overwrite_ok, overwrite_cancel);

         dialogOverwrite.showAndWait().ifPresent(overwrite_response -> {
         if (overwrite_response.getButtonData().getTypeCode() != JFXButtonType.OK.getButtonData().getTypeCode()) {
         return;
         }
         });
         }

         try {
         dashboardPojo.setName(name.getText());
         dashboardPojo.setTitle(name.getText());
         saveDashboard(dashboardPojo, widgetList, name.getText(), currentAnalysisDirectory.getValue(), wallpaper);
         } catch (Exception ex) {
         ex.printStackTrace();
         }

         }
         });
         **/
    }


}
