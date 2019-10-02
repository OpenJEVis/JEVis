package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.tool.I18n;

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
            boolean isDynamic = (jsonNode.get("source").asInt(-1) > 0);
            if (isDynamic) {
                mode = MODE.DYNAMIC;
                limitWidget = jsonNode.get("source").asInt(-1);
                lowerLimitOffset = jsonNode.get("lowerLimitOffset").asDouble(1);
                upperLimitOffset = jsonNode.get("upperLimitOffset").asDouble(1);
            } else {
                mode = MODE.STATIC;
                limitWidget = jsonNode.get("source").asInt(0);
                lowerLimit = jsonNode.get("lowerLimitValue").asDouble(Double.NaN);
                upperLimit = jsonNode.get("upperLimitValue").asDouble(Double.NaN);
            }

            hasLowerLimit = jsonNode.get("lowerLimit").asBoolean(false);
            hasUpperLimit = jsonNode.get("upperLimit").asBoolean(false);

            lowerLimitColor = Color.valueOf(jsonNode.get("lowerLimitColor").asText(Color.RED.toString()));
            upperLimitColor = Color.valueOf(jsonNode.get("upperLimitColor").asText(Color.GREEN.toString()));
        }


    }


    public Tab getConfigTab() {
        Tab tab = new Tab(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.tab"));

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setVgap(8);
        gridPane.setHgap(8);


        AnchorPane editorPane = new AnchorPane();
        LimitDynamicPane limitDynamicPane = new LimitDynamicPane(this, dashboardControl.getWidgetList());
        LimitStaticPane limitStaticPane = new LimitStaticPane(this);


        Label limitTypeLabel = new Label(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.type"));
        ComboBox<MODE> limitTypeBox = new ComboBox<>(types);
        Callback<ListView<MODE>, ListCell<MODE>> cellFactory = new Callback<ListView<MODE>, ListCell<MODE>>() {
            @Override
            public ListCell<MODE> call(ListView<MODE> param) {
                return new ListCell<MODE>() {
                    @Override
                    protected void updateItem(MODE item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            switch (item) {
                                case STATIC:
                                    setText(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.type.static"));
                                    break;
                                case DYNAMIC:
                                    setText(I18n.getInstance().getString("plugin.dashboard.valuewidget.limit.type.dynamic"));
                                    break;
                            }

                        }
                    }
                };
            }
        };


        limitTypeBox.getSelectionModel().select(mode);
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
        if (limitWidget > 0) {
            dataNode.put("source", limitWidget);
            dataNode.put("lowerLimitOffset", lowerLimitOffset);
            dataNode.put("upperLimitOffset", upperLimitOffset);
        } else {
            dataNode.put("lowerLimitValue", lowerLimit);
            dataNode.put("upperLimitValue", upperLimit);
        }

        dataNode.put("lowerLimit", isHasLowerLimit());
        dataNode.put("upperLimit", isHasUpperLimit());
        dataNode.put("lowerLimitColor", lowerLimitColor.toString());
        dataNode.put("upperLimitColor", upperLimitColor.toString());


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