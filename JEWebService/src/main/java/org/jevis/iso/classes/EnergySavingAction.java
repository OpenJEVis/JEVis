/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.ws.sql.SQLDataSource;

import java.util.List;

import static org.jevis.iso.add.Snippets.getValueString;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class EnergySavingAction {
    private long ID;
    private String name;
    private double investmentcosts;
    private String measure;
    private String responsibleperson;
    private double paybacktime;
    private double savingspotentialcapital;
    private double savingspotentialco2;
    private double savingspotentialenergy;
    private SQLDataSource ds;

    public EnergySavingAction(SQLDataSource ds, JsonObject input) throws JEVisException {
        ID = 0L;
        name = "";
        this.ID = input.getId();
        this.name = input.getName();
        this.ds = ds;
        investmentcosts = 0.0;
        measure = "";
        responsibleperson = "";
        paybacktime = 0.0;
        savingspotentialcapital = 0.0;
        savingspotentialco2 = 0.0;
        savingspotentialenergy = 0.0;

        List<JsonAttribute> listEnergySavingActionAttributes = ds.getAttributes(input.getId());
        for (JsonAttribute att : listEnergySavingActionAttributes) {
            String name = att.getType();

            final String attSavingsPotentialEnergy = "Savings Potential Energy";
            final String attSavingsPotentialCO2 = "Savings Potential CO2";
            final String attSavingsPotentialCapital = "Savings Potential Capital";
            final String attResponsiblePerson = "Responsible Person";
            final String attPaybackTime = "Payback Time";
            final String attMeasure = "Measure";
            final String attInvestmentCosts = "Investment Costs";
            switch (name) {
                case attInvestmentCosts:
                    if (getValueString(att, "") != "") {
                        this.setinvestmentcosts(Double.parseDouble(getValueString(att, "")));
                    } else this.setinvestmentcosts(0.0);
                    break;
                case attMeasure:
                    this.setmeasure(getValueString(att, ""));
                    break;
                case attPaybackTime:
                    if (getValueString(att, "") != "") {
                        this.setpaybacktime(Double.parseDouble(getValueString(att, "")));
                    } else this.setpaybacktime(0.0);
                    break;
                case attResponsiblePerson:
                    this.setresponsibleperson(getValueString(att, ""));
                    break;
                case attSavingsPotentialCO2:
                    if (getValueString(att, "") != "") {
                        this.setsavingspotentialco2(Double.parseDouble(getValueString(att, "")));
                    } else this.setsavingspotentialco2(0.0);
                    break;
                case attSavingsPotentialCapital:
                    if (getValueString(att, "") != "") {
                        this.setsavingspotentialcapital(Double.parseDouble(getValueString(att, "")));
                    } else this.setsavingspotentialcapital(0.0);
                    break;
                case attSavingsPotentialEnergy:
                    if (getValueString(att, "") != "") {
                        this.setsavingspotentialenergy(Double.parseDouble(getValueString(att, "")));
                    } else this.setsavingspotentialenergy(0.0);
                    break;
                default:
                    break;
            }
        }
    }

    public SQLDataSource getDs() {
        return ds;
    }

    public void setDs(SQLDataSource ds) {
        this.ds = ds;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public double getinvestmentcosts() {
        return investmentcosts;
    }

    public void setinvestmentcosts(double investmentcosts) {
        this.investmentcosts = investmentcosts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getmeasure() {
        return measure;
    }

    public void setmeasure(String measure) {
        this.measure = measure;
    }

    public double getpaybacktime() {
        return paybacktime;
    }

    public void setpaybacktime(double paybacktime) {
        this.paybacktime = paybacktime;
    }

    public double getsavingspotentialcapital() {
        return savingspotentialcapital;
    }

    public void setsavingspotentialcapital(double savingspotentialcapital) {
        this.savingspotentialcapital = savingspotentialcapital;
    }

    public double getsavingspotentialco2() {
        return savingspotentialco2;
    }

    public void setsavingspotentialco2(double savingspotentialco2) {
        this.savingspotentialco2 = savingspotentialco2;
    }

    public double getsavingspotentialenergy() {
        return savingspotentialenergy;
    }

    public void setsavingspotentialenergy(double savingspotentialenergy) {
        this.savingspotentialenergy = savingspotentialenergy;
    }

    public String getresponsibleperson() {
        return responsibleperson;
    }

    public void setresponsibleperson(String responsibleperson) {
        this.responsibleperson = responsibleperson;
    }

    @Override
    public String toString() {
        return "EnergySavingAction{" + "ID=" + ID + ", investmentcosts=" + investmentcosts + ", measure=" + measure + ", responsibleperson=" + responsibleperson + ", paybacktime=" + paybacktime + ", savingspotentialcapital=" + savingspotentialcapital + ", savingspotentialco2=" + savingspotentialco2 + ", savingspotentialenergy=" + savingspotentialenergy + '}';
    }
}
