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
import java.util.Objects;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class EquipmentRegisterDirectory {
    private Long AirConditioningDirID;
    private String AirConditioningDirName;
    private List<EquipmentAirConditioning> listAirConditioning;
    private Long CompressorDirID;
    private String CompressorDirName;
    private List<EquipmentCompressor> listCompressor;
    private Long CoolerDirID;
    private String CoolerDirName;
    private List<EquipmentCooler> listCooler;
    private Long HeaterDirID;
    private String HeaterDirName;
    private List<EquipmentHeater> listHeater;
    private Long LightingDirID;
    private String LightingDirName;
    private List<EquipmentLighting> listLighting;
    private Long OfficeDirID;
    private String OfficeDirName;
    private List<EquipmentOffice> listOffice;
    private Long PantryDirID;
    private String PantryDirName;
    private List<EquipmentPantry> listPantry;
    private Long ProductionDirID;
    private String ProductionDirName;
    private List<EquipmentProduction> listProduction;
    private Long VentilationDirID;
    private String VentilationDirName;
    private List<EquipmentVentilation> listVentilation;

    private long ID;
    private String name;
    private JsonObject object;
    private SQLDataSource ds;

    public EquipmentRegisterDirectory() {
        AirConditioningDirID = 0L;
        AirConditioningDirName = "";
        listAirConditioning = new ArrayList<>();
        CompressorDirID = 0L;
        CompressorDirName = "";
        listCompressor = new ArrayList<>();
        CoolerDirID = 0L;
        CoolerDirName = "";
        listCooler = new ArrayList<>();
        HeaterDirID = 0L;
        HeaterDirName = "";
        listHeater = new ArrayList<>();
        LightingDirID = 0L;
        LightingDirName = "";
        listLighting = new ArrayList<>();
        OfficeDirID = 0L;
        OfficeDirName = "";
        listOffice = new ArrayList<>();
        PantryDirID = 0L;
        PantryDirName = "";
        listPantry = new ArrayList<>();
        ProductionDirID = 0L;
        ProductionDirName = "";
        listProduction = new ArrayList<>();
        VentilationDirID = 0L;
        VentilationDirName = "";
        listVentilation = new ArrayList<>();
        ID = 0L;
        name = "";
    }

    public EquipmentRegisterDirectory(SQLDataSource ds, JsonObject input) {
        ID = 0L;
        name = "";

        this.ID = input.getId();
        this.name = input.getName();

        this.object = input;
        this.ds = ds;

        AirConditioningDirID = 0L;
        AirConditioningDirName = "";
        listAirConditioning = new ArrayList<>();
        CompressorDirID = 0L;
        CompressorDirName = "";
        listCompressor = new ArrayList<>();
        CoolerDirID = 0L;
        CoolerDirName = "";
        listCooler = new ArrayList<>();
        HeaterDirID = 0L;
        HeaterDirName = "";
        listHeater = new ArrayList<>();
        LightingDirID = 0L;
        LightingDirName = "";
        listLighting = new ArrayList<>();
        OfficeDirID = 0L;
        OfficeDirName = "";
        listOffice = new ArrayList<>();
        PantryDirID = 0L;
        PantryDirName = "";
        listPantry = new ArrayList<>();
        ProductionDirID = 0L;
        ProductionDirName = "";
        listProduction = new ArrayList<>();
        VentilationDirID = 0L;
        VentilationDirName = "";
        listVentilation = new ArrayList<>();
    }

    public SQLDataSource getDs() {
        return ds;
    }

    public void setDs(SQLDataSource ds) {
        this.ds = ds;
    }

    public List<EquipmentAirConditioning> getListAirConditioning() throws Exception {
        if (getObject() != null) {
            listAirConditioning.clear();
            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getAirConditionEquipmentDir().getName(), false)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject m : getDs().getObjects(ISO50001.getJc().getAirConditioning().getName(), false)) {
                        if (Objects.nonNull(m)) {
                            Snippets.getParent(getDs(), m);
                            if (m.getParent() == obj.getId()) {
                                EquipmentAirConditioning eac = new EquipmentAirConditioning(getDs(), m);
                                listAirConditioning.add(eac);

                            }
                        }
                    }
                }
            }
        }
        return listAirConditioning;
    }

    public void setListAirConditioning(List<EquipmentAirConditioning> listAirConditioning) {
        this.listAirConditioning = listAirConditioning;
    }

    public List<EquipmentCompressor> getListCompressor() throws Exception {
        if (getObject() != null) {
            listCompressor.clear();
            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getCompressorEquipmentDir().getName(), false)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject m : getDs().getObjects(ISO50001.getJc().getCompressor().getName(), false)) {
                        if (Objects.nonNull(m)) {
                            Snippets.getParent(getDs(), m);
                            if (m.getParent() == obj.getId()) {
                                EquipmentCompressor ec = new EquipmentCompressor(getDs(), m);
                                listCompressor.add(ec);
                            }

                        }
                    }
                }
            }
        }
        return listCompressor;
    }

    public void setListCompressor(List<EquipmentCompressor> listCompressor) {
        this.listCompressor = listCompressor;
    }

    public List<EquipmentCooler> getListCooler() throws Exception {
        if (getObject() != null) {
            listCooler.clear();
            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getCoolingEquipmentDir().getName(), false)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject m : getDs().getObjects(ISO50001.getJc().getCooler().getName(), false)) {
                        if (Objects.nonNull(m)) {
                            Snippets.getParent(getDs(), m);
                            if (m.getParent() == obj.getId()) {
                                EquipmentCooler ec = new EquipmentCooler(getDs(), m);
                                listCooler.add(ec);
                            }

                        }
                    }
                }
            }
        }
        return listCooler;
    }

    public void setListCooler(List<EquipmentCooler> listCooler) {
        this.listCooler = listCooler;
    }

    public List<EquipmentHeater> getListHeater() throws Exception {
        if (getObject() != null) {
            listHeater.clear();
            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getHeatingEquipmentDir().getName(), false)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject m : getDs().getObjects(ISO50001.getJc().getHeater().getName(), false)) {
                        if (Objects.nonNull(m)) {
                            Snippets.getParent(getDs(), m);
                            if (m.getParent() == obj.getId()) {
                                EquipmentHeater eh = new EquipmentHeater(getDs(), m);
                                listHeater.add(eh);
                            }
                        }
                    }
                }
            }
        }
        return listHeater;
    }

    public void setListHeater(List<EquipmentHeater> listHeater) {
        this.listHeater = listHeater;
    }

    public List<EquipmentLighting> getListLighting() throws Exception {
        if (getObject() != null) {
            listLighting.clear();
            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getLightingEquipmentDir().getName(), false)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject m : getDs().getObjects(ISO50001.getJc().getLighting().getName(), false)) {
                        if (Objects.nonNull(m)) {
                            Snippets.getParent(getDs(), m);
                            if (m.getParent() == obj.getId()) {
                                EquipmentLighting el = new EquipmentLighting(getDs(), m);
                                listLighting.add(el);
                            }
                        }

                    }
                }
            }
        }
        return listLighting;
    }

    public void setListLighting(List<EquipmentLighting> listLighting) {
        this.listLighting = listLighting;
    }

    public List<EquipmentOffice> getListOffice() throws Exception {
        if (getObject() != null) {
            listOffice.clear();
            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getOfficeEquipmentDir().getName(), false)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject m : getDs().getObjects(ISO50001.getJc().getOfficeEquipment().getName(), false)) {
                        if (Objects.nonNull(m)) {
                            Snippets.getParent(getDs(), m);
                            if (m.getParent() == obj.getId()) {
                                EquipmentOffice eo = new EquipmentOffice(getDs(), m);
                                listOffice.add(eo);
                            }

                        }
                    }
                }
            }
        }
        return listOffice;
    }

    public void setListOffice(List<EquipmentOffice> listOffice) {
        this.listOffice = listOffice;
    }

    public List<EquipmentPantry> getListPantry() throws Exception {
        if (getObject() != null) {
            listPantry.clear();
            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getPantryEquipmentDir().getName(), false)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject m : getDs().getObjects(ISO50001.getJc().getPantryEquipment().getName(), false)) {
                        if (Objects.nonNull(m)) {
                            Snippets.getParent(getDs(), m);
                            if (m.getParent() == obj.getId()) {
                                EquipmentPantry ep = new EquipmentPantry(getDs(), m);
                                listPantry.add(ep);
                            }

                        }
                    }
                }
            }
        }
        return listPantry;
    }

    public void setListPantry(List<EquipmentPantry> listPantry) {
        this.listPantry = listPantry;
    }

    public List<EquipmentProduction> getListProduction() throws Exception {
        if (getObject() != null) {
            listProduction.clear();
            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getProductionEquipmentDir().getName(), false)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject m : getDs().getObjects(ISO50001.getJc().getProductionEquipment().getName(), false)) {
                        if (Objects.nonNull(m)) {
                            Snippets.getParent(getDs(), m);
                            if (m.getParent() == obj.getId()) {
                                EquipmentProduction ep = new EquipmentProduction(getDs(), m);
                                listProduction.add(ep);
                            }

                        }
                    }
                }
            }
        }
        return listProduction;
    }

    public void setListProduction(List<EquipmentProduction> listProduction) {
        this.listProduction = listProduction;
    }

    public List<EquipmentVentilation> getListVentilation() throws Exception {
        if (getObject() != null) {
            listVentilation.clear();
            for (JsonObject obj : getDs().getObjects(ISO50001.getJc().getVentilationEquipmentDir().getName(), false)) {
                Snippets.getParent(getDs(), obj);
                if (obj.getParent() == getObject().getId()) {
                    for (JsonObject m : getDs().getObjects(ISO50001.getJc().getVentilation().getName(), false)) {
                        if (Objects.nonNull(m)) {
                            Snippets.getParent(getDs(), m);
                            if (m.getParent() == obj.getId()) {
                                EquipmentVentilation ev = new EquipmentVentilation(getDs(), m);
                                listVentilation.add(ev);
                            }
                        }

                    }
                }
            }
        }
        return listVentilation;
    }

    public void setListVentilation(List<EquipmentVentilation> listVentilation) {
        this.listVentilation = listVentilation;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAirConditioningDirName() throws JEVisException {
        AirConditioningDirName = Snippets.getChildName(getDs(), ISO50001.getJc().getAirConditionEquipmentDir().getName(), getObject());
        return AirConditioningDirName;
    }

    public void setAirConditioningDirName(String AirConditioningDirName) {
        this.AirConditioningDirName = AirConditioningDirName;
    }

    public String getCompressorDirName() throws JEVisException {
        CompressorDirName = Snippets.getChildName(getDs(), ISO50001.getJc().getCompressorEquipmentDir().getName(), getObject());
        return CompressorDirName;
    }

    public void setCompressorDirName(String CompressorDirName) {
        this.CompressorDirName = CompressorDirName;
    }

    public String getCoolerDirName() throws JEVisException {
        CoolerDirName = Snippets.getChildName(getDs(), ISO50001.getJc().getCoolingEquipmentDir().getName(), getObject());
        return CoolerDirName;
    }

    public void setCoolerDirName(String CoolerDirName) {
        this.CoolerDirName = CoolerDirName;
    }

    public String getHeaterDirName() throws JEVisException {
        HeaterDirName = Snippets.getChildName(getDs(), ISO50001.getJc().getHeatingEquipmentDir().getName(), getObject());
        return HeaterDirName;
    }

    public void setHeaterDirName(String HeaterDirName) {
        this.HeaterDirName = HeaterDirName;
    }

    public String getLightingDirName() throws JEVisException {
        LightingDirName = Snippets.getChildName(getDs(), ISO50001.getJc().getLightingEquipmentDir().getName(), getObject());
        return LightingDirName;
    }

    public void setLightingDirName(String LightingDirName) {
        this.LightingDirName = LightingDirName;
    }

    public String getOfficeDirName() throws JEVisException {
        OfficeDirName = Snippets.getChildName(getDs(), ISO50001.getJc().getOfficeEquipmentDir().getName(), getObject());
        return OfficeDirName;
    }

    public void setOfficeDirName(String OfficeDirName) {
        this.OfficeDirName = OfficeDirName;
    }

    public String getPantryDirName() throws JEVisException {
        PantryDirName = Snippets.getChildName(getDs(), ISO50001.getJc().getPantryEquipmentDir().getName(), getObject());
        return PantryDirName;
    }

    public void setPantryDirName(String PantryDirName) {
        this.PantryDirName = PantryDirName;
    }

    public String getProductionDirName() throws JEVisException {
        ProductionDirName = Snippets.getChildName(getDs(), ISO50001.getJc().getProductionEquipmentDir().getName(), getObject());
        return ProductionDirName;
    }

    public void setProductionDirName(String ProductionDirName) {
        this.ProductionDirName = ProductionDirName;
    }

    public String getVentilationDirName() throws JEVisException {

        VentilationDirName = Snippets.getChildName(getDs(), ISO50001.getJc().getVentilationEquipmentDir().getName(), getObject());
        return VentilationDirName;
    }

    public void setVentilationDirName(String VentilationDirName) {
        this.VentilationDirName = VentilationDirName;
    }

    public Long getAirConditioningDirID() throws JEVisException {
        AirConditioningDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getAirConditionEquipmentDir().getName(), getObject());
        return AirConditioningDirID;
    }

    public void setAirConditioningDirID(Long AirConditioningDirID) {
        this.AirConditioningDirID = AirConditioningDirID;
    }

    public Long getCompressorDirID() throws JEVisException {
        CompressorDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getCompressorEquipmentDir().getName(), getObject());
        return CompressorDirID;
    }

    public void setCompressorDirID(Long CompressorDirID) {
        this.CompressorDirID = CompressorDirID;
    }

    public Long getCoolerDirID() throws JEVisException {
        CoolerDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getCoolingEquipmentDir().getName(), getObject());
        return CoolerDirID;
    }

    public void setCoolerDirID(Long CoolerDirID) {
        this.CoolerDirID = CoolerDirID;
    }

    public Long getHeaterDirID() throws JEVisException {
        HeaterDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getHeatingEquipmentDir().getName(), getObject());
        return HeaterDirID;
    }

    public void setHeaterDirID(Long HeaterDirID) {
        this.HeaterDirID = HeaterDirID;
    }

    public Long getLightingDirID() throws JEVisException {
        LightingDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getLightingEquipmentDir().getName(), getObject());
        return LightingDirID;
    }

    public void setLightingDirID(Long LightingDirID) {
        this.LightingDirID = LightingDirID;
    }

    public Long getOfficeDirID() throws JEVisException {
        OfficeDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getOfficeEquipmentDir().getName(), getObject());
        return OfficeDirID;
    }

    public void setOfficeDirID(Long OfficeDirID) {
        this.OfficeDirID = OfficeDirID;
    }

    public Long getPantryDirID() throws JEVisException {
        PantryDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getPantryEquipmentDir().getName(), getObject());
        return PantryDirID;
    }

    public void setPantryDirID(Long PantryDirID) {
        this.PantryDirID = PantryDirID;
    }

    public Long getProductionDirID() throws JEVisException {
        ProductionDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getProductionEquipmentDir().getName(), getObject());
        return ProductionDirID;
    }

    public void setProductionDirID(Long ProductionDirID) {
        this.ProductionDirID = ProductionDirID;
    }

    public Long getVentilationDirID() throws JEVisException {
        VentilationDirID = Snippets.getChildId(getDs(), ISO50001.getJc().getVentilationEquipmentDir().getName(), getObject());
        return VentilationDirID;
    }

    public void setVentilationDirID(Long VentilationDirID) {
        this.VentilationDirID = VentilationDirID;
    }

    public JsonObject getObject() {
        return object;
    }

    public void setObject(JsonObject object) {
        this.object = object;
    }

}
