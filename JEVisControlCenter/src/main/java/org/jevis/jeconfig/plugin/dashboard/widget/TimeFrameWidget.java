package org.jevis.jeconfig.plugin.dashboard.widget;

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
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config2.*;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelWidget;
import org.jevis.jeconfig.plugin.dashboard.datahandler.SampleHandlerEvent;
import org.jevis.jeconfig.tool.Layouts;
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
    public static String TIME_FRAME_DESIGN_NODE_NAME = "Time Frame";
    private final Label label = new Label();
    private final AnchorPane anchorPane = new AnchorPane();
    private DataModelDataHandler sampleHandler;
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
        logger.debug(interval);

        this.timeFramePojo.addWidgets();

        if (this.timeFramePojo.getSelectedWidget().isPresent()) {
            this.timeFramePojo.getSelectedWidget().get().getDataHandler().removeEventListener(this::valueChanged);
        }

        this.sampleHandler.setAutoAggregation(true);
        this.sampleHandler.update(interval);

        if (!sampleHandler.getChartDataRows().isEmpty()) {
            sampleHandler.getChartDataRows().get(0).setCustomWorkDay(true);
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

        if (this.timeFramePojo.getSelectedWidget().isPresent()) {
            try {
                Platform.runLater(this::setLabelText);
            } catch (Exception e) {
                logger.error(e);
            }
        }

        if (this.timeFramePojo.getSelectedWidget().isPresent()) {
            this.timeFramePojo.getSelectedWidget().get().getDataHandler().addEventListener(this::valueChanged);
        }
    }

    private void setLabelText() {
        if (this.timeFramePojo.getTimeFrameWidgetObject().isPresent()) {

            if (this.timeFramePojo.getTimeFrameWidgetObject().get().getCountOfSamples()) {
                Platform.runLater(() -> this.label.setText(getSampleCount()));

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
            switch (this.timeFramePojo.getTimeFrameWidgetObject().orElseThrow(RuntimeException::new).getEndObjectProperty()) {
                case NONE:
                    dateTime = null;
                    break;
                case LAST_TS:
                    DataModelWidget dataModelWidget = this.timeFramePojo.getSelectedWidget().orElseThrow(RuntimeException::new);
                    dateTime = !dataModelWidget.getDataHandler().getMaxTimeStamps().isEmpty() ? dataModelWidget.getDataHandler().getMaxTimeStamps().get(0) : null;
                    break;
                case PERIODE_UNTIL:
                    dateTime = this.timeFramePojo.getSelectedWidget().orElseThrow(RuntimeException::new)
                            .getDataHandler().getDuration().getEnd();
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
            switch (this.timeFramePojo.getTimeFrameWidgetObject().orElseThrow(RuntimeException::new).getStartObjectProperty()) {
                case NONE:
                    dateTime = null;
                    break;
                case PERIODE_FROM:
                    dateTime = this.timeFramePojo.getSelectedWidget().orElseThrow(RuntimeException::new).getDataHandler().getDuration().getStart();
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

    private String getSampleCount() {
        try {
            ChartDataRow dataModel = this.timeFramePojo.getSelectedWidget().
                    orElseThrow(RuntimeException::new).getDataHandler().getChartDataRows().get(0);
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
                logger.debug(this.timeFramePojo.getSelectedWidget());
                this.timeFramePojo.getSelectedWidget().orElseThrow(RuntimeException::new);
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
        for (ChartDataRow chartDataRow : this.sampleHandler.getChartDataRows()) {
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

        if (!this.sampleHandler.getChartDataRows().isEmpty()) {
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
        logger.debug(dashBoardNode);
        return dashBoardNode;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("iconfinder_calendar-clock_299096.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }

    public void valueChanged(SampleHandlerEvent event) {
        logger.info("{} fired  Update {}", event, this);
        setLabelText();
    }
}
