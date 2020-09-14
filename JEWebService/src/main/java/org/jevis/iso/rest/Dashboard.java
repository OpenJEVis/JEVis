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
@Path("/JEWebService/v1/dashboard")
public class Dashboard {

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
            Map<String, Object> root = new HashMap<>();

            root.put("bauth", httpHeaders.getRequestHeader("authorization").get(0));

            //get the last three years
            long lastYear = DateTime.now().getYear() - 1;
            root.put("listenergyconsumptionlastname", lastYear);
            root.put("listenergyconsumptionlastlastname", lastYear - 1);
            root.put("listenergyconsumptionlastlastlastname", lastYear - 2);

            Boolean energysource1 = false;
            Boolean energysource2 = false;
            Boolean energysource3 = false;
            Boolean energysource4 = false;
            Boolean energysource5 = false;

            Double totalenergyconsumptionlast = 0.0;
            Double totalenergyconsumptionlastlast = 0.0;
            Double totalenergyconsumptionlastlastlast = 0.0;

            Double totalcostslast = 0.0;
            Double totalcostslastlast = 0.0;
            Double totalcostslastlastlast = 0.0;

            //a maximum of five energy sources supported as of yet
            EnergyConsumption eclast1 = new EnergyConsumption();
            EnergyConsumption eclastlast1 = new EnergyConsumption();
            EnergyConsumption eclastlastlast1 = new EnergyConsumption();
            List<Double> energyconsumptionlast1 = new ArrayList<>();
            List<Double> energyconsumptionlastlast1 = new ArrayList<>();
            List<Double> energyconsumptionlastlastlast1 = new ArrayList<>();

            EnergyConsumption eclast2 = new EnergyConsumption();
            EnergyConsumption eclastlast2 = new EnergyConsumption();
            EnergyConsumption eclastlastlast2 = new EnergyConsumption();
            List<Double> energyconsumptionlast2 = new ArrayList<>();
            List<Double> energyconsumptionlastlast2 = new ArrayList<>();
            List<Double> energyconsumptionlastlastlast2 = new ArrayList<>();

            EnergyConsumption eclast3 = new EnergyConsumption();
            EnergyConsumption eclastlast3 = new EnergyConsumption();
            EnergyConsumption eclastlastlast3 = new EnergyConsumption();
            List<Double> energyconsumptionlast3 = new ArrayList<>();
            List<Double> energyconsumptionlastlast3 = new ArrayList<>();
            List<Double> energyconsumptionlastlastlast3 = new ArrayList<>();

            EnergyConsumption eclast4 = new EnergyConsumption();
            EnergyConsumption eclastlast4 = new EnergyConsumption();
            EnergyConsumption eclastlastlast4 = new EnergyConsumption();
            List<Double> energyconsumptionlast4 = new ArrayList<>();
            List<Double> energyconsumptionlastlast4 = new ArrayList<>();
            List<Double> energyconsumptionlastlastlast4 = new ArrayList<>();

            EnergyConsumption eclast5 = new EnergyConsumption();
            EnergyConsumption eclastlast5 = new EnergyConsumption();
            EnergyConsumption eclastlastlast5 = new EnergyConsumption();
            List<Double> energyconsumptionlast5 = new ArrayList<>();
            List<Double> energyconsumptionlastlast5 = new ArrayList<>();
            List<Double> energyconsumptionlastlastlast5 = new ArrayList<>();

            List<EnergyConsumption> consumption1 = new ArrayList<>();
            List<EnergyConsumption> consumption2 = new ArrayList<>();
            List<EnergyConsumption> consumption3 = new ArrayList<>();

            EnergyPlanningDirectory energyPlanning = iso.getOrganisation().getSite(site).getenergyplanning();

            List<org.jevis.iso.classes.Produce> production1 = new ArrayList<>();
            production1 = energyPlanning.getProduction(lastYear);
            List<org.jevis.iso.classes.Produce> production2 = new ArrayList<>();
            production2 = energyPlanning.getProduction(lastYear - 1);
            List<org.jevis.iso.classes.Produce> production3 = new ArrayList<>();
            production3 = energyPlanning.getProduction(lastYear - 2);

            String energysource1name = "";
            String energysource2name = "";
            String energysource3name = "";
            String energysource4name = "";
            String energysource5name = "";

            List<EnergySource> listES = iso.getOrganisation().getSite(site).getenergyplanning().getEnergySources();

