package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;

public class Percent {

    private static final Logger logger = LogManager.getLogger(Percent.class);
    final DashboardControl dashboardControl;
    int percentWidget = -1;
    private int minFracDigits = 0;
    private int maxFracDigits = 0;
    private boolean diff = false;

    public Percent(DashboardControl control) {
        this(control, null);
    }

    public Percent(DashboardControl control, JsonNode jsonNode) {
        this.dashboardControl = control;

        if (jsonNode != null) {
            percentWidget = jsonNode.get("source").asInt(-1);
            try {
                minFracDigits = jsonNode.get("minFracDigits").asInt(0);
                maxFracDigits = jsonNode.get("maxFracDigits").asInt(0);
            } catch (Exception ignored) {
            }

            try {
                diff = jsonNode.get("diff").asBoolean(false);
            } catch (Exception ignored) {
            }
        }
    }

    public int getPercentWidgetID() {
        return percentWidget;
    }

    public Tab getConfigTab() {
        PercentTab tab = new PercentTab(I18n.getInstance().getString("plugin.dashboard.valuewidget.percent.tab")
                , this);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setVgap(8);
        gridPane.setHgap(8);


        AnchorPane editorPane = new AnchorPane();
        PercentPane percentPane = new PercentPane(dashboardControl, this, dashboardControl.getWidgetList());

        editorPane.getChildren().add(percentPane);

        gridPane.add(new Separator(Orientation.HORIZONTAL), 0, 1, 2, 1);
        gridPane.add(editorPane, 0, 2, 2, 1);


        tab.setContent(gridPane);
        return tab;
    }

    public int getLimitSource() {
        return percentWidget;
    }

    public int getPercentWidget() {
        return percentWidget;
    }

    public void setPercentWidget(int limitWidget) {
        this.percentWidget = limitWidget;
    }

    public int getMinFracDigits() {
        return minFracDigits;
    }

    public void setMinFracDigits(int minFracDigits) {
        this.minFracDigits = minFracDigits;
    }

    public int getMaxFracDigits() {
        return maxFracDigits;
    }

    public void setMaxFracDigits(int maxFracDigits) {
        this.maxFracDigits = maxFracDigits;
    }

    public boolean isDiff() {
        return diff;
    }

    public void setDiff(boolean diff) {
        this.diff = diff;
    }

    public ObjectNode toJSON() {
        ObjectNode dataNode = JsonNodeFactory.instance.objectNode();
        dataNode.put("source", percentWidget);
        dataNode.put("minFracDigits", minFracDigits);
        dataNode.put("maxFracDigits", maxFracDigits);
        dataNode.put("diff", diff);

        return dataNode;
    }

    @Override
    public String toString() {
        return "Percent{" +
                "percentWidget=" + percentWidget +
                "; minFracDigits=" + minFracDigits +
                "; maxFracDigits=" + maxFracDigits +
                "; diff=" + diff +
                '}';
    }

    private class PercentTab extends Tab implements ConfigTab {
        Percent percent;

        public PercentTab(String text, Percent limit) {
            super(text);
            this.percent = limit;
        }

        @Override
        public void commitChanges() {
            //TODO;
        }
    }
}