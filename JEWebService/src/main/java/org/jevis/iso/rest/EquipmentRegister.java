/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.rest;

import org.jevis.iso.add.TemplateChooser;
import org.jevis.iso.add.Translations;
import org.jevis.iso.classes.EquipmentRegisterDirectory;
import org.jevis.iso.classes.ISO50001;
import org.jevis.rest.Config;
import org.jevis.ws.sql.SQLDataSource;
import org.joda.time.DateTime;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
@Path("/JEWebService/v1/equipmentregister")
public class EquipmentRegister {

    /**
     * @param httpHeaders
     * @param site
     * @param lang
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getObject(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @DefaultValue("") @QueryParam("site") String site,
            @DefaultValue("") @QueryParam("lang") String lang
    ) throws Exception {
        SQLDataSource ds = null;

        try {

            ds = new SQLDataSource(httpHeaders, request, url);
            ds.preload(SQLDataSource.PRELOAD.ALL_OBJECT);
            ds.preload(SQLDataSource.PRELOAD.ALL_REL);

            ISO50001 iso = new ISO50001(ds);

            EquipmentRegisterDirectory erDir = iso.getOrganisation().getSite(site).getenergyplanning().getEquipmentregister();

            Map<String, Object> root = new HashMap<>();

            root.put("bauth", httpHeaders.getRequestHeader("authorization").get(0));

            root.put("siteName", site);

            root.put("AirConditioningDirName", erDir.getAirConditioningDirName());
            long eqipmentAirConditioningDirID = erDir.getAirConditioningDirID();
            List<org.jevis.iso.classes.EquipmentAirConditioning> listAirConditioningEquipment = erDir.getListAirConditioning();
            root.put("eqipmentAirConditioningDirID", eqipmentAirConditioningDirID);
            root.put("listAirConditioningEquipment", listAirConditioningEquipment);

            root.put("CompressorDirName", erDir.getCompressorDirName());
            long eqipmentCompressorDirID = erDir.getCompressorDirID();
            List<org.jevis.iso.classes.EquipmentCompressor> listCompressorEquipment = erDir.getListCompressor();
            root.put("eqipmentCompressorDirID", eqipmentCompressorDirID);
            root.put("listCompressorEquipment", listCompressorEquipment);

            root.put("CoolerDirName", erDir.getCoolerDirName());
            long eqipmentCoolerDirID = erDir.getCoolerDirID();
            List<org.jevis.iso.classes.EquipmentCooler> listCoolerEquipment = erDir.getListCooler();
            root.put("eqipmentCoolerDirID", eqipmentCoolerDirID);
            root.put("listCoolerEquipment", listCoolerEquipment);

            root.put("HeaterDirName", erDir.getHeaterDirName());
            long eqipmentHeaterDirID = erDir.getHeaterDirID();
            List<org.jevis.iso.classes.EquipmentHeater> listHeaterEquipment = erDir.getListHeater();
            root.put("eqipmentHeaterDirID", eqipmentHeaterDirID);
            root.put("listHeaterEquipment", listHeaterEquipment);

            root.put("LightingDirName", erDir.getLightingDirName());
            long eqipmentLightingDirID = erDir.getLightingDirID();
            List<org.jevis.iso.classes.EquipmentLighting> listLightingEquipment = erDir.getListLighting();
            root.put("eqipmentLightingDirID", eqipmentLightingDirID);
            root.put("listLightingEquipment", listLightingEquipment);

            root.put("OfficeDirName", erDir.getOfficeDirName());
            long eqipmentOfficeDirID = erDir.getOfficeDirID();
            List<org.jevis.iso.classes.EquipmentOffice> listOfficeEquipment = erDir.getListOffice();
            root.put("eqipmentOfficeDirID", eqipmentOfficeDirID);
            root.put("listOfficeEquipment", listOfficeEquipment);

            root.put("PantryDirName", erDir.getPantryDirName());
            long eqipmentPantryDirID = erDir.getPantryDirID();
            List<org.jevis.iso.classes.EquipmentPantry> listPantryEquipment = erDir.getListPantry();
            root.put("eqipmentPantryDirID", eqipmentPantryDirID);
            root.put("listPantryEquipment", listPantryEquipment);

            root.put("ProductionDirName", erDir.getProductionDirName());
            long eqipmentProductionDirID = erDir.getProductionDirID();
            List<org.jevis.iso.classes.EquipmentProduction> listProductionEquipment = erDir.getListProduction();
            root.put("eqipmentProductionDirID", eqipmentProductionDirID);
            root.put("listProductionEquipment", listProductionEquipment);

            root.put("VentilationDirName", erDir.getVentilationDirName());
            long eqipmentVentilationDirID = erDir.getVentilationDirID();
            List<org.jevis.iso.classes.EquipmentVentilation> listVentilationEquipment = erDir.getListVentilation();
            root.put("eqipmentVentilationDirID", eqipmentVentilationDirID);
            root.put("listVentilationEquipment", listVentilationEquipment);

            if (!lang.equals("")) {
                Translations t = new Translations();
                root.put("addAirConditioning", t.getTranslatedKey(lang, "Add Air Conditioning"));
                root.put("addCompressor", t.getTranslatedKey(lang, "Add Compressor"));
                root.put("addCooler", t.getTranslatedKey(lang, "Add Cooler"));
                root.put("addHeater", t.getTranslatedKey(lang, "Add Heater"));
                root.put("addLighting", t.getTranslatedKey(lang, "Add Lighting"));
                root.put("addOfficeEquipment", t.getTranslatedKey(lang, "Add Office Equipment"));
                root.put("addPantryEquipment", t.getTranslatedKey(lang, "Add Pantry Equipment"));
                root.put("addProductionEquipment", t.getTranslatedKey(lang, "Add Production Equipment"));
                root.put("addVentilation", t.getTranslatedKey(lang, "Add Ventilation"));
                root.put("assetRegister", t.getTranslatedKey(lang, "Asset Register for "));
            } else {
                root.put("addAirConditioning", "Add Air Conditioning");
                root.put("addCompressor", "Add Compressor");
                root.put("addCooler", "Add Cooler");
                root.put("addHeater", "Add Heater");
                root.put("addLighting", "Add Lighting");
                root.put("addOfficeEquipment", "Add Office Equipment");
                root.put("addPantryEquipment", "Add Pantry Equipment");
                root.put("addProductionEquipment", "Add Production Equipment");
                root.put("addVentilation", "Add Ventilation");
                root.put("assetRegister", "Asset Register for ");
            }

            root.put("year", DateTime.now().getYear());

            TemplateChooser tc = new TemplateChooser(root, "equipmentregister");

            return Response.ok(tc.getOutput()).build();
        } finally {
            Config.CloseDS(ds);
        }
    }
}