            for (EnergySource es : listES) {

                switch (listES.indexOf(es)) {
                    case 0:
                        energysource1name = es.getName();
                        eclast1 = es.getEnergyConsumption(lastYear);
                        eclast1.setname(energysource1name);
                        eclastlast1 = es.getEnergyConsumption(lastYear - 1);
                        eclastlast1.setname(energysource1name);
                        eclastlastlast1 = es.getEnergyConsumption(lastYear - 2);
                        eclastlastlast1.setname(energysource1name);
                        energyconsumptionlast1 = eclast1.getList();
                        totalenergyconsumptionlast += eclast1.getSum();
                        totalcostslast += eclast1.getCosts();
                        energyconsumptionlastlast1 = eclastlast1.getList();
                        totalenergyconsumptionlastlast += eclastlast1.getSum();
                        totalcostslastlast += eclastlast1.getCosts();
                        energyconsumptionlastlastlast1 = eclastlastlast1.getList();
                        totalenergyconsumptionlastlastlast += eclastlastlast1.getSum();
                        totalcostslastlastlast += eclastlastlast1.getCosts();
                        energysource1 = true;
                        root.put("energysource1name", energysource1name);
                        root.put("listenergyconsumption1last", energyconsumptionlast1);
                        root.put("listenergyconsumption1lastlast", energyconsumptionlastlast1);
                        root.put("listenergyconsumption1lastlastlast", energyconsumptionlastlastlast1);
                        consumption1.add(eclast1);
                        consumption2.add(eclastlast1);
                        consumption3.add(eclastlastlast1);
                        break;
                    case 1:
                        energysource2name = es.getName();
                        eclast2 = es.getEnergyConsumption(lastYear);
                        eclast2.setname(energysource2name);
                        eclastlast2 = es.getEnergyConsumption(lastYear - 1);
                        eclastlast2.setname(energysource2name);
                        eclastlastlast2 = es.getEnergyConsumption(lastYear - 2);
                        eclastlastlast2.setname(energysource2name);
                        energyconsumptionlast2 = eclast2.getList();
                        totalenergyconsumptionlast += eclast2.getSum();
                        totalcostslast += eclast2.getCosts();
                        energyconsumptionlastlast2 = eclastlast2.getList();
                        totalenergyconsumptionlastlast += eclastlast2.getSum();
                        totalcostslastlast += eclastlast2.getCosts();
                        energyconsumptionlastlastlast2 = eclastlastlast2.getList();
                        totalenergyconsumptionlastlastlast += eclastlastlast2.getSum();
                        totalcostslastlastlast += eclastlastlast2.getCosts();
                        energysource2 = true;
                        root.put("energysource2name", energysource2name);
                        root.put("listenergyconsumption2last", energyconsumptionlast2);
                        root.put("listenergyconsumption2lastlast", energyconsumptionlastlast2);
                        root.put("listenergyconsumption2lastlastlast", energyconsumptionlastlastlast2);
                        consumption1.add(eclast2);
                        consumption2.add(eclastlast2);
                        consumption3.add(eclastlastlast2);
                        break;
                    case 2:
                        energysource3name = es.getName();
                        eclast3 = es.getEnergyConsumption(lastYear);
                        eclast3.setname(energysource3name);
                        eclastlast3 = es.getEnergyConsumption(lastYear - 1);
                        eclastlast3.setname(energysource3name);
                        eclastlastlast3 = es.getEnergyConsumption(lastYear - 2);
                        eclastlastlast3.setname(energysource3name);
                        energyconsumptionlast3 = eclast3.getList();
                        totalenergyconsumptionlast += eclast3.getSum();
                        totalcostslast += eclast3.getCosts();
                        energyconsumptionlastlast3 = eclastlast3.getList();
                        totalenergyconsumptionlastlast += eclastlast3.getSum();
                        totalcostslastlast += eclastlast3.getCosts();
                        energyconsumptionlastlastlast3 = eclastlastlast3.getList();
                        totalenergyconsumptionlastlastlast += eclastlastlast3.getSum();
                        totalcostslastlastlast += eclastlastlast3.getCosts();
                        energysource3 = true;
                        root.put("energysource3name", energysource3name);
                        root.put("listenergyconsumption3last", energyconsumptionlast3);
                        root.put("listenergyconsumption3lastlast", energyconsumptionlastlast3);
                        root.put("listenergyconsumption3lastlastlast", energyconsumptionlastlastlast3);
                        consumption1.add(eclast3);
                        consumption2.add(eclastlast3);
                        consumption3.add(eclastlastlast3);
                        break;
                    case 3:
                        energysource4name = es.getName();
                        eclast4 = es.getEnergyConsumption(lastYear);
                        eclast4.setname(energysource4name);
                        eclastlast4 = es.getEnergyConsumption(lastYear - 1);
                        eclastlast4.setname(energysource4name);
                        eclastlastlast4 = es.getEnergyConsumption(lastYear - 2);
                        eclastlastlast4.setname(energysource4name);
                        energyconsumptionlast4 = eclast4.getList();
                        totalenergyconsumptionlast += eclast4.getSum();
                        totalcostslast += eclast4.getCosts();
                        energyconsumptionlastlast4 = eclastlast4.getList();
                        totalenergyconsumptionlastlast += eclastlast4.getSum();
                        totalcostslastlast += eclastlast4.getCosts();
                        energyconsumptionlastlastlast4 = eclastlastlast4.getList();
                        totalenergyconsumptionlastlastlast += eclastlastlast4.getSum();
                        totalcostslastlastlast += eclastlastlast4.getCosts();
                        energysource4 = true;
                        root.put("energysource4name", energysource4name);
                        root.put("listenergyconsumption4last", energyconsumptionlast4);
                        root.put("listenergyconsumption4lastlast", energyconsumptionlastlast4);
                        root.put("listenergyconsumption4lastlastlast", energyconsumptionlastlastlast4);
                        consumption1.add(eclast4);
                        consumption2.add(eclastlast4);
                        consumption3.add(eclastlastlast4);
                        break;
                    case 4:
                        energysource5name = es.getName();
                        eclast5 = es.getEnergyConsumption(lastYear);
                        eclast5.setname(energysource5name);
                        eclastlast5 = es.getEnergyConsumption(lastYear - 1);
                        eclastlast5.setname(energysource5name);
                        eclastlastlast5 = es.getEnergyConsumption(lastYear - 2);
                        eclastlastlast5.setname(energysource5name);
                        energyconsumptionlast5 = eclast5.getList();
                        totalenergyconsumptionlast += eclast5.getSum();
                        totalcostslast += eclast5.getCosts();
                        energyconsumptionlastlast5 = eclastlast5.getList();
                        totalenergyconsumptionlastlast += eclastlast5.getSum();
                        totalcostslastlast += eclastlast5.getCosts();
                        energyconsumptionlastlastlast5 = eclastlastlast5.getList();
                        totalenergyconsumptionlastlastlast += eclastlastlast5.getSum();
                        totalcostslastlastlast += eclastlastlast5.getCosts();
                        energysource5 = true;
                        root.put("energysource5name", energysource5name);
                        root.put("listenergyconsumption5last", energyconsumptionlast5);
                        root.put("listenergyconsumption5lastlast", energyconsumptionlastlast5);
                        root.put("listenergyconsumption5lastlastlast", energyconsumptionlastlastlast5);
                        consumption1.add(eclast5);
                        consumption2.add(eclastlast5);
                        consumption3.add(eclastlastlast5);
                        break;
                    default:
                        break;
                }
            }

