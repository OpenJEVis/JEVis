package org.jevis.loytecxmldl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.joda.time.DateTime;

public class OPCUAStatus {
    private final static Logger log = LogManager.getLogger(OPCUAStatus.class.getName());
    static final int SUCCESS = 1; // The Opc Node was successfully written with the new value
    static final int OPC_SERVER_NOT_REACHABLE = 2; // could not connect to OPC-UA Server
    static final int NOT_WRITTEN = 3; // Error occurred during write action
    static final String STATUS_LOG = "Status Log";
    private final int status;

    public OPCUAStatus(int status) {
        this.status = status;
    }

    public void writeStatus(JEVisObject jeVisObject, DateTime dateTime) {
        try {
            jeVisObject.getAttribute(STATUS_LOG).buildSample(dateTime, status).commit();
        } catch (JEVisException e) {
            log.error(e);
        }
    }
}
