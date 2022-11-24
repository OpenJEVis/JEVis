package org.jevis.jeconfig.plugin.dtrc;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.plugin.dashboard.DashBordPlugIn;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrame;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrameEditor;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrameFactory;
import org.jevis.jeconfig.plugin.dashboard.timeframe.ToolBarIntervalSelector;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.time.LocalDate;
import java.time.LocalTime;

public class IntervalSelector extends ToolBarIntervalSelector {
    private final SimpleObjectProperty<TimeFrame> activeTimeFrame = new SimpleObjectProperty<>(new TimeFrame() {
        @Override
        public String getListName() {
            return "";
        }


        @Override
        public Interval nextPeriod(Interval interval, int addAmount) {
            return interval;
        }

        @Override
        public Interval previousPeriod(Interval interval, int addAmount) {
            return interval;
        }

        @Override
        public String format(Interval interval) {
            return "";
        }

        @Override
        public Interval getInterval(DateTime dateTime) {
            return new Interval(dateTime, dateTime);
        }

        @Override
        public String getID() {
            return "";
        }

        @Override
        public boolean hasNextPeriod(Interval interval) {
            return false;
        }

        @Override
        public boolean hasPreviousPeriod(Interval interval) {
            return false;
        }
    });

    private final TimeFrameFactory timeFrameFactory;
    private final JFXDatePicker startDatePicker;
    private final JFXTimePicker startTimePicker;
    private final JFXDatePicker endDatePicker;
    private final JFXTimePicker endTimePicker;
    private final SimpleBooleanProperty update = new SimpleBooleanProperty(false);
    private Interval interval;

    public IntervalSelector(JEVisDataSource ds, JFXDatePicker startDatePicker, JFXTimePicker startTimePicker, JFXDatePicker endDatePicker, JFXTimePicker endTimePicker) {
        super();
        this.startDatePicker = startDatePicker;
        this.startTimePicker = startTimePicker;
        this.endDatePicker = endDatePicker;
        this.endTimePicker = endTimePicker;

        DateTime start = new DateTime(startDatePicker.getValue().getYear(), startDatePicker.getValue().getMonthValue(), startDatePicker.getValue().getDayOfMonth(),
                startTimePicker.getValue().getHour(), startTimePicker.getValue().getMinute(), startTimePicker.getValue().getSecond());
        DateTime end = new DateTime(endDatePicker.getValue().getYear(), endDatePicker.getValue().getMonthValue(), endDatePicker.getValue().getDayOfMonth(),
                endTimePicker.getValue().getHour(), endTimePicker.getValue().getMinute(), endTimePicker.getValue().getSecond());

        interval = new Interval(start, end);

        this.timeFrameFactory = new TimeFrameFactory(ds);

        this.setAlignment(Pos.CENTER_LEFT);
        JFXButton dateButton = new JFXButton("");
        dateButton.setMinWidth(100);

        GlobalToolBar.changeBackgroundOnHoverUsingBinding(prevButton);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(nextButton);

        timeFactoryBox.setPrefWidth(200);
        timeFactoryBox.setMinWidth(200);

        timeFactoryBox.getItems().setAll(timeFrameFactory.getAll());

//        dateButton.setText(controller.getActiveTimeFrame().format(controller.getInterval()));
//        dateButton.setTooltip(new Tooltipcontroller.getInterval().toString()));

        this.timeFrameEditor = new TimeFrameEditor(activeTimeFrame.get(), interval);
        this.timeFrameEditor.getIntervalProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                interval = activeTimeFrame.get().getInterval(newValue.getEnd());
                applyNewDate(interval);
                dateButton.setText(activeTimeFrame.get().format(interval));

                setUpdate(true);
            }
        });

        dateButton.setOnAction(event -> {
            if (this.timeFrameEditor.isShowing()) {
                this.timeFrameEditor.hide();
            } else {
                Point2D point = dateButton.localToScreen(0.0, 0.0);
                this.timeFrameEditor.show(dateButton, point.getX() - 40, point.getY() + 40);
            }
        });

        timeFactoryBox.selectValue(activeTimeFrame.get());

        timeFactoryBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                activeTimeFrame.set(newValue);

                setUpdate(true);
            }
        });

        prevButton.setOnAction(event -> {
            Interval nextInterval = this.activeTimeFrame.get().previousPeriod(this.interval, 1);
            if (nextInterval.getStart().isBeforeNow()) {
                this.interval = nextInterval;
                dateButton.setText(activeTimeFrame.get().format(interval));
                applyNewDate(nextInterval);
            }
        });

        nextButton.setOnAction(event -> {
            Interval nextInterval = this.activeTimeFrame.get().nextPeriod(this.interval, 1);
            if (nextInterval.getStart().isBeforeNow()) {
                interval = nextInterval;
                dateButton.setText(activeTimeFrame.get().format(interval));
                applyNewDate(nextInterval);
            }
        });

        activeTimeFrame.addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                interval = activeTimeFrame.get().getInterval(interval.getEnd());
                dateButton.setText(activeTimeFrame.get().format(interval));
                applyNewDate(interval);
            }
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

    private void applyNewDate(Interval newValue) {
        DateTime start = newValue.getStart();
        DateTime end = newValue.getEnd();

        startDatePicker.setValue(LocalDate.of(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth()));
        startTimePicker.setValue(LocalTime.of(start.getHourOfDay(), start.getMinuteOfHour(), start.getSecondOfMinute()));

        endDatePicker.setValue(LocalDate.of(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth()));
        endTimePicker.setValue(LocalTime.of(end.getHourOfDay(), end.getMinuteOfHour(), end.getSecondOfMinute()));

        timeFrameEditor.setDate(end);
    }

    public boolean isUpdate() {
        return update.get();
    }

    public void setUpdate(boolean update) {
        this.update.set(update);
    }

    public SimpleBooleanProperty updateProperty() {
        return update;
    }
}