            EnergyConsumption total1 = new EnergyConsumption();
            total1.setYear(lastYear);
            total1.setname("total");
            total1.setSum(totalenergyconsumptionlast);
            total1.setCosts(totalcostslast);
            consumption1.add(total1);

            for (EnergyConsumption ec : consumption1) {
                ec.setShareoftotalconsumption(divZeroFix(ec.getSum(), totalenergyconsumptionlast) * 100);
                ec.setCostrelated(divZeroFix(ec.getCosts(), totalcostslast) * 100);
                for (EnergySource es : listES) {
                    if (es.getName().equals(ec.getType())) {
                        ec.setCo2emissions(ec.getSum() * es.getCO2EmissionFactor());
                    }
                }
            }

            double totalCO2Consumption1 = 0.0;
            for (EnergyConsumption ec : consumption1) {
                if (consumption1.indexOf(ec) < (consumption1.size() - 1)) {
                    totalCO2Consumption1 += ec.getCo2emissions();
                }
            }
            consumption1.get(consumption1.size() - 1).setCo2emissions(totalCO2Consumption1);

            for (EnergyConsumption ec : consumption1) {
                ec.setCo2shareoftotal(divZeroFix(ec.getCo2emissions(), totalCO2Consumption1) * 100);
            }

            Table tablecaptionsForHeader;
            if (!lang.equals("")) {
                Translations t = new Translations();
                tablecaptionsForHeader = new Table(new TableColumn[]{
                        new TableColumn(t.getTranslatedKey(lang, "Year")),
                        new TableColumn(t.getTranslatedKey(lang, "Energy Source")),
                        new TableColumn(t.getTranslatedKey(lang, "Energy Consumption<br>kWh")),
                        new TableColumn(t.getTranslatedKey(lang, "Share of total consumption<br>%")),
                        new TableColumn(t.getTranslatedKey(lang, "Costs<br>€")),
                        new TableColumn(t.getTranslatedKey(lang, "Cost-share of total<br>%")),
                        new TableColumn(t.getTranslatedKey(lang, "CO2-emissions<br>kg")),
                        new TableColumn(t.getTranslatedKey(lang, "CO2-share of total<br>%"))});
            } else {
                tablecaptionsForHeader = new Table(new TableColumn[]{
                        new TableColumn("Year"),
                        new TableColumn("Energy Source"),
                        new TableColumn("Energy Consumption<br>kWh"),
                        new TableColumn("Share of total consumption<br>%"),
                        new TableColumn("Costs<br>€"),
                        new TableColumn("Cost-share of total<br>%"),
                        new TableColumn("CO2-emissions<br>kg"),
                        new TableColumn("CO2-share of total<br>%")});

            }
            Table tablecaptions1 = new Table(new TableColumn[]{new TableColumn("year"), new TableColumn("name"), new TableColumn("sum"),
                    new TableColumn("shareoftotalconsumption"), new TableColumn("costs"), new TableColumn("costrelated"), new TableColumn("co2emissions"),
                    new TableColumn("co2shareoftotal")});

            root.put("tablecaptionsForHeader", tablecaptionsForHeader);
            root.put("tablecaptions1", tablecaptions1);
            root.put("tabledata1", consumption1);

            EnergyConsumption total2 = new EnergyConsumption();
            total2.setYear(lastYear - 1);
            total2.setname("total");
            total2.setSum(totalenergyconsumptionlastlast);
            total2.setCosts(totalcostslastlast);
            consumption2.add(total2);

            for (EnergyConsumption ec : consumption2) {
                ec.setShareoftotalconsumption(divZeroFix(ec.getSum(), totalenergyconsumptionlastlast) * 100);
                ec.setCostrelated(divZeroFix(ec.getCosts(), totalcostslastlast) * 100);
                for (EnergySource es : listES) {
                    if (es.getName().equals(ec.getType())) {
                        ec.setCo2emissions(ec.getSum() * es.getCO2EmissionFactor());
                    }
                }
            }

            double totalCO2Consumption2 = 0.0;
            for (EnergyConsumption ec : consumption2) {
                if (consumption2.indexOf(ec) < (consumption2.size() - 1)) {
                    totalCO2Consumption2 += ec.getCo2emissions();
                }
            }
            consumption2.get(consumption2.size() - 1).setCo2emissions(totalCO2Consumption2);

            for (EnergyConsumption ec : consumption2) {
                ec.setCo2shareoftotal(divZeroFix(ec.getCo2emissions(), totalCO2Consumption2) * 100);
            }

            Table tablecaptions2 = new Table(new TableColumn[]{new TableColumn("year"), new TableColumn("name"), new TableColumn("sum"),
                    new TableColumn("shareoftotalconsumption"), new TableColumn("costs"), new TableColumn("costrelated"), new TableColumn("co2emissions"),
                    new TableColumn("co2shareoftotal")});

            root.put("tablecaptions2", tablecaptions2);
            root.put("tabledata2", consumption2);

            EnergyConsumption total3 = new EnergyConsumption();
            total3.setYear(lastYear - 2);
            total3.setname("total");
            total3.setSum(totalenergyconsumptionlastlastlast);
            total3.setCosts(totalcostslastlastlast);
            consumption3.add(total3);

            for (EnergyConsumption ec : consumption3) {
                ec.setShareoftotalconsumption(divZeroFix(ec.getSum(), totalenergyconsumptionlastlastlast) * 100);
                ec.setCostrelated(divZeroFix(ec.getCosts(), totalcostslastlastlast) * 100);
                for (EnergySource es : listES) {
                    if (es.getName().equals(ec.getType())) {
                        ec.setCo2emissions(ec.getSum() * es.getCO2EmissionFactor());
                    }
                }
            }

            double totalCO2Consumption3 = 0.0;
            for (EnergyConsumption ec : consumption3) {
                if (consumption3.indexOf(ec) < (consumption3.size() - 1)) {
                    totalCO2Consumption3 += ec.getCo2emissions();
                }
            }
            consumption3.get(consumption3.size() - 1).setCo2emissions(totalCO2Consumption3);

