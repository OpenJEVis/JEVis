package org.jevis.jecc.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.text.Font;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.application.Chart.data.ChartDataRow;
import org.jevis.jecc.plugin.dashboard.DashboardControl;
import org.jevis.jecc.plugin.dashboard.config2.*;
import org.jevis.jecc.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jecc.tool.Layouts;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TimeFrameWidget extends Widget {

    private static final Logger logger = LogManager.getLogger(TimeFrameWidget.class);
    public static String WIDGET_ID = "Time Frame";
    public static String DISPLAY_NAME = I18n.getInstance().getString("plugin.dashboard.widget.timeframe");
    private final Label label = new Label();

    private DataModelDataHandler sampleHandler;
    public static String TIME_FRAME_DESIGN_NODE_NAME = "Time Frame";
    private final AnchorPane anchorPane = new AnchorPane();

    private TimeFramePojo timeFramePojo;
    private Tab timeFrameTabe;

    public TimeFrameWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
        this.setId(WIDGET_ID);
    }

    @Override
    public void debug() {

    }

    private String createTimeString(DateTimeFormatter dateTimeFormatter, List<DateTime> dateTimes) {
        if (!dateTimes.isEmpty() && dateTimes.size() > 0) {
            return dateTimeFormatter.print(dateTimes.get(0));
        } else return "";
    }

    @Override
    public void updateData(Interval interval) {
        System.out.println(interval);

        this.sampleHandler.setAutoAggregation(true);
        this.sampleHandler.setInterval(interval);
//            setIntervalForLastValue(interval);
        this.sampleHandler.update();


        if (sampleHandler.getDataModel().size() != 0) {
            sampleHandler.getDataModel().get(0).setCustomWorkDay(true);
            Platform.runLater(() -> {
                timeFramePojo.getWidgetObjects().forEach(timeFrameWidgetObject -> {
                    timeFrameWidgetObject.setSelected(false);
                    timeFrameWidgetObject.setStartObjectProperty(TimeFrameWidgetObject.Start.NONE);
                    timeFrameWidgetObject.setEndObjectProperty(TimeFrameWidgetObject.End.NONE);
                });
                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(timeFramePojo.getParser());
                this.label.setText(createTimeString(dateTimeFormatter, this.getMaxTimeStamps()));
            });
            return;

        }


        try {
            System.out.println(this.sampleHandler.getMaxTimeStamps());


        } catch (Exception e) {
            e.printStackTrace();
        }
        Platform.runLater(() -> {
            if (!this.timeFramePojo.getSelectedWidget().isPresent()) {
            } else {
                try {
                    logger.debug(this.timeFramePojo.getSelectedWidget().get().getCurrentInterval(control.getInterval()));
                    setLabelText();
                } catch (Exception e) {
                    logger.error(e);
                }
            }

        });


    }

    private void setLabelText() {
        if (this.timeFramePojo.getSelectedTimeFarmeObjectWidget().isPresent()) {

            if (this.timeFramePojo.getSelectedTimeFarmeObjectWidget().get().getCountOfSamples()) {
                Platform.runLater(() -> this.label.setText(getSmapleCount()));

            } else {
                Platform.runLater(() -> this.label.setText(convertIntervalToString(getStart(), getEnd())));
            }
        }
    }

    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.titlewidget.newname"));
        widgetPojo.setType(typeID());
        widgetPojo.setSize(new Size(control.getActiveDashboard().yGridInterval * 1, control.getActiveDashboard().xGridInterval * 12));

        return widgetPojo;
    }


    private DateTime getEnd() {

        DateTime dateTime = new DateTime();
        try {
            switch (this.timeFramePojo.getSelectedTimeFarmeObjectWidget().orElseThrow(RuntimeException::new).getEndObjectProperty()) {
                case NONE:
                    dateTime = null;
                    break;
                case LAST_TS:
                    Widget selcted = this.timeFramePojo.getSelectedWidget().orElseThrow(RuntimeException::new);
                    dateTime = !selcted.sampleHandler.getMaxTimeStamps().isEmpty() ? selcted.sampleHandler.getMaxTimeStamps().get(0) : null;
                    break;
                case PERIODE_UNTIL:
                    dateTime = this.timeFramePojo.getSelectedWidget().orElseThrow(RuntimeException::new)
                            .getCurrentInterval(control.getInterval()).getEnd();
                    break;

            }
        } catch (RuntimeException e) {
            dateTime = null;
            logger.error(e);
        } catch (Exception ex) {
            dateTime = null;
            logger.error(ex);
        }


        return dateTime;

    }

    private DateTime getStart() {
        DateTime dateTime = new DateTime();
        try {
            switch (this.timeFramePojo.getSelectedTimeFarmeObjectWidget().orElseThrow(RuntimeException::new).getStartObjectProperty()) {
                case NONE:
                    dateTime = null;
                    break;
                case PERIODE_FROM:
                    dateTime = this.timeFramePojo.getSelectedWidget().orElseThrow(RuntimeException::new).getCurrentInterval(control.getInterval()).getStart();
                    break;
            }
        } catch (RuntimeException e) {
            logger.error(e);
            dateTime = null;
        } catch (Exception ex) {
            logger.error(ex);
            dateTime = null;
        }

        return dateTime;
    }

    private String convertIntervalToString(DateTime start, DateTime end) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(timeFramePojo.getParser());
        StringBuilder stringBuilder = new StringBuilder();
        try {
            if (start != null) {
                stringBuilder.append(dateTimeFormatter.print(start));
            }
            if (start != null && end != null) {
                stringBuilder.append(" - ");
            }
            if (end != null) {
                stringBuilder.append(dateTimeFormatter.print(end));
            }

        } catch (IllegalArgumentException e) {
            logger.error(e);
            return null;
        }

        return stringBuilder.toString();
    }

    private String getSmapleCount() {
        try {
            ChartDataRow dataModel = this.timeFramePojo.getSelectedWidget().
                    orElseThrow(RuntimeException::new).sampleHandler.getDataModel().get(0);
            return String.valueOf(dataModel.getSamples().size());

        } catch (Exception e) {
            logger.error(e);
        }
        return "0";
    }

    @Override
    public void updateLayout() {


    }


    @Override
    public void updateConfig() {
        logger.debug("UpdateConfig");
        Platform.runLater(() -> {
            try {
                Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
                this.label.setBackground(bgColor);
                this.label.setTextFill(this.config.getFontColor());
                Font font = Font.font(this.label.getFont().getFamily(), this.getConfig().getFontWeight(), this.getConfig().getFontPosture(), this.config.getFontSize());
                this.label.setFont(font);
                this.label.setUnderline(this.getConfig().getFontUnderlined());
                this.label.setPrefWidth(this.config.getSize().getWidth());
                this.label.setAlignment(this.config.getTitlePosition());
                System.out.println(this.timeFramePojo.getSelectedWidget());
                this.timeFramePojo.getSelectedWidget().orElseThrow(RuntimeException::new);

                if (this.timeFramePojo.getSelectedWidget().isPresent()) {
                    this.timeFramePojo.getSelectedWidget().get().sampleHandler.addEventListener(event -> {
                        logger.info("{} fired  Update {}", event, this);
                        setLabelText();
                    });
                }


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
        List<DateTime> dateTimes = new ArrayList<>();
        for (ChartDataRow chartDataRow : this.sampleHandler.getDataModel()) {
            try {
                JEVisSample samples = chartDataRow.getObject().getAttribute("Value").getLatestSample();
                dateTimes.add(samples.getTimestamp());
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
        return dateTimes;
    }


    @Override
    public void init() {

        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.control, this.config, WIDGET_ID);

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

        sampleHandler.setAutoAggregation(true);

        Tab timeFrameTab = timeFramePojo.getConfigTab();

        if (this.sampleHandler.getDataModel().size() != 0) {
            timeFramePojo.getTimeFrameTable().setDisable(true);
        }

        WidgetConfigDialog widgetConfigDialog = new WidgetConfigDialog(this);

        widgetConfigDialog.addGeneralTabsDataModel(this.sampleHandler);

        if (timeFramePojo != null) {
            widgetConfigDialog.addTab(timeFrameTab);
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
        dashBoardNode.set(JsonNames.Widget.DATA_HANDLER_NODE, this.sampleHandler.toJsonNode());
        if (timeFramePojo != null) {
            dashBoardNode
                    .set(TIME_FRAME_DESIGN_NODE_NAME, timeFramePojo.toJSON());
        }
        System.out.println(dashBoardNode);
        return dashBoardNode;
    }

    @Override
    public ImageView getImagePreview() {
        return ControlCenter.getImage("iconfinder_calendar-clock_299096.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }
}
