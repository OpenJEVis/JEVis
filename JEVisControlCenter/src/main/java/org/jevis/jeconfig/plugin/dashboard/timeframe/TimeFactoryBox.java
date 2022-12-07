package org.jevis.jeconfig.plugin.dashboard.timeframe;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.tools.DisabledItemsComboBox;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;

public class TimeFactoryBox extends DisabledItemsComboBox<TimeFrame> {


    private final TimeFrame offTimeFrame = emptyTimeFrame();

    public TimeFactoryBox(boolean addOffValue) {
        super();

        setPrefWidth(200);
        setMinWidth(200);

        if (addOffValue) {
            getItems().add(0, offTimeFrame);
        }

        Callback<ListView<TimeFrame>, ListCell<TimeFrame>> cellFactory = new Callback<ListView<TimeFrame>, ListCell<TimeFrame>>() {
            @Override
            public ListCell<TimeFrame> call(ListView<TimeFrame> param) {
                final ListCell<TimeFrame> cell = new ListCell<TimeFrame>() {

                    @Override
                    protected void updateItem(TimeFrame item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(item.getListName());
                        } else {
                            setText(null);
                        }
                    }
                };

                return cell;
            }
        };

        setCellFactory(cellFactory);
        setButtonCell(cellFactory.call(null));

        getSelectionModel().selectFirst();
    }


    public void selectValue(TimeFrame factory) {
        if (factory == null) return;

        for (TimeFrame timeFrame : getItems()) {
            if (timeFrame.getID().equals(factory.getID())) {
                getSelectionModel().select(timeFrame);
                return;
            }
        }

        /** factory does not exist, create a new one **/
        getItems().add(factory);
        getSelectionModel().select(factory);

    }

    public boolean isOffValue() {
        return getValue().equals(offTimeFrame);
    }

    private TimeFrame emptyTimeFrame() {
        return new TimeFrame() {
            @Override
            public String getListName() {
                return I18n.getInstance().getString("plugin.dashboard.timefactory.off");
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
                return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").print(interval.getStart()) + " / " + DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").print(interval.getEnd());
            }

            @Override
            public Interval getInterval(DateTime dateTime) {
                return new Interval(dateTime, dateTime);
            }

            @Override
            public String getID() {
                return "Disable";
            }

            @Override
            public boolean hasNextPeriod(Interval interval) {
                return false;
            }

            @Override
            public boolean hasPreviousPeriod(Interval interval) {
                return false;
            }

        };
    }
}
