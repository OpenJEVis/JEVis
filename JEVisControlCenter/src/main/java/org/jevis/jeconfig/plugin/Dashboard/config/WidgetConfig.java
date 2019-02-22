package org.jevis.jeconfig.plugin.Dashboard.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.*;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.PropertySheet;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.plugin.Dashboard.widget.Size;
import org.jevis.jeconfig.plugin.scada.data.ConfigSheet;
import org.jevis.jeconfig.tool.I18n;

import java.util.*;

public class WidgetConfig {

    private final static String GENERAL_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.groupgeneral"), UPPER_LIMIT_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.groupupperlimitl"), LOWER_LIMIT_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.grouplowerlimit");
    private static final Logger logger = LogManager.getLogger(WidgetConfig.class);
    public StringProperty uuid = new SimpleStringProperty(UUID.randomUUID().toString());
    public ObjectProperty<Color> fontColor = new SimpleObjectProperty<>(Color.class, "Font Color", Color.WHITE);
    public ObjectProperty<Color> fontColorSecondary = new SimpleObjectProperty<>(Color.class, "Font Color Secondary", Color.DODGERBLUE);
    public ObjectProperty<Size> minumimSize = new SimpleObjectProperty<>(Size.class, "Min Size", new Size(20, 20));
    public StringProperty title = new SimpleStringProperty("");
    public StringProperty unit = new SimpleStringProperty("");
    public ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.class, "Background Color", Color.web("#126597"));
    //    public ObjectProperty<Position> position = new SimpleObjectProperty<>(new Position(100, 100));
    public DoubleProperty xPosition = new SimpleDoubleProperty(100d);
    public DoubleProperty yPosition = new SimpleDoubleProperty(100d);

    public ObjectProperty<Size> size = new SimpleObjectProperty<>(Size.DEFAULT);
    public ObjectProperty<Font> font = new SimpleObjectProperty<>(Font.getDefault());
    private String type = "";
    private Map<String, ConfigSheet.Property> userConfig = new LinkedHashMap<>();
    private List<WidgetConfigProperty> additionalSetting = new ArrayList<>();

    public WidgetConfig(String type) {
        this.type = type;
    }


    public WidgetConfig(JsonNode jsonNode) {
        try {
            try {
                title.setValue(jsonNode.get(title.getName()).asText(title.get()));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", title.getName(), ex);
            }

            try {
                type = jsonNode.get("WidgetType").asText("");
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", "WidgetType", ex);
            }

            try {
                backgroundColor.setValue(Color.valueOf(jsonNode.get(backgroundColor.getName()).asText(backgroundColor.get().toString())));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", title.getName(), ex);
            }

            try {
                fontColor.setValue(Color.valueOf(jsonNode.get(fontColor.getName()).asText(fontColor.get().toString())));
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", title.getName(), ex);
            }
            try {
                Size newSize = new Size(jsonNode.get("height").asDouble(), jsonNode.get("width").asDouble());
                size.setValue(newSize);
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", title.getName(), ex);
            }
            try {
//                Position newPosition = new Position(jsonNode.get("xPos").asDouble(), jsonNode.get("yPos").asDouble());
//                System.out.println("Load Postion :" + newPosition.toString());
//                position.setValue(newPosition);
                xPosition.setValue(jsonNode.get("xPos").asDouble());
                yPosition.setValue(jsonNode.get("yPos").asDouble());
            } catch (Exception ex) {
                logger.error("Could not parse {}: {}", title.getName(), ex);
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public String getType() {
        return type;
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
        userConfig.clear();
        userConfig.put(title.getName(), new ConfigSheet.Property("Title", GENERAL_GROUP, title.getValue(), "Help"));
        userConfig.put(unit.getName(), new ConfigSheet.Property("Unit", GENERAL_GROUP, unit.getValue(), "Help"));

        userConfig.put(backgroundColor.getName(), new ConfigSheet.Property("Background Color", GENERAL_GROUP, backgroundColor.getValue(), "Help"));
        userConfig.put(fontColorSecondary.getName(), new ConfigSheet.Property(fontColorSecondary.getName(), GENERAL_GROUP, fontColorSecondary.getValue(), "Help"));
        userConfig.put(fontColor.getName(), new ConfigSheet.Property("Font Color", GENERAL_GROUP, fontColor.getValue(), "Help"));
        userConfig.put("Height", new ConfigSheet.Property("Height", GENERAL_GROUP, size.getValue().getHeight(), "Help"));
        userConfig.put("Width", new ConfigSheet.Property("Width", GENERAL_GROUP, size.getValue().getWidth(), "Help"));

        additionalSetting.forEach(widgetConfigProperty -> {
            System.out.println("Add additional config: " + widgetConfigProperty.getId() + "  " + widgetConfigProperty.getName());
            userConfig.put(widgetConfigProperty.getId(), new ConfigSheet.Property(widgetConfigProperty.getName(), widgetConfigProperty.getCategory(), widgetConfigProperty.getWritableValue().getValue(), widgetConfigProperty.getDescription()));
        });


        ConfigSheet ct = new ConfigSheet();
        PropertySheet propertySheet = ct.getSheet(userConfig);
        propertySheet.setMode(PropertySheet.Mode.CATEGORY);
        propertySheet.setSearchBoxVisible(false);
        propertySheet.setModeSwitcherVisible(false);
        return propertySheet;
    }

    public ObjectNode toJsonNode() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put(title.getName(), title.getValue());
        jsonNode.put("WidgetType", type);

        jsonNode.put(backgroundColor.getName(), backgroundColor.getValue().toString());
        jsonNode.put(fontColor.getName(), fontColor.getValue().toString());
        jsonNode.put("height", size.getValue().getHeight());
        jsonNode.put("width", size.getValue().getWidth());
        jsonNode.put("xPos", xPosition.getValue());
        jsonNode.put("yPos", yPosition.getValue());

        return jsonNode;
    }

    public void addAdditionalSetting(List<WidgetConfigProperty> list) {
        additionalSetting.addAll(list);
    }


    public void applyUserConfig() {
        applyUserConfig(userConfig);
    }

    public void applyUserConfig(Map<String, ConfigSheet.Property> userConfig) {
        System.out.println("ApplyUserConfig");
        backgroundColor.setValue((Color) userConfig.get(backgroundColor.getName()).getObject());
        fontColor.setValue((Color) userConfig.get(fontColor.getName()).getObject());
        fontColorSecondary.setValue((Color) userConfig.get(fontColorSecondary.getName()).getObject());
        unit.setValue((String) userConfig.get(unit.getName()).getObject());
        Size newSize = new Size((double) userConfig.get("Height").getObject(), (double) userConfig.get("Width").getObject());
        size.setValue(newSize);

        title.setValue((String) userConfig.get(title.getName()).getObject());

        System.out.println("Widget.Settings: " + additionalSetting);
        additionalSetting.forEach(widgetConfigProperty -> {
            widgetConfigProperty.getWritableValue().setValue(userConfig.get(widgetConfigProperty.getId()).getObject());
        });
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
        System.out.println("Config result: " + opt);
        if (opt.get().equals(buttonTypeOk)) {
            System.out.println("Done");
            applyUserConfig(userConfig);

            return true;
        }

        return false;
    }


}