            for (EnergyConsumption ec : consumption3) {
                ec.setCo2shareoftotal(divZeroFix(ec.getCo2emissions(), totalCO2Consumption3) * 100);
            }

            Table tablecaptions3 = new Table(new TableColumn[]{new TableColumn("year"), new TableColumn("name"), new TableColumn("sum"),
                    new TableColumn("shareoftotalconsumption"), new TableColumn("costs"), new TableColumn("costrelated"), new TableColumn("co2emissions"),
                    new TableColumn("co2shareoftotal")});

            root.put("tablecaptions3", tablecaptions3);
            root.put("tabledata3", consumption3);

            BasicEnPIs lastBasicEnPIs = new BasicEnPIs(lastYear, listES, production1);
            BasicEnPIs lastlastBasicEnPIs = new BasicEnPIs(lastYear - 1, listES, production2);
            BasicEnPIs lastlastlastBasicEnPIs = new BasicEnPIs(lastYear - 2, listES, production3);

            List<Double> lastTotalEnPIs = new ArrayList<>();
            lastTotalEnPIs = lastBasicEnPIs.getListTotalEnPIs();
            List<Double> lastlastTotalEnPIs = new ArrayList<>();
            lastlastTotalEnPIs = lastlastBasicEnPIs.getListTotalEnPIs();
            List<Double> lastlastlastTotalEnPIs = new ArrayList<>();
            lastlastlastTotalEnPIs = lastlastlastBasicEnPIs.getListTotalEnPIs();

            root.put("lastTotalEnPIs", lastTotalEnPIs);
            root.put("lastlastTotalEnPIs", lastlastTotalEnPIs);
            root.put("lastlastlastTotalEnPIs", lastlastlastTotalEnPIs);

            List<BasicEnPIs> listBasicEnPIs = new ArrayList<>();
            listBasicEnPIs.add(lastBasicEnPIs);
            listBasicEnPIs.add(lastlastBasicEnPIs);
            listBasicEnPIs.add(lastlastlastBasicEnPIs);

            Table tablecaptionsEnPIs;
            if (!lang.equals("")) {
                Translations t = new Translations();
                tablecaptionsEnPIs = new Table(new TableColumn[]{
                        new TableColumn(t.getTranslatedKey(lang, "Year")), new TableColumn(t.getTranslatedKey(lang, "Year-Round")),
                        new TableColumn(t.getTranslatedKey(lang, "January")), new TableColumn(t.getTranslatedKey(lang, "February")),
                        new TableColumn(t.getTranslatedKey(lang, "March")), new TableColumn(t.getTranslatedKey(lang, "April")),
                        new TableColumn(t.getTranslatedKey(lang, "May")), new TableColumn(t.getTranslatedKey(lang, "June")),
                        new TableColumn(t.getTranslatedKey(lang, "July")), new TableColumn(t.getTranslatedKey(lang, "August")),
                        new TableColumn(t.getTranslatedKey(lang, "September")), new TableColumn(t.getTranslatedKey(lang, "October")),
                        new TableColumn(t.getTranslatedKey(lang, "November")), new TableColumn(t.getTranslatedKey(lang, "December"))});
            } else {
                tablecaptionsEnPIs = new Table(new TableColumn[]{new TableColumn("Year"), new TableColumn("Year-Round"), new TableColumn("January"), new TableColumn("February"),
                        new TableColumn("March"), new TableColumn("April"), new TableColumn("May"), new TableColumn("June"), new TableColumn("July"),
                        new TableColumn("August"), new TableColumn("September"), new TableColumn("October"), new TableColumn("November"), new TableColumn("December")});
            }

            root.put("tablecaptionsEnPIs", tablecaptionsEnPIs);
            root.put("tabledataEnPIs", listBasicEnPIs);

            root.put("energysource1", energysource1);
            root.put("energysource2", energysource2);
            root.put("energysource3", energysource3);
            root.put("energysource4", energysource4);
            root.put("energysource5", energysource5);

            //calculate captured energy consumption
            List<AssetHelper> listAH = new ArrayList<>();

            Double yearlyCalculatedConsumptionSource1 = 0.0;
            Double yearlyCalculatedConsumptionSource2 = 0.0;
            Double yearlyCalculatedConsumptionSource3 = 0.0;
            Double yearlyCalculatedConsumptionSource4 = 0.0;
            Double yearlyCalculatedConsumptionSource5 = 0.0;

            EquipmentRegisterDirectory equipmentRegister = energyPlanning.getEquipmentregister();

            for (EquipmentAirConditioning eac : equipmentRegister.getListAirConditioning()) {

                if (Objects.nonNull(ds.getObject(eac.getEnergySource()))) {
                    AssetHelper ah = new AssetHelper();
                    ah.setName(eac.getName());
                    ah.setEnergysource(ds.getObject(eac.getEnergySource()).getName());
                    ah.setYearlyconsumption(eac.getYearlyconsumption());
                    ah.setShareoftotalconsumption(eac.getShareoftotalconsumption());
                    ah.setCo2emissions(eac.getCo2emissions());
                    ah.setCo2shareoftotal(eac.getCo2shareoftotal());
                    listAH.add(ah);

                    if (eac.getEnergySource().equals(energysource1name)) {
                        yearlyCalculatedConsumptionSource1 += eac.getYearlyconsumption();
                    } else if (eac.getEnergySource().equals(energysource2name)) {
                        yearlyCalculatedConsumptionSource2 += eac.getYearlyconsumption();
                    } else if (eac.getEnergySource().equals(energysource3name)) {
                        yearlyCalculatedConsumptionSource3 += eac.getYearlyconsumption();
                    } else if (eac.getEnergySource().equals(energysource4name)) {
                        yearlyCalculatedConsumptionSource4 += eac.getYearlyconsumption();
                    } else if (eac.getEnergySource().equals(energysource5name)) {
                        yearlyCalculatedConsumptionSource5 += eac.getYearlyconsumption();
                    }
                }
            }

