package org.jevis.jeconfig.application.tools;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.ScreenSize;

import java.util.*;

public class JEVisHelp {

    private static JEVisHelp jevisHelp;
    private static final Logger logger = LogManager.getLogger(JEVisHelp.class);
    private Map<String, Set<ToolTipElement>> controlsMap = new HashMap<>();
    private Map<String, Set<ToolTipElement>> controlsInfoMap = new HashMap<>();
    private BooleanProperty isHelpShowing = new SimpleBooleanProperty(false);
    private BooleanProperty isInfoShowing = new SimpleBooleanProperty(false);
    private String activePlugin = "";
    private String activeSubModule = "";
    private final KeyCombination help = new KeyCodeCombination(KeyCode.F1);
    public static final Font font = Font.font("Liberation Mono", FontWeight.SEMI_BOLD, 11);

    public enum LAYOUT {
        HORIZONTAL_TOP_LEFT,
        HORIZONTAL_TOP_CENTERED,
        VERTICAL_BOT_CENTER,
    }

    public JEVisHelp() {
    }

    public void deactivatePluginModule() {
        activeSubModule = "";
        update();
    }

    public void setActivePlugin(String plugin) {
        activePlugin = plugin;
        activeSubModule = "";
        update();
    }

    public void setActiveSubModule(String subModule) {
        logger.error("Set active sub module: {},{}", activePlugin, subModule);
        //removeAll(activePlugin, subModule);
        this.activeSubModule = subModule;
        //  update();
    }


    public static synchronized JEVisHelp getInstance() {
        if (jevisHelp == null) {
            jevisHelp = new JEVisHelp();
        }
        return jevisHelp;
    }

    public void update() {
        logger.error("------------------Update()-----------------------------");
        logger.error("Update: {}/{} {}", isHelpShowing.get(), isInfoShowing.get(), toKey(activePlugin, activeSubModule));

        hideAllTooltips(controlsMap);
        if (isHelpShowing.get()) showHelpTooltips(true);

        hideAllTooltips(controlsInfoMap);
        if (isInfoShowing.get()) showInfoTooltips(true);

    }

    private void showToolTips(Map<String, Set<ToolTipElement>> map, boolean show) {
        map.forEach((s, controls1) -> {
            logger.debug("showToolTips.map: {}", s);
            //if (!s.startsWith(activePlugin)) return; // if we want plugin and subModule visible at the same time
            String key = toKey(activePlugin, activeSubModule);
            if (!key.equals(s)) return;

            logger.error("Use map: {}->{}", key, controls1);
            for (ToolTipElement obj : controls1) {
                try {
                    logger.error("showInfoTooltips.Show: {}", obj);
                    obj.show(show);
                } catch (Exception ex) {
                    logger.warn(ex, ex);
                }
            }
        });
    }

    public void showInfoTooltips(boolean show) {
        logger.error("Show info tooltips: {},{}", show, toKey(activePlugin, activeSubModule));
        showToolTips(controlsInfoMap, show);
        isInfoShowing.setValue(show);
    }

    public void showHelpTooltips(boolean show) {
        logger.debug("Show tooltips: {},{}", show, activePlugin);
        showToolTips(controlsMap, show);
        isHelpShowing.setValue(show);
    }

    public void hideAllTooltips(Map<String, Set<ToolTipElement>> map) {
        logger.error("Hide All");

        map.forEach((s, toolTipElements) -> {
            //List<ToolTipElement> toRemove = new ArrayList<>();
            toolTipElements.forEach(toolTipElement -> {
                if (toolTipElement.isVisible()) toolTipElement.show(false);
                //if (toolTipElement.toDelete()) toRemove.add(toolTipElement);
            });
            //toolTipElements.removeAll(toRemove);
        });


    }


    public void toggle() {
        showHelpTooltips(!isHelpShowing.get());
    }

    public void toggleInfo() {
        showInfoTooltips(!isInfoShowing.get());
    }

    public ObservableBooleanValue isHelpShowingProperty() {
        return isHelpShowing;
    }

    public ObservableBooleanValue isInfoShowingProperty() {
        return isInfoShowing;
    }

    public void addHelpItems(String plugin, String subModule, LAYOUT layout, List<Node> nodes) {
        logger.error("Add Help items for: {}.{}", plugin, subModule);
        for (Node node : nodes) {
            try {
                if (node instanceof Control) {
                    addHelpControl(plugin, subModule, layout, (Control) node);
                }
            } catch (Exception ex) {
                logger.warn(ex);
            }
        }
    }

    public void addControl(Map<String, Set<ToolTipElement>> map, String plugin, String subModule, LAYOUT layout, Control... elements) {
        String key = toKey(plugin, subModule);
        logger.error("AddInfoControls: {},{}", key, elements.length);
        if (map.get(key) == null || map.get(key) == null) {
            map.put(key, new HashSet<>());
        }

        for (Control element : elements) {
            System.out.println("Add Control: " + element.getId());
            map.get(key).add(new ToolTipElement(layout, element));
            JEVisHelp.setStyle(element.getTooltip());
        }

        if (isInfoShowing.get()) update();
    }

    public void addHelpControl(String plugin, String subModule, LAYOUT layout, Control... elements) {
        addControl(controlsMap, plugin, subModule, layout, elements);
        //if (isHelpShowing.get()) update();
        if (isHelpShowing.get()) {
            showHelpTooltips(true);
        }
    }

    public void addInfoControl(String plugin, String subModule, LAYOUT layout, Control... elements) {
        addControl(controlsInfoMap, plugin, subModule, layout, elements);
        if (isInfoShowing.get()) update();
    }


    private String toKey(String plugin, String subModule) {
        return plugin + "." + subModule;
    }

