package org.jevis.loytecxmldl;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.commons.driver.DataCollectorTypes;
import org.joda.time.DateTime;
import org.joda.time.format.*;

/**
 * JEVis Class: Loytec XML-DL Channel
 */
public interface LoytecXmlDlChannelClass extends DataCollectorTypes.Channel {
    // JEVis class mapping strings
    String NAME = "Loytec XML-DL Channel";
    String TREND_ID = "Trend ID";
    String TARGET_ID = "Target ID";
    String LAST_READOUT = "Last Readout";
    String STATUS_LOG = "Status Log";

    Long inAlarm = 1L;
    Long fault = 2L;
    Long overidden = 4L;
    Long outOfService = 8L;
    Long offline = 16L;
    Long statusNotAvailable = 128L;

    // Date time formatter for legacy (yyyy-MM... and ISO format
    DateTimeParser[] parsers = {
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").getParser(),
            ISODateTimeFormat.dateTime().getParser()};
    DateTimeFormatter DATE_FORMAT = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();

    // Default values
    String DEFAULT_TREND_ID = null;
    String DEFAULT_TARGET = null;

    // Methods to implement
    String getName();

    String getTrendId();

    Long getTargetId();

    DateTime getLastReadout();

    JEVisObject getJeVisObject();

    JEVisAttribute getTarget();

    String getTargetString();

    JEVisAttribute getStatusLog();
}