            for (EquipmentCompressor ec : equipmentRegister.getListCompressor()) {
                if (Objects.nonNull(ds.getObject(ec.getEnergySource()))) {
                    AssetHelper ah = new AssetHelper();
                    ah.setName(ec.getName());
                    ah.setEnergysource(ds.getObject(ec.getEnergySource()).getName());
                    ah.setYearlyconsumption(ec.getYearlyconsumption());
                    ah.setShareoftotalconsumption(ec.getShareoftotalconsumption());
                    ah.setCo2emissions(ec.getCo2emissions());
                    ah.setCo2shareoftotal(ec.getCo2shareoftotal());
                    listAH.add(ah);

                    if (ec.getEnergySource().equals(energysource1name)) {
                        yearlyCalculatedConsumptionSource1 += ec.getYearlyconsumption();
                    } else if (ec.getEnergySource().equals(energysource2name)) {
                        yearlyCalculatedConsumptionSource2 += ec.getYearlyconsumption();
                    } else if (ec.getEnergySource().equals(energysource3name)) {
                        yearlyCalculatedConsumptionSource3 += ec.getYearlyconsumption();
                    } else if (ec.getEnergySource().equals(energysource4name)) {
                        yearlyCalculatedConsumptionSource4 += ec.getYearlyconsumption();
                    } else if (ec.getEnergySource().equals(energysource5name)) {
                        yearlyCalculatedConsumptionSource5 += ec.getYearlyconsumption();
                    }
                }
            }
            for (EquipmentCooler ec : equipmentRegister.getListCooler()) {
                if (Objects.nonNull(ds.getObject(ec.getEnergySource()))) {
                    AssetHelper ah = new AssetHelper();
                    ah.setName(ec.getName());
                    ah.setEnergysource(ds.getObject(ec.getEnergySource()).getName());
                    ah.setYearlyconsumption(ec.getYearlyconsumption());
                    ah.setShareoftotalconsumption(ec.getShareoftotalconsumption());
                    ah.setCo2emissions(ec.getCo2emissions());
                    ah.setCo2shareoftotal(ec.getCo2shareoftotal());
                    listAH.add(ah);

                    if (ec.getEnergySource().equals(energysource1name)) {
                        yearlyCalculatedConsumptionSource1 += ec.getYearlyconsumption();
                    } else if (ec.getEnergySource().equals(energysource2name)) {
                        yearlyCalculatedConsumptionSource2 += ec.getYearlyconsumption();
                    } else if (ec.getEnergySource().equals(energysource3name)) {
                        yearlyCalculatedConsumptionSource3 += ec.getYearlyconsumption();
                    } else if (ec.getEnergySource().equals(energysource4name)) {
                        yearlyCalculatedConsumptionSource4 += ec.getYearlyconsumption();
                    } else if (ec.getEnergySource().equals(energysource5name)) {
                        yearlyCalculatedConsumptionSource5 += ec.getYearlyconsumption();
                    }
                }
            }
            for (EquipmentHeater eh : equipmentRegister.getListHeater()) {
                if (Objects.nonNull(ds.getObject(eh.getEnergySource()))) {
                    AssetHelper ah = new AssetHelper();
                    ah.setName(eh.getName());
                    ah.setEnergysource(ds.getObject(eh.getEnergySource()).getName());
                    ah.setYearlyconsumption(eh.getYearlyconsumption());
                    ah.setShareoftotalconsumption(eh.getShareoftotalconsumption());
                    ah.setCo2emissions(eh.getCo2emissions());
                    ah.setCo2shareoftotal(eh.getCo2shareoftotal());
                    listAH.add(ah);

                    if (eh.getEnergySource().equals(energysource1name)) {
                        yearlyCalculatedConsumptionSource1 += eh.getYearlyconsumption();
                    } else if (eh.getEnergySource().equals(energysource2name)) {
                        yearlyCalculatedConsumptionSource2 += eh.getYearlyconsumption();
                    } else if (eh.getEnergySource().equals(energysource3name)) {
                        yearlyCalculatedConsumptionSource3 += eh.getYearlyconsumption();
                    } else if (eh.getEnergySource().equals(energysource4name)) {
                        yearlyCalculatedConsumptionSource4 += eh.getYearlyconsumption();
                    } else if (eh.getEnergySource().equals(energysource5name)) {
                        yearlyCalculatedConsumptionSource5 += eh.getYearlyconsumption();
                    }
                }
            }
            for (EquipmentLighting el : equipmentRegister.getListLighting()) {
                if (Objects.nonNull(ds.getObject(el.getEnergySource()))) {
                    AssetHelper ah = new AssetHelper();
                    ah.setName(el.getName());
                    ah.setEnergysource(ds.getObject(el.getEnergySource()).getName());
                    ah.setYearlyconsumption(el.getYearlyconsumption());
                    ah.setShareoftotalconsumption(el.getShareoftotalconsumption());
                    ah.setCo2emissions(el.getCo2emissions());
                    ah.setCo2shareoftotal(el.getCo2shareoftotal());
                    listAH.add(ah);

                    if (el.getEnergySource().equals(energysource1name)) {
                        yearlyCalculatedConsumptionSource1 += el.getYearlyconsumption();
                    } else if (el.getEnergySource().equals(energysource2name)) {
                        yearlyCalculatedConsumptionSource2 += el.getYearlyconsumption();
                    } else if (el.getEnergySource().equals(energysource3name)) {
                        yearlyCalculatedConsumptionSource3 += el.getYearlyconsumption();
                    } else if (el.getEnergySource().equals(energysource4name)) {
                        yearlyCalculatedConsumptionSource4 += el.getYearlyconsumption();
                    } else if (el.getEnergySource().equals(energysource5name)) {
                        yearlyCalculatedConsumptionSource5 += el.getYearlyconsumption();
                    }
                }
            }
            for (EquipmentOffice eo : equipmentRegister.getListOffice()) {
                if (Objects.nonNull(ds.getObject(eo.getEnergySource()))) {
                    AssetHelper ah = new AssetHelper();
                    ah.setName(eo.getName());
                    ah.setEnergysource(ds.getObject(eo.getEnergySource()).getName());
                    ah.setYearlyconsumption(eo.getYearlyconsumption());
                    ah.setShareoftotalconsumption(eo.getShareoftotalconsumption());
                    ah.setCo2emissions(eo.getCo2emissions());
                    ah.setCo2shareoftotal(eo.getCo2shareoftotal());
                    listAH.add(ah);

                    if (eo.getEnergySource().equals(energysource1name)) {
                        yearlyCalculatedConsumptionSource1 += eo.getYearlyconsumption();
                    } else if (eo.getEnergySource().equals(energysource2name)) {
                        yearlyCalculatedConsumptionSource2 += eo.getYearlyconsumption();
                    } else if (eo.getEnergySource().equals(energysource3name)) {
                        yearlyCalculatedConsumptionSource3 += eo.getYearlyconsumption();
                    } else if (eo.getEnergySource().equals(energysource4name)) {
                        yearlyCalculatedConsumptionSource4 += eo.getYearlyconsumption();
                    } else if (eo.getEnergySource().equals(energysource5name)) {
                        yearlyCalculatedConsumptionSource5 += eo.getYearlyconsumption();
                    }
                }
            }
            for (EquipmentPantry ep : equipmentRegister.getListPantry()) {
                if (Objects.nonNull(ds.getObject(ep.getEnergySource()))) {
                    AssetHelper ah = new AssetHelper();
                    ah.setName(ep.getName());
                    ah.setEnergysource(ds.getObject(ep.getEnergySource()).getName());
                    ah.setYearlyconsumption(ep.getYearlyconsumption());
                    ah.setShareoftotalconsumption(ep.getShareoftotalconsumption());
                    ah.setCo2emissions(ep.getCo2emissions());
                    ah.setCo2shareoftotal(ep.getCo2shareoftotal());
                    listAH.add(ah);

                    if (ep.getEnergySource().equals(energysource1name)) {
                        yearlyCalculatedConsumptionSource1 += ep.getYearlyconsumption();
                    } else if (ep.getEnergySource().equals(energysource2name)) {
                        yearlyCalculatedConsumptionSource2 += ep.getYearlyconsumption();
                    } else if (ep.getEnergySource().equals(energysource3name)) {
                        yearlyCalculatedConsumptionSource3 += ep.getYearlyconsumption();
                    } else if (ep.getEnergySource().equals(energysource4name)) {
                        yearlyCalculatedConsumptionSource4 += ep.getYearlyconsumption();
                    } else if (ep.getEnergySource().equals(energysource5name)) {
                        yearlyCalculatedConsumptionSource5 += ep.getYearlyconsumption();
                    }
                }
            }
            for (EquipmentProduction ep : equipmentRegister.getListProduction()) {
                if (Objects.nonNull(ds.getObject(ep.getEnergySource()))) {
                    AssetHelper ah = new AssetHelper();
                    ah.setName(ep.getName());
                    ah.setEnergysource(ds.getObject(ep.getEnergySource()).getName());
                    ah.setYearlyconsumption(ep.getYearlyconsumption());
                    ah.setShareoftotalconsumption(ep.getShareoftotalconsumption());
                    ah.setCo2emissions(ep.getCo2emissions());
                    ah.setCo2shareoftotal(ep.getCo2shareoftotal());
                    listAH.add(ah);

                    if (ep.getEnergySource().equals(energysource1name)) {
                        yearlyCalculatedConsumptionSource1 += ep.getYearlyconsumption();
                    } else if (ep.getEnergySource().equals(energysource2name)) {
                        yearlyCalculatedConsumptionSource2 += ep.getYearlyconsumption();
                    } else if (ep.getEnergySource().equals(energysource3name)) {
                        yearlyCalculatedConsumptionSource3 += ep.getYearlyconsumption();
                    } else if (ep.getEnergySource().equals(energysource4name)) {
                        yearlyCalculatedConsumptionSource4 += ep.getYearlyconsumption();
                    } else if (ep.getEnergySource().equals(energysource5name)) {
                        yearlyCalculatedConsumptionSource5 += ep.getYearlyconsumption();
                    }
                }
            }
            for (EquipmentVentilation ev : equipmentRegister.getListVentilation()) {
                if (Objects.nonNull(ds.getObject(ev.getEnergySource()))) {
                    AssetHelper ah = new AssetHelper();
                    ah.setName(ev.getName());
                    ah.setEnergysource(ds.getObject(ev.getEnergySource()).getName());
                    ah.setYearlyconsumption(ev.getYearlyconsumption());
                    ah.setShareoftotalconsumption(ev.getShareoftotalconsumption());
                    ah.setCo2emissions(ev.getCo2emissions());
                    ah.setCo2shareoftotal(ev.getCo2shareoftotal());
                    listAH.add(ah);

                    if (ev.getEnergySource().equals(energysource1name)) {
                        yearlyCalculatedConsumptionSource1 += ev.getYearlyconsumption();
                    } else if (ev.getEnergySource().equals(energysource2name)) {
                        yearlyCalculatedConsumptionSource2 += ev.getYearlyconsumption();
                    } else if (ev.getEnergySource().equals(energysource3name)) {
                        yearlyCalculatedConsumptionSource3 += ev.getYearlyconsumption();
                    } else if (ev.getEnergySource().equals(energysource4name)) {
                        yearlyCalculatedConsumptionSource4 += ev.getYearlyconsumption();
                    } else if (ev.getEnergySource().equals(energysource5name)) {
                        yearlyCalculatedConsumptionSource5 += ev.getYearlyconsumption();
                    }
                }
            }

