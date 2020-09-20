/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.sql.SQLDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class EnergyBill extends EvaluatedOutput {

    public List<Double> listBills;
    private double costs;

    public EnergyBill(SQLDataSource ds, JsonObject input) throws Exception {
        super(ds, input);
        costs = 0.0;
        listBills = new ArrayList<>();
        this.listBills = getList();
    }

    public EnergyBill() {
        costs = 0.0;
        listBills = new ArrayList<>();
    }

    public List<Double> getListBills() {
        return listBills;
    }

    public void setListBills(List<Double> listBills) {
        this.listBills = listBills;
    }

    public double getcosts() {
        return costs;
    }

    public void setcosts(double costs) {
        this.costs = costs;
    }
}
