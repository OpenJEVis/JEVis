package org.jevis.jeopc;

import org.jevis.api.JEVisObject;
import org.jevis.commons.driver.DataSource;

import java.io.InputStream;
import java.util.List;

public class OPCUADataSource implements DataSource {

    @Override
    public void run() {

    }

    @Override
    public void initialize(JEVisObject dataSourceJEVis) {

    }

    @Override
    public List<InputStream> sendSampleRequest(JEVisObject channel) throws Exception {
        return null;
    }

    @Override
    public void parse(List<InputStream> input) {

    }

    @Override
    public void importResult() {

    }
}
