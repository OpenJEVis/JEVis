package org.jevis.jecc.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXTextField;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.Icon;
import org.jevis.jecc.plugin.dashboard.DashboardControl;
import org.jevis.jecc.plugin.dashboard.datahandler.DataModelWidget;
import org.jevis.jecc.plugin.dashboard.datahandler.SampleHandlerEventListener;
import org.jevis.jecc.plugin.dashboard.widget.TimeFrameWidget;
import org.jevis.jecc.plugin.dashboard.widget.Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TimeFramePojo {


    private static final Logger logger = LogManager.getLogger(TimeFramePojo.class);
    final DashboardControl dashboardControl;
    private final double iconSize = 20;
    final Region lockIcon = ControlCenter.getSVGImage(Icon.VISIBILITY_ON, this.iconSize, this.iconSize);
    final Region unlockIcon = ControlCenter.getSVGImage(Icon.VISIBILITY_OFF, this.iconSize, this.iconSize);
    int timeFrameWidgetID = -1;
    private final List<TimeFrameWidgetObject> widgetObjects = new ArrayList<>();
    private TimeFrameTableView timeFrameTable;
    private JFXTextField jfxTextFieldParser;

    //    private Integer selectedWidgetId;
//    private Widget selectedWidget;
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

    public TimeFrameTableView getTimeFrameTable() {
        return timeFrameTable;
    }

    public List<TimeFrameWidgetObject> getWidgetObjects() {
        return widgetObjects;
    }

    public Optional<DataModelWidget> getSelectedWidget() {
        try {
            Optional<DataModelWidget> widget = Optional.empty();
            for (Widget widget1 : this.dashboardControl.getWidgets()) {
                if (widget1.getConfig().getUuid() == selectedId) {
                    widget = Optional.of((DataModelWidget) widget1);
                    break;
                }
            }
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
        Optional<TimeFrameWidgetObject> selectedWidget = getTimeFrameWidgetObject();
        if (selectedWidget.isPresent()) {
            start = selectedWidget.get().getStartObjectProperty().toString();
            end = selectedWidget.get().getEndObjectProperty().toString();
            selectedId = selectedWidget.get().getConfig().getUuid();
            countOfSamples = selectedWidget.get().countOfSamplesProperty().getValue();
        }
        ObjectNode dataNode1 = dataNode.putObject("selectedWidget");
        dataNode1.put("start", start);
        dataNode1.put("end", end);
        dataNode1.put("id", selectedId);
        dataNode1.put("isCount", countOfSamples);


        return dataNode;
    }

    public void addWidgets() {

        try {
            widgetObjects.forEach(timeFrameWidgetObject -> {
                for (SampleHandlerEventListener sampleHandlerEventListener : timeFrameWidgetObject.getWidget().getDataHandler().getEventListener()) {
                    timeFrameWidgetObject.getWidget().getDataHandler().removeEventListener(sampleHandlerEventListener);
                }
            });
            widgetObjects.clear();

            for (Widget widget : dashboardControl.getWidgetList()) {
                if (!(widget instanceof TimeFrameWidget) && widget instanceof DataModelWidget) {
                    DataModelWidget dataModelWidget = (DataModelWidget) widget;
                    TimeFrameWidgetObject frameWidgetObject = new TimeFrameWidgetObject(dataModelWidget, widget.getConfig(), widget.getImagePreview());
                    widgetObjects.add(frameWidgetObject);
                }
            }

            Optional<TimeFrameWidgetObject> selected = getTimeFrameWidgetObject();
            if (selected.isPresent()) {
                selected.get().setSelected(true);
                selected.get().setEndObjectProperty(TimeFrameWidgetObject.End.valueOf(end));
                selected.get().setStartObjectProperty(TimeFrameWidgetObject.Start.valueOf(start));
                selected.get().setCountOfSamples(countOfSamples);
            }
        } catch (Exception e) {
            logger.error(e);
        }


    }

    public Optional<TimeFrameWidgetObject> getTimeFrameWidgetObject() {
        Optional<TimeFrameWidgetObject> optionalTimeFrameWidgetObject = Optional.empty();
        for (TimeFrameWidgetObject timeFrameWidgetObject : widgetObjects) {
            if (timeFrameWidgetObject.getConfig().getUuid() == selectedId) {
                optionalTimeFrameWidgetObject = Optional.of(timeFrameWidgetObject);
                break;
            }
        }

        return optionalTimeFrameWidgetObject;
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

}