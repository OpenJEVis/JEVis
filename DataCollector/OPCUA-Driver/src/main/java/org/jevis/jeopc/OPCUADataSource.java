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
        log.debug("run:");

        opcuaServer.run();
        log.debug("done");

    }

    @Override
    public void initialize(JEVisObject dataSourceJEVis) {
        log.debug("initialize: Driver version {} for object: {}", version, dataSourceJEVis);

        dataSourceObj = dataSourceJEVis;
        opcuaServer = new OPCUAServer(dataSourceJEVis);


        log.debug("Done init");
    }

    @Override
    public List<InputStream> sendSampleRequest(JEVisObject channel) throws Exception {
        log.debug("sendSampleRequest: " + channel);

        return null;
    }

    @Override
    public void parse(List<InputStream> input) {
        log.debug("parse: {}", input);
    }

    @Override
    public void importResult() {
        log.debug("importResult");
    }
}
