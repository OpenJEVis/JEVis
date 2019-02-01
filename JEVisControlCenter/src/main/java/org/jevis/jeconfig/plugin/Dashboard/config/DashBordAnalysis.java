package org.jevis.jeconfig.plugin.Dashboard.config;

import javafx.beans.property.*;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.plugin.scada.data.ConfigSheet;
import org.jevis.jeconfig.tool.I18n;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.*;

/**
 * Configuration for an BashBoard Analysis
 */
public class DashBordAnalysis {

    private final static String GENERAL_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.groupgeneral"), UPPER_LIMIT_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.groupupperlimitl"), LOWER_LIMIT_GROUP = I18n.getInstance().getString("plugin.scada.element.setting.label.grouplowerlimit");
    /**
     * Update rage in seconds
     */
    public final IntegerProperty updateRate = new SimpleIntegerProperty(new Integer(0), "Update Rate", 900) {
        @Override
        public void set(int newValue) {
            if (newValue < 900) {
                newValue = 900;
            }
            super.set(newValue);
        }

        @Override
        public void setValue(Number v) {
            if (v.intValue() < 900) {
                v = 900;
            }
            super.setValue(v);
        }
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
    public final ObjectProperty<Color> colorWidgetPlugin = new SimpleObjectProperty<>(Color.class, "Default Widget Background Color", Color.LEMONCHIFFON);
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

    public final BooleanProperty updateIsRunning = new SimpleBooleanProperty(Boolean.class, "Update", false);

    private final List<ChangeListener> changeListeners = new ArrayList<>();
    /**
     * Lower and upper Zoom limit
     */
    private double[] zommLimit = new double[]{0.2, 3};
    /**
     * Zoom factor for the dashbord view.
     * 1 is no zoom
     * 0.1 is 10% of the original size
     * 1.5 is 150% of the original size
     */
    public final DoubleProperty zoomFactor = new SimpleDoubleProperty(Double.class, "Zoom Factor", 1.0d) {
        @Override
        public void set(double value) {

            if (value < zommLimit[0]) {
                value = zommLimit[0];
            }
            if (value > zommLimit[1]) {
                value = zommLimit[1];
            }
            super.set(value);
        }

        @Override
        public void setValue(Number value) {
            if (value == null) {
                // depending on requirements, throw exception, set to default value, etc.
            } else {
                if (value.doubleValue() < zommLimit[0]) {
                    value = new Double(zommLimit[0]);
                }
                if (value.doubleValue() > zommLimit[1]) {
                    value = new Double(zommLimit[1]);
                }
                super.setValue(value);
            }
        }
    };

    public DashBordAnalysis() {

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
        System.out.println("Config result: " + opt);
        if (opt.get().equals(buttonTypeOk)) {
            System.out.println("Done");

            showGridProperty.setValue((boolean) userConfig.get(showGridProperty.getName()).getObject());
            snapToGridProperty.setValue((boolean) userConfig.get(snapToGridProperty.getName()).getObject());
            xGridInterval.setValue((Double) userConfig.get(xGridInterval.getName()).getObject());
            yGridInterval.setValue((Double) userConfig.get(yGridInterval.getName()).getObject());

            changeListeners.forEach(listener -> {
                listener.stateChanged(new ChangeEvent(this));
            });

            return true;
        }

        return false;

    }

    public void load() {


    }


}
