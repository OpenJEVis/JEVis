package org.jevis.jecc.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.plugin.dashboard.DashboardControl;

import java.util.ArrayList;
import java.util.List;

public class IncrementPojo {


    private static final Logger logger = LogManager.getLogger(IncrementPojo.class);
    final DashboardControl dashboardControl;
    private final double iconSize = 20;
    private final ActionEvent actionEvent = new ActionEvent();
    int gaugeWidgetID = -1;
    private double increment = 0.1;
    private List<GaugeSectionPojo> sections = new ArrayList();
    private TextField minTextField;

    private TextField maxTextField;

    private TextField incrementTextField;


    private GridPane gridPane;


    public IncrementPojo(DashboardControl control) {
        this(control, null);
    }

    public IncrementPojo(DashboardControl control, JsonNode jsonNode) {
        this.dashboardControl = control;

        if (jsonNode != null) {
            if (jsonNode.has("Increment")) {
                increment = jsonNode.get("Increment").asDouble(0.1);
            }


        }


    }

    public int getLimitWidgetID() {
        return gaugeWidgetID;
    }

    public List<GaugeSectionPojo> getSections() {
        return sections;
    }

    public void setSections(List<GaugeSectionPojo> sections) {
        this.sections = sections;
    }


    public Tab getConfigTab() {

        GaugeDesignTab tab = new GaugeDesignTab(I18n.getInstance().getString("plugin.dashboard.increment")
                , this);
        gridPane = new GridPane();
        ColumnConstraints column1 = new ColumnConstraints();
        ColumnConstraints column2 = new ColumnConstraints();
        ColumnConstraints column3 = new ColumnConstraints();
        column3.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().addAll(column1, column2, column3);
        gridPane.setVgap(8);
        gridPane.setHgap(8);

        gridPane.setPadding(new Insets(8, 5, 8, 5));
        incrementTextField = new TextField(String.valueOf(increment));


        gridPane.addRow(0, new Label(I18n.getInstance().getString("plugin.dashboard.increment")), incrementTextField);


        tab.setContent(gridPane);
        return tab;
    }

    public ObjectNode toJSON() {
        ObjectNode dataNode = JsonNodeFactory.instance.objectNode();

        dataNode.put("Increment", increment);

        return dataNode;
    }


    public int getGaugeWidgetID() {
        return gaugeWidgetID;
    }

    public void setGaugeWidgetID(int gaugeWidgetID) {
        this.gaugeWidgetID = gaugeWidgetID;
    }

    @Override
    public String toString() {
        return "MinMax{" +
                "iconSize=" + iconSize +
                ", gaugeWidgetID=" + gaugeWidgetID +
                ", sections=" + sections +
                ", dashboardControl=" + dashboardControl +
                ", actionEvent=" + actionEvent +
                ", minTextField=" + minTextField +
                ", maxTextField=" + maxTextField +
                ", gridPane=" + gridPane +
                '}';
    }

    public double getIncrement() {
        return increment;
    }

    public void setIncrement(double increment) {
        this.increment = increment;
    }

    private class GaugeDesignTab extends Tab implements ConfigTab {
        IncrementPojo gaugeDesign;

        public GaugeDesignTab(String text, IncrementPojo gaugeDesign) {
            super(text);
            this.gaugeDesign = gaugeDesign;
        }

        @Override
        public void commitChanges() {
            try {
                increment = Double.parseDouble(incrementTextField.getText());

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

}