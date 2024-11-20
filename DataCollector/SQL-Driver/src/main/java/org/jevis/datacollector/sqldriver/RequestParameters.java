package org.jevis.datacollector.sqldriver;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.classes.JC;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.driver.VarFiller;
import org.jevis.commons.object.plugin.TargetHelper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Map;

public class RequestParameters {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(RequestParameters.class);
    private final JEVisAttribute targetAtt;
    private final String query;
    private final String exeQuery;

    private final String valueColumn;
    private final String timeStampColumn;
    private final String timeStampFormate;
    private final String noteColumn;
    private final String valueFormate;
    private final JEVisAttribute lastReadoutAttribute;
    private final JEVisAttribute readoutOffsetAttribute;
    private final JEVisAttribute statusAttribute;
    private final DateTime lastLog = new DateTime();
    private final DateTime lastReadout;

    public RequestParameters(JEVisObject queryObject) throws Exception {

        query = Parameters.getAttValue(queryObject, "Query", "", false);
        JEVisAttribute target = queryObject.getAttribute("Target");
        TargetHelper th = new TargetHelper(queryObject.getDataSource(), target);
        if (!th.isValid()) throw new RuntimeException("target not reachable");
        targetAtt = th.getAttribute().get(0);
//        JEVisSample lastSample = targetAtt.getLatestSample();

        lastReadoutAttribute = queryObject.getAttribute(JC.Channel.a_LastReadout);

        DateTime initialDate = new DateTime(1980, 1, 1, 0, 0, 0, 0);
        SampleHandler sampleHandler = new SampleHandler();

        readoutOffsetAttribute = queryObject.getAttribute(JC.Channel.a_ReadoutOffset);
        Long readoutOffset = sampleHandler.getLastSample(queryObject, JC.Channel.a_ReadoutOffset, 0L);

        lastReadout = sampleHandler.getLastSample(queryObject, JC.Channel.a_LastReadout, initialDate).minus(readoutOffset);

        statusAttribute = queryObject.getAttribute("Status Log");

        timeStampColumn = Parameters.getAttValue(queryObject, "Timestamp Column", "", false);
        valueColumn = Parameters.getAttValue(queryObject, "Value Column", "", false);
        timeStampFormate = Parameters.getAttValue(queryObject, "Timestamp Formate", "TIMESTAMP", true);
        noteColumn = Parameters.getAttValue(queryObject, "Note Column", "", true);
        valueFormate = Parameters.getAttValue(queryObject, "Value Formate", "DOUBLE", true);

        Map<VarFiller.Variable, VarFiller.VarFunction> variables = new HashMap<>();

        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

        variables.put(VarFiller.Variable.LAST_TS, () -> {
            try {
                return fmt.print(lastReadout);
            } catch (Exception e) {
                return "";
            }
        });

        VarFiller varFiller = new VarFiller(query, variables);
        exeQuery = varFiller.getFilledURIString();

    }

    public JEVisAttribute getTarget() {
        return targetAtt;
    }

    public String getExecutableQuery() {
        return exeQuery;
    }

    public String valueColumn() {
        return valueColumn;
    }

    public String timeStampColumn() {
        return timeStampColumn;
    }

    public String timeStampFormate() {
        return timeStampFormate;
    }

    public String noteColumn() {
        return noteColumn;
    }

    public String valueFormate() {
        return valueFormate;
    }

    public DateTime getLastReadout() {
        return lastReadout;
    }

    public void updateLastReadout() {
        try {
            JEVisSample sample = lastReadoutAttribute.buildSample(new DateTime(), targetAtt.getLatestSample().getTimestamp());
            sample.commit();
        } catch (Exception ex) {
            logger.error("Cant log status to JEVis", ex, ex);
        }
    }

    /**
     * Log a status message into JEVis.
     * <p>
     * We can only have one msg per second, so we check for the last msg
     *
     * @param status
     * @param ts
     * @param msg
     */
    public void logStatus(int status, DateTime ts, String msg) {
        try {
            if (status == 0) {
                logger.info("Status: {} msg: {}", status, msg);
            } else {
                logger.error("Status: {} msg: {}", status, msg);
            }

            JEVisSample sample = null;
            if (lastLog.plusSeconds(1).isAfter(ts)) {
                sample = statusAttribute.buildSample(ts, status + ": " + msg);
            } else {
                sample = statusAttribute.buildSample(lastLog.plusSeconds(1), status + ": " + msg);
            }
            sample.commit();
        } catch (Exception ex) {
            logger.error("Cant log status to JEVis", ex, ex);
        }
    }

    @Override
    public String toString() {
        return "RequestParameters{" +
                "targetAtt=" + targetAtt +
                ", exeQuery='" + exeQuery + '\'' +
                '}';
    }
}
