package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;

import java.util.Optional;

public class TimeFramePojo {


    private static final Logger logger = LogManager.getLogger(TimeFramePojo.class);

    private double iconSize = 20;


    private TimeFrameColumnFactory timeFrameColumnFactory;


    int timeFrameWidgetID = -1;



    final DashboardControl dashboardControl;

    final ImageView lockIcon = JEConfig.getImage("eye_visible.png", this.iconSize, this.iconSize);
    final ImageView unlockIcon = JEConfig.getImage("eye_hidden.png", this.iconSize, this.iconSize);

    private JFXTextField jfxTextFieldParser;

    private Integer selectedWidgetId;
    private Widget selectedWidget;

    private String parser = "yyyy.MM.dd HH:mm";

    public TimeFramePojo(DashboardControl control) {
        this(control, null);
    }

    public TimeFramePojo(DashboardControl control, JsonNode jsonNode) {
        this.dashboardControl = control;
        if (jsonNode != null) {
            selectedWidgetId = jsonNode.get("selectedWidget").asInt();
            parser = jsonNode.get("format").asText();
        }

    }

    public Widget getSelectedWidget() {
        return selectedWidget;
    }

    public void setSelectedWidget(Widget selectedWidget) {
        this.selectedWidget = selectedWidget;
    }

    public void selectWidget() {

        if (selectedWidget == null) {
            System.out.println(dashboardControl.getWidgets());
            Optional<Widget> widget =  dashboardControl.getWidgets().stream().filter(widget2 -> widget2.getConfig().getUuid() == selectedWidgetId).findAny();
            if (widget.isPresent()) {
                selectedWidget = widget.get();
            }
            System.out.println(selectedWidget);
        }





    }

    public String getParser() {
        return parser;
    }

    public void setParser(String parser) {
        this.parser = parser;
    }


    private class GaugeDesignTab extends Tab implements ConfigTab {
        TimeFramePojo timeFrameDesign;

        public GaugeDesignTab(String text, TimeFramePojo timeFramePojo) {
            super(text);
            this.timeFrameDesign = timeFramePojo;
        }


        @Override
        public void commitChanges() {

            System.out.println(timeFrameColumnFactory.getSelectedWidget());
            setSelectedWidget(timeFrameColumnFactory.getSelectedWidget());
            parser = jfxTextFieldParser.getText();


        }
    }

    public Tab getConfigTab() {

        GaugeDesignTab tab = new GaugeDesignTab(I18n.getInstance().getString("plugin.plugin.dashboard.timeframe")
                , this);

        GridPane gridPane = new GridPane();
        ColumnConstraints column1 = new ColumnConstraints();
        ColumnConstraints column2 = new ColumnConstraints();
        ColumnConstraints column3 = new ColumnConstraints();
        column3.setPercentWidth(50);

        gridPane.getColumnConstraints().addAll(column1, column2, column3);
        gridPane.setVgap(8);
        gridPane.setHgap(8);
        gridPane.setPadding(new Insets(8, 5, 8, 5));



        ToggleButton highlightButton = new ToggleButton("", this.unlockIcon);
        highlightButton.setOnAction(event -> {
            dashboardControl.enableHighlightGlow(highlightButton.isSelected());
        });

        highlightButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                highlightButton.setGraphic(this.lockIcon);
            } else {
                highlightButton.setGraphic(this.unlockIcon);
            }
        });

        jfxTextFieldParser = new JFXTextField(parser);
        jfxTextFieldParser.setPrefWidth(160d);

        gridPane.addRow(0,new Label(I18n.getInstance().getString("plugin.plugin.dashboard.timeframe.format")),jfxTextFieldParser);

        gridPane.addRow(1, highlightButton);

        timeFrameColumnFactory = new TimeFrameColumnFactory(dashboardControl);
        gridPane.add(timeFrameColumnFactory.buildTable(dashboardControl.getWidgetList()),0,2,3,2);
        Platform.runLater(() -> {
            timeFrameColumnFactory.setSelectedWidget(selectedWidget);
        });
        tab.setContent(gridPane);


        return tab;
    }


    public int getLimitSource() {
        return timeFrameWidgetID;
    }

    public int getTimeFrameWidgetID() {
        return timeFrameWidgetID;
    }

    public void setTimeFrameWidgetID(int timeFrameWidgetID) {
        this.timeFrameWidgetID = timeFrameWidgetID;
    }


    public ObjectNode toJSON() {
        ObjectNode dataNode = JsonNodeFactory.instance.objectNode();
        dataNode.put("selectedWidget", selectedWidget.getConfig().getUuid());
        dataNode.put("format", parser);

        System.out.println(dataNode);


        return dataNode;
    }

}