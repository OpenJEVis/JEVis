/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.commons.ws.json.JsonObject;
import org.jevis.ws.sql.SQLDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class EnergyConsumption extends EvaluatedOutput {

    private List<Double> listConsumption;
    private long billsID;
    private String nameBills;
    private double shareoftotalconsumption;
    private double costs;
    private double costrelated;
    private double co2emissions;
    private double co2shareoftotal;

    public EnergyConsumption(SQLDataSource ds, JsonObject input) throws Exception {
        super(ds, input);
        billsID = 0L;
        nameBills = "";
        shareoftotalconsumption = 0.0;
        costs = 0.0;
        costrelated = 0.0;
        co2emissions = 0.0;
        co2shareoftotal = 0.0;
        listConsumption = new ArrayList<>();
        this.listConsumption = getList();

    }

    public EnergyConsumption() {
        listConsumption = new ArrayList<>();
        billsID = 0L;
        nameBills = "";
        shareoftotalconsumption = 0.0;
        costs = 0.0;
        costrelated = 0.0;
        co2emissions = 0.0;
        co2shareoftotal = 0.0;
    }

    public List<Double> getListConsumption() {
        return listConsumption;
    }

    public void setListConsumption(List<Double> listConsumption) {
        this.listConsumption = listConsumption;
    }

    public long getBillsID() {
        return billsID;
    }

    public void setBillsID(long billsID) {
        this.billsID = billsID;
    }

    @Override
    public String toString() {
        return "EnergyConsumption{" + "ID=" + ID + ", EnergySupplier=" + EnergySupplier + ", Year=" + year + ", January=" + January + ", February=" + February + ", March=" + March + ", April=" + April + ", May=" + May + ", June=" + June + ", July=" + July + ", August=" + August + ", September=" + September + ", October=" + October + ", November=" + November + ", December=" + December + '}';
    }

    public String getNameBills() {
        return nameBills;
    }

    public void setNameBills(String nameBills) {
        this.nameBills = nameBills;
    }

    public double getShareoftotalconsumption() {
        return shareoftotalconsumption;
    }

    public void setShareoftotalconsumption(double shareoftotalconsumption) {
        this.shareoftotalconsumption = shareoftotalconsumption;
    }

    public double getCosts() {
        return costs;
    }

    public void setCosts(double costs) {
        this.costs = costs;
    }

    public double getCostrelated() {
        return costrelated;
    }

    public void setCostrelated(double costrelated) {
        this.costrelated = costrelated;
    }

    public double getCo2emissions() {
        return co2emissions;
    }

    public void setCo2emissions(double co2emissions) {
        this.co2emissions = co2emissions;
    }

    public double getCo2shareoftotal() {
        return co2shareoftotal;
    }

    public void setCo2shareoftotal(double co2shareoftotal) {
        this.co2shareoftotal = co2shareoftotal;
    }

}
