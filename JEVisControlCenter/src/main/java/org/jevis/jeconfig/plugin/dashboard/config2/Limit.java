package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;

public class Limit {

    private static final Logger logger = LogManager.getLogger(Limit.class);

    Double lowerLimit = 0d;
    Double upperLimit = 0d;

    Double lowerLimitDynamic = Double.NaN;
    Double upperLimitDynamic = Double.NaN;

    Double lowerLimitOffset = 1d;
    Double upperLimitOffset = 1d;

    boolean hasLowerLimit = false;
    boolean hasUpperLimit = false;

    Color upperLimitColor = Color.RED;
    Color lowerLimitColor = Color.GREEN;

    public enum MODE {
        STATIC, DYNAMIC
    }

    ObservableList<MODE> types = FXCollections.observableArrayList(MODE.STATIC, MODE.DYNAMIC);
    private MODE mode = MODE.STATIC;

    int limitWidget = -1;
    final DashboardControl dashboardControl;

    public Limit(DashboardControl control) {
        this(control, null);
    }

    public int getLimitWidgetID() {
        return limitWidget;
    }

    public Limit(DashboardControl control, JsonNode jsonNode) {
        this.dashboardControl = control;

        if (jsonNode != null) {
            boolean isDynamic = false;
            if ((jsonNode.get("source")) != null) {
                mode = (jsonNode.get("source").asInt(-1) > 0) ? MODE.DYNAMIC : MODE.STATIC;
            }

            if (mode == MODE.DYNAMIC) {
                limitWidget = jsonNode.get("source").asInt(-1);
                lowerLimitOffset = jsonNode.get("lowerLimitOffset").asDouble(1);
                upperLimitOffset = jsonNode.get("upperLimitOffset").asDouble(1);
            } else if (mode == MODE.STATIC) {
                lowerLimit = jsonNode.get("lowerLimitValue").asDouble(Double.NaN);
                upperLimit = jsonNode.get("upperLimitValue").asDouble(Double.NaN);
            }

            hasLowerLimit = jsonNode.get("lowerLimit").asBoolean(false);
            hasUpperLimit = jsonNode.get("upperLimit").asBoolean(false);

            lowerLimitColor = Color.valueOf(jsonNode.get("lowerLimitColor").asText(Color.RED.toString()));
            upperLimitColor = Color.valueOf(jsonNode.get("upperLimitColor").asText(Color.GREEN.toString()));
        }


    }


    private class LimitTab extends Tab implements ConfigTab {
        Limit limit;

        public LimitTab(String text, Limit limit) {
            super(text);
            this.limit = limit;
        }

        @Override
        public void commitChanges() {
            //TODO;
        }
    }

    public Tab getConfigTab() {
        LimitTab tab = new LimitTab(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.tab")
                , this);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setVgap(8);
        gridPane.setHgap(8);


        AnchorPane editorPane = new AnchorPane();
        LimitDynamicPane limitDynamicPane = new LimitDynamicPane(this, dashboardControl.getWidgetList());
        LimitStaticPane limitStaticPane = new LimitStaticPane(this);


        Label limitTypeLabel = new Label(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.type"));
        MFXComboBox<MODE> limitTypeBox = new MFXComboBox<>(types);

        //TODO JFX17

        limitTypeBox.setConverter(new StringConverter<MODE>() {
            @Override
            public String toString(MODE object) {
                String text = "";
                if (object != null) {
                    switch (object) {
                        case STATIC:
                            text = I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.type.static");
                            break;
                        case DYNAMIC:
                            text = I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.type.dynamic");
                            break;
                    }

                }

                return text;
            }

            @Override
            public MODE fromString(String string) {
                return limitTypeBox.getItems().get(limitTypeBox.getSelectedIndex());
            }
        });

        limitTypeBox.selectItem(mode);
        if (mode == MODE.DYNAMIC) {
            editorPane.getChildren().add(limitDynamicPane);
        } else if (mode == MODE.STATIC) {
            editorPane.getChildren().add(limitStaticPane);
        }

        limitTypeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            mode = newValue;
            Platform.runLater(() -> {
                if (newValue == MODE.DYNAMIC) {
                    editorPane.getChildren().set(0, limitDynamicPane);
                } else if (newValue == MODE.STATIC) {
                    editorPane.getChildren().set(0, limitStaticPane);
                }
            });
        });
        gridPane.addRow(0, limitTypeLabel, limitTypeBox);
        gridPane.add(new Separator(Orientation.HORIZONTAL), 0, 1, 2, 1);
        gridPane.add(editorPane, 0, 2, 2, 1);

