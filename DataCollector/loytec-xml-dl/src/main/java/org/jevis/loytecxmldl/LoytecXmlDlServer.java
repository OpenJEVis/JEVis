package org.jevis.loytecxmldl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.commons.DatabaseHelper;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;

/**
 * This implements the server and related functions
 */
public class LoytecXmlDlServer implements LoytecXmlDlServerClass {

    private final static Logger log = LogManager.getLogger(LoytecXmlDlServer.class.getName());

    private final String host;
    private final Integer port;
    private final Integer connectionTimeout;
    private final Integer readTimeout;
    private final Boolean ssl;
    private final String name;
    private final String user;
    private final String password;
    private final String logHandleBasePath;
    private final DateTimeZone timezone;
    private final List<LoytecXmlDlChannelDirectory> channelDirectories = new ArrayList<>();
    private final JEVisObject object;

    public LoytecXmlDlServer(JEVisObject dataSourceObject) {

        log.debug("Create LoytecXmlDlServer Object");

        this.object = dataSourceObject;

        name = dataSourceObject.getName();
        log.debug("LoytecXmlDlServer - Name: " + name);

        Helper helper = new Helper();
        host = helper.getValue(dataSourceObject, DataServer.HOST);
        log.debug("LoytecXmlDlServer - Host: " + host);
        port = Integer.parseInt(helper.getValue(dataSourceObject, DataServer.PORT, DEFAULT_PORT));
        log.debug("LoytecXmlDlServer - Port: " + port);
        connectionTimeout = Integer.parseInt(helper.getValue(dataSourceObject, DataServer.CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT));
        log.debug("LoytecXmlDlServer - Connection Timeout: " + connectionTimeout);
        readTimeout = Integer.parseInt(helper.getValue(dataSourceObject, DataServer.READ_TIMEOUT, DEFAULT_READ_TIMEOUT));
        log.debug("LoytecXmlDlServer - Read Timeout: " + readTimeout);
        user = helper.getValue(dataSourceObject, USER, USER_DEFAULT);
        log.debug("LoytecXmlDlServer - User: " + user);
        password = helper.getValue(dataSourceObject, PASSWORD, DEFAULT_PASSWORD);
        log.debug("LoytecXmlDlServer - Password: " + printPW());
        ssl = Boolean.valueOf(helper.getValue(dataSourceObject, SSL, DEFAULT_SSL));
        log.debug("LoytecXmlDlServer - Ssl: " + ssl);
        logHandleBasePath = helper.getValue(dataSourceObject, LOGHANDLE_BASE_PATH, DEFAULT_LOGHANDLE_BASE_PATH);
//        System.out.println("???????????????  :"+ Helper.getValue(dataSourceObject,LOGHANDLE_BASE_PATH));
        log.debug("LoytecXmlDlServer - LogHandle Base Path: " + logHandleBasePath);
//        timezone = helper.getValue(dataSourceObject, DataServer.TIMEZONE, DEFAULT_TIMEZONE);

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


    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public Boolean getSsl() {
        return ssl;
    }

    @Override
    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    @Override
    public Integer getReadTimeout() {
        return readTimeout;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public List<LoytecXmlDlChannelDirectory> getChannelDirectories() {
        return channelDirectories;
    }

    @Override
    public String getLogHandleBasePath() {
        return logHandleBasePath;
    }

    @Override
    public DateTimeZone getTimezone() {
        return timezone;
    }

    private String printPW() {
        String str = null;
        for (int i = 0; i < password.length(); i++) {
            str = "*" + str;
        }
        return str;
    }

    public JEVisObject getObject() {
        return object;
    }
}
