/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.iso.add.Snippets;
import org.jevis.ws.sql.SQLDataSource;

import java.util.ArrayList;
import java.util.List;

import static org.jevis.iso.add.Snippets.getValueString;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class EnergySource {
    List<EnergyBill> EnergyBills;
    List<EnergyConsumption> energyconsumptions;
    private String Name;
    private long ID;
    private JsonObject object;
    private SQLDataSource ds;
    private Double CO2EmissionFactor;

    public EnergySource(SQLDataSource ds, JsonObject input) throws Exception {
        Name = "";
        ID = 0L;
        EnergyBills = new ArrayList<>();
        energyconsumptions = new ArrayList<>();
        CO2EmissionFactor = 0.0;
        this.ID = input.getId();
        this.Name = input.getName();

        this.object = input;
        this.ds = ds;

        List<JsonAttribute> listEnergySourceAttributes = ds.getAttributes(input.getId());

        for (JsonAttribute att : listEnergySourceAttributes) {
            String name = att.getType();

            final String attCO2EmissionFactor = "CO2 Emission Factor";
            switch (name) {
                case attCO2EmissionFactor:
                    if (getValueString(att, "") != "") {
                        this.setCO2EmissionFactor(Double.parseDouble(getValueString(att, "")));
                    } else this.setCO2EmissionFactor(0.0);
                    break;
                default:
                    break;
            }
        }

    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    @Override
    public String toString() {
        return "EnergySource{" + "Name=" + Name + ", ID=" + ID + ", EnergyBills=" + EnergyBills + ", EnergyConsumptions=" + energyconsumptions + '}';
    }

    public SQLDataSource getDs() {
        return ds;
    }

    public void setDs(SQLDataSource ds) {
        this.ds = ds;
    }

    public List<EnergyBill> getEnergyBills() throws Exception {
        EnergyBills.clear();

        for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getEnergyBills().getName(), false)) {
            Snippets.getParent(getDs(), obj);
            if (obj.getParent() == getObject().getId()) {
                EnergyBill eb = new EnergyBill(getDs(), obj);
                EnergyBills.add(eb);
            }
        }
        return EnergyBills;
    }

    public void setEnergyBills(List<EnergyBill> EnergyBills) {
        this.EnergyBills = EnergyBills;
    }

    public JsonObject getObject() {
        return object;
    }

    public void setObject(JsonObject object) {
        this.object = object;
    }

    public List<EnergyConsumption> getEnergyconsumptions() throws Exception {
        energyconsumptions.clear();
        for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getEnergyConsumption().getName(), false)) {
            Snippets.getParent(getDs(), obj);
            if (obj.getParent() == getObject().getId()) {
                EnergyConsumption ec = new EnergyConsumption(getDs(), obj);
                ec.setType(Name);
                energyconsumptions.add(ec);
            }
        }
        if (EnergyBills.isEmpty()) {
            getEnergyBills();
        }
        for (EnergyConsumption ec : energyconsumptions) {
            for (EnergyBill eb : EnergyBills) {
                if (eb.getYear() == ec.getYear()) {
                    ec.setCosts(eb.getcosts());
                    ec.setBillsID(eb.getID());
                    ec.setNameBills(eb.getName());
                }
            }
        }
        return energyconsumptions;
    }

    public void setEnergyconsumptions(List<EnergyConsumption> energyconsumptions) {
        this.energyconsumptions = energyconsumptions;
    }

    public List<Double> getListConsumptionForYear(long year) throws Exception {
        if (energyconsumptions.isEmpty()) {
            getEnergyconsumptions();
        }

        for (EnergyConsumption ec : energyconsumptions) {
            if (ec.getYear() == year) {
                return ec.getListConsumption();
            }
        }
        return null;
    }

    public EnergyConsumption getEnergyConsumption(long year) throws Exception {
        if (energyconsumptions.isEmpty()) {
            getEnergyconsumptions();
        }
        EnergyConsumption newEC = new EnergyConsumption();

        for (EnergyConsumption ec : energyconsumptions) {
            if (ec.getYear() == year) {
                newEC = ec;
                newEC.setCosts(getEnergyBill(year).getSum());
            }
        }
        return newEC;
    }

    public List<Double> getListBillsForYear(long year) throws Exception {
        if (EnergyBills.isEmpty()) {
            getEnergyBills();
        }

        for (EnergyBill eb : EnergyBills) {
            if (eb.getYear() == year) {
                return eb.getListBills();
            }
        }
        return null;
    }

    public EnergyBill getEnergyBill(long year) throws Exception {
        if (EnergyBills.isEmpty()) {
            getEnergyBills();
        }
        for (EnergyBill eb : EnergyBills) {
            if (eb.getYear() == year) {
                return eb;
            }
        }
        return null;
    }

    public Double getCO2EmissionFactor() {
        return CO2EmissionFactor;
    }

    public void setCO2EmissionFactor(Double CO2EmissionFactor) {
        this.CO2EmissionFactor = CO2EmissionFactor;
    }

}