    public void removeAll(String plugin) {
        controlsMap.forEach((s, controls1) -> {
            if (s.startsWith(plugin)) {
                if (controls1 != null) {
                    controls1.clear();
                }
            }
        });
    }

    public void removeAll(String plugin, String subModule) {
        Set<ToolTipElement> set = controlsMap.get(toKey(plugin, subModule));
        if (set != null) {
            set.clear();
        }
    }

    public void registerHotKey(Stage dialog) {
        dialog.getScene().setOnKeyPressed(ke -> {
            if (help.match(ke)) {
                JEVisHelp.getInstance().toggle();
                ke.consume();
            }
        });
    }

    public void registerHotKey(Dialog dialog) {
        dialog.getDialogPane().setOnKeyPressed(ke -> {
            if (help.match(ke)) {
                JEVisHelp.getInstance().toggle();
                ke.consume();
            }
        });
    }

    public Node buildSpacerNode() {
        Region spacerForRightSide = new Region();
        HBox.setHgrow(spacerForRightSide, Priority.ALWAYS);
        return spacerForRightSide;
    }

    public ToggleButton buildHelpButtons(double width, double height) {
        ToggleButton helpButton = new ToggleButton("", JEConfig.getImage("1404161580_help_blue.png", height, width));
        helpButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.toolbar.tip.help")));
        helpButton.setOnAction(event -> JEVisHelp.getInstance().toggle());
        isHelpShowing.addListener((observable, oldValue, newValue) -> {
            helpButton.setSelected(newValue);
        });

        return helpButton;
    }

    public ToggleButton buildInfoButtons(double width, double height) {
        ToggleButton infoButton = new ToggleButton("", JEConfig.getImage("1404337146_info.png", height, width));
        infoButton.setOnAction(event -> toggleInfo());
        isInfoShowing.addListener((observable, oldValue, newValue) -> {
            infoButton.setSelected(newValue);
        });
        return infoButton;
    }


    public class ToolTipElement {

        private LAYOUT layout = LAYOUT.VERTICAL_BOT_CENTER;
        private Control control;
        private boolean isVisible = false;

        public ToolTipElement(LAYOUT layout, Control control) {
            this.layout = layout;
            this.control = control;
        }

        public boolean isVisible() {
            return isVisible;
        }

        /**
         * Simple check if this Control element is still in use
         *
         * @return
         */
        public boolean toDelete() {
            return control.getPadding() == null;
        }

        public void show(boolean show) {
            logger.debug("Show: {}", control);
            isVisible = show;
            Platform.runLater(() -> {
                try {
                    Tooltip tooltip = control.getTooltip();
                    if (tooltip != null && !tooltip.getText().isEmpty()) {
                        logger.debug("Show tt: {},{} text: {} ", tooltip.getFont().getName(), tooltip.getFont().getSize(), tooltip.getText());
                        //JEVisHelp.setStyle(tooltip);
                        if (tooltip.getGraphic() == null) tooltip.setGraphic(new Region());
                        if (tooltip.isShowing() != show) {
                            if (tooltip.isShowing()) Platform.runLater(() -> {
                                tooltip.hide();
                                Label parent = (Label) tooltip.getGraphic().getParent();
                                parent.getTransforms().clear();
                            });
                            else {
                                double[] pos = ScreenSize.getAbsoluteScreenPosition(control);
                                double xPos = pos[0];
                                double yPos = pos[1];
                                if (pos[0] + pos[1] == 0) return; // in case the parent is not visible anymore
                                //System.out.println("Pos: " + xPos + "/" + yPos);
                                tooltip.show(control, xPos, yPos);
                                Node parent = (Node) tooltip.getGraphic().getParent();
                                switch (layout) {
                                    case VERTICAL_BOT_CENTER:
                                        double ttHeight = tooltip.getHeight(); // after the Rotate the numbers are bad

                                        //logger.error("xPos: {}, controlH: {}, controlW: {}, ttH: {}, ttW; {}", xPos, control.getWidth(), control.getHeight(), tooltip.getHeight(), tooltip.getWidth());
                                        parent.getTransforms().add(new Rotate(-90));
                                        //xPos += -control.getHeight() / 2;
                                        //xPos += (control.getWidth() / 2);// + (control.getHeight() / 2);
                                        //xPos += control.getWidth() / 2;
                                        double shadow = 5;
                                        double ttWithoutShadow = ttHeight - shadow;
                                        //xPos += -shadow;
                                        xPos += (control.getWidth() / 2) - (ttWithoutShadow / 2);
                                        //logger.error("<--> {} | {} result: {}, {},{}", control.getWidth(), tooltip.getHeight(), (control.getWidth() / 2) - (ttWithoutShaow / 2), tooltip.getWidth(), tooltip.getHeight());

                                        yPos += control.getHeight();

                                        break;
                                    case HORIZONTAL_TOP_LEFT:
                                        yPos += -36;//-tooltip.getHeight();
                                        xPos += -8;// magic number
                                        break;
                                    case HORIZONTAL_TOP_CENTERED:
                                        yPos += -36;
                                        xPos += (parent.getLayoutY() / 2) - (tooltip.widthProperty().doubleValue() / 2) - 8;
                                        break;
                                }

                                tooltip.setX(xPos);
                                tooltip.setY(yPos);

                                logger.debug("done show: {}", control);

                            }
                        }
                    }

                } catch (Exception ex) {
                    logger.warn(ex, ex);
                    //ex.getStackTrace();
                }
            });
        }

    }

    public static void setStyle(Tooltip tooltip) {
        if (tooltip == null) return;
        tooltip.setFont(font);
        tooltip.setStyle(" -fx-background-color: rgb(235,235,235,1); -fx-text-fill: black; -fx-font-smoothing-type: lcd;"); //tooltip
    }

}
