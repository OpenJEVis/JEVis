package org.jevis.loytecxmldl;


import org.apache.commons.io.IOUtils;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.driver.*;
import org.jevis.soapdatasource.Channel;
import org.jevis.soapdatasource.SOAPDataSource;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Loytec XML-DL Data Source
 */
public class LoytecXmlDlDataSource implements DataSource {

    private final static Logger log = LogManager.getLogger(LoytecXmlDlDataSource.class.getName());
    private final ConcurrentHashMap<JEVisObject, DateTime> activeChannels = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<JEVisObject, DateTime> channels = new ConcurrentHashMap<>();
    private LoytecXmlDlServerClass dataServer;
    private SOAPDataSource soapDataSource;
    private Importer importer;
    private ExecutorService executorService;
    private final OPCUAWriter opcuaWriter = new OPCUAWriter();

    @Override
    public void initialize(JEVisObject dataSourceObject) {
        log.info("initialize called for: {}", dataSourceObject);

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
        DateTime start = new DateTime();
        // For every channel directory
        executorService = Executors.newFixedThreadPool(15);

        for (LoytecXmlDlChannelDirectory channelDirectory : dataServer.getChannelDirectories()) {

            channelDirectory.getChannels().forEach(channel -> {
                Runnable runnable = getJob(channelDirectory, channel);

                FutureTask<?> ft = new FutureTask<Void>(runnable, null);
                channels.put(channel.getJeVisObject(), new DateTime());
                executorService.submit(ft);
            });

            channelDirectory.getOutputChannels().forEach(channel -> {
                Runnable runnable = getJob(channelDirectory, channel);

                FutureTask<?> ft = new FutureTask<Void>(runnable, null);
                channels.put(channel.getJeVisObject(), new DateTime());
                executorService.submit(ft);
            });
        }

        waitForExecutorService(1800);

        DateTime end = new DateTime();

        log.info("Run completed in {}", new Period(start, end).toString(PeriodFormat.wordBased()));
    }

