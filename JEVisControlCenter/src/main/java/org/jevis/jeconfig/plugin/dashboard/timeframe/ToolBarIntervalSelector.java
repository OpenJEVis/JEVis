package org.jevis.jeconfig.plugin.dashboard.timeframe;

import com.jfoenix.controls.JFXButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.plugin.dashboard.DashBordPlugIn;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;

public class ToolBarIntervalSelector extends HBox {

    private static final Logger logger = LogManager.getLogger(ToolBarIntervalSelector.class);
    private final Double iconSize = 20d;
    protected final ToggleButton prevButton = new ToggleButton("", JEConfig.getImage("arrow_left.png", iconSize, iconSize));
    protected final ToggleButton nextButton = new ToggleButton("", JEConfig.getImage("arrow_right.png", iconSize, iconSize));
    protected final TimeFactoryBox timeFactoryBox = new TimeFactoryBox(false);
    protected TimeFrameEditor timeFrameEditor;
    protected ObservableList<TimeFrameFactory> timeFrames;
    protected boolean disableEventListener = false;
    private DashboardControl controller;

    public ToolBarIntervalSelector() {
        super();
    }

    public ToolBarIntervalSelector(DashboardControl controller) {
        this();
        this.setAlignment(Pos.CENTER_LEFT);
        JFXButton dateButton = new JFXButton("");
        dateButton.setMinWidth(100);
        this.controller = controller;

        GlobalToolBar.changeBackgroundOnHoverUsingBinding(prevButton);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(nextButton);

        timeFactoryBox.setPrefWidth(200);
        timeFactoryBox.setMinWidth(200);

        timeFrames = FXCollections.observableArrayList(controller.getAllTimeFrames().getAll());
        timeFactoryBox.getItems().setAll(timeFrames);

//        dateButton.setText(controller.getActiveTimeFrame().format(controller.getInterval()));
//        dateButton.setTooltip(new Tooltipcontroller.getInterval().toString()));

        this.timeFrameEditor = new TimeFrameEditor(controller.getActiveTimeFrame(), controller.getInterval());
        this.timeFrameEditor.getIntervalProperty().addListener((observable, oldValue, newValue) -> {
            if (disableEventListener) return;
            controller.setInterval(newValue);
        });

        dateButton.setOnAction(event -> {
            if (this.timeFrameEditor.isShowing()) {
                this.timeFrameEditor.hide();
            } else {
                this.timeFrameEditor.setDate(controller.getInterval().getEnd());
                Point2D point = dateButton.localToScreen(0.0, 0.0);
                this.timeFrameEditor.show(dateButton, point.getX() - 40, point.getY() + 40);
            }
        });

        timeFactoryBox.selectValue(controller.getActiveTimeFrame());

        timeFactoryBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (disableEventListener) return;
            controller.setActiveTimeFrame(newValue);
        });

        prevButton.setOnAction(event -> {
            controller.setPrevInterval();
        });

        nextButton.setOnAction(event -> {
            controller.setNextInterval();
        });

        controller.getActiveIntervalProperty().addListener((observable, oldValue, newValue) -> {
            if (disableEventListener) return;
            dateButton.setText(controller.getActiveTimeFrame().format(controller.getInterval()));
        });

        Region spacer = new Region();
        spacer.setMinWidth(10);
        spacer.setMaxWidth(10);

        timeFactoryBox.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.timeinterval")));
        prevButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.previnterval")));
        dateButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.dateselector")));
        nextButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.nextvinterval")));

        getChildren().addAll(timeFactoryBox, spacer, prevButton, dateButton, nextButton);
        JEVisHelp.getInstance().addHelpItems(DashBordPlugIn.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, getChildren());
    }

    public void updateView() {
        logger.debug("updateView: timeframe: '{}', interval: '{}' date: '{}'", controller.getActiveTimeFrame(), controller.getInterval(), controller.getInterval().getEnd());
        disableEventListener = true;

        timeFactoryBox.selectValue(controller.getActiveTimeFrame());
        timeFrameEditor.setTimeFrame(controller.getActiveTimeFrame());
        timeFrameEditor.setIntervalProperty(controller.getInterval());
        timeFrameEditor.setDate(controller.getInterval().getEnd());


        disableEventListener = false;
    }

    public TimeFactoryBox getTimeFactoryBox() {
        return timeFactoryBox;
    }
}
