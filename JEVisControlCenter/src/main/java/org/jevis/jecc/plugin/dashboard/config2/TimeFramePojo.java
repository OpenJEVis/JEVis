package org.jevis.jecc.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.Icon;
import org.jevis.jecc.plugin.dashboard.DashboardControl;
import org.jevis.jecc.plugin.dashboard.widget.Widget;

import java.util.Optional;

public class TimeFramePojo {


    private static final Logger logger = LogManager.getLogger(TimeFramePojo.class);
    final DashboardControl dashboardControl;
    int timeFrameWidgetID = -1;
    private double iconSize = 20;
    final Region lockIcon = ControlCenter.getSVGImage(Icon.VISIBILITY_ON, this.iconSize, this.iconSize);
    final Region unlockIcon = ControlCenter.getSVGImage(Icon.VISIBILITY_OFF, this.iconSize, this.iconSize);
    private TimeFrameColumnFactory timeFrameColumnFactory;
    private MFXTextField MFXTextFieldParser;

    private Integer selectedWidgetId;
    private Widget selectedWidget;

    private String parser = "yyyy.MM.dd HH:mm";

    public TimeFramePojo(DashboardControl control) {
        this(control, null);
    }

    public TimeFramePojo(DashboardControl control, JsonNode jsonNode) {
        this.dashboardControl = control;
        if (jsonNode != null) {
            if (jsonNode.has("selectedWidget")) {
                selectedWidgetId = jsonNode.get("selectedWidget").asInt();
            }
            if (jsonNode.has("format")) {
                parser = jsonNode.get("format").asText("yyyy.MM.dd HH:mm");
            }


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
            logger.debug(selectedWidgetId);
            logger.debug(dashboardControl.getWidgets());
            Optional<Widget> widget = dashboardControl.getWidgets().stream().filter(widget2 -> widget2.getConfig().getUuid() == selectedWidgetId).findAny();
            if (widget.isPresent()) {
                selectedWidget = widget.get();
                logger.debug(selectedWidget);
            }
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
                ", timeFrameColumnFactory=" + timeFrameColumnFactory +
                ", timeFrameWidgetID=" + timeFrameWidgetID +
                ", dashboardControl=" + dashboardControl +
                ", lockIcon=" + lockIcon +
                ", unlockIcon=" + unlockIcon +
                ", MFXTextFieldParser=" + MFXTextFieldParser +
                ", selectedWidgetId=" + selectedWidgetId +
                ", selectedWidget=" + selectedWidget +
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

        MFXTextFieldParser = new MFXTextField(parser);
        MFXTextFieldParser.setPrefWidth(160d);

        gridPane.addRow(0, new Label(I18n.getInstance().getString("plugin.dashboard.timeframe.format")), MFXTextFieldParser);

        gridPane.addRow(1, highlightButton);

        timeFrameColumnFactory = new TimeFrameColumnFactory(dashboardControl);
        gridPane.add(timeFrameColumnFactory.buildTable(dashboardControl.getWidgetList()), 0, 2, 3, 1);
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
        logger.debug(dataNode);


        return dataNode;
    }

    private class GaugeDesignTab extends Tab implements ConfigTab {
        TimeFramePojo timeFrameDesign;

        public GaugeDesignTab(String text, TimeFramePojo timeFramePojo) {
            super(text);
            this.timeFrameDesign = timeFramePojo;
        }


        @Override
        public void commitChanges() {

            logger.debug("Selected Widget:", timeFrameColumnFactory.getSelectedWidget());
            setSelectedWidget(timeFrameColumnFactory.getSelectedWidget());
            parser = MFXTextFieldParser.getText();


        }
    }

}