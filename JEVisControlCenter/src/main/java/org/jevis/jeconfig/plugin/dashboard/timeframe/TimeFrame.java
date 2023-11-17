package org.jevis.jeconfig.plugin.dashboard.timeframe;

import org.joda.time.DateTime;
import org.joda.time.Interval;

public interface TimeFrame {

    String getListName();


    /**
     * @param addAmount
     * @return
     */
    Interval nextPeriod(Interval interval, int addAmount);

    Interval previousPeriod(Interval interval, int addAmount);

    String format(Interval interval);

    Interval getInterval(DateTime dateTime, Boolean fixed);

    String getID();

    boolean hasNextPeriod(Interval interval);

    boolean hasPreviousPeriod(Interval interval);

    default boolean timeFrameEqual(Object other) {
        try {
            if (other instanceof TimeFrame) {
                return getID().equals(((TimeFrame) other).getID());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

}

