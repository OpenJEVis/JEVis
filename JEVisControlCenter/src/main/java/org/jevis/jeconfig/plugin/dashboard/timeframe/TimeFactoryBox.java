package org.jevis.jeconfig.plugin.dashboard.timeframe;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jevis.commons.i18n.I18n;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;

public class TimeFactoryBox extends ComboBox<TimeFrameFactory> {


    private TimeFrameFactory offTimeFrame = emptyTimeFrame();

    public TimeFactoryBox(boolean addOffValue) {
        super();

        setPrefWidth(200);
        setMinWidth(200);

        if (addOffValue) {
            getItems().add(0, offTimeFrame);
        }

        Callback<ListView<TimeFrameFactory>, ListCell<TimeFrameFactory>> cellFactory = new Callback<ListView<TimeFrameFactory>, ListCell<TimeFrameFactory>>() {
            @Override
            public ListCell<TimeFrameFactory> call(ListView<TimeFrameFactory> param) {
                final ListCell<TimeFrameFactory> cell = new ListCell<TimeFrameFactory>() {

                    @Override
                    protected void updateItem(TimeFrameFactory item, boolean empty) {
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


    public void selectValue(TimeFrameFactory factory) {
        if (factory == null) return;

        for (TimeFrameFactory timeFrameFactory : getItems()) {
            if (timeFrameFactory.getID().equals(factory.getID())) {
                getSelectionModel().select(timeFrameFactory);
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

    private TimeFrameFactory emptyTimeFrame() {
        return new TimeFrameFactory() {
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