            Table tablecaptionsForHeaderForAssetRegister;
            if (!lang.equals("")) {
                Translations t = new Translations();
                tablecaptionsForHeaderForAssetRegister = new Table(new TableColumn[]{
                        new TableColumn(t.getTranslatedKey(lang, "Asset")),
                        new TableColumn(t.getTranslatedKey(lang, "Energy Source")),
                        new TableColumn(t.getTranslatedKey(lang, "Energy Consumption<br>kWh")),
                        new TableColumn(t.getTranslatedKey(lang, "Share of total energysource consumption<br>%")),
                        new TableColumn(t.getTranslatedKey(lang, "Share of total consumption<br>%")),
                        new TableColumn(t.getTranslatedKey(lang, "CO2-emissions<br>kg")),
                        new TableColumn(t.getTranslatedKey(lang, "CO2-share of total<br>%"))});
            } else {
                tablecaptionsForHeaderForAssetRegister = new Table(new TableColumn[]{
                        new TableColumn("Asset"),
                        new TableColumn("Energy Source"),
                        new TableColumn("Energy Consumption<br>kWh"),
                        new TableColumn("Share of total energysource consumption<br>%"),
                        new TableColumn("Share of total consumption<br>%"),
                        new TableColumn("CO2-emissions<br>kg"),
                        new TableColumn("CO2-share of total<br>%")});
            }

