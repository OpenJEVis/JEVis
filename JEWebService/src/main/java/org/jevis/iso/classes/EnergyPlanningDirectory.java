/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.jevis.iso.add.Snippets;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class EnergyPlanningDirectory {

    List<EnergySource> EnergySources;
    String EnergySourcesDirName;
    List<EnergyFlowChart> EnergyFlowCharts;
    String EnergyFlowChartDirName;
    List<Produce> Production;
    String ProductionDirName;
    EquipmentRegisterDirectory equipmentregister;
    String EquipmentRegisterDirName;
    private SQLDataSource ds;
    private long ID;
    private JsonObject object;
    private String name;
    private long EnergySourcesDirID;
    private long EnergyFlowChartDirID;
    private long ProductionDirID;
    private long EquipmentRegisterDirID;

    private double totalCosts;
    private double totalConsumption;

    public EnergyPlanningDirectory(SQLDataSource ds, JsonObject input) {

        ID = 0L;
        this.ID = input.getId();
        name = "";
        this.name = input.getName();

        this.object = input;
        this.ds = ds;

        EnergySources = new ArrayList<>();
        EnergySourcesDirName = "";
        EnergyFlowCharts = new ArrayList<>();
        EnergyFlowChartDirName = "";
        Production = new ArrayList<>();
        ProductionDirName = "";
        equipmentregister = new EquipmentRegisterDirectory();
        EquipmentRegisterDirName = "";
        EnergySourcesDirID = 0L;
        EnergyFlowChartDirID = 0L;
        ProductionDirID = 0L;
        EquipmentRegisterDirID = 0L;
        totalCosts = 0.0;
        totalConsumption = 0.0;
    }

    EnergyPlanningDirectory() {

        EnergySources = new ArrayList<>();
        EnergySourcesDirName = "";
        EnergyFlowCharts = new ArrayList<>();
        EnergyFlowChartDirName = "";
        Production = new ArrayList<>();
        ProductionDirName = "";
        equipmentregister = new EquipmentRegisterDirectory();
        EquipmentRegisterDirName = "";
        ID = 0L;
        name = "";
        EnergySourcesDirID = 0L;
        EnergyFlowChartDirID = 0L;
        ProductionDirID = 0L;
        EquipmentRegisterDirID = 0L;
        totalCosts = 0.0;
        totalConsumption = 0.0;
    }

    public EnergySource getEnergySource(String sourceName) throws Exception {
        if (EnergySources.isEmpty()) {
            EnergySources = getEnergySources();
        }
        for (EnergySource es : getEnergySources()) {
            if (sourceName.equals(es.getName())) {
                return es;
            }
        }
        return null;
    }

    public List<String> getEnergySourcesNames() throws Exception {
        if (EnergySources.isEmpty()) {
            EnergySources = getEnergySources();
        }
        List<String> output = new ArrayList<>();
        for (EnergySource es : getEnergySources()) {
            output.add(es.getName());
        }
        return output;
    }

    public List<Produce> getProduction(long year) throws Exception {
        if (Production.isEmpty()) {
            Production = getProduction();
        }
        List<Produce> output = new ArrayList<>();
        for (Produce p : getProduction()) {
            if (year == p.getYear()) {
                output.add(p);
            }
        }
        return output;
    }

    public List<EnergyConsumption> getEnergyConsumption(long year) throws Exception {
        if (EnergySources.isEmpty()) {
            EnergySources = getEnergySources();
        }
        List<EnergyConsumption> listEC = new ArrayList<>();
        for (EnergySource es : EnergySources) {
            for (EnergyConsumption ec : es.getEnergyconsumptions()) {
                if (ec.getYear() == year) {
                    listEC.add(ec);
                }
            }
        }
        return listEC;
    }

    public List<Long> getListYearsEnergyFlowCharts() throws Exception {
        List<Long> output = new ArrayList<>();
        for (EnergyFlowChart efc : getEnergyFlowCharts()) {
            if (!output.contains(efc.getYear())) {
                output.add(efc.getYear());
            }
        }
        return output;
    }

    public List<Long> getListYears() throws Exception {
        List<Long> output = new ArrayList<>();
        if (EnergySources.isEmpty()) {
            EnergySources = getEnergySources();
        }
        for (EnergySource es : getEnergySources()) {
            for (EnergyConsumption ec : es.getEnergyconsumptions()) {
                if (!output.contains(ec.getYear())) {
                    output.add(ec.getYear());
                }
            }
            for (EnergyBill eb : es.getEnergyBills()) {
                if (!output.contains(eb.getYear())) {
                    output.add(eb.getYear());
                }
            }
        }
        for (Produce p : getProduction()) {
            if (!output.contains(p.getYear())) {
                output.add(p.getYear());
            }
        }
        return output;
    }

    public long getId() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public JsonObject getObject() {
        return object;
    }

    public void setObject(JsonObject object) {
        this.object = object;
    }

    public SQLDataSource getDs() {
        return ds;
    }

    public void setDs(SQLDataSource ds) {
        this.ds = ds;
    }

    public List<EnergySource> getEnergySources() throws Exception {
        if (getObject() != null) {
            EnergySources.clear();
            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getEnergySourcesDir().getName(), false)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject m : getDs().getObjects(ISO50001.getJc().getEnergySource().getName(), false)) {
                        Snippets.getParent(getDs(), m);
                        if (m.getParent() == obj.getId()) {
                            EnergySource es = new EnergySource(getDs(), m);
                            EnergySources.add(es);
                        }
                    }
                }
            }
        }
        return EnergySources;
    }

    public void setEnergySources(List<EnergySource> EnergySources) {
        this.EnergySources = EnergySources;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getEnergySourcesDirID() throws JEVisException {
        EnergySourcesDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getEnergySourcesDir().getName(), getObject());
        return EnergySourcesDirID;
    }

    public void setEnergySourcesDirID(long EnergySourcesDirID) {
        this.EnergySourcesDirID = EnergySourcesDirID;
    }

    public double getTotalCosts() {
        return totalCosts;
    }

    public void setTotalCosts(double totalCosts) {
        this.totalCosts = totalCosts;
    }

    public double getTotalConsumption() {
        return totalConsumption;
    }

    public void setTotalConsumption(double totalConsumption) {
        this.totalConsumption = totalConsumption;
    }

    public List<EnergyFlowChart> getEnergyFlowCharts() throws Exception {
        if (getObject() != null) {
            EnergyFlowCharts.clear();
            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getEnergyFlowChartDir().getName(), false)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject m : getDs().getObjects(ISO50001.getJc().getEnergyFlowChart().getName(), false)) {
                        Snippets.getParent(getDs(), m);
                        if (m.getParent() == obj.getId()) {
                            EnergyFlowChart efc = new EnergyFlowChart(getDs(), m);
                            EnergyFlowCharts.add(efc);
                        }
                    }
                }
            }
        }
        return EnergyFlowCharts;
    }

    public void setEnergyFlowCharts(List<EnergyFlowChart> EnergyFlowCharts) {
        this.EnergyFlowCharts = EnergyFlowCharts;
    }

    public String getEnergySourcesDirName() throws JEVisException {
        EnergySourcesDirName = Snippets.getChildName(getDs(), ISO50001.getJc().getEnergySourcesDir().getName(), getObject());
        return EnergySourcesDirName;
    }

    public void setEnergySourcesDirName(String EnergySourcesDirName) {
        this.EnergySourcesDirName = EnergySourcesDirName;
    }

    public String getEnergyFlowChartDirName() throws JEVisException {
        EnergyFlowChartDirName = Snippets.getChildName(getDs(), ISO50001.getJc().getEnergyFlowChartDir().getName(), getObject());
        return EnergyFlowChartDirName;
    }

    public void setEnergyFlowChartDirName(String EnergyFlowChartDirName) {
        this.EnergyFlowChartDirName = EnergyFlowChartDirName;
    }

    public String getProductionDirName() throws JEVisException {
        ProductionDirName = Snippets.getChildName(getDs(), ISO50001.getJc().getPerformanceDir().getName(), getObject());
        return ProductionDirName;
    }

    public void setProductionDirName(String ProductionDirName) {
        this.ProductionDirName = ProductionDirName;
    }

    public EquipmentRegisterDirectory getEquipmentregister() throws Exception {
        if (getObject() != null) {
            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getEquipmentRegisterDir().getName(), false)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    equipmentregister = new EquipmentRegisterDirectory(getDs(), obj);
                }
            }
        }
        return equipmentregister;
    }

    public void setEquipmentregister(EquipmentRegisterDirectory equipmentregister) {
        this.equipmentregister = equipmentregister;
    }

    public String getEquipmentRegisterDirName() throws JEVisException {
        EquipmentRegisterDirName = Snippets.getChildName(getDs(), ISO50001.getJc().getEquipmentRegisterDir().getName(), getObject());
        return EquipmentRegisterDirName;
    }

    public void setEquipmentRegisterDirName(String EquipmentRegisterDirName) {
        this.EquipmentRegisterDirName = EquipmentRegisterDirName;
    }

    public List<Produce> getProduction() throws Exception {
        if (getObject() != null) {
            Production.clear();
            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getPerformanceDir().getName(), false)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject m : getDs().getObjects(ISO50001.getJc().getEvaluatedOutput().getName(), false)) {
                        Snippets.getParent(getDs(), m);
                        if (m.getParent() == obj.getId()) {
                            Produce p = new Produce(getDs(), m);
                            Production.add(p);
                        }
                    }
                }
            }
        }
        return Production;
    }

    public void setProduction(List<Produce> Production) {
        this.Production = Production;
    }

    public long getEnergyFlowChartDirID() throws JEVisException {
        EnergyFlowChartDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getEnergyFlowChartDir().getName(), getObject());
        return EnergyFlowChartDirID;
    }

    public void setEnergyFlowChartDirID(long EnergyFlowChartDirID) {
        this.EnergyFlowChartDirID = EnergyFlowChartDirID;
    }

    public long getProductionDirID() throws JEVisException {
        ProductionDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getPerformanceDir().getName(), getObject());
        return ProductionDirID;
    }

    public void setProductionDirID(long ProductionDirID) {
        this.ProductionDirID = ProductionDirID;
    }

    public long getEquipmentRegisterDirID() throws JEVisException {
        EquipmentRegisterDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getEquipmentRegisterDir().getName(), getObject());
        return EquipmentRegisterDirID;
    }

    public void setEquipmentRegisterDirID(long EquipmentRegisterDirID) {
        this.EquipmentRegisterDirID = EquipmentRegisterDirID;
    }

}
