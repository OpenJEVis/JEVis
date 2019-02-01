package org.jevis.jeconfig.plugin.Dashboard.config;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.paint.Color;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.plugin.scada.data.ConfigSheet;
import org.jevis.jeconfig.tool.I18n;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class WidgetConfig {
    private final static String GENERAL_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.groupgeneral"), UPPER_LIMIT_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.groupupperlimitl"), LOWER_LIMIT_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.grouplowerlimit");


    public ObjectProperty<Color> fontColor = new SimpleObjectProperty<>(Color.class, "Font Color", Color.DODGERBLUE);
    public ObjectProperty<Color> fontColorSecondary = new SimpleObjectProperty<>(Color.class, "Font Color Secondary", Color.DODGERBLUE);

    public ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.class, "Background Color", Color.LIGHTCORAL);
    public ObjectProperty<Position> position = new SimpleObjectProperty<>(Position.DEFAULT_1);
    public ObjectProperty<Size> size = new SimpleObjectProperty<>(Size.DEFAULT);
    private String type = "";

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean openConfig() {
        Map<String, ConfigSheet.Property> userConfig = new LinkedHashMap<>();
        userConfig.put(backgroundColor.getName(), new ConfigSheet.Property("Background Color", GENERAL_GROUP, backgroundColor.getValue(), "Help"));
        userConfig.put(fontColorSecondary.getName(), new ConfigSheet.Property(fontColorSecondary.getName(), GENERAL_GROUP, fontColorSecondary.getValue(), "Help"));
        userConfig.put(fontColor.getName(), new ConfigSheet.Property("Font Color", GENERAL_GROUP, fontColor.getValue(), "Help"));


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


        configDia.getDialogPane().getButtonTypes().addAll(buttonTypeCancel, buttonTypeOk);

        Optional<ButtonType> opt = configDia.showAndWait();
        System.out.println("Config result: " + opt);
        if (opt.get().equals(buttonTypeOk)) {
            System.out.println("Done");

            backgroundColor.setValue((Color) userConfig.get(backgroundColor.getName()).getObject());
            fontColor.setValue((Color) userConfig.get(fontColor.getName()).getObject());
            return true;
        }

        return false;
    }


    public enum Position {
        DEFAULT_1(100, 100), DEFAULT_2(100, 200), DEFAULT_3(200, 100), DEFAULT_4(200, 200);

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

    /**
     * Common Sizes for an Widget
     */
    public enum Size {
        DEFAULT(100, 250), ONE_TWO(50, 100), TWO_TWO(100, 100), TWO_THREE(200, 300);

        private double height = 50;
        private double width = 100;

        Size(double height, double width) {
            this.height = height;
            this.width = width;
        }

        public double getHeight() {
            return height;
        }

        public double getWidth() {
            return width;
        }
    }

}
