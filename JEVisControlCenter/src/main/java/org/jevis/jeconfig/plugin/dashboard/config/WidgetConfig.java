package org.jevis.jeconfig.plugin.dashboard.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderWidths;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.PropertySheet;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.plugin.dashboard.config2.Size;
import org.jevis.jeconfig.plugin.scada.data.ConfigSheet;
import org.jevis.jeconfig.tool.I18n;

import java.util.*;

public class WidgetConfig {


    private final static String GENERAL_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.groupgeneral"), UPPER_LIMIT_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.groupupperlimitl"), LOWER_LIMIT_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.grouplowerlimit");
    private static final Logger logger = LogManager.getLogger(WidgetConfig.class);
    public static String DATA_HANDLER_NODE = "dataHandler";
    public static String WIDGET_SETTINGS_NODE = "extra";
    public final ObjectProperty<BorderWidths> borderSize = new SimpleObjectProperty(Double.class, "Border Size", new BorderWidths(0.2));
    public final ObjectProperty<Color> fontColor = new SimpleObjectProperty<>(Color.class, "Font Color", Color.WHITE);
    public final ObjectProperty<Color> fontColorSecondary = new SimpleObjectProperty<>(Color.class, "Font Color Secondary", Color.DODGERBLUE);
    public final StringProperty title = new SimpleStringProperty(String.class, "title", "Title");
    public final StringProperty unit = new SimpleStringProperty("");
    public final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.class, "Background Color", Color.web("#126597"));
    public final DoubleProperty xPosition = new SimpleDoubleProperty(100d);
    public final DoubleProperty yPosition = new SimpleDoubleProperty(100d);
    public final ObjectProperty<Size> size = new SimpleObjectProperty<>(Size.DEFAULT);
    public final ObjectProperty<Font> font = new SimpleObjectProperty<>(Font.getDefault());
    public final DoubleProperty fontSize = new SimpleDoubleProperty(13);
    public final ObjectProperty<Pos> titlePosition = new SimpleObjectProperty<>(Pos.class, "Title Position", Pos.CENTER);
    public final BooleanProperty showShadow = new SimpleBooleanProperty(Boolean.class, "Show Shadows", true);
    public final IntegerProperty decimals = new SimpleIntegerProperty(Integer.class, "Decimals", 2);

    private String type = "";
    private Map<String, ConfigSheet.Property> userConfig = new LinkedHashMap<>();
    private List<WidgetConfigProperty> additionalSetting = new ArrayList<>();
    private Map<String, JsonNode> additionalConfigNodes = new HashMap<>();
    private ObjectMapper mapper = new ObjectMapper();//.enable(SerializationFeature.INDENT_OUTPUT);
    private JsonNode extraNode = this.mapper.createObjectNode();
    private String dataHandlerJson;
    private JsonNode jsonNode;

    public WidgetConfig(String type) {
        this.type = type;
    }

    public WidgetConfig(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
        try {
            try {
                this.title.setValue(jsonNode.get(this.title.getName()).asText(this.title.get()));
            } catch (Exception ex) {
                logger.debug("Could not parse {}: {}", this.title.getName(), ex.getMessage());
            }

            try {
                this.titlePosition.setValue(Pos.valueOf(jsonNode.get(this.titlePosition.getName()).asText(Pos.CENTER.toString())));
            } catch (Exception ex) {
                logger.debug("Could not parse {}: {}", this.titlePosition.getName(), ex);
            }


            try {
                this.type = jsonNode.get("WidgetType").asText("");
            } catch (Exception ex) {
                logger.debug("Could not parse {}: {}", "WidgetType", ex);
            }

            try {
                this.backgroundColor.setValue(Color.valueOf(jsonNode.get(this.backgroundColor.getName()).asText(this.backgroundColor.get().toString())));
            } catch (Exception ex) {
                logger.debug("Could not parse {}: {}", this.backgroundColor.getName(), ex);
            }

            try {
                this.fontColor.setValue(Color.valueOf(jsonNode.get(this.fontColor.getName()).asText(Color.BLACK.toString())));
            } catch (Exception ex) {
                logger.debug("Could not parse {}: {}", this.fontColor.getName(), ex);
            }
            try {
                Size newSize = new Size(jsonNode.get("height").asDouble(), jsonNode.get("width").asDouble());
                this.size.setValue(newSize);
            } catch (Exception ex) {
                logger.debug("Could not parse Size: {}", ex);
            }
            try {
                this.fontSize.setValue(jsonNode.get("fontSize").asDouble(13));
            } catch (Exception ex) {
//                logger.error("Could not parse position: {}", fontSize.getName(), ex);
            }

            try {
                this.xPosition.setValue(jsonNode.get("xPos").asDouble(0));
                this.yPosition.setValue(jsonNode.get("yPos").asDouble(0));
            } catch (Exception ex) {
                logger.debug("Could not parse position: {}", this.title.getName(), ex);
            }

            try {
                this.showShadow.setValue(jsonNode.get("shadow").asBoolean(true));
            } catch (Exception ex) {
                logger.debug("Could not parse {}: {}", this.showShadow.getName(), ex.getMessage());
            }

            try {
                this.borderSize.setValue(new BorderWidths(jsonNode.get("borderSize").asDouble(0.2)));
            } catch (Exception ex) {
                logger.debug("Could not parse position: {}", this.showShadow.getName(), ex.getMessage());
            }


            if (jsonNode.get(DATA_HANDLER_NODE) != null) {
                this.dataHandlerJson = jsonNode.asText(DATA_HANDLER_NODE);
//                System.out.println("DATA_HANDLER_NODE: " + dataHandlerJson);
//                dataHandlerNode = jsonNode.get(DATA_HANDLER_NODE);
            } else {
                logger.debug("------ missing json node: {}", DATA_HANDLER_NODE);
            }


            if (jsonNode.get(WIDGET_SETTINGS_NODE) != null) {
                this.extraNode = jsonNode.get(WIDGET_SETTINGS_NODE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public boolean hasChanged(String configHash) {
        //TODO create hash cron JSon.toString
        return true;
    }

    public JsonNode getConfigNode(String name) {
        return this.jsonNode.get(name);
    }

    public JsonNode getExtraSettingNode() {
        return this.extraNode;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * TODO: add default setting to list like the additional settings
     *
     * @return
     */
    public PropertySheet getConfigSheet() {
        WidgetConfigEditor widgetConfigEditor = new WidgetConfigEditor(this);
        widgetConfigEditor.show();


        this.userConfig.clear();
        this.userConfig.put(this.title.getName(), new ConfigSheet.Property("Title", GENERAL_GROUP, this.title.getValue(), "Help"));
        this.userConfig.put(this.unit.getName(), new ConfigSheet.Property("Unit", GENERAL_GROUP, this.unit.getValue(), "Help"));

        this.userConfig.put(this.backgroundColor.getName(), new ConfigSheet.Property("Background Color", GENERAL_GROUP, this.backgroundColor.getValue(), "Help"));
        this.userConfig.put(this.fontColorSecondary.getName(), new ConfigSheet.Property(this.fontColorSecondary.getName(), GENERAL_GROUP, this.fontColorSecondary.getValue(), "Help"));
        this.userConfig.put(this.fontColor.getName(), new ConfigSheet.Property("Font Color", GENERAL_GROUP, this.fontColor.getValue(), "Help"));
        this.userConfig.put("Font Size", new ConfigSheet.Property("fontSize", GENERAL_GROUP, this.fontSize.getValue(), "Help"));

        this.userConfig.put("Height", new ConfigSheet.Property("Height", GENERAL_GROUP, this.size.getValue().getHeight(), "Help"));
        this.userConfig.put("Width", new ConfigSheet.Property("Width", GENERAL_GROUP, this.size.getValue().getWidth(), "Help"));


        this.additionalSetting.forEach(widgetConfigProperty -> {
//            System.out.println("Add additional config: " + widgetConfigProperty.getId() + "  " + widgetConfigProperty.getName());
            this.userConfig.put(widgetConfigProperty.getId(), new ConfigSheet.Property(widgetConfigProperty.getName(), widgetConfigProperty.getCategory(), widgetConfigProperty.getWritableValue().getValue(), widgetConfigProperty.getDescription()));

        });


        ConfigSheet ct = new ConfigSheet();
        PropertySheet propertySheet = ct.getSheet(this.userConfig);
        propertySheet.setMode(PropertySheet.Mode.CATEGORY);
        propertySheet.setSearchBoxVisible(false);
        propertySheet.setModeSwitcherVisible(false);
        return propertySheet;
    }

    public ObjectNode toJsonNode() {
        ObjectNode jsonNode = this.mapper.createObjectNode();
        jsonNode.put(this.title.getName(), this.title.getValue());
        jsonNode.put("WidgetType", this.type);

        jsonNode.put(this.backgroundColor.getName(), this.backgroundColor.getValue().toString());
        jsonNode.put(this.fontColor.getName(), this.fontColor.getValue().toString());
        jsonNode.put("height", this.size.getValue().getHeight());
        jsonNode.put("width", this.size.getValue().getWidth());
        jsonNode.put("xPos", this.xPosition.getValue());
        jsonNode.put("yPos", this.yPosition.getValue());
        jsonNode.put("fontSize", this.fontSize.getValue());


        ObjectNode additionalNodes = this.mapper.createObjectNode();
        additionalNodes.setAll(this.additionalConfigNodes);
        jsonNode.setAll(additionalNodes);


        return jsonNode;
    }


    public void applyUserConfig() {
        applyUserConfig(this.userConfig);
    }

    public void applyUserConfig(Map<String, ConfigSheet.Property> userConfig) {
        System.out.println("ApplyUserConfig");
        this.backgroundColor.setValue((Color) userConfig.get(this.backgroundColor.getName()).getObject());
        this.fontColor.setValue((Color) userConfig.get(this.fontColor.getName()).getObject());
        this.fontColorSecondary.setValue((Color) userConfig.get(this.fontColorSecondary.getName()).getObject());
        this.unit.setValue((String) userConfig.get(this.unit.getName()).getObject());
        Size newSize = new Size((double) userConfig.get("Height").getObject(), (double) userConfig.get("Width").getObject());
        this.size.setValue(newSize);

        try {
            this.fontSize.setValue((double) userConfig.get("fontSize").getObject());
        } catch (Exception ex) {
            logger.error(ex);
        }
        this.title.setValue((String) userConfig.get(this.title.getName()).getObject());

        this.additionalSetting.forEach(widgetConfigProperty -> {
            widgetConfigProperty.getWritableValue().setValue(userConfig.get(widgetConfigProperty.getId()).getObject());
        });

        System.out.println("Font Color: " + this.fontColor.getValue().toString());
        System.out.println("Background color: " + this.backgroundColor.getValue().toString());


    }

    public boolean openConfig() {

        Dialog configDia = new Dialog();
        configDia.setTitle(I18n.getInstance().getString("plugin.scada.element.config.title"));
        configDia.setHeaderText(I18n.getInstance().getString("plugin.scada.element.config.header"));


        configDia.getDialogPane().setContent(getConfigSheet());
        configDia.resizableProperty().setValue(true);
        configDia.setHeight(800);
        configDia.setWidth(500);

        configDia.getDialogPane().setMinWidth(500);
        configDia.getDialogPane().setMinHeight(500);
        configDia.setGraphic(ResourceLoader.getImage("1394482166_blueprint_tool.png", 50, 50));

        ButtonType buttonTypeOk = new ButtonType(I18n.getInstance().getString("plugin.scada.element.config.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType(I18n.getInstance().getString("plugin.scada.element.config.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);


        configDia.getDialogPane().getButtonTypes().addAll(buttonTypeCancel, buttonTypeOk);

        Optional<ButtonType> opt = configDia.showAndWait();
        if (opt.get().equals(buttonTypeOk)) {
            applyUserConfig(this.userConfig);
            return true;
        }

        return false;
    }


}
