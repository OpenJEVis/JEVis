package org.jevis.jeconfig.plugin.Dashboard.config;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.controlsfx.control.PropertySheet;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.plugin.Dashboard.widget.Size;
import org.jevis.jeconfig.plugin.scada.data.ConfigSheet;
import org.jevis.jeconfig.tool.I18n;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class WidgetConfig {

    private final static String GENERAL_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.groupgeneral"), UPPER_LIMIT_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.groupupperlimitl"), LOWER_LIMIT_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.grouplowerlimit");

    public ObjectProperty<Color> fontColor = new SimpleObjectProperty<>(Color.class, "Font Color", Color.WHITE);
    public ObjectProperty<Color> fontColorSecondary = new SimpleObjectProperty<>(Color.class, "Font Color Secondary", Color.DODGERBLUE);

    public ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.class, "Background Color", Color.web("#126597"));
    public ObjectProperty<Position> position = new SimpleObjectProperty<>(Position.DEFAULT_1);
    public ObjectProperty<Size> size = new SimpleObjectProperty<>(Size.DEFAULT);
    public ObjectProperty<Font> font = new SimpleObjectProperty<>(Font.getDefault());
    private String type = "";
    private Map<String, ConfigSheet.Property> userConfig = new LinkedHashMap<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public PropertySheet getConfigSheet() {
        userConfig.clear();
        userConfig.put(backgroundColor.getName(), new ConfigSheet.Property("Background Color", GENERAL_GROUP, backgroundColor.getValue(), "Help"));
        userConfig.put(fontColorSecondary.getName(), new ConfigSheet.Property(fontColorSecondary.getName(), GENERAL_GROUP, fontColorSecondary.getValue(), "Help"));
        userConfig.put(fontColor.getName(), new ConfigSheet.Property("Font Color", GENERAL_GROUP, fontColor.getValue(), "Help"));
        userConfig.put("Height", new ConfigSheet.Property("Height", GENERAL_GROUP, size.getValue().getHeight(), "Help"));
        userConfig.put("Width", new ConfigSheet.Property("Width", GENERAL_GROUP, size.getValue().getWidth(), "Help"));

//        userConfig.put(font.getName(), new ConfigSheet.Property("Font", GENERAL_GROUP, font.getValue(), "Help"));
        ConfigSheet ct = new ConfigSheet();
        return ct.getSheet(userConfig);
    }


    public void applyUserConfig(Map<String, ConfigSheet.Property> userConfig) {
        backgroundColor.setValue((Color) userConfig.get(backgroundColor.getName()).getObject());
        fontColor.setValue((Color) userConfig.get(fontColor.getName()).getObject());
        fontColorSecondary.setValue((Color) userConfig.get(fontColorSecondary.getName()).getObject());
//            font.setValue((Font) userConfig.get(font.getName()).getObject());

        Size newSize = new Size((double) userConfig.get("Height").getObject(), (double) userConfig.get("Width").getObject());
        size.setValue(newSize);
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


    public enum Position {
        DEFAULT_1(100, 100), DEFAULT_2(100, 200), DEFAULT_3(500, 100), DEFAULT_4(500, 200);

        private double xPos = 100;
        private double yPos = 100;

        Position(double xPos, double yPos) {
            this.xPos = xPos;
            this.yPos = yPos;
        }

        public double getxPos() {
            return xPos;
        }

        public double getyPos() {
            return yPos;
        }
    }


}
