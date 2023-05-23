package org.jevis.datacollector.sqldriver;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.driver.DataSource;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class SQLDriver implements DataSource {

    private Parameters parameters;
    private JEVisAttribute statusAttribute = null;
    private JEVisAttribute lastReadoutAttribute = null;

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


                for (RequestParameters requestParameters : parameters.getRequest()) {
                    try {
                        logger.info("Execute SQL: {}", requestParameters.getExecutableQuery());
                        ResultSet rs = stmt.executeQuery(requestParameters.getExecutableQuery());
                        List<JEVisSample> resultSamples = new ArrayList<>();
                        DateTimeFormatter fmt = null;
                        while (rs.next()) {
                            try {
                                DateTime dateTime = null;

                                if (requestParameters.timeStampFormate().toUpperCase().startsWith("STRING")) {
                                    //TODO: Test and make more save
                                    if (fmt == null) {
                                        fmt = DateTimeFormat.forPattern(requestParameters.timeStampFormate().substring(7));
                                    }
                                    dateTime = fmt.parseDateTime(requestParameters.timeStampColumn());
                                } else if (requestParameters.timeStampFormate().toUpperCase().equals("TIMESTAMP")) {
                                    Timestamp ts = rs.getTimestamp(requestParameters.timeStampColumn());
                                    dateTime = new DateTime(ts.getTime());
                                }

                                Double value = null;
                                String sValue = null;
                                if (requestParameters.valueFormate().toUpperCase().startsWith("DOUBLE")) {
                                    value = rs.getDouble(requestParameters.valueColumn());
                                } else if (requestParameters.valueFormate().toUpperCase().startsWith("INTEGER")) {
                                    value = BigDecimal.valueOf(rs.getInt(requestParameters.valueColumn())).doubleValue();
                                } else if (requestParameters.valueFormate().toUpperCase().startsWith("FLOAT")) {
                                    value = BigDecimal.valueOf(rs.getFloat(requestParameters.valueColumn())).doubleValue();
                                } else if (requestParameters.valueFormate().toUpperCase().startsWith("LONG")) {
                                    value = BigDecimal.valueOf(rs.getLong(requestParameters.valueColumn())).doubleValue();
                                } else if (requestParameters.valueFormate().toUpperCase().startsWith("LONG")) {
                                    value = BigDecimal.valueOf(rs.getLong(requestParameters.valueColumn())).doubleValue();
                                } else if (requestParameters.valueFormate().toUpperCase().startsWith("STRING")) {
                                    //TODO: Support String to Double Parser with user given regex
                                    sValue = rs.getString(requestParameters.valueColumn());
                                }


                                String note = "";
                                if (!requestParameters.noteColumn().isEmpty()) {
                                    note = rs.getString(requestParameters.noteColumn());
                                }

                                if (value != null && dateTime != null) {
                                    resultSamples.add(requestParameters.getTarget().buildSample(dateTime, value, note));
                                } else if (sValue != null && dateTime != null) {
                                    resultSamples.add(requestParameters.getTarget().buildSample(dateTime, sValue, note));
                                }


                            } catch (Exception ex) {
                                logger.error("Error in Row: ", ex, ex);
                                logStatus(5, new DateTime(), "SQL Error: " + ex.toString());
                            }
                        }
                        logger.info("Importing {} sample for {}:{}:{}", resultSamples.size(), sqlServerObj.getID(), sqlServerObj.getName(), requestParameters.getTarget().getObjectID());
                        requestParameters.getTarget().addSamples(resultSamples);
                        if (resultSamples.size() > 0) {
                            requestParameters.logStatus(0, new DateTime(), "Import " + resultSamples.size() + " Samples");
                            requestParameters.updateLastReadout();
                        } else {
                            requestParameters.logStatus(0, new DateTime(), "Nothing to Import");
                        }
                        totalSamples = totalSamples + resultSamples.size();

                    } catch (Exception ex) {
                        logger.error(ex, ex);
                        logStatus(4, new DateTime(), "Error: " + ex.toString());
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
            logger.error("Error while Run SQL Driver: {}:{}",sqlServerObj.getID(),sqlServerObj.getName(),e);
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

            logStatus(0, new DateTime(), "Start Readout");


            parameters = new Parameters(sqlServerObj);
            loadDriver(parameters.driver());
        } catch (Exception ex) {
            logger.error("Error while fetching data from Server: {}:{}", sqlServerObj.getID(), sqlServerObj.getName(), ex);
            logStatus(1, new DateTime(), "Unexpected error: " + ex.toString());
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


}
