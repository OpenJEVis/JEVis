package org.jevis.loytecxmldl;

import org.jevis.api.JEVisObject;
import org.jevis.commons.driver.DataCollectorTypes;
import org.joda.time.DateTimeZone;

import java.util.List;

/**
 * JEVis Class: Loytec XML-DL Dataserver
 */
public interface LoytecXmlDlServerClass extends DataCollectorTypes.DataSource.DataServer {

    // Settings
    /**
     * max is 500
     */
    Integer NUMBER_OF_SAMPLES_PER_REQUEST = 500;

    // JEVis class mapping strings
    String NAME = "Loytec XML-DL Server";
    String USER = "User";
    String PASSWORD = "Password";
    String SSL = "SSL";
    String LOGHANDLE_BASE_PATH = "LogHandle Base Path";

    // Default values
    String USER_DEFAULT = "operator";
    String DEFAULT_PASSWORD = "loytec4u";
    String DEFAULT_SSL = "false";
    String DEFAULT_PORT = "80";
    String DEFAULT_CONNECTION_TIMEOUT = "30";
    String DEFAULT_READ_TIMEOUT = "60";
    String DEFAULT_LOGHANDLE_BASE_PATH = "00/var/lib/";
    String DEFAULT_TIMEZONE = "UTC";

    // Methods to implement
    String getHost();

    Integer getPort();

    Integer getConnectionTimeout();

    Integer getReadTimeout();

    Boolean getSsl();

    String getName();

    String getUser();

    String getPassword();

    String getLogHandleBasePath();

    DateTimeZone getTimezone();

    List<LoytecXmlDlChannelDirectory> getChannelDirectories();

    JEVisObject getObject();
}
