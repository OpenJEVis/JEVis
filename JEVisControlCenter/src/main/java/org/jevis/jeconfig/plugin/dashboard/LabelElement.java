package org.jevis.jeconfig.plugin.dashboard;

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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.jevis.api.*;
import org.jevis.application.jevistree.UserSelection;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.plugin.dashboard.data.ConfigSheet;
import org.jevis.jeconfig.plugin.dashboard.data.ScadaElementData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class LabelElement extends MoveableNode implements SCADAElement {

    private final static String SHOW_UNIT = "showUnit", SHOW_LABEL = "showLabel", LABEL_TEXT = "labelText", FONT_COLOR = "fontColor", FONT_SIZE = "fontSize", BG_COLOR = "bgColor", BG_OPANCITY = "bgOpacity";
    private final static String LOW_LIMIT_ENABLED = "lowLimitEnabled", UP_LIMIT_ENABLED = "upLimitEnabled", UPPER_LIMIT = "upLimit", LOW_LIMIT = "lowLimit", FONT_COLOR_UP_LIMIT = "fontColorUpLimit", BG_COLOR_UP_LIMIT = "bgColorUpLimit", FONT_COLOR_LOW_LIMIT = "fontColorLowLimit", BG_COLOR_LOW_LIMIT = "bgColorLowLimit";
    private final static String GENERAL_GROUP = "General", UPPER_LIMIT_GROUP = "Upper Limit", LOWER_LIMIT_GROUP = "Lower Limit";


    public static String TYPE = "LabelElement";
    private static Map<String, ConfigSheet.Property> userConfig = new LinkedHashMap<>();


    private StringProperty titleProperty = new SimpleStringProperty();
    private DoubleProperty xPositionProperty = new SimpleDoubleProperty();
    private DoubleProperty yPositionProperty = new SimpleDoubleProperty();
    private JEVisAttribute attribute;
    private HBox view = new HBox(8);
    private Label label = new Label();
    private Label text = new Label();
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
        userConfig.put(SHOW_LABEL, new ConfigSheet.Property("Zeige Label", GENERAL_GROUP, true, "Help"));
        userConfig.put(LABEL_TEXT, new ConfigSheet.Property("Zeige Label", GENERAL_GROUP, "", "label text, object+attribute name wen leer"));
        userConfig.put(BG_OPANCITY, new ConfigSheet.Property("Transparents", GENERAL_GROUP, 0.5d, "Help"));

        userConfig.put(FONT_COLOR, new ConfigSheet.Property("Schriftfarbe", GENERAL_GROUP, Color.WHITE, "Help"));
        userConfig.put(BG_COLOR, new ConfigSheet.Property("Hintergrundfarbe", GENERAL_GROUP, Color.BLACK, "Help"));
        userConfig.put(FONT_SIZE, new ConfigSheet.Property("Schriftgroeße", GENERAL_GROUP, 16.0, "Help"));
        userConfig.put(BG_OPANCITY, new ConfigSheet.Property("Transparents", GENERAL_GROUP, 0.5d, "Help"));

        userConfig.put(UP_LIMIT_ENABLED, new ConfigSheet.Property("Aktiviere", UPPER_LIMIT_GROUP, false, "Help"));
        userConfig.put(LOW_LIMIT_ENABLED, new ConfigSheet.Property("Aktiviere", LOWER_LIMIT_GROUP, false, "Help"));

        userConfig.put(UPPER_LIMIT, new ConfigSheet.Property("Grensswert", UPPER_LIMIT_GROUP, 1000.99, "Help"));
        userConfig.put(LOW_LIMIT, new ConfigSheet.Property("Grensswert", LOWER_LIMIT_GROUP, 1000.99, "Help"));

        userConfig.put(FONT_COLOR_UP_LIMIT, new ConfigSheet.Property("Schriftfarbe", UPPER_LIMIT_GROUP, Color.RED, "Help"));
        userConfig.put(FONT_COLOR_LOW_LIMIT, new ConfigSheet.Property("Schriftfarbe", LOWER_LIMIT_GROUP, Color.RED, "Help"));

        userConfig.put(BG_COLOR_UP_LIMIT, new ConfigSheet.Property("Hintergrundfarbe", UPPER_LIMIT_GROUP, Color.BLACK, "Help"));
        userConfig.put(BG_COLOR_LOW_LIMIT, new ConfigSheet.Property("Hintergrundfarbe", LOWER_LIMIT_GROUP, Color.BLACK, "Help"));

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
                System.out.println("Error while settibg key: " + key);
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
                    System.out.println("Error while setting Key: " + key + " value: '" + value + "'");
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
        System.out.println("Label.getGraphic()");
        view.getChildren().clear();
        view.getChildren().add(label);
        view.getChildren().add(text);
        view.setMinWidth(50);


//        view.setBorder(new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
//        view.setStyle("-fx-background-color: " + options.get(BG_COLOR) + ";");//#00ff00;");


        return this;
    }

    @Override
    public void update() {
        System.out.println("Label.update()");
        String value = "";
        JEVisSample lastSample = null;

        /**
         * TODO: maybe improve performance by initial the color once and not every update
         */
        Color textColor = (Color) userConfig.get(FONT_COLOR).getObject();
        double opacity = (double) userConfig.get(BG_OPANCITY).getObject();
        Color bgColor = (Color) userConfig.get(BG_COLOR).getObject();
        Color textColorUpLimit = (Color) userConfig.get(FONT_COLOR_UP_LIMIT).getObject();
        Color textColorLowLimit = (Color) userConfig.get(FONT_COLOR_LOW_LIMIT).getObject();
        Color bgColorUpLimit = (Color) userConfig.get(BG_COLOR_UP_LIMIT).getObject();
        Color bgColorLowLimit = (Color) userConfig.get(BG_COLOR_LOW_LIMIT).getObject();
        boolean showUnit = (boolean) userConfig.get(SHOW_LABEL).getObject();
        boolean upperLimitON = (boolean) userConfig.get(UP_LIMIT_ENABLED).getObject();
        boolean lowLimitON = (boolean) userConfig.get(LOW_LIMIT_ENABLED).getObject();
        double lowLimit = (double) userConfig.get(LOW_LIMIT).getObject();
        double upperLimit = (double) userConfig.get(UPPER_LIMIT).getObject();
        boolean labelVisible = (boolean) userConfig.get(SHOW_LABEL).getObject();
        String labelText = (String) userConfig.get(LABEL_TEXT).getObject();

        bgColor.deriveColor(0, 0, 0, opacity);
        bgColorUpLimit.deriveColor(0, 0, 0, opacity);
        bgColorLowLimit.deriveColor(0, 0, 0, opacity);


        Font font = new Font((double) userConfig.get(FONT_SIZE).getObject());
        text.setFont(font);
        label.setFont(font);

        label.setVisible(labelVisible);
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

            } catch (JEVisException e) {
                e.printStackTrace();
            }
        }

        try {
            if (showUnit && attribute.getDisplayUnit() != null) {
                value += UnitManager.getInstance().formate(attribute.getDisplayUnit());
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
        configDia.setTitle("Config");
        configDia.setHeaderText("Label Element Configuration");


        ConfigSheet ct = new ConfigSheet();
        configDia.getDialogPane().setContent(ct.getSheet(userConfig));
        configDia.resizableProperty().setValue(true);
        configDia.setHeight(500);


        ButtonType buttonTypeOk = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        configDia.getDialogPane().getButtonTypes().add(buttonTypeOk);

        Optional<ButtonType> opt = configDia.showAndWait();
        update();

    }


    /**
     * Code sippet for later
     */
//    ObservableList<PropertySheet.Item> props = BeanPropertyUtils.getProperties(cData);
//    PropertySheet sheet = new PropertySheet(props);


    /**
     * Code for later
     */
//        sheet.setPropertyEditorFactory(new DefaultPropertyEditorFactory() {
//            @Override
//            public PropertyEditor<?> call(Item item) {
//                if (item.getType() == String.class) {
//                    return new TextPropertyEditor(item);
//                }
//                if (item.getType() == Integer.class) {
//                    return new IntegerPropertyEditor(item);
//                }
//                if (Number.class.isAssignableFrom(item.getType())) {
//                    return new NumberPropertyEditor(item);
//                }
//                if (item.getType() == Boolean.class) {
//                    return new ToggleSwitchEditor(item);
//                }
//                if (item.getType() == Theme.class) {
//                    return new ThemePropertyEditor(item);
//                }
//                return super.call(item);
//            }
//        });


}
