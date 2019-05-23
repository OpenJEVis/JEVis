package org.jevis.jeconfig.plugin.Dashboard.timeframe;

import org.joda.time.DateTime;
import org.joda.time.Interval;

public interface TimeFrameFactory {

    String getListName();


    /**
     * @param addAmount
     * @return
     */
    Interval nextPeriod(Interval interval, int addAmount);

    Interval previousPeriod(Interval interval, int addAmount);

    String format(Interval interval);

    Interval getInterval(DateTime dateTime);

    String getID();

    boolean hasNextPeriod(Interval interval);
    boolean hasPreviousPeriod(Interval interval);

}

