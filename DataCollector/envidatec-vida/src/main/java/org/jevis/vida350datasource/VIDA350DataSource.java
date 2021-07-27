/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.vida350datasource;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.*;
import org.jevis.commons.driver.inputHandler.GenericConverter;
import org.jevis.csvparser.CSVParser;
import org.jevis.csvparser.DataPoint;
import org.jevis.httpdatasource.Channel;
import org.jevis.httpdatasource.HTTPDataSource;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


/**
 * @author broder
 */
public class VIDA350DataSource implements DataSource {
    private static final Logger log = LogManager.getLogger(VIDA350DataSource.class);
    private final List<JEVisObject> _channels = new ArrayList<>();
    private final String INDEX = "Index";
    private final String TARGET = "Target";
    private final String LAST_READOUT = "Last Readout";
    private DateTimeZone _timeZone;
    private CSVParser _csvParser;
    private List<Result> _result;
    private Importer _importer;
    private HTTPDataSource _httpdatasource;
    private DateTime dateTime;

    @Override
    public void parse(List<InputStream> input) {
        _csvParser.parse(input, _httpdatasource.getDateTimeZone());
        _result = _csvParser.getResult();
    }

    @Override
    public void importResult() {
        //        _importer.importResult(_result);
        //workaround until server is threadsave
//        JEVisImporterAdapter.importResults(_result, _importer);
    }

    @Override
    public void initialize(JEVisObject httpObject) {
        initializeAttributes(httpObject);
        initializeChannelObjects(httpObject);

        _importer = ImporterFactory.getImporter(httpObject);
        _importer.initialize(httpObject);

    }

    private void initializeAttributes(JEVisObject httpObject) {
        try {
            JEVisClass httpType = httpObject.getDataSource().getJEVisClass(VIDA350.NAME);
            JEVisType server = httpType.getType(VIDA350.HOST);
            JEVisType portType = httpType.getType(VIDA350.PORT);
            JEVisType connectionTimeoutType = httpType.getType(VIDA350.CONNECTION_TIMEOUT);
            JEVisType readTimeoutType = httpType.getType(VIDA350.READ_TIMEOUT);
            JEVisType timezoneType = httpType.getType(VIDA350.TIMEZONE);
            JEVisType enableType = httpType.getType(VIDA350.ENABLE);

            String serverURL = DatabaseHelper.getObjectAsString(httpObject, server);
            Integer port = DatabaseHelper.getObjectAsInteger(httpObject, portType);
            Integer connectionTimeout = DatabaseHelper.getObjectAsInteger(httpObject, connectionTimeoutType);
            Integer readTimeout = DatabaseHelper.getObjectAsInteger(httpObject, readTimeoutType);
            String timeZone = DatabaseHelper.getObjectAsString(httpObject, timezoneType);
            if (timeZone != null) {
                _timeZone = DateTimeZone.forID(timeZone);
            } else {
                _timeZone = DateTimeZone.UTC;
            }

            _httpdatasource = new HTTPDataSource();
            _httpdatasource.setId(httpObject.getID());
            _httpdatasource.setName(httpObject.getName());
            _httpdatasource.setConnectionTimeout(connectionTimeout);
            _httpdatasource.setPassword("");
            _httpdatasource.setPort(port);
            _httpdatasource.setReadTimeout(readTimeout);
            _httpdatasource.setServerURL(serverURL);
            _httpdatasource.setSsl(Boolean.FALSE);
            _httpdatasource.setUserName("");
            _httpdatasource.setDateTimeZone(_timeZone);

        } catch (JEVisException ex) {
            log.error(ex);
        }
    }

    private CSVParser getNewParser(JEVisObject channel) {
        CSVParser parser = new CSVParser();
        try {
            parser.setDateFormat("dd.MM.yyyy");
            parser.setDateIndex(2);
            parser.setDecimalSeperator(".");
            parser.setDelim(";");
            parser.setDpIndex(0);
            parser.setHeaderLines(2);
            parser.setQuote(null);
            parser.setThousandSeperator(null);
            parser.setTimeFormat("HH:mm:ss");
            parser.setTimeIndex(1);
            parser.setDpType("ROW");
            parser.setCharset(StandardCharsets.UTF_8);

            List<DataPoint> csvdatapoints = new ArrayList<DataPoint>();
            JEVisClass channelClass = channel.getJEVisClass();
            JEVisType targetType = channelClass.getType(VIDA350ChannelTypes.TARGET);
            String target = DatabaseHelper.getObjectAsString(channel, targetType);
            DataPoint csvDataPoint = new DataPoint();
            csvDataPoint.setTarget(target);
            csvDataPoint.setValueIndex(0);
            csvdatapoints.add(csvDataPoint);

            parser.setDataPoints(csvdatapoints);

            parser.setConverter(new GenericConverter());
        } catch (JEVisException ex) {
            log.fatal(ex);
        }
        return parser;
    }

