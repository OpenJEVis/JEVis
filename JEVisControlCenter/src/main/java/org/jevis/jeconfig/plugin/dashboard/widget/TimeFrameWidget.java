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
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
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
    public void updateData(Interval interval) {
        Platform.runLater(() -> {
            if (!this.timeFramePojo.getSelectedWidget().isPresent()) {
                return;
            }else {
                try {
                    logger.debug(this.timeFramePojo.getSelectedWidget().get().getCurrentInterval(control.getInterval()));
//                Widget select = this.timeFramePojo.getSelectedWidget();
                    setLabelText();
                } catch (Exception e) {
                    logger.error(e);
                }
            }

        });


    }

    private void setLabelText() {
        if (this.timeFramePojo.getSelectedTimeFarmeObjectWidget().isPresent()) {

            if (this.timeFramePojo.getSelectedTimeFarmeObjectWidget().get().isCuntOfSamples()) {
                this.label.setText(getSmapleCount());

            }else {
                this.label.setText(convertIntervalToString(getStart(), getEnd()));
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
        if (!this.timeFramePojo.getSelectedTimeFarmeObjectWidget().isPresent()) return null;
        switch (this.timeFramePojo.getSelectedTimeFarmeObjectWidget().get().getEndObjectProperty()) {
            case NONE:
                dateTime = null;
                break;
            case LAST_TS:
                if (this.timeFramePojo.getSelectedWidget().isPresent()) {
                    dateTime = this.timeFramePojo.getSelectedWidget().get().sampleHandler.getMaxTimeStamps().get(0);
                }else {
                    dateTime = null;
                }
                break;
            case PERIODE_UNTIL:
                if (this.timeFramePojo.getSelectedWidget().isPresent()) {
                    dateTime = this.timeFramePojo.getSelectedWidget().get().getCurrentInterval(control.getInterval()).getEnd();
                }else{
                    dateTime = null;
                }
                break;

        }

        return dateTime;

    }

    private DateTime getStart() {
        if (!this.timeFramePojo.getSelectedTimeFarmeObjectWidget().isPresent()) return null;
        DateTime dateTime = new DateTime();
        switch (this.timeFramePojo.getSelectedTimeFarmeObjectWidget().get().getStartObjectProperty()) {
            case NONE:
                dateTime = null;
                break;
            case PERIODE_FROM:
                if (this.timeFramePojo.getSelectedWidget().isPresent()) {
                    dateTime = this.timeFramePojo.getSelectedWidget().get().getCurrentInterval(control.getInterval()).getStart();
                }else {
                    dateTime = null;
                }
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
            if (this.timeFramePojo.getSelectedWidget().isPresent()) {
                ChartDataRow dataModel = this.timeFramePojo.getSelectedWidget().get().sampleHandler.getDataModel().get(0);
                return String.valueOf(dataModel.getSamples().size());
            }else {
                return "0";
            }
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

                if (this.timeFramePojo.getSelectedWidget().isPresent()) {
                    this.timeFramePojo.getSelectedWidget().get().sampleHandler.addEventListener(event -> {
                        logger.info("{} fired  Update {}",event,this);
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
