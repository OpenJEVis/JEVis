package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TimeFramePojo {


    private static final Logger logger = LogManager.getLogger(TimeFramePojo.class);

    private final double iconSize = 20;


    public TimeFrameTableView getTimeFrameTable() {
        return timeFrameTable;
    }

    private TimeFrameTableView timeFrameTable;


    int timeFrameWidgetID = -1;


    final DashboardControl dashboardControl;

    final Region lockIcon = JEConfig.getSVGImage(Icon.VISIBILITY_ON, this.iconSize, this.iconSize);
    final Region unlockIcon = JEConfig.getSVGImage(Icon.VISIBILITY_OFF, this.iconSize, this.iconSize);

    private JFXTextField jfxTextFieldParser;

//    private Integer selectedWidgetId;
//    private Widget selectedWidget;

    public ObservableList<TimeFrameWidgetObject> getWidgetObjects() {
        return widgetObjects;
    }

    public void setWidgetObjects(ObservableList<TimeFrameWidgetObject> widgetObjects) {
        this.widgetObjects = widgetObjects;
    }

    ObservableList<TimeFrameWidgetObject> widgetObjects = FXCollections.observableArrayList();

    private String parser = "yyyy.MM.dd HH:mm";

    private String start = "";

    private String end = "";

    private int selectedId = -1;

    private boolean countOfSamples = false;

    public TimeFramePojo(DashboardControl control) {
        this(control, null);
    }

    public TimeFramePojo(DashboardControl control, JsonNode jsonNode) {
        this.dashboardControl = control;
        //widgetObjects.addAll(dashboardControl.getWidgetList().stream().map(widget -> new TimeFrameWidgetObject(dashboardControl, widget.getConfig())).collect(Collectors.toList()));


        if (jsonNode != null) {
            if (jsonNode.has("selectedWidget")) {
                selectedId = jsonNode.get("selectedWidget").get("id").asInt();
                start = jsonNode.get("selectedWidget").get("start").asText();
                end = jsonNode.get("selectedWidget").get("end").asText();
                countOfSamples = jsonNode.get("selectedWidget").get("isCount").asBoolean(false);
            }
            if (jsonNode.has("format")) {
                parser = jsonNode.get("format").asText("yyyy.MM.dd HH:mm");
            }


        }

    }

    public Optional<TimeFrameWidgetObject>  getSelectedTimeFarmeObjectWidget() {
        Optional<TimeFrameWidgetObject> optionalTimeFrameWidgetObject = widgetObjects.stream().filter(timeFrameWidgetObject -> timeFrameWidgetObject.isSelected()).findFirst();
        return optionalTimeFrameWidgetObject;
    }

    public Optional<Widget> getSelectedWidget() {
        addWidgets();
        try {
                Optional<Widget> widget = this.dashboardControl.getWidgets().stream()
                        .filter(widget1 -> widget1.config.getUuid() == getSelectedTimeFarmeObjectWidget().orElseThrow(RuntimeException::new).getConfig().getUuid()).findFirst();
                return widget;
        } catch (Exception e) {
            logger.error(e);
            return Optional.empty();
        }


    }


    public String getParser() {
        return parser;
    }

    public void setParser(String parser) {
        this.parser = parser;
    }

    @Override
    public String toString() {
        return "TimeFramePojo{" +
                "iconSize=" + iconSize +
                ", timeFrameColumnFactory=" + timeFrameTable +
                ", timeFrameWidgetID=" + timeFrameWidgetID +
                ", dashboardControl=" + dashboardControl +
                ", lockIcon=" + lockIcon +
                ", unlockIcon=" + unlockIcon +
                ", jfxTextFieldParser=" + jfxTextFieldParser +
                ", parser='" + parser + '\'' +
                '}';
    }


    private class GaugeDesignTab extends Tab implements ConfigTab {
        TimeFramePojo timeFrameDesign;

        public GaugeDesignTab(String text, TimeFramePojo timeFramePojo) {
            super(text);
            this.timeFrameDesign = timeFramePojo;
        }


        @Override
        public void commitChanges() {

            //logger.debug("Selected Widget:",timeFrameColumnFactory.getSelectedWidget());
            //setSelectedWidget(timeFrameColumnFactory.getSelectedWidget());
            parser = jfxTextFieldParser.getText();


        }
    }

    public Tab getConfigTab() {

        GaugeDesignTab tab = new GaugeDesignTab(I18n.getInstance().getString("plugin.dashboard.timeframe")
                , this);

        GridPane gridPane = new GridPane();
        ColumnConstraints column1 = new ColumnConstraints();
        ColumnConstraints column2 = new ColumnConstraints();
        ColumnConstraints column3 = new ColumnConstraints();
        column3.setHgrow(Priority.ALWAYS);

        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();
        RowConstraints row3 = new RowConstraints();
        row3.setVgrow(Priority.ALWAYS);

        gridPane.getColumnConstraints().addAll(column1, column2, column3);
        gridPane.getRowConstraints().addAll(row1, row2, row3);
        gridPane.setVgap(8);
        gridPane.setHgap(8);
        gridPane.setPadding(new Insets(8, 5, 8, 5));


        jfxTextFieldParser = new JFXTextField(parser);
        jfxTextFieldParser.setPrefWidth(160d);

        gridPane.addRow(0, new Label(I18n.getInstance().getString("plugin.dashboard.timeframe.format")), jfxTextFieldParser);


        addWidgets();


        timeFrameTable = new TimeFrameTableView(widgetObjects);
        gridPane.add(timeFrameTable, 0, 2, 3, 1);
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
        dataNode.put("format", parser);
        logger.debug(dataNode);
        Optional<TimeFrameWidgetObject> selectedWidget = getSelectedTimeFarmeObjectWidget();
        if (selectedWidget.isPresent()) {
            start = selectedWidget.get().getStartObjectProperty().toString();
            end = selectedWidget.get().getEndObjectProperty().toString();
            selectedId =selectedWidget.get().getConfig().getUuid();
            countOfSamples =getSelectedTimeFarmeObjectWidget().get().countOfSamplesProperty().getValue();
        }
            ObjectNode dataNode1 = dataNode.putObject("selectedWidget");
            dataNode1.put("start", start);
            dataNode1.put("end", end);
            dataNode1.put("id", selectedId);
            dataNode1.put("isCount", countOfSamples);



        return dataNode;
    }

    private void addWidgets() {

        try{
            List<TimeFrameWidgetObject> allWidgets = dashboardControl.getWidgetList().stream().map(widget -> new TimeFrameWidgetObject(dashboardControl, widget.getConfig())).collect(Collectors.toList());
            widgetObjects.removeAll(widgetObjects.stream().filter(timeFrameWidgetObject -> !allWidgets.contains(timeFrameWidgetObject)).collect(Collectors.toList()));
            widgetObjects.addAll(allWidgets.stream().filter(timeFrameWidgetObject -> !widgetObjects.contains(timeFrameWidgetObject)).collect(Collectors.toList()));
            if (widgetObjects.stream().filter(timeFrameWidgetObject -> timeFrameWidgetObject.isSelected()).count() == 0) {
                Optional<TimeFrameWidgetObject> optionalTimeFrameWidgetObject = widgetObjects.stream().filter(timeFrameWidgetObject -> timeFrameWidgetObject.getConfig().getUuid() == selectedId).findFirst();
                TimeFrameWidgetObject selected = optionalTimeFrameWidgetObject.orElseThrow(RuntimeException::new);
                selected.setSelected(true);
                selected.setEndObjectProperty(TimeFrameWidgetObject.End.valueOf(end));
                selected.setStartObjectProperty(TimeFrameWidgetObject.Start.valueOf(start));
                selected.setCountOfSamples(countOfSamples);
            }
        }catch (Exception e){
            logger.error(e);
        }



    }

}