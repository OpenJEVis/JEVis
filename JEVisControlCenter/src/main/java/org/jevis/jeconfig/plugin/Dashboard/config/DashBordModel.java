package org.jevis.jeconfig.plugin.Dashboard.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.plugin.Dashboard.DashBordPlugIn;
import org.jevis.jeconfig.plugin.Dashboard.timeframe.TimeFrameFactory;
import org.jevis.jeconfig.plugin.Dashboard.timeframe.TimeFrames;
import org.jevis.jeconfig.plugin.Dashboard.widget.Size;
import org.jevis.jeconfig.plugin.scada.data.ConfigSheet;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * Configuration for an BashBoard Analysis
 */
public class DashBordModel {
    private static final Logger logger = LogManager.getLogger(DashBordModel.class);
    private final static String GENERAL_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.groupgeneral"), UPPER_LIMIT_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.groupupperlimitl"), LOWER_LIMIT_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.grouplowerlimit");
    public final BooleanProperty updateIsRunningProperty = new SimpleBooleanProperty(Boolean.class, "run Updater", false);
    /**
     * Update rage in seconds
     */
    public final IntegerProperty updateRate = new SimpleIntegerProperty(new Integer(0), "Update Rate", 900) {
        /** disabled the 900 min limit because its not transparent for the user in the current UI **/
//        @Override
//        public void set(int newValue) {
//            if (newValue < 900) {
//                newValue = 900;
//            }
//            super.set(newValue);
//        }
//
//        @Override
//        public void setValue(Number v) {
//            if (v.intValue() < 900) {
//                v = 900;
//            }
//            super.setValue(v);
//        }
    };
    /**
     * Interval between each x axis grid line
     **/
    public final DoubleProperty xGridInterval = new SimpleDoubleProperty(Double.class, "X Axis Grid Interval", 50.0d);
    /**
     * Interval between each y axis grid line
     */
    public final DoubleProperty yGridInterval = new SimpleDoubleProperty(Double.class, "Y Axis Grid Interval", 50.0d);
    /**
     * Background color of the bash board
     */
    public final ObjectProperty<Color> colorDashBoardBackground = new SimpleObjectProperty<>(Color.class, "Dash Board Color", Color.TRANSPARENT);
    /**
     * Background image
     */
    public final ObjectProperty<Image> imageBoardBackground = new SimpleObjectProperty<>(Image.class, "Dash Board Color", JEConfig.getImage("transPixel.png"));
    /**
     * Default color of an widget
     */
    public final ObjectProperty<Color> colorWidgetPlugin = new SimpleObjectProperty<>(Color.class, "Default Widget Background Color", Color.web("#126597"));
    /**
     * Default opacity of the widget background
     */
    public final DoubleProperty opacityWidgetPlugin = new SimpleDoubleProperty(Double.class, "Default Widget Opacity", 0.7d);
    /**
     * Default font color
     */
    public final ObjectProperty<Color> colorFont = new SimpleObjectProperty<>(Color.class, "Default Font Color", Color.BLACK);
    /**
     * Enable the possibility to edit the current analysis
     */
    public final BooleanProperty editProperty = new SimpleBooleanProperty(Boolean.class, "Enable Edit", false);
    /**
     * Enable snap to grid
     */
    public final BooleanProperty snapToGridProperty = new SimpleBooleanProperty(Boolean.class, "Snap to Grid", true);
    /**
     * Show the snap to grid lines
     */
    public final BooleanProperty showGridProperty = new SimpleBooleanProperty(Boolean.class, "Show Grid", true);
    public final ObjectProperty<Period> dataPeriodProperty = new SimpleObjectProperty(Period.class, "Data Period", Period.days(2));
    /**
     * Current displayed interval
     */
    public final ObjectProperty<Interval> displayedIntervalProperty = new SimpleObjectProperty(Interval.class, "Data Interval", new Interval(new DateTime(), new DateTime()));
    public final ObjectProperty<Size> pageSize = new SimpleObjectProperty<>(new Size(1080, 1600));
    public final ObjectProperty<Interval> intervalProperty = new SimpleObjectProperty<>();
    public final ObjectProperty<DateTime> dateTimereferrenzProperty = new SimpleObjectProperty<>(new DateTime());
    private final List<ChangeListener> changeListeners = new ArrayList<>();
    private final JEVisDataSource jeVisDataSource;
    private ObjectMapper mapper = new ObjectMapper();
    private TimeFrames timeFrames = new TimeFrames();
    public final ObjectProperty<TimeFrameFactory> timeFrameProperty = new SimpleObjectProperty<>(timeFrames.day());
    private JEVisObject analysisObject;
    private List<WidgetConfig> widgetList = new ArrayList<>();
    /**
     * Lower and upper Zoom limit
     */
    private double[] zoomLimit = new double[]{0.2, 3};
    /**
     * Zoom factor for the dashbord view.
     * 1 is no zoom
     * 0.1 is 10% of the original size
     * 1.5 is 150% of the original size
     */
    public final DoubleProperty zoomFactor = new SimpleDoubleProperty(Double.class, "Zoom Factor", 1.0d) {

        @Override
        public void set(double value) {

            if (value < zoomLimit[0]) {
                value = zoomLimit[0];
            }
            if (value > zoomLimit[1]) {
                value = zoomLimit[1];
            }
            super.set(value);
        }

        @Override
        public void setValue(Number value) {
            if (value == null) {
                // depending on requirements, throw exception, set to default value, etc.
            } else {
                if (value.doubleValue() < zoomLimit[0]) {
                    value = new Double(zoomLimit[0]);
                }
                if (value.doubleValue() > zoomLimit[1]) {
                    value = new Double(zoomLimit[1]);
                }
                super.setValue(value);
            }
        }
    };


    public DashBordModel(JEVisDataSource jeVisDataSource) {
        this.jeVisDataSource = jeVisDataSource;
    }

    public DashBordModel(JEVisObject analysisObject) throws JEVisException {
        this.analysisObject = analysisObject;
        this.jeVisDataSource = analysisObject.getDataSource();
        this.timeFrames.setDs(jeVisDataSource);
        load();
    }

    public JEVisDataSource getDataSource() {
        return jeVisDataSource;
    }

    public void addWidget(WidgetConfig config) {
        widgetList.add(config);
    }


    private void load() {
        try {
            JEVisSample lastConfigSample = analysisObject.getAttribute(DashBordPlugIn.ATTRIBUTE_DATA_MODEL_FILE).getLatestSample();
            if (lastConfigSample == null || lastConfigSample.getValueAsFile() == null || lastConfigSample.getValueAsFile().getBytes() == null) {
                logger.error("Missing Json File configuration");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(I18n.getInstance().getString("plugin.dashboard.load.error.file.header"));
                alert.setContentText(I18n.getInstance().getString("plugin.dashboard.load.error.file.content"));

                alert.showAndWait();
                return;
            }

            JEVisFile file = lastConfigSample.getValueAsFile();
            System.out.println("file: " + file);
            JsonNode jsonNode = mapper.readTree(file.getBytes());

            try {
                Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
                Size newSize = new Size(jsonNode.get("height").asDouble(primaryScreenBounds.getHeight() - 80)
                        , jsonNode.get("width").asDouble(primaryScreenBounds.getWidth() - 35));
                pageSize.setValue(newSize);
            } catch (Exception ex) {
                logger.error("Could not parse Size: {}", ex);
            }

            try {
                colorDashBoardBackground.setValue(Color.valueOf(jsonNode.get(colorDashBoardBackground.getName()).asText(colorDashBoardBackground.toString())));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", colorDashBoardBackground.getName(), ex);
            }
            try {
                xGridInterval.setValue(jsonNode.get(xGridInterval.getName()).asDouble(xGridInterval.doubleValue()));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", xGridInterval.getName(), ex);
            }
            try {
                yGridInterval.setValue(jsonNode.get(yGridInterval.getName()).asDouble(yGridInterval.doubleValue()));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", yGridInterval.getName(), ex);
            }
            try {
                zoomFactor.setValue(jsonNode.get(zoomFactor.getName()).asDouble(zoomFactor.doubleValue()));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", zoomFactor.getName(), ex);
            }
            try {
                updateRate.setValue(jsonNode.get(updateRate.getName()).asInt(updateRate.intValue()));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", updateRate.getName(), ex);
            }
            try {
                snapToGridProperty.setValue(jsonNode.get(snapToGridProperty.getName()).asBoolean(snapToGridProperty.getValue()));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", snapToGridProperty.getName(), ex);
            }
            try {
                showGridProperty.setValue(jsonNode.get(showGridProperty.getName()).asBoolean(showGridProperty.getValue()));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", showGridProperty.getName(), ex);
            }
            try {
                dataPeriodProperty.setValue(ISOPeriodFormat.standard().parsePeriod(jsonNode.get(dataPeriodProperty.getName()).asText(dataPeriodProperty.toString())));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", dataPeriodProperty.getName(), ex);
            }

            JsonNode widgets = jsonNode.get("Widget");

            if (widgets.isArray()) {
                for (final JsonNode objNode : widgets) {
                    WidgetConfig newConfig = new WidgetConfig(objNode);
                    widgetList.add(newConfig);
                }
            }


            //WidgetConfig

            try {
                JEVisAttribute bgFile = analysisObject.getAttribute(DashBordPlugIn.ATTRIBUTE_BACKGROUND);
                if (bgFile != null && bgFile.hasSample()) {
                    JEVisSample backgroundImage = bgFile.getLatestSample();
                    if (backgroundImage != null) {
                        JEVisFile imageFile = backgroundImage.getValueAsFile();
                        InputStream in = new ByteArrayInputStream(imageFile.getBytes());
//                    BufferedImage image = ImageIO.read(in);
                        imageBoardBackground.setValue(new Image(in));
                        logger.info("Done loading image");
                    }

                } else {
                    logger.info("No image set");
                }


            } catch (Exception ex) {
                logger.error("Could load background image: {}", ex);
            }


        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
    }


    public List<WidgetConfig> getWidgets() {
        return widgetList;
    }

    public JEVisObject getAnalysisObject() {
        return analysisObject;
    }


    public boolean isNew() {
        return true;
    }

    public void save() {
        save(analysisObject);
    }

    public void save(JEVisObject analysisObject) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put(colorDashBoardBackground.getName(), colorDashBoardBackground.getValue().toString());
        jsonNode.put(xGridInterval.getName(), xGridInterval.getValue().toString());
        jsonNode.put(yGridInterval.getName(), yGridInterval.getValue().toString());
        jsonNode.put(snapToGridProperty.getName(), snapToGridProperty.getValue().toString());
        jsonNode.put(showGridProperty.getName(), showGridProperty.getValue().toString());
        jsonNode.put(dataPeriodProperty.getName(), dataPeriodProperty.getValue().toString());
        jsonNode.put(zoomFactor.getName(), zoomFactor.getValue().toString());
        jsonNode.put(updateRate.getName(), updateRate.getValue().toString());
        jsonNode.put("height", pageSize.getValue().getHeight());
        jsonNode.put("width", pageSize.getValue().getWidth());

        ArrayNode widgetJson = jsonNode.putArray("Widget");
        widgetList.forEach(widgetConfig -> {
            widgetJson.add(widgetConfig.toJsonNode());
        });


        try {
            DateTime now = new DateTime().withMillis(0).withSecondOfMinute(0);
            String userName = analysisObject.getDataSource().getCurrentUser().getAccountName();
            JEVisSample dataModelSample = analysisObject.getAttribute("Data Model")
                    .buildSample(now, jsonNode.toString(), userName);
            dataModelSample.commit();


            /** TODO: check if the image changed **/
            if (imageBoardBackground.getValue() != null) {
                BufferedImage bImage = SwingFXUtils.fromFXImage(this.imageBoardBackground.getValue(), null);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                javax.imageio.ImageIO.write(bImage, "png", bos);


                byte[] data = bos.toByteArray();
                JEVisFile jfile = new JEVisFileImp();
                jfile.setBytes(data);
                jfile.setFilename("bg.png");


                JEVisSample backgroundImage = analysisObject.getAttribute("Background")
                        .buildSample(now, jfile, userName);
                backgroundImage.commit();
            }
            this.analysisObject = analysisObject;
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    public void zoomIn() {
        zoomFactor.setValue(zoomFactor.getValue() + 0.1d);
    }

    public void zoomOut() {
        zoomFactor.setValue(zoomFactor.getValue() - 0.1d);
    }

    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }


    /**
     * Opens UI configuration
     * TODO replace translation
     *
     * @return true if the configurations changed
     */
    public boolean openConfig() {
        Map<String, ConfigSheet.Property> userConfig = new LinkedHashMap<>();
        userConfig.put(xGridInterval.getName(), new ConfigSheet.Property("X-Grid Interval", GENERAL_GROUP, xGridInterval.getValue(), "Help"));
        userConfig.put(yGridInterval.getName(), new ConfigSheet.Property("Y-Grid Interval", GENERAL_GROUP, yGridInterval.getValue(), "Help"));
        userConfig.put(snapToGridProperty.getName(), new ConfigSheet.Property("Snap to Grid", GENERAL_GROUP, snapToGridProperty.getValue(), "Help"));
        userConfig.put(showGridProperty.getName(), new ConfigSheet.Property("Show Grid", GENERAL_GROUP, showGridProperty.getValue(), "Help"));
        userConfig.put(updateRate.getName(), new ConfigSheet.Property("Update Rate (sec)", GENERAL_GROUP, updateRate.getValue(), "Help"));
        userConfig.put("Height", new ConfigSheet.Property("Height", GENERAL_GROUP, pageSize.getValue().getHeight(), "Help"));
        userConfig.put("Width", new ConfigSheet.Property("Width", GENERAL_GROUP, pageSize.getValue().getWidth(), "Help"));


        Dialog configDia = new Dialog();
        configDia.setTitle(I18n.getInstance().getString("plugin.scada.element.config.title"));
        configDia.setHeaderText(I18n.getInstance().getString("plugin.scada.element.config.header"));


        ConfigSheet ct = new ConfigSheet();
        configDia.getDialogPane().setContent(ct.getSheet(userConfig));
        configDia.resizableProperty().setValue(true);
        configDia.setHeight(800);
        configDia.setWidth(500);

        configDia.getDialogPane().setMinWidth(500);
        configDia.getDialogPane().setMinHeight(500);
        configDia.setGraphic(ResourceLoader.getImage("1394482166_blueprint_tool.png", 50, 50));

        ButtonType buttonTypeOk = new ButtonType(I18n.getInstance().getString("plugin.scada.element.config.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType(I18n.getInstance().getString("plugin.scada.element.config.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        configDia.setOnShowing(event -> {

        });

        configDia.getDialogPane().getButtonTypes().addAll(buttonTypeCancel, buttonTypeOk);

        Optional<ButtonType> opt = configDia.showAndWait();
        if (opt.get().equals(buttonTypeOk)) {

            showGridProperty.setValue((boolean) userConfig.get(showGridProperty.getName()).getObject());
            snapToGridProperty.setValue((boolean) userConfig.get(snapToGridProperty.getName()).getObject());
            xGridInterval.setValue((Double) userConfig.get(xGridInterval.getName()).getObject());
            yGridInterval.setValue((Double) userConfig.get(yGridInterval.getName()).getObject());
            updateRate.setValue((Integer) userConfig.get(updateRate.getName()).getObject());
            changeListeners.forEach(listener -> {
                listener.stateChanged(new ChangeEvent(this));
            });

            return true;
        }

        return false;

    }


}
