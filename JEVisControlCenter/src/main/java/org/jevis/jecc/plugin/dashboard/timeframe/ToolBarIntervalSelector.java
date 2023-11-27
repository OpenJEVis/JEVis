package org.jevis.jecc.plugin.dashboard.timeframe;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.GlobalToolBar;
import org.jevis.jecc.application.tools.JEVisHelp;
import org.jevis.jecc.plugin.dashboard.DashBordPlugIn;
import org.jevis.jecc.plugin.dashboard.DashboardControl;

public class ToolBarIntervalSelector extends HBox {

    private static final Logger logger = LogManager.getLogger(ToolBarIntervalSelector.class);
    protected final TimeFactoryBox timeFactoryBox = new TimeFactoryBox(false);
    private final Double iconSize = 20d;
    protected final ToggleButton prevButton = new ToggleButton("", ControlCenter.getImage("arrow_left.png", iconSize, iconSize));
    protected final ToggleButton nextButton = new ToggleButton("", ControlCenter.getImage("arrow_right.png", iconSize, iconSize));
    private final Button dateButton = new Button("");
    protected TimeFrameEditor timeFrameEditor;
    protected boolean disableEventListener = false;
    protected ObservableList<TimeFrame> timeFrames;
    private DashboardControl controller;

    public ToolBarIntervalSelector() {
        super();
    }

    public ToolBarIntervalSelector(DashboardControl controller) {
        this();
        this.setAlignment(Pos.CENTER_LEFT);

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
        this.timeFrameEditor.intervalProperty().addListener((observable, oldValue, newValue) -> {
            if (disableEventListener) {
                return;
            }
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

        timeFrameEditor.setTimeFrame(controller.getActiveTimeFrame());
        timeFrameEditor.setInterval(controller.getInterval());
        timeFrameEditor.setDate(controller.getInterval().getEnd());

        timeFactoryBox.selectValue(controller.getActiveTimeFrame());


        disableEventListener = false;
    }

    public TimeFactoryBox getTimeFactoryBox() {
        return timeFactoryBox;
    }

    public Button getDateButton() {
        return dateButton;
    }

}
