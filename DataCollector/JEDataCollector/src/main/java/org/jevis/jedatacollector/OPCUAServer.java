package org.jevis.jedatacollector;

import loytecxmldl.jevis.Helper;
import loytecxmldl.jevis.LoytecXmlDlChannelDirectory;
import loytecxmldl.jevis.LoytecXmlDlChannelDirectoryClass;
import loytecxmldl.jevis.LoytecXmlDlServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.DataCollectorTypes;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;

import static org.jevis.commons.driver.DataCollectorTypes.DataSource.TIMEZONE;

public class OPCUAServer {

    private final static Logger log = LogManager.getLogger(LoytecXmlDlServer.class.getName());

    String USER = "User";
    String PASSWORD = "Password";
    String SSL = "SSL";
    String USER_DEFAULT = "operator";
    String DEFAULT_PASSWORD = "loytec4u";
    String DEFAULT_SSL = "false";
    String DEFAULT_PORT = "80";
    String DEFAULT_CONNECTION_TIMEOUT = "30";
    String DEFAULT_READ_TIMEOUT = "60";

    private String host;
    private Integer port;
    private Integer connectionTimeout;
    private Integer readTimeout;
    private Boolean ssl;
    private String name;
    private String user;
    private String password;
    private String logHandleBasePath;
    private DateTimeZone timezone;
    private List<LoytecXmlDlChannelDirectory> channelDirectories = new ArrayList<>();

    public OPCUAServer(JEVisObject dataSourceObject) {


        Helper helper = new Helper();
        host = helper.getValue(dataSourceObject, DataCollectorTypes.DataSource.DataServer.HOST);
        log.debug("LoytecXmlDlServer - Host: " + host);
        port = Integer.parseInt(helper.getValue(dataSourceObject, DataCollectorTypes.DataSource.DataServer.PORT, DEFAULT_PORT));
        log.debug("LoytecXmlDlServer - Port: " + port);
        connectionTimeout = Integer.parseInt(helper.getValue(dataSourceObject, DataCollectorTypes.DataSource.DataServer.CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT));
        log.debug("LoytecXmlDlServer - Connection Timeout: " + connectionTimeout);
        readTimeout = Integer.parseInt(helper.getValue(dataSourceObject, DataCollectorTypes.DataSource.DataServer.READ_TIMEOUT, DEFAULT_READ_TIMEOUT));
        log.debug("LoytecXmlDlServer - Read Timeout: " + readTimeout);
        user = helper.getValue(dataSourceObject, USER, USER_DEFAULT);
        log.debug("LoytecXmlDlServer - User: " + user);
        password = helper.getValue(dataSourceObject, PASSWORD, DEFAULT_PASSWORD);
        log.debug("LoytecXmlDlServer - Password: "+password );
        ssl = Boolean.valueOf(helper.getValue(dataSourceObject, SSL, DEFAULT_SSL));
        log.debug("LoytecXmlDlServer - Ssl: " + ssl);



        JEVisClass loytecXmlDlServerClass = null;
        try {
            loytecXmlDlServerClass = dataSourceObject.getJEVisClass();
        } catch (JEVisException e) {
            e.printStackTrace();
        }
        JEVisType timezoneType = null;
        try {
            if (loytecXmlDlServerClass != null) {
                timezoneType = loytecXmlDlServerClass.getType(TIMEZONE);
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }
        String timezoneString = null;
        try {
            timezoneString = DatabaseHelper.getObjectAsString(dataSourceObject, timezoneType);
        } catch (JEVisException e) {
            e.printStackTrace();
        }
        if (timezoneString != null) {
            timezone = DateTimeZone.forID(timezoneString);
        } else {
            timezone = DateTimeZone.UTC;
        }
        log.debug("LoytecXmlDlServer - Timezone: " + timezone);

        // Get all data channel directories
        try {
            log.debug("Getting the channel directories (type: " + LoytecXmlDlChannelDirectoryClass.NAME + ")");
            // Get channel directory class
            JEVisClass loytecXmlDlChannelDirectoryClass = dataSourceObject.getDataSource().getJEVisClass(LoytecXmlDlChannelDirectoryClass.NAME);
            List<Long> checkForAPIError = new ArrayList<>();
            getChannelDirectoriesRecursive(dataSourceObject, loytecXmlDlChannelDirectoryClass, channelDirectories, checkForAPIError);
            log.info("Found " + channelDirectories.size() + " channel directories.");

        } catch (JEVisException ex) {
            log.error("Error while getting data channel directories");
            log.debug(ex.getMessage());
        }



    }

    private void getChannelDirectoriesRecursive(JEVisObject parent, JEVisClass loytecXmlDlChannelDirectoryClass, List<LoytecXmlDlChannelDirectory> children, List<Long> checkForAPIError) throws JEVisException {

        // Get all children objects of data server object with class channel directory
        for (JEVisObject channelDirectoryObject : parent.getChildren(loytecXmlDlChannelDirectoryClass, true)) {
            log.debug("Channel Directory found");
            if (!checkForAPIError.contains(channelDirectoryObject.getID())) {
                children.add(new LoytecXmlDlChannelDirectory(channelDirectoryObject));
                checkForAPIError.add(channelDirectoryObject.getID());
                getChannelDirectoriesRecursive(channelDirectoryObject, loytecXmlDlChannelDirectoryClass, children, checkForAPIError);
            }
        }
    }
}
