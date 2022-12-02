package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;

import java.util.ArrayList;
import java.util.List;

public class MinMaxPojo {


    private static final Logger logger = LogManager.getLogger(MinMaxPojo.class);

    private final double iconSize = 20;

    int gaugeWidgetID = -1;

    private double maximum = 0;

    private double minimum = 0;



    private List<GaugeSectionPojo> sections = new ArrayList();

    final DashboardControl dashboardControl;


    private final ActionEvent actionEvent = new ActionEvent();


    private JFXTextField minTextField;

    private JFXTextField maxTextField;



    private GridPane gridPane;

    public Double getMaximum() {
        return maximum;
    }

    public void setMaximum(Double maximum) {
        this.maximum = maximum;
    }

    public Double getMinimum() {
        return minimum;
    }

    public void setMinimum(Double minimum) {
        this.minimum = minimum;
    }


    public MinMaxPojo(DashboardControl control) {
        this(control, null);
    }

    public int getLimitWidgetID() {
        return gaugeWidgetID;
    }

    public MinMaxPojo(DashboardControl control, JsonNode jsonNode) {
        this.dashboardControl = control;

        if (jsonNode != null) {

            if (jsonNode.has("maximum")) {
                maximum = jsonNode.get("maximum").asDouble();
            }
            if (jsonNode.has("minimum")) {
                minimum = jsonNode.get("minimum").asDouble();
            }


        }


    }


    public List<GaugeSectionPojo> getSections() {
        return sections;
    }

    public void setSections(List<GaugeSectionPojo> sections) {
        this.sections = sections;
    }


    public Tab getConfigTab() {

        GaugeDesignTab tab = new GaugeDesignTab(I18n.getInstance().getString("plugin.dashboard.minmax.tab")
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
        minTextField = new JFXTextField(String.valueOf(minimum));
        maxTextField = new JFXTextField(String.valueOf(maximum));

        gridPane.addRow(0,new Label(I18n.getInstance().getString("plugin.dashboard.min")),minTextField);
        gridPane.addRow(1,new Label(I18n.getInstance().getString("plugin.dashboard.max")),maxTextField);



        tab.setContent(gridPane);
        return tab;
    }

    public ObjectNode toJSON() {
        ObjectNode dataNode = JsonNodeFactory.instance.objectNode();

        dataNode.put("minimum", minimum);
        dataNode.put("maximum", maximum);

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
                ", maximum=" + maximum +
                ", minimum=" + minimum +
                ", sections=" + sections +
                ", dashboardControl=" + dashboardControl +
                ", actionEvent=" + actionEvent +
                ", minTextField=" + minTextField +
                ", maxTextField=" + maxTextField +
                ", gridPane=" + gridPane +
                '}';
    }

    private class GaugeDesignTab extends Tab implements ConfigTab {
        MinMaxPojo gaugeDesign;

        public GaugeDesignTab(String text, MinMaxPojo gaugeDesign) {
            super(text);
            this.gaugeDesign = gaugeDesign;
        }

        @Override
        public void commitChanges() {
            try {
                minimum = Double.parseDouble(minTextField.getText());
                maximum = Double.parseDouble(maxTextField.getText());

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

}