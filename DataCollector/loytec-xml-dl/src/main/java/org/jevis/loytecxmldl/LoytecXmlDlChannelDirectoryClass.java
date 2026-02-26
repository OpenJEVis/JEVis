package org.jevis.loytecxmldl;

import org.jevis.commons.driver.DataCollectorTypes;

import java.util.List;

/**
 * JEVis Class: Loytec XML-DL Channel Directory
 */
public interface LoytecXmlDlChannelDirectoryClass extends DataCollectorTypes.ChannelDirectory {
    // JEVis class mapping strings
    String NAME = "Loytec XML-DL Channel Directory";

    // Default values
    String DEFAULT_TECHNOLOGY = "dpal";

    // Methods to implement
    String getName();

    String getTechnology();

    List<LoytecXmlDlChannel> getChannels();

    List<LoytecXmlDlOutputChannel> getOutputChannels();
}