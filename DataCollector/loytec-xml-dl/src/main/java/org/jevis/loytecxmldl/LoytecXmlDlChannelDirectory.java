package org.jevis.loytecxmldl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;

import java.util.ArrayList;
import java.util.List;

import static org.jevis.loytecxmldl.LoytecXmlDlGenericChannelDirectoryClass.NAME_GENERIC;
import static org.jevis.loytecxmldl.LoytecXmlDlGenericChannelDirectoryClass.TECHNOLOGY;
import static org.jevis.loytecxmldl.LoytecXmlDlSpecializedChannelDirectoryClass.LOYTEC_XML_DL_SPECIALIZED_CHANNEL_DIRECTORIES;

/**
 * This implements the channel directory and related functions
 */
public class LoytecXmlDlChannelDirectory implements LoytecXmlDlChannelDirectoryClass {

    private final static Logger log = LogManager.getLogger(LoytecXmlDlChannelDirectory.class.getName());

    private final String name;
    private final List<LoytecXmlDlChannel> channels = new ArrayList<>();
    private String technology;

    public LoytecXmlDlChannelDirectory(JEVisObject channelDirectoryObject) {

        log.debug("Create LoytecXmlDlChannelDirectory");

        log.debug("Getting the name");
        name = channelDirectoryObject.getName();

        //todo check special classes
        String channelType = null;
        try {
            channelType = channelDirectoryObject.getJEVisClass().getName();
        } catch (JEVisException e) {
            log.error(e);
        }

        log.debug("Getting the technology");
        technology = DEFAULT_TECHNOLOGY;
        for (LoytecXmlDlSpecializedChannelDirectory loytecXmlDlSpecializedChannelDirectory : LOYTEC_XML_DL_SPECIALIZED_CHANNEL_DIRECTORIES) {
            if (channelType.equals(loytecXmlDlSpecializedChannelDirectory.getName())) {
                log.debug("Getting technology for specialized directory");
                technology = loytecXmlDlSpecializedChannelDirectory.getTechnology();
            } else if (channelType.equals(NAME_GENERIC)) {
                log.debug("Getting technology attribute of generic channel directory");
                Helper helper = new Helper();
                helper.getValue(channelDirectoryObject, TECHNOLOGY, DEFAULT_TECHNOLOGY);
            }
        }

        // Add all channels
        try {
            log.debug("Getting the channels");
            JEVisClass channelClass = channelDirectoryObject.getDataSource().getJEVisClass(LoytecXmlDlChannelClass.NAME);

            List<JEVisObject> listChannels = new ArrayList<>();
            channelDirectoryObject.getChildren().forEach(jeVisObject -> {
                try {
                    if (jeVisObject.getJEVisClass().equals(channelClass)) listChannels.add(jeVisObject);
                } catch (JEVisException e) {
                    log.error(e);
                }
            });
            log.info("Found " + listChannels.size() + " channel objects in " + channelDirectoryObject.getName() + ":" + channelDirectoryObject.getID());

            List<Long> counterCheckForErrorInAPI = new ArrayList<>();
            listChannels.forEach(channelObject -> {
                try {
                    JEVisAttribute attTargetId = channelObject.getAttribute(LoytecXmlDlChannel.TARGET_ID);
                    JEVisAttribute attTrendId = channelObject.getAttribute(LoytecXmlDlChannel.TREND_ID);
//                    JEVisAttribute attLastReadOut = channelObject.getAttribute(LoytecXmlDlChannel.LAST_READOUT);
//                    if (attTargetId.hasSample() && attTrendId.hasSample() && attLastReadOut.hasSample()) {
                    if (attTargetId.hasSample() && attTrendId.hasSample()) {
                        if (!counterCheckForErrorInAPI.contains(channelObject.getID())) {
                            channels.add(new LoytecXmlDlChannel(channelObject));
                            counterCheckForErrorInAPI.add(channelObject.getID());
                            log.debug("Channel added");
                        }
                    }
                } catch (Exception e) {
                    log.error("No valid channel Configuration for channel: " + channelObject.getName() + ":" + channelObject.getID());
                    log.debug(e);
                }

            });
        } catch (JEVisException e) {
            log.error("Error while adding channels");
            log.debug(e.getMessage());
        }

        log.info(channelDirectoryObject.getName() + ":" + channelDirectoryObject.getID() + " has " + channels.size() + " channels.");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTechnology() {
        return technology;
    }

    @Override
    public List<LoytecXmlDlChannel> getChannels() {
        return channels;
    }
}