    private void waitForExecutorService(int timeout) {
        while (timeout > 0 && !activeChannels.isEmpty()) {
            try {
                log.info("Waiting for timeout: {}s and {} active channels from {} total.", timeout, activeChannels.size(), channels.size());
                timeout = timeout - 1;
                Thread.sleep(1000);
                waitForExecutorService(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Runnable getJob(LoytecXmlDlChannelDirectory channelDirectory, LoytecXmlDlChannel channel) {
        return () -> {
            // For every channel

            List<InputStream> responseStreams;
            List<Result> results;
            List<JEVisSample> statusResults;
            activeChannels.put(channel.getJeVisObject(), new DateTime());

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
                    removeJob(channel.getJeVisObject());

                    // use stream, no use later...
                } else {
                    log.info("The parse result is ok. Results: {}. Status results: {} for channel {}", results.size(), statusResults.size(), channel.getName());
                    //check for results beyond readout scope
                    try {
                        List<Result> outOfScopeResults = results.stream().filter(result -> result.getDate().isBefore(channel.getLastReadout())).collect(Collectors.toList());
                        List<JEVisSample> outOfScopeStatusResults = new ArrayList<>();
                        for (JEVisSample jeVisSample : statusResults) {
                            if (jeVisSample.getTimestamp().isBefore(channel.getLastReadout())) {
                                outOfScopeStatusResults.add(jeVisSample);
                            }
                        }

                        if (!outOfScopeResults.isEmpty() || !outOfScopeStatusResults.isEmpty()) {
                            logger.info("Found {} out of scope results and {} out of scope status results for channel {}.", outOfScopeResults.size(), outOfScopeStatusResults.size(), channel.getName());
                        }

                        results.removeAll(outOfScopeResults);
                        statusResults.removeAll(outOfScopeStatusResults);
                    } catch (Exception e) {
                        logger.error("Error while checking results for not requested timestamps", e);
                    }

                    // Import
                    JEVisImporterAdapter.importResults(results, statusResults, importer, channel.getJeVisObject());

                    if (results.size() == 1000) {
                        channel.update();
                        Runnable runnable = getJob(channelDirectory, channel);

                        FutureTask<?> ft = new FutureTask<Void>(runnable, null);
                        executorService.submit(ft);
                    } else {
                        removeJob(channel.getJeVisObject());
                    }
                }
            } catch (MalformedURLException ex) {
                log.error("MalformedURLException. For channel {}:{}. {}", channel.getJeVisObject().getID(), channel.getName(), ex.getMessage());
                log.debug("MalformedURLException. For channel {}:{}", channel.getJeVisObject().getID(), channel.getName(), ex);
                removeJob(channel.getJeVisObject());
            } catch (ClientProtocolException ex) {
                log.error("Exception. For channel {}:{}. {}", channel.getJeVisObject().getID(), channel.getName(), ex.getMessage());
                log.debug("Exception. For channel {}:{}", channel.getJeVisObject().getID(), channel.getName(), ex);
                removeJob(channel.getJeVisObject());
            } catch (IOException ex) {
                log.error("IO Exception. For channel {}:{}. {}", channel.getJeVisObject().getID(), channel.getName(), ex.getMessage());
                log.debug("IO Exception. For channel {}:{}.", channel.getJeVisObject().getID(), channel.getName(), ex);
                removeJob(channel.getJeVisObject());
            } catch (ParseException ex) {
                log.error("Parse Exception. For channel {}:{}. {}", channel.getJeVisObject().getID(), channel.getName(), ex.getMessage());
                log.debug("Parse Exception. For channel {}:{}", channel.getJeVisObject().getID(), channel.getName(), ex);
                removeJob(channel.getJeVisObject());
            } catch (Exception ex) {
                log.error("Exception. For channel {}:{}", channel.getJeVisObject().getID(), channel.getName(), ex);
                removeJob(channel.getJeVisObject());
            }
        };
    }

    private Runnable getJob(LoytecXmlDlChannelDirectory channelDirectory, LoytecXmlDlOutputChannel channel) {
        return () -> {
            // For every channel

            List<InputStream> responseStreams;
            List<Result> results;
            List<JEVisSample> statusResults;
            activeChannels.put(channel.getJeVisObject(), new DateTime());
            try {

                opcuaWriter.connectToOPCUAServer(dataServer.getObject());

                JEVisObject dataObject = channel.getTarget().getObject();

                if (opcuaWriter.sendOPCUANotification(channel.getJeVisObject(), dataObject)) {
                    setLastReadout(channel.getJeVisObject(), dataObject.getAttribute("Value").getTimestampOfLastSample());
                }

            } catch (UaException | ExecutionException | InterruptedException e) {
                log.error(e);

                OPCUAStatus opcUAStatus = new OPCUAStatus(OPCUAStatus.OPC_SERVER_NOT_REACHABLE);
                opcUAStatus.writeStatus(channel.getJeVisObject(), DateTime.now());
                removeJob(channel.getJeVisObject());
            } catch (Exception e) {
                log.error(e);
                removeJob(channel.getJeVisObject());
            }

            opcuaWriter.disconnect();
        };
    }

    private void setLastReadout(JEVisObject outputChannel, DateTime dateTime) throws JEVisException {
        outputChannel.getAttribute(DataCollectorTypes.Channel.LAST_READOUT).buildSample(DateTime.now(), dateTime.toString()).commit();
    }

    private void removeJob(JEVisObject channel) {
        activeChannels.remove(channel);
        channels.remove(channel);

        if (activeChannels.isEmpty()) {
            log.debug("No more active threads, shutting down executor.");
            executorService.shutdown();
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
                String text = IOUtils.toString(responseStream, StandardCharsets.UTF_8);
                //log.debug(text);
                log.debug(text);
            } catch (IOException e) {
                log.error(e);
            }
        }
    }
}