package org.jevis.mscons;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;

import org.jevis.api.JEVisType;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.Parser;
import org.jevis.commons.driver.ParserReport;
import org.jevis.commons.driver.Result;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MSCONSJEVisParser implements Parser {

    private static final Logger logger = LogManager.getLogger(MSCONSJEVisParser.class);

    private List<JEVisObject> msconsChannels = new ArrayList<>();

    List<Result> results = new ArrayList<>();

    JEVisObject parserObject;

    @Override
    public void initialize(JEVisObject parserObject) {

        this.parserObject = parserObject;
        try {
            System.out.println(getChannels(parserObject));
            msconsChannels.addAll(getChannels(parserObject));
        } catch (Exception e) {
          logger.error(e);
        }

    }

    @Override
    public void parse(List<InputStream> input, DateTimeZone timezone) {


        for (InputStream inputStream : input) {

            MsconsParser msconsParser = new MsconsParser(inputStream);


            if(!msconsParser.parse()) continue;

            MsconsPojo msconsPojo = msconsParser.getMsconsPojo();
            try {
                if (parserObject.getAttribute("File Created Date").hasSample()) {
                    if (!msconsPojo.getInterchangeHeader().getCreatedDateTime().isAfter(parserObject.getAttribute("File Created Date").getLatestSample().getTimestamp()))
                        break;

                }

            } catch (Exception e) {
                logger.error(e);
            }


            msconsChannels.forEach(jeVisObject -> {
                String messlukationnumber;
                try {

                    if (!jeVisObject.getAttribute(DataCollectorTypes.Channel.MSCONSChannel.MESSLIKATION).hasSample())
                        return;
                    messlukationnumber = jeVisObject.getAttribute(DataCollectorTypes.Channel.MSCONSChannel.MESSLIKATION).getLatestSample().getValueAsString();
                    Optional<Messlokation> messlokationOptional = msconsPojo.getMesslokation().stream().filter(nr -> nr.getMesslokationNumber().equals(messlukationnumber)).findFirst();
                    if (!messlokationOptional.isPresent()) return;
                    results.addAll(convertMSCONSSampletoResult(messlokationOptional.get().getSampleList(), getStartEnd(jeVisObject), getTargetValueAtribute(jeVisObject)));


                } catch (Exception e) {
                    logger.error(e);
                }


            });

            try {
                parserObject.getAttribute("Sender").buildSample(DateTime.now(), msconsPojo.getMessageHeader().getSender()).commit();
                parserObject.getAttribute("Recipient").buildSample(DateTime.now(), msconsPojo.getMessageHeader().getRecipient()).commit();
                parserObject.getAttribute("File Created Date").buildSample(DateTime.now(), msconsPojo.getInterchangeHeader().getCreatedDateTime()).commit();
            } catch (Exception e) {
                logger.error(e);
            }


        }


    }

    @Override
    public List<Result> getResult() {
        return results;
    }

    @Override
    public ParserReport getReport() {
        return null;
    }

    public List<JEVisObject> getChannels(JEVisObject jeVisObject) {
        List<JEVisObject> channels = new ArrayList<>();
        try {
            JEVisClass channelDirClass = jeVisObject.getDataSource().getJEVisClass(DataCollectorTypes.ChannelDirectory.MSCONShannelDirectory.NAME);
            JEVisClass channelClass = jeVisObject.getDataSource().getJEVisClass(DataCollectorTypes.Channel.MSCONSChannel.NAME);
            jeVisObject.getChildren(channelDirClass, false).forEach(dir -> {
                channels.addAll(getChannels(dir));
            });
            List<JEVisObject> channelsToBeAdded = jeVisObject.getChildren(channelClass, false);
            logger.debug("Added Channels to List {}", channelsToBeAdded);
            channels.addAll(channelsToBeAdded);
        } catch (Exception e) {
            logger.error(e);
        }
        return channels;

    }

    public List<Result> convertMSCONSSampletoResult(List<MsconsSample> sampleList, String startEnd, String targetString) {
        List<Result> resultList = null;

        if (startEnd.equals("START")) {
            resultList = sampleList.stream().map(msconsSample -> new Result(targetString, msconsSample.getQuantity(), msconsSample.getStart())).collect(Collectors.toList());

        } else if (startEnd.equals("END")) {
            resultList = sampleList.stream().map(msconsSample -> new Result(targetString, msconsSample.getQuantity(), msconsSample.getEnd())).collect(Collectors.toList());
        }

        return resultList;
    }

    private String getTargetValueAtribute(JEVisObject jsonChannel) throws JEVisException {
        JEVisClass channelClass = jsonChannel.getDataSource().getJEVisClass(DataCollectorTypes.Channel.MSCONSChannel.NAME);
        JEVisType targetIdType = channelClass.getType(DataCollectorTypes.Channel.MSCONSChannel.TARGETID);
        String targetString = DatabaseHelper.getObjectAsString(jsonChannel, targetIdType);
        return targetString;
    }

    private String getStartEnd(JEVisObject jeVisObject) {
        String StartEnd = null;
        try {
            StartEnd = jeVisObject.getAttribute("Date Time").getLatestSample().getValueAsString();
            System.out.println(StartEnd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StartEnd;


    }

}
