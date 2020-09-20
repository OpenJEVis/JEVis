/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.rest;

import org.jevis.commons.ws.sql.Config;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.jevis.iso.add.*;
import org.jevis.iso.classes.*;
import org.joda.time.DateTime;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
@Path("/JEWebService/v1/assetregister")
public class AssetRegister {

    /**
     * @param httpHeaders
     * @param site
     * @param lang
     * @param year
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getObject(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @DefaultValue("") @QueryParam("site") String site,
            @DefaultValue("") @QueryParam("lang") String lang,
            @DefaultValue("") @QueryParam("year") Integer year
    ) throws Exception {
        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            ds.preload(SQLDataSource.PRELOAD.ALL_OBJECT);
            ds.preload(SQLDataSource.PRELOAD.ALL_REL);
            ISO50001 iso = new ISO50001(ds);

            Map<String, Object> root = new HashMap<>();

            root.put("bauth", httpHeaders.getRequestHeader("authorization").get(0));
            if (Objects.isNull(year) || year.equals("")) {
                year = DateTime.now().getYear();
            }

            //calculate captured energy consumption
            List<AssetHelper> listAH = new ArrayList<>();

            List<Double> calculatedConsumption = new ArrayList<>();
            List<Double> co2Emissions = new ArrayList<>();
            List<Double> costs = new ArrayList<>();

            Double overallCalculatedConsumption = 0.0;
            Double overallCO2Emissions = 0.0;
            Double overallCosts = 0.0;

            EnergyPlanningDirectory energyPlanning = iso.getOrganisation().getSite(site).getenergyplanning();
            EquipmentRegisterDirectory equipmentRegister = energyPlanning.getEquipmentregister();
            List<EnergySource> energySources = energyPlanning.getEnergySources();
            for (EnergySource es : energySources) {
                calculatedConsumption.add(0.0);
                co2Emissions.add(0.0);
                costs.add(0.0);
            }

            for (EquipmentAirConditioning eac : equipmentRegister.getListAirConditioning()) {
                if (Objects.nonNull(ds.getObject(eac.getEnergySource()))) {
                    AssetHelper ah = new AssetHelper();
                    ah.setName(eac.getName());
                    ah.setEnergysource(ds.getObject(eac.getEnergySource()).getName());
                    ah.setYearlyconsumption(eac.getYearlyconsumption(year));
                    ah.setCo2emissions(ah.getYearlyconsumption() * energyPlanning.getEnergySource(ah.getEnergysource()).getCO2EmissionFactor());
                    listAH.add(ah);

                    for (EnergySource es : energySources) {
                        if (Objects.nonNull(calculatedConsumption.get(energySources.indexOf(es)))) {
                            if (ah.getEnergysource().equals(es.getName())) {
                                calculatedConsumption.set(energySources.indexOf(es), calculatedConsumption.get(energySources.indexOf(es)) + ah.getYearlyconsumption());
                                co2Emissions.set(energySources.indexOf(es), co2Emissions.get(energySources.indexOf(es)) + ah.getCo2emissions());
                            }
                        }
                    }
                }
            }

            for (EquipmentCompressor ec : equipmentRegister.getListCompressor()) {
                if (Objects.nonNull(ds.getObject(ec.getEnergySource()))) {
                    AssetHelper ah = new AssetHelper();
                    ah.setName(ec.getName());
                    ah.setEnergysource(ds.getObject(ec.getEnergySource()).getName());
                    ah.setYearlyconsumption(ec.getYearlyconsumption(year));
                    ah.setCo2emissions(ah.getYearlyconsumption() * energyPlanning.getEnergySource(ah.getEnergysource()).getCO2EmissionFactor());
                    listAH.add(ah);

                    for (EnergySource es : energySources) {
                        if (Objects.nonNull(calculatedConsumption.get(energySources.indexOf(es)))) {
                            if (ah.getEnergysource().equals(es.getName())) {
                                calculatedConsumption.set(energySources.indexOf(es), calculatedConsumption.get(energySources.indexOf(es)) + ah.getYearlyconsumption());
                                co2Emissions.set(energySources.indexOf(es), co2Emissions.get(energySources.indexOf(es)) + ah.getCo2emissions());
                            }
                        }
                    }
                }
            }
            for (EquipmentCooler ec : equipmentRegister.getListCooler()) {
                if (Objects.nonNull(ds.getObject(ec.getEnergySource()))) {
                    AssetHelper ah = new AssetHelper();
                    ah.setName(ec.getName());
                    ah.setEnergysource(ds.getObject(ec.getEnergySource()).getName());
                    ah.setYearlyconsumption(ec.getYearlyconsumption(year));
                    ah.setCo2emissions(ah.getYearlyconsumption() * energyPlanning.getEnergySource(ah.getEnergysource()).getCO2EmissionFactor());
                    listAH.add(ah);

                    for (EnergySource es : energySources) {
                        if (Objects.nonNull(calculatedConsumption.get(energySources.indexOf(es)))) {
                            if (ah.getEnergysource().equals(es.getName())) {
                                calculatedConsumption.set(energySources.indexOf(es), calculatedConsumption.get(energySources.indexOf(es)) + ah.getYearlyconsumption());
                                co2Emissions.set(energySources.indexOf(es), co2Emissions.get(energySources.indexOf(es)) + ah.getCo2emissions());
                            }
                        }
                    }
                }
            }
            for (EquipmentHeater eh : equipmentRegister.getListHeater()) {
                if (Objects.nonNull(ds.getObject(eh.getEnergySource()))) {
                    AssetHelper ah = new AssetHelper();
                    ah.setName(eh.getName());
                    ah.setEnergysource(ds.getObject(eh.getEnergySource()).getName());
                    ah.setYearlyconsumption(eh.getYearlyconsumption(year));
                    ah.setCo2emissions(ah.getYearlyconsumption() * energyPlanning.getEnergySource(ah.getEnergysource()).getCO2EmissionFactor());
                    listAH.add(ah);

                    for (EnergySource es : energySources) {
                        if (Objects.nonNull(calculatedConsumption.get(energySources.indexOf(es)))) {
                            if (ah.getEnergysource().equals(es.getName())) {
                                calculatedConsumption.set(energySources.indexOf(es), calculatedConsumption.get(energySources.indexOf(es)) + ah.getYearlyconsumption());
                                co2Emissions.set(energySources.indexOf(es), co2Emissions.get(energySources.indexOf(es)) + ah.getCo2emissions());
                            }
                        }
                    }
                }
            }
            for (EquipmentLighting el : equipmentRegister.getListLighting()) {
                if (Objects.nonNull(ds.getObject(el.getEnergySource()))) {
                    AssetHelper ah = new AssetHelper();
                    ah.setName(el.getName());
                    ah.setEnergysource(ds.getObject(el.getEnergySource()).getName());
                    ah.setYearlyconsumption(el.getYearlyconsumption(year));
                    ah.setCo2emissions(ah.getYearlyconsumption() * energyPlanning.getEnergySource(ah.getEnergysource()).getCO2EmissionFactor());
                    listAH.add(ah);

                    for (EnergySource es : energySources) {
                        if (Objects.nonNull(calculatedConsumption.get(energySources.indexOf(es)))) {
                            if (ah.getEnergysource().equals(es.getName())) {
                                calculatedConsumption.set(energySources.indexOf(es), calculatedConsumption.get(energySources.indexOf(es)) + ah.getYearlyconsumption());
                                co2Emissions.set(energySources.indexOf(es), co2Emissions.get(energySources.indexOf(es)) + ah.getCo2emissions());
                            }
                        }
                    }
                }
            }
            for (EquipmentOffice eo : equipmentRegister.getListOffice()) {
                if (Objects.nonNull(ds.getObject(eo.getEnergySource()))) {
                    AssetHelper ah = new AssetHelper();
                    ah.setName(eo.getName());
                    ah.setEnergysource(ds.getObject(eo.getEnergySource()).getName());
                    ah.setYearlyconsumption(eo.getYearlyconsumption(year));
                    ah.setCo2emissions(ah.getYearlyconsumption() * energyPlanning.getEnergySource(ah.getEnergysource()).getCO2EmissionFactor());
                    listAH.add(ah);

                    for (EnergySource es : energySources) {
                        if (Objects.nonNull(calculatedConsumption.get(energySources.indexOf(es)))) {
                            if (ah.getEnergysource().equals(es.getName())) {
                                calculatedConsumption.set(energySources.indexOf(es), calculatedConsumption.get(energySources.indexOf(es)) + eo.getYearlyconsumption());
                                co2Emissions.set(energySources.indexOf(es), co2Emissions.get(energySources.indexOf(es)) + ah.getCo2emissions());
                            }
                        }
                    }
                }
            }
            for (EquipmentPantry ep : equipmentRegister.getListPantry()) {
                if (Objects.nonNull(ds.getObject(ep.getEnergySource()))) {
                    AssetHelper ah = new AssetHelper();
                    ah.setName(ep.getName());
                    ah.setEnergysource(ds.getObject(ep.getEnergySource()).getName());
                    ah.setYearlyconsumption(ep.getYearlyconsumption(year));
                    ah.setCo2emissions(ah.getYearlyconsumption() * energyPlanning.getEnergySource(ah.getEnergysource()).getCO2EmissionFactor());
                    listAH.add(ah);

                    for (EnergySource es : energySources) {
                        if (Objects.nonNull(calculatedConsumption.get(energySources.indexOf(es)))) {
                            if (ah.getEnergysource().equals(es.getName())) {
                                calculatedConsumption.set(energySources.indexOf(es), calculatedConsumption.get(energySources.indexOf(es)) + ep.getYearlyconsumption());
                                co2Emissions.set(energySources.indexOf(es), co2Emissions.get(energySources.indexOf(es)) + ah.getCo2emissions());
                            }
                        }
                    }
                }
            }
            for (EquipmentProduction ep : equipmentRegister.getListProduction()) {
                if (Objects.nonNull(ds.getObject(ep.getEnergySource()))) {
                    AssetHelper ah = new AssetHelper();
                    ah.setName(ep.getName());
                    ah.setEnergysource(ds.getObject(ep.getEnergySource()).getName());
                    ah.setYearlyconsumption(ep.getYearlyconsumption(year));
                    ah.setCo2emissions(ah.getYearlyconsumption() * energyPlanning.getEnergySource(ah.getEnergysource()).getCO2EmissionFactor());
                    listAH.add(ah);

                    for (EnergySource es : energySources) {
                        if (Objects.nonNull(calculatedConsumption.get(energySources.indexOf(es)))) {
                            if (ah.getEnergysource().equals(es.getName())) {
                                calculatedConsumption.set(energySources.indexOf(es), calculatedConsumption.get(energySources.indexOf(es)) + ep.getYearlyconsumption());
                                co2Emissions.set(energySources.indexOf(es), co2Emissions.get(energySources.indexOf(es)) + ah.getCo2emissions());
                            }
                        }
                    }
                }
            }
            for (EquipmentVentilation ev : equipmentRegister.getListVentilation()) {
                if (Objects.nonNull(ds.getObject(ev.getEnergySource()))) {
                    AssetHelper ah = new AssetHelper();
                    ah.setName(ev.getName());
                    ah.setEnergysource(ds.getObject(ev.getEnergySource()).getName());
                    ah.setYearlyconsumption(ev.getYearlyconsumption(year));
                    ah.setCo2emissions(ah.getYearlyconsumption() * energyPlanning.getEnergySource(ah.getEnergysource()).getCO2EmissionFactor());
                    listAH.add(ah);

                    for (EnergySource es : energySources) {
                        if (Objects.nonNull(calculatedConsumption.get(energySources.indexOf(es)))) {
                            if (ah.getEnergysource().equals(es.getName())) {
                                calculatedConsumption.set(energySources.indexOf(es), calculatedConsumption.get(energySources.indexOf(es)) + ev.getYearlyconsumption());
                                co2Emissions.set(energySources.indexOf(es), co2Emissions.get(energySources.indexOf(es)) + ah.getCo2emissions());
                            }
                        }
                    }
                }
            }

            Table tablecaptionsForHeaderForAssetRegister = new Table(new TableColumn[]{
                    new TableColumn("Asset"),
                    new TableColumn("Energy Source"),
                    new TableColumn("Energy Consumption<br>kWh"),
                    new TableColumn("Share of total energysource consumption<br>%"),
                    new TableColumn("Share of total consumption<br>%"),
                    new TableColumn("CO2-emissions<br>kg"),
                    new TableColumn("CO2-share of total<br>%")});

            Table tablecaptionsAssetRegister = new Table(new TableColumn[]{new TableColumn("name"), new TableColumn("energysource"),
                    new TableColumn("yearlyconsumption"), new TableColumn("shareoftotalesconsumption"),
                    new TableColumn("shareoftotalconsumption"), new TableColumn("co2emissions"), new TableColumn("co2shareoftotal")});

            for (Double d : calculatedConsumption) {
                overallCalculatedConsumption += d;
                overallCO2Emissions += co2Emissions.get(calculatedConsumption.indexOf(d));
            }

            for (AssetHelper ah : listAH) {
                for (EnergySource es : energySources) {
                    if (energySources.get(energySources.indexOf(es)).getName().equals(ah.getEnergysource())) {
                        ah.setShareoftotalesconsumption(divZeroFix(ah.getYearlyconsumption(), calculatedConsumption.get(energySources.indexOf(es))) * 100);
                        ah.setShareoftotalconsumption(divZeroFix(ah.getYearlyconsumption(), overallCalculatedConsumption) * 100);
                        overallCO2Emissions += ah.getCo2emissions();
                    }
                }
            }

            for (AssetHelper ah : listAH) {
                ah.setCo2shareoftotal(divZeroFix(ah.getCo2emissions(), overallCO2Emissions) * 100);
            }

            root.put("tablecaptionsForHeaderForAssetRegister", tablecaptionsForHeaderForAssetRegister);
            root.put("tablecaptionsAssetRegister", tablecaptionsAssetRegister);
            root.put("assetregister", listAH);
            root.put("siteName", site);
            root.put("lastyear", year - 1);
            root.put("year", year);
            root.put("nextyear", year + 1);

            if (!lang.equals("")) {
                Translations t = new Translations();
                root.put("assetRegister", t.getTranslatedKey(lang, "Asset Register for "));
            } else {
                root.put("assetRegister", "Asset Register for ");
            }

            TemplateChooser tc = new TemplateChooser(root, "assetregister");
            return Response.ok(tc.getOutput()).build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    private Double divZeroFix(Double dividend, Double divisor) {
        Double d = 0.0;

        d = dividend / divisor;

        if (d.isNaN()) {
            return 0.0;
        } else {
            return d;
        }
    }
}
