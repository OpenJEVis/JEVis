package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.text.Font;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config2.*;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.tool.Layouts;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TimeFrameWidget extends Widget {

    private static final Logger logger = LogManager.getLogger(TimeFrameWidget.class);
    public static String WIDGET_ID = "Time Frame";
    public static String DISPLAY_NAME = I18n.getInstance().getString("plugin.dashboard.widget.timeframe");
    private final Label label = new Label();

    private DataModelDataHandler sampleHandler;
    private AnchorPane anchorPane = new AnchorPane();
    public static String TIME_FRAME_DESIGN_NODE_NAME = "Time Frame";

    private TimeFramePojo timeFramePojo;

    public TimeFrameWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
        this.setId(WIDGET_ID);
    }

    @Override
    public void debug() {

    }


    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.titlewidget.newname"));
        widgetPojo.setType(typeID());
        widgetPojo.setSize(new Size(control.getActiveDashboard().yGridInterval * 1, control.getActiveDashboard().xGridInterval * 12));

        return widgetPojo;
    }


    @Override
    public void updateData(Interval interval) {
        Platform.runLater(() -> {
            try{
                logger.debug(this.timeFramePojo.getSelectedWidget().getCurrentInterval(control.getInterval()));
                this.label.setText(convertIntervalToString(this.timeFramePojo.getSelectedWidget().getCurrentInterval(control.getInterval())));

            }catch (Exception e){
                logger.error(e);
            }
        });




    }

    private String convertIntervalToString(Interval interval) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(timeFramePojo.getParser());
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append(dateTimeFormatter.print(interval.getStart()));
            stringBuilder.append(" - ");
            stringBuilder.append(dateTimeFormatter.print(interval.getEnd()));
        }catch (IllegalArgumentException e){
            logger.error(e);
            return null;
        }

        return stringBuilder.toString();
    }

    @Override
    public void updateLayout() {


    }

    @Override
    public void updateConfig() {
        logger.debug("UpdateConfig");

        timeFramePojo.selectWidget();


        Platform.runLater(() -> {
            try {


                Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
                this.label.setBackground(bgColor);
                this.label.setTextFill(this.config.getFontColor());
                this.label.setFont(new Font(this.config.getFontSize()));
                this.label.setPrefWidth(this.config.getSize().getWidth());
                this.label.setAlignment(this.config.getTitlePosition());
            } catch (Exception ex) {
                logger.error(ex);
            }
        });
    }

    @Override
    public boolean isStatic() {
        return false;
    }


    @Override
    public List<DateTime> getMaxTimeStamps() {
        return new ArrayList<>();
    }


    @Override
    public void init() {
        this.label.setPadding(new Insets(0, 8, 0, 8));
        anchorPane.setBackground(null);
        anchorPane.getChildren().add(this.label);
        Layouts.setAnchor(this.label, 0);

        try {
            this.timeFramePojo = new TimeFramePojo(this.control, this.config.getConfigNode(TIME_FRAME_DESIGN_NODE_NAME));
        } catch (Exception e) {
            e.printStackTrace();
        }

        setGraphic(anchorPane);
        if (this.timeFramePojo == null) {
            this.timeFramePojo = new TimeFramePojo(this.control);
        }
    }


    @Override
    public void openConfig() {
        WidgetConfigDialog widgetConfigDialog = new WidgetConfigDialog(this);
        widgetConfigDialog.addGeneralTabsDataModel(null);

        if (timeFramePojo != null) {
            widgetConfigDialog.addTab(timeFramePojo.getConfigTab());
        }

        Optional<ButtonType> result = widgetConfigDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                widgetConfigDialog.commitSettings();
                control.updateWidget(this);
                updateConfig();
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }

    @Override
    public String typeID() {
        return WIDGET_ID;
    }


    @Override
    public ObjectNode toNode() {
        ObjectNode dashBoardNode = super.createDefaultNode();
        if (timeFramePojo != null) {
            dashBoardNode
                    .set(TIME_FRAME_DESIGN_NODE_NAME, timeFramePojo.toJSON());
        }
        return dashBoardNode;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("iconfinder_calendar-clock_299096.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }
}
