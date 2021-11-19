package org.jevis.opcuawrite;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.joda.time.DateTime;

public class OpcUAStatus {
    static final int SUCCESS = 1;
    static final int OPC_SERVER_NOT_REACHABLE = 2;
    static final int NOT_WRITTEN = 3;
    static final String STATUS_LOG = "Status Log";
    private final int status;

    public OpcUAStatus(int status) {
        this.status = status;
    }

    public void writeStatus(JEVisObject jeVisObject, DateTime dateTime) {
        try {
            jeVisObject.getAttribute(STATUS_LOG).buildSample(dateTime, status).commit();
        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }
}
