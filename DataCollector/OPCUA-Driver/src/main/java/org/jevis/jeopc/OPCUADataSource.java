package org.jevis.jeopc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.jevis.commons.driver.DataSource;

import java.io.InputStream;
import java.util.List;

public class OPCUADataSource implements DataSource {

    private final String version = "1.1 2020-11-30";
    private final static Logger log = LogManager.getLogger(OPCUADataSource.class.getName());

    private JEVisObject dataSourceObj = null;
    private OPCUAServer opcuaServer = null;

    @Override
    public void run() {
        logger.error("run:");

        opcuaServer.run();
        logger.error("done");

    }

    @Override
    public void initialize(JEVisObject dataSourceJEVis) {
        logger.error("initialize: Driver version {} for object: {}", version, dataSourceJEVis);

        dataSourceObj = dataSourceJEVis;
        opcuaServer = new OPCUAServer(dataSourceJEVis);


        logger.error("Done init");
    }

    @Override
    public List<InputStream> sendSampleRequest(JEVisObject channel) throws Exception {
        System.out.println("sendSampleRequest: " + channel);

        return null;
    }

    @Override
    public void parse(List<InputStream> input) {
        logger.error("parse: {}", input);
    }

    @Override
    public void importResult() {
        logger.error("importResult");
    }
}
