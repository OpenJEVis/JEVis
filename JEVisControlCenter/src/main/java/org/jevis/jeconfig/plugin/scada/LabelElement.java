package org.jevis.jeconfig.plugin.scada;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.application.jevistree.UserSelection;
import org.jevis.application.resource.ResourceLoader;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.scada.data.ConfigSheet;
import org.jevis.jeconfig.plugin.scada.data.ScadaElementData;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class LabelElement extends MoveableNode implements SCADAElement {

    private static final Logger logger = LogManager.getLogger(LabelElement.class);
    private final static String XPOS = "xPos", YPOS = "yPos", SHOW_UNIT = "showUnit", SHOW_LABEL = "showLabel", LABEL_TEXT = "labelText", FONT_COLOR = "fontColor", FONT_SIZE = "fontSize", BG_COLOR = "bgColor", BG_OPANCITY = "bgOpacity";
    private final static String LOW_LIMIT_ENABLED = "lowLimitEnabled", UP_LIMIT_ENABLED = "upLimitEnabled", UPPER_LIMIT = "upLimit", LOW_LIMIT = "lowLimit", FONT_COLOR_UP_LIMIT = "fontColorUpLimit", BG_COLOR_UP_LIMIT = "bgColorUpLimit", FONT_COLOR_LOW_LIMIT = "fontColorLowLimit", BG_COLOR_LOW_LIMIT = "bgColorLowLimit";
    private final static String GENERAL_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.groupgeneral"), UPPER_LIMIT_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.groupupperlimitl"), LOWER_LIMIT_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.grouplowerlimit");


    public static String TYPE = "LabelElement";
    private static Map<String, ConfigSheet.Property> userConfig = new LinkedHashMap<>();


    private StringProperty titleProperty = new SimpleStringProperty();
    private DoubleProperty xPositionProperty = new SimpleDoubleProperty();
    private DoubleProperty yPositionProperty = new SimpleDoubleProperty();
    private JEVisAttribute attribute;
    private HBox view = new HBox(8);
    private Label label = new Label();
    private Label text = new Label();
    private ImageView warningImage = JEConfig.getImage("Warning-icon.png", 15, 15);
    private ScadaElementData data = new ScadaElementData();
    private SCADAAnalysis analysis;


    public LabelElement(SCADAAnalysis analysis) {
        super();
        super.setContent(view);
        super.setDCADAElement(this);
        this.analysis = analysis;

        /**
         * Default config
         * TODO: localize
         */
        userConfig.put(SHOW_LABEL, new ConfigSheet.Property(I18n.getInstance().getString("plugin.scada.element.setting.label.showlabel"), GENERAL_GROUP, true, "Help"));
        userConfig.put(LABEL_TEXT, new ConfigSheet.Property(I18n.getInstance().getString("plugin.scada.element.setting.label.labeltext"), GENERAL_GROUP, "", "label text, object+attribute name wen leer"));
        userConfig.put(BG_OPANCITY, new ConfigSheet.Property(I18n.getInstance().getString("plugin.scada.element.setting.label.opacity"), GENERAL_GROUP, 0.5d, "Help"));
        userConfig.put(SHOW_UNIT, new ConfigSheet.Property(I18n.getInstance().getString("plugin.scada.element.setting.label.showunit"), GENERAL_GROUP, true, "Help"));


        userConfig.put(XPOS, new ConfigSheet.Property(I18n.getInstance().getString("plugin.scada.element.setting.label.xpos"), GENERAL_GROUP, 100, "Help"));
        userConfig.put(YPOS, new ConfigSheet.Property(I18n.getInstance().getString("plugin.scada.element.setting.label.ypos"), GENERAL_GROUP, 100, "Help"));


        userConfig.put(FONT_COLOR, new ConfigSheet.Property(I18n.getInstance().getString("plugin.scada.element.setting.label.fontcolor"), GENERAL_GROUP, Color.WHITE, "Help"));
        userConfig.put(BG_COLOR, new ConfigSheet.Property(I18n.getInstance().getString("plugin.scada.element.setting.label.backgroundcolor"), GENERAL_GROUP, Color.BLACK, "Help"));
        userConfig.put(FONT_SIZE, new ConfigSheet.Property(I18n.getInstance().getString("plugin.scada.element.setting.label.fontsize"), GENERAL_GROUP, 16.0, "Help"));

        userConfig.put(UP_LIMIT_ENABLED, new ConfigSheet.Property(I18n.getInstance().getString("plugin.scada.element.setting.label.upperlimit.enable"), UPPER_LIMIT_GROUP, false, "Help"));
        userConfig.put(LOW_LIMIT_ENABLED, new ConfigSheet.Property(I18n.getInstance().getString("plugin.scada.element.setting.label.lowerlimit.enable"), LOWER_LIMIT_GROUP, false, "Help"));

        userConfig.put(UPPER_LIMIT, new ConfigSheet.Property(I18n.getInstance().getString("plugin.scada.element.setting.label.upperlimit.limitvalue"), UPPER_LIMIT_GROUP, 1000.99, "Help"));
        userConfig.put(LOW_LIMIT, new ConfigSheet.Property(I18n.getInstance().getString("plugin.scada.element.setting.label.lowerlimit.limitvalue"), LOWER_LIMIT_GROUP, 1000.99, "Help"));

        userConfig.put(FONT_COLOR_UP_LIMIT, new ConfigSheet.Property(I18n.getInstance().getString("plugin.scada.element.setting.label.upperlimit.fontcolor"), UPPER_LIMIT_GROUP, Color.RED, "Help"));
        userConfig.put(FONT_COLOR_LOW_LIMIT, new ConfigSheet.Property(I18n.getInstance().getString("plugin.scada.element.setting.label.lowerlimit.fontcolor"), LOWER_LIMIT_GROUP, Color.RED, "Help"));

        userConfig.put(BG_COLOR_UP_LIMIT, new ConfigSheet.Property(I18n.getInstance().getString("plugin.scada.element.setting.label.upperlimit.backgroundcolor"), UPPER_LIMIT_GROUP, Color.BLACK, "Help"));
        userConfig.put(BG_COLOR_LOW_LIMIT, new ConfigSheet.Property(I18n.getInstance().getString("plugin.scada.element.setting.label.lowerlimit.backgroundcolor"), LOWER_LIMIT_GROUP, Color.BLACK, "Help"));


    }

    @Override
    public ScadaElementData getData() {
        data.setType(TYPE);
        data.setxPos(xPositionProperty.getValue());
        data.setyPos(yPositionProperty.getValue());
        data.setObjectID(attribute.getObjectID());
        data.setAttribute(attribute.getName());

        userConfig.forEach((key, config) -> {
            try {
                if (config.getObject() instanceof Color) {
                    Color color = (Color) config.getObject();
                    String htmlColor = String.format("#%02X%02X%02X",
                            (int) (color.getRed() * 255),
                            (int) (color.getGreen() * 255),
                            (int) (color.getBlue() * 255));

                    data.getOptions().put(key, htmlColor);
                } else {
                    data.getOptions().put(key, config.getObject().toString());
                }
            } catch (Exception ex) {
                logger.info("Error while settibg key: " + key);
            }
        });

        return data;

    }

    @Override
    public void setData(ScadaElementData data) {
        try {
            this.data = data;

            JEVisDataSource ds = analysis.getObject().getDataSource();
            JEVisObject targetObj = ds.getObject(data.getObjectID());
            JEVisAttribute targetAtt = targetObj.getAttribute(data.getAttribute());
            attribute = targetAtt;

            titleProperty.set(attribute.getName());
            xPositionProperty.setValue(data.getxPos());
            yPositionProperty.setValue(data.getyPos());

            userConfig.get(XPOS).setObject(xPositionProperty.getValue());
            userConfig.get(YPOS).setObject(yPositionProperty.getValue());


            data.getOptions().forEach((key, value) -> {
                try {
                    switch (key) {
                        case SHOW_LABEL:
                            userConfig.get(key).setObject(Boolean.parseBoolean(value));
                            break;
                        case LABEL_TEXT:
                            userConfig.get(key).setObject(value);
                            break;
                        case SHOW_UNIT:
                            userConfig.get(key).setObject(Boolean.parseBoolean(value));
                            break;
                        case FONT_COLOR:
                            userConfig.get(key).setObject(Color.web(value));
                            break;
                        case BG_COLOR:
                            userConfig.get(key).setObject(Color.web(value));
                            break;
                        case FONT_SIZE:
                            userConfig.get(key).setObject(Double.parseDouble(value));
                            break;
                        case BG_OPANCITY:
                            userConfig.get(key).setObject(Double.parseDouble(value));
                            break;
                        case UP_LIMIT_ENABLED:
                            userConfig.get(key).setObject(Boolean.parseBoolean(value));
                            break;
                        case LOW_LIMIT_ENABLED:
                            userConfig.get(key).setObject(Boolean.parseBoolean(value));
                            break;
                        case UPPER_LIMIT:
                            userConfig.get(key).setObject(Double.parseDouble(value));
                            break;
                        case LOW_LIMIT:
                            userConfig.get(key).setObject(Double.parseDouble(value));
                            break;
                        case FONT_COLOR_UP_LIMIT:
                            userConfig.get(key).setObject(Color.web(value));
                            break;
                        case FONT_COLOR_LOW_LIMIT:
                            userConfig.get(key).setObject(Color.web(value));
                            break;
                        case BG_COLOR_UP_LIMIT:
                            userConfig.get(key).setObject(Color.web(value));
                            break;
                        case BG_COLOR_LOW_LIMIT:
                            userConfig.get(key).setObject(Color.web(value));
                            break;
                    }
                } catch (Exception ex) {
                    logger.info("Error while setting Key: " + key + " value: '" + value + "'");
                }

            });


        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }

    @Override
    public StringProperty titleProperty() {
        return titleProperty;
    }

    @Override
    public void setAttribute(JEVisAttribute att) {
        attribute = att;
    }

    @Override
    public DoubleProperty xPositionProperty() {
        return xPositionProperty;
    }

    @Override
    public DoubleProperty yPositionProperty() {
        return yPositionProperty;
    }

    @Override
    public Node getGraphic() {
        logger.info("Label.getGraphic()");
        warningImage.setPreserveRatio(true);
        view.setMinWidth(20);
//        view.getChildren().clear();
//        view.getChildren().add(label);
//        view.getChildren().add(text);
//        view.getChildren().add(warningImage);
//        warningImage.setPreserveRatio(true);
//        warningImage.setVisible(false);

//        view.setBorder(new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
//        view.setStyle("-fx-background-color: " + options.get(BG_COLOR) + ";");//#00ff00;");


        return this;
    }

    @Override
    public void update() {
        view.getChildren().clear();
        view.getChildren().add(text);


        String value = "";
        JEVisSample lastSample = null;
//        ((Pane) getParent()).setMinSize(5, 5);

        Color textColor = (Color) userConfig.get(FONT_COLOR).getObject();
        double opacity = (double) userConfig.get(BG_OPANCITY).getObject();
        Color bgColor = (Color) userConfig.get(BG_COLOR).getObject();
        Color textColorUpLimit = (Color) userConfig.get(FONT_COLOR_UP_LIMIT).getObject();
        Color textColorLowLimit = (Color) userConfig.get(FONT_COLOR_LOW_LIMIT).getObject();
        Color bgColorUpLimit = (Color) userConfig.get(BG_COLOR_UP_LIMIT).getObject();
        Color bgColorLowLimit = (Color) userConfig.get(BG_COLOR_LOW_LIMIT).getObject();
        boolean showUnit = (boolean) userConfig.get(SHOW_UNIT).getObject();
        boolean upperLimitON = (boolean) userConfig.get(UP_LIMIT_ENABLED).getObject();
        boolean lowLimitON = (boolean) userConfig.get(LOW_LIMIT_ENABLED).getObject();
        double lowLimit = (double) userConfig.get(LOW_LIMIT).getObject();
        double upperLimit = (double) userConfig.get(UPPER_LIMIT).getObject();
        boolean labelVisible = (boolean) userConfig.get(SHOW_LABEL).getObject();
        String labelText = (String) userConfig.get(LABEL_TEXT).getObject();

        bgColor = bgColor.deriveColor(0, 0, 0, opacity);
        bgColorUpLimit = bgColorUpLimit.deriveColor(0, 0, 0, opacity);
        bgColorLowLimit = bgColorLowLimit.deriveColor(0, 0, 0, opacity);

        logger.info("Relocate: " + xPositionProperty.getValue() + " " + yPositionProperty.getValue());
        relocateRelativ(xPositionProperty.getValue(), yPositionProperty.getValue());


        Font font = new Font((double) userConfig.get(FONT_SIZE).getObject());
        text.setFont(font);
        label.setFont(font);

        if (labelVisible) {
            view.getChildren().add(label);
        }
        if (labelVisible && labelText.isEmpty()) {
            label.setText(attribute.getObject().getName() + "." + attribute.getName() + ":");
        } else {
            label.setText(labelText + ":");
        }


        if (attribute != null) {
            lastSample = attribute.getLatestSample();
            if (lastSample != null) {
                try {
                    value = lastSample.getValueAsString();
                } catch (NullPointerException | JEVisException e) {
                    e.printStackTrace();
                }
            }
        }

        if (lastSample != null) {
            try {
                if (attribute.getPrimitiveType() == JEVisConstants.PrimitiveType.DOUBLE
                        || attribute.getPrimitiveType() == JEVisConstants.PrimitiveType.LONG) {
                    double doubleValue = lastSample.getValueAsDouble(attribute.getDisplayUnit());
                    value = doubleValue + "";

                    if (upperLimitON) {
                        if (doubleValue >= lowLimit) {
                            textColor = textColorUpLimit;
                            bgColor = bgColorUpLimit;
                        }
                    }

                    if (lowLimitON) {
                        if (doubleValue <= upperLimit) {
                            textColor = textColorLowLimit;
                            bgColor = bgColorLowLimit;
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            /**
             * Show an warning icon if the the attribute has an period and the lastSample is older then that
             */


            try {
                logger.info("imput rate: " + attribute.getInputSampleRate());
                if (attribute.getInputSampleRate() != null &&
                        attribute.getInputSampleRate().getMillis() > 1) {
                    DateTime now = new DateTime();
                    if (lastSample.getTimestamp().isBefore(now.minus(attribute.getInputSampleRate()))) {
                        view.getChildren().add(warningImage);
                        warningImage.minHeight(view.getHeight());
                        warningImage.setFitHeight(view.getHeight());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            logger.info("Show Unit: " + showUnit + " " + attribute.getDisplayUnit());
            if (showUnit && attribute.getDisplayUnit() != null) {
                value += attribute.getDisplayUnit().getPrefix() + UnitManager.getInstance().formate(attribute.getDisplayUnit());
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }


        text.setTextFill(textColor);
        label.setTextFill(textColor);
        Background bg = new Background(new BackgroundFill(bgColor, new CornerRadii(0), Insets.EMPTY));
        view.setBackground(bg);

        this.text.setText(value);


    }

    @Override
    public UserSelection getUserSeclection() {
        UserSelection us = new UserSelection(UserSelection.SelectionType.Attribute, attribute, null, null);
        return us;
    }

    @Override
    public String toString() {
        String ts = String.format("[%s] Object: %s, Att: '%s', x: %s, y %s  "
                , this.getClass().getSimpleName()
                , attribute.getObjectID()
                , attribute.getName()
                , xPositionProperty.getValue()
                , yPositionProperty.getValue());
        return ts;
    }


    @Override
    public void openConfig() {
        Dialog configDia = new Dialog();
        configDia.setTitle(I18n.getInstance().getString("plugin.scada.element.config.title"));
        configDia.setHeaderText(I18n.getInstance().getString("plugin.scada.element.config.header"));


        ConfigSheet ct = new ConfigSheet();
        configDia.getDialogPane().setContent(ct.getSheet(userConfig));
        configDia.resizableProperty().setValue(true);
        configDia.setHeight(500);
        configDia.setWidth(500);
        configDia.getDialogPane().setMinWidth(500);
        configDia.setGraphic(ResourceLoader.getImage("1394482166_blueprint_tool.png", 50, 50));

        ButtonType buttonTypeOk = new ButtonType(I18n.getInstance().getString("plugin.scada.element.config.ok"), ButtonBar.ButtonData.OK_DONE);

        configDia.getDialogPane().getButtonTypes().add(buttonTypeOk);

        Optional<ButtonType> opt = configDia.showAndWait();

        xPositionProperty.setValue((double) userConfig.get(XPOS).getObject());
        yPositionProperty.setValue((double) userConfig.get(YPOS).getObject());

        update();


    }


}