    @Override
    public void run() {
        log.info("VIDA Driver started");

        for (JEVisObject channel : _channels) {

            try {
                _result = new ArrayList<Result>();
                dateTime = null;

                try {
                    List<InputStream> input = this.sendSampleRequest(channel);

                    if (dateTime != null) {
                        try {
                            JEVisClass channelClass = channel.getJEVisClass();
                            JEVisType readoutTryType = channelClass.getType(VIDA350ChannelTypes.LAST_READOUT_TRY);
                            JEVisAttribute attribute = channel.getAttribute(readoutTryType);
                            JEVisSample jeVisSample = attribute.buildSample(new DateTime(), dateTime);
                            jeVisSample.commit();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                    _csvParser = getNewParser(channel);

                    if (!input.isEmpty()) {
                        this.parse(input);
                    }

                    if (!_result.isEmpty()) {

//                    this.importResult();
//
//                    DataSourceHelper.setLastReadout(channel, _importer.getLatestDatapoint());
                        JEVisImporterAdapter.importResults(_result, _importer, channel);
                    }
                } catch (MalformedURLException ex) {
                    log.error("MalformedURLException. For channel {}:{}. {}", channel.getID(), channel.getName(), ex.getMessage());
                    log.debug("MalformedURLException. For channel {}:{}", channel.getID(), channel.getName(), ex);
                } catch (ClientProtocolException ex) {
                    log.error("Exception. For channel {}:{}. {}", channel.getID(), channel.getName(), ex.getMessage());
                    log.debug("Exception. For channel {}:{}", channel.getID(), channel.getName(), ex);
                } catch (IOException ex) {
                    log.error("IO Exception. For channel {}:{}. {}", channel.getID(), channel.getName(), ex.getMessage());
                    log.debug("IO Exception. For channel {}:{}.", channel.getID(), channel.getName(), ex);
                } catch (ParseException ex) {
                    log.error("Parse Exception. For channel {}:{}. {}", channel.getID(), channel.getName(), ex.getMessage());
                    log.debug("Parse Exception. For channel {}:{}", channel.getID(), channel.getName(), ex);
                } catch (Exception ex) {
                    log.error("Exception. For channel {}:{}", channel.getID(), channel.getName(), ex);
                }
            } catch (Exception ex) {
                log.fatal(ex);
            }
        }
    }

    /**
     * komplett überarbeiten!!!!!
     *
     * @param channel
     * @return
     */
    @Override
    public List<InputStream> sendSampleRequest(JEVisObject channel) throws Exception {
        Channel httpChannel = new Channel();

        try {
            JEVisClass channelClass = channel.getJEVisClass();
            JEVisType dpType = channelClass.getType(VIDA350ChannelTypes.INDEX);
            String dp = DatabaseHelper.getObjectAsString(channel, dpType);
            JEVisType readoutType = channelClass.getType(VIDA350ChannelTypes.LAST_READOUT);
            JEVisType readoutTryType = channelClass.getType(VIDA350ChannelTypes.LAST_READOUT_TRY);
            DateTime lastReadout = DatabaseHelper.getObjectAsDate(channel, readoutType);
            DateTime lastReadoutTry = DatabaseHelper.getObjectAsDate(channel, readoutTryType);

            if (lastReadoutTry.minusDays(3).isAfter(lastReadout)) {
                lastReadout = lastReadoutTry;
            }

            httpChannel.setLastReadout(lastReadout);
            DateTimeFormatter dtf = DateTimeFormat.forPattern("ddMMyyyyHHmmss");
            String from = lastReadout.toString(dtf.withZone(_httpdatasource.getDateTimeZone()));
            dateTime = new DateTime();
            String until = dateTime.toString(dtf.withZone(_httpdatasource.getDateTimeZone()));

            String path = "DP" + dp + "-" + from + "-" + until;
            httpChannel.setPath(path);
        } catch (JEVisException ex) {
            log.fatal(ex);
        }

        return _httpdatasource.sendSampleRequest(httpChannel);
    }

    private void initializeChannelObjects(JEVisObject httpObject) {
        try {
            JEVisClass channelDirClass = httpObject.getDataSource().getJEVisClass(VIDA350ChannelDirectoryTypes.NAME);
            JEVisObject channelDir = httpObject.getChildren(channelDirClass, false).get(0);

            JEVisClass channelClass = httpObject.getDataSource().getJEVisClass(VIDA350ChannelTypes.NAME);

            List<JEVisObject> listChannels = new ArrayList<>();
            channelDir.getChildren().forEach(jeVisObject -> {
                try {
                    if (jeVisObject.getJEVisClass().equals(channelClass)) listChannels.add(jeVisObject);
                } catch (JEVisException e) {
                    log.error(e);
                }
            });
            log.info("Found " + listChannels.size() + " channel objects in " + channelDir.getName() + ":" + channelDir.getID());

            List<Long> counterCheckForErrorInAPI = new ArrayList<>();
            listChannels.forEach(channelObject -> {
                try {
                    JEVisAttribute attTarget = channelObject.getAttribute(TARGET);
                    JEVisAttribute attIndex = channelObject.getAttribute(INDEX);
//                    JEVisAttribute attLastReadOut = channelObject.getAttribute(LAST_READOUT);
//                    if (attTarget.hasSample() && attIndex.hasSample() && attLastReadOut.hasSample()) {
                    if (attTarget.hasSample() && attIndex.hasSample()) {
                        if (!counterCheckForErrorInAPI.contains(channelObject.getID())) {
                            _channels.add(channelObject);
                            counterCheckForErrorInAPI.add(channelObject.getID());
                            log.debug("Channel added");
                        }
                    }
                } catch (Exception e) {
                    log.error("No valid channel Configuration for channel: " + channelObject.getName() + ":" + channelObject.getID());
                    log.debug(e);
                }

            });

            log.info(channelDir.getName() + ":" + channelDir.getID() + " has " + _channels.size() + " channels.");
        } catch (Exception ex) {
            log.fatal(ex);
        }
    }

    interface VIDA350 extends DataCollectorTypes.DataSource.DataServer {

        String NAME = "VIDA350";
        String PASSWORD = "Password";
        String SSL = "SSL";
        String USER = "User";
    }

    interface VIDA350ChannelDirectoryTypes extends DataCollectorTypes.ChannelDirectory {

        String NAME = "VIDA350 Channel Directory";
    }

    public interface VIDA350ChannelTypes extends DataCollectorTypes.Channel {

        String NAME = "VIDA350 Channel";
        String TARGET = "Target";
        String INDEX = "Index";
        String LAST_READOUT_TRY = "Last Readout Try";
    }
}
