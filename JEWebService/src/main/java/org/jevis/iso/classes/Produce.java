/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.sql.SQLDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class Produce extends EvaluatedOutput {

    private static final Logger logger = LogManager.getLogger(Produce.class);

    private List<Double> listProduction;

    public Produce(SQLDataSource ds, JsonObject input) throws Exception {
        super(ds, input);

        listProduction = new ArrayList<>();
        listProduction = getList();
    }

    public List<Double> getListProduction() {
        return listProduction;
    }
}
