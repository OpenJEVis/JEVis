package org.jevis.datacollector.sqldriver;

import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.DataSource;
import org.jevis.commons.utils.CommonMethods;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class SQLDriver implements DataSource {

    private Parameters parameters;
    private JEVisAttribute statusAttribute = null;
    private JEVisAttribute lastReadoutAttribute = null;
    private DateTimeZone timeZone = DateTimeZone.getDefault();
    private boolean overwrite = false;


    private JEVisObject sqlServerObj;

    private int totalSamples = 0;

    private DateTime lastLog = new DateTime();

    public SQLDriver() {
    }


    private void loadDriver(String driverClassName) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        //DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());

        DriverManager.registerDriver((Driver) Class.forName(driverClassName).newInstance());
        //Driver driver = DriverManager.getDriver(driverClassName);
        //DriverManager.registerDriver(driver);
    }

    private void startReadout() throws SQLException, JEVisException {
        try (Connection con = DriverManager.getConnection(parameters.getFullConnectionURI()); Statement stmt = con.createStatement()) {
            con.setNetworkTimeout(Executors.newSingleThreadExecutor(), parameters.connectionTimeout());
            con.setReadOnly(true);
            // not all driver support this way to set a readtimeout or setNetworkTimeout, there is no default way
            if (con.isValid(parameters.connectionTimeout())) {
                //logger.info("SQL Connection successful, execude request: {}", parameters.getRequest());

                for (RequestParameters requestParameters : parameters.getSQLRequests()) {
                    try {
                        logger.info("Execute SQL: {}", requestParameters.getExecutableQuery());
                        ResultSet rs = stmt.executeQuery(requestParameters.getExecutableQuery());
                        String requestValueType = requestParameters.valueFormate().toUpperCase();
                        List<JEVisSample> resultSamples = new ArrayList<>();
                        DateTimeFormatter fmt = null;
                        JEVisAttribute targetAttribute = requestParameters.getTarget();
                        while (rs.next()) {
                            try {
                                DateTime dateTime = null;
                                String note = "";

                                if (requestParameters.timeStampFormate().toUpperCase().startsWith("STRING")) {
                                    //TODO: untested
                                    if (fmt == null) {
                                        fmt = DateTimeFormat.forPattern(requestParameters.timeStampFormate().substring(7));
                                    }
                                    dateTime = fmt.parseDateTime(requestParameters.timeStampColumn());
                                } else if (requestParameters.timeStampFormate().equalsIgnoreCase("TIMESTAMP")) {
                                    java.time.LocalDateTime dt = rs.getObject(requestParameters.timeStampColumn(), java.time.LocalDateTime.class);
                                    dateTime = new DateTime(dt.getYear(), dt.getMonthValue(), dt.getDayOfMonth(),
                                            dt.getHour(), dt.getMinute(), dt.getSecond(), timeZone);

                                }

                                Double value = null;
                                String sValue = null;
                                if (requestValueType.startsWith("DOUBLE")) {
                                    value = rs.getDouble(requestParameters.valueColumn());
                                } else if (requestValueType.startsWith("INTEGER")) {
                                    value = BigDecimal.valueOf(rs.getInt(requestParameters.valueColumn())).doubleValue();
                                } else if (requestValueType.startsWith("FLOAT")) {
                                    value = BigDecimal.valueOf(rs.getFloat(requestParameters.valueColumn())).doubleValue();
                                } else if (requestValueType.startsWith("LONG")) {
                                    value = BigDecimal.valueOf(rs.getLong(requestParameters.valueColumn())).doubleValue();
                                } else if (requestValueType.startsWith("STRING")) {
                                    //TODO: Support String to Double Parser with user given regex
                                    sValue = rs.getString(requestParameters.valueColumn());
                                }


                                //String note = "";
                                if (!requestParameters.noteColumn().isEmpty()) {
                                    note = rs.getString(requestParameters.noteColumn());
                                }

                                if (value != null && dateTime != null) {
                                    resultSamples.add(targetAttribute.buildSample(dateTime, value, note));
                                } else if (sValue != null && dateTime != null) {
                                    resultSamples.add(targetAttribute.buildSample(dateTime, sValue, note));
                                }


                            } catch (Exception ex) {
                                logger.error("Error in Row: ", ex);
                                logStatus(5, new DateTime(), "SQL Error: " + ex);
                            }
                        }

                        resultSamples.sort((o1, o2) -> {
                            try {
                                return o1.getTimestamp().compareTo(o2.getTimestamp());
                            } catch (Exception ex) {
                                return 0;
                            }
                        });

                        logger.info("Found {} samples for {}:{}:{}", resultSamples.size(), sqlServerObj.getID(), sqlServerObj.getName(), targetAttribute.getObjectID());

                        if (!resultSamples.isEmpty() && overwrite) {
                            DateTime firstNewSampleTS = requestParameters.getLastReadout();
                            if (resultSamples.get(0).getTimestamp().isBefore(firstNewSampleTS)) {
                                firstNewSampleTS = resultSamples.get(0).getTimestamp();
                            }
                            DateTime currentTS = new DateTime();

                            logger.info("Overwrite is enabled, requested samples in between: {}-{}. Checking for changed values.",
                                    firstNewSampleTS,
                                    currentTS);

                            List<JEVisSample> oldSamples = targetAttribute.getSamples(firstNewSampleTS, currentTS);
                            StringBuilder samplesString = new StringBuilder();
                            if (logger.isInfoEnabled()) {
                                for (JEVisSample jeVisSample : oldSamples) {
                                    samplesString.append(jeVisSample);
                                }

                                logger.info("Found {} old Samples in target attribute. {}", oldSamples.size(), samplesString);

                                samplesString.delete(0, samplesString.length());

                                for (JEVisSample jeVisSample : resultSamples) {
                                    samplesString.append(jeVisSample);
                                }

                                logger.info("Found {} new Samples in target attribute. {}", resultSamples.size(), samplesString);
                            }


                            Map<DateTime, JEVisSample> oldSamplesMap = new HashMap<>();
                            for (JEVisSample resultSample : oldSamples) {
                                oldSamplesMap.put(resultSample.getTimestamp(), resultSample);
                            }

                            List<Sample> samples = new ArrayList<>();
                            DateTime firstRelevantDate = new DateTime();
                            logger.debug("First date {}", firstRelevantDate);

                            for (JEVisSample newSample : resultSamples) {
                                JEVisSample oldSample = oldSamplesMap.remove(newSample.getTimestamp());

                                if (oldSample != null && !requestValueType.startsWith("STRING")) {
                                    Double v1 = oldSample.getValueAsDouble();
                                    Double v2 = newSample.getValueAsDouble();
                                    int compare = Double.compare(v1, v2);
                                    logger.debug("Double Sample for {} oldValue = {} new Value = {} compare = {}", newSample.getTimestamp(), v1, v2, compare);
                                    if (compare != 0) {
                                        samples.add(new Sample(newSample, SampleStatus.CHANGED));
                                        if (newSample.getTimestamp().isBefore(firstRelevantDate)) {
                                            firstRelevantDate = newSample.getTimestamp();
                                        }
                                    } else {
                                        samples.add(new Sample(newSample, SampleStatus.UNCHANGED));
                                    }
                                } else if (oldSample != null && requestValueType.startsWith("STRING")) {
                                    String s1 = oldSample.getValueAsString();
                                    String s2 = newSample.getValueAsString();

                                    logger.debug("String Sample for {} oldValue = {} new Value = {}", newSample.getTimestamp(), s1, s2);

                                    if (!s1.equals(s2)) {
                                        samples.add(new Sample(newSample, SampleStatus.CHANGED));
                                        if (newSample.getTimestamp().isBefore(firstRelevantDate)) {
                                            firstRelevantDate = newSample.getTimestamp();
                                        }
                                    } else {
                                        samples.add(new Sample(newSample, SampleStatus.UNCHANGED));
                                    }
                                } else {
                                    samples.add(new Sample(newSample, SampleStatus.NEW));
                                    if (newSample.getTimestamp().isBefore(firstRelevantDate)) {
                                        firstRelevantDate = newSample.getTimestamp();
                                    }
                                }
                            }

                            logger.debug("First relevant date for changed/new samples is {}", firstRelevantDate);

                            if (logger.isInfoEnabled()) {
                                samplesString.delete(0, samplesString.length());
                                List<JEVisSample> valuesList = new ArrayList<>(oldSamplesMap.values());
                                valuesList.sort((o1, o2) -> {
                                    try {
                                        return o1.getTimestamp().compareTo(o2.getTimestamp());
                                    } catch (Exception ex) {
                                        return 0;
                                    }
                                });
                                for (JEVisSample jeVisSample : valuesList) {
                                    samplesString.append(jeVisSample);
                                }

                                logger.info("Found {} old samples which don't exist. {}", valuesList.size(), samplesString);
                            }

                            for (Map.Entry<DateTime, JEVisSample> entry : oldSamplesMap.entrySet()) {
                                DateTime key = entry.getKey();
                                logger.info("Deleting sample for {}", key);
                                targetAttribute.deleteSamplesBetween(key, key);
                            }

                            logger.info("Changed new samples in between: {}-{}. Deleting dependencies.",
                                    firstRelevantDate,
                                    currentTS);

                            targetAttribute.deleteSamplesBetween(firstRelevantDate, currentTS);
                            try {
                                List<JEVisObject> objects = new ArrayList<>();
                                objects.add(targetAttribute.getObject());

                                CommonMethods.cleanDependentObjects(objects, firstRelevantDate);
                            } catch (Exception e) {
                                logger.error("Failed cleaning of dependencies", e);
                            }


                            List<JEVisSample> notRelevantSamples = new ArrayList<>();
                            for (JEVisSample jeVisSample : resultSamples) {
                                if (!jeVisSample.getTimestamp().equals(firstRelevantDate) && !jeVisSample.getTimestamp().isAfter(firstRelevantDate)) {
                                    notRelevantSamples.add(jeVisSample);
                                }
                            }

                            if (logger.isInfoEnabled()) {
                                samplesString.delete(0, samplesString.length());
                                for (JEVisSample jeVisSample : notRelevantSamples) {
                                    samplesString.append(jeVisSample);
                                }

                                logger.info("Found {} not relevant Samples in results. {}", notRelevantSamples.size(), samplesString);
                            }

                            resultSamples.removeAll(notRelevantSamples);
                        }

                        targetAttribute.addSamples(resultSamples);

                        if (!resultSamples.isEmpty()) {
                            requestParameters.logStatus(0, new DateTime(), "Import " + resultSamples.size() + " Samples into " + targetAttribute.getName() + ":" + targetAttribute.getObjectID());
                            requestParameters.updateLastReadout();
                        } else {
                            requestParameters.logStatus(0, new DateTime(), "Nothing to Import for " + targetAttribute.getName() + ":" + targetAttribute.getObjectID());
                        }
                        totalSamples = totalSamples + resultSamples.size();

                    } catch (Exception ex) {
                        logger.error(ex, ex);
                        logStatus(4, new DateTime(), "Error: " + ex);
                    }
                }

            } else {
                logger.error("Lost SQl Connection");
                logStatus(3, new DateTime(), "Lost Connection");
            }

        }
    }

    @Override
    public void run() {
        try {
            startReadout();
            logStatus(0, new DateTime(), "End Readout - Imported: " + totalSamples);
        } catch (Exception e) {
            logger.error("Error while Run SQL Driver: {}:{}", sqlServerObj.getID(), sqlServerObj.getName(), e);
        }
    }

    @Override
    public void initialize(JEVisObject dataSourceJEVis) {
        try {
            this.sqlServerObj = dataSourceJEVis;
            if (sqlServerObj == null) {
                logger.error("Error Server Object is null");
                return;
            }

            lastReadoutAttribute = sqlServerObj.getAttribute("Last Readout");
            statusAttribute = sqlServerObj.getAttribute("Status Log");
            timeZone = getTimeZone(dataSourceJEVis);
            JEVisType overwriteType = dataSourceJEVis.getJEVisClass().getType(DataCollectorTypes.DataSource.DataServer.OVERWRITE);
            overwrite = DatabaseHelper.getObjectAsBoolean(dataSourceJEVis, overwriteType);

            cleanStatus();
            logStatus(0, new DateTime(), "Start Readout");


            parameters = new Parameters(sqlServerObj);
            loadDriver(parameters.driver());
        } catch (Exception ex) {
            logger.error("Error while fetching data from Server: {}:{}", sqlServerObj.getID(), sqlServerObj.getName(), ex);
            logStatus(1, new DateTime(), "Unexpected error: " + ex);
        }
    }

    @Override
    public List<InputStream> sendSampleRequest(JEVisObject channel) throws Exception {
        return new ArrayList<>();
    }

    @Override
    public void parse(List<InputStream> input) {

    }

    @Override
    public void importResult() {

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
            logger.info("SQL Datasource: {}:'{}' Status: {} msg: {}", sqlServerObj.getID(), sqlServerObj.getName(), status, msg);
            JEVisSample sample = null;
            if (lastLog.plusSeconds(1).isAfter(ts)) {
                ts = lastLog.plusSeconds(1);
            }
            lastLog = ts;
            sample = statusAttribute.buildSample(ts, status + ": " + msg);
            sample.commit();
        } catch (Exception ex) {
            logger.error("Cant log status to JEVis", ex, ex);
        }
    }

    public void cleanStatus() {
        try {
            logger.info("Cleaning status log");
            DateTime dateTime = new DateTime().minusMonths(1);
            DateTime timestampOfFirstSample = statusAttribute.getTimestampOfFirstSample();
            if (timestampOfFirstSample != null && timestampOfFirstSample.isBefore(dateTime)) {
                statusAttribute.deleteSamplesBetween(timestampOfFirstSample, dateTime);
            }
        } catch (Exception ex) {
            logger.error("Cant clean status", ex);
        }
    }
}
