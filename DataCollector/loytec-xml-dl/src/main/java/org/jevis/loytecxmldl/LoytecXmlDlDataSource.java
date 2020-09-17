package org.jevis.loytecxmldl;

import org.apache.commons.io.IOUtils;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.loytecxmldl.jevis.*;
import org.jevis.soapdatasource.Channel;
import org.jevis.soapdatasource.SOAPDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Loytec XML-DL Data Source
 */
public class LoytecXmlDlDataSource implements DataSource {

    private final static Logger log = LogManager.getLogger(LoytecXmlDlDataSource.class.getName());

    private LoytecXmlDlServerClass dataServer;
    private SOAPDataSource soapDataSource;
    private Channel soapChannel;
    private Importer importer;

    @Override
    public void initialize(JEVisObject dataSourceObject) {

        log.info("initialize called");

        dataServer = new LoytecXmlDlServer(dataSourceObject);
        System.setProperty("java.net.useSystemProxies", "true");

        // Initialize the datasource

        soapDataSource = new SOAPDataSource();
        soapDataSource.setHost(dataServer.getHost());
        soapDataSource.setPort(dataServer.getPort());
        soapDataSource.setConnectionTimeout(dataServer.getConnectionTimeout());
        soapDataSource.setReadTimeout(dataServer.getReadTimeout());
        soapDataSource.setSsl(dataServer.getSsl());
        soapDataSource.setUserName(dataServer.getUser());
        soapDataSource.setPassword(dataServer.getPassword());
        soapDataSource.set_timezone(dataServer.getTimezone());

        // Initialize the importer
        importer = ImporterFactory.getImporter(dataSourceObject);
        log.info("Importer created");
        if (importer != null) {
            log.info("Init importer");
            importer.initialize(dataSourceObject);
            log.info("Init importer completed");
        } else {
            log.warn("Could not get Importer");
        }
        log.info("Initialization completed");
    }

    @Override
    public void run() {
        log.info("Running");
        // For every channel directory
        for (LoytecXmlDlChannelDirectory channelDirectory : dataServer.getChannelDirectories()) {

            ExecutorService executorService = Executors.newFixedThreadPool(15);

            try {
                channelDirectory.getChannels().forEach(channel -> {
                    try {
                        executorService.submit(() -> {
                            // For every channel

                            List<InputStream> responseStreams;
                            List<Result> results;
                            List<JEVisSample> statusResults;

                            // Create parser
                            LoytecXmlDlParser parser = new LoytecXmlDlParser();
                            parser.initChannel(channel);

                            // Send request
                            try {
                                responseStreams = this.sendSampleRequest(channelDirectory, channel);
                                if (responseStreams.isEmpty()) {
                                    log.info("The sample request response is empty");
                                    printResponse(responseStreams);
                                } else {
                                    log.info("The sample request response is ok");
                                    //printResponse(responseStreams);
                                }

                                // Parse the response
                                parser.initChannel(channel);

                                results = parser.parseStream(responseStreams, dataServer.getTimezone());
                                statusResults = parser.getStatusResults();

                                if (results.isEmpty() && statusResults.isEmpty()) {
                                    log.info("The parse result is empty");
                                    // use stream, no use later...
                                } else {
                                    log.info("The parse result is ok. Results: {}. Status results: {}", results.size(), statusResults.size());
                                    // Import
                                    JEVisImporterAdapter.importResults(results, statusResults, importer, channel.getJeVisObject());
                                }
                            } catch (
                                    MalformedURLException ex) {
                                logger.error("MalformedURLException. For channel {}:{}. {}", channel.getJeVisObject().getID(), channel.getName(), ex.getMessage());
                                logger.debug("MalformedURLException. For channel {}:{}", channel.getJeVisObject().getID(), channel.getName(), ex);
                            } catch (
                                    ClientProtocolException ex) {
                                logger.error("Exception. For channel {}:{}. {}", channel.getJeVisObject().getID(), channel.getName(), ex.getMessage());
                                logger.debug("Exception. For channel {}:{}", channel.getJeVisObject().getID(), channel.getName(), ex);
                            } catch (
                                    IOException ex) {
                                logger.error("IO Exception. For channel {}:{}. {}", channel.getJeVisObject().getID(), channel.getName(), ex.getMessage());
                                logger.debug("IO Exception. For channel {}:{}.", channel.getJeVisObject().getID(), channel.getName(), ex);
                            } catch (ParseException ex) {
                                logger.error("Parse Exception. For channel {}:{}. {}", channel.getJeVisObject().getID(), channel.getName(), ex.getMessage());
                                logger.debug("Parse Exception. For channel {}:{}", channel.getJeVisObject().getID(), channel.getName(), ex);
                            } catch (Exception ex) {
                                logger.error("Exception. For channel {}:{}", channel.getJeVisObject().getID(), channel.getName(), ex);
                            }
                        }).get();
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("Thread Pool was interrupted or execution was stopped: " + e);
                    }

                });
            } finally {
                executorService.shutdown();
            }

            log.info("Run completed");
        }
    }

    private List<InputStream> sendSampleRequest(LoytecXmlDlChannelDirectory
                                                        channelDirectoryObject, LoytecXmlDlChannel channelObject) throws Exception {

        LoytecXmlDlSoapRequestTemplate requestTemplate = new LoytecXmlDlSoapRequestTemplate(channelDirectoryObject, channelObject);
        requestTemplate.setLogHandle(dataServer.getLogHandleBasePath());
        Channel soapChannel = new Channel();
        soapChannel.setLastReadout(channelObject.getLastReadout());
        soapChannel.setPath("/DL/");
        soapChannel.setTemplate(requestTemplate.getTemplate());

        return soapDataSource.sendSampleRequest(soapChannel);
    }

    @Override
    public List<InputStream> sendSampleRequest(JEVisObject dataSourceChannelObject) {
        // not used see other sendSampleRequest(...) method
        return null;
    }

    @Override
    public void parse(List<InputStream> list) {
        // Not used, see run method
    }

    @Override
    public void importResult() {
        // Not used, see run method
    }

    /**
     * Debug: print out soap response
     */
    private void printResponse(List<InputStream> response) {
        //log.debug("-- DEBUG RESPONSE --");
        log.info(" --- DEBUG RESPONSE ---");
        for (InputStream responseStream : response) {
            try {
                String text = IOUtils.toString(responseStream, StandardCharsets.UTF_8.name());
                //log.debug(text);
                log.debug(text);
            } catch (IOException e) {
                log.error(e);
            }
        }
    }
}