        tab.setContent(gridPane);
        return tab;
    }


    public Double getUpperLimitDynamic() {
        return upperLimitDynamic;
    }

    public void setUpperLimitDynamic(Double upperLimitDynamic) {
        this.upperLimitDynamic = upperLimitDynamic;
    }

    public Double getLowerLimitDynamic() {
        return lowerLimitDynamic;
    }

    public void setLowerLimitDynamic(Double lowerLimitDynamic) {
        this.lowerLimitDynamic = lowerLimitDynamic;
    }

    public Color getExceedsLimitColor(Color defaultColor, double value) {
        if (exceedsLowerLimit(value)) {
            return lowerLimitColor;
        } else if (exceedsUpperLimit(value)) {
            return upperLimitColor;
        } else {
            return defaultColor;
        }
    }

    public boolean exceedsLowerLimit(double value) {
        if (hasLowerLimit) {
            Double limitValue = getLowerLimit();

            if (mode == MODE.DYNAMIC) {
                limitValue = lowerLimitDynamic * lowerLimitOffset;
            }

            if (limitWidget > 0) {
                if (limitValue.isNaN()) {
                    return false;
                }
                limitValue = lowerLimitDynamic;
            }

            logger.debug("exceedsLowerLimit(): {}<={}*{}={}", value, limitValue, lowerLimitOffset, (value <= (limitValue * lowerLimitOffset)));
            return (value <= (limitValue * lowerLimitOffset));
        } else {
            return false;
        }
    }

    public boolean exceedsUpperLimit(double value) {
        if (hasUpperLimit) {

            Double limitValue = getUpperLimit();

            if (mode == MODE.DYNAMIC) {
                limitValue = upperLimitDynamic * upperLimitOffset;
            }


            if (limitWidget > 0) {
                if (limitValue.isNaN()) {
                    return false;
                }
                limitValue = upperLimitDynamic;
            }
            logger.debug("exceedsUpperLimit(): {}>={}*{}={}", value, limitValue, upperLimitOffset, (value >= (limitValue * upperLimitOffset)));
            return (value >= (limitValue * upperLimitOffset));
        } else {
            return false;
        }
    }

    public int getLimitSource() {
        return limitWidget;
    }

    public Double getLowerLimitOffset() {
        return lowerLimitOffset;
    }

    public int getLimitWidget() {
        return limitWidget;
    }

    public void setLimitWidget(int limitWidget) {
        this.limitWidget = limitWidget;
    }

    public void setLowerLimitOffset(Double lowerLimitOffset) {
        this.lowerLimitOffset = lowerLimitOffset;
    }

    public Double getUpperLimitOffset() {
        return upperLimitOffset;
    }

    public void setUpperLimitOffset(Double upperLimitOffset) {
        this.upperLimitOffset = upperLimitOffset;
    }

    public ObjectNode toJSON() {
        ObjectNode dataNode = JsonNodeFactory.instance.objectNode();
        if (mode == MODE.DYNAMIC) {
            dataNode.put("source", limitWidget);
            dataNode.put("lowerLimitOffset", lowerLimitOffset);
            dataNode.put("upperLimitOffset", upperLimitOffset);
        } else if (mode == MODE.STATIC) {
            dataNode.put("lowerLimitValue", lowerLimit);
            dataNode.put("upperLimitValue", upperLimit);
        }

        dataNode.put("lowerLimit", isHasLowerLimit());
        dataNode.put("upperLimit", isHasUpperLimit());
        dataNode.put("lowerLimitColor", lowerLimitColor.toString());
        dataNode.put("upperLimitColor", upperLimitColor.toString());

//        System.out.println("Limit json: " + dataNode);
        return dataNode;
    }

    public Double getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(Double lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public Double getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(Double upperLimit) {
        this.upperLimit = upperLimit;
    }

    public boolean isHasLowerLimit() {
        return hasLowerLimit;
    }

    public void setHasLowerLimit(boolean hasLowerLimit) {
        this.hasLowerLimit = hasLowerLimit;
    }

    public boolean isHasUpperLimit() {
        return hasUpperLimit;
    }

    public void setHasUpperLimit(boolean hasUpperLimit) {
        this.hasUpperLimit = hasUpperLimit;
    }

    public Color getUpperLimitColor() {
        return upperLimitColor;
    }

    public void setUpperLimitColor(Color upperLimitColor) {
        this.upperLimitColor = upperLimitColor;
    }

    public Color getLowerLimitColor() {
        return lowerLimitColor;
    }

    public void setLowerLimitColor(Color lowerLimitColor) {
        this.lowerLimitColor = lowerLimitColor;
    }


    @Override
    public String toString() {
        return "Limit{" +
                "lowerLimit=" + lowerLimit +
                ", upperLimit=" + upperLimit +
                ", lowerLimitOffset=" + lowerLimitOffset +
                ", upperLimitOffset=" + upperLimitOffset +
                ", hasLowerLimit=" + hasLowerLimit +
                ", hasUpperLimit=" + hasUpperLimit +
                ", upperLimitColor=" + upperLimitColor +
                ", lowerLimitColor=" + lowerLimitColor +
                ", limitWidget=" + limitWidget +
                '}';
    }
}