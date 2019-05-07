package org.jevis.jestatus;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;

public class ServiceStati {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ServiceStati.class);

    public ServiceStati(JEVisDataSource ds) {
        try {
            JEVisClass dataCollectorClass = ds.getJEVisClass("JEDataCollector");
            JEVisClass dataprocessorClass = ds.getJEVisClass("JEDataProcessor");
            JEVisClass calcClass = ds.getJEVisClass("JECalc");
            JEVisClass reportClass = ds.getJEVisClass("JEReport");
        } catch (JEVisException e) {
            logger.error("Could not get Service Classes.", e);
        }
    }

    public String getStati() {
        return null;
    }
}
