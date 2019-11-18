package org.jevis.commons.utils;

import org.jevis.commons.ws.json.JsonSample;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.List;

public class Samples {

    /**
     * Returns the period of an sample List.
     * <p>
     * TODO: support non sorted lists
     *
     * @param samples
     * @return
     */
    public static Period getDuration(List<JsonSample> samples) {
        try {
            if (samples.isEmpty()) {
                return Period.ZERO;
            }

            if (samples.size() == 1) {
                return new Period(DateTime.parse(samples.get(0).getTs()), DateTime.parse(samples.get(0).getTs()));
            }

            return new Period(DateTime.parse(samples.get(0).getTs()), DateTime.parse(samples.get(samples.size() - 1).getTs()));

        } catch (Exception ex) {
            return Period.ZERO;
        }
    }


}