            Table tablecaptionsAssetRegister = new Table(new TableColumn[]{new TableColumn("name"), new TableColumn("energysource"),
                    new TableColumn("yearlyconsumption"), new TableColumn("shareoftotalesconsumption"),
                    new TableColumn("shareoftotalconsumption"), new TableColumn("co2emissions"), new TableColumn("co2shareoftotal")});

            double totalenergyconsumptionlast1 = 0.0;
            double totalco2emissionslast1 = 0.0;
            if (energysource1) {
                for (double d : energyconsumptionlast1) {
                    totalenergyconsumptionlast1 += d;
                    totalco2emissionslast1 += d * listES.get(0).getCO2EmissionFactor();
                }
            }
            double totalenergyconsumptionlast2 = 0.0;
            double totalco2emissionslast2 = 0.0;
            if (energysource2) {

                for (double d : energyconsumptionlast2) {
                    totalenergyconsumptionlast2 += d;
                    totalco2emissionslast2 += d * listES.get(1).getCO2EmissionFactor();
                }
            }
            double totalenergyconsumptionlast3 = 0.0;
            double totalco2emissionslast3 = 0.0;
            if (energysource3) {

                for (double d : energyconsumptionlast3) {
                    totalenergyconsumptionlast3 += d;
                    totalco2emissionslast3 += d * listES.get(2).getCO2EmissionFactor();
                }
            }
            double totalenergyconsumptionlast4 = 0.0;
            double totalco2emissionslast4 = 0.0;
            if (energysource4) {

                for (double d : energyconsumptionlast4) {
                    totalenergyconsumptionlast4 += d;
                    totalco2emissionslast4 += d * listES.get(3).getCO2EmissionFactor();
                }
            }
            double totalenergyconsumptionlast5 = 0.0;
            double totalco2emissionslast5 = 0.0;
            if (energysource5) {

                for (double d : energyconsumptionlast5) {
                    totalenergyconsumptionlast5 += d;
                    totalco2emissionslast5 += d * listES.get(4).getCO2EmissionFactor();
                }
            }

            double overallco2emissions = totalco2emissionslast1 + totalco2emissionslast2 + totalco2emissionslast3
                    + totalco2emissionslast4 + totalco2emissionslast5;

            double overalltotalconsumption = totalenergyconsumptionlast1 + totalenergyconsumptionlast2 + totalenergyconsumptionlast3
                    + totalenergyconsumptionlast4 + totalenergyconsumptionlast5;

            for (AssetHelper ah : listAH) {
                if (energysource1) {
                    if (listES.get(0).getName().equals(ah.getEnergysource())) {
                        ah.setCo2emissions(ah.getYearlyconsumption() * listES.get(0).getCO2EmissionFactor());
                        ah.setShareoftotalesconsumption(divZeroFix(ah.getYearlyconsumption(), totalenergyconsumptionlast1) * 100);
                        ah.setShareoftotalconsumption(divZeroFix(ah.getYearlyconsumption(), overalltotalconsumption) * 100);
                    }
                }
                if (energysource2) {
                    if (listES.get(1).getName().equals(ah.getEnergysource())) {
                        ah.setCo2emissions(ah.getYearlyconsumption() * listES.get(1).getCO2EmissionFactor());
                        ah.setShareoftotalesconsumption(divZeroFix(ah.getYearlyconsumption(), totalenergyconsumptionlast2) * 100);
                        ah.setShareoftotalconsumption(divZeroFix(ah.getYearlyconsumption(), overalltotalconsumption) * 100);
                    }
                }
                if (energysource3) {
                    if (listES.get(2).getName().equals(ah.getEnergysource())) {
                        ah.setCo2emissions(ah.getYearlyconsumption() * listES.get(2).getCO2EmissionFactor());
                        ah.setShareoftotalesconsumption(divZeroFix(ah.getYearlyconsumption(), totalenergyconsumptionlast3) * 100);
                        ah.setShareoftotalconsumption(divZeroFix(ah.getYearlyconsumption(), overalltotalconsumption) * 100);
                    }
                }
                if (energysource4) {
                    if (listES.get(3).getName().equals(ah.getEnergysource())) {
                        ah.setCo2emissions(ah.getYearlyconsumption() * listES.get(3).getCO2EmissionFactor());
                        ah.setShareoftotalesconsumption(divZeroFix(ah.getYearlyconsumption(), totalenergyconsumptionlast4) * 100);
                        ah.setShareoftotalconsumption(divZeroFix(ah.getYearlyconsumption(), overalltotalconsumption) * 100);
                    }
                }
                if (energysource5) {
                    if (listES.get(4).getName().equals(ah.getEnergysource())) {
                        ah.setCo2emissions(ah.getYearlyconsumption() * listES.get(4).getCO2EmissionFactor());
                        ah.setShareoftotalesconsumption(divZeroFix(ah.getYearlyconsumption(), totalenergyconsumptionlast5) * 100);
                        ah.setShareoftotalconsumption(divZeroFix(ah.getYearlyconsumption(), overalltotalconsumption) * 100);
                    }
                }
            }

