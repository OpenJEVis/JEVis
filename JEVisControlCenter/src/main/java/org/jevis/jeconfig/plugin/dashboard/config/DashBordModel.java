package org.jevis.jeconfig.plugin.dashboard.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.*;
import javafx.concurrent.Task;
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
import org.jevis.jeconfig.plugin.dashboard.DashBordPlugIn;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrameFactory;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrames;
import org.jevis.jeconfig.plugin.dashboard.widget.Size;
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
    public final BooleanProperty snapToGridProperty = new SimpleBooleanProperty(Boolean.class, "trze", true);
    /**
     * Show the snap to grid lines
     */
    public final BooleanProperty showGridProperty = new SimpleBooleanProperty(Boolean.class, "Show Grid", true);
    public final ObjectProperty<Period> dataPeriodProperty = new SimpleObjectProperty(Period.class, "Data Period", Period.days(2));
    /**
     * Current displayed interval
     */
    public final ObjectProperty<Interval> displayedIntervalProperty = new SimpleObjectProperty(Interval.class, "Data Interval", new Interval(new DateTime(), new DateTime()));
    /**
     * disable the intervalSelection
     */
    public final BooleanProperty disableIntervalUI = new SimpleBooleanProperty(Boolean.class, "Disable Interval UI", false);

    public final StringProperty defaultPeriod = new SimpleStringProperty(String.class, "Default Period", Period.days(1).toString());

    public final ObjectProperty<Size> pageSize = new SimpleObjectProperty<>(new Size(1080, 1600));
    public final ObjectProperty<Interval> intervalProperty = new SimpleObjectProperty<>();
    public final ObjectProperty<DateTime> dateTimereferrenzProperty = new SimpleObjectProperty<>(new DateTime());
    private final List<ChangeListener> changeListeners = new ArrayList<>();
    private final JEVisDataSource jeVisDataSource;
    private ObjectMapper mapper = new ObjectMapper();
    private final TimeFrames timeFrames;
    public final ObjectProperty<TimeFrameFactory> timeFrameProperty;
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

            if (value < DashBordModel.this.zoomLimit[0]) {
                value = DashBordModel.this.zoomLimit[0];
            }
            if (value > DashBordModel.this.zoomLimit[1]) {
                value = DashBordModel.this.zoomLimit[1];
            }
            super.set(value);
        }

        @Override
        public void setValue(Number value) {
            if (value == null) {
                // depending on requirements, throw exception, set to default value, etc.
            } else {
                if (value.doubleValue() < DashBordModel.this.zoomLimit[0]) {
                    value = new Double(DashBordModel.this.zoomLimit[0]);
                }
                if (value.doubleValue() > DashBordModel.this.zoomLimit[1]) {
                    value = new Double(DashBordModel.this.zoomLimit[1]);
                }
                super.setValue(value);
            }
        }
    };


    public DashBordModel(JEVisDataSource jeVisDataSource) {
        this.jeVisDataSource = jeVisDataSource;
        this.timeFrames = new TimeFrames(jeVisDataSource);
        this.timeFrameProperty = new SimpleObjectProperty<>(this.timeFrames.day());
    }

    public DashBordModel(JEVisObject analysisObject) throws JEVisException {
        this.analysisObject = analysisObject;
        this.jeVisDataSource = analysisObject.getDataSource();
        this.timeFrames = new TimeFrames(this.jeVisDataSource);
        this.timeFrameProperty = new SimpleObjectProperty<>(this.timeFrames.day());


        load();
    }

    public JEVisDataSource getDataSource() {
        return this.jeVisDataSource;
    }


    private void load() {
        try {
            JEVisSample lastConfigSample = this.analysisObject.getAttribute(DashBordPlugIn.ATTRIBUTE_DATA_MODEL_FILE).getLatestSample();
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
            JsonNode jsonNode = this.mapper.readTree(file.getBytes());

//            this.timeFrame.addListener((observable, oldValue, newValue) -> {
//                System.out.println("DashBoardModel.timframe.changed: " + oldValue.getID() + " to " + newValue.getID());
//                intervalProperty.setValue(newValue.getInterval(new DateTime()));
//            });

            try {
                String defaultPeriodStrg = jsonNode.get("defaultPeriod").asText(Period.days(1).toString());
//                System.out.println("Default period: " + defaultPeriodStrg);

                for (TimeFrameFactory timeFrameFactory : this.timeFrames.getAll()) {
                    if (timeFrameFactory.getID().equals(defaultPeriodStrg)) {
                        this.timeFrameProperty.setValue(timeFrameFactory);
                    }
                }


            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", this.defaultPeriod.getName(), ex);
            }


            try {
                Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
                Size newSize = new Size(jsonNode.get("height").asDouble(primaryScreenBounds.getHeight() - 80)
                        , jsonNode.get("width").asDouble(primaryScreenBounds.getWidth() - 35));
                this.pageSize.setValue(newSize);
            } catch (Exception ex) {
                logger.error("Could not parse Size: {}", ex);
            }

            try {
                this.colorDashBoardBackground.setValue(Color.valueOf(jsonNode.get(this.colorDashBoardBackground.getName()).asText(this.colorDashBoardBackground.toString())));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", this.colorDashBoardBackground.getName(), ex);
            }
            try {
                this.xGridInterval.setValue(jsonNode.get(this.xGridInterval.getName()).asDouble(this.xGridInterval.doubleValue()));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", this.xGridInterval.getName(), ex);
            }
            try {
                this.yGridInterval.setValue(jsonNode.get(this.yGridInterval.getName()).asDouble(this.yGridInterval.doubleValue()));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", this.yGridInterval.getName(), ex);
            }
            try {
                this.zoomFactor.setValue(jsonNode.get(this.zoomFactor.getName()).asDouble(this.zoomFactor.doubleValue()));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", this.zoomFactor.getName(), ex);
            }
            try {
                this.updateRate.setValue(jsonNode.get(this.updateRate.getName()).asInt(this.updateRate.intValue()));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", this.updateRate.getName(), ex);
            }
            try {
                this.snapToGridProperty.setValue(jsonNode.get(this.snapToGridProperty.getName()).asBoolean(this.snapToGridProperty.getValue()));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", this.snapToGridProperty.getName(), ex);
            }
            try {
                this.showGridProperty.setValue(jsonNode.get(this.showGridProperty.getName()).asBoolean(this.showGridProperty.getValue()));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", this.showGridProperty.getName(), ex);
            }
            try {
                this.dataPeriodProperty.setValue(ISOPeriodFormat.standard().parsePeriod(jsonNode.get(this.dataPeriodProperty.getName()).asText(this.dataPeriodProperty.toString())));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", this.dataPeriodProperty.getName(), ex);
            }
            try {
                this.disableIntervalUI.setValue(jsonNode.get("disableIntervalUI").asBoolean(this.snapToGridProperty.getValue()));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", this.disableIntervalUI.getName(), ex.getMessage());
            }


            JsonNode widgets = jsonNode.get("Widget");

            if (widgets.isArray()) {
                for (final JsonNode objNode : widgets) {
                    WidgetConfig newConfig = new WidgetConfig(objNode);
                    this.widgetList.add(newConfig);
                }
            }


            //WidgetConfig


            Task<Image> imageLoadTask = new Task<Image>() {
                @Override
                public Image call() throws InterruptedException {
                    try {
                        JEVisAttribute bgFile = DashBordModel.this.analysisObject.getAttribute(DashBordPlugIn.ATTRIBUTE_BACKGROUND);
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

            imageLoadTask.setOnSucceeded(e -> this.imageBoardBackground.setValue(imageLoadTask.getValue()));
            new Thread(imageLoadTask).start();


        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
    }


    public List<WidgetConfig> getWidgets() {
        return this.widgetList;
    }

    public JEVisObject getAnalysisObject() {
        return this.analysisObject;
    }


    public boolean isNew() {
        return true;
    }

    public void save() {
        save(this.analysisObject);
    }

    public void save(JEVisObject analysisObject) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put(this.colorDashBoardBackground.getName(), this.colorDashBoardBackground.getValue().toString());
        jsonNode.put(this.xGridInterval.getName(), this.xGridInterval.getValue().toString());
        jsonNode.put(this.yGridInterval.getName(), this.yGridInterval.getValue().toString());
        jsonNode.put(this.snapToGridProperty.getName(), this.snapToGridProperty.getValue().toString());
        jsonNode.put(this.showGridProperty.getName(), this.showGridProperty.getValue().toString());
        jsonNode.put(this.dataPeriodProperty.getName(), this.dataPeriodProperty.getValue().toString());
        jsonNode.put(this.zoomFactor.getName(), this.zoomFactor.getValue().toString());
        jsonNode.put(this.updateRate.getName(), this.updateRate.getValue().toString());
        jsonNode.put("height", this.pageSize.getValue().getHeight());
        jsonNode.put("width", this.pageSize.getValue().getWidth());

        ArrayNode widgetJson = jsonNode.putArray("Widget");
        this.widgetList.forEach(widgetConfig -> {
            widgetJson.add(widgetConfig.toJsonNode());
        });


        try {
            DateTime now = new DateTime().withMillis(0).withSecondOfMinute(0);
            String userName = analysisObject.getDataSource().getCurrentUser().getAccountName();
            JEVisSample dataModelSample = analysisObject.getAttribute("Data Model")
                    .buildSample(now, jsonNode.toString(), userName);
            dataModelSample.commit();


            /** TODO: check if the image changed **/
            if (this.imageBoardBackground.getValue() != null) {
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
        this.zoomFactor.setValue(this.zoomFactor.getValue() + 0.1d);
    }

    public void zoomOut() {
        this.zoomFactor.setValue(this.zoomFactor.getValue() - 0.1d);
    }

    public void addChangeListener(ChangeListener listener) {
        this.changeListeners.add(listener);
    }


    /**
     * Opens UI configuration
     * TODO replace translation
     *
     * @return true if the configurations changed
     */
    public boolean openConfig() {
        Map<String, ConfigSheet.Property> userConfig = new LinkedHashMap<>();
        userConfig.put(this.xGridInterval.getName(), new ConfigSheet.Property("X-Grid Interval", GENERAL_GROUP, this.xGridInterval.getValue(), "Help"));
        userConfig.put(this.yGridInterval.getName(), new ConfigSheet.Property("Y-Grid Interval", GENERAL_GROUP, this.yGridInterval.getValue(), "Help"));
        userConfig.put(this.snapToGridProperty.getName(), new ConfigSheet.Property("Snap to Grid", GENERAL_GROUP, this.snapToGridProperty.getValue(), "Help"));
        userConfig.put(this.showGridProperty.getName(), new ConfigSheet.Property("Show Grid", GENERAL_GROUP, this.showGridProperty.getValue(), "Help"));
        userConfig.put(this.updateRate.getName(), new ConfigSheet.Property("Update Rate (sec)", GENERAL_GROUP, this.updateRate.getValue(), "Help"));
        userConfig.put("Height", new ConfigSheet.Property("Height", GENERAL_GROUP, this.pageSize.getValue().getHeight(), "Help"));
        userConfig.put("Width", new ConfigSheet.Property("Width", GENERAL_GROUP, this.pageSize.getValue().getWidth(), "Help"));


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

            this.showGridProperty.setValue((boolean) userConfig.get(this.showGridProperty.getName()).getObject());
            this.snapToGridProperty.setValue((boolean) userConfig.get(this.snapToGridProperty.getName()).getObject());
            this.xGridInterval.setValue((Double) userConfig.get(this.xGridInterval.getName()).getObject());
            this.yGridInterval.setValue((Double) userConfig.get(this.yGridInterval.getName()).getObject());
            this.updateRate.setValue((Integer) userConfig.get(this.updateRate.getName()).getObject());
            this.changeListeners.forEach(listener -> {
                listener.stateChanged(new ChangeEvent(this));
            });

            return true;
        }

        return false;

    }


}