            for (AssetHelper ah : listAH) {
                ah.setCo2shareoftotal(divZeroFix(ah.getCo2emissions(), overallco2emissions) * 100);

            }

            List<AssetHelper> sumAssets = new ArrayList<>();

            for (EnergySource es : listES) {
                AssetHelper ahOfficial = new AssetHelper();
                if (!lang.equals("")) {
                    Translations t = new Translations();
                    ahOfficial.setName(t.getTranslatedKey(lang, "Sum ") + " " + es.getName());
                } else {
                    ahOfficial.setName("Sum " + es.getName());
                }
                ahOfficial.setYearlyconsumption(es.getEnergyConsumption(lastYear).getSum());
                ahOfficial.setCo2emissions(es.getEnergyConsumption(lastYear).getCo2emissions());
                sumAssets.add(ahOfficial);

                AssetHelper ah = new AssetHelper();
                if (!lang.equals("")) {
                    Translations t = new Translations();
                    ah.setName(t.getTranslatedKey(lang, "Sum captured ") + " " + es.getName());
                } else {
                    ah.setName("Sum captured " + es.getName());
                }
                for (AssetHelper ah2 : listAH) {
                    if (ah2.getEnergysource().equals(es.getName())) {
                        ah.setYearlyconsumption(ah.getYearlyconsumption() + ah2.getYearlyconsumption());
                        ah.setCo2emissions(ah.getCo2emissions() + ah2.getCo2emissions());
                    }
                }
                sumAssets.add(ah);

                AssetHelper ahDiff = new AssetHelper();
                if (!lang.equals("")) {
                    Translations t = new Translations();
                    ahDiff.setName(t.getTranslatedKey(lang, "Deviation ") + " " + es.getName());
                } else {
                    ahDiff.setName("Deviation " + es.getName());
                }
                ahDiff.setYearlyconsumption(ahOfficial.getYearlyconsumption() - ah.getYearlyconsumption());
                ahDiff.setCo2emissions(ahOfficial.getCo2emissions() - ah.getCo2emissions());
                ahDiff.setCo2shareoftotal((1 - divZeroFix(ah.getYearlyconsumption(), ahOfficial.getYearlyconsumption())) * 100);
                sumAssets.add(ahDiff);

                AssetHelper empty = new AssetHelper();
                sumAssets.add(empty);
            }

            Table tablecaptionsAssetSums = new Table(new TableColumn[]{new TableColumn("name"),
                    new TableColumn("yearlyconsumption"), new TableColumn("co2shareoftotal")});

            Table tablecaptionsForHeaderForAssetSums;
            if (!lang.equals("")) {
                Translations t = new Translations();
                tablecaptionsForHeaderForAssetSums = new Table(new TableColumn[]{
                        new TableColumn(t.getTranslatedKey(lang, "")),
                        new TableColumn(t.getTranslatedKey(lang, "Energy Consumption<br>kWh")),
                        new TableColumn(t.getTranslatedKey(lang, "Share missing<br>%"))});
            } else {
                tablecaptionsForHeaderForAssetSums = new Table(new TableColumn[]{
                        new TableColumn(""),
                        new TableColumn("Energy Consumption<br>kWh"),
                        new TableColumn("Share missing<br>%")});
            }

            root.put("tablecaptionsForHeaderForAssetRegister", tablecaptionsForHeaderForAssetRegister);
            root.put("tablecaptionsForHeaderForAssetSums", tablecaptionsForHeaderForAssetSums);
            root.put("tablecaptionsAssetRegister", tablecaptionsAssetRegister);
            root.put("tablecaptionsAssetSums", tablecaptionsAssetSums);
            root.put("assetregister", listAH);
            root.put("sumAssets", sumAssets);

            if (!lang.equals("")) {
                Translations t = new Translations();
                root.put("totalEnPIs", t.getTranslatedKey(lang, "Total EnPIs"));
                root.put("consumption", t.getTranslatedKey(lang, "Consumption"));
                root.put("assetRegisterFor", t.getTranslatedKey(lang, "Asset Register for "));
                root.put("janStr", t.getTranslatedKey(lang, "January"));
                root.put("febStr", t.getTranslatedKey(lang, "February"));
                root.put("marStr", t.getTranslatedKey(lang, "March"));
                root.put("aprStr", t.getTranslatedKey(lang, "April"));
                root.put("mayStr", t.getTranslatedKey(lang, "May"));
                root.put("junStr", t.getTranslatedKey(lang, "June"));
                root.put("julStr", t.getTranslatedKey(lang, "July"));
                root.put("augStr", t.getTranslatedKey(lang, "August"));
                root.put("sepStr", t.getTranslatedKey(lang, "September"));
                root.put("octStr", t.getTranslatedKey(lang, "October"));
                root.put("novStr", t.getTranslatedKey(lang, "November"));
                root.put("decStr", t.getTranslatedKey(lang, "December"));
            } else {
                root.put("totalEnPIs", "Total EnPIs");
                root.put("consumption", "Consumption");
                root.put("assetRegisterFor", "Asset Register for ");
                root.put("janStr", "January");
                root.put("febStr", "February");
                root.put("marStr", "March");
                root.put("aprStr", "April");
                root.put("mayStr", "May");
                root.put("junStr", "June");
                root.put("julStr", "July");
                root.put("augStr", "August");
                root.put("sepStr", "September");
                root.put("octStr", "October");
                root.put("novStr", "November");
                root.put("decStr", "December");
            }

            TemplateChooser tc = new TemplateChooser(root, "dashboard");

